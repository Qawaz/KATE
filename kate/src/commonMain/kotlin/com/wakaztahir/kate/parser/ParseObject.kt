package com.wakaztahir.kate.parser

import com.wakaztahir.kate.lexer.tokens.StaticTokens
import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.parser.block.ParsedBlock
import com.wakaztahir.kate.lexer.stream.*
import com.wakaztahir.kate.lexer.stream.parseTextWhile
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.parser.stream.ParserSourceStream
import com.wakaztahir.kate.parser.variable.parseKATEType

private fun Char.isObjectName(): Boolean = isLetterOrDigit() || this == '_'

private fun ParserSourceStream.parseObjectName(): String {
    if (increment(StaticTokens.LeftParenthesis)) {
        val text = parseTextWhile { currentChar.isObjectName() }
        if (increment(StaticTokens.RightParenthesis)) {
            return text
        } else {
            throw IllegalStateException("expected '${StaticTokens.RightParenthesis}' while declaring object $text got $currentChar")
        }
    } else {
        throw IllegalStateException("expected '${StaticTokens.LeftParenthesis}' while declaring object got $currentChar")
    }
}

class ObjectDeclarationParsedBlock(val model: ObjectDeclarationModel, codeGens: List<CodeGenRange>) :
    ParsedBlock(codeGens) {
    override fun generateTo(destination: DestinationStream) {
        for (range in codeGens) {
            range.gen.generateTo(destination = destination)
        }
    }
}

private fun LazyBlock.parseObjectDeclarationSlice(
    objectName: String,
    itemType: KATEType?
): ObjectDeclarationParsedBlock {

    val slice = parseBlockSlice(
        startsWith = StaticTokens.DefineObject,
        endsWith = StaticTokens.EndDefineObject,
        isDefaultNoRaw = false,
        inheritModel = true
    )

    val current = ObjectDeclarationModel(objectName = objectName, parent = model)

    val block = ObjectDeclarationBlockSlice(
        parentBlock = this,
        startPointer = slice.startPointer,
        length = slice.length,
        blockEndPointer = slice.blockEndPointer,
        provider = ModelProvider.Single(current),
        indentationLevel = indentationLevel + 1
    ).parse()

    return ObjectDeclarationParsedBlock(model = current, codeGens = block.codeGens)

}

fun LazyBlock.parseObjectDeclaration(): ObjectDeclaration? {
    if (source.incrementDirective(StaticTokens.DefineObject)) {
        val itemType: KATEType? = if (source.increment(StaticTokens.LessThan)) {
            val type = source.parseKATEType(parseMetadata = false)
            if (!source.increment(StaticTokens.BiggerThan)) {
                throw IllegalStateException("expected '${StaticTokens.BiggerThan}' after type declaration got '${source.currentChar}'")
            }
            type
        } else {
            null
        }
        val objectName = source.parseObjectName()
        return ObjectDeclaration(
            objectName = objectName,
            itemsType = itemType,
            parsedBlock = parseObjectDeclarationSlice(objectName = objectName, itemType = itemType),
            model = model
        )
    }
    return null
}