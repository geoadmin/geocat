<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco">
	<xsl:output omit-xml-declaration="yes" indent="yes"/>
	
	<!-- copies original metadata -->
	<xsl:template match="node()|@*">
	  <xsl:copy>
	   <xsl:apply-templates select="node()|@*"/>
	  </xsl:copy>
	 </xsl:template>
	 
	 <!-- then concat the file identifier -->
	 <xsl:template match="/gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString">
	  <xsl:copy>
	  <xsl:choose>
	 	 <xsl:when test = "string-length(text()) &lt; 5" >
	 	  <xsl:value-of select="concat('ASIT-VD_', text())"/>
	   	</xsl:when>
	   	<xsl:otherwise>
	   		<xsl:value-of select="(text())"/>
	  	</xsl:otherwise>
	 </xsl:choose>
	 </xsl:copy>
       </xsl:template>
	
</xsl:stylesheet>