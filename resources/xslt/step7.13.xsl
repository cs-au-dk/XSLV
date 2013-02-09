<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://relaxng.org/ns/structure/1.0" xmlns:rng="http://relaxng.org/ns/structure/1.0" version="1.0">
  <xsl:template match="*|text()|@*">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="rng:define[count(*)&gt;1]|rng:oneOrMore[count(*)&gt;1]|rng:zeroOrMore[count(*)&gt;1]|rng:optional[count(*)&gt;1]|rng:list[count(*)&gt;1]|rng:mixed[count(*)&gt;1]">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:call-template name="reduce7.13">
        <xsl:with-param name="node-name" select="'group'"/>
      </xsl:call-template>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="rng:except[count(*)&gt;1]">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:call-template name="reduce7.13">
        <xsl:with-param name="node-name" select="'choice'"/>
      </xsl:call-template>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="rng:attribute[count(*) =1]">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates select="*"/>
      <text/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="rng:element[count(*)&gt;2]">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates select="*[1]"/>
      <xsl:call-template name="reduce7.13">
        <xsl:with-param name="left" select="*[4]"/>
        <xsl:with-param name="node-name" select="'group'"/>
        <xsl:with-param name="out">
          <group>
            <xsl:apply-templates select="*[2]"/>
            <xsl:apply-templates select="*[3]"/>
          </group>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="rng:group[count(*)=1]|rng:choice[count(*)=1]|rng:interleave[count(*)=1]">
    <xsl:apply-templates select="*"/>
  </xsl:template>
  <xsl:template match="rng:group[count(*)&gt;2]|rng:choice[count(*)&gt;2]|rng:interleave[count(*)&gt;2]" name="reduce7.13">
    <xsl:param name="left" select="*[3]"/>
    <xsl:param name="node-name" select="name()"/>
    <xsl:param name="out">
      <xsl:element name="{$node-name}">
        <xsl:apply-templates select="*[1]"/>
        <xsl:apply-templates select="*[2]"/>
      </xsl:element>
    </xsl:param>
    <xsl:choose>
      <xsl:when test="$left">
        <xsl:variable name="newOut">
          <xsl:element name="{$node-name}">
            <xsl:copy-of select="$out"/>
            <xsl:apply-templates select="$left"/>
          </xsl:element>
        </xsl:variable>
        <xsl:call-template name="reduce7.13">
          <xsl:with-param name="left" select="$left/following-sibling::*[1]"/>
          <xsl:with-param name="out" select="$newOut"/>
          <xsl:with-param name="node-name" select="$node-name"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="$out"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
