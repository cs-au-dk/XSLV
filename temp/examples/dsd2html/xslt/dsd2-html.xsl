<?xml version="1.0" encoding="ISO-8859-1" ?>

<!DOCTYPE xsl:stylesheet [

   <!-- Used in documentation -->
   <!ENTITY lt       "&#38;#60;" >     <!-- less-than sign -->
   <!ENTITY gt       "&#62;" >         <!-- greater-than sign -->
   <!ENTITY dash     "&#x2010;" >      <!-- hyphen -->
   <!ENTITY ndash    "&#x2013;" >      <!-- en dash -->
   <!ENTITY mdash    "&#x2014;" >      <!-- em dash -->
   <!ENTITY ldquo    "&#x201c;" >      <!-- Left Double Quote -->
   <!ENTITY rdquo    "&#x201d;" >      <!-- Right Double Quote -->
   <!ENTITY lsquo    "&#x2018;" >      <!-- Left Single Quote -->
   <!ENTITY rsquo    "&#x2019;" >      <!-- Right Single Quote -->
   <!ENTITY bull     "&#x2022;" >      <!-- round bullet, filled -->
   <!ENTITY nbsp     "&#160;" >        <!-- no break space -->
   <!ENTITY rarr     "&#x2192;" >      <!-- right arrow -->
   <!ENTITY copy     "&#169;" >        <!-- copyright -->

	<!ENTITY br '<xsl:text  disable-output-escaping="yes">&lt;br&gt;</xsl:text>' >
	<!ENTITY hr '<xsl:text  disable-output-escaping="yes">&lt;hr&gt;</xsl:text>' >
	<!ENTITY p '<xsl:text  disable-output-escaping="yes">&lt;p&gt;</xsl:text>' >

	<!ENTITY ruleElements 'unique|pointer|if|rule|declare|require'>
	<!ENTITY isRuleElement "
		name()='unique' or
		name()='pointer' or
		name()='if' or
		name()='rule' or
		name()='declare' or
		name()='require'">
	
	<!ENTITY boolexpElements 'and|or|not|imply|equiv|one|parent|ancestor|child|descendant|this|root|element|attribute|contents|boolexp'>
	<!ENTITY isBoolexpElement "
		name()='and' or
		name()='or' or
		name()='not' or
		name()='imply' or
		name()='equiv' or
		name()='one' or
		name()='parent' or
		name()='ancestor' or
		name()='child' or
		name()='descendant' or
		name()='this' or
		name()='root' or
		name()='element' or
		name()='attribute' or
		name()='contents' or
		name()='boolexp'">

	<!ENTITY regexpElements 'sequence|optional|complement|union|intersection|minus|repeat|string|char|stringtype|contenttype'>
	<!ENTITY isRegexpElement "
		name()='sequence' or
		name()='optional' or
		name()='complement' or
		name()='union' or
		name()='intersection' or
		name()='minus' or
		name()='repeat' or
		name()='string' or
		name()='char' or
		name()='stringtype' or
		name()='contenttype'">

	<!ENTITY definitionElements 'rule|contenttype|stringtype|boolexp'>
	<!ENTITY isDefinitionElement "
		name()='rule' or
		name()='contenttype' or
		name()='stringtype' or
		name()='boolexp' ">

	<!ENTITY rule-spacing '2ex'>
	<!ENTITY rule-block 'margin-bottom:2ex; margin-top:&rule-spacing;; border-width: 2px;' >
	<!ENTITY rule-margins 'margin-left=1pt; margin-right=1pt; margin-bottom:4pt; margin-top:1pt;' >
	
	<!ENTITY entry-desc-block 'margin-left=1pt; margin-right=1pt; margin-bottom:1pt; margin-top:15pt; border-width: 2px;' >
	<!ENTITY rule-code-block 'font-family:Courier; font-weight:bold; &rule-margins; border-style: solid; border-width: 2px;  border-color:pink;' >

	<!ENTITY body-background 'beige' >
	<!ENTITY default-background 'silver' >
	<!ENTITY condition-background 'pink' >
	<!ENTITY  block-indentation-level '2.7'>
] >

<!-- HSFR: The extra namespaces below are my local ones for the documentation. This could
be changed is required to make more universal. -->
<xsl:stylesheet
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xi="http://www.w3.org/2001/XInclude"
   xmlns:m="http://www.brics.dk/DSD/2.0/meta"
   xmlns:dsd="http://www.brics.dk/DSD/2.0"
	xmlns:doc="http://russet.eris.qinetiq.com/Library/DSDs/doc"
	xmlns:list="http://russet.eris.qinetiq.com/Library/DSDs/list"
	xmlns:copyright="http://russet.eris.qinetiq.com/Library/DSDs/copyright"
	xmlns:address="http://russet.eris.qinetiq.com/Library/DSDs/address"
	xmlns:postcode="http://russet.eris.qinetiq.com/Library/DSDs/postcode"
	xmlns:person="http://russet.eris.qinetiq.com/Library/DSDs/person"
	xmlns:t="http://russet.eris.qinetiq.com/Library/DSDs/text"
	xmlns:vers="http://russet.eris.qinetiq.com/Library/DSDs/vers"
	xmlns:link="http://russet.eris.qinetiq.com/Library/DSDs/link"
  xmlns="http://www.w3.org/1999/xhtml"
   version="1.0" >

<xsl:output method="html" />
<xsl:strip-space elements="*" />

<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-* -->

<xsl:template match="/">
	<xsl:apply-templates select="dsd" />
</xsl:template>

<xsl:template match="dsd">
	<html>
		<head>
			<META HTTP-EQUIV="Content-Type" CONTENT="text/html; CHARSET=iso-8859-1" />
			<title><xsl:apply-templates select="//title" mode="title" /></title>
			<style type="text/css">
				h1.titleHeader {font-size:18pt;  color: purple; font-family: Arial, Helvetica, Sans-serif;}
				h1.header {font-size:18pt;  color: white; font-family: Arial, Helvetica, Sans-serif;}
				span.chars {color: green;}
				span.header {font-size:12pt;  color: purple; font-family: Arial, Helvetica, Sans-serif;}
				span.keyword {font-family: Courier; color:blue; font-weight:bold; font-style:italic }
				span.keyword-def {font-family: Courier; color:red; font-weight:bold; font-style:italic }
				span.element-name {font-family: Courier; color:brown; font-weight:bold; }
				span.namespace {font-family: Courier; color: brown; font-weight:bold;}
				span.condition {background-color:pink;}
				span.desc {font-family: Helvetica,Arial; color: black; font-weight:normal;}
				body {font-size:10pt; background-color:&body-background;}
            a { color: blue; text-decoration: none; }
            a.visited { color: green; text-decoration: none }
            a.active { text-decoration: underline }
            a:hover { color: red; text-decoration: none }
			</style>
		</head>
		<body>
			<xsl:apply-templates select="doc" mode="headings" />&hr;
			<xsl:call-template name="processRules" />&hr;
			<xsl:apply-templates select="doc" mode="meta-data" />
		</body>
	</html>
</xsl:template>

<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*- -->
<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*- PROCESS RULES -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*- -->
<!--
		Each child of the main DSD is processed here and the appropriate action
		is taken.
		
			SCHEMA  ::=  <dsd root="PNAME"? >
								( RULE  |  DEFINITION  |  SCHEMA )*
							</dsd>
-->

<xsl:template name="processRules">
   <xsl:variable name="indentLevel" select="0"/>
	<xsl:variable name="block-indentation" select="'background-color: &body-background;; text-indent:{$indentLevel}ex;'"/>
	<xsl:variable name="condition-block-indentation" select="'background-color: &condition-background;; text-indent:{$indentLevel}ex;'"/>
	<xsl:for-each select="*[not(self::doc) and not(self::import)]">
		<xsl:call-template name="output-entry-title">
			<xsl:with-param name="rule" select="." />
		</xsl:call-template>
		<div style="&rule-code-block;">
		<xsl:choose>
			<xsl:when test="( &isDefinitionElement; ) and @id">
				<xsl:call-template name="definitions">
					<xsl:with-param name="indentLevel" select="$indentLevel"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="( &isBoolexpElement; )">
				<xsl:call-template name="boolexp-definitions">
					<xsl:with-param name="indentLevel" select="$indentLevel"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="( &isRuleElement; )">
				<xsl:call-template name="rule-definitions">
					<xsl:with-param name="indentLevel" select="$indentLevel"/>
				</xsl:call-template >
			</xsl:when>
		</xsl:choose>
		</div>
	</xsl:for-each>
</xsl:template>

<!-- Output the title of each entry together with the documentation for this entry -->

<xsl:template name="output-entry-title">
	<div style="&entry-desc-block;">
		<xsl:apply-templates select="doc" mode="definitions" />
	</div>
</xsl:template>

<!-- Output the description of each entry -->

<xsl:template match="doc" mode="definitions">
	<xsl:apply-templates select="desc" mode="definitions"/>
</xsl:template>

<xsl:template match="desc" mode="definitions">
	<span class="desc"><xsl:apply-templates mode="inline-text"/></span>
</xsl:template>

<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*- -->
<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*- RULES -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*- -->
<!--
		RULE  ::=	<declare> DECLARATION* </declare>
						| <require> BOOLEXP* </require>
						| <if> BOOLEXP RULE* </if>
						| <rule ref="PNAME"/>
						| UNIQUE
						| POINTER
-->

<!-- When rules are used as main entries in the list they require slightly different treatment -->

<xsl:template name="rule-definitions">
   <xsl:param name="indentLevel"/>
	<xsl:choose>
		<xsl:when test="name(.)='declare'">
			<xsl:call-template name="declare-entry">
				<xsl:with-param name="indentLevel" select="$indentLevel"/>
			</xsl:call-template>
		</xsl:when>
		<xsl:when test="name(.)='require'">
			<xsl:call-template name="require-entry">
				<xsl:with-param name="indentLevel" select="$indentLevel"/>
			</xsl:call-template>
		</xsl:when>
		<xsl:when test="name(.)='if'">
			<xsl:call-template name="if-entry">
				<xsl:with-param name="indentLevel" select="$indentLevel"/>
			</xsl:call-template>
		</xsl:when>
		<xsl:when test="name(.)='unique' or name()='pointer'">
			<xsl:call-template name="unique">
				<xsl:with-param name="indentLevel" select="$indentLevel + &block-indentation-level;"/>
			</xsl:call-template>
		</xsl:when>
		<xsl:otherwise></xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template name="rules">
   <xsl:param name="indentLevel"/>
	<xsl:element name="div">
		<xsl:attribute name="style">background-color: &body-background;; text-indent:<xsl:value-of select="$indentLevel"/>ex;</xsl:attribute>
		<xsl:choose>
			<xsl:when test="name(.)='declare'">
				<xsl:call-template name="declare-rule">
					<xsl:with-param name="indentLevel" select="$indentLevel + &block-indentation-level;"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="name(.)='require'">
				<xsl:call-template name="require-rule">
					<xsl:with-param name="indentLevel" select="$indentLevel + &block-indentation-level;"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="name(.)='if'">
				<xsl:call-template name="if-rule">
					<xsl:with-param name="indentLevel" select="$indentLevel + &block-indentation-level;"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="name(.)='rule'">
				<xsl:call-template name="rule-rule">
					<xsl:with-param name="indentLevel" select="$indentLevel + &block-indentation-level;"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="name(.)='unique' or name()='pointer'">
				<xsl:call-template name="unique">
					<xsl:with-param name="indentLevel" select="$indentLevel + &block-indentation-level;"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise></xsl:otherwise>
		</xsl:choose>
		</xsl:element>
</xsl:template>

<xsl:template name="if-rule">
   <xsl:param name="indentLevel"/>
	<xsl:call-template name="render-element-name"/>
	<xsl:for-each select="*[not(self::doc)]">
		<xsl:choose>
			<xsl:when test="position()=1">		<!-- condition boolexp -->
				<xsl:call-template name="boolexp">
					<xsl:with-param name="indentLevel" select="$indentLevel"/>
					<xsl:with-param name="isCondition" select="true()"/>
				</xsl:call-template >
			</xsl:when>
			<xsl:when test="not(self::doc)">		<!-- ignore documentation -->
				<xsl:call-template name="rules">
					<xsl:with-param name="indentLevel" select="$indentLevel"/>
				</xsl:call-template>
			</xsl:when>
		</xsl:choose>
	</xsl:for-each>
</xsl:template>

<xsl:template name="if-entry">
   <xsl:param name="indentLevel"/>
	<xsl:call-template name="render-element-name"/>
	<xsl:for-each select="*[not(self::doc)]">
		<xsl:choose>
			<xsl:when test="position()=1">		<!-- condition boolexp -->
				<xsl:call-template name="boolexp">
					<xsl:with-param name="indentLevel" select="$indentLevel + &block-indentation-level;"/>
					<xsl:with-param name="isCondition" select="true()"/>
				</xsl:call-template >
			</xsl:when>
			<xsl:when test="not(self::doc)">		<!-- ignore documentation -->
				<xsl:call-template name="rules">
					<xsl:with-param name="indentLevel" select="$indentLevel + &block-indentation-level;"/>
				</xsl:call-template>
			</xsl:when>
		</xsl:choose>
	</xsl:for-each>
</xsl:template>

<xsl:template name="require-rule">
   <xsl:param name="indentLevel"/>
	<xsl:call-template name="render-element-name"/>
	<xsl:element name="div">
		<xsl:attribute name="style">background-color: &body-background;; text-indent:<xsl:value-of select="$indentLevel"/>ex;</xsl:attribute>
		<xsl:for-each select="*">
			<xsl:call-template name="boolexp">
				<xsl:with-param name="indentLevel" select="$indentLevel+ &block-indentation-level;"/>
			</xsl:call-template >
		</xsl:for-each>
	</xsl:element>
</xsl:template>

<xsl:template name="require-entry">
   <xsl:param name="indentLevel"/>
	<xsl:call-template name="render-element-name"/>
	<xsl:for-each select="*[not(self::doc)]">
		<xsl:call-template name="boolexp">
			<xsl:with-param name="indentLevel" select="$indentLevel + &block-indentation-level;"/>
		</xsl:call-template>
	</xsl:for-each>
</xsl:template>

<xsl:template name="declare-entry">
   <xsl:param name="indentLevel"/>
	<xsl:call-template name="render-element-name"/>
	<xsl:for-each select="*[not(self::doc)]">
		<xsl:call-template name="declare-rule">
			<xsl:with-param name="indentLevel" select="$indentLevel + &block-indentation-level;"/>
		</xsl:call-template>
	</xsl:for-each>
</xsl:template>

<xsl:template name="rule-rule">
   <xsl:param name="indentLevel"/>
	<xsl:call-template name="render-element-name"/><span class="keyword"><xsl:value-of select="@ref"/></span>
</xsl:template>

<xsl:template name="unique">
   <xsl:param name="indentLevel"/>
	<xsl:call-template name="render-element-name"/>
	<xsl:for-each select="*[not(self::doc)]">
		<xsl:choose>
			<xsl:when test="position()=1">		<!-- condition boolexp -->
				<xsl:call-template name="boolexp">
					<xsl:with-param name="indentLevel" select="$indentLevel"/>
					<xsl:with-param name="isCondition" select="true()"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="not(self::doc)">		<!-- ignore documentation -->
				<xsl:call-template name="field">
					<xsl:with-param name="indentLevel" select="$indentLevel"/>
				</xsl:call-template>
			</xsl:when>
		</xsl:choose>
	</xsl:for-each>
</xsl:template>

<xsl:template name="field">
   <xsl:param name="indentLevel"/>
	<xsl:element name="div">
		<xsl:attribute name="style">background-color: &body-background;; text-indent:<xsl:value-of select="$indentLevel"/>ex;</xsl:attribute>
		<xsl:call-template name="render-element-name"/>
		<span class="keyword"><xsl:value-of select="@name"/></span>
		<xsl:if test="@type"><span class="keyword">{<xsl:value-of select="@type"/>}</span></xsl:if>
		<xsl:for-each select="*">
			<xsl:call-template name="boolexp">
				<xsl:with-param name="indentLevel" select="$indentLevel"/>
			</xsl:call-template>
		</xsl:for-each>
	</xsl:element>
</xsl:template>

<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*- -->
<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*- DEFINITIONS -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*- -->
<!--
   stringtype, boolexp and contenttype all expect to have a regular expression.
	
		DEFINITION  ::= 	<rule id="NCNAME" namespace="VALUE"? > RULE* </rule>
								| <contenttype id="NCNAME" namespace="VALUE"? > REGEXP </contenttype>
								| <stringtype id="NCNAME" namespace="VALUE"? > REGEXP </stringtype>
								| <boolexp id="NCNAME" namespace="VALUE"? > REGEXP </boolexp>
-->

<xsl:template name="definitions">
   <xsl:param name="indentLevel"/>
	<xsl:call-template name="render-element-name"/>
	<xsl:call-template name="render-id-attrib"/>
	<xsl:call-template name="render-namespace-attrib"/>
	<xsl:choose>
		<xsl:when test="name(.)='rule'">
			<xsl:for-each select="*[not(self::doc) and not(self::import)]">
				<xsl:call-template name="rules">
					<xsl:with-param name="indentLevel" select="$indentLevel + &block-indentation-level;"/>
				</xsl:call-template >
			</xsl:for-each>
		</xsl:when>
		<xsl:when test="name(.)='stringtype' or name(.)='boolexp' or name(.)='contenttype'">
			<xsl:for-each select="*[not(self::doc) and not(self::import)]">
				<xsl:call-template name="regexp">
					<xsl:with-param name="indentLevel" select="$indentLevel + &block-indentation-level;"/>
				</xsl:call-template >
			</xsl:for-each>
		</xsl:when>
		<xsl:otherwise></xsl:otherwise>
	</xsl:choose>
</xsl:template>

<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*- -->
<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*- DECLARATIONS *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*- -->
<!--
		DECLARATION  ::=  ATTRIBUTEDECL
								| <required> ATTRIBUTEDECL* </required>
								| <contents> ( REGEXP  |  NORMALIZE  |  CONTENTSDEFAULT )* </contents>
-->

<xsl:template name="declare-rule">
   <xsl:param name="indentLevel"/>
	<xsl:call-template name="render-element-name"/>
	<xsl:element name="div">
		<xsl:attribute name="style">background-color:&body-background;; text-indent:<xsl:value-of select="$indentLevel"/>ex;</xsl:attribute>
		<xsl:for-each select="*">
			<xsl:choose>
				<xsl:when test="name(.)='contents'">
					<xsl:call-template name="contents-decl">
						<xsl:with-param name="indentLevel" select="$indentLevel"/>
					</xsl:call-template>
				</xsl:when>
				<xsl:when test="name(.)='attribute'">
					<xsl:call-template name="attribute-decl">
						<xsl:with-param name="indentLevel" select="$indentLevel"/>
					</xsl:call-template>
				</xsl:when>
				<xsl:when test="name(.)='required'">
					<xsl:call-template name="required-decl">
						<xsl:with-param name="indentLevel" select="$indentLevel"/>
					</xsl:call-template>
				</xsl:when>
			</xsl:choose>
		</xsl:for-each>
	</xsl:element>
</xsl:template>

<xsl:template name="contents-decl">
   <xsl:param name="indentLevel"/>
	<xsl:element name="div">
		<xsl:attribute name="style">background-color:&body-background;; text-indent:<xsl:value-of select="$indentLevel"/>ex;</xsl:attribute>
		<xsl:call-template name="render-element-name"/>
		<xsl:for-each select="*">
			<xsl:choose>
				<xsl:when test="&isRegexpElement; or &isBoolexpElement;">
					<xsl:call-template name="regexp" >
						<xsl:with-param name="indentLevel" select="$indentLevel + &block-indentation-level;"/>
					</xsl:call-template>
				</xsl:when>
				<xsl:when test="name(.)='normalize'">
					<xsl:call-template name="normalize" >
						<xsl:with-param name="indentLevel" select="$indentLevel + &block-indentation-level;"/>
					</xsl:call-template>
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="contents-default" >
						<xsl:with-param name="indentLevel" select="$indentLevel + &block-indentation-level;"/>
					</xsl:call-template>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</xsl:element>
</xsl:template>

<xsl:template name="required-decl">
   <xsl:param name="indentLevel"/>
	<xsl:element name="div">
		<xsl:attribute name="style">background-color: &body-background;; text-indent:<xsl:value-of select="$indentLevel"/>ex;</xsl:attribute>
		<xsl:call-template name="render-element-name"/>
		<xsl:for-each select="*">
			<xsl:call-template name="attribute-decl">
				<xsl:with-param name="indentLevel" select="$indentLevel + &block-indentation-level;"/>
			</xsl:call-template>
		</xsl:for-each>
	</xsl:element>
</xsl:template>

<xsl:template name="normalize">
   <xsl:param name="indentLevel"/>
	<xsl:element name="div">
		<xsl:attribute name="style">background-color: &body-background;; text-indent:<xsl:value-of select="$indentLevel"/>ex;</xsl:attribute>
		<xsl:call-template name="render-element-name"/>
		<xsl:if test="@whitespace"><xsl:value-of select="@whitespace"/>&nbsp;whitespace</xsl:if>
		<xsl:if test="@case"><xsl:value-of select="@case"/>&nbsp;case</xsl:if>
	</xsl:element>
</xsl:template>

<!--
		CONTENTSDEFAULT ::=  <default> ANYCONTENTS </default>
-->

<xsl:template name="contents-default">
   <xsl:param name="indentLevel"/>
	<xsl:element name="div">
		<xsl:attribute name="style">background-color: &body-background;; text-indent:<xsl:value-of select="$indentLevel"/>ex;</xsl:attribute>
		<xsl:call-template name="render-element-name"/>&br;
		<xsl:element name="div">
			<xsl:attribute name="style">background-color: &default-background;; text-indent:<xsl:value-of select="$indentLevel + &block-indentation-level;"/>ex;</xsl:attribute>
			<xsl:for-each select="*">
				<xsl:choose>
					<xsl:when test="*">     <!-- if there is content -->
						<xsl:call-template name="render-start-element-and-attrib"/>
						<xsl:apply-templates mode="xml-content-defaults"/>
						<xsl:call-template name="render-end-element"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="render-empty-element-and-attrib"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</xsl:element>
	</xsl:element>
</xsl:template>

<!--
	These templates are to take content defaults which could be any XML tags and content.
	It is impossible to provide the correct indentation here so we output as best we can.
-->

<xsl:template match="*" mode="xml-content-defaults">
	<xsl:call-template name="render-start-element-and-attrib"/>
	<xsl:apply-templates mode="xml-content-defaults"/>
	<xsl:call-template name="render-end-element"/>
</xsl:template>

<xsl:template name="render-start-element-and-attrib">
	&lt;<xsl:value-of select="name()"/>
	<xsl:for-each select="@*">&nbsp;<xsl:value-of select="name()"/>=&quot;<xsl:value-of select="."/>&quot;</xsl:for-each>&gt;
</xsl:template>

<xsl:template name="render-end-element">
	&lt;/<xsl:value-of select="name()"/>&gt;
</xsl:template>

<xsl:template name="render-empty-element-and-attrib">
	&lt;<xsl:value-of select="name()"/>
	<xsl:for-each select="@*">&nbsp;<xsl:value-of select="name()"/>=&quot;<xsl:value-of select="."/>&quot;</xsl:for-each>/&gt;
</xsl:template>

<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*- -->
<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*- ATTRIBUTES *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*- -->
<!--
		ATTRIBUTEDECL  ::=  <attribute name="PNAME"? >
										( REGEXP  |  NORMALIZE  |  ATTRIBUTEDEFAULT )*
									</attribute>
-->

<xsl:template name="attribute-decl">
   <xsl:param name="indentLevel"/>
	<xsl:element name="div">
		<xsl:attribute name="style">background-color: &body-background;; text-indent:<xsl:value-of select="$indentLevel"/>ex;</xsl:attribute>
		<xsl:call-template name="render-element-name"/>
		<xsl:call-template name="render-name-attrib"/>
		<xsl:call-template name="render-namespace-attrib"/>
		<xsl:for-each select="*">
			<xsl:choose>
				<xsl:when test="name(.)='normalize'">
					<xsl:call-template name="normalize">
						<xsl:with-param name="indentLevel" select="$indentLevel + &block-indentation-level;"/>
					</xsl:call-template>
				</xsl:when>
				<xsl:when test="name(.)='default'">
					<xsl:call-template name="attribute-default">
						<xsl:with-param name="indentLevel" select="$indentLevel + &block-indentation-level;"/>
					</xsl:call-template>
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="regexp" >
						<xsl:with-param name="indentLevel" select="$indentLevel + &block-indentation-level;"/>
					</xsl:call-template>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</xsl:element>
</xsl:template>

<!--
		ATTRIBUTEDEFAULT  ::=  <default value="VALUE" />
-->

<xsl:template name="attribute-default">
   <xsl:param name="indentLevel"/>
	<xsl:element name="div">
		<xsl:attribute name="style">background-color: &body-background;; text-indent:<xsl:value-of select="$indentLevel"/>ex;</xsl:attribute>
		<xsl:call-template name="render-element-name"/> "<span class="chars"><xsl:value-of select="@value"/></span>"
	</xsl:element>
</xsl:template>

<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*- -->
<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*- REGULAR EXPRESSIONS -*-*-*-*-*-*-*-*-*-*-*-*-*-*- -->
<!--
	Regular expressions describe sets of strings or contents sequences.
	
		REGEXP  ::=	<sequence> REGEXP* </sequence>
						| <optional> REGEXP </optional>
						| <complement> REGEXP </complement>
						| <union> REGEXP* </union>
						| <intersection> REGEXP* </intersection>
						| <minus> REGEXP REGEXP </minus>
						| <repeat ( number="NUMERAL"?  |  min="NUMERAL"? max="NUMERAL"? ) > REGEXP </repeat>
						| <string value="VALUE"? />
						| <char ( set="VALUE"?  |  min="CHAR" max="CHAR" ) />
						| <stringtype ref="NCNAME" namespace="VALUE"? />
						| <contenttype ref="NCNAME" namespace="VALUE"? />
						| BOOLEXP
-->

<xsl:template name="regexp">
   <xsl:param name="indentLevel"/>
	<xsl:element name="div">
		<xsl:attribute name="style">background-color: &body-background;; text-indent:<xsl:value-of select="$indentLevel"/>ex;</xsl:attribute>
		<xsl:choose>
			<xsl:when test="name(.)='optional' or name(.)='complement'">
				<xsl:call-template name="unary-regexp">
					<xsl:with-param name="indentLevel" select="$indentLevel"/>
				</xsl:call-template >
			</xsl:when>
			<xsl:when test="name(.)='sequence' or name(.)='union' or name(.)='intersection'">
				<xsl:call-template name="seq-regexp">
					<xsl:with-param name="indentLevel" select="$indentLevel"/>
				</xsl:call-template >
			</xsl:when>
			<xsl:when test="name(.)='minus'">
				<xsl:call-template name="minus-regexp">
					<xsl:with-param name="indentLevel" select="$indentLevel"/>
				</xsl:call-template >
			</xsl:when>
			<xsl:when test="name(.)='repeat'">
				<xsl:call-template name="repeat-regexp">
					<xsl:with-param name="indentLevel" select="$indentLevel"/>
				</xsl:call-template >
			</xsl:when>
			<xsl:when test="name(.)='char'">
				<xsl:call-template name="char-regexp"/>
			</xsl:when>
			<xsl:when test="name(.)='string'">
				<xsl:call-template name="string-regexp"/>
			</xsl:when>
			<xsl:when test="name(.)='stringtype' or name(.)='contenttype'">
				<xsl:call-template name="stringtype-regexp"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="boolexp" >
					<xsl:with-param name="indentLevel" select="$indentLevel"/>
				</xsl:call-template >
			</xsl:otherwise>
		</xsl:choose>
	</xsl:element>
</xsl:template>

<xsl:template name="unary-regexp">
   <xsl:param name="indentLevel"/>
	<xsl:call-template name="render-element-name"/>
	<xsl:for-each select="*[not(position()>1)]">
		<xsl:call-template name="regexp" >
			<xsl:with-param name="indentLevel" select="$indentLevel + &block-indentation-level;"/>
		</xsl:call-template >
	</xsl:for-each>
</xsl:template>

<xsl:template name="seq-regexp">
   <xsl:param name="indentLevel"/>
	<xsl:call-template name="render-element-name"/>
	<xsl:for-each select="*">
		<xsl:call-template name="regexp" >
			<xsl:with-param name="indentLevel" select="$indentLevel + &block-indentation-level;"/>
		</xsl:call-template>
	</xsl:for-each>
</xsl:template>

<xsl:template name="minus-regexp">
   <xsl:param name="indentLevel"/>
	<xsl:call-template name="render-element-name"/>
	<xsl:for-each select="*[not(position()>2)]">
		<xsl:call-template name="regexp" >
			<xsl:with-param name="indentLevel" select="$indentLevel + &block-indentation-level;"/>
		</xsl:call-template >
	</xsl:for-each>
</xsl:template>

<xsl:template name="repeat-regexp">
   <xsl:param name="indentLevel"/>
	<xsl:choose>
		<xsl:when test="@number">
			<xsl:call-template name="render-element-name"/> {<xsl:value-of select="@number"/> times}
		</xsl:when>
		<xsl:when test="@min">
			<xsl:call-template name="render-element-name"/> {<xsl:value-of select="@min"/>..<xsl:value-of select="@max"/>}
		</xsl:when>
		<xsl:otherwise>
			<xsl:call-template name="render-element-name"/> {forever}
		</xsl:otherwise>
	</xsl:choose>
	<xsl:for-each select="*">
		<xsl:call-template name="regexp" >
			<xsl:with-param name="indentLevel" select="$indentLevel + &block-indentation-level;"/>
		</xsl:call-template >
	</xsl:for-each>
</xsl:template>

<xsl:template name="char-regexp">
	<xsl:choose>
		<xsl:when test="@set">
			[<span class="chars"><xsl:value-of select="@set"/></span>]
		</xsl:when>
		<xsl:when test="@min or @max">
			[<span class="chars"><xsl:value-of select="@min"/>..<xsl:value-of select="@max"/></span>]
		</xsl:when>
		<xsl:otherwise>
			<span class="chars">any character</span>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template name="stringtype-regexp">
   <xsl:param name="indentLevel"/>
	<xsl:call-template name="render-element-name"/> <span class="keyword"><xsl:value-of select="@ref"/></span>
	<xsl:if test="@namespace">&nbsp;<span class="namespace">{xmlns:<xsl:value-of select="@namespace"/>}</span></xsl:if>
</xsl:template>

<xsl:template name="string-regexp">
   <xsl:param name="indentLevel"/>
	<xsl:choose>
		<xsl:when test="@value">
			"<span class="chars"><xsl:value-of select="@value"/></span>"
		</xsl:when>
		<xsl:otherwise>
			<span class="chars">any string</span>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*- -->
<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*- BOOLEAN EXPRESSIONS -*-*-*-*-*-*-*-*-*-*-*-*-*-*- -->
<!--
	A boolean expression describes a property of an element in the instance document:
	
		BOOLEXP ::=  <and> BOOLEXP* </and>
							| <or> BOOLEXP* </or>
							| <not> BOOLEXP </not>
							| <imply> BOOLEXP BOOLEXP </imply>
							| <equiv> BOOLEXP* </equiv>
							| <one> BOOLEXP* </one>
							| <parent> BOOLEXP </parent>
							| <ancestor> BOOLEXP </ancestor>
							| <child> BOOLEXP </child>
							| <descendant> BOOLEXP </descendant>
							| <this/>
							| <root/>
							| <element name="PNAME"? />
							| <attribute name="PNAME"? > REGEXP? </attribute>
							| <contents> REGEXP* </contents>
							| <boolexp ref="PNAME"/>
-->

<xsl:template name="boolexp-definitions">
	<xsl:param name="indentLevel"/>
	<xsl:param name="isCondition" select="false()"/>
	<xsl:variable name="theNode" select="name(child::*[1])"/>
	<xsl:element name="div">
		<xsl:attribute name="style">
			background-color: &body-background;; text-indent:<xsl:value-of select="$indentLevel"/>ex;
		</xsl:attribute>
		<xsl:choose>
			<xsl:when test="$theNode='and' or $theNode='or' or $theNode='equiv' or $theNode='one'">
				<xsl:call-template name="multi-boolexp">
					<xsl:with-param name="indentLevel" select="$indentLevel"/>
					<xsl:with-param name="isCondition" select="$isCondition"/>
				</xsl:call-template >
			</xsl:when>
			<xsl:when test="$theNode='parent' or $theNode='ancestor' or $theNode='child' or $theNode='descendant' or $theNode='not'">
				<xsl:call-template name="unary-boolexp">
					<xsl:with-param name="indentLevel" select="$indentLevel"/>
					<xsl:with-param name="isCondition" select="$isCondition"/>
				</xsl:call-template >
			</xsl:when>
			<xsl:when test="$theNode='imply'">
				<xsl:call-template name="imply-boolexp">
					<xsl:with-param name="indentLevel" select="$indentLevel"/>
					<xsl:with-param name="isCondition" select="$isCondition"/>
				</xsl:call-template >
			</xsl:when>
			<xsl:when test="$theNode='contents'">
				<xsl:call-template name="contents-boolexp">
					<xsl:with-param name="indentLevel" select="$indentLevel"/>
					<xsl:with-param name="isCondition" select="$isCondition"/>
				</xsl:call-template >
			</xsl:when>
			<xsl:when test="$theNode='attribute'">
				<xsl:call-template name="attribute-boolexp">
					<xsl:with-param name="indentLevel" select="$indentLevel"/>
					<xsl:with-param name="isCondition" select="$isCondition"/>
				</xsl:call-template >
			</xsl:when>
			<xsl:when test="$theNode='element'">
				<xsl:call-template name="element-exp">
					<xsl:with-param name="indentLevel" select="$indentLevel"/>
					<xsl:with-param name="isCondition" select="$isCondition"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="$theNode='root'">
				<xsl:call-template name="root-exp">
					<xsl:with-param name="indentLevel" select="$indentLevel"/>
					<xsl:with-param name="isCondition" select="$isCondition"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="$theNode='this'">
				<xsl:call-template name="this-exp">
					<xsl:with-param name="indentLevel" select="$indentLevel"/>
					<xsl:with-param name="isCondition" select="$isCondition"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="$theNode='boolexp'">
				<xsl:call-template name="boolexp-exp">
					<xsl:with-param name="indentLevel" select="$indentLevel"/>
					<xsl:with-param name="isCondition" select="$isCondition"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="render-element-name"/>
				<xsl:call-template name="render-id-attrib"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:element>
</xsl:template>


<xsl:template name="boolexp">
	<xsl:param name="indentLevel"/>
	<xsl:param name="isCondition"/>
	<xsl:element name="div">
		<xsl:attribute name="style">
			<xsl:choose>
				<xsl:when test="$isCondition">background-color: &condition-background;; text-indent:<xsl:value-of select="$indentLevel"/>ex;</xsl:when>
				<xsl:otherwise>background-color: &body-background;; text-indent:<xsl:value-of select="$indentLevel"/>ex;</xsl:otherwise>
			</xsl:choose>
		</xsl:attribute>
		<xsl:choose>
			<xsl:when test="name(.)='and' or name(.)='or' or name(.)='equiv' or name(.)='one'">
				<xsl:call-template name="multi-boolexp">
					<xsl:with-param name="indentLevel" select="$indentLevel"/>
					<xsl:with-param name="isCondition" select="$isCondition"/>
				</xsl:call-template >
			</xsl:when>
			<xsl:when test="name(.)='parent' or name(.)='ancestor' or name(.)='child' or name(.)='descendant' or name(.)='not'">
				<xsl:call-template name="unary-boolexp">
					<xsl:with-param name="indentLevel" select="$indentLevel"/>
					<xsl:with-param name="isCondition" select="$isCondition"/>
				</xsl:call-template >
			</xsl:when>
			<xsl:when test="name(.)='contents'">
				<xsl:call-template name="contents-boolexp">
					<xsl:with-param name="indentLevel" select="$indentLevel"/>
					<xsl:with-param name="isCondition" select="$isCondition"/>
				</xsl:call-template >
			</xsl:when>
			<xsl:when test="name(.)='imply'">
				<xsl:call-template name="contents-boolexp">
					<xsl:with-param name="indentLevel" select="$indentLevel"/>
					<xsl:with-param name="isCondition" select="$isCondition"/>
				</xsl:call-template >
			</xsl:when>
			<xsl:when test="name(.)='attribute'">
				<xsl:call-template name="attribute-boolexp">
					<xsl:with-param name="indentLevel" select="$indentLevel"/>
					<xsl:with-param name="isCondition" select="$isCondition"/>
				</xsl:call-template >
			</xsl:when>
			<xsl:when test="name(.)='element'">
				<xsl:call-template name="element-exp">
					<xsl:with-param name="indentLevel" select="$indentLevel"/>
					<xsl:with-param name="isCondition" select="$isCondition"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="name(.)='root'">
				<xsl:call-template name="root-exp">
					<xsl:with-param name="indentLevel" select="$indentLevel"/>
					<xsl:with-param name="isCondition" select="$isCondition"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="name(.)='this'">
				<xsl:call-template name="this-exp">
					<xsl:with-param name="indentLevel" select="$indentLevel"/>
					<xsl:with-param name="isCondition" select="$isCondition"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="name(.)='boolexp'">
				<xsl:call-template name="boolexp-exp">
					<xsl:with-param name="indentLevel" select="$indentLevel"/>
					<xsl:with-param name="isCondition" select="$isCondition"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise></xsl:otherwise>
		</xsl:choose>
	</xsl:element>
</xsl:template>

<xsl:template name="unary-boolexp">
   <xsl:param name="indentLevel"/>
	<xsl:param name="isCondition"/>
	<xsl:call-template name="render-element-name"/>
	<xsl:call-template name="render-id-attrib"/>
	<xsl:for-each select="*[not(position()>1)]">
		<xsl:call-template name="boolexp" >
			<xsl:with-param name="indentLevel" select="$indentLevel + &block-indentation-level;"/>
			<xsl:with-param name="isCondition" select="$isCondition"/>
		</xsl:call-template >
	</xsl:for-each>
</xsl:template>

<xsl:template name="multi-boolexp">
   <xsl:param name="indentLevel"/>
	<xsl:param name="isCondition"/>
	<xsl:call-template name="render-element-name"/>
	<xsl:call-template name="render-id-attrib"/>
	<xsl:for-each select="*">
		<xsl:call-template name="boolexp" >
			<xsl:with-param name="indentLevel" select="$indentLevel + &block-indentation-level;"/>
			<xsl:with-param name="isCondition" select="$isCondition"/>
		</xsl:call-template>
	</xsl:for-each>
</xsl:template>

<xsl:template name="attribute-boolexp">
   <xsl:param name="indentLevel"/>
	<xsl:param name="isCondition"/>
	<xsl:call-template name="render-element-name"/>
	<xsl:call-template name="render-id-attrib"/>
	<xsl:if test="@name"><!-- =&gt; --><span class="keyword"><xsl:value-of select="@name"/></span></xsl:if>
	<xsl:for-each select="*">
		<xsl:call-template name="regexp">
			<xsl:with-param name="indentLevel" select="$indentLevel + &block-indentation-level;"/>
		</xsl:call-template>
	</xsl:for-each>
</xsl:template>

<xsl:template name="element-exp">
   <xsl:param name="indentLevel"/>
	<xsl:param name="isCondition"/>
	<xsl:choose>
		<xsl:when test="@name">
			<xsl:call-template name="render-element-name"/>
			<span class="element-name">
				<xsl:if test="@prefix"><xsl:value-of select="@prefix"/><!-- =&gt; --></xsl:if>
				<xsl:value-of select="@name"/>
			</span>
			<xsl:call-template name="render-namespace-attrib"/>
		</xsl:when>
		<xsl:otherwise>
			<span class="chars">any element</span>
			<xsl:call-template name="render-namespace-attrib"/>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template name="imply-boolexp">
   <xsl:param name="indentLevel"/>
	<xsl:param name="isCondition"/>
	<xsl:call-template name="render-element-name"/>
	<xsl:call-template name="render-id-attrib"/>
	<xsl:for-each select="*[not(position()>2)]">
		<xsl:call-template name="boolexp" >
			<xsl:with-param name="indentLevel" select="$indentLevel + &block-indentation-level;"/>
			<xsl:with-param name="isCondition" select="$isCondition"/>
		</xsl:call-template >
	</xsl:for-each>
</xsl:template>

<xsl:template name="this-exp">
   <xsl:param name="indentLevel"/>
	<xsl:param name="isCondition"/>
	<span class="chars">this element</span>
</xsl:template>

<xsl:template name="root-exp">
   <xsl:param name="indentLevel"/>
	<xsl:param name="isCondition"/>
	<span class="chars">root element</span>
</xsl:template>

<xsl:template name="boolexp-exp">
   <xsl:param name="indentLevel"/>
	<xsl:param name="isCondition"/>
	<xsl:call-template name="render-element-name"/>
	<span class="keyword">
		<xsl:if test="@prefix"><xsl:value-of select="@prefix"/>=></xsl:if>
		<xsl:value-of select="@ref"/>
	</span>
	<xsl:call-template name="render-namespace-attrib"/>
</xsl:template>

<xsl:template name="contents-boolexp">
   <xsl:param name="indentLevel"/>
	<xsl:param name="isCondition"/>
	<xsl:call-template name="render-element-name"/>
	<xsl:call-template name="render-id-attrib"/>
	<xsl:for-each select="*">
		<xsl:call-template name="regexp">
			<xsl:with-param name="indentLevel" select="$indentLevel + &block-indentation-level;"/>
		</xsl:call-template>
	</xsl:for-each>
</xsl:template>

<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*- -->
<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*- UTILITY TEMPLATES -*-*-*-*-*-*-*-*-*-*-*-*-*-*- -->

<xsl:template name="render-dsd-link">
   <xsl:param name="xml-file"/>
	<xsl:element name="a">
		<xsl:attribute name="href"><xsl:value-of select="$xml-file"/></xsl:attribute>
      <xsl:value-of select="$xml-file"/>
	</xsl:element>
</xsl:template>

<xsl:template name="render-element-name">
	<xsl:value-of select="substring-after(name(.),':')"/>&nbsp;&rarr;
</xsl:template>

<xsl:template name="render-id-attrib">
	<xsl:if test="@id">&nbsp;<span class="keyword-def"><xsl:value-of select="@id"/></span></xsl:if>
</xsl:template>

<xsl:template name="render-name-attrib">
	<xsl:if test="@name">&nbsp;<span class="keyword"><xsl:value-of select="@name"/></span></xsl:if>
</xsl:template>

<xsl:template name="render-namespace-attrib">
	<xsl:if test="@namespace">&nbsp;<span class="namespace">{xmlns:<xsl:value-of select="@namespace"/>}</span></xsl:if>
</xsl:template>

<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-* -->
<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-* -->
<!--                        DSD Documentation Pretty Printer                        -->
<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-* -->
<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-* -->
<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-* EXTRACT TITLE *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-* -->
<!--
	Title extraction for the browser window header title
-->

<xsl:template match="title" mode="title">
   DSD-2:&nbsp;<xsl:value-of select="."/>
</xsl:template>

<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-* -->
<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-* PREFIX HEADER *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-* -->
<!--
	Produce the titling at the top of the document
-->

<xsl:template match="doc" mode="headings">
	<h1 class="titleHeader"><xsl:value-of select="title"/></h1>
	<table border="0">
		<tr align="left">
			<td><span class="header"><b>Version:&nbsp;</b></span></td>
			<td><span class="header"><i><xsl:call-template name="latest-version"/></i></span></td>
		</tr>
		<xsl:for-each select="/*/@*">
			<tr align="left">
				<td><span class="header"><b><xsl:value-of select="name()"/>:&nbsp;</b></span></td>
				<td><span class="header"><i><xsl:value-of select="."/></i></span></td>
			</tr>
		</xsl:for-each>
		<tr align="left">
			<td><span class="header"><b>Namespace:&nbsp;</b></span></td>
			<td><span class="header"><i><xsl:value-of select="substring-before( //dsd/@root, ':' )"/>&nbsp;</i></span></td>
		</tr>
		<xsl:for-each select="//import">
			<tr align="left">
				<td><span class="header"><b>Imported file:&nbsp;</b></span></td>
				<td><span class="header">
				<i><xsl:call-template name="render-dsd-link">
					<xsl:with-param name="xml-file" select="./@href"/>
				</xsl:call-template></i>
				</span></td>
			</tr>
		</xsl:for-each>
	</table>
	&br;
	<span class="header"><b>Description</b></span>&br;&br;
	<xsl:apply-templates select="desc" mode="meta-data"/>&p;
</xsl:template>

<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-* -->
<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-* SUFFIX META-DATA -*-*-*-*-*-*-*-*-*-*-*-*-*-*-* -->
<!--
	Meta-data documentation at the bottom of the documentation
-->

<xsl:template match="doc" mode="meta-data">
	<table border="1" cellpadding="2">
		<tr valign="top">
			<td width="55%"><xsl:apply-templates select="versions" mode="meta-data"/></td>
			<td><xsl:apply-templates select="authors" mode="meta-data"/></td>
		</tr>
	</table>
	<xsl:apply-templates select="copyright" mode="meta-data"/>
</xsl:template>

<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-* -->
<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*- VERSION NUMBERS -*-*-*-*-*-*-*-*-*-*-*-*-*-*-* -->

<xsl:template match="versions" mode="meta-data">
	<table border="0">
		<!-- <tr align="left" colspan="3"><td><font size="+1"><b>Versions</b></font></td></tr> -->
		<tr align="left"><th>Version</th><th>Date</th><th>Who</th><th>Reason</th></tr>
		<xsl:for-each select="*">
			<xsl:choose>
				<xsl:when test="name()='entry'">
					<tr align="left" valign="top">
					<td><xsl:value-of select="@vers"/>&nbsp;</td>
					<td><xsl:value-of select="@day"/>&nbsp;<xsl:value-of select="@month"/>&nbsp;<xsl:value-of select="@year"/>&nbsp;&nbsp;</td>
					<td><xsl:value-of select="@who"/>&nbsp;&nbsp;</td>
					<td><xsl:value-of select="."/></td>
					</tr>
				</xsl:when>
			</xsl:choose>
		</xsl:for-each>
	</table>
</xsl:template>

<xsl:template name="latest-version">
	<xsl:value-of select="//entry[last()]/@vers" />
</xsl:template>

<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-* -->
<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*- LIST OF AUTHORS -*-*-*-*-*-*-*-*-*-*-*-*-*-*-* -->

<xsl:template match="authors" mode="meta-data">
	<p><font size="+1"><b>Author(s)</b></font></p>
		<xsl:for-each select="*">
			<xsl:choose>
				<xsl:when test="name()='person'">
					<xsl:variable name="mail" select="email" />
					<b><a href="mailto:{$mail}"><xsl:apply-templates select="forenames" mode="person-forenames" />
					<xsl:value-of select="surname"/></a></b><br/>
					<i><xsl:apply-templates select="address" mode="person-address" /></i><br/>
					<xsl:apply-templates select="email" mode="person-address" />
				</xsl:when>
			</xsl:choose>
		</xsl:for-each>
</xsl:template>

<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-* -->
<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-* AUTHOR'S NAME *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-* -->

<xsl:template match="forenames" mode="person-forenames">
	<xsl:for-each select="list/item">
      <xsl:apply-templates select="forename" mode="person-forenames" />
	</xsl:for-each>
</xsl:template>

<xsl:template match="forename" mode="person-forenames">
	<xsl:if test="@type='primary'"><xsl:value-of select="text()"/>&nbsp;</xsl:if>
</xsl:template>

<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-* -->
<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-* AUTHOR'S ADDRESS -*-*-*-*-*-*-*-*-*-*-*-*-*-*-* -->

<xsl:template match="address" mode="person-address">
	<xsl:for-each select="address">
		<xsl:choose>
			<xsl:when test="@where='work'">Work: </xsl:when>
			<xsl:otherwise>? : </xsl:otherwise>
		</xsl:choose>
		<xsl:for-each select="//line">
			<xsl:sort select="@numb" data-type="number" order="ascending" />
			<xsl:value-of select="."/>,&nbsp;
		</xsl:for-each>
		<xsl:value-of select="//town"/>,&nbsp;
		<xsl:value-of select="//county"/>,&nbsp;
		<xsl:value-of select="//country"/>,&nbsp;
		<xsl:value-of select="//postcode"/>
	</xsl:for-each>
</xsl:template>

<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-* -->
<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-* AUTHOR'S EMAIL -*-*-*-*-*-*-*-*-*-*-*-*-*-*-* -->

<xsl:template match="email" mode="person-address">
   <tt><xsl:element name="a">
            <xsl:attribute name="href">mailto:<xsl:value-of select="."/></xsl:attribute>
            <xsl:value-of select="."/>
         </xsl:element></tt>
</xsl:template>

<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-* -->
<!-- -*-*-*-*-*-*-*-*-*-*-*- GENERAL DOCUMENT DESCRIPTION *-*-*-*-*-*-*-*-*-*-*-*-* -->

<xsl:template match="desc" mode="meta-data">
	<span class="desc"><xsl:apply-templates mode="inline-text"/></span>
</xsl:template>

<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-* -->
<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-* DOCUMENT COPYRIGHT -*-*-*-*-*-*-*-*-*-*-*-*-*-* -->

<xsl:template match="copyright" mode="meta-data">
	<xsl:value-of select="leader/@value"/>&nbsp;&copy;&nbsp;
	<xsl:value-of select="date/@value"/>&nbsp;
	<xsl:value-of select="who/@value"/>
</xsl:template>

<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-* -->
<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-* -->
<!--                               Text Pretty Printer                              -->
<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-* -->
<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-* -->

<xsl:template match="p" mode="inline-text">
	<p><xsl:apply-templates mode="inline-text"/></p>
</xsl:template>

<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*- -->
<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*- -->

<xsl:template match="verbatim" mode="inline-text">
	<pre><xsl:apply-templates mode="inline-text"/></pre>
</xsl:template>

<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*- -->
<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*- -->

<xsl:template match="heading" mode="inline-text">
	<xsl:choose>
		<xsl:when test="@level">
			<xsl:value-of select="'&lt;h'" disable-output-escaping="yes"/><xsl:value-of select="@level"/><xsl:value-of select="'&gt;'" disable-output-escaping="yes"/>
			<xsl:apply-templates mode="inline-text"/>
			<xsl:value-of select="'&lt;/h'" disable-output-escaping="yes"/><xsl:value-of select="@level"/><xsl:value-of select="'&gt;'" disable-output-escaping="yes"/>
		</xsl:when>
		<xsl:otherwise>
			<h1><xsl:apply-templates mode="inline-text"/></h1>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*- -->
<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*- -->

<xsl:template match="list" mode="inline-text">
	<xsl:choose>
		<xsl:when test="@type='ordered' or @type='numbered'">
			<ol><xsl:apply-templates mode="inline-text"/></ol>
		</xsl:when>
		<xsl:when test="@type='unordered'">
			<ul><xsl:apply-templates mode="inline-text"/></ul>
		</xsl:when>
	</xsl:choose>
</xsl:template>

<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*- -->
<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*- -->

<xsl:template match="link" mode="inline-text">
   <xsl:choose>
      <xsl:when test="@type='uri'">
         <xsl:element name="a">
            <xsl:attribute name="href"><xsl:value-of select="@ref"/></xsl:attribute>
            <xsl:apply-templates mode="inline-text"/>
         </xsl:element>
      </xsl:when>
      <xsl:when test="@type='email'">
         <xsl:element name="a">
            <xsl:attribute name="href">mailto:<xsl:value-of select="@ref"/></xsl:attribute>
            <xsl:apply-templates mode="inline-text"/>
         </xsl:element>
      </xsl:when>
      <xsl:when test="@type='anchor'">
         <xsl:element name="a">
            <xsl:attribute name="name"><xsl:value-of select="@ref"/></xsl:attribute>
            <xsl:apply-templates mode="inline-text"/>
         </xsl:element>
      </xsl:when>
      <xsl:otherwise>
         <xsl:element name="a">
            <xsl:attribute name="href">#<xsl:value-of select="@ref"/></xsl:attribute>
            <xsl:apply-templates mode="inline-text"/>
         </xsl:element>
      </xsl:otherwise>
   </xsl:choose>
</xsl:template>

<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*- -->
<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*- -->

<xsl:template match="code" mode="inline-text">
   <xsl:choose>
      <xsl:when test="@type='res'">
         <b><xsl:apply-templates mode="inline-text"/></b>
      </xsl:when>
      <xsl:when test="@type='dir'">
         <tt><xsl:apply-templates mode="inline-text"/></tt>
      </xsl:when>
      <xsl:when test="@type='var'">
         <i><xsl:apply-templates mode="inline-text"/></i>
      </xsl:when>
      <xsl:otherwise>
         <i><xsl:apply-templates mode="inline-text"/></i>
      </xsl:otherwise>
   </xsl:choose>
</xsl:template>

<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*- -->
<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*- -->

<xsl:template match="item" mode="inline-text">
	<li><xsl:apply-templates mode="inline-text"/></li>
</xsl:template>

<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*- -->
<!-- -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*- -->

<xsl:template match="emph" mode="inline-text">
	<xsl:choose>
		<xsl:when test="@degree='weak'">
			<i><xsl:apply-templates mode="inline-text"/></i>
		</xsl:when>
		<xsl:when test="@degree='strong'">
			<font size="+1"><i><b><xsl:apply-templates mode="inline-text"/></b></i></font>
		</xsl:when>
		<xsl:otherwise>
			<i><b><xsl:apply-templates mode="inline-text"/></b></i>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template match="variable" mode="inline-text">
	<i><xsl:apply-templates mode="inline-text"/></i>
</xsl:template>

</xsl:stylesheet>
