<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- just a necessary rootstrap case -->
<xsl:template match="foo">
  <r>
    <xsl:apply-templates/>
  </r>
</xsl:template>

<!-- if attribute is there, go s -->
<xsl:template match="bar[@a='1']">
  <s>
    <xsl:apply-templates select="." mode="afterburner"/>
  </s>
</xsl:template>

<!-- otherwise, t -->
<xsl:template match="bar[@a='2']">
  <t>
    <xsl:apply-templates select="." mode="afterburner"/>
  </t>
</xsl:template>

<!-- and in that the mandatory x element is output into s (check on pattern not in if-expression) -->
<xsl:template match="bar[@a='1']" mode="afterburner">
  <x/>
</xsl:template>

<!-- the test is in the recognition that the mandatory y element is output into t -->
<xsl:template match="bar" mode="afterburner">
  <xsl:if test="@a='2'">
    <y/>
  </xsl:if>
</xsl:template>

</xsl:stylesheet>
