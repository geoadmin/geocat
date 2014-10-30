package org.fao.geonet.geocat.services.reusable;

import com.google.common.collect.Maps;
import jeeves.server.context.ServiceContext;
import jeeves.server.local.LocalServiceRequest;
import org.apache.lucene.search.Query;
import org.fao.geonet.Constants;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.geocat.kernel.reusable.AbstractSharedObjectStrategyTest;
import org.fao.geonet.geocat.kernel.reusable.ContactsStrategy;
import org.fao.geonet.geocat.kernel.reusable.DeletedObjects;
import org.fao.geonet.geocat.kernel.reusable.FindMetadataReferences;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.geocat.RejectedSharedObjectRepository;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

import static org.fao.geonet.domain.Pair.read;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RejectTest extends AbstractSharedObjectTest {

    @Autowired
    private MetadataRepository metadataRepository;
    @Autowired
    private RejectedSharedObjectRepository rejectedSharedObjectRepository;

    @Test
    public void testExec() throws Exception {
        Reject reject = new Reject();
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);
        String uuid = UUID.randomUUID().toString();
        final Metadata subtemplate = addUserSubtemplate(uuid, false);
        String mdUUID = UUID.randomUUID().toString();
        int groupId = ReservedGroup.all.getId();
        String md = "<che:CHE_MD_Metadata xmlns:che=\"http://www.geocat.ch/2008/che\" xmlns:srv=\"http://www.isotc211.org/2005/srv\" " +
                    "xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:gmd=\"http://www" +
                    ".isotc211" +
                    ".org/2005/gmd\" xmlns:geonet=\"http://www.fao.org/geonetwork\" gco:isoType=\"gmd:MD_Metadata\">\n"
                    + "    <gmd:contact>" + subtemplate.getData() + "    </gmd:contact>\n"
                    + "</che:CHE_MD_Metadata>";

        InputStream mdStream = new ByteArrayInputStream(md.getBytes(Constants.CHARSET));
        int mdId = importMetadataXML(context, mdUUID, mdStream, MetadataType.METADATA, groupId, "REPLACE");

        final ContactsStrategy contactsStrategy = new ContactsStrategy(_applicationContext);
        final Query contactQuery = contactsStrategy.createFindMetadataQuery(contactsStrategy.getInvalidXlinkLuceneField(),
                subtemplate.getUuid(), false);
        final FindMetadataReferences deletedFindMetadataReferences = DeletedObjects.createFindMetadataReferences();

        AbstractSharedObjectStrategyTest.assertCorrectMetadataInLucene(_applicationContext, contactQuery, mdUUID);


        Element params = createParams(
                read("type", "contacts"),
                read("id", subtemplate.getUuid()),
                read("testing", "true"),
                read("msg", "rejecting"));

        ServiceContext mockContext = new ServiceContext("service", _applicationContext, Maps.<String, Object>newHashMap(), null) {
            @Override
            public void executeOnly(LocalServiceRequest request) throws Exception {
                request.getOutputStream().write(subtemplate.getData().getBytes(Constants.CHARSET));
            }

            @Override
            public Element execute(LocalServiceRequest request) throws Exception {
                this.executeOnly(request);
                return request.getResult();
            }
        };
        final Element result = reject.exec(params, mockContext);

        final Metadata updatedMd = metadataRepository.findOne(mdId);
        assertFalse(updatedMd.getData().contains("subtemplate?uuid=" + subtemplate.getUuid()));
        String deletedId = Xml.selectString(result, "idMap[oldId = '" + subtemplate.getUuid() + "']/newIds/id");
        assertTrue(updatedMd.getData().contains("deleted?id=" + deletedId));

        AbstractSharedObjectStrategyTest.assertCorrectMetadataInLucene(_applicationContext, contactQuery);
        final Query deletedQuery = deletedFindMetadataReferences.createFindMetadataQuery(DeletedObjects.getLuceneIndexField(), deletedId, false);
        AbstractSharedObjectStrategyTest.assertCorrectMetadataInLucene(_applicationContext, deletedQuery, mdUUID);

        rejectedSharedObjectRepository.exists(Integer.parseInt(deletedId));
    }
}