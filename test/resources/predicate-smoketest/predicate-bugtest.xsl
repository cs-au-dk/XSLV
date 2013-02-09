<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template match="foo">
  <root>
    <xsl:apply-templates/>
  </root>
</xsl:template>

<xsl:template match="bar">
  <xsl:choose>
  <xsl:when test="@a">
    has
    <xsl:apply-templates select="." mode="afterburner"/>
  </xsl:when>
  <xsl:otherwise>
    hasnot
    <xsl:apply-templates select="." mode="afterburner"/>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="bar[@a]" mode="afterburner">
after template to template flow, still has.
</xsl:template>

<xsl:template match="bar[!@a]" mode="afterburner">
after template to template flow, still has not.
</xsl:template>

<xsl:template match="bar" mode="afterburner">
after template to template flow, confusion.
</xsl:template>

</xsl:stylesheet>
