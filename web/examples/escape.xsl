<?xml version="1.0" encoding="UTF-8"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
 
  <xsl:template match="*">
    <pre style="margin-left:1em">
      <xsl:text>&lt;</xsl:text>
      <xsl:value-of select="local-name(.)"/>
      <xsl:apply-templates select="@*"/>
      <xsl:text>&gt;</xsl:text>
      <xsl:apply-templates/>
      <xsl:text>&lt;</xsl:text>
      <xsl:value-of select="local-name(.)"/>
      <xsl:text>/&gt;</xsl:text>
    </pre>
  </xsl:template>
  
  <xsl:template match="@*">
    <xsl:text> </xsl:text>
    <xsl:value-of select="local-name(.)"/>    
    <xsl:text>="</xsl:text>
    <xsl:value-of select="."/>
    <xsl:text>"</xsl:text>
  </xsl:template>
</xsl:stylesheet>
  
