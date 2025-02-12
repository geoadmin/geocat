<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2001-2016 Food and Agriculture Organization of the
  ~ United Nations (FAO-UN), United Nations World Food Programme (WFP)
  ~ and United Nations Environment Programme (UNEP)
  ~
  ~ This program is free software; you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation; either version 2 of the License, or (at
  ~ your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful, but
  ~ WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  ~ General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program; if not, write to the Free Software
  ~ Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
  ~
  ~ Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
  ~ Rome - Italy. email: geonetwork@osgeo.org
  -->

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
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:digestUtils="java:org.apache.commons.codec.digest.DigestUtils"
                xmlns:gn-fn-rel="http://geonetwork-opensource.org/xsl/functions/relations"
                xmlns:che="http://www.geocat.ch/2008/che"
                version="2.0"
                exclude-result-prefixes="#all">

  <!-- Convert an element gco:CharacterString
  to the GN localized string structure -->
  <xsl:template mode="get-iso19139-localized-string" match="*">

    <xsl:variable name="mainLanguage">
      <xsl:call-template name="langId_from_gmdlanguage19139">
          <xsl:with-param name="gmdlanguage" select="ancestor::metadata/*[@gco:isoType='che:CHE_MD_Metadata' or name()='che:CHE_MD_Metadata']/gmd:language"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:for-each select="gco:CharacterString|gmx:Anchor|
                          gmd:PT_FreeText/*/gmd:LocalisedCharacterString">
      <xsl:variable name="localeId"
                    select="substring-after(@locale, '#')"/>

      <value lang="{if (@locale)
                  then ancestor::metadata/*[@gco:isoType='che:CHE_MD_Metadata' or name()='che:CHE_MD_Metadata']/gmd:locale/*[@id = $localeId]/gmd:languageCode/*/@codeListValue
                  else if ($mainLanguage) then $mainLanguage else $lang}">
        <xsl:copy-of select="@xlink:href"/>
        <xsl:value-of select="."/>
      </value>
    </xsl:for-each>
  </xsl:template>

  <!-- Convert an element
  to the GN localized url structure -->
  <xsl:template mode="get-iso19139-localized-url" match="*">

    <xsl:variable name="metadata"
                  select="ancestor::metadata/*[@gco:isoType='che:CHE_MD_Metadata' or name()='che:CHE_MD_Metadata']"/>
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
  It could be a document or thumbnails
  -->
  <xsl:template mode="relation"
                match="metadata[che:CHE_MD_Metadata or *[contains(@gco:isoType, 'che:CHE_MD_Metadata')]]"
                priority="299">

    <xsl:variable name="mainLanguage">
      <xsl:call-template name="langId_from_gmdlanguage19139">
          <xsl:with-param name="gmdlanguage" select="*/gmd:language"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:if test="count(*//gmd:graphicOverview) > 0">
      <thumbnails>
        <xsl:for-each select="*//gmd:graphicOverview">
          <item>
            <id>
              <xsl:value-of select="gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString"/>
            </id>
            <idx>
              <xsl:value-of select="position()"/>
            </idx>
            <hash>
              <xsl:value-of select="digestUtils:md5Hex(normalize-space(.))"/>
            </hash>
            <url>
              <xsl:apply-templates mode="get-iso19139-localized-string"
                                   select="gmd:MD_BrowseGraphic/gmd:fileName"/>
            </url>
            <title>
              <xsl:apply-templates mode="get-iso19139-localized-string"
                                   select="gmd:MD_BrowseGraphic/gmd:fileDescription"/>
            </title>
            <type>thumbnail</type>
          </item>
        </xsl:for-each>
      </thumbnails>
    </xsl:if>

    <xsl:variable name="links" select="*/gmd:onLine"/>
    <xsl:if test="count($links) > 0">
      <onlines>
        <xsl:for-each select="$links">
          <xsl:if test="gmd:CI_OnlineResource[gmd:linkage/gmd:URL!='' or gmd:linkage/che:PT_FreeURL//che:LocalisedURL[text() != ''] or gmd:linkage/che:LocalisedURL!='']">
          <item>
              <xsl:variable name="langCode">
                <xsl:value-of select="concat('#', upper-case(util:twoCharLangCode($lang, 'EN')))"/>
              </xsl:variable>
              <xsl:variable name="url" select="gmd:CI_OnlineResource/gmd:linkage/gmd:URL"/>
              <id>
                <xsl:value-of select="$url"/>
              </id>
              <idx>
                <xsl:value-of select="position()"/>
              </idx>
              <hash>
                <xsl:value-of select="digestUtils:md5Hex(normalize-space(.))"/>
              </hash>
              <title>
                <xsl:apply-templates mode="get-iso19139-localized-string"
                                     select="gmd:CI_OnlineResource/gmd:name"/>
              </title>
              <url>
                <xsl:apply-templates mode="get-iso19139-localized-url" select="gmd:linkage"/>
              </url>
              <function>
                <xsl:value-of select="gmd:CI_OnlineResource/gmd:function/*/@codeListValue"/>
              </function>
              <applicationProfile>
                <xsl:value-of select="gmd:CI_OnlineResource/gmd:applicationProfile/*/text()"/>
              </applicationProfile>
              <description>
                <xsl:apply-templates mode="get-iso19139-localized-string"
                                     select="gmd:CI_OnlineResource/gmd:description"/>
              </description>
              <protocol>
                <xsl:value-of select="gn-fn-rel:translate(gmd:CI_OnlineResource/gmd:protocol, $langCode)"/>
              </protocol>
              <mimeType>
                <xsl:value-of select="if (gmd:CI_OnlineResource/*/gmx:MimeFileType)
                                  then gmd:CI_OnlineResource/*/gmx:MimeFileType/@type
                                  else if (starts-with(gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString, 'WWW:DOWNLOAD:'))
                                  then replace(gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString, 'WWW:DOWNLOAD:', '')
                                  else ''"/>
              </mimeType>
              <type>onlinesrc</type>
            </item>
          </xsl:if>
        </xsl:for-each>
      </onlines>
    </xsl:if>
<!--
    <xsl:if test="count(*//gco:CharacterString[contains(., 'http')] > 0">
      <embeddedLinks>
        <xsl:for-each select="*//gco:CharacterString[contains(., 'http')]">
          <xsl:analyze-string select="."
                              regex="(regextforurl)*">

            <xsl:matching-substring>
              <item>
                <xsl:variable name="langCode">
                  <xsl:value-of select="concat('#', upper-case(util:twoCharLangCode($lang, 'EN')))"/>
                </xsl:variable>
                <xsl:variable name="url" select="regex-group(1)"/>
                <id>
                  <xsl:value-of select="regex-group(1)"/>
                </id>
                <url>
                  <value lang="{$mainLanguage}">
                    <xsl:value-of select="regex-group(1)"/>
                  </value>
                </url>
                <type>embeddedLinks</type>
              </item>
            </xsl:matching-substring>
          </xsl:analyze-string>
        </xsl:for-each>
      </embeddedLinks>
    </xsl:if>-->
  </xsl:template>
</xsl:stylesheet>
