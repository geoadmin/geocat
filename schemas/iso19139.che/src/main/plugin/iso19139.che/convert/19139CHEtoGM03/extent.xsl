<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns="http://www.interlis.ch/INTERLIS2.3"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:gml320="http://www.opengis.net/gml"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:utilgc="java:org.fao.geonet.schema.iso19139che.ISO19139cheUtil"
                xmlns:xalan="http://xml.apache.org/xalan"
                exclude-result-prefixes="#all">

    <xsl:template mode="Extent" match="gmd:extent">
        <xsl:apply-templates mode="Extent" select="gmd:EX_Extent"/>
    </xsl:template>

    <xsl:template mode="Extent" match="gmd:EX_Extent">
        <GM03_2_1Core.Core.EX_Extent TID="x{util:randomId()}">
            <xsl:apply-templates mode="textGroup" select="gmd:description"/>
            <BACK_REF name="MD_DataIdentification"/>
            <xsl:apply-templates mode="Extent" select="gmd:geographicElement"/>
            <xsl:apply-templates mode="Extent" select="gmd:temporalElement"/>
            <xsl:apply-templates mode="Extent" select="gmd:verticalElement"/>
        </GM03_2_1Core.Core.EX_Extent>
    </xsl:template>

    <xsl:template mode="Extent" match="gmd:geographicElement">
        <GM03_2_1Core.Core.EX_ExtentgeographicElement TID="x{util:randomId()}">
            <BACK_REF name="EX_Extent"/>
            <geographicElement REF="?">
                <xsl:apply-templates mode="Extent"/>
            </geographicElement>
        </GM03_2_1Core.Core.EX_ExtentgeographicElement>
    </xsl:template>

    <xsl:template mode="Extent" match="gmd:temporalElement">
        <GM03_2_1Core.Core.EX_ExtenttemporalElement TID="x{util:randomId()}">
            <BACK_REF name="EX_Extent"/>
            <temporalElement REF="?">
	            <GM03_2_1Core.Core.EX_TemporalExtent TID="x{util:randomId()}">
		            <extent>
		                <GM03_2_1Core.Core.TM_Primitive>
		                    <xsl:apply-templates mode="TimePeriod"/>
		                </GM03_2_1Core.Core.TM_Primitive>
		            </extent>
	            </GM03_2_1Core.Core.EX_TemporalExtent>
            </temporalElement>
        </GM03_2_1Core.Core.EX_ExtenttemporalElement>
    </xsl:template>

    <xsl:template mode="TimePeriod" match="gml:begin|gml320:begin">
        <begin><xsl:value-of select=".//text()"/></begin>
    </xsl:template>
    <xsl:template mode="TimePeriod" match="gml:end|gml320:end">
        <end><xsl:value-of select=".//text()"/></end>
    </xsl:template>

    <xsl:template mode="Extent" match="gmd:geographicElement[gmd:EX_BoundingPolygon//(gml:MultiSurface|gml320:MultiSurface)]" priority="10">
        <xsl:variable name="exploded">
            <xsl:apply-templates mode="explode-multipolygons" />
        </xsl:variable>
        <xsl:for-each select="$exploded/gmd:geographicElement">
            <GM03_2_1Core.Core.EX_ExtentgeographicElement TID="x{util:randomId()}">
                <BACK_REF name="EX_Extent"/>
                <geographicElement REF="?">
                    <xsl:apply-templates mode="Extent"/>
                </geographicElement>
            </GM03_2_1Core.Core.EX_ExtentgeographicElement>
        </xsl:for-each>
    </xsl:template>

    <xsl:template mode="Extent" match="gmd:EX_BoundingPolygon">
        <GM03_2_1Core.Core.EX_BoundingPolygon TID="x{util:randomId()}">
            <xsl:apply-templates mode="text" select="gmd:extentTypeCode"/>
            <xsl:apply-templates mode="Extent" select="gmd:polygon"/>
        </GM03_2_1Core.Core.EX_BoundingPolygon>
    </xsl:template>

    <xsl:template mode="Extent" match="gmd:polygon">
        <xsl:apply-templates mode="gml"/>
    </xsl:template>

    <xsl:template mode="Extent" match="gmd:EX_GeographicBoundingBox">
        <GM03_2_1Core.Core.EX_GeographicBoundingBox TID="x{util:randomId()}">
            <xsl:apply-templates mode="text" select="gmd:extentTypeCode"/>
            <xsl:apply-templates mode="text" select="gmd:northBoundLatitude"/>
            <xsl:apply-templates mode="text" select="gmd:southBoundLatitude"/>
            <xsl:apply-templates mode="text" select="gmd:eastBoundLongitude"/>
            <xsl:apply-templates mode="text" select="gmd:westBoundLongitude"/>
        </GM03_2_1Core.Core.EX_GeographicBoundingBox>
    </xsl:template>

    <xsl:template mode="Extent" match="gmd:EX_GeographicDescription">
        <GM03_2_1Core.Core.EX_GeographicDescription TID="x{util:randomId()}">
            <xsl:apply-templates mode="text" select="gmd:extentTypeCode"/>
            <xsl:apply-templates mode="Extent" select="gmd:geographicIdentifier"/>
        </GM03_2_1Core.Core.EX_GeographicDescription>
    </xsl:template>

    <xsl:template mode="Extent" match="gmd:geographicIdentifier">
        <geographicIdentifier REF="?">
            <xsl:apply-templates mode="Extent"/>
        </geographicIdentifier>
    </xsl:template>

    <xsl:template mode="Extent" match="gmd:MD_Identifier">
        <GM03_2_1Core.Core.MD_Identifier TID="x{util:randomId()}">
            <xsl:apply-templates mode="text" select="gmd:code"/>
            <xsl:apply-templates mode="Extent" select="gmd:authority"/>
        </GM03_2_1Core.Core.MD_Identifier>
    </xsl:template>

    <xsl:template mode="Extent" match="*" priority="-100">
        <ERROR>Unknown Extent element <xsl:value-of select="local-name(.)"/></ERROR>
    </xsl:template>

    <xsl:template mode="gml" match="gml:Polygon[count(ancestor::gml:MultiSurface) = 0]|gml:MultiSurface[gml:surfaceMember]|gml320:Polygon[count(ancestor::gml320:MultiSurface) = 0]|gml320:MultiSurface[gml320:surfaceMember]">
        <polygon>
            <SURFACE>
                <BOUNDARY>
                    <xsl:apply-templates mode="gml" select="child::node()[1]"/>
                </BOUNDARY>
                <xsl:for-each select="gml:surfaceMember//gml:interior|gml320:surfaceMember//gml320:interior">
                <BOUNDARY>
                    <xsl:apply-templates mode="gml" select="."/>
                </BOUNDARY>
                </xsl:for-each>
            </SURFACE>
        </polygon>
    </xsl:template>

    <xsl:template mode="gml" match="gml:exterior|gml:curveMember|gml:Ring|gml:LinearRing|gml:surfaceMember|gml:Polygon|gml320:exterior|gml320:curveMember|gml320:Ring|gml320:LinearRing|gml320:surfaceMember|gml320:Polygon">
        <xsl:choose>
        <xsl:when test=".//(gml:exterior|gml320:exterior)">
            <xsl:apply-templates mode="gml" select=".//(gml:exterior|gml320:exterior)[1]"/>
        </xsl:when>
        <xsl:otherwise>
            <xsl:apply-templates mode="gml" />
        </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template mode="gml" match="gml:interior|gml320:interior">
        <xsl:apply-templates mode="gml"/>
        <!--        do nothing        -->
    </xsl:template>

    <xsl:template mode="gml" match="gml:LineString|gml320:LineString">
        <POLYLINE>
            <xsl:apply-templates mode="gml"/>
        </POLYLINE>
    </xsl:template>

    <xsl:template mode="gml" match="gml:posList|gml320:posList">
        <xsl:variable name="line" select="utilgc:posListToGM03Coords(node(),string(.),'2')"/>
        <xsl:copy-of select="$line"/>
    </xsl:template>

    <xsl:template name="explode">
        <xsl:param name="string"/>

        <xsl:variable name="sep" select="' '"/>
        <xsl:variable name="x" select="substring-before($string, $sep)"/>
        <xsl:variable name="next" select="substring-after($string, $sep)"/>
        <xsl:variable name="y">
            <xsl:choose>
                <xsl:when test="not(contains($next, $sep))"><xsl:value-of select="$next"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="substring-before($next, $sep)"/></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="nextnext" select="substring-after($next, $sep)"/>

        <xsl:if test="$x != '' and $y != ''">
            <COORD>
                <C1><xsl:value-of select="$x"/></C1>
                <C2><xsl:value-of select="$y"/></C2>
            </COORD>

            <xsl:call-template name="explode">
                <xsl:with-param name="string" select="$nextnext"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template mode="gml" match="gml:coordinates|gml320:coordinates">
        <!-- will be converted by ISO19139CHEtoGM03 -->
        <GML_COORDINATES decimal="{@decimal}" cs="{@cs}" ts="{@ts}"><xsl:value-of select="."/></GML_COORDINATES>
    </xsl:template>

    <xsl:template mode="gml" match="*" priority="-100">
        <ERROR>Unknown gml element <xsl:value-of select="local-name(.)"/></ERROR>
    </xsl:template>

    <!-- Explode multipolygons into multiple normal polygons -->
    <xsl:template mode="explode-multipolygons" match="gml:Polygon[ancestor::gml:MultiSurface]|gml320:Polygon[ancestor::gml320:MultiSurface]">
        <gmd:geographicElement>
            <gmd:EX_BoundingPolygon>
                <xsl:if test="ancestor::gmd:EX_BoundingPolygon/gmd:extentTypeCode">
                    <xsl:copy-of select="ancestor::gmd:EX_BoundingPolygon/gmd:extentTypeCode"/>
                </xsl:if>
                <gmd:polygon>
                    <gml:MultiSurface gml:id="Na2846ee8bca9421795638ae6c62eb5ea">
                        <gml:surfaceMember>
                            <xsl:copy-of select="."/>
                        </gml:surfaceMember>
                    </gml:MultiSurface>
                </gmd:polygon>
            </gmd:EX_BoundingPolygon>
        </gmd:geographicElement>
    </xsl:template>


</xsl:stylesheet>

