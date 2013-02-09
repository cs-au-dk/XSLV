<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet version="1.0"  
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:t="http://dongfang.dk/testdata">

  <xsl:import href="B.xsl"/>
  <xsl:import href="C.xsl"/>
  
  <xsl:template match="t:a">apply-imports:<xsl:apply-imports/>end of apply-imports, now next-match:<xsl:next-match/>end of next-match</xsl:template>

</xsl:stylesheet>
 
