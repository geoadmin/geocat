package org.fao.geonet.geocat;

import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.exceptions.JeevesException;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.services.metadata.format.Format;
import org.fao.geonet.services.metadata.format.FormatterWidth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.ServletWebRequest;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

@Controller
public class TestPdfExportAllMd {
    @Autowired
    ServiceManager serviceManager;
    @RequestMapping(value="/{lang}/pdf.export.all",produces = "text/plain")
    @ResponseBody
    public String exec(@PathVariable String lang, HttpServletRequest request) throws Exception {
        ServiceContext context = serviceManager.createServiceContext("pdf.export.all", lang, request);

        Format format = context.getBean(Format.class);
        MetadataRepository repo = context.getBean(MetadataRepository.class);
        List<Integer> ids = repo.findAllIdsBy(MetadataSpecs.hasType(MetadataType.METADATA));
        StringBuilder response = new StringBuilder("Failures: ");
        long lastPrintProgress = System.currentTimeMillis();
        long lastFlushResults = lastPrintProgress;
        int i = 0;
        for (Integer id : ids) {
            if (repo.exists(id)) {
                i++;
                if (System.currentTimeMillis() - lastPrintProgress > 30000) {
                    System.out.println("\n\n==============\n" + i + " / " + ids.size() + " \n===================\n");
                    lastPrintProgress = System.currentTimeMillis();
                }
                if (System.currentTimeMillis() - lastFlushResults > (60000 * 10)) {
                    Files.write(Paths.get("/tmp/PDFExportReport.txt"), response.toString().getBytes());
                    lastFlushResults = System.currentTimeMillis();
                }
                for (String l : new String[]{"eng", "ger", "fra", "ita"}) {
                    try {
                        format.exec(l, "pdf", id.toString(), "full_view", "y", true, FormatterWidth._100, new ServletWebRequest(new MockHttpServletRequest(), new MockHttpServletResponse()));

                    } catch (Throwable t) {
                        response.append("\n - ").append(l).append(": ").append(id).append(" - ").append(t.getMessage());
                    }
                }
            }
        }
        Files.write(Paths.get("/tmp/PDFExportReport.txt"), response.toString().getBytes());
        return response.toString();
    }

    private void throw404() throws JeevesException {
        throw new JeevesException("Backup file does not yet exist", null) {
            private static final long serialVersionUID = 1L;

            {
                this.code = 404;
                this.id = "NoBackup";
            }
        };
    }

}
