<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:t="http://dongfang.dk/testdata">

  <xsl:import href="H.xsl"/>
  <xsl:include href="I.xsl"/>

  <xsl:template match="t:a" priority="2">
    <xsl:apply-imports/>
    <xsl:next-match/>
  </xsl:template>

</xsl:stylesheet>
 
