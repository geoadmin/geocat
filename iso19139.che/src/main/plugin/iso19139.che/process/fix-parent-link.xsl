<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd" version="1.0">

  <!-- root element -->
	<xsl:template match="che:CHE_MD_Metadata">
    <xsl:element name="{name()}">
      <xsl:namespace name="che" select="'http://www.geocat.ch/2008/che'"/>
      <xsl:namespace name="gmd" select="'http://www.isotc211.org/2005/gmd'"/>
      <xsl:namespace name="gmx" select="'http://www.isotc211.org/2005/gmx'"/>
      <xsl:namespace name="gts" select="'http://www.isotc211.org/2005/gts'"/>
      <xsl:namespace name="srv" select="'http://www.isotc211.org/2005/srv'"/>
      <xsl:namespace name="gml" select="'http://www.opengis.net/gml'"/>
      <xsl:namespace name="xsi" select="'http://www.w3.org/2001/XMLSchema-instance'"/>
      <xsl:namespace name="xlink" select="'http://www.w3.org/1999/xlink'"/>

      <xsl:apply-templates select="@*|*"/>

      <!-- add a parent identifier in place of aggregation info -->
      <xsl:variable name="aggInfo" select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:aggregationInfo/gmd:MD_AggregateInformation"/>
      <xsl:variable name="parentUuid" select="$aggInfo[gmd:associationType/gmd:DS_AssociationTypeCode/@codeListValue='largerWorkCitation']/gmd:aggregateDataSetIdentifier/gmd:MD_Identifier/gmd:code/gco:CharacterString"/>
      <xsl:if test="$parentUuid != '' and count(gmd:parentIdentifier) = 0">
        <gmd:parentIdentifier>
          <gco:CharacterString><xsl:value-of select="$parentUuid"/></gco:CharacterString>
        </gmd:parentIdentifier>
      </xsl:if>
    </xsl:element>
	</xsl:template>

	<!--Copy -->
  <xsl:template match="@* | node()" priority="-10">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- remove aggregation info with largerWorkCitation type -->
	<xsl:template match="gmd:aggregationInfo[./gmd:MD_AggregateInformation/gmd:associationType/gmd:DS_AssociationTypeCode/@codeListValue='largerWorkCitation']">
	</xsl:template>

  <!-- Always remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>

</xsl:stylesheet>
