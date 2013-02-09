<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:foons="http://dongfang.dk/foo"
  xmlns:barns="http://dongfang.dk/bar"
  xmlns="http://dongfang.dk/default"
>
   <xsl:variable
   name="test_seq_constructor_var_contents_simplification">

   <!-- attribute value template should work - - and the first
   attribute value should NOT be interpreted as XPath -->
     <fooelem>

     <!-- should be turned into a value-of -->
       <xsl:text>XSL:TEXTTEXT</xsl:text>
       <xsl:text> </xsl:text>TEXTNODE  
  </fooelem>
   </xsl:variable>
 
   <xsl:template select="/">

     <!-- should be resolved away -->
     <!-- Not really literals simp business but test even so... -->
     <xsl:copy-of select="$test_seq_constructor_var_contents_simplification"/>
   </xsl:template>
 </xsl:stylesheet>
