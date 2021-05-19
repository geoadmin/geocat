<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                version="1.0">

  <xsl:template match="che:CHE_MD_Metadata">
    <dateStamp>
      <xsl:value-of select="gmd:dateStamp/*"/>
    </dateStamp>
  </xsl:template>

</xsl:stylesheet>
