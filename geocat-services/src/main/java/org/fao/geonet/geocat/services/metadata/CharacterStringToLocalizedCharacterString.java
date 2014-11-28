package org.fao.geonet.geocat.services.metadata;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Util;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import java.io.File;
import java.nio.file.Path;

public class CharacterStringToLocalizedCharacterString implements Service {
    private static final String SEP = File.separator;

    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    public Element exec(Element params, ServiceContext context) throws Exception {
        Path xsl = context.getAppPath().resolve("xsl/characterstring-to-localisedcharacterstring.xsl");

        String id = Util.getParam(params, "id", null);
        String uuid = Util.getParam(params, "uuid", null);

        DataManager dataMan = context.getBean(DataManager.class);

        if (id == null && uuid != null) {
            id = dataMan.getMetadataId(uuid);
        }

        if (id != null) {
            Element md = dataMan.getMetadata(context, id, false, false, true);
            Xml.transform(md, xsl);

            boolean validate = false;
            boolean updatefixedInfo = false;
            boolean index = true;
            String changeDate = null;
            boolean updateDateStamp = false;
            dataMan.updateMetadata(context, id, md, validate, updatefixedInfo, index, context.getLanguage(), changeDate,
                    updateDateStamp, true);
            return new Element("response").setAttribute("success", "1");
        } else {
            return new Element("response").setAttribute("processed", "0");
        }
    }

}
