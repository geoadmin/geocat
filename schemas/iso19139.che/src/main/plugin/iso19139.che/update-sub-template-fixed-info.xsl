<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                version="2.0"
                exclude-result-prefixes="#all">


  <!-- ================================================================= -->
  <xsl:variable name="countryNameMapping">
    <cnt name="schweiz">CH</cnt>
    <cnt name="suisse">CH</cnt>
    <cnt name="switzerland">CH</cnt>
    <cnt name="svizzera">CH</cnt>

    <cnt name="deutschland">DE</cnt>
    <cnt name="germany">DE</cnt>
    <cnt name="allemagne">DE</cnt>
    <cnt name="germania">DE</cnt>

    <cnt name="france">FR</cnt>
    <cnt name="frankreich">FR</cnt>
    <cnt name="francia">FR</cnt>

    <cnt name="austria">AT</cnt>
    <cnt name="osterreich">AT</cnt>
    <cnt name="österreich">AT</cnt>
    <cnt name="autriche">AT</cnt>

    <cnt name="italie">IT</cnt>
    <cnt name="italy">IT</cnt>
    <cnt name="italien">IT</cnt>
    <cnt name="italia">IT</cnt>

    <cnt name="liechtenstein">LI</cnt>
  </xsl:variable>

  <xsl:template match="/root" priority="5">
    <xsl:apply-templates select="/root/*[local-name() != 'env']"/>
  </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="gmd:country/gco:CharacterString" priority="100">
    <xsl:variable name="country" select="lower-case(text())"/>
    <xsl:choose>
      <xsl:when test="$countryNameMapping/cnt[@name = $country]">
        <gco:CharacterString>
          <xsl:value-of select="$countryNameMapping/cnt[@name = $country]/text()"/>
        </gco:CharacterString>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
