<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:ili="http://www.interlis.ch/INTERLIS2.3"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                exclude-result-prefixes="che gco gmd util">

    <xsl:template mode="Legislation" match="che:CHE_MD_Legislation">
        <ili:GM03_2_1Comprehensive.Comprehensive.MD_Legislation TID='x{generate-id(.)}'>
            <xsl:apply-templates mode="enumISO" select=".">
                <xsl:with-param name="name">CodeISO.CountryCodeISO_</xsl:with-param>
                <xsl:with-param name="element">country</xsl:with-param>
            </xsl:apply-templates>
            <xsl:apply-templates mode="enumISO" select=".">
                <xsl:with-param name="name">CodeISO.LanguageCodeISO_</xsl:with-param>
                <xsl:with-param name="element">language</xsl:with-param>
                <xsl:with-param name="lowercase">1</xsl:with-param>
            </xsl:apply-templates>
            <xsl:apply-templates mode="text" select="che:legislationType"/>
            <xsl:apply-templates mode="Legislation" select="che:internalReference"/>
            <xsl:apply-templates mode="Legislation" select="che:title"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.MD_Legislation>
    </xsl:template>

    <xsl:template mode="Legislation" match="che:title">
        <ili:title REF="?">
            <ili:GM03_2_1Comprehensive.Comprehensive.CI_Citation TID="x{util:randomId()}">
                <xsl:apply-templates mode="RefSystem"/>
            </ili:GM03_2_1Comprehensive.Comprehensive.CI_Citation>
        </ili:title>
    </xsl:template>

    <xsl:template mode="Legislation" match="che:internalReference">
        <ili:internalReference>
            <xsl:for-each select="gco:CharacterString">
                <ili:GM03_2_1Core.Core.CharacterString_>
                    <ili:value><xsl:value-of select="."/></ili:value>
                </ili:GM03_2_1Core.Core.CharacterString_>
            </xsl:for-each>
        </ili:internalReference>
    </xsl:template>

    <xsl:template mode="Legislation" match="*" priority="-100">
        <ili:ERROR>Unknown Legislation element <xsl:value-of select="local-name(.)"/></ili:ERROR>
    </xsl:template>
</xsl:stylesheet>
