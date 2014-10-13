<xsl:stylesheet version="2.0"
    xmlns:gco="http://www.isotc211.org/2005/gco"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:include href="language-che_ci_responsibleparty.xsl"/>

    <xsl:template match="/">

        <Documents>
            <Document locale="eng">
                <Field name="_locale" string="eng" store="true" index="false" />
                <xsl:apply-templates mode="contact" select=".//gco:CharacterString"/>
            </Document>
        </Documents>
    </xsl:template>
</xsl:stylesheet>