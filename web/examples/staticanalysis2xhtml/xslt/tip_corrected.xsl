<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns="http://www.w3.org/1999/xhtml">
  <xsl:template match="tip">
    <html>
      <head>
        <title>TIP</title>
          <style type="text/css">
            body {
              font-family: monospace;
            }
            span { 
              cursor: default;
              color: blue;
              text-decoration: none; 
            } 
            textarea {
              color: red;
            }              
          </style>          
        <script type="text/javascript">

            analysis = '0';

            function setAnalysis(a){
              analysis = a;
              document.result.area.value = '';
            }          

            function getRightAnalysis(an){
              document.result.area.value = an[analysis];
            }
        </script>
      </head>
      <body>
        <xsl:attribute name="onload">document.result.area.value = '';</xsl:attribute>
        <h1>TIP - static analysis</h1>
          (move the cursor over the blue fields to see the analysis results)
        <xsl:apply-templates select="program"/>
      </body>
    </html>              
  </xsl:template> 

  <xsl:template match="program">

   <table border="0" cellspacing="30">
    <tr>
     <td>
      <xsl:apply-templates select="function"/>
      <xsl:apply-templates select="stm"/>
     </td>
     <td>
      <form name="result" action="mypage.html">Results from the analysis: 
       <select name="choice" onchange="setAnalysis(this.options[this.selectedIndex].value);">
       <option> 
         <xsl:attribute name="value"><xsl:value-of select="../analysis/@ref"/></xsl:attribute>
         <xsl:value-of select="../analysis/@name"/>
       </option>
       </select><br/><br/>

       <textarea name="area" rows="5" cols="40" disabled="disabled"></textarea>
      </form>
     </td>
    </tr>
   </table>
  </xsl:template>

  <xsl:template match="function">
    <xsl:value-of select="@name"/>(<xsl:value-of select="@arguments"/>) {<br/>
    <xsl:if test="@locals != ''"> 
     <xsl:call-template name="indentation">
       <xsl:with-param name="indent">1</xsl:with-param> 
     </xsl:call-template>var <xsl:value-of select="@locals"/>;<br/>
    </xsl:if>

    <xsl:apply-templates select="stm">
      <xsl:with-param name="indent">1</xsl:with-param> 
    </xsl:apply-templates>

    &#160;return <xsl:apply-templates select="return/exp">
                  <xsl:with-param name="nested">0</xsl:with-param> 
                 </xsl:apply-templates>;<br/>
    }<br/><br/>
  </xsl:template>

  <xsl:template match="stm">
     <xsl:param name="indent"/>
     <xsl:call-template name="indentation">
         <xsl:with-param name="indent"><xsl:value-of select="$indent"/></xsl:with-param> 
     </xsl:call-template>

     <xsl:choose>
      <xsl:when test="analysis">
       <span>
        <xsl:attribute name="onmouseover">getRightAnalysis({<xsl:for-each select="analysis"><xsl:value-of select="./@ref"/>: '<xsl:value-of select="./text()"/>', </xsl:for-each>});</xsl:attribute>

          <!-- <xsl:apply-templates select="while|assignment|if|output|ptrassign"> -->
          <xsl:apply-templates select="assignment">
           <xsl:with-param name="indent"><xsl:value-of select="$indent"/></xsl:with-param> 
          </xsl:apply-templates>
       </span>
      </xsl:when>
      <xsl:otherwise>
          <xsl:apply-templates select="while|assignment|if|output|ptrassign">
         <xsl:with-param name="indent"><xsl:value-of select="$indent"/></xsl:with-param> 
          </xsl:apply-templates>
      </xsl:otherwise>
     </xsl:choose>
  </xsl:template>


  <xsl:template match="while">
   <xsl:param name="indent"/>
   while (<xsl:apply-templates select="exp"/>) {<br/>
     <xsl:apply-templates select="stm">
      <xsl:with-param name="indent"><xsl:value-of select="$indent + 1"/></xsl:with-param> 
     </xsl:apply-templates>
     <xsl:call-template name="indentation">
      <xsl:with-param name="indent"><xsl:value-of select="$indent"/></xsl:with-param> 
     </xsl:call-template>}<br/>
  </xsl:template>

  <xsl:template match="assignment">
   <xsl:value-of select="@id"/> = <xsl:apply-templates select="exp"/>;<br/>
  </xsl:template>

  <xsl:template match="if">
   <xsl:param name="indent"/>
   if (<xsl:apply-templates select="exp"/>) {<br/>
     <xsl:apply-templates select="then/stm">
      <xsl:with-param name="indent"><xsl:value-of select="$indent + 1"/></xsl:with-param> 
     </xsl:apply-templates>
     <xsl:call-template name="indentation">
      <xsl:with-param name="indent"><xsl:value-of select="$indent"/></xsl:with-param> 
     </xsl:call-template>}<br/>
     <xsl:if test="else">
      <xsl:call-template name="indentation">
         <xsl:with-param name="indent"><xsl:value-of select="$indent"/></xsl:with-param> 
      </xsl:call-template>else {<br/>
      <xsl:apply-templates select="else/stm">
       <xsl:with-param name="indent"><xsl:value-of select="$indent + 1"/></xsl:with-param> 
      </xsl:apply-templates>
      <xsl:call-template name="indentation">
         <xsl:with-param name="indent"><xsl:value-of select="$indent"/></xsl:with-param>
      </xsl:call-template>}<br/>
     </xsl:if>
  </xsl:template>

  <xsl:template match="output">
    output <xsl:apply-templates select="exp"/>;<br/>
  </xsl:template>

  <xsl:template match="exp">
    <xsl:param name="nested">0</xsl:param>
     <xsl:choose>
      <xsl:when test="analysis">
       <span>
        <xsl:attribute name="onmouseover">getRightAnalysis({<xsl:for-each select="analysis"><xsl:value-of select="./@ref"/>: '<xsl:value-of select="./text()"/>', </xsl:for-each>});</xsl:attribute>
        <xsl:apply-templates select="id|intconst|binop|input|call|ptrcall|ptr|malloc|deref|null">
         <xsl:with-param name="nested"><xsl:value-of select="$nested"/></xsl:with-param> 
          </xsl:apply-templates>
       </span>
      </xsl:when>
      <xsl:otherwise>
       <xsl:apply-templates select="id|intconst|binop|input|call|ptrcall|ptr|malloc|deref|null">
        <xsl:with-param name="nested"><xsl:value-of select="$nested"/></xsl:with-param> 
       </xsl:apply-templates>
      </xsl:otherwise>
     </xsl:choose>
  </xsl:template>

  <xsl:template match="id">
   <xsl:value-of select="@name"/>
  </xsl:template>

  <xsl:template match="intconst">
   <xsl:value-of select="@value"/>
  </xsl:template>

  <xsl:template match="binop">
   <xsl:param name="nested"/>
   <xsl:if test="$nested &gt; 0">(</xsl:if>
   <xsl:apply-templates select="exp[position()=1]">
    <xsl:with-param name="nested">1</xsl:with-param> 
   </xsl:apply-templates>
 
   <xsl:value-of select="@kind"/> 
   <xsl:apply-templates select="exp[position()=2]">
    <xsl:with-param name="nested">1</xsl:with-param> 
   </xsl:apply-templates>
   <xsl:if test="$nested &gt; 0">)</xsl:if>
  </xsl:template>

  <xsl:template match="input">
    input</xsl:template>

  <xsl:template match="call">
   <xsl:param name="nested"/>
   <xsl:if test="$nested &gt; 0">(</xsl:if>
   <xsl:value-of select="@id"/>(<xsl:variable name="countexp"><xsl:value-of select="count(exp)"/></xsl:variable>
   <xsl:for-each select="exp[not (position() = $countexp)]">
    <xsl:apply-templates select=".">
     <xsl:with-param name="nested">0</xsl:with-param> 
    </xsl:apply-templates>, 
   </xsl:for-each>
    <xsl:apply-templates select="exp[position() = $countexp]">
     <xsl:with-param name="nested">0</xsl:with-param> 
    </xsl:apply-templates>)<xsl:if test="$nested &gt; 0">)</xsl:if>
  </xsl:template>

  <xsl:template match="ptrcall">
   <xsl:param name="nested"/>
   <xsl:if test="$nested &gt; 0">(</xsl:if>(<xsl:apply-templates select="exp[position()=1]">
     <xsl:with-param name="nested">0</xsl:with-param> 
   </xsl:apply-templates>)(<xsl:variable name="countexp"><xsl:value-of select="count(exp)"/></xsl:variable>
   <xsl:for-each select="exp[(position() &gt; 1) and (not (position() = $countexp))]">
    <xsl:apply-templates select=".">
     <xsl:with-param name="nested">0</xsl:with-param> 
    </xsl:apply-templates>, 
   </xsl:for-each>
   <xsl:if test="$countexp &gt; 1">
    <xsl:apply-templates select="exp[position()=$countexp]">
     <xsl:with-param name="nested">0</xsl:with-param> 
    </xsl:apply-templates>
   </xsl:if>)<xsl:if test="$nested &gt; 0">)</xsl:if>
  </xsl:template>

  <xsl:template match="ptr">
   <xsl:param name="nested"/>
    &amp;<xsl:value-of select="@id"/>
  </xsl:template>

  <xsl:template match="malloc">
   <xsl:param name="nested"/>malloc</xsl:template>

  <xsl:template match="deref">
   <xsl:param name="nested"/>
   <xsl:if test="$nested &gt; 0">(</xsl:if>
    *<xsl:apply-templates select="exp">
      <xsl:with-param name="nested">0</xsl:with-param>
     </xsl:apply-templates>
   <xsl:if test="$nested &gt; 0">)</xsl:if>
  </xsl:template>

  <xsl:template match="null">
   <xsl:param name="nested"/>null</xsl:template>

  <xsl:template match="ptrassign">
   *<xsl:value-of select="@id"/> = <xsl:apply-templates select="exp"/>;<br/>
  </xsl:template>

 <xsl:template name="indentation">
  <xsl:param name="indent"/>
  <xsl:if test="$indent &gt; 0">
   &#160;
   <xsl:call-template name="indentation">
    <xsl:with-param name="indent" select="$indent - 1"/>
   </xsl:call-template>
  </xsl:if>
 </xsl:template>

</xsl:stylesheet>
