<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="text" indent="no" media-type="text/csv"></xsl:output>

  <xsl:template match="/">
    <xsl:text>"owner";"Title";"Identifier";"UUID";"geodata type";"Point of contact";"topic category";"maintenance &amp; update frequency";"duration of conservation";"comment on duration of conservation";"";"";"";"";"";"comment on archival value";"appraisal of archival value";"reason for archiving value";"";"";"";"";"";"";"";"";"";"";"";""</xsl:text>
    <xsl:apply-templates select="/root/records"/>
  </xsl:template>
  <xsl:template match="record">
"<xsl:value-of select="owner"/>";"<xsl:value-of select="title"/>";"<xsl:value-of select="identifier"/>";"<xsl:value-of select="uuid"/>";"<xsl:value-of select="geodatatype"/>";"<xsl:value-of select="specialistAuthority"/>";"<xsl:value-of select="topicCategory"/>";"<xsl:value-of select="updateFrequency"/>";"<xsl:value-of select="durationOfConservation"/>";"<xsl:value-of select="commentOnDuration"/>";"";"";"";"";"";"<xsl:value-of select="commentOnArchival"/>";"<xsl:value-of select="appraisalOfArchival"/>";"<xsl:value-of select="reasonForArchiving"/>";"";"";"";"";"";"";"";"";"";"";""</xsl:template>

</xsl:stylesheet>
