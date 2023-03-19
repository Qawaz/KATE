package com.wakaztahir.kte.parser

import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.model.Condition
import com.wakaztahir.kte.model.IfStatement
import com.wakaztahir.kte.model.SingleIf
import com.wakaztahir.kte.parser.stream.*
import com.wakaztahir.kte.parser.stream.escapeSpaces
import com.wakaztahir.kte.parser.stream.increment

internal fun SourceStream.parseConditionType(): ConditionType? {
    if (increment("==")) {
        return ConditionType.Equals
    } else if (increment("!=")) {
        return ConditionType.NotEquals
    } else if (increment('>')) {
        return if (increment('=')) {
            ConditionType.GreaterThanEqualTo
        } else {
            ConditionType.GreaterThan
        }
    } else if (increment('<')) {
        return if (increment('=')) {
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
    val type = parseConditionType()

    val storedValue = propertyFirst.getStoredValue()
    if (type == null && storedValue != null && storedValue is BooleanValue) {
        return EvaluatedCondition(storedValue.value)
    }

    if (type == null) {
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

private fun SourceStream.parseIfBlockValue(ifType: IfType): LazyBlockSlice {
    val previous = pointer

    if (ifType == IfType.Else) {
        incrementUntil("@endif")
    } else {
        incrementUntil("@elseif", "@else", "@endif")
    }

    decrementPointer()
    val length = if (currentChar == ' ') {
        pointer - previous
    } else {
        pointer - previous + 1
    }
    incrementPointer()

    return LazyBlockSlice(
        pointer = previous,
        length = length
    )
}

internal fun SourceStream.parseSingleIf(start: String, ifType: IfType): SingleIf? {
    if (currentChar == '@' && increment(start)) {
        if (ifType != IfType.Else) {
            val condition = parseCondition()
            if (condition != null) {
                if (increment(')')) {
                    increment(' ')
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
            increment(' ')
            val value = parseIfBlockValue(ifType)
            if (!increment("@endif")) {
                throw IllegalStateException("@if must end with @endif")
            }
            return SingleIf(
                condition = EvaluatedCondition(true),
                type = IfType.Else,
                blockValue = value
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