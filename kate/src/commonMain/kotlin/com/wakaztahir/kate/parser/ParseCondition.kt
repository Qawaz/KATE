package com.wakaztahir.kate.parser

import com.wakaztahir.kate.dsl.ScopedModelLazyParent
import com.wakaztahir.kate.lexer.model.StaticToken
import com.wakaztahir.kate.lexer.tokens.StaticTokens
import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.ReferencedOrDirectValue
import com.wakaztahir.kate.lexer.stream.*
import com.wakaztahir.kate.lexer.stream.escapeSpaces
import com.wakaztahir.kate.lexer.stream.increment
import com.wakaztahir.kate.lexer.stream.incrementUntilDirectiveWithSkip
import com.wakaztahir.kate.parser.stream.ParserSourceStream
import com.wakaztahir.kate.parser.stream.escapeBlockSpacesBackward
import com.wakaztahir.kate.parser.stream.printErrorLineNumberAndCharacterIndex
import com.wakaztahir.kate.parser.variable.parseValueOfType

internal fun ParserSourceStream.parseConditionType(): ConditionType? {
    if (increment(StaticTokens.Equals)) {
        return if (increment(StaticTokens.SingleEqual)) {
            ConditionType.ReferentiallyEquals
        } else {
            ConditionType.Equals
        }
    } else if (increment(StaticTokens.NotEqual)) {
        return ConditionType.NotEquals
    } else if (increment(StaticTokens.BiggerThan)) {
        return if (increment(StaticTokens.SingleEqual)) {
            ConditionType.GreaterThanEqualTo
        } else {
            ConditionType.GreaterThan
        }
    } else if (increment(StaticTokens.LessThan)) {
        return if (increment(StaticTokens.SingleEqual)) {
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

    source.escapeBlockSpacesForward()

    val previous = source.pointer

    val blockEnder = if (ifType == IfType.Else) {
        source.incrementUntilDirectiveWithSkip(StaticTokens.If) {
            source.incrementAndReturnDirective(StaticTokens.EndIf)
        }
    } else {
        source.incrementUntilDirectiveWithSkip(StaticTokens.If) { skips ->
            if (skips == 0) {
                source.incrementAndReturnDirective(StaticTokens.ElseIf) ?: source.incrementAndReturnDirective(
                    StaticTokens.Else
                ) ?: source.incrementAndReturnDirective(StaticTokens.EndIf)
            } else {
                source.incrementAndReturnDirective(StaticTokens.EndIf)
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

internal fun LazyBlock.parseSingleIf(start: StaticToken.String, ifType: IfType): SingleIf? {
    if (source.incrementDirective(start)) {
        if (ifType != IfType.Else) {
            if (!source.increment(StaticTokens.LeftParenthesis)) {
                throw IllegalStateException("missing '(' in $start statement of $ifType")
            }
            val condition = parseCondition(parseDirectRefs = true)
            if (condition != null) {
                if (source.increment(StaticTokens.RightParenthesis)) {
                    source.increment(StaticTokens.SingleSpace)
                    val value = parseIfBlockValue(ifType = ifType)
                    return SingleIf(
                        condition = condition,
                        type = ifType,
                        parsedBlock = value
                    )
                } else {
                    source.printErrorLineNumberAndCharacterIndex()
                    throw IllegalStateException("missing '${StaticTokens.RightParenthesis}' in $start statement of $ifType")
                }
            }
        } else {
            source.increment(StaticTokens.SingleSpace)
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
    parseSingleIf(start = StaticTokens.If, ifType = IfType.If)

internal fun LazyBlock.parseElseIf(): SingleIf? =
    parseSingleIf(start = StaticTokens.ElseIf, ifType = IfType.ElseIf)

internal fun LazyBlock.parseElse(): SingleIf? =
    parseSingleIf(start = StaticTokens.Else, ifType = IfType.Else)

internal fun LazyBlock.parseIfStatement(): IfStatement? {
    val singleIf = parseFirstIf() ?: return null
    val ifs = mutableListOf(singleIf)
    while (true) {
        val elseIf = parseElseIf() ?: break
        ifs.add(elseIf)
    }
    parseElse()?.let { ifs.add(it) }
    if (source.incrementDirective(StaticTokens.EndIf)) {
        return IfStatement(ifs)
    } else {
        println("UNPARSED : ")
        source.printLeft()
        throw IllegalStateException("@if must end with @endif")
    }
}