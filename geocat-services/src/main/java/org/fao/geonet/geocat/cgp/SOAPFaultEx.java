package org.fao.geonet.geocat.cgp;

import org.fao.geonet.exceptions.JeevesClientEx;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

/**
 * SOAP Fault wrapper.
 * <p/>
 * Basic version for now. Full Fault Element string will be printed in log
 */
public class SOAPFaultEx extends JeevesClientEx {
    private static final long serialVersionUID = 1L;

    public SOAPFaultEx(Element faultElm) {
        super(Xml.getString(faultElm), faultElm);
        id = "soap-fault";
    }
}
