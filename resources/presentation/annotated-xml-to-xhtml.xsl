<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
xmlns:df="http://dongfang.dk/XSLV">

<xsl:output method="xml" indent="yes" doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" version="1.0" />

<xsl:strip-space elements="*"/>

<xsl:template match="/">
  <html>
    <xsl:apply-templates/>
  </html>
</xsl:template>

<xsl:template match="*">
  <pre style="margin-left:1em">
      <span style="color:black">
      <a id="{@df:eid}"/>
      <xsl:text>&lt;</xsl:text>
      <xsl:value-of select="name()"/>
    <xsl:apply-templates select="@*"/>
    <xsl:call-template name="ns-mapping">
      <xsl:with-param name="prefixes" select="@df:ns-prefixes"/>
      <xsl:with-param name="uris" select="@df:ns-uris"/>
    </xsl:call-template>
    <xsl:choose>
      <xsl:when test="child::*|text()|comment()|processing-instruction()">
        <xsl:text>&gt;</xsl:text>
        <xsl:apply-templates select="*"/>
        <xsl:text>&lt;/</xsl:text>
        <xsl:value-of select="name()"/>
        <xsl:text>&gt;</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>/&gt;</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
    </span>
  </pre>
</xsl:template>

<xsl:template match="@*">
  <xsl:if test="not(namespace-uri()='http://dongfang.dk/XSLV')">
  <xsl:text> </xsl:text>
    <xsl:value-of select="name()"/>    
    <xsl:text>="</xsl:text>
    <xsl:value-of select="."/>
    <xsl:text>"</xsl:text>
  </xsl:if>
</xsl:template>

<xsl:template name="ns-mapping">
  <xsl:param name="prefixes"/>
  <xsl:param name="uris"/>
  <xsl:variable name="first-prefix" select="substring-before($prefixes,',')"/>
  <xsl:variable name="first-uri" select="substring-before($uris,',')"/>
  <xsl:variable name="output-prefix">
    <xsl:choose>
      <xsl:when test="$first-prefix=''">
        <xsl:value-of select="$prefixes"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$first-prefix"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="output-uri">
    <xsl:choose>
      <xsl:when test="$first-uri=''">
        <xsl:value-of select="$uris"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$first-uri"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  
  <xsl:variable name="rest-prefixes" select="substring-after($prefixes,',')"/>
  <xsl:variable name="rest-uris" select="substring-after($uris,',')"/>
  <xsl:if test="$uris">
    <xsl:text> xmlns</xsl:text>
    <xsl:if test="not($output-prefix='')">
      <xsl:text>:</xsl:text>
      <xsl:value-of select="$output-prefix"/>
    </xsl:if>
    <xsl:text>=&quot;</xsl:text>
    <xsl:value-of select="$output-uri"/>
    <xsl:text>&quot;</xsl:text>
    <xsl:call-template name="ns-mapping">
      <xsl:with-param name="prefixes" select="$rest-prefixes"/>
      <xsl:with-param name="uris" select="$rest-uris"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<xsl:template match="xsl:template/@eid">
  <xsl:text> </xsl:text>
  <span style="color:blue">
  <xsl:value-of select="name()"/>    
  <xsl:text>="</xsl:text>
  <xsl:value-of select="."/>
  <xsl:text>"</xsl:text>
  </span>
</xsl:template>

<xsl:template match="df:context-set">
  <span style="color:green">
  <xsl:text>
&lt;!-- </xsl:text>
    <xsl:choose>
      <xsl:when test="df:context-type">
        <xsl:text>Possible context types are: </xsl:text>
        <xsl:apply-templates mode="contextlist"/>
      </xsl:when>
      <xsl:otherwise>
        <b><span style="color:red"><xsl:text>This code is dead. It has no possible context types.</xsl:text></span></b>
      </xsl:otherwise>
    </xsl:choose>

  <!-- if there is anything below context-type, we verbose the context -->
  <xsl:if test="df:context-type/*">
      <xsl:apply-templates mode="verbosecontext"/>
  </xsl:if>
  <xsl:text>--&gt;
</xsl:text>
</span>
</xsl:template>

<xsl:template match="df:context-set/df:context-type[position()!=last()]" mode="contextlist">
  <span style="color:black"><xsl:value-of select="@type"/><xsl:text>, </xsl:text></span>
</xsl:template>

<xsl:template match="df:context-set/df:context-type[position()=last()]" mode="contextlist">
  <span style="color:black"><xsl:value-of select="@type"/><xsl:text> </xsl:text></span>
</xsl:template>

<xsl:template match="df:context-type" mode="verbosecontext">
  <xsl:choose>
  <xsl:when test="df:outflows">
  With <span style="color:black"><xsl:value-of select="@type"/></span> as context type, possible context flow is:
  <table border="0">
    <xsl:apply-templates select="df:outflows/df:outflow" mode="verbosecontext">
      <xsl:sort select="@type"/>
    </xsl:apply-templates>
  </table>
  </xsl:when>
  <xsl:otherwise>
  There is no flow out of here.
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="df:outflow" mode="verbosecontext">
  <tr>
    <td>
      <xsl:value-of select="@type"/>
    </td>
    <td>to</td>
    <!--<xsl:text> to: </xsl:text>-->
    <td>
      <xsl:apply-templates select="df:target" mode="verbosecontext"/>
    </td>
  </tr>
</xsl:template>

<xsl:template match="df:target[position()!=last()]" mode="verbosecontext">
  <a>
    <xsl:attribute name="href">
      <xsl:text>#</xsl:text><xsl:value-of select="@df:eid"/>
    </xsl:attribute>
    <xsl:value-of select="@df:eid"/>
  </a>
  <xsl:text>, </xsl:text>
</xsl:template>

<xsl:template match="df:target[position()=last()]" mode="verbosecontext">
  <a>
    <xsl:attribute name="href">
      <xsl:text>#</xsl:text><xsl:value-of select="@df:eid"/>
    </xsl:attribute>
    <xsl:value-of select="@df:eid"/>
  </a>
</xsl:template>

<xsl:template match="text()" mode="commented"/>
<xsl:template match="text()" mode="contextlist"/>

</xsl:stylesheet>