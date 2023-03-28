<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:exslt="http://exslt.org/common" xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:gmd="http://www.isotc211.org/2005/gmd" version="2.0" exclude-result-prefixes="exslt">

  <xsl:import href="process-utility.xsl"/>

  <!-- i18n information -->
  <xsl:variable name="add-extent-loc">
    <msg id="a" xml:lang="en">Keyword field contains place keywords (ie.</msg>
    <msg id="b" xml:lang="en">). Try to compute metadata extent using thesaurus.</msg>
    <msg id="a" xml:lang="fr">Certains mots clés sont de type géographique (ie.</msg>
    <msg id="b" xml:lang="fr">). Exécuter cette action pour essayer de calculer l'emprise à partir des thésaurus.</msg>
  </xsl:variable>

  <!-- GeoNetwork base url -->
  <xsl:param name="gurl" select="'http://localhost:8080/geonetwork'"/>

  <!-- The UI language. Thesaurus search is made according to GUI language -->
  <xsl:param name="lang" select="'en'"/>

  <!-- Replace or not existing extent -->
  <xsl:param name="replace" select="'0'"/>


  <xsl:variable name="replaceMode"
                select="geonet:parseBoolean($replace)"/>
  <xsl:variable name="serviceUrl"
                select="concat($gurl, '/srv/', $lang, '/xml.search.keywords?pNewSearch=true&amp;pTypeSearch=2&amp;pKeyword=')"/>


  <xsl:template name="list-add-extent-from-geokeywords">
    <suggestion process="add-extent-from-geokeywords"/>
  </xsl:template>


  <!-- Analyze the metadata record and return available suggestion
      for that process -->
  <xsl:template name="analyze-add-extent-from-geokeywords">
    <xsl:param name="root"/>

    <xsl:variable name="extentDescription"
                  select="string-join($root//gmd:EX_Extent/gmd:description/gco:CharacterString, ' ')"/>

    <xsl:variable name="geoKeywords"
                  select="$root//gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword[
                      not(gco:CharacterString/@gco:nilReason)
                      and not(contains($extentDescription, gco:CharacterString))
                      and ../gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='place']"/>
    <xsl:if test="$geoKeywords">
      <suggestion process="add-extent-from-geokeywords" id="{generate-id()}" category="keyword" target="extent">
        <name>
          <xsl:value-of select="geonet:i18n($add-extent-loc, 'a', $guiLang)"/><xsl:value-of
          select="string-join($geoKeywords, ', ')"/>
          <xsl:value-of select="geonet:i18n($add-extent-loc, 'b', $guiLang)"/>
        </name>
        <operational>true</operational>
        <params>{gurl:{type:'string', defaultValue:'<xsl:value-of select="$gurl"/>'},
          lang:{type:'string', defaultValue:'<xsl:value-of select="$lang"/>'},
          replace:{type:'boolean', defaultValue:'<xsl:value-of select="$replace"/>'}}
        </params>
      </suggestion>
    </xsl:if>

  </xsl:template>


  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>

  <xsl:template
    match="gmd:identificationInfo/gmd:MD_DataIdentification|
        gmd:identificationInfo/*[@gco:isoType='gmd:MD_DataIdentification']|
        gmd:identificationInfo/srv:SV_ServiceIdentification|
        gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']"
    priority="2">

    <xsl:variable name="srv"
                  select="local-name(.)='SV_ServiceIdentification'
            or @gco:isoType='srv:SV_ServiceIdentification'"/>

    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <!-- Copy all elements from AbstractMD_IdentificationType-->
      <xsl:copy-of
        select="gmd:citation|
                gmd:abstract|
                gmd:purpose|
                gmd:credit|
                gmd:status|
                gmd:pointOfContact|
                gmd:resourceMaintenance|
                gmd:graphicOverview|
                gmd:resourceFormat|
                gmd:descriptiveKeywords|
                gmd:resourceSpecificUsage|
                gmd:resourceConstraints|
                gmd:aggregationInfo
                "/>

      <!-- Data -->
      <xsl:copy-of
        select="gmd:spatialRepresentationType|
                gmd:spatialResolution|
                gmd:language|
                gmd:characterSet|
                gmd:topicCategory|
                gmd:environmentDescription
                "/>

      <!-- Service -->
      <xsl:copy-of
        select="srv:serviceType|
                srv:serviceTypeVersion|
                srv:accessProperties|
                srv:restrictions|
                srv:keywords
                "/>

      <!-- Keep existing extent and compute
            from keywords -->

      <!-- replace or add extent. Default mode is add.
            All extent element are processed and if a geographicElement is found,
            it will be removed. Description, verticalElement and temporalElement
            are preserved.

            GeographicElement element having BoundingPolygon are preserved.
            -->
      <xsl:choose>
        <xsl:when test="$replaceMode">
          <xsl:for-each select="srv:extent|gmd:extent">
            <xsl:if
              test="gmd:EX_Extent/gmd:temporalElement or gmd:EX_Extent/gmd:verticalElement
                            or gmd:EX_Extent/gmd:geographicElement[gmd:EX_BoundingPolygon]">
              <xsl:copy>
                <xsl:copy-of select="gmd:EX_Extent"/>
              </xsl:copy>
            </xsl:if>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:copy-of select="srv:extent|gmd:extent"/>
        </xsl:otherwise>
      </xsl:choose>

      <!-- New extent position is after existing ones. -->
      <xsl:call-template name="add-extent">
        <xsl:with-param name="srv" select="$srv"/>
      </xsl:call-template>

      <!-- End of data -->
      <xsl:copy-of select="gmd:supplementalInformation"/>

      <!-- End of service -->
      <xsl:copy-of
        select="srv:coupledResource|
                srv:couplingType|
                srv:containsOperations|
                srv:operatesOn
                "/>

      <!-- Note: When applying this stylesheet
            to an ISO profil having a new substitute for
            MD_Identification, profil specific element copy.
            -->
      <xsl:for-each
        select="*[namespace-uri()!='http://www.isotc211.org/2005/gmd' and namespace-uri()!='http://www.isotc211.org/2005/srv']">
        <xsl:copy-of select="."/>
      </xsl:for-each>
    </xsl:copy>
  </xsl:template>


  <!-- Loop on all non empty keywords -->
  <xsl:template name="add-extent">
    <xsl:param name="srv" select="false()"/>
    <xsl:for-each
      select="gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword[not(gco:CharacterString/@gco:nilReason)]">

      <xsl:call-template name="get-bbox">
        <xsl:with-param name="word" select="gco:CharacterString"/>
        <xsl:with-param name="srv" select="$srv"/>
      </xsl:call-template>

    </xsl:for-each>
  </xsl:template>


  <!-- Search into current thesaurus and look for a bounding box -->
  <xsl:template name="get-bbox">
    <xsl:param name="word"/>
    <xsl:param name="srv" select="false()"/>

    <!-- Get keyword information -->
    <xsl:variable name="keyword" select="document(concat($serviceUrl, gco:CharacterString))"/>
    <xsl:variable name="knode" select="exslt:node-set($keyword)"/>

    <!-- It should be one but if one keyword is found in more
        thant one thesaurus, then each will be processed.-->
    <xsl:for-each select="$knode/response/descKeys/keyword">
      <xsl:if test="geo">
        <xsl:choose>
          <xsl:when test="$srv">
            <srv:extent>
              <xsl:copy-of select="geonet:make-iso-extent(geo/west, geo/south, geo/east, geo/north, $word)"/>
            </srv:extent>
          </xsl:when>
          <xsl:otherwise>
            <gmd:extent>
              <xsl:copy-of select="geonet:make-iso-extent(geo/west, geo/south, geo/east, geo/north, $word)"/>
            </gmd:extent>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>
