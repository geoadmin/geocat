<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:che="http://www.geocat.ch/2008/che"
                version="1.0"
>

  <xsl:template match="che:CHE_MD_Metadata|gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']">
    <uuid>
      <xsl:value-of select="gmd:fileIdentifier/gco:CharacterString"/>
    </uuid>
  </xsl:template>

</xsl:stylesheet>
