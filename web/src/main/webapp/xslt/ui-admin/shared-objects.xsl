<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:include href="../base-layout.xsl"/>

  <xsl:template mode="content" match="/">
    <div class="container" data-ng-controller="SharedObjects" data-ng-show="authenticated" data-ng-view="">
    </div>
    
    <!-- Make this a directive TODO -->
    <!--<div ng-include="'{$uiResourcesPath}geocat-shared-objects/index.html'">-->
    <!--</div>-->
  </xsl:template>

</xsl:stylesheet>
