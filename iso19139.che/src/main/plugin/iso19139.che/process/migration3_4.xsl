<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gts="http://www.isotc211.org/2005/gts"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:sdi="http://www.easysdi.org/2011/sdi"
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



  <xsl:variable name="sb218matchText"
                select="'Modèle de géodonnées'"/>

  <xsl:variable name="isInScopeOfSb218"
                select="count(*/gmd:identificationInfo/*/gmd:citation/*/
                          gmd:title[
                            starts-with(gco:CharacterString, $sb218matchText) or
                           */gmd:textGroup/gmd:LocalisedCharacterString[
                              starts-with(., $sb218matchText)]
                          ]) = 1"/>



  <xsl:variable name="hasExpiryDate"
                select="count(//gmd:CI_DateTypeCode[@codeListValue = 'expiry']) > 0"/>
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
      <xsl:namespace name="gts" select="'http://www.isotc211.org/2005/gts'"/>
      <xsl:namespace name="srv" select="'http://www.isotc211.org/2005/srv'"/>
      <xsl:namespace name="gml" select="'http://www.opengis.net/gml'"/>
      <xsl:namespace name="xsi" select="'http://www.w3.org/2001/XMLSchema-instance'"/>
      <xsl:namespace name="xlink" select="'http://www.w3.org/1999/xlink'"/>

      <xsl:apply-templates select="@*|*"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="@xsi:schemaLocation"/>

  <!-- Remove all easysdi extension -->
  <xsl:template match="sdi:*"/>

  <xsl:template match="gmd:topicCategory[count(*) = 0]"/>

  <xsl:template match="gmd:locale">
    <xsl:variable name="id" select="*/@id"/>
    <xsl:if test="count(preceding-sibling::gmd:locale[*/@id = $id]) = 0">
      <xsl:copy-of select="." copy-namespaces="no"/>
    </xsl:if>
  </xsl:template>

  <!-- Encode language following INSPIRE rules -->
  <xsl:template match="gmd:language[gco:CharacterString != '']">
    <xsl:element name="{name()}">
      <xsl:apply-templates select="@*"/>
      <gmd:LanguageCode codeList="http://www.loc.gov/standards/iso639-2/"
                        codeListValue="{gco:CharacterString}"/>
    </xsl:element>
  </xsl:template>
  <!-- Remove empty language which will break on schematron rule
  checking non empty values in codelist -->
  <xsl:template match="gmd:language[gco:CharacterString = '']"/>


  <!-- Convert expiry codelist to lastRevision -->
  <xsl:template match="gmd:CI_DateTypeCode/@codeListValue[. = 'expiry']">
    <xsl:attribute name="codeListValue">lastRevision</xsl:attribute>
  </xsl:template>

  <!-- https://jira.swisstopo.ch/browse/MGEO_SB-218
   Move description to name for all gmd:CI_OnlineResource
   where title starts with Modèle de géodonnées -->
  <xsl:template match="gmd:CI_OnlineResource[$isInScopeOfSb218]">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates select="gmd:linkage"/>
      <xsl:apply-templates select="gmd:protocol"/>
      <xsl:apply-templates select="gmd:applicationProfile"/>
      <xsl:if test="gmd:description">
        <gmd:name>
          <xsl:apply-templates select="gmd:description/*"/>
        </gmd:name>
      </xsl:if>
      <xsl:if test="gmd:name">
        <gmd:description>
          <xsl:apply-templates select="gmd:name/*"/>
        </gmd:description>
      </xsl:if>
      <xsl:apply-templates select="gmd:function"/>
    </xsl:copy>
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
                  select="replace(., '.*id=([0-9.]+)(&amp;.*|$)', '$1')"/>
    <xsl:variable name="type"
      select="replace(., '.*(featuretype|typename)=([A-Za-z0-9:_]+)(&amp;.*|$)', '$2')"/>


    <!-- Shared object stored in xlinks tables are
    now identified as custom. For reference dataset,
    gn: prefix is removed (eg. gn:gemeindenBB) -->
    <xsl:variable name="newType"
                  select="if ($type = 'gn:xlinks')
                          then 'custom'
                          else if ($type = 'gn:countries')
                          then 'landesgebiet'
                          else if ($type = 'gn:kantoneBB')
                          then 'kantonsgebiet'
                          else if ($type = 'gn:gemeindenBB')
                          then 'hoheitsgebiet'
                          else substring-after($type, ':')"/>

    <!-- Remap old id to new one
    LANDESGEBIET(NAME)=CountriesBB(NAME)
    KANTONSGEBIET(KANTONSNUM)=KantoneBB(KANTONSNR)
    HOHEITSGEBIET(BFS_NUMMER)=gemeindenBB(OBJECTVAL)
    -->

    <xsl:variable name="newId">
      <xsl:choose>
        <xsl:when test="$newType = 'landesgebiet'">
          <xsl:value-of select="if ($id = '0') then 'CH'
                                else if ($id = '1') then 'LI' else $id"/>
        </xsl:when>
        <xsl:otherwise>
          <!-- Old Ids were numerics-->
          <xsl:value-of select="if (contains($id, '.'))
                                then substring-before($id, '.')
                                else $id"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:attribute name="xlink:href" select="concat('local://srv/api/registries/entries/',
                    'geocatch-subtpl-extent-', $newType, '-',
                    normalize-space($newId), '?',
                      if (count($langs) > 0)
                      then concat('lang=', string-join(distinct-values($langs), '&amp;lang='))
                      else ''
                    )"/>
  </xsl:template>



  <!-- Old subtemplates are preserved, only the base URL is reworded
  and the list of metadata language added.
  -->
  <xsl:template match="@xlink:href[starts-with(., 'local://subtemplate?')]">

    <xsl:variable name="uuid"
      select="replace(., '.*uuid=([a-z0-9-]+)(&amp;.*|$)', '$1')"/>

    <xsl:variable name="params"
      select="replace(., '.*uuid=([a-z0-9-]+)(&amp;|$)(.*)', '$3')"/>

    <xsl:attribute name="xlink:href" select="concat(
                        'local://srv/api/registries/entries/',
                        $uuid, '?',
                        if ($params != '')
                          then normalize-space($params)
                          else '',
                        if (count($langs) > 0)
                          then concat('&amp;lang=',
                            string-join(distinct-values($langs), '&amp;lang='))
                          else ''
                        )"/>
  </xsl:template>


  <!-- Remove non validated XLinks.
  eg. local://eng/xml.keyword.get?thesaurus=local._none_.non_validated&amp;id=http%3A%2F%2Fcustom.shared.obj.ch%2Fconcept%2301521da7-8aa1-42da-b73c-271f5c566def,http%3A%2F%2Fcustom.shared.obj.ch%2Fconcept%23f5099d41-2b76-4efa-9736-918caccfa675,http%3A%2F%2Fcustom.shared.obj.ch%2Fconcept%23c93b198f-156c-4dc0-a0a8-9a237bd7304c,http%3A%2F%2Fcustom.shared.obj.ch%2Fconcept%2335d4f0be-449b-4f5d-b7c2-002160a07e17,http%3A%2F%2Fcustom.shared.obj.ch%2Fconcept%236deb04a9-cb57-4020-8925-9bf69de563f4,http%3A%2F%2Fcustom.shared.obj.ch%2Fconcept%230fe4668b-02f2-4800-a01c-8c381c15e03d,http%3A%2F%2Fcustom.shared.obj.ch%2Fconcept%239e6e35d9-a2c7-4f31-8f3a-476bdb7bda37,http%3A%2F%2Fcustom.shared.obj.ch%2Fconcept%238853db72-01d2-4df9-9567-fd6d2f3e3c1e,http%3A%2F%2Fcustom.shared.obj.ch%2Fconcept%231f09df8a-d811-44be-8748-56d70437fef0,http%3A%2F%2Fcustom.shared.obj.ch%2Fconcept%23a395a4c2-33da-4dc9-9282-9021173ef08a,http%3A%2F%2Fcustom.shared.obj.ch%2Fconcept%23be8f32a1-54f2-41b8-96a1-48480f753df1,http%3A%2F%2Fcustom.shared.obj.ch%2Fconcept%231d495352-eb56-4f5e-b7c7-94f23e8e5fca,http%3A%2F%2Fcustom.shared.obj.ch%2Fconcept%23f2a4aaca-efdd-4927-8bb6-e750208ab19a,http%3A%2F%2Fcustom.shared.obj.ch%2Fconcept%23af1a6e79-f33e-4adf-87f1-b1671c6f35ce&amp;multiple=true&amp;lang=eng,ger,ita,fre,roh&amp;textgroupOnly=true&amp;skipdescriptivekeywords=true
  -->
  <xsl:template match="*[starts-with(@xlink:href, 'local://eng/xml.keyword.get?') and contains(@xlink:href, 'keyword.get?thesaurus=local._none_.non_validated')]" priority="10"/>

  <xsl:template match="@xlink:href[starts-with(., 'local://eng/xml.keyword.get?') or starts-with(., 'local://che.keyword.get?')]">

    <!-- Extract parameters and remove old locales parameter which
    is replaced by lang parameters. -->
    <xsl:variable name="params"
      select="replace(
                replace(
                replace(
                replace(.,
                  '(local://eng/xml.keyword.get\?|local://che.keyword.get\?)(.*)', '$2'),
                  '(&amp;locales=fr,en,de,it|&amp;lang=eng,ger,ita,fre,roh)', ''),
                  'local._none_.geocat.ch', 'local.theme.geocat.ch'),
                  '&amp;multiple=true', '')"/>

    <xsl:attribute name="xlink:href"
                   select="concat('local://srv/api/registries/vocabularies/keyword?',
                                $params,
                                if (count($langs) > 0)
                                 then concat('&amp;lang=', string-join(distinct-values($langs), '&amp;lang='))
                                 else ''
                                 )"/>
  </xsl:template>




  <!--
  Parent identifier is replaced by aggregate with
  association type 'crossReference'.
  https://jira.swisstopo.ch/browse/MGEO_SB-73
  -->
  <xsl:template match="gmd:parentIdentifier"/>

  <xsl:template match="gmd:identificationInfo/*">

    <!-- In production, some service records contains
    first an empty DataIdentification block that we ignore here -->
    <xsl:if test="name(following-sibling::*[1]) != 'che:CHE_SV_ServiceIdentification' and
                  name(following-sibling::*[1]) != 'srv:SV_ServiceIdentification'">
      <xsl:element name="{name()}">
        <xsl:copy-of select="@*"/>
        <xsl:apply-templates select="gmd:citation"/>
        <xsl:apply-templates select="gmd:abstract"/>
        <xsl:apply-templates select="gmd:purpose"/>
        <xsl:apply-templates select="gmd:credit"/>
        <xsl:apply-templates select="gmd:status"/>

        <!-- If expiry then flag record as completed -->
        <xsl:if test="$hasExpiryDate and
                      count(gmd:status[gmd:MD_ProgressCode/@codeListValue = 'completed']) = 0">
          <gmd:status>
            <gmd:MD_ProgressCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#MD_ProgressCode"
                                 codeListValue="completed"/>
          </gmd:status>
        </xsl:if>

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
    </xsl:if>
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
