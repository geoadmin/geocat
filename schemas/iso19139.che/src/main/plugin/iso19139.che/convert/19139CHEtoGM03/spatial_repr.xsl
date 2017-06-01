<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:ili="http://www.interlis.ch/INTERLIS2.3"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                exclude-result-prefixes="che gco gmd gml util">

    <!-- ================================================================================== -->

    <xsl:template mode="SpatialRepr" match="gmd:MD_VectorSpatialRepresentation">
        <ili:GM03_2_1Comprehensive.Comprehensive.MD_VectorSpatialRepresentation TID="x{util:randomId()}">
            <ili:BACK_REF name="MD_Metadata"/>
            <xsl:apply-templates mode="text" select="gmd:topologyLevel"/>
            <xsl:apply-templates mode="SpatialRepr" select="gmd:geometricObjects"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.MD_VectorSpatialRepresentation>
    </xsl:template>

    <xsl:template mode="SpatialRepr" match="gmd:geometricObjects">
        <xsl:apply-templates mode="SpatialRepr"/>
    </xsl:template>

    <xsl:template mode="SpatialRepr" match="gmd:MD_GeometricObjects">
        <ili:GM03_2_1Comprehensive.Comprehensive.MD_GeometricObjects TID="x{util:randomId()}">
            <xsl:apply-templates mode="text" select="gmd:geometricObjectType"/>
            <xsl:apply-templates mode="text" select="gmd:geometricObjectCount"/>
            <ili:BACK_REF name="MD_VectorSpatialRepresentation"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.MD_GeometricObjects>
    </xsl:template>

    <!-- ================================================================================== -->

    <xsl:template mode="SpatialRepr" match="gmd:MD_GridSpatialRepresentation">
        <ili:GM03_2_1Comprehensive.Comprehensive.MD_GridSpatialRepresentation TID="x{util:randomId()}">
            <ili:BACK_REF name="MD_Metadata"/>
            <xsl:apply-templates mode="text" select="gmd:numberOfDimensions"/>
            <xsl:apply-templates mode="SpatialRepr" select="gmd:axisDimensionProperties"/>
            <xsl:apply-templates mode="text" select="gmd:cellGeometry"/>
            <xsl:apply-templates mode="text" select="gmd:transformationParameterAvailability"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.MD_GridSpatialRepresentation>
    </xsl:template>

    <xsl:template mode="SpatialRepr" match="gmd:axisDimensionProperties">
        <xsl:apply-templates mode="SpatialRepr"/>
    </xsl:template>

    <xsl:template mode="SpatialRepr" match="gmd:MD_Dimension">
        <ili:GM03_2_1Comprehensive.Comprehensive.MD_Dimension TID="x{util:randomId()}">
            <xsl:apply-templates mode="text" select="gmd:dimensionName"/>
            <xsl:apply-templates mode="text" select="gmd:dimensionSize"/>
            <xsl:apply-templates mode="text" select="gmd:resolution"/>
            <ili:BACK_REF name="MD_GridSpatialRepresentation"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.MD_Dimension>
    </xsl:template>

    <!-- ================================================================================== -->

    <xsl:template mode="SpatialRepr" match="gmd:MD_Georectified">
        <ili:GM03_2_1Comprehensive.Comprehensive.MD_Georectified TID="x{util:randomId()}">
            <ili:BACK_REF name="MD_Metadata"/>
    <!-- gridSpatial properties -->
            <xsl:apply-templates mode="text" select="gmd:numberOfDimensions"/>
            <xsl:apply-templates mode="SpatialRepr" select="gmd:axisDimensionProperties"/>
            <xsl:apply-templates mode="text" select="gmd:cellGeometry"/>
            <xsl:apply-templates mode="text" select="gmd:transformationParameterAvailability"/>

    <!-- specific to MD_Georectified -->
            <xsl:apply-templates mode="text" select="gmd:checkPointAvailability"/>
            <xsl:apply-templates mode="text" select="gmd:checkPointDescription"/>
            <xsl:if test="gmd:cornerPoints">
                <ili:cornerPoints>
                    <xsl:apply-templates mode="SpatialRepr" select="gmd:cornerPoints"/>
                </ili:cornerPoints>
            </xsl:if>
            <xsl:apply-templates mode="SpatialRepr" select="gmd:centerPoint"/>
            <xsl:apply-templates mode="SpatialRepr" select="gmd:pointInPixel"/>
            <xsl:apply-templates mode="text" select="gmd:transformationDimensionDescription"/>
            <xsl:apply-templates mode="text_" select="gmd:transformationDimensionMapping"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.MD_Georectified>
    </xsl:template>

    <xsl:template match="gmd:cornerPoints" mode="SpatialRepr">
                <ili:GM03_2_1Core.Core.GM_Point_>
                    <ili:value>
                        <xsl:call-template name="explode" >
                            <xsl:with-param name="string" select="gml:Point/gml:coordinates"/>
                        </xsl:call-template>
                    </ili:value>
                </ili:GM03_2_1Core.Core.GM_Point_>
    </xsl:template>

    <xsl:template match="gmd:centerPoint" mode="SpatialRepr">
        <ili:centerPoint>
                <xsl:call-template name="explode" >
                    <xsl:with-param name="string" select="gml:Point/gml:coordinates"/>
                </xsl:call-template>
        </ili:centerPoint>
    </xsl:template>

    <xsl:template match="gmd:pointInPixel" mode="SpatialRepr">
        <ili:pointInPixel><xsl:value-of select="gmd:MD_PixelOrientationCode"/></ili:pointInPixel>
    </xsl:template>

    <!-- ================================================================================== -->

    <xsl:template mode="SpatialRepr" match="gmd:MD_Georeferenceable">
        <ili:GM03_2_1Comprehensive.Comprehensive.MD_Georeferenceable TID="x{util:randomId()}">
            <ili:BACK_REF name="MD_Metadata"/>

    <!-- gridSpatial properties -->
            <xsl:apply-templates mode="text" select="gmd:numberOfDimensions"/>
            <xsl:apply-templates mode="SpatialRepr" select="gmd:axisDimensionProperties"/>
            <xsl:apply-templates mode="text" select="gmd:cellGeometry"/>
            <xsl:apply-templates mode="text" select="gmd:transformationParameterAvailability"/>

    <!-- specific to MD_Georeferenceable -->
            <xsl:apply-templates mode="text" select="gmd:controlPointAvailability"/>
            <xsl:apply-templates mode="text" select="gmd:orientationParameterAvailability"/>
            <xsl:apply-templates mode="text" select="gmd:orientationParameterDescription"/>
            <xsl:apply-templates mode="SpatialRepr" select="gmd:georeferencedParameters"/>
            <xsl:apply-templates mode="Citation" select="gmd:parameterCitation"/>
        </ili:GM03_2_1Comprehensive.Comprehensive.MD_Georeferenceable>
    </xsl:template>

    <xsl:template mode="SpatialRepr" match="gmd:georeferencedParameters">
        <ili:georeferencedParameters><xsl:value-of select="gco:Record"/></ili:georeferencedParameters>
    </xsl:template>

    <!-- ================================================================================== -->

    <xsl:template mode="SpatialRepr" match="*" priority="-100">
        <ili:ERROR>Unknown SpatialRepr element <xsl:value-of select="local-name(.)"/></ili:ERROR>
    </xsl:template>

</xsl:stylesheet>
