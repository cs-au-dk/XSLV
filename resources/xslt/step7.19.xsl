<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://relaxng.org/ns/structure/1.0" xmlns:rng="http://relaxng.org/ns/structure/1.0" version="1.0">

  <xsl:template match="*|text()|@*">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="/rng:grammar" priority="2">
<!--<xsl:message>HALLLLLLLLLLLLLLLLLLOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO!!!! </xsl:message>-->
    <grammar>
<!--<xsl:variable name="a" select="//rng:define"/>
   <xsl:message>Found these preservation worthy defines: <xsl:copy-of select="$a"/></xsl:message>
-->
      <xsl:apply-templates/>
      <xsl:apply-templates select="//rng:define" mode="step7.19-define"/>
    </grammar>
  </xsl:template>

  <xsl:template match="/*">
    <grammar>
      <start>
        <xsl:copy>
          <xsl:apply-templates select="@*"/>
          <xsl:apply-templates/>
        </xsl:copy>
      </start>
    </grammar>
  </xsl:template>

  <xsl:template match="rng:define|rng:define/@name|rng:ref/@name">
<!--    <xsl:message><xsl:value-of select="@name"/> killed</xsl:message>-->
  </xsl:template>

  <xsl:template match="rng:define" mode="step7.19-define">
<!--    <xsl:message><xsl:value-of select="@name"/> saved</xsl:message>-->
    <xsl:copy>
      <xsl:attribute name="name">
        <xsl:value-of select="concat(@name, '-', generate-id())"/>
      </xsl:attribute>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="rng:grammar">
    <xsl:apply-templates select="rng:start/*"/>
  </xsl:template>
  <xsl:template match="rng:ref">
    <xsl:copy>
      <xsl:attribute name="name">
        <xsl:value-of select="concat(@name, '-', generate-id(ancestor::rng:grammar[1]/rng:define[@name=current()/@name]))"/>
      </xsl:attribute>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="rng:parentRef">
    <ref>
      <xsl:attribute name="name">
        <xsl:value-of select="concat(@name, '-', generate-id(ancestor::rng:grammar[2]/rng:define[@name=current()/@name]))"/>
      </xsl:attribute>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </ref>
  </xsl:template>
</xsl:stylesheet>
