<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet version="1.0" 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:test="http://groenlandskstrudseindustri.com">

<xsl:param name="param"/>

<xsl:variable name="var1" select="'hejsa'"/>

<xsl:variable name="var2" select="$var1"/>

<xsl:template name="foo">
  <xsl:variable name="node-set-var" select="."/>
  <xsl:copy-of select="'bar'"/>
  <xsl:copy-of select="$param"/>
  <xsl:copy-of select="$var1"/>
  <xsl:copy-of select="$var2"/>
  <xsl:copy-of select="2+2"/>
  <xsl:copy-of select="$node-set-var"/>
</xsl:template>

</xsl:stylesheet>