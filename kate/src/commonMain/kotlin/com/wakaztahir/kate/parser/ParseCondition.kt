package com.wakaztahir.kate.parser

import com.wakaztahir.kate.dsl.ScopedModelLazyParent
import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.ReferencedOrDirectValue
import com.wakaztahir.kate.parser.stream.*
import com.wakaztahir.kate.parser.stream.escapeSpaces
import com.wakaztahir.kate.parser.stream.increment
import com.wakaztahir.kate.parser.stream.incrementUntilDirectiveWithSkip
import com.wakaztahir.kate.parser.variable.parseValueOfType

internal fun ParserSourceStream.parseConditionType(): ConditionType? {
    if (increment("==")) {
        return if (increment('=')) {
            ConditionType.ReferentiallyEquals
        } else {
            ConditionType.Equals
        }
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

internal fun LazyBlock.parseConditionAfter(
    propertyFirst: ReferencedOrDirectValue,
    type: ConditionType,
    valueType: KATEType,
    allowAtLessExpressions: Boolean,
    parseDirectRefs: Boolean
): Condition? {
    val propertySecond = parseValueOfType(
        type = valueType,
        allowAtLessExpressions = allowAtLessExpressions,
        parseDirectRefs = parseDirectRefs
    )
    return if (propertySecond != null) {
        LogicalCondition(
            propertyFirst = propertyFirst,
            type = type,
            propertySecond = propertySecond
        )
    } else {
        null
    }
}

internal fun LazyBlock.parseCondition(parseDirectRefs: Boolean) = parseCondition(
    valueType = KATEType.Any,
    allowAtLessExpressions = true,
    parseDirectRefs = parseDirectRefs
)

internal fun LazyBlock.parseCondition(
    valueType: KATEType,
    allowAtLessExpressions: Boolean,
    parseDirectRefs: Boolean
): ReferencedOrDirectValue? {

    val propertyFirst = parseValueOfType(
        type = valueType,
        allowAtLessExpressions = allowAtLessExpressions,
        parseDirectRefs = parseDirectRefs
    ) ?: return null

    source.escapeSpaces()
    val type = source.parseConditionType() ?: return propertyFirst

    source.escapeSpaces()

    return parseConditionAfter(
        propertyFirst = propertyFirst,
        type = type,
        valueType = valueType,
        allowAtLessExpressions = allowAtLessExpressions,
        parseDirectRefs = parseDirectRefs
    )

}

private fun LazyBlock.parseIfBlockValue(ifType: IfType): IfParsedBlock {

    escapeBlockSpacesForward()

    val previous = source.pointer

    val blockEnder: String? = if (ifType == IfType.Else) {
        source.incrementUntilDirectiveWithSkip("@if") {
            if (source.increment("@endif")) "@endif" else null
        }
    } else {
        source.incrementUntilDirectiveWithSkip("@if") { skips ->
            if (skips == 0) {
                if (source.increment("@elseif")) {
                    "@elseif"
                } else if (source.increment("@else")) {
                    "@else"
                } else if (source.increment("@endif")) {
                    "@endif"
                } else null
            } else {
                if (source.increment("@endif")) "@endif" else null
            }
        }
    }

    if (blockEnder == null) {
        throw IllegalStateException("@if block must end with @elseif / @else / @endif")
    }

    source.decrementPointer(blockEnder.length)

    val pointerBeforeEnder = source.pointer

    escapeBlockSpacesBackward()

    val length = source.pointer - previous

    val block = LazyBlockSlice(
        parentBlock = this,
        startPointer = previous,
        length = length,
        provider = ModelProvider.Single(ScopedModelLazyParent { model }),
        blockEndPointer = source.pointer + blockEnder.length,
        isDefaultNoRaw = isDefaultNoRaw,
        indentationLevel = indentationLevel + 1
    )

    val parsedBlock = block.parse()

    source.setPointerAt(pointerBeforeEnder)

    return IfParsedBlock(provider = block.provider, parsedBlock.codeGens)
}

internal fun LazyBlock.parseSingleIf(start: String, ifType: IfType): SingleIf? {
    if (source.currentChar == '@' && source.increment(start)) {
        if (ifType != IfType.Else) {
            val condition = parseCondition(parseDirectRefs = true)
            if (condition != null) {
                if (source.increment(')')) {
                    source.increment(' ')
                    val value = parseIfBlockValue(ifType = ifType)
                    return SingleIf(
                        condition = condition,
                        type = ifType,
                        parsedBlock = value
                    )
                } else {
                    source.printErrorLineNumberAndCharacterIndex()
                    throw IllegalStateException("missing ')' in $start statement of $ifType")
                }
            }
        } else {
            source.increment(' ')
            val value = parseIfBlockValue(ifType = ifType)
            return SingleIf(
                condition = BooleanValue(true),
                type = IfType.Else,
                parsedBlock = value
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
    val ifs = mutableListOf(singleIf)
    while (true) {
        val elseIf = parseElseIf() ?: break
        ifs.add(elseIf)
    }
    parseElse()?.let { ifs.add(it) }
    if (source.increment("@endif")) {
        return IfStatement(ifs)
    } else {
        println("UNPARSED : ")
        source.printLeft()
        throw IllegalStateException("@if must end with @endif")
    }
}