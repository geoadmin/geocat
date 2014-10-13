<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:gmd="http://www.isotc211.org/2005/gmd"
    xmlns:gco="http://www.isotc211.org/2005/gco"
    xmlns:che="http://www.geocat.ch/2008/che"
    xmlns:xslutil="java:org.fao.geonet.util.XslUtil">


    <xsl:template match="/">

        <Documents>
            <xsl:variable name="root" select="/"/>
            <xsl:for-each select="distinct-values(//gmd:LocalisedCharacterString/@locale)">
                <xsl:variable name="locale" select="string(.)" />
                <xsl:variable name="langId" select="xslutil:threeCharLangCode(substring($locale,2,2))" />
                <Document locale="{$langId}">
                    <Field name="_locale" string="{$langId}" store="true" index="false" />
                    <xsl:apply-templates mode="contact" select="
                        $root//*[normalize-space(@locale) = normalize-space($locale) and not(ancestor::che:parentResponsibleParty)] |
                        $root//gco:CharacterString[not(ancestor::che:parentResponsibleParty)] |
                        $root//gmd:CI_RoleCode[not(ancestor::che:parentResponsibleParty)]
                    "/>
                </Document>
            </xsl:for-each>
        </Documents>
    </xsl:template>


    <xsl:template mode="contact" match="gmd:LocalisedCharacterString">
        <Field name="{../../../lower-case(local-name())}" string="{string(.)}" store="true" index="true" />
    </xsl:template>

    <xsl:template mode="contact" match="che:LocalisedURL">
        <Field name="{../../../lower-case(local-name())}" string="{string(.)}" store="true" index="true" />
    </xsl:template>

    <xsl:template mode="contact" match="gco:CharacterString">
        <Field name="{../lower-case(local-name())}" string="{string(.)}" store="true" index="true" />
    </xsl:template>

    <xsl:template mode="contact" match="gmd:CI_RoleCode">
        <Field name="role" string="{string(@codeListValue)}" store="true" index="true" />
    </xsl:template>

</xsl:stylesheet>