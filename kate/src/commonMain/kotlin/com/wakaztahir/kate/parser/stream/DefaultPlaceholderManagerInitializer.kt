package com.wakaztahir.kate.parser.stream

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

    private fun placeholderBlock(name: String, content: String) =
        "@define_placeholder($name) $content @end_define_placeholder"

    private fun functionPlaceholder(name: String, call: String, writer: String = "print_string") =
        placeholderBlock(name = name, content = "@runtime.$writer(__param__$call)")

    private fun toStringPlaceholder(name: String) = functionPlaceholder(name = name, call = ".toString()")
    private fun joinToStringPlaceholder(name: String) = functionPlaceholder(name = name, call = ".joinToString()")
    private fun toCharPlaceholder(name: String) = functionPlaceholder(name = name, call = "", writer = "print_char")

    fun initializerDefaultPlaceholders(source: SourceStream) {
        val destination = TextDestinationStream()
        try {
            TextSourceStream(
                sourceCode = placeholderBlock(name = UnitPlaceholderName, content = "") +
                        toStringPlaceholder(DoublePlaceholderName) +
                        toStringPlaceholder(IntPlaceholderName) +
                        toStringPlaceholder(LongPlaceholderName) +
                        functionPlaceholder(StringPlaceholderName, call = "") +
                        toCharPlaceholder(CharPlaceholderName) +
                        toStringPlaceholder(BooleanPlaceholderName) +
                        joinToStringPlaceholder(ListPlaceholderName) +
                        joinToStringPlaceholder(MutableListPlaceholderName) +
                        toStringPlaceholder(ObjectPlaceholderName),
                model = source.model,
                placeholderManager = source.placeholderManager,
                embeddingManager = source.embeddingManager,
                initialize = false
            ).block.generateTo(destination)
        }catch (e : Exception){
            throw IllegalStateException("default placeholder initialization failed",e)
        }
        require(destination.getValue().isEmpty())
    }

}