package org.fao.geonet.geocat.kernel.reusable;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.geocat.kernel.extent.ExtentManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.geocat.kernel.reusable.log.ReusableObjectLogger;
import org.jdom.Element;

public class ProcessParams
{
    public final ReusableObjectLogger logger;
    public final Element              elementToProcess;
    public final Element              metadata;
    public final String               baseURL;
    public final String               metadataId;
    public final boolean              addOnly;
    public final String 			  defaultLang;
	public final ServiceContext srvContext;

    public ProcessParams(ReusableObjectLogger logger, String metadataId, Element elementToProcess,
            Element metadata, boolean addOnly, String defaultLang,ServiceContext srvContext)
    {
        this.logger = logger;
        this.elementToProcess = elementToProcess;
        this.metadata = metadata;
        this.baseURL = Utils.mkBaseURL(srvContext.getBaseUrl(), srvContext.getBean(SettingManager.class));
        this.addOnly = addOnly;
        this.metadataId = metadataId;
        this.defaultLang = defaultLang;
        this.srvContext = srvContext;
    }

    public ProcessParams(String metadataId, Element elementToProcess, Element metadata,
            boolean addOnly,ServiceContext srvContext)
    {
        this(ReusableObjectLogger.THREAD_SAFE_LOGGER, metadataId, elementToProcess, metadata, addOnly,null,srvContext);
    }

    public ProcessParams(String metadataId, Element elementToProcess, Element metadata, ServiceContext srvContext)
    {
        this(ReusableObjectLogger.THREAD_SAFE_LOGGER, metadataId, elementToProcess, metadata,false,null,srvContext);
    }
    
    public ProcessParams updateElementToProcess(Element newElem) {
    	return new ProcessParams(logger, metadataId, newElem, metadata, addOnly, defaultLang,srvContext);
    }
}