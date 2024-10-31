<?xml version="1.0" encoding="UTF-8"?>
<!--
Stylesheet used to update metadata for a service and
detach a dataset metadata
-->
<xsl:stylesheet xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:geonet="http://www.fao.org/geonetwork"
                version="2.0"
>

  <xsl:param name="uuidref"/>

  <!-- Detach -->
  <!-- Remove attributes uuidref and xlink:href -->
  <xsl:template
    match="gmd:contentInfo/gmd:MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation[@uuidref=$uuidref]"
    priority="2">
    <xsl:copy>
      <xsl:apply-templates select="node()"/>
    </xsl:copy>
  </xsl:template>


  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>

</xsl:stylesheet>
