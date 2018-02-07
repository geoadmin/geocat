<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:che="http://www.geocat.ch/2008/che"
                version="2.0">
  <xsl:import href="../../../iso19139/present/csw/gmd-brief.xsl"/>

  <!-- remove che:* elements w/o base type -->
  <xsl:template match="*[starts-with(name(.), 'che:') and not(@gco:isoType)]"/>
</xsl:stylesheet>
