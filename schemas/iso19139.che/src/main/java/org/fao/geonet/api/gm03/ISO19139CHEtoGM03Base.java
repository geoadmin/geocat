package org.fao.geonet.api.gm03;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.fao.geonet.Constants;
import org.fao.geonet.utils.TransformerFactoryFactory;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public abstract class ISO19139CHEtoGM03Base {
    protected static final String NS = "http://www.interlis.ch/INTERLIS2.3";
    protected static final SchemaFactory SCHEMA_FACTORY = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    protected static final Pattern EMPTY = Pattern.compile("^[\\s\n\r]*$");
    protected final Schema schema;
    protected Transformer xslt;

    public ISO19139CHEtoGM03Base(Path schemaLocation, Path xslFilename) throws SAXException, TransformerConfigurationException {
        if (schemaLocation != null) {
            schema = SCHEMA_FACTORY.newSchema(schemaLocation.toFile());
        } else {
            schema = null;
        }
        xslt = TransformerFactoryFactory.getTransformerFactory().newTransformer(new StreamSource(xslFilename.toFile()));
    }

    protected void convert(String[] xmlFilenames, String group) {
        for (int i = 0; i < xmlFilenames.length; i++) {
            String xmlFilename = xmlFilenames[i];
            try {
                convert(xmlFilename, group);
            } catch (Throwable e) {
                System.out.println("Error while converting " + xmlFilename);
                e.printStackTrace();
                throw new Error(e);
            }
        }
    }

    public Document convert(Document domIn) throws TransformerException, FlattenerException, IOException, SAXException {
        final Source source = new DOMSource(domIn);

        // TODO make the group the group of the metadata being transformed.  But this could cause bugs so I am leaving it now
        final Document doc = doTransform("geocat.ch", source, null);

        validate(domIn.getDocumentURI(), doc);
        return doc;
    }

    private Document doTransform(String group, final Source source, String intermediateFile) throws TransformerException,
            FlattenerException, IOException {
        DOMResult transformed = new DOMResult();
        xslt.setParameter("group", group);
        xslt.transform(source, transformed);
        final Document doc = (Document) transformed.getNode();

        if (wantIntermediate() && intermediateFile != null) {
            OutputStream outputStream = new FileOutputStream(intermediateFile);
            saveDom(doc, outputStream);
        }

        flatten(doc);
        convertCoordinates(doc);
        removeDuplicates(doc);
        removeUnwantedTID(doc);
        return doc;
    }

    private static final String[] TID_LESS_ELEMS = {
            "GM03_2_1Comprehensive.Comprehensive.formatDistributordistributorFormat",
            "GM03_2_1Core.Core.MD_DistributiondistributionFormat",
            "GM03_2_1Comprehensive.Comprehensive.MD_Distributiondistributor",
            "GM03_2_1Core.Core.CI_ResponsiblePartyparentinfo",
            "GM03_2_1Comprehensive.Comprehensive.distributionOrderProcessMD_Distributor",
            "GM03_2_1Core.Core.CI_ResponsiblePartyparentinfoGM03_2_1Core.Core.referenceSystemInfoMD_Metadata",
            "GM03_2_1Comprehensive.Comprehensive.CI_CitationcitedResponsibleParty",
            "GM03_2_1Comprehensive.Comprehensive.CI_Citationidentifier",
            "GM03_2_1Core.Core.MD_IdentificationpointOfContact",
            "GM03_2_1Comprehensive.Comprehensive.MD_MaintenanceInformationupdateScopeDescription",
            "GM03_2_1Comprehensive.Comprehensive.MD_MaintenanceInformationcontact",
            "GM03_2_1Comprehensive.Comprehensive.resourceFormatMD_Identification",
            "GM03_2_1Core.Core.descriptiveKeywordsMD_Identification",
            "GM03_2_1Comprehensive.Comprehensive.MD_UsageuserContactInfo",
            "GM03_2_1Comprehensive.Comprehensive.resourceConstraintsMD_Identification",
            "GM03_2_1Comprehensive.Comprehensive.aggregationInfo_MD_Identification",
            "GM03_2_1Core.Core.EX_ExtentgeographicElement",
            "GM03_2_1Comprehensive.Comprehensive.revisionMD_Identification",
            "GM03_2_1Comprehensive.Comprehensive.dimensionMD_CoverageDescription",
            "GM03_2_1Comprehensive.Comprehensive.classMD_FeatureCatalogueDescription",
            "GM03_2_1Comprehensive.Comprehensive.MD_AttributenamedType",
            "GM03_2_1Comprehensive.Comprehensive.domainMD_FeatureCatalogueDescription",
            "GM03_2_1Comprehensive.Comprehensive.CI_Citationidentifier",
            "GM03_2_1Comprehensive.Comprehensive.CI_CitationcitedResponsibleParty",
            "GM03_2_1Comprehensive.Comprehensive.DQ_Scopeextent",
            "GM03_2_1Core.Core.EX_ExtentgeographicElement",
            "GM03_2_1Comprehensive.Comprehensive.reportDQ_DataQuality",
            "GM03_2_1Comprehensive.Comprehensive.sourceLI_Lineage",
            "GM03_2_1Comprehensive.Comprehensive.CI_CitationcitedResponsibleParty",
            "GM03_2_1Comprehensive.Comprehensive.CI_Citationidentifier",
            "GM03_2_1Comprehensive.Comprehensive.portrayalCatalogueInfoMD_Metadata",
            "GM03_2_1Comprehensive.Comprehensive.MD_MaintenanceInformationupdateScopeDescription",
            "GM03_2_1Comprehensive.Comprehensive.MD_MaintenanceInformationcontact",
            "GM03_2_1Comprehensive.Comprehensive.MD_MetadatalegislationInformation",
            "GM03_2_1Core.Core.referenceSystemInfoMD_Metadata",
            "GM03_2_1Comprehensive.Comprehensive.distributorTransferOptionsMD_Distributor",
            "GM03_2_1Comprehensive.Comprehensive.extentSV_ServiceIdentification",
            "GM03_2_1Comprehensive.Comprehensive.containsOperationsSV_ServiceIdentification",
            "GM03_2_1Comprehensive.Comprehensive.SV_OperationMetadataconnectPoint"
    };

    private void removeUnwantedTID(Document doc) {
        for (String tagname : TID_LESS_ELEMS) {
            NodeList elementsByTagName = doc.getElementsByTagName(tagname);
            for (int i = elementsByTagName.getLength() - 1; i >= 0; i--) {
                Node item = elementsByTagName.item(i);
                NamedNodeMap attributes = item.getAttributes();
                if (attributes.getNamedItem("TID") != null) {
                    attributes.removeNamedItem("TID");
                }
            }
        }

    }

    public void convert(String xmlFilename, String group) throws FlattenerException, IOException, TransformerException, SAXException {
        File xmlFile = new File(xmlFilename);
        String parent = xmlFile.getParent();
        if (parent == null) parent = ".";

        final Source source = new StreamSource(xmlFilename);
        Document doc = doTransform(group, source, parent + "/intermediate_" + xmlFile.getName());

        final String resultFilename = parent + "/result_" + xmlFile.getName();
        OutputStream outputStream = new FileOutputStream(resultFilename);
        saveDom(doc, outputStream);
        validate(resultFilename, doc);

    }

    public void convert(Source source, String group, OutputStream outputStream) throws FlattenerException, IOException,
            TransformerException, SAXException {

        Document doc = doTransform(group, source, null);

        saveDom(doc, outputStream);

    }

    protected abstract boolean wantIntermediate();

    protected abstract void flatten(Document source) throws FlattenerException;

    protected abstract void removeDuplicates(Document doc) throws FlattenerException;

    /**
     * Will replace all the gml:coordinates with a set of COORD with C1 and C2 childs.
     */
    private void convertCoordinates(Document doc) {
        while (true) {
            NodeList toConvert = doc.getElementsByTagNameNS(getGmlCoordinateNs(), "GML_COORDINATES");
            if (toConvert.getLength() == 0) break;
            Node cur = toConvert.item(0);
            convertCoordinates(doc, cur);
        }
    }

    protected abstract String getGmlCoordinateNs();

    /**
     * Will replace a gml:coordinates with a set of COORD with C1 and C2 childs.
     */
    protected void convertCoordinates(Document doc, Node gmlCoordinates) {
        Node parent = gmlCoordinates.getParentNode();

        String cs = gmlCoordinates.getAttributes().getNamedItem("cs").getTextContent();
        String decimal = gmlCoordinates.getAttributes().getNamedItem("decimal").getTextContent();
        String ts = gmlCoordinates.getAttributes().getNamedItem("ts").getTextContent();
        String raw = gmlCoordinates.getTextContent();

        String[] coords = raw.split(Pattern.quote(ts));
        for (int j = 0; j < coords.length; j++) {
            String coord = coords[j];
            String[] vals = coord.split(Pattern.quote(cs));
            if (vals.length >= 2) {
                for (int i = 0; i < vals.length; i++) {
                    vals[i] = vals[i].replace(decimal, ".");
                }
                Element coordNode = createCoordinate(doc, vals);
                parent.insertBefore(coordNode, gmlCoordinates);
            }
        }

        parent.removeChild(gmlCoordinates);
    }

    protected abstract Element createCoordinate(Document doc, String[] vals);

    protected void validate(String xmlFilename, Document doc) throws IOException, SAXException {
/*
        if (schema != null) {
            Validator validator = schema.newValidator();
            final TranslateAndValidate.MyErrorHandler errorHandler = new TranslateAndValidate.MyErrorHandler(xmlFilename);
            validator.setErrorHandler(errorHandler);

            Source validationSource = new DOMSource(doc);

            validator.validate(validationSource);
            //errorHandler.printError(System.out);
            errorHandler.throwErrors();
        }
*/
    }

    public static String toString(Element node) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        try {
            OutputFormat format = new OutputFormat();
            format.setIndent(2);
            format.setIndenting(true);
            format.setPreserveSpace(false);

            XMLSerializer serializer = new XMLSerializer();
            serializer.setOutputFormat(format);
            serializer.setOutputByteStream(stream);
            serializer.serialize(node);
        } finally {
            stream.close();
        }

        return stream.toString(Constants.ENCODING);
    }
    public static void saveDom(Document node, OutputStream outputStream) throws IOException {
        try {
            OutputFormat format = new OutputFormat();
            format.setIndent(2);
            format.setIndenting(true);
            format.setPreserveSpace(false);

            XMLSerializer serializer = new XMLSerializer();
            serializer.setOutputFormat(format);
            serializer.setOutputByteStream(outputStream);
            serializer.serialize(node);
        } finally {
            outputStream.close();
        }
    }

    public static class FlattenerException extends Exception {
        private static final long serialVersionUID = 1L;

        public FlattenerException(String message) {
            super(message);
        }
    }

    /**
     * A DOM child node iterator that takes a snapshot of the list of child nodes
     * at construction. It is therefore not impacted by the modifications while iterating
     */
    protected static class ChildIterator implements Iterator<Node> {
        private final List<Node> childs;
        private int pos = 0;

        public ChildIterator(Node root) {
            childs = new ArrayList<Node>();
            for (Node child = root.getFirstChild(); child != null; child = child.getNextSibling()) {
                childs.add(child);
            }
        }

        public boolean hasNext() {
            return pos < childs.size();
        }

        public Node next() {
            return childs.get(pos++);
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
