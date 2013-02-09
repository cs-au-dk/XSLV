<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns="http://www.w3.org/1999/xhtml">

  <xsl:template match="root">
    <xsl:apply-templates />
  </xsl:template>

  <xsl:template match="//stm">
	<stm/>
  </xsl:template>

  <xsl:template match="//exp">
	<exp/>
  </xsl:template>
</xsl:stylesheet>
