<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns="http://dongfang.dk/testdata" 
xmlns:input="http://dongfang.dk/testdata" 
version="1.0">

<xsl:template match="input:root">
  <root>
    <xsl:for-each select="input:a">
      <a>
        <xsl:for-each select="input:b">
          <temp:b xmlns:temp="http://foo/bar"/>
        </xsl:for-each>
      </a>
    </xsl:for-each>
  </root>
</xsl:template>

<xsl:template match="input:c">
  <c/>
</xsl:template>
</xsl:stylesheet>
