package org.fao.geonet.geocat.services.reusable;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Util;
import org.fao.geonet.geocat.kernel.reusable.ProcessParams;
import org.fao.geonet.geocat.kernel.reusable.ReusableObjManager;
import org.fao.geonet.geocat.kernel.reusable.log.ReusableObjectLogger;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.List;

/**
 * Process xml and create shared objects from it
 */
public class Process implements Service
{

    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    public Element exec(Element params, ServiceContext context) throws Exception {

        boolean addOnly = Boolean.parseBoolean(Util.getParam(params, "addOnly", "false").trim());
        String defaultLang = Util.getParam(params,"defaultLang","EN").trim();
        String xmlString = Util.getChild(params, "xml").getText();

        Element xml = Xml.loadString(xmlString, false);
        Element wrapped = new Element("wrapped").addContent(xml);

        ProcessParams processParams = new ProcessParams(ReusableObjectLogger.THREAD_SAFE_LOGGER, null, xml, wrapped,addOnly,defaultLang,context);
        List<Element> updated = context.getBean(ReusableObjManager.class).process(processParams);

        return new Element("updated").addContent(updated);
    }
}
