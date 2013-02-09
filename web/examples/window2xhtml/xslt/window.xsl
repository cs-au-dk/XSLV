<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns="http://www.w3.org/1999/xhtml">
<!--Matches toplevel in xmldoc. -->
<xsl:template match="window">
<xsl:text disable-output-escaping="yes">
&lt;!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd"&gt;
</xsl:text>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
  <head>
    <title><xsl:value-of select="titlebar/title/text()"/></title>
    <xsl:comment> 
      <xsl:text> 
	/*****************************************************************************\
	*                               Page disclaimer                               *
	*    This material contains, and is a part of a computer software program     *
	*    which is, proprietary and confidential information owned by:             *
	*                               Clemen &amp; Fuglsang			      *
	*    The program, including this material, may not be duplicated, disclosed   *
	*    or reproduced in whole or in part for any purpose without the express    *
	*    written authorisation of Clemen^2 &amp; Fuglsang. 			      *
	*    All authorised reproductions must be marked with this legend.            *
	*                                                                             *
	*                             Copyright (c) 2002                              *
	*                      Simon Clemen &amp; Christina Fuglsang    	              * 
	*                             All rights reserved.                            *
	*                   ALL CONTENT IN THIS FILE IS AUTO-GENERATED                *
	\*****************************************************************************/
      </xsl:text>
    </xsl:comment>          
    <meta http-equiv="Content-Type"         content="text/html; charset=iso-8859-1" />
    <meta http-equiv="Content-Script-Type"  content="text/javascript" />
    <meta http-equiv="Content-Style-Type"   content="text/css" />
    <!--meta http-equiv="Pragma" content="no-cache" /-->
    <meta name="author" content="{pageinfo/author/name/text()}"/>
    <!--meta http-equiv="Page-Exit" content="blendTrans(duration=0.1)"/-->
   
    <link rel="stylesheet" type="text/css" href="http://{skinlocation/@path}/css/clemen.css"/>	
    <link rel="stylesheet" type="text/css" href="http://{skinlocation/@path}/css/menu.css"/>	
    <link rel="stylesheet" type="text/css" href="http://{skinlocation/@path}/css/expandmenu.css"/>	

    <link rel="stylesheet" type="text/css" href="Home/userpages/css/userstyle.css"/>	
    <script type="text/javascript" src="Home/js/utilities.js"><xsl:text> </xsl:text></script>

    <script type="text/javascript" src="http://{skinlocation/@path}/js/menu.js"><xsl:text> </xsl:text></script>
    <script type="text/javascript" src="http://{skinlocation/@path}/js/expandmenu.js"><xsl:text> </xsl:text></script>
    <script type="text/javascript" src="http://{skinlocation/@path}/js/login.js"><xsl:text> </xsl:text></script>


  </head>
  <body>
    <script type="text/javascript">
	onload=null;
	onload=null;
	var pageloadtime=null;
  </script>

    <table id='loader' style="vertial-align:center;width:100%;height:100%;text-align:center;">
      <tr>
        <td>
          <!--OBJECT classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"
            codebase="http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=5,0,0,0"
            WIDTH="140" HEIGHT="49">
            <PARAM NAME="movie" VALUE="Home/Flash/load.swf"/> 
            <PARAM NAME="quality" VALUE="high"/> 
            <PARAM NAME="bgcolor" VALUE="#FFFFFF"/> 
            <EMBED src="load.swf" quality="high" bgcolor="#FFFFFF" WIDTH="140" HEIGHT="49" TYPE="application/x-shockwave-flash" PLUGINSPAGE="http://www.macromedia.com/shockwave/download/index.cgi?P1_Prod_Version=ShockwaveFlash">
            </EMBED>
          </OBJECT-->www.clemen.dk<br/>
          Loading : Remaining elements.
          <span id="loaderstatus"></span>
          
        </td>
      </tr>
    </table>
      <form method="get" action="?" id="pageGetSelect">
    	  <input type="hidden" id="pid_get" name="get_pid" value="{pageinfo/pageid/text()}"/>
	      <input type="hidden" id="sid1_get" name="get_sid1" value="{pageinfo/pagesubid/text()}"/>
    	  <input type="hidden" id="sid2_get" name="get_sid2" value="0"/>
      </form>
      <form method="post" action="?" id="pageSelect">
        <div id="page" class="realbody" style="visibility:visible" >
	  <input type="hidden" id="pid" name="post_pid" value="{pageinfo/pageid/text()}"/>
	  <input type="hidden" id="sid1" name="post_sid1" value="{pageinfo/pagesubid/text()}"/>
	  <input type="hidden" id="sid2" name="post_sid2" />
  	  <input type="hidden" id="logout" name="logout"/>
	  <input type="hidden" id="login" name="login"/>
  	  <input type="hidden" id="update" name="update"/>
	  <input type="hidden" id="save" name="save"/>
   	  <input type="hidden" id="delete" name="delete"/>
   	  <input type="hidden" id="skinselect" name="skinselect"/>
	  <table id="pageTable" cellspacing="0" cellpadding="0">
	    <tr id="menurows" class="menuRow">
	      <td class="menuRow">
	        <div class="head">
	          <table id="titlebar" class="titleBar" cellspacing="0" cellpadding="0">
		    <tr>
		      <td class="iconTop"><img src="{titlebar/icon/path/text()}" alt=""/></td>
		      <td class="title">
		        <xsl:value-of select="titlebar/title/text()"/>
		      </td>		
		      <td></td>
		      <td class="closeicon"><img src="Home/pics/close_ikon.gif" alt="" onmousedown="window.close();"/></td>
		    </tr>
		  </table>
		</div>
	  	<xsl:comment>          
		  <xsl:text>/********* PRIMARY MENU  *********/</xsl:text>	
   	        </xsl:comment>          
  	           <xsl:apply-templates select="primarymenu"/>		
	  	<xsl:comment>          
		  <xsl:text>/********* /PRIMARY MENU  ********/</xsl:text>	
   	        </xsl:comment>          
	      </td>
	    </tr>
	    <tr id="trdivpage" >				
	      <td class="page" >	
	        <table class="fullTable" cellspacing="0" cellpadding="0">
	          <tr>
		    <xsl:comment>          
		      <xsl:text>/********* SECONDARY MENU  *********/</xsl:text>	
		    </xsl:comment>          
		    <xsl:apply-templates select="secondarymenu"/>		
		    <xsl:comment>          
		      <xsl:text>/**********/SECONDARY MENU** *******/</xsl:text>	
	   	    </xsl:comment>          
  		    <td>			
	              <div id="divpage" class="divpage">

          <xsl:comment>          
		      <xsl:text>/********* PAGE CONTENT    *********/</xsl:text>	
		    </xsl:comment>          

         <xsl:value-of disable-output-escaping="yes"  select="pagecontent/text()"/>

		     <script type="text/javascript">
  		     pageloadtime = <xsl:value-of select="loadtime/text()"/>;
         </script>
		    <xsl:comment>          
		      <xsl:text>/**********/PAGE CONTENT     *******/</xsl:text>	
	   	    </xsl:comment>          
  		
      

			
	  <div id="LoginScreen" class="LoginScreen" style="position:absolute;;z-index:1;">
		  <div>
			  <input type="hidden" name="loaded" value="1"/>
			  <input type="hidden" name="imode" value="0"/>
		  </div>
			<table style="width:100%;height:280px;border:1px inset silver;">
			  <tr style="height:150px;text-align:left;">
			    <td style="width:100%;vertical-align:top;padding:30px 0px 0px 0px">		
				    <span style="filter:alpha(opacity=0);position:absolute;visibility:hidden;z-index:2;" id="fade1"><img class="fadepic" alt="" src="Home/pics/handrotFade1.png"/></span>
			    </td>
			    <td></td>
	  	  </tr>
			</table>
	  	<div id="info" style="position:absolute;top:0px;left:0px;z-index:3">
			  <table style="width:100px;height:80px;background:transparent" class="logininfo">
			    <tr style="height:20px;text-align:right;">
				    <td></td>
				    <td style="color:silver;">Please Login.</td>	
				    <td>
				      <xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
				    </td>
			    </tr>
		  	  <tr style="height:20px;text-align:right;">
				    <td id='userHead'>Username<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>:</td>
				    <td><input type="text" name="userid" id="userid" class="loginField"/></td>
				    <td><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text></td>
			    </tr>
			    <tr style="height:20px;text-align:right;">
				    <td id='passHead'>Password<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>:</td>
    				<td><input type="password" name="userpassword" id="userpassword" class="loginField"	/></td>
    				<td><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text></td>
  	      </tr>
		      <tr style="height:20px;text-align:right;">
				    <td colspan="2" style="vertical-align:top;color:silver;">Login Automatically : <input id="savecheck" type="checkbox" style="margin:-2px;" name="autologin"/></td>
 	 			    <td><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text></td>
			    </tr>		
			    <tr style="height:20px;text-align:right;">
			      <td></td>
				    <td><input type="button" class="logonknap" name="loginknap" value="Login" onmousedown="setBusy();document.getElementById('login').value=1;document.getElementById('pageSelect').submit();"/><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
              <input onmousedown="cancelLogin()" class="logonknap" type="button" name="cancel" value="Cancel"/>
            </td>
				    <td><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text></td>
			    </tr>
			  </table>
			</div>
	  </div>
		

  </div>	
		    </td>
		  </tr>
		</table>
	      </td>
	    </tr>
	   <tr>
	    <td>
	      <div id="avartarCont" style="z-index:100;position:absolute;margin-left:20px;;margin-top:-100px;" onmouseover="showhelp()" onmouseout="hidehelp()">
	        <img id="avartar" src="Home/pics/clippitwink.gif" style="width:100px;" alt=""/>
		<div id="avartarText" style="position:absolute;margin-left:0px;;margin-top:-92px;background:#FFFFE1;height:130px;width:270px;border:1px inset silver;visibility:hidden;">
		   <table style="width:100%;height:100%;text-align:center;" cellspacing="0" cellpadding="0">
   		     <tr>
   	 	       <td style="padding:2px 2px 8px 2px">
			<span style="line-height:21px;text-decoration:underline">Welcome to www.clemen.dk/<span style="color:blue;font-weight:bold">sc</span></span><br/>
      You can use the menu located in the left panel to traverse the pages. <br/>
			By clicking the help-item you can gain further infomation regarding a specific page. .<br/>
			<span style="line-height:3px;"><br/></span>Enjoy your stay<br/>Regards <i>Simon Clemen</i>. 
			 
			</td>
  	             </tr>
		     <tr>
   	 	       <td style="height:14px;border-top:1px solid silver;text-align:left;">
				<table cellpadding="0" cellspacing="0" style="width:100%">
					<tr>
						<td style="width:17px;padding:0px 0px 0px 5px;">
						   <button onmouseup="doHelp();" style="background:#FFFFE1;margin:0px;border:0px;background:"><img style="margin-top:1px;" src="Home/pics/icons/help.gif" alt=""/></button>
						</td>
						<td style="width:20px;text-align:left">							
						   Help
						</td>
						<td style="text-align:center">
							<table>
							  <tr>
							    <xsl:choose>
        						      <xsl:when test='number(msgcount/text())=0'>
		 					       <td style="text-align:right;width:17px;">
						                 <button onmouseup="doMsg();" style="background:#FFFFE1;margin:0px;border:0px;background:"><img style="margin-top:1px;" src="Home/pics/icons/msg2.gif" alt=""/></button>
						               </td>
							       <td style="color:silver">Messages :
		 					         --
							       </td>
							      </xsl:when>
							      <xsl:otherwise>
		 					       <td style="text-align:right;width:17px;">
						                 <button onmouseup="doMsg();" style="background:#FFFFE1;margin:0px;border:0px;background:"><img style="margin-top:1px;" src="Home/pics/icons/msg2.gif" alt=""/></button>
						               </td>
							       <td style="">Messages :
		 					         <xsl:value-of disable-output-escaping="yes" select="msgcount/text()"/>
							       </td>
							      </xsl:otherwise>

							    </xsl:choose>
							  </tr>							   
							</table>
						</td>
						<td style="width:17px">
		 	  	                   <button onmouseup="doInfo();" style="background:#FFFFE1;margin:0px;border:0px;background:"><img style="margin-top:1px;" src="Home/pics/icons/info.gif" alt=""/></button>
						</td>
						<td style="padding:0px 7px 0px 0px;width:20px;text-align:right">							
						   Info
						</td>
					</tr>
				</table>
			</td>
  	             </tr>
	
		   </table>
  	        </div>
	      </div>
	    </td>	
	 </tr>
	    <tr class="menuRow">
	      <td>
		<div>	
		  <table class="fullTable" cellspacing="0" cellpadding="0">	
		    <tr id="statusrows">	
		      <td style="display:block;width:30px;" class="statusImg"><img id="statusImg" src="Home/pics/progress_bar.gif" style="border:0px;" alt="Site Running."/></td>
		      <td id="status" class="statusText"><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text></td>
		      <td class="status"></td>
		      <td class="status">User<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>:<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text></td>
		      <td class="statusLable">
		        <table>
		          <tr>
			    <td class="statusUserID" id="UserID"><xsl:value-of disable-output-escaping="yes" select="pageinfo/userid/text()"/></td>
			  </tr>
			</table>
		      </td>
		      <td class="status">Location<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>:<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text></td>
		      <td class="statusLable">
	                <table>
        	          <tr>
	                    <td class="statusUserID" id="locationID"><xsl:value-of disable-output-escaping="yes" select="pageinfo/pagelocation/text()"/></td>
	                  </tr>
	                </table>
	              </td>
		    </tr>			
		  </table>
	 	  <table class="fullTable" cellspacing="0" cellpadding="0">	
		    <tr id="statusIcons" class="descriptionRow">
		       <td class="copyright">All content on this page is Copyright &#169; <xsl:value-of select="pageinfo/systemyear/text()"/>, <a class="mail" href="mailto:{pageinfo/author/mail/text()}">
				<xsl:value-of select="pageinfo/author/name/text()"/>
				</a><xsl:text> &amp; </xsl:text>
				<a class="mail" href="mailto:{pageinfo/coauthor/mail/text()}">
				<xsl:value-of disable-output-escaping="yes" select="pageinfo/coauthor/name/text()"/>.</a>

		       </td>
	 	       <td rowspan="2" style="text-align:right;" >
		      	 <a style="text-decoration:none" href="http://validator.w3.org/check/referer">
		          <img src="Home/pics/xhtml11.bmp" style="border:0px;"
	         	  alt="Valid XHTML 1.1!" height="31" width="88" /></a>
	         	 <a style="text-decoration:none" href="http://jigsaw.w3.org/css-validator/check/referer" >
			   <img style="border:0;width:88px;height:31px" src="Home/pics/css.bmp" alt="Valid CSS!" />
		         </a>
		       </td>							
		    </tr>
		    <tr class="descriptionRow">
		       <td class="copyright">Last Update :  <span class="update"><xsl:value-of select="pageinfo/lastupdate/text()"/> </span>,&#160;&#160; Visitor #: <span class="update"><xsl:value-of select="pageinfo/visitcounter/text()"/></span></td>
		    </tr>
		  </table>
	        </div>		
	      </td>
	    </tr>
	  </table>	
	</div>	
     </form> 
     <script type="text/javascript">
       var userOnload = onload;
       var userResize = null;
     
       onload=init;
       function init(){	   
         userResize = window.onresize;
         window.onresize=doResize;
         sizeIt();
	 initSecMenu()
	 if(userOnload) userOnload();
       }
       function doResize(){
         if (userResize) userResize();
	 sizeIt();
       }
     </script> 
   </body>
  </html>
</xsl:template>

<!--Matches primarymenu, and recursively builds the menu. -->
<xsl:template match="primarymenu">
     <xsl:apply-templates select="menu"/>		
     <xsl:apply-templates select="toolbar"/>		

</xsl:template>

<xsl:template match="menu">
     <div id='menubar' class='menubar'>
	<table class='menu' cellspacing='1'>
	  <tr id='menurow'>
	     <xsl:apply-templates select="menusection"/>
	     <td></td>
     	  </tr>
     	</table>
     </div>
     <xsl:for-each select="menusection"> 	
       <xsl:call-template name="menusectionBody"/>
     </xsl:for-each>

</xsl:template>

<xsl:template match="menusection">
	<td style='width:1px;'>
		<div id='tpos{position()}' style='position:absolute;'>
			<xsl:text> </xsl:text>
		</div>
	</td>
	<td onmouseout='doMouseOutHead(this)' onmouseover='doMouseOverHead(this,{position()});removehightlight()'  onmouseup='doMouseDownHead({position()});' class='menuhead'>
		<xsl:value-of disable-output-escaping="yes" select="@title"/>
	</td>
</xsl:template>


<xsl:template name="menusectionBody">
       <div id='menu{position()}' onmouseout='if (extendlevel==0) removehightlight();' class='dragmenu' >
           <table style='width:100%' cellspacing='0' cellpadding='1'>
  	       <tr>
                   <td>
	                <table class='menucontent' cellspacing='0' cellpadding='0'>
				<xsl:apply-templates select="item">				  
				  <xsl:with-param name="menulevel" select = "0"/>
				</xsl:apply-templates>
				<tr><td></td></tr>
			</table>
		   </td>
	       </tr>
	   </table>
       </div>		
</xsl:template>


<xsl:template match="item">
      <xsl:param name='menulevel' />
      <xsl:choose>
        <xsl:when test='submenu'>
		<tr id="{@id}" onmouseover="javascript:highlight({$menulevel},this);openExtendMenu(this,{$menulevel});" onmouseup="hitMenu=true;window.event.cancelBubble = true;masterDown()"  class="menuitemRow">
		       <xsl:call-template name="menuelement">		
			  <xsl:with-param name="menuid" select = "@id"/>
			  <xsl:with-param name="menuaction" select = "action/text()"/>
			  <xsl:with-param name="menutext" select = "itemtitle/text()"/>
		       </xsl:call-template>
	              <td class='menuspace' onmouseup='document.getElementById("{@id}menutab").onmouseup();window.event.cancelBubble = true;masterDown()'>			
		          <xsl:apply-templates select="submenu">				  			
 			      <xsl:with-param name="menuid" select = "@id"/>
	            	  </xsl:apply-templates>
	              </td>
		</tr>
       </xsl:when>
        <xsl:otherwise>
              	<tr id="{@id}" onmouseover="highlight({$menulevel},this);" onmouseup="hitMenu=true;window.event.cancelBubble = true;masterDown()"  class="menuitemRow">
		       <xsl:call-template name="menuelement">		
			  <xsl:with-param name="menuid" select = "@id"/>
			  <xsl:with-param name="menuaction" select = "action/text()"/>
			  <xsl:with-param name="menutext" select = "itemtitle/text()"/>
		       </xsl:call-template>
	              <td class='menuspace' onmouseup='document.getElementById("{@id}menutab").onmouseup();window.event.cancelBubble = true;masterDown()'>			
		          <xsl:apply-templates select="submenu">				  			
 			      <xsl:with-param name="menuid" select = "@id"/>
	            	  </xsl:apply-templates>
	              </td>
		</tr>
        </xsl:otherwise>
      </xsl:choose>
 </xsl:template>

<xsl:template name="menuelement">
  <xsl:param name='menuid' />
  <xsl:param name='menuaction' />
  <xsl:param name='menutext' />

	   <td class="menuspace" id='{$menuid}_check' onmouseup='document.getElementById("{$menuid}menutab").onmouseup();window.event.cancelBubble = true;masterDown()'>
	   </td>
  	   <td class='menuitem' onmouseup='if (this.className=="menuitem") {$menuaction};window.event.cancelBubble = true;masterDown()'  id='{$menuid}menutab'>
		<xsl:value-of disable-output-escaping="yes" select="$menutext"/>		
	   </td>
</xsl:template>

<xsl:template match="submenu">
  <xsl:param name='menuid' />
	<img src='Home/pics/expand.gif' alt=''/>
	<div id='{$menuid}_menu' style='visibility:hidden;color:black;position:absolute;' class='dragmenu'>
	   <table style='width:100%' cellspacing='0' cellpadding='1'>
	       <tr>
	          <td>
	             <table class='menucontent' id='{$menuid}_menu_table' cellspacing='0' cellpadding='0' >
	         	<xsl:apply-templates select="item">				  	   
			  <xsl:with-param name="menulevel" select = "@level"/>
			</xsl:apply-templates>

		     </table>
		  </td>
	       </tr>
	   </table>
	</div>
</xsl:template>


<xsl:template match="toolbar">
    	<xsl:apply-templates select="tbar"/>				  	   	
</xsl:template>

<xsl:template match="tbar">
       <div id='{@id}_bar' class='iconbar' >
          <table class='iconbar' cellspacing='0' cellpadding='0'>
              <tr id='{@id}'>
                 <td style='width:4px;'></td>
	         <xsl:apply-templates select="toolbarsection"/>				  	   	
		 <td style='border-left:1px solid #FFFFFF;'><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text></td>
	      </tr>
          </table>
       </div>
</xsl:template>

<xsl:template match="toolbarsection">
      <xsl:choose>
        <xsl:when test='position()=1'>
          <td style='width:10px;border-right:1px solid silver;'>
	    <div style='text-align:center;'>
	        <table style='' cellspacing='0' cellpadding='0'>
	             <tr style='text-align:left;height:20px;' id='{@id}'>
 			  <td style='width:10px;'><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text></td>
 	 	          <xsl:apply-templates select="toolbaritem"/> 	
	 	     </tr>
	 	</table>
	    </div> 
	</td>

       </xsl:when>
       <xsl:otherwise>
	<td style='width:10px;border-right:1px solid silver;border-left:1px solid #FFFFFF;'>    		
	    <div style='text-align:center;'>
	        <table style='' cellspacing='0' cellpadding='0'>
	             <tr style='text-align:left;height:20px;' id='{@id}'>
 			  <td style='width:10px;'><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text></td>
 	 	          <xsl:apply-templates select="toolbaritem"/> 	
	 	     </tr>
	 	</table>
	    </div> 
	</td>
       </xsl:otherwise>
    </xsl:choose>

</xsl:template>

<xsl:template match="toolbaritem">			
	 <td class='icon'>
	     <img  class='activeicon' id='{@id}' src='{img/@src}' alt='' title=''  onmouseover='setTitle("{itemtitle/text()}",this);if (this.className=="activeicon") this.style.border="1px outset #FFFFFF";' onmouseout='if (this.className=="activeicon") this.style.border="1px solid #DDDDDD"' style='border:1px solid #DDDDDD;margin-top:3px; ' onmousedown='if (this.className=="activeicon") this.style.border="1px inset #EEEEEE"' onmouseup='if (allowIconHit(this)) document.getElementById(this.id.substring(0,this.id.length-4)+"menutab").onmouseup();'/>
	 </td>     		
</xsl:template>



<!--Builds the vertical dots -->
<xsl:template name="doRepeatDots">
  <xsl:param name='repeatCounter' />
	<xsl:if test="number($repeatCounter) >= 0">
	   <span class='ExpandVertDots' style='margin-left:{3-((number($repeatCounter))*17)}px;'>
		<img src='Home/pics/dots.gif' alt=''/> 
	   </span>
	   <xsl:call-template name="doRepeatDots">
		   <xsl:with-param name="repeatCounter" select="$repeatCounter - 1"/>
	  </xsl:call-template>
 </xsl:if>

</xsl:template>

<!--Matches secondarymenu, and recursively builds the menu. -->
<xsl:template match="secondarymenu">

  <xsl:for-each select="menu"> 	
    <td class="sideBar">					   
      <div id="sidebar{position()}" class="sideBar">
        <xsl:variable name = "style" select = "menusection/@style"/>
        <xsl:for-each select="menusection/item"> 	
        <xsl:choose>
	  <xsl:when test='number($style)=2'>		
            <xsl:call-template name="secondaryitem">			
	      <xsl:with-param name="menulevel" select = "0"/>	
	      <xsl:with-param name="menuid" select = "@id"/>	
	      <xsl:with-param name="picexpanded">Home/pics/icons/arrow2_icon.gif</xsl:with-param>	
	      <xsl:with-param name="picnotexpanded">Home/pics/icons/arrow1_icon.gif</xsl:with-param>	
	      <xsl:with-param name="connectdots">0</xsl:with-param>	
	      <xsl:with-param name="childconnectlevel">0</xsl:with-param>	
	      <xsl:with-param name="thisconnectlevel">0</xsl:with-param>	

	    </xsl:call-template>
	  </xsl:when>		
	  <xsl:when test='number($style)=1'>		
      <xsl:call-template name="secondaryitem">			
	      <xsl:with-param name="menulevel" select = "0"/>	
	      <xsl:with-param name="menuid" select = "@id"/>	
	      <xsl:with-param name="picexpanded">Home/pics/isexpand.gif</xsl:with-param>	
	      <xsl:with-param name="picnotexpanded">Home/pics/notexpand.gif</xsl:with-param>	
	      <xsl:with-param name="connectdots">1</xsl:with-param>	
	      <xsl:with-param name="childconnectlevel">0</xsl:with-param>	
	      <xsl:with-param name="thisconnectlevel">0</xsl:with-param>	
	    </xsl:call-template>
	  </xsl:when>		
        </xsl:choose>
      </xsl:for-each> 	
      </div>
    </td>
  </xsl:for-each> 	
</xsl:template>




<!--Executed on items in the secondary menu. -->
<xsl:template name="secondaryitem">
  <xsl:param name='menulevel' />
  <xsl:param name='menuid' />
  <xsl:param name='connectdots' />
  <xsl:param name='thisconnectlevel' />
  <xsl:param name='picexpanded' />
  <xsl:param name='picnotexpanded' />
  <xsl:param name='childconnectlevel' />

  <div class='admrow' id='{@id}' style="cursor:hand;">
    <span class='Expanditem' style='margin-left:{17*($menulevel)}px'>	
        <xsl:choose>
          <xsl:when test='$menulevel!=0'>
	        <xsl:choose>
        	  <xsl:when test='$connectdots!=0'>
		    <span id='{@id}_1'  class='ExpandHorzDots'>.....................</span>
	  	    <xsl:call-template name="doRepeatDots">			
  			<xsl:with-param name="repeatCounter" select = "$thisconnectlevel"/>	
		    </xsl:call-template>
		  </xsl:when>
	        </xsl:choose>  	
	  </xsl:when>
        </xsl:choose>

      <span style='z-index:100;'><xsl:text> </xsl:text>
        <xsl:choose>
          <xsl:when test='submenu'>
           <xsl:choose>
             <xsl:when test='number(active/@on)=0'>
               <img id='{@id}_2' class='ExpandImg'  onmouseover='document.getElementById(this.id.substring(0,this.id.length-2)+ "_4").style.textDecoration = "underline"' onmouseout='document.getElementById(this.id.substring(0,this.id.length-2) + "_4").style.textDecoration = "none"' onmouseup='document.getElementById(this.id.substring(0,this.id.length-2) + "_4").onmouseup()' src="{$picnotexpanded}" alt=""/>
             </xsl:when>
	     <xsl:when test='number(active/@on)=1'>
               <img id='{@id}_2' class='ExpandImg'  onmouseover='document.getElementById(this.id.substring(0,this.id.length-2)+ "_4").style.textDecoration = "underline"' onmouseout='document.getElementById(this.id.substring(0,this.id.length-2) + "_4").style.textDecoration = "none"' onmouseup='document.getElementById(this.id.substring(0,this.id.length-2) + "_4").onmouseup()' src="{$picexpanded}" alt=""/>
             </xsl:when>
           </xsl:choose>

  	  </xsl:when>
        </xsl:choose>
      </span>
      <xsl:choose>
	<xsl:when test='img/@src!=""'>
   	  <img id='{@id}_3' class='ExpanditemImg' onmouseover='document.getElementById(this.id.substring(0,this.id.length-2)+ "_4").style.textDecoration = "underline"' onmouseout='document.getElementById(this.id.substring(0,this.id.length-2) + "_4").style.textDecoration = "none"' onmouseup='document.getElementById(this.id.substring(0,this.id.length-2) + "_4").onmouseup()'  src='Home/{img/@src}' alt=""/>
	</xsl:when>
      </xsl:choose>
      <span id='{@id}_4' class='ExpandText' onmouseover='document.getElementById(this.id.substring(0,this.id.length-2)+ "_4").style.textDecoration = "underline"' onmouseout='document.getElementById(this.id.substring(0,this.id.length-2) + "_4").style.textDecoration = "none"' onmouseup='{action/text()};changeExpand(this,"{$picexpanded}","{$picnotexpanded}");'>
         <xsl:value-of disable-output-escaping="yes" select="itemtitle/text()"/>
      </span>
  </span>			
  </div>
  <xsl:choose>
    <xsl:when test='submenu'>
     <xsl:choose>
      <xsl:when test='number(active/@on)=1'>
       <table class='ExpandsubtreeWrapper' style='display:block' cellspacing='0' cellpadding='0' id='sub{$menuid}' >
        <tr>
	  <td>
	     <xsl:for-each select="submenu/item"> 	
	       <xsl:choose>
	          <xsl:when test='following-sibling::*'>		
	             <xsl:call-template name="secondaryitem">			
			 <xsl:with-param name="menulevel" select = "$menulevel+1"/>	
			 <xsl:with-param name="menuid" select = "@id"/>	
			 <xsl:with-param name="picexpanded" select = "$picexpanded"/>	
		         <xsl:with-param name="picnotexpanded" select = "$picnotexpanded"/>	
		         <xsl:with-param name="connectdots" select = "$connectdots"/>	
		         <xsl:with-param name="childconnectlevel" select = "$childconnectlevel+1"/>			
		         <xsl:with-param name="thisconnectlevel" select = "$childconnectlevel"/>			
	             </xsl:call-template>
		  </xsl:when>	
	          <xsl:otherwise>
		     <xsl:call-template name="secondaryitem">			
			 <xsl:with-param name="menulevel" select = "$menulevel+1"/>	
			 <xsl:with-param name="menuid" select = "@id"/>	
			 <xsl:with-param name="picexpanded" select = "$picexpanded"/>	
		         <xsl:with-param name="picnotexpanded" select = "$picnotexpanded"/>	
		         <xsl:with-param name="connectdots" select = "$connectdots"/>	
		         <xsl:with-param name="childconnectlevel" select = "$childconnectlevel"/>			
		         <xsl:with-param name="thisconnectlevel" select = "$childconnectlevel"/>			
	             </xsl:call-template>
		  </xsl:otherwise>
		</xsl:choose>
	     </xsl:for-each> 	
	  </td>
        </tr>
      </table>
     </xsl:when>
      <xsl:when test='number(active/@on)=0'>
       <table class='ExpandsubtreeWrapper' style='display:none' cellspacing='0' cellpadding='0' id='sub{$menuid}' >
        <tr>
	  <td>
	     <xsl:for-each select="submenu/item"> 	
	       <xsl:choose>
	          <xsl:when test='following-sibling::*'>		
	             <xsl:call-template name="secondaryitem">			
			 <xsl:with-param name="menulevel" select = "$menulevel+1"/>	
			 <xsl:with-param name="menuid" select = "@id"/>	
			 <xsl:with-param name="picexpanded" select = "$picexpanded"/>	
		         <xsl:with-param name="picnotexpanded" select = "$picnotexpanded"/>	
		         <xsl:with-param name="connectdots" select = "$connectdots"/>	
		         <xsl:with-param name="childconnectlevel" select = "$childconnectlevel+1"/>			
		         <xsl:with-param name="thisconnectlevel" select = "$childconnectlevel"/>			
	             </xsl:call-template>
		  </xsl:when>	
	          <xsl:otherwise>
		     <xsl:call-template name="secondaryitem">			
			 <xsl:with-param name="menulevel" select = "$menulevel+1"/>	
			 <xsl:with-param name="menuid" select = "@id"/>	
			 <xsl:with-param name="picexpanded" select = "$picexpanded"/>	
		         <xsl:with-param name="picnotexpanded" select = "$picnotexpanded"/>	
		         <xsl:with-param name="connectdots" select = "$connectdots"/>	
		         <xsl:with-param name="childconnectlevel" select = "$childconnectlevel"/>			
		         <xsl:with-param name="thisconnectlevel" select = "$childconnectlevel"/>			
	             </xsl:call-template>
	  
		  </xsl:otherwise>
		</xsl:choose>

	     </xsl:for-each> 	
	  </td>
        </tr>
      </table>
     </xsl:when>

    </xsl:choose>
    </xsl:when>
  </xsl:choose>
</xsl:template>


</xsl:stylesheet>
