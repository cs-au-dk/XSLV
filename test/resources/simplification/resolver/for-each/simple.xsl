<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet version="1.0" 
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:variable name="foo" select="'foo'"/>
  <xsl:template match="/">
    <xsl:variable name="bar" select="."/>
    <xsl:for-each select="//baz">
      <xsl:value-of select="$bar"/>      
      <xsl:value-of select="$foo"/>
      <xsl:variable name="foobar" select="."/>
      <xsl:for-each select=".">
        <xsl:variable name="dud" select="$foobar"/>
        <xsl:value-of select="$foo"/>
        <xsl:value-of select="$bar"/>
        <xsl:value-of select="$foobar"/>
        <xsl:value-of select="$dud"/>
      </xsl:for-each> 
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>