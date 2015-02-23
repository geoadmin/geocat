package org.fao.geonet.geocat.kernel.reusable;

import jeeves.server.context.ServiceContext;
import jeeves.xlink.Processor;
import jeeves.xlink.XLink;
import org.apache.jcs.access.exception.CacheException;
import org.apache.log4j.Level;
import org.fao.geonet.constants.Geocat;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.geocat.kernel.extent.ExtentManager;
import org.fao.geonet.geocat.kernel.reusable.log.Record;
import org.fao.geonet.geocat.kernel.reusable.log.ReusableObjectLogger;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.util.ElementFinder;
import org.fao.geonet.util.LangUtils;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.filter.Filter;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReusableObjManager {
    // The following constants are used in stylesheet and the log4J
    // configuration so make
    // sure they are updated if these are changed
    public static final String CONTACTS = "contacts";
    private static final String CONTACTS_PLACEHOLDER = "contactsPlaceholder";
    public static final String EXTENTS = "extents";
    private static final String EXTENTS_PLACEHOLDER = "extentsPlaceholder";
    public static final String FORMATS = "formats";
    private static final String FORMATS_PLACEHOLDER = "formatsPlaceholder";
    public static final String KEYWORDS = "keywords";
    private static final String KEYWORDS_PLACEHOLDER = "keywordsPlaceholder";

    private final Lock lock = new ReentrantLock();

    public static final String NON_VALID_ROLE = "http://www.geonetwork.org/non_valid_obj";
    @Autowired
    private GeonetworkDataDirectory dataDirectory;

    public Path getStyleSheet() {
        return dataDirectory.getWebappDir().resolve("xsl/reusable-objects-extractor.xsl");
    }
    public Path getAppPath() {
        return dataDirectory.getWebappDir();
    }

    public int process(ServiceContext context, Set<String> elements, DataManager dm, boolean sendEmail, boolean idIsUuid,
                       boolean ignoreErrors)
            throws Exception {
        try {
            // Note if this lock is removed the ReusableObjectsLogger must also
            // be made thread-safe notes on how to do that are in that class
            if (!lock.tryLock(5, TimeUnit.SECONDS)) {
                throw new IllegalArgumentException("ProcessReusableObject is locked");
            }

            int count = 0;
            ReusableObjectLogger logger = new ReusableObjectLogger();


            for (String uuid : elements) {
                try {
                    boolean changed = process(context, dm, uuid, logger, idIsUuid);
                    if (changed) {
                        count++;
                    }

                } catch (Exception e) {
                    StringWriter w = new StringWriter();
                    e.printStackTrace(new PrintWriter(w));
                    Log.debug("Reusable Objects", "Selection: " + uuid + " can not be looked up: " + w.toString());
                    if (!ignoreErrors) {
                        throw e;
                    }
                }
            }

            logger.sendEmail(context, sendEmail);
            return count;
        } finally {
            lock.unlock();
        }
    }

    private boolean process(ServiceContext context, DataManager dm, String uuid,
                            ReusableObjectLogger logger, boolean idIsUUID) throws Exception {
        // the metadata ID
        String id = uuidToId(dm, uuid, idIsUUID);


        Element metadata = dm.getMetadata(context, id, false, false, false);

        ProcessParams searchParams = new ProcessParams(logger, id, metadata, metadata, false, null, context);
        List<Element> process = process(searchParams);
        if (process != null) {
            Element changed = process.get(0);

            if (changed != null) {
                Processor.processXLink(changed, context);
                context.getBean(XmlSerializer.class).update(id, changed, new ISODate().toString(), false, null, context);
            }
        }
        return process != null;
    }

    public static String uuidToId(DataManager dm, String uuid, boolean idIsUUID) {
        String id = uuid;

        if (!idIsUUID) {
            // does the request contain a UUID ?
            try {
                // lookup ID by UUID
                id = dm.getMetadataId(uuid);
                if (id == null) {
                    id = uuid;
                }
            } catch (Exception x) {
                // the selection is not a uuid maybe try id?
                id = uuid;
            }
        }
        return id;
    }

    public List<Element> process(ProcessParams parameterObject) throws Exception {

        String metadataId = parameterObject.metadataId;
        if (metadataId == null) {
            metadataId = "anonymous";
        }

        Log.info(Geocat.Module.REUSABLE,
                "Beginning reusable object processing on metadata object id= " + metadataId);
        Element elementToProcess = parameterObject.elementToProcess;

        String defaultMetadataLang = parameterObject.defaultLang;
        if (defaultMetadataLang == null) {
            defaultMetadataLang = LangUtils.iso19139DefaultLang(parameterObject.metadata);
            if (defaultMetadataLang != null && defaultMetadataLang.length() > 2) {
                defaultMetadataLang = parameterObject.srvContext.getBean(IsoLanguagesMapper.class).iso639_2_to_iso639_1
                        (defaultMetadataLang, "en");
                defaultMetadataLang = defaultMetadataLang.substring(0, 2).toUpperCase();
            } else {
                defaultMetadataLang = "EN";
            }
        }

        if (defaultMetadataLang.equalsIgnoreCase("GE")) {
            defaultMetadataLang = "DE";
        }

        Element xml = Xml.transform(elementToProcess, getStyleSheet());

        boolean changed;
        Log.debug(Geocat.Module.REUSABLE, "Replace formats with xlinks");
        changed = replaceFormats(xml, defaultMetadataLang, parameterObject);
        Log.debug(Geocat.Module.REUSABLE, "Replace contacts with xlinks");
        changed |= replaceContacts(xml, defaultMetadataLang, parameterObject);
        Log.debug(Geocat.Module.REUSABLE, "Replace keywords with xlinks");
        changed |= replaceKeywords(xml, defaultMetadataLang, parameterObject);
        Log.debug(Geocat.Module.REUSABLE, "Replace extents with xlinks");
        changed |= replaceExtents(xml, defaultMetadataLang, parameterObject);

        Log.info(Geocat.Module.REUSABLE, "Finished processing on id=" + parameterObject.metadataId
                                         + ".  " + (changed ? "Metadata was modified" : "No change was made"));

        if (changed) {
            @SuppressWarnings("unchecked")
            List<Element> metadata = xml.getChild("metadata").getChildren();
            ArrayList<Element> results = new ArrayList<Element>(metadata);
            for (Element md : results) {
                md.detach();
                for (Object ns : elementToProcess.getAdditionalNamespaces()) {
                    md.addNamespaceDeclaration((Namespace) ns);
                }

            }
            return results;
        }

        return Collections.emptyList();
    }

    private boolean replaceKeywords(Element xml, String defaultMetadataLang, ProcessParams params) throws Exception {

        ReusableObjectLogger logger = params.logger;
        String baseURL = params.baseURL;
        ThesaurusManager thesaurusMan = params.srvContext.getBean(ThesaurusManager.class);
        final IsoLanguagesMapper isoLanguagesMapper = params.srvContext.getBean(IsoLanguagesMapper.class);
        final String language = params.srvContext.getLanguage();
        KeywordsStrategy strategy = new KeywordsStrategy(isoLanguagesMapper, thesaurusMan, getAppPath(), baseURL, language);
        return performReplace(xml, defaultMetadataLang, KEYWORDS_PLACEHOLDER, KEYWORDS, logger, strategy,
                params.addOnly, params.srvContext);
    }

    private boolean replaceFormats(Element xml, String defaultMetadataLang, ProcessParams params) throws Exception {
        ReusableObjectLogger logger = params.logger;

        FormatsStrategy strategy = new FormatsStrategy(params.srvContext.getApplicationContext());
        return performReplace(xml, defaultMetadataLang, FORMATS_PLACEHOLDER, FORMATS, logger, strategy,
                params.addOnly, params.srvContext);
    }

    private boolean replaceContacts(Element xml, String defaultMetadataLang, ProcessParams params) throws Exception {
        ReusableObjectLogger logger = params.logger;
        ContactsStrategy strategy = new ContactsStrategy(params.srvContext.getApplicationContext());
        return performReplace(xml, defaultMetadataLang, CONTACTS_PLACEHOLDER, CONTACTS, logger, strategy,
                params.addOnly, params.srvContext);
    }

    private boolean replaceExtents(Element xml, String defaultMetadataLang, ProcessParams params) throws Exception {

        ReusableObjectLogger logger = params.logger;
        String baseURL = params.baseURL;
        ExtentManager extentMan = params.srvContext.getBean(ExtentManager.class);

        ExtentsStrategy strategy = new ExtentsStrategy(getAppPath(), extentMan, null);

        @SuppressWarnings("unchecked")
        Iterator<Element> iter = xml.getChild(EXTENTS).getChildren().iterator();
        List<Content> originalElems = Utils.convertToList(iter, Content.class);

        for (Content extent : originalElems) {
            Element extentAsElem = (Element) extent;
            Element exExtent = extentAsElem.getChild("EX_Extent", Geonet.Namespaces.GMD);
            boolean needToProcessDescendants = exExtent != null && exExtent.getDescendants(new ElementFinder("EX_Extent",
                    Geonet.Namespaces.GMD, "*")).hasNext();
            if (needToProcessDescendants) {
                int index = extentAsElem.indexOf(exExtent);
                List<Element> changed = process(params.updateElementToProcess(exExtent));
                if (changed != null && !changed.isEmpty()) {
                    extentAsElem.setContent(index, changed);
                }
            }
        }

        return performReplace(xml, defaultMetadataLang, EXTENTS_PLACEHOLDER, EXTENTS, logger, strategy,
                params.addOnly, params.srvContext);
    }

    private boolean performReplace(Element xml, String defaultMetadataLang, String placeholderElemName,
                                   String originalElementName, ReusableObjectLogger logger, SharedObjectStrategy strategy, boolean addOnly,
                                   ServiceContext srvContext)
            throws Exception {

        HashSet<String> updatedElements = new HashSet<String>();
        Map<String, Element> currentXLinkElements = new HashMap<String, Element>();

        @SuppressWarnings("unchecked")
        Iterator<Content> iter = xml.getChild("metadata").getDescendants(new PlaceholderFilter(placeholderElemName));

        List<Element> placeholders = Utils.convertToList(iter, Element.class);

        @SuppressWarnings("unchecked")
        Iterator<Element> iter2 = xml.getChild(originalElementName).getChildren().iterator();
        Iterator<Content> originalElems = Utils.convertToList(iter2, Content.class).iterator();

        boolean changed = false;

        for (Element placeholder : placeholders) {
            Element originalElem = Utils.nextElement(originalElems);

            if (XLink.isXLink(originalElem)) {
                originalElem.detach();

                changed = updateXLinkAsRequired(defaultMetadataLang, strategy,
                        updatedElements, currentXLinkElements, changed,
                        placeholder, originalElem, srvContext, originalElementName, logger);
                continue;
            }
            if (originalElem != null) {
                changed |= replaceSingleElement(placeholder, originalElem, strategy, defaultMetadataLang, addOnly,
                        originalElementName, logger);
            }
        }

        return changed;
    }

    private boolean updateXLinkAsRequired(String defaultMetadataLang,
                                          SharedObjectStrategy strategy, HashSet<String> updatedElements,
                                          Map<String, Element> currentXLinkElements, boolean changed,
                                          Element placeholder, Element originalElem,
                                          ServiceContext srvContext, String originalElementName,
                                          ReusableObjectLogger logger) throws AssertionError, Exception {

        if (!isValidated(originalElem)) {
            String href = XLink.getHRef(originalElem);
            Element current = resolveXLink(currentXLinkElements, href, srvContext);

            if (current == null || originalElem.getChildren().isEmpty()) {
                if (current == null || current.getChildren().isEmpty()) {
                    updatePlaceholder(placeholder, originalElem);
                } else {
                    updatePlaceholder(placeholder, current);
                }
                return false;
            }

            boolean equals = Utils.equalElem((Element) originalElem.getChildren().get(0), current);
            if (current.getName().equalsIgnoreCase("error")) {
                Log.error(Geocat.Module.REUSABLE, "ERROR resolving shared object xlink: " + href);
            }
            if (!equals && !current.getName().equalsIgnoreCase("error")) {
                if (updatedElements.contains(href)) {
                    Log.error(Geocat.Module.REUSABLE, "The same xlinks was updated twice, the second xlink is being processed as if new" +
                                                      ". HREF=" + href);
                    originalElem.removeAttribute(XLink.HREF, XLink.NAMESPACE_XLINK);
                    originalElem.removeAttribute(XLink.ROLE, XLink.NAMESPACE_XLINK);
                    originalElem.removeAttribute(XLink.SHOW, XLink.NAMESPACE_XLINK);
                    originalElem.removeAttribute(XLink.TITLE, XLink.NAMESPACE_XLINK);
                    originalElem.removeAttribute(XLink.TYPE, XLink.NAMESPACE_XLINK);
                    replaceSingleElement(placeholder, originalElem, strategy, defaultMetadataLang, false, originalElementName, logger);
                } else {
                    updatedElements.add(href);
                    Processor.uncacheXLinkUri(XLink.getHRef(originalElem));

                    Collection<Element> newElements = strategy.updateObject(originalElem, defaultMetadataLang);
                    if (!newElements.isEmpty()) {
                        ArrayList<Element> toAdd = new ArrayList<Element>(newElements);
                        toAdd.add(0, originalElem);
                        updatePlaceholder(placeholder, toAdd);
                    } else {
                        updatePlaceholder(placeholder, originalElem);
                    }
                    changed = true;
                }
            } else {
                updatePlaceholder(placeholder, originalElem);
            }
        } else {
            updatePlaceholder(placeholder, originalElem);
        }
        return changed;
    }

    /**
     * Get the XLink.  It is the unchanged copy so one can detect if the same xlink is modified more than once
     */
    private Element resolveXLink(Map<String, Element> currentXLinkElements,
                                 String href, ServiceContext srvContext) throws IOException, JDOMException, CacheException {
        Element current;
        if (currentXLinkElements.containsKey(href)) {
            current = currentXLinkElements.get(href);
        } else {
            current = Processor.resolveXLink(href, srvContext);
            currentXLinkElements.put(href, current);
        }
        return current;
    }

    private boolean replaceSingleElement(Element placeholder, Element originalElem, SharedObjectStrategy strategy,
                                         String defaultMetadataLang, boolean addOnly, String originalElementName,
                                         ReusableObjectLogger logger) throws Exception {

        boolean updated = false;
        if (!addOnly) {
            updated = updatePlaceholder(placeholder, strategy.find(placeholder, originalElem, defaultMetadataLang));
            if (updated)
                Log.debug(Geocat.Module.REUSABLE, "An existing match was found for " + strategy);
        }
        if (!updated) {
            updated = updatePlaceholder(placeholder, strategy.add(placeholder, originalElem,
                    defaultMetadataLang));
            if (updated)
                Log.debug(Geocat.Module.REUSABLE, "A new reusable element was added for "
                                                  + strategy);
        }
        if (!updated) {
            updatePlaceholder(placeholder, originalElem);
            Log.debug(Geocat.Module.REUSABLE, strategy + " object was not modified");
        }

        if (updated) {
            logger.log(Level.DEBUG, Record.Type.lookup(originalElementName),
                    "Following object was replaced by xlink(s)" + Xml.getString(originalElem));
        }
        return updated;
    }

    private boolean updatePlaceholder(Element placeholder, Element elem) {
        return updatePlaceholder(placeholder, Collections.singleton(elem));
    }

    private boolean updatePlaceholder(Element placeholder, Collection<Element> xlinks) {
        return updatePlaceholder(placeholder, Pair.read(xlinks, !xlinks.isEmpty()));
    }

    private boolean updatePlaceholder(Element placeholder, Pair<Collection<Element>, Boolean> xlinks) {
        if (xlinks == null) {
            return false;
        }

        if (!Utils.isEmpty(xlinks.one())) {
            for (Element element : xlinks.one()) {
                element.detach();
            }
            Element parent = placeholder.getParentElement();
            int index = parent.indexOf(placeholder);
            if (xlinks.two()) {
                parent.setContent(index, xlinks.one());
            } else {
                parent.addContent(index, xlinks.one());
            }
            return xlinks.two();
        }
        return false;
    }

    private static final class PlaceholderFilter implements Filter {

        private static final long serialVersionUID = 1L;
        private final String elemName;

        public PlaceholderFilter(String elemName) {
            this.elemName = elemName;
        }

        public boolean matches(Object obj) {
            if (obj instanceof Element) {
                Element elem = (Element) obj;

                return elemName.equals(elem.getName());
            }
            return false;
        }

    }

    public Collection<Element> updateXlink(Element xlink, ProcessParams params) throws Exception {
        try {
            // Note if this lock is removed the ReusableObjectsLogger must also
            // be made thread-safe notes on how to do that are in that class
            if (!lock.tryLock(5, TimeUnit.SECONDS)) {
                throw new IllegalArgumentException("ProcessReusableObject is locked");
            }
            String baseUrl = params.baseURL;

            SharedObjectStrategy strategy;

            final String language = params.srvContext.getLanguage();
            if (xlink.getName().equals("contact") || xlink.getName().equals("pointOfContact")
                || xlink.getName().equals("distributorContact") || xlink.getName().equals("citedResponsibleParty") || xlink.getName()
                    .equals("parentResponsibleParty")) {

                strategy = new ContactsStrategy(params.srvContext.getApplicationContext());
            } else if (xlink.getName().equals("resourceFormat") || xlink.getName().equals("distributionFormat")) {
                strategy = new FormatsStrategy(params.srvContext.getApplicationContext());
            } else if (xlink.getName().equals("descriptiveKeywords")) {
                ThesaurusManager thesaurusManager = params.srvContext.getBean(ThesaurusManager.class);
                IsoLanguagesMapper isoLanguagesMapper = params.srvContext.getBean(IsoLanguagesMapper.class);
                strategy = new KeywordsStrategy(isoLanguagesMapper, thesaurusManager, getAppPath(), baseUrl, language);
            } else {
                ExtentManager extentManager = params.srvContext.getBean(ExtentManager.class);
                strategy = new ExtentsStrategy(getAppPath(), extentManager, language);
            }

            Log.info(Geocat.Module.REUSABLE, "Updating a " + strategy + " in metadata id="
                                             + params.metadataId);

            Processor.uncacheXLinkUri(xlink.getAttributeValue(XLink.HREF, XLink.NAMESPACE_XLINK));

            String metadataLang = LangUtils.iso19139DefaultLang(params.elementToProcess);
            if (metadataLang != null && metadataLang.length() > 1) {
                metadataLang = metadataLang.substring(0, 2).toUpperCase();
            } else {
                metadataLang = "EN";
            }
            Collection<Element> newElements = strategy.updateObject(xlink, metadataLang);
            Log.info(Geocat.Module.REUSABLE, "New elements were created as a result of update");
            Log.info(Geocat.Module.REUSABLE, "Done updating " + strategy + " in metadata id="
                                             + params.metadataId);

            return newElements;
        } finally {
            lock.unlock();
        }
    }


    /**
     * Create the shared object if it needs to be created.  currently only required for extents
     */
    public String createAsNeeded(String href, ServiceContext context) throws Exception {
        try {
            // Note if this lock is removed the ReusableObjectsLogger must also
            // be made thread-safe notes on how to do that are in that class
            if (!lock.tryLock(5, TimeUnit.SECONDS)) {
                throw new IllegalArgumentException("ProcessReusableObject is locked");
            }
            ReusableTypes type = hrefToReusableType(href);

            return Utils.strategy(type, context).createAsNeeded(href, context.getUserSession());
        } finally {
            lock.unlock();
        }
    }


    public boolean isValidated(String href, ServiceContext context) throws Exception {
        try {
            // Note if this lock is removed the ReusableObjectsLogger must also
            // be made thread-safe notes on how to do that are in that class
            if (!lock.tryLock(5, TimeUnit.SECONDS)) {
                throw new IllegalArgumentException("ProcessReusableObject is locked");
            }
            ReusableTypes type = hrefToReusableType(href);

            return Utils.strategy(type, context).isValidated(
                    href);
        } finally {
            lock.unlock();
        }
    }

    private ReusableTypes hrefToReusableType(String href) {
        ReusableTypes type;
        if (href.contains("che.keyword.get") || href.contains("xml.keyword.get")) {
            type = ReusableTypes.keywords;
        } else if (href.contains("xml.user.get")) {
            type = ReusableTypes.contacts;
        } else if (href.contains("xml.format.get")) {
            type = ReusableTypes.formats;
        } else if (href.contains("xml.extent.get")) {
            type = ReusableTypes.extents;
        } else {
            throw new IllegalArgumentException(href + " is not recognized as a shared object xlink");
        }
        return type;
    }


    public static boolean isValidated(Element xlinkParent) {
        String attributeValue = xlinkParent.getAttributeValue(XLink.ROLE, XLink.NAMESPACE_XLINK);
        return !NON_VALID_ROLE.equals(attributeValue);
    }

}
