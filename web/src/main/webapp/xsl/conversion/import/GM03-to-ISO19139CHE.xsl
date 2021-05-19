<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
				xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
				xmlns:util="java:org.fao.geonet.api.gm03.TranslateAndValidate"
				exclude-result-prefixes="#all">
	<xsl:output method="xml"
				version="1.0"
				encoding="UTF-8"
				indent="yes"/>

	<xsl:param name="uuid"/>
	<xsl:param name="validate"/>
	<xsl:param name="debugDir"/>
	<xsl:param name="debugFileName"/>
	<xsl:param name="webappDir"/>
	<xsl:template match="/">
		<xsl:copy-of select="util:toCheBootstrap(., $uuid, $validate, $debugFileName, $webappDir)"></xsl:copy-of>
	</xsl:template>
	<xsl:strip-space elements="*"/>
</xsl:stylesheet>
