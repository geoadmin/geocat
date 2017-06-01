<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:ili="http://www.interlis.ch/INTERLIS2.3"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:gmi="http://www.isotc211.org/2005/gmi"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                exclude-result-prefixes="#all">

    <xsl:template mode="DataQuality" match="gmd:DQ_DataQuality">
        <ili:GM03_2_1Core.Core.DQ_DataQuality TID="x{util:randomId()}">
            <ili:BACK_REF name="MD_Metadata"/>
            <xsl:apply-templates mode="DataQuality" select="gmd:scope"/>
            <xsl:apply-templates mode="DataQuality" select="gmd:report"/>
            <xsl:apply-templates mode="DataQuality" select="gmd:lineage"/>
        </ili:GM03_2_1Core.Core.DQ_DataQuality>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:scope|gmd:lineage">
        <xsl:apply-templates mode="DataQuality"/>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:LI_Source">
        <ili:GM03_2_1Comprehensive.Comprehensive.sourceLI_Lineage TID="x{util:randomId()}">
            <ili:source REF="?">
                <ili:GM03_2_1Comprehensive.Comprehensive.LI_Source  TID="x2{generate-id(.)}">
                 <xsl:apply-templates mode="textGroup" select="gmd:description" />
                 <xsl:apply-templates mode="DataQuality" select="gmd:scaleDenominator" />
                 <xsl:apply-templates mode="DataQuality" select="gmd:sourceCitation" />
                 <xsl:apply-templates mode="DataQuality" select="gmd:sourceReferenceSystem" />


<!--             Doesn't seem to be part of the gm03 schema    -->
<!--                 <xsl:apply-templates mode="DataQuality" select="gmd:sourceExtent" />-->
                 <xsl:apply-templates mode="DataQuality" select="gmd:sourceStep" />
               </ili:GM03_2_1Comprehensive.Comprehensive.LI_Source>
            </ili:source>
            <ili:BACK_REF name="LI_Lineage" />
        </ili:GM03_2_1Comprehensive.Comprehensive.sourceLI_Lineage>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:scaleDenominator">
        <ili:scaleDenominator REF="?">
            <xsl:apply-templates mode="DataIdentification" />
        </ili:scaleDenominator>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:sourceReferenceSystem">
        <ili:sourceReferenceSystem REF="?">
            <xsl:apply-templates mode="RefSystem" />
        </ili:sourceReferenceSystem>
    </xsl:template>


    <xsl:template mode="DataQuality" match="gmd:sourceCitation">
        <ili:sourceCitation REF="?">
            <xsl:apply-templates mode="DataIdentification" />
        </ili:sourceCitation>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:sourceStep">
      <ili:GM03_2_1Comprehensive.Comprehensive.sourceStepsource>
        <ili:sourceStep REF="?">
          <xsl:apply-templates mode="DataQuality" select="gmd:LI_ProcessStep" />
        </ili:sourceStep>

        <ili:source REF="../../?" />
      </ili:GM03_2_1Comprehensive.Comprehensive.sourceStepsource>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:report">
        <xsl:variable name="report">
            <xsl:apply-templates mode="DataQuality"/>
        </xsl:variable>
        <xsl:if test="count($report/*)>0">
            <ili:GM03_2_1Comprehensive.Comprehensive.reportDQ_DataQuality TID="x{util:randomId()}">
                <ili:report REF="?">
                    <xsl:copy-of select="$report"/>
                </ili:report>
                <ili:BACK_REF name="DQ_Qualitiy" />
            </ili:GM03_2_1Comprehensive.Comprehensive.reportDQ_DataQuality>
        </xsl:if>
    </xsl:template>


    <xsl:template mode="DataQuality" match="gmd:extent">
        <ili:GM03_2_1Comprehensive.Comprehensive.DQ_Scopeextent TID="x{util:randomId()}">
          <ili:extent REF="?">
              <xsl:apply-templates mode="DataQuality" />
          </ili:extent>
            <ili:BACK_REF name="DQ_Scope"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.DQ_Scopeextent>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:EX_Extent">
        <ili:GM03_2_1Core.Core.EX_Extent TID="x{util:randomId()}">
            <xsl:apply-templates mode="textGroup" select="gmd:description"/>
            <xsl:apply-templates mode="Extent" select="gmd:geographicElement"/>
            <xsl:apply-templates mode="Extent" select="gmd:temporalElement"/>
            <xsl:apply-templates mode="Extent" select="gmd:verticalElement"/>
        </ili:GM03_2_1Core.Core.EX_Extent>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:DQ_Scope">
        <ili:GM03_2_1Core.Core.DQ_Scope TID="x{util:randomId()}">
            <xsl:apply-templates mode="text" select="gmd:level"/>
            <ili:BACK_REF name="DQ_DataQuality"/>
            <xsl:apply-templates mode="DataQuality" select="gmd:extent"/>
            <xsl:apply-templates mode="DataQuality" select="gmd:levelDescription/gmd:MD_ScopeDescription"/>
        </ili:GM03_2_1Core.Core.DQ_Scope>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:MD_ScopeDescription">
        <ili:GM03_2_1Core.Core.MD_ScopeDescription TID="x{util:randomId()}">
           <xsl:apply-templates mode="text" select="gmd:attributes"/>
           <xsl:apply-templates mode="text" select="gmd:features"/>
           <xsl:apply-templates mode="text" select="gmd:featureInstances"/>
           <xsl:apply-templates mode="text" select="gmd:attributeInstances"/>
            <xsl:apply-templates mode="text" select="gmd:dataset"/>
            <xsl:apply-templates mode="text" select="gmd:other"/>
            <ili:BACK_REF name="DQ_Scope"/>
        </ili:GM03_2_1Core.Core.MD_ScopeDescription>
    </xsl:template>


    <xsl:template mode="DataQuality" match="gmd:DQ_TemporalValidity">
        <ili:GM03_2_1Comprehensive.Comprehensive.DQ_TemporalValidity TID="x{util:randomId()}">
            <xsl:apply-templates mode="DQ_Element" select=".">
                <xsl:with-param name="backRef">DQ_TemporalValidity</xsl:with-param>
            </xsl:apply-templates>
        </ili:GM03_2_1Comprehensive.Comprehensive.DQ_TemporalValidity>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:DQ_TemporalConsistency">
        <ili:GM03_2_1Comprehensive.Comprehensive.DQ_TemporalConsistency TID="x{util:randomId()}">
            <xsl:apply-templates mode="DQ_Element" select=".">
                <xsl:with-param name="backRef">DQ_TemporalConsistency</xsl:with-param>
            </xsl:apply-templates>
        </ili:GM03_2_1Comprehensive.Comprehensive.DQ_TemporalConsistency>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:DQ_AccuracyOfATimeMeasurement">
        <ili:GM03_2_1Comprehensive.Comprehensive.DQ_AccuracyOfATimeMeasurement TID="x{util:randomId()}">
            <xsl:apply-templates mode="DQ_Element" select=".">
                <xsl:with-param name="backRef">DQ_AccuracyOfATimeMeasurement</xsl:with-param>
            </xsl:apply-templates>
        </ili:GM03_2_1Comprehensive.Comprehensive.DQ_AccuracyOfATimeMeasurement>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:DQ_QuantitativeAttributeAccuracy">
        <ili:GM03_2_1Comprehensive.Comprehensive.DQ_QuantitativeAttributeAccuracy TID="x{util:randomId()}">
            <xsl:apply-templates mode="DQ_Element" select=".">
                <xsl:with-param name="backRef">DQ_QuantitativeAttributeAccuracy</xsl:with-param>
            </xsl:apply-templates>
        </ili:GM03_2_1Comprehensive.Comprehensive.DQ_QuantitativeAttributeAccuracy>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:DQ_NonQuantitativeAttributeAccuracy">
        <ili:GM03_2_1Comprehensive.Comprehensive.DQ_NonQuantitativeAttributeAccuracy TID="x{util:randomId()}">
            <xsl:apply-templates mode="DQ_Element" select=".">
                <xsl:with-param name="backRef">DQ_NonQuantitativeAttributeAccuracy</xsl:with-param>
            </xsl:apply-templates>
        </ili:GM03_2_1Comprehensive.Comprehensive.DQ_NonQuantitativeAttributeAccuracy>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:DQ_ThematicClassificationCorrectness">
        <ili:GM03_2_1Comprehensive.Comprehensive.DQ_ThematicClassificationCorrectness TID="x{util:randomId()}">
            <xsl:apply-templates mode="DQ_Element" select=".">
                <xsl:with-param name="backRef">DQ_ThematicClassificationCorrectness</xsl:with-param>
            </xsl:apply-templates>
        </ili:GM03_2_1Comprehensive.Comprehensive.DQ_ThematicClassificationCorrectness>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:DQ_RelativeInternalPositionalAccuracy">
        <ili:GM03_2_1Comprehensive.Comprehensive.DQ_RelativeInternalPositionalAccuracy TID="x{util:randomId()}">
            <xsl:apply-templates mode="DQ_Element" select=".">
                <xsl:with-param name="backRef">DQ_RelativeInternalPositionalAccuracy</xsl:with-param>
            </xsl:apply-templates>
        </ili:GM03_2_1Comprehensive.Comprehensive.DQ_RelativeInternalPositionalAccuracy>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:DQ_GriddedDataPositionalAccuracy">
        <ili:GM03_2_1Comprehensive.Comprehensive.DQ_GriddedDataPositionalAccuracy TID="x{util:randomId()}">
            <xsl:apply-templates mode="DQ_Element" select=".">
                <xsl:with-param name="backRef">DQ_GriddedDataPositionalAccuracy</xsl:with-param>
            </xsl:apply-templates>
        </ili:GM03_2_1Comprehensive.Comprehensive.DQ_GriddedDataPositionalAccuracy>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:DQ_AbsoluteExternalPositionalAccuracy">
        <ili:GM03_2_1Comprehensive.Comprehensive.DQ_AbsoluteExternalPositionalAccuracy TID="x{util:randomId()}">
            <xsl:apply-templates mode="DQ_Element" select=".">
                <xsl:with-param name="backRef">DQ_AbsoluteExternalPositionalAccuracy</xsl:with-param>
            </xsl:apply-templates>
        </ili:GM03_2_1Comprehensive.Comprehensive.DQ_AbsoluteExternalPositionalAccuracy>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:DQ_TopologicalConsistency">
        <ili:GM03_2_1Comprehensive.Comprehensive.DQ_TopologicalConsistency TID="x{util:randomId()}">
            <xsl:apply-templates mode="DQ_Element" select=".">
                <xsl:with-param name="backRef">DQ_TopologicalConsistency</xsl:with-param>
            </xsl:apply-templates>
        </ili:GM03_2_1Comprehensive.Comprehensive.DQ_TopologicalConsistency>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:DQ_FormatConsistency">
        <ili:GM03_2_1Comprehensive.Comprehensive.DQ_FormatConsistency TID="x{util:randomId()}">
            <xsl:apply-templates mode="DQ_Element" select=".">
                <xsl:with-param name="backRef">DQ_FormatConsistency</xsl:with-param>
            </xsl:apply-templates>
        </ili:GM03_2_1Comprehensive.Comprehensive.DQ_FormatConsistency>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:DQ_DomainConsistency">
        <ili:GM03_2_1Comprehensive.Comprehensive.DQ_DomainConsistency TID="x{util:randomId()}">
            <xsl:apply-templates mode="DQ_Element" select=".">
                <xsl:with-param name="backRef">DQ_DomainConsistency</xsl:with-param>
            </xsl:apply-templates>
        </ili:GM03_2_1Comprehensive.Comprehensive.DQ_DomainConsistency>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:DQ_ConceptualConsistency">
        <ili:GM03_2_1Comprehensive.Comprehensive.DQ_ConceptualConsistency TID="x{util:randomId()}">
            <xsl:apply-templates mode="DQ_Element" select=".">
                <xsl:with-param name="backRef">DQ_ConceptualConsistency</xsl:with-param>
            </xsl:apply-templates>
        </ili:GM03_2_1Comprehensive.Comprehensive.DQ_ConceptualConsistency>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:DQ_CompletenessOmission">
        <ili:GM03_2_1Comprehensive.Comprehensive.DQ_CompletenessOmission TID="x{util:randomId()}">
            <xsl:apply-templates mode="DQ_Element" select=".">
                <xsl:with-param name="backRef">DQ_CompletenessOmission</xsl:with-param>
            </xsl:apply-templates>
        </ili:GM03_2_1Comprehensive.Comprehensive.DQ_CompletenessOmission>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:DQ_CompletenessCommission">
        <ili:GM03_2_1Comprehensive.Comprehensive.DQ_CompletenessCommission TID="x{util:randomId()}">
            <xsl:apply-templates mode="DQ_Element" select=".">
                <xsl:with-param name="backRef">DQ_CompletenessCommission</xsl:with-param>
            </xsl:apply-templates>
        </ili:GM03_2_1Comprehensive.Comprehensive.DQ_CompletenessCommission>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmi:QE_Usability">
        <!-- XXX do nothing for now gm03 does not support this report -->
    </xsl:template>


    <xsl:template mode="DQ_Element" match="*">
           <xsl:param name="backRef"/>
           <xsl:apply-templates mode="DataQuality" select="gmd:nameOfMeasure"/>
           <xsl:apply-templates mode="text" select="gmd:measureDescription"/>
           <xsl:apply-templates mode="text" select="gmd:evaluationMethodType"/>
           <xsl:apply-templates mode="text" select="gmd:evaluationMethodDescription"/>
           <xsl:if test="normalize-space(gmd:dateTime) != ''">
               <xsl:for-each select="gmd:dateTime[normalize-space(.) != '']">
                   <ili:dateTime>
                      <ili:GM03_2_1Core.Core.DateTime_>
                        <ili:value><xsl:value-of select="gmd:dateTime"/></ili:value>
                      </ili:GM03_2_1Core.Core.DateTime_>
                   </ili:dateTime>
               </xsl:for-each>
           </xsl:if>
           <xsl:apply-templates mode="DataQuality" select="gmd:measureIdentification"/>
           <xsl:apply-templates mode="DataQuality" select="gmd:result">
                <xsl:with-param name="backRef" select="$backRef"/>
           </xsl:apply-templates>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:result">
        <xsl:param name="backRef"/>

        <xsl:apply-templates mode="DataQualityResult">
            <xsl:with-param name="backRef" select="$backRef"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template mode="DataQualityResult" match="gmi:QE_CoverageResult">
        <!-- XXX Ignore for now -->
    </xsl:template>

    <xsl:template mode="DataQualityResult" match="gmd:DQ_ConformanceResult">
        <xsl:param name="backRef"/>
          <ili:GM03_2_1Comprehensive.Comprehensive.DQ_ConformanceResult TID="x{util:randomId()}">
                <ili:BACK_REF name="DQ_Element" />
                <xsl:apply-templates mode="characterString" select="gmd:explanation"/>
                <xsl:apply-templates mode="text" select="gmd:pass"/>
                <xsl:if test="gmd:specification/gmd:CI_Citation">
                    <ili:GM03_2_1Comprehensive.Comprehensive.CI_Citation TID="xCI{generate-id(.)}">
                        <xsl:apply-templates mode="RefSystem" select="gmd:specification/gmd:CI_Citation">
                            <xsl:with-param name="backRef">DQ_ConformanceResult</xsl:with-param>
                        </xsl:apply-templates>
                    </ili:GM03_2_1Comprehensive.Comprehensive.CI_Citation>
                </xsl:if>
          </ili:GM03_2_1Comprehensive.Comprehensive.DQ_ConformanceResult>
    </xsl:template>

    <xsl:template mode="DataQualityResult" match="gmd:DQ_QuantitativeResult">
        <xsl:param name="backRef"/>
          <ili:GM03_2_1Comprehensive.Comprehensive.DQ_QuantitativeResult TID="x{util:randomId()}">
                <ili:BACK_REF name="DQ_Element" />
                <xsl:if test="normalize-space(gmd:valueType/gco:RecordType) != ''">
                    <ili:valueType><xsl:value-of select="gmd:valueType/gco:RecordType"/></ili:valueType>
                </xsl:if>

                <xsl:apply-templates mode="DataQualityResult" select="gmd:valueUnit"/>
                <xsl:apply-templates mode="text" select="gmd:errorStatistic"/>
                <xsl:apply-templates mode="DataQualityResult" select="gmd:value"/>
                <xsl:apply-templates mode="RefSystem" select="gmd:specification/gmd:CI_Citation">
                    <xsl:with-param name="backRef">DQ_QuantitativeResult</xsl:with-param>
                </xsl:apply-templates>
          </ili:GM03_2_1Comprehensive.Comprehensive.DQ_QuantitativeResult>
    </xsl:template>

    <xsl:template mode="DataQualityResult" match="gmd:valueUnit">
          <ili:valueUnit>m</ili:valueUnit>
    </xsl:template>

    <xsl:template mode="DataQualityResult" match="gmd:value">
        <xsl:choose>
            <xsl:when test="gco:Record/text()">
                <ili:value>
                  <ili:GM03_2_1Comprehensive.Comprehensive.Record_>
                    <ili:value><xsl:value-of select="gco:Record"/></ili:value>
                  </ili:GM03_2_1Comprehensive.Comprehensive.Record_>
                </ili:value>
            </xsl:when>
            <xsl:when test="gco:Record/node()">
                <ili:value>
                  <ili:GM03_2_1Comprehensive.Comprehensive.Record_>
                    <ili:value>
                         <ili:XMLBLBOX>
                            <xsl:copy-of select="gco:Record/*"/>
                         </ili:XMLBLBOX>
                     </ili:value>
                  </ili:GM03_2_1Comprehensive.Comprehensive.Record_>
                </ili:value>
            </xsl:when>
            <xsl:otherwise>
            <!-- Do nothing -->
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:nameOfMeasure">
          <ili:nameOfMeasure>
             <ili:GM03_2_1Core.Core.CharacterString_>
                 <ili:value><xsl:value-of select="."/></ili:value>
             </ili:GM03_2_1Core.Core.CharacterString_>
          </ili:nameOfMeasure>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:measureIdentification">
        <xsl:if test="normalize-space(text()) != ''">
            <ili:measureIdentification REF="?">
                <xsl:apply-templates mode="RefSystem" select="gmd:RS_Identifier"/>
            </ili:measureIdentification>
       </xsl:if>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:LI_Lineage">
        <ili:GM03_2_1Core.Core.LI_Lineage TID="x{util:randomId()}">
            <xsl:apply-templates mode="textGroup" select="gmd:statement" />
            <ili:BACK_REF name="DQ_DataQuality"/>
            <xsl:apply-templates mode="DataQuality" select="gmd:processStep">
                <xsl:with-param name="backref" select="true()" />
            </xsl:apply-templates>
            <xsl:apply-templates mode="DataQuality" select="gmd:source/gmd:LI_Source"/>
        </ili:GM03_2_1Core.Core.LI_Lineage>
    </xsl:template>

     <xsl:template mode="DataQuality" match="gmd:processStep">
         <xsl:param name="backref" select="false()" />
            <xsl:apply-templates mode="DataQuality" select="gmd:LI_ProcessStep">
                <xsl:with-param name="backref" select="$backref" />
            </xsl:apply-templates>
     </xsl:template>
     <xsl:template mode="DataQuality" match="gmd:LI_ProcessStep">
       <xsl:param name="backref" select="false()" />
        <ili:GM03_2_1Comprehensive.Comprehensive.LI_ProcessStep TID="x{util:randomId()}">
            <ili:description>
              <xsl:variable name="locale" >
                <xsl:call-template name="lang3_to_lang2">
                  <xsl:with-param name="lang3" select="$defaultLanguage" />
                </xsl:call-template>
              </xsl:variable>

              <xsl:choose>
                <xsl:when test="normalize-space(gco:CharacterString[text()]) != ''">
                  <xsl:value-of select="gco:CharacterString" />
                </xsl:when>
                <xsl:when test="normalize-space(.//gmd:LocalisedCharacterString[@locale = concat('#', upper-case($locale))]) = ''">
                  <xsl:value-of select=".//gmd:LocalisedCharacterString[@locale = concat('#', upper-case($locale))]" />
                </xsl:when>
                <xsl:when test=".//gmd:LocalisedCharacterString[normalize-space(text()) != '']">
                  <xsl:value-of select="(.//gmd:LocalisedCharacterString[normalize-space(text()) != ''])[1]" />
                </xsl:when>
              </xsl:choose>
            </ili:description>
            <xsl:apply-templates mode="text" select="gmd:dateTime"/>
            <xsl:apply-templates mode="text_" select="gmd:rationale"/>
            <xsl:apply-templates mode="DataQuality" select="processor"/>

          <xsl:if test="$backref">
            <ili:BACK_REF name="LI_Lineage"/>
          </xsl:if>
        </ili:GM03_2_1Comprehensive.Comprehensive.LI_ProcessStep>
    </xsl:template>

    <xsl:template mode="DQ_Element" match="*" priority="-100">
        <ili:ERROR>Unknown DataQualityResult element <xsl:value-of select="local-name(.)"/></ili:ERROR>
    </xsl:template>

    <xsl:template mode="DataQualityResult" match="*" priority="-100">
        <ili:ERROR>Unknown DataQualityResult element <xsl:value-of select="local-name(.)"/></ili:ERROR>
    </xsl:template>

    <xsl:template mode="DataQuality" match="*" priority="-100">
        <ili:ERROR>Unknown DataQuality element <xsl:value-of select="local-name(.)"/></ili:ERROR>
    </xsl:template>
</xsl:stylesheet>

