<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template match="foo">
  <root>
    <xsl:apply-templates/>
  </root>
</xsl:template>

<xsl:template match="bar">
  <xsl:choose>
  <xsl:when test="@a='x'">
    It is x. Check: <xsl:value-of select="@a"/>
    <xsl:apply-templates select="." mode="afterburner"/>
  </xsl:when>
  <xsl:otherwise>
    is not x: <xsl:value-of select="@a"/>
    <xsl:apply-templates select="." mode="afterburner"/>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="bar[@a='x']" mode="afterburner">
after template to template flow, still is x.
</xsl:template>

<xsl:template match="bar[@a!='x']" mode="afterburner">
after template to template flow, still is not x.
</xsl:template>

<xsl:template match="bar" mode="afterburner">
after template to template flow, confusion.
</xsl:template>

</xsl:stylesheet>
