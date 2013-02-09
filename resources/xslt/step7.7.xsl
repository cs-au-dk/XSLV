<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://relaxng.org/ns/structure/1.0" xmlns:rng="http://relaxng.org/ns/structure/1.0" version="1.0"  xmlns:exsl="http://exslt.org/common" extension-element-prefixes="exsl" >
  <xsl:template match="*|text()|@*">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
<!-- resolve external refs -->
  <xsl:template match="rng:externalRef">
    <xsl:variable name="ref-rtf">
      <xsl:apply-templates select="document(@href)">
        <xsl:with-param name="out" select="0"/>
        <xsl:with-param name="stop-after" select="'step7.7'"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:variable name="ref" select="exsl:node-set($ref-rtf)"/>
    <xsl:element name="{local-name($ref/*)}" namespace="http://relaxng.org/ns/structure/1.0">
      <xsl:if test="not($ref/*/@ns) and @ns">
        <xsl:attribute name="ns">
          <xsl:value-of select="@ns"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:copy-of select="$ref/*/@*"/>
      <xsl:copy-of select="$ref/*/*|$ref/*/text()"/>
    </xsl:element>
  </xsl:template>
</xsl:stylesheet>
