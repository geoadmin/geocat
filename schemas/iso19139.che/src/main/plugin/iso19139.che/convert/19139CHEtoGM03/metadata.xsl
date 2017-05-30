<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:ili="http://www.interlis.ch/INTERLIS2.3"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                exclude-result-prefixes="che gco gmd util">

    <xsl:template mode="metadata" match="che:CHE_MD_Metadata|gmd:MD_Metadata">
        <ili:GM03_2_1Core.Core.MD_Metadata TID="x{util:randomId()}">
            <xsl:apply-templates mode="text" select="gmd:fileIdentifier"/>
            <xsl:apply-templates mode="metadata" select="gmd:language"/>
            <xsl:apply-templates mode="text" select="gmd:characterSet"/>
            <xsl:apply-templates mode="text" select="gmd:dateStamp"/>
            <xsl:apply-templates mode="text" select="gmd:metadataStandardName"/>
            <xsl:apply-templates mode="text" select="gmd:metadataStandardVersion"/>
            <xsl:if test="gmd:hierarchyLevel">
                <ili:hierarchyLevel>
                    <xsl:for-each select="gmd:hierarchyLevel/gmd:MD_ScopeCode">
                        <xsl:element name="GM03_2_1Core.Core.MD_ScopeCode_">
                            <ili:value><xsl:value-of select="@codeListValue"/></ili:value>
                        </xsl:element>
                    </xsl:for-each>
                </ili:hierarchyLevel>
            </xsl:if>
            <xsl:if test="gmd:hierarchyLevelName and normalize-space(gmd:hierarchyLevelName/gco:CharacterString) != ''">
                <ili:hierarchyLevelName>
                    <xsl:apply-templates mode="metadata" select="gmd:hierarchyLevelName"/>
                </ili:hierarchyLevelName>
            </xsl:if>
            <xsl:apply-templates mode="text" select="gmd:dataSetURI"/>
            <xsl:apply-templates mode="distribution" select="gmd:distributionInfo"/>
            <xsl:apply-templates mode="metadata" select="gmd:contact"/>
            <xsl:apply-templates mode="metadata" select="gmd:parentIdentifier"/>
            <!--not figuring in GM03: <xsl:apply-templates mode="metadata" select="gmd:locale"/>-->
            <xsl:apply-templates mode="metadata" select="gmd:spatialRepresentationInfo"/>
            <xsl:apply-templates mode="RefSystem" select="gmd:referenceSystemInfo"/>
            <xsl:apply-templates mode="metadata" select="gmd:metadataExtensionInfo"/>
            <xsl:apply-templates mode="DataIdentification" select="gmd:identificationInfo"/>
            <xsl:apply-templates mode="metadata" select="gmd:contentInfo"/>
            <xsl:apply-templates mode="metadata" select="gmd:dataQualityInfo"/>
            <xsl:apply-templates mode="metadata" select="gmd:portrayalCatalogueInfo"/>
            <!-- <xsl:apply-templates mode="metadata" select="gmd:metadataConstraints"/> -->
            <xsl:apply-templates mode="metadata" select="gmd:applicationSchemaInfo"/>
            <xsl:apply-templates mode="metadata" select="gmd:metadataMaintenance"/>
            <xsl:apply-templates mode="metadata" select="gmd:series"/>
            <xsl:apply-templates mode="metadata" select="gmd:describes"/>
            <xsl:apply-templates mode="metadata" select="gmd:propertyType"/>
            <xsl:apply-templates mode="metadata" select="gmd:featureType"/>
            <xsl:apply-templates mode="metadata" select="gmd:featureAttribute"/>
            <xsl:apply-templates mode="metadata" select="che:legislationInformation"/>
        </ili:GM03_2_1Core.Core.MD_Metadata>
    </xsl:template>

    <xsl:template mode="metadata" match="gmd:parentIdentifier">
        <!-- TODO -->
    </xsl:template>

    <xsl:template mode="metadata" match="che:legislationInformation">
        <ili:GM03_2_1Comprehensive.Comprehensive.MD_MetadatalegislationInformation TID='x{generate-id(.)}'>
            <ili:BACK_REF name="MD_Metadata"/>
            <ili:legislationInformation REF="?">
                <xsl:apply-templates mode="Legislation"/>
            </ili:legislationInformation>
        </ili:GM03_2_1Comprehensive.Comprehensive.MD_MetadatalegislationInformation>
    </xsl:template>

    <xsl:template mode="metadata" match="gmd:metadataMaintenance|che:CHE_MD_MaintenanceInformation">
        <xsl:apply-templates mode="MaintenanceInfo">
            <xsl:with-param name="backRef">MD_Metadata</xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template mode="metadata" match="gmd:contentInfo">
        <xsl:apply-templates mode="Content"/>
    </xsl:template>

    <xsl:template mode="metadata" match="gmd:spatialRepresentationInfo">
        <xsl:apply-templates mode="SpatialRepr"/>
    </xsl:template>

    <xsl:template mode="metadata" match="gmd:portrayalCatalogueInfo">
        <ili:GM03_2_1Comprehensive.Comprehensive.portrayalCatalogueInfoMD_Metadata TID="x{util:randomId()}">
            <ili:portrayalCatalogueInfo REF="?">
                <xsl:apply-templates mode="metadata"/>
            </ili:portrayalCatalogueInfo>
            <ili:BACK_REF name="MD_Metadata"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.portrayalCatalogueInfoMD_Metadata>
    </xsl:template>

    <xsl:template mode="metadata" match="gmd:MD_PortrayalCatalogueReference|che:CHE_MD_PortrayalCatalogueReference">
        <ili:GM03_2_1Comprehensive.Comprehensive.MD_PortrayalCatalogueReference TID="x{util:randomId()}">
            <xsl:apply-templates mode="metadata" select="gmd:portrayalCatalogueCitation"/>
            <xsl:apply-templates mode="text" select="che:portrayalCatalogueURL"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.MD_PortrayalCatalogueReference>
    </xsl:template>

    <xsl:template mode="metadata" match="gmd:portrayalCatalogueCitation">
        <xsl:apply-templates mode="metadata"/>
    </xsl:template>

    <xsl:template mode="metadata" match="gmd:CI_Citation">
        <ili:GM03_2_1Comprehensive.Comprehensive.CI_Citation TID="x{util:randomId()}">
            <xsl:apply-templates mode="RefSystem" select="."/>
            <ili:BACK_REF name="MD_PortrayalCatalogReference"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.CI_Citation>
    </xsl:template>

    <xsl:template mode="metadata" match="gmd:dataQualityInfo">
        <xsl:apply-templates mode="DataQuality"/>
    </xsl:template>

   <xsl:template mode="metadata" match="gmd:contact">
        <ili:GM03_2_1Core.Core.MD_Metadatacontact>
            <ili:contact REF="?">
                <xsl:apply-templates mode="RespParty"/>
            </ili:contact>
            <ili:BACK_REF name="MD_Metadata"/>
            <xsl:apply-templates mode="RespPartyRole" select="che:CHE_CI_ResponsibleParty|gmd:CI_ResponsibleParty"/>
        </ili:GM03_2_1Core.Core.MD_Metadatacontact>
    </xsl:template>

    <xsl:template mode="metadata" match="gmd:language">
        <ili:language><xsl:value-of select="$defaultLanguage"/></ili:language>
    </xsl:template>

    <xsl:template mode="metadata" match="gmd:hierarchyLevelName">
        <xsl:for-each select="gco:CharacterString">
            <ili:GM03_2_1Core.Core.CharacterString_>
                <ili:value><xsl:value-of select="."/></ili:value>
            </ili:GM03_2_1Core.Core.CharacterString_>
        </xsl:for-each>
    </xsl:template>

    <xsl:template mode="metadata" match="*" priority="-100">
        <ili:ERROR>Unknown metadata element <xsl:value-of select="local-name(.)"/></ili:ERROR>
    </xsl:template>
</xsl:stylesheet>
