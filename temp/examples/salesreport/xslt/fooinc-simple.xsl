<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:ex="http://www.example.com">

<xsl:template match="/ex:sales">
  <html>
    <head>
    </head>
    <body>
      <xsl:apply-templates select="ex:salespersons"/>
    </body>
  </html>
</xsl:template>

<xsl:template match="ex:salespersons">
  <xsl:apply-templates select="following-sibling::*[1]"/>
</xsl:template>

</xsl:stylesheet>
