<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns="http://dongfang.dk/testdata" 
xmlns:input="http://dongfang.dk/testdata" 
version="1.0">

<xsl:template match="input:a">
  <!-- we want to see a precise type of this -->
  <xsl:value-of select="."/>
  <xsl:apply-templates select="text()" mode="n"/>
</xsl:template>

<xsl:template match="text()" mode="n">
  <!-- we want to see a precise type here, too, because a is simple type and easy for text models... -->
  <xsl:value-of select="."/>
</xsl:template>

</xsl:stylesheet>
