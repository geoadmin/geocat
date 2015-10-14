package v300;

import com.google.common.collect.Lists;
import org.fao.geonet.DatabaseMigrationTask;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;

import java.nio.file.Path;
import java.sql.*;
import java.util.*;

import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GCO;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GMD;
import static org.fao.geonet.schema.iso19139che.ISO19139cheNamespaces.CHE;

/**
 * @author Fgravin on 10/15/2015.
 *
 * @description
 * This class is used as a batch during migration.
 * It takes all metadata from 2.x version that will be invalid in 3.0 version
 * because they don't have sub topic categories.
 * It updates thoses metadata and add the required sub categories, regarding to the
 * `INVALID_TOPICCAT_MAP` map object.
 */
@SuppressWarnings("unchecked")
public class UpdateTopicCategories implements DatabaseMigrationTask {

    private static final ArrayList<Namespace> NAMESPACES = Lists.newArrayList(GMD, GCO, CHE);

    private static final HashMap<String, String > INVALID_TOPICCAT_MAP = new HashMap<String, String>(){{
        put("imageryBaseMapsEarthCover","imageryBaseMapsEarthCover_BaseMaps");
        put("planningCadastre","planningCadastre_Planning");
        put("geoscientificInformation","geoscientificInformation_Geology");
        put("environment","environment_EnvironmentalProtection");
        put("utilitiesCommunication","utilitiesCommunication_Energy");
    }};

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
                    boolean changed = updateTopicCategories(xml);
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

    }

    /**
     * Just used for testing
     * @param path
     * @throws Exception
     */
    public final void test(final Path path) throws Exception{
        final Element xml = Xml.loadFile(path);

        boolean changed = updateTopicCategories(xml);

        if(changed) {
            XMLOutputter outp = new XMLOutputter();
            String s = outp.outputString(xml);
        }
    }

    private boolean updateTopicCategories(Element xml) throws JDOMException {
        boolean changed = false;

        final List<Element> identificationInfo =
                Lists.newArrayList((Iterable<? extends Element>) Xml.selectNodes(xml, "*//che:CHE_MD_DataIdentification",  NAMESPACES));

        final List<Element> topicat =
                Lists.newArrayList((Iterable<? extends Element>) Xml.selectNodes(xml, "*//gmd:MD_TopicCategoryCode",  NAMESPACES));

        final List<String> topicatStrings = Lists.newArrayList();

        for (Element element : topicat) {
            final String value = element.getText();
            topicatStrings.add(value);
        }

        for (Element element : topicat) {
            final String value = element.getText();
            if(INVALID_TOPICCAT_MAP.get(value) != null && topicatStrings.indexOf(value + "_") < 0) {
                Element e = new Element("topicCategory", GMD);
                Element ee = new Element("MD_TopicCategoryCode", GMD);
                e.addContent(ee);
                ee.setText(INVALID_TOPICCAT_MAP.get(value));
                identificationInfo.get(0).addContent(e);
                changed = true;
            }
        }
        return changed;
    }
}
