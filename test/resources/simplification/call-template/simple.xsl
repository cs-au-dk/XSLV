<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet version="1.0" 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:struds="http://groenlandskstrudseindustri.com">
  <xsl:template name="foo">
    <struds:foo>
      <xsl:apply-templates/> 
	  <xsl:call-template name="foobar"/>
 	  <xsl:call-template name="bar"/>
    </struds:foo>
  </xsl:template>
  
  <xsl:template name="bar" match="bar">
    <struds:bar>
      <xsl:apply-templates/> 
   	  <xsl:call-template name="foobar"/>
   	  <xsl:call-template name="foo"/>
    </struds:bar>
  </xsl:template>
  
  <xsl:template name="foobar" match="foobar" mode="m">
    <struds:foo>
      <xsl:apply-templates/> 
    </struds:foo>
  </xsl:template>
  
  <xsl:template name="nothit"/>
  
</xsl:stylesheet>