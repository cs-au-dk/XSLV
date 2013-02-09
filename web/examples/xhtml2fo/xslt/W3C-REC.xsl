<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:html="http://www.w3.org/1999/xhtml">
  
  <xsl:import href="xhtml2fo.xsl"/>
  
  <xsl:output method="xml"
              version="1.0"
              encoding="utf-8"
              indent="no"/>
  
  <!--======================================================================
      Parameters
  =======================================================================-->
  
  <!-- page size -->
  <xsl:param name="page-width">auto</xsl:param>
  <xsl:param name="page-height">auto</xsl:param>
  <xsl:param name="page-margin-top">1in</xsl:param>
  <xsl:param name="page-margin-bottom">1in</xsl:param>
  <xsl:param name="page-margin-left">1in</xsl:param>
  <xsl:param name="page-margin-right">1in</xsl:param>
  
  <!-- page header and footer -->
  <xsl:param name="page-header-margin">0.5in</xsl:param>
  <xsl:param name="page-footer-margin">0.5in</xsl:param>
  <xsl:param name="title-print-in-header">true</xsl:param>
  <xsl:param name="page-number-print-in-footer">true</xsl:param>
  
  <!-- multi column -->
  <xsl:param name="column-count">1</xsl:param>
  <xsl:param name="column-gap">12pt</xsl:param>
  
  <!-- writing-mode: lr-tb | rl-tb | tb-rl -->
  <xsl:param name="writing-mode">lr-tb</xsl:param>
  
  <!-- text-align: justify | start -->
  <xsl:param name="text-align">justify</xsl:param>
  
  <!-- hyphenate: true | false -->
  <xsl:param name="hyphenate">true</xsl:param>
  
  
  <!--======================================================================
      Attribute Sets
  =======================================================================-->
  
  <!--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
       Root
  =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-->
  
  <xsl:attribute-set name="root">
    <xsl:attribute name="writing-mode"><xsl:value-of select="$writing-mode"/></xsl:attribute>
    <xsl:attribute name="hyphenate"><xsl:value-of select="$hyphenate"/></xsl:attribute>
    <xsl:attribute name="text-align"><xsl:value-of select="$text-align"/></xsl:attribute>
    <!-- specified on fo:root to change the properties' initial values -->
  </xsl:attribute-set>
  
  <xsl:attribute-set name="page">
    <xsl:attribute name="page-width"><xsl:value-of select="$page-width"/></xsl:attribute>
    <xsl:attribute name="page-height"><xsl:value-of select="$page-height"/></xsl:attribute>
    <!-- specified on fo:simple-page-master -->
  </xsl:attribute-set>
  
  <xsl:attribute-set name="body">
    <!-- specified on fo:flow's only child fo:block -->
    <xsl:attribute name="font-family">sans-serif</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="page-header">
    <!-- specified on (page-header)fo:static-content's only child fo:block -->
    <xsl:attribute name="font-size">small</xsl:attribute>
    <xsl:attribute name="text-align">center</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="page-footer">
    <!-- specified on (page-footer)fo:static-content's only child fo:block -->
    <xsl:attribute name="font-size">small</xsl:attribute>
    <xsl:attribute name="text-align">center</xsl:attribute>
  </xsl:attribute-set>
  
  <!--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
       Block-level
  =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-->
  
  <xsl:attribute-set name="h1">
    <xsl:attribute name="font-size">170%</xsl:attribute>
    <xsl:attribute name="font-weight">normal</xsl:attribute>
    <xsl:attribute name="font-family">sans-serif</xsl:attribute>
    <xsl:attribute name="color">#005A9C</xsl:attribute>
    <xsl:attribute name="text-align">start</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="h2">
    <xsl:attribute name="font-size">140%</xsl:attribute>
    <xsl:attribute name="font-weight">normal</xsl:attribute>
    <xsl:attribute name="font-family">sans-serif</xsl:attribute>
    <xsl:attribute name="color">#005A9C</xsl:attribute>
    <xsl:attribute name="text-align">start</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="h3">
    <xsl:attribute name="font-size">120%</xsl:attribute>
    <xsl:attribute name="font-weight">normal</xsl:attribute>
    <xsl:attribute name="font-family">sans-serif</xsl:attribute>
    <xsl:attribute name="color">#005A9C</xsl:attribute>
    <xsl:attribute name="text-align">start</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="h4">
    <xsl:attribute name="font-size">100%</xsl:attribute>
    <xsl:attribute name="font-weight">bold</xsl:attribute>
    <xsl:attribute name="font-family">sans-serif</xsl:attribute>
    <xsl:attribute name="color">#005A9C</xsl:attribute>
    <xsl:attribute name="text-align">start</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="h5">
    <xsl:attribute name="font-size">100%</xsl:attribute>
    <xsl:attribute name="font-weight">normal</xsl:attribute>
    <xsl:attribute name="font-style">italic</xsl:attribute>
    <xsl:attribute name="font-family">sans-serif</xsl:attribute>
    <xsl:attribute name="color">#005A9C</xsl:attribute>
    <xsl:attribute name="text-align">start</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="h6">
    <xsl:attribute name="font-size">100%</xsl:attribute>
    <xsl:attribute name="font-weight">normal</xsl:attribute>
    <xsl:attribute name="font-variant">small-caps</xsl:attribute>
    <xsl:attribute name="font-family">sans-serif</xsl:attribute>
    <xsl:attribute name="color">#005A9C</xsl:attribute>
    <xsl:attribute name="text-align">start</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="p">
  </xsl:attribute-set>
  
  <xsl:attribute-set name="blockquote">
  </xsl:attribute-set>
  
  <xsl:attribute-set name="pre">
    <xsl:attribute name="start-indent">inherited-property-value(start-indent) + 2em</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="address">
    <xsl:attribute name="font-style">italic</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="hr">
  </xsl:attribute-set>
  
  <!--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
       List
  =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-->
  <!--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
       Table
  =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-->
  <!--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
       Inline-level
  =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-->
  <!--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
       Link
  =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-->
  <!-- default attributes are defined in xhtml2fo.xsl -->
  
  
  <!--======================================================================
      misc. additional
  =======================================================================-->
  
  <!-- .hide { display: none } -->
  <xsl:template match="*[@class = 'hide']"/>
  
  <!-- div.head { margin-bottom: 1em } -->
  <xsl:template match="html:div[@class = 'head']">
    <fo:block space-after="1em">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>
  
  <!-- div.head h1 { margin-top: 2em; clear: both } -->
  <xsl:template match="html:div[@class = 'head']//html:h1">
    <fo:block xsl:use-attribute-sets="h1"
              space-before="2em" space-before.conditionality="retain">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>
  
  <!-- div.head table { margin-left: 2em; margin-top: 2em } -->
  <xsl:template match="html:div[@class = 'head']//html:table">
    <fo:table-and-caption xsl:use-attribute-sets="table-and-caption"
                          start-indent="2em" end-indent="2em">
      <xsl:call-template name="make-table-caption"/>
      <fo:table xsl:use-attribute-sets="table">
        <xsl:call-template name="process-table"/>
      </fo:table>
    </fo:table-and-caption>
  </xsl:template>
  
  <!-- div.head img { color: white; border: none } /* remove border from top image */ -->
  <xsl:template match="html:div[@class = 'head']//html:img">
    <fo:external-graphic xsl:use-attribute-sets="img"
                         color="white" border="none">
      <xsl:call-template name="process-img"/>
    </fo:external-graphic>
  </xsl:template>
  
  <!-- p.copyright { font-size: small } -->
  <xsl:template match="html:p[@class = 'copyright']">
    <fo:block xsl:use-attribute-sets="p" font-size="small">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>
  
  <!-- p.copyright small { font-size: small } -->
  <xsl:template match="html:p[@class = 'copyright']//html:small">
    <fo:inline font-size="small">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:inline>
  </xsl:template>
  
  <!-- ul.toc { list-style: none; } -->
  <xsl:template match="html:ul[@class = 'toc']/html:li">
    <fo:list-item xsl:use-attribute-sets="ul-li">
      <xsl:call-template name="process-common-attributes"/>
      <fo:list-item-label>
        <fo:block/>
      </fo:list-item-label>
      <fo:list-item-body start-indent="body-start()">
        <fo:block>
          <xsl:apply-templates/>
        </fo:block>
      </fo:list-item-body>
    </fo:list-item>
  </xsl:template>
  
</xsl:stylesheet>
