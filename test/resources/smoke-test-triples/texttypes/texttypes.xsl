<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns="http://dongfang.dk/testdata" 
xmlns:input="http://dongfang.dk/testdata" 
version="1.0">

<xsl:template match="input:a">
value-of-b
  <xsl:value-of select="input:b"/>
value-of-c
  <xsl:value-of select="input:c"/>

<!-- just try out the element select AND union along the way -->
<!-- first fix the above problem - - they come out as empty strings! DONE !!! -->

  <xsl:apply-templates select="element(*, input:b) | element(*, input:c)"/> 

  <xsl:apply-templates select="input:a"/>

</xsl:template>

<xsl:template match="input:b">
value-of-contexttype-b
  <xsl:value-of select="."/>
texttype-as-contexttype-b
  <xsl:apply-templates select="text()"/>
</xsl:template>

<xsl:template match="input:c">
value-of-contexttype-c
  <xsl:value-of select="."/>
texttype-as-contexttype-c
  <xsl:apply-templates select="text()"/>
</xsl:template>

</xsl:stylesheet>
