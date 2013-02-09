<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet version="1.0" 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:test="http://dongfang.dk/test"
xmlns:variables="http://dongfang.dk/variables">

  <xsl:variable name="tl-0" select="'right'"/>

  <xsl:attribute-set name="tl-attset-0">
    <xsl:attribute name="att0">
<!-- should be 'right'-->
      <xsl:value-of select="$tl-0"/>
    </xsl:attribute>
  </xsl:attribute-set>

  <xsl:variable name="foo">
<!-- should be 'right' -->

<!-- p t går denne use att set galt - - der er ikke noget i attvals
     i tl-attset-0 på resolvetidspunktet -->

    <ztinkdyr some-att="{$tl-0}" xsl:use-attribute-sets="tl-attset-0"/>
  </xsl:variable>

<!-- foo and bar effectively the same -->
  <xsl:variable name="bar">
    <xsl:copy-of select="$foo"/>
  </xsl:variable>
  
  <xsl:template match="gedefims">
    <xsl:element name="elem-01" use-attribute-sets="tl-attset-0"/>
<!-- try to confuse with tl-0 variable in different scope -->
    <xsl:variable name="tl-0" select="'wrong'"/>

<!-- try to confuse with other foo variable when referring bar -->
    <xsl:element name="local-scope">
      <xsl:variable name="tl-0" select="'wrong'"/>
      <xsl:variable name="foo" select="'wrong'"/>
      <xsl:copy-of select="$bar"/>
    </xsl:element> 

<!-- check equality -->
    <xsl:copy-of select="$foo"/>
    <xsl:copy-of select="$bar"/>
  </xsl:template>
  
</xsl:stylesheet>