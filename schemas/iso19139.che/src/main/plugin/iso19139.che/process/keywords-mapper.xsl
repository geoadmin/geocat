<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:exslt="http://exslt.org/common"
                xmlns:geonet="http://www.fao.org/geonetwork" xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd" version="1.0" exclude-result-prefixes="exslt">

  <!-- The keyword separator -->
  <xsl:param name="separator">;</xsl:param>
  <!-- The keyword to search for -->
  <xsl:param name="search">key1;key2/</xsl:param>
  <!-- The keyword to put in -->
  <xsl:param name="replace">newkey1;newkey2</xsl:param>

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>

  <!-- Build a map from the inputs parameters -->
  <xsl:variable name="map">
    <xsl:for-each select="tokenize($search, $separator)">
      <xsl:variable name="pos" select="position()"/>
      <map key="{.}" value="{tokenize($replace, $separator)[position() = $pos]}"/>
    </xsl:for-each>
  </xsl:variable>

  <!-- Map all keywords to new value.
      If no new value define, current value is used. -->
  <xsl:template match="gmd:keyword" priority="2">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>

      <xsl:variable name="mapNodes" select="exslt:node-set($map)"/>
      <xsl:variable name="currentValue" select="gco:CharacterString"/>
      <xsl:variable name="newValue" select="$mapNodes/map[@key=$currentValue]/@value"/>
      <!--<xsl:message>Mapping '<xsl:value-of select="$currentValue"/>' with '<xsl:value-of select="$newValue"/>'</xsl:message>-->
      <gco:CharacterString>
        <xsl:choose>
          <xsl:when test="$newValue!=''">
            <xsl:value-of select="$newValue"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$currentValue"/>
          </xsl:otherwise>
        </xsl:choose>
      </gco:CharacterString>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
