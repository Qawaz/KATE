package com.wakaztahir.kate.parser.variable

import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.model.model.ReferencedValue
import com.wakaztahir.kate.parser.*
import com.wakaztahir.kate.parser.stream.SourceStream

private class CharacterValueExpressionParser(private val parseDirectRefs: Boolean) : ExpressionValueParser {
    override fun SourceStream.parseExpressionValue(): ReferencedValue? {
        parseCharacterValue()?.let { return it }
        parseVariableReference(parseDirectRefs = parseDirectRefs)?.let { return it }
        return null
    }
}

private class StringValueExpressionParser(private val parseDirectRefs: Boolean) : ExpressionValueParser {
    override fun SourceStream.parseExpressionValue(): ReferencedValue? {
        parseStringValue()?.let { return it }
        parseVariableReference(parseDirectRefs = parseDirectRefs)?.let { return it }
        return null
    }
}

private class NumberValueExpressionParser(private val type: KATEType, private val parseDirectRefs: Boolean) :
    ExpressionValueParser {
    override fun SourceStream.parseExpressionValue(): ReferencedValue? {
        parseNumberValue()?.let { value ->
            when (value) {
                is IntValue -> {
                    when (type) {
                        is KATEType.Int -> value

                        is KATEType.Long -> {
                            LongValue(value.value.toLong())
                        }

                        else -> {
                            throw IllegalStateException("$value is not of type $type")
                        }
                    }
                }

                is DoubleValue -> if (type is KATEType.Double) value else throw IllegalStateException()
                is LongValue -> if (type is KATEType.Long) value else throw IllegalStateException()
                else -> {
                    throw IllegalStateException()
                }
            }
        }?.let { return it }
        parseVariableReference(parseDirectRefs = parseDirectRefs)?.let { return it }
        return null
    }
}

fun LazyBlock.parseValueOfType(
    type: KATEType,
    allowAtLessExpressions: Boolean,
    parseDirectRefs: Boolean
): KATEValue? {
    return when (type) {

        is KATEType.Unit -> {
            return null
        }

        is KATEType.NullableKateType -> {
            parseValueOfType(
                type = type.actual,
                allowAtLessExpressions = allowAtLessExpressions,
                parseDirectRefs = parseDirectRefs,
            )
        }

        is KATEType.Any -> {
            source.parseAnyExpressionOrValue(
                parseFirstStringOrChar = true,
                parseNotFirstStringOrChar = true,
                parseDirectRefs = parseDirectRefs,
                allowAtLessExpressions = allowAtLessExpressions
            )
        }

        is KATEType.Boolean -> {
            source.parseBooleanValue()?.let { return it }
            source.parseVariableReference(parseDirectRefs = parseDirectRefs)?.let { return it }
        }

        is KATEType.Char -> {
            source.parseExpression(
                firstValueParser = CharacterValueExpressionParser(parseDirectRefs = parseDirectRefs),
                notFirstValueParser = { CharacterValueExpressionParser(parseDirectRefs = parseDirectRefs) },
                allowAtLessExpressions = allowAtLessExpressions
            )
        }

        is KATEType.Double -> {
            source.parseExpression(
                firstValueParser = NumberValueExpressionParser(parseDirectRefs = parseDirectRefs, type = type),
                notFirstValueParser = { NumberValueExpressionParser(parseDirectRefs = parseDirectRefs, type = type) },
                allowAtLessExpressions = allowAtLessExpressions
            )
        }

        is KATEType.Int -> {
            source.parseExpression(
                firstValueParser = NumberValueExpressionParser(parseDirectRefs = parseDirectRefs, type = type),
                notFirstValueParser = { NumberValueExpressionParser(parseDirectRefs = parseDirectRefs, type = type) },
                allowAtLessExpressions = allowAtLessExpressions
            )
        }

        is KATEType.MutableList -> {
            parseMutableListDefinition()?.let { return it }
            source.parseVariableReference(parseDirectRefs = parseDirectRefs)?.let { return it }
        }

        is KATEType.List -> {
            parseListDefinition()?.let { return it }
            parseMutableListDefinition()?.let { return it }
            source.parseVariableReference(parseDirectRefs = parseDirectRefs)?.let { return it }
        }

        is KATEType.Long -> {
            source.parseExpression(
                firstValueParser = NumberValueExpressionParser(parseDirectRefs = parseDirectRefs, type = type),
                notFirstValueParser = { NumberValueExpressionParser(parseDirectRefs = parseDirectRefs, type = type) },
                allowAtLessExpressions = allowAtLessExpressions
            )
        }

        is KATEType.Object -> {
            source.parseVariableReference(parseDirectRefs = parseDirectRefs)?.let { return it }
        }

        is KATEType.String -> {
            source.parseExpression(
                firstValueParser = StringValueExpressionParser(parseDirectRefs = parseDirectRefs),
                notFirstValueParser = { StringValueExpressionParser(parseDirectRefs = parseDirectRefs) },
                allowAtLessExpressions = allowAtLessExpressions
            )
        }

    }
}