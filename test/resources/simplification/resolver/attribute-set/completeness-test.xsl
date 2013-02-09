<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet version="1.0" 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:test="http://dongfang.dk/test"
xmlns:variables="http://dongfang.dk/variables">

<!-- a global variable -->
  <xsl:variable name="variables:foo" select="'global-variable-slipped-past-local-decl'"/>
  <xsl:variable name="variables:bar" select="'right'"/>

<!-- the set should have the attributes: 
	att0->0, 
	att1->'right' 
	att2->'ok' 
-->
  <xsl:attribute-set name="foo">
    <xsl:attribute name="att0">0</xsl:attribute> 
    <xsl:attribute name="att1">

<!-- the locally declared variable should shadow the global one -->
      <xsl:variable name="variables:foo" select="'local-variable-ok'"/>
<!-- so att1="right" -->
	<xsl:value-of select="$variables:foo"/>
    </xsl:attribute>

    <xsl:attribute name="att2">
<!-- the global variable ref should be resolved -->
      <xsl:value-of select="$variables:bar"/>
    </xsl:attribute>

  </xsl:attribute-set>


<!-- see that the variable binding is right when referring to the foo attribute set from within other att set -->  <!-- the set should have the attributes: 
	att0->1, (override) 
	att1->'local-variable-ok', 
	att2->'right' 
	att3->1 
-->
  <xsl:attribute-set name="bar" use-attribute-sets="foo">
    <xsl:attribute name="att0">1</xsl:attribute>
    <xsl:attribute name="att3">1</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:template match="/">
  <!-- refer to foo from element -->
  <!-- the element should have the attributes: 
	att0->0, (override) 
	att1->'local-variable-ok', 
	att2->'right' 
  -->

    <xsl:element name="elem-01" use-attribute-sets="foo"/>

    <xsl:element name="elem-02" use-attribute-sets="bar">
      <xsl:attribute name="att3">use-site-override</xsl:attribute>
    </xsl:element>

    <xsl:variable name="variables:bar" select="'scope-fuckup!'"/>

  <!-- make a local attribute set, see that it extends foo correctly -->
    <xsl:attribute-set name="bar" use-attribute-sets="foo">

      <xsl:attribute name="att3">2</xsl:attribute>

  <!-- see that the variable decl + ref are resolved away -->
      <xsl:attribute name="att4">
	    <xsl:variable name="variables:vfoo2" select="'right-again'"/>
	    <xsl:value-of select="$variables:vfoo2"/>
      </xsl:attribute>
      <xsl:attribute name="att5">
  <!-- see that the if is simplified -->
        <xsl:if test="'haha'">
          <xsl:value-of select="'ged'"/>
        </xsl:if>
      </xsl:attribute>
    </xsl:attribute-set>

    <!-- make a reference to the attribute set -->
    <!-- element should have attributes: 
	att0->1, (extension of overriding bar...)
	att1->'local-variable-ok
	att2->'right'
	att3->2
	att4->'right-again'
	att5-> (et udtryk)
     -->
    <xsl:element name="elem-03" use-attribute-sets="bar">
      <xsl:attribute name="att1">local</xsl:attribute>
    </xsl:element>
  </xsl:template>
</xsl:stylesheet>