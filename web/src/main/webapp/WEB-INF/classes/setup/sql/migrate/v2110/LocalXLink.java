package v2110;

import jeeves.xlink.XLink;
import org.fao.geonet.DatabaseMigrationTask;
import org.fao.geonet.csw.common.util.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jesse on 12/16/2014.
 */
public class LocalXLink implements DatabaseMigrationTask {
    private static final Pattern XLINK_PATTERN = Pattern.compile("http://?[^/]+/geonetwork/srv/\\w\\w\\w/(.+)");

    @Override
    public void update(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             PreparedStatement insertStatement = connection.prepareStatement("UPDATE metadata SET data=? WHERE id=?")
        ) {
            Set<Integer> processed = new HashSet<>();

            String sql = "select id, data from metadata where data ~* 'xlink:href\\s*=\\s*.http://[^/]+/geonetwork/srv/\\w\\w\\w/\\w'";
            try (ResultSet result = statement.executeQuery(sql)) {
                while (result.next()) {
                    int id = result.getInt("id");
                    if (!processed.contains(id)) {
                        processed.add(id);
                        String data = result.getString("data");
                        final String updatedMd = updateLinks(data);
                        insertStatement.setString(1, updatedMd);
                        insertStatement.setInt(2, id);
                        insertStatement.addBatch();
                    }
                }
            }

            insertStatement.executeBatch();
        } catch (JDOMException | IOException e) {
            throw new RuntimeException(e);
        }
        connection.commit();
    }

    private String updateLinks(String data) throws SQLException, IOException, JDOMException {
        final Element md = Xml.loadString(data, false);
        final Iterator descendants = md.getDescendants();
        while (descendants.hasNext()) {
            Object next = descendants.next();
            if (next instanceof Element) {
                Element element = (Element) next;
                String xlink = element.getAttributeValue(XLink.HREF, XLink.NAMESPACE_XLINK);

                if (xlink != null) {
                    final Matcher matcher = XLINK_PATTERN.matcher(xlink);
                    if (matcher.find() && (
                            xlink.contains("xml.extent.get") ||
                            xlink.contains("xml.user.get") ||
                            xlink.contains("xml.format.get") ||
                            xlink.contains("che.keyword.get")
                    )) {
                        String newXLink = XLink.LOCAL_PROTOCOL + matcher.group(1);
                        element.setAttribute(XLink.HREF, newXLink, XLink.NAMESPACE_XLINK);
                    }
                }
            }
        }

        return Xml.getString(md);
    }
}
