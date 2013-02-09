<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns="http://dongfang.dk/testdata" 
  xmlns:input="http://dongfang.dk/testdata" 
  version="1.0">

<xsl:template match="root">
  <xsl:apply-templates select="a | b"/>
</xsl:template>

<xsl:template match="a" priority="100">
  a100
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="a" priority="200">
  a200
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="a/c">
  a/c
  <xsl:apply-templates select=".[foo]"/> 
</xsl:template>

<xsl:template match="b/c">
  b/c
  <xsl:apply-templates select="..[foo]"/> 
</xsl:template>

</xsl:stylesheet>
