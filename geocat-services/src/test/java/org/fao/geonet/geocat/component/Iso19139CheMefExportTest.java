package org.fao.geonet.geocat.component;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.mef.MEFLibIntegrationTest.ImportMetadata;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertTrue;

public class Iso19139CheMefExportTest extends AbstractServiceIntegrationTest {
    @Autowired
    Iso19139CheMefExport mefExport;
    @Autowired
    MetadataRepository metadataRepository;

    @Test
    public void testGetFormats() throws Exception {
        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);
        final ImportMetadata importMetadata = new ImportMetadata(this, serviceContext);
        importMetadata.invoke();
        String mdId = importMetadata.getMetadataIds().get(0);
        Metadata md = this.metadataRepository.findOne(mdId);
        final Iterable<Pair<String, String>> formats = mefExport.getFormats(serviceContext, md);

        assertTrue (formats.iterator().hasNext());
    }
}