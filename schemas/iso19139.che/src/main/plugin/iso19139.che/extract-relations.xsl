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
              <xsl:value-of select="gmd:fileName/gco:CharacterString"/>
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
            <xsl:variable name="url">
              <xsl:choose>
                <xsl:when test="gmd:linkage/gmd:URL!=''">
                  <xsl:value-of select="gmd:linkage/gmd:URL"/>
                </xsl:when>
                <xsl:when test="gmd:linkage/che:LocalisedURL!=''">
                  <xsl:value-of select="gmd:linkage/che:LocalisedURL"/>
                </xsl:when>
                <xsl:when test="(gmd:linkage/che:PT_FreeURL//che:LocalisedURL[@locale = $langCode][text() != ''])[1]">
                  <xsl:value-of
                    select="(gmd:linkage/che:PT_FreeURL//che:LocalisedURL[@locale = $langCode][text() != ''])[1]"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="(gmd:linkage/che:PT_FreeURL//che:LocalisedURL[text() != ''])[1]"/>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:variable>
            <!-- Compute title based on online source info-->
            <xsl:variable name="title">
              <xsl:variable name="title" select="''"/>
              <xsl:value-of select="if ($title = '' and ../@uuidref)
                                    then ../@uuidref
                                    else $title"/><xsl:text> </xsl:text>
              <xsl:value-of select="if (gn-fn-rel:translate(gmd:name, $langCode) != '')
                                    then gn-fn-rel:translate(gmd:name, $langCode)
                                    else if (gmd:name/gmx:MimeFileType != '')
                                    then gmd:name/gmx:MimeFileType
                                    else if (gn-fn-rel:translate(gmd:description, $langCode) != '')
                                    then gn-fn-rel:translate(gmd:description, $langCode)
                                    else $url"/>
            </xsl:variable>
            <id>
              <xsl:value-of select="$url"/>
            </id>
            <title>
              <xsl:value-of select="if ($title != '') then $title else $url"/>
            </title>
            <url>
              <xsl:value-of select="$url"/>
            </url>
            <name>
              <xsl:value-of select="gn-fn-rel:translate(gmd:name, $langCode)"/>
            </name>
            <abstract>
              <xsl:value-of select="gn-fn-rel:translate(gmd:description, $langCode)"/>
            </abstract>
            <function>
              <xsl:value-of select="gmd:function/*/@codeListValue"/>
            </function>
            <applicationProfile>
              <xsl:value-of select="gmd:applicationProfile/gco:CharacterString"/>
            </applicationProfile>
            <description>
              <xsl:value-of select="gn-fn-rel:translate(gmd:description, $langCode)"/>
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
