<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:sc="scaling">

	<xsl:include href="main.xsl" />

	<!-- page content -->
	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title"
				select="/root/gui/strings/metadataschemaautoLoad" />
			<xsl:with-param name="content">
				<xsl:call-template name="form" />
			</xsl:with-param>
			<xsl:with-param name="buttons">
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<!-- ================================================================== -->

	<xsl:template name="form">
	<link rel="stylesheet"
		href="http://code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.css" />
	<script src="http://code.jquery.com/jquery-1.9.1.js"></script>
	<script>
		var $j = jQuery.noConflict();
	</script>
	<script src="http://code.jquery.com/ui/1.10.3/jquery-ui.js"></script>
 <style>
	.ui-autocomplete-loading {
	background: white url('images/ui-anim_basic_16x16.gif') right center no-repeat;
	}
	#city { width: 25em; }
</style>
		<h3>
			<xsl:value-of select="/root/gui/strings/metadataschemaValidate" />
		</h3>
		<p>
			<xsl:value-of select="/root/gui/strings/metadataschemaValidateDes" />
		</p>
		<div class="metadataValidation">
			<form method="post">
				<input type="hidden" name="action" value="add" />
				<div>
					<label>
						<xsl:value-of select="/root/gui/strings/xpathschematron" />
						:
					</label>
					<select name="schematron" autofocus="autofocus" required="true">
						<xsl:for-each select="/root/schematron/schematron">
							<option value="{@id}">
								[
								<xsl:value-of select="@isoschema" />
								]
								<xsl:value-of select="@file" />
							</option>
						</xsl:for-each>
					</select>
				</div>
				<div>
					<label>
						<xsl:value-of select="/root/gui/strings/xpathlabel" />
						:
					</label>
					<select name="type" id="xpathtype" placeholder="{/root/gui/strings/xpathlabel}"
						required="true">
						<option value="KEYWORD">
							<xsl:value-of select="/root/gui/strings/capitalKeyword" />
						</option>
						<option value="GROUP">
							<xsl:value-of select="/root/gui/strings/group" />
						</option>
					</select>
				</div>
				<div>
					<label>
						<xsl:value-of select="/root/gui/strings/xpathvalue" />
						:
					</label>
					<input style="display:none" id="xpath" type="text" name="value" required="true"
						placeholder="{/root/gui/strings/xpathvalue}" />
					<input type="text" id="keyword" 
						placeholder="{/root/gui/strings/xpathvalue}" />
					<input style="display:none" type="text" id="group" 
						placeholder="{/root/gui/strings/xpathvalue}" />
				</div>
				<input type="submit" />
			</form>
			<div class="xpath list">

				<xsl:if test="count(/root/schematron/criteria) > 0">
					<table>
						<tr>
							<th>
								<xsl:value-of select="/root/gui/strings/xpathrequired" />
							</th>
							<th>
								<xsl:value-of select="/root/gui/strings/xpathschematron" />
							</th>
							<th>
								<xsl:value-of select="/root/gui/strings/xpathlabel" />
							</th>
							<th>
								<xsl:value-of select="/root/gui/strings/xpathvalue" />
							</th>
							<td>
							</td>
						</tr>
						<xsl:for-each select="/root/schematron/criteria">
							<tr>
								<td>
									<xsl:choose>
										<xsl:when test="schematron/@required = 'true'">
											<span class="required" />
										</xsl:when>
										<xsl:otherwise>
											<span class="optional" />
										</xsl:otherwise>
									</xsl:choose>
								</td>
								<td>
									[
									<xsl:value-of select="schematron/@isoschema" />
									]
									<xsl:value-of select="schematron/@file" />
								</td>
								<td>
									<xsl:value-of select="@type" />
								</td>
								<td>
									<xsl:value-of select="@value" />
								</td>
								<td>
									<a class="remove" href="#">
										<xsl:attribute name="onclick">javascript:removeItem(<xsl:value-of
											select="@id" />)</xsl:attribute>
									</a>
								</td>
							</tr>
						</xsl:for-each>
					</table>
				</xsl:if>
			</div>
		</div>


		<script type="text/javascript">
			function removeItem(id) {
				if(confirm('<xsl:value-of select="/root/gui/strings/xpathconfirm" />') == true) {
					new Ajax.Request(
						'',
						{
							method: 'post',
							parameters: {action: 'delete', id: id},
							onComplete: function(){location.reload();}});
					}
			}
		  $j(function() {
		  	
		  	$j("#xpathtype").change(function() {
		  		if($j("#xpathtype").val() == "KEYWORD") {
		  			$j("#group").hide();
		  			$j("#keyword").show();
		  		} else {
		  			$j("#keyword").hide();
		  			$j("#group").show();
		  		}
		  	});
		    $j( "#keyword" ).change(function( ) {
		  		$j("#xpath").val($j("#keyword").val());
		      });
		    $j( "#keyword" ).autocomplete({
		      source: function( request, response ) {
		        $j.get("xml.search.keywords",
		          {
		            pNewSearch: true,
		            pTypeSearch:1,
		            pKeyword: request.term
		          },
		          function( data ) {
		          	var res = [];
		          	$j.each(data.getElementsByTagName("keyword"), 
		          		function(index, item) {
		          				var value = item.getElementsByTagName("value")[0];
		          				res.push({
					                label: value.innerText || value.textContent,
					                term: value.innerText || value.textContent,
					                field: value.innerText || value.textContent,
					                  value: value.innerText || value.textContent
					              }
					              );
		          		});
		          	response(res);
		          });
		      },
		      minLength: 2,
		      select: function( event, ui ) {
		  		$j("#xpath").val(ui.item.value);
		      },
		      open: function() {
		        $j( this ).removeClass( "ui-corner-all" ).addClass( "ui-corner-top" );
		      },
		      close: function() {
		        $j( this ).removeClass( "ui-corner-top" ).addClass( "ui-corner-all" );
		      }
		    });
		    
		    
		    $j( "#group" ).change(function( ) {
		  		$j("#xpath").val($j("#group").val());
		      });
		    $j( "#group" ).autocomplete({
		      source: function( request, response ) {
		        $j.get("xml.info",
		          {
		            type: 'groups'
		          },
		          function( data ) {
		          console.log(data);
		          	var res = [];
		          	$j.each(data.getElementsByTagName("group"), 
		          		function(index, item) {
		          				if(item.getAttribute("id") != null) {
			          				var value = item.getElementsByTagName("name")[0];
			          				var val = value.innerText || value.textContent;
			          				if(val.startsWith($j("#group").val())) {
				          				res.push({
							                 label: val,
							                 value: item.getAttribute("id")
							              	}
							              );
						              }
					             }
		          		});
		          	response(res);
		          });
		      },
		      minLength: 2,
		      select: function( event, ui ) {
		  		$j("#xpath").val(ui.item.value);
		      },
		      open: function() {
		        $j( this ).removeClass( "ui-corner-all" ).addClass( "ui-corner-top" );
		      },
		      close: function() {
		        $j( this ).removeClass( "ui-corner-top" ).addClass( "ui-corner-all" );
		      }
		    });
		  });
	</script>

	</xsl:template>

	<!-- ================================================================== -->

</xsl:stylesheet>
