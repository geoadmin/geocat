<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron">

    <sch:title xmlns="http://www.w3.org/2001/XMLSchema">URL Validation</sch:title>
    <sch:ns prefix="gml" uri="http://www.opengis.net/gml" />
    <sch:ns prefix="gmd" uri="http://www.isotc211.org/2005/gmd" />
    <sch:ns prefix="che" uri="http://www.geocat.ch/2008/che" />
    <sch:ns prefix="srv" uri="http://www.isotc211.org/2005/srv" />
    <sch:ns prefix="gco" uri="http://www.isotc211.org/2005/gco" />
    <sch:ns prefix="geonet" uri="http://www.fao.org/geonetwork" />
    <sch:ns prefix="xlink" uri="http://www.w3.org/1999/xlink" />
    <sch:ns prefix="xslutil" uri="java:org.fao.geonet.util.XslUtil" />
    <sch:ns prefix="xsi" uri="http://www.w3.org/2001/XMLSchema-instance" />

    <!-- =============================================================
    CHE schematron rules:
    ============================================================= -->

    <sch:pattern>
        <sch:title>$loc/strings/invalidURLCheck</sch:title>
        <!-- Check specification names and status -->
        <sch:rule context="//gmd:linkage//che:LocalisedURL | //gmd:linkage//gmd:URL">

            <sch:let name="isValidUrl" value="xslutil:validateURL(text())" />
            <sch:assert test="$isValidUrl = true()" see="./ancestor::gmd:linkage/geonet:element/@ref">
                <sch:value-of select="$loc/strings/alert.invalidURL/div" />
                '<sch:value-of select="string(.)" />'
            </sch:assert>
        </sch:rule>
    </sch:pattern>

    <sch:pattern>
        <sch:title>$loc/strings/invalidURLCheck</sch:title>
        <!-- Check specification names and status -->
        <sch:rule context="//gco:CharacterString[matches(., 'http|ftp')] |
                           //gmd:LocalisedCharacterString[matches(., 'http|ftp')]">

            <sch:let name="isValidUrl" value="xslutil:findAndValidateURLs(text())" />

            <sch:assert test="$isValidUrl = true()" see="(./ancestor::*[@xsi:type])[1]/geonet:element/@ref">
                <sch:value-of select="$loc/strings/alert.invalidURL/div" />
                '<sch:value-of select="string(.)" />'
            </sch:assert>
        </sch:rule>
    </sch:pattern>

</sch:schema>
