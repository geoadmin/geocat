<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:java="java:org.fao.geonet.util.XslUtil"
                exclude-result-prefixes="#all">

  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

  <xsl:template match="geonet:info" priority="2"/>

  <xsl:template match="/root">
    <xsl:choose>
      <!-- Export 19139 XML and related profil (just a copy)-->
      <xsl:when test="gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']">
        <xsl:apply-templates select="gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']"/>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']" priority="101">
    <gmd:MD_Metadata>
      <xsl:attribute name="xsi:schemaLocation">http://www.isotc211.org/2005/gmd http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd</xsl:attribute>
      <xsl:apply-templates select="*"/>
    </gmd:MD_Metadata>
  </xsl:template>

  <!-- Rename all ISO profil elements based on ISO -->
  <xsl:template match="*[@gco:isoType]" priority="100">
    <xsl:element name="{@gco:isoType}">
      <xsl:apply-templates select="*"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="che:*[not(@gco:isoType)]" priority="100"/>

  <xsl:template match="@xsi:type[.='che:PT_FreeURL_PropertyType']" priority="2">
    <xsl:apply-templates mode="url" select=".."/>
  </xsl:template>

  <xsl:template match="gmd:linkage">
    <gmd:linkage>
      <xsl:apply-templates mode="url" select="."/>
    </gmd:linkage>
  </xsl:template>

  <xsl:template mode="url" match="*">
    <xsl:choose>
      <xsl:when test="./che:LocalisedURL">
        <gmd:URL>
          <xsl:value-of select="./che:LocalisedURL"/>
        </gmd:URL>
      </xsl:when>
      <xsl:when test="not(./gmd:URL) and .//che:LocalisedURL[string(@locale) = string($langId)]">
        <gmd:URL>
          <xsl:value-of select=".//che:LocalisedURL[string(@locale) = $langId]"/>
        </gmd:URL>
      </xsl:when>
      <xsl:when test="not(./gmd:URL)">
        <gmd:URL>
          <xsl:value-of select=".//che:LocalisedURL[1]"/>
        </gmd:URL>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="./gmd:URL"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="@xlink:*[name(..)!='srv:operatesOn']">
    <!--
        do not copy xlinks because they should be filled out and are not always viable when importing
        into other systems
        Note: exception for xlinks in srv:operatesOn (these are kept)
     -->
  </xsl:template>


  <!-- Remove all ISO profil specific elements
  <xsl:template match="*[not(@gco:isoType) and
    (namespace-uri(.) != 'http://www.isotc211.org/2005/gmd' or
    namespace-uri(.) != 'http://www.isotc211.org/2005/gts' or
    namespace-uri(.) != 'http://www.opengis.net/gml' or
    namespace-uri(.) != 'http://www.isotc211.org/2005/gco')]" priority="100"/>-->

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()[name(self::*)!='geonet:info']"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove all extended categories -->
  <xsl:template match="gmd:topicCategory[contains(gmd:MD_TopicCategoryCode,'_')]">
  </xsl:template>

</xsl:stylesheet>
