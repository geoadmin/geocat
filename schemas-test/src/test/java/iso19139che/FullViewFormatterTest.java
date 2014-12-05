package iso19139che;

import com.google.common.collect.Lists;

import java.io.File;
import java.util.List;

/**
 * @author Jesse on 10/17/2014.
 */
public class FullViewFormatterTest extends iso19139.FullViewFormatterTest {
    @Override
    protected List<String> excludes() {
        return Lists.newArrayList(
                "> che:CHE_MD_Metadata > gmd:identificationInfo > che:CHE_MD_DataIdentification > gmd:citation > gmd:CI_Citation > gmd:title > " +
                "gco:PT_FreeText > gco:textGroup > gmd:LocalisedCharacterString > Text"
        );
    }

    @Override
    protected File getTestMetadataFile() {
        final String mdFile = FullViewFormatterTest.class.getResource("/iso19139che/example.xml").getFile();
        return new File(mdFile);
    }
}
