package com.wakaztahir.kte.parser

import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.parser.stream.*
import com.wakaztahir.kte.parser.stream.escapeSpaces
import com.wakaztahir.kte.parser.stream.increment
import com.wakaztahir.kte.parser.stream.incrementUntilDirectiveWithSkip

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

    val propertyFirst = this.parseAnyExpressionOrValue() ?: run {
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
    val propertySecond = this.parseAnyExpressionOrValue() ?: run {
        throw IllegalStateException("condition's right hand side cannot be found")
    }

    return LogicalCondition(
        propertyFirst = propertyFirst,
        type = type,
        propertySecond = propertySecond
    )
}

private fun LazyBlock.parseIfBlockValue(ifType: IfType): LazyBlockSlice {

    source.escapeBlockSpacesForward()

    val previous = source.pointer

    val blockEnder: String? = if (ifType == IfType.Else) {
        source.incrementUntilDirectiveWithSkip("@if") {
            if (source.increment("@endif")) "@endif" else null
        }
    } else {
        source.incrementUntilDirectiveWithSkip("@if") {
            if (source.increment("@elseif")) {
                "@elseif"
            } else if (source.increment("@else")) {
                "@else"
            } else if (source.increment("@endif")) {
                "@endif"
            } else null
        }
    }

    if (blockEnder == null) {
        throw IllegalStateException("@if block must end with @elseif / @else / @endif")
    }

    source.decrementPointer(blockEnder.length)

    val pointerBeforeEnder = source.pointer

    source.escapeBlockSpacesBackward()

    val length = source.pointer - previous

    source.setPointerAt(pointerBeforeEnder)

    return LazyBlockSlice(
        source = source,
        startPointer = previous,
        length = length,
        parent = this@parseIfBlockValue.model,
        blockEndPointer = source.pointer + blockEnder.length
    )
}

internal fun LazyBlock.parseSingleIf(start: String, ifType: IfType): SingleIf? {
    if (source.currentChar == '@' && source.increment(start)) {
        if (ifType != IfType.Else) {
            val condition = source.parseCondition()
            if (condition != null) {
                if (source.increment(')')) {
                    source.increment(' ')
                    val value = parseIfBlockValue(ifType = ifType)
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
            source.increment(' ')
            val value = parseIfBlockValue(ifType = ifType)
            return SingleIf(
                condition = EvaluatedCondition(true),
                type = IfType.Else,
                blockValue = value
            )
        }
    }
    return null
}

internal fun LazyBlock.parseFirstIf(): SingleIf? =
    parseSingleIf(start = "@if(", ifType = IfType.If)

internal fun LazyBlock.parseElseIf(): SingleIf? =
    parseSingleIf(start = "@elseif(", ifType = IfType.ElseIf)

internal fun LazyBlock.parseElse(): SingleIf? =
    parseSingleIf(start = "@else", ifType = IfType.Else)

internal fun LazyBlock.parseIfStatement(): IfStatement? {
    val singleIf = parseFirstIf() ?: return null
    if (source.increment("@endif")) {
        return IfStatement(mutableListOf(singleIf))
    }
    val ifs = mutableListOf<SingleIf>()
    ifs.add(singleIf)
    while (true) {
        val elseIf = parseElseIf() ?: break
        ifs.add(elseIf)
        if (source.increment("@endif")) return IfStatement(ifs)
    }
    parseElse()?.let { ifs.add(it) }
    if (source.increment("@endif")) {
        return IfStatement(ifs)
    } else {
        throw IllegalStateException("@if must end with @endif")
    }
}