<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                exclude-result-prefixes="#all"
                version="2.0">

  <!-- Migration process for iso19139.che metadata for version 3.4 -->
  <xsl:output method="xml" indent="yes"/>

  <!--
  Remove hardcoded schemaLocation in records. Schema location
  is added by the application depending on the schema configuration
  (see schema-ident.xml).
  https://jira.swisstopo.ch/browse/MGEO_SB-2

  Try to cleanup namespaces by defining them at the root element level.
  -->
  <xsl:template match="che:CHE_MD_Metadata">
    <xsl:copy copy-namespaces="no">
      <xsl:namespace name="che" select="'http://www.geocat.ch/2008/che'"/>
      <xsl:namespace name="gmd" select="'http://www.isotc211.org/2005/gmd'"/>
      <xsl:namespace name="gco" select="'http://www.isotc211.org/2005/gco'"/>
      <xsl:namespace name="gmx" select="'http://www.isotc211.org/2005/gmx'"/>
      <xsl:namespace name="srv" select="'http://www.isotc211.org/2005/srv'"/>
      <xsl:namespace name="gml" select="'http://www.opengis.net/gml'"/>
      <xsl:namespace name="xsi" select="'http://www.w3.org/2001/XMLSchema-instance'"/>
      <xsl:namespace name="xlink" select="'http://www.w3.org/1999/xlink'"/>

      <xsl:apply-templates select="@*|*"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="@xsi:schemaLocation"/>



  <!-- Encode language following INSPIRE rules -->
  <xsl:template match="gmd:language[gco:CharacterString]">
    <xsl:element name="{name()}">
      <xsl:apply-templates select="@*"/>
      <gmd:LanguageCode codeList="http://www.loc.gov/standards/iso639-2/"
                        codeListValue="{gco:CharacterString}"/>
    </xsl:element>
  </xsl:template>



  <!--
  Parent identifier is replaced by aggregate with
  association type 'crossReference'.
  https://jira.swisstopo.ch/browse/MGEO_SB-73
  -->
  <xsl:template match="gmd:parentIdentifier"/>

  <xsl:template match="gmd:identificationInfo/*">
    <xsl:element name="{name()}">
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="gmd:citation"/>
      <xsl:apply-templates select="gmd:abstract"/>
      <xsl:apply-templates select="gmd:purpose"/>
      <xsl:apply-templates select="gmd:credit"/>
      <xsl:apply-templates select="gmd:status"/>
      <xsl:apply-templates select="gmd:pointOfContact"/>
      <xsl:apply-templates select="gmd:resourceMaintenance"/>
      <xsl:apply-templates select="gmd:graphicOverview"/>
      <xsl:apply-templates select="gmd:resourceFormat"/>
      <xsl:apply-templates select="gmd:descriptiveKeywords"/>
      <xsl:apply-templates select="gmd:resourceSpecificUsage"/>
      <xsl:apply-templates select="gmd:resourceConstraints"/>
      <xsl:apply-templates select="gmd:aggregationInfo"/>

      <!-- Move the parent identifier to an aggregate-->
      <xsl:variable name="parentIdentifier"
                    select="ancestor::che:CHE_MD_Metadata/gmd:parentIdentifier/gco:CharacterString"/>
      <xsl:if test="normalize-space($parentIdentifier) != ''">
        <gmd:aggregationInfo>
          <gmd:MD_AggregateInformation>
            <gmd:aggregateDataSetIdentifier>
              <gmd:MD_Identifier>
                <gmd:code>
                  <gco:CharacterString>
                    <xsl:value-of select="$parentIdentifier"/>
                  </gco:CharacterString>
                </gmd:code>
              </gmd:MD_Identifier>
            </gmd:aggregateDataSetIdentifier>
            <gmd:associationType>
              <gmd:DS_AssociationTypeCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#DS_AssociationTypeCode"
                                          codeListValue="largerWorkCitation"/>
            </gmd:associationType>
          </gmd:MD_AggregateInformation>
        </gmd:aggregationInfo>
      </xsl:if>

      <xsl:apply-templates select="gmd:spatialRepresentationType"/>
      <xsl:apply-templates select="gmd:spatialResolution"/>
      <xsl:apply-templates select="gmd:language"/>
      <xsl:apply-templates select="gmd:characterSet"/>
      <xsl:apply-templates select="gmd:topicCategory"/>
      <xsl:apply-templates select="gmd:environmentDescription"/>
      <xsl:apply-templates select="gmd:extent"/>
      <xsl:apply-templates select="gmd:supplementalInformation"/>

      <xsl:apply-templates select="srv:*"/>
      <xsl:apply-templates select="che:*"/>
    </xsl:element>
  </xsl:template>

  <!-- Do a copy of every nodes (removing extra namespaces) -->
  <xsl:template match="*">
    <xsl:element name="{name()}">
      <xsl:apply-templates select="@*|node()"/>
    </xsl:element>
  </xsl:template>

  <!-- Do a copy of every attributes and text node. -->
  <xsl:template match="@*|text()">
    <xsl:copy/>
  </xsl:template>

  <!-- Always remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>

</xsl:stylesheet>
