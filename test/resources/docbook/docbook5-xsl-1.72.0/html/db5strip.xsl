<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:d="http://docbook.org/ns/docbook"
xmlns:ng="http://docbook.org/docbook-ng"
		xmlns:db="http://docbook.org/ns/docbook"
                xmlns:exsl="http://exslt.org/common"
                exclude-result-prefixes="db ng exsl d"
                version='1.0'>

<xsl:import href="docbook.xsl"/>

<xsl:output method="xml" encoding="utf-8" indent="no"/>

<xsl:template match="/">
  <xsl:choose>
    <!-- include extra test for Xalan quirk -->
    <xsl:when test="namespace-uri(*[1]) != 'http://docbook.org/ns/docbook'">
  <xsl:message>Adding DocBook namespace to version 4 DocBook document</xsl:message>
  <xsl:variable name="addns">
    <xsl:apply-templates mode="addNS"/>
  </xsl:variable>
  <xsl:apply-templates select="exsl:node-set($addns)"/>
</xsl:when>
    <xsl:otherwise>
      <xsl:message terminate="yes">
	<xsl:text>Cannot strip without exsl:node-set.</xsl:text>
      </xsl:message>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>
