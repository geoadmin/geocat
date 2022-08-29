<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:java="java:org.fao.geonet.util.XslUtil"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="html" indent="no" />

	<xsl:template match="/">
		<xsl:variable name="parsed">
			<xsl:apply-templates mode="parse" select="/"/>
		</xsl:variable>
		<xsl:apply-templates mode="build" select="$parsed" />
	</xsl:template>

		<xsl:template mode="parse" match="@*|*|processing-instruction()|comment()">
			<xsl:copy>
				<xsl:apply-templates mode="parse" select="*|@*|text()|processing-instruction()|comment()"/>
			</xsl:copy>
		</xsl:template>

		<xsl:template mode="parse" match="failurerule | failurereasons" priority="5">
			<xsl:copy>
				<xsl:apply-templates mode="parse" select="java:parse(.)"/>
			</xsl:copy>
		</xsl:template>

	<xsl:template mode="build" match="/">
		<html>
			<head>
				<link href="../../catalog/lib/style/font-awesome/css/font-awesome.min.css" rel="stylesheet" type="text/css" />
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
						top: 4em;
						left: 66%;
						width: 20%;
						border-width: 2px;
						border-color: #3F3FA5;
						background-color: #C5D9EC;
						border-style: outset;
					}
					.help-link {
						position: fixed;
						top: 10px;
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
					form {
						border-style: solid;
						border-width: thin;
						border-color: #BDBCBC;
						border-bottom-width: 3px;
						padding: 5px;
						background-color: #EAE8E8;
					}
					form label {
						display: inline-block;
						width: 10em;
					}
					#not {
						width: 1em;
					}
					form input, form select {
						width: 50%;
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
							var failureText = el.find('.failure').text()
							var uuidText = el.find('.uuid').text();
							var text = failureText + uuidText;
							var foundSearchTerm = text.toLowerCase().indexOf(searchTerm)> -1;
							var foundRule = true;
							var foundTest = true;
							var foundEntity = true;
							var foundSource = true;
							var foundIspublished = true;

							var rule = $('#rule-select').val();
							if (rule !== '') {
								var ruleText = el.find('failurerule').text();
								foundRule = ruleText.indexOf(rule) > -1;
							}

							var test = $('#test-select').val();
							if (test !== '' &amp;&amp; test !== null) {
								var testText = el.find('.failure > .test').text()
								foundTest = testText.indexOf(test) > -1;
							}

							var entity = $('#entity-select').val();
							if (entity !== '') {
								var entityText = el.find('.entity').text()
								foundEntity = entityText.indexOf(entity) > -1;
							}

							var source = $('#source-select').val();
							if (source !== '') {
								var sourceText = el.find('.sourceName').text()
								foundSource = sourceText.indexOf(source) > -1;
							}
							var ispublished = $('#ispublished-select').val();
							if (ispublished !== '') {
								var ispublishedText = el.find('.ispublished').text()
								foundIspublished = ispublishedText.indexOf(ispublished) > -1;
							}

							var found = foundSearchTerm &amp;&amp; foundRule &amp;&amp; foundTest &amp;&amp; foundEntity &amp;&amp; foundSource &amp;&amp; foundIspublished;
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
				<div class="help-link">
					<a href="{/root/gui/locService}/admin.console#/geocat">Back</a> <br/>
					<a  href="javascript:$('#help').toggle()">Help</a>
				</div>
				<div id="help" style="display:none">
					Press a keyboard key for the following functionality:
					<ul>
						<li>j/n - show next item</li>
						<li>k/p - show previous item</li>
						<li>s - go to search box</li>
						<li>h - toggle help</li>
					</ul>
				</div>
				<form>
					<label for="search">All: </label><input id="search" class="search-form" onkeypress="" placeholder="Search"/>
					<xsl:call-template name="select">
						<xsl:with-param name="id" select="'entity-select'"/>
						<xsl:with-param name="name" select="'Entity'"/>
						<xsl:with-param name="elements" select="distinct-values(/root/report/allElements/record/entity/text())" />
					</xsl:call-template>
					<xsl:call-template name="select">
						<xsl:with-param name="id" select="'source-select'"/>
						<xsl:with-param name="name" select="'Source'"/>
						<xsl:with-param name="elements" select="distinct-values(/root/report/allElements/record/sourceName/text())" />
					</xsl:call-template>
					<xsl:call-template name="select">
						<xsl:with-param name="id" select="'rule-select'"/>
						<xsl:with-param name="name" select="'Schematron'"/>
						<xsl:with-param name="elements" select="distinct-values(/root/report/allElements/record/failurerule//div[count(./*) = 0 and normalize-space(.) != '']/text())" />
					</xsl:call-template>
					<xsl:call-template name="select">
						<xsl:with-param name="id" select="'test-select'"/>
						<xsl:with-param name="name" select="'Test'"/>
						<xsl:with-param name="elements" select="distinct-values(/root/report/allElements/record/failurereasons//div[@class = 'test']/text())" />
					</xsl:call-template>
					<xsl:variable name="booleanOpts">
						<v>true</v>
						<v>false</v>
					</xsl:variable>

					<xsl:call-template name="select">
						<xsl:with-param name="id" select="'ispublished-select'"/>
						<xsl:with-param name="name" select="'Is Published'"/>
						<xsl:with-param name="elements" select="distinct-values($booleanOpts/v/text())"/>
					</xsl:call-template>
					<div><label for="id">Hide matches:</label><input type="checkbox" id="not" class="search-form" onchange="doSearch()"/></div>
					<div>
						<button class="search-form" onmouseup="doSearch()">Search</button>
					</div>
				</form>
				<div id="found">Found: <span id="count"><xsl:value-of select="count(/root/report/allElements/record)"/></span></div>
				<xsl:apply-templates mode="entry" select="/root/report/allElements/record"/>
			</body>
		</html>
	</xsl:template>

	<xsl:template name="select" >
		<xsl:param name="id"/>
		<xsl:param name="name"/>
		<xsl:param name="elements"/>

		<div>
			<label for="{$id}"><xsl:value-of select="$name"/>: </label>
			<select id="{$id}" class="search-form">
				<xsl:choose>
					<xsl:when test="count($elements) > 0">
						<xsl:attribute name="onchange">doSearch()</xsl:attribute>
						<option value=""></option>
						<xsl:for-each select="$elements">
							<option value="{.}"><xsl:value-of select="."/></option>
						</xsl:for-each>
					</xsl:when>
					<xsl:otherwise>
						<xsl:for-each select="$elements[1]">
							<option value=""><xsl:value-of select="."/></option>
						</xsl:for-each>
					</xsl:otherwise>
				</xsl:choose>
			</select>
		</div>
	</xsl:template>
	<xsl:template mode="entry" match="record">
		<div id="detail-{uuid}" class="details">

			<a href="javascript:showDetail('detail-{uuid}')" style="text-decoration: none">

				<xsl:variable name="validIcon" select="if (published = 'true') then 'fa-unlock' else 'fa-lock'" />

				<xsl:if test="logo">
					<img src="{/root/gui/url}{logo}" title="{sourceName}" style="max-height: 50px; padding: 5px 10px; display: inline"/>
				</xsl:if>
				<div style="display: inline-block">
					<i class="fa {$validIcon}" style="display: inline; padding-right:5px"/>
					<h3 class="uuid" style="display: inline">
						<xsl:value-of select="uuid" /></h3>
					<div><strong>Changing Entity: </strong><span class="entity"><xsl:value-of select="entity"/></span></div>
					<xsl:for-each select="distinct-values(failurereasons/div//div[@class = 'test'])">
						<div><strong>Test: </strong><xsl:value-of select="."/></div>
					</xsl:for-each>
				</div>
			</a>
			<div id="content-detail-{uuid}" class="detail-content" style="display:none">
				<div class="detail-links">
					<a href="{/root/gui/locService}/md.viewer#/full_view/{uuid}">View</a>
					<a href="{/root/gui/locService}/xml.metadata.get?uuid={uuid}">XML</a>
				</div>
				<div class="sourceName"><strong>Source Name: </strong><xsl:value-of select="sourceName"/></div>
				<div><strong>Valid: </strong><xsl:value-of select="validated"/></div>
				<div><strong>Published: </strong><span class="ispublished"><xsl:value-of select="published"/></span></div>
				<div><strong>Change Date: </strong><xsl:value-of select="changedate"/></div>
				<div><strong>Change Time: </strong><xsl:value-of select="changetime"/></div>

				<h3>Violated Schematrons</h3>
				<div class="failureReason">
					<xsl:copy-of select="failurerule" />
				</div>
				<h3>Failure Reasons</h3>

				<xsl:copy-of select="failurereasons" />
			</div>
		</div>
	</xsl:template>

</xsl:stylesheet>
