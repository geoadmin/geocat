<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gts="http://www.isotc211.org/2005/gts"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:sdi="http://www.easysdi.org/2011/sdi"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                exclude-result-prefixes="#all"
                version="2.0">

  <!-- Migration process for iso19139.che metadata for version 3.4 -->
  <xsl:output method="xml" indent="yes"/>

  <!-- MGDI fix: remove duplicate localised strings -->
  <xsl:template match="gmd:textGroup[./gmd:LocalisedCharacterString]">
    <xsl:variable name="locale" select="./gmd:LocalisedCharacterString/@locale"/>
    <xsl:if test="count(preceding-sibling::gmd:textGroup[./gmd:LocalisedCharacterString/@locale = $locale]) = 0">
      <xsl:copy-of select="." copy-namespaces="no"/>
    </xsl:if>
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
