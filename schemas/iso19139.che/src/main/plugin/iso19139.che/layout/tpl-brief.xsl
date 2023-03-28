<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0">

  <xsl:include href="utility-tpl.xsl"/>
  <xsl:template name="iso19139.cheBrief">
    <metadata>
      <xsl:call-template name="iso19139-brief"/>
    </metadata>
  </xsl:template>

  <xsl:template name="iso19139.che-brief">
    <xsl:call-template name="iso19139-brief"/>
  </xsl:template>
</xsl:stylesheet>