<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns="http://dongfang.dk/testdata" 
xmlns:input="http://dongfang.dk/testdata" 
version="1.0">

<!-- we want to see that the following works:
  Parent step on attributes
  name() AVT

-->

<xsl:template match="input:a">
<xsl:element name="{name()}">
From element a to @a1|@a2
  <xsl:apply-templates select="@*"/>
From element a to @a1|@a2 - union
  <xsl:apply-templates select="@*" mode="union"/>
We're back. Bye bye.
</xsl:element>
</xsl:template>

<xsl:template match="input:b">
<xsl:element name="{name()}">
From element a to @a1|@a2
  <xsl:apply-templates select="@*"/>
From element a to @a1|@a2 - union
  <xsl:apply-templates select="@*" mode="union"/>
We're back. Bye bye.
</xsl:element>
</xsl:template>

<xsl:template match="@a1">
Welcome <xsl:value-of select="name()"/> to the @a1 template. Your value is in the set <xsl:value-of select="."/>. Recursing now. See you soon!
  <xsl:apply-templates select="parent::node()"/>
</xsl:template>

<xsl:template match="@a2">
Welcome <xsl:value-of select="name()"/> to the @a2 template. Your value is in the set <xsl:value-of select="."/>. Recursing now. See you soon!
  <xsl:apply-templates select="parent::node()"/>
</xsl:template>

<!-- this should be able to distinguish the a1 on a from
  that on b..!(and the same with a2, of course) -->
<xsl:template match="@*" mode="union">
Welcome <xsl:value-of select="name()"/> to the union template. We can see your value is <xsl:value-of select="."/>. Recursing now. See you soon!
  <xsl:apply-templates select="parent::node()"/>
</xsl:template>

</xsl:stylesheet>
