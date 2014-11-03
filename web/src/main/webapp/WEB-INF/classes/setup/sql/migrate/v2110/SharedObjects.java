package v2110;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jeeves.xlink.XLink;
import org.fao.geonet.DatabaseMigrationTask;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static jeeves.xlink.XLink.HREF;
import static jeeves.xlink.XLink.LOCAL_PROTOCOL;
import static jeeves.xlink.XLink.NAMESPACE_XLINK;
import static org.fao.geonet.geocat.kernel.reusable.ReplacementStrategy.LUCENE_EXTRA_NON_VALIDATED;
import static org.fao.geonet.geocat.kernel.reusable.ReplacementStrategy.LUCENE_EXTRA_VALIDATED;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GCO;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GMD;
import static org.fao.geonet.schema.iso19139che.ISO19139cheNamespaces.CHE;

/**
 * @author Jesse on 10/31/2014.
 */
public class SharedObjects implements DatabaseMigrationTask {

    private final static Pattern ID_PATTERN = Pattern.compile(".*id=(\\d+).*");
    private final static Pattern ROLE_PATTERN = Pattern.compile(".*role=([^&]+).*");
    @Override
    public void update(Statement statement) throws SQLException {
        try {
            AtomicInteger idIndex = getMaxMetadataId(statement);
            String source = getSourceId(statement);

            Map<String, String> formatIdMap = migrateFormats(idIndex, source, statement);
            Map<String, String> contactIdMap = migrateContacts(idIndex, source, statement);

            updateMetadata(statement, formatIdMap, contactIdMap);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void updateMetadata(Statement statement, Map<String, String> formatIdMap, Map<String, String> contactIdMap) throws
            SQLException, IOException, JDOMException {
        Iterator<Map.Entry<String, String>> entries = formatIdMap.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, String> entry = entries.next();
            String pattern = "'%xml.format.get?id=" + entry.getKey() + "%'";
            try (ResultSet results = statement.executeQuery("select id, data from metadata where data like "+pattern)) {
                while (results.next()) {
                    String id = results.getString("id");
                    String data = results.getString("data");

                    Element md = Xml.loadString(data, false);

                    final Iterator descendants = md.getDescendants();
                    while (descendants.hasNext()) {
                        Object node = descendants.next();
                        if (node instanceof Element) {
                            Element el = (Element) node;
                            final String atValue = el.getAttributeValue(HREF, NAMESPACE_XLINK);
                            if (atValue != null && atValue.contains("xml.format.get?id=")) {
                                String formatId = extractId(atValue);
                                final String subtemplateUUID = formatIdMap.get(formatId);
                                el.setAttribute(HREF, LOCAL_PROTOCOL + "subtemplate?uuid=" + subtemplateUUID, NAMESPACE_XLINK);
                            } else if (atValue != null && atValue.contains("xml.user.get?id=")) {
                                String userId = extractId(atValue);
                                String role = extractRole(atValue);
                                final String subtemplateUUID = contactIdMap.get(userId);
                                el.setAttribute(HREF, LOCAL_PROTOCOL + "subtemplate?uuid=" + subtemplateUUID +
                                                      "&process=*//gmd:CI_RoleCode/@codeListValue~" + role, NAMESPACE_XLINK);
                            }
                        }
                    }

                    String updatedData = Xml.getString(md);
                    statement.execute("UPDATE metadata SET data=" + updatedData + " WHERE id=" + id);
                }
                entries.remove();
            }
        }
    }

    private String extractId(String atValue) {
        final Matcher matcher = ID_PATTERN.matcher(atValue);
        if (!matcher.find()) {
            throw new Error(atValue + " does not match the pattern: " + ID_PATTERN);
        }
        return matcher.group(1);
    }

    private String extractRole(String atValue) {
        final Matcher matcher = ROLE_PATTERN.matcher(atValue);
        if (!matcher.find()) {
            throw new Error(atValue + " does not match the pattern: " + ROLE_PATTERN);
        }
        return matcher.group(1);
    }

    private Map<String, String> migrateContacts(AtomicInteger idIndex, String source, Statement statement) throws SQLException,
            IOException {
        List<SharedObject> objs = Lists.newArrayList();
        try (ResultSet contacts = statement.executeQuery("select u1.*, u2.validated as parentValidated from Users u1 " +
                                                         "LEFT OUTER JOIN Users u2 ON u1.parentinfo = u2.id " +
                                                         "where u1.profile = 'Shared'")) {
            while (contacts.next()) {
                String id = contacts.getString("id");
                Element contactEl = new Element("CHE_CI_ResponsibleParty", CHE);
                Element contactInfoEl = new Element("contactInfo", GMD);
                Element ciContactEl = new Element("CI_Contact", GMD);
                Element phoneEl = new Element("phone", GMD);
                Element ciTelephoneEl = new Element("CHE_CI_Telephone", CHE);
                Element addressEl = new Element("address", GMD);
                Element cheAddressEl = new Element("CHE_CI_Address", CHE);
                Element onlineResourceEl = new Element("onlineResource", CHE);
                Element ciOnlineResourceEl = new Element("CI_OnlineResource", CHE);

                addLocalizedEl(contacts, contactEl, "organisation", "organisationName", GMD);
                addLocalizedEl(contacts, contactEl, "positionname", "positionName", GMD);

                contactEl.addContent(
                        contactInfoEl.addContent(
                                ciContactEl.addContent(
                                        phoneEl.addContent(ciTelephoneEl))));

                addCharacterString(contacts, ciTelephoneEl, "phone", "voice", GMD);
                addCharacterString(contacts, ciTelephoneEl, "phone1", "voice", GMD);
                addCharacterString(contacts, ciTelephoneEl, "phone2", "voice", GMD);

                addCharacterString(contacts, ciTelephoneEl, "facsimile", "facsimile", GMD);
                addCharacterString(contacts, ciTelephoneEl, "facsimile1", "facsimile", GMD);
                addCharacterString(contacts, ciTelephoneEl, "facsimile2", "facsimile", GMD);

                addCharacterString(contacts, ciTelephoneEl, "directnumber", "directNumber", GMD);
                addCharacterString(contacts, ciTelephoneEl, "mobile", "mobile", GMD);

                ciContactEl.addContent(addressEl.addContent(cheAddressEl));

                addCharacterString(contacts, cheAddressEl, "city", "city", GMD);
                addCharacterString(contacts, cheAddressEl, "state", "administrativeArea", GMD);
                addCharacterString(contacts, cheAddressEl, "zip", "postalCode", GMD);
                addCharacterString(contacts, cheAddressEl, "country", "country", GMD);
                String email = addCharacterString(contacts, cheAddressEl, "email", "electronicMailAddress", GMD);
                addCharacterString(contacts, cheAddressEl, "email1", "electronicMailAddress", GMD);
                addCharacterString(contacts, cheAddressEl, "email2", "electronicMailAddress", GMD);
                addCharacterString(contacts, cheAddressEl, "streetname", "streetName", GMD);
                addCharacterString(contacts, cheAddressEl, "streetnumber", "streetNumber", GMD);
                addCharacterString(contacts, cheAddressEl, "address", "addressLine", GMD);
                addCharacterString(contacts, cheAddressEl, "postbox", "postBox", GMD);

                ciContactEl.addContent(onlineResourceEl.addContent(ciOnlineResourceEl));

                addLocalizedEl(contacts, ciOnlineResourceEl, "onlineresource", "linkage", GMD);
                addCharacterString(contacts, ciOnlineResourceEl, "postbox", "protocol", GMD); // text/html
                addLocalizedEl(contacts, ciOnlineResourceEl, "onlinename", "name", GMD);
                addLocalizedEl(contacts, ciOnlineResourceEl, "onlinedescription", "description", GMD);

                addCharacterString(contacts, ciContactEl, "hoursofservice", "hoursOfService", GMD);
                addLocalizedEl(contacts, ciContactEl, "contactinstructions", "contactInstructions", GMD);

                contactEl.addContent(
                        new Element("role", GMD).addContent(
                                new Element("CI_RoleCode", GMD).
                                        setAttribute("codeList", "http://www.isotc211.org/2005/resources/codeList.xml#CI_RoleCode").
                                        setAttribute("codeListValue", "pointOfContact")
                        )
                );
                String name = addCharacterString(contacts, contactEl, "name", "individualFirstName", GMD);
                String surname = addCharacterString(contacts, contactEl, "surname", "individualLastName", GMD);

                addLocalizedEl(contacts, ciContactEl, "orgacronym", "organisationAcronym", GMD);

                String parentinfo = contacts.getString("parentinfo");

                if (parentinfo != null && !parentinfo.trim().isEmpty()) {
                    final Element parentResponsibleParty = new Element("parentResponsibleParty", CHE).
                            setAttribute(HREF, parentinfo, NAMESPACE_XLINK).
                            setAttribute(XLink.SHOW, "embed", NAMESPACE_XLINK);

                    if (contacts.getString("parentValidated").equalsIgnoreCase("y")) {
                        parentResponsibleParty.setAttribute(XLink.ROLE, "embed", NAMESPACE_XLINK);
                    }

                    contactEl.addContent(parentResponsibleParty);
                }

                String validated = contacts.getString("validated");

                String emailInBrackets = "";
                if (email == null || !email.trim().isEmpty()) {
                    emailInBrackets = "(" + email + ")";
                }
                String title = name + " " + surname + emailInBrackets;

                objs.add(new SharedObject(id, contactEl, title, validated, "che:CHE_CI_ResponsibleParty"));
            }
        }
        Map<String, String> idMap = Maps.newHashMap();
        for (SharedObject obj : objs) {
            String uuid = registerSubtemplate(idIndex, source, statement, obj);
            idMap.put(obj.id, uuid);

        }
        return idMap;
    }



    private void addLocalizedEl(ResultSet contacts, Element contactEl,
                                String columnName, String elName,
                                Namespace ns) throws
            SQLException, IOException {
        final String value = contacts.getString(columnName);
        Element newEl = new Element(elName, ns);
        contactEl.addContent(newEl);

        if (value == null || value.trim().isEmpty()) {
            addMissingCharString(newEl);
        } else {
            final Element translations = loadInternalMultiLingualElem(value);

            addPtFreeText(newEl, translations.getChildText("EN"), "#EN");
            addPtFreeText(newEl, translations.getChildText("DE"), "#DE");
            addPtFreeText(newEl, translations.getChildText("FR"), "#EN");
            addPtFreeText(newEl, translations.getChildText("IT"), "#EN");
            addPtFreeText(newEl, translations.getChildText("RM"), "#EN");
        }
    }

    private void addPtFreeText(Element newEl, String translation, String locale) {
        if (translation != null && !translation.trim().isEmpty()) {
            newEl.addContent(new Element("PT_FreeText", GMD).addContent(
                    new Element("textGroup", GMD).addContent(
                            new Element("LocalisedCharacterString", GMD).setAttribute("locale", locale).setText(translation.trim())
                    )
            ));
        }
    }

    public static Element loadInternalMultiLingualElem(String basicValue) throws IOException{

        final String xml = "<description>" + basicValue.replaceAll("(<\\w+>)\\s*(\\<!\\[CDATA\\[)*\\s*(.*?)\\s*(\\]\\]\\>)*(</\\w+>)","$1<![CDATA[$3]]>$5") + "</description>";

        Log.debug(Geonet.GEONETWORK, "Parsing xml to get languages: \n" + xml);

        Element desc;
        try {
            desc = Xml.loadString(xml, false);
        } catch(JDOMException jdomParse) {
            try {
                String encoded = URLEncoder.encode(basicValue, "UTF-8");
                desc = Xml.loadString(String.format("<description><EN>%1$s</EN><DE>%1$s</DE><FR>%1$s</FR><IT>%1$s</IT></description>", encoded),false);
            } catch (JDOMException e) {
                Element en = new Element("EN").setText("Error setting parsing text: " + basicValue);
                desc = new Element("description").addContent(en);
            }
        }
        return desc;
    }

    private String getSourceId(Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery("select value from Settings where name = 'system/site/siteId'")) {
            resultSet.next();
            return resultSet.getString("value");
        }
    }

    private AtomicInteger getMaxMetadataId(Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery("select max(id) from metadata")) {
            resultSet.next();
            return new AtomicInteger(resultSet.getInt(1));
        }
    }

    private Map<String, String> migrateFormats(AtomicInteger idIndex, String source, Statement statement) throws SQLException {
        List<SharedObject> objs = Lists.newArrayList();
        try (ResultSet formats = statement.executeQuery("select * from Formats")) {
            while (formats.next()) {
                String id = String.valueOf(formats.getInt("id"));
                String name = formats.getString("name");
                String validated = formats.getString("validated");

                Element formatEl = new Element("MD_Format");
                addCharacterString(formats, formatEl, "name","name", GMD);
                addCharacterString(formats, formatEl, "version","version", GMD);
                objs.add(new SharedObject(id, formatEl, name, validated, "gmd:MD_Format"));

            }

        }
        Map<String, String> idMap = Maps.newHashMap();
        for (SharedObject obj : objs) {
            String uuid = registerSubtemplate(idIndex, source, statement, obj);
            idMap.put(obj.id, uuid);

        }
        return idMap;

    }

    private String registerSubtemplate(AtomicInteger idIndex, String source, Statement statement, SharedObject sharedObject) throws SQLException {
        String uuid = UUID.randomUUID().toString();
        int mdId = idIndex.incrementAndGet();
        String date = new ISODate().getDateAndTime();
        String values = String.format(
                "%s, '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', 1, 2, 0, 0, 0", mdId, uuid,
                "iso19139.che", "s", "n", date, date, sharedObject.getXml(), source, sharedObject.name, sharedObject.root,
                sharedObject.validated);

        statement.execute("INSERT INTO public.metadata(\n"
                          + "            id, uuid, schemaid, istemplate, isharvested, createdate, changedate, \n"
                          + "            data, source, title, root, extra, owner, groupowner, \n"
                          + "            rating, popularity, displayorder)\n"
                          + "    VALUES (" + values + ")");
        return uuid;
    }

    private String addCharacterString(ResultSet results, Element parent, String columnName, String elemName, Namespace ns) throws
            SQLException {

        final Element elem = new Element(elemName, ns);
        parent.addContent(elem);

        final String text = results.getString(columnName);
        if (text == null || text.trim().isEmpty()) {
            addMissingCharString(elem);
        } else {
            elem.addContent(new Element("CharacterString", GCO).setText(text));
        }
        return text;
    }

    private void addMissingCharString(Element elem) {
        elem.addContent(new Element("CharacterString", GCO).setAttribute("nilReason", "missing", GCO));
    }

    private static final class SharedObject {
        final String id;
        final Element xml;
        final String name;
        final String validated;
        final String root;

        private SharedObject(String id, Element xml, String name, String validated, String root) {
            this.id = id;
            this.xml = xml;
            this.name = name;
            this.validated = validated.equalsIgnoreCase("y") ? LUCENE_EXTRA_VALIDATED : LUCENE_EXTRA_NON_VALIDATED;
            this.root = root;
        }

        public String getXml() {
            return Xml.getString(this.xml);
        }
    }
}
