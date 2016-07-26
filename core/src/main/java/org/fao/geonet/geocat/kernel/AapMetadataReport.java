package org.fao.geonet.geocat.kernel;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.fao.geonet.domain.Metadata;
import org.fao.geonet.schema.iso19139.ISO19139Namespaces;
import org.fao.geonet.schema.iso19139che.ISO19139cheNamespaces;
import org.fao.geonet.utils.Xml;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.Text;

import com.google.common.annotations.VisibleForTesting;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

public class AapMetadataReport implements Service {

    private final String xpMdOwner = "gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact"
            + "/che:CHE_CI_ResponsibleParty[/gmd:role/gmd:CI_RoleCode/@codeListValue='owner']";
    private final String xpTitle = "gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation"
            + "/gmd:CI_Citation/gmd:title/gco:CharacterString/text()";
    private final String xpBasicGeodataIdentifier = "gmd:identificationInfo/gmd:MD_DataIdentification"
            + "/che:basicGeodataId/gco:CharacterString/text()";
    private final String xpUuid = "gmd:fileIdentifier/gco:CharacterString/text()";
    
    // MD_geodataType ? ReferenceGeodata ???
    
    // TODO: point of contact with role "Specialist authority"
    
    
    // maintenanceAndUpdateFrequency
    private final String xpMaintAndUpdateFreq = "gmd:identificationInfo/che:CHE_MD_DataIdentification"
            + "/gmd:resourceMaintenance/che:CHE_MD_MaintenanceInformation/gmd:maintenanceAndUpdateFrequency"
            + "/gmd:MD_MaintenanceFrequencyCode/@codeListValue";

    private final String xpAap = "gmd:metadataMaintenance/che:CHE_MD_MaintenanceInformation/che:CHE_Appraisal_AAP/";
    // AAP duration of conservation
    private final String xpAapDuration = xpAap + "che:CHE_DurationOfConservation/gco:Integer/text()";
    // AAP comment on the duration
    private final String xpAapCommentDuration = xpAap + "che:CHE_CommentOnDurationOfConservation/gco:CharacterString/text()";
    // AAP appraisal of archival
    private final String xpAapAppraisalOfArchival = xpAap + "che:CHE_AppraisalOfArchivalValue/che:CHE_AppraisalOfArchivalValueCode/@codeListValue";
    // AAP reason for archiving value
    private final String xpAapReasonForArchiving = xpAap + "che:CHE_ReasonForArchivingValue/che:CHE_ReasonForArchivingValueCode/@codeListValue";
    // AAP comment on archival
    private final String xpAapCommentOnArchival = xpAap + "che:CHE_CommentOnArchivalValue/gco:CharacterString/text()";
    private final List<Namespace> xpNamespaces = Arrays.asList(new Namespace[] {
            ISO19139Namespaces.GMD,
            ISO19139Namespaces.GCO,
            ISO19139cheNamespaces.CHE
            });
    @Override
    public void init(Path appPath, ServiceConfig params) throws Exception {}

    private String safeGetText(Element metadata, String xpath) {
        String ret = "";
        try {
            Text txt = (Text) Xml.selectSingle(metadata, xpath, xpNamespaces);
            if (txt != null) {
                ret = txt.getText();
            }
        } catch (JDOMException e) {
            return "";
        }
        return ret;
    }
    
    private String safeGetAttribute(Element metadata, String xpath) {
        String ret = "";
        try {
            Attribute att = (Attribute) Xml.selectSingle(metadata, xpath, xpNamespaces);
            if (att != null) {
                ret = att.getValue();
            }
        } catch (JDOMException e) {
            return "";
        }
        return ret;
    }
    
    @VisibleForTesting
    public Element extractAapInfo(Metadata m) throws IOException, JDOMException {
        if (m == null) {
            throw new NullPointerException("Metadata cannot be null");
        }
        Element mi = new Element("metadata");

        Element rawMd = m.getXmlData(false);

        // TODO point of contact ? several fields, which to consider ?
        String title = this.safeGetText(rawMd, xpTitle);        
        String basicGeodataId = this.safeGetText(rawMd, xpBasicGeodataIdentifier);
        String uuid = this.safeGetText(rawMd, xpUuid);

        // TODO MD_GeodataType ?
        // TODO point of contact with new role code "specialist authority"

        // maintenance and update frequency
        String updateFreq = this.safeGetAttribute(rawMd, xpMaintAndUpdateFreq);
        // duration of conservation (xpAapDuration)
        String durationConservation = this.safeGetText(rawMd, xpAapDuration);
        // comment on duration (xpAapCommentDuration)
        String commentDuration = this.safeGetText(rawMd, xpAapCommentDuration);
        // comment on archival
        String commentOnArchival = this.safeGetText(rawMd, xpAapCommentOnArchival);
        // appraisal of archival (xpAapAppraisalOnArchival)
        String appraisalOfArchival = this.safeGetAttribute(rawMd, xpAapAppraisalOfArchival);
        //reason for archiving
        String reasonForArchiving = this.safeGetAttribute(rawMd, xpAapReasonForArchiving);

        // TODO Still missing point of contact (owner)
        mi.addContent(new Element("title").setText(title));
        mi.addContent(new Element("identifier").setText(basicGeodataId));
        mi.addContent(new Element("uuid").setText(uuid));
        // TODO md geodatatype
        // TODO Point of contact / role code "Specialist authority"
        mi.addContent(new Element("updateFrequency").setText(updateFreq));
        mi.addContent(new Element("durationOfConservation").setText(durationConservation));
        mi.addContent(new Element("commentOnDuration").setText(commentDuration));
        mi.addContent(new Element("commentOnArchival").setText(commentOnArchival));
        mi.addContent(new Element("appraisalOfArchival").setText(appraisalOfArchival));
        mi.addContent(new Element("reasonForArchiving").setText(reasonForArchiving));

        return mi;
    }
    
    @Override
    public Element exec(Element params, ServiceContext context) throws Exception {

        // Point of contact (Owner)
        // Md title
        // identifier (basicGeodataID)
        // UUID
        // MD_geodataType (referenceGeodata)
        // Point of contact (specialist authority)
        // maintenance and update frequency (AAP)
        // duration of conservation (AAP)
        // comment on duration
        //   -- 5 empty columns --
        // comment on archival value (AAP)
        // appraisal on archival value  (AAP)
        // reason for archival value (AAP)
        // -- 11 empty columns --
        return new Element("empty");

    }
}