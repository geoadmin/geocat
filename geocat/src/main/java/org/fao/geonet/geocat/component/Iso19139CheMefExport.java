package org.fao.geonet.geocat.component;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.Constants;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.mef.ExportFormat;
import org.fao.geonet.geocat.services.gm03.ISO19139CHEtoGM03;
import org.springframework.stereotype.Component;

import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Collections;

/**
 * Export iso19139 also as GM03
 * User: Jesse
 * Date: 11/8/13
 * Time: 3:50 PM
 */
@Component
public class Iso19139CheMefExport extends ExportFormat {
    private static final String FS = File.separator;

    @Override
    public Iterable<Pair<String, String>> getFormats(ServiceContext context, Metadata metadata) throws Exception {
        String schema = metadata.getDataInfo().getSchemaId();
        if(schema.equals("iso19139.che")) {
            String appPath = context.getAppPath();
            ISO19139CHEtoGM03 togm03 = new ISO19139CHEtoGM03(null, appPath +FS+"xsl"+FS+"conversion"+FS+"import"+FS+"ISO19139CHE-to-GM03.xsl");
            final StreamSource source = new StreamSource(formatData(metadata, false, ""));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            togm03.convert(source, "TransformationTestSupport", out);
            return Collections.singleton(Pair.read("metadata-gm03_2.xml", new String(out.toByteArray(), Constants.ENCODING)));
        }

        return Collections.emptySet();
    }
}
