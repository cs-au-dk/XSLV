<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:import href="super.xsl"/>
  <xsl:template match="*" priority="1000">
    <xsl:apply-templates/>
  </xsl:template>
  <xsl:template match="*" priority="2000">
    <xsl:apply-templates/>
  </xsl:template>
</xsl:stylesheet>