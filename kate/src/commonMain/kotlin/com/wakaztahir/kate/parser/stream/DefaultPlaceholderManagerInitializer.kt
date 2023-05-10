package com.wakaztahir.kate.parser.stream

import com.wakaztahir.kate.model.BlockPlaceholderBlock
import com.wakaztahir.kate.model.ModelDirective
import com.wakaztahir.kate.model.ModelReference
import com.wakaztahir.kate.parser.ParsedBlock
import com.wakaztahir.kate.parser.WriteChar
import com.wakaztahir.kate.parser.WriteString

object DefaultPlaceholderManagerInitializer {

    private const val UnitPlaceholderName = "unit"
    private const val DoublePlaceholderName = "double"
    private const val IntPlaceholderName = "int"
    private const val LongPlaceholderName = "long"
    private const val StringPlaceholderName = "string"
    private const val CharPlaceholderName = "char"
    private const val BooleanPlaceholderName = "boolean"
    private const val ListPlaceholderName = "list"
    private const val MutableListPlaceholderName = "mutable_list"
    private const val ObjectPlaceholderName = "object"

    private fun paramToStringCall(source: SourceStream, call: String): ParsedBlock {
        return ParsedBlock(
            listOf(
                ParsedBlock.CodeGenRange(
                    gen = WriteString(
                        ModelDirective(
                            listOf(
                                ModelReference.Property("__param__"),
                                ModelReference.FunctionCall(call, emptyList())
                            ),
                            referenceModel = source.block.model
                        )
                    ),
                    start = 0,
                    end = 0
                )
            )
        )
    }

    private fun paramToChar(source: SourceStream): ParsedBlock {
        return ParsedBlock(
            listOf(
                ParsedBlock.CodeGenRange(
                    gen = WriteChar(
                        ModelDirective(
                            propertyPath = listOf(ModelReference.Property("__param__")),
                            referenceModel = source.block.model
                        )
                    ),
                    start = 0,
                    end = 0
                )
            )
        )
    }

    fun initializerDefaultPlaceholders(source: SourceStream) {
        val paramToStringParsedBlock = paramToStringCall(source, "toString")
        val paramJoinToStringParsedBlock = paramToStringCall(source, "joinToString")
        source.placeholderManager.placeholders.addAll(
            listOf(
                BlockPlaceholderBlock(
                    block = ParsedBlock(emptyList()),
                    parent = source.block,
                    placeholderName = UnitPlaceholderName,
                    definitionName = UnitPlaceholderName,
                    parameterName = null
                ),
                BlockPlaceholderBlock(
                    block = paramToStringParsedBlock,
                    parent = source.block,
                    placeholderName = DoublePlaceholderName,
                    definitionName = DoublePlaceholderName,
                    parameterName = null
                ),
                BlockPlaceholderBlock(
                    block = paramToStringParsedBlock,
                    parent = source.block,
                    placeholderName = IntPlaceholderName,
                    definitionName = IntPlaceholderName,
                    parameterName = null
                ),
                BlockPlaceholderBlock(
                    block = paramToStringParsedBlock,
                    parent = source.block,
                    placeholderName = LongPlaceholderName,
                    definitionName = LongPlaceholderName,
                    parameterName = null
                ),
                BlockPlaceholderBlock(
                    block = paramToStringParsedBlock,
                    parent = source.block,
                    placeholderName = StringPlaceholderName,
                    definitionName = StringPlaceholderName,
                    parameterName = null
                ),
                BlockPlaceholderBlock(
                    block = paramToChar(source),
                    parent = source.block,
                    placeholderName = CharPlaceholderName,
                    definitionName = CharPlaceholderName,
                    parameterName = null
                ),
                BlockPlaceholderBlock(
                    block = paramToStringParsedBlock,
                    parent = source.block,
                    placeholderName = BooleanPlaceholderName,
                    definitionName = BooleanPlaceholderName,
                    parameterName = null
                ),
                BlockPlaceholderBlock(
                    block = paramJoinToStringParsedBlock,
                    parent = source.block,
                    placeholderName = ListPlaceholderName,
                    definitionName = ListPlaceholderName,
                    parameterName = null
                ),
                BlockPlaceholderBlock(
                    block = paramJoinToStringParsedBlock,
                    parent = source.block,
                    placeholderName = MutableListPlaceholderName,
                    definitionName = MutableListPlaceholderName,
                    parameterName = null
                ),
                BlockPlaceholderBlock(
                    block = paramToStringParsedBlock,
                    parent = source.block,
                    placeholderName = ObjectPlaceholderName,
                    definitionName = ObjectPlaceholderName,
                    parameterName = null
                )
            )
        )
    }

}