<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:include href="che_ci_responsibleparty-functions.xsl"/>

    <xsl:template match="/">
        <xsl:variable name="title">
            <xsl:call-template name="title"/>
        </xsl:variable>

        <Document locale="eng">
            <Field name="_title" string="eng {$title}" store="true" index="false" />
        </Document>
    </xsl:template>

</xsl:stylesheet>