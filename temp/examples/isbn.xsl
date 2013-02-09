<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i="http://www.publishing.org" version="1.0">
 
    <xsl:output method="text"/>

    <xsl:template match="*"/>

    <xsl:template match="i:BookCatalogue">
        <xsl:for-each select="i:Book">
            <xsl:variable name="checkDigit" select="substring(i:ISBN, 13, 1)"/>
            <xsl:variable name="sum">
                <xsl:call-template name="calculateCorrectCheckDigit">
                    <xsl:with-param name="currentSum" select="0"/>
                    <xsl:with-param name="multiplicationFactor" select="10"/>
                    <xsl:with-param name="portionOfISBNleftToProcess" select="i:ISBN"/>
                </xsl:call-template>
            </xsl:variable> 
            <xsl:variable name="correctCheckDigit" select="11 - ($sum - (floor($sum div 11) * 11))"/>
            <xsl:if test="$checkDigit != 'x'">
                <xsl:if test="$correctCheckDigit != $checkDigit">
                    <xsl:text>Error! Invalid check digit</xsl:text>
                    <xsl:text>

</xsl:text>
                    <xsl:text>   isbn = </xsl:text><xsl:value-of select="i:ISBN"/>
                    <xsl:text>

</xsl:text>
                    <xsl:text>   check digit = </xsl:text><xsl:value-of select="$checkDigit"/>
                    <xsl:text>

</xsl:text>
                    <xsl:text>   correct check digit = </xsl:text><xsl:value-of select="$correctCheckDigit"/>
                    <xsl:text>

</xsl:text>
                    <xsl:text>

</xsl:text>
                </xsl:if>
                <xsl:if test="$correctCheckDigit = $checkDigit">
                    <xsl:text>isbn = </xsl:text><xsl:value-of select="i:ISBN"/>
                    <xsl:text>

</xsl:text>
                    <xsl:text>   check digit </xsl:text><xsl:value-of select="$checkDigit"/><xsl:text> is valid.</xsl:text>
                    <xsl:text>

</xsl:text>
                    <xsl:text>

</xsl:text>
                </xsl:if>
            </xsl:if>   
            <xsl:if test="checkDigit = 'x'">
                <xsl:if test="$correctCheckDigit != 'x'">
                    <xsl:text>Error! Invalid check digit</xsl:text>
                    <xsl:text>

</xsl:text>
                    <xsl:text>   isbn = </xsl:text><xsl:value-of select="i:ISBN"/>
                    <xsl:text>

</xsl:text>
                    <xsl:text>   check digit = </xsl:text><xsl:value-of select="$checkDigit"/>
                    <xsl:text>

</xsl:text>
                    <xsl:text>   correct check digit = </xsl:text><xsl:value-of select="$correctCheckDigit"/>
                    <xsl:text>

</xsl:text>
                    <xsl:text>

</xsl:text>
                </xsl:if>
                <xsl:if test="$correctCheckDigit = 'x'">
                    <xsl:text>isbn = </xsl:text><xsl:value-of select="i:ISBN"/>
                    <xsl:text>

</xsl:text>
                    <xsl:text>   check digit </xsl:text><xsl:value-of select="$checkDigit"/><xsl:text> is valid.</xsl:text>
                    <xsl:text>

</xsl:text>
                    <xsl:text>

</xsl:text>
                </xsl:if>
            </xsl:if> 
        </xsl:for-each>
    </xsl:template>             


    <xsl:template name="calculateCorrectCheckDigit">
        <xsl:param name="currentSum"/>
        <xsl:param name="multiplicationFactor"/>
        <xsl:param name="portionOfISBNleftToProcess"/>
        <xsl:choose>
            <xsl:when test="$multiplicationFactor = 1">
                <xsl:value-of select="$currentSum"/>
            </xsl:when>
            <xsl:when test="substring($portionOfISBNleftToProcess, 1, 1) = ' '">
                <xsl:call-template name="calculateCorrectCheckDigit">
                    <xsl:with-param name="currentSum" select="$currentSum"/>
                    <xsl:with-param name="multiplicationFactor" select="$multiplicationFactor"/>
                    <xsl:with-param name="portionOfISBNleftToProcess" select="substring($portionOfISBNleftToProcess, 2)"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="substring($portionOfISBNleftToProcess, 1, 1) = '-'">
                <xsl:call-template name="calculateCorrectCheckDigit">
                    <xsl:with-param name="currentSum" select="$currentSum"/>
                    <xsl:with-param name="multiplicationFactor" select="$multiplicationFactor"/>
                    <xsl:with-param name="portionOfISBNleftToProcess" select="substring($portionOfISBNleftToProcess, 2)"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="addThisValue" select="substring($portionOfISBNleftToProcess, 1, 1) * $multiplicationFactor"/>
                <xsl:call-template name="calculateCorrectCheckDigit">
                    <xsl:with-param name="currentSum" select="$currentSum + $addThisValue"/>
                    <xsl:with-param name="multiplicationFactor" select="$multiplicationFactor - 1"/>
                    <xsl:with-param name="portionOfISBNleftToProcess" select="substring($portionOfISBNleftToProcess, 2)"/>
                </xsl:call-template>
           </xsl:otherwise>
       </xsl:choose>
    </xsl:template>

</xsl:stylesheet>