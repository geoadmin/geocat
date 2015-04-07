package v300;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.fao.geonet.DatabaseMigrationTask;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GCO;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GMD;

/**
 * @author Jesse on 3/30/2015.
 */
@SuppressWarnings("unchecked")
public class RemoveDuplicateLocalizedTextEl implements DatabaseMigrationTask {
    private static final ArrayList<Namespace> NAMESPACES = Lists.newArrayList(GMD, GCO);
    public static final String LOCALE_ATT = "locale";
    public static final String GE = "#GE";
    public static final String DE = "#DE";

    public static boolean UPGRADE_RAN = false;

    @Override
    public void update(Connection connection) throws SQLException {
        try (PreparedStatement update = connection.prepareStatement("UPDATE metadata SET data=? WHERE id=?")
        ) {
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("SELECT data,id FROM metadata WHERE isharvested = 'n'")
            ) {
                int numInBatch = 0;
                while (resultSet.next()) {
                    final Element xml = Xml.loadString(resultSet.getString(1), false);
                    final int id = resultSet.getInt(2);
                    boolean changed = updateLocalizedString(xml);
                    changed |= removeEmptyGraphicOverview(xml);
                    if (changed) {
                        String updatedData = Xml.getString(xml);
                        update.setString(1, updatedData);
                        update.setInt(2, id);
                        update.addBatch();
                        numInBatch++;
                        if (numInBatch > 200) {
                            update.executeBatch();
                            numInBatch = 0;
                        }
                    }
                }
                update.executeBatch();
            } catch (java.sql.BatchUpdateException e) {
                System.out.println("-------------------------------  Error occurred removing duplicate localized strings  -------------------------------");
                e.printStackTrace();

                SQLException next = e.getNextException();
                while (next != null) {
                    System.err.println("-------------------------------  Next error   ---------------------------");
                    next.printStackTrace();
                }

                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new Error(e);
            }
        }

        UPGRADE_RAN = true;
    }

    private boolean removeEmptyGraphicOverview(Element xml) throws JDOMException {
        final List<Element> graphicOverview =
                (List<Element>) Xml.selectNodes(xml, "gmd:identificationInfo/*/gmd:graphicOverview", NAMESPACES);
        boolean changed = false;
        for (Element element : graphicOverview) {
            final List<?> text = Xml.selectNodes(element, "*//*[normalize-space(text()) != '']", NAMESPACES);
            if (text.isEmpty()) {
                changed = true;
                element.detach();
            }
        }
        return changed;
    }

    private boolean updateLocalizedString(Element xml) throws JDOMException {
        String mainLang = lookupMainLanguage(xml);
        boolean changed = false;
        final List<Element> freeText =
                Lists.newArrayList((Iterable<? extends Element>) Xml.selectNodes(xml, "*//gmd:PT_FreeText",  NAMESPACES));
        for (Element element : freeText) {
            final List<Element> localisedEls = (List<Element>) Xml.selectNodes(element, "*//gmd:LocalisedCharacterString", NAMESPACES);
            changed |= updateCharacterString(element, localisedEls, mainLang);
            Multimap<String, Element> langToEl = HashMultimap.create();
            if (localisedEls.isEmpty()) {
                changed = true;
                element.detach();
            } else {
                for (Element localisedEl : localisedEls) {
                    String locale = getLocale(localisedEl);
                    if (locale != null && locale.startsWith(GE)) {
                        locale = DE;
                    }
                    langToEl.put(locale, localisedEl);
                }

                for (Map.Entry<String, Collection<Element>> entry : langToEl.asMap().entrySet()) {
                    if (entry.getValue().size() > 1) {
                        Element best = null;
                        for (Element el : entry.getValue()) {
                            if (best == null ||
                                (best.getAttributeValue(LOCALE_ATT) != null && best.getAttributeValue(LOCALE_ATT).startsWith(GE)) ||
                                (best.getTextTrim().isEmpty() && !el.getTextTrim().isEmpty())) {
                                best = el;
                            }
                        }

                        if (best != null && GE.equals(getLocale(best))) {
                            changed = true;
                            best.setAttribute(LOCALE_ATT, DE);
                        }

                        for (Element el : entry.getValue()) {
                            String locale = getLocale(el);
                            el.setAttribute(LOCALE_ATT, locale);

                            if (el != best) {
                                changed = true;
                                el.detach();
                                List<Element> textGroups = Lists.newArrayList(element.getChildren("textGroup", GMD));
                                for (Element textGroup : textGroups) {
                                    if (textGroup.getChildren().isEmpty()) {
                                        textGroup.detach();
                                    }
                                }
                            }
                        }

                    } else if (entry.getValue().size() == 1) {
                        final Element el = entry.getValue().iterator().next();
                        if (GE.equals(el.getAttributeValue(LOCALE_ATT))) {
                            changed = true;
                            el.setAttribute(LOCALE_ATT, DE);
                        }
                    }
                }
            }
        }
        return changed;
    }

    private String getLocale(Element el) {
        String locale = el.getAttributeValue(LOCALE_ATT);
        locale = locale == null ? "" : locale.toUpperCase();
        return locale;
    }

    private String lookupMainLanguage(Element xml) throws JDOMException {
        String mainLang = Xml.selectString(xml, "gmd:language/gco:CharacterString", NAMESPACES);
        switch (mainLang) {
            case "eng":
                mainLang = "#EN";
                break;
            case "deu":
            case "ger":
                mainLang = "#DE";
                break;
            case "fre":
                mainLang = "#FR";
                break;
            case "ita":
                mainLang = "#IT";
                break;
            case "roh":
                mainLang = "#RM";
                break;
            default:
                mainLang = null;
        }
        return mainLang;
    }

    private boolean updateCharacterString(Element element, List<Element> localisedEls, String mainLang) {
        if (mainLang == null) {
            return false;
        }
        final Element characterString = element.getParentElement().getChild("CharacterString", GCO);
        if (characterString != null) {
            for (Element localisedEl : localisedEls) {
                if (mainLang.equals(localisedEl.getAttributeValue(LOCALE_ATT))) {
                    characterString.detach();
                    return true;
                }
            }

            element.addContent(new Element("textGroup", GMD).addContent(
                    new Element("LocalisedCharacterString", GMD).setAttribute(LOCALE_ATT, mainLang).setText(characterString.getText())
            ));
            characterString.detach();
            return true;
        }
        return false;
    }
}
