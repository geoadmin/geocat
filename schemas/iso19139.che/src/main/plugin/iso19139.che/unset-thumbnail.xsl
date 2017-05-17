<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:che="http://www.geocat.ch/2008/che"
                version="1.0">

  <!-- ================================================================= -->

  <xsl:template match="/root">
    <xsl:apply-templates select="che:CHE_MD_Metadata"/>
  </xsl:template>

  <!-- ================================================================= -->

  <xsl:template priority="5"
                match="gmd:graphicOverview[gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString = /root/env/id]"/>

  <!-- ================================================================= -->

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="geonet:info" priority="2"/>
</xsl:stylesheet>
