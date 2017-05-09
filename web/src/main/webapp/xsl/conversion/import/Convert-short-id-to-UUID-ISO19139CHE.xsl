<xsl:stylesheet version="2.0"
				xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
				xmlns:gmd="http://www.isotc211.org/2005/gmd"
				xmlns:gco="http://www.isotc211.org/2005/gco"
				xmlns:util="xalan://org.fao.geonet.geocat.services.gm03.TranslateAndValidate"
				exclude-result-prefixes="util">
	<xsl:output omit-xml-declaration="yes" indent="yes"/>
	<xsl:strip-space elements="*"/>

	<xsl:template match="node()|@*">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*"/>
		</xsl:copy>
	</xsl:template>

	<!--<xsl:value-of select="util:convertToUuid(.)"/>-->

	<xsl:template match="//gmd:fileIdentifier/gco:CharacterString/text()"><xsl:value-of select="util:convertToUuid(.)"/>
	</xsl:template>
</xsl:stylesheet>

