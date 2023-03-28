<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:che="http://www.geocat.ch/2008/che"
                version="1.0"
                exclude-result-prefixes="gmd">

  <!-- ================================================================= -->

  <xsl:template match="/root">
    <xsl:apply-templates select="*[name() != 'env']"/>
  </xsl:template>

  <!-- ================================================================= -->

  <xsl:template match="che:CHE_MD_Metadata">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <gmd:fileIdentifier>
        <gco:CharacterString>
          <xsl:value-of select="/root/env/uuid"/>
        </gco:CharacterString>
      </gmd:fileIdentifier>
      <xsl:apply-templates select="node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- ================================================================= -->

  <xsl:template match="gmd:fileIdentifier"/>

  <!-- ================================================================= -->

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- ================================================================= -->

</xsl:stylesheet>
