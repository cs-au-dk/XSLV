<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns="http://dongfang.dk/testdata" 
xmlns:input="http://dongfang.dk/testdata" 
version="1.0">

<xsl:key name="input:k1" match="foo/bar" use="@shanyang"/>
<xsl:key name="k2" match="//bar/baz" use="@shanyang"/>

<xsl:template match="x">
  <xsl:value-of select="key('input:k1')"/>
  <xsl:value-of select="key('k2')"/>
  <xsl:value-of select="key('k2')/foobar"/>
</xsl:template>

</xsl:stylesheet>