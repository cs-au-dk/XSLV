<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet version="1.0" 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:test="http://dongfang.dk/test"
xmlns:variables="http://dongfang.dk/variables">
  
  <xsl:variable name="att0value" select="'right-for-foo'"/>

  <xsl:attribute-set name="some-att-set">
    <xsl:attribute name="att0">
       <xsl:value-of select="$att0value"/> 
    </xsl:attribute>
    <xsl:attribute name="att1">
       <xsl:value-of select="'should-stay-in-attset-alone'"/> 
    </xsl:attribute>
  </xsl:attribute-set>

  <xsl:variable name="foo">
    <elem-01/>
    <elem-02/>
    <elem-03  att1="should-override-in-variable-foo" xsl:use-attribute-sets="some-att-set"/>
  </xsl:variable>
  
  <xsl:template match="elem-01">

    <xsl:variable name="att0value" select="'wrong (template suck)'"/>

    <xsl:element name="foo" namespace="bar" use-attribute-sets="some-att-set"/>

    <elem-04>
      <xsl:copy-of select="$foo"/>
    </elem-04>

    <elem-05>
      <xsl:copy-of select="$bar"/>
    </elem-05>
  </xsl:template>

  <xsl:variable name="bar">
    <elem-01/>
    <elem-02/>
    <elem-03 att1="should-override-in-variable-foo" xsl:use-attribute-sets="some-att-set"/>
  </xsl:variable>
  
</xsl:stylesheet>