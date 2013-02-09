<?xml version="1.0" encoding="UTF-8"?>

<!-- This example is not interesting in itself. It was made to catch a bug. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- just a necessary rootstrap case -->
<xsl:template match="/">
  <demo>
    <xsl:apply-templates/>
  </demo>
</xsl:template>

<!-- if attribute a is there -->
<xsl:template match="foo[@a='gedefims']">
  <has-a-attribute>
    <xsl:copy-of select="@a"/>
    <xsl:if test="@a">
hejsa
    </xsl:if>
  </has-a-attribute>
</xsl:template>
</xsl:stylesheet>
