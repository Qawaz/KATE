package com.wakaztahir.kate.parser.variable

import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.model.model.ReferencedOrDirectValue
import com.wakaztahir.kate.parser.*

private class CharacterValueExpressionParser(private val parseDirectRefs: Boolean) : ExpressionValueParser {
    override fun LazyBlock.parseExpressionValue(): ReferencedOrDirectValue? {
        source.parseCharacterValue()?.let { return it }
        parseVariableReference(parseDirectRefs = parseDirectRefs)?.let { return it }
        return null
    }
}

private class StringValueExpressionParser(private val parseDirectRefs: Boolean) : ExpressionValueParser {
    override fun LazyBlock.parseExpressionValue(): ReferencedOrDirectValue? {
        source.parseStringValue()?.let { return it }
        parseVariableReference(parseDirectRefs = parseDirectRefs)?.let { return it }
        return null
    }
}

private class NumberValueExpressionParser(private val type: KATEType, private val parseDirectRefs: Boolean) :
    ExpressionValueParser {

    private fun PrimitiveValue<*>.verifyPrimitiveType(): KATEValue {
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

    override fun LazyBlock.parseExpressionValue(): ReferencedOrDirectValue? {
        source.parseNumberValue()?.verifyPrimitiveType()?.let { return it }
        parseVariableReference(parseDirectRefs = parseDirectRefs)?.let { return it }
        return null
    }
}

private class BooleanValueExpressionParser(val parseDirectRefs: Boolean) : ExpressionValueParser {
    override fun LazyBlock.parseExpressionValue(): ReferencedOrDirectValue? {
        source.parseBooleanValue()?.let { return it }
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

        is KATEType.Class -> {
            // TODO class should be parsed
            return null
        }

        is KATEType.Function -> {
            // TODO function should be parsed
            return null
        }

        is KATEType.TypeWithMetadata -> {
            parseValueOfType(
                type = type.actual,
                allowAtLessExpressions = allowAtLessExpressions,
                parseDirectRefs = parseDirectRefs,
            )
        }

        is KATEType.NullableKateType -> {
            parseValueOfType(
                type = type.actual,
                allowAtLessExpressions = allowAtLessExpressions,
                parseDirectRefs = parseDirectRefs,
            )
        }

        is KATEType.Any -> {
            parseAnyExpressionOrValue(
                parseDirectRefs = parseDirectRefs
            )
        }

        is KATEType.Boolean -> {
            source.parseBooleanValue()?.let { return it }
            parseVariableReference(parseDirectRefs = parseDirectRefs)?.let { return it }
        }

        is KATEType.Char -> {
            parseExpression(
                valueParser = CharacterValueExpressionParser(parseDirectRefs = parseDirectRefs)
            )
        }

        is KATEType.Double -> {
            parseExpression(
                valueParser = NumberValueExpressionParser(parseDirectRefs = parseDirectRefs, type = type)
            )
        }

        is KATEType.Int -> {
            parseExpression(
                valueParser = NumberValueExpressionParser(parseDirectRefs = parseDirectRefs, type = type)
            )
        }

        is KATEType.MutableList -> {
            parseMutableListDefinition(itemType = type.itemType)?.let { return it }
            parseVariableReference(parseDirectRefs = parseDirectRefs)?.let { return it }
        }

        is KATEType.List -> {
            parseListDefinition(itemType = type.itemType)?.let { return it }
            parseMutableListDefinition(itemType = type.itemType)?.let { return it }
            parseVariableReference(parseDirectRefs = parseDirectRefs)?.let { return it }
        }

        is KATEType.Long -> {
            parseExpression(
                valueParser = NumberValueExpressionParser(parseDirectRefs = parseDirectRefs, type = type)
            )
        }

        is KATEType.Object -> {
            parseVariableReference(parseDirectRefs = parseDirectRefs)?.let { return it }
        }

        is KATEType.String -> {
            parseExpression(
                valueParser = StringValueExpressionParser(parseDirectRefs = parseDirectRefs)
            )
        }

    }
}