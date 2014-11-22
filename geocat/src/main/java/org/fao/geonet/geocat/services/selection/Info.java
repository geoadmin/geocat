package org.fao.geonet.geocat.services.selection;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.resources.Resources;
import org.fao.geonet.utils.IO;
import org.jdom.Element;

import java.nio.file.Files;
import java.nio.file.Path;

public class Info implements Service {

    private static final String EMAIL_TEMPLATES = "emailTemplates.xml";
    private Path appPath;

    @Override
    public void init(Path appPath, ServiceConfig params) throws Exception {
        this.appPath = appPath;
    }

    @Override
    public Element exec(Element params, ServiceContext context) throws Exception {
        synchronized (this) {
            Path templates = Resources.locateResourcesDir(context).resolve(EMAIL_TEMPLATES);
            if (!Files.exists(templates)) {
                Path base = appPath.resolve("resources").resolve(EMAIL_TEMPLATES);
                IO.copyDirectoryOrFile(base, templates, false);
            }
        }
        return context.getXmlCacheManager().get(context, false, Resources.locateResourcesDir(context), EMAIL_TEMPLATES, "eng",
                "eng", true);
    }

}
