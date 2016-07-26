<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="text" indent="no" media-type="text/csv"></xsl:output>

  <xsl:template match="/">
    <xsl:text>"owner";"Title";"Identifier";"UUID";"geodata type";"Point of contact";"maintenance &amp; update frequency";"duration of conservation";"comment on duration of conservation";;;;;;"comment on archival value";"appraisal of archival value";"reason for archiving value";;;;;;;;;;;;</xsl:text>
    <xsl:apply-templates select="/root/records"/>
  </xsl:template>
  <xsl:template match="record">
"<xsl:value-of select="uuid"/>";"<xsl:value-of select="entity"/>";"<xsl:value-of select="validated"/>";"<xsl:value-of select="published"/>";"<xsl:value-of select="changedate"/>";"<xsl:value-of select="changetime"/>";"<xsl:value-of select="failurerule"/>";"<xsl:value-of select="replace(failurereasons, '&quot;', '`')"/>"</xsl:template>

</xsl:stylesheet>
