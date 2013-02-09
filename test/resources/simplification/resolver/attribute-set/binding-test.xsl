<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet version="1.0" 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:test="http://dongfang.dk/test"
>

  <xsl:attribute-set name="foo" test:test-id="attset0">
    <xsl:attribute name="a1" select="filbert"/>
    <xsl:attribute name="a2" select="hugo"/>
    <xsl:attribute name="a3" select="osvald"/>
  </xsl:attribute-set>

  <xsl:attribute-set name="baz" test:test-id="attset3" use-attribute-sets="foo bar">
    <xsl:attribute name="a3" select="osvald"/>
    <xsl:attribute name="a6" select="gorm"/>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="foo" test:test-id="attset1">
    <xsl:attribute name="a3" select="osvald"/>
    <xsl:attribute name="a4" select="gorm"/>
  </xsl:attribute-set>

  <xsl:attribute-set name="bar" test:test-id="attset2">
    <xsl:attribute name="a4" select="hubert"/>
    <xsl:attribute name="a5" select="fido"/>
  </xsl:attribute-set>

</xsl:stylesheet>
