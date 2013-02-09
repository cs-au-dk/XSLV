<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns="http://www.w3.org/1999/xhtml">

  <xsl:template match="root">
    <xsl:apply-templates />
  </xsl:template>

  <xsl:template match="stm">
    <span>
      <xsl:attribute name="onmouseover">getRightAnalysis({<xsl:for-each select="analysis"><xsl:value-of select="./@ref"/>: '<xsl:value-of select="./text()"/>', </xsl:for-each>});</xsl:attribute>
    </span>
  </xsl:template>
</xsl:stylesheet>
