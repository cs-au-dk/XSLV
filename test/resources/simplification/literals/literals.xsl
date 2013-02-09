<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:foons="http://dongfang.dk/foo"
  xmlns:barns="http://dongfang.dk/bar"
  xmlns="http://dongfang.dk/default"
>
   <xsl:variable name="v" select="'barattvalue'"/>
 
   <xsl:variable
   name="test_seq_constructor_var_contents_simplification">

   <!-- attribute value template should work - - and the first
   attribute value should NOT be interpreted as XPath -->
     <fooelem fooattr="fooattrvalue" barattr="{$v}">

     <!-- should be turned into a value-of -->
       <xsl:text>TEXT</xsl:text>
     </fooelem>
   </xsl:variable>
 
   <xsl:template select="/">

     <!-- should be resolved away -->
     <!-- Not really literals simp business but test even so... -->
     <xsl:copy-of select="$test_seq_constructor_var_contents_simplification"/>
     <mark/>
     <!-- should be left unmodified although resolved(?) -->
     <xsl:value-of select="$test_seq_constructor_var_contents_simplification"/>

     <!-- we want to test: Literal in def ns, literal in explicit ns,
     attribute w/o prefix and attributes with prefix -->

     <fooelem fooattr="fooattrvalue"
     foons:fooattr="other_namespace_fooattrvalue"/>


<!-- it is legal to translate this to:
<xsl:element name="fooelem" namespace="http...foo">
or
<xsl:element name="foo:fooelem" namespace="http...foo">
or
(<xsl:element name="foons:fooelem"> if foons maps to http...foo)
-->
     <foons:fooelem fooattr="fooattrvalue"
     foons:fooattr="other_namespace_fooattrvalue"/>

     <!-- also want to test attribute value templates -->

     <fooelem barattr="{$v}"/>

     <!--also want to test attribute constructor in literal and
     constructed elements-->

     <fooelem>
       <xsl:attribute name="fooattr">fooattrvalue</xsl:attribute>
     </fooelem>

     <!-- should be turned into a value-of -->

     <xsl:text>TEXT</xsl:text>
   </xsl:template>
 </xsl:stylesheet>
