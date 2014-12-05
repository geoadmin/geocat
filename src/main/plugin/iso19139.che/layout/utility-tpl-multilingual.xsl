<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="#all">


  <xsl:template name="get-iso19139.che-language">
    <xsl:call-template name="get-iso19139-language"/>
  </xsl:template>

  <xsl:template name="get-iso19139.che-other-languages">
    <xsl:call-template name="get-iso19139-other-languages"/>
  </xsl:template>

  <xsl:template name="get-iso19139.che-other-languages-as-json">
    <xsl:call-template name="get-iso19139-other-languages-as-json"/>
  </xsl:template>

</xsl:stylesheet>
