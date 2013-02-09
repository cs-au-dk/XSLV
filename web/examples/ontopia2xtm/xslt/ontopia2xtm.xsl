<?xml version="1.0"?>

<xsl:stylesheet  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns="http://www.topicmaps.org/xtm/1.0/"
	version="1.0"
>
  <xsl:output method="xml"  
	indent="yes" 
	doctype-system = "xtm1.dtd"
	encoding="iso-8859-1"
  />

  <xsl:template match="topicmap">
	<topicMap>
		<xsl:apply-templates select="topic|assoc"/>
	</topicMap>
  </xsl:template>

  <xsl:template match="topic">
	<!--ELEMENT topic ( instanceOf*, subject?, ( baseName | occurrence )* ) -->
	<topic>
		<xsl:apply-templates select="@id"/>
		<xsl:call-template name="typesRefSplit">
			<xsl:with-param name="topicRefs" select="@types"/>
		</xsl:call-template>
		<xsl:if test="@identity">
			<subjectIdentity>
				<subjectIndicatorRef href="{@identity}"/>
			</subjectIdentity>
		</xsl:if>
		<xsl:for-each select="topname">
			<!--ELEMENT baseName  ( scope?, baseNameString, variant* ) -->
			<baseName>
				<xsl:if test="@scope or basename/@scope">
					<scope>
						<xsl:call-template name="topicRefSplit">
							<xsl:with-param name="topicRefs" select="@scope"/>
						</xsl:call-template>
						<xsl:call-template name="topicRefSplit">
							<xsl:with-param name="topicRefs" select="basename/@scope"/>
						</xsl:call-template>
					</scope>
				</xsl:if>
				<baseNameString><xsl:value-of select="basename"/></baseNameString>
				<!--ELEMENT variant  ( parameters?, ( variantName | variant )+ ) -->
				<xsl:if test="dispname">
					<variant>
						<parameters>
							<topicRef href="http://www.topicmaps.org/xtm/1.0/#psi-display"/>
							<xsl:call-template name="topicRefSplit">
								<xsl:with-param name="topicRefs" select="basename/@scope"/>
							</xsl:call-template>
						</parameters>
					        <variantName>
							<resourceData id="{generate-id(dispname)}"><xsl:value-of select="dispname"/></resourceData>
						</variantName>
					</variant>
				</xsl:if>
				<xsl:if test="sortname">
					<variant>
						<parameters>
							<topicRef href="http://www.topicmaps.org/xtm/1.0/#psi-sort"/>
							<xsl:call-template name="topicRefSplit">
								<xsl:with-param name="topicRefs" select="basename/@scope"/>
							</xsl:call-template>
						</parameters>
					        <variantName>
							<resourceData  id="{generate-id(sortname)}"><xsl:value-of select="sortname"/></resourceData>
						</variantName>
					</variant>
				</xsl:if>
			</baseName>
		</xsl:for-each>
		<!--ELEMENT occurrence ( instanceOf?, scope?, ( resourceRef | resourceData ) ) -->
		<xsl:for-each select="occurs">
			<occurrence>
				<xsl:apply-templates select="@id"/>
				<xsl:apply-templates select="@type"/>
				<xsl:apply-templates select="@scope"/>
				<xsl:choose>
					<xsl:when test="@href">
						<resourceRef href="{@href}"/>
					</xsl:when>
					<xsl:otherwise>
						<resourceData  id="{generate-id(.)}"><xsl:value-of select="."/></resourceData>
					</xsl:otherwise>
				</xsl:choose>
			</occurrence>
		</xsl:for-each>
	</topic>
  </xsl:template>

  <xsl:template match="assoc">
	<!--ELEMENT association  ( instanceOf?, scope?, member+ )-->
	<association>
		<xsl:choose>
			<xsl:when test="@id">
				<xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
			</xsl:when>
			<xsl:otherwise>
				<xsl:attribute name="id"><xsl:value-of select="generate-id(.)"/></xsl:attribute>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:apply-templates select="@type"/>
		<xsl:apply-templates select="@scope"/>
		<xsl:for-each select="assocrl">
		<!--ELEMENT member ( instanceOf?, ( topicRef | resourceRef | subjectIndicatorRef )+ )-->
			<member>
				<xsl:apply-templates select="@id"/>
				<xsl:apply-templates select="@type" mode="roleSpec"/>
				<topicRef href="#{@href}"/>
			</member>
		</xsl:for-each>
	</association>
  </xsl:template>

  <xsl:template match="@id">
	<xsl:attribute name="id"><xsl:value-of select="."/></xsl:attribute>
  </xsl:template>

  <xsl:template match="@type|@role">
	<xsl:choose>
		<xsl:when test="contains(.,':') or contains(.,'.') or contains(.,'\') or contains(.,'/')">
			<instanceOf><subjectIndicatorRef  href="{.}"/></instanceOf>
		</xsl:when>
		<xsl:otherwise>
			<instanceOf><topicRef href="#{.}"/></instanceOf>
		</xsl:otherwise>
	</xsl:choose>
  </xsl:template>

  <xsl:template match="@type|@role" mode="roleSpec">
	<xsl:choose>
		<xsl:when test="contains(.,':') or contains(.,'.') or contains(.,'\') or contains(.,'/')">
			<roleSpec><subjectIndicatorRef  href="{.}"/></roleSpec>
		</xsl:when>
		<xsl:otherwise>
			<roleSpec><topicRef href="#{.}"/></roleSpec>
		</xsl:otherwise>
	</xsl:choose>
  </xsl:template>

  <xsl:template match="@scope">
	<scope>
		<xsl:call-template name="topicRefSplit">
			<xsl:with-param name="topicRefs" select="."/>
		</xsl:call-template>
	</scope>
  </xsl:template>

  <xsl:template name="topicRefSplit">
	<xsl:param name="topicRefs"/>
	<xsl:variable name="n_topicRefs" select="normalize-space($topicRefs)"/>
	<xsl:choose>
	<xsl:when test="contains($n_topicRefs,' ')">
		<topicRef href="#{substring-before($n_topicRefs,' ')}"/>
		<xsl:call-template name="topicRefSplit">
			<xsl:with-param name="topicRefs" select="substring-after($n_topicRefs,' ')"/>
		</xsl:call-template>
	</xsl:when>
	<xsl:otherwise>
		<xsl:if test="$n_topicRefs != ''">
			<topicRef href="#{$n_topicRefs}"/>
		</xsl:if>
	</xsl:otherwise>
	</xsl:choose>
  </xsl:template>

  <xsl:template name="typesRefSplit">
	<xsl:param name="topicRefs"/>
	<xsl:variable name="n_topicRefs" select="normalize-space($topicRefs)"/>
	<xsl:choose>
	<xsl:when test="contains($n_topicRefs,' ')">
		<instanceOf><topicRef href="#{substring-before($n_topicRefs,' ')}"/></instanceOf>
		<xsl:call-template name="typesRefSplit">
			<xsl:with-param name="topicRefs" select="substring-after($n_topicRefs,' ')"/>
		</xsl:call-template>
	</xsl:when>
	<xsl:otherwise>
		<xsl:if test="$n_topicRefs != ''">
			<instanceOf><topicRef href="#{$n_topicRefs}"/></instanceOf>
		</xsl:if>
	</xsl:otherwise>
	</xsl:choose>
  </xsl:template>

</xsl:stylesheet>
