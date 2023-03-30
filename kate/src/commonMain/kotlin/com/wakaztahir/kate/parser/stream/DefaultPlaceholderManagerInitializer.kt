package com.wakaztahir.kate.parser.stream

import com.wakaztahir.kate.model.TextPlaceholderBlock

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

    fun initializerDefaultPlaceholders(source: SourceStream) {
        source.placeholderManager.placeholders.addAll(
            listOf(
                TextPlaceholderBlock(
                    text = "",
                    parent = source,
                    placeholderName = UnitPlaceholderName,
                    definitionName = UnitPlaceholderName
                ),
                TextPlaceholderBlock(
                    text = "@runtime.print_string(@var(__param__.toString()))",
                    parent = source,
                    placeholderName = DoublePlaceholderName,
                    definitionName = DoublePlaceholderName
                ),
                TextPlaceholderBlock(
                    text = "@runtime.print_string(@var(__param__.toString()))",
                    parent = source,
                    placeholderName = IntPlaceholderName,
                    definitionName = IntPlaceholderName
                ),
                TextPlaceholderBlock(
                    text = "@runtime.print_string(@var(__param__.toString()))",
                    parent = source,
                    placeholderName = LongPlaceholderName,
                    definitionName = LongPlaceholderName
                ),
                TextPlaceholderBlock(
                    text = "@runtime.print_string(@var(__param__))",
                    parent = source,
                    placeholderName = StringPlaceholderName,
                    definitionName = StringPlaceholderName
                ),
                TextPlaceholderBlock(
                    text = "@runtime.print_char(@var(__param__))",
                    parent = source,
                    placeholderName = CharPlaceholderName,
                    definitionName = CharPlaceholderName
                ),
                TextPlaceholderBlock(
                    text = "@if(@var(__param__)) @runtime.print_string(\"true\") @else @runtime.print_string(\"false\") @endif",
                    parent = source,
                    placeholderName = BooleanPlaceholderName,
                    definitionName = BooleanPlaceholderName
                ),
                TextPlaceholderBlock(
                    text = "@runtime.print_string(@var(__param__.joinToString()))",
                    parent = source,
                    placeholderName = ListPlaceholderName,
                    definitionName = ListPlaceholderName
                ),
                TextPlaceholderBlock(
                    text = "@runtime.print_string(@var(__param__.joinToString()))",
                    parent = source,
                    placeholderName = MutableListPlaceholderName,
                    definitionName = MutableListPlaceholderName
                ),
                TextPlaceholderBlock(
                    text = "@runtime.print_string(@var(__param__.toString()))",
                    parent = source,
                    placeholderName = ObjectPlaceholderName,
                    definitionName = ObjectPlaceholderName
                )
            )
        )
    }

}