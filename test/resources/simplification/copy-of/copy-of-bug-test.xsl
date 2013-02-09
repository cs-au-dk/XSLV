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
     <fooelem fooattr="fooattrvalue" barattr="gedefims">
     <!-- should be turned into a value-of -->
       <xsl:text>TEXT</xsl:text>
     </fooelem>
   </xsl:variable>
 
   <xsl:template select="/">

     <xsl:copy-of select="$test_seq_constructor_var_contents_simplification"/>

     <mark/>

     <!-- should be left unmodified although resolved(?) -->
     <xsl:value-of select="$test_seq_constructor_var_contents_simplification"/>

     <!-- we want to test: Literal in def ns, literal in explicit ns,
     attribute w/o prefix and attributes with prefix -->

   </xsl:template>
 </xsl:stylesheet>
