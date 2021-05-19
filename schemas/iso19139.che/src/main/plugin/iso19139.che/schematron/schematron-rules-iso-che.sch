<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <sch:title xmlns="http://www.w3.org/2001/XMLSchema">GM03 implementing rules</sch:title>
    <sch:ns prefix="gml" uri="http://www.opengis.net/gml"/>
    <sch:ns prefix="gmd" uri="http://www.isotc211.org/2005/gmd"/>
    <sch:ns prefix="che" uri="http://www.geocat.ch/2008/che"/>
    <sch:ns prefix="srv" uri="http://www.isotc211.org/2005/srv"/>
    <sch:ns prefix="gco" uri="http://www.isotc211.org/2005/gco"/>
    <sch:ns prefix="geonet" uri="http://www.fao.org/geonetwork"/>
    <sch:ns prefix="xlink" uri="http://www.w3.org/1999/xlink"/>    
    <sch:ns prefix="xslutil" uri="java:org.fao.geonet.util.XslUtil"/>

    <!-- =============================================================
    CHE schematron rules:
    ============================================================= -->
    <sch:pattern>
        <sch:title>$loc/strings/M100</sch:title>
        <sch:rule
            context="//che:CHE_MD_DataIdentification">
        	<sch:let name="emptyGeoId" value="che:basicGeodataID/gco:CharacterString!='' and che:basicGeodataIDType/che:basicGeodataIDTypeCode/@codeListValue=''"/>
        	<sch:let name="noGeoId" value="che:basicGeodataID/gco:CharacterString!='' and not(che:basicGeodataIDType)"/>

            <!--  Check that basicGeodataId is defined -->
            <sch:assert test="not($noGeoId)" see="./geonet:child[@name='basicGeodataIDType']/@uuid"><sch:value-of select="$loc/strings/alert.M100"/></sch:assert>

            <sch:assert test="not($emptyGeoId)" see="./che:basicGeodataIDType/geonet:element/@ref"><sch:value-of select="$loc/strings/alert.M100"/></sch:assert>
            <sch:report test="not($emptyGeoId)"><sch:value-of select="$loc/strings/report.M100/div"/></sch:report>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>$loc/strings/M101</sch:title>
        <sch:rule
            context="//che:CHE_MD_FeatureCatalogueDescription">
        	<sch:let name="emptyModelType" value="che:dataModel/che:PT_FreeURL/che:URLGroup/che:LocalisedURL!='' and (not(che:modelType) or che:modelType/che:CHE_MD_modelTypeCode/@codeListValue='')"/>
   
            <!--  Check that basicGeodataId is defined -->
            <sch:assert test="not($emptyModelType)">$loc/strings/alert.M101</sch:assert>
            <sch:report test="not($emptyModelType)"><sch:value-of select="$loc/strings/report.M101/div"/></sch:report>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>$loc/strings/M102</sch:title>
        <sch:rule
            context="//*[*/@codeListValue]">
        	<sch:let name="emptyCodeList" value="*/@codeListValue='' and not(name(.) = 'che:basicGeodataIDType' and ./@gco:nilReason)"/>
   
            <!--  Check that basicGeodataId is defined -->
            <sch:assert test="not($emptyCodeList)">'<sch:value-of select="name(.)"/>' - <sch:value-of select="$loc/strings/alert.M102"/></sch:assert>
            <sch:report test="not($emptyCodeList)">'<sch:value-of select="name(.)"/>' - <sch:value-of select="$loc/strings/report.M102/div"/></sch:report>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>$loc/strings/M103</sch:title>
        <sch:rule
            context="//gmd:CI_Citation/gmd:title">
            <sch:let name="language" value="gmd:language/gco:CharacterString|gmd:language/gmd:LanguageCode/@codeListValue"/>
        	<sch:let name="emptyTitle" value="//gmd:LocalisedCharacterString[@locale=$language]=''"/>
   
            <!--  Check that basicGeodataId is defined -->
            <sch:assert test="not($emptyTitle)">$loc/strings/alert.M103</sch:assert>
            <sch:report test="not($emptyTitle)"><sch:value-of select="$loc/strings/report.M103/div"/></sch:report>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>$loc/strings/M104</sch:title>
        <sch:rule
            context="//gmd:topicCategory">
            <sch:let name="code" value="gmd:MD_TopicCategoryCode[
            	normalize-space(.)='planningCadastre' or
            	normalize-space(.)='geoscientificInformation' or
            	normalize-space(.)='imageryBaseMapsEarthCover' or
            	normalize-space(.)='environment']"/>
        	<sch:let name="sibling" value="../gmd:topicCategory/gmd:MD_TopicCategoryCode[starts-with(normalize-space(.), concat(normalize-space($code), '_'))]"/>

            <sch:let name="legalTopicCategory" value="($code and $sibling) or not($code)"/>

            <!--  Check that basicGeodataId is defined -->
            <sch:assert test="$legalTopicCategory">$loc/strings/alert.M104</sch:assert>
            <sch:report test="$legalTopicCategory"><sch:value-of select="$loc/strings/report.M104/div"/></sch:report>
        </sch:rule>
    </sch:pattern>
    <sch:pattern>
        <sch:title>$loc/strings/M105</sch:title>
        <sch:rule
            context="//che:CHE_CI_ResponsibleParty">
            <sch:let name="code" value="normalize-space(gmd:contactInfo/gmd:CI_Contact/gmd:address//gmd:country/gco:CharacterString)"/>

            <!--  Check that basicGeodataId is defined -->
            <sch:assert test="($code  = '' or $code  = 'CH' or $code  = 'LI' or $code  = 'DE' or $code  = 'FR' or $code  = 'IT' or $code  = 'AT')">$loc/strings/alert.M105</sch:assert>
            <sch:report test="($code  = '' or $code  = 'CH' or $code  = 'LI' or $code  = 'DE' or $code  = 'FR' or $code  = 'IT' or $code  = 'AT')"><sch:value-of select="$loc/strings/report.M105/div"/></sch:report>
        </sch:rule>
    </sch:pattern>


    <sch:pattern>
        <sch:title>$loc/strings/hierarchyLevelName</sch:title>
        <!-- Check specification names and status -->
        <sch:rule context="/node()">

            <sch:let name="hierarchyLevels" value="count(gmd:hierarchyLevel/gmd:MD_ScopeCode)"/>
            <sch:let name="dataset" value="count(gmd:hierarchyLevel/gmd:MD_ScopeCode[@codeListValue='dataset'])"/>
            <sch:let name="hierarchyLevelNames" value="count(gmd:hierarchyLevelName[normalize-space(gco:CharacterString[1]) != ''])"/>
            <sch:assert test="$hierarchyLevels = 0 or $dataset = 1 or ($dataset != 1 and $hierarchyLevelNames &gt; 0)">
                <sch:value-of select="$loc/strings/alert.needsHierarchyLevelName/div"/>
            </sch:assert>
            <sch:report test="$hierarchyLevels = 0 or $dataset = 1 or ($dataset != 1 and $hierarchyLevelNames &gt; 0)">
                <sch:value-of select="$loc/strings/report.needsHierarchyLevelName/div"/>
            </sch:report>
        </sch:rule>
    </sch:pattern>

</sch:schema>
