<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://relaxng.org/ns/structure/1.0" xmlns:rng="http://relaxng.org/ns/structure/1.0" version="1.0" exclude-result-prefixes="rng">
  <xsl:template match="*|text()|@*">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
<!-- kill datatypelibrary attributes -->
  <xsl:template match="@datatypeLibrary"/>
<!-- except on these elements -->
  <xsl:template match="rng:data|rng:value">
    <xsl:copy>
      <xsl:attribute name="datatypeLibrary">
        <xsl:value-of select="ancestor-or-self::*[@datatypeLibrary][1]/@datatypeLibrary"/>
      </xsl:attribute>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
