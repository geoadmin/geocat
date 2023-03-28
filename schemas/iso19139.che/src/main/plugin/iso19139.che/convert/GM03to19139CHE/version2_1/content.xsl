<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:int="http://www.interlis.ch/INTERLIS2.3"
                exclude-result-prefixes="int"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:gml320="http://www.opengis.net/gml">

    <xsl:template mode="Content"
                  match="int:GM03_2_1Comprehensive.Comprehensive.MD_CoverageDescription">
        <che:CHE_MD_CoverageDescription>
            <xsl:apply-templates mode="Content" select="int:attributeDescription"/>
            <xsl:apply-templates mode="Content" select="int:contentType"/>
            <xsl:apply-templates mode="Content" select="int:GM03_2_1Comprehensive.Comprehensive.dimensionMD_CoverageDescription"/>
            <xsl:apply-templates mode="Content" select="int:filmType"/>
            <xsl:apply-templates mode="integerCHE" select="int:focalDistance"/>
        </che:CHE_MD_CoverageDescription>
    </xsl:template>

    <xsl:template mode="Content" match="int:filmType">
        <che:filmType>
            <che:CHE_MD_FilmTypeCode codeList="?" codeListValue="{.}"/>
        </che:filmType>
    </xsl:template>

    <xsl:template mode="Content" match="int:attributeDescription">
        <gmd:attributeDescription>
            <gco:RecordType>
                <xsl:value-of select="."/>
            </gco:RecordType>
        </gmd:attributeDescription>
    </xsl:template>

    <xsl:template mode="Content" match="int:contentType">
        <gmd:contentType>
            <gmd:MD_CoverageContentTypeCode
                    codeList="./resources/codeList.xml#MD_CoverageContentTypeCode"
                    codeListValue="{.}"/>
        </gmd:contentType>
    </xsl:template>

    <xsl:template mode="Content" match="int:GM03_2_1Comprehensive.Comprehensive.dimensionMD_CoverageDescription">
        <xsl:apply-templates mode="Content"/>
    </xsl:template>

    <xsl:template mode="Content" match="int:dimension">
        <gmd:dimension>
            <xsl:apply-templates mode="Content"/>
        </gmd:dimension>
    </xsl:template>

    <!-- ==================================================================================== -->

    <xsl:template mode="Content" match="int:GM03_2_1Comprehensive.Comprehensive.MD_Band">
        <gmd:MD_Band>
            <xsl:apply-templates mode="Content" select="int:sequenceIdentifier"/>
            <xsl:apply-templates mode="text" select="int:descriptor"/>
            <xsl:apply-templates mode="real" select="int:maxValue"/>
            <xsl:apply-templates mode="real" select="int:minValue"/>
            <xsl:apply-templates mode="Content" select="int:units"/>
            <xsl:apply-templates mode="real" select="int:peakResponse"/>
            <xsl:apply-templates mode="integer" select="int:bitsPerValue"/>
            <xsl:apply-templates mode="integer" select="int:toneGradation"/>
            <xsl:apply-templates mode="real" select="int:scaleFactor"/>
            <xsl:apply-templates mode="real" select="int:offset"/>
        </gmd:MD_Band>
    </xsl:template>

    <xsl:template mode="Content" match="int:sequenceIdentifier">
        <gmd:sequenceIdentifier>
            <gco:MemberName>
                <!-- TODO: what to put here? -->
                <gco:aName>
                    <gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
                </gco:aName>
                <gco:attributeType>
                    <gco:TypeName>
                        <gco:aName>
                            <gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
                        </gco:aName>
                    </gco:TypeName>
                </gco:attributeType>
            </gco:MemberName>
        </gmd:sequenceIdentifier>
    </xsl:template>

    <xsl:template mode="Content" match="int:units">
        <xsl:variable name="unit" select="text()"/>
        <gmd:units>
          <xsl:copy-of select="document('../units.xml')//(gml:dictionaryEntry|gml320:dictionaryEntry)/*[@gml:id=$unit or @gml320:id=$unit]"/>
        </gmd:units>
    </xsl:template>

    <!-- ==================================================================================== -->

    <xsl:template mode="Content" match="int:GM03_2_1Comprehensive.Comprehensive.MD_ImageDescription">
        <che:CHE_MD_ImageDescription>
        <!--<MD_ImageDescription>-->
            <xsl:apply-templates mode="Content" select="int:attributeDescription"/>
            <xsl:apply-templates mode="Content" select="int:contentType"/>
            <xsl:apply-templates mode="Content" select="int:dimension"/>

            <xsl:apply-templates mode="real" select="int:illuminationElevationAngle"/>
            <xsl:apply-templates mode="real" select="int:illuminationAzimuthAngle"/>
            <xsl:apply-templates mode="Content" select="int:imagingCondition"/>
            <xsl:apply-templates mode="Content" select="int:imageQualityCode"/>
            <xsl:apply-templates mode="real" select="int:cloudCoverPercentage"/>
            <xsl:apply-templates mode="Content" select="int:processingLevelCode"/>
            <xsl:apply-templates mode="integer" select="int:compressionGenerationQuantity"/>
            <xsl:apply-templates mode="boolean" select="int:triangulationIndicator"/>
            <xsl:apply-templates mode="boolean" select="int:radiometricCalibrationDataAvailability"/>
            <xsl:apply-templates mode="boolean" select="int:cameraCalibrationInformationAvailability"/>
            <xsl:apply-templates mode="boolean" select="int:filmDistortionInformationAvailability"/>
            <xsl:apply-templates mode="boolean" select="int:lensDistortionInformationAvailability"/>

            <xsl:apply-templates mode="Content" select="int:filmType"/>
            <xsl:apply-templates mode="integerCHE" select="int:focalDistance"/>
        <!--</MD_ImageDescription>-->
        </che:CHE_MD_ImageDescription>

    </xsl:template>

    <xsl:template mode="Content" match="int:imagingCondition">
        <gmd:imagingCondition>
            <gmd:MD_ImagingConditionCode codeList="./resources/codeList.xml#MD_ImagingConditionCode" codeListValue="{./@value|.}" />
        </gmd:imagingCondition>
    </xsl:template>

    <xsl:template mode="Content" match="int:imageQualityCode|int:processingLevelCode">
        <xsl:element name="{local-name(.)}"
                     namespace="http://www.isotc211.org/2005/gmd">
            <xsl:apply-templates mode="Identifier"/>
        </xsl:element>
    </xsl:template>

    <!-- ==================================================================================== -->

    <xsl:template mode="Content" match="int:GM03_2_1Comprehensive.Comprehensive.MD_RangeDimension">
        <gmd:MD_RangeDimension>
            <xsl:apply-templates mode="Content" select="int:sequenceIdentifier"/>
            <xsl:apply-templates mode="text" select="int:descriptor"/>
        </gmd:MD_RangeDimension>
    </xsl:template>

    <!-- ==================================================================================== -->

    <xsl:template mode="Content"
                  match="int:GM03_2_1Comprehensive.Comprehensive.MD_FeatureCatalogueDescription">
        <che:CHE_MD_FeatureCatalogueDescription gco:isoType="gmd:MD_FeatureCatalogueDescription">
            <xsl:for-each select="int:complianceCode">
                <gmd:complianceCode>
                    <xsl:apply-templates mode="boolean" select="text()"/>
                </gmd:complianceCode>
            </xsl:for-each>

            <xsl:for-each select="int:language">
                <xsl:apply-templates mode="language" select="."/>
            </xsl:for-each>

            <xsl:for-each select="int:includedWithDataset">
                <gmd:includedWithDataset>
                    <xsl:apply-templates mode="boolean" select="text()"/>
                </gmd:includedWithDataset>
            </xsl:for-each>

            <xsl:for-each select="int:featureTypes">
                <xsl:for-each select="int:GM03_2_1Comprehensive.Comprehensive.GenericName_/int:value">
                    <gmd:featureTypes>
                        <gco:LocalName><xsl:value-of select="."/></gco:LocalName>
                    </gmd:featureTypes>
                </xsl:for-each>
            </xsl:for-each>

            <xsl:for-each select="int:GM03_2_1Comprehensive.Comprehensive.CI_Citation">
                <gmd:featureCatalogueCitation>
                    <xsl:apply-templates mode="Citation" select="."/>
                </gmd:featureCatalogueCitation>
            </xsl:for-each>
            <xsl:if test="not(int:GM03_2_1Comprehensive.Comprehensive.CI_Citation)">
                <gmd:featureCatalogueCitation/>
            </xsl:if>

            <xsl:for-each select="int:dataModel">
                <che:dataModel xsi:type="che:PT_FreeURL_PropertyType">
                    <xsl:choose>
                        <xsl:when test="int:GM03_2_1Core.Core.PT_FreeURL">
                            <xsl:apply-templates mode="language" select="int:GM03_2_1Core.Core.PT_FreeURL"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <gmd:URL><xsl:value-of select="."/></gmd:URL>
                        </xsl:otherwise>
                    </xsl:choose>
                </che:dataModel>
            </xsl:for-each>
            <xsl:apply-templates mode="Content" select="int:GM03_2_1Comprehensive.Comprehensive.classMD_FeatureCatalogueDescription"/>
            <xsl:apply-templates mode="Content" select="int:GM03_2_1Comprehensive.Comprehensive.domainMD_FeatureCatalogueDescription"/>
            <xsl:choose>
              <xsl:when test="int:modelType">
                 <xsl:apply-templates mode="Content" select="int:modelType"/>
              </xsl:when>
              <xsl:otherwise>
              <che:modelType>
                  <che:CHE_MD_modelTypeCode codeListValue="FeatureDescription" codeList="#che:CHE_MD_modelTypeCode" />
              </che:modelType>
            </xsl:otherwise>
          </xsl:choose>
            <xsl:apply-templates mode="Content" select="int:GM03_2_1_2Comprehensive.Comprehensive.CI_Citation"/>
        </che:CHE_MD_FeatureCatalogueDescription>
    </xsl:template>

    <xsl:template mode="Content" match="int:GM03_2_1Comprehensive.Comprehensive.domainMD_FeatureCatalogueDescription">
        <xsl:apply-templates mode="Content"/>
    </xsl:template>

    <xsl:template mode="Content" match="int:domain">
        <che:domain>
            <xsl:apply-templates mode="Content"/>
        </che:domain>
    </xsl:template>

    <xsl:template mode="Content" match="int:modelType">
        <che:modelType>
            <che:CHE_MD_modelTypeCode codeListValue="{.}" codeList="#che:CHE_MD_modelTypeCode" />
        </che:modelType>
    </xsl:template>

    <xsl:template mode="Content" match="int:GM03_2_1Comprehensive.Comprehensive.MD_CodeDomain">
        <che:CHE_MD_CodeDomain>
            <xsl:apply-templates mode="textCHE" select="int:name"/>
            <xsl:apply-templates mode="textCHE" select="int:description"/>
            <xsl:apply-templates mode="Content" select="int:type"/>
            <xsl:apply-templates mode="Content" select="int:subDomain"/>
            <xsl:apply-templates mode="Content" select="int:baseDomain"/>
        </che:CHE_MD_CodeDomain>
    </xsl:template>

    <xsl:template mode="Content" match="int:subDomain|int:baseDomain">
        <xsl:element name="che:{local-name()}">
            <xsl:apply-templates mode="Content"/>
        </xsl:element>
    </xsl:template>

    <xsl:template mode="Content" match="int:type">
        <che:type>
            <xsl:apply-templates mode="Content"/>
        </che:type>
    </xsl:template>

    <xsl:template mode="Content" match="int:GM03_2_1Comprehensive.Comprehensive.MD_Type">
        <!--<che:CHE_MD_Type>-->
            <xsl:apply-templates mode="textCHE" select="int:type"/>
            <xsl:for-each select="int:GM03_2_1Comprehensive.Comprehensive.MD_CodeValue">
                <che:value>
                    <xsl:apply-templates mode="Content" select="."/>
                </che:value>
            </xsl:for-each>
        <!--</che:CHE_MD_Type>-->
    </xsl:template>

    <xsl:template mode="Content" match="int:GM03_2_1Comprehensive.Comprehensive.MD_CodeValue">
        <che:CHE_MD_CodeValue>
            <xsl:apply-templates mode="textCHE" select="int:name"/>
            <xsl:apply-templates mode="textCHE" select="int:code"/>
            <xsl:apply-templates mode="textCHE" select="int:description"/>
            <xsl:for-each select="int:GM03_2_1Comprehensive.Comprehensive.MD_CodeValue">
                <che:subValue>
                    <xsl:apply-templates mode="Content" select="."/>
                </che:subValue>
            </xsl:for-each>
        </che:CHE_MD_CodeValue>
    </xsl:template>

    <xsl:template mode="Content" match="int:GM03_2_1Comprehensive.Comprehensive.classMD_FeatureCatalogueDescription">
        <xsl:apply-templates mode="Content"/>
    </xsl:template>

    <xsl:template mode="Content" match="int:class">
        <che:class>
            <xsl:apply-templates mode="Content"/>
        </che:class>
    </xsl:template>

    <xsl:template mode="Content" match="int:GM03_2_1Comprehensive.Comprehensive.MD_Class">
        <che:CHE_MD_Class>
            <xsl:apply-templates mode="textCHE" select="int:name"/>
            <che:description>
                <xsl:apply-templates mode="language" select="int:description/*"/>
            </che:description>
            <xsl:apply-templates mode="Content" select="int:GM03_2_1Comprehensive.Comprehensive.MD_Attribute"/>
            <xsl:apply-templates mode="Content" select=".//baseClass"/>   <!-- TODO -->
            <xsl:apply-templates mode="Content" select=".//subClass"/>   <!-- TODO -->
        </che:CHE_MD_Class>
    </xsl:template>

    <xsl:template mode="Content" match="int:GM03_2_1Comprehensive.Comprehensive.MD_Attribute">
        <che:attribute>
            <xsl:apply-templates mode="textCHE" select="int:name"/>
            <xsl:apply-templates mode="textCHE" select="int:description"/>
            <che:namedType>
                <xsl:apply-templates mode="Content" select="int:GM03_2_1Comprehensive.Comprehensive.MD_AttributenamedType"/>
            </che:namedType>
            <xsl:apply-templates mode="Content" select="int:anonymousType"/>
        </che:attribute>
    </xsl:template>

    <xsl:template mode="Content" match="int:GM03_2_1Comprehensive.Comprehensive.MD_AttributenamedType">
            <xsl:apply-templates mode="Content" select="int:namedType/int:GM03_2_1Comprehensive.Comprehensive.MD_CodeDomain"/>
    </xsl:template>

    <xsl:template mode="Content" match="int:anonymousType">
        <che:anonymousType>
            <che:CHE_MD_Type>
                <xsl:apply-templates mode="textCHE" select="int:GM03_2_1Comprehensive.Comprehensive.MD_Type/int:type"/>
                <che:value>
                    <xsl:apply-templates mode="Content" select="int:GM03_2_1Comprehensive.Comprehensive.MD_Type/int:GM03_2_1Comprehensive.Comprehensive.MD_CodeValue"/>
                </che:value>
            </che:CHE_MD_Type>
        </che:anonymousType>
    </xsl:template>

    <xsl:template mode="Content" match="text()">
        <xsl:call-template name="UnMatchedText">
            <xsl:with-param name="mode">Content</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
</xsl:stylesheet>
