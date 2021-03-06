<?xml version="1.0" encoding="UTF-8"?>

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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:geonet="http://www.fao.org/geonetwork"
                exclude-result-prefixes="#all"
                version="2.0">

  <xsl:param name="displayInfo"/>

  <xsl:template match="@*|node()[name(.)!='geonet:info']">
    <xsl:variable name="info" select="geonet:info"/>
    <xsl:copy>
      <xsl:apply-templates select="@*|node()[name(.)!='geonet:info']"/>
      <xsl:if test="$displayInfo = 'true'">
        <xsl:copy-of select="$info"/>
      </xsl:if>
    </xsl:copy>
  </xsl:template>

  <!-- Avoid insertion of schema location in the CSW
  response - which is invalid. -->
  <xsl:template match="@xsi:schemaLocation"/>
</xsl:stylesheet>
