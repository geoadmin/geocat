<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:che="http://www.geocat.ch/2008/che" version="2.0"
                exclude-result-prefixes="gmd xsl gco srv geonet che">


  <!-- Example of replacements parameter:

     <replacements>
      <caseInsensitive>i</caseInsensitive>
      <replacement>
        <field>id.contact.individualName</field>
        <searchValue>John Doe</searchValue>
        <replaceValue>Jennifer Smith</replaceValue>
      </replacement>
      <replacement>
        <field>id.contact.organisationName</field>
        <searchValue>Acme</searchValue>
        <replaceValue>New Acme</replaceValue>
      </replacement>
    </replacements>
  -->
  <xsl:param name="replacements"/>

  <!-- by default is case sensitive, sending i value in the param makes replacements case insensitive -->
  <xsl:variable name="case_insensitive" select="$replacements/replacements/caseInsensitive"/>

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- METADATA CONTACT updates: che:CHE_MD_Metadata/gmd:contact -->
  <!-- individualName -->
  <xsl:template match="che:CHE_MD_Metadata/gmd:contact/che:CHE_CI_ResponsibleParty/*[name()='che:individualFirstName' or name()='che:individualLastName']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.contact.individualName</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- organisationName -->
  <xsl:template match="che:CHE_MD_Metadata/gmd:contact/che:CHE_CI_ResponsibleParty/*[name()='gmd:organisationName' or name()='che:organisationAcronym']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.contact.organisationName</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- voice -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/*[name()='gmd:voice' or name()='che:directNumber' or name()='che:mobile']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.contact.voicePhone</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- facsimile -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:facsimile">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.contact.faxPhone</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- deliveryPoint -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/*[name()='che:streetName' or name()='che:streetNumber']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.contact.address</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- city -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:city">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.contact.city</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- administrativeArea/province -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:administrativeArea">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.contact.province</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- postalCode -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:postalCode">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.contact.postalCode</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- country -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:country">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.contact.country</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- email -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:electronicMailAddress">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.contact.email</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- hoursOfService -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:hoursOfService">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.contact.hoursOfService</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- contactInstructions -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:contactInstructions">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.contact.contactInstructions</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- onlineResource url -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage//*[name()='gmd:URL' or name()='che:LocalisedURL']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.contact.or.url</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- onlineResource protocol -->
  <!--<xsl:template match="che:CHE_MD_Metadata/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:protocol">
      <xsl:call-template name="replaceField">
          <xsl:with-param name="fieldId">id.contact.or.protocol</xsl:with-param>
      </xsl:call-template>
  </xsl:template>-->

  <!-- onlineResource app profile -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:applicationProfile">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.contact.or.ap</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- onlineResource name -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:name">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.contact.or.name</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- onlineResource description -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:description">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.contact.or.description</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- IDENTIFICATION updates: che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact -->
  <!-- individualName -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/*[name()='che:individualFirstName' or name()='che:individualLastName']|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='che:CHE_MD_DataIdentification']/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/*[name()='che:individualFirstName' or name()='che:individualLastName']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.dataid.poc.individualName</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- organisationName -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/*[name()='gmd:organisationName' or name()='che:organisationAcronym']|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='che:CHE_MD_DataIdentification']/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/*[name()='gmd:organisationName' or name()='che:organisationAcronym']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.dataid.poc.organisationName</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- voice -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/*[name()='gmd:voice' or name()='che:directNumber' or name()='che:mobile']|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='che:CHE_MD_DataIdentification']/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/*[name()='gmd:voice' or name()='che:directNumber' or name()='che:mobile']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.dataid.poc.voicePhone</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- facsimile -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:facsimile|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='che:CHE_MD_DataIdentification']/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:facsimile">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.dataid.poc.faxPhone</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- deliveryPoint -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/*[name()='che:streetName' or name()='che:streetNumber']|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='che:CHE_MD_DataIdentification']/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/*[name()='che:streetName' or name()='che:streetNumber']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.dataid.poc.address</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- city -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:city|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='che:CHE_MD_DataIdentification']/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:city">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.dataid.poc.city</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- administrativeArea/province -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:administrativeArea|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='che:CHE_MD_DataIdentification']/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:administrativeArea">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.dataid.poc.province</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- postalCode -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:postalCode|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='che:CHE_MD_DataIdentification']/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:postalCode">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.dataid.poc.postalCode</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- country -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:country|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='che:CHE_MD_DataIdentification']/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:country">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.dataid.poc.country</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- email -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:electronicMailAddress|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='che:CHE_MD_DataIdentification']/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:electronicMailAddress">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.dataid.poc.email</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- hoursOfService -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:hoursOfService|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='che:CHE_MD_DataIdentification']/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:hoursOfService">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.dataid.poc.hoursOfService</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- contactInstructions -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:contactInstructions|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='che:CHE_MD_DataIdentification']/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:contactInstructions">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.dataid.poc.contactInstructions</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- onlineResource url -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage//*[name()='gmd:URL' or name()='che:LocalisedURL']|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='che:CHE_MD_DataIdentification']/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage//*[name()='gmd:URL' or name()='che:LocalisedURL']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.dataid.poc.or.url</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- onlineResource app profile -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:applicationProfile|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='che:CHE_MD_DataIdentification']/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:applicationProfile">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.dataid.poc.or.ap</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- onlineResource name -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:name|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='che:CHE_MD_DataIdentification']/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:name">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.dataid.poc.or.name</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- onlineResource description -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:description|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='che:CHE_MD_DataIdentification']/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:description">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.dataid.poc.or.description</xsl:with-param>
    </xsl:call-template>
  </xsl:template>


  <!-- abstract -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:abstract|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='che:CHE_MD_DataIdentification']/gmd:abstract">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.dataid.abstract</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- purpose -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:purpose|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='che:CHE_MD_DataIdentification']/gmd:purpose">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.dataid.purpose</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- IDENTIFICATION CITATION updates: che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/che:CHE_CI_ResponsibleParty  -->
  <!-- individualName -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/*[name()='che:individualFirstName' or name()='che:individualLastName']|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='che:CHE_MD_DataIdentification']/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/*[name()='che:individualFirstName' or name()='che:individualLastName']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.dataid.citation.individualName</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- organisationName -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/*[name()='gmd:organisationName' or name()='che:organisationAcronym']|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='che:CHE_MD_DataIdentification']/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/*[name()='gmd:organisationName' or name()='che:organisationAcronym']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.dataid.citation.organisationName</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- voice -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/*[name()='gmd:voice' or name()='che:directNumber' or name()='che:mobile']|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='che:CHE_MD_DataIdentification']/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/*[name()='gmd:voice' or name()='che:directNumber' or name()='che:mobile']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.dataid.citation.voicePhone</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- facsimile -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:facsimile|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='che:CHE_MD_DataIdentification']/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:facsimile">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.dataid.citation.faxPhone</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- deliveryPoint -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/*[name()='che:streetName' or name()='che:streetNumber']|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='che:CHE_MD_DataIdentification']/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/*[name()='che:streetName' or name()='che:streetNumber']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.dataid.citation.address</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- city -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:city|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='che:CHE_MD_DataIdentification']/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:city">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.dataid.citation.city</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- administrativeArea/province -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:administrativeArea|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='che:CHE_MD_DataIdentification']/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:administrativeArea">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.dataid.citation.province</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- postalCode -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:postalCode|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='che:CHE_MD_DataIdentification']/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:postalCode">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.dataid.citation.postalCode</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- country -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:country|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='che:CHE_MD_DataIdentification']/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:country">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.dataid.citation.country</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- email -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:electronicMailAddress|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='che:CHE_MD_DataIdentification']/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:electronicMailAddress">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.dataid.citation.email</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- onlineResource url -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage//*[name()='gmd:URL' or name()='che:LocalisedURL']|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='che:CHE_MD_DataIdentification']/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage//*[name()='gmd:URL' or name()='che:LocalisedURL']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.dataid.citation.or.url</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- onlineResource app profile -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:applicationProfile|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='che:CHE_MD_DataIdentification']/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:applicationProfile">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.dataid.citation.or.ap</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- onlineResource name -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:name|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='che:CHE_MD_DataIdentification']/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:name">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.dataid.citation.or.name</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- onlineResource description -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:description|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='che:CHE_MD_DataIdentification']/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:description">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.dataid.citation.or.description</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- hoursOfService -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:hoursOfService|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='che:CHE_MD_DataIdentification']/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:hoursOfService">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.dataid.citation.hoursOfService</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- contactInstructions -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:contactInstructions|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='che:CHE_MD_DataIdentification']/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:contactInstructions">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.dataid.citation.contactInstructions</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- keywords -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='che:CHE_MD_DataIdentification']/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.dataid.keyword</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- resource constraints general - use limitation -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:resourceConstraints/gmd:MD_Constraints/gmd:useLimitation|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='che:CHE_MD_DataIdentification']/gmd:resourceConstraints/gmd:MD_Constraints/gmd:useLimitation">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.dataid.resc.gc.useLimitation</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- resource constraints legal - use limitation -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:resourceConstraints/che:CHE_MD_LegalConstraints/gmd:useLimitation|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='che:CHE_MD_DataIdentification']/gmd:resourceConstraints/che:CHE_MD_LegalConstraints/gmd:useLimitation">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.dataid.resc.lc.useLimitation</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- resource constraints legal - other constraints -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:resourceConstraints/che:CHE_MD_LegalConstraints/gmd:otherConstraints|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='che:CHE_MD_DataIdentification']/gmd:resourceConstraints/che:CHE_MD_LegalConstraints/gmd:otherConstraints">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.dataid.resc.lc.otherConstraints</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- resource constraints security - use limitation -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:resourceConstraints/gmd:MD_SecurityConstraints/gmd:useLimitation|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='che:CHE_MD_DataIdentification']/gmd:resourceConstraints/gmd:MD_SecurityConstraints/gmd:useLimitation">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.dataid.resc.sc.useLimitation</xsl:with-param>
    </xsl:call-template>
  </xsl:template>


  <!-- SERVICE IDENTIFICATION -->
  <!-- abstract -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:abstract|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:abstract">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.serviceid.abstract</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- purpose -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:purpose|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:purpose">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.serviceid.purpose</xsl:with-param>
    </xsl:call-template>
  </xsl:template>


  <!-- individualName -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/*[name()='che:individualFirstName' or name()='che:individualLastName']|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/*[name()='che:individualFirstName' or name()='che:individualLastName']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.serviceid.citation.individualName</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- organisationName -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/*[name()='gmd:organisationName' or name()='che:organisationAcronym']|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/*[name()='gmd:organisationName' or name()='che:organisationAcronym']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.serviceid.citation.organisationName</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- voice -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/*[name()='gmd:voice' or name()='che:directNumber' or name()='che:mobile']|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/*[name()='gmd:voice' or name()='che:directNumber' or name()='che:mobile']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.serviceid.citation.voicePhone</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- facsimile -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:facsimile|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:facsimile">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.serviceid.citation.faxPhone</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- deliveryPoint -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/*[name()='che:streetName' or name()='che:streetNumber']|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/*[name()='che:streetName' or name()='che:streetNumber']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.serviceid.citation.address</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- city -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:city|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:city">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.serviceid.citation.city</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- administrativeArea/province -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:administrativeArea|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:administrativeArea">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.serviceid.citation.province</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- postalCode -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:postalCode|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:postalCode">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.serviceid.citation.postalCode</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- country -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:country|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:country">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.serviceid.citation.country</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- email -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:electronicMailAddress|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:electronicMailAddress">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.serviceid.citation.email</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- onlineResource url -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage//*[name()='gmd:URL' or name()='che:LocalisedURL']|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage//*[name()='gmd:URL' or name()='che:LocalisedURL']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.serviceid.citation.or.url</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- onlineResource app profile -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:applicationProfile|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:applicationProfile">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.serviceid.citation.or.ap</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- onlineResource name -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:name|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:name">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.serviceid.citation.or.name</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- onlineResource description -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:description|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:description">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.serviceid.citation.or.description</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- hoursOfService -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:hoursOfService|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:hoursOfService">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.serviceid.citation.hoursOfService</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- contactInstructions -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:contactInstructions|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:contactInstructions">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.serviceid.citation.contactInstructions</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- poc -->
  <!-- individualName -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/*[name()='che:individualFirstName' or name()='che:individualLastName']|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/*[name()='che:individualFirstName' or name()='che:individualLastName']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.serviceid.poc.individualName</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- organisationName -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/*[name()='gmd:organisationName' or name()='che:organisationAcronym']|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/*[name()='gmd:organisationName' or name()='che:organisationAcronym']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.serviceid.poc.organisationName</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- voice -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/*[name()='gmd:voice' or name()='che:directNumber' or name()='che:mobile']|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/*[name()='gmd:voice' or name()='che:directNumber' or name()='che:mobile']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.serviceid.poc.voicePhone</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- facsimile -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:facsimile|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:facsimile">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.serviceid.poc.faxPhone</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- deliveryPoint -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/*[name()='che:streetName' or name()='che:streetNumber']|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/*[name()='che:streetName' or name()='che:streetNumber']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.serviceid.poc.address</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- city -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:city|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:city">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.serviceid.poc.city</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- administrativeArea/province -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:administrativeArea|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:administrativeArea">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.serviceid.poc.province</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- postalCode -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:postalCode|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:postalCode">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.serviceid.poc.postalCode</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- country -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:country|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:country">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.serviceid.poc.country</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- email -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:electronicMailAddress|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:electronicMailAddress">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.serviceid.poc.email</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- hoursOfService -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:hoursOfService|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:hoursOfService">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.serviceid.poc.hoursOfService</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- contactInstructions -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:contactInstructions|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:contactInstructions">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.serviceid.poc.contactInstructions</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- onlineResource url -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage//*[name()='gmd:URL' or name()='che:LocalisedURL']|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage//*[name()='gmd:URL' or name()='che:LocalisedURL']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.serviceid.poc.or.url</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- onlineResource app profile -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:applicationProfile|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:applicationProfile">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.serviceid.poc.or.ap</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- onlineResource name -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:name|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:name">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.serviceid.poc.or.name</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- onlineResource description -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:description|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:description">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.serviceid.poc.or.description</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- connect point -->
  <!-- onlineResource url -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/srv:containsOperations/srv:SV_OperationMetadata/srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage//*[name()='gmd:URL' or name()='che:LocalisedURL']|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/srv:containsOperations/srv:SV_OperationMetadata/srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage//*[name()='gmd:URL' or name()='che:LocalisedURL']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.serviceid.connectpoint.url</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- onlineResource app profile -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/srv:containsOperations/srv:SV_OperationMetadata/srv:connectPoint/gmd:CI_OnlineResource/gmd:applicationProfile|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/srv:containsOperations/srv:SV_OperationMetadata/srv:connectPoint/gmd:CI_OnlineResource/gmd:applicationProfile">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.serviceid.connectpoint.ap</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- onlineResource name -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/srv:containsOperations/srv:SV_OperationMetadata/srv:connectPoint/gmd:CI_OnlineResource/gmd:name|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/srv:containsOperations/srv:SV_OperationMetadata/srv:connectPoint/gmd:CI_OnlineResource/gmd:name">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.serviceid.connectpoint.name</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- onlineResource description -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/srv:containsOperations/srv:SV_OperationMetadata/srv:connectPoint/gmd:CI_OnlineResource/gmd:description|che:CHE_MD_Metadata/gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/srv:containsOperations/srv:SV_OperationMetadata/srv:connectPoint/gmd:CI_OnlineResource/gmd:description">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">id.serviceid.connectpoint.description</xsl:with-param>
    </xsl:call-template>
  </xsl:template>


  <!-- MAINTENANCE INFORMATION updates:  che:CHE_MD_Metadata/gmd:metadataMaintenance -->
  <!-- individualName -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:metadataMaintenance/che:CHE_MD_MaintenanceInformation/gmd:contact/che:CHE_CI_ResponsibleParty/*[name()='che:individualFirstName' or name()='che:individualLastName']|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:metadataMaintenance/che:CHE_MD_MaintenanceInformation/gmd:contact/che:CHE_CI_ResponsibleParty/*[name()='che:individualFirstName' or name()='che:individualLastName']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">mi.contact.individualName</xsl:with-param>
    </xsl:call-template>
  </xsl:template>


  <!-- individualName -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:metadataMaintenance/che:CHE_MD_MaintenanceInformation/gmd:contact/che:CHE_CI_ResponsibleParty/*[name()='gmd:organisationName' or name()='che:organisationAcronym']|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:metadataMaintenance/che:CHE_MD_MaintenanceInformation/gmd:contact/che:CHE_CI_ResponsibleParty/*[name()='gmd:organisationName' or name()='che:organisationAcronym']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">mi.contact.organisationName</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- voice -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:metadataMaintenance/che:CHE_MD_MaintenanceInformation/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/*[name()='gmd:voice' or name()='che:directNumber' or name()='che:mobile']|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:metadataMaintenance/che:CHE_MD_MaintenanceInformation/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/*[name()='gmd:voice' or name()='che:directNumber' or name()='che:mobile']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">mi.contact.voicePhone</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- facsimile -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:metadataMaintenance/che:CHE_MD_MaintenanceInformation/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:facsimile|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:metadataMaintenance/che:CHE_MD_MaintenanceInformation/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:facsimile">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">mi.contact.faxPhone</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- deliveryPoint -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:metadataMaintenance/che:CHE_MD_MaintenanceInformation/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/*[name()='che:streetName' or name()='che:streetNumber']|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:metadataMaintenance/che:CHE_MD_MaintenanceInformation/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/*[name()='che:streetName' or name()='che:streetNumber']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">mi.contact.address</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- city -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:metadataMaintenance/che:CHE_MD_MaintenanceInformation/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:city|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:metadataMaintenance/che:CHE_MD_MaintenanceInformation/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:city">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">mi.contact.city</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- administrativeArea/province -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:metadataMaintenance/che:CHE_MD_MaintenanceInformation/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:administrativeArea|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:metadataMaintenance/che:CHE_MD_MaintenanceInformation/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:administrativeArea">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">mi.contact.province</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- country -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:metadataMaintenance/che:CHE_MD_MaintenanceInformation/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:country|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:metadataMaintenance/che:CHE_MD_MaintenanceInformation/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:country">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">mi.contact.country</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- postalCode -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:metadataMaintenance/che:CHE_MD_MaintenanceInformation/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:postalCode|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:metadataMaintenance/che:CHE_MD_MaintenanceInformation/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:postalCode">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">mi.contact.postalCode</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- email -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:metadataMaintenance/che:CHE_MD_MaintenanceInformation/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:electronicMailAddress|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:metadataMaintenance/che:CHE_MD_MaintenanceInformation/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:electronicMailAddress">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">mi.contact.email</xsl:with-param>
    </xsl:call-template>
  </xsl:template>


  <!-- onlineResource url -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:metadataMaintenance/che:CHE_MD_MaintenanceInformation/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage//*[name()='gmd:URL' or name()='che:LocalisedURL']|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:metadataMaintenance/che:CHE_MD_MaintenanceInformation/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage//*[name()='gmd:URL' or name()='che:LocalisedURL']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">mi.contact.or.url</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- onlineResource app profile -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:metadataMaintenance/che:CHE_MD_MaintenanceInformation/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:applicationProfile|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:metadataMaintenance/che:CHE_MD_MaintenanceInformation/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:applicationProfile">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">mi.contact.or.ap</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- onlineResource name -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:metadataMaintenance/che:CHE_MD_MaintenanceInformation/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:name|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:metadataMaintenance/che:CHE_MD_MaintenanceInformation/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:name">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">mi.contact.or.name</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- onlineResource description -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:metadataMaintenance/che:CHE_MD_MaintenanceInformation/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:description|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:metadataMaintenance/che:CHE_MD_MaintenanceInformation/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:description">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">mi.contact.or.description</xsl:with-param>
    </xsl:call-template>
  </xsl:template>


  <!-- hoursOfService -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:metadataMaintenance/che:CHE_MD_MaintenanceInformation/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:hoursOfService|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:metadataMaintenance/che:CHE_MD_MaintenanceInformation/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:hoursOfService">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">mi.contact.hoursOfService</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- contactInstructions -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:metadataMaintenance/che:CHE_MD_MaintenanceInformation/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:contactInstructions|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:metadataMaintenance/che:CHE_MD_MaintenanceInformation/gmd:contact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:contactInstructions">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">mi.contact.contactInstructions</xsl:with-param>
    </xsl:call-template>
  </xsl:template>


  <!-- CONTENT INFORMATION updates: che:CHE_MD_Metadata/gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:citedResponsibleParty    -->
  <!-- individualName -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/*[name()='che:individualFirstName' or name()='che:individualLastName']|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/*[name()='che:individualFirstName' or name()='che:individualLastName']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">ci.citation.individualName</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- organisationName -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/*[name()='gmd:organisationName' or name()='che:organisationAcronym']|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/*[name()='gmd:organisationName' or name()='che:organisationAcronym']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">ci.citation.organisationName</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- voice -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/*[name()='gmd:voice' or name()='che:directNumber' or name()='che:mobile']|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/*[name()='gmd:voice' or name()='che:directNumber' or name()='che:mobile']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">ci.citation.voicePhone</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- facsimile -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:facsimile|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:facsimile">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">ci.citation.faxPhone</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- deliveryPoint -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/*[name()='che:streetName' or name()='che:streetNumber']|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/*[name()='che:streetName' or name()='che:streetNumber']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">ci.citation.address</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- city -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:city|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:city">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">ci.citation.city</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- administrative area -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:administrativeArea|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:administrativeArea">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">ci.citation.province</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- postal code -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:postalCode|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:postalCode">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">ci.citation.postalCode</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- country -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:country|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:country">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">ci.citation.country</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- email -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:electronicMailAddress|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:electronicMailAddress">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">ci.citation.email</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- hours of service -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:hoursOfService|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:hoursOfService">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">ci.citation.hoursOfService</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- contact instructions -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:contactInstructions|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:contactInstructions">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">ci.citation.contactInstructions</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- onlineResource url -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage//*[name()='gmd:URL' or name()='che:LocalisedURL']|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage//*[name()='gmd:URL' or name()='che:LocalisedURL']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">ci.citation.or.url</xsl:with-param>
    </xsl:call-template>
  </xsl:template>


  <!-- onlineResource app profile -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:applicationProfile|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:applicationProfile">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">ci.citation.or.ap</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- onlineResource name -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:name|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:name">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">ci.citation.or.name</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- onlineResource description -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:description|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:citedResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:description">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">ci.citation.or.description</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- DISTRIBUTION INFORMATION updates: che:CHE_MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution -->
  <!-- individualName -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/*[name()='che:individualFirstName' or name()='che:individualLastName']|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/*[name()='che:individualFirstName' or name()='che:individualLastName']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">di.contact.individualName</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- organisationName -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/*[name()='gmd:organisationName' or name()='che:organisationAcronym']|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/*[name()='gmd:organisationName' or name()='che:organisationAcronym']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">di.contact.organisationName</xsl:with-param>
    </xsl:call-template>
  </xsl:template>


  <!-- voice -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/*[name()='gmd:voice' or name()='che:directNumber' or name()='che:mobile']|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/*[name()='gmd:voice' or name()='che:directNumber' or name()='che:mobile']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">di.contact.voicePhone</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- facsimile -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:facsimile|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:facsimile">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">di.contact.faxPhone</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- deliveryPoint -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/*[name()='che:streetName' or name()='che:streetNumber']|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/*[name()='che:streetName' or name()='che:streetNumber']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">di.contact.address</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- city -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:city|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:city">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">di.contact.city</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- administrative area -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:administrativeArea|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:administrativeArea">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">di.contact.province</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- country -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:country|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:country">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">di.contact.country</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- postal code -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:postalCode|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:postalCode">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">di.contact.postalCode</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- email -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:electronicMailAddress|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:electronicMailAddress">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">di.contact.email</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- hours of service -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:hoursOfService|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:hoursOfService">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">di.contact.hoursOfService</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- onlineResource url -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage//*[name()='gmd:URL' or name()='che:LocalisedURL']|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage//*[name()='gmd:URL' or name()='che:LocalisedURL']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">di.contact.or.url</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- onlineResource protocol -->
  <!--<xsl:template match="che:CHE_MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:protocol|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:protocol">
      <xsl:call-template name="replaceField">
          <xsl:with-param name="fieldId">di.contact.or.protocol</xsl:with-param>
      </xsl:call-template>
  </xsl:template>-->

  <!-- onlineResource app profile -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:applicationProfile|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:applicationProfile">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">di.contact.or.ap</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- onlineResource name -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:name|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:name">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">di.contact.or.name</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- onlineResource description -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:description|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:description">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">di.contact.or.description</xsl:with-param>
    </xsl:call-template>
  </xsl:template>


  <!-- contact instructions -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:contactInstructions|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:contactInstructions">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">di.contact.contactInstructions</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- fees -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributionOrderProcess/gmd:MD_StandardOrderProcess/gmd:fees|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributionOrderProcess/gmd:MD_StandardOrderProcess/gmd:fees">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">di.fees</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- ordering instructions -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributionOrderProcess/gmd:MD_StandardOrderProcess/gmd:orderingInstructions|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributionOrderProcess/gmd:MD_StandardOrderProcess/gmd:orderingInstructions">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">di.orderingInstructions</xsl:with-param>
    </xsl:call-template>
  </xsl:template>


  <!-- transfer options -->
  <!-- onlineResource url -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage//*[name()='gmd:URL' or name()='che:LocalisedURL']|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage//*[name()='gmd:URL' or name()='che:LocalisedURL']">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">di.transferOptions.url</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- onlineResource protocol -->
  <!--<xsl:template match="che:CHE_MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:protocol|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:protocol">
      <xsl:call-template name="replaceField">
          <xsl:with-param name="fieldId">di.transferOptions.protocol</xsl:with-param>
      </xsl:call-template>
  </xsl:template>-->

  <!-- onlineResource app profile -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:applicationProfile|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:applicationProfile">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">di.transferOptions.ap</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- onlineResource name -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:name|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:CI_OnlineResource/gmd:name">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">di.transferOptions.name</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- onlineResource description -->
  <xsl:template
    match="che:CHE_MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:description|*[@gco:isoType='che:CHE_MD_Metadata']/gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:description">
    <xsl:call-template name="replaceField">
      <xsl:with-param name="fieldId">di.transferOptions.description</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>

  <!--
    Field replacement template. Checks if a replacement for the field is defined to apply it, otherwise copies the field value.
  -->
  <xsl:template name="replaceField">
    <xsl:param name="fieldId"/>

    <xsl:copy>
      <xsl:copy-of select="@*"/>

      <xsl:choose>
        <!-- A replacement defined for the field, apply it -->
        <xsl:when
          test="$replacements/replacements/replacement[field = $fieldId] and string($replacements/replacements/replacement[field = $fieldId]/searchValue)">

          <xsl:choose>
            <!-- gmd:URL -->
            <xsl:when test="name() = 'gmd:URL' or name() = 'che:LocalisedURL'">
              <xsl:call-template name="replaceValueForField">
                <xsl:with-param name="fieldId" select="$fieldId"/>
                <xsl:with-param name="value" select="."/>
              </xsl:call-template>
            </xsl:when>

            <!-- Fields with gco:CharacterString -->
            <xsl:when test="name(*[1]) = 'gco:CharacterString'">
              <gco:CharacterString>
                <xsl:call-template name="replaceValueForField">
                  <xsl:with-param name="fieldId" select="$fieldId"/>
                  <xsl:with-param name="value" select="gco:CharacterString"/>
                </xsl:call-template>
              </gco:CharacterString>

              <xsl:if test="gmd:PT_FreeText">
                <xsl:for-each select="gmd:PT_FreeText">
                  <xsl:copy>
                    <xsl:copy-of select="@*"/>
                    <xsl:for-each select="gmd:textGroup">
                      <xsl:copy>
                        <xsl:copy-of select="@*"/>
                        <xsl:for-each select="gmd:LocalisedCharacterString">
                          <gmd:LocalisedCharacterString locale="{@locale}">
                            <xsl:call-template name="replaceValueForField">
                              <xsl:with-param name="fieldId" select="$fieldId"/>
                              <xsl:with-param name="value" select="."/>
                            </xsl:call-template>
                          </gmd:LocalisedCharacterString>
                        </xsl:for-each>
                      </xsl:copy>
                    </xsl:for-each>
                  </xsl:copy>

                </xsl:for-each>
              </xsl:if>
            </xsl:when>

            <!-- Other type, just copy them -->
            <xsl:otherwise>
              <xsl:apply-templates select="@*|node()"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>

        <!-- No replacement defined, just process the field to copy it -->
        <xsl:otherwise>
          <xsl:apply-templates select="@*|node()"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:copy>

  </xsl:template>

  <!--
  Template to manage about a field value replacement using the case insensitive parameter.
  -->
  <xsl:template name="replaceValueForField">
    <xsl:param name="fieldId"/>
    <xsl:param name="value"/>

    <xsl:choose>
      <xsl:when test="string($case_insensitive)">
        <xsl:call-template name="replaceCaseInsensitive">
          <xsl:with-param name="fieldId" select="$fieldId"/>
          <xsl:with-param name="currentValue" select="$value"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="replaceCaseSensitive">
          <xsl:with-param name="fieldId" select="$fieldId"/>
          <xsl:with-param name="currentValue" select="$value"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <xsl:template name="replaceCaseInsensitive">
    <xsl:param name="fieldId"/>
    <xsl:param name="currentValue"/>

    <xsl:variable name="newValue"
                  select="replace($currentValue, $replacements/replacements/replacement[field = $fieldId]/searchValue, $replacements/replacements/replacement[field = $fieldId]/replaceValue, $case_insensitive)"/>

    <!--<xsl:message>====== replaceCaseInsensitive fieldId:<xsl:value-of select="$fieldId" /></xsl:message>
    <xsl:message>====== replaceCaseInsensitive currentVal:<xsl:value-of select="$currentValue" /></xsl:message>
    <xsl:message>====== replaceCaseInsensitive newVal:<xsl:value-of select="$newValue" /></xsl:message>-->


    <xsl:if test="$currentValue != $newValue">
      <xsl:attribute name="geonet:change" select="$fieldId"/>
      <xsl:attribute name="geonet:original" select="$currentValue"/>
      <xsl:attribute name="geonet:new" select="$newValue"/>
    </xsl:if>

    <xsl:value-of select="$newValue"/>
  </xsl:template>


  <xsl:template name="replaceCaseSensitive">
    <xsl:param name="fieldId"/>
    <xsl:param name="currentValue"/>

    <xsl:variable name="newValue"
                  select="replace($currentValue, $replacements/replacements/replacement[field = $fieldId]/searchValue, $replacements/replacements/replacement[field = $fieldId]/replaceValue)"/>

    <!--<xsl:message>====== replaceCaseSensitive fieldId:<xsl:value-of select="$fieldId" /></xsl:message>
    <xsl:message>====== replaceCaseSensitive currentVal:<xsl:value-of select="$currentValue" /></xsl:message>
    <xsl:message>====== replaceCaseSensitive newVal:<xsl:value-of select="$newValue" /></xsl:message>-->

    <xsl:if test="$currentValue != $newValue">
      <xsl:attribute name="geonet:change" select="$fieldId"/>
      <xsl:attribute name="geonet:original" select="$currentValue"/>
      <xsl:attribute name="geonet:new" select="$newValue"/>
    </xsl:if>

    <xsl:value-of select="$newValue"/>
  </xsl:template>
</xsl:stylesheet>
