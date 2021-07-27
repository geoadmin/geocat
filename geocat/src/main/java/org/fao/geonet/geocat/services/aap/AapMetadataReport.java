package org.fao.geonet.geocat.services.aap;

import com.google.common.annotations.VisibleForTesting;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.schema.iso19139.ISO19139Namespaces;
import org.fao.geonet.schema.iso19139che.ISO19139cheNamespaces;
import org.fao.geonet.utils.Xml;
import org.jdom.*;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by fgravin on 9/20/17.
 */
public class AapMetadataReport implements Service {

    // Owner
    private final String xpMdOwnerBase = "gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact"
            + "/che:CHE_CI_ResponsibleParty[gmd:role/gmd:CI_RoleCode/@codeListValue='owner']/gmd:organisationName/";
    private final String xpOwnerDe = xpMdOwnerBase + "gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale='#DE']/text()";
    private final String xpOwnerFr = xpMdOwnerBase + "gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale='#FR']/text()";
    private final String xpOwnerEn = xpMdOwnerBase + "gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale='#EN']/text()";
    private final String xpOwnerIt = xpMdOwnerBase + "gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale='#IT']/text()";

    private final String xpMdOwnerDef = xpOwnerDe +"|"+ xpOwnerFr +"|"+ xpOwnerEn +"|"+ xpOwnerIt;

    // SpecialistAuthority
    private final String xpMdSpecialistBase = "gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact"
            + "/che:CHE_CI_ResponsibleParty[gmd:role/gmd:CI_RoleCode/@codeListValue='specialistAuthority']/che:organisationAcronym/";
    private final String xpSpecialistDe = xpMdSpecialistBase + "gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale='#DE']/text()";
    private final String xpSpecialistFr = xpMdSpecialistBase + "gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale='#FR']/text()";
    private final String xpSpecialistEn = xpMdSpecialistBase + "gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale='#EN']/text()";
    private final String xpSpecialistIt = xpMdSpecialistBase + "gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale='#IT']/text()";

    private final String xpSpecialistDef = xpSpecialistDe +"|"+ xpSpecialistFr +"|"+ xpSpecialistEn +"|"+ xpSpecialistIt;

    // topicCategory
    private final String xpTopicCategory = "gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:topicCategory/gmd:MD_TopicCategoryCode/text()";

    private final String xpTitleBase = "gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title";
    private final String xpTitleDe = xpTitleBase + "/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale='#DE']/text()";
    private final String xpTitleFr = xpTitleBase + "/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale='#FR']/text()";
    private final String xpTitleEn = xpTitleBase + "/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale='#EN']/text()";
    private final String xpTitleIt = xpTitleBase + "/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale='#IT']/text()";

    private final String[] xpTitle = new String[] { xpTitleBase + "/gco:CharacterString/text()", xpTitleDe, xpTitleFr, xpTitleEn, xpTitleIt };

    private final String xpBasicGeodataIdentifier = "gmd:identificationInfo//che:basicGeodataID/gco:CharacterString/text()";
    private final String xpGeodataType = "gmd:identificationInfo//che:geodataType/che:MD_geodataTypeCode/@codeListValue";
    private final String xpUuid = "gmd:fileIdentifier/gco:CharacterString/text()";

    private final String xpMaintAndUpdateFreq = " gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:resourceMaintenance/che:CHE_MD_MaintenanceInformation"
            + "/gmd:maintenanceAndUpdateFrequency/gmd:MD_MaintenanceFrequencyCode/@codeListValue";

    // AAP
    private final String xpAap = "gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:resourceMaintenance/che:CHE_MD_MaintenanceInformation/che:appraisal/";
    // AAP duration of conservation
    private final String xpAapDuration = xpAap + "che:CHE_MD_Appraisal_AAP/che:durationOfConservation/gco:Integer/text()";

    // AAP comment on the duration
    private final String xpAapCommentDurBase = xpAap + "che:CHE_MD_Appraisal_AAP/che:commentOnDurationOfConservation";
    private final String xpAapCommentDurDe = xpAapCommentDurBase + "/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale='#DE']/text()";
    private final String xpAapCommentDurFr = xpAapCommentDurBase + "/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale='#FR']/text()";
    private final String xpAapCommentDurEn = xpAapCommentDurBase + "/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale='#EN']/text()";
    private final String xpAapCommentDurIt = xpAapCommentDurBase + "/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale='#IT']/text()";
    private final String xpAapCommentDuration = xpAapCommentDurBase + "/gco:CharacterString/text()" + "|"
            + xpAapCommentDurDe + "|" + xpAapCommentDurFr + "|" + xpAapCommentDurEn + "|" + xpAapCommentDurIt;
    // AAP appraisal of archival
    private final String xpAapAppraisalOfArchival = xpAap + "che:CHE_MD_Appraisal_AAP/che:appraisalOfArchivalValue/che:CHE_AppraisalOfArchivalValueCode/@codeListValue";
    // AAP reason for archiving value
    private final String xpAapReasonForArchiving = xpAap + "che:CHE_MD_Appraisal_AAP/che:reasonForArchivingValue/che:CHE_ReasonForArchivingValueCode/@codeListValue";
    // AAP comment on archival
    private final String xpAapCommentOnArchival = xpAap + "che:CHE_MD_Appraisal_AAP/che:commentOnArchivalValue/gco:CharacterString/text()";

    private final List<Namespace> xpNamespaces = Arrays.asList(new Namespace[] {
            ISO19139Namespaces.GMD,
            ISO19139Namespaces.GCO,
            ISO19139cheNamespaces.CHE
    });

    private String DEFAULT_QUERY = "+tag.\\\\*:\\\"Aufbewahrungs- und Archivierungsplanung AAP - Bund\\\"";
    private String query = "";
    private int size = 10000;

    @Override
    public void init(Path appPath, ServiceConfig params) throws Exception {
        query = params.getValue("query", DEFAULT_QUERY);
        size = Integer.parseInt(params.getValue("size", "10000"));
    }

    private String safeGetText(Element metadata, String xpath) {
        String ret = "";
        Object txt = null;
        try {
            txt = Xml.selectSingle(metadata, xpath, xpNamespaces);
            if (txt == null) {
                return "";
            }
            if (txt instanceof Text) {
                ret = ((Text) txt).getText();
            } else {
                ret = txt.toString();
            }
        } catch (JDOMException e) {
            return "";
        }
        return ret;
    }

    private String safeGetAttribute(Element metadata, String xpath) {
        String ret = "";
        Object att = null;
        try {
            att =  Xml.selectSingle(metadata, xpath, xpNamespaces);
            if (att == null) {
                return "";
            }
            if (att instanceof Attribute) {
                ret = ((Attribute) att).getValue();
            } else {

                ret = att.toString();
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
        Element mi = new Element("record");

        Element rawMd = m.getXmlData(false);

        String title = "";
        // Note: it should be possible to do this in "pure xpath",
        // but I don't want to have to fiddle with different xml / xpath
        // implementations between utests and live webapp, I consider this
        // approach as acceptable.
        for (String xpt : xpTitle) {
            title = this.safeGetText(rawMd, xpt);
            if (! StringUtils.isEmpty(title))
                break;
        }
        String basicGeodataId = this.safeGetText(rawMd, xpBasicGeodataIdentifier);
        String uuid = this.safeGetText(rawMd, xpUuid);
        String geodataType = this.safeGetAttribute(rawMd, xpGeodataType);


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

        String mdOwner      = this.safeGetText(rawMd, xpMdOwnerDef);
        String mdSpecialist = this.safeGetText(rawMd, xpSpecialistDef);
        String topicCategory = this.safeGetText(rawMd, xpTopicCategory);

        mi.addContent(new Element("title").setText(title));
        mi.addContent(new Element("identifier").setText(basicGeodataId));
        mi.addContent(new Element("uuid").setText(uuid));

        mi.addContent(new Element("geodatatype").setText(geodataType.equals("ReferenceGeodata") ? "Ja" : "Nein"));
        mi.addContent(new Element("owner").setText(mdOwner));
        mi.addContent(new Element("specialistAuthority").setText(mdSpecialist));
        mi.addContent(new Element("topicCategory").setText(topicCategory));

        // TODO Point of contact / role code "Specialist authority"
        mi.addContent(new Element("updateFrequency").setText(updateFreq));
        mi.addContent(new Element("durationOfConservation").setText(durationConservation));
        mi.addContent(new Element("commentOnDuration").setText(commentDuration));
        mi.addContent(new Element("commentOnArchival").setText(commentOnArchival));
        mi.addContent(new Element("appraisalOfArchival").setText(appraisalOfArchival));
        mi.addContent(new Element("reasonForArchiving").setText(reasonForArchiving));

        return mi;
    }

    /**
     * Retrieves all documents in the index with an AAP entry.
     * This feature is geocat-specific, see https://jira.swisstopo.ch/browse/GEOCAT_SB-422.
     *
     * @return
     * @throws Exception
     */
    private Set<String> getDocsWithAap(ServiceContext context) throws Exception {
        EsSearchManager sm = context.getBean(EsSearchManager.class);
        // :"+tag.\\*:\"Aufbewahrungs- und Archivierungsplanung AAP - Bund\""
        Set<String> source = new HashSet<String>();
        source.add("uuid");
        SearchResponse response = sm.query(
            "+tag.\\*:\"Aufbewahrungs- und Archivierungsplanung AAP - Bund\"",
            null, source, 0, size);

         return Arrays.stream(response.getHits().getHits())
             .map(SearchHit::getId)
             .collect(Collectors.toSet());
    }

    @Override
    public Element exec(Element params, ServiceContext context) throws Exception {
        Element records = new Element("records");
        MetadataRepository mdRepo = context.getBean(MetadataRepository.class);

        Set<String> mds = getDocsWithAap(context);
        for (String uuid : mds) {
            Metadata curMd = mdRepo.findOneByUuid(uuid);
            records.addContent(extractAapInfo(curMd));
        }
        return records;
    }
}
