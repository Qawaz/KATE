package com.wakaztahir.kate.parser.variable

import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.model.model.ReferencedOrDirectValue
import com.wakaztahir.kate.parser.*
import com.wakaztahir.kate.parser.stream.SourceStream

private class CharacterValueExpressionParser(private val parseDirectRefs: Boolean) : ExpressionValueParser {
    override fun SourceStream.parseExpressionValue(): ReferencedOrDirectValue? {
        parseCharacterValue()?.let { return it }
        parseVariableReference(parseDirectRefs = parseDirectRefs)?.let { return it }
        return null
    }
}

private class StringValueExpressionParser(private val parseDirectRefs: Boolean) : ExpressionValueParser {
    override fun SourceStream.parseExpressionValue(): ReferencedOrDirectValue? {
        parseStringValue()?.let { return it }
        parseVariableReference(parseDirectRefs = parseDirectRefs)?.let { return it }
        return null
    }
}

private class NumberValueExpressionParser(private val type: KATEType, private val parseDirectRefs: Boolean) :
    ExpressionValueParser {

    private fun PrimitiveValue<*>.verifyPrimitiveType() : KATEValue {
        return when (this) {
            is IntValue -> {
                when (type) {
                    is KATEType.Int -> this

                    is KATEType.Long -> {
                        LongValue(this.value.toLong())
                    }

                    else -> {
                        throw IllegalStateException("$this is not of type $type")
                    }
                }
            }

            is DoubleValue -> if (type is KATEType.Double) this else throw IllegalStateException()
            is LongValue -> if (type is KATEType.Long) this else throw IllegalStateException()
            else -> {
                throw IllegalStateException("Unknown type of primitive for type $type")
            }
        }
    }

    override fun SourceStream.parseExpressionValue(): ReferencedOrDirectValue? {
        parseNumberValue()?.verifyPrimitiveType()?.let { return it }
        parseVariableReference(parseDirectRefs = parseDirectRefs)?.let { return it }
        return null
    }
}

fun LazyBlock.parseValueOfType(
    type: KATEType,
    allowAtLessExpressions: Boolean,
    parseDirectRefs: Boolean
): ReferencedOrDirectValue? {
    return when (type) {

        is KATEType.Unit -> {
            return null
        }

        is KATEType.Function -> {
            // TODO function should be parsed
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
            parseMutableListDefinition(itemType = type.itemType)?.let { return it }
            source.parseVariableReference(parseDirectRefs = parseDirectRefs)?.let { return it }
        }

        is KATEType.List -> {
            parseListDefinition(itemType = type.itemType)?.let { return it }
            parseMutableListDefinition(itemType = type.itemType)?.let { return it }
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