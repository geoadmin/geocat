<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gts="http://www.isotc211.org/2005/gts"
                xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gml="http://www.opengis.net/gml" xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gn="http://www.fao.org/geonetwork"
                xmlns:gn-fn-core="http://geonetwork-opensource.org/xsl/functions/core"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                xmlns:gn-fn-iso19139="http://geonetwork-opensource.org/xsl/functions/profiles/iso19139"
                xmlns:exslt="http://exslt.org/common" exclude-result-prefixes="#all">


  <xsl:include href="utility-tpl.xsl"/>

  <!-- Visit all XML tree recursively -->
  <xsl:template mode="mode-iso19139.che" match="*|@*">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>

    <xsl:apply-templates mode="mode-iso19139" select=".">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="labels" select="$labels"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template mode="mode-iso19139.che" match="che:*">
    <xsl:apply-templates mode="mode-iso19139" select=".">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="labels" select="$labels"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template mode="mode-iso19139" match="gmd:geographicElement" priority="2000">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>

    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>

    <xsl:call-template name="render-boxed-element">
      <xsl:with-param name="label"
                      select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), $isoType, $xpath)/label"/>
      <xsl:with-param name="editInfo" select="gn:element"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="isDisabled" select="'true'"/>
      <xsl:with-param name="subTreeSnippet">
        <xsl:variable name="hasXlink" select="@xlink:href"/>

        <xsl:choose>
          <xsl:when test="gmd:EX_GeographicBoundingBox">

            <img src="region.getmap.png?mapsrs=EPSG:21781&amp;width=250&amp;background=geocat&amp;geom=Polygon(({*/gmd:westBoundLongitude/gco:Decimal}%20{*/gmd:northBoundLatitude/gco:Decimal},{*/gmd:eastBoundLongitude/gco:Decimal}%20{*/gmd:northBoundLatitude/gco:Decimal},{*/gmd:eastBoundLongitude/gco:Decimal}%20{*/gmd:southBoundLatitude/gco:Decimal},{*/gmd:westBoundLongitude/gco:Decimal}%20{*/gmd:southBoundLatitude/gco:Decimal},{*/gmd:westBoundLongitude/gco:Decimal}%20{*/gmd:northBoundLatitude/gco:Decimal}))&amp;geomsrs=EPSG:4326"/>

          </xsl:when>
          <xsl:when test="gmd:EX_BoundingPolygon">
            <img class="gn-img-extent"
                 src="region.getmap.png?mapsrs=EPSG:21781&amp;width=250&amp;background=geocat&amp;id=metadata:@id{$metadataId}:@xpathgmd:identificationInfo{$xpath}/gmd:EX_BoundingPolygon"/>
          </xsl:when>
        </xsl:choose>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

</xsl:stylesheet>