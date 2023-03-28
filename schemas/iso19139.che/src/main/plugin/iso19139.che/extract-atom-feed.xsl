<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:che="http://www.geocat.ch/2008/che"
                version="2.0"
>

  <xsl:param name="atomProtocol"/>

  <xsl:template match="che:CHE_MD_Metadata">
    <!--<feeds>
    <xsl:for-each  select="gmd:identificationInfo/srv:SV_ServiceIdentification/srv:containsOperations/srv:SV_OperationMetadata/srv:connectPoint[ends-with(gmd:CI_OnlineResource/gmd:linkage/gmd:URL, 'xml')]">
        <atomfeed><xsl:value-of select="gmd:CI_OnlineResource/gmd:linkage/gmd:URL"/></atomfeed>
    </xsl:for-each>
    </feeds>-->

    <!-- Get first element -->
    <atomfeed>
      <xsl:value-of
        select="gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource[lower-case(gmd:protocol/gco:CharacterString) = lower-case($atomProtocol)]/gmd:linkage/gmd:URL"/>
    </atomfeed>

  </xsl:template>
</xsl:stylesheet>
