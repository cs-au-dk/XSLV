<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:foo="http://dongfang.dk/foo"
 xmlns:bar="http://dongfang.dk/bar">

  <xsl:import href="super.xsl"/>
  <xsl:include href="included.xsl"/>
  
  <!-- test NS significance -->
  <xsl:variable name="a" select="1"/>
  <xsl:variable name="foo:a" select="2"/>
  <xsl:variable name="bar:a" select="3"/>
  
  <!-- test ref in decl, this module -->
  <xsl:variable name="b" select="$a"/>
  
  <xsl:variable name="t" select="'principal'"/>

  <!-- see that override works, should be value bound here -->
  <xsl:variable name="e" select="$t"/>

  <!-- make binding of same name to different other variables in principal 
       and imported module, see that the right guy is rebound -->
  <xsl:variable name="x" select="$z"/>
  <xsl:variable name="y" select="'MY1101'"/>
  <xsl:variable name="z" select="'MZ1401'"/>
  

  <!-- for the unit test to look at, if variables are removed -->
  <xsl:template match="gummistruds">
    <!-- should be the same, 'MZ1401' -->
<test1>
    <xsl:value-of select="$x"/>
    <xsl:value-of select="$z"/>
</test1>

    <!-- a name declared w different import precedences; 'principal'
    should win -->
<override-test>
    <xsl:value-of select="$t"/>
</override-test>    

    <!-- 2 values should be the same. This test makes sense as white 
    box - - t is resolved from within resolution of e, that is 
    not the same code at above -->
<continued-resolution-test>
    <xsl:value-of select="$t"/>
    <xsl:value-of select="$e"/>
</continued-resolution-test>

    <!-- test that ns binding works. Should not be the same
    as above (this should be 2, that should be 1) -->
<namespace-test>
    <xsl:value-of select="$a"/>
    <xsl:value-of select="$foo:a"/>
</namespace-test>    

  </xsl:template>
  
</xsl:stylesheet>
