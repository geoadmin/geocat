package v2110;

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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static jeeves.xlink.XLink.HREF;
import static jeeves.xlink.XLink.LOCAL_PROTOCOL;
import static jeeves.xlink.XLink.NAMESPACE_XLINK;
import static org.fao.geonet.geocat.kernel.reusable.SharedObjectStrategy.LUCENE_EXTRA_NON_VALIDATED;
import static org.fao.geonet.geocat.kernel.reusable.SharedObjectStrategy.LUCENE_EXTRA_VALIDATED;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GCO;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GMD;
import static org.fao.geonet.schema.iso19139che.ISO19139cheNamespaces.CHE;

/**
 * @author Jesse on 10/31/2014.
 */
public class SharedObjects implements DatabaseMigrationTask {

    private final static Pattern ID_PATTERN = Pattern.compile(".*id=(\\d+).*");
    private final static Pattern ROLE_PATTERN = Pattern.compile(".*role=([^&]+).*");
    protected static final String PREPARED_STATEMENT_SQL = "INSERT INTO public.metadata(" +
                                                           "              id, uuid, schemaid, istemplate, isharvested, createdate, \n" +
                                                           "              changedate,  data, source, title, root, extra, owner, \n" +
                                                           "              rating, popularity, displayorder)\n" +
                                                           "    VALUES (" +
                                                           "              ?, ?, 'iso19139.che', 's', 'n', ?, ?, ?, ?, ?, ?, ?, " +
                                                           "              1, 0, 0, 0)";

    @Override
    public void update(Connection connection) throws SQLException {
        try {
            AtomicInteger idIndex;
            String source;
            try (Statement statement = connection.createStatement()) {
                idIndex = getMaxMetadataId(statement);
                source = getSourceId(statement);
            }

            Map<String, String> formatIdMap = migrateFormats(idIndex, source, connection);
            Map<String, String> contactIdMap = migrateContacts(idIndex, source, connection);

            updateMetadata(connection, formatIdMap, contactIdMap);

            try (Statement statement = connection.createStatement()) {
                statement.execute("DELETE FROM useraddress WHERE userid IN (SELECT id FROM users WHERE profile = 'Shared');");
                statement.execute("DELETE FROM email WHERE user_id IN (SELECT id FROM users WHERE profile = 'Shared');");
                statement.execute("DELETE FROM users where profile='Shared'");
                statement.execute("DROP TABLE Formats");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void updateMetadata(Connection conn, Map<String, String> formatIdMap, Map<String, String> contactIdMap) throws
            SQLException, IOException, JDOMException {
        try (
                PreparedStatement select = conn.prepareStatement("select id, data from metadata where data like ?");
                PreparedStatement update = conn.prepareStatement("UPDATE metadata SET data=? WHERE id=?")
        ) {
            Iterator<Map.Entry<String, String>> entries = formatIdMap.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, String> entry = entries.next();
                String pattern = "%xml.format.get?id=" + entry.getKey() + "%";
                select.setString(1, pattern);
                try (ResultSet results = select.executeQuery()) {
                    while (results.next()) {
                        int id = results.getInt("id");
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
                                    if (subtemplateUUID == null) {
                                        removeBrokenXLink(el);
                                    } else {
                                        el.setAttribute(HREF, LOCAL_PROTOCOL + "subtemplate?uuid=" + subtemplateUUID, NAMESPACE_XLINK);
                                    }
                                } else if (atValue != null && atValue.contains("xml.user.get?id=")) {
                                    String userId = extractId(atValue);
                                    String role = extractRole(atValue);
                                    final String subtemplateUUID = contactIdMap.get(userId);

                                    if (subtemplateUUID == null) {
                                        removeBrokenXLink(el);
                                    } else {
                                        String href = LOCAL_PROTOCOL + "subtemplate?uuid=" + subtemplateUUID;
                                        if (role != null && !role.trim().isEmpty()) {
                                            href += "&process=*//gmd:CI_RoleCode/@codeListValue~" + role;
                                        }
                                        el.setAttribute(HREF, href, NAMESPACE_XLINK);
                                    }
                                }
                            }
                        }

                        String updatedData = Xml.getString(md);
                        update.setString(1, updatedData);
                        update.setInt(2, id);
                        update.execute();
                    }
                    entries.remove();
                }
            }
        }
    }

    private void removeBrokenXLink(Element el) {
        el.removeAttribute(HREF, NAMESPACE_XLINK);
        el.removeAttribute(XLink.ROLE, NAMESPACE_XLINK);
        el.removeAttribute(XLink.SHOW, NAMESPACE_XLINK);
        el.removeAttribute(XLink.TITLE, NAMESPACE_XLINK);
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
           return "pointOfContact";
        }
        return matcher.group(1);
    }

    private Map<String, String> migrateContacts(AtomicInteger idIndex, String source, Connection conn) throws SQLException,
            IOException {
        Map<String, String> idMap = Maps.newHashMap();
        try (
                PreparedStatement subtemplateStatement = conn.prepareStatement(PREPARED_STATEMENT_SQL);
                Statement selectStatement = conn.createStatement();
                ResultSet contacts = selectStatement.executeQuery("SELECT u1.*, u2.validated AS parentValidated FROM Users u1 " +
                                                                  "LEFT OUTER JOIN Users u2 ON u1.parentinfo = u2.id " +
                                                                  "WHERE u1.profile = 'Shared'");
        ) {
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
                    String parentUUid = idMap.get(parentinfo);
                    if (parentUUid == null) {
                        parentUUid = UUID.randomUUID().toString();
                        idMap.put(parentinfo, parentUUid);
                    }
                    String parentHref =  LOCAL_PROTOCOL + "subtemplate?uuid=" + parentUUid;
                    final Element parentResponsibleParty = new Element("parentResponsibleParty", CHE).
                            setAttribute(HREF, parentHref, NAMESPACE_XLINK).
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
                String uuid = idMap.get(id);
                if (uuid == null) {
                    uuid = UUID.randomUUID().toString();
                }

                SharedObject obj = new SharedObject(id, contactEl, title, validated, "che:CHE_CI_ResponsibleParty", uuid);
                registerSubtemplate(idIndex, source, subtemplateStatement, obj);
                idMap.put(obj.id, uuid);
            }
            subtemplateStatement.executeBatch();
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

    public static Element loadInternalMultiLingualElem(String basicValue) throws IOException {

        final String xml = "<description>" + basicValue.replaceAll("(<\\w+>)\\s*(\\<!\\[CDATA\\[)*\\s*(.*?)\\s*(\\]\\]\\>)*(</\\w+>)",
                "$1<![CDATA[$3]]>$5") + "</description>";

        Log.debug(Geonet.GEONETWORK, "Parsing xml to get languages: \n" + xml);

        Element desc;
        try {
            desc = Xml.loadString(xml, false);
        } catch (JDOMException jdomParse) {
            try {
                String encoded = URLEncoder.encode(basicValue, "UTF-8");
                desc = Xml.loadString(String.format("<description><EN>%1$s</EN><DE>%1$s</DE><FR>%1$s</FR><IT>%1$s</IT></description>",
                        encoded), false);
            } catch (JDOMException e) {
                Element en = new Element("EN").setText("Error setting parsing text: " + basicValue);
                desc = new Element("description").addContent(en);
            }
        }
        return desc;
    }

    private String getSourceId(Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery("SELECT value FROM Settings WHERE name = 'system/site/siteId'")) {
            resultSet.next();
            return resultSet.getString("value");
        }
    }

    private AtomicInteger getMaxMetadataId(Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery("SELECT max(id) FROM metadata")) {
            resultSet.next();
            return new AtomicInteger(resultSet.getInt(1));
        }
    }

    private Map<String, String> migrateFormats(AtomicInteger idIndex, String source, Connection conn) throws SQLException {
        Map<String, String> idMap = Maps.newHashMap();
        try (
                PreparedStatement subtemplateStatement = conn.prepareStatement(PREPARED_STATEMENT_SQL);
                Statement selectStatement = conn.createStatement();
                ResultSet formats = selectStatement.executeQuery("SELECT * FROM Formats");
        ) {
            while (formats.next()) {
                String id = String.valueOf(formats.getInt("id"));
                String name = formats.getString("name");
                String validated = formats.getString("validated");

                Element formatEl = new Element("MD_Format");
                addCharacterString(formats, formatEl, "name", "name", GMD);
                addCharacterString(formats, formatEl, "version", "version", GMD);
                SharedObject obj = new SharedObject(id, formatEl, name, validated, "gmd:MD_Format");

                registerSubtemplate(idIndex, source, subtemplateStatement, obj);
                idMap.put(obj.id, obj.uuid);
            }
            subtemplateStatement.executeBatch();
        }
        return idMap;

    }

    private void registerSubtemplate(AtomicInteger idIndex, String source, PreparedStatement statement,
                                       SharedObject sharedObject) throws SQLException {
        int mdId = idIndex.incrementAndGet();
        String date = new ISODate().getDateAndTime();
        statement.setInt(1, mdId);
        statement.setString(2, sharedObject.uuid);
        statement.setString(3, date);
        statement.setString(4, date);
        statement.setString(5, sharedObject.getXml());
        statement.setString(6, source);
        statement.setString(7, sharedObject.name);
        statement.setString(8, sharedObject.root);
        statement.setString(9, sharedObject.validated);
        statement.addBatch();
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
        final String uuid;

        private SharedObject(String id, Element xml, String name, String validated, String root) {
            this(id, xml, name, validated, root, UUID.randomUUID().toString());
        }
        private SharedObject(String id, Element xml, String name, String validated, String root, String uuid) {
            this.id = id;
            this.xml = xml;
            this.name = name;
            this.validated = validated.equalsIgnoreCase("y") ? LUCENE_EXTRA_VALIDATED : LUCENE_EXTRA_NON_VALIDATED;
            this.root = root;
            this.uuid = uuid;
        }

        public String getXml() {
            return Xml.getString(this.xml);
        }
    }
}
