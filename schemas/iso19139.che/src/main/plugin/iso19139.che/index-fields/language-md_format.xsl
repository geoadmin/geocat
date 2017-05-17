<xsl:stylesheet version="2.0"
    xmlns:gco="http://www.isotc211.org/2005/gco"
    xmlns:gmd="http://www.isotc211.org/2005/gmd"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:include href="shared-object-util.xsl"/>

    <xsl:template match="/">

        <Documents>
            <Document locale="eng">
                <Field name="_locale" string="eng" store="true" index="false" />
                <Field name="_title" string="{.//gmd:name/gco:CharacterString} ({.//gmd:version/gco:CharacterString})" store="true" index="false" />
                <xsl:apply-templates mode="sharedobject" select=".//gco:CharacterString"/>
            </Document>
        </Documents>
    </xsl:template>
</xsl:stylesheet>