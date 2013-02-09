<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns="http://dongfang.dk/testdata" 
xmlns:input="http://dongfang.dk/testdata" 
version="1.0">

<xsl:template match="input:root">
  <root>
    <a/>
    <xsl:apply-templates/>
  </root>
</xsl:template>

<xsl:template match="input:*">
  <b>
    <xsl:value-of select="."/>
    <xsl:apply-templates select="input:nonexistant_in_schema"/>
  </b>
</xsl:template>

<xsl:template match="input:b">
  <b>
    <xsl:value-of select="."/>
    <xsl:apply-templates/>
  </b>
</xsl:template>

<xsl:template match="input:c">
  <c/>
</xsl:template>

<xsl:template match="input:nonexsitant_in_schema">
<xsl:text>  
  hi!
</xsl:text>
</xsl:template>

</xsl:stylesheet>
