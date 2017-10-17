package org.fao.geonet.api.gm03;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Logger;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.exceptions.MetadataNotFoundEx;
import org.fao.geonet.exceptions.MissingParameterEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.IO;
import org.jdom.Element;
import org.jdom.input.DOMBuilder;
import org.jdom.output.DOMOutputter;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerConfigurationException;
import java.nio.file.Path;

public abstract class Gm03BaseService implements Service {
    protected Path xsl;
    private Path xsd;

    public void init(Path appPath, ServiceConfig params) throws Exception {
        final String xslTxt = params.getValue("xsl");
        xsl = IO.toPath(xslTxt);
        if (!xsl.isAbsolute())
            xsl = appPath.resolve(xslTxt);

        final String xsdTxt = params.getValue("xsd");
        xsd = IO.toPath(xsdTxt);
        if (!xsd.isAbsolute())
            xsd = appPath.resolve(xsdTxt);
    }

    public Element exec(Element params, ServiceContext context) throws Exception {
        boolean validate = Util.getParam(params, "validate", false);
        final Path xsdFile;
        if (validate) {
            xsdFile = xsd;
        } else {
            xsdFile = null;
        }

        UserSession session = context.getUserSession();

        //-----------------------------------------------------------------------
        //--- handle current tab

        Element elCurrTab = params.getChild(Params.CURRTAB);

        if (elCurrTab != null)
            session.setProperty(Geonet.Session.METADATA_SHOW, elCurrTab.getText());

        //-----------------------------------------------------------------------
        //--- check access

        DataManager dm = context.getBean(DataManager.class);

        // the metadata ID
        String id;

        // does the request contain a UUID ?
        try {
            String uuid = Util.getParam(params, Params.UUID);
            // lookup ID by UUID
            id = dm.getMetadataId(uuid);
        } catch (MissingParameterEx x) {
            // request does not contain UUID; use ID from request
            try {
                id = Util.getParam(params, Params.ID);
            }
            // request does not contain ID
            catch (MissingParameterEx xx) {
                // give up
                throw new Exception("Request must contain a UUID or an ID");
            }
        }

        Lib.resource.checkPrivilege(context, id, ReservedOperation.view);

        //-----------------------------------------------------------------------
        //--- get metadata

        Element elMd = dm.getMetadata(context, id, false, true, true);

        if (elMd == null)
            throw new MetadataNotFoundEx(id);

        Logger logger = context.getLogger();
        try {
            logger.info("1");
            DOMOutputter outputter = new DOMOutputter();
            logger.info("2");
            Document domIn = outputter.output(new org.jdom.Document(elMd));
            logger.info("3");

            ISO19139CHEtoGM03Base toGm03 = createConverter(xsdFile);
            logger.info("4");
            Document domOut = toGm03.convert(domIn);
            logger.info("5");

            DOMBuilder builder = new DOMBuilder();
            logger.info("6");
            return builder.build(domOut).getRootElement();
        } catch (RuntimeException e) {
            logger.error(e.toString());
            throw e;
        } catch (Exception e) {
            logger.error(e.toString());
            throw e;
        }
    }

    protected abstract ISO19139CHEtoGM03Base createConverter(Path xsdFile) throws SAXException, TransformerConfigurationException;
}
