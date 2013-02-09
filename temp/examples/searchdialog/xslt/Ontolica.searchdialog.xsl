<?xml version="1.0" encoding="UTF-8"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:msxsl="urn:schemas-microsoft-com:xslt" version="1.0" xmlns="http://www.w3.org/1999/xhtml">
 
  <!-- Matches the root element. -->
  <xsl:template match="SearchDialog">    
    <style type="text/css">
      .ns-ontolica-searchdialog-tab
      {
        padding:3px 10px 3px 10px;
        border-bottom:1px solid darkgray
      }

      .ns-ontolica-searchdialog-activetab
      {
        padding:3px 10px 3px 10px;
        border:1px solid darkgray;
        background-color:#F8F8F8;
      }      
      
      .ns-ontolica-componentgroup
      {
        border:2px solid #EEE;
        margin-bottom:10px
      }

      .ns-ontolica-componentgroupheader
      {
        padding:5px;background-color:#EEE
      }           
            
      .ns-ontolica-componentgroupbody
      {
        padding:5px;
      }                  
    </style>

    <xsl:apply-templates select="ComponentView[@active='True']"/>     
    
    <!-- Give the default focus to the keyword input field. -->
    <script language="javascript" type="text/javascript">
      <xsl:text>var keywordTextBoxElement = document.getElementById('</xsl:text>
      <xsl:value-of select="/SearchDialog/@id"/>
      <xsl:text>_KeywordTextBox');</xsl:text>
      <xsl:text>if (keywordTextBoxElement != null) keywordTextBoxElement.focus();</xsl:text>
    </script>    
  </xsl:template>
  
  <!-- ======================================================= -->  
  <!-- Default rendering of search tabs.                       -->
  <!-- ======================================================= -->  

  <xsl:template match="ComponentView">
    <!-- Pick the controls for the main dialog. -->
    <xsl:variable name="KeywordTextBox" select="Component[@type='SingleLineKeywords']/TextBox"/>
    <xsl:variable name="Message" select="Component[@type='SingleLineKeywords']/Message"/>

    <script language="javascript" type="text/javascript">
      var searchDialogId = '<xsl:value-of select="/SearchDialog/@id"/>';
      var advancedSearchURL = '<xsl:value-of select="following-sibling::ComponentView[1]/@url"/>';
      var searchSettingsURL = '<xsl:value-of select="/SearchDialog/ComponentView[@name='Settings']/@url"/>';
      var maxRows = <xsl:value-of select="Component[@type='Settings']/MaxRows/@value"/>;
      var keywordPostBackName = '<xsl:value-of select="$KeywordTextBox/@name"/>';
      var sourcePostBackName = '<xsl:value-of select="Component[@type='SingleSource']/PostBackName"/>';    
    </script>        
      
    <table border="0" cellspacing="0" cellpadding="0">
      <!-- Show search tabs if there is more than one -->
      <xsl:if test="count(/SearchDialog/ComponentView[@type='Tab']) &gt; 1">
        <tr><td colspan="4">
          <xsl:call-template name="RenderSearchTabs"/>
        </td></tr>
      </xsl:if>
      
      <!-- Vertical spacing -->
      <tr><td colspan="4" height="10px"></td></tr>
      
      <tr>
        <xsl:apply-templates select="Component[@type='SingleLineKeywords']"/>
        <xsl:apply-templates select="Component[@type='SingleSource']"/>
        <td>
          <xsl:apply-templates select="//Component[@type='Buttons']" mode="Search"/>          
        </td>      
      </tr> 

      <!-- Add search filter(s) if included in the search dialog definition. --> 
      <xsl:apply-templates select="Component[@type='Filters']"/>          
    </table>

    <xsl:apply-templates select="$Message">
      <xsl:with-param name="style">margin-top:10px</xsl:with-param>
    </xsl:apply-templates>      
  </xsl:template>
  
  <xsl:template match="Component[@type='Filters']">
    <tr><td colspan="4">
      <table cellpadding="0" cellspacing="0"><tr>
      
        <!-- Show single filter as checkbox and multiple filters as radio buttons. -->
        <xsl:apply-templates select="Filter">
          <xsl:with-param name="type">
            <xsl:choose>
              <xsl:when test="count(Filter) &gt; 1">radio</xsl:when>
              <xsl:otherwise>checkbox</xsl:otherwise>
            </xsl:choose>
          </xsl:with-param>
        </xsl:apply-templates>        
        
      </tr></table>
    </td></tr>
  </xsl:template>

  <xsl:template match="Filter">
    <xsl:param name="type"/>
    <td>
      <xsl:choose>
        <xsl:when test="@selected='True'">
          <input type="{$type}" name="{../PostBackName}" value="{.}" checked="True"/>
        </xsl:when>
        <xsl:otherwise>
          <input type="{$type}" name="{../PostBackName}" value="{.}"/>
        </xsl:otherwise>
      </xsl:choose>
    </td>
    <td style="padding-right:10px">
      <xsl:value-of select="@displayName"/>
    </td>
  </xsl:template>
  
  <!-- ======================================================= -->  
  <!-- Search Tabs                                             -->
  <!-- ======================================================= -->  

  <xsl:template name="RenderSearchTabs">
    <table cellpadding="0" cellspacing="0" width="100%"><tr>
      <xsl:for-each select="/SearchDialog/ComponentView[@type='Tab']">
        <xsl:choose>
          <xsl:when test="@active = 'True'">
            <td class="ns-ontolica-searchdialog-activetab">
              <xsl:if test="@icon">
                <img alt="" src="{@icon}" align="bottom" style="margin-right:3px"/>
              </xsl:if>
              <xsl:value-of select="@displayName"/>
            </td>
          </xsl:when>
          <xsl:otherwise>
            <td class="ns-ontolica-searchdialog-tab">              
              <xsl:if test="@icon">
                <img alt="" src="{@icon}" align="bottom" style="margin-right:3px"/>
              </xsl:if>
              <a href="javascript:SearchDialog_SetView('{@url}')">
                <xsl:value-of select="@displayName"/>
              </a>
            </td>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
      <td class="ns-ontolica-searchdialog-tab" width="90%">
        <xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>      
      </td>
    </tr></table>    
  </xsl:template>

  <!-- ======================================================= -->  
  <!-- Keyword Search Box                                      -->
  <!-- ======================================================= -->  

  <xsl:template match="Component[@type='SingleLineKeywords']">
    <xsl:param name="Width">400px</xsl:param>
    <td>
      <input id="{/SearchDialog/@id}_KeywordTextBox"
              class="ns" 
              type="text" 
              name="{TextBox/@name}" 
              value="{TextBox}" 
              accesskey="s" 
              title="§SearchDialog.EnterKeywords§"
              style="width:{$Width}"  
              onkeypress="javascript:SearchDialog_HandleEnter(event, '{/SearchDialog/@id}')"/>
    </td>
  </xsl:template>

  <!-- ======================================================= -->  
  <!-- Template with HTML rendering logic for the single       -->
  <!-- source component that is  defined as                    -->
  <!-- <Component type="SingleSource"/> in the Ontolica        -->
  <!-- search configuration file.                              -->
  <!-- ======================================================= -->  

  <xsl:template match="Component[@type='SingleSource']">  
    <td style="padding-left:3px">
      <select class="ns" name="{PostBackName}" onchange="javascript:{AutoPostBack}">
        <xsl:apply-templates select="Sources/Source" mode="DropDown"/>
      </select>
    </td>
    <td style="padding-left:2px">
      <xsl:apply-templates select="Sources/Source[@selected='True']" mode="Icon"/>          
    </td>
  </xsl:template>  

  <xsl:template match="Source" mode="Icon">  
    <xsl:choose>
      <!-- Render icon with hyperlink -->
      <xsl:when test="URL and Icon">
        <a href="{URL}">
          <img alt="" border="0" src="{Icon}" title="{Icon/@title}" style="margin-right:3px"/>
        </a>
      </xsl:when>

      <!-- Render icon without hyperlink -->
      <xsl:when test="Icon">
        <img alt="" src="{Icon}" title="{Icon/@title}" style="margin-right:3px"/>        
      </xsl:when>
    </xsl:choose>
  </xsl:template>  

  <xsl:template match="Source" mode="DropDown">  
    <xsl:choose>
      <xsl:when test="@selected='True'">
        <option selected="1">    
          <xsl:value-of select="DisplayName"/>
        </option>
      </xsl:when>

      <xsl:otherwise>
        <option>    
          <xsl:value-of select="DisplayName"/>
        </option>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>  

  <!-- ========================================================= -->  
  <!-- Template with HTML rendering logic for the Advanced View  -->
  <!-- ========================================================= -->  

  <xsl:template match="ComponentView[@name='IntranetAdvanced' or @name='WebAdvanced']">  
    <xsl:apply-templates select="ComponentGroup" mode="Advanced"/>        
  </xsl:template>

  <xsl:template match="ComponentGroup" mode="Advanced">
    <div class="ns-ontolica-componentgroup">    
      <div class="ns-ontolica-componentgroupheader">
        <xsl:value-of select="@displayName"/>
      </div>
      <div class="ns-ontolica-componentgroupbody">
        <xsl:apply-templates select="Component" mode="Advanced"/>    
      </div>
    </div>
  </xsl:template>

  <xsl:template match="Component[@type='MultiLineKeywords']" mode="Advanced">
    <table>
      <tr>
        <td colspan="3" style="padding-bottom:10px">
          <em>§SearchDialog.FindResults§</em>
        </td>
      </tr>    
      <tr>
        <td nowrap="true">§SearchDialog.AllOfKeywords§</td>
        <td>
          <xsl:apply-templates select="TextBox[1]" mode="Advanced">
            <xsl:with-param name="id">
              <xsl:value-of select="/SearchDialog/@id"/>
              <xsl:text>_KeywordTextBox</xsl:text>
            </xsl:with-param>
          </xsl:apply-templates>
        </td>
        <td style="padding-left:20px">
          <xsl:apply-templates select="//Component[@type='Settings']/MaxRows"/>
        </td>
        <td style="padding-left:5px">
          <xsl:apply-templates select="//Component[@type='Buttons']" mode="Search"/>
        </td>
      </tr>
      <tr>
        <td nowrap="true">§SearchDialog.PhraseKeywords§</td>
        <td>
          <xsl:apply-templates select="TextBox[2]" mode="Advanced"/>
        </td>
      </tr>
      <tr>
        <td nowrap="true" style="padding-right:20px">§SearchDialog.OneOfKeywords§</td>
        <td>
          <xsl:apply-templates select="TextBox[3]" mode="Advanced"/>
        </td>
      </tr>
      <tr>
        <td nowrap="true">§SearchDialog.NoneOfKeywords§</td>
        <td>
          <xsl:apply-templates select="TextBox[4]" mode="Advanced"/>
        </td>
      </tr>
    </table>       
  </xsl:template>

  <xsl:template match="TextBox" mode="Advanced">
    <xsl:param name="id"/>

    <xsl:variable name="HighlightStyle">
      <xsl:if test="string-length(current()) &gt; 0">background-color:FFFFCC</xsl:if>
    </xsl:variable>    

    <xsl:choose>
      <xsl:when test="$id">
        <input id="{$id}" 
              type="text" 
              accesskey="s" 
              name="{@name}" 
              style="width:265px;{$HighlightStyle}" 
              value="{.}"
              onkeypress="javascript:SearchDialog_HandleEnter(event, '{/SearchDialog/@id}')"/>
      </xsl:when>
      <xsl:otherwise>
        <input type="text" 
              accesskey="s" 
              name="{@name}" 
              style="width:265px;{$HighlightStyle}" 
              value="{.}"
              onkeypress="javascript:SearchDialog_HandleEnter(event, '{/SearchDialog/@id}')"/>
      </xsl:otherwise>
    </xsl:choose>    
  </xsl:template>

  <!-- Do not render settings. They are picked elsewhere. -->
  <xsl:template match="Component[@type='Settings']" mode="Advanced">
  </xsl:template>

  <!-- Do not render buttons. They are picked elsewhere. -->
  <xsl:template match="Component[@type='Buttons']" mode="Advanced">
    <input id="OntolicaNewSearchBtn"
           class="ns" 
           type="Submit" 
           accesskey="g"
           name="{Button[@type='NewSearch']/@name}" 
           value="New Search"
           style="display:none"/>
  </xsl:template>

  <xsl:template match="Component[@type='Buttons']" mode="Search">
    <a href="javascript:OntolicaSearchDialog_Search('{/SearchDialog/@id}')">
      <img src="/_layouts/images/icongo01.gif" 
            alt="§SearchDialog.Search§" 
            style="cursor:pointer" 
            border="0"
            onmouseover="javascript:Ontolica_ChangeImage(this, 'icongo02.gif'); return true;"
            onmouseout="javascript:Ontolica_ChangeImage(this, 'icongo01.gif'); return true;"
            onmousedown="javascript:Ontolica_ChangeImage(this, 'icongo03.gif'); return true;"
            onmouseup="javascript:Ontolica_ChangeImage(this, 'icongo02.gif'); return true;"/>
    </a>

    <!-- The real but hidden button that makes a correct submit -->
    <input id="{/SearchDialog/@id}_SearchBtn"
           class="ns" 
           type="Submit" 
           accesskey="g"
           name="{Button[@type='Search']/@name}" 
           value="§SearchDialog.Search§"
           style="display:none"/>               
  </xsl:template>

  <!-- ======================================================= -->  
  <!-- Template with HTML rendering logic for the multi        -->
  <!-- source component that is  defined as                    -->
  <!-- <Component type="MultiSource"/> in the Ontolica         -->
  <!-- search configuration file.                              -->
  <!-- ======================================================= -->  
  <xsl:template match="Component[@type='MultiSource']" mode="Advanced">
    <xsl:apply-templates select="Sources" mode="Advanced"/>
  </xsl:template>

  <xsl:template match="Sources" mode="Advanced">  
    <!-- This table controls the layout of the search scope check boxes. -->
    <table cellpadding="0" cellspacing="0">
      <tr><xsl:apply-templates select="Source[1]" mode="CheckBox"/></tr>    
      <tr><xsl:apply-templates select="Source[position() &gt; 1 and position() &lt; 6]" mode="CheckBox"/></tr>
      <tr><xsl:apply-templates select="Source[position() &gt; 5 and position() &lt; 10]" mode="CheckBox"/></tr>
      <tr><xsl:apply-templates select="Source[position() &gt; 9 and position() &lt; 14]" mode="CheckBox"/></tr>
      <tr><xsl:apply-templates select="Source[position() &gt; 13 and position() &lt; 18]" mode="CheckBox"/></tr>
    </table>
  </xsl:template>  

  <xsl:template match="Source" mode="CheckBox">  
    <td width="1%">          
      <xsl:choose>
        <xsl:when test="@selected='True'">
          <input type="checkbox" 
                 name="{../../PostBackName}" 
                 value="{DisplayName}" 
                 checked="checked"
                 onclick="javascript:SearchDialog_UpdateSources(event)"/>
        </xsl:when>
        <xsl:otherwise>
          <input type="checkbox" 
                 name="{../../PostBackName}" 
                 value="{DisplayName}"
                 onclick="javascript:SearchDialog_UpdateSources(event)"/>
        </xsl:otherwise>
      </xsl:choose>
    </td>
    
    <td width="5%" nowrap="true" style="padding-right:20px">
      <xsl:choose>
      
        <!-- Render source with linked icon -->
        <xsl:when test="URL and Icon">
          <table cellpadding="0" cellspacing="0"><tr>
            <td nowrap="true"><xsl:value-of select="DisplayName"/></td>
            <td><a href="{URL}"><img alt="" border="0" hspace="5" src="{Icon}" title="{Icon/@title}"/></a></td>
          </tr></table>
        </xsl:when>
        
        <!-- Render source with unlinked icon -->
        <xsl:when test="Icon">
          <table cellpadding="0" cellspacing="0"><tr>
            <td nowrap="true"><xsl:value-of select="DisplayName"/></td>
            <td><img alt="" hspace="5" src="{Icon}" title="{Icon/@title}"/></td>
          </tr></table>
        </xsl:when>
        
        <!-- Render source without an icon -->
        <xsl:otherwise>
          <xsl:value-of select="DisplayName"/>
        </xsl:otherwise>
      </xsl:choose>        
    </td>
  </xsl:template>  

  <!-- ========================================================= -->  
  <!-- MultiLineProperties                                       -->
  <!-- ========================================================= -->  

  <xsl:template match="Component[@type='MultiLineProperties']" mode="Advanced">
    <table>    
      <xsl:apply-templates select="Properties" mode="Advanced"/>
    </table>
  </xsl:template>

  <xsl:template match="Properties" mode="Advanced">  
    <tr>
      <td colspan="3" style="padding-bottom:10px">
        <em>§SearchDialog.PropertiesHint§.</em>
      </td>
    </tr>
    
    <!-- Maps property operators to a display name. -->
    <xsl:variable name="OperatorDisplayNames">
      <Operator value="Contains">§SearchDialog.Operator.Contains§</Operator>
      <Operator value="EqualTo">§SearchDialog.Operator.EqualTo§</Operator>
      <Operator value="NotEqualTo">§SearchDialog.Operator.NotEqualTo§</Operator>
      <Operator value="LessThan">§SearchDialog.Operator.LessThan§</Operator>
      <Operator value="LessThanOrEqualTo">§SearchDialog.Operator.LessThanOrEqualTo§</Operator>
      <Operator value="GreaterThan">§SearchDialog.Operator.GreaterThan§</Operator>
      <Operator value="GreaterThanOrEqualTo">§SearchDialog.Operator.GreaterThanOrEqualTo§</Operator>
    </xsl:variable>
  
    <xsl:apply-templates select="Property" mode="Advanced">
      <xsl:with-param name="OperatorDisplayNames" select="$OperatorDisplayNames"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="Property[Name='Separator']" mode="Advanced">
    <tr style="height:1px">
      <td colspan="6" style="border-top:1px dotted gray"><img src="/_layouts/images/blank.gif" alt=""/></td>
    </tr>
  </xsl:template>

  <xsl:template match="Property" mode="Advanced">
    <xsl:param name="OperatorDisplayNames"/>
    
    <tr>
      <td style="font-weight:bold;padding-right:10px" nowrap="true">
        <xsl:value-of select="DisplayName"/>
      </td>

      <xsl:variable name="ValueFieldCols">
        <xsl:choose>
          <xsl:when test="count(Operators/Operator) &gt; 1">1</xsl:when>
          <xsl:otherwise>2</xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
            
      <xsl:variable name="ValueFieldWidth">
        <xsl:choose>
          <xsl:when test="count(Operators/Operator) &gt; 1">200</xsl:when>
          <xsl:otherwise>354</xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:variable name="HighlightStyle">
        <xsl:choose>
          <xsl:when test="Message">
            background-color:red;color:white
          </xsl:when>
          <xsl:when test="string-length(Value) &gt; 0">
            background-color:FFFFCC
          </xsl:when>
        </xsl:choose>
      </xsl:variable>
      
      <!-- Show operator Drop-Down if property has more -->
      <!-- than one possible operator.                  -->
      <xsl:if test="count(Operators/Operator) &gt; 1">
        <td>
          <select name="{Operator/@name}" style="width:150px">
            <xsl:variable name="SelectedOperator" select="Operator"/>
            
            <xsl:for-each select="Operators/Operator">
              <xsl:choose>
                <xsl:when test="$SelectedOperator=current()">
                  <Option value="{.}" selected="selected" style="{$HighlightStyle}">
                    <xsl:value-of select="'hejsa'"/>
                  </Option>
                </xsl:when>
                <xsl:otherwise>
                  <Option value="{.}">
                    <xsl:value-of select="'hejsa'"/>
                  </Option>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:for-each>
          </select>
        </td>
      </xsl:if>
             
      <!-- Show value text box or value drop-down. -->      
      <td colspan="{$ValueFieldCols}">
        <xsl:choose>
          <xsl:when test="Values">          
            <select name="{Value/@name}" style="width:{$ValueFieldWidth}">
              <Option></Option>
              <xsl:for-each select="Values/Value">
                <xsl:choose>
                  <xsl:when test="@selected = 'True'">
                    <Option selected="selected" style="{$HighlightStyle}">
                      <xsl:value-of select="."/>
                    </Option>
                  </xsl:when>
                  
                  <xsl:otherwise>
                    <Option>
                      <xsl:value-of select="."/>
                    </Option>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:for-each>
            </select>
          </xsl:when>
          
          <xsl:otherwise>
            <input name="{Value/@name}" 
                   value="{Value}" 
                   style="width:{$ValueFieldWidth};{$HighlightStyle}"/>
          </xsl:otherwise>
        </xsl:choose>  
      </td>  
      
      <!-- Show Message, Description or nothing. --> 
      <xsl:choose>
        <xsl:when test="Message">
          <xsl:apply-templates select="Message" mode="Property"/>
        </xsl:when>
        
        <xsl:when test="HelpLink">
          <td>
            <a href="{HelpLink}" target="help">
              <img src="/_layouts/§LCID§/Ontolica/Help.gif" border="0" alt="help" title="{Description}"/>
            </a>            
          </td>
        </xsl:when>        
        
        <xsl:when test="Description">
          <td>
            <img src="/_layouts/§LCID§/Ontolica/Info.gif" alt="info" title="{Description}"/>
          </td>
        </xsl:when>
        
        <xsl:otherwise>
          <td></td><td></td>
        </xsl:otherwise>
      </xsl:choose>
      
      <td width="75%"></td>
    </tr>
  </xsl:template>

  <!-- ========================================================= -->  
  <!-- Template with HTML rendering logic for custom components  -->
  <!-- that is defined as <Component type="Custom"/> in the      -->
  <!-- Ontolica search configuration file.                       -->
  <!-- ========================================================= -->  

  <xsl:template match="Component[@type='Custom']" mode="Advanced">
    <xsl:apply-templates select="Html | Message"/>
  </xsl:template>  

  <xsl:template match="Html">
    <xsl:value-of select="." disable-output-escaping="yes"/>
  </xsl:template>  

  <!-- ========================================================= -->  
  <!-- Template with HTML rendering logic for the Settings View  -->
  <!-- ========================================================= -->  
  
  <xsl:template match="ComponentView[@name='Settings']">
        <xsl:apply-templates select="Component" mode="Settings"/>
  </xsl:template>
  
  <xsl:template match="Component[@type='Settings']" mode="Settings">
    <xsl:apply-templates select="ValidSettings"/>

    <div class="ns-ontolica-componentgroup">    
      <div class="ns-ontolica-componentgroupheader">
        §SearchDialog.Settings.Title§
      </div>
      <div class="ns-group-body">
        <table>
          <tr>
            <td style="padding-right:30px"><strong>§SearchDialog.Settings.NumberOfResults.Title§</strong></td> 
            <td>
              <table><tr>
                <td>§SearchDialog.Settings.NumberOfResults.Description1§</td>
                <td><xsl:apply-templates select="MaxRows"/></td>
                <td>§SearchDialog.Settings.NumberOfResults.Description2§.</td>
              </tr></table>
            </td>
          </tr>     
          
          <tr>
            <td><strong>§SearchDialog.Settings.DetailLevel.Title§</strong></td>        
            <td>
              <table><tr>
                <td>§SearchDialog.Settings.DetailLevel.Description1§</td>
                <td><xsl:apply-templates select="DetailLevel"/></td>
                <td>§SearchDialog.Settings.DetailLevel.Description2§.</td>
              </tr></table>
            </td>
          </tr>     
          
          <tr>
            <td><strong>§SearchDialog.Settings.ResultsWindow.Title§</strong></td>
            <td>
              <table><tr>        
                <td><xsl:apply-templates select="OpenLinksInNewWindow"/></td>
                <td>§SearchDialog.Settings.ResultsWindow.Description§.</td>
              </tr></table>
            </td>
          </tr>
          
          <!-- HINT: Remove this hidden style to include the word breaker setting. -->
          <tr style="display:none">
            <td><strong>§SearchDialog.Settings.EnableWordBreaker.Title§</strong></td>
            <td>
              <table><tr>        
                <td><xsl:apply-templates select="EnableWordBreaker"/></td>
                <td>§SearchDialog.Settings.EnableWordBreaker.Description§.</td>
              </tr></table>
            </td>
          </tr>
        </table>
      </div>
    </div>  
  </xsl:template>

  <xsl:template match="Component[@type='Buttons']" mode="Settings">
    <table style="margin-top:10px" width="100%">
      <tr>
        <td>
          §SearchDialog.Settings.SaveHint§.
        </td>
        <td align="right">
          <input type="submit" 
                 name="{Button[@type='SaveSettings']/@name}" 
                 accesskey="a" 
                 value="§SearchDialog.Settings.Save§"/> 
        </td>
      </tr>
    </table>    
    <table style="margin-top:30px">
      <tr>
        <td style="color:gray">
          §SearchDialog.Settings.CookieNote§.
        </td>
      </tr>
    </table>    
  </xsl:template>

  <xsl:template match="MaxRows">
    <xsl:variable name="SelectedValue" select="@value"/>
    
    <xsl:variable name="MaxRowsOptions">
      <Option value="10">10 §SearchDialog.Settings.MaxRowOption§</Option>
      <Option value="20">20 §SearchDialog.Settings.MaxRowOption§</Option>
      <Option value="30">30 §SearchDialog.Settings.MaxRowOption§</Option>
      <Option value="40">40 §SearchDialog.Settings.MaxRowOption§</Option>
      <Option value="50">50 §SearchDialog.Settings.MaxRowOption§</Option>
      <Option value="100">100 §SearchDialog.Settings.MaxRowOption§</Option>      
    </xsl:variable>
    
    <select class="ns" name="{@name}">
        <xsl:choose>
          <xsl:when test="'10' = @value">
            <option value="{@value}" selected="selected">
              <xsl:value-of select="'10 §SearchDialog.Settings.MaxRowOption§'"/>
            </option>
          </xsl:when>
          <xsl:otherwise>
            <option value="{@value}">
              <xsl:value-of select="'10 §SearchDialog.Settings.MaxRowOption§'"/>
            </option>
          </xsl:otherwise>
        </xsl:choose>
    </select>
  </xsl:template>

  <xsl:template match="DetailLevel">
    <xsl:variable name="SelectedValue" select="@value"/>
    
    <xsl:variable name="DetailLevelOptions">
      <Option value="0">§SearchDialog.Settings.DetailLevel.High§</Option>
      <Option value="1">§SearchDialog.Settings.DetailLevel.Medium§</Option>
      <Option value="2">§SearchDialog.Settings.DetailLevel.Low§</Option>
    </xsl:variable>
    
    <select class="ns" name="{@name}">
        <xsl:choose>
          <xsl:when test="'10' = @value">
            <option value="{@value}" selected="selected">
              <xsl:value-of select="'§SearchDialog.Settings.DetailLevel.High§'"/>
            </option>
          </xsl:when>
          <xsl:otherwise>
            <option value="{@value}">
              <xsl:value-of select="'§SearchDialog.Settings.DetailLevel.High§'"/>
            </option>
          </xsl:otherwise>
        </xsl:choose>
    </select>
  </xsl:template>

  <xsl:template match="OpenLinksInNewWindow | EnableWordBreaker">
    <xsl:choose>
      <xsl:when test="@checked='True'">
        <input type="checkbox" name="{@name}" checked="checked"/>
      </xsl:when>
      <xsl:otherwise>
        <input type="checkbox" name="{@name}"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Tells Ontolica what settings that contains a valid value, i.e. what settings -->
  <!-- that where actually rendered in the UI. If a form element of setting is not  -->
  <!-- rendered then Ontolica needs to know this or it might pick up an invalid or  -->
  <!-- empty value.                                                                 -->
  <xsl:template match="ValidSettings">
    <input type="hidden" name="{@name}" value="MaxRows;DetailLevel;OpenLinksInNewWindow;EnableWordBreaker"/>
  </xsl:template>
      
  <!-- ======================================================= -->  
  <!-- Other templates                                         -->
  <!-- ======================================================= -->  

  <xsl:template match="Message" mode="Property">
    <td><img src="/_layouts/§LCID§/Ontolica/{@type}.gif" alt="{@type}" title="{@tooltip}"/></td>
    <!-- 
    <td class="ns-{@type}" nowrap="true">
      <span title="{@tooltip}">
        <xsl:value-of select="." disable-output-escaping="yes"/>
      </span>
    </td>
    -->        
  </xsl:template>  

  <xsl:template match="Message">
    <xsl:param name="style"/>
    <table border="0" style="{$style}"><tr>
      <td valign="top"><img src="/_layouts/§LCID§/Ontolica/{@type}.gif" alt="{@type}"/></td>
      <td class="ns-{@type}">
        <span title="{@tooltip}">
          <xsl:value-of select="." disable-output-escaping="yes"/>
        </span>
      </td>        
    </tr></table>
  </xsl:template>  
                
</xsl:stylesheet>
  
