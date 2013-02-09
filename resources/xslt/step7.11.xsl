<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://relaxng.org/ns/structure/1.0" xmlns:rng="http://relaxng.org/ns/structure/1.0" version="1.0">
  <xsl:template match="*|text()|@*">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="rng:name[contains(., ':')]">
    <xsl:variable name="prefix" select="substring-before(., ':')"/>
    <name>
      <xsl:attribute name="ns">
        <xsl:for-each select="namespace::*">
          <xsl:if test="name()=$prefix">
            <xsl:value-of select="."/>
          </xsl:if>
        </xsl:for-each>
      </xsl:attribute>
      <xsl:value-of select="substring-after(., ':')"/>
    </name>
  </xsl:template>
</xsl:stylesheet>
