<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:java="java:org.fao.geonet.util.XslUtil"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="html" indent="yes" />

	<xsl:template match="/">
		<html>
			<head>
				<style>
					body {
						font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
						font-size: 14px;
						line-height: 1.428571429;
						color: #333333;
						background-color: #fff;
						width: 95%;
					}
					div.failure {
						border-color: lightgrey;
						border-style: dashed;
						border-width: thin;
						padding: 5px;
						background-color: #eee;
						margin-left: 10px;
						width: 90%;
					}
					div.failureReason {
						border-color: lightgrey;
						border-style: dashed;
						border-width: thin;
						padding: 5px;
						background-color: #eee;
						margin-left: 10px;
						width: 90%;
					}
					div.details {
						margin-bottom: 10px;
						margin-top: 10px;
						padding: 0 0 5px 5px;
						border-color: lightgrey;
						border-style: solid;
						border-width: thin;
					}
					div.details > a > h3 {
						margin-top: 0px;
						margin-bottom: 0px;
					}
					div.details:hover {
						background-color: #eeeeee;
					}

					.reason h1, .reason h2, .reason h3, .reason h4, .reason h5 {
					  font-size: 1em;
					}
					td.detail-link {
						font-size: 14px;
						font-weight: bolder;
					}
					#detail-container {
						float: right;
					  width: 66%
					}

					.details a {
						text-decoration: initial;
					  color: black;
					}
					.detail-content {
						text-decoration: initial;
					  color: black;
						margin-top: 1em;
					}
				</style>
				<script type="text/javascript" src="{root/gui/url}/static/lib.js" />
				<script type="text/javascript">
					showDetail = function(hash) {
						if (window.location.hash === '#'  + hash) {
							window.location.hash = '';
						} else {
							window.location.hash = hash;
						}
						doShowDetail();
					};
					doShowDetail = function() {
					$('.detail-content').hide();
						if (window.location.hash.indexOf("#detail-") === 0) {
							var detail = $('#content-' + window.location.hash.substr(1));
							detail.show();
							$('html, body').animate({
								scrollTop: detail.offset()
							}, 500);
						}
					}
					window.onload = function() {
						doShowDetail();
						$('index-container').height($(window).height());
					}
				</script>
			</head>
			<body>
				<xsl:apply-templates mode="entry" select="/root/report/allElements/record"/>
			</body>
		</html>
	</xsl:template>

	<xsl:template mode="entry" match="record">
		<div id="detail-{uuid}" class="details">
			<a href="javascript:showDetail('detail-{uuid}')">
				<h3><xsl:value-of select="uuid" /></h3>
				<div><strong>Changing Entity: </strong><xsl:value-of select="entity"/></div>
			</a>
			<div id="content-detail-{uuid}" class="detail-content" style="display:none">
				<div><strong>Valid: </strong><xsl:value-of select="validated"/></div>
				<div><strong>Published: </strong><xsl:value-of select="published"/></div>
				<div><strong>Change Date: </strong><xsl:value-of select="changedate"/></div>
				<div><strong>Change Time: </strong><xsl:value-of select="changetime"/></div>

				<h3>Violated Schematrons</h3>
				<div class="failureReason">
					<xsl:copy-of select="java:parse(failurerule)" />
				</div>
				<h3>Failure Reasons</h3>
				<xsl:copy-of select="java:parse(failurereasons)" />
			</div>
		</div>
	</xsl:template>

</xsl:stylesheet>
