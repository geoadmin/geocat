<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gn="http://www.fao.org/geonetwork"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                xmlns:gn-fn-iso19139="http://geonetwork-opensource.org/xsl/functions/profiles/iso19139"
                xmlns:util="java:org.fao.geonet.util.GeocatXslUtil"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:exslt="http://exslt.org/common"
                version="2.0" exclude-result-prefixes="#all">


  <xsl:include href="utility-tpl.xsl"/>

  <!-- Visit all XML tree recursively -->
  <xsl:template mode="mode-iso19139.che" match="*|@*">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>
    <xsl:param name="overrideLabel" as="xs:string" required="no" select="''"/>

    <xsl:apply-templates mode="mode-iso19139" select=".">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="labels" select="$labels"/>
      <xsl:with-param name="overrideLabel" select="$overrideLabel"/>
    </xsl:apply-templates>
  </xsl:template>



  <!-- Use iso19139 mode unless something takes priority in that file -->
  <xsl:template mode="mode-iso19139.che" match="che:*">
    <xsl:param name="overrideLabel" as="xs:string" required="no" select="''"/>

    <xsl:apply-templates mode="mode-iso19139" select=".">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="labels" select="$labels"/>
      <xsl:with-param name="overrideLabel" select="$overrideLabel"/>
    </xsl:apply-templates>
  </xsl:template>





  <xsl:template mode="mode-iso19139" priority="99999"
                match="che:CHE_CI_ResponsibleParty/gn:child[@name = 'individualName']">
    <!-- Do nothing -->
  </xsl:template>

  <!--
  Old bbox editor style for geocat providing only an image of the bounding box.
  Editing was providing in the subtemplate editor.

  <xsl:template mode="mode-iso19139" match="gmd:geographicElement[gmd:EX_GeographicBoundingBox]|
  gmd:geographicElement[gmd:EX_BoundingPolygon]" priority="2000">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>

    <xsl:if test="not(gmd:EX_GeographicBoundingBox) or not(../gmd:geographicElement/gmd:EX_BoundingPolygon)">

      <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(., true())"/>
      <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>
      <xsl:variable name="regionId" select="''"/>
      &lt;!&ndash;GEOCAT-DEPRECATED <xsl:variable name="regionId" select="util:parseRegionIdFromXLink(../../@xlink:href)"/>&ndash;&gt;

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
              <img
                src="region.getmap.png?mapsrs=EPSG:21781&amp;width=250&amp;background=settings&amp;geom=Polygon(({*/gmd:westBoundLongitude/gco:Decimal}%20{*/gmd:northBoundLatitude/gco:Decimal},{*/gmd:eastBoundLongitude/gco:Decimal}%20{*/gmd:northBoundLatitude/gco:Decimal},{*/gmd:eastBoundLongitude/gco:Decimal}%20{*/gmd:southBoundLatitude/gco:Decimal},{*/gmd:westBoundLongitude/gco:Decimal}%20{*/gmd:southBoundLatitude/gco:Decimal},{*/gmd:westBoundLongitude/gco:Decimal}%20{*/gmd:northBoundLatitude/gco:Decimal}))&amp;geomsrs=EPSG:4326"/>
            </xsl:when>
            <xsl:when test="gmd:EX_BoundingPolygon">
              <img class="gn-img-extent"
                   src="region.getmap.png?mapsrs=EPSG:21781&amp;width=250&amp;background=settings&amp;id={$regionId}"/>
            </xsl:when>
          </xsl:choose>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>-->

  <xsl:template mode="mode-iso19139" priority="201"
                match="*[gmd:URL|che:PT_FreeURL][$schema = 'iso19139.che']">
    <xsl:param name="labels" select="$labels" required="no"/>
    <xsl:param name="overrideLabel" select="''" required="no"/>

    <xsl:variable name="elementName" select="name()"/>

    <xsl:variable name="hasOnlyPTFreeText"
                  select="count(che:PT_FreeURL) > 0 and count(gmd:URL) = 0"/>
    <xsl:variable name="isMultilingualElement"
                  select="$metadataIsMultilingual and
                          count($editorConfig/editor/multilingualFields/exclude[
                                    name = $elementName]) = 0"/>
    <xsl:variable name="isMultilingualElementExpanded"
                  select="count($editorConfig/editor/multilingualFields/expanded[
                                    name = $elementName]) > 0"/>

    <xsl:variable name="theElement"
                  select="if ($isMultilingualElement and $hasOnlyPTFreeText)
                          then che:PT_FreeURL
                          else gmd:URL"/>

    <xsl:variable name="xpath"
                  select="gn-fn-metadata:getXPathByRef(gn:element/@ref, $metadata, true())"/>
    <xsl:variable name="isoType"
                  select="if (../@gco:isoType) then ../@gco:isoType else ''"/>
    <xsl:variable name="labelConfig"
                  select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), $isoType, $xpath)"/>
    <xsl:variable name="helper"
                  select="gn-fn-metadata:getHelper($labelConfig/helper, .)"/>

    <xsl:variable name="attributes">

      <!-- Create form for all existing attribute (not in gn namespace)
      and all non existing attributes not already present for the
      current element and its children (eg. @uom in gco:Distance).
      A list of exception is defined in form-builder.xsl#render-for-field-for-attribute. -->
      <xsl:apply-templates mode="render-for-field-for-attribute"
                           select="@*|
                                   gn:attribute[not(@name = parent::node()/@*/name())]">
        <xsl:with-param name="ref"
                        select="gn:element/@ref"/>
        <xsl:with-param name="insertRef"
                        select="$theElement/gn:element/@ref"/>
      </xsl:apply-templates>
      <xsl:apply-templates mode="render-for-field-for-attribute"
                           select="*/@*|
                                   */gn:attribute[not(@name = parent::node()/@*/name())]">
        <xsl:with-param name="ref" select="*/gn:element/@ref"/>
        <xsl:with-param name="insertRef" select="$theElement/gn:element/@ref"/>
      </xsl:apply-templates>
    </xsl:variable>

    <xsl:variable name="values">
      <xsl:if test="$isMultilingualElement">
        <xsl:variable name="url"
                      select="normalize-space(gmd:URL)"/>
        <values>
          <!-- Or the PT_FreeText element matching the main language
          <xsl:if test="gmd:URL">
            <value ref="{$theElement/gn:element/@ref}"
                   lang="{$metadataLanguage}">
              <xsl:value-of select="gmd:URL"/>
            </value>
          </xsl:if> -->

          <!-- the existing translation -->
          <xsl:for-each select="che:PT_FreeURL/che:URLGroup/che:LocalisedURL">
            <value ref="{gn:element/@ref}"
                   lang="{substring-after(@locale, '#')}">
              <xsl:value-of select="."/>
            </value>
          </xsl:for-each>

          <!-- and create field for none translated language -->
          <xsl:for-each select="$metadataOtherLanguages/lang">
            <xsl:variable name="currentLanguageId" select="@id"/>
            <xsl:variable name="code" select="@code"/>
            <xsl:variable name="ptFreeElementDoesNotExist"
                          select="count($theElement/parent::node()/
                              che:PT_FreeURL/che:URLGroup/
                              che:LocalisedURL[@locale = concat('#',$currentLanguageId)]) = 0"/>


            <xsl:choose>
              <!-- In case we have a gmd:URL set and a PTFreeUrl not
              set for the main language. Inject this value.-->
              <xsl:when test="$url != '' and
                            $code = $metadataLanguage and
                            $ptFreeElementDoesNotExist">
                <value ref="lang_{@id}_{$theElement/parent::node()/gn:element/@ref}"
                       lang="{@id}"><xsl:value-of select="$url"/></value>
              </xsl:when>
              <xsl:when test="$ptFreeElementDoesNotExist">
                <value ref="lang_{@id}_{$theElement/parent::node()/gn:element/@ref}"
                       lang="{@id}"></value>
              </xsl:when>
            </xsl:choose>

          </xsl:for-each>
        </values>
      </xsl:if>
    </xsl:variable>

    <xsl:variable name="labelConfig">
      <xsl:choose>
        <xsl:when test="$overrideLabel != ''">
          <element>
            <label><xsl:value-of select="$overrideLabel"/></label>
          </element>
        </xsl:when>
        <xsl:otherwise>
          <xsl:copy-of select="$labelConfig"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:call-template name="render-element">
      <xsl:with-param name="label"
                      select="$labelConfig/*"/>
      <xsl:with-param name="value"
                      select="if ($isMultilingualElement)
                              then $values else *"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <!--<xsl:with-param name="widget"/>
        <xsl:with-param name="widgetParams"/>-->
      <xsl:with-param name="xpath" select="$xpath"/>
      <xsl:with-param name="attributesSnippet" select="$attributes"/>
      <xsl:with-param name="type"
                      select="gn-fn-metadata:getFieldType($editorConfig, name(),
        name($theElement), 'xpath_need_to_be_computed_merging_in_progress')"/>
      <xsl:with-param name="name"
                      select="if ($isEditing = true)
                              then $theElement/gn:element/@ref else ''"/>
      <xsl:with-param name="editInfo" select="$theElement/gn:element"/>
      <xsl:with-param name="parentEditInfo" select="gn:element"/>
      <!-- TODO: Handle conditional helper -->
      <xsl:with-param name="listOfValues" select="$helper"/>
      <xsl:with-param name="toggleLang" select="$isMultilingualElementExpanded"/>
      <xsl:with-param name="forceDisplayAttributes" select="false()"/>
      <xsl:with-param name="isFirst"
                      select="count(preceding-sibling::*[name() = $elementName]) = 0"/>
    </xsl:call-template>
  </xsl:template>


  <xsl:template mode="mode-iso19139" match="*[name() = 'gmd:MD_TopicCategoryCode' and $schema = 'iso19139.che']" priority="2000">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="codelists" select="$codelists" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>

    <xsl:variable name="name" select="name(.)"/>
    <xsl:variable name="value" select="string(.)"/>


    <xsl:variable name="list">
      <items>
        <xsl:for-each select="gn:element/gn:text">
          <xsl:variable name="choiceValue" select="string(@value)"/>
          <xsl:variable name="label" select="$codelists/codelist[@name = $name]/entry[code = $choiceValue]/label"/>
          <xsl:if test="$label">
            <item>
              <value>
                <xsl:if test="contains(@value,'_')">
                  <xsl:attribute name="parent">
                    <xsl:value-of select="substring-before(@value, '_')"/>
                  </xsl:attribute>
                </xsl:if>
                <xsl:value-of select="@value"/>
              </value>
              <label>
                  <xsl:value-of select="$label"/>
              </label>
            </item>
        </xsl:if>
        </xsl:for-each>
      </items>
    </xsl:variable>
    <xsl:variable name="fieldId" select="concat('gn-field-', gn:element/@ref)"/>

    <xsl:variable name="possible_derivation_count" select="count($list/items/item/value[@parent=$value])" />
    <xsl:variable name="effective_derivation_count" select="count(//gmd:topicCategory[starts-with(gmd:MD_TopicCategoryCode, concat($value, '_'))])" />
    <xsl:variable name="invalidCls" select="if($possible_derivation_count >0 and effective_derivation_count=0) then 'has-error' else ''"/>
    <xsl:variable name="unavalaibleLabel" select="$value and $possible_derivation_count=0 and count($list/items/item/value[text() = $value]) = 0"/>

    <xsl:choose>
      <xsl:when test="($possible_derivation_count > 0 and $effective_derivation_count > 0) or $unavalaibleLabel"/>

      <xsl:otherwise>
        <div class="form-group gn-field {$invalidCls} gn-required" data-gn-field-highlight="" id="gn-el-{gn:element/@ref}">
          <label for="{$fieldId}" class="col-sm-2 control-label">
            <xsl:value-of select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), '', '')/label"/>
          </label>
          <div class="col-sm-9 gn-value">
            <select id="{$fieldId}" class="form-control" name="_{gn:element/@ref}" size="1">
              <option name=""/>

              <xsl:for-each select="$list/items/item">
                <xsl:sort select="label"/>
                <xsl:variable name="curValue" select="value"/>
                <xsl:choose>
                  <xsl:when test="count($list/items/item/value[@parent=$curValue]) > 0">
                    <optgroup>
                      <xsl:attribute name="label">
                        <xsl:value-of select="label"/>
                      </xsl:attribute>
                      <xsl:for-each select="$list/items/item[value/@parent=$curValue]">
                        <option>
                          <xsl:if test="value=$value">
                            <xsl:attribute name="selected"/>
                          </xsl:if>
                          <xsl:attribute name="value">
                            <xsl:value-of select="value"/>
                          </xsl:attribute>
                          <xsl:value-of select="label"/>
                        </option>
                      </xsl:for-each>
                    </optgroup>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:if test="not(value/@parent)">
                      <option>
                        <xsl:if test="value=$value">
                          <xsl:attribute name="selected"/>
                        </xsl:if>
                        <xsl:attribute name="value">
                          <xsl:value-of select="value"/>
                        </xsl:attribute>
                        <xsl:value-of select="label"/>
                      </option>
                    </xsl:if>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:for-each>
            </select>
          </div>
          <div class="col-sm-1 gn-control" data-gn-field-highlight="">
            <xsl:call-template name="render-form-field-control-remove">
              <xsl:with-param name="editInfo" select="gn:element"/>
              <xsl:with-param name="parentEditInfo" select="../gn:element"/>
            </xsl:call-template>
          </div>

        </div>

      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>



  <!-- Handle codelist element which may have no match
  in profile codelist files. In such case, then use
  iso19139 codelist files.
  -->
  <xsl:template mode="mode-iso19139"
                priority="30000"
                match="*[*/@codeList and
                         $schema = 'iso19139.che' and
                         name() != 'gmd:dateType']">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>
    <xsl:param name="codelists" select="$schemaInfo/codelists" required="no"/>
    <xsl:param name="overrideLabel" select="''" required="no"/>


    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>
    <xsl:variable name="elementName" select="name()"/>
    <xsl:variable name="labelConfig">
      <xsl:choose>
        <xsl:when test="$overrideLabel != ''">
          <element>
            <label><xsl:value-of select="$overrideLabel"/></label>
          </element>
        </xsl:when>
        <xsl:otherwise>
          <xsl:copy-of select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), '', $xpath)"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <!-- check iso19139.che first, then fall back to iso19139 -->
    <xsl:variable name="listOfValues" as="node()">
      <xsl:variable name="profileCodeList"
                    as="node()"
                    select="gn-fn-metadata:getCodeListValues(
                              $schema, name(*[@codeListValue]), $codelists, .)"/>
      <xsl:choose>
        <xsl:when test="count($profileCodeList/*) = 0"> <!-- do iso19139 -->
          <xsl:copy-of select="gn-fn-metadata:getCodeListValues(
                                'iso19139', name(*[@codeListValue]), $iso19139codelists, .)"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:copy-of select="$profileCodeList"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>


    <xsl:call-template name="render-element">
      <xsl:with-param name="label" select="$labelConfig/*"/>
      <xsl:with-param name="value" select="*/@codeListValue"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="xpath" select="$xpath"/>
      <xsl:with-param name="type" select="gn-fn-iso19139:getCodeListType(name())"/>
      <xsl:with-param name="name"
                      select="if ($isEditing) then concat((*/gn:element/@ref)[1], '_codeListValue') else ''"/>
      <xsl:with-param name="editInfo" select="*/gn:element"/>
      <xsl:with-param name="parentEditInfo" select="gn:element"/>
      <xsl:with-param name="listOfValues" select="$listOfValues"/>
      <xsl:with-param name="isFirst" select="count(preceding-sibling::*[name() = $elementName]) = 0"/>
    </xsl:call-template>

  </xsl:template>


</xsl:stylesheet>
