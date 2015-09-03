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
					.detail-links a {
						text-decoration: underline;
						color: blue;
						padding-right: 10px;
					}
					#help {
						padding-left: 5px;
						padding-right: 5px;
						position: fixed;
						top: 2em;
						left: 66%;
						width: 20%;
						border-width: 2px;
						border-color: #3F3FA5;
						background-color: #C5D9EC;
						border-style: outset;
					}
					.help-link {
						position: fixed;
						top: 0;
					left: 66%;
					}
					.search-form {
						margin-left: 5px;
						margin-top: 5px;
					}
					#found {
						margin-top: 10px;
					}
					#count {
						font-weight: bold;
					}
					.test {
						font-weight: bold;
					}
					.xpath {
						font-family: monospace;
						font-size: .9em;
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
							window.scroll(document, detail.parent().offset().top - 40)
						} else {
							$('#search').focus();
						}
					}
					next = function(count) {
						count = count === undefined ? 0 : count;
						var nextEl;
						if (window.location.hash.indexOf("#detail-") === 0) {
							nextEl = $(window.location.hash).nextAll(':visible').first();
							if (nextEl.length === 0) {
								nextEl = $('.details:visible').first();
							}
						} else {
							nextEl = $('.details:visible').first();
						}

						window.location.hash = nextEl.attr('id')
						doShowDetail();
					}
					prev = function(count) {
						count = count === undefined ? 0 : count;
						var nextEl;
						if (window.location.hash.indexOf("#detail-") === 0) {
							nextEl = $(window.location.hash).prevAll(':visible').first();
							if (nextEl.length === 0 || !nextEl.attr('id') || nextEl.attr('id').indexOf("detail") === -1) {
								nextEl = $('.details:visible').last();
							}
						} else {
							nextEl = $('.details:visible').last();
						}

						window.location.hash = nextEl.attr('id')
						doShowDetail();
					}
					window.onload = function() {
						doShowDetail();
						$('index-container').height($(window).height());

						document.onkeypress = function(e){
							var code = e.keyCode;
							if (code === 110 || code === 106) {
								next();
							} else if (code === 112 || code == 107) {
								prev();
							} else if (code === 115) {
								window.location.hash = "";
								$('#search').focus();
							} else if (code === 104) {
								$('#help').toggle()
							}
						};

						$("#search").keypress(searchInputKeyListener);
						$("#not").keypress(searchInputKeyListener);
						$("button.search-form").keypress(function(e){
							if(e.keyCode === 13 || e.keyCode === 32) {
								doSearch();
							}
						});
					}
					searchInputKeyListener = function(e){
						e.stopPropagation();
						var code = e.keyCode;

						if (code === 13) {
							doSearch();
						}
					}
					doSearch = function() {
						var not = $('#not').is(':checked');
						var searchTerm = $('#search').val().toLowerCase();
						var details = $('.details');
						var count = 0;
						details.each(function(idx, htmlEl){
							var el = $(htmlEl);
							var found = el.find('.failure, .rule').text().toLowerCase().indexOf(searchTerm) > -1;
							var match = (found &amp;&amp; !not) || (!found &amp;&amp; not);
							el.toggle(match)
							if (match) {
								count ++;
							}
						});
						$('#count').text(count);
					}
				</script>
			</head>
			<body>
				<a class="help-link" href="javascript:$('#help').toggle()">Help</a>
				<div id="help" style="display:none">
					Press a keyboard key for the following functionality:
					<ul>
						<li>j/n - show next item</li>
						<li>k/p - show previous item</li>
						<li>s - go to search box</li>
						<li>h - toggle help</li>
					</ul>
				</div>
				<input id="search" class="search-form" onkeypress="" placeholder="Search"/>
				<input type="checkbox" id="not" class="search-form">Not</input>
				<div>
					<button class="search-form" onmouseup="doSearch()">Search</button>
				</div>
				<div id="found">Found: <span id="count"><xsl:value-of select="count(/root/report/allElements/record)"/></span></div>
				<xsl:apply-templates mode="entry" select="/root/report/allElements/record"/>
			</body>
		</html>
	</xsl:template>

	<xsl:template mode="entry" match="record">
		<div id="detail-{uuid}" class="details">
			<xsl:variable name="rule" select="java:parse(failurerule)" />
			<xsl:variable name="failureReason" select="java:parse(failurereasons)" />

			<a href="javascript:showDetail('detail-{uuid}')">
				<h3><xsl:value-of select="uuid" /></h3>
				<div><strong>Changing Entity: </strong><xsl:value-of select="entity"/></div>
				<xsl:for-each select="$failureReason/div//div[@class = 'test']">
					<div><strong>Test: </strong><xsl:value-of select="."/></div>
				</xsl:for-each>
			</a>
			<div id="content-detail-{uuid}" class="detail-content" style="display:none">
				<div class="detail-links">
					<a href="{/root/gui/locService}/md.viewer#/full_view/{uuid}">View</a>
					<a href="{/root/gui/locService}/xml.metadata.get?uuid={uuid}">XML</a>
				</div>
				<div><strong>Valid: </strong><xsl:value-of select="validated"/></div>
				<div><strong>Published: </strong><xsl:value-of select="published"/></div>
				<div><strong>Change Date: </strong><xsl:value-of select="changedate"/></div>
				<div><strong>Change Time: </strong><xsl:value-of select="changetime"/></div>

				<h3>Violated Schematrons</h3>
				<div class="failureReason">
					<xsl:copy-of select="$rule" />
				</div>
				<h3>Failure Reasons</h3>
				<xsl:copy-of select="$failureReason" />
			</div>
		</div>
	</xsl:template>

</xsl:stylesheet>
