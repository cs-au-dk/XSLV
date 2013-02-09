<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns="http://www.w3.org/1999/xhtml">

	<xsl:template match="ListOfEmails">
		<LINK REL="stylesheet" TYPE="text/css" HREF="./email_list.css" TITLE="Style"/>
		<HTML>
		<HEAD>
		<TITLE>Bangalore Network</TITLE>
		</HEAD>
		<BODY>
		<CENTER>
		<H1>Welcome to the Bangalore Network!</H1>
		<SMALL><SMALL>
		    <P>Brought to you by <a href="mailto:lasse@daimi.au.dk">Shanti Lassi</a>
		    <BR/>
		    Last updated: <xsl:value-of select="UpdateDate"/></P>
		</SMALL></SMALL>
		<P>Hi! You have stumbled upon the homepage of the Bangalore Network. We are a bunch of foreigners trying to get by in the bustling city of Bangalore, India. To this end, we have a network of contacts to eachother and some external resources, namely the list of emails below.</P>
			
		<P>Use this link to get in touch with everyone in Bangalore:
		    <xsl:text disable-output-escaping="yes">&lt;A class="email" href="mailto: </xsl:text>
			<xsl:call-template name="BangaloreEmails"/>
			<xsl:text disable-output-escaping="yes">"></xsl:text>
		    <xsl:call-template name="BangaloreEmails"/>
			<xsl:text disable-output-escaping="yes">&lt;/A></xsl:text>
		</P>	
		
		<xsl:call-template name="Trainees"/>
		<HR/>
		<xsl:call-template name="theRest"/>
		<HR/>
		<xsl:call-template name="AIESECers"/>
		<HR/>
		<xsl:call-template name="Extrainees"/>
		<HR/>
		<xsl:call-template name="theExRest"/>
		<HR/>
		<P>Use this link to address everyone on the page:
		    <xsl:text disable-output-escaping="yes">&lt;A class="email" href="mailto: </xsl:text>
			<xsl:call-template name="AllEmails"/>
			<xsl:text disable-output-escaping="yes">"></xsl:text>
		    <xsl:call-template name="AllEmails"/>
			<xsl:text disable-output-escaping="yes">&lt;/A></xsl:text>
		</P>
		<HR/>
		<P>If you have comments, suggestions or corrections, please do not hesitate to email the webwaster: <A href="mailto: lasse@daimi.au.dk">Shanti Lassi</A></P>
        </CENTER>
		</BODY>
		</HTML>
	</xsl:template>

  	<xsl:template match="Person">
		<TR>
			<xsl:if test="position() mod 2 = 0">
				<xsl:attribute name="id">tableelement</xsl:attribute>
			</xsl:if>
		<TD>
			<xsl:value-of select="Name/FirstName"/>
			<xsl:text> </xsl:text>
			<xsl:value-of select="Name/LastName"/>
			<xsl:if test="not(Name/NickName = '')">
			    <BR/>
			    <xsl:text>(</xsl:text>
			    <xsl:value-of select="Name/NickName"/>
			    <xsl:text>)</xsl:text>
			</xsl:if>
		</TD>
		
		<TD>
		    <xsl:if test="not(Address/CO = '')">
		        <xsl:text>CO/ </xsl:text>
		        <xsl:value-of select="Address/CO"/>
		        <BR/>
		    </xsl:if>
		    <xsl:value-of select="Address/Street"/>
		    <BR/>
		    <xsl:value-of select="Address/City"/>
		    <BR/>
		    <xsl:value-of select="Address/Country"/>
		</TD>
		
		<TD>
		    <xsl:if test="not(ContactInfo/Email = '')">
		        <xsl:text disable-output-escaping="yes">
				&lt;A class="email" href="mailto:
			    </xsl:text>
			    <xsl:value-of select="ContactInfo/Email"/>
			    <xsl:text disable-output-escaping="yes">
				    ">
			    </xsl:text>
			    <xsl:value-of select="ContactInfo/Email"/>
			    <xsl:text disable-output-escaping="yes">
				    &lt;/A>
			    </xsl:text>
		        <BR/>
		    </xsl:if>
		    <xsl:if test="not(ContactInfo/Phone = '')">
		        <xsl:text>Phone: </xsl:text>
		        <xsl:value-of select="ContactInfo/Phone"/>
			    <BR/>
			</xsl:if>
		    <xsl:if test="not(ContactInfo/Mobile = '')">
		        <xsl:text>Mobile: </xsl:text>
		        <xsl:value-of select="ContactInfo/Mobile"/>
			    <BR/>
			</xsl:if>
		    <xsl:if test="not(ContactInfo/ICQ = '')">
		        <xsl:text>ICQ# </xsl:text>
		        <xsl:value-of select="ContactInfo/ICQ"/>
			    <BR/>
			</xsl:if>
		</TD>
		<TD align="center">
            <xsl:value-of select="BirthDate"/>
		</TD>
		<xsl:if test="(ContactInfo/Home != '')">
		    <TD>
		        <xsl:value-of select="ContactInfo/Home/Name"/>
		        <BR/>
		        <xsl:value-of select="ContactInfo/Home/Phone"/>
		    </TD>
		</xsl:if>
		</TR>
  	</xsl:template>
	
	<xsl:template name="AllEmails">
		<xsl:for-each select="Person/ContactInfo/Email">
			<xsl:value-of select="text()"/>
			<xsl:text>, </xsl:text>
		</xsl:for-each> 
	</xsl:template>
	
	<xsl:template name="BangaloreEmails">
		<xsl:for-each select="Person[Location = 'Bangalore']/ContactInfo/Email">
			<xsl:value-of select="text()"/>
			<xsl:text>, </xsl:text>
		</xsl:for-each> 
	</xsl:template>
	
	
	<xsl:template match="Person/ContactInfo/Email">
        <xsl:value-of select="text()"/>
        <xsl:text>, </xsl:text>
	</xsl:template>
		
	<xsl:template name="Trainees">
	    <TABLE border="0" rules="none" cellpadding="5" cellspacing="0" summary="List of Emails of the Bangalore Trainees">
		<CAPTION>The Trainees</CAPTION>
		<TR id="tableheader">
		<TH>Name</TH>
		<TH>Address</TH>
		<TH>Contact Info</TH>
		<TH>BirthDate</TH>
		<TH>Emergency Info</TH>
		</TR>
		<xsl:apply-templates select="Person[@group='Trainees']">
			<xsl:sort select="Name/FirstName"/>
		</xsl:apply-templates>
		</TABLE>
		<P>Email all the Trainees: 
		<xsl:text disable-output-escaping="yes">&lt;A class="email" href="mailto: </xsl:text>
		<xsl:apply-templates select="Person[@group = 'Trainees']/ContactInfo/Email"/>
		<xsl:text disable-output-escaping="yes">"></xsl:text>
		<xsl:apply-templates select="Person[@group = 'Trainees']/ContactInfo/Email"/>
		<xsl:text disable-output-escaping="yes">&lt;/A></xsl:text>
		</P>
	</xsl:template>
    
	<xsl:template name="AIESECers">
	    <TABLE border="0" rules="none" cellpadding="5" cellspacing="0" summary="List of Emails of the Bangalore Trainees">
		<CAPTION>The AIESEC'ers</CAPTION>
		<TR id="tableheader">
		<TH>Name</TH>
		<TH>Address</TH>
		<TH>Contact Info</TH>
		<TH>BirthDate</TH>
		</TR>
		<xsl:apply-templates select="Person[@group='AIESECers']">
			<xsl:sort select="Name/FirstName"/>
		</xsl:apply-templates>
		</TABLE>
		<P>Email all the AIESEC'ers: 
		<xsl:text disable-output-escaping="yes">&lt;A class="email" href="mailto: </xsl:text>
		<xsl:apply-templates select="Person[@group = 'AIESECers']/ContactInfo/Email"/>
		<xsl:text disable-output-escaping="yes">"></xsl:text>
		<xsl:apply-templates select="Person[@group = 'AIESECers']/ContactInfo/Email"/>
		<xsl:text disable-output-escaping="yes">&lt;/A></xsl:text>
		</P>
	</xsl:template>
    
	<xsl:template name="Extrainees">
	    <TABLE border="0" rules="none" cellpadding="5" cellspacing="0" summary="List of Emails of the Bangalore Trainees">
		<CAPTION>The ex-Trainees</CAPTION>
		<TR id="tableheader">
		<TH>Name</TH>
		<TH>Address</TH>
		<TH>Contact Info</TH>
		<TH>BirthDate</TH>
		</TR>
		<xsl:apply-templates select="Person[@group='Extrainees']">
			<xsl:sort select="Name/FirstName"/>
		</xsl:apply-templates>
		</TABLE>
		<P>Email all the ex-Trainees: 
		<xsl:text disable-output-escaping="yes">&lt;A class="email" href="mailto: </xsl:text>
		<xsl:apply-templates select="Person[@group = 'Extrainees']/ContactInfo/Email"/>
		<xsl:text disable-output-escaping="yes">"></xsl:text>
		<xsl:apply-templates select="Person[@group = 'Extrainees']/ContactInfo/Email"/>
		<xsl:text disable-output-escaping="yes">&lt;/A></xsl:text>
		</P>
	</xsl:template>
    
	<xsl:template name="theRest">
	    <TABLE border="0" rules="none" cellpadding="5" cellspacing="0" summary="List of Emails of the Bangalore Trainees">
		<CAPTION>The Hangarounds</CAPTION>
		<TR id="tableheader">
		<TH>Name</TH>
		<TH>Address</TH>
		<TH>Contact Info</TH>
		<TH>BirthDate</TH>
		</TR>
		<xsl:apply-templates select="Person[@group='theRest']">
			<xsl:sort select="Name/FirstName"/>
		</xsl:apply-templates>
		</TABLE>
		<P>Email all the hang-arounds: 
		<xsl:text disable-output-escaping="yes">&lt;A class="email" href="mailto: </xsl:text>
		<xsl:apply-templates select="Person[@group = 'theRest']/ContactInfo/Email"/>
		<xsl:text disable-output-escaping="yes">"></xsl:text>
		<xsl:apply-templates select="Person[@group = 'theRest']/ContactInfo/Email"/>
		<xsl:text disable-output-escaping="yes">&lt;/A></xsl:text>
		</P>
	</xsl:template>
    
	<xsl:template name="theExRest">
	    <TABLE border="0" rules="none" cellpadding="5" cellspacing="0" summary="List of Emails of the Bangalore Trainees">
		<CAPTION>The Ex-Hangarounds</CAPTION>
		<TR id="tableheader">
		<TH>Name</TH>
		<TH>Address</TH>
		<TH>Contact Info</TH>
		<TH>BirthDate</TH>
		</TR>
		<xsl:apply-templates select="Person[@group='theExRest']">
			<xsl:sort select="Name/FirstName"/>
		</xsl:apply-templates>
		</TABLE>
		<P>Email all the rest: 
		<xsl:text disable-output-escaping="yes">&lt;A class="email" href="mailto: </xsl:text>
		<xsl:apply-templates select="Person[@group = 'theExRest']/ContactInfo/Email"/>
		<xsl:text disable-output-escaping="yes">"></xsl:text>
		<xsl:apply-templates select="Person[@group = 'theExRest']/ContactInfo/Email"/>
		<xsl:text disable-output-escaping="yes">&lt;/A></xsl:text>
		</P>
	</xsl:template>
    
</xsl:stylesheet>








