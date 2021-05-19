<?xml version="1.0" encoding="UTF-8" standalone="yes"?>

<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  queryBinding="xslt2">
  <sch:title xmlns="http://www.w3.org/2001/XMLSchema">AAP</sch:title>
  <sch:ns prefix="gml" uri="http://www.opengis.net/gml"/>
  <sch:ns prefix="gmd" uri="http://www.isotc211.org/2005/gmd"/>
  <sch:ns prefix="gmx" uri="http://www.isotc211.org/2005/gmx"/>
  <sch:ns prefix="srv" uri="http://www.isotc211.org/2005/srv"/>
  <sch:ns prefix="gco" uri="http://www.isotc211.org/2005/gco"/>
  <sch:ns prefix="che" uri="http://www.geocat.ch/2008/che"/>
  <sch:ns prefix="geonet" uri="http://www.fao.org/geonetwork"/>
  <sch:ns prefix="skos" uri="http://www.w3.org/2004/02/skos/core#"/>
  <sch:ns prefix="xlink" uri="http://www.w3.org/1999/xlink"/>

  <sch:pattern>
    <sch:title>$loc/strings/mandatoryFields</sch:title>

    <sch:rule context="//gmd:identificationInfo/che:CHE_MD_DataIdentification">
        <sch:let name="AAPKeyworkSet" value="gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharacterString[contains(., 'AAP')]"/>
        <sch:let name="appraisalSectionDefined" value="//gmd:resourceMaintenance/che:CHE_MD_MaintenanceInformation/che:appraisal/che:CHE_MD_Appraisal_AAP"/>

        <sch:let name="failure" value="$AAPKeyworkSet and not($appraisalSectionDefined)"/>

        <sch:assert test="not($failure)" see="gmd:resourceMaintenance/che:CHE_MD_MaintenanceInformation/geonet:child[@name='appraisal']/@uuid">
            <sch:value-of select="$loc/strings/appraisalSectionRequired"/>
        </sch:assert>
        <sch:report test="not($failure)">
            <sch:value-of select="$loc/strings/notAppraisalSectionRequired"/>
        </sch:report>
    </sch:rule>

    <sch:rule context="//gmd:resourceMaintenance/che:CHE_MD_MaintenanceInformation/che:appraisal/che:CHE_MD_Appraisal_AAP">

      <sch:let name="durationOfConservation" value="che:durationOfConservation/gco:Integer/text()"/>
      <sch:let name="appraisalOfArchivalValue" value="che:appraisalOfArchivalValue/che:CHE_AppraisalOfArchivalValueCode/@codeListValue" />
      <sch:let name="reasonForArchiving" value="che:reasonForArchivingValue/che:CHE_ReasonForArchivingValueCode/@codeListValue" />

      <sch:assert test="string-length($durationOfConservation) &gt; 0">
        <sch:value-of select="$loc/strings/durationOfConservationRequired"/>
      </sch:assert>
      <sch:report test="string-length($durationOfConservation) &gt; 0">
        <sch:value-of select="$loc/strings/durationOfConservationReport"/>
        <sch:value-of select="floor($durationOfConservation)"/>
      </sch:report>

      <sch:assert test="string-length($appraisalOfArchivalValue) &gt; 0">
        <sch:value-of select="$loc/strings/appraisalOfArchivalValueRequired"/>
      </sch:assert>
      <sch:report test="string-length($appraisalOfArchivalValue) &gt; 0">
        <sch:value-of select="$loc/strings/appraisalOfArchivalValueReport"/>
        <sch:value-of select="normalize-space($appraisalOfArchivalValue)"/>
      </sch:report>

    </sch:rule>
  </sch:pattern>

  <sch:pattern>
    <sch:title>$loc/strings/reasonForArchivalValuePresence</sch:title>
    <sch:rule context="//gmd:resourceMaintenance/che:CHE_MD_MaintenanceInformation/che:appraisal/che:CHE_MD_Appraisal_AAP">
      <sch:let name="appraisalOfArchivalValue" value="che:appraisalOfArchivalValue/che:CHE_AppraisalOfArchivalValueCode/@codeListValue" />
      <sch:let name="archWurdigOrSampling" value="$appraisalOfArchivalValue = 'S' or $appraisalOfArchivalValue = 'A'" />
      <sch:let name="reasonForArchiving" value="che:reasonForArchivingValue/che:CHE_ReasonForArchivingValueCode/@codeListValue" />
      <sch:let name="reasonPresent" value="string-length($reasonForArchiving) &gt; 0" />

      <sch:let name="failure" value="not($reasonPresent) and $archWurdigOrSampling"/>

      <sch:assert test="not($failure)">
        <sch:value-of select="$loc/strings/reasonForArchivalValueAbsent" />
      </sch:assert>
      <sch:report test="not($failure)">
        <sch:value-of select="$loc/strings/notReasonForArchivalValueAbsent" />
      </sch:report>

    </sch:rule>
  </sch:pattern>

  <sch:pattern>
    <sch:title>$loc/strings/officalAndAAP</sch:title>
    <sch:rule context="//gmd:identificationInfo/che:CHE_MD_DataIdentification">
        <sch:let name="AAPKeyworkSet" value="gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharacterString[contains(., 'AAP')]"/>
        <sch:let name="debase" value="gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharacterString[contains(., 'géodonnée de base')]"/>
        <sch:let name="basis" value="gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharacterString[contains(., 'Geobasisdaten')]"/>
        <sch:let name="dibase" value="gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharacterString[contains(., 'geodati di base')]"/>
        <sch:let name="official" value="gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharacterString[contains(., 'official geodata')]"/>
        <sch:let name="officialKeyworkSet" value="$debase or $basis or $dibase or $official"/>
        <sch:let name="manyTopicSet" value="count(//gmd:MD_TopicCategoryCode) > 1"/>

        <sch:let name="failure" value="$manyTopicSet and $AAPKeyworkSet and $officialKeyworkSet" />

        <sch:assert test="not($failure)">
            <sch:value-of select="$loc/strings/cantSetManyTopicsWhenOfficalAndAAP"/>
        </sch:assert>
        <sch:report test="not($failure)">
            <sch:value-of select="$loc/strings/notCantSetManyTopicsWhenOfficalAndAAP"/>
        </sch:report>
    </sch:rule>
  </sch:pattern>

</sch:schema>
