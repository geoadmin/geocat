<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:gmd="http://www.isotc211.org/2005/gmd"
    xmlns:gco="http://www.isotc211.org/2005/gco"
    xmlns:che="http://www.geocat.ch/2008/che"
    xmlns:xslutil="java:org.fao.geonet.util.XslUtil">

    <xsl:template mode="sharedobject" match="gmd:LocalisedCharacterString">
        <Field name="{../../../lower-case(local-name())}" string="{string(.)}" store="true" index="true" />
    </xsl:template>

    <xsl:template mode="sharedobject" match="che:LocalisedURL">
        <Field name="{../../../lower-case(local-name())}" string="{string(.)}" store="true" index="true" />
    </xsl:template>

    <xsl:template mode="sharedobject" match="gco:CharacterString">
        <Field name="{../lower-case(local-name())}" string="{string(.)}" store="true" index="true" />
    </xsl:template>

    <xsl:template mode="sharedobject" match="gmd:CI_RoleCode">
        <Field name="role" string="{string(@codeListValue)}" store="true" index="true" />
    </xsl:template>

</xsl:stylesheet>