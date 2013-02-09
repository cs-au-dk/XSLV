<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- just a necessary rootstrap case -->
<xsl:template match="/">
  <demo>
    <xsl:apply-templates/>
  </demo>
</xsl:template>

<!-- if attribute a is there -->
<xsl:template match="foo">
    <xsl:choose>
      <xsl:when test="@b">it's there.</xsl:when>
      <xsl:otherwise>
        <has-a-but-not-b-attribute>
          <xsl:copy-of select="@b"/>
        </has-a-but-not-b-attribute>
      </xsl:otherwise>
    </xsl:choose>
</xsl:template>
</xsl:stylesheet>
