<?xml version="1.0" encoding="utf-8"?>
<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:template match="r">
r element hit
  <xsl:apply-templates select="*/@*"/>
<!--    <xsl:apply-templates select="a/@a"/>
    <xsl:apply-templates select="b/@a"/> -->
  </xsl:template>

  <xsl:template match="@a">
    Value of context item: <xsl:value-of select="."/>
    <xsl:apply-templates select=".."/>
  </xsl:template>

  <xsl:template match="a">a element got smacked<!--<xsl:apply-templates select="//b"/>--></xsl:template>

  <xsl:template match="b">b element got smacked<!--<xsl:apply-templates select="parent::*/a"/>--></xsl:template>

</xsl:transform>
