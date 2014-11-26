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
import java.util.ArrayList;
import java.util.Collection;

/**
 * Update a reusable object by passing the xml
 */
public class Update implements Service {
    public void init(Path appPath, ServiceConfig params) throws Exception {

    }

    public Element exec(Element params, ServiceContext context) throws Exception {
        String defaultLang = Util.getParam(params, "defaultLang", "EN").trim();
        String xmlString = Util.getChild(params, "xml").getText();

        Element xml = Xml.loadString(xmlString, false);
        Element wrapped = new Element("wrapped").addContent(xml);

        ProcessParams processParams = new ProcessParams(ReusableObjectLogger.THREAD_SAFE_LOGGER, null, xml, wrapped, false,
                defaultLang, context);
        Collection<Element> newElements = context.getBean(ReusableObjManager.class).updateXlink(xml, processParams);

        ArrayList<Element> updated = new ArrayList<Element>(newElements);
        updated.add(0, xml);
        xml.detach();

        return new Element("updated").addContent(updated);
    }
}
