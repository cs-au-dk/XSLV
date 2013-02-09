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
  <xsl:param name="page-margin-top">0.83in</xsl:param>
  <xsl:param name="page-margin-bottom">0.83in</xsl:param>
  <xsl:param name="page-margin-left">0.83in</xsl:param>
  <xsl:param name="page-margin-right">0.83in</xsl:param>
  
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
    <xsl:attribute name="font-family">Verdana, Myriad Web, Syntax, sans-serif</xsl:attribute>
    <xsl:attribute name="font-size-adjust">0.58</xsl:attribute>
    <xsl:attribute name="color">#CCCC99</xsl:attribute>
    <xsl:attribute name="background">black</xsl:attribute>
    <xsl:attribute name="line-height.minimum">1.5em</xsl:attribute>
    <xsl:attribute name="line-height.optimum">1.58em</xsl:attribute>
    <xsl:attribute name="line-height.maximum">1.67em</xsl:attribute>
    <xsl:attribute name="padding-start">12pt</xsl:attribute>
    <xsl:attribute name="padding-end">12pt</xsl:attribute>
    <xsl:attribute name="start-indent">12pt</xsl:attribute>
    <xsl:attribute name="end-indent">12pt</xsl:attribute>
    
  </xsl:attribute-set>
  
  <xsl:attribute-set name="page-header">
    <!-- specified on (page-header)fo:static-content's only child fo:block -->
    <xsl:attribute name="font-family">Tahoma, Verdana, Myriad Web, Syntax, sans-serif</xsl:attribute>
    <xsl:attribute name="font-size-adjust">0.53</xsl:attribute>
    <xsl:attribute name="color">gray</xsl:attribute>
    <xsl:attribute name="font-size">small</xsl:attribute>
    <xsl:attribute name="text-align">center</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="page-footer">
    <!-- specified on (page-footer)fo:static-content's only child fo:block -->
    <xsl:attribute name="font-family">Tahoma, Verdana, Myriad Web, Syntax, sans-serif</xsl:attribute>
    <xsl:attribute name="font-size-adjust">0.53</xsl:attribute>
    <xsl:attribute name="color">gray</xsl:attribute>
    <xsl:attribute name="font-size">small</xsl:attribute>
    <xsl:attribute name="text-align">center</xsl:attribute>
  </xsl:attribute-set>
  
  <!--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
       Block-level
  =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-->
  
  <xsl:attribute-set name="h1">
    <xsl:attribute name="font-family">Tahoma, Verdana, Myriad Web, Syntax, sans-serif</xsl:attribute>
    <xsl:attribute name="font-size-adjust">0.53</xsl:attribute>
    <xsl:attribute name="font-size">2em</xsl:attribute>
    <xsl:attribute name="font-weight">500</xsl:attribute>
    <xsl:attribute name="color">#66CCCC</xsl:attribute>
    <xsl:attribute name="line-height">1.33em</xsl:attribute>
    <xsl:attribute name="space-before.conditionality">retain</xsl:attribute>
    <xsl:attribute name="space-before.minimum">1em</xsl:attribute>
    <xsl:attribute name="space-before.optimum">1.33em</xsl:attribute>
    <xsl:attribute name="space-before.maximum">1.75em</xsl:attribute>
    <xsl:attribute name="space-after.minimum">0.25em</xsl:attribute>
    <xsl:attribute name="space-after.optimum">0.33em</xsl:attribute>
    <xsl:attribute name="space-after.maximum">0.42em</xsl:attribute>
    <xsl:attribute name="text-align">start</xsl:attribute>
    <xsl:attribute name="keep-with-next.within-column">always</xsl:attribute>
    <xsl:attribute name="keep-together.within-column">always</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="h2">
    <xsl:attribute name="font-family">Tahoma, Verdana, Myriad Web, Syntax, sans-serif</xsl:attribute>
    <xsl:attribute name="font-size-adjust">0.53</xsl:attribute>
    <xsl:attribute name="font-size">1.75em</xsl:attribute>
    <xsl:attribute name="font-weight">500</xsl:attribute>
    <xsl:attribute name="color">#66CCCC</xsl:attribute>
    <xsl:attribute name="line-height">1.33em</xsl:attribute>
    <xsl:attribute name="space-before.minimum">1.33em</xsl:attribute>
    <xsl:attribute name="space-before.optimum">1.75em</xsl:attribute>
    <xsl:attribute name="space-before.maximum">2.33em</xsl:attribute>
    <xsl:attribute name="space-after.minimum">0.25em</xsl:attribute>
    <xsl:attribute name="space-after.optimum">0.33em</xsl:attribute>
    <xsl:attribute name="space-after.maximum">0.42em</xsl:attribute>
    <xsl:attribute name="text-align">start</xsl:attribute>
    <xsl:attribute name="keep-with-next.within-column">always</xsl:attribute>
    <xsl:attribute name="keep-together.within-column">always</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="h3">
    <xsl:attribute name="font-family">Tahoma, Verdana, Myriad Web, Syntax, sans-serif</xsl:attribute>
    <xsl:attribute name="font-size-adjust">0.53</xsl:attribute>
    <xsl:attribute name="font-size">1.58em</xsl:attribute>
    <xsl:attribute name="font-weight">600</xsl:attribute>
    <xsl:attribute name="color">#66CCCC</xsl:attribute>
    <xsl:attribute name="line-height">1.33em</xsl:attribute>
    <xsl:attribute name="space-before.minimum">1.17em</xsl:attribute>
    <xsl:attribute name="space-before.optimum">1.58em</xsl:attribute>
    <xsl:attribute name="space-before.maximum">2.08em</xsl:attribute>
    <xsl:attribute name="space-after.minimum">0.25em</xsl:attribute>
    <xsl:attribute name="space-after.optimum">0.33em</xsl:attribute>
    <xsl:attribute name="space-after.maximum">0.42em</xsl:attribute>
    <xsl:attribute name="text-align">start</xsl:attribute>
    <xsl:attribute name="keep-with-next.within-column">always</xsl:attribute>
    <xsl:attribute name="keep-together.within-column">always</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="h4">
    <xsl:attribute name="font-family">Tahoma, Verdana, Myriad Web, Syntax, sans-serif</xsl:attribute>
    <xsl:attribute name="font-size-adjust">0.53</xsl:attribute>
    <xsl:attribute name="font-size">1.33em</xsl:attribute>
    <xsl:attribute name="font-weight">600</xsl:attribute>
    <xsl:attribute name="color">#66CCCC</xsl:attribute>
    <xsl:attribute name="line-height">1.33em</xsl:attribute>
    <xsl:attribute name="space-before.minimum">1em</xsl:attribute>
    <xsl:attribute name="space-before.optimum">1.33em</xsl:attribute>
    <xsl:attribute name="space-before.maximum">1.75em</xsl:attribute>
    <xsl:attribute name="space-after.minimum">0.25em</xsl:attribute>
    <xsl:attribute name="space-after.optimum">0.33em</xsl:attribute>
    <xsl:attribute name="space-after.maximum">0.42em</xsl:attribute>
    <xsl:attribute name="text-align">start</xsl:attribute>
    <xsl:attribute name="keep-with-next.within-column">always</xsl:attribute>
    <xsl:attribute name="keep-together.within-column">always</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="h5">
    <xsl:attribute name="font-family">Tahoma, Verdana, Myriad Web, Syntax, sans-serif</xsl:attribute>
    <xsl:attribute name="font-size-adjust">0.53</xsl:attribute>
    <xsl:attribute name="font-size">1.17em</xsl:attribute>
    <xsl:attribute name="font-weight">600</xsl:attribute>
    <xsl:attribute name="color">#66CCCC</xsl:attribute>
    <xsl:attribute name="line-height">1.33em</xsl:attribute>
    <xsl:attribute name="space-before.minimum">0.83em</xsl:attribute>
    <xsl:attribute name="space-before.optimum">1.17em</xsl:attribute>
    <xsl:attribute name="space-before.maximum">1.5em</xsl:attribute>
    <xsl:attribute name="space-after.minimum">0.25em</xsl:attribute>
    <xsl:attribute name="space-after.optimum">0.33em</xsl:attribute>
    <xsl:attribute name="space-after.maximum">0.42em</xsl:attribute>
    <xsl:attribute name="text-align">start</xsl:attribute>
    <xsl:attribute name="keep-with-next.within-column">always</xsl:attribute>
    <xsl:attribute name="keep-together.within-column">always</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="h6">
    <xsl:attribute name="font-family">Tahoma, Verdana, Myriad Web, Syntax, sans-serif</xsl:attribute>
    <xsl:attribute name="font-size-adjust">0.53</xsl:attribute>
    <xsl:attribute name="font-size">1em</xsl:attribute>
    <xsl:attribute name="font-weight">700</xsl:attribute>
    <xsl:attribute name="color">#66CCCC</xsl:attribute>
    <xsl:attribute name="line-height">1.33em</xsl:attribute>
    <xsl:attribute name="space-before.minimum">0.75em</xsl:attribute>
    <xsl:attribute name="space-before.optimum">1em</xsl:attribute>
    <xsl:attribute name="space-before.maximum">1.33em</xsl:attribute>
    <xsl:attribute name="space-after.minimum">0.25em</xsl:attribute>
    <xsl:attribute name="space-after.optimum">0.33em</xsl:attribute>
    <xsl:attribute name="space-after.maximum">0.42em</xsl:attribute>
    <xsl:attribute name="text-align">start</xsl:attribute>
    <xsl:attribute name="keep-with-next.within-column">always</xsl:attribute>
    <xsl:attribute name="keep-together.within-column">always</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="p">
    <xsl:attribute name="space-before.minimum">0.67em</xsl:attribute>
    <xsl:attribute name="space-before.optimum">0.75em</xsl:attribute>
    <xsl:attribute name="space-before.maximum">0.92em</xsl:attribute>
    <xsl:attribute name="space-after.minimum">0.67em</xsl:attribute>
    <xsl:attribute name="space-after.optimum">0.75em</xsl:attribute>
    <xsl:attribute name="space-after.maximum">0.92em</xsl:attribute>
    <xsl:attribute name="padding-start">1.58em</xsl:attribute>
    <xsl:attribute name="start-indent">inherited-property-value(start-indent) + 1.58em</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="p-initial" use-attribute-sets="p">
    <!-- initial paragraph, preceded by h1..6 or div -->
  </xsl:attribute-set>
  
  <xsl:attribute-set name="p-initial-first" use-attribute-sets="p-initial">
    <!-- initial paragraph, first child of div, body or td -->
    <xsl:attribute name="padding-start">0em</xsl:attribute>
    <xsl:attribute name="start-indent">inherit</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="blockquote">
    <xsl:attribute name="space-before.minimum">0.67em</xsl:attribute>
    <xsl:attribute name="space-before.optimum">0.75em</xsl:attribute>
    <xsl:attribute name="space-before.maximum">0.92em</xsl:attribute>
    <xsl:attribute name="space-after.minimum">0.67em</xsl:attribute>
    <xsl:attribute name="space-after.optimum">0.75em</xsl:attribute>
    <xsl:attribute name="space-after.maximum">0.92em</xsl:attribute>
    <xsl:attribute name="padding-start">1.58em</xsl:attribute>
    <xsl:attribute name="start-indent">inherited-property-value(start-indent) + 3.17em</xsl:attribute>
    <xsl:attribute name="end-indent">inherit</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="pre">
    <xsl:attribute name="font-family">'Monotype.com', Courier New, monospace</xsl:attribute>
    <xsl:attribute name="font-size">0.83em</xsl:attribute>
    <xsl:attribute name="line-height">1.25em</xsl:attribute>
    <xsl:attribute name="space-before.minimum">0.67em</xsl:attribute>
    <xsl:attribute name="space-before.optimum">0.75em</xsl:attribute>
    <xsl:attribute name="space-before.maximum">0.92em</xsl:attribute>
    <xsl:attribute name="space-after.minimum">0.67em</xsl:attribute>
    <xsl:attribute name="space-after.optimum">0.75em</xsl:attribute>
    <xsl:attribute name="space-after.maximum">0.92em</xsl:attribute>
    <xsl:attribute name="padding-start">1.58em</xsl:attribute>
    <xsl:attribute name="start-indent">inherited-property-value(start-indent) + 1.58em</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="address">
    <xsl:attribute name="space-before.minimum">1.33em</xsl:attribute>
    <xsl:attribute name="space-before.optimum">1.58em</xsl:attribute>
    <xsl:attribute name="space-before.maximum">1.83em</xsl:attribute>
    <xsl:attribute name="space-after.minimum">1.33em</xsl:attribute>
    <xsl:attribute name="space-after.optimum">1.58em</xsl:attribute>
    <xsl:attribute name="space-after.maximum">1.83em</xsl:attribute>
    <xsl:attribute name="font-style">normal</xsl:attribute>
    <xsl:attribute name="letter-spacing">0.1em</xsl:attribute>
    <xsl:attribute name="padding-start">1.58em</xsl:attribute>
    <xsl:attribute name="start-indent">inherited-property-value(start-indent) + 1.58em</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="hr">
    <xsl:attribute name="visibility">hidden</xsl:attribute>
    <xsl:attribute name="space-before.minimum">0.67em</xsl:attribute>
    <xsl:attribute name="space-before.optimum">0.75em</xsl:attribute>
    <xsl:attribute name="space-before.maximum">0.92em</xsl:attribute>
    <xsl:attribute name="space-after.minimum">0.67em</xsl:attribute>
    <xsl:attribute name="space-after.optimum">0.75em</xsl:attribute>
    <xsl:attribute name="space-after.maximum">0.92em</xsl:attribute>
    <xsl:attribute name="padding-start">1.58em</xsl:attribute>
    <xsl:attribute name="start-indent">inherited-property-value(start-indent) + 3.58em</xsl:attribute>
  </xsl:attribute-set>
  
  <!--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
       List
  =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-->
  
  <xsl:attribute-set name="ul">
    <xsl:attribute name="space-before.minimum">0.67em</xsl:attribute>
    <xsl:attribute name="space-before.optimum">0.75em</xsl:attribute>
    <xsl:attribute name="space-before.maximum">0.92em</xsl:attribute>
    <xsl:attribute name="space-after.minimum">0.67em</xsl:attribute>
    <xsl:attribute name="space-after.optimum">0.75em</xsl:attribute>
    <xsl:attribute name="space-after.maximum">0.92em</xsl:attribute>
    <xsl:attribute name="provisional-distance-between-starts">3.16em</xsl:attribute>
    <xsl:attribute name="padding-start">1.58em</xsl:attribute>
    <xsl:attribute name="start-indent">inherited-property-value(start-indent) + 1.58em</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="ul-nested">
  </xsl:attribute-set>
  
  <xsl:attribute-set name="ol">
    <xsl:attribute name="space-before.minimum">0.67em</xsl:attribute>
    <xsl:attribute name="space-before.optimum">0.75em</xsl:attribute>
    <xsl:attribute name="space-before.maximum">0.92em</xsl:attribute>
    <xsl:attribute name="space-after.minimum">0.67em</xsl:attribute>
    <xsl:attribute name="space-after.optimum">0.75em</xsl:attribute>
    <xsl:attribute name="space-after.maximum">0.92em</xsl:attribute>
    <xsl:attribute name="provisional-distance-between-starts">3.16em</xsl:attribute>
    <xsl:attribute name="padding-start">1.58em</xsl:attribute>
    <xsl:attribute name="start-indent">inherited-property-value(start-indent) + 1.58em</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="ol-nested">
  </xsl:attribute-set>
  
  <xsl:attribute-set name="ul-li">
    <!-- for (unordered)fo:list-item -->
  </xsl:attribute-set>
  
  <xsl:attribute-set name="ol-li">
    <!-- for (ordered)fo:list-item -->
  </xsl:attribute-set>
  
  <xsl:attribute-set name="dl">
    <xsl:attribute name="space-before.minimum">0.67em</xsl:attribute>
    <xsl:attribute name="space-before.optimum">0.75em</xsl:attribute>
    <xsl:attribute name="space-before.maximum">0.92em</xsl:attribute>
    <xsl:attribute name="space-after.minimum">0.67em</xsl:attribute>
    <xsl:attribute name="space-after.optimum">0.75em</xsl:attribute>
    <xsl:attribute name="space-after.maximum">0.92em</xsl:attribute>
    <xsl:attribute name="padding-start">1.58em</xsl:attribute>
    <xsl:attribute name="start-indent">inherited-property-value(start-indent) + 1.58em</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="dt">
    <xsl:attribute name="font-family">Tahoma, Verdana, Myriad Web, Syntax, sans-serif</xsl:attribute>
    <xsl:attribute name="font-size-adjust">0.53</xsl:attribute>
    <xsl:attribute name="font-size">1.17em</xsl:attribute>
    <xsl:attribute name="font-weight">600</xsl:attribute>
    <xsl:attribute name="color">#66CCCC</xsl:attribute>
    <xsl:attribute name="keep-with-next.within-column">always</xsl:attribute>
    <xsl:attribute name="keep-together.within-column">always</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="dd">
    <xsl:attribute name="space-after.minimum">0.67em</xsl:attribute>
    <xsl:attribute name="space-after.optimum">0.75em</xsl:attribute>
    <xsl:attribute name="space-after.maximum">0.92em</xsl:attribute>
    <xsl:attribute name="start-indent">inherited-property-value(start-indent) + 1.58em</xsl:attribute>
  </xsl:attribute-set>
  
  <!-- list-item-label format for each nesting level -->
  
  <xsl:param name="ul-label-1">&#x2022;</xsl:param>
  <xsl:attribute-set name="ul-label-1">
    <xsl:attribute name="font">1em serif</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:param name="ul-label-2">o</xsl:param>
  <xsl:attribute-set name="ul-label-2">
    <xsl:attribute name="font">0.67em monospace</xsl:attribute>
    <xsl:attribute name="baseline-shift">0.25em</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:param name="ul-label-3">-</xsl:param>
  <xsl:attribute-set name="ul-label-3">
    <xsl:attribute name="font">bold 0.9em sans-serif</xsl:attribute>
    <xsl:attribute name="baseline-shift">0.05em</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:param name="ol-label-1">1.</xsl:param>
  <xsl:attribute-set name="ol-label-1"/>
  
  <xsl:param name="ol-label-2">a.</xsl:param>
  <xsl:attribute-set name="ol-label-2"/>
  
  <xsl:param name="ol-label-3">i.</xsl:param>
  <xsl:attribute-set name="ol-label-3"/>
  
  <!--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
       Table
  =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-->
   
  <xsl:attribute-set name="inside-table">
    <!-- prevent unwanted inheritance -->
    <xsl:attribute name="start-indent">0pt</xsl:attribute>
    <xsl:attribute name="end-indent">0pt</xsl:attribute>
    <xsl:attribute name="text-indent">0pt</xsl:attribute>
    <xsl:attribute name="last-line-end-indent">0pt</xsl:attribute>
    <xsl:attribute name="text-align">start</xsl:attribute>
    <xsl:attribute name="text-align-last">relative</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="table-and-caption" >
    <xsl:attribute name="text-align">start</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="table">
    <xsl:attribute name="border-color">#CCCC99</xsl:attribute>
    <xsl:attribute name="text-align">start</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="table-caption" use-attribute-sets="inside-table">
    <xsl:attribute name="text-align">start</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="table-column">
    <xsl:attribute name="border-color">#CCCC99</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="thead" use-attribute-sets="inside-table">
    <xsl:attribute name="font-family">Verdana, Myriad Web, Syntax, sans-serif</xsl:attribute>
    <xsl:attribute name="font-size-adjust">0.53</xsl:attribute>
    <xsl:attribute name="color">#66CCCC</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="tfoot" use-attribute-sets="inside-table">
    <xsl:attribute name="font-family">Verdana, Myriad Web, Syntax, sans-serif</xsl:attribute>
    <xsl:attribute name="font-size-adjust">0.53</xsl:attribute>
    <xsl:attribute name="color">#66CCCC</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="tbody" use-attribute-sets="inside-table">
    <xsl:attribute name="border-color">#CCCC99</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="tr">
    <xsl:attribute name="border-color">#CCCC99</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="th">
    <xsl:attribute name="display-align">auto</xsl:attribute>
    <xsl:attribute name="relative-align">baseline</xsl:attribute>
    <xsl:attribute name="font-family">Verdana, Myriad Web, Syntax, sans-serif</xsl:attribute>
    <xsl:attribute name="font-size-adjust">0.53</xsl:attribute>
    <xsl:attribute name="font-weight">bold</xsl:attribute>
    <xsl:attribute name="color">#66CCCC</xsl:attribute>
    <xsl:attribute name="line-height">1.33em</xsl:attribute>
    <xsl:attribute name="text-align">start</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="td">
    <xsl:attribute name="border-color">#CCCC99</xsl:attribute>
    <xsl:attribute name="line-height">1.33em</xsl:attribute>
    <xsl:attribute name="text-align">start</xsl:attribute>
  </xsl:attribute-set>
  
  <!--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
       Inline-level
  =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-->
  
  <xsl:attribute-set name="b">
    <xsl:attribute name="font-weight">bold</xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="strong">
    <xsl:attribute name="font-style">italic</xsl:attribute>
    <xsl:attribute name="font-weight">bold</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="strong-em">
    <xsl:attribute name="text-transform">uppercase</xsl:attribute>
    <xsl:attribute name="font-style">normal</xsl:attribute>
    <xsl:attribute name="font-weight">bolder</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="i">
    <xsl:attribute name="font-style">italic</xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="cite">
    <xsl:attribute name="font-style">italic</xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="em">
    <xsl:attribute name="font-style">normal</xsl:attribute>
    <xsl:attribute name="font-weight">bold</xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="var">
    <xsl:attribute name="font-style">italic</xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="dfn">
    <xsl:attribute name="font-style">italic</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="tt">
    <xsl:attribute name="font-family">monospace</xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="code">
    <xsl:attribute name="font-family">monospace</xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="kbd">
    <xsl:attribute name="font-family">monospace</xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="samp">
    <xsl:attribute name="font-family">monospace</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="big">
    <xsl:attribute name="font-size">1.17em</xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="small">
    <xsl:attribute name="font-size">0.92em</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="sub">
    <xsl:attribute name="baseline-shift">sub</xsl:attribute>
    <xsl:attribute name="font-size">smaller</xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="sup">
    <xsl:attribute name="baseline-shift">super</xsl:attribute>
    <xsl:attribute name="font-size">smaller</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="s">
    <xsl:attribute name="text-decoration">line-through</xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="strike">
    <xsl:attribute name="text-decoration">line-through</xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="del">
    <xsl:attribute name="text-decoration">line-through</xsl:attribute>
    <xsl:attribute name="background">#990000</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="u">
    <xsl:attribute name="text-decoration">underline</xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="ins">
    <xsl:attribute name="text-decoration">none</xsl:attribute>
    <xsl:attribute name="background">#006600</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="abbr">
    <!-- e.g.,
    <xsl:attribute name="font-variant">small-caps</xsl:attribute>
    <xsl:attribute name="letter-spacing">0.1em</xsl:attribute>
    -->
  </xsl:attribute-set>
  
  <xsl:attribute-set name="acronym">
    <xsl:attribute name="font-variant">small-caps</xsl:attribute>
    <xsl:attribute name="letter-spacing">0.1em</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="q"/>
  <xsl:attribute-set name="q-nested"/>
  
  <!--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
       Image
  =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-->
  
  <xsl:attribute-set name="img">
  </xsl:attribute-set>
  
  <xsl:attribute-set name="img-link">
    <xsl:attribute name="border">2px solid</xsl:attribute>
  </xsl:attribute-set>
  
  <!--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
       Link
  =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-->
  
  <xsl:attribute-set name="a-link">
    <xsl:attribute name="text-decoration">none</xsl:attribute>
    <xsl:attribute name="font-weight">bold</xsl:attribute>
    <xsl:attribute name="color">#FFFF66</xsl:attribute>
  </xsl:attribute-set>
  
  <!--======================================================================
      misc. additional
  =======================================================================-->
  
  
  <xsl:attribute-set name="p-first" use-attribute-sets="p-initial">
    <!-- DIV > P:first-child, BODY > P:first-child, TD > P:first-child -->
    <xsl:attribute name="color">red</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="warning">
    <!-- .warning -->
    <xsl:attribute name="text-transform">none</xsl:attribute>
    <xsl:attribute name="font-style">normal</xsl:attribute>
    <xsl:attribute name="font-weight">bolder</xsl:attribute>
    <xsl:attribute name="background">#FFFF66</xsl:attribute>
    <xsl:attribute name="color">black</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="note">
    <!-- .note -->
    <xsl:attribute name="font-style">italic</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="subhead">
    <!-- .subhead -->
    <xsl:attribute name="space-before.minimum">0.67em</xsl:attribute>
    <xsl:attribute name="space-before.optimum">0.75em</xsl:attribute>
    <xsl:attribute name="space-before.maximum">0.92em</xsl:attribute>
    <xsl:attribute name="space-after.minimum">0.67em</xsl:attribute>
    <xsl:attribute name="space-after.optimum">0.75em</xsl:attribute>
    <xsl:attribute name="space-after.maximum">0.92em</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="stb">
    <!-- .stb -->
    <xsl:attribute name="space-before.minimum">1.67em</xsl:attribute>
    <xsl:attribute name="space-before.optimum">2.17em</xsl:attribute>
    <xsl:attribute name="space-before.maximum">2.67em</xsl:attribute>
    <xsl:attribute name="space-after.minimum">0.67em</xsl:attribute>
    <xsl:attribute name="space-after.optimum">0.75em</xsl:attribute>
    <xsl:attribute name="space-after.maximum">0.92em</xsl:attribute>
    <xsl:attribute name="padding-before">2.17em</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="mtb">
    <!-- .mtb -->
    <xsl:attribute name="space-before.minimum">2.58em</xsl:attribute>
    <xsl:attribute name="space-before.optimum">3.08em</xsl:attribute>
    <xsl:attribute name="space-before.maximum">3.5em</xsl:attribute>
    <xsl:attribute name="space-after.minimum">0.67em</xsl:attribute>
    <xsl:attribute name="space-after.optimum">0.75em</xsl:attribute>
    <xsl:attribute name="space-after.maximum">0.92em</xsl:attribute>
    <xsl:attribute name="padding-before">3.08em</xsl:attribute>
    <xsl:attribute name="border-before-width">0.1em</xsl:attribute>
    <xsl:attribute name="border-before-style">solid</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="ltb">
    <!-- .ltb -->
    <xsl:attribute name="space-before.minimum">3.5em</xsl:attribute>
    <xsl:attribute name="space-before.optimum">4.34em</xsl:attribute>
    <xsl:attribute name="space-before.maximum">5em</xsl:attribute>
    <xsl:attribute name="space-after.minimum">0.67em</xsl:attribute>
    <xsl:attribute name="space-after.optimum">0.75em</xsl:attribute>
    <xsl:attribute name="space-after.maximum">0.92em</xsl:attribute>
    <xsl:attribute name="padding-before">4.34em</xsl:attribute>
    <xsl:attribute name="border-before-width">0.25em</xsl:attribute>
    <xsl:attribute name="border-before-style">solid</xsl:attribute>
  </xsl:attribute-set>
  
  <!-- .warning -->
  <xsl:template match="html:div[@class = 'warning']">
    <fo:block xsl:use-attribute-sets="warning">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>
  <xsl:template match="html:p[@class = 'warning']">
    <fo:block xsl:use-attribute-sets="p warning">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>
  <xsl:template match="html:span[@class = 'warning']">
    <fo:inline xsl:use-attribute-sets="warning">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:inline>
  </xsl:template>
  <xsl:template match="html:strong[@class = 'warning']">
    <fo:inline xsl:use-attribute-sets="strong warning">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:inline>
  </xsl:template>
  <xsl:template match="html:em[@class = 'warning']">
    <fo:inline xsl:use-attribute-sets="em warning">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:inline>
  </xsl:template>
  
  <!-- .note -->
  <xsl:template match="html:div[@class = 'note']">
    <fo:block xsl:use-attribute-sets="note">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>
  <xsl:template match="html:p[@class = 'note']">
    <fo:block xsl:use-attribute-sets="p note">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>
  <xsl:template match="html:span[@class = 'note']">
    <fo:inline xsl:use-attribute-sets="note">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:inline>
  </xsl:template>
  <xsl:template match="html:strong[@class = 'note']">
    <fo:inline xsl:use-attribute-sets="strong note">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:inline>
  </xsl:template>
  <xsl:template match="html:em[@class = 'note']">
    <fo:inline xsl:use-attribute-sets="em note">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:inline>
  </xsl:template>
  
  <!-- .subhead -->
  <xsl:template match="html:div[@class = 'subhead']">
    <fo:block xsl:use-attribute-sets="subhead">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>
  <xsl:template match="html:h1[@class = 'subhead']">
    <fo:block xsl:use-attribute-sets="h1 subhead">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>
  <xsl:template match="html:h2[@class = 'subhead']">
    <fo:block xsl:use-attribute-sets="h2 subhead">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>
  <xsl:template match="html:h3[@class = 'subhead']">
    <fo:block xsl:use-attribute-sets="h3 subhead">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>
  <xsl:template match="html:h4[@class = 'subhead']">
    <fo:block xsl:use-attribute-sets="h4 subhead">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>
  <xsl:template match="html:h5[@class = 'subhead']">
    <fo:block xsl:use-attribute-sets="h5 subhead">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>
  <xsl:template match="html:h6[@class = 'subhead']">
    <fo:block xsl:use-attribute-sets="h6 subhead">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>
  
  <!-- .stb -->
  <xsl:template match="html:div[@class = 'stb']">
    <fo:block xsl:use-attribute-sets="stb">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>
  <xsl:template match="html:p[@class = 'stb']">
    <fo:block xsl:use-attribute-sets="p stb">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>
  
  <!-- .mtb -->
  <xsl:template match="html:div[@class = 'mtb']">
    <fo:block xsl:use-attribute-sets="mtb">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>
  <xsl:template match="html:p[@class = 'mtb']">
    <fo:block xsl:use-attribute-sets="p mtb">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>
  
  <!-- .ltb -->
  <xsl:template match="html:div[@class = 'ltb']">
    <fo:block xsl:use-attribute-sets="ltb">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>
  <xsl:template match="html:p[@class = 'ltb']">
    <fo:block xsl:use-attribute-sets="p ltb">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>
  
  <!-- p.initial -->
  <xsl:template match="html:p[@class = 'initial']">
    <fo:block xsl:use-attribute-sets="p-initial">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>
  
  <!-- center -->
  <xsl:template match="html:center">
    <fo:block text-align="start">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>


</xsl:stylesheet>
