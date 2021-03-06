<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template mode="RefSystem" match="GM03Core.Core.MD_ReferenceSystem">
        <gmd:MD_ReferenceSystem>
            <xsl:apply-templates mode="RefSystem"/>
        </gmd:MD_ReferenceSystem>
    </xsl:template>

    <xsl:template mode="RefSystem" match="referenceSystemIdentifier">
        <gmd:referenceSystemIdentifier>
            <xsl:apply-templates mode="MdIdent"/>
        </gmd:referenceSystemIdentifier>
    </xsl:template>

    <xsl:template mode="RefSystem" match="text()">
        <xsl:call-template name="UnMatchedText">
            <xsl:with-param name="mode">RefSystem</xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <!-- ======================================================================== -->

    <xsl:template mode="MdIdent" match="GM03Comprehensive.Comprehensive.RS_Identifier|GM03Core.Core.RS_Identifier">
        <gmd:RS_Identifier>
            <xsl:apply-templates mode="MdIdent"/>
        </gmd:RS_Identifier>
    </xsl:template>

    <xsl:template mode="MdIdent" match="code">
        <gmd:code xsi:type="gmd:PT_FreeText_PropertyType">
            <xsl:attribute name="xsi:type">gmd:PT_FreeText_PropertyType</xsl:attribute>
            <xsl:apply-templates mode="language"/>
        </gmd:code>
    </xsl:template>

    <xsl:template mode="MdIdent" match="codeSpace">
        <gmd:codeSpace>
            <xsl:apply-templates mode="string"/>
        </gmd:codeSpace>
    </xsl:template>

    <xsl:template mode="MdIdent" match="version">
        <gmd:version>
            <xsl:apply-templates mode="string"/>
        </gmd:version>
    </xsl:template>

    <xsl:template mode="MdIdent" match="text()">
        <xsl:call-template name="UnMatchedText">
            <xsl:with-param name="mode">MdIdent</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
</xsl:stylesheet>
