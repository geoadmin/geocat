package org.fao.geonet.geocat.component;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.Constants;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.geocat.services.gm03.ISO19139CHEtoGM03;
import org.fao.geonet.kernel.mef.ExportFormat;
import org.fao.geonet.utils.Log;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.Collections;
import javax.xml.transform.stream.StreamSource;

/**
 * Export iso19139.che also as GM03.
 *
 * User: Jesse
 * Date: 11/8/13
 * Time: 3:50 PM
 */
@Component
public class Iso19139CheMefExport extends ExportFormat {
    @Override
    public Iterable<Pair<String, String>> getFormats(ServiceContext context, Metadata metadata) throws Exception {
        String schema = metadata.getDataInfo().getSchemaId();
        if (schema.equals("iso19139.che")) {
            try {
                Path appPath = context.getAppPath();
                ISO19139CHEtoGM03 togm03 = new ISO19139CHEtoGM03(null, appPath.resolve("xsl/conversion/import/ISO19139CHE-to-GM03.xsl"));
                final String metadataAsString = formatData(metadata, false, null);
                final StreamSource source = new StreamSource(new StringReader(metadataAsString));
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                togm03.convert(source, "TransformationTestSupport", out);
                return Collections.singleton(Pair.read("metadata-gm03_2_1.xml", new String(out.toByteArray(), Constants.ENCODING)));
            } catch (Exception e) {
                Log.error(Geonet.MEF, "Error exporting iso19139.che metadata to gm03: '" + metadata.getUuid() + "'", e);
                return Collections.singleton(Pair.read("metadata-gm03_2_1-error.txt", "Error occurred while exporting iso19139.che metadata to GM03: " + e.getMessage()));
            }
        }

        return Collections.emptySet();
    }
}
