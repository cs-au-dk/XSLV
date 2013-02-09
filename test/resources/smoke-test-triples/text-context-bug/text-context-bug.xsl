<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns="http://dongfang.dk/testdata" 
xmlns:input="http://dongfang.dk/testdata" 
version="1.0">

<xsl:template match="input:a">
  <xsl:apply-templates select="text()"/>
</xsl:template>

<xsl:template match="input:b">
  <xsl:apply-templates select="text()"/>
</xsl:template>

<xsl:template match="text()">
  <xsl:apply-templates mode="m1" select="."/>
</xsl:template>

<xsl:template match="text()" mode="m1">
  <xsl:value-of select="."/>
</xsl:template>

</xsl:stylesheet>

<!--
Cause o bug:
<schema-context-flows>
  <node type="[pcdata](HundePrutt)"/>
  <context-flow>
    <template-rule home-module="file:/home/dongfang/speciale/experimental-data/text-context-bug.xsl" index="3" mode="{}m1" match="child::text()" priority="-0.5"/>
    <node type="[pcdata](HundePrutt)"/>
    <node type="[pcdata](gedefims)"/>
  </context-flow>
</schema-context-flows>
-->
