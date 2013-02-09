<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<!--
xmlns="http://www.w3.org/1999/xhtml">
-->
  <xsl:output method="xml"/>
  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="bugs">
    <html>
      <head>
        <title>Bugs</title>
      </head>
      <body>
        <h2>XSLV Bugs</h2>
        <p>Date: <xsl:value-of select="@date"/></p>
        <table border="0" width="640" cellspacing="8">
          <xsl:call-template name="sort">
            <xsl:with-param name="status" select="'Open'"/>
          </xsl:call-template>
          <xsl:call-template name="sort">
            <xsl:with-param name="status" select="'Closed'"/>
          </xsl:call-template>
        </table>
      </body>
    </html>
  </xsl:template>

  <xsl:template name="sort">
    <xsl:param name="status"/>
	  <xsl:apply-templates select="bug[string(status)=$status and string(severity)='Critical']"/>
          <xsl:apply-templates select="bug[string(status)=$status and severity='High']"/>
          <xsl:apply-templates select="bug[string(status)=$status and severity='Medium']"/>
          <xsl:apply-templates select="bug[string(status)=$status and severity='Low']"/>
          <xsl:apply-templates select="bug[string(status)=$status 
		and severity != 'Critical' 
		and severity != 'High' 
		and severity != 'Medium' 
		and severity != 'Low' ]"/>
  </xsl:template>

  <xsl:template match="bug">
    <tr>
      <td>
        <table border="1" rules="all" width="100%">
          <tr>
            <th width="25%" align="right">Id:</th>
            <td align="left">
              <xsl:value-of select="generate-id()"/>
            </td>
          </tr>
          <tr>
            <th align="right">Severity:</th>
            <td align="left">
              <xsl:value-of select="severity"/>
            </td>
          </tr>
          <tr>
            <th align="right">Title:</th>
            <td align="left">
              <xsl:value-of select="title"/>
            </td>
          </tr>
          <tr>
            <th align="right">Status:</th>
            <td align="left">
              <xsl:value-of select="status"/>
            </td>
          </tr>
          <tr>
            <th align="right">Reproducability:</th>
            <td align="left">
              <xsl:value-of select="reproducability"/>
            </td>
          </tr>
          <tr>
            <th align="right">Environment:</th>
            <td align="left">
              <xsl:value-of select="environment"/>
            </td>
          </tr>
<!--
          <tr>
            <th colspan="2" align="left">Description:</th>
          </tr>
-->
          <tr>
            <td colspan="2" align="left">
		<xsl:value-of select="description"/>
	    </td>
          </tr>
          <xsl:apply-templates select="research-log"/>
        </table>
      </td>
    </tr>
  </xsl:template>

<xsl:template match="research-log">
<tr>
<td colspan="2">
<table border="1" rules="all" width="100%">
<th align="left">Research Log:</th>
<xsl:apply-templates/>
</table>
</td>
</tr>
</xsl:template>
  
<xsl:template match="log-entry">
<tr>
<td>
Date: <xsl:apply-templates select="@date"/>
<br/>
<xsl:copy-of select="* | text()"/>
</td>
</tr>
</xsl:template>

</xsl:stylesheet>
