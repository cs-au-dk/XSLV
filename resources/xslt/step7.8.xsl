<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://relaxng.org/ns/structure/1.0" xmlns:rng="http://relaxng.org/ns/structure/1.0" version="1.0"  xmlns:exsl="http://exslt.org/common" extension-element-prefixes="exsl" >
  <xsl:template match="*|text()|@*">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
<!-- resolve includes -->
  <xsl:template match="rng:include">
    <xsl:variable name="ref-rtf">
      <xsl:apply-templates select="document(@href)">
        <xsl:with-param name="out" select="0"/>
        <xsl:with-param name="stop-after" select="'step7.8'"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:variable name="ref" select="exsl:node-set($ref-rtf)"/>
    <div>
      <xsl:copy-of select="@*[name() != 'href']"/>
      <xsl:copy-of select="*"/>
      <xsl:copy-of select="$ref/rng:grammar/rng:start[not(current()/rng:start)]"/>
      <xsl:copy-of select="$ref/rng:grammar/rng:define[not(@name = current()/rng:define/@name)]"/>
    </div>
  </xsl:template>
</xsl:stylesheet>
