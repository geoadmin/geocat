<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd" version="1.0">

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="gmd:dataSetURI/gco:CharacterString[not(starts-with(text(), 'http://')) and not(starts-with(text(), 'https://'))]" priority="2">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:value-of select="concat('https://', .)"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
