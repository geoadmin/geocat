<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                exclude-result-prefixes="#all">


  <xsl:template name="get-iso19139.che-language">
    <xsl:call-template name="get-iso19139-language"/>
  </xsl:template>

  <xsl:template name="get-iso19139.che-other-languages">
    <xsl:call-template name="get-iso19139-other-languages"/>
  </xsl:template>

  <xsl:template name="get-iso19139.che-other-languages-as-json">
    <xsl:call-template name="get-iso19139-other-languages-as-json"/>
  </xsl:template>

</xsl:stylesheet>
