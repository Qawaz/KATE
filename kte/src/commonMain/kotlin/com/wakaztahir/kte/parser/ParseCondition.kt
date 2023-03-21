package com.wakaztahir.kte.parser

import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.parser.stream.SourceStream
import com.wakaztahir.kte.parser.stream.escapeSpaces
import com.wakaztahir.kte.parser.stream.increment
import com.wakaztahir.kte.parser.stream.incrementUntil

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

    if (type == null) {
        val storedValue = propertyFirst as? PrimitiveValue<*>
        return if (storedValue != null) {
            if (storedValue is BooleanValue) {
                EvaluatedCondition(storedValue.value)
            } else {
                throw IllegalStateException("condition cannot contain value of type other than boolean")
            }
        } else {
            ReferencedBoolean(propertyFirst)
        }
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

    val blockEnder: String? = if (ifType == IfType.Else) {
        if (incrementUntil("@endif")) {
            "@endif"
        } else {
            null
        }
    } else {
        incrementUntil("@elseif", "@else", "@endif")
    }

    if (blockEnder == null) {
        throw IllegalStateException("@if block must end with @elseif / @else / @endif")
    }

    val length = pointer - previous

    decrementPointer()
    val spaceDecrement = if (currentChar == ' ') 1 else 0
    incrementPointer()

    return LazyBlockSlice(
        startPointer = previous,
        length = length - spaceDecrement,
        parent = model,
        blockEndPointer = pointer + blockEnder.length
    )
}

internal fun SourceStream.parseSingleIf(start: String, ifType: IfType): SingleIf? {
    if (currentChar == '@' && increment(start)) {
        if (ifType != IfType.Else) {
            val condition = parseCondition()
            if (condition != null) {
                if (increment(')')) {
                    increment(' ')
                    val value = parseIfBlockValue(ifType)
                    return SingleIf(
                        condition = condition,
                        type = ifType,
                        blockValue = value
                    )
                } else {
                    throw IllegalStateException("missing ')' in @if statement")
                }
            }
        } else {
            increment(' ')
            val value = parseIfBlockValue(ifType)
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
    if (increment("@endif")) {
        return IfStatement(mutableListOf(singleIf))
    }
    val ifs = mutableListOf<SingleIf>()
    ifs.add(singleIf)
    while (true) {
        val elseIf = parseElseIf() ?: break
        ifs.add(elseIf)
        if (increment("@endif")) return IfStatement(ifs)
    }
    parseElse()?.let { ifs.add(it) }
    if (increment("@endif")) {
        return IfStatement(ifs)
    } else {
        throw IllegalStateException("@if must end with @endif")
    }
}