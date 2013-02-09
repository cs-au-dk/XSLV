<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="*|/">
    <xsl:apply-templates/>
  </xsl:template>

<!--
  <xsl:template match="*|/" mode="m">
    <xsl:apply-templates mode="m"/>
  </xsl:template>
-->

  <xsl:template match="text()|@*">
    <xsl:value-of select="."/>
  </xsl:template>

  <xsl:template match="processing-instruction()|comment()"/>
</xsl:stylesheet>
