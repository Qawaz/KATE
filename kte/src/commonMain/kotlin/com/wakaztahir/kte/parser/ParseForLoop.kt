package com.wakaztahir.kte.parser

import com.wakaztahir.kte.model.Condition
import com.wakaztahir.kte.model.DynamicProperty
import com.wakaztahir.kte.model.ReferencedValue
import com.wakaztahir.kte.parser.stream.*
import com.wakaztahir.kte.parser.stream.increment
import com.wakaztahir.kte.parser.stream.parseTextUntilConsumed
import com.wakaztahir.kte.parser.stream.parseTextWhile

internal sealed interface ForLoop {

    val blockValue: String

    class ConditionalFor(
        val condition: Condition,
        override val blockValue: String
    ) : ForLoop

    class IterableFor(
        val indexConstName: String?,
        val elementConstName: String,
        val listProperty: ReferencedValue,
        override val blockValue: String
    ) : ForLoop

}

private fun SourceStream.parseForBlockValue(): String {
    return parseTextUntilConsumed("@endfor").let {
        if (it.lastOrNull() == ' ') it.substringBeforeLast(' ') else it
    }
}

internal fun SourceStream.incrementBreakFor(): Boolean {
    return currentChar == '@' && increment("@breakfor")
}

internal fun SourceStream.parseForLoop(): ForLoop? {
    if (currentChar == '@' && increment("@for(")) {

        val condition = parseCondition()
        if (condition != null) {
            increment(')')
            increment(' ')
            val blockValue = parseForBlockValue()
            return ForLoop.ConditionalFor(
                condition = condition,
                blockValue = blockValue
            )
        }

        val variableName = parseConstantVariableName()
        if (variableName != null) {
            var secondVariableName: String? = null
            if (increment(',')) {
                secondVariableName = parseTextWhile { currentChar.isConstantVariableName() }
            }
            escapeSpaces()
            increment(':')
            escapeSpaces()
            val referencedValue = parseReferencedValue()
            escapeSpaces()
            increment(')')
            increment(' ')
            val blockValue = parseForBlockValue()
            if (referencedValue != null) {
                return ForLoop.IterableFor(
                    indexConstName = secondVariableName,
                    elementConstName = variableName,
                    listProperty = referencedValue,
                    blockValue = blockValue
                )
            }
        }

    }
    return null
}