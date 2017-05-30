<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:ili="http://www.interlis.ch/INTERLIS2.3"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                exclude-result-prefixes="che gco gmd gml util">

    <xsl:template mode="Content" match="gmd:MD_CoverageDescription|che:CHE_MD_CoverageDescription">
        <ili:GM03_2_1Comprehensive.Comprehensive.MD_CoverageDescription TID="x{util:randomId()}">
            <ili:BACK_REF name="MD_Metadata"/>
            <xsl:apply-templates mode="Content" select="gmd:attributeDescription"/>
            <xsl:apply-templates mode="text" select="gmd:contentType"/>
            <xsl:apply-templates mode="Content" select="gmd:dimension"/>
            <xsl:apply-templates mode="text" select="che:filmType"/>
            <xsl:apply-templates mode="text" select="che:focalDistance"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.MD_CoverageDescription>
    </xsl:template>

    <xsl:template mode="Content" match="che:CHE_MD_FeatureCatalogueDescription|gmd:MD_FeatureCatalogueDescription">
      <xsl:variable name="lcletters">abcdefghijklmnopqrstuvwxyz</xsl:variable>
      <xsl:variable name="ucletters">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>
        <ili:GM03_2_1Comprehensive.Comprehensive.MD_FeatureCatalogueDescription TID="x{util:randomId()}">
            <ili:BACK_REF name="MD_Metadata"/>
            <xsl:if test="gmd:language">
                <ili:language>
                    <xsl:for-each select="gmd:language/gco:CharacterString">
                        <ili:CodeISO.LanguageCodeISO_>
                            <ili:value>
                                    <xsl:call-template name="lang3_to_lang2">
                                    <xsl:with-param name="lang3" select="translate(.,$ucletters,$lcletters)"/>
                                </xsl:call-template>
                            </ili:value>
                        </ili:CodeISO.LanguageCodeISO_>
                    </xsl:for-each>
                </ili:language>
            </xsl:if>
            <xsl:apply-templates mode="text" select="gmd:includedWithDataset"/>
            <xsl:apply-templates mode="text" select="gmd:complianceCode"/>
            <xsl:if test="gmd:featureTypes">
                <ili:featureTypes>
                    <xsl:for-each select="gmd:featureTypes/*">
                        <ili:GM03_2_1Comprehensive.Comprehensive.GenericName_>
                            <ili:value>
                                <xsl:value-of select="."/>
                            </ili:value>
                        </ili:GM03_2_1Comprehensive.Comprehensive.GenericName_>
                    </xsl:for-each>
                </ili:featureTypes>
            </xsl:if>
            <xsl:choose>
              <xsl:when test="lower-case(che:modelType/che:CHE_MD_modelTypeCode/@codeListValue) = 'other'">
                  <ili:modelType>other</ili:modelType>
              </xsl:when>
              <xsl:when test="che:modelType">
                  <ili:modelType><xsl:value-of select="che:modelType/che:CHE_MD_modelTypeCode/@codeListValue"/></ili:modelType>
              </xsl:when>
              <xsl:otherwise>
                  <ili:modelType>other</ili:modelType>
              </xsl:otherwise>
            </xsl:choose>
            <xsl:apply-templates mode="text" select="che:dataModel"/>
            <xsl:apply-templates mode="Content" select="che:class"/>
            <xsl:apply-templates mode="Content" select="che:domain"/>
            <xsl:apply-templates mode="text" select="che:portrayalCatalogueURL"/>
            <xsl:apply-templates mode="Content" select="gmd:featureCatalogueCitation"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.MD_FeatureCatalogueDescription>
    </xsl:template>


    <xsl:template mode="Content" match="gmd:dimension">
        <ili:GM03_2_1Comprehensive.Comprehensive.dimensionMD_CoverageDescription TID="x{util:randomId()}">
            <ili:dimension REF="?">
                <xsl:apply-templates mode="Content"/>
            </ili:dimension>
            <ili:BACK_REF name="MD_CoverageDescription"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.dimensionMD_CoverageDescription>
    </xsl:template>


    <xsl:template mode="Content" match="che:class">
        <ili:GM03_2_1Comprehensive.Comprehensive.classMD_FeatureCatalogueDescription TID="x{util:randomId()}">
            <ili:class REF="?">
                <xsl:apply-templates mode="Content"/>
            </ili:class>
            <ili:BACK_REF name="MD_FeatureCatalogueDescription"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.classMD_FeatureCatalogueDescription>
    </xsl:template>

    <xsl:template mode="Content" match="gmd:MD_Band">
        <ili:GM03_2_1Comprehensive.Comprehensive.MD_Band TID="x{util:randomId()}">
            <xsl:apply-templates mode="Content" select="gmd:sequenceIdentifier/gco:MemberName/gco:aName"/>
            <xsl:apply-templates mode="text" select="gmd:descriptor"/>
            <xsl:apply-templates mode="text" select="gmd:maxValue"/>
            <xsl:apply-templates mode="text" select="gmd:minValue"/>
            <xsl:apply-templates mode="Content" select="gmd:units"/>
            <xsl:apply-templates mode="text" select="gmd:peakResponse"/>
            <xsl:apply-templates mode="text" select="gmd:bitsPerValue"/>
            <xsl:apply-templates mode="text" select="gmd:toneGradation"/>
            <xsl:apply-templates mode="text" select="gmd:scaleFactor"/>
            <xsl:apply-templates mode="text" select="gmd:offset"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.MD_Band>
    </xsl:template>

    <xsl:template mode="Content" match="gmd:MD_RangeDimension">
        <ili:GM03_2_1Comprehensive.Comprehensive.MD_RangeDimension TID="x{util:randomId()}">
            <xsl:apply-templates mode="Content" select="gmd:sequenceIdentifier/gco:MemberName/gco:aName"/>
            <xsl:apply-templates mode="text" select="gmd:descriptor"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.MD_RangeDimension>
    </xsl:template>

    <xsl:template mode="Content" match="gco:aName">
        <ili:sequenceIdentifier><xsl:value-of select="."/></ili:sequenceIdentifier>
    </xsl:template>

    <xsl:template mode="Content" match="che:CHE_MD_Class">
        <ili:GM03_2_1Comprehensive.Comprehensive.MD_Class TID="x{util:randomId()}">
            <xsl:apply-templates mode="text" select="che:name"/>
            <xsl:apply-templates mode="forceGroupText" select="che:description"/>
            <xsl:apply-templates mode="Content" select="che:baseClass"/>
            <xsl:apply-templates mode="Content" select="che:subClass"/>
            <xsl:apply-templates mode="Content" select="che:attribute"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.MD_Class>
    </xsl:template>

    <xsl:template mode="Content" match="che:attribute">
        <ili:GM03_2_1Comprehensive.Comprehensive.MD_Attribute TID="x{util:randomId()}">
            <xsl:apply-templates mode="text" select="che:name"/>
            <xsl:apply-templates mode="forceGroupText" select="che:description"/>
            <xsl:apply-templates mode="Content" select="che:namedType"/>
            <ili:BACK_REF name="MD_AbstractClass"/>
            <xsl:apply-templates mode="Content" select="che:anonymousType"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.MD_Attribute>
    </xsl:template>

    <xsl:template mode="forceGroupText" match="*">
        <xsl:choose>
            <xsl:when test="gmd:PT_FreeText">
               <xsl:apply-templates mode="groupText" select=".">
                 <xsl:with-param name="element" select="local-name(.)"/>
               </xsl:apply-templates>
            </xsl:when>
            <xsl:when test="gco:CharacterString[normalize-space(.) != '']">
                <xsl:element name="{local-name(.)}">
                    <ili:GM03_2_1Core.Core.PT_FreeText>
                        <ili:textGroup>
                            <ili:GM03_2_1Core.Core.PT_Group>
                                <ili:language><xsl:value-of select="$defaultLanguage"/></ili:language>
                                <ili:plainText><xsl:value-of select="gco:CharacterString"/></ili:plainText>
                            </ili:GM03_2_1Core.Core.PT_Group>
                        </ili:textGroup>
                    </ili:GM03_2_1Core.Core.PT_FreeText>
                </xsl:element>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

  <xsl:template mode="Content" match="che:domain">
    <ili:GM03_2_1Comprehensive.Comprehensive.domainMD_FeatureCatalogueDescription TID="x{util:randomId()}">
      <ili:domain REF="?">
        <xsl:apply-templates mode="Content" select="che:CHE_MD_CodeDomain"/>
      </ili:domain>
      <ili:BACK_REF name="MD_FeatureCatalogueDescription"/>
    </ili:GM03_2_1Comprehensive.Comprehensive.domainMD_FeatureCatalogueDescription>
  </xsl:template>

    <xsl:template mode="Content" match="che:type">
      <xsl:if test="normalize-space(gco:CharacterString) != ''">
        <ili:type REF="?">
          <ili:GM03_2_1Comprehensive.Comprehensive.MD_Type TID="x{util:randomId()}">
              <xsl:apply-templates mode="text" select="che:type"/>
              <xsl:apply-templates mode="Content" select="che:value/che:CHE_MD_CodeValue"/>
          </ili:GM03_2_1Comprehensive.Comprehensive.MD_Type>
        </ili:type>
      </xsl:if>
    </xsl:template>

    <xsl:template mode="Content" match="che:anonymousType">
        <ili:anonymousType REF="?">
            <xsl:apply-templates mode="Content" select="che:CHE_MD_Type"/>
        </ili:anonymousType>
    </xsl:template>

    <xsl:template mode="Content" match="che:CHE_MD_Type">
    <ili:GM03_2_1Comprehensive.Comprehensive.MD_Type TID="x{util:randomId()}">
      <xsl:apply-templates mode="text" select="che:type" />
      <xsl:apply-templates mode="Content" select="che:value/che:CHE_MD_CodeValue" />
    </ili:GM03_2_1Comprehensive.Comprehensive.MD_Type>
  </xsl:template>

    <xsl:template mode="Content" match="che:CHE_MD_CodeValue">
        <ili:GM03_2_1Comprehensive.Comprehensive.MD_CodeValue TID="x{util:randomId()}">
            <xsl:apply-templates mode="text" select="che:name"/>
            <xsl:apply-templates mode="text" select="che:code"/>
            <xsl:apply-templates mode="text" select="che:description"/>
            <xsl:apply-templates mode="Content" select="che:subValue/che:CHE_MD_CodeValue"/>
            <xsl:choose>
            <xsl:when test="name(..) = 'che:value'">
                <ili:BACK_REF name="MD_Type"/>
            </xsl:when>
            <xsl:otherwise>
                <ili:BACK_REF name="MD_CodeValue"/>
            </xsl:otherwise>
            </xsl:choose>
        </ili:GM03_2_1Comprehensive.Comprehensive.MD_CodeValue>
    </xsl:template>

  <xsl:template mode="Content" match="che:CHE_MD_CodeDomain">
    <ili:GM03_2_1Comprehensive.Comprehensive.MD_CodeDomain TID="x{util:randomId()}">
      <xsl:apply-templates mode="text" select="che:name" />
      <xsl:apply-templates mode="text" select="che:description" />
        <xsl:apply-templates mode="Content" select="che:baseDomain" />
      <xsl:apply-templates mode="Content" select="che:type" />
    </ili:GM03_2_1Comprehensive.Comprehensive.MD_CodeDomain>
  </xsl:template>

  <xsl:template mode="Content" match="che:baseDomain">
     <ili:baseDomain REF="?">
      <xsl:apply-templates mode="Content" select="che:CHE_MD_CodeDomain" />
    </ili:baseDomain>
  </xsl:template>

    <xsl:template mode="Content" match="che:namedType">
        <xsl:if test="./*">
            <ili:GM03_2_1Comprehensive.Comprehensive.MD_AttributenamedType TID="x{util:randomId()}">
                <ili:BACK_REF name="MD_Attribute"/>
              <ili:namedType REF="?">
                  <xsl:apply-templates mode="Content"/>
             </ili:namedType>
          </ili:GM03_2_1Comprehensive.Comprehensive.MD_AttributenamedType>
        </xsl:if>
    </xsl:template>

    <xsl:template mode="Content" match="gmd:featureCatalogueCitation">
        <xsl:apply-templates mode="Content"/>
    </xsl:template>

    <xsl:template mode="Content" match="gmd:units">
        <ili:units><xsl:value-of select=".//gml:catalogSymbol"/></ili:units>
    </xsl:template>
    <xsl:template mode="Content" match="che:dataModel">
        <xsl:choose>
        <xsl:when test="normalize-space(text()) = ''">
            <ili:dataModel>
             <ili:GM03_2_1Core.Core.PT_FreeURL>
                <ili:URLGroup>
                    <ili:GM03_2_1Core.Core.PT_URLGroup>
                        <ili:language><xsl:value-of select="$defaultLanguage"/></ili:language>
                        <ili:plainURL/>
                    </ili:GM03_2_1Core.Core.PT_URLGroup>
              </ili:URLGroup>
            </ili:GM03_2_1Core.Core.PT_FreeURL>
            </ili:dataModel>
        </xsl:when>
        <xsl:otherwise>
        <xsl:apply-templates mode="text" select="."/>
        </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template mode="Content" match="che:modelType">
        <xsl:apply-templates mode="text" select="."/>
    </xsl:template>

    <xsl:template mode="Content" match="gmd:CI_Citation">
        <ili:GM03_2_1Comprehensive.Comprehensive.CI_Citation TID="x{util:randomId()}">
        <xsl:apply-templates mode="textGroup" select="gmd:title"/>
        <xsl:apply-templates mode="text" select="gmd:edition"/>
        <xsl:apply-templates mode="text" select="gmd:editionDate"/>
        <xsl:apply-templates mode="groupEnum" select=".">
          <xsl:with-param name="element">presentationForm</xsl:with-param>
          <xsl:with-param name="newName">GM03_2_1Comprehensive.Comprehensive.CI_PresentationFormCode_</xsl:with-param>
        </xsl:apply-templates>
        <xsl:apply-templates mode="text" select="gmd:ISBN"/>
        <xsl:apply-templates mode="text" select="gmd:ISSN"/>
        <xsl:apply-templates mode="groupText" select=".">
            <xsl:with-param name="element">alternateTitle</xsl:with-param>
        </xsl:apply-templates>
        <xsl:apply-templates mode="text" select="gmd:collectiveTitle"/>
        <xsl:apply-templates mode="text" select="gmd:otherCitationDetails"/>
        <ili:BACK_REF name="MD_FeatureCatalogueDescription"/>
        <xsl:apply-templates mode="RefSystem" select="gmd:series/gmd:CI_Series"/>

        <xsl:apply-templates mode="RefSystem" select="gmd:date"/>

        <!-- not mapped -->
        <xsl:apply-templates mode="DataIdentification" select="gmd:identifier"/>
        <xsl:apply-templates mode="RefSystem" select="gmd:citedResponsibleParty"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.CI_Citation>

    </xsl:template>


    <xsl:template mode="Content" match="gmd:attributeDescription">
        <ili:attributeDescription><xsl:value-of select="gco:RecordType"/></ili:attributeDescription>
    </xsl:template>

    <xsl:template mode="Content" match="che:filmType">
        <ili:filmType><xsl:value-of select="che:CHE_MD_FilmTypeCode/@codeListValue"/></ili:filmType>
    </xsl:template>

    <xsl:template mode="Content" match="gmd:MD_ImageDescription|che:CHE_MD_ImageDescription">
        <ili:GM03_2_1Comprehensive.Comprehensive.MD_ImageDescription TID="x{util:randomId()}">
            <ili:BACK_REF name="MD_Metadata"/>
            <xsl:apply-templates mode="Content" select="gmd:attributeDescription"/>
            <xsl:apply-templates mode="text" select="gmd:contentType"/>
             <xsl:apply-templates mode="Content" select="che:filmType"/>
             <xsl:apply-templates mode="text" select="che:focalDistance"/>
            <xsl:apply-templates mode="text" select="gmd:illuminationElevationAngle"/>
            <xsl:apply-templates mode="text" select="gmd:illuminationAzimuthAngle"/>
            <xsl:apply-templates mode="text" select="gmd:imagingCondition"/>
            <xsl:apply-templates mode="text" select="gmd:cloudCoverPercentage"/>
            <xsl:apply-templates mode="text" select="gmd:compressionGenerationQuantity"/>
            <xsl:apply-templates mode="text" select="gmd:triangulationIndicator"/>
            <xsl:apply-templates mode="text" select="gmd:radiometricCalibrationDataAvailability"/>
            <xsl:apply-templates mode="text" select="gmd:cameraCalibrationInformationAvailability"/>
            <xsl:apply-templates mode="text" select="gmd:filmDistortionInformationAvailability"/>
            <xsl:apply-templates mode="text" select="gmd:lensDistortionInformationAvailability"/>

            <ili:imageQualityCode REF="?">
                <xsl:apply-templates mode="Extent" select="gmd:imageQualityCode/gmd:MD_Identifier"/>
            </ili:imageQualityCode>
            <ili:processingLevelCode REF="?">
                <xsl:apply-templates mode="Extent" select="gmd:processingLevelCode/gmd:MD_Identifier"/>
            </ili:processingLevelCode>
        </ili:GM03_2_1Comprehensive.Comprehensive.MD_ImageDescription>
    </xsl:template>

    <xsl:template mode="Content" match="*" priority="-100">
        <ili:ERROR>Unknown Content element <xsl:value-of select="local-name(..)"/>/<xsl:value-of select="local-name(.)"/></ili:ERROR>
    </xsl:template>
</xsl:stylesheet>
