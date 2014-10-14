<?xml version="1.0" encoding="UTF-8"?>

	<!--
		Extracts the information from iso19139 snippets for ReusableObjManager
		strategies when adding new reusable objects
	-->

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
	xmlns:che="http://www.geocat.ch/2008/che" xmlns:gco="http://www.isotc211.org/2005/gco"
	xmlns:gml="http://www.opengis.net/gml" xmlns:gmd="http://www.isotc211.org/2005/gmd">

    <xsl:output indent="yes" method="xml"/>

	<xsl:template match="/">
		<data>
			<xsl:apply-templates />
		</data>
	</xsl:template>

	<!-- Extent info (and online resource)-->
    <xsl:template match="//gmd:description">
        <desc>
            <xsl:copy-of select="." />
        </desc>
    </xsl:template>    
    <xsl:template match="//gmd:geographicIdentifier">
        <geoId>
            <xsl:copy-of select="." />
        </geoId>
    </xsl:template>
    <xsl:template match="//gmd:geographicIdentifier//gmd:LocalisedCharacterString">
        <geoId>
             <xsl:value-of select="." />
        </geoId>
    </xsl:template>
    <xsl:template match="//gmd:extentTypeCode//gco:Boolean">
        <extentTypeCode>
             <xsl:value-of select="." />
        </extentTypeCode>
    </xsl:template>


    <!-- Keywords info -->
    <xsl:template match="//gmd:keyword//gco:CharacterString">
        <xsl:element name="keyword">
            <xsl:value-of select="." />
        </xsl:element>
    </xsl:template>
    <xsl:template match="//gmd:keyword//gmd:LocalisedCharacterString">
        <xsl:element name="keyword">
            <xsl:attribute name="locale"><xsl:value-of select="substring(@locale,2)" /></xsl:attribute>
            <xsl:value-of select="." />
        </xsl:element>
    </xsl:template>

    <!-- Format info -->
    <xsl:template match="//gmd:MD_Format">
        <xsl:element name="format">
            <xsl:attribute name="name">
            <xsl:value-of select="gmd:name/gco:CharacterString" />
        </xsl:attribute>
            <xsl:if test="gmd:version">
                <xsl:attribute name="version">
                <xsl:value-of select="gmd:version/gco:CharacterString" />
            </xsl:attribute>
            </xsl:if>
        </xsl:element>
    </xsl:template>



	<xsl:template match="text()">
	</xsl:template>


</xsl:stylesheet>