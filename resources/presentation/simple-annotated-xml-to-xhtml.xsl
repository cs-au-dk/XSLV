<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
xmlns:df="http://dongfang.dk/XSLV">

<xsl:output 
method="xml" 
indent="yes" 
doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" version="1.0" />

<!--<xsl:strip-space elements="*"/>-->

<xsl:param name="this-mid"/>

<xsl:variable name="red"   select="'#ffb0b0'"/>
<xsl:variable name="green" select="'#b0ffc0'"/>
<xsl:variable name="blue"  select="'#b0coff'"/>

<xsl:variable name="prestyle" select="'margin-left:1em'"/>

<xsl:variable name="twidth" select="'98%'"/>
<xsl:variable name="talign" select="'left'"/>
<xsl:variable name="cellspacing" select="'2'"/>

<!-- make html headers -->
<xsl:template match="/">
  <html original="{$this-mid}">
    <head>
<!--      <meta http-equiv="refresh" content="5"/>-->
      <title>
        <xsl:value-of select="child::xsl:*/@df:original-uri"/>
      </title>
    </head>
    <body>
      <h2>
        <xsl:choose>
          <xsl:when test="child::xsl:*/@df:original-uri">
	    <xsl:value-of select="child::xsl:*/@df:original-uri"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>Unknown / Default</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </h2>
      <xsl:apply-templates/>
    </body>
  </html>
</xsl:template>

<xsl:template match="xsl:stylesheet | xsl:transform">
  <table width="{$twidth}" cellspacing="{$cellspacing}">
    <tr>
      <td bgcolor="{$blue}" align="{$talign}">
        <xsl:call-template name="xsl-default"/>
      </td>
    </tr>
  </table>
</xsl:template>

<xsl:template match="xsl:template">
  <table width="{$twidth}" cellspacing="{$cellspacing}">
    <tr>
      <td bgcolor="{$green}" align="{$talign}">
        <xsl:call-template name="xsl-default"/>
      </td>
    </tr>
  </table>
</xsl:template>

<xsl:template match="xsl:apply-templates | xsl:call-template">
  <table width="{$twidth}" cellspacing="{$cellspacing}">
    <tr>
      <td bgcolor="{$red}" align="{$talign}">
        <xsl:call-template name="xsl-default"/>
      </td>
    </tr>
  </table>
</xsl:template>

<!-- all xsl elements and output space elements this way -->
<xsl:template match="*" name="xsl-default">
  <xsl:param name="recurse" select="true()"/>
    <pre style="{$prestyle}">
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
        <xsl:if test="$recurse and not(self::xsl:text)">
          <xsl:apply-templates select="* | child::text()"/>
        </xsl:if>
        <xsl:if test="self::xsl:text">
          <xsl:value-of select="child::text()"/>
        </xsl:if>
        <xsl:text>&lt;/</xsl:text>
        <xsl:value-of select="name()"/>
        <xsl:text>&gt;</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>/&gt;</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </pre>
</xsl:template>

<!-- output attribute text for df-eid for templates -->
<xsl:template match="xsl:template/@df:eid">
  <xsl:text> </xsl:text>
  <xsl:value-of select="name()"/>    
  <xsl:text>="</xsl:text>
  <xsl:value-of select="."/>
  <xsl:text>"</xsl:text>
</xsl:template>

<!-- output attribute text for all non-df attributes -->
<xsl:template match="@*">
  <xsl:if test="not(namespace-uri()='http://dongfang.dk/XSLV')">
  <xsl:text> </xsl:text>
    <xsl:value-of select="name()"/>    
    <xsl:text>="</xsl:text>
    <xsl:value-of select="."/>
    <xsl:text>"</xsl:text>
  </xsl:if>
</xsl:template>

<!-- output xmlns pseusoattribute text -->
<xsl:template name="ns-mapping">
  <xsl:param name="prefixes"/>
  <xsl:param name="uris"/>
  <xsl:variable name="first-prefix" select="substring-before($prefixes,',')"/>
  <xsl:variable name="first-uri" select="substring-before($uris,',')"/>
  <xsl:variable name="output-prefix">
   <xsl:choose>
      <xsl:when test="not(starts-with($prefixes, ',')) and $first-prefix='' and $prefixes!=''">
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

<!-- one might switch off flow info entirely in here -->
<xsl:template match="df:flows">
  <xsl:if test="*">
<!--    <br/>-->
    <xsl:text>
&lt;!--
</xsl:text>
    <xsl:apply-templates/>
    <xsl:text>--&gt;
</xsl:text>
  </xsl:if>
</xsl:template>

<!-- context sets -->
<xsl:template match="df:context-set">
    <xsl:choose>
      <xsl:when test="df:context-type">
        <xsl:text>Possible context types are: </xsl:text>
        <xsl:apply-templates mode="contextlist"/>
        <xsl:text>
</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>This code is never used. It has no possible context types.
</xsl:text>
      </xsl:otherwise>
    </xsl:choose>

  <!-- if there is anything below context-type, we verbose the context -->
  <xsl:if test="df:context-type/*">
      <xsl:apply-templates mode="verbosecontext"/>
  </xsl:if>
</xsl:template>

<xsl:template match="@type" mode="klam">
  <xsl:choose>
    <xsl:when test="substring(.,1,1)='['">
      <xsl:value-of select="."/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:text>&lt;</xsl:text><xsl:value-of select="."/><xsl:text>&gt;</xsl:text>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="df:context-type[not(position()=last())]" mode="contextlist">
  <xsl:apply-templates mode="klam" select="@type"/><xsl:text>, </xsl:text>
</xsl:template>

<xsl:template match="df:context-type[position()=last()]" mode="contextlist">
  <xsl:apply-templates mode="klam" select="@type"/><xsl:text> </xsl:text>
</xsl:template>

<xsl:template match="df:dead-targets">
  <xsl:text>
These flows may appear to exist, but actually do not:</xsl:text>
  <table>
  <xsl:apply-templates mode="verbosecontext"/>
  </table>
  <xsl:text>
</xsl:text>
</xsl:template>

<xsl:template match="df:flowdeath" mode="verbosecontext">
  <tr>
  <td>
  <xsl:text>Flow</xsl:text>
  <xsl:if test="df:context-set/df:context-type">
    <xsl:text> of </xsl:text>
    <xsl:apply-templates select="df:context-set/df:context-type" mode="contextlist"/>
  </xsl:if>
  <xsl:text> to </xsl:text>
  <xsl:apply-templates select="df:victim/df:target" mode="verbosecontext"/>
  </td>
  <td>
  <xsl:choose>
  <xsl:when test="df:robber">
    <xsl:text> is overridden by </xsl:text>
    <xsl:apply-templates select="df:robber/df:target" mode="verbosecontext"/>
  </xsl:when>
  <xsl:otherwise>
    <xsl:text>does not exist</xsl:text>
  </xsl:otherwise>
  </xsl:choose>
  </td>
  <td>
  <xsl:text> (</xsl:text>
  <xsl:value-of select="@cause"/>
  <xsl:text>)</xsl:text>
  </td>
  </tr>
</xsl:template>

<xsl:template match="df:context-type" mode="verbosecontext">
  <xsl:choose>
  <xsl:when test="df:outflow">
<xsl:text>
With </xsl:text><xsl:value-of select="@type"/><xsl:text> as context type, possible context flow is:</xsl:text>
  <table>
    <xsl:apply-templates select="df:outflow" mode="verbosecontext">
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
      <xsl:apply-templates mode="klam" select="@type"/>
    </td>
    <td>to</td>
    <td>
      <xsl:apply-templates select="df:target" mode="verbosecontext"/>
    </td>
  </tr>
</xsl:template>

<xsl:template match="df:target[position()!=last()]" mode="verbosecontext">
  <a>
    <xsl:attribute name="href">
      <xsl:value-of select="@df:mid"/>
      <xsl:text>.html</xsl:text>
      <xsl:text>#</xsl:text><xsl:value-of select="@df:eid"/>
    </xsl:attribute>
    <xsl:value-of select="@df:eid"/>
  </a>
  <xsl:if test="string(@df:mid)!=string($this-mid)">
    <xsl:text> in </xsl:text>
    <a>
      <xsl:attribute name="href">
      <xsl:value-of select="@df:mid"/><xsl:text>.html</xsl:text>
    </xsl:attribute>
    <xsl:value-of select="@df:sid"/>
    </a>
  </xsl:if>
  <xsl:text>, </xsl:text>
</xsl:template>

<xsl:template match="df:target[position()=last()]" mode="verbosecontext">
  <a>
    <xsl:attribute name="href">
      <xsl:value-of select="@df:mid"/>
      <xsl:text>.html</xsl:text>
      <xsl:text>#</xsl:text><xsl:value-of select="@df:eid"/>
    </xsl:attribute>
    <xsl:value-of select="@df:eid"/>
  </a>
  <xsl:if test="string(@df:mid)!=string($this-mid)">
    <xsl:text> in </xsl:text>
    <a>
      <xsl:attribute name="href">
      <xsl:value-of select="@df:mid"/><xsl:text>.html</xsl:text>
    </xsl:attribute>
    <xsl:value-of select="@df:sid"/>
    </a>
  </xsl:if>
</xsl:template>

<xsl:template match="text()">
  <xsl:value-of select="normalize-space(.)"/>
</xsl:template>

</xsl:stylesheet>
