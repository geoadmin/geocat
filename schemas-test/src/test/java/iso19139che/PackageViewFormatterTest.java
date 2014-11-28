package iso19139che;

import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.repository.IsoLanguageRepository;
import org.fao.geonet.services.metadata.format.AbstractFormatterTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Jesse on 10/17/2014.
 */
public class PackageViewFormatterTest extends AbstractFormatterTest {

    @Autowired
    private IsoLanguagesMapper mapper;
    @Autowired
    private IsoLanguageRepository langRepo;
    @Autowired
    private SchemaManager manager;

    @Test
    @SuppressWarnings("unchecked")
    public void testBasicFormat() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("html", "true");

        final Path iso19139Dir = manager.getSchemaDir("iso19139.che").resolve("formatter/package");
        try (DirectoryStream<Path> packages = Files.newDirectoryStream(iso19139Dir)) {
            for (Path aPackage : packages) {

                // just check that the formatter works

                String formatterId = iso19139Dir.getFileName() + "/" + aPackage.getFileName();
                final MockHttpServletResponse response = new MockHttpServletResponse();
                formatService.exec("eng", "html", "" + id, null, formatterId, "true", false, request, response);
                final String view = response.getContentAsString();

                // for now the fact that there was no error is good enough
            }
        }


    }

    @Override
    protected File getTestMetadataFile() {
        final String mdFile = PackageViewFormatterTest.class.getResource("/iso19139che/example.xml").getFile();
        return new File(mdFile);
    }
}
