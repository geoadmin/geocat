<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gmd="http://www.isotc211.org/2005/gmd"
                version="1.0">

  <xsl:template match="gmd:MD_Metadata">
    <type>
      <xsl:value-of select="gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue"/>
    </type>
  </xsl:template>
</xsl:stylesheet>
