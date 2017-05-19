<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:include href="utility-tpl-multilingual.xsl"/>

  <xsl:template name="get-iso19139.che-is-service">
    <xsl:value-of
      select="count(
                $metadata/gmd:identificationInfo/che:CHE_SV_ServiceIdentification|
                $metadata/gmd:identificationInfo/srv:SV_ServiceIdentification) > 0"/>
  </xsl:template>

  <xsl:template name="get-iso19139.che-title">
    <xsl:value-of select="$metadata/gmd:identificationInfo/*/gmd:citation/*/gmd:title/gco:CharacterString"/>
  </xsl:template>

  <xsl:template name="get-iso19139.che-extents-as-json">
    <xsl:call-template name="get-iso19139-extents-as-json"/>
  </xsl:template>

  <xsl:template name="get-iso19139.che-online-source-config">
    <xsl:param name="pattern"/>
    <xsl:call-template name="get-iso19139-online-source-config">
      <xsl:with-param name="pattern" select="$pattern"/>
    </xsl:call-template>
  </xsl:template>
</xsl:stylesheet>
