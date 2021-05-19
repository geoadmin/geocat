package org.fao.geonet.schema.iso19139che;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.index.es.EsRestClient;
import org.fao.geonet.kernel.schema.AssociatedResource;
import org.fao.geonet.kernel.schema.AssociatedResourcesSchemaPlugin;
import org.fao.geonet.kernel.schema.ExportablePlugin;
import org.fao.geonet.kernel.schema.ISOPlugin;
import org.fao.geonet.kernel.schema.LinkAwareSchemaPlugin;
import org.fao.geonet.kernel.schema.LinkPatternStreamer.ILinkBuilder;
import org.fao.geonet.kernel.schema.LinkPatternStreamer.RawLinkPatternStreamer;
import org.fao.geonet.kernel.schema.MultilingualSchemaPlugin;
import org.fao.geonet.schema.iso19139.ISO19139Namespaces;
import org.fao.geonet.schema.iso19139.ISO19139SchemaPlugin;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;
import org.jdom.xpath.XPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.*;

/**
 * Created by francois on 6/15/14.
 */
public class ISO19139cheSchemaPlugin
    extends org.fao.geonet.kernel.schema.SchemaPlugin
    implements
    AssociatedResourcesSchemaPlugin,
    MultilingualSchemaPlugin,
    ExportablePlugin,
    ISOPlugin,
    LinkAwareSchemaPlugin {
    public static final String IDENTIFIER = "iso19139.che";

    public static ImmutableSet<Namespace> allNamespaces;

    private static Map<String, Namespace> allTypenames;

    private static Map<String, String> allExportFormats;

    private static final String GMO3_URI = "http://www.geocat.ch/2008/gm03_2";
    private static final String GMO3_PREFIX = "gm03";

    @Autowired
    private EsRestClient esRestClient;

    @Value("${es.index.records:gn-records}")
    private String defaultIndex = "records";

    static {
        allNamespaces = ImmutableSet.<Namespace>builder()
            .add(ISO19139Namespaces.GCO)
            .add(ISO19139Namespaces.GMD)
            .add(ISO19139Namespaces.SRV)
            .add(ISO19139cheNamespaces.CHE)
            .build();

        allTypenames = ImmutableMap.<String, Namespace>builder()
            .put("csw:Record", Namespace.getNamespace("csw", "http://www.opengis.net/cat/csw/2.0.2"))
            .put("gmd:MD_Metadata", ISO19139Namespaces.GMD)
            .put("che:CHE_MD_Metadata", ISO19139cheNamespaces.CHE)
            .put("gm03", Namespace.getNamespace(GMO3_PREFIX, GMO3_URI))
            .put("dcat", Namespace.getNamespace("dcat", "http://www.w3.org/ns/dcat#"))
            .build();

        allExportFormats = ImmutableMap.<String, String>builder()
            // This is more for all basic iso19139 profiles using this bean as default
            // The conversion is not available in regular iso19139 plugin.
            // This is for backward compatibility.
            .put("convert/to19139.xsl", "metadata-iso19139.xml")
            // GEOCAT-TODO: Add GM03
            .build();
    }

//    private SubtemplatesByLocalXLinksReplacer subtemplatesByLocalXLinksReplacer;

    public ISO19139cheSchemaPlugin() {
        super(IDENTIFIER, allNamespaces);
    }


    /**
     * Return sibling relation defined in aggregationInfo.
     */
    public Set<AssociatedResource> getAssociatedResourcesUUIDs(Element metadata) {

        String XPATH_FOR_AGGRGATIONINFO = "*//gmd:aggregationInfo/*" +
            "[gmd:aggregateDataSetIdentifier/*/gmd:code " +
            "and gmd:associationType/gmd:DS_AssociationTypeCode/@codeListValue!='']";
        Set<AssociatedResource> listOfResources = new HashSet<AssociatedResource>();
        List<?> sibs = null;

        try {
            sibs = Xml.selectNodes(
                metadata,
                XPATH_FOR_AGGRGATIONINFO,
                allNamespaces.asList());

            for (Object o : sibs) {
                try {
                    if (o instanceof Element) {
                        Element sib = (Element) o;
                        Element agId = (Element) sib.getChild("aggregateDataSetIdentifier", ISO19139Namespaces.GMD)
                            .getChildren().get(0);
                        String sibUuid = getChild(agId, "code", ISO19139Namespaces.GMD)
                            .getChildText("CharacterString", ISO19139Namespaces.GCO);
                        final Element associationTypeEl = getChild(sib, "associationType", ISO19139Namespaces.GMD);
                        String associationType = getChild(associationTypeEl, "DS_AssociationTypeCode", ISO19139Namespaces.GMD)
                            .getAttributeValue("codeListValue");
                        final Element initiativeTypeEl = getChild(sib, "initiativeType", ISO19139Namespaces.GMD);
                        String initiativeType = "";
                        if (initiativeTypeEl != null) {
                            initiativeType = getChild(initiativeTypeEl, "DS_InitiativeTypeCode", ISO19139Namespaces.GMD)
                                .getAttributeValue("codeListValue");
                        }
                        AssociatedResource resource = new AssociatedResource(sibUuid, initiativeType, associationType);
                        listOfResources.add(resource);
                    }
                } catch (Exception e) {
                    Log.error(Log.JEEVES, "Error getting resources UUIDs", e);
                }
            }
        } catch (Exception e) {
            Log.error(Log.JEEVES, "Error getting resources UUIDs", e);
        }
        return listOfResources;
    }


    private Element getChild(Element el, String name, Namespace namespace) {
        final Element child = el.getChild(name, namespace);
        if (child == null) {
            return new Element(name, namespace);
        }
        return child;
    }

    @Override
    public Set<String> getAssociatedParentUUIDs(Element metadata) {
        ElementFilter elementFilter = new ElementFilter("parentIdentifier", ISO19139Namespaces.GMD);
        return Xml.filterElementValues(
            metadata,
            elementFilter,
            "CharacterString", ISO19139Namespaces.GCO,
            null);
    }

    public Set<String> getAssociatedDatasetUUIDs(Element metadata) {
        return getAttributeUuidrefValues(metadata, "operatesOn", ISO19139Namespaces.SRV);
    }

    public Set<String> getAssociatedFeatureCatalogueUUIDs(Element metadata) {
        return getAttributeUuidrefValues(metadata, "featureCatalogueCitation", ISO19139Namespaces.GMD);
    }

    public Set<String> getAssociatedSourceUUIDs(Element metadata) {
        return getAttributeUuidrefValues(metadata, "source", ISO19139Namespaces.GMD);
    }

    private Set<String> getAttributeUuidrefValues(Element metadata, String tagName, Namespace namespace) {
        ElementFilter elementFilter = new ElementFilter(tagName, namespace);
        return Xml.filterElementValues(
            metadata,
            elementFilter,
            null, null,
            "uuidref");
    }


    @Override
    @SuppressWarnings("unchecked")
    public List<Element> getTranslationForElement(Element element, String languageIdentifier) {
        List<Element> matches = null;

        // Collect ISO19139 translation elements
        String path = ".//gmd:LocalisedCharacterString" +
            "[@locale='#" + languageIdentifier + "']";
        try {
            XPath xpath = XPath.newInstance(path);
            matches = xpath.selectNodes(element);
        } catch (Exception e) {
            Log.debug(LOGGER_NAME, String.format(
                "%s: getTranslationForElement failed on element %s using XPath '%s' updatedLocalizedTextElement exception %s",
                getIdentifier(), Xml.getString(element), path, e.getMessage()));
        }


        // If none found, check ISO19139.che localised URL type
        if (matches == null || matches.isEmpty()) {
            path = ".//che:LocalisedURL" +
                "[@locale='#" + languageIdentifier + "']";
            try {
                XPath xpath = XPath.newInstance(path);
                matches = xpath.selectNodes(element);
            } catch (Exception e) {
                Log.debug(LOGGER_NAME, String.format(
                    "%s: getTranslationForElement LocalisedURL failed on element %s using XPath '%s' updatedLocalizedTextElement exception %s",
                    getIdentifier(), Xml.getString(element), path, e.getMessage()));
            }
        }

        return matches;
    }

    /**
     * Add a LocalisedCharacterString or localisedURL to an element.
     * In ISO19139, the translation are
     * stored gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString.
     * <p>
     * <pre>
     * <gmd:title xsi:type="gmd:PT_FreeText_PropertyType">
     *    <gco:CharacterString>Template for Vector data in ISO19139 (multilingual)</gco:CharacterString>
     *    <gmd:PT_FreeText>
     *        <gmd:textGroup>
     *            <gmd:LocalisedCharacterString locale="#FRE">Modèle de données vectorielles en ISO19139.che (multilingue)</gmd:LocalisedCharacterString>
     *        </gmd:textGroup>
     * </pre>
     *
     * @param element
     * @param languageIdentifier
     * @param value
     */
    @Override
    public void addTranslationToElement(Element element, String languageIdentifier, String value) {
        if (element.getChild("PT_FreeText", ISO19139Namespaces.GMD) != null ||
            element.getChild("Anchor", GMX) != null ||
            element.getChild("CharacterString", ISO19139Namespaces.GCO) != null) {
//            super.addTranslationToElement(element, languageIdentifier, value);
            // An ISO19139 element containing translation has an xsi:type attribute
            element.setAttribute("type", "gmd:PT_FreeText_PropertyType",
                Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance"));

            // Create a new translation for the language
            Element langElem = new Element("LocalisedCharacterString", ISO19139Namespaces.GMD);
            langElem.setAttribute("locale", "#" + languageIdentifier);
            langElem.setText(value);
            Element textGroupElement = new Element("textGroup", ISO19139Namespaces.GMD);
            textGroupElement.addContent(langElem);

            // Get the PT_FreeText node where to insert the translation into
            Element freeTextElement = element.getChild("PT_FreeText", ISO19139Namespaces.GMD);
            if (freeTextElement == null) {
                freeTextElement = new Element("PT_FreeText", ISO19139Namespaces.GMD);
                element.addContent(freeTextElement);
            }
            freeTextElement.addContent(textGroupElement);
        } else {
            // An ISO19139 element containing translation has an xsi:type attribute
            element.setAttribute("type", "che:PT_FreeURL_PropertyType",
                Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance"));

            // Create a new translation for the language
            Element langElem = new Element("LocalisedURL", ISO19139cheNamespaces.CHE);
            langElem.setAttribute("locale", "#" + languageIdentifier);
            langElem.setText(value);
            Element textGroupElement = new Element("URLGroup", ISO19139cheNamespaces.CHE);
            textGroupElement.addContent(langElem);

            // Get the PT_FreeURL node where to insert the translation into
            Element freeTextElement = element.getChild("PT_FreeURL", ISO19139cheNamespaces.CHE);
            if (freeTextElement == null) {
                freeTextElement = new Element("PT_FreeURL", ISO19139cheNamespaces.CHE);
                element.addContent(freeTextElement);
            }
            freeTextElement.addContent(textGroupElement);
        }
    }

    @Override
    public String getBasicTypeCharacterStringName() {
        return "gco:CharacterString";
    }

    @Override
    public Element createBasicTypeCharacterString() {
        return new Element("CharacterString", ISO19139Namespaces.GCO);
    }

    @Override
    public Element addOperatesOn(Element serviceRecord, Map<String, String> layers, String serviceType, String baseUrl) {
        Element root = serviceRecord
            .getChild("identificationInfo", GMD)
            .getChild("SV_ServiceIdentification", SRV);

        if (root != null) {

            // Coupling type MUST be present as it is the insertion point
            // for coupledResource
            Element couplingType = root.getChild("couplingType", SRV);
            int coupledResourceIdx = root.indexOf(couplingType);

            layers.keySet().forEach(uuid -> {
                String layerName = layers.get(uuid);

                // Create coupled resources elements to register all layername
                // in service metadata. This information could be used to add
                // interactive map button when viewing service metadata.
                Element coupledResource = new Element("coupledResource", SRV);
                coupledResource.setAttribute("nilReason", "synchronizedFromOGC", ISO19139Namespaces.GCO);
                Element scr = new Element("SV_CoupledResource", SRV);


                // Create operation according to service type
                Element operation = new Element("operationName", SRV);
                Element operationValue = new Element("CharacterString", GCO);

                if (serviceType.startsWith("WMS"))
                    operationValue.setText("GetMap");
                else if (serviceType.startsWith("WFS"))
                    operationValue.setText("GetFeature");
                else if (serviceType.startsWith("WCS"))
                    operationValue.setText("GetCoverage");
                else if (serviceType.startsWith("SOS"))
                    operationValue.setText("GetObservation");
                operation.addContent(operationValue);

                // Create identifier (which is the metadata identifier)
                Element id = new Element("identifier", SRV);
                Element idValue = new Element("CharacterString", GCO);
                idValue.setText(uuid);
                id.addContent(idValue);

                // Create scoped name element as defined in CSW 2.0.2 ISO profil
                // specification to link service metadata to a layer in a service.
                Element scopedName = new Element("ScopedName", GCO);
                scopedName.setText(layerName);

                scr.addContent(operation);
                scr.addContent(id);
                scr.addContent(scopedName);
                coupledResource.addContent(scr);

                // Add coupled resource before coupling type element
                if (coupledResourceIdx != -1) {
                    root.addContent(coupledResourceIdx, coupledResource);
                }


                // Add operatesOn element at the end of identification section.
                Element op = new Element("operatesOn", SRV);
                op.setAttribute("nilReason", "synchronizedFromOGC", GCO);
                op.setAttribute("uuidref", uuid);

                String hRefLink = baseUrl + "api/records/" + uuid + "/formatters/xml";
                op.setAttribute("href", hRefLink, XLINK);

                root.addContent(op);
            });
        }

        return serviceRecord;
    }

    @Override
    public List<Extent> getExtents(Element record) {
        List<Extent> extents = new ArrayList<>();

        ElementFilter bboxFinder = new ElementFilter("EX_GeographicBoundingBox", GMD);
        @SuppressWarnings("unchecked")
        Iterator<Element> bboxes = record.getDescendants(bboxFinder);
        while (bboxes.hasNext()) {
            Element box = bboxes.next();
            try {
                extents.add(new Extent(
                    Double.valueOf(box.getChild("westBoundLongitude", GMD).getChild("Decimal", GCO).getText()),
                    Double.valueOf(box.getChild("eastBoundLongitude", GMD).getChild("Decimal", GCO).getText()),
                    Double.valueOf(box.getChild("southBoundLatitude", GMD).getChild("Decimal", GCO).getText()),
                    Double.valueOf(box.getChild("northBoundLatitude", GMD).getChild("Decimal", GCO).getText())
                ));
            } catch (NullPointerException e) {
            }
        }
        return extents;
    }

    @Override
    public Map<String, Namespace> getCswTypeNames() {
        return allTypenames;
    }

    @Override
    public Map<String, String> getExportFormats() {
        return allExportFormats;
    }


    /**
     * Remove all multilingual aspect of an element. Keep the md language localized strings
     * as default gco:CharacterString for the element.
     *
     * @param element
     * @param langs   Metadata languages. The main language MUST be the first one.
     * @return
     * @throws JDOMException
     */
    @Override
    public Element removeTranslationFromElement(Element element, List<String> langs) throws JDOMException {
        String mainLanguage = langs != null && langs.size() > 0 ? langs.get(0) : "#EN";

        List<Element> nodesWithStrings = (List<Element>) Xml.selectNodes(element, "*//gmd:PT_FreeText", Arrays.asList(ISO19139Namespaces.GMD));

        for (Element e : nodesWithStrings) {
            // Retrieve or create the main language element
            Element mainCharacterString = ((Element) e.getParent()).getChild("CharacterString", ISO19139Namespaces.GCO);
            if (mainCharacterString == null) {
                // create it if it does not exist
                mainCharacterString = new Element("CharacterString", ISO19139Namespaces.GCO);
                ((Element) e.getParent()).addContent(0, mainCharacterString);
            }

            // Retrieve the main language value if exist
            List<Element> mainLangElement = (List<Element>) Xml.selectNodes(
                e,
                "*//gmd:LocalisedCharacterString[@locale='" + mainLanguage + "']",
                Arrays.asList(ISO19139Namespaces.GMD));

            // Set the main language value
            if (mainLangElement.size() == 1) {
                String mainLangString = mainLangElement.get(0).getText();

                if (StringUtils.isNotEmpty(mainLangString)) {
                    mainCharacterString.setText(mainLangString);
                } else if (mainCharacterString.getAttribute("nilReason", ISO19139Namespaces.GCO) == null) {
                    ((Element) mainCharacterString.getParent()).setAttribute("nilReason", "missing", ISO19139Namespaces.GCO);
                }
            } else if (StringUtils.isEmpty(mainCharacterString.getText()) &&
                mainCharacterString.getAttribute("nilReason", ISO19139Namespaces.GCO) == null) {
                ((Element) mainCharacterString.getParent()).setAttribute("nilReason", "missing", ISO19139Namespaces.GCO);
            }
        }

        List<Element> nodesWithUrls = (List<Element>) Xml.selectNodes(element, "*//che:PT_FreeURL", Arrays.asList(ISO19139cheNamespaces.CHE));

        for (Element e : nodesWithUrls) {
            // Retrieve or create the main language element
            Element mainCharacterString = ((Element) e.getParent()).getChild("URL", ISO19139Namespaces.GMD);
            if (mainCharacterString == null) {
                // create it if it does not exist
                mainCharacterString = new Element("URL", ISO19139Namespaces.GMD);
                ((Element) e.getParent()).addContent(0, mainCharacterString);
            }

            // Retrieve the main language value if exist
            List<Element> mainLangElement = (List<Element>) Xml.selectNodes(
                e,
                "*//che:LocalisedURL[@locale='" + mainLanguage + "']",
                Arrays.asList(ISO19139cheNamespaces.CHE));

            // Set the main language value
            if (mainLangElement.size() == 1) {
                String mainLangString = mainLangElement.get(0).getText();

                if (StringUtils.isNotEmpty(mainLangString)) {
                    mainCharacterString.setText(mainLangString);
                } else if (mainCharacterString.getAttribute("nilReason", ISO19139Namespaces.GCO) == null) {
                    ((Element) mainCharacterString.getParent()).setAttribute("nilReason", "missing", ISO19139Namespaces.GCO);
                }
            } else if (StringUtils.isEmpty(mainCharacterString.getText()) &&
                mainCharacterString.getAttribute("nilReason", ISO19139Namespaces.GCO) == null) {
                ((Element) mainCharacterString.getParent()).setAttribute("nilReason", "missing", ISO19139Namespaces.GCO);
            }
        }

        // Remove unused lang entries
        List<Element> translationNodes = (List<Element>) Xml.selectNodes(element, "*//node()[@locale]");
        for (Element el : translationNodes) {
            // Remove all translations if there is no or only one language requested
            if (langs.size() <= 1 ||
                !langs.contains(el.getAttribute("locale").getValue())) {
                Element parent = (Element) el.getParent();
                parent.detach();
            }
        }
        // Remove PT_FreeText which might be emptied by above processing
        for (Element el : nodesWithStrings) {
            if (el.getChildren().size() == 0) {
                el.detach();
            }
        }
        for (Element el : nodesWithUrls) {
            if (el.getChildren().size() == 0) {
                el.detach();
            }
        }


        // Sort all children elements translation
        // according to the language list.
        // When a directory entry is added as an xlink, the URL
        // contains an ordered list of language and this ordre must
        // be preserved in order to display fields in the editor in the same
        // order as other element in the record.
        if (langs.size() > 1) {
            List<Element> elementList = (List<Element>) Xml.selectNodes(element,
                ".//*[gmd:PT_FreeText]",
                Arrays.asList(ISO19139Namespaces.GMD));
            for (Element el : elementList) {
                final Element ptFreeText = el.getChild("PT_FreeText", GMD);
                List<Element> orderedTextGroup = new ArrayList<>();
                for (String l : langs) {
                    List<Element> node = (List<Element>) Xml.selectNodes(ptFreeText, "gmd:textGroup[*/@locale='" + l + "']", Arrays.asList(ISO19139Namespaces.GMD));
                    if (node != null && node.size() == 1) {
                        orderedTextGroup.add((Element) node.get(0).clone());
                    }
                }
                ptFreeText.removeContent();
                ptFreeText.addContent(orderedTextGroup);
            }

            List<Element> urlElementList = (List<Element>) Xml.selectNodes(element,
                ".//*[che:PT_FreeURL]",
                Arrays.asList(ISO19139cheNamespaces.CHE));
            for (Element el : urlElementList) {
                final Element ptFreeText = el.getChild("PT_FreeURL", ISO19139cheNamespaces.CHE);
                List<Element> orderedTextGroup = new ArrayList<>();
                for (String l : langs) {
                    List<Element> node = (List<Element>) Xml.selectNodes(ptFreeText, "che:URLGroup[*/@locale='" + l + "']", Arrays.asList(ISO19139cheNamespaces.CHE));
                    if (node != null && node.size() == 1) {
                        orderedTextGroup.add((Element) node.get(0).clone());
                    }
                }
                ptFreeText.removeContent();
                ptFreeText.addContent(orderedTextGroup);
            }
        }

        return element;
    }

    // TODO geocat4
//    @Override
//    public Element replaceSubtemplatesByLocalXLinks(Element dataXml, String templatesToOperateOn) {
//        return subtemplatesByLocalXLinksReplacer.replaceSubtemplatesByLocalXLinks(
//                dataXml,
//                templatesToOperateOn);
//    }
//
//    @Override
//    public void init(ManagersProxy managersProxy, ConstantsProxy constantsProxy) {
//        List<Namespace> namespaces = new ArrayList<>(allNamespaces);
//        subtemplatesByLocalXLinksReplacer = new SubtemplatesByLocalXLinksReplacer(namespaces,managersProxy) {
//
//            @Override
//            public List<String> getLocalesAsHrefParam(Element dataXml) {
//                return ISO19139SchemaPlugin.getLanguages(dataXml);
//            }
//        };
//        subtemplatesByLocalXLinksReplacer.addReplacer(new FormatReplacer(namespaces, managersProxy, constantsProxy));
//        subtemplatesByLocalXLinksReplacer.addReplacer(new ContactReplacer(namespaces, managersProxy, constantsProxy));
//        subtemplatesByLocalXLinksReplacer.addReplacer(new ExtentReplacer(namespaces, managersProxy, constantsProxy));
//        subtemplatesByLocalXLinksReplacer.addReplacer(new KeywordReplacer(managersProxy));
//    }
//
//    @Override
//    public boolean isInitialised() {
//        return subtemplatesByLocalXLinksReplacer!=null;
//    }

    /**
     * Process some of the ISO elements which can have substitute.
     * <p>
     * For example, a CharacterString can have a gmx:Anchor as a substitute
     * to encode a text value + an extra URL. To make the transition between
     * CharacterString and Anchor transparent, this method takes care of
     * creating the appropriate element depending on the presence of an xlink:href attribute.
     * If the attribute is empty, then a CharacterString is used, if a value is set, an Anchor is created.
     *
     * @param el                  element to process.
     * @param attributeRef        the attribute reference
     * @param parsedAttributeName the name of the attribute, for example <code>xlink:href</code>
     * @param attributeValue      the attribute value
     * @return
     */
    @Override
    public Element processElement(Element el,
                                  String attributeRef,
                                  String parsedAttributeName,
                                  String attributeValue) {
        if (Log.isDebugEnabled(LOGGER_NAME)) {
            Log.debug(LOGGER_NAME, String.format(
                "Processing element %s, attribute %s with attributeValue %s.",
                el, attributeRef, attributeValue));
        }

        boolean elementToProcess = isElementToProcess(el);

        if (elementToProcess && parsedAttributeName.equals("xlink:href")) {
            boolean isEmptyLink = StringUtils.isEmpty(attributeValue);
            boolean isMultilingualElement = el.getName().equals("LocalisedCharacterString");

            if (isMultilingualElement) {
                // The attribute provided relates to the CharacterString and not to the LocalisedCharacterString
                Element targetElement = el.getParentElement().getParentElement().getParentElement()
                    .getChild("CharacterString", GCO);
                if (targetElement != null) {
                    el = targetElement;
                }
            }

            if (isEmptyLink) {
                el.setNamespace(GCO).setName("CharacterString");
                el.removeAttribute("href", XLINK);
                return el;
            } else {
                el.setNamespace(GMX).setName("Anchor");
                el.setAttribute("href", "", XLINK);
                return el;
            }
        } else if (elementToProcess && StringUtils.isNotEmpty(parsedAttributeName) &&
            parsedAttributeName.startsWith(":")) {
            // eg. :codeSpace
            el.setAttribute(parsedAttributeName.substring(1), attributeValue);
            return el;
        } else {
            return super.processElement(el, attributeRef, parsedAttributeName, attributeValue);
        }

    }

    /**
     * Checks if an element requires processing in {@link #processElement(Element, String, String, String)}.
     *
     * @param el Element to check.
     * @return boolean indicating if the element requires processing or not.
     */
    protected boolean isElementToProcess(Element el) {
        if (el == null) return false;

        return elementsToProcess.contains(el.getQualifiedName());
    }

    @Override
    public <L, M> RawLinkPatternStreamer<L, M> createLinkStreamer(ILinkBuilder<L, M> linkbuilder, String excludePattern) {
        RawLinkPatternStreamer patternStreamer = new RawLinkPatternStreamer(linkbuilder, excludePattern);
        patternStreamer.setNamespaces(ISO19139SchemaPlugin.allNamespaces.asList());
        patternStreamer.setRawTextXPath(".//*[name() = 'gco:CharacterString' or name() = 'gmd:URL' or name() = 'gmd:LocalisedCharacterString' or name() = 'che:LocalisedURL']");
        // TODO: che:URL and multilingual text
        return patternStreamer;
    }

    @Override
    public Set<AssociatedResource> getAssociatedSources(Element metadata) {
        Set<AssociatedResource> associatedResources = collectAssociatedResources(metadata, "*//gmd:source");
        return associatedResources;
    }

    private Set<AssociatedResource> collectAssociatedResources(Element metadata, String XPATH) {
        Set<AssociatedResource> associatedResources = new HashSet<>();
        try {
            final List<?> parentMetadata = Xml
                .selectNodes(
                    metadata,
                    XPATH,
                    allNamespaces.asList());
            for (Object o : parentMetadata) {
                Element sib = (Element) o;
                AssociatedResource resource = elementAsAssociatedResource(sib);
                associatedResources.add(resource);
            }
        } catch (JDOMException e) {
            e.printStackTrace();
        }
        return associatedResources;
    }

    private AssociatedResource elementAsAssociatedResource(Element ref) {
        String sibUuid = ref.getAttributeValue("uuidref");
        if (StringUtils.isEmpty(sibUuid)) {
            sibUuid = ref.getTextNormalize();
        }
        String title = ref.getAttributeValue("title", XLINK);
        String url = ref.getAttributeValue("href", XLINK);
        return new AssociatedResource(sibUuid, "", "", url, title);
    }

    @Override
    public Set<AssociatedResource> getAssociatedFeatureCatalogues(Element metadata) {
        Set<AssociatedResource> associatedResources = collectAssociatedResources(metadata, "*//gmd:featureCatalogueCitation");
        return associatedResources;
    }

    @Override
    public Set<AssociatedResource> getAssociatedDatasets(Element metadata) {
        Set<AssociatedResource> associatedResources = collectAssociatedResources(metadata, "*//srv:operatesOn");
        return associatedResources;
    }

    @Override
    public Set<AssociatedResource> getAssociatedParents(Element metadata) {
        Set<AssociatedResource> associatedResources = new HashSet<>();

        Element parentIdentifier = metadata.getChild("parentIdentifier", GMD);
        if (parentIdentifier != null) {
            Element characterString = parentIdentifier.getChild("CharacterString", GCO);
            if (characterString != null) {
                associatedResources.add(new AssociatedResource(characterString.getText(), "", ""));
            }
            Element anchor = parentIdentifier.getChild("Anchor", GMX);
            if (anchor != null) {
                associatedResources.add(elementAsAssociatedResource(anchor));
            }
        }
        // Parent relation is also frequently encoded using
        // aggregation. See parentAssociatedResourceType in ISO19115-3
        return associatedResources;
    }

}
