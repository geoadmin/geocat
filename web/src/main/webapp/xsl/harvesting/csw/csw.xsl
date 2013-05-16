<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->
	<!-- === editPanel -->
	<!-- ============================================================================================= -->

	<xsl:template name="editPanel-CSW">
		<div id="csw.editPanel">
            <xsl:call-template name="ownerGroup-CSW"/>
            <div class="dots"/>
			<xsl:call-template name="site-CSW"/>
			<div class="dots"/>
			<xsl:call-template name="search-CSW"/>
			<div class="dots"/>
			<xsl:call-template name="options-CSW"/>
			<div class="dots"/>
			<xsl:call-template name="content-CSW"/>
			<div class="dots"/>
			<xsl:call-template name="privileges">
				<xsl:with-param name="type" select="'csw'"/>
			</xsl:call-template>
			<div class="dots"/>
			<xsl:call-template name="categories-CSW"/>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->

    <xsl:template name="ownerGroup-CSW">
        <table border="0">
            <tr>
                <td class="padded"><xsl:value-of select="/root/gui/harvesting/selectownergroup"/></td>
                <td class="padded"><select id="csw.ownerGroup" class="content"/></td>
            </tr>
            <tr>
                <td colspan="2">&#xA0;</td>
            </tr>
        </table>
    </xsl:template>

    <!-- ============================================================================================= -->

	<xsl:template name="site-CSW">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/site"/></h1>
	
		<table border="0">
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/name"/></td>
				<td class="padded"><input id="csw.name" class="content" type="text" value="" size="30"/></td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/capabUrl"/></td>
				<td class="padded"><input id="csw.capabUrl" class="content" type="text" value="" size="30"/></td>
			</tr>

            <tr>
                <script language="javascript">
                function updateTextFieldOutputSchema(string)
                {
                   $('csw.outputSchema').value =  string;
                }
                </script>
                <td class="padded"><xsl:value-of select="/root/gui/harvesting/outputSchema"/></td>
                <td class="padded">
                    <input type="text" class="content" size="70" name="outputSchema" id="csw.outputSchema" value="http://www.geocat.ch/2008/che" />
                    <p>Examples : </p>
                    <ul>
                      <li><a href="javascript:updateTextFieldOutputSchema('http://www.opengis.net/cat/csw/2.0.2')">http://www.opengis.net/cat/csw/2.0.2' for Dublin Core</a></li>
                      <li><a href="javascript:updateTextFieldOutputSchema('http://www.isotc211.org/2005/gmd')">http://www.isotc211.org/2005/gmd' for ISO19139</a></li>
                      <li><a href="javascript:updateTextFieldOutputSchema('http://www.geocat.ch/2008/che')">http://www.geocat.ch/2008/che' for ISO19139-CHE</a></li>
                      <!--  issue #133730 : deactivated for now : GC subsystem is not able to handle GM03 MD yet -->
                      <!-- <li><a href="javascript:updateTextFieldOutputSchema('http://www.geocat.ch/2008/gm03_2')">'http://www.geocat.ch/2008/gm03_2' for GM03_2</a></li>   -->
                    </ul>
                </td>
            </tr>

			<tr>
				<td class="padded" valign="bottom"><xsl:value-of select="/root/gui/harvesting/icon"/></td>
				<td class="padded">
					<select id="csw.icon" class="content" name="icon" size="1"/>
					&#xA0;
					<img id="csw.icon.image" src="" alt="" class="logo"/>
				</td>
			</tr>
			
			<xsl:call-template name="useAccount">
				<xsl:with-param name="type" select="'csw'"/>
			</xsl:call-template>
		
						<tr>
				<td class="padded"><label for="csw.rejectDuplicateResource"><xsl:value-of select="/root/gui/harvesting/rejectDuplicateResource"/></label></td>
				<td class="padded"><input id="csw.rejectDuplicateResource" type="checkbox" value=""/></td>
			</tr>			
		</table>
	</xsl:template>
	
	<!-- ============================================================================================= -->
	
	<xsl:template name="search-CSW">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/search"/></h1>
		
		<div id="csw.searches"/>
		
		<button id="csw.addSearch" class="content" onclick="harvesting.csw.addSearchRow()">
			<xsl:value-of select="/root/gui/harvesting/add"/>
		</button>
	</xsl:template>

	<!-- ============================================================================================= -->
	
	<xsl:template name="options-CSW">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/options"/></h1>
		<xsl:call-template name="schedule-widget">
			<xsl:with-param name="type">csw</xsl:with-param>
		</xsl:call-template>
		</xsl:template>
	
	<!-- ============================================================================================= -->

	<xsl:template name="content-CSW">
	<div>
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/content"/></h1>

		<table border="0">
             <!-- UNUSED -->
			<tr style="display:none;">
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/importxslt"/></td>
				<td class="padded">
					&#160;
					<select id="csw.importxslt" class="content" name="importxslt" size="1"/>
				</td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/validate"/></td>
				<td class="padded">
					<select id="csw.validate" class="content" name="validate" size="1">
						<option value="NOVALIDATION" selected="selected"><xsl:value-of select="/root/gui/harvesting/noValidation"/></option>
						<option value="XSDVALIDATION"><xsl:value-of select="/root/gui/harvesting/xsdValidation"/></option>
						<option value="SCHEMATRONVALIDATION"><xsl:value-of select="/root/gui/harvesting/schematronValidation"/></option>
					</select>
				</td>
			</tr>
		
		</table>
	</div>
	</xsl:template>
	
	<!-- ============================================================================================= -->

	<xsl:template name="categories-CSW">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/categories"/></h1>
		
		<select id="csw.categories" class="content" size="8" multiple="on"/>
	</xsl:template>
	
	<!-- ============================================================================================= -->
	
</xsl:stylesheet>
