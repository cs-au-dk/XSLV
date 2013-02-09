<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet version="1.0" 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:test="http://dongfang.dk/test"
xmlns:variables="http://dongfang.dk/variables">

  <xsl:attribute-set name="foo">
    <xsl:attribute name="att0" select="0"/> 
  </xsl:attribute-set>
  
  <xsl:template match="gedefims">
    <xsl:element name="foo" namespace="bar" use-attribute-sets="foo"/>
    <baz xsl:use-attribute-sets="foo"/>
  </xsl:template>
</xsl:stylesheet>