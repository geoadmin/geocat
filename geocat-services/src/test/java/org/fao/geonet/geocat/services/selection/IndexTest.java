package org.fao.geonet.geocat.services.selection;

import com.google.common.collect.Sets;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.kernel.mef.MEFLibIntegrationTest;
import org.jdom.Element;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class IndexTest extends AbstractCoreIntegrationTest {

    @Test
    public void testExec() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);
        final MEFLibIntegrationTest.ImportMetadata importMetadata = new MEFLibIntegrationTest.ImportMetadata(this, context);
        importMetadata.getMefFilesToLoad().add("mef2-example-2md.zip");
        List<String> mdIds = importMetadata.invoke().getMetadataIds();

        final Index index = new Index();

        Element exec = index.exec(createParams(), context);
        assertEquals("0", exec.getAttributeValue("numberIndexed"));

        final SelectionManager manager = SelectionManager.getManager(context.getUserSession());
        manager.addSelection(SelectionManager.SELECTION_METADATA, mdIds.get(0));

        exec = index.exec(createParams(), context);
        assertEquals("1", exec.getAttributeValue("numberIndexed"));

        manager.addAllSelection(SelectionManager.SELECTION_METADATA, Sets.newHashSet(mdIds));
        exec = index.exec(createParams(), context);
        assertEquals("" + mdIds.size(), exec.getAttributeValue("numberIndexed"));

        manager.close(SelectionManager.SELECTION_METADATA);

        exec = index.exec(createParams(), context);
        assertEquals("0", exec.getAttributeValue("numberIndexed"));
    }
}