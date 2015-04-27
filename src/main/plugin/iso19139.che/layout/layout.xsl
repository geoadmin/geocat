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

  <xsl:template mode="mode-iso19139" match="gmd:geographicElement[gmd:EX_GeographicBoundingBox]|
  gmd:geographicElement[gmd:EX_BoundingPolygon]" priority="2000">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>

    <xsl:if test="not(gmd:EX_GeographicBoundingBox) or not(../gmd:geographicElement/gmd:EX_BoundingPolygon)">

      <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(., true())"/>
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
                <img src="region.getmap.png?mapsrs=EPSG:21781&amp;width=250&amp;background=settings&amp;geom=Polygon(({*/gmd:westBoundLongitude/gco:Decimal}%20{*/gmd:northBoundLatitude/gco:Decimal},{*/gmd:eastBoundLongitude/gco:Decimal}%20{*/gmd:northBoundLatitude/gco:Decimal},{*/gmd:eastBoundLongitude/gco:Decimal}%20{*/gmd:southBoundLatitude/gco:Decimal},{*/gmd:westBoundLongitude/gco:Decimal}%20{*/gmd:southBoundLatitude/gco:Decimal},{*/gmd:westBoundLongitude/gco:Decimal}%20{*/gmd:northBoundLatitude/gco:Decimal}))&amp;geomsrs=EPSG:4326"/>
            </xsl:when>
            <xsl:when test="gmd:EX_BoundingPolygon">
                <img class="gn-img-extent"
                     src="region.getmap.png?mapsrs=EPSG:21781&amp;width=250&amp;background=settings&amp;id=metadata:@id{$metadataId}:@xpathgmd:identificationInfo{$xpath}/gmd:EX_BoundingPolygon"/>
            </xsl:when>
          </xsl:choose>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="mode-iso19139" priority="200"
    match="*[che:PT_FreeURL|che:LocalisedURL]">
    <xsl:param name="labels" select="$labels" required="no"/>
    <xsl:param name="overrideLabel" select="''" required="no"/>

    <xsl:variable name="elementName" select="name()"/>

    <xsl:variable name="hasOnlyPTFreeText" select="count(che:PT_FreeURL) > 0 and count(gmd:URL) = 0"/>
    <xsl:variable name="isMultilingualElement"
      select="$metadataIsMultilingual and
                    count($editorConfig/editor/multilingualFields/exclude[name = $elementName]) = 0"/>
    <xsl:variable name="isMultilingualElementExpanded"
      select="count($editorConfig/editor/multilingualFields/expanded[name = $elementName]) > 0"/>

    <xsl:variable name="theElement" select="if ($isMultilingualElement and $hasOnlyPTFreeText)
    then che:PT_FreeURL
    else
      gmd:URL"/>
    <xsl:variable name="xpath" select="gn-fn-metadata:getXPathByRef(gn:element/@ref, $metadata, true())"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>
    <xsl:variable name="labelConfig"
      select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), $isoType, $xpath)"/>
    <xsl:variable name="helper" select="gn-fn-metadata:getHelper($labelConfig/helper, .)"/>

    <xsl:variable name="attributes">

      <!-- Create form for all existing attribute (not in gn namespace)
      and all non existing attributes not already present for the
      current element and its children (eg. @uom in gco:Distance).
      A list of exception is defined in form-builder.xsl#render-for-field-for-attribute. -->
      <xsl:apply-templates mode="render-for-field-for-attribute"
        select="
            @*|
            gn:attribute[not(@name = parent::node()/@*/name())]">
        <xsl:with-param name="ref" select="gn:element/@ref"/>
        <xsl:with-param name="insertRef" select="$theElement/gn:element/@ref"/>
      </xsl:apply-templates>
      <xsl:apply-templates mode="render-for-field-for-attribute"
        select="
        */@*|
        */gn:attribute[not(@name = parent::node()/@*/name())]">
        <xsl:with-param name="ref" select="*/gn:element/@ref"/>
        <xsl:with-param name="insertRef" select="$theElement/gn:element/@ref"/>
      </xsl:apply-templates>
    </xsl:variable>

    <xsl:variable name="errors">
      <xsl:if test="$showValidationErrors">
        <xsl:call-template name="get-errors">
          <xsl:with-param name="theElement" select="$theElement"/>
        </xsl:call-template>
      </xsl:if>
    </xsl:variable>

    <xsl:variable name="values">
      <xsl:if test="$isMultilingualElement">

        <values>
          <!-- Or the PT_FreeText element matching the main language -->
          <xsl:if test="gmd:URL">
            <value ref="{$theElement/gn:element/@ref}" lang="{$metadataLanguage}"><xsl:value-of select="gmd:URL"/></value>
          </xsl:if>

          <!-- the existing translation -->
          <xsl:for-each select="che:PT_FreeURL/che:URLGroup/che:LocalisedURL">
            <value ref="{gn:element/@ref}" lang="{substring-after(@locale, '#')}"><xsl:value-of select="."/></value>
          </xsl:for-each>

          <!-- and create field for none translated language -->
          <xsl:for-each select="$metadataOtherLanguages/lang">
            <xsl:variable name="currentLanguageId" select="@id"/>
            <xsl:if test="count($theElement/parent::node()/
                che:PT_FreeURL/che:URLGroup/
                che:LocalisedURL[@locale = concat('#',$currentLanguageId)]) = 0">
              <value ref="lang_{@id}_{$theElement/parent::node()/gn:element/@ref}" lang="{@id}"></value>
            </xsl:if>
          </xsl:for-each>
        </values>
      </xsl:if>
    </xsl:variable>

    <xsl:call-template name="render-element">
      <xsl:with-param name="label" select="if ($overrideLabel != '') then $overrideLabel else $labelConfig/label"/>
      <xsl:with-param name="value" select="if ($isMultilingualElement) then $values else *"/>
      <xsl:with-param name="errors" select="$errors"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <!--<xsl:with-param name="widget"/>
        <xsl:with-param name="widgetParams"/>-->
      <xsl:with-param name="xpath" select="$xpath"/>
      <xsl:with-param name="attributesSnippet" select="$attributes"/>
      <xsl:with-param name="type"
        select="gn-fn-metadata:getFieldType($editorConfig, name(),
        name($theElement))"/>
      <xsl:with-param name="name" select="if ($isEditing) then $theElement/gn:element/@ref else ''"/>
      <xsl:with-param name="editInfo" select="$theElement/gn:element"/>
      <xsl:with-param name="parentEditInfo" select="gn:element"/>
      <!-- TODO: Handle conditional helper -->
      <xsl:with-param name="listOfValues" select="$helper"/>
      <xsl:with-param name="toggleLang" select="$isMultilingualElementExpanded"/>
      <xsl:with-param name="forceDisplayAttributes" select="false()"/>
      <xsl:with-param name="isFirst" select="count(preceding-sibling::*[name() = $elementName]) = 0"/>
    </xsl:call-template>
  </xsl:template>

  <!--GEOCAT Topic category-->
  <xsl:template mode="mode-iso19139" match="gmd:topicCategory[
                                              gmd:MD_TopicCategoryCode[1] = 'environment' or
                                              gmd:MD_TopicCategoryCode[1] = 'envirogeoscientificInformationnment' or
                                              gmd:MD_TopicCategoryCode[1] = 'planningCadastre' or
                                              gmd:MD_TopicCategoryCode[1] = 'imageryBaseMapsEarthCover' or
                                              gmd:MD_TopicCategoryCode[1] = 'utilitiesCommunication']" priority="2000">
    <!-- do nothing -->
  </xsl:template>

  <xsl:template mode="mode-iso19139" match="gmd:MD_TopicCategoryCode" priority="2000">

    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="codelists" select="$iso19139codelists" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>


    <xsl:variable name="name"  select="name(.)"/>
    <xsl:variable name="value" select="string(.)"/>

    <xsl:variable name="invalidValue" select="if($value = 'environment' or $value = 'geoscientificInformation'
        or $value = 'planningCadastre' or $value = 'imageryBaseMapsEarthCover' or $value= 'utilitiesCommunication') then 'true' else 'false'" />

    <xsl:variable name="invalidCls" select="if($invalidValue = 'true') then 'has-error' else ''" />

    <xsl:variable name="list">
      <items>
        <xsl:for-each select="gn:element/gn:text">
          <xsl:variable name="choiceValue" select="string(@value)"/>
          <xsl:variable name="label" select="$codelists/codelist[@name = $name]/entry[code = $choiceValue]/label"/>

          <item>
            <value>
              <xsl:if test="contains(@value,'_')">
                <xsl:attribute name="parent"><xsl:value-of select="substring-before(@value, '_')" /></xsl:attribute>
              </xsl:if>
              <xsl:value-of select="@value"/>
            </value>
            <label>
              <xsl:choose>
                <xsl:when test="$label"><xsl:value-of select="$label"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="$choiceValue"/></xsl:otherwise>
              </xsl:choose>
            </label>
          </item>
        </xsl:for-each>
      </items>
    </xsl:variable>
    <xsl:variable name="fieldId" select="concat('gn-field-', gn:element/@ref)" />

    <xsl:choose>
      <xsl:when test="$invalidValue and count(//gmd:topicCategory[starts-with(gmd:MD_TopicCategoryCode, concat($value, '_'))]) > 0">

        <!--Hide the topic cat if it is a root one and one of its children is within the metadata-->
        <div class="form-group gn-field hidden" data-gn-field-highlight="">
          <label for="{$fieldId}" class="col-sm-2 control-label">
            <xsl:value-of select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), '', '')/label"/>
          </label>
          <div class="col-sm-9 gn-value">
            <input id="{$fieldId}" class="form-control" name="_{gn:element/@ref}"  type="text" value="{$value}"></input>
          </div>
        </div>
      </xsl:when>
      <xsl:otherwise>
        <div class="form-group gn-field {$invalidCls} gn-required" data-gn-field-highlight="">
          <label for="{$fieldId}" class="col-sm-2 control-label">
            <xsl:value-of select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), '', '')/label"/>
          </label>
          <div class="col-sm-9 gn-value">
            <select id="{$fieldId}" class="form-control" name="_{gn:element/@ref}" size="1">
              <option name=""/>

              <xsl:for-each select="exslt:node-set($list)//item">
                <xsl:sort select="label" />
                <xsl:variable name="curValue" select="value"/>
                <xsl:choose>
                  <xsl:when test="count(exslt:node-set($list)//item/value[@parent=$curValue]) > 0">
                    <optgroup>
                      <xsl:attribute name="label"><xsl:value-of select="label" /></xsl:attribute>
                      <xsl:for-each select="exslt:node-set($list)//item[value/@parent=$curValue]">
                        <option>
                          <xsl:if test="value=$value">
                            <xsl:attribute name="selected" />
                          </xsl:if>
                          <xsl:attribute name="value"><xsl:value-of select="value" /></xsl:attribute>
                          <xsl:value-of select="label" />
                        </option>
                      </xsl:for-each>
                    </optgroup>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:if test="not(value/@parent)">
                      <option>
                        <xsl:if test="value=$value">
                          <xsl:attribute name="selected" />
                        </xsl:if>
                        <xsl:attribute name="value"><xsl:value-of select="value" /></xsl:attribute>
                        <xsl:value-of select="label" />
                      </option>
                    </xsl:if>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:for-each>
            </select>
          </div>
          <div class="col-sm-1 gn-control" data-gn-field-highlight="">
            <!--
                    <xsl:call-template name="render-boxed-element-control">
                      <xsl:with-param name="editInfo" select="gn:element"/>
                    </xsl:call-template>
            -->
          </div>

        </div>

      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>

  <xsl:template mode="mode-iso19139" priority="99999" match="che:*[*/@codeList] | gmd:associationType">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>
    <xsl:param name="codelists" select="$codelists" required="no"/>
    <xsl:param name="overrideLabel" select="''" required="no"/>


    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>
    <xsl:variable name="elementName" select="name()"/>

    <xsl:call-template name="render-element">
      <xsl:with-param name="label"
        select="if ($overrideLabel != '') then $overrideLabel else gn-fn-metadata:getLabel($schema, name(), $labels, name(..), $isoType, $xpath)/label"/>
      <xsl:with-param name="value" select="*/@codeListValue"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="xpath" select="$xpath"/>
      <xsl:with-param name="type" select="gn-fn-iso19139:getCodeListType(name())"/>
      <xsl:with-param name="name"
        select="if ($isEditing) then concat(*/gn:element/@ref, '_codeListValue') else ''"/>
      <xsl:with-param name="editInfo" select="*/gn:element"/>
      <xsl:with-param name="parentEditInfo" select="gn:element"/>
      <xsl:with-param name="listOfValues"
        select="gn-fn-metadata:getCodeListValues($schema, name(*[@codeListValue]), $codelists, .)"/>
      <xsl:with-param name="isFirst" select="count(preceding-sibling::*[name() = $elementName]) = 0"/>
    </xsl:call-template>

  </xsl:template>

</xsl:stylesheet>