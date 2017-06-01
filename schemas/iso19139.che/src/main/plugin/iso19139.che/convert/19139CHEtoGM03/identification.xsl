<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:ili="http://www.interlis.ch/INTERLIS2.3"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                exclude-result-prefixes="che gco gmd srv util gmx">

    <xsl:template mode="DataIdentification" match="gmd:identificationInfo">
        <xsl:apply-templates mode="DataIdentification"/>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="che:CHE_MD_DataIdentification|gmd:MD_DataIdentification">
        <ili:GM03_2_1Comprehensive.Comprehensive.MD_DataIdentification TID="x{util:randomId()}">
            <xsl:call-template name="dataIdentification"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.MD_DataIdentification>
    </xsl:template>

    <xsl:template name="dataIdentification">
            <xsl:apply-templates mode="enum" select="gmd:status"/>
            <xsl:choose>
              <xsl:when test="abstract/node()">
                <xsl:apply-templates mode="text" select="gmd:abstract"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:apply-templates mode="textGroup" select="gmd:abstract"/>
              </xsl:otherwise>
            </xsl:choose>
            <xsl:apply-templates mode="text" select="gmd:purpose"/>
            <ili:BACK_REF name="MD_Metadata"/>
            <xsl:apply-templates mode="DataIdentification" select="gmd:citation"/>
            <xsl:apply-templates mode="enum" select="gmd:spatialRepresentationType"/>
            <xsl:if test="gmd:language">
                <ili:language>
                    <xsl:apply-templates mode="DataIdentification" select="gmd:language"/>
                </ili:language>
            </xsl:if>
            <xsl:apply-templates mode="enum" select="gmd:characterSet"/>
            <xsl:variable name="tc">
                    <xsl:apply-templates mode="DataIdentification" select="gmd:topicCategory"/>
            </xsl:variable>
            <xsl:if test="gmd:topicCategory and count($tc/*) &gt; 0">
                <ili:topicCategory>
                  <xsl:copy-of select="$tc"/>
                </ili:topicCategory>
            </xsl:if>
            <xsl:apply-templates mode="DataIdentification" select="che:projectType"/>
            <xsl:apply-templates mode="DataIdentification" select="gmd:credit"/>
            <xsl:apply-templates mode="text" select="che:basicGeodataID"/>
            <xsl:apply-templates mode="DataIdentification" select="che:basicGeodataIDType"/>

            <xsl:if test="normalize-space(gmd:environmentDescription)!=''">
                <xsl:apply-templates mode="textGroup" select="gmd:environmentDescription"/>
            </xsl:if>
            <xsl:if test="normalize-space(gmd:supplementalInformation)!=''">
                <xsl:apply-templates mode="textGroup" select="gmd:supplementalInformation"/>
            </xsl:if>

            <xsl:apply-templates mode="DataIdentification" select="gmd:pointOfContact"/>
            <xsl:apply-templates mode="DataIdentification" select="gmd:resourceMaintenance"/>
            <xsl:apply-templates mode="DataIdentification" select="gmd:graphicOverview"/>
            <xsl:apply-templates mode="DataIdentification" select="gmd:resourceFormat"/>
            <xsl:apply-templates mode="DataIdentification" select="gmd:descriptiveKeywords"/>
            <xsl:apply-templates mode="DataIdentification" select="gmd:resourceSpecificUsage"/>
            <xsl:apply-templates mode="DataIdentification" select="gmd:resourceConstraints|srv:restrictions"/>
            <xsl:apply-templates mode="DataIdentification" select="gmd:aggregationInfo"/>
            <xsl:apply-templates mode="DataIdentification" select="gmd:spatialResolution/*"/>
            <xsl:apply-templates mode="Extent" select="gmd:extent"/>
            <xsl:apply-templates mode="DataIdentification" select="gmd:graphicOverview"/>
            <xsl:apply-templates mode="DataIdentification" select="srv:extent"/>
            <xsl:apply-templates mode="DataIdentification" select="che:revision"/>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:aggregationInfo">
        <ili:GM03_2_1Comprehensive.Comprehensive.aggregationInfo_MD_Identification TID="x{util:randomId()}">
            <xsl:apply-templates mode="DataIdentification" select="gmd:MD_AggregateInformation"/>
            <ili:BACK_REF name="MD_Identification"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.aggregationInfo_MD_Identification>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:MD_AggregateInformation">
        <ili:aggregationInfo REF="?">
            <ili:GM03_2_1Comprehensive.Comprehensive.MD_AggregateInformation TID="x{util:randomId()}">
            <xsl:apply-templates mode="text" select="gmd:associationType"/>
            <xsl:apply-templates mode="text" select="gmd:initiativeType"/>
            <xsl:apply-templates mode="DataIdentification" select="gmd:aggregateDataSetIdentifier"/>
            <xsl:apply-templates mode="DataIdentification" select="gmd:aggregateDataSetName/CI_Citation"/>
            </ili:GM03_2_1Comprehensive.Comprehensive.MD_AggregateInformation>
        </ili:aggregationInfo>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:aggregateDataSetIdentifier">
            <ili:aggregateDataSetIdentifier REF="?">
                <xsl:apply-templates mode="DataIdentification"/>
            </ili:aggregateDataSetIdentifier>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:resourceSpecificUsage">
        <ili:GM03_2_1Comprehensive.Comprehensive.MD_Usage TID="x{util:randomId()}">
            <xsl:apply-templates mode="text" select="gmd:MD_Usage/gmd:usageDateTime"/>
            <xsl:apply-templates mode="text" select="gmd:MD_Usage/gmd:userDeterminedLimitations"/>
            <xsl:apply-templates mode="text" select="gmd:MD_Usage/gmd:specificUsage"/>
            <xsl:apply-templates mode="DataIdentification" select="gmd:MD_Usage/gmd:userContactInfo"/>
            <ili:BACK_REF name="MD_Identification"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.MD_Usage>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:userContactInfo">
        <ili:GM03_2_1Comprehensive.Comprehensive.MD_UsageuserContactInfo TID="x{util:randomId()}">
            <xsl:apply-templates mode="DataIdentification" select="che:CHE_CI_ResponsibleParty"/>
            <ili:BACK_REF name="MD_Usage"/>
            <xsl:apply-templates mode="enum" select="che:CHE_CI_ResponsibleParty/gmd:role"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.MD_UsageuserContactInfo>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="che:CHE_CI_ResponsibleParty">
        <ili:userContactInfo REF="?">
            <xsl:apply-templates mode="RespParty" select="."/>
        </ili:userContactInfo>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:graphicOverview">
        <ili:GM03_2_1Comprehensive.Comprehensive.MD_BrowseGraphic TID="x{util:randomId()}">
            <xsl:apply-templates mode="text" select="gmd:MD_BrowseGraphic/gmd:fileName"/>
            <xsl:apply-templates mode="text" select="gmd:MD_BrowseGraphic/gmd:fileType"/>
            <xsl:apply-templates mode="text" select="gmd:MD_BrowseGraphic/gmd:fileDescription"/>
            <ili:BACK_REF name="MD_Identification"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.MD_BrowseGraphic>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="che:projectType">
        <ili:ProjectType><xsl:value-of select="che:CHE_CI_projectTypeCode/@codeListValue"/></ili:ProjectType>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:credit">
        <xsl:variable name="credit"><xsl:apply-templates mode="text" select="."/></xsl:variable>
        <ili:credit>
        <ili:GM03_2_1Core.Core.CharacterString_>
            <ili:value><xsl:value-of select="$credit"/></ili:value>
        </ili:GM03_2_1Core.Core.CharacterString_>
        </ili:credit>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="che:revision">
        <ili:GM03_2_1Comprehensive.Comprehensive.revisionMD_Identification TID="x{util:randomId()}">
            <ili:revision REF="?">
                <xsl:apply-templates mode="DataIdentification"/>
            </ili:revision>
            <ili:BACK_REF name="MD_Identification"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.revisionMD_Identification>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="che:CHE_MD_Revision">
        <ili:GM03_2_1Comprehensive.Comprehensive.MD_Revision TID="x{util:randomId()}">
            <xsl:apply-templates mode="enum" select="che:revisionScope"/>
            <xsl:apply-templates mode="text" select="che:dateOfLastUpdate"/>
            <xsl:apply-templates mode="text" select="che:revisionNote"/>
            <xsl:apply-templates mode="DataIdentification" select="che:revisionExtent"/>

            <xsl:apply-templates mode="DataIdentification" select="che:revisionScopeDescription"/>
            <xsl:apply-templates mode="DataIdentification" select="che:revisionContact"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.MD_Revision>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:resourceConstraints|srv:restrictions">
        <ili:GM03_2_1Comprehensive.Comprehensive.resourceConstraintsMD_Identification TID="x{util:randomId()}">
            <ili:resourceConstraints REF="?">
                <xsl:apply-templates mode="DataIdentification"/>
            </ili:resourceConstraints>
            <ili:BACK_REF name="MD_Identification"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.resourceConstraintsMD_Identification>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:MD_SecurityConstraints">
        <ili:GM03_2_1Comprehensive.Comprehensive.MD_SecurityConstraints TID="x{util:randomId()}">
            <xsl:apply-templates mode="groupText" select=".">
                <xsl:with-param name="element">useLimitation</xsl:with-param>
            </xsl:apply-templates>
            <xsl:apply-templates mode="DataIdentification" select="gmd:classification"/>
            <xsl:apply-templates mode="text" select="gmd:classificationSystem"/>
            <xsl:apply-templates mode="textGroup" select="gmd:userNote"/>
            <xsl:apply-templates mode="text" select="gmd:handlingDescription"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.MD_SecurityConstraints>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:classification">
        <ili:classification>
            <xsl:value-of select="gmd:MD_ClassificationCode/@codeListValue"/>
        </ili:classification>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="che:CHE_MD_LegalConstraints|gmd:MD_LegalConstraints">
        <ili:GM03_2_1Comprehensive.Comprehensive.MD_LegalConstraints TID="x{util:randomId()}">
            <xsl:apply-templates mode="groupText" select=".">
                <xsl:with-param name="element">useLimitation</xsl:with-param>
            </xsl:apply-templates><!--
            <xsl:apply-templates mode="text" select="gmd:accessConstraints"/>-->
            <xsl:apply-templates mode="groupEnumC" select=".">
                <xsl:with-param name="element">accessConstraints</xsl:with-param>
            </xsl:apply-templates>
            <xsl:apply-templates mode="groupEnumC" select=".">
                <xsl:with-param name="element">useConstraints</xsl:with-param>
            </xsl:apply-templates>
            <xsl:apply-templates mode="groupText" select=".">
                <xsl:with-param name="element">otherConstraints</xsl:with-param>
            </xsl:apply-templates>
            <xsl:apply-templates mode="DataIdentification" select="che:legislationConstraints"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.MD_LegalConstraints>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="che:legislationConstraints">
        <ili:GM03_2_1Comprehensive.Comprehensive.legislationConstraintsMD_LegalConstraints>
            <ili:legislationConstraints REF="?">
                <xsl:apply-templates mode="Legislation" />
            </ili:legislationConstraints>

            <ili:BACK_REF name="MD_LegalConstraints"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.legislationConstraintsMD_LegalConstraints>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:MD_Constraints">
        <ili:GM03_2_1Comprehensive.Comprehensive.MD_Constraints TID="x{util:randomId()}">
            <xsl:apply-templates mode="groupText" select=".">
                <xsl:with-param name="element">useLimitation</xsl:with-param>
            </xsl:apply-templates>
        </ili:GM03_2_1Comprehensive.Comprehensive.MD_Constraints>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:MD_Constraints">
        <ili:GM03_2_1Comprehensive.Comprehensive.MD_Constraints TID="x{util:randomId()}">
            <xsl:apply-templates mode="groupText" select=".">
                <xsl:with-param name="element">useLimitation</xsl:with-param>
            </xsl:apply-templates>
        </ili:GM03_2_1Comprehensive.Comprehensive.MD_Constraints>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:resourceMaintenance">
        <xsl:apply-templates mode="MaintenanceInfo">
            <xsl:with-param name="backRef">MD_Identification</xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:MD_Resolution">
        <ili:GM03_2_1Core.Core.MD_Resolution TID="x{util:randomId()}">
            <xsl:apply-templates mode="text" select="gmd:distance"/>
            <ili:BACK_REF name="MD_DataIdentification"/>
            <xsl:apply-templates mode="DataIdentification" select="gmd:equivalentScale"/>
        </ili:GM03_2_1Core.Core.MD_Resolution>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:equivalentScale">
        <ili:equivalentScale REF="?">
            <xsl:apply-templates mode="DataIdentification" select="*"/>
        </ili:equivalentScale>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:MD_RepresentativeFraction">
        <ili:GM03_2_1Core.Core.MD_RepresentativeFraction TID="x{util:randomId()}">
            <xsl:apply-templates mode="text" select="gmd:denominator"/>
        </ili:GM03_2_1Core.Core.MD_RepresentativeFraction>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:descriptiveKeywords">
        <ili:GM03_2_1Core.Core.descriptiveKeywordsMD_Identification TID="x{util:randomId()}">
            <ili:descriptiveKeywords REF='?'>
                <xsl:apply-templates mode="DataIdentification" select="gmd:MD_Keywords"/>
            </ili:descriptiveKeywords>
            <ili:BACK_REF name="MD_Identification"/>
        </ili:GM03_2_1Core.Core.descriptiveKeywordsMD_Identification>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:MD_Keywords">
        <ili:GM03_2_1Core.Core.MD_Keywords TID="x{util:randomId()}">
            <!--<xsl:apply-templates mode="text" select="gmd:type"/>-->
            <xsl:choose>
                <xsl:when test="count(gmd:keyword/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[normalize-space(.) != '']) = 0">
                    <ili:keyword>
                        <ili:GM03_2_1Core.Core.PT_FreeText>
                            <ili:textGroup>
                                <ili:GM03_2_1Core.Core.PT_Group>
                                    <ili:language><xsl:value-of select="$defaultLanguage"/></ili:language>
                                    <ili:plainText><xsl:value-of select="gmd:keyword/gco:CharacterString"/></ili:plainText>
                                </ili:GM03_2_1Core.Core.PT_Group>
                            </ili:textGroup>
                        </ili:GM03_2_1Core.Core.PT_FreeText>
                    </ili:keyword>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates mode="groupText" select=".">
                        <xsl:with-param name="element">keyword</xsl:with-param>
                    </xsl:apply-templates>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:apply-templates mode="DataIdentification" select="gmd:thesaurusName"/>
        </ili:GM03_2_1Core.Core.MD_Keywords>
    </xsl:template>
    <xsl:template mode="DataIdentification" match="gmd:thesaurusName">
      <ili:thesaurus REF='?'>
          <ili:GM03_2_1Core.Core.MD_Thesaurus TID="x{util:randomId()}">
              <ili:citation REF="?">
                <xsl:apply-templates mode="DataIdentification" select="gmd:CI_Citation"/>
              </ili:citation>
          </ili:GM03_2_1Core.Core.MD_Thesaurus>
        </ili:thesaurus>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:resourceFormat">
        <ili:GM03_2_1Comprehensive.Comprehensive.resourceFormatMD_Identification TID='x{util:randomId()}'>
            <ili:resourceFormat REF='?'>
                <xsl:apply-templates mode="DataIdentification"/>
            </ili:resourceFormat>
            <ili:BACK_REF name="MD_Identification"/>
      </ili:GM03_2_1Comprehensive.Comprehensive.resourceFormatMD_Identification>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:MD_Format">
        <ili:GM03_2_1Comprehensive.Comprehensive.MD_Format TID='x{util:randomId()}'>
            <xsl:apply-templates mode="text" select="gmd:name"/>
            <xsl:apply-templates mode="text" select="gmd:version"/>
            <xsl:apply-templates mode="text" select="gmd:amendmentNumber"/>
            <xsl:apply-templates mode="text" select="gmd:specification"/>
            <xsl:apply-templates mode="text" select="gmd:fileDecompressionTechnique"/>
            <xsl:apply-templates mode="DataIdentification" select="gmd:formatDistributor"/>
      </ili:GM03_2_1Comprehensive.Comprehensive.MD_Format>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:pointOfContact">
        <ili:GM03_2_1Core.Core.MD_IdentificationpointOfContact TID="x{util:randomId()}">
            <ili:pointOfContact REF="?">
                <xsl:apply-templates mode="RespParty"/>
            </ili:pointOfContact>
            <ili:BACK_REF name="MD_Identification"/>
            <xsl:apply-templates mode="RespPartyRole" select="che:CHE_CI_ResponsibleParty|gmd:CI_ResponsibleParty"/>
        </ili:GM03_2_1Core.Core.MD_IdentificationpointOfContact>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:topicCategory">
    <xsl:choose>
      <xsl:when test="
          normalize-space(.) = 'imageryBaseMapsEarthCover' or
          normalize-space(.) = 'planningCadastre' or
          normalize-space(.) = 'geoscientificInformation' or
          normalize-space(.) = 'utilitiesCommunication' or
          normalize-space(.) = 'environment'">

                    <!-- We don't want these in GM03 -->

          </xsl:when>
      <xsl:when test="not(contains(normalize-space(.), '_'))">
                <ili:GM03_2_1Core.Core.MD_TopicCategoryCode_>
                    <ili:value><xsl:value-of select="normalize-space(.)"/></ili:value>
                </ili:GM03_2_1Core.Core.MD_TopicCategoryCode_>
          </xsl:when>
        <xsl:otherwise>
            <ili:GM03_2_1Core.Core.MD_TopicCategoryCode_>
              <ili:value><xsl:value-of select="concat(substring-before(normalize-space(.), '_'),'.',normalize-space(.))"/></ili:value>
            </ili:GM03_2_1Core.Core.MD_TopicCategoryCode_>
        </xsl:otherwise>
    </xsl:choose>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:language">
        <xsl:variable name="code">
                <xsl:call-template name="lang3_to_lang2">
                    <xsl:with-param name="lang3" select="gco:CharacterString"/>
                </xsl:call-template>
        </xsl:variable>
        <ili:CodeISO.LanguageCodeISO_>
            <ili:value>
                <xsl:choose>
                    <xsl:when test="normalize-space($code) != ''">
                        <xsl:value-of select="$code"></xsl:value-of>
                    </xsl:when>
                    <xsl:when test="/che:CHE_MD_Metadata/gmd:language/gco:CharacterString">
                        <xsl:call-template name="lang3_to_lang2">
                            <xsl:with-param name="lang3" select="/che:CHE_MD_Metadata/gmd:language/gco:CharacterString"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="/gmd:MD_Metadata/gmd:language/gco:CharacterString">
                        <xsl:call-template name="lang3_to_lang2">
                            <xsl:with-param name="lang3" select="/gmd:MD_Metadata/gmd:language/gco:CharacterString"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <ili:text>en</ili:text>
                    </xsl:otherwise>
                </xsl:choose>
            </ili:value>
        </ili:CodeISO.LanguageCodeISO_>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:citation">
        <ili:citation REF="?">
            <xsl:apply-templates mode="DataIdentification" />
        </ili:citation>
    </xsl:template>


    <xsl:template mode="DataIdentification" match="gmd:CI_Citation">
        <ili:GM03_2_1Comprehensive.Comprehensive.CI_Citation TID="x{util:randomId()}">
            <xsl:apply-templates mode="RefSystem" select=".">
                <xsl:with-param name="showIdentifier" select="false()"/>
            </xsl:apply-templates>
            <xsl:apply-templates mode="DataIdentification" select="gmd:identifier"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.CI_Citation>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:identifier">
        <ili:GM03_2_1Comprehensive.Comprehensive.CI_Citationidentifier TID="x{util:randomId()}">
            <ili:identifier REF='?'>
                <xsl:choose>
                <xsl:when test="gmd:RS_Identifier">
                    <xsl:apply-templates mode="RefSystem" select="gmd:RS_Identifier"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates mode="DataIdentification" select="gmd:MD_Identifier"/>
                </xsl:otherwise>
                </xsl:choose>
            </ili:identifier>
            <ili:BACK_REF name="CI_Citation"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.CI_Citationidentifier>
    </xsl:template>


    <xsl:template mode="DataIdentification" match="gmd:MD_Identifier">
        <ili:GM03_2_1Core.Core.MD_Identifier TID="x{util:randomId()}">
            <xsl:choose>
                <xsl:when test="gmd:code/gmx:Anchor">
                    <ili:code>
                        <ili:GM03_2_1Core.Core.PT_FreeText>
                            <ili:textGroup>
                                <ili:GM03_2_1Core.Core.PT_Group>
                                    <ili:language><xsl:value-of select="$defaultLanguage"/></ili:language>
                                    <ili:plainText><xsl:value-of select="gmd:code/gmx:Anchor"/></ili:plainText>
                                </ili:GM03_2_1Core.Core.PT_Group>
                            </ili:textGroup>
                        </ili:GM03_2_1Core.Core.PT_FreeText>
                    </ili:code>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates mode="textGroup" select="gmd:code"/>
                </xsl:otherwise>
            </xsl:choose>
        </ili:GM03_2_1Core.Core.MD_Identifier>
    </xsl:template>


    <xsl:template mode="DataIdentification" match="che:CHE_SV_ServiceIdentification | srv:SV_ServiceIdentification">
        <ili:GM03_2_1Comprehensive.Comprehensive.SV_ServiceIdentification TID="x{util:randomId()}">
            <xsl:call-template name="dataIdentification" />
            <xsl:apply-templates mode="DataIdentification" select="srv:credit"/>
            <xsl:apply-templates mode="DataIdentification" select="srv:inspireServiceType"/>
            <xsl:apply-templates mode="text" select="srv:serviceTypeVersion"/>
            <xsl:apply-templates mode="DataIdentification" select="srv:couplingType"/>
            <xsl:apply-templates mode="DataIdentification" select="srv:operatesOn"/>
            <xsl:if test="srv:coupledResource">
                <ili:coupledResource>
                <xsl:for-each select="srv:coupledResource">
                   <xsl:apply-templates mode="DataIdentification"/>
                </xsl:for-each>
              </ili:coupledResource>
            </xsl:if>
            <xsl:apply-templates mode="DataIdentification" select="srv:serviceType"/>
            <xsl:apply-templates mode="DataIdentification" select="srv:containsOperations"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.SV_ServiceIdentification>
    </xsl:template>
    <xsl:template mode="DataIdentification" match="srv:couplingType">
            <ili:couplingType><xsl:value-of select="srv:SV_CouplingType/@codeListValue"/></ili:couplingType>
    </xsl:template>
    <xsl:template mode="DataIdentification" match="srv:operatesOn">
        <ili:operatesOn>
          <ili:GM03_2_1Core.Core.CharacterString_>
            <ili:value><xsl:value-of select="./@uuidref"/></ili:value>
          </ili:GM03_2_1Core.Core.CharacterString_>
        </ili:operatesOn>
    </xsl:template>
    <xsl:template mode="DataIdentification" match="srv:serviceType">
        <ili:serviceType>
            <ili:GM03_2_1Comprehensive.Comprehensive.gml_CodeType>
                <ili:code><xsl:value-of select="gco:LocalName"/></ili:code>
                <xsl:apply-templates mode="text" select="codeSpace"/>
            </ili:GM03_2_1Comprehensive.Comprehensive.gml_CodeType>
        </ili:serviceType>
    </xsl:template>
    <xsl:template mode="DataIdentification" match="srv:SV_CoupledResource">
        <ili:GM03_2_1Comprehensive.Comprehensive.SV_CoupledResource>
            <xsl:apply-templates mode="text" select="srv:identifier"/>
            <xsl:apply-templates mode="text" select="srv:operationName"/>
            <xsl:apply-templates mode="text" select="gco:ScopedName"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.SV_CoupledResource>
    </xsl:template>
    <xsl:template mode="DataIdentification" match="srv:containsOperations">
        <ili:GM03_2_1Comprehensive.Comprehensive.containsOperationsSV_ServiceIdentification TID="x{util:randomId()}">
            <ili:containsOperations REF="?">
                <xsl:apply-templates mode="DataIdentification"/>
            </ili:containsOperations>
            <ili:BACK_REF name="SV_ServiceIdentification"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.containsOperationsSV_ServiceIdentification>
    </xsl:template>
    <xsl:template mode="DataIdentification" match="srv:SV_OperationMetadata">
        <ili:GM03_2_1Comprehensive.Comprehensive.SV_OperationMetadata TID="x{util:randomId()}">
              <xsl:apply-templates mode="text" select="srv:operationName"/>
              <xsl:apply-templates mode="DataIdentification" select="srv:DCP"/>
              <xsl:apply-templates mode="text" select="srv:invocationName"/>
                <xsl:apply-templates mode="textGroup" select="srv:operationDescription"/>
                <xsl:apply-templates mode="DataIdentification" select="srv:connectPoint"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.SV_OperationMetadata>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="srv:connectPoint">
        <ili:GM03_2_1Comprehensive.Comprehensive.SV_OperationMetadataconnectPoint TID="x{util:randomId()}">
            <ili:BACK_REF name="SV_OperationMetadata"/>
            <ili:connectPoint REF="?">
                <xsl:apply-templates mode="distribution"><xsl:with-param name="backRef">false</xsl:with-param></xsl:apply-templates>
            </ili:connectPoint>
        </ili:GM03_2_1Comprehensive.Comprehensive.SV_OperationMetadataconnectPoint>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="srv:DCP">
    <ili:DCP>
        <ili:GM03_2_1Comprehensive.Comprehensive.DCPList_>
          <xsl:for-each select="*">
              <ili:value><xsl:value-of select="@codeListValue"/></ili:value>
          </xsl:for-each>
        </ili:GM03_2_1Comprehensive.Comprehensive.DCPList_>
    </ili:DCP>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="srv:extent">
        <ili:GM03_2_1Comprehensive.Comprehensive.extentSV_ServiceIdentification TID="x{util:randomId()}">
        <ili:extent REF="?">
            <xsl:apply-templates mode="DataIdentification" select="gmd:EX_Extent"/>
        </ili:extent>
            <ili:BACK_REF name="SV_ServiceIdentification"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.extentSV_ServiceIdentification>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="gmd:EX_Extent">
        <ili:GM03_2_1Core.Core.EX_Extent TID="x{util:randomId()}">
            <xsl:apply-templates mode="textGroup" select="gmd:description"/>
            <xsl:apply-templates mode="Extent" select="gmd:geographicElement"/>
            <xsl:apply-templates mode="Extent" select="gmd:temporalElement"/>
            <xsl:apply-templates mode="Extent" select="gmd:verticalElement"/>
        </ili:GM03_2_1Core.Core.EX_Extent>
    </xsl:template>


    <xsl:template mode="DataIdentification" match="che:basicGeodataIDType">
        <ili:basicGeodataIDType><xsl:value-of select="che:basicGeodataIDTypeCode/@codeListValue"/></ili:basicGeodataIDType>
    </xsl:template>

    <xsl:template mode="DataIdentification" match="*" priority="-100">
        <ili:ERROR>Unknown DataIdentification element <xsl:value-of select="local-name(.)"/></ili:ERROR>
    </xsl:template>
</xsl:stylesheet>
