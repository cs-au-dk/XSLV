<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns="http://dongfang.dk/testdata" 
xmlns:input="http://dongfang.dk/testdata" 
version="1.0">

<!-- Becuase of the select expression, this template will only ever have "a" context -->
<xsl:template match="*">

<!-- demo: Name will copy OK -->
  <xsl:element name="{name()}">

<!-- value-of the context node (it's complex, so result gets approximated -->
    <xsl:attribute name="value-of-demo">
      <xsl:value-of select="."/>
    </xsl:attribute>

<!-- value-of an attribute on context -->
    <xsl:attribute name="copy-of-a1">
      <xsl:value-of select="@a1"/>
    </xsl:attribute>

<!-- value-of an attribute on context -->
    <xsl:attribute name="copy-of-a2">
      <xsl:value-of select="@a2"/>
    </xsl:attribute>

<!-- Send BOTH attributes to a different template for copying -->
    <attributes-copy-demo>
      <xsl:apply-templates select="@*"/>
    </attributes-copy-demo>

<!-- The node selected can only be "a" elements, and the template can only be this -->
    <xsl:apply-templates select="child::node()/child::node()"/>
    <xsl:apply-templates select="child::node()"/>

    <position-predicate-demo>
      <xsl:apply-templates select="input:b[2]"/>
    </position-predicate-demo>

  </xsl:element>
</xsl:template>

<!-- Send contents (text only) of BOTH nodes to the SAME template, and see that they are NOT mixed up -->
<xsl:template match="input:b | input:c">
  <xsl:value-of select="concat('Element ', local-name(), ' contains')"/>
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="@*">
  <xsl:attribute name="{name()}">
     <xsl:value-of select="."/>
  </xsl:attribute>
</xsl:template>

<xsl:template match="comment()">Hej kommentar!</xsl:template>

<xsl:template match="processing-instruction()">Hej PI!</xsl:template>

</xsl:stylesheet>
