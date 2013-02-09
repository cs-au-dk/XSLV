<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://relaxng.org/ns/structure/1.0" xmlns:rng="http://relaxng.org/ns/structure/1.0" version="1.0">
  <xsl:template match="*|text()|@*">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="@ns"/>
  <xsl:template match="rng:name|rng:nsName|rng:value">
    <xsl:copy>
      <xsl:attribute name="ns">
        <xsl:value-of select="ancestor-or-self::*[@ns][1]/@ns"/>
      </xsl:attribute>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
