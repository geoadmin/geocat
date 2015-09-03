<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  exclude-result-prefixes="#all">

  <xsl:output encoding="UTF-8" method="html"/>

  <!-- Use the link parameter to display a custom hyperlink instead of 
  a default GeoNetwork Jeeves service URL. -->
  <xsl:template match="/">
    Indexed: <xsl:value-of select="/root/results/@numberIndexed" />
  </xsl:template>
</xsl:stylesheet>
