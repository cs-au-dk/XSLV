<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns="http://dongfang.dk/testdata" 
xmlns:input="http://dongfang.dk/testdata" 
version="1.0">

<xsl:template match="*">
  <xsl:element name="{name()}{name()}"/>
  <xsl:element name="{concat('foo', local-name())}"/>
  <xsl:apply-templates/>
</xsl:template>

</xsl:stylesheet>
