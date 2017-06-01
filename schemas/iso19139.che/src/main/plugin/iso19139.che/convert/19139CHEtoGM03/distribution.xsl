<xsl:stylesheet version="1.0"
                xmlns:ili="http://www.interlis.ch/INTERLIS2.3"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                exclude-result-prefixes="che gco gmd util">

    <xsl:template mode="distribution" match="gmd:distributionInfo">
        <ili:distributionInfo REF="?">
            <ili:GM03_2_1Core.Core.MD_Distribution TID="x{util:randomId()}">
                <xsl:apply-templates mode="distribution"/>
            </ili:GM03_2_1Core.Core.MD_Distribution>
        </ili:distributionInfo>
    </xsl:template>

    <xsl:template mode="distribution" match="gmd:transferOptions">
        <xsl:apply-templates mode="distribution"/>
    </xsl:template>

    <xsl:template mode="distribution" match="gmd:distributor">
    <ili:GM03_2_1Comprehensive.Comprehensive.MD_Distributiondistributor TID="x{util:randomId()}">
        <ili:distributor REF="?">
              <xsl:apply-templates mode="distribution" select="./*"/>
        </ili:distributor>
        <ili:BACK_REF name="MD_Distribution"/>
    </ili:GM03_2_1Comprehensive.Comprehensive.MD_Distributiondistributor>

    </xsl:template>

    <xsl:template mode="distribution" match="gmd:distributionOrderProcess">
    <ili:GM03_2_1Comprehensive.Comprehensive.distributionOrderProcessMD_Distributor TID="x{util:randomId()}">
          <ili:distributionOrderProcess REF="?">
            <ili:GM03_2_1Comprehensive.Comprehensive.MD_StandardOrderProcess TID="x2{generate-id(.)}">
                <xsl:apply-templates mode="text" select="gmd:MD_StandardOrderProcess/gmd:fees"/>
                <xsl:apply-templates mode="text" select="gmd:MD_StandardOrderProcess/gmd:plannedAvailableDateTime"/>
                <xsl:apply-templates mode="text" select="gmd:MD_StandardOrderProcess/gmd:turnaround"/>
                <xsl:apply-templates mode="textGroup" select="gmd:MD_StandardOrderProcess/gmd:orderingInstructions"/>
            </ili:GM03_2_1Comprehensive.Comprehensive.MD_StandardOrderProcess>
          </ili:distributionOrderProcess>
          <ili:BACK_REF name="MD_Distributor"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.distributionOrderProcessMD_Distributor>
    </xsl:template>

    <xsl:template mode="distribution" match="gmd:MD_DigitalTransferOptions">
        <xsl:param name="showBackRef" select="true()"/>
        <ili:GM03_2_1Comprehensive.Comprehensive.MD_DigitalTransferOptions TID="x{util:randomId()}">
            <xsl:if test="$showBackRef = true()">
                <ili:BACK_REF name="MD_Distribution"/>
            </xsl:if>
            <xsl:apply-templates mode="distribution" select="gmd:MD_Distribution"/>
            <xsl:apply-templates mode="text" select="gmd:unitsOfDistribution"/>
            <xsl:apply-templates mode="text" select="gmd:transferSize"/>
            <xsl:apply-templates mode="distribution" select="gmd:onLine/gmd:CI_OnlineResource"/>
            <xsl:apply-templates mode="distribution" select="gmd:offLine"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.MD_DigitalTransferOptions>
    </xsl:template>

    <xsl:template mode="distribution" match="gmd:CI_OnlineResource">
        <xsl:param name="backRef">true</xsl:param>
        <ili:GM03_2_1Core.Core.CI_OnlineResource TID="x{util:randomId()}">
            <xsl:apply-templates mode="text" select="gmd:protocol"/>
            <xsl:apply-templates mode="text" select="gmd:applicationProfile"/>
            <xsl:apply-templates mode="text" select="gmd:function"/>
            <xsl:if test="normalize-space(gmd:description)!=''">
                <xsl:apply-templates mode="textGroup" select="gmd:description"/>
            </xsl:if>

            <xsl:if test="normalize-space(gmd:name)!=''">
                <xsl:apply-templates mode="textGroup" select="gmd:name"/>
            </xsl:if>
            <xsl:apply-templates mode="text" select="gmd:linkage"/>
            <xsl:if test="$backRef = 'true'">
                <ili:BACK_REF name="MD_DigitalTransferOptions"/>
            </xsl:if>
        </ili:GM03_2_1Core.Core.CI_OnlineResource>
    </xsl:template>
    <xsl:template mode="distribution" match="gmd:offLine">
        <ili:offLine REF="?">
            <xsl:apply-templates mode="distribution"/>
        </ili:offLine>
    </xsl:template>
    <xsl:template mode="distribution" match="gmd:MD_Medium">
        <ili:GM03_2_1Comprehensive.Comprehensive.MD_Medium TID="x{util:randomId()}">
            <xsl:apply-templates mode="text" select="gmd:name"/>
            <xsl:apply-templates mode="text" select="gmd:density"/>
            <xsl:apply-templates mode="text" select="gmd:densityUnits"/>
            <xsl:apply-templates mode="text" select="gmd:volumes"/>
            <xsl:apply-templates mode="text" select="gmd:mediumFormat"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.MD_Medium>
    </xsl:template>

    <xsl:template mode="distribution" match="gmd:MD_Distribution">
        <xsl:apply-templates mode="distribution"/>
    </xsl:template>

    <xsl:template mode="distribution" match="gmd:distributionFormat">
        <ili:GM03_2_1Core.Core.MD_DistributiondistributionFormat TID="x{util:randomId()}">
            <ili:BACK_REF name="MD_Distribution"/>
            <ili:distributionFormat REF="?">
                <xsl:apply-templates mode="distribution"/>
            </ili:distributionFormat>
        </ili:GM03_2_1Core.Core.MD_DistributiondistributionFormat>
    </xsl:template>

    <xsl:template mode="distribution" match="gmd:MD_Format">
        <xsl:param name="showDistributor" select="true()"/>
        <ili:GM03_2_1Comprehensive.Comprehensive.MD_Format TID="x{util:randomId()}">
            <xsl:apply-templates mode="text" select="gmd:name"/>
            <xsl:apply-templates mode="text" select="gmd:version"/>
            <xsl:apply-templates mode="text" select="gmd:amendmentNumber"/>
            <xsl:apply-templates mode="text" select="gmd:specification"/>
            <xsl:apply-templates mode="text" select="gmd:fileDecompressionTechnique"/>
            <xsl:if test="$showDistributor = true()">
                <xsl:apply-templates mode="distribution" select="gmd:formatDistributor"/>
            </xsl:if>
        </ili:GM03_2_1Comprehensive.Comprehensive.MD_Format>
    </xsl:template>

    <xsl:template mode="distribution" match="gmd:formatDistributor">
        <ili:GM03_2_1Comprehensive.Comprehensive.formatDistributordistributorFormat TID="x{util:randomId()}">
            <ili:formatDistributor REF="?">
                <xsl:apply-templates mode="distribution"/>
            </ili:formatDistributor>
            <ili:BACK_REF name="distributorFormat"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.formatDistributordistributorFormat>
    </xsl:template>

    <xsl:template mode="distribution" match="gmd:MD_Distributor">
        <ili:GM03_2_1Comprehensive.Comprehensive.MD_Distributor TID="x{util:randomId()}">
            <xsl:apply-templates mode="distribution" select="gmd:distributorContact"/>
            <xsl:apply-templates mode="distribution" select="gmd:distributionOrderProcess"/>
            <xsl:apply-templates mode="distribution" select="gmd:distributorTransferOptions"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.MD_Distributor>
    </xsl:template>

    <xsl:template mode="distribution" match="gmd:distributorContact">
        <ili:distributorContact REF="?">
            <xsl:apply-templates mode="RespParty"/>  <!-- the node taken by the REF, what follows will stay in place -->
            <ili:GM03_2_1Comprehensive.Comprehensive.MD_DistributordistributorContact>
                <xsl:apply-templates mode="RespPartyRole"/>
            </ili:GM03_2_1Comprehensive.Comprehensive.MD_DistributordistributorContact>
        </ili:distributorContact>
    </xsl:template>

    <xsl:template mode="distribution" match="gmd:distributorTransferOptions">
        <ili:GM03_2_1Comprehensive.Comprehensive.distributorTransferOptionsMD_Distributor TID="x{util:randomId()}">
            <ili:distributorTransferOptions REF="?">
                <xsl:apply-templates mode="distribution">
                    <xsl:with-param name="showBackRef" select="false()"/>
                </xsl:apply-templates>
            </ili:distributorTransferOptions>
            <ili:BACK_REF name="MD_Distributor"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.distributorTransferOptionsMD_Distributor>
    </xsl:template>

    <xsl:template mode="distribution" match="*">
        <ili:ERROR>Unknown distribution element <xsl:value-of select="local-name(.)"/> child of <xsl:value-of select="local-name(..)"/> </ili:ERROR>
    </xsl:template>
</xsl:stylesheet>
