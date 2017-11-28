<?xml version="1.0" encoding="UTF-8" ?>
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

<xsl:stylesheet xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:che="http://www.geocat.ch/2008/che"
                version="2.0"
>

  <xsl:include href="../../iso19139/index-fields/index-subtemplate.xsl" />

  <!--Contacts & Organisations CHE-->
  <xsl:template mode="index" priority="1000"
                match="gmd:CI_ResponsibleParty[count(ancestor::node()) =  1]|*[@gco:isoType='gmd:CI_ResponsibleParty'][count(ancestor::node()) = 1]">

    <xsl:param name="isoLangId"/>
    <xsl:param name="langId"/>
    <xsl:param name="locale"/>

    <xsl:variable name="firstName" select="normalize-space((.//che:individualFirstName)[1])" />
    <xsl:variable name="lastName" select="normalize-space((.//che:individualLastName)[1])" />
    <Field name="firstName" string="{$firstName}" store="true" index="true"/>
    <Field name="lastName" string="{$lastName}" store="true" index="true"/>

    <xsl:for-each
            select="gmd:contactInfo/*/gmd:address/*/gmd:electronicMailAddress/gco:CharacterString">
      <Field name="email" string="{.}" store="true" index="true"/>
    </xsl:for-each>
    <xsl:for-each
            select="gmd:contactInfo/*/gmd:address/*/gmd:electronicMailAddress/gco:CharacterString">
      <Field name="_email" string="{.}" store="true" index="true"/>
    </xsl:for-each>


    <xsl:choose>
      <xsl:when test="$isMultilingual">
        <xsl:variable name="org">
          <xsl:choose>
            <xsl:when test="normalize-space(gmd:organisationName/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale = $locale]) != ''">
              <xsl:copy-of select="normalize-space(gmd:organisationName/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale = $locale])"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:copy-of select="normalize-space((gmd:organisationName/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[./text()!=''])[1])"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="name"
                      select="normalize-space(gmd:individualName/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale = $locale])"/>
        <Field name="individualName" string="{$name}" store="true" index="true"/>
        <Field name="_orgName" string="{$org}" store="true" index="true"/>
        <Field name="orgNameTree" string="{$org}" store="true" index="true"/>
        <xsl:variable name="basicTitle" select="concat($firstName, ' ', $lastName, ' ', $org)" />
        <Field name="_title" string="{$basicTitle}" store="true" index="false" />

      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="org"
                      select="normalize-space(gmd:organisationName/gco:CharacterString)"/>
        <xsl:variable name="name"
                      select="normalize-space(gmd:individualName/gco:CharacterString)"/>
        <Field name="individualName" string="{$name}" store="true" index="true"/>
        <Field name="_orgName" string="{$org}" store="true" index="true"/>
        <Field name="orgNameTree" string="{$org}" store="true" index="true"/>
        <xsl:variable name="basicTitle" select="concat($firstName, ' ', $lastName, ' ', $org)" />
        <Field name="_title" string="{$basicTitle}" store="true" index="false" />
      </xsl:otherwise>
    </xsl:choose>


    <xsl:call-template name="subtemplate-common-fields"/>

  </xsl:template>


</xsl:stylesheet>
