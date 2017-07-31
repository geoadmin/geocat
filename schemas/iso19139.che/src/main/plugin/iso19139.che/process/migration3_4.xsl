<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                exclude-result-prefixes="#all"
                version="2.0">

  <!-- Migration process for iso19139.che metadata for version 3.4 -->
  <xsl:output method="xml" indent="yes"/>

  <xsl:param name="nodeUrl" select="'https://www.geocat.ch/geonetwork/srv/'"/>

  <xsl:variable name="uuid"
                select="*/gmd:fileIdentifier/*/text()"/>

  <xsl:variable name="langs"
                select="*/gmd:language/gco:CharacterString|
                        */gmd:language/gmd:LanguageCode/@codeListValue|
                        */gmd:locale/gmd:PT_Locale/gmd:languageCode/gmd:LanguageCode/@codeListValue"/>

  <!--
  Remove hardcoded schemaLocation in records. Schema location
  is added by the application depending on the schema configuration
  (see schema-ident.xml).
  https://jira.swisstopo.ch/browse/MGEO_SB-2

  Try to cleanup namespaces by defining them at the root element level.
  -->
  <xsl:template match="che:CHE_MD_Metadata">
    <xsl:copy copy-namespaces="no">
      <xsl:namespace name="che" select="'http://www.geocat.ch/2008/che'"/>
      <xsl:namespace name="gmd" select="'http://www.isotc211.org/2005/gmd'"/>
      <xsl:namespace name="gco" select="'http://www.isotc211.org/2005/gco'"/>
      <xsl:namespace name="gmx" select="'http://www.isotc211.org/2005/gmx'"/>
      <xsl:namespace name="srv" select="'http://www.isotc211.org/2005/srv'"/>
      <xsl:namespace name="gml" select="'http://www.opengis.net/gml'"/>
      <xsl:namespace name="xsi" select="'http://www.w3.org/2001/XMLSchema-instance'"/>
      <xsl:namespace name="xlink" select="'http://www.w3.org/1999/xlink'"/>

      <xsl:apply-templates select="@*|*"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="@xsi:schemaLocation"/>



  <!-- Encode language following INSPIRE rules -->
  <xsl:template match="gmd:language[gco:CharacterString]">
    <xsl:element name="{name()}">
      <xsl:apply-templates select="@*"/>
      <gmd:LanguageCode codeList="http://www.loc.gov/standards/iso639-2/"
                        codeListValue="{gco:CharacterString}"/>
    </xsl:element>
  </xsl:template>


  <!-- Replace old link to resources.get to attachement API -->
  <xsl:template match="text()[contains(., '/resources.get?')]">
    <xsl:value-of select="replace(
      .,
      '(.*)/([a-zA-Z0-9_\-]+)/([a-z]{2,3})/{1,2}resources.get\?.*fname=([\w,\s-]+\.[\w]+)(&amp;.*|$)',
      concat($nodeUrl, 'api/records/', $uuid, '/attachments/$4')
      )"/>
  </xsl:template>

  <xsl:template match="gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString[not(starts-with(., 'http'))]">
    <xsl:copy copy-namespaces="no">
      <xsl:apply-templates select="@*"/>

      <xsl:value-of select="concat($nodeUrl, 'api/records/', $uuid, '/attachments/', .)"/>
    </xsl:copy>
  </xsl:template>


  <!-- XLink updates -->
  <!-- Remove deprecated ones but keep what's in the resolved (if any elements) -->
  <xsl:template match="@xlink:href[starts-with(., 'local://xml.reusable.deleted?')]"/>
  <xsl:template match="@xlink:href[starts-with(., 'local://xml.format.get?id=')]"/>
  <xsl:template match="@xlink:href[starts-with(., 'local://xml.user.get?')]"/>


  <!-- Remove non validated Xlinks ?
  eg. "local://xml.extent.get?id=255&amp;wfs=default&amp;typename=gn:non_validated&amp;format=GMD_BBOX&amp;extentTypeCode=true" -->
  <xsl:template match="*[contains(@xlink:href, 'gn:non_validated')]"/>


  <!-- Extent subtemplates are now located in metadata template
  as subtemplate with a uuid like: geocatch-subtpl-extent-custom-{{oldId}}

  Reference dataset like canton, ... are also loaded in metadata table
  with if like geocatch-subtpl-extent-{{type}}-{{uuid}}.
  -->
  <xsl:template match="@xlink:href[starts-with(.,
    'http://www.geocat.ch:80/geonetwork/srv/deu/xml.extent.get?') or
    starts-with(., 'local://xml.extent.get?')]">

    <xsl:variable name="id"
                  select="replace(., '.*id=([0-9]+)(&amp;.*|$)', '$1')"/>
    <xsl:variable name="type"
      select="replace(., '.*(featuretype|typename)=([A-Za-z0-9:_]+)(&amp;.*|$)', '$2')"/>


    <!-- Shared object stored in xlinks tables are
    now identified as custom. For reference dataset,
    gn: prefix is removed (eg. gn:gemeindenBB) -->
    <xsl:variable name="newType"
                  select="if ($type = 'gn:xlinks')
                          then 'custom'
                          else substring-after($type, ':')"/>

    <!-- Remap old id to new one TODO -->

    <xsl:value-of select="concat('local://srv/api/registries/entries/',
                    'geocatch-subtpl-extent-', $newType, '-', $id)"/>
  </xsl:template>



  <!-- Old subtemplates are preserved, only the base URL is reworded
  and the list of metadata language added. -->
  <xsl:template match="@xlink:href[starts-with(., 'local://subtemplate?')]">

    <xsl:variable name="uuid"
      select="replace(., '.*uuid=([a-z0-9-]+)(&amp;.*|$)', '$1')"/>

    <xsl:variable name="params"
      select="replace(., '.*uuid=([a-z0-9-]+)(&amp;|$)(.*)', '$3')"/>

    <xsl:value-of select="concat(
                        'local://srv/api/registries/entries/',
                        $uuid,
                        if ($params != '')
                          then concat('?', normalize-space($params))
                          else '',
                        if (count($langs) > 0)
                          then concat('&amp;lang=',
                            string-join(distinct-values($langs), '&amp;lang='))
                          else ''
                        )"/>
  </xsl:template>


  <xsl:template match="@xlink:href[starts-with(., 'local://che.keyword.get?')]">

    <xsl:variable name="params"
      select="replace(., 'local://che.keyword.get?(.*)', '$1')"/>

    <xsl:value-of select="concat('local://srv/api/registries/vocabularies/',
      '', $params)"/>
    <!-- TODO:
    * Add language parameter
    * Remove non validated keywords ? From https://tc-geocat.int.bgdi.ch/geonetwork/srv/eng/thesaurus.download?ref=local._none_.non_validated
    "local://eng/xml.keyword.get?thesaurus=local._none_.geocat.ch&amp;id=http%3A%2F%2Fcustom.shared.obj.ch%2Fconcept%2304c0f143-6611-4c2e-af22-e9a7f375e7ea&amp;multiple=true&amp;lang=eng,ger,ita,fre,roh&amp;textgroupOnly=true&amp;skipdescriptivekeywords=true"
    -->
  </xsl:template>

  <!--
  Parent identifier is replaced by aggregate with
  association type 'crossReference'.
  https://jira.swisstopo.ch/browse/MGEO_SB-73
  -->
  <xsl:template match="gmd:parentIdentifier"/>

  <xsl:template match="gmd:identificationInfo/*">
    <xsl:element name="{name()}">
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="gmd:citation"/>
      <xsl:apply-templates select="gmd:abstract"/>
      <xsl:apply-templates select="gmd:purpose"/>
      <xsl:apply-templates select="gmd:credit"/>
      <xsl:apply-templates select="gmd:status"/>
      <xsl:apply-templates select="gmd:pointOfContact"/>
      <xsl:apply-templates select="gmd:resourceMaintenance"/>
      <xsl:apply-templates select="gmd:graphicOverview"/>
      <xsl:apply-templates select="gmd:resourceFormat"/>
      <xsl:apply-templates select="gmd:descriptiveKeywords"/>
      <xsl:apply-templates select="gmd:resourceSpecificUsage"/>
      <xsl:apply-templates select="gmd:resourceConstraints"/>
      <xsl:apply-templates select="gmd:aggregationInfo"/>

      <!-- Move the parent identifier to an aggregate-->
      <xsl:variable name="parentIdentifier"
                    select="ancestor::che:CHE_MD_Metadata/gmd:parentIdentifier/gco:CharacterString"/>
      <xsl:if test="normalize-space($parentIdentifier) != ''">
        <gmd:aggregationInfo>
          <gmd:MD_AggregateInformation>
            <gmd:aggregateDataSetIdentifier>
              <gmd:MD_Identifier>
                <gmd:code>
                  <gco:CharacterString>
                    <xsl:value-of select="$parentIdentifier"/>
                  </gco:CharacterString>
                </gmd:code>
              </gmd:MD_Identifier>
            </gmd:aggregateDataSetIdentifier>
            <gmd:associationType>
              <gmd:DS_AssociationTypeCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#DS_AssociationTypeCode"
                                          codeListValue="largerWorkCitation"/>
            </gmd:associationType>
          </gmd:MD_AggregateInformation>
        </gmd:aggregationInfo>
      </xsl:if>

      <xsl:apply-templates select="gmd:spatialRepresentationType"/>
      <xsl:apply-templates select="gmd:spatialResolution"/>
      <xsl:apply-templates select="gmd:language"/>
      <xsl:apply-templates select="gmd:characterSet"/>
      <xsl:apply-templates select="gmd:topicCategory"/>
      <xsl:apply-templates select="gmd:environmentDescription"/>
      <xsl:apply-templates select="gmd:extent"/>
      <xsl:apply-templates select="gmd:supplementalInformation"/>

      <xsl:apply-templates select="srv:*"/>
      <xsl:apply-templates select="che:*"/>
    </xsl:element>
  </xsl:template>

  <!-- Do a copy of every nodes (removing extra namespaces) -->
  <xsl:template match="*">
    <xsl:element name="{name()}">
      <xsl:apply-templates select="@*|node()"/>
    </xsl:element>
  </xsl:template>

  <!-- Do a copy of every attributes and text node. -->
  <xsl:template match="@*|text()">
    <xsl:copy/>
  </xsl:template>

  <!-- Always remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>

</xsl:stylesheet>
