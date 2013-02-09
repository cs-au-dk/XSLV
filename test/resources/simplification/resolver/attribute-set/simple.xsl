<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet version="1.0" 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:test="http://dongfang.dk/test"
xmlns:variables="http://dongfang.dk/variables">

  <xsl:attribute-set name="foo">
    <xsl:attribute name="att0" select="0"/> 
    <xsl:attribute name="att1">
<!-- the locally declared variable should shadow the global one -->
      <xsl:variable name="variable:foo" select="right"/>
<!-- so att1="right" -->
      <xsl:value-of select="$variable:foo"/>
    </xsl:attribute>
  </xsl:attribute-set>

<!-- see that the variable binding is right when referring to the foo attribute set from within other att set -->  
  <xsl:attribute-set name="bar" use-attribute-sets="foo">
    <xsl:attribute name="att2" select="1"/>
  </xsl:attribute-set>
  
  <xsl:template match="/">
  <!-- refer to foo from element -->
    <xsl:element name="hoho" use-attribute-sets="foo">
      <xsl:attribute name="att1" select="bwadr"/>
    </xsl:element>
    <foo bar="baz">
    </foo>
  </xsl:template>
</xsl:stylesheet>