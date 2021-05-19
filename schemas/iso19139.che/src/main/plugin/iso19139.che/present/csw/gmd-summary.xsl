<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                version="2.0">
  <xsl:import href="../../../iso19139/present/csw/gmd-summary.xsl"/>

  <!-- remove references to che:* elements in xsi:type attrs -->
  <xsl:template match="*[starts-with(@xsi:type, 'che:')]">
    <xsl:element name="{name()}">
      <xsl:apply-templates select="@*[name() != 'xsi:type']|node()"/>
      <!-- <xsl:copy-of select="@*[name() != '@xsi:type']"/> -->
    </xsl:element>
  </xsl:template>

  <!-- remove che:* elements w/o base type -->
  <xsl:template match="*[starts-with(name(.), 'che:') and not(@gco:isoType)]"/>

  <!-- use iso19139 schemaLocation instead of the record one -->
  <xsl:template match="@xsi:schemaLocation">
    <xsl:attribute name="xsi:schemaLocation">http://www.isotc211.org/2005/gmd http://www.isotc211.org/2005/gmd/gmd.xsd
      http://www.isotc211.org/2005/gmx http://www.isotc211.org/2005/gmx/gmx.xsd
      http://www.isotc211.org/2005/srv http://schemas.opengis.net/iso/19139/20060504/srv/srv.xsd</xsl:attribute>
  </xsl:template>
</xsl:stylesheet>
