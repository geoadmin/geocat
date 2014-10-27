package jeeves.server.dispatchers.guiservices;
import jeeves.server.context.ServiceContext;
import jeeves.utils.XmlFileCacher;
import org.fao.geonet.JeevesJCS;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;

public class XmlCacheManager {
    private static final String XML_FILE_CACHE_KEY = "XmlFile";
	Map<String, Map<String, XmlFileCacher>> eternalCaches = new HashMap<String, Map<String, XmlFileCacher>>();
    private Map<String, XmlFileCacher> getExternalCacheMap(boolean localized, String base, String file) {
        String key = localized+":"+base+":"+file;
        Map<String, XmlFileCacher> cacheMap = eternalCaches.get(key);
        if(cacheMap == null) {
            cacheMap = new HashMap<String, XmlFileCacher>(10);
            eternalCaches.put(key, cacheMap);
        }
        
        return cacheMap;
    }
    @SuppressWarnings("unchecked")
	private Map<String, XmlFileCacher> getVolatileCacheMap(boolean localized, String base, String file) {
    	try {
	    	JeevesJCS cache = JeevesJCS.getInstance(XML_FILE_CACHE_KEY);
	    	String key = localized+":"+base+":"+file;
	    	Map<String, XmlFileCacher> cacheMap = (Map<String, XmlFileCacher>) cache.get(key);
	    	if(cacheMap == null) {
	    		cacheMap = new HashMap<String, XmlFileCacher>(10);
				cache.put(key, cacheMap);
	    	}
	    	return cacheMap;
    	} catch (Exception e) {
    		Log.error(Log.JEEVES, "JeevesJCS cache not available, THIS IS NOT AN ERROR IF TESTING", e);
    		return getExternalCacheMap(localized, base, file);
    	}
    	
    }

    /**
     * Obtain the stings for the provided xml file.
     *
     * @param context
     * @param localized        if this xml is a localized file or is a normal xml file
     * @param base             the directory to the localization directory (often is loc).
     *                         If file is not localized then this is the directory that contains the xml file.
     * @param file             the name of the file to load
     * @param preferedLanguage the language to attempt to load if it exists
     * @param defaultLang      a fall back language
     * @param makeCopy         if false then xml is not cloned and MUST NOT BE MODIFIED!
     */
    public Element get(ServiceContext context, boolean localized, String base, String file, String preferedLanguage,
                                    String defaultLang, boolean makeCopy, boolean isEternal) throws JDOMException, IOException {

        
        String appPath = context.getAppPath();
        String xmlFilePath;

        boolean isBaseAbsolutePath = (new File(base)).isAbsolute();
        String rootPath = (isBaseAbsolutePath) ? base : appPath + base;

        if (localized) {
            xmlFilePath = rootPath + File.separator + preferedLanguage +File.separator + file;
        } else {
            xmlFilePath = rootPath + File.separator + file;
            if (!new File(xmlFilePath).exists()) {
                xmlFilePath = appPath + file;
            }
        }

        ServletContext servletContext = null;
        if(context.getServlet() != null) {
            servletContext = context.getServlet().getServletContext();
        }
        File xmlFile = new File(xmlFilePath);


        Element result;

        result = getFromCache(localized, base, file, preferedLanguage, defaultLang, isEternal, appPath, xmlFilePath, rootPath,
                servletContext, xmlFile);
        if (makeCopy) {
            result = (Element) result.clone();
             String name = xmlFile.getName();
             int lastIndexOfDot = name.lastIndexOf('.');
            if (lastIndexOfDot > 0) {
                name = name.substring(0,lastIndexOfDot);
            }
            return result.setName(name);
        } else {
            return result;
        }
    }

    private Element getFromCache(boolean localized, String base, String file, String preferedLanguage, String defaultLang,
                                 boolean isExternal, String appPath, String xmlFilePath, String rootPath, ServletContext servletContext,
                                 File xmlFile) throws JDOMException, IOException {
        Element result;
        synchronized (this) {
            Map<String, XmlFileCacher> cacheMap;

            if (isExternal) {
                cacheMap = getExternalCacheMap(localized, base, file);
            } else {
                cacheMap = getVolatileCacheMap(localized, base, file);
            }

        XmlFileCacher xmlCache = cacheMap.get(preferedLanguage);
        if (xmlCache == null){
            xmlCache = new XmlFileCacher(xmlFile,servletContext,appPath);
            cacheMap.put(preferedLanguage, xmlCache);
        }

        try {
                result = xmlCache.get();
        } catch (Exception e) {
                Log.error(Log.RESOURCES, "Error cloning the cached data.  Attempted to get: " + xmlFilePath + "but failed so falling " +
                                         "back to default language");

            Log.debug(Log.RESOURCES, "Error cloning the cached data.  Attempted to get: "+xmlFilePath+"but failed so falling back to default language", e);
            String xmlDefaultLangFilePath = rootPath + File.separator + defaultLang + File.separator + file;
            xmlCache = new XmlFileCacher(new File(xmlDefaultLangFilePath),servletContext, appPath);
            cacheMap.put(preferedLanguage, xmlCache);
                result = xmlCache.get();
        }
        }
        return result;
    }

}
