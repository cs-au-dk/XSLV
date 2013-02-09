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

<xsl:template mode="#all" match="*">
<all>
  <xsl:apply-templates mode="#current"/>
  <xsl:apply-templates mode="a"/>
  <xsl:apply-templates mode="b"/>
</all>
</xsl:template>

<xsl:template match="*" mode="a" priority="1">
<a>
  <xsl:next-match mode="#current"/>
  <xsl:apply-templates mode="b"/>
</a>
</xsl:template>

<xsl:template mode="a" match="text()">
mode a
</xsl:template>

<xsl:template mode="b" match="text()">
mode b
</xsl:template>

<xsl:template match="comment()" mode="b">
<b/>
</xsl:template>

</xsl:stylesheet>
