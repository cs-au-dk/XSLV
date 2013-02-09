<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns="http://dongfang.dk/testdata" 
xmlns:input="http://dongfang.dk/testdata" 
version="1.0">

<!-- Should test:
     Match with default mode (implied)
     Select with default mode (implied)
     Match with #all mode pattern
     Select with #current pattern, and more than one matching mode for the template (#all, that is)
-->

<xsl:template mode="#default" match="*">
  <xsl:apply-templates select="*" mode="a"/>
  <xsl:apply-templates select="*" mode="b"/>
</xsl:template>

<xsl:template mode="a" match="*">
  <modea>
    <xsl:next-match mode="a"/>
  </modea>
</xsl:template>

<xsl:template mode="b" match="*">
  <modeb>
    <xsl:next-match mode="#current"/>
  </modeb>
</xsl:template>

<xsl:template match="*" mode="a" priority="-10">
  <xsl:apply-templates select="r" mode="#current"/> 
</xsl:template>

<xsl:template match="*" mode="b" priority="-10">
  <xsl:apply-templates select="s" mode="#current"/> 
</xsl:template>

</xsl:stylesheet>
