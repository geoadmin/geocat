<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:geonet="http://www.fao.org/geonetwork"
                exclude-result-prefixes="#all"
                version="2.0">

  <xsl:output indent="yes"/>

  <xsl:variable name="uuid"
                select="//gmd:fileIdentifier/gco:CharacterString"/>

  <xsl:param name="thesauriDir"
             select="'/data/dev/gn/mongeosource/web/src/main/webapp/WEB-INF/data/config/codelist'"/>

  <xsl:variable name="inspire-themes"
                select="document(concat('file:///', $thesauriDir, '/external/thesauri/theme/httpinspireeceuropaeutheme-theme.rdf'))//skos:Concept"/>

  <xsl:variable name="hasSpatialScope"
                select="count(//*/gmd:identificationInfo/*/gmd:descriptiveKeywords[*/gmd:thesaurusName/*/gmd:title/*/text() = 'Spatial scope']) > 0"/>




  <xsl:template match="gmd:thesaurusName/*[
                          gmd:title/*/text() = 'Spatial scope']
                          /gmd:date/*/gmd:date/gco:Date[text() = '2019-08-07']">
    <gco:Date>2019-05-22</gco:Date>
  </xsl:template>



  <!-- No limitation to public access -->
  <xsl:template match="gmd:resourceConstraints[che:CHE_MD_LegalConstraints/gmd:otherConstraints/normalize-space(lower-case(gco:CharacterString)) = (
        'keine einschränkung',
        'keine einschränkungen',
        'keine')]">
    <gmd:resourceConstraints>
      <che:CHE_MD_LegalConstraints gco:isoType="gmd:MD_LegalConstraints">
        <gmd:accessConstraints>
          <gmd:MD_RestrictionCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#MD_RestrictionCode"
                                  codeListValue="otherRestrictions"/>
        </gmd:accessConstraints>
        <gmd:otherConstraints gco:nilReason="missing" xsi:type="gmd:PT_FreeText_PropertyType">
          <gmx:Anchor xlink:href="http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/noLimitations">no limitations to public access</gmx:Anchor>
          <gmd:PT_FreeText>
            <gmd:textGroup>
              <gmd:LocalisedCharacterString locale="#DE">no limitations to public access</gmd:LocalisedCharacterString>
            </gmd:textGroup>
          </gmd:PT_FreeText>
        </gmd:otherConstraints>
        <xsl:copy-of select="*/che:*"/>
      </che:CHE_MD_LegalConstraints>
    </gmd:resourceConstraints>

    <xsl:if test="*/gmd:useLimitation">
      <gmd:resourceConstraints>
        <gmd:MD_LegalConstraints>
          <gmd:useLimitation>
            <gco:CharacterString>Zugangs- und Nutzungsbedingungen</gco:CharacterString>
            <gmd:PT_FreeText>
              <gmd:textGroup>
                <gmd:LocalisedCharacterString locale="#DE">Zugangs- und Nutzungsbedingungen</gmd:LocalisedCharacterString>
              </gmd:textGroup>
            </gmd:PT_FreeText>
          </gmd:useLimitation>
          <gmd:useConstraints>
            <gmd:MD_RestrictionCode
              codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_RestrictionCode"
              codeListValue="otherRestrictions"/>
          </gmd:useConstraints>
          <gmd:otherConstraints>
            <xsl:copy-of select="*/gmd:useLimitation/*"/>
          </gmd:otherConstraints>
        </gmd:MD_LegalConstraints>
      </gmd:resourceConstraints>
    </xsl:if>
  </xsl:template>


  <!--
  XML document 'file.xml', record '50fb7bec-c4bd-4b71-a3f3-9b223ee998bb': When one of the default Coordinate Reference Systems are used (annex D.4 of Technical Guide), it must be coded using the HTTP URI Identifier column of annex and codeSpace node must not exists.
  -->
  <xsl:template match="gmd:referenceSystemInfo/*/gmd:referenceSystemIdentifier/*/gmd:codeSpace"/>

  <!--
  UPDATE metadata SET data = replace(data, 'https://www.opengis.net/def/crs/', 'http://www.opengis.net/def/crs/') WHERE data LIKE '%https://www.opengis.net/def/crs/%'-->
  <xsl:template match="*[starts-with(text(), 'https://www.opengis.net/def/crs/')]">
    <xsl:copy>
      <xsl:value-of select="replace(text(), 'https://www.opengis.net/def/crs/', 'http://www.opengis.net/def/crs/')"/>
    </xsl:copy>
  </xsl:template>



  <!--
  <gmd:pass gco:nilReason="unknown"/>
  -->
  <xsl:template match="gmd:pass">
    <xsl:variable name="booleanValue"
                  select="if (gco:Boolean = ('1', 'true')) then 'true'
                          else if (gco:Boolean = ('0', 'false')) then 'false' else ''"/>
    <xsl:copy>
      <xsl:choose>
        <xsl:when test="$booleanValue = ''">
          <xsl:attribute name="gco:nilReason" select="'unknown'"/>
        </xsl:when>
        <xsl:otherwise>
          <gco:Boolean><xsl:value-of select="$booleanValue"/></gco:Boolean>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>


  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()|comment()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|comment()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>

</xsl:stylesheet>
