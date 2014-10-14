package org.fao.geonet.geocat.services.reusable;

import jeeves.server.context.ServiceContext;
import org.apache.lucene.document.Document;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataDataInfo;
import org.fao.geonet.domain.MetadataSourceInfo;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.geocat.kernel.reusable.ContactsStrategy;
import org.fao.geonet.geocat.kernel.reusable.ReplacementStrategy;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.schema.iso19139.ISO19139Namespaces;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;

/**
 * @author Jesse on 10/7/2014.
 */
public class AbstractSharedObjectTest extends AbstractServiceIntegrationTest {

    protected static final String SHARED_USER_XML = "shared-user.xml";
    protected static final String SHARED_FORMAT_XML = "shared-format.xml";
    @Autowired
    private MetadataRepository metadataRepository;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private MetadataCategoryRepository categoryRepository;

    public Metadata addFormatSubtemplate(String uuid, boolean validated) throws Exception {
        return addSubTemplate(uuid, validated, SHARED_FORMAT_XML, "gmd:MD_Format");
    }

    /**
     * Create and add a new shared user to system and index the new user.
     * @param uuid
     * @param validated
     * @param excludeLangs
     * @return
     * @throws Exception
     */
    public Metadata addUserSubtemplate(String uuid, boolean validated, String... excludeLangs) throws Exception {
        return addSubTemplate(uuid, validated, SHARED_USER_XML, ContactsStrategy.LUCENE_ROOT_RESPONSIBLE_PARTY, excludeLangs);
    }

    /**
     *
     * @param uuid sub-template uuid (will be in metadata table)
     * @param validated if shared object is validated
     * @param xmlFileName the name of the file containing the shared object
     * @param root the root name to assign to the metadata object
     * @param excludeLangs the 2 letter language codes to remove from the object so that the object doesn't have all languages
     * @return
     * @throws Exception
     */
    private Metadata addSubTemplate(String uuid, boolean validated, String xmlFileName, String root, String... excludeLangs) throws Exception {
        ServiceContext context = createServiceContext();
        context.setAsThreadLocal();

        Element sharedObjTmp = Xml.loadFile(AbstractSharedObjectTest.class.getResource(xmlFileName));

        for (String code : excludeLangs) {
            Xml.selectNodes(sharedObjTmp, "*//gmd:LocalizedString[@locale='#" + code.toUpperCase() + "']", Arrays.asList(ISO19139Namespaces.GMD));
        }

        Metadata metadata = new Metadata().setUuid(uuid);
        metadata.setData(Xml.getString(sharedObjTmp).replace("{{uuid}}", uuid));
        final String categoryName = validated ? ReplacementStrategy.LUCENE_EXTRA_VALIDATED :
                ReplacementStrategy.LUCENE_EXTRA_NON_VALIDATED;
        MetadataDataInfo dataInfo = new MetadataDataInfo().
                setSchemaId("iso19139.che").
                setRoot(root).
                setDisplayOrder(0).
                setType(MetadataType.SUB_TEMPLATE).
                setExtra(categoryName);
        metadata.setDataInfo(dataInfo);
        MetadataSourceInfo sourceInfo = new MetadataSourceInfo().setSourceId(getSourceId());
        metadata.setSourceInfo(sourceInfo);

        final Metadata save = metadataRepository.save(metadata);
        this.dataManager.indexMetadata(String.valueOf(save.getId()), true, false, false, false);
        return save;
    }
    protected void assertField(Document document, String fieldName, String... expectedValues) {
        final String[] values = document.getValues(fieldName.toLowerCase());
        Arrays.sort(expectedValues);
        Arrays.sort(values);
        assertArrayEquals("For field '" + fieldName + "'\nExpected: " + Arrays.toString(expectedValues) + "\nActual:   " +
                          Arrays.toString(values), expectedValues, values);
    }


    private String getSourceId() {
        return "source";
    }
}
