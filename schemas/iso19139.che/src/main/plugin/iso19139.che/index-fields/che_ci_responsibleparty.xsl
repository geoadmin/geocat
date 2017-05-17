<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0"
>

  <xsl:include href="che_ci_responsibleparty-functions.xsl"/>

  <xsl:template match="/">
    <xsl:variable name="title">
      <xsl:call-template name="title"/>
    </xsl:variable>

    <Document locale="eng">
      <Field name="_title" string="eng {$title}" store="true" index="false"/>
      <Field name="any" store="false" index="true">
        <xsl:attribute name="string">
          <xsl:value-of select="//(gco:CharacterString|gmd:LocalisedCharacterString)"/>
        </xsl:attribute>
      </Field>
    </Document>
  </xsl:template>

</xsl:stylesheet>
