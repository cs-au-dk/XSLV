<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet version="1.0"  
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:t="http://dongfang.dk/testdata">

  <xsl:template match="t:a">
    Template in c.xsl, applying!<xsl:apply-templates select="self::node()"/>now next-match!<xsl:next-match/>
  </xsl:template>

</xsl:stylesheet>
 
