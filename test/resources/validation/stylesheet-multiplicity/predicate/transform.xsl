<?xml version="1.0"?>

<?df-test should-succeed=false?>
<?df-test input=input.dtd?>
<?df-test output=output.dsd?>

<xsl:stylesheet 
xmlns="http://dongfang.dk/testdata" 
xmlns:input="http://dongfang.dk/testdata" 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template match="input:boot">
  <boot>
    <xsl:apply-templates select="input:a[2+2]"/>
    <xsl:apply-templates select="input:b"/> <!-- does not exist -->
  </boot>
</xsl:template>

<xsl:template match="input:a">
  <xsl:copy/>
</xsl:template>

<xsl:template match="input:b">
  <xsl:copy/>
</xsl:template>

</xsl:stylesheet>
