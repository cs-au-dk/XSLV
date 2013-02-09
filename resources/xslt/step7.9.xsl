<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://relaxng.org/ns/structure/1.0" xmlns:rng="http://relaxng.org/ns/structure/1.0" version="1.0">
  <xsl:template match="*|text()|@*">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="@name[parent::rng:element|parent::rng:attribute]"/>
<!-- move name attributes down -->
  <xsl:template match="rng:element[@name]|rng:attribute[@name]">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:if test="self::rng:attribute and not(@ns)">
        <xsl:attribute name="ns"/>
      </xsl:if>
      <name>
        <xsl:value-of select="@name"/>
      </name>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
