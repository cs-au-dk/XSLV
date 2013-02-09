<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:html="http://www.w3.org/1999/xhtml">
  
  <xsl:import href="xhtml2fo.xsl"/>
  
  <!--======================================================================
      Parameters
  =======================================================================-->
  
  <!-- page size -->
  <xsl:param name="page-width">auto</xsl:param>
  <xsl:param name="page-height">auto</xsl:param>
  <xsl:param name="page-margin-top">2cm</xsl:param>
  <xsl:param name="page-margin-bottom">2cm</xsl:param>
  <xsl:param name="page-margin-left">2cm</xsl:param>
  <xsl:param name="page-margin-right">2cm</xsl:param>
  
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
  <xsl:param name="text-align">start</xsl:param>
  
  <!-- hyphenate: true | false -->
  <xsl:param name="hyphenate">false</xsl:param>
  
  
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
    <xsl:attribute name="background-color">rgb(249, 254,226)</xsl:attribute>
    <xsl:attribute name="font-size">11pt</xsl:attribute>
    <xsl:attribute name="padding-start">6pt</xsl:attribute>
    <xsl:attribute name="padding-end">6pt</xsl:attribute>
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
       Blocks
  =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-->
  
  <!-- Headings -->

  <!-- for top document title -->
  <xsl:attribute-set name="h1.doctitle" use-attribute-sets="h1">
    <xsl:attribute name="font-size">18pt</xsl:attribute>
    <xsl:attribute name="font-family">sans-serif</xsl:attribute>
    <xsl:attribute name="background-color">rgb(51,153,153)</xsl:attribute>
    <xsl:attribute name="color">rgb(255,255,255)</xsl:attribute>
    <xsl:attribute name="text-align">center</xsl:attribute>
    <xsl:attribute name="margin-left">20%</xsl:attribute>
    <xsl:attribute name="margin-right">20%</xsl:attribute>
    <xsl:attribute name="padding">10pt 0pt 10pt 0pt</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="h1">
    <xsl:attribute name="font-size">20pt</xsl:attribute>
    <xsl:attribute name="font-weight">900</xsl:attribute>
    <xsl:attribute name="color">#000000</xsl:attribute>
    <xsl:attribute name="text-decoration">underline</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="h2">
    <xsl:attribute name="font-size">14pt</xsl:attribute>
    <xsl:attribute name="color">#000000</xsl:attribute>
    <xsl:attribute name="font-weight">bold</xsl:attribute>
    <xsl:attribute name="line-height">15pt</xsl:attribute>
    <xsl:attribute name="border-style">double</xsl:attribute>
    <xsl:attribute name="border-width">3px</xsl:attribute>
    <xsl:attribute name="padding">3px</xsl:attribute>
    <xsl:attribute name="background-color">rgb(222,252,182)</xsl:attribute>
    <xsl:attribute name="space-before">30px</xsl:attribute>
    <xsl:attribute name="space-after">20px</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="h3">
    <xsl:attribute name="font-size">12pt</xsl:attribute>
    <xsl:attribute name="font-weight">600</xsl:attribute>
    <xsl:attribute name="color">#000000</xsl:attribute>
    <xsl:attribute name="line-height">11pt</xsl:attribute>
    <xsl:attribute name="space-before">15px</xsl:attribute>
    <xsl:attribute name="space-after">0px</xsl:attribute>
    <xsl:attribute name="padding-before">5px</xsl:attribute>
    <xsl:attribute name="padding-after">2px</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="h4">
    <xsl:attribute name="font-size">10pt</xsl:attribute>
    <xsl:attribute name="font-weight">600</xsl:attribute>
    <xsl:attribute name="color">#000000</xsl:attribute>
    <xsl:attribute name="line-height">11pt</xsl:attribute>
    <xsl:attribute name="space-before">0px</xsl:attribute>
    <xsl:attribute name="space-after">0px</xsl:attribute>
    <xsl:attribute name="padding-before">5px</xsl:attribute>
    <xsl:attribute name="padding-after">2px</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="h5">
    <xsl:attribute name="font-size">8pt</xsl:attribute>
    <xsl:attribute name="font-weight">normal</xsl:attribute>
    <xsl:attribute name="color">#000000</xsl:attribute>
    <xsl:attribute name="line-height">10pt</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="h6">
    <xsl:attribute name="color">#000000</xsl:attribute>
    <xsl:attribute name="font-size">8pt</xsl:attribute>
    <xsl:attribute name="font-weight">normal</xsl:attribute>
    <xsl:attribute name="line-height">10pt</xsl:attribute>
  </xsl:attribute-set>
  
  
  <!-- Divs -->
  
  <xsl:attribute-set name="div.tips">
    <xsl:attribute name="border-style">dotted</xsl:attribute>
    <xsl:attribute name="border-width">1px</xsl:attribute>
    <xsl:attribute name="border-color">teal</xsl:attribute>
    <xsl:attribute name="padding">5px</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="div.content">
    <xsl:attribute name="padding">0in</xsl:attribute>
    <xsl:attribute name="space-before">0.5em</xsl:attribute>
    <xsl:attribute name="space-after">0.5em</xsl:attribute>
    <xsl:attribute name="text-align">center</xsl:attribute>
    <xsl:attribute name="font-size">larger</xsl:attribute>
    <xsl:attribute name="line-height">140%</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="div.side-title">
    <xsl:attribute name="start-indent">10%</xsl:attribute>
    <xsl:attribute name="end-indent">10%</xsl:attribute>
    <xsl:attribute name="font-size">larger</xsl:attribute>
    <xsl:attribute name="font-weight">800</xsl:attribute>
    <xsl:attribute name="line-height">130%</xsl:attribute>
    <xsl:attribute name="background-color">#66cc99</xsl:attribute>
    <xsl:attribute name="text-align">center</xsl:attribute>
    <xsl:attribute name="border-style">solid</xsl:attribute>
    <xsl:attribute name="border-width">1px</xsl:attribute>
    <xsl:attribute name="padding">3px</xsl:attribute>
    <xsl:attribute name="space-before">10px</xsl:attribute>
    <xsl:attribute name="space-after">20px</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="div.program-block">
    <xsl:attribute name="linefeed-treatment">preserve</xsl:attribute>
    <xsl:attribute name="white-space-collapse">false</xsl:attribute>
    <xsl:attribute name="white-space-treatment">preserve</xsl:attribute>
    <xsl:attribute name="wrap-option">wrap</xsl:attribute>
    <xsl:attribute name="color">navy</xsl:attribute>
    <xsl:attribute name="font-family">monospace</xsl:attribute>
    <xsl:attribute name="font-size">10pt</xsl:attribute>
    <xsl:attribute name="line-height">120%</xsl:attribute>
    <xsl:attribute name="space-before">10px</xsl:attribute>
    <xsl:attribute name="space-after">20px</xsl:attribute>
    <xsl:attribute name="padding">0.5em</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="div.top">
    <xsl:attribute name="text-align">center</xsl:attribute>
    <xsl:attribute name="padding-after">10pt</xsl:attribute>
    <xsl:attribute name="space-before">10pt</xsl:attribute>
    <xsl:attribute name="space-after">10pt</xsl:attribute>
    <xsl:attribute name="start-indent">10%</xsl:attribute>
    <xsl:attribute name="end-indent">10%</xsl:attribute>
  </xsl:attribute-set>
  
  <!-- for top document paragraph -->
  <xsl:attribute-set name="p.top">
    <xsl:attribute name="text-align">center</xsl:attribute>
    <xsl:attribute name="start-indent">10%</xsl:attribute>
    <xsl:attribute name="end-indent">10%</xsl:attribute>
  </xsl:attribute-set>
  
  <!--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
       Lists
  =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-->

  <xsl:attribute-set name="ul.tips-level1">
    <xsl:attribute name="space-before">3pt</xsl:attribute>
    <xsl:attribute name="start-indente">3%</xsl:attribute>
    <xsl:attribute name="font-size">11pt</xsl:attribute>
    <xsl:attribute name="line-height">130%</xsl:attribute>
  </xsl:attribute-set>
  
 
  <!--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
       Tables
  =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-->
  
  <xsl:attribute-set name="table">
    <xsl:attribute name="border-style">solid</xsl:attribute>
    <xsl:attribute name="border-width">2px</xsl:attribute>
    <xsl:attribute name="border-collapse">collapse</xsl:attribute>
    <xsl:attribute name="border-color">black</xsl:attribute>
    <xsl:attribute name="empty-cells">show</xsl:attribute>
    <xsl:attribute name="space-before">6pt</xsl:attribute>
    <xsl:attribute name="space-after">6pt</xsl:attribute>
    <xsl:attribute name="border-spacing">0pt</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="th">
    <xsl:attribute name="border-style">solid</xsl:attribute>
    <xsl:attribute name="border-width">1px</xsl:attribute>
    <xsl:attribute name="border-color">black</xsl:attribute>
    <xsl:attribute name="font-size">11pt</xsl:attribute>
    <xsl:attribute name="padding">3px</xsl:attribute>
    <xsl:attribute name="background-color">#ccffcc</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="td">
    <xsl:attribute name="border-style">solid</xsl:attribute>
    <xsl:attribute name="border-width">1px</xsl:attribute>
    <xsl:attribute name="border-color">black</xsl:attribute>
    <xsl:attribute name="font-size">11pt</xsl:attribute>
    <xsl:attribute name="padding">3px</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="td.no-conf" use-attribute-sets="td">
    <xsl:attribute name="text-align">center</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="table.fospec" use-attribute-sets="table">
    <xsl:attribute name="table-layout">fixed</xsl:attribute>
    <xsl:attribute name="inline-progression-dimension">100%</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="col.fo-spec" use-attribute-sets="table-column">
    <xsl:attribute name="column-width">7.5cm</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="col.fo-conf" use-attribute-sets="table-column">
    <xsl:attribute name="column-width">2cm</xsl:attribute>
    <xsl:attribute name="text-align">start</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="col.fo-imp" use-attribute-sets="table-column">
    <xsl:attribute name="column-width">1.5cm</xsl:attribute>
    <xsl:attribute name="text-align">center</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="col.fo-etc" use-attribute-sets="table-column">
    <xsl:attribute name="column-width">9.5cm</xsl:attribute>
  </xsl:attribute-set>
  
  <!--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
       Inlines
  =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-->
  
  <xsl:attribute-set name="span.tip-title">
    <xsl:attribute name="font-size">11pt</xsl:attribute>
    <xsl:attribute name="font-weight">700</xsl:attribute>
    <xsl:attribute name="text-decoration">underline</xsl:attribute>
  </xsl:attribute-set>  
  
  <xsl:attribute-set name="span.char-disp">
    <xsl:attribute name="border-style">solid</xsl:attribute>
    <xsl:attribute name="border-width">2px</xsl:attribute>
    <xsl:attribute name="border-color">black</xsl:attribute>
    <xsl:attribute name="font-size">11pt</xsl:attribute>
    <xsl:attribute name="font-weight">500</xsl:attribute>
    <xsl:attribute name="background-color">white</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="span.notation">
    <xsl:attribute name="color">red</xsl:attribute>
    <xsl:attribute name="vertical-align">super</xsl:attribute>
    <xsl:attribute name="font-size">8pt</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="span.yet">
    <xsl:attribute name="color">blue</xsl:attribute>
    <xsl:attribute name="font-weight">bold</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="span.new">
    <xsl:attribute name="color">navy</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="span.old">
    <xsl:attribute name="color">red</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="span.list-item">
    <xsl:attribute name="font-weight">bold</xsl:attribute>
  </xsl:attribute-set>
  
  <!--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
       Images
  =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-->
  
  <xsl:attribute-set name="img">
    <xsl:attribute name="content-width">75%</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="img.inline" use-attribute-sets="img">
    <xsl:attribute name="vertical-align">bottom</xsl:attribute>
    <xsl:attribute name="padding-start">5px</xsl:attribute>
    <xsl:attribute name="padding-end">5px</xsl:attribute>
    <xsl:attribute name="padding-before">10px</xsl:attribute>
    <xsl:attribute name="padding-bottom">0px</xsl:attribute>
    <xsl:attribute name="border">none</xsl:attribute>
    <xsl:attribute name="margin">2px</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="img.chapttl" use-attribute-sets="img">
    <xsl:attribute name="vertical-align">bottom</xsl:attribute>
    <xsl:attribute name="space-end">5pt</xsl:attribute>
  </xsl:attribute-set>
  
  
  <!--======================================================================
      Templates
  =======================================================================-->
  
  <xsl:template match="html:h1[@class = 'doctitle']">
    <fo:block xsl:use-attribute-sets="h1.doctitle">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>
  
  <xsl:template match="html:div[@class = 'tips']">
    <fo:block xsl:use-attribute-sets="div.tips">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>
  
  <xsl:template match="html:div[@class = 'content']">
    <fo:block xsl:use-attribute-sets="div.content">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>
  
  <xsl:template match="html:div[@class = 'side-title']">
    <fo:block xsl:use-attribute-sets="div.side-title">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>
  
  <xsl:template match="html:div[@class = 'program-block']">
    <fo:block xsl:use-attribute-sets="div.program-block">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>
  
  <xsl:template match="html:div[@class = 'top']">
    <fo:block xsl:use-attribute-sets="div.top">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>
  
  <xsl:template match="html:p[@class = 'top']">
    <fo:block xsl:use-attribute-sets="p.top">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>
  
  <xsl:template match="html:td[@class = 'no-conf']">
    <fo:table-cell xsl:use-attribute-sets="td.no-conf">
      <xsl:call-template name="process-table-cell"/>
    </fo:table-cell>
  </xsl:template>
  
  <xsl:template match="html:table[@class = 'fospec']">
    <fo:table-and-caption xsl:use-attribute-sets="table-and-caption">
      <xsl:call-template name="make-table-caption"/>
      <fo:table xsl:use-attribute-sets="table.fospec">
        <xsl:call-template name="process-table"/>
      </fo:table>
    </fo:table-and-caption>
  </xsl:template>
  
  <xsl:template match="html:col[@class = 'fo-spec']">
    <fo:block xsl:use-attribute-sets="col.fo-spec">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>
  
  <xsl:template match="html:col[@class = 'fo-conf']">
    <fo:block xsl:use-attribute-sets="col.fo-conf">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>
  
  <xsl:template match="html:col[@class = 'fo-imp']">
    <fo:block xsl:use-attribute-sets="col.fo-imp">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>
  
  <xsl:template match="html:col[@class = 'fo-etc']">
    <fo:block xsl:use-attribute-sets="col.fo-etc">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>
  
  <xsl:template match="html:span[@class = 'tip-title']">
    <fo:inline xsl:use-attribute-sets="span.tip-title">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:inline>
  </xsl:template>
  
  <xsl:template match="html:span[@class = 'char-disp']">
    <fo:inline xsl:use-attribute-sets="span.char-disp">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:inline>
  </xsl:template>
  
  <xsl:template match="html:span[@class = 'notation']">
    <fo:inline xsl:use-attribute-sets="span.notation">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:inline>
  </xsl:template>
  
  <xsl:template match="html:span[@class = 'yet']">
    <fo:inline xsl:use-attribute-sets="span.yet">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:inline>
  </xsl:template>
  
  <xsl:template match="html:span[@class = 'new']">
    <fo:inline xsl:use-attribute-sets="span.new">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:inline>
  </xsl:template>
  
  <xsl:template match="html:span[@class = 'old']">
    <fo:inline xsl:use-attribute-sets="span.old">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:inline>
  </xsl:template>
  
  <xsl:template match="html:span[@class = 'list-item']">
    <fo:inline xsl:use-attribute-sets="span.list-item">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:inline>
  </xsl:template>
  
  <xsl:template match="html:img[@class = 'inline']">
    <fo:external-graphic xsl:use-attribute-sets="img.inline">
      <xsl:call-template name="process-img"/>
    </fo:external-graphic>
  </xsl:template>
  
  <xsl:template match="html:img[@class = 'chapttl']">
    <fo:external-graphic xsl:use-attribute-sets="img.chapttl">
      <xsl:call-template name="process-img"/>
    </fo:external-graphic>
  </xsl:template>
  
  <!-- img{margin-top:6pt;margin-bottom:6pt;} -->
  <xsl:template match="html:div[not(@class) and count(node()) = 1 and html:img]">
    <fo:block space-before="6pt" space-after="6pt">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>
  
  <!-- Fix top level empty anchor -->
  <xsl:template match="html:body/html:a[not(node())]">
    <fo:block line-height="0pt" font-size="0pt" space-before.precedence="force" keep-with-next="always">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>
  
  <!-- Fix BR problem -->
  <xsl:template match="html:br">
    <fo:block keep-with-next="always">
      <xsl:call-template name="process-common-attributes"/>
    </fo:block>
  </xsl:template>
  
  <!-- Fix wrong THEAD -->
  <xsl:template match="html:thead[not(following-sibling::*)]">
    <fo:table-body xsl:use-attribute-sets="tbody">
      <xsl:call-template name="process-table-rowgroup"/>
    </fo:table-body>
  </xsl:template>
  
</xsl:stylesheet>
