package org.fao.geonet.schema.iso19139che;

import com.google.common.collect.ImmutableSet;
import org.fao.geonet.schema.iso19139.ISO19139Namespaces;
import org.fao.geonet.schema.iso19139.ISO19139SchemaPlugin;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.xpath.XPath;

import java.util.List;

/**
 * Created by francois on 6/15/14.
 */
public class ISO19139cheSchemaPlugin
        extends ISO19139SchemaPlugin {
    public static final String IDENTIFIER = "iso19139che";

    private static ImmutableSet<Namespace> allNamespaces;

    static {
        allNamespaces = ImmutableSet.<Namespace>builder()
                .add(ISO19139Namespaces.GCO)
                .add(ISO19139Namespaces.GMD)
                .add(ISO19139Namespaces.SRV)
                .add(ISO19139cheNamespaces.CHE)
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Element> getTranslationForElement(Element element, String languageIdentifier) {
        List<Element> matches = super.getTranslationForElement(element, languageIdentifier);
        if (matches == null || matches.isEmpty()) {
            final String path = ".//che:LocalisedURL" +
                                "[@locale='#" + languageIdentifier + "' and ../local-name() == 'URLGroup']";
            try {
                XPath xpath = XPath.newInstance(path);
                matches = xpath.selectNodes(element);
                return matches;
            } catch (Exception e) {
                Log.debug(LOGGER_NAME, getIdentifier() + ": getTranslationForElement failed " +
                                       "on element " + Xml.getString(element) +
                                       " using XPath '" + path +
                                       "updatedLocalizedTextElement exception " + e.getMessage());
            }
        }
        return null;
    }

    /**
     *  Add a LocalisedCharacterString or localisedURL to an element. In ISO19139, the translation are
     *  stored gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString.
     *
     * <pre>
     * <gmd:title xsi:type="gmd:PT_FreeText_PropertyType">
     *    <gco:CharacterString>Template for Vector data in ISO19139 (multilingual)</gco:CharacterString>
     *    <gmd:PT_FreeText>
     *        <gmd:textGroup>
     *            <gmd:LocalisedCharacterString locale="#FRE">Modèle de données vectorielles en ISO19139.che (multilingue)</gmd:LocalisedCharacterString>
     *        </gmd:textGroup>
     * </pre>
     *
     * @param element
     * @param languageIdentifier
     * @param value
     */
    @Override
    public void addTranslationToElement(Element element, String languageIdentifier, String value) {
        final List<Element> translationForElement = getTranslationForElement(element, languageIdentifier);

        if (translationForElement != null && !translationForElement.isEmpty()) {
            if (translationForElement.get(0).getName().equals("LocalisedCharacterString")){
                super.addTranslationToElement(element, languageIdentifier, value);
            } else {
                // An ISO19139 element containing translation has an xsi:type attribute
                element.setAttribute("type", "che:PT_FreeURL_PropertyType",
                        Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance"));

                // Create a new translation for the language
                Element langElem = new Element("LocalisedURL", ISO19139cheNamespaces.CHE);
                langElem.setAttribute("locale", "#" + languageIdentifier);
                langElem.setText(value);
                Element textGroupElement = new Element("URLGroup", ISO19139cheNamespaces.CHE);
                textGroupElement.addContent(langElem);

                // Get the PT_FreeURL node where to insert the translation into
                Element freeTextElement = element.getChild("PT_FreeURL", ISO19139cheNamespaces.CHE);
                if (freeTextElement == null) {
                    freeTextElement = new Element("PT_FreeURL", ISO19139cheNamespaces.CHE);
                    element.addContent(freeTextElement);
                }
                freeTextElement.addContent(textGroupElement);
            }
        }
    }
}
