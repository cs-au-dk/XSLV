<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns="http://dongfang.dk/testdata" version="1.0">

<xsl:template match="root">
  <root>
    <xsl:apply-templates/>
  </root>
</xsl:template>

<xsl:template match="*">
  <b>
    <xsl:value-of select="."/>
    <c/>
    <xsl:apply-templates select="andemad"/>
  </b>
</xsl:template>

<xsl:template match="b">
  <b>
    <xsl:value-of select="."/>
    <xsl:apply-templates/>
  </b>
</xsl:template>

<xsl:template match="c">
  <c>
  hejsa
  </c>
</xsl:template>

<xsl:template match="gedefims">
  <c/>
</xsl:template>

</xsl:stylesheet>
