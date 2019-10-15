package org.fao.geonet.kernel.analyse;

import com.google.common.collect.ImmutableSet;
import org.fao.geonet.schema.iso19139.ISO19139Namespaces;
import org.fao.geonet.schema.iso19139.ISO19139SchemaPlugin;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import org.jdom.JDOMException;
import org.jdom.Namespace;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.core.converters.ConverterUtils.DataSink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weka.core.converters.ConverterUtils.DataSink;

public class RecordAnalyser {

    public static final String ALL_FIELD_XPATH = ".//*[name() = 'gco:CharacterString' or name() = 'gmd:URL' or name() = 'gmd:LocalisedCharacterString' or name() = 'che:LocalisedURL']";
    public static ImmutableSet<Namespace> allNamespaces;

    private static Attribute nameAttr = new Attribute("uuid", (List<String>) null);
    private Instances data;

    static {
        allNamespaces = ImmutableSet.<Namespace>builder()
                .add(ISO19139Namespaces.GCO)
                .add(ISO19139Namespaces.GMD)
                .add(ISO19139Namespaces.SRV)
                .add(Namespace.getNamespace("che", "http://www.geocat.ch/2008/che"))
                .build();
    }

    protected ArrayList<Attribute> proto = new ArrayList<>();
    protected List< Map<Attribute, Object>> instances = new ArrayList<>();

    private double[] zeros;

    public RecordAnalyser() {
        proto.add(nameAttr);
    }

    public void processMetadata(Element metadata, String uuid) throws JDOMException {
        List<Element> encounteredElems = (List<Element>) Xml.selectNodes(metadata, ALL_FIELD_XPATH, allNamespaces.asList());

        Map<Attribute, Object> metadataFields = new HashMap();
        encounteredElems.stream().forEach(field -> processOneField(field, metadata, metadataFields));
        metadataFields.put(nameAttr, uuid);
        instances.add(metadataFields);
    }

    public void persist(String filePath) throws Exception {
        data = new Instances("UsedOrNotUsed", proto, instances.size());

        zeros = new double[proto.size()];

        instances.stream().map(instance -> buildInstance(instance)).forEach(data::add);
        DataSink.write(filePath, data);

    }

    private Instance buildInstance(Map<Attribute, Object> metadataFields) {
        Instance instance = new DenseInstance(proto.size());
        instance.setDataset(data);
        instance.setValue(nameAttr, (String)metadataFields.remove(nameAttr));
        instance.replaceMissingValues(zeros);
        metadataFields.keySet().stream().forEach(key -> instance.setValue(key, (Double)metadataFields.get(key)));
        return instance;
    }

    private void processOneField(Element element, Element root, Map<Attribute, Object> metadataFields) {
        StringBuffer acc = new StringBuffer(element.getQualifiedName());
        String attributeName = getFullPath(element, root, acc);
        Attribute fieldAttr = new Attribute(attributeName);
        if (!proto.contains(fieldAttr)) {
            proto.add(fieldAttr);
        }

        if (!metadataFields.containsKey(fieldAttr)) {
            metadataFields.put(proto.get(proto.indexOf(fieldAttr)), 0d);
        }

        metadataFields.replace(fieldAttr, (Double)metadataFields.get(fieldAttr) + 1);

    }

    private String getFullPath(Element element, Element root, StringBuffer acc) {
        if (element == root) {
            return acc.toString();
        } else {
            acc.append("-");
            Element parent = element.getParentElement();
            acc.append(parent.getQualifiedName());
            return getFullPath(parent, root, acc);
        }
    }



}
