<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:ili="http://www.interlis.ch/INTERLIS2.3"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                exclude-result-prefixes="che gco gmd util">

    <xsl:template mode="RespParty" match="che:CHE_CI_ResponsibleParty|gmd:CI_ResponsibleParty">
        <ili:GM03_2_1Core.Core.CI_ResponsibleParty TID="x{util:randomId()}">
            <xsl:apply-templates mode="text" select="che:individualFirstName"/>
            <xsl:apply-templates mode="text" select="che:individualLastName"/>
            <xsl:if test="gmd:contactInfo/gmd:CI_Contact/gmd:address/*/gmd:electronicMailAddress and
                          normalize-space(gmd:contactInfo/gmd:CI_Contact/gmd:address/*/gmd:electronicMailAddress) != ''">
                <ili:electronicalMailAddress>
                    <xsl:apply-templates mode="RespParty" select="gmd:contactInfo/gmd:CI_Contact/gmd:address/*/gmd:electronicMailAddress"/>
                </ili:electronicalMailAddress>
            </xsl:if>
            <xsl:apply-templates mode="text" select="gmd:organisationName"/>
            <xsl:apply-templates mode="text" select="gmd:positionName"/>
            <xsl:apply-templates mode="text" select="che:organisationAcronym"/>
            <xsl:apply-templates mode="RespParty" select="gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource"/>
            <xsl:apply-templates mode="RespParty" select="gmd:contactInfo/gmd:CI_Contact/gmd:address"/>
            <xsl:apply-templates mode="RespParty" select="gmd:contactInfo"/>

            <xsl:apply-templates mode="RespParty" select="che:parentResponsibleParty"/>

            <xsl:apply-templates mode="RespParty" select="gmd:contactInfo/gmd:CI_Contact/gmd:phone/che:CHE_CI_Telephone"/>
        </ili:GM03_2_1Core.Core.CI_ResponsibleParty>
    </xsl:template>

    <xsl:template mode="RespParty" match="che:parentResponsibleParty">
        <ili:GM03_2_1Core.Core.CI_ResponsiblePartyparentinfo TID="x{util:randomId()}">
            <ili:parentResponsibleParty REF="?">
                <xsl:apply-templates mode="RespParty"/>
            </ili:parentResponsibleParty>
            <ili:BACK_REF name="CI_ResponsibleParty"/>
        </ili:GM03_2_1Core.Core.CI_ResponsiblePartyparentinfo>
    </xsl:template>

    <xsl:template mode="RespParty" match="gmd:electronicMailAddress">
        <ili:GM03_2_1Core.Core.URL_>
            <ili:value><xsl:value-of select="gco:CharacterString/text()"/></ili:value>
        </ili:GM03_2_1Core.Core.URL_>
    </xsl:template>

    <xsl:template mode="RespParty" match="gmd:onlineResource">
        <xsl:apply-templates mode="text" select="gmd:CI_OnlineResource/gmd:linkage"/>
    </xsl:template>

    <xsl:template mode="RespParty" match="che:CHE_CI_Telephone">
        <xsl:for-each select="gmd:voice[normalize-space(.) != '']">
            <ili:GM03_2_1Core.Core.CI_Telephone TID="x{util:randomId()}">
                <ili:number><xsl:value-of select="gco:CharacterString"/></ili:number>
                <ili:numberType>mainNumber</ili:numberType>
                <ili:BACK_REF name="CI_ResponsibleParty"/>
            </ili:GM03_2_1Core.Core.CI_Telephone>
        </xsl:for-each>
        <xsl:for-each select="gmd:facsimile[normalize-space(.) != '']">
            <ili:GM03_2_1Core.Core.CI_Telephone TID="x{util:randomId()}">
                <ili:number><xsl:value-of select="gco:CharacterString"/></ili:number>
                <ili:numberType>facsimile</ili:numberType>
                <ili:BACK_REF name="CI_ResponsibleParty"/>
            </ili:GM03_2_1Core.Core.CI_Telephone>
        </xsl:for-each>
        <xsl:for-each select="che:directNumber[normalize-space(.) != '']">
            <ili:GM03_2_1Core.Core.CI_Telephone TID="x{util:randomId()}">
                <ili:number><xsl:value-of select="gco:CharacterString"/></ili:number>
                <ili:numberType>directNumber</ili:numberType>
                <ili:BACK_REF name="CI_ResponsibleParty"/>
            </ili:GM03_2_1Core.Core.CI_Telephone>
        </xsl:for-each>
        <xsl:for-each select="che:mobile[normalize-space(.) != '']">
            <ili:GM03_2_1Core.Core.CI_Telephone TID="x{util:randomId()}">
                <ili:number><xsl:value-of select="gco:CharacterString"/></ili:number>
                <ili:numberType>mobile</ili:numberType>
                <ili:BACK_REF name="CI_ResponsibleParty"/>
            </ili:GM03_2_1Core.Core.CI_Telephone>
        </xsl:for-each>
    </xsl:template>

    <xsl:template mode="RespParty" match="gmd:address">
        <ili:address REF="?">
            <xsl:apply-templates mode="RespParty"/>
        </ili:address>
    </xsl:template>

    <xsl:template mode="RespParty" match="che:CHE_CI_Address|gmd:CI_Address">
        <ili:GM03_2_1Core.Core.CI_Address TID="x{util:randomId()}">
            <xsl:apply-templates mode="text" select="che:streetName"/>
            <xsl:apply-templates mode="text" select="che:streetNumber"/>
            <xsl:apply-templates mode="text" select="che:addressLine"/>
            <xsl:apply-templates mode="text" select="che:postBox"/>
            <xsl:apply-templates mode="text" select="gmd:postalCode"/>
            <xsl:apply-templates mode="text" select="gmd:city"/>
            <xsl:apply-templates mode="text" select="gmd:administrativeArea"/>
            <xsl:apply-templates mode="RespParty" select="gmd:country"/>
        </ili:GM03_2_1Core.Core.CI_Address>
    </xsl:template>

    <xsl:template mode="RespParty" match="gmd:country">
        <xsl:choose>
        <xsl:when test="@codeListValue">
            <ili:country><xsl:value-of select="@codeListValue"/></ili:country>
        </xsl:when>
        <xsl:when test="normalize-space(.) != ''">
            <ili:country><xsl:value-of select="."/></ili:country>
        </xsl:when>
        </xsl:choose>
    </xsl:template>
    <xsl:template mode="RespParty" match="gmd:contactInfo">
        <ili:contactInfo REF="?">
            <xsl:apply-templates mode="RespParty"/>
        </ili:contactInfo>
    </xsl:template>

    <xsl:template mode="RespParty" match="gmd:CI_Contact">
        <ili:GM03_2_1Core.Core.CI_Contact TID="x{util:randomId()}">
            <xsl:apply-templates mode="text" select="gmd:hoursOfService"/>
            <xsl:apply-templates mode="textGroup" select="gmd:contactInstructions"/>
        </ili:GM03_2_1Core.Core.CI_Contact>
    </xsl:template>

    <xsl:template mode="RespParty" match="*" priority="-100">
        <ili:ERROR>Unknown RespParty element <xsl:value-of select="local-name(.)"/></ili:ERROR>
    </xsl:template>

    <xsl:template mode="RespPartyRole" match="che:CHE_CI_ResponsibleParty|gmd:CI_ResponsibleParty">
        <xsl:apply-templates mode="enum" select="gmd:role"/>
    </xsl:template>

    <xsl:template mode="RespPartyRole" match="*" priority="-100">
        <ili:ERROR>Unknown RespPartyRole element <xsl:value-of select="local-name(.)"/></ili:ERROR>
    </xsl:template>
</xsl:stylesheet>
