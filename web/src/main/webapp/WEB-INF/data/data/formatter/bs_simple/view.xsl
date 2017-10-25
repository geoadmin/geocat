<?xml version="1.0" encoding="UTF-8"?>
<!-- XSL-Styleshett für geocat.ch ( 7.Juli 2011 )-->
<!-- Dieses XSL-Styleshett wurde für die Metadatenverwaltung des Kantons Basel-Stadt durch die Fachstelle für Geoinformationen erstellt und gepflegt. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmx="http://www.isotc211.org/2005/gmx" xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:gml="http://www.opengis.net/gml" xmlns:gts="http://www.isotc211.org/2005/gts" xmlns:che="http://www.geocat.ch/2008/che" xmlns:geonet="http://www.fao.org/geonetwork" xmlns:exslt="http://exslt.org/common" xmlns:xlink="http://www.w3.org/1999/xlink">
	<!-- Load labels. -->
	<xsl:variable name="label" select="/root/schemas/iso19139/iso19139"/>
	<xsl:variable name="label.che" select="/root/schemas/iso19139.che/labels"/>
	<xsl:variable name="value" select="/root/schemas/iso19139/iso19139"/>
	<xsl:variable name="value.che" select="/root/schemas/iso19139.che/codelists"/>
	<xsl:template match="/">
		<!-- Der Slash (/) bedeutet dass das Template für alle Elemente verwendet wird. -->
		<html>
			<head>
				<meta http-equiv="expires" content="0"/>
				<!-- Ist nötig, damit die Attribute des Ausdehungs iframes nicht im cache gespeichert werden -->
				<title>GeoPortal Basel-Stadt - Metadatenkatalog</title>
			</head>
			<body leftmargin="0" topmargin="0" bgcolor="#ffffff" marginheight="0" marginwidth="0">
				<xsl:apply-templates select="/root/che:CHE_MD_Metadata"/>
			</body>
		</html>
	</xsl:template>
	<!-- Hier wird das Layout Gerüst für Kanton BS, also Banner mit Logo etc. angegeben -->
	<xsl:template match="che:CHE_MD_Metadata">
		<table width="800" border="0" cellpadding="0" cellspacing="0" height="100%">
			<tbody>
				<tr>
					<td height="64" valign="top">
						<table width="800" border="0" cellpadding="0" cellspacing="0" height="64">
							<tbody>
								<tr align="left" valign="top">
									<td width="2"/>
									<td width="398" bgcolor="#ffffff">
										<a href="http://www.geo.bs.ch" target="_blank">
											<img border="0">
												<xsl:attribute name="src">http://www.stadtplan.bs.ch/geoviewer/custom/images/geoportal/stab_geoportal.png</xsl:attribute>
											</img>
										</a>
									</td>
									<td align="right" valign="middle" width="325">
									<a target="_blank">
											<xsl:attribute name="href">
												<xsl:text>http://www.geocat.ch/geonetwork/srv/deu/geocat?uuid=</xsl:text>
												<xsl:value-of select="gmd:fileIdentifier/gco:CharacterString"/>
											</xsl:attribute>
										<img border="0" src="http://www.stadtplan.bs.ch/geoviewer/data/meta/digital/images/Logos/banner_geocat.ch.png"></img></a>
									</td>
									<td width="75"  valign="middle" align="center">
										<a target="_blank">
											<xsl:attribute name="href">
												<xsl:text>http://www.geocat.ch/geonetwork/srv/deu/geocat?uuid=</xsl:text>
												<xsl:value-of select="gmd:fileIdentifier/gco:CharacterString"/>
											</xsl:attribute>
										<img border="0" src="http://www.stadtplan.bs.ch/geoviewer/data/meta/digital/images/Logos/logo_geocat.ch.png"></img></a>
									</td>
								</tr>
							</tbody>
						</table>
					</td>
				</tr>
				<tr>
					<td height="47" valign="top">
						<table width="800" border="0" cellpadding="0" cellspacing="0" height="47">
							<tbody>
								<tr align="left" valign="top">
									<td width="800" bgcolor="#204454" height="46">
										<table width="800" border="0" cellpadding="0" cellspacing="0">
											<tbody>
												<tr>
													<td colspan="2" height="12"/>
												</tr>
												<tr>
													<td width="75" height="26"> </td>
<!-- Das Textende muss je nach Version angepasst werden: Komplett / Erweitert / Einfach -->													
													<td valign="top" height="26" align="left" width="725" style="font-family: Arial, Helvetica, sans-serif; font-size: 20px;font-style:normal; font-weight:normal; color:#D5DBDE; text-decoration:none; line-height:26px">Metadatenkatalog - Einfach</td> 		
												</tr>
											</tbody>
										</table>
									</td>
								</tr>
								<tr>
									<td colspan="3" bgcolor="#ffffff" height="1"/>
								</tr>
							</tbody>
						</table>
					</td>
				</tr>
				<tr>
					<td valign="top">
						<table width="800" border="0" cellpadding="0" cellspacing="0" height="100%">
							<tbody>
								<tr>
									<td width="800" align="left" bgcolor="#f1f1f1" height="504" valign="top">
										<table class="fliess" width="800" border="0" cellpadding="0" cellspacing="0">
											<tbody>
												<tr>
													<td width="75" valign="top">
													</td>
													<td class="text" width="*" align="left" valign="top">
														<br/>
														<table width="725" border="0" cellpadding="0" cellspacing="0" style="font-family: Arial, Helvetica, sans-serif;font-size: 12px; font-style: normal; color: #000000; text-decoration: none; line-height: 17px;">
															<!-- Change border to 1 here -->
															<tbody>
																<tr>
																	<td colspan="2" class="textl">
																		<b>Details zum ausgewählten digitalen Datensatz</b>
																	</td>
																</tr>
																<tr>
																	<td valign="middle" class="text" height="50">
																		<a style="font-family: Arial, Helvetica, sans-serif; font-size: 11px; color: #235f35; font-style: normal; text-decoration: underline; line-height: 14px;" href="javascript:window.print();">Drucken
																		</a>
																	</td>
																	<td align= "right" valign="middle" height="50" style="font-family: Arial, Helvetica, sans-serif; font-size: 11px; color: #235f35; font-style: normal; text-decoration: underline; line-height: 14px;">
																		Darstellung:
<!-- Die Links müssen je nachdem auf Test- oder Produktionsumgebung angepasst werden -->	
																		<a target="_self">																		
																			<xsl:attribute name="href">
																				<!-- Testumgebung  <xsl:text>http://tc-geocat0i.bgdi.admin.ch/geonetwork/srv/deu/metadata.formatter.html?xsl=bs_simple_test_20120203&amp;uuid=</xsl:text> --> 
																				<!-- Produktivumgebung	--> <xsl:text>http://www.geocat.ch/geonetwork/srv/deu/metadata.formatter.html?xsl=bs_simple&amp;uuid=</xsl:text> 
																				<xsl:value-of select="gmd:fileIdentifier/gco:CharacterString"/>
																			</xsl:attribute>
																			Einfach</a>
																			 / 
																		<a target="_self">																		
																			<xsl:attribute name="href">	
																				<!-- Testumgebung  <xsl:text>http://tc-geocat0i.bgdi.admin.ch/geonetwork/srv/deu/metadata.formatter.html?xsl=bs_extended_test_20120203&amp;uuid=</xsl:text> --> 
																				<!-- Produktivumgebung	--> <xsl:text>http://www.geocat.ch/geonetwork/srv/deu/metadata.formatter.html?xsl=bs_extended&amp;uuid=</xsl:text> 
																				<xsl:value-of select="gmd:fileIdentifier/gco:CharacterString"/>
																			</xsl:attribute>
																			Erweitert</a>
																			/ 
																		<a target="_self">																		
																			<xsl:attribute name="href">	
																				<!-- Testumgebung  <xsl:text>http://tc-geocat0i.bgdi.admin.ch/geonetwork/srv/deu/metadata.formatter.html?xsl=bs_full_test_20120203&amp;uuid=</xsl:text> --> 
																				<!-- Produktivumgebung	--> <xsl:text>http://www.geocat.ch/geonetwork/srv/deu/metadata.formatter.html?xsl=bs_full&amp;uuid=</xsl:text> 
																				<xsl:value-of select="gmd:fileIdentifier/gco:CharacterString"/>
																			</xsl:attribute>
																			Komplett</a>
																	</td>
																</tr>
																<tr>
																	<td colspan="2">
																		<xsl:call-template name="content"/>
																		<!--Verweist auf das Template mit dem Namen content-->
																	</td>
																</tr>
																<tr>
																	<td colspan="2" bgcolor="#f1f1f1">
																		<br/>
																	</td>
																</tr>
															</tbody>
														</table>
													</td>
													<td width="27" align="left" valign="top">
													 </td>
													<td width="30" align="left" valign="top">
													</td>
												</tr>
											</tbody>
										</table>
									</td>
								</tr>
							</tbody>
						</table>
					</td>
				</tr>
			</tbody>
		</table>
	</xsl:template>
	<!-- 	
====================================================== 
	CONTENT 
======================================================
    Inhalt den wir aus der XML Datei herausholen -->
	<xsl:template name="content">
		<table border="0" cellpadding="4" cellspacing="2" style="font-family: Arial, Helvetica, sans-serif;font-size: 12px; font-style: normal; color: #000000; text-decoration: none">
<!-- Titel - simple-->		
			<tr>
				<td colspan="2" class="text" bgcolor="silver">
					<br/>
					<span style="font-size: 18px;font-weight:bold">
						<!-- hier kann direkt gmd:ident.. angegeben werden ohne // weil im template vorhin schon che:CHE_MD_Metadata selektiert wurde und der pfad nun dort weitergeht. -->
						<!-- <xsl:value-of select="//gmd:title"/> 110209 Vollstaendiger Pfad angegeben-->
						<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString"/>
					</span>
				</td>
			</tr>
<!--Beispielbild - simple-->	
			<tr>
				<td colspan="2" class="text" bgcolor="#ffffff" valign="TOP">
					<!-- Beispielbild das bei uns liegt. Die Adresse des Bildes ist im XML-Document abgespeichert. -->
					<!-- [2] muss zurzeit eingeführt werden, weil aus irgendeinem Grund bereits ein anderes Bild vorab verlinkt ist -->
					<img src="{gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:graphicOverview[2]/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString}" border="0">
						<xsl:attribute name="alt"><xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:graphicOverview[2]/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString"/></xsl:attribute>
						<xsl:attribute name="title"><xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:graphicOverview[2]/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString"/></xsl:attribute>
					</img>
					<!-- Beispielbild das via upload bei geocat gespeichert ist. -->
					<!--<img src="http://geocat0i.bgdi.admin.ch/geonetwork/srv/deu/resources.get?access=public&amp;id={geonet:info/id}&amp;fname={gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:graphicOverview[2]/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString}" border="0"/>-->
					<!--	<xsl:element name="img">
								<xsl:attribute name="src">
									<xsl:value-of select="string('resources.get?access=public&amp;id=')" />
									<xsl:value-of select="geonet:info/id" />
									<xsl:value-of select="string('&amp;fname=')" />
									<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:graphicOverview[2]/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString" />
								</xsl:attribute>
								<xsl:attribute name="alt">
									<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:graphicOverview[2]/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString"/>
								</xsl:attribute>
								<xsl:attribute name="title">
									<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:graphicOverview[2]/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString"/>
								</xsl:attribute>
							</xsl:element>-->
				</td>
			</tr>
			<tr>
				<td colspan="2" class="text" bgcolor="#f1f1f1" valign="TOP">
					<xsl:text disable-output-escaping="no">&#160;</xsl:text>
				</td>
			</tr>
<!-- Kurzbeschreibung - simple-->
			<tr>
				<td colspan="2" class="text" bgcolor="silver" valign="TOP">
					<b>
						<xsl:value-of select="string($label/element[@name='gmd:abstract']/label)"/>
					</b>
				</td>
			</tr>
			<tr>
				<td colspan="2" class="text" bgcolor="#ffffff" valign="TOP">
					<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:abstract/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString"/>
					<br/><br/>
				</td>
			</tr>
			<tr>
				<td colspan="2" class="text" bgcolor="#f1f1f1" valign="TOP">
					<xsl:text disable-output-escaping="no">&#160;</xsl:text>
				</td>
			</tr>
<!-- Ausdehung - simple-->
			<tr>
				<td colspan="2" class="text" bgcolor="silver" valign="TOP">
					<b>
						<xsl:value-of select="string($label/element[@name='gmd:extent']/label)"/>
					</b>
				</td>
			</tr>
			<tr>
				<td colspan="2" class="text" bgcolor="#ffffff">
					<iframe  frameborder="no" scrolling="no" width="725" height="400" align="center" style="border-width: 0px; border-style: solid; margin:0; padding:0;">
							<xsl:attribute name="src">
								<xsl:text>http://www.stadtplan.bs.ch/geoviewer/index.php?instance=mashup_simple&amp;wgs84=</xsl:text>
								<xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_BoundingPolygon/gmd:polygon/gml:MultiSurface/gml:surfaceMember/gml:Polygon/gml:exterior/gml:LinearRing/gml:posList"/>
							</xsl:attribute>
						<p>Ihr Browser erfüllt die notwendigen Mindestanforderungen für diese Appikation nicht.</p>
					</iframe>
					<br/>
				</td>
			</tr>
			<tr>
				<td colspan="2" class="text" bgcolor="#f1f1f1" valign="TOP">
					<xsl:text disable-output-escaping="no">&#160;</xsl:text>
				</td>
			</tr>
		</table>
	</xsl:template>
<!-- Der codelist Wert (label) wird ausgewählt wenn der Eintrag code gleich dem codeListValue vom xml ist -->
	<xsl:template name="codelist">
		<xsl:param name="code"/>
		<xsl:param name="path"/>
		<xsl:value-of select="string($value/codelist[@name= $path]/entry[code = $code]/label)"/>
	</xsl:template>
</xsl:stylesheet>
