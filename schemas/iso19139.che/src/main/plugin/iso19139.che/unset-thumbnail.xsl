<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet   xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" 
						xmlns:gco="http://www.isotc211.org/2005/gco"
						xmlns:gmd="http://www.isotc211.org/2005/gmd"
	          xmlns:geonet="http://www.fao.org/geonetwork"
	          xmlns:che="http://www.geocat.ch/2008/che" >

	<!-- ================================================================= -->
	
	<xsl:template match="/root">
		 <xsl:apply-templates select="che:CHE_MD_Metadata"/>
	</xsl:template>

	<!-- ================================================================= -->
	
	<xsl:template priority="5" match="gmd:graphicOverview[gmd:MD_BrowseGraphic/gmd:fileDescription/gco:CharacterString = /root/env/type or
						 gmd:MD_BrowseGraphic/gmd:fileDescription/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString = /root/env/type]"/>

	<!-- ================================================================= -->

	<xsl:template match="@*|node()">
		 <xsl:copy>
			  <xsl:apply-templates select="@*|node()"/>
		 </xsl:copy>
	</xsl:template>
	
	<xsl:template match="geonet:info" priority="2"/>
</xsl:stylesheet>
