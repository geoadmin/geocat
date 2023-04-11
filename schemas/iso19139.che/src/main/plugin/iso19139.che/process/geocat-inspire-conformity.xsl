<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:geonet="http://www.fao.org/geonetwork"
                exclude-result-prefixes="#all"
                version="2.0">

  <xsl:output indent="yes"/>

  <xsl:variable name="uuid"
                select="/che:CHE_MD_Metadata//gmd:fileIdentifier/gco:CharacterString"/>


  <xsl:variable name="ithemeTopiccatMap">
    <entry>
      <theme>Geographical names</theme>
      <newitheme>http://inspire.ec.europa.eu/theme/gn</newitheme>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/3</itheme>
      <topiccat>location</topiccat>
    </entry>
    <entry>
      <theme>Administrative units</theme>
      <newitheme>http://inspire.ec.europa.eu/theme/au</newitheme>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/4</itheme>
      <topiccat>boundaries</topiccat>
    </entry>
    <entry>
      <theme>Addresses</theme>
      <newitheme>http://inspire.ec.europa.eu/theme/ad</newitheme>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/5</itheme>
      <topiccat>location</topiccat>
    </entry>
    <entry>
      <theme>Cadastral parcels</theme>
      <newitheme>http://inspire.ec.europa.eu/theme/cp</newitheme>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/6</itheme>
      <topiccat>planningCadastre</topiccat>
    </entry>
    <entry>
      <theme>Transport networks</theme>
      <newitheme>http://inspire.ec.europa.eu/theme/tn</newitheme>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/7</itheme>
      <topiccat>transportation</topiccat>
    </entry>
    <entry>
      <theme>Hydrography</theme>
      <newitheme>http://inspire.ec.europa.eu/theme/hy</newitheme>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/8</itheme>
      <topiccat>inlandWaters</topiccat>
    </entry>
    <entry>
      <theme>Protected sites</theme>
      <newitheme>http://inspire.ec.europa.eu/theme/ps</newitheme>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/9</itheme>
      <topiccat>environment</topiccat>
    </entry>
    <entry>
      <theme>Elevation</theme>
      <newitheme>http://inspire.ec.europa.eu/theme/el</newitheme>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/10</itheme>
      <topiccat>elevation</topiccat>
    </entry>
    <entry>
      <theme>Land cover</theme>
      <newitheme>http://inspire.ec.europa.eu/theme/lc</newitheme>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/11</itheme>
      <topiccat>imageryBaseMapsEarthCover</topiccat>
    </entry>
    <entry>
      <theme>Orthoimagery</theme>
      <newitheme>http://inspire.ec.europa.eu/theme/oi</newitheme>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/12</itheme>
      <topiccat>imageryBaseMapsEarthCover</topiccat>
    </entry>
    <entry>
      <theme>Geology</theme>
      <newitheme>http://inspire.ec.europa.eu/theme/ge</newitheme>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/13</itheme>
      <topiccat>geoscientificInformation</topiccat>
    </entry>
    <entry>
      <theme>Statistical units</theme>
      <newitheme>http://inspire.ec.europa.eu/theme/su</newitheme>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/14</itheme>
      <topiccat>boundaries</topiccat>
    </entry>
    <entry>
      <theme>Buildings</theme>
      <newitheme>http://inspire.ec.europa.eu/theme/bu</newitheme>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/15</itheme>
      <topiccat>structure</topiccat>
    </entry>
    <entry>
      <theme>Soil</theme>
      <newitheme>http://inspire.ec.europa.eu/theme/so</newitheme>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/16</itheme>
      <topiccat>geoscientificInformation</topiccat>
    </entry>
    <entry>
      <theme>Land use</theme>
      <newitheme>http://inspire.ec.europa.eu/theme/lu</newitheme>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/17</itheme>
      <topiccat>planningCadastre</topiccat>
    </entry>
    <entry>
      <theme>Human health and safety</theme>
      <newitheme>http://inspire.ec.europa.eu/theme/hh</newitheme>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/18</itheme>
      <topiccat>health</topiccat>
    </entry>
    <entry>
      <theme>Utility and governmental services</theme>
      <newitheme>http://inspire.ec.europa.eu/theme/us</newitheme>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/19</itheme>
      <topiccat>utilitiesCommunication</topiccat>
    </entry>
    <entry>
      <theme>Environmental monitoring facilities</theme>
      <newitheme>http://inspire.ec.europa.eu/theme/ef</newitheme>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/20</itheme>
      <topiccat>structure</topiccat>
    </entry>
    <entry>
      <theme>Production and industrial facilities</theme>
      <newitheme>http://inspire.ec.europa.eu/theme/pf</newitheme>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/21</itheme>
      <topiccat>structure</topiccat>
    </entry>
    <entry>
      <theme>Agricultural and aquaculture facilities</theme>
      <newitheme>http://inspire.ec.europa.eu/theme/af</newitheme>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/22</itheme>
      <topiccat>farming</topiccat>
    </entry>
    <entry>
      <theme>Population distribution — demography</theme>
      <newitheme>http://inspire.ec.europa.eu/theme/pd</newitheme>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/23</itheme>
      <topiccat>society</topiccat>
    </entry>
    <entry>
      <theme>Area management/restriction/regulation zones and reporting units</theme>
      <newitheme>http://inspire.ec.europa.eu/theme/am</newitheme>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/24</itheme>
      <topiccat>planningCadastre</topiccat>
    </entry>
    <entry>
      <theme>Natural risk zones</theme>
      <newitheme>http://inspire.ec.europa.eu/theme/nz</newitheme>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/25</itheme>
      <topiccat>geoscientificInformation</topiccat>
    </entry>
    <entry>
      <theme>Atmospheric conditions</theme>
      <newitheme>http://inspire.ec.europa.eu/theme/ac</newitheme>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/26</itheme>
      <topiccat>climatologyMeteorologyAtmosphere</topiccat>
    </entry>
    <entry>
      <theme>Meteorological geographical features</theme>
      <newitheme>http://inspire.ec.europa.eu/theme/mf</newitheme>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/27</itheme>
      <topiccat>climatologyMeteorologyAtmosphere</topiccat>
    </entry>
    <entry>
      <theme>Oceanographic geographical features</theme>
      <newitheme>http://inspire.ec.europa.eu/theme/of</newitheme>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/28</itheme>
      <topiccat>oceans</topiccat>
    </entry>
    <entry>
      <theme>Sea regions</theme>
      <newitheme>http://inspire.ec.europa.eu/theme/sr</newitheme>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/29</itheme>
      <topiccat>oceans</topiccat>
    </entry>
    <entry>
      <theme>Bio-geographical regions</theme>
      <newitheme>http://inspire.ec.europa.eu/theme/br</newitheme>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/30</itheme>
      <topiccat>biota</topiccat>
    </entry>
    <entry>
      <theme>Habitats and biotopes</theme>
      <newitheme>http://inspire.ec.europa.eu/theme/hb</newitheme>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/31</itheme>
      <topiccat>biota</topiccat>
    </entry>
    <entry>
      <theme>Species distribution</theme>
      <newitheme>http://inspire.ec.europa.eu/theme/sd</newitheme>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/32</itheme>
      <topiccat>biota</topiccat>
    </entry>
    <entry>
      <theme>Energy resources</theme>
      <newitheme>http://inspire.ec.europa.eu/theme/er</newitheme>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/33</itheme>
      <topiccat>economy</topiccat>
    </entry>
    <entry>
      <theme>Mineral resources</theme>
      <newitheme>http://inspire.ec.europa.eu/theme/mr</newitheme>
      <itheme>http://rdfdata.eionet.europa.eu/inspirethemes/themes/34</itheme>
      <topiccat>economy</topiccat>
    </entry>
  </xsl:variable>

  <xsl:variable name="hasSpatialScope"
                select="count(/che:CHE_MD_Metadata/gmd:identificationInfo/*/gmd:descriptiveKeywords[*/gmd:thesaurusName/*/gmd:title/*/text() = 'Spatial scope']) > 0"/>

  <xsl:variable name="hasQualitySection"
                select="count(/che:CHE_MD_Metadata/gmd:dataQualityInfo) > 0"/>

  <xsl:variable name="hasQualityReport10892010"
                select="count(/che:CHE_MD_Metadata/gmd:dataQualityInfo/*/
                  gmd:report/gmd:DQ_DomainConsistency/gmd:result/*/
                    gmd:specification/*/gmd:title[
                      contains(*[1]/text(), '1089/2010')]) > 0"/>

  <xsl:variable name="hasQualityReport9762009"
                select="count(/che:CHE_MD_Metadata/gmd:dataQualityInfo/*/
                  gmd:report/gmd:DQ_DomainConsistency/gmd:result/*/
                    gmd:specification/*/gmd:title[
                      contains(*[1]/text(), '976/2009')]) > 0"/>

  <xsl:variable name="isService"
                select="count(/che:CHE_MD_Metadata/*/srv:SV_ServiceIdentification) > 0"/>

  <xsl:variable name="mainLanguage"
                select="/che:CHE_MD_Metadata/gmd:language/gmd:LanguageCode/@codeListValue"/>

  <xsl:variable name="threeLettersLanguageCodeList"
                select="string-join(($mainLanguage|/che:CHE_MD_Metadata/gmd:locale/*/
                          gmd:languageCode/*/@codeListValue[. != $mainLanguage]), ',')"/>

  <xsl:variable name="contactXlinkRoleExpression"
                select="'~(distributor|processor|owner|originator|resourceProvider|custodian|author|principalInvestigator|distributor)'"/>

  <!-- Metadata / Role is pointOfContact -->
  <xsl:template match="che:CHE_MD_Metadata/gmd:contact
                        /@xlink:href[matches(., $contactXlinkRoleExpression)]">
    <xsl:message select="concat('INSPIRE|', $uuid, '|Metadata|Contact: Change role to pointOfContact.')"/>
    <xsl:attribute name="xlink:href"
                   select="replace(., $contactXlinkRoleExpression, '~pointOfContact')"/>
  </xsl:template>

  <!-- Resource / Identification / Temporal reference / LastRevision -->
  <xsl:template match="@codeListValue[. = 'lastRevision']">
    <xsl:message select="concat('INSPIRE|', $uuid, '|Identification|Date: Replace lastRevision by revision.')"/>
    <xsl:attribute name="codeListValue"
                   select="'revision'"/>
  </xsl:template>


  <xsl:template match="che:CHE_MD_Metadata">
    <xsl:copy>
      <xsl:copy-of select="@*"/>

      <xsl:apply-templates select="
        gmd:fileIdentifier|
        gmd:language|
        gmd:characterSet|
        gmd:parentIdentifier|
        gmd:hierarchyLevel"/>

      <xsl:if test="not(gmd:hierarchyLevel)">
        <xsl:message select="concat('INSPIRE|', $uuid, '|Metadata|Add hierarchy level.')"/>
        <gmd:hierarchyLevel>
          <gmd:MD_ScopeCode
            codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#MD_ScopeCode"
            codeListValue="{if ($isService) then 'service' else 'dataset'}"/>
        </gmd:hierarchyLevel>
      </xsl:if>


      <xsl:apply-templates select="
        gmd:hierarchyLevelName|
        gmd:contact|
        gmd:dateStamp|
        gmd:metadataStandardName|
        gmd:metadataStandardVersion|
        gmd:dataSetURI|
        gmd:locale|
        gmd:spatialRepresentationInfo|
        gmd:referenceSystemInfo"/>

      <xsl:if test="not(gmd:referenceSystemInfo) and not($isService)">
        <xsl:message select="concat('INSPIRE|', $uuid, '|CRS|Add default EPSG:2056.')"/>
        <gmd:referenceSystemInfo>
          <gmd:MD_ReferenceSystem>
            <gmd:referenceSystemIdentifier>
              <gmd:RS_Identifier>
                <gmd:code xsi:type="gmd:PT_FreeText_PropertyType">
                  <gco:CharacterString>EPSG:2056</gco:CharacterString>
                  <gmd:PT_FreeText>
                    <gmd:textGroup>
                      <gmd:LocalisedCharacterString locale="#DE">EPSG:2056</gmd:LocalisedCharacterString>
                    </gmd:textGroup>
                    <gmd:textGroup>
                      <gmd:LocalisedCharacterString locale="#FR">EPSG:2056</gmd:LocalisedCharacterString>
                    </gmd:textGroup>
                    <gmd:textGroup>
                      <gmd:LocalisedCharacterString locale="#IT">EPSG:2056</gmd:LocalisedCharacterString>
                    </gmd:textGroup>
                    <gmd:textGroup>
                      <gmd:LocalisedCharacterString locale="#EN">EPSG:2056</gmd:LocalisedCharacterString>
                    </gmd:textGroup>
                  </gmd:PT_FreeText>
                </gmd:code>
              </gmd:RS_Identifier>
            </gmd:referenceSystemIdentifier>
          </gmd:MD_ReferenceSystem>
        </gmd:referenceSystemInfo>
      </xsl:if>

      <xsl:apply-templates select="
        gmd:metadataExtensionInfo|
        gmd:identificationInfo|
        gmd:contentInfo|
        gmd:distributionInfo|
        gmd:dataQualityInfo"/>

      <!-- Resource / Quality / Lineage -->
      <xsl:if test="not($hasQualitySection)">
        <xsl:call-template name="create-dq-section"/>
      </xsl:if>

      <xsl:apply-templates select="
        gmd:portrayalCatalogueInfo|
        gmd:metadataConstraints|
        gmd:applicationSchemaInfo|
        gmd:metadataMaintenance|
        gmd:series|
        gmd:describes|
        gmd:propertyType|
        gmd:featureType|
        gmd:featureAttribute"/>

      <xsl:apply-templates select="
        *[namespace-uri()!='http://www.isotc211.org/2005/gmd' and
          namespace-uri()!='http://www.isotc211.org/2005/srv']"/>
    </xsl:copy>
  </xsl:template>


  <xsl:template match="gmd:identificationInfo/*">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="gmd:citation"/>
      <xsl:apply-templates select="gmd:abstract"/>
      <xsl:apply-templates select="gmd:purpose"/>
      <xsl:apply-templates select="gmd:credit"/>
      <xsl:apply-templates select="gmd:status"/>

      <!-- Resource / Responsible organization missing -->
      <xsl:apply-templates select="gmd:pointOfContact"/>
      <xsl:if test="count(gmd:pointOfContact) = 0">
        <xsl:message
          select="concat('INSPIRE|', $uuid, '|Identification|Point of contact: copy first metadata contact as resource contact.')"/>
        <gmd:pointOfContact xlink:href="{replace(
                              ancestor::che:CHE_MD_Metadata/gmd:contact[1]/@xlink:href,
                              $contactXlinkRoleExpression,
                              '~pointOfContact')}"/>
      </xsl:if>

      <xsl:apply-templates select="gmd:resourceMaintenance"/>
      <xsl:apply-templates select="gmd:graphicOverview"/>
      <xsl:apply-templates select="gmd:resourceFormat"/>
      <xsl:apply-templates select="gmd:descriptiveKeywords"/>


      <xsl:variable name="hasInspireKeywords"
                    select="count(gmd:descriptiveKeywords[
                              contains(@xlink:href,
                              'thesaurus=external.theme.httpinspireeceuropaeutheme-theme')]) > 0"/>

      <xsl:if test="not($hasInspireKeywords)">
        <xsl:message
          select="concat('INSPIRE|', $uuid, '|Identification|Add INSPIRE themes based on topic category.')"/>

        <xsl:variable name="topics"
                      select="gmd:topicCategory/*/text()"/>
        <xsl:variable name="listOfId"
                      select="string-join($ithemeTopiccatMap//entry[topiccat = $topics]/newitheme/encode-for-uri(.), ',')"/>

        <gmd:descriptiveKeywords
          xlink:href="local://srv/api/registries/vocabularies/keyword?skipdescriptivekeywords=true&amp;thesaurus=external.theme.httpinspireeceuropaeutheme-theme&amp;id={$listOfId}&amp;lang={$threeLettersLanguageCodeList}"/>

      </xsl:if>

      <xsl:variable name="hasClassificationOfService"
                    select="count(gmd:descriptiveKeywords[contains(@xlink:href, 'httpinspireeceuropaeumetadatacodelistSpatialDataServiceCategory')]) > 0"/>
      <xsl:if test="$isService and not($hasClassificationOfService)">
        <xsl:variable name="serviceType"
                      select="srv:serviceType/gco:LocalName"/>
        <!-- TODO: Can't add keyword with XLink and Anchor -->
        <xsl:message
          select="concat('INSPIRE|', $uuid, '|Service| Add missing service classification based on service type ', $serviceType, '.')"/>

        <xsl:variable name="serviceTypeMapping" as="node()*">
          <entry key="download"
                 value="http%3A%2F%2Finspire.ec.europa.eu%2Fmetadata-codelist%2FSpatialDataServiceCategory%2FinfoProductAccessService"/>
          <entry key="OGC:WFS"
                 value="http%3A%2F%2Finspire.ec.europa.eu%2Fmetadata-codelist%2FSpatialDataServiceCategory%2FinfoProductAccessService"/>
          <entry key="view"
                 value="http%3A%2F%2Finspire.ec.europa.eu%2Fmetadata-codelist%2FSpatialDataServiceCategory%2FinfoMapAccessService"/>
          <entry key="OGC:WMS"
                 value="http%3A%2F%2Finspire.ec.europa.eu%2Fmetadata-codelist%2FSpatialDataServiceCategory%2FinfoMapAccessService"/>
          <entry key="OGC:WMTS"
                 value="http%3A%2F%2Finspire.ec.europa.eu%2Fmetadata-codelist%2FSpatialDataServiceCategory%2FinfoMapAccessService"/>
          <entry key="discovery"
                 value="http%3A%2F%2Finspire.ec.europa.eu%2Fmetadata-codelist%2FSpatialDataServiceCategory%2FinfoCatalogueService"/>
          <entry key="OGC:CSW"
                 value="http%3A%2F%2Finspire.ec.europa.eu%2Fmetadata-codelist%2FSpatialDataServiceCategory%2FinfoCatalogueService"/>
        </xsl:variable>

        <gmd:descriptiveKeywords
          xlink:href="local://srv/api/registries/vocabularies/keyword?skipdescriptivekeywords=true&amp;thesaurus=external.theme.httpinspireeceuropaeumetadatacodelistSpatialDataServiceCategory-SpatialDataServiceCategory&amp;id={$serviceTypeMapping[@key = $serviceType]/@value}&amp;lang={$threeLettersLanguageCodeList}&amp;transformation=to-iso19139-keyword-with-anchor"/>
      </xsl:if>


      <xsl:variable name="basicGeodataIDType"
                    select="che:basicGeodataIDType/che:basicGeodataIDTypeCode/@codeListValue"/>
      <xsl:variable name="hasSpatialScope"
                    select="count(gmd:descriptiveKeywords[contains(@xlink:href, 'httpinspireeceuropaeumetadatacodelistSpatialScope')]) > 0"/>

      <xsl:if test="not($hasSpatialScope)">
        <xsl:choose>
          <xsl:when test="$basicGeodataIDType = 'federal'">
            <gmd:descriptiveKeywords
              xlink:href="local://srv/api/registries/vocabularies/keyword?skipdescriptivekeywords=true&amp;thesaurus=external.theme.httpinspireeceuropaeumetadatacodelistSpatialScope-SpatialScope&amp;id=http%3A%2F%2Finspire.ec.europa.eu%2Fmetadata-codelist%2FSpatialScope%2Fnational&amp;lang={$threeLettersLanguageCodeList}"/>
          </xsl:when>
          <xsl:when test="$basicGeodataIDType = 'cantonal'">
            <gmd:descriptiveKeywords
              xlink:href="local://srv/api/registries/vocabularies/keyword?skipdescriptivekeywords=true&amp;thesaurus=external.theme.httpinspireeceuropaeumetadatacodelistSpatialScope-SpatialScope&amp;id=http%3A%2F%2Finspire.ec.europa.eu%2Fmetadata-codelist%2FSpatialScope%2Fregional&amp;lang={$threeLettersLanguageCodeList}"/>
          </xsl:when>
          <xsl:when test="$basicGeodataIDType = 'communal'">
            <gmd:descriptiveKeywords
              xlink:href="local://srv/api/registries/vocabularies/keyword?skipdescriptivekeywords=true&amp;thesaurus=external.theme.httpinspireeceuropaeumetadatacodelistSpatialScope-SpatialScope&amp;id=http%3A%2F%2Finspire.ec.europa.eu%2Fmetadata-codelist%2FSpatialScope%2Flocal&amp;lang={$threeLettersLanguageCodeList}"/>
          </xsl:when>
        </xsl:choose>
      </xsl:if>

      <xsl:apply-templates select="gmd:resourceSpecificUsage"/>

      <!-- Resource / Access constraint / Limitation on public access AND condition to access and use TODO -->
      <xsl:apply-templates select="gmd:resourceConstraints"/>

      <xsl:if test="not(gmd:resourceConstraints[*/gmd:accessConstraints])">
        <xsl:message
          select="concat('INSPIRE|', $uuid, '|Identification|Adding default access resource constraints.')"/>
        <gmd:resourceConstraints>
          <gmd:MD_LegalConstraints>
            <gmd:accessConstraints>
              <gmd:MD_RestrictionCode
                codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#MD_RestrictionCode"
                codeListValue="otherRestrictions"/>
            </gmd:accessConstraints>
            <gmd:otherConstraints xsi:type="gmd:PT_FreeText_PropertyType">
              <gmx:Anchor
                xlink:href="http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/noLimitations">No
                limitations to public access
              </gmx:Anchor>
              <gmd:PT_FreeText>
                <gmd:textGroup>
                  <gmd:LocalisedCharacterString locale="#DE">No limitations to public access
                  </gmd:LocalisedCharacterString>
                </gmd:textGroup>
                <gmd:textGroup>
                  <gmd:LocalisedCharacterString locale="#FR">No limitations to public access
                  </gmd:LocalisedCharacterString>
                </gmd:textGroup>
                <gmd:textGroup>
                  <gmd:LocalisedCharacterString locale="#IT">No limitations to public access
                  </gmd:LocalisedCharacterString>
                </gmd:textGroup>
                <gmd:textGroup>
                  <gmd:LocalisedCharacterString locale="#EN">No limitations to public access
                  </gmd:LocalisedCharacterString>
                </gmd:textGroup>
              </gmd:PT_FreeText>
            </gmd:otherConstraints>
          </gmd:MD_LegalConstraints>
        </gmd:resourceConstraints>
      </xsl:if>

      <xsl:if test="not(gmd:resourceConstraints[*/gmd:useConstraints])">
        <xsl:message
          select="concat('INSPIRE|', $uuid, '|Identification|Adding default use access resource constraints.')"/>
        <gmd:resourceConstraints>
          <gmd:MD_LegalConstraints>
            <gmd:useConstraints>
              <gmd:MD_RestrictionCode
                codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#MD_RestrictionCode"
                codeListValue="otherRestrictions"/>
            </gmd:useConstraints>
            <gmd:otherConstraints xsi:type="gmd:PT_FreeText_PropertyType">
              <gmx:Anchor
                xlink:href="http://inspire.ec.europa.eu/metadata-codelist/ConditionsApplyingToAccessAndUse/noConditionsApply">
                No conditions to access and use
              </gmx:Anchor>
              <gmd:PT_FreeText>
                <gmd:textGroup>
                  <gmd:LocalisedCharacterString locale="#DE">No conditions to access and use
                  </gmd:LocalisedCharacterString>
                </gmd:textGroup>
                <gmd:textGroup>
                  <gmd:LocalisedCharacterString locale="#FR">No conditions to access and use
                  </gmd:LocalisedCharacterString>
                </gmd:textGroup>
                <gmd:textGroup>
                  <gmd:LocalisedCharacterString locale="#IT">No conditions to access and use
                  </gmd:LocalisedCharacterString>
                </gmd:textGroup>
                <gmd:textGroup>
                  <gmd:LocalisedCharacterString locale="#EN">No conditions to access and use
                  </gmd:LocalisedCharacterString>
                </gmd:textGroup>
              </gmd:PT_FreeText>
            </gmd:otherConstraints>
          </gmd:MD_LegalConstraints>
        </gmd:resourceConstraints>
      </xsl:if>

      <xsl:apply-templates select="gmd:aggregationInfo"/>
      <xsl:apply-templates select="gmd:spatialRepresentationType"/>

      <!--
       XML document 'file.xml', record '8fc6f748-80ce-47b2-8d49-b05a0c8ba975': The spatial representation type shall be given using at least one element gmd:spatialRepresentationType/gmd:MD_SpatialRepresentationTypeCode.
       -->
      <xsl:if test="not($isService) and not(gmd:spatialRepresentationType)">
        <xsl:message
          select="concat('INSPIRE|', $uuid, '|Identification|Spatial representation type: add default vector because missing.')"/>
        <gmd:spatialRepresentationType>
          <gmd:MD_SpatialRepresentationTypeCode
            codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#MD_SpatialRepresentationTypeCode"
            codeListValue="vector"/>
        </gmd:spatialRepresentationType>
      </xsl:if>

      <xsl:apply-templates select="gmd:spatialResolution"/>
      <xsl:apply-templates select="gmd:language"/>
      <xsl:apply-templates select="gmd:characterSet"/>
      <xsl:apply-templates select="gmd:topicCategory"/>
      <xsl:apply-templates select="gmd:environmentDescription"/>
      <xsl:apply-templates select="gmd:extent"/>
      <xsl:apply-templates select="gmd:supplementalInformation"/>

      <xsl:apply-templates select="srv:*"/>
      <xsl:apply-templates select="*[namespace-uri()!='http://www.isotc211.org/2005/gmd' and
                                     namespace-uri()!='http://www.isotc211.org/2005/srv']"/>
    </xsl:copy>
  </xsl:template>


  <xsl:template match="gmd:distributionInfo/*[not(gmd:distributionFormat)]">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="gmd:distributionFormat"/>
      <xsl:for-each select="gmd:distributor//gmd:distributorFormat">
        <xsl:message
          select="concat('INSPIRE|', $uuid, '|Distribution|Move distribution format from distributor to distributionInfo.')"/>
        <gmd:distributionFormat>
          <xsl:copy-of select="@*"/>
        </gmd:distributionFormat>
      </xsl:for-each>
      <xsl:apply-templates select="gmd:distributor"/>
      <xsl:apply-templates select="gmd:transferOptions"/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="gmd:distributionInfo/*/gmd:distributor/*/gmd:distributorFormat"/>


  <xsl:template match="gmd:resourceConstraints[string-join((.//*|//@*), '') = '']">
    <xsl:message
      select="concat('INSPIRE|', $uuid, '|Identification|Removing empty resource constraints.')"/>
  </xsl:template>


  <xsl:template match="gmd:identificationInfo/*/gmd:citation/*[
                                not(gmd:identifier)
                                and not($isService)]">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:variable name="elements"
                    select="gmd:title|gmd:alternateTitle|gmd:date|gmd:edition|gmd:editionDate"/>
      <xsl:apply-templates select="$elements"/>

      <!-- Dataset / No resource identifier -->
      <xsl:variable name="basicgeodataId"
                    select="ancestor::gmd:identificationInfo/*/che:basicGeodataID/*/text()"/>
      <gmd:identifier>
        <gmd:MD_Identifier>
          <gmd:code>
            <gco:CharacterString>
              <xsl:choose>
                <xsl:when test="$basicgeodataId != ''">
                  <xsl:message
                    select="concat('INSPIRE|', $uuid, '|Identification|Resource identifier: copy basic geodata id ', $basicgeodataId , ' as identifier.')"/>
                  <xsl:value-of select="concat('basicGeodataID:', $basicgeodataId)"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:message
                    select="concat('INSPIRE|', $uuid, '|Identification|Resource identifier: copy uuid ', $uuid , ' as identifier.')"/>
                  <xsl:value-of select="$uuid"/>
                </xsl:otherwise>
              </xsl:choose>
            </gco:CharacterString>
          </gmd:code>
        </gmd:MD_Identifier>
      </gmd:identifier>

      <xsl:apply-templates select="* except $elements"/>
    </xsl:copy>
  </xsl:template>


  <xsl:template match="gmd:dataQualityInfo/*">
    <xsl:copy>
      <xsl:apply-templates select="gmd:scope"/>
      <xsl:apply-templates select="gmd:report"/>
      <xsl:call-template name="add-inspire-regulation-conformity"/>
      <xsl:apply-templates select="gmd:lineage"/>
    </xsl:copy>
  </xsl:template>


  <xsl:template name="add-inspire-regulation-conformity">
    <xsl:if test="not($hasQualityReport10892010)">
      <xsl:message select="concat('INSPIRE|', $uuid, '|DataQuality|Regulation: add conformity for metadata.')"/>
      <gmd:report>
        <gmd:DQ_DomainConsistency>
          <gmd:result>
            <gmd:DQ_ConformanceResult>
              <gmd:specification>
                <gmd:CI_Citation>
                  <gmd:title xsi:type="gmd:PT_FreeText_PropertyType">
                    <gmx:Anchor xlink:href="http://data.europa.eu/eli/reg/2010/1089"></gmx:Anchor>
                    <gmd:PT_FreeText>
                      <gmd:textGroup>
                        <gmd:LocalisedCharacterString locale="#DE">Verordnung (EG) Nr. 1089/2010 der Kommission vom 23.
                          November 2010 zur Durchführung der Richtlinie 2007/2/EG des Europäischen Parlaments und des
                          Rates hinsichtlich der Interoperabilität von Geodatensätzen und -diensten
                        </gmd:LocalisedCharacterString>
                      </gmd:textGroup>
                      <gmd:textGroup>
                        <gmd:LocalisedCharacterString locale="#FR">Règlement (UE) n ° 1089/2010 de la Commission du 23
                          novembre 2010 portant modalités d'application de la directive 2007/2/CE du Parlement européen
                          et du Conseil en ce qui concerne l'interopérabilité des séries et des services de données
                          géographiques
                        </gmd:LocalisedCharacterString>
                      </gmd:textGroup>
                      <gmd:textGroup>
                        <gmd:LocalisedCharacterString locale="#IT">Regolamento (UE) n. 1089/2010 della Commissione, del
                          23 novembre 2010 , recante attuazione della direttiva 2007/2/CE del Parlamento europeo e del
                          Consiglio per quanto riguarda l'interoperabilità dei set di dati territoriali e dei servizi di
                          dati territoriali
                        </gmd:LocalisedCharacterString>
                      </gmd:textGroup>
                      <gmd:textGroup>
                        <gmd:LocalisedCharacterString locale="#EN">Commission Regulation (EU) No 1089/2010 of 23
                          November 2010 implementing Directive 2007/2/EC of the European Parliament and of the Council
                          as regards interoperability of spatial data sets and services
                        </gmd:LocalisedCharacterString>
                      </gmd:textGroup>
                    </gmd:PT_FreeText>
                  </gmd:title>
                  <gmd:date>
                    <gmd:CI_Date>
                      <gmd:date>
                        <gco:Date>2010-12-08</gco:Date>
                      </gmd:date>
                      <gmd:dateType>
                        <gmd:CI_DateTypeCode
                          codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_DateTypeCode"
                          codeListValue="publication"/>
                      </gmd:dateType>
                    </gmd:CI_Date>
                  </gmd:date>
                </gmd:CI_Citation>
              </gmd:specification>
              <gmd:explanation>
                <gco:CharacterString>See the referenced specification</gco:CharacterString>
              </gmd:explanation>
              <gmd:pass gco:nilReason="unknown"/>
            </gmd:DQ_ConformanceResult>
          </gmd:result>
        </gmd:DQ_DomainConsistency>
      </gmd:report>
    </xsl:if>
    <xsl:if test="$isService and not($hasQualityReport9762009)">
      <xsl:message select="concat('INSPIRE|', $uuid, '|DataQuality|Regulation: add conformity for service.')"/>
      <gmd:report>
        <gmd:DQ_DomainConsistency>
          <gmd:result>
            <gmd:DQ_ConformanceResult>
              <gmd:specification>
                <gmd:CI_Citation>
                  <gmd:title xsi:type="gmd:PT_FreeText_PropertyType">
                    <gmx:Anchor xlink:href="http://data.europa.eu/eli/reg/2009/976"></gmx:Anchor>
                    <gmd:PT_FreeText>
                      <gmd:textGroup>
                        <gmd:LocalisedCharacterString locale="#DE">Verordnung (EG) Nr. 976/2009 der Kommission vom 19.
                          Oktober 2009 zur Durchführung der Richtlinie 2007/2/EG des Europäischen Parlaments und des
                          Rates hinsichtlich der Netzdienste
                        </gmd:LocalisedCharacterString>
                      </gmd:textGroup>
                      <gmd:textGroup>
                        <gmd:LocalisedCharacterString locale="#FR">Règlement (CE) n o 976/2009 de la Commission du 19
                          octobre 2009 portant modalités d’application de la directive 2007/2/CE du Parlement européen
                          et du Conseil en ce qui concerne les services en réseau
                        </gmd:LocalisedCharacterString>
                      </gmd:textGroup>
                      <gmd:textGroup>
                        <gmd:LocalisedCharacterString locale="#IT">Regolamento (CE) n. 976/2009 della Commissione, del
                          19 ottobre 2009 , recante attuazione della direttiva 2007/2/CE del Parlamento europeo e del
                          Consiglio per quanto riguarda i servizi di rete
                        </gmd:LocalisedCharacterString>
                      </gmd:textGroup>
                      <gmd:textGroup>
                        <gmd:LocalisedCharacterString locale="#EN">Commission Regulation (EC) No 976/2009 of 19 October
                          2009 implementing Directive 2007/2/EC of the European Parliament and of the Council as regards
                          the Network Services
                        </gmd:LocalisedCharacterString>
                      </gmd:textGroup>
                    </gmd:PT_FreeText>
                  </gmd:title>
                  <gmd:date>
                    <gmd:CI_Date>
                      <gmd:date>
                        <gco:Date>2009-10-19</gco:Date>
                      </gmd:date>
                      <gmd:dateType>
                        <gmd:CI_DateTypeCode
                          codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_DateTypeCode"
                          codeListValue="publication"/>
                      </gmd:dateType>
                    </gmd:CI_Date>
                  </gmd:date>
                </gmd:CI_Citation>
              </gmd:specification>
              <gmd:explanation>
                <gco:CharacterString>See the referenced specification</gco:CharacterString>
              </gmd:explanation>
              <gmd:pass gco:nilReason="unknown"/>
            </gmd:DQ_ConformanceResult>
          </gmd:result>
        </gmd:DQ_DomainConsistency>
      </gmd:report>
    </xsl:if>
  </xsl:template>

  <xsl:template name="create-dq-section">
    <gmd:dataQualityInfo>
      <gmd:DQ_DataQuality>
        <gmd:scope>
          <gmd:DQ_Scope>
            <gmd:level>
              <gmd:MD_ScopeCode
                codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#MD_ScopeCode"
                codeListValue="{if ($isService) then 'service' else 'dataset'}"/>
            </gmd:level>
          </gmd:DQ_Scope>
        </gmd:scope>
        <xsl:call-template name="add-inspire-regulation-conformity"/>
        <gmd:lineage>
          <gmd:LI_Lineage>
            <gmd:statement xsi:type="gmd:PT_FreeText_PropertyType">
              <gco:CharacterString>-</gco:CharacterString>
            </gmd:statement>
          </gmd:LI_Lineage>
        </gmd:lineage>
      </gmd:DQ_DataQuality>
    </gmd:dataQualityInfo>
  </xsl:template>

  <xsl:template match="srv:serviceType/gco:LocalName[. = 'OGC:CSW']">
    <xsl:message select="concat('INSPIRE|', $uuid, '|Service|Replace OGC:CSW by discovery.')"/>
    <gco:LocalName>discovery</gco:LocalName>
  </xsl:template>
  <xsl:template match="srv:serviceType/gco:LocalName[. = 'OGC:WFS']">
    <xsl:message select="concat('INSPIRE|', $uuid, '|Service|Replace OGC:WFS by download.')"/>
    <gco:LocalName>download</gco:LocalName>
  </xsl:template>
  <xsl:template match="srv:serviceType/gco:LocalName[. = ('OGC:WMS', 'OGC:WMTS', 'OGC:WMTS (Web Map Tile Service)')]">
    <xsl:message select="concat('INSPIRE|', $uuid, '|Service|Replace OGC:WMT?S by view.')"/>
    <gco:LocalName>view</gco:LocalName>
  </xsl:template>


  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()|comment()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|comment()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>

</xsl:stylesheet>
