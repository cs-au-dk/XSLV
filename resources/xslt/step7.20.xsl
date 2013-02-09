<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://relaxng.org/ns/structure/1.0" xmlns:rng="http://relaxng.org/ns/structure/1.0" version="1.0"  xmlns:exsl="http://exslt.org/common" extension-element-prefixes="exsl" >
  <xsl:template match="*|text()|@*">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="/rng:grammar">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
      <xsl:apply-templates select="//rng:element[not(parent::rng:define)]" mode="step7.20-define"/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="rng:element" mode="step7.20-define">
    <define name="__{rng:name}-elt-{generate-id()}">
      <xsl:copy>
        <xsl:apply-templates select="@*"/>
        <xsl:apply-templates/>
      </xsl:copy>
    </define>
  </xsl:template>
  <xsl:template match="rng:element[not(parent::rng:define)]">
    <ref name="__{rng:name}-elt-{generate-id()}"/>
  </xsl:template>
  <xsl:template match="rng:define[not(rng:element)]"/>
  <xsl:template match="rng:ref[@name=/*/rng:define[not(rng:element)]/@name]">
    <xsl:apply-templates select="/*/rng:define[@name=current()/@name]/*"/>
  </xsl:template>
</xsl:stylesheet>
