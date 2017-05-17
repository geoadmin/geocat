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
    <sch:rule context="/che:CHE_MD_Metadata/gmd:metadataMaintenance/che:CHE_MD_MaintenanceInformation/che:appraisal/che:CHE_MD_Appraisal_AAP">

      <sch:let name="durationOfConservation" value="che:durationOfConservation/gco:Integer/text()"/>
      <sch:let name="appraisalOfArchivalValue" value="che:appraisalOfArchivalValue/che:CHE_AppraisalOfArchivalValueCode/@codeListValue" />
      <sch:let name="reasonForArchiving" value="che:reasonForArchivingValue/che:CHE_ReasonForArchivingValueCode/@codeListValue" />

      <!-- duration of conservation -->
      <sch:assert test="string-length($durationOfConservation) &gt; 0">
        <sch:value-of select="$loc/strings/durationOfConservationRequired"/>
      </sch:assert>
      <sch:report test="string-length($durationOfConservation) &gt; 0">
        <sch:value-of select="$loc/strings/durationOfConservationReport"/>
        <sch:value-of select="floor($durationOfConservation)"/>
      </sch:report>

      <!-- appraisal of archival value (codelist) -->
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
    <sch:rule context="/che:CHE_MD_Metadata/gmd:metadataMaintenance/che:CHE_MD_MaintenanceInformation/che:appraisal/che:CHE_MD_Appraisal_AAP">
      <sch:let name="appraisalOfArchivalValue" value="che:appraisalOfArchivalValue/che:CHE_AppraisalOfArchivalValueCode/@codeListValue" />
      <sch:let name="archWurdigOrSampling" value="$appraisalOfArchivalValue = 'S' or $appraisalOfArchivalValue = 'A'" />
      <sch:let name="reasonForArchiving" value="che:reasonForArchivingValue/che:CHE_ReasonForArchivingValueCode/@codeListValue" />
      <sch:let name="reasonPresent" value="string-length($reasonForArchiving) &gt; 0" />
      <sch:let name="reasonReport" value="if ($reasonPresent) then $loc/strings/reasonForArchivalValuePresent else $loc/strings/reasonForArchivalValueAbsent" />

      <sch:assert test="($archWurdigOrSampling and $reasonPresent) or (not($archWurdigOrSampling) and not($reasonPresent))">
        <sch:value-of select="$reasonReport" />
      </sch:assert>
      <sch:report test="($archWurdigOrSampling and $reasonPresent) or (not($archWurdigOrSampling) and not($reasonPresent))">
        <sch:value-of select="$loc/strings/reasonForArchivalValuePresenceReport" />
        <sch:value-of select="$reasonReport" />
      </sch:report>

    </sch:rule>
  </sch:pattern>

</sch:schema>
