<?xml version="1.0" encoding="UTF-8"?>
<!--
  Create a simple XML tree for relation description.
  <relations>
    <relation type="related|services|children">
      + super-brief representation.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:gn-fn-rel="http://geonetwork-opensource.org/xsl/functions/relations"
                version="2.0"
                exclude-result-prefixes="#all">

  <!-- Convert an element gco:CharacterString
  to the GN localized string structure -->
  <xsl:template mode="get-iso19139-localized-url" match="*">

    <xsl:variable name="metadata"
                  select="ancestor::metadata/*[@gco:isoType='gmd:MD_Metadata' or name()='gmd:MD_Metadata']"/>
    <xsl:variable name="mainLanguage"
                  select="string($metadata/gmd:language/gco:CharacterString|
                                 $metadata/gmd:language/gmd:LanguageCode/@codeListValue)"/>
    <xsl:variable name="otherLanguage">
      <xsl:copy-of select="concat('#', upper-case(util:twoCharLangCode($mainLanguage, '')))"/>
        <xsl:for-each select="$metadata/gmd:locale/gmd:PT_Locale/@id">
          <xsl:copy-of select="concat('#', .)"/>
        </xsl:for-each>
    </xsl:variable>
    <xsl:for-each select="gmd:URL|che:PT_FreeURL/*/che:LocalisedURL">
      <!-- GEOCAT, as when url not defined for ui language, the first one returned by getRelated is used,
      sort getrelated ouput by other languages, adding main language at first index if exist -->
      <xsl:sort select="string((string-length(substring-before($otherLanguage, @locale)) - 1) div 4)"/>
      <xsl:variable name="localeId" select="substring-after(@locale, '#')"/>
      <value lang="{if (@locale)
                  then $metadata/gmd:locale/*[@id = $localeId]/gmd:languageCode/*/@codeListValue
                  else if ($mainLanguage) then $mainLanguage else $lang}">
        <xsl:value-of select="string(.)"/>
      </value>
    </xsl:for-each>
  </xsl:template>


  <!-- Relation contained in the metadata record has to be returned
  It could be document or thumbnails
  -->
  <xsl:template mode="relation"
                match="metadata[gmd:MD_Metadata or *[contains(@gco:isoType, 'MD_Metadata')]]"
                priority="299">

    <xsl:if test="count(*/descendant::*[name(.) = 'gmd:graphicOverview']/*) > 0">
      <thumbnails>
        <xsl:for-each select="*/descendant::*[name(.) = 'gmd:graphicOverview']/*">
          <item>
            <id>
              <xsl:value-of select="gmd:fileName/gco:CharacterString"/>
            </id>
            <url>
              <xsl:apply-templates mode="get-iso19139-localized-string"
                                   select="gmd:fileName"/>
            </url>
            <title>
              <xsl:apply-templates mode="get-iso19139-localized-string"
                                   select="gmd:fileDescription"/>
            </title>
            <type>thumbnail</type>
          </item>
        </xsl:for-each>
      </thumbnails>
    </xsl:if>

    <xsl:variable name="links" select="*/descendant::*[name(.) = 'gmd:onLine']/*[
                                    gmd:linkage/gmd:URL!='' or
                                    gmd:linkage/che:PT_FreeURL//che:LocalisedURL[text() != ''] or
                                    gmd:linkage/che:LocalisedURL!='']"/>

    <xsl:if test="count($links) > 0">
      <onlines>
        <xsl:for-each select="$links">

          <item>
            <xsl:variable name="langCode">
              <xsl:value-of select="concat('#', upper-case(util:twoCharLangCode($lang, 'EN')))"/>
            </xsl:variable>
            <xsl:variable name="url" select="gmd:linkage/gmd:URL"/>
            <id>
              <xsl:value-of select="$url"/>
            </id>
            <title>
              <xsl:apply-templates mode="get-iso19139-localized-string"
                                   select="gmd:name"/>
            </title>
            <url>
              <xsl:apply-templates mode="get-iso19139-localized-url"
                                   select="gmd:linkage"/>
            </url>
            <function>
              <xsl:value-of select="gmd:function/*/@codeListValue"/>
            </function>
            <applicationProfile>
              <xsl:value-of select="gmd:applicationProfile/gco:CharacterString"/>
            </applicationProfile>
            <description>
              <xsl:apply-templates mode="get-iso19139-localized-string"
                                   select="gmd:description"/>
            </description>
            <protocol>
              <xsl:value-of select="gn-fn-rel:translate(gmd:protocol, $langCode)"/>
            </protocol>
            <type>onlinesrc</type>
          </item>
        </xsl:for-each>
      </onlines>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
