<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:che="http://www.geocat.ch/2008/che"
                version="2.0"
                exclude-result-prefixes="gmd xsl geonet che">

  <xsl:template match="che:CHE_MD_Metadata">
    <changes>
      <xsl:apply-templates select="@*|node()"/>
    </changes>
  </xsl:template>

  <xsl:template match="*[@geonet:change]">
    <change>
      <fieldid>
        <xsl:value-of select="@geonet:change"/>
      </fieldid>
      <originalval>
        <xsl:value-of select="@geonet:original"/>
      </originalval>
      <changedval>
        <xsl:value-of select="@geonet:new"/>
      </changedval>
    </change>
  </xsl:template>


  <xsl:template match="@*|node()">
    <xsl:apply-templates select="@*|node()"/>
  </xsl:template>

</xsl:stylesheet>
