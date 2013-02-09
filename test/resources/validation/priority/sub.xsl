<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:import href="super.xsl"/>
  <xsl:template match="foo"/>
  <xsl:template match="foo/*"/>
  <xsl:template match="foo/bar[@hejsa]"/>
</xsl:stylesheet>