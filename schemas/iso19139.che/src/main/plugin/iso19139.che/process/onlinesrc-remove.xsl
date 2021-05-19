<?xml version="1.0" encoding="UTF-8"?>
<!--
Stylesheet used to remove a reference to a online resource.
-->
<xsl:stylesheet xmlns:geonet="http://www.fao.org/geonetwork" xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0">

  <xsl:param name="url"/>
  <xsl:param name="name"/>

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template
    match="geonet:*|gmd:onLine[normalize-space(gmd:CI_OnlineResource/gmd:linkage/gmd:URL) = $url and normalize-space(gmd:CI_OnlineResource/gmd:name/gco:CharacterString) = $name]"
    priority="2"/>
  <xsl:template
    match="geonet:*|gmd:onLine[count(gmd:CI_OnlineResource/gmd:linkage/che:PT_FreeURL/che:URLGroup[normalize-space(che:LocalisedURL) = normalize-space($url)]) > 0 and count(gmd:CI_OnlineResource/gmd:name/gmd:PT_FreeText/gmd:textGroup[normalize-space(gmd:LocalisedCharacterString) = normalize-space($name)]) > 0]"
    priority="2"/>
  <xsl:template
    match="geonet:*|gmd:onLine[count(gmd:CI_OnlineResource/gmd:linkage/che:PT_FreeURL/che:URLGroup[normalize-space(che:LocalisedURL) = normalize-space($url)]) > 0 and normalize-space(gmd:CI_OnlineResource/gmd:name/gco:CharacterString) = $name]"
    priority="2"/>
  <xsl:template
    match="geonet:*|gmd:onLine[normalize-space(gmd:CI_OnlineResource/gmd:linkage/gmd:URL) = $url and normalize-space(gmd:CI_OnlineResource/gmd:name//gmd:LocalisedCharacterString) = $name]"
    priority="2"/>
  <xsl:template
    match="geonet:*|gmd:onLine[normalize-space(gmd:CI_OnlineResource/gmd:linkage/gmd:URL) = $url and normalize-space(gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString) = 'WWW:DOWNLOAD-1.0-http--download']"
    priority="2"/>
  <xsl:template
    match="geonet:*|gmd:onLine[normalize-space(gmd:CI_OnlineResource/gmd:linkage/che:LocalisedURL) = $url and normalize-space(gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString) = 'WWW:DOWNLOAD-1.0-http--download']"
    priority="2"/>
</xsl:stylesheet>
