<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://relaxng.org/ns/structure/1.0" xmlns:rng="http://relaxng.org/ns/structure/1.0" version="1.0">
<xsl:template match="*|text()|@*">
	<xsl:copy>
		<xsl:apply-templates select="@*"/>
		<xsl:apply-templates/>
	</xsl:copy>
</xsl:template>
<!-- zap whitespace -->
  <xsl:template match="text()[normalize-space(.)='' and not(parent::rng:param or parent::rng:value)]"/>
<!-- normalize attributes -->
  <xsl:template match="@name|@type|@combine">
    <xsl:attribute name="{name()}">
      <xsl:value-of select="normalize-space(.)"/>
    </xsl:attribute>
  </xsl:template>
<!-- normalize names -->
  <xsl:template match="rng:name/text()">
    <xsl:value-of select="normalize-space(.)"/>
  </xsl:template>
</xsl:stylesheet>
