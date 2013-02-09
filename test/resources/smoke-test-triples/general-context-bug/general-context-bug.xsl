<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns="http://dongfang.dk/testdata" 
xmlns:input="http://dongfang.dk/testdata" 
version="1.0">

<xsl:template match="input:a">
  <xsl:apply-templates select="input:c" mode="m1"/>
</xsl:template>

<xsl:template match="input:b">
  <xsl:apply-templates select="input:d" mode="m1"/>
</xsl:template>

<xsl:template match="*" mode="m1">
gonk
  <xsl:apply-templates select="." mode="m2"/>
bump
</xsl:template>

<xsl:template match="*" mode="m2">
  <xsl:value-of select="name()"/>
</xsl:template>

</xsl:stylesheet>
