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
</xsl:template>

<xsl:template mode="a b" match="*">
  <modea>
    <xsl:apply-templates mode="b" select="self::node()"/>
    <xsl:apply-templates mode="#current" select="child::node()"/>
  </modea>
</xsl:template>

<xsl:template mode="b c" match="*" priority="-99">
  <modeb>
    <xsl:apply-templates mode="#current" select="self::node()"/>
    <xsl:apply-templates mode="#current" select="child::node()"/>
  </modeb>
</xsl:template>

<xsl:template mode="a" match="text()">
mode a
</xsl:template>

<xsl:template mode="a" match="text()" priority="10">
mode a
</xsl:template>

<xsl:template mode="b" match="text()">
mode b
</xsl:template>

</xsl:stylesheet>
