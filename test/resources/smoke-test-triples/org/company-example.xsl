<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns="http://dongfang.dk/testdata" 
  xmlns:input="http://dongfang.dk/testdata" 
  version="1.0">

<xsl:template match="company">
  <xsl:apply-templates select="*/manager" mode="m"/>
</xsl:template>

<xsl:template match="manager" mode="m">
  Got a team manager: <xsl:value-of select="."/>
</xsl:template>

</xsl:stylesheet>
