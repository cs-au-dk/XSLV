<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet version="1.0" 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:test="http://dongfang.dk/test"
xmlns:variables="http://dongfang.dk/variables">
  <xsl:variable name="foo">

    <!-- should be / prefixed-->
    <xsl:value-of select="foo"/>

    <!-- should not be / prefixed-->
    <xsl:value-of select="/foo"/>

    <!-- should not be / prefixed-->
    <xsl:value-of select="//foo"/>

    <xsl:value-of select="current()"/>

    <xsl:value-of select="last()"/>

    <xsl:value-of select="position()"/>

    <xsl:value-of select="string(.)"/>

    <xsl:value-of select="local-name()"/>

    <xsl:value-of select="name()"/>

    <xsl:value-of select="namespace-uri()"/>

    <!-- should have same action in here -->
    <xsl:variable name="bar">

    <!-- only bother to test these - - the rest will work or fail the same way -->
      <xsl:value-of select="foo"/>
      <xsl:value-of select="namespace-uri()"/>

    </xsl:variable>
  </xsl:variable>

  <!-- no simplification should take place here -->
  <xsl:template match="foo">

    <xsl:copy-of select="$foo"/>

 
    <!-- should not be / prefixed-->
    <xsl:value-of select="foo"/>

    <!-- should not be / prefixed-->
    <xsl:value-of select="/foo"/>

    <!-- should not be / prefixed-->
    <xsl:value-of select="//foo"/>

    <xsl:value-of select="current()"/>

    <xsl:value-of select="last()"/>

    <xsl:value-of select="position()"/>

    <xsl:value-of select="string(.)"/>

    <xsl:value-of select="local-name()"/>

    <xsl:value-of select="name()"/>

    <xsl:value-of select="namespace-uri()"/>

    <!-- should have no action in here either -->
    <xsl:variable name="bar">

    <!-- only bother to test these - - the rest will work or fail the same way -->
      <xsl:value-of select="foo"/>
      <xsl:value-of select="namespace-uri()"/>

    </xsl:variable>
  </xsl:template>
</xsl:stylesheet>