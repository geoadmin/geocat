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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:gml320="http://www.opengis.net/gml"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gn-fn-iso19139="http://geonetwork-opensource.org/xsl/functions/profiles/iso19139"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:java="java:org.fao.geonet.util.XslUtil"
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:include href="../iso19139/convert/functions.xsl"/>
  <xsl:include href="../iso19139/update-fixed-info-keywords.xsl"/>
  <xsl:include href="../iso19139/layout/utility-fn.xsl"/>
  <xsl:include href="update-sub-template-fixed-info.xsl"/>

  <xsl:template name="trim">
    <xsl:param name="str"/>
    <xsl:choose>
      <xsl:when test="string-length($str) &gt; 0 and substring($str, 1, 1) = ' '">
        <xsl:call-template name="trim"><xsl:with-param name="str"><xsl:value-of select="substring($str, 2)"/></xsl:with-param></xsl:call-template></xsl:when>
      <xsl:when test="string-length($str) &gt; 0 and substring($str, string-length($str)) = ' '">
        <xsl:call-template name="trim"><xsl:with-param name="str"><xsl:value-of select="substring($str, 1, string-length($str)-1)"/></xsl:with-param></xsl:call-template></xsl:when>
      <xsl:otherwise><xsl:value-of select="$str"/></xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <xsl:variable name="serviceUrl" select="/root/env/siteURL"/>
  <xsl:variable name="node" select="/root/env/node"/>

  <xsl:variable name="schemaLocationFor2007"
                select="'http://www.isotc211.org/2005/gmd http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd'"/>

  <!-- Try to determine if using the 2005 or 2007 version
  of ISO19139. Based on this GML 3.2.0 or 3.2.1 is used.
  Default is 2007 with GML 3.2.1.
  You can force usage of a schema by setting:
  * ISO19139:2007
  <xsl:variable name="isUsing2005Schema" select="false()"/>
  * ISO19139:2005 (not recommended)
  <xsl:variable name="isUsing2005Schema" select="true()"/>
  -->
  <xsl:variable name="isUsing2005Schema"
                select="(/root/che:CHE_MD_Metadata/@xsi:schemaLocation
                          and /root/che:CHE_MD_Metadata/@xsi:schemaLocation != $schemaLocationFor2007)
                        or
                        count(//gml320:*) > 0"/>

  <!-- This variable is used to migrate from 2005 to 2007 version.
  By setting the schema location in a record, on next save, the record
  will use GML3.2.1.-->
  <xsl:variable name="isUsing2007Schema"
                select="/root/che:CHE_MD_Metadata/@xsi:schemaLocation
                          and /root/che:CHE_MD_Metadata/@xsi:schemaLocation = $schemaLocationFor2007"/>

  <!-- The default language is also added as gmd:locale
  for multilingual metadata records. -->
  <xsl:variable name="mainLanguage"
                select="/root/*/gmd:language/gco:CharacterString/text()|
                        /root/*/gmd:language/gmd:LanguageCode/@codeListValue"/>

  <xsl:variable name="isMultilingual"
                select="count(/root/*/gmd:locale[*/gmd:languageCode/*/@codeListValue != $mainLanguage]) > 0"/>

  <xsl:variable name="languages"
                select="/root/*/gmd:locale/*"/>

  <xsl:variable name="mainLanguageId"
                select="upper-case(java:twoCharLangCode($mainLanguage))"/>

  <xsl:variable name="defaultEncoding"
                select="'utf8'"/>

  <xsl:variable name="editorConfig"
                select="document('layout/config-editor.xml')"/>

  <xsl:variable name="nonMultilingualFields"
                select="$editorConfig/editor/multilingualFields/exclude"/>

  <xsl:variable name="locales"
                select="/root/*/gmd:locale/gmd:PT_Locale"/>

  <xsl:template match="/root">
    <xsl:apply-templates select="che:CHE_MD_Metadata"/>
  </xsl:template>

  <!-- Encode language following INSPIRE rules -->
  <xsl:template match="gmd:language[gco:CharacterString != '']" priority="99">
    <xsl:element name="{name()}">
      <xsl:apply-templates select="@*"/>
      <gmd:LanguageCode codeList="http://www.loc.gov/standards/iso639-2/"
                        codeListValue="{gco:CharacterString}"/>
    </xsl:element>
  </xsl:template>

  <!-- Remove empty language which will break on schematron rule
  checking non empty values in codelist -->
  <xsl:template match="gmd:language[gco:CharacterString = '']" priority="99"/>

  <xsl:template name="add-namespaces">
    <xsl:namespace name="xsi" select="'http://www.w3.org/2001/XMLSchema-instance'"/>
    <xsl:namespace name="che" select="'http://www.geocat.ch/2008/che'"/>
    <xsl:namespace name="gco" select="'http://www.isotc211.org/2005/gco'"/>
    <xsl:namespace name="gmd" select="'http://www.isotc211.org/2005/gmd'"/>
    <xsl:namespace name="srv" select="'http://www.isotc211.org/2005/srv'"/>
    <xsl:namespace name="gmx" select="'http://www.isotc211.org/2005/gmx'"/>
    <xsl:namespace name="gts" select="'http://www.isotc211.org/2005/gts'"/>
    <xsl:namespace name="gsr" select="'http://www.isotc211.org/2005/gsr'"/>
    <xsl:namespace name="gmi" select="'http://www.isotc211.org/2005/gmi'"/>
    <xsl:choose>
      <xsl:when test="$isUsing2005Schema and not($isUsing2007Schema)">
        <xsl:namespace name="gml" select="'http://www.opengis.net/gml'"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:namespace name="gml" select="'http://www.opengis.net/gml/3.2'"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:namespace name="xlink" select="'http://www.w3.org/1999/xlink'"/>
  </xsl:template>

  <xsl:template match="che:CHE_MD_Metadata">
    <xsl:copy copy-namespaces="no">
      <xsl:call-template name="add-namespaces"/>
      <xsl:apply-templates select="@*"/>

      <gmd:fileIdentifier>
        <gco:CharacterString>
          <xsl:value-of select="/root/env/uuid"/>
        </gco:CharacterString>
      </gmd:fileIdentifier>

      <xsl:apply-templates select="gmd:language"/>
      <xsl:apply-templates select="gmd:characterSet"/>

      <xsl:choose>
        <xsl:when test="/root/env/parentUuid!=''">
          <gmd:parentIdentifier>
            <gco:CharacterString>
              <xsl:value-of select="/root/env/parentUuid"/>
            </gco:CharacterString>
          </gmd:parentIdentifier>
        </xsl:when>
        <xsl:when test="gmd:parentIdentifier">
          <xsl:apply-templates select="gmd:parentIdentifier"/>
        </xsl:when>
      </xsl:choose>

      <xsl:apply-templates select="
        gmd:hierarchyLevel|
        gmd:hierarchyLevelName|
        gmd:contact|
        gmd:dateStamp|
        gmd:metadataStandardName|
        gmd:metadataStandardVersion|
        gmd:dataSetURI"/>

      <!-- Copy existing locales and create an extra one for the default metadata language. -->
      <xsl:if test="$isMultilingual">
        <xsl:apply-templates select="gmd:locale[*/gmd:languageCode/*/@codeListValue != $mainLanguage]"/>
        <gmd:locale>
          <gmd:PT_Locale id="{$mainLanguageId}">
            <gmd:languageCode>
              <gmd:LanguageCode codeList="http://www.loc.gov/standards/iso639-2/"
                                codeListValue="{$mainLanguage}"/>
            </gmd:languageCode>
            <gmd:characterEncoding>
              <gmd:MD_CharacterSetCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#MD_CharacterSetCode"
                                       codeListValue="{$defaultEncoding}"/>
            </gmd:characterEncoding>
          </gmd:PT_Locale>
        </gmd:locale>
      </xsl:if>

      <xsl:apply-templates select="
        gmd:spatialRepresentationInfo|
        gmd:referenceSystemInfo|
        gmd:metadataExtensionInfo|
        gmd:identificationInfo|
        gmd:contentInfo|
        gmd:distributionInfo|
        gmd:dataQualityInfo|
        gmd:portrayalCatalogueInfo|
        gmd:metadataConstraints|
        gmd:applicationSchemaInfo|
        gmd:metadataMaintenance|
        gmd:series|
        gmd:describes|
        gmd:propertyType|
        gmd:featureType|
        gmd:featureAttribute"/>

      <!-- Handle ISO profiles extensions. -->
      <xsl:apply-templates select="
        *[namespace-uri()!='http://www.isotc211.org/2005/gmd' and
          namespace-uri()!='http://www.isotc211.org/2005/srv']"/>
    </xsl:copy>
  </xsl:template>



  <!-- ================================================================= -->

  <xsl:template match="gmd:dateStamp">
    <xsl:choose>
      <xsl:when test="/root/env/changeDate">
        <xsl:copy>
          <gco:DateTime>
            <xsl:value-of select="/root/env/changeDate"/>
          </gco:DateTime>
        </xsl:copy>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ================================================================= -->

  <!--  swisstopo says they want to be able to put custom names in
   See 154971 and 140141
  <xsl:template match="gmd:metadataStandardName" priority="10">
    <xsl:copy>
      <gco:CharacterString>GM03 2+</gco:CharacterString>
    </xsl:copy>
  </xsl:template>


  <xsl:template match="gmd:metadataStandardVersion" priority="10">
    <xsl:copy>
      <gco:CharacterString>1.0</gco:CharacterString>
    </xsl:copy>
  </xsl:template>
 -->


  <!-- ================================================================= -->
  <!-- Add gmd:id attribute to all gml elements which required one. -->
  <xsl:template match="gml:MultiSurface[not(@gml:id)]|gml:Polygon[not(@gml:id)]|
                        gml320:MultiSurface[not(@gml320:id)]|gml320:Polygon[not(@gml320:id)]">
    <xsl:copy copy-namespaces="no">
      <xsl:choose>
        <xsl:when test="$isUsing2005Schema and not($isUsing2007Schema)">
          <xsl:namespace name="gml320" select="'http://www.opengis.net/gml'"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:namespace name="gml" select="'http://www.opengis.net/gml/3.2'"/>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:attribute name="{if ($isUsing2005Schema and not($isUsing2007Schema))
                            then 'gml320' else 'gml'}:id">
        <xsl:value-of select="generate-id(.)"/>
      </xsl:attribute>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="@gml:id|@gml320:id">
    <xsl:choose>
      <xsl:when test="normalize-space(.)=''">
        <xsl:attribute name="{if ($isUsing2005Schema and not($isUsing2007Schema))
                              then 'gml320' else 'gml'}:id">
          <xsl:value-of select="generate-id(.)"/>
        </xsl:attribute>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ==================================================================== -->
  <!-- Fix srsName attribute generate CRS:84 (EPSG:4326 with long/lat
       ordering) by default -->

  <xsl:template match="gml:LinearRing/@srsName|gml320:LinearRing/@srsName"/>

  <xsl:template match="@srsName">
    <xsl:choose>
      <xsl:when test="normalize-space(.)=''">
        <xsl:attribute name="srsName">
          <xsl:text>CRS:84</xsl:text>
        </xsl:attribute>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Add required gml attributes if missing -->
  <xsl:template match="gml:Polygon[not(@gml:id) or not(@srsName)]|
                       gml:MultiSurface[not(@gml:id) or not(@srsName)]|
                       gml:LineString[not(@gml:id) or not(@srsName)]|
                       gml320:Polygon[not(@gml320:id) or not(@srsName)]|
                       gml320:MultiSurface[not(@gml:id) or not(@srsName)]|
                       gml320:LineString[not(@gml:id) or not(@srsName)]">
    <xsl:element name="gml:{local-name()}"
                 namespace="{if($isUsing2005Schema)
                             then 'http://www.opengis.net/gml'
                             else 'http://www.opengis.net/gml/3.2'}">

      <xsl:attribute name="gml:id"
                     namespace="{if($isUsing2005Schema)
                                 then 'http://www.opengis.net/gml'
                                 else 'http://www.opengis.net/gml/3.2'}">
        <xsl:value-of select="if (@gml:id != '')
                              then @gml:id
                              else if (@gml320:id != '')
                              then @gml320:id
                              else generate-id(.)"/>
      </xsl:attribute>
      <xsl:attribute name="srsName">
        <xsl:value-of select="if (@srsName != '') then @srsName else 'urn:ogc:def:crs:EPSG:6.6:4326'"/>
      </xsl:attribute>
      <xsl:copy-of select="@*[name() != 'srsName' and local-name() != 'id']"/>
      <xsl:apply-templates select="*"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="gml:*|gml320:*">
    <xsl:element name="gml:{local-name()}"
                 namespace="{if($isUsing2005Schema)
                             then 'http://www.opengis.net/gml'
                             else 'http://www.opengis.net/gml/3.2'}">
      <xsl:apply-templates select="@*|*"/>
    </xsl:element>
  </xsl:template>

  <!-- ================================================================= -->

  <!-- Replace by PT_FreeURL if only gmd:URL set in multilingual records. -->
  <xsl:template match="gmd:linkage[$isMultilingual and
                                   gmd:URL and
                                   not(che:PT_FreeURL)]">
    <xsl:variable name="url"
                  select="gmd:URL"/>
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:attribute name="xsi:type">che:PT_FreeURL_PropertyType</xsl:attribute>

      <che:PT_FreeURL>
        <xsl:for-each select="$languages/@id">
          <che:URLGroup>
            <che:LocalisedURL locale="#{.}">
              <xsl:value-of select="$url"/>
            </che:LocalisedURL>
          </che:URLGroup>
        </xsl:for-each>
      </che:PT_FreeURL>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="gmd:linkage[che:PT_FreeURL]">
    <xsl:copy>
      <xsl:apply-templates select="@*[not(name() = 'gco:nilReason') and not(name() = 'xsi:type')]"/>
      <xsl:variable name="isInPTFreeText" select="count(che:PT_FreeURL/*/che:LocalisedURL[@locale = concat('#', $mainLanguageId)]) = 1"/>
      <xsl:variable name="valueInPtFreeURLForMainLanguage" select="normalize-space((che:PT_FreeURL/*/che:LocalisedURL[@locale = concat('#', $mainLanguageId)])[1])"/>

      <xsl:choose>
        <xsl:when test="not($isMultilingual) and
                        $valueInPtFreeURLForMainLanguage != '' and
                        normalize-space(gmd:URL) = ''">
          <gmd:URL>
            <xsl:value-of select="$valueInPtFreeURLForMainLanguage"/>
          </gmd:URL>
        </xsl:when>
        <xsl:when test="not($isMultilingual)">
          <xsl:apply-templates select="gmd:URL"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="xsi:type" select="'che:PT_FreeURL_PropertyType'"/>
          <xsl:choose>
            <xsl:when test="$isInPTFreeText">
              <gmd:URL>
                <xsl:value-of select="$valueInPtFreeURLForMainLanguage"/>
              </gmd:URL>
              <xsl:if test="che:PT_FreeURL[normalize-space(.) != '']">
                <che:PT_FreeURL>
                  <xsl:call-template name="populate-free-text"/>
                </che:PT_FreeURL>
              </xsl:if>
            </xsl:when>
            <xsl:otherwise>
              <xsl:apply-templates select="gmd:URL"/>
              <che:PT_FreeURL>
                <che:URLGroup>
                  <che:LocalisedURL locale="#{$mainLanguageId}">
                    <xsl:value-of select="gmd:URL"/>
                  </che:LocalisedURL>
                </che:URLGroup>
                <xsl:call-template name="populate-free-text"/>
              </che:PT_FreeURL>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*[gco:CharacterString|gmx:Anchor|gmd:PT_FreeText]">
    <xsl:copy>
      <xsl:apply-templates select="@*[not(name() = 'gco:nilReason') and not(name() = 'xsi:type')]"/>

      <!-- Add nileason if text is empty -->
      <xsl:variable name="excluded"
                    select="gn-fn-iso19139:isNotMultilingualField(., $editorConfig)"/>

      <xsl:variable name="isInPTFreeText"
                    select="count(gmd:PT_FreeText/*/gmd:LocalisedCharacterString[
                                            @locale = concat('#', $mainLanguageId)]) = 1"/>

      <xsl:variable name="valueInPtFreeTextForMainLanguage">
        <xsl:call-template name="trim">
          <xsl:with-param name="str" select="(gmd:PT_FreeText/*/gmd:LocalisedCharacterString[
                                            @locale = concat('#', $mainLanguageId)])[1]"/>
        </xsl:call-template>
      </xsl:variable>

      <!-- Add nileason if text is empty -->
      <xsl:variable name="isEmpty"
                    select="if ($isMultilingual and not($excluded))
                            then $valueInPtFreeTextForMainLanguage = ''
                            else if ($valueInPtFreeTextForMainLanguage != '')
                            then $valueInPtFreeTextForMainLanguage = ''
                            else normalize-space(gco:CharacterString|gmx:Anchor) = ''"/>

      <!-- TODO ? Removes @nilReason from parents of gmx:Anchor if anchor has @xlink:href attribute filled. -->
      <xsl:variable name="isEmptyAnchor"
                    select="normalize-space(gmx:Anchor/@xlink:href) = ''" />

      <xsl:choose>
        <xsl:when test="$isEmpty">
          <xsl:attribute name="gco:nilReason">
            <xsl:choose>
              <xsl:when test="@gco:nilReason">
                <xsl:value-of select="@gco:nilReason"/>
              </xsl:when>
              <xsl:otherwise>missing</xsl:otherwise>
            </xsl:choose>
          </xsl:attribute>
        </xsl:when>
        <xsl:when test="@gco:nilReason != 'missing' and not($isEmpty)">
          <xsl:copy-of select="@gco:nilReason"/>
        </xsl:when>
      </xsl:choose>

      <!-- For multilingual records, for multilingual fields,
       create a gco:CharacterString or gmx:Anchor containing
       the same value as the default language PT_FreeText.
      -->
      <xsl:variable name="element" select="name()"/>

      <xsl:choose>
        <!-- Check record does not contains multilingual elements
          matching the main language. This may happen if the main
          language is declared in locales and only PT_FreeText are set.
          It should not be possible in GeoNetwork, but record user can
          import may use this encoding. -->
        <xsl:when test="not($isMultilingual) and
                        $valueInPtFreeTextForMainLanguage != '' and
                        normalize-space(gco:CharacterString|gmx:Anchor) = ''">
          <xsl:element name="{if (gmx:Anchor) then 'gmx:Anchor' else 'gco:CharacterString'}">
            <xsl:copy-of select="gmx:Anchor/@*"/>
            <xsl:value-of select="$valueInPtFreeTextForMainLanguage"/>
          </xsl:element>
        </xsl:when>
        <xsl:when test="not($isMultilingual) or
                        $excluded">
          <!-- Copy gco:CharacterString only. PT_FreeText are removed if not multilingual. -->
          <xsl:apply-templates select="gco:CharacterString|gmx:Anchor"/>
        </xsl:when>
        <xsl:otherwise>
          <!-- Add xsi:type for multilingual element. -->
          <xsl:attribute name="xsi:type" select="'gmd:PT_FreeText_PropertyType'"/>

          <!-- Is the default language value set in a PT_FreeText ? -->

          <xsl:choose>
            <xsl:when test="$isInPTFreeText">
              <!-- Update gco:CharacterString to contains
                   the default language value from the PT_FreeText.
                   PT_FreeText takes priority. -->
              <xsl:element name="{if (gmx:Anchor) then 'gmx:Anchor' else 'gco:CharacterString'}">
                <xsl:copy-of select="gmx:Anchor/@*"/>
                <xsl:value-of select="$valueInPtFreeTextForMainLanguage"/>
              </xsl:element>

              <xsl:if test="gmd:PT_FreeText[normalize-space(.) != '']">
                <gmd:PT_FreeText>
                  <xsl:call-template name="populate-free-text"/>
                </gmd:PT_FreeText>
              </xsl:if>
            </xsl:when>
            <xsl:otherwise>
              <!-- Populate PT_FreeText for default language if not existing. -->
              <xsl:apply-templates select="gco:CharacterString|gmx:Anchor"/>
              <gmd:PT_FreeText>
                <gmd:textGroup>
                  <gmd:LocalisedCharacterString locale="#{$mainLanguageId}">
                    <xsl:value-of select="gco:CharacterString"/>
                  </gmd:LocalisedCharacterString>
                </gmd:textGroup>
                <xsl:call-template name="populate-free-text"/>
              </gmd:PT_FreeText>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>

  <xsl:template name="populate-free-text">
    <xsl:variable name="freeText"
                  select="gmd:PT_FreeText/gmd:textGroup|che:PT_FreeURL/che:URLGroup"/>

    <!-- Loop on locales in order to preserve order.
        Keep main language on top.
        Translations having no locale are ignored. eg. when removing a lang. -->

      <xsl:variable name="element" select="$freeText[*/@locale = concat('#', $mainLanguageId)]"/>

    <xsl:apply-templates select="$element"/>

    <xsl:for-each select="$locales[@id != $mainLanguageId]">
      <xsl:variable name="localId"
                    select="@id"/>
      <xsl:variable name="element"
                    select="$freeText[*/@locale = concat('#', $localId)]"/>
      <xsl:apply-templates select="$element"/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="che:basicGeodataIDType">
    <xsl:copy>
      <xsl:copy-of select="@*[not(name()='gco:nilReason')]"/>
      <xsl:if test="normalize-space(che:basicGeodataIDTypeCode/@codeListValue)=''">
        <xsl:attribute name="gco:nilReason">
          <xsl:value-of select="'missing'"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates select="che:basicGeodataIDTypeCode"/>
    </xsl:copy>
  </xsl:template>
  <!-- ================================================================= -->
  <!-- codelists: set @codeList path -->
  <!-- ================================================================= -->
  <xsl:template match="gmd:LanguageCode[@codeListValue]" priority="10">
    <gmd:LanguageCode codeList="http://www.loc.gov/standards/iso639-2/">
      <xsl:apply-templates select="@*[name(.)!='codeList']"/>
    </gmd:LanguageCode>
  </xsl:template>


  <xsl:template match="gmd:*[@codeListValue]">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:attribute name="codeList">
        <xsl:value-of
          select="concat('http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#',local-name(.))"/>
      </xsl:attribute>
    </xsl:copy>
  </xsl:template>

  <!-- can't find the location of the 19119 codelists - so we make one up -->

  <xsl:template match="srv:*[@codeListValue]">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:attribute name="codeList">
        <xsl:value-of
          select="concat('http://www.isotc211.org/2005/iso19119/resources/Codelist/gmxCodelists.xml#',local-name(.))"/>
      </xsl:attribute>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="gmx:FileName[contains(../../@id,'geonetwork.thesaurus.')]" priority="200">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>


  <!-- ================================================================= -->

  <!-- Do not allow to expand operatesOn sub-elements
      and constrain users to use uuidref attribute to link
      service metadata to datasets. This will avoid to have
      error on XSD validation. -->

  <xsl:template match="srv:operatesOn|gmd:featureCatalogueCitation">
    <xsl:copy>
      <xsl:copy-of select="@uuidref"/>
      <xsl:choose>

        <!-- Do not expand operatesOn sub-elements when using uuidref
             to link service metadata to datasets or datasets to iso19110.
         -->
        <xsl:when test="@uuidref">
        <xsl:choose>
          <xsl:when test="not(string(@xlink:href)) or starts-with(@xlink:href, $serviceUrl)">
            <xsl:attribute name="xlink:href">
              <xsl:value-of
                select="concat($serviceUrl,'csw?service=CSW&amp;request=GetRecordById&amp;version=2.0.2&amp;outputSchema=http://www.isotc211.org/2005/gmd&amp;elementSetName=full&amp;id=',@uuidref)"/>
            </xsl:attribute>
          </xsl:when>
          <xsl:otherwise>
            <xsl:copy-of select="@xlink:href"/>
          </xsl:otherwise>
        </xsl:choose>
        </xsl:when>

        <xsl:otherwise>
          <xsl:apply-templates select="node()" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>

  <!-- For XLinked subtemplates, the lang parameter MUST be in the same order as in the record.
  Main language first, then other locales. If not, then the default CharacterString does not contain
  the main language. It user change the language order in the record, the lang parameter needs to
  be reordered too.

  Example of URL:
  <gmd:pointOfContact xmlns:xlink="http://www.w3.org/1999/xlink"
                             xlink:href="local://srv/api/registries/entries/af9e5d4e-2c1a-48c0-853f-3a771fcf9ee3?
                               process=gmd:role/gmd:CI_RoleCode/@codeListValue~distributor&amp;
                               lang=eng,ara,spa,rus,fre,ger,chi&amp;
                               schema=iso19139"
  Can also be using lang=eng&amp;lang=ara.
  -->
  <xsl:template match="@xlink:href[starts-with(., 'local://srv/api/registries/entries') and contains(., '?')]">
    <xsl:variable name="urlBase"
                  select="substring-before(., '?')"/>
    <xsl:variable name="urlParameters"
                  select="substring-after(., '?')"/>

    <!-- Collect all parameters excluding language -->
    <xsl:variable name="listOfAllParameters">
      <xsl:for-each select="tokenize($urlParameters, '&amp;')">
        <xsl:variable name="parameterName"
                      select="tokenize(., '=')[1]"/>

        <xsl:if test="$parameterName != 'lang'">
          <param name="{$parameterName}"
                 value="{.}"/>
        </xsl:if>
      </xsl:for-each>
    </xsl:variable>

    <xsl:attribute name="xlink:href"
                   select="concat(
                    $urlBase,
                    '?lang=', string-join(($mainLanguage, $locales//gmd:LanguageCode/@codeListValue[. != $mainLanguage]), ','),
                    '&amp;',
                    string-join($listOfAllParameters/param/@value, '&amp;'))"/>
  </xsl:template>

  <!-- ================================================================= -->
  <!-- Set local identifier to the first 2 letters of iso code. Locale ids
    are used for multilingual charcterString using #iso2code for referencing.
  -->
  <xsl:template match="gmd:PT_Locale">
    <xsl:element name="gmd:{local-name()}">
      <xsl:variable name="id"
                    select="upper-case(java:twoCharLangCode(gmd:languageCode/gmd:LanguageCode/@codeListValue))"/>

      <xsl:apply-templates select="@*"/>
      <xsl:if test="normalize-space(@id)='' or normalize-space(@id)!=$id">
        <xsl:attribute name="id">
          <xsl:value-of select="$id"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates select="node()"/>
    </xsl:element>
  </xsl:template>

  <!-- Apply same changes as above to the gmd:LocalisedCharacterString -->
  <xsl:variable name="language" select="//gmd:PT_Locale"/> <!-- Need list of all locale -->
  <xsl:template match="gmd:LocalisedCharacterString">
    <xsl:element name="gmd:{local-name()}">
      <xsl:variable name="currentLocale"
                    select="upper-case(replace(normalize-space(@locale), '^#', ''))"/>
      <xsl:variable name="ptLocale"
                    select="$language[upper-case(replace(normalize-space(@id), '^#', ''))=string($currentLocale)]"/>
      <xsl:variable name="id"
                    select="upper-case(java:twoCharLangCode(($ptLocale/gmd:languageCode/gmd:LanguageCode/@codeListValue)[1]))"/>
      <xsl:apply-templates select="@*"/>
      <xsl:if test="$id != '' and ($currentLocale='' or @locale!=concat('#', $id)) ">
        <xsl:attribute name="locale">
          <xsl:value-of select="concat('#',$id)"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates select="node()"/>
    </xsl:element>
  </xsl:template>


  <xsl:template match="che:LocalisedURL|gmd:LocalisedURL">
    <xsl:element name="che:{local-name()}">
      <xsl:variable name="currentLocale">
        <xsl:variable name="baseLoc" select="upper-case(replace(normalize-space(@locale), '^#', ''))"/>
        <xsl:choose>
          <xsl:when test="$baseLoc = 'GE'">DE</xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$baseLoc"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:variable name="ptLocale"
                    select="$language[upper-case(replace(normalize-space(@id), '^#', ''))=string($currentLocale)]"/>
      <xsl:variable name="id"
                    select="upper-case(java:twoCharLangCode($ptLocale/gmd:languageCode/gmd:LanguageCode/@codeListValue, ''))"/>
      <xsl:apply-templates select="@*"/>
      <xsl:if test="$id != '' and ($currentLocale='' or @locale!=concat('#', $id)) ">
        <xsl:attribute name="locale">
          <xsl:value-of select="concat('#',$id)"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates select="node()"/>
    </xsl:element>
  </xsl:template>

  <!-- Remove URLGroups with empty LocalizedURLs or without it -->
  <xsl:template match="che:URLGroup[che:LocalisedURL[not(text())]]"/>
  <xsl:template match="che:URLGroup[not(che:LocalisedURL)]"/>

  <!-- Remove textGroup with empty LocalisedCharacterString or without it -->
  <xsl:template match="gmd:textGroup[gmd:LocalisedCharacterString[not(text())]]"/>
  <xsl:template match="gmd:textGroup[not(gmd:LocalisedCharacterString)]"/>


  <!-- For multilingual elements. Check that the local
is defined in record. If not, remove the element. -->
  <xsl:template match="gmd:textGroup">
    <xsl:variable name="elementLocalId"
                  select="replace(gmd:LocalisedCharacterString/@locale, '^#', '')"/>
    <xsl:choose>
      <xsl:when test="count($locales[@id = $elementLocalId]) > 0 and not($elementLocalId = $mainLanguageId) ">
        <gmd:textGroup>
          <gmd:LocalisedCharacterString>
            <xsl:variable name="currentLocale"
                          select="replace(gmd:LocalisedCharacterString/@locale, '^#', '')"/>
            <xsl:variable name="ptLocale"
                          select="$locales[@id = string($currentLocale)]"/>
            <xsl:variable name="id"
                          select="upper-case(java:twoCharLangCode($ptLocale/gmd:languageCode/gmd:LanguageCode/@codeListValue[. != '']))"/>
            <xsl:apply-templates select="@*"/>
            <xsl:if test="$id != ''">
              <xsl:attribute name="locale">
                <xsl:value-of select="concat('#',$id)"/>
              </xsl:attribute>
            </xsl:if>

            <xsl:apply-templates select="gmd:LocalisedCharacterString/text()"/>
          </gmd:LocalisedCharacterString>
        </gmd:textGroup>
      </xsl:when>
      <xsl:when test="$elementLocalId = $mainLanguageId">
        <gmd:textGroup>
          <gmd:LocalisedCharacterString>
            <xsl:attribute name="locale">
              <xsl:value-of select="concat('#',$mainLanguageId)"/>
            </xsl:attribute>
            <xsl:apply-templates select="gmd:LocalisedCharacterString/text()"/>
          </gmd:LocalisedCharacterString>
        </gmd:textGroup>
      </xsl:when>

    </xsl:choose>
  </xsl:template>

  <!-- Remove attribute indeterminatePosition having empty
  value which is not a valid facet for it. -->
  <xsl:template match="@indeterminatePosition[. = '']" priority="2"/>

  <!-- ================================================================= -->
  <!-- Adjust the namespace declaration - In some cases name() is used to get the
    element. The assumption is that the name is in the format of  <ns:element>
    however in some cases it is in the format of <element xmlns=""> so the
    following will convert them back to the expected value. This also corrects the issue
    where the <element xmlns=""> loose the xmlns="" due to the exclude-result-prefixes="#all" -->
  <!-- Note: Only included prefix gml, gmd and gco for now. -->
  <!-- TODO: Figure out how to get the namespace prefix via a function so that we don't need to hard code them -->
  <!-- ================================================================= -->

  <xsl:template name="correct_ns_prefix">
    <xsl:param name="element"/>
    <xsl:param name="prefix"/>
    <xsl:choose>
      <xsl:when test="local-name($element)=name($element) and $prefix != '' ">
        <xsl:element name="{$prefix}:{local-name($element)}">
          <xsl:apply-templates select="@*|node()"/>
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="correct_ns_prefix_with_namespace">
    <xsl:param name="element"/>
    <xsl:param name="prefix"/>
    <xsl:param name="namespace"/>

    <xsl:choose>
      <xsl:when test="local-name($element)=name($element) and $prefix != '' ">
        <xsl:element name="{$prefix}:{local-name($element)}" namespace="{$namespace}">
          <xsl:apply-templates select="@*|node()"/>
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="gmd:*">
    <xsl:call-template name="correct_ns_prefix">
      <xsl:with-param name="element" select="."/>
      <xsl:with-param name="prefix" select="'gmd'"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="gco:*">
    <xsl:call-template name="correct_ns_prefix">
      <xsl:with-param name="element" select="."/>
      <xsl:with-param name="prefix" select="'gco'"/>
    </xsl:call-template>
  </xsl:template>

  <!-- Move to GML 3.2.1 when using 2007 version. -->
  <xsl:template match="gml320:*[$isUsing2007Schema]">
    <xsl:element name="gml:{local-name()}" namespace="http://www.opengis.net/gml/3.2">
      <xsl:apply-templates select="@*|node()"/>
    </xsl:element>
  </xsl:template>
  <xsl:template match="@gml320:*[$isUsing2007Schema]">
    <xsl:attribute name="gml:{local-name()}" namespace="http://www.opengis.net/gml/3.2" select="."/>
  </xsl:template>

  <xsl:template match="gml:*|gml320:*">
    <xsl:call-template name="correct_ns_prefix_with_namespace">
      <xsl:with-param name="element" select="."/>
      <xsl:with-param name="prefix"
                      select="'gml'"/>
      <xsl:with-param name="namespace"
                      select="if($isUsing2005Schema) then 'http://www.opengis.net/gml' else 'http://www.opengis.net/gml/3.2'"/>
    </xsl:call-template>
  </xsl:template>

  <!-- ================================================================= -->
  <xsl:template match="che:*[@codeListValue]">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:attribute name="codeList">
        <xsl:value-of select="concat('#',local-name(.))"/>
      </xsl:attribute>
    </xsl:copy>
  </xsl:template>

  <xsl:template priority="30" match="
            gmd:topicCategory[normalize-space(gmd:MD_TopicCategoryCode)='environment' and
            not(following-sibling::gmd:topicCategory[starts-with(normalize-space(gmd:MD_TopicCategoryCode), 'environment_')])] "/>
  <xsl:template priority="30" match="
            gmd:topicCategory[normalize-space(gmd:MD_TopicCategoryCode)='geoscientificInformation' and
            not(following-sibling::gmd:topicCategory[starts-with(normalize-space(gmd:MD_TopicCategoryCode), 'geoscientificInformation_')])] "/>
  <xsl:template priority="30" match="
            gmd:topicCategory[normalize-space(gmd:MD_TopicCategoryCode)='planningCadastre' and
            not(following-sibling::gmd:topicCategory[starts-with(normalize-space(gmd:MD_TopicCategoryCode), 'planningCadastre_')])] "/>
  <xsl:template priority="30" match="
            gmd:topicCategory[normalize-space(gmd:MD_TopicCategoryCode)='imageryBaseMapsEarthCover' and
            not(following-sibling::gmd:topicCategory[starts-with(normalize-space(gmd:MD_TopicCategoryCode), 'imageryBaseMapsEarthCover_')])] "/>
  <xsl:template priority="30" match="
            gmd:topicCategory[normalize-space(gmd:MD_TopicCategoryCode)='utilitiesCommunication' and
            not(following-sibling::gmd:topicCategory[starts-with(normalize-space(gmd:MD_TopicCategoryCode), 'utilitiesCommunication_')])] "/>

  <xsl:template priority="20" match="
            gmd:topicCategory[normalize-space(gmd:MD_TopicCategoryCode)='environment' and
            (preceding-sibling::gmd:topicCategory[starts-with(normalize-space(gmd:MD_TopicCategoryCode), 'environment')] or
            following-sibling::gmd:topicCategory[starts-with(normalize-space(gmd:MD_TopicCategoryCode), 'environment')])] "/>
  <xsl:template priority="20" match="
            gmd:topicCategory[normalize-space(gmd:MD_TopicCategoryCode)='geoscientificInformation' and
            (preceding-sibling::gmd:topicCategory[starts-with(normalize-space(gmd:MD_TopicCategoryCode), 'geoscientificInformation')] or
            following-sibling::gmd:topicCategory[starts-with(normalize-space(gmd:MD_TopicCategoryCode), 'geoscientificInformation')])] "/>
  <xsl:template priority="20" match="
            gmd:topicCategory[normalize-space(gmd:MD_TopicCategoryCode)='planningCadastre' and
            (preceding-sibling::gmd:topicCategory[starts-with(normalize-space(gmd:MD_TopicCategoryCode), 'planningCadastre')] or
            following-sibling::gmd:topicCategory[starts-with(normalize-space(gmd:MD_TopicCategoryCode), 'planningCadastre')])] "/>
  <xsl:template priority="20" match="
            gmd:topicCategory[normalize-space(gmd:MD_TopicCategoryCode)='imageryBaseMapsEarthCover' and
            (preceding-sibling::gmd:topicCategory[starts-with(normalize-space(gmd:MD_TopicCategoryCode), 'imageryBaseMapsEarthCover')] or
            following-sibling::gmd:topicCategory[starts-with(normalize-space(gmd:MD_TopicCategoryCode), 'imageryBaseMapsEarthCover')])] "/>
  <xsl:template priority="20" match="
            gmd:topicCategory[normalize-space(gmd:MD_TopicCategoryCode)='utilitiesCommunication' and
            (preceding-sibling::gmd:topicCategory[starts-with(normalize-space(gmd:MD_TopicCategoryCode), 'utilitiesCommunication')] or
            following-sibling::gmd:topicCategory[starts-with(normalize-space(gmd:MD_TopicCategoryCode), 'utilitiesCommunication')])] "/>

  <xsl:template priority="10" match="
            gmd:topicCategory[starts-with(normalize-space(gmd:MD_TopicCategoryCode), 'imageryBaseMapsEarthCover_') and
                not( preceding-sibling::gmd:topicCategory[starts-with(normalize-space(gmd:MD_TopicCategoryCode), 'imageryBaseMapsEarthCover_')])]">
    <gmd:topicCategory>
      <gmd:MD_TopicCategoryCode>imageryBaseMapsEarthCover</gmd:MD_TopicCategoryCode>
    </gmd:topicCategory>
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template priority="10" match="
            gmd:topicCategory[starts-with(normalize-space(gmd:MD_TopicCategoryCode), 'planningCadastre_') and
                not( preceding-sibling::gmd:topicCategory[starts-with(normalize-space(gmd:MD_TopicCategoryCode), 'planningCadastre_')])]">
    <gmd:topicCategory>
      <gmd:MD_TopicCategoryCode>planningCadastre</gmd:MD_TopicCategoryCode>
    </gmd:topicCategory>
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template priority="10" match="
            gmd:topicCategory[starts-with(normalize-space(gmd:MD_TopicCategoryCode), 'geoscientificInformation_') and
                not( preceding-sibling::gmd:topicCategory[starts-with(normalize-space(gmd:MD_TopicCategoryCode), 'geoscientificInformation_')])]">
    <gmd:topicCategory>
      <gmd:MD_TopicCategoryCode>geoscientificInformation</gmd:MD_TopicCategoryCode>
    </gmd:topicCategory>
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template priority="10" match="
            gmd:topicCategory[starts-with(normalize-space(gmd:MD_TopicCategoryCode), 'environment_') and
                not( preceding-sibling::gmd:topicCategory[starts-with(normalize-space(gmd:MD_TopicCategoryCode), 'environment_')])]">
    <gmd:topicCategory>
      <gmd:MD_TopicCategoryCode>environment</gmd:MD_TopicCategoryCode>
    </gmd:topicCategory>
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template priority="10" match="
            gmd:topicCategory[starts-with(normalize-space(gmd:MD_TopicCategoryCode), 'utilitiesCommunication_') and
                not( preceding-sibling::gmd:topicCategory[starts-with(normalize-space(gmd:MD_TopicCategoryCode), 'utilitiesCommunication_')])]">
    <gmd:topicCategory>
      <gmd:MD_TopicCategoryCode>utilitiesCommunication</gmd:MD_TopicCategoryCode>
    </gmd:topicCategory>
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- copy everything else as is -->

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="@xsi:schemaLocation">
    <xsl:if test="java:getSettingValue('system/metadata/validation/removeSchemaLocation') = 'false'">
      <xsl:copy-of select="."/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="gml:LinearRing/@srsName"/>

</xsl:stylesheet>
