package com.wakaztahir.kte.parser

import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.model.Condition
import com.wakaztahir.kte.model.IfStatement
import com.wakaztahir.kte.model.SingleIf
import com.wakaztahir.kte.parser.stream.*
import com.wakaztahir.kte.parser.stream.escapeSpaces
import com.wakaztahir.kte.parser.stream.increment
import com.wakaztahir.kte.parser.stream.parseTextUntil

internal enum class ConditionType {

    Equals {
        override fun verifyCompare(result: Int) = result == 0
    },
    NotEquals {
        override fun verifyCompare(result: Int) = result != 0
    },
    GreaterThan {
        override fun verifyCompare(result: Int) = result == 1
    },
    LessThan {
        override fun verifyCompare(result: Int) = result == -1
    },
    GreaterThanEqualTo {
        override fun verifyCompare(result: Int) = result == 1 || result == 0
    },
    LessThanEqualTo {
        override fun verifyCompare(result: Int) = result == -1 || result == 0
    };

    abstract fun verifyCompare(result: Int): Boolean

}

internal fun SourceStream.parseConditionType(): ConditionType? {
    if (increment("==")) {
        return ConditionType.Equals
    } else if (increment("!=")) {
        return ConditionType.NotEquals
    } else if (increment(">")) {
        return if (increment("=")) {
            ConditionType.GreaterThanEqualTo
        } else {
            ConditionType.GreaterThan
        }
    } else if (increment("<")) {
        return if (increment("=")) {
            ConditionType.LessThanEqualTo
        } else {
            ConditionType.LessThan
        }
    } else {
        return null
    }
}

internal fun SourceStream.parseCondition(): Condition? {


    val propertyFirst = parseDynamicProperty() ?: run {
        return null
    }

    escapeSpaces()
    val type = parseConditionType() ?: run {
        return null
    }

    escapeSpaces()
    val propertySecond = parseDynamicProperty() ?: run {
        throw IllegalStateException("condition's right hand side cannot be found")
    }

    return LogicalCondition(
        propertyFirst = propertyFirst,
        type = type,
        propertySecond = propertySecond
    )
}

private fun SourceStream.parseIfBlockValue(ifType: IfType): String {
    return if (ifType == IfType.Else) {
        parseTextUntilConsumed("@endif")
    } else {
        parseTextUntil("@elseif", "@else", "@endif")
    }
}

internal fun SourceStream.parseSingleIf(start: String, ifType: IfType): SingleIf? {
    if (currentChar == '@' && increment(start)) {
        if (ifType != IfType.Else) {
            val condition = parseCondition()
            if (condition != null) {
                if (increment(")")) {
                    return SingleIf(
                        condition = condition,
                        type = ifType,
                        blockValue = parseIfBlockValue(ifType)
                    )
                } else {
                    throw IllegalStateException("missing ')' in @if statement")
                }
            }
        } else {
            return SingleIf(
                condition = EvaluatedCondition(true),
                type = IfType.Else,
                blockValue = parseIfBlockValue(ifType)
            )
        }
    }
    return null
}

internal fun SourceStream.parseFirstIf(): SingleIf? =
    parseSingleIf(start = "@if(", ifType = IfType.If)

internal fun SourceStream.parseElseIf(): SingleIf? =
    parseSingleIf(start = "@elseif(", ifType = IfType.ElseIf)

internal fun SourceStream.parseElse(): SingleIf? =
    parseSingleIf(start = "@else", ifType = IfType.Else)

internal fun SourceStream.parseIfStatement(): IfStatement? {
    val singleIf = parseFirstIf() ?: return null
    val ifs = mutableListOf<SingleIf>()
    ifs.add(singleIf)
    while (true) {
        val elseIf = parseElseIf() ?: break
        ifs.add(elseIf)
    }
    parseElse()?.let { ifs.add(it) }
    return IfStatement(ifs)
}