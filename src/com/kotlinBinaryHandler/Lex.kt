package com.kotlinBinaryHandler

import java.math.BigDecimal
import java.math.BigInteger
import java.util.regex.Pattern

/**
 * Created by SuperRainbowNinja on 8/01/2018.
 */

const val DEFAULT_STRUCT_PATTERN = "\\s*(?:struct)\\s+(\\w[\\w\\d]+)\\s*\\{(?:((?:\\s|.)*?\\;)\\s*\\}|\\s*\\})"
const val DEFAULT_PROPERTY_PATTERN = "\\s*(\\w[\\w\\d]*)\\s+(\\w[\\w\\d]*)\\s*(?:\\[([^\\;]*)\\])?(?:\\s+=\\s*([^;]+))?\\;"

const val NAME_GROUP = 1
const val DATA_GROUP = 2

class Lex() {
    var structPattern
        get() = sPat.pattern()
        set(value) {
            sPat = if (value[0] == '^')
                Pattern.compile(value)
            else
                Pattern.compile('^' + value)
        }
    var propertyPattern
        get() = pPat.pattern()
        set(value) {
            pPat = if (value[0] == '^')
                Pattern.compile(value)
            else
                Pattern.compile('^' + value)
        }

    private var sPat = Pattern.compile('^' + DEFAULT_STRUCT_PATTERN)
    private var pPat = Pattern.compile('^' + DEFAULT_PROPERTY_PATTERN)



    fun parse(source: String) : ParseResult {
        val structMatch = sPat.matcher(source)
        val structs = ArrayList<ParsedStruct>()

        while (structMatch.lookingAt()) {
            val properties = ArrayList<Property>()
            val propMatch = pPat.matcher(structMatch.group(DATA_GROUP))

            while (propMatch.lookingAt()) {
                when {
                    propMatch.group(4) != null -> properties.add(
                            ParsedMagicNumber(propMatch.group(1),
                                    propMatch.group(2),
                                    getConstantProperty(propMatch.group(4)), propMatch.group(3))
                    )
                    propMatch.group(3) != null -> properties.add(ParsedIndexProperty(propMatch.group(1), propMatch.group(2), propMatch.group(3)))
                    else -> properties.add(ParsedProperty(propMatch.group(1), propMatch.group(2)))
                }

                propMatch.region(propMatch.end(), propMatch.regionEnd())
            }

            structs.add(ParsedStruct(structMatch.group(NAME_GROUP), Array(properties.size, properties::get)))

            structMatch.region(structMatch.end(), structMatch.regionEnd())
        }
        return ParseResult(Array(structs.size, structs::get))
    }
}

sealed class Property
data class ParsedProperty(val type: String, val name: String) : Property()
data class ParsedIndexProperty(val type: String, val name: String, val indexExp: String) : Property()
data class ParsedMagicNumber(val type: String, val name: String, val value: ConstantProperty, val array: String?) : Property()

sealed class ConstantProperty
data class ConsInt(val num: BigInteger) : ConstantProperty()
data class ConsFloat(val num: BigDecimal) : ConstantProperty()
data class ConsString(val str: String) : ConstantProperty()
data class ConsCompound(val arr: Array<ConstantProperty>) : ConstantProperty()

fun getConstantProperty(source: String) : ConstantProperty {
    val n = source.replace("\\s+".toRegex(), "")
    return if (n.isEmpty()) {
        ConsString("")
    } else if (n[0] == '{') {
        ConsCompound(breakUp(n).map { getConstantProperty(it) }.toTypedArray())
    } else {
        if (n.contains('.')) {
            val num = n.toBigDecimalOrNull()
            if (num != null) {
                ConsFloat(num)
            } else {
                ConsString(n)
            }
        } else {
            val num = n.toBigIntegerOrNull()
            if (num != null) {
                ConsInt(num)
            } else {
                ConsString(n)
            }
        }
    }
}

data class ParsedStruct(val name: String, val property: Array<Property>)

data class ParseResult(val structs: Array<ParsedStruct>)


fun breakUp(source: String) : Array<String> {
    var x = 1
    var subStrings = ArrayList<String>()
    var braceCount = 0
    for ((i, char) in source.withIndex()) {
        if (char == '{') {
            braceCount++
        } else if (char == '}') {
            braceCount--
        }
        if ((char == ',' && braceCount == 1) || (char == '}' && braceCount == 0)) {
            subStrings.add(source.substring(x, i))
            x = i+1
        }
    }
    return Array(subStrings.size, subStrings::get)
}
