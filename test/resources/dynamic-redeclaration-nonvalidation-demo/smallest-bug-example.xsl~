<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- just a necessary rootstrap case -->
<xsl:template match="/">
  <demo>
    <xsl:apply-templates/>
  </demo>
</xsl:template>

<!-- if attribute a is there -->
<xsl:template match="foo[@a]">
  <has-a-attribute>
    <xsl:copy-of select="@a"/>
    <xsl:choose>
      <xsl:when test="@b">
        <has-a-and-b-attributes>
          <xsl:copy-of select="@a"/>
          <xsl:copy-of select="@b"/>
        </has-a-and-b-attributes>
      </xsl:when>
      <xsl:otherwise>
        <has-a-but-not-b-attribute>
          <xsl:copy-of select="@a"/>
          <xsl:copy-of select="@b"/>
        </has-a-but-not-b-attribute>
      </xsl:otherwise>
    </xsl:choose>
  </has-a-attribute>
</xsl:template>

<!-- if attribute a is not there (lower priority) -->
<xsl:template match="foo">
  <has-no-a-attribute-but-we-dont-recognize-that-yet>
<!-- this should select nothing, but that still is not working -->
    <xsl:copy-of select="@a"/>
    <xsl:if test="@b='some-fixed-value'">
      <xsl:copy-of select="@b"/>
    </xsl:if>
  </has-no-a-attribute-but-we-dont-recognize-that-yet>
</xsl:template>
</xsl:stylesheet>
