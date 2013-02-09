<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml" version="1.0">

	<xsl:template match="ListOfEmails">
		<link rel="stylesheet" type="text/css" href="./email_list.css" title="Style"/>
		<html>
		<head>
		<title>Bangalore Network</title>
		</head>
		<body>
		<center>
		<h1>Welcome to the Bangalore Network!</h1>
		<small><small>
		    <p>Brought to you by <a href="mailto:lasse@daimi.au.dk">Shanti Lassi</a>
		    <br/>
		    Last updated: <xsl:value-of select="UpdateDate"/></p>
		</small></small>
		<p>Hi! You have stumbled upon the homepage of the Bangalore Network. We are a bunch of foreigners trying to get by in the bustling city of Bangalore, India. To this end, we have a network of contacts to eachother and some external resources, namely the list of emails below.</p>
			
		<p>Use this link to get in touch with everyone in Bangalore:
		    <a><xsl:attribute name="class">email</xsl:attribute><xsl:attribute name="href">mailto: 
            <xsl:call-template name="BangaloreEmails"/></xsl:attribute>
		        <xsl:call-template name="BangaloreEmails"/>
        </a>
		</p>	
		
		<xsl:call-template name="Trainees"/>
		<hr/>
		<xsl:call-template name="theRest"/>
		<hr/>
		<xsl:call-template name="AIESECers"/>
		<hr/>
		<xsl:call-template name="Extrainees"/>
		<hr/>
		<xsl:call-template name="theExRest"/>
		<hr/>
		<p>Use this link to address everyone on the page:
		    <a><xsl:attribute name="class">email</xsl:attribute><xsl:attribute name="href">mailto: 
            <xsl:call-template name="AllEmails"/></xsl:attribute>
		        <xsl:call-template name="AllEmails"/>
        </a>
		</p>
		<hr/>
		<p>If you have comments, suggestions or corrections, please do not hesitate to email the webwaster: <a href="mailto: lasse@daimi.au.dk">Shanti Lassi</a></p>
        </center>
		</body>
		</html>
	</xsl:template>

  	<xsl:template match="Person">
		<tr>
			<xsl:if test="position() mod 2 = 0">
				<xsl:attribute name="id">tableelement</xsl:attribute>
			</xsl:if>
		<td>
			<xsl:value-of select="Name/FirstName"/>
			<xsl:text> </xsl:text>
			<xsl:value-of select="Name/LastName"/>
			<xsl:if test="not(Name/NickName = '')">
			    <br/>
			    <xsl:text>(</xsl:text>
			    <xsl:value-of select="Name/NickName"/>
			    <xsl:text>)</xsl:text>
			</xsl:if>
		</td>
		
		<td>
		    <xsl:if test="not(Address/CO = '')">
		        <xsl:text>CO/ </xsl:text>
		        <xsl:value-of select="Address/CO"/>
		        <br/>
		    </xsl:if>
		    <xsl:value-of select="Address/Street"/>
		    <br/>
		    <xsl:value-of select="Address/City"/>
		    <br/>
		    <xsl:value-of select="Address/Country"/>
		</td>
		
		<td>
		    <xsl:if test="not(ContactInfo/Email = '')">
		    <a><xsl:attribute name="class">email</xsl:attribute><xsl:attribute name="href">mailto: 
			      <xsl:value-of select="ContactInfo/Email"/></xsl:attribute>
			      <xsl:value-of select="ContactInfo/Email"/>
        </a>
		        <br/>
		    </xsl:if>
		    <xsl:if test="not(ContactInfo/Phone = '')">
		        <xsl:text>Phone: </xsl:text>
		        <xsl:value-of select="ContactInfo/Phone"/>
			    <br/>
			</xsl:if>
		    <xsl:if test="not(ContactInfo/Mobile = '')">
		        <xsl:text>Mobile: </xsl:text>
		        <xsl:value-of select="ContactInfo/Mobile"/>
			    <br/>
			</xsl:if>
		    <xsl:if test="not(ContactInfo/ICQ = '')">
		        <xsl:text>ICQ# </xsl:text>
		        <xsl:value-of select="ContactInfo/ICQ"/>
			    <br/>
			</xsl:if>
		</td>
		<td align="center">
            <xsl:value-of select="BirthDate"/>
		</td>
		<xsl:if test="(ContactInfo/Home != '')">
		    <td>
		        <xsl:value-of select="ContactInfo/Home/Name"/>
		        <br/>
		        <xsl:value-of select="ContactInfo/Home/Phone"/>
		    </td>
		</xsl:if>
		</tr>
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
	    <table border="0" rules="none" cellpadding="5" cellspacing="0" summary="List of Emails of the Bangalore Trainees">
		<caption>The Trainees</caption>
		<tr id="tableheader">
		<th>Name</th>
		<th>Address</th>
		<th>Contact Info</th>
		<th>BirthDate</th>
		<th>Emergency Info</th>
		</tr>
		<xsl:apply-templates select="Person[@group='Trainees']">
			<xsl:sort select="Name/FirstName"/>
		</xsl:apply-templates>
		</table>
		<p>Email all the Trainees: 
		    <a><xsl:attribute name="class">email</xsl:attribute><xsl:attribute name="href">mailto: 
      		<xsl:apply-templates select="Person[@group = 'Trainees']/ContactInfo/Email"/></xsl:attribute>
      		<xsl:apply-templates select="Person[@group = 'Trainees']/ContactInfo/Email"/>
        </a>
		</p>
	</xsl:template>
    
	<xsl:template name="AIESECers">
	    <table border="0" rules="none" cellpadding="5" cellspacing="0" summary="List of Emails of the Bangalore Trainees">
		<caption>The AIESEC'ers</caption>
		<tr id="tableheader">
		<th>Name</th>
		<th>Address</th>
		<th>Contact Info</th>
		<th>BirthDate</th>
		</tr>
		<xsl:apply-templates select="Person[@group='AIESECers']">
			<xsl:sort select="Name/FirstName"/>
		</xsl:apply-templates>
		</table>
		<p>Email all the AIESEC'ers: 
		    <a><xsl:attribute name="class">email</xsl:attribute><xsl:attribute name="href">mailto: 
		<xsl:apply-templates select="Person[@group = 'AIESECers']/ContactInfo/Email"/></xsl:attribute>
		<xsl:apply-templates select="Person[@group = 'AIESECers']/ContactInfo/Email"/>
        </a>
		</p>
	</xsl:template>
    
	<xsl:template name="Extrainees">
	    <table border="0" rules="none" cellpadding="5" cellspacing="0" summary="List of Emails of the Bangalore Trainees">
		<caption>The ex-Trainees</caption>
		<tr id="tableheader">
		<th>Name</th>
		<th>Address</th>
		<th>Contact Info</th>
		<th>BirthDate</th>
		</tr>
		<xsl:apply-templates select="Person[@group='Extrainees']">
			<xsl:sort select="Name/FirstName"/>
		</xsl:apply-templates>
		</table>
		<p>Email all the ex-Trainees: 
		    <a><xsl:attribute name="class">email</xsl:attribute><xsl:attribute name="href">mailto: 
		<xsl:apply-templates select="Person[@group = 'Extrainees']/ContactInfo/Email"/></xsl:attribute>
		<xsl:apply-templates select="Person[@group = 'Extrainees']/ContactInfo/Email"/>
        </a>
		</p>
	</xsl:template>
    
	<xsl:template name="theRest">
	    <table border="0" rules="none" cellpadding="5" cellspacing="0" summary="List of Emails of the Bangalore Trainees">
		<caption>The Hangarounds</caption>
		<tr id="tableheader">
		<th>Name</th>
		<th>Address</th>
		<th>Contact Info</th>
		<th>BirthDate</th>
		</tr>
		<xsl:apply-templates select="Person[@group='theRest']">
			<xsl:sort select="Name/FirstName"/>
		</xsl:apply-templates>
		</table>
		<p>Email all the hang-arounds: 
		    <a><xsl:attribute name="class">email</xsl:attribute><xsl:attribute name="href">mailto: 
		<xsl:apply-templates select="Person[@group = 'theRest']/ContactInfo/Email"/></xsl:attribute>
		<xsl:apply-templates select="Person[@group = 'theRest']/ContactInfo/Email"/>
        </a>
		</p>
	</xsl:template>
    
	<xsl:template name="theExRest">
	    <table border="0" rules="none" cellpadding="5" cellspacing="0" summary="List of Emails of the Bangalore Trainees">
		<caption>The Ex-Hangarounds</caption>
		<tr id="tableheader">
		<th>Name</th>
		<th>Address</th>
		<th>Contact Info</th>
		<th>BirthDate</th>
		</tr>
		<xsl:apply-templates select="Person[@group='theExRest']">
			<xsl:sort select="Name/FirstName"/>
		</xsl:apply-templates>
		</table>
		<p>Email all the rest: 
		    <a><xsl:attribute name="class">email</xsl:attribute><xsl:attribute name="href">mailto: 
		<xsl:apply-templates select="Person[@group = 'theExRest']/ContactInfo/Email"/></xsl:attribute>
		<xsl:apply-templates select="Person[@group = 'theExRest']/ContactInfo/Email"/>
        </a>
		</p>
	</xsl:template>
    
</xsl:stylesheet>
