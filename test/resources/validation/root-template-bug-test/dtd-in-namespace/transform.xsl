<?xml version="1.0"?>
<xsl:stylesheet 

xmlns="http://dongfang.dk/testdata" 
xmlns:input="http://dongfang.dk/testdata" 
xmlns:df="http://dongfang.dk/XSLV" 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:template match="/">
    <root>
      <xsl:apply-templates  mode="df:mo-01" select="child::input:root/child::input:a"/>
    </root>
  </xsl:template>

  <xsl:template mode="df:mo-00" match="node()" priority="0">
    <xsl:element name="input:b" xmlns:input="http://dongfang.dk/testdata"/>
  </xsl:template>

  <xsl:template mode="df:mo-01" match="node()" priority="0">
    <xsl:element name="a" namespace="http://dongfang.dk/testdata">
      <xsl:apply-templates mode="df:mo-00" select="child::input:b"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="input:c" priority="0.0">
    <xsl:element name="c" namespace="http://dongfang.dk/testdata">
    </xsl:element>
  </xsl:template>
</xsl:stylesheet>
