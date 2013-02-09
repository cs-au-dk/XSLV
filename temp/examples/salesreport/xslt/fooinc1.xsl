<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:ex="http://www.example.com">
<xsl:output method="xml" indent="yes"/>

<!-- the document element matches here -->
<xsl:template match="/ex:sales">
  <html>
    <head><title>Sales Report</title></head>
    <body>
      <xsl:apply-templates select="ex:spersons"/>
    </body>
  </html>
</xsl:template>

<!-- the salesperson subtree matches -->
<xsl:template match="ex:spersons">
 - Sales Scoreboard -
  <xsl:apply-templates/>
</xsl:template>

<!-- Color table in team color -->
<xsl:template match="@teamcolor">
  <xsl:choose>
<!-- the red color is too ugly; we change it -->
    <xsl:when test=". = 'ff1010'">#ff1212</xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="concat('#',.)"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- Individual salespersons -->
<xsl:template match="ex:sperson">
  <xsl:apply-templates select="ex:name"/>
  <table>
    <xsl:attribute name="bgcolor">
      <xsl:apply-templates select="@teamcolor"/>
    </xsl:attribute>
    <xsl:apply-templates select="ex:sales"/>
  </table>
 </xsl:template>

<!-- Name of salesperson -->
<xsl:template match="ex:sperson/ex:name">
  <h2>SP: <xsl:value-of select="text()"/></h2>
</xsl:template>

<!-- Other names -->
<xsl:template match="ex:name">
  <a>
<!-- if a contact mail is available, use that -->
    <xsl:choose>
      <xsl:when test="../ex:contact">
        <xsl:apply-templates select="../ex:contact"/>
        <xsl:value-of select="../ex:name"/>
      </xsl:when>
      <xsl:otherwise>
<!-- if not, just use the homepage for link tgt. -->
        <xsl:attribute name="href">
          <xsl:value-of select="../ex:homepage"/>
        </xsl:attribute>
        <xsl:value-of select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </a>
</xsl:template>

<!-- contacts' email -->
<xsl:template match="ex:contact">
  <xsl:attribute name="href">
    <xsl:value-of select="ex:email"/>
  </xsl:attribute>
</xsl:template>

<!-- sales(2) elements -->
<xsl:template match="ex:sales">
  <xsl:for-each select="ex:customer">
    <tr>
      <td>
        <!-- these customer elements are inside sales elements -->
        <xsl:apply-templates select="."/>
      </td>
      <xsl:apply-templates select="following-sibling::ex:amount[1]"/>
    </tr>
  </xsl:for-each>
</xsl:template>

<!-- match customer inside sales -->
<xsl:template match="ex:sales/ex:customer">
  <xsl:variable name="id" select="."/>
  <xsl:apply-templates select=
    "//ex:customers/ex:customer[@id=$id]/ex:name"/>
</xsl:template>

<!-- render the amount as a hr -->
<xsl:template match="ex:amount">
  <td><hr align="left" width="{text()}"/><xsl:value-of select="."/></td>
</xsl:template>

</xsl:stylesheet>
