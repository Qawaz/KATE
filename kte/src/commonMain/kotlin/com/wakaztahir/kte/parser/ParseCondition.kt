package com.wakaztahir.kte.parser

import com.wakaztahir.kte.dsl.ScopedModelObject
import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.model.model.MutableTemplateModel
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

internal interface BreakableIfBlockParser : BlockParser {

    val startPointer: Int
    val ifType: IfType

    override fun hasNext(stream: SourceStream): Boolean {
        return !stream.hasEnded && !incrementEnder(stream)
    }

    override fun generateTo(source: SourceStream, destination: DestinationStream) {
        source.setPointerAt(startPointer)
        super.generateTo(source, destination)
    }

    private fun incrementEnder(source: SourceStream): Boolean {
        val ender = parseBreakIfEnder(source)
        return if (ender != null) {
            source.incrementPointer(ender.length)
            true
        } else {
            false
        }
    }

    private fun parseBreakIfEnder(source: SourceStream): String? {
        return if (source.currentChar == '@') {
            return if (ifType == IfType.Else && source.incrementUntil("@endif")) "@endif" else {
                source.incrementUntil("@elseif", "@endif")
            }
        } else {
            null
        }
    }

}

private class IfParser(parent: MutableTemplateModel, override val ifType: IfType, override val startPointer: Int) :
    BreakableIfBlockParser {
    override val model = ScopedModelObject(parent)
}

internal fun BlockParser.parseSingleIf(source: SourceStream, start: String, ifType: IfType): SingleIf? {
    if (source.currentChar == '@' && source.increment(start)) {
        if (ifType != IfType.Else) {
            val condition = source.parseCondition()
            if (condition != null) {
                if (source.increment(')')) {
                    source.increment(' ')
                    val parser = IfParser(model, ifType, source.pointer)
                    return SingleIf(
                        parser = parser,
                        condition = condition,
                        type = ifType
                    )
                } else {
                    throw IllegalStateException("missing ')' in @if statement")
                }
            }
        } else {
            source.increment(' ')
            val parser = IfParser(model, ifType, source.pointer)
            while (parser.hasNext(source)) source.incrementPointer()
            return SingleIf(
                parser = parser,
                condition = EvaluatedCondition(true),
                type = ifType
            )
        }
    }
    return null
}

internal fun BlockParser.parseFirstIf(source: SourceStream): SingleIf? =
    parseSingleIf(source = source, start = "@if(", ifType = IfType.If)

internal fun BlockParser.parseElseIf(source: SourceStream): SingleIf? =
    parseSingleIf(source = source, start = "@elseif(", ifType = IfType.ElseIf)

internal fun BlockParser.parseElse(source: SourceStream): SingleIf? =
    parseSingleIf(source = source, start = "@else", ifType = IfType.Else)

internal fun BlockParser.parseIfStatement(source: SourceStream): IfStatement? {
    val singleIf = parseFirstIf(source = source) ?: return null
    if (source.increment("@endif")) {
        return IfStatement(mutableListOf(singleIf))
    }
    val ifs = mutableListOf<SingleIf>()
    ifs.add(singleIf)
    while (true) {
        val elseIf = parseElseIf(source = source) ?: break
        ifs.add(elseIf)
        if (source.increment("@endif")) return IfStatement(ifs)
    }
    parseElse(source = source)?.let { ifs.add(it) }
    return IfStatement(ifs)
}