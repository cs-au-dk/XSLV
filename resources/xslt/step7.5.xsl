<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://relaxng.org/ns/structure/1.0" xmlns:rng="http://relaxng.org/ns/structure/1.0" version="1.0">

  <xsl:template match="*|text()|@*">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

<!-- ?? -->
  <xsl:template match="rng:value[not(@type)]/@datatypeLibrary"/>
  <xsl:template match="rng:value[not(@type)]">
    <value type="token" datatypeLibrary="">
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </value>
  </xsl:template>
</xsl:stylesheet>
