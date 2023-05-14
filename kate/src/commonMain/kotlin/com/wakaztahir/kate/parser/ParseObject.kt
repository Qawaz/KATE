package com.wakaztahir.kate.parser

import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.parser.stream.SourceStream
import com.wakaztahir.kate.parser.stream.increment
import com.wakaztahir.kate.parser.stream.parseTextWhile
import com.wakaztahir.kate.parser.variable.parseKATEType

private fun Char.isObjectName(): Boolean = isLetterOrDigit() || this == '_'

private fun SourceStream.parseObjectName(): String {
    if (increment('(')) {
        val text = parseTextWhile { currentChar.isObjectName() }
        if (increment(')')) {
            return text
        } else {
            throw IllegalStateException("expected ')' while declaring object $text got $currentChar")
        }
    } else {
        throw IllegalStateException("expected ')' while declaring object got $currentChar")
    }
}

class ObjectDeclarationParsedBlock(val model : ObjectDeclarationModel,codeGens: List<CodeGenRange>) : ParsedBlock(codeGens) {
    override fun generateTo(destination: DestinationStream) {
        for (range in codeGens) {
            range.gen.generateTo(destination = destination)
        }
    }
}

private fun LazyBlock.parseObjectDeclarationSlice(objectName: String, itemType: KATEType?): ObjectDeclarationParsedBlock {
    val slice = parseBlockSlice(
        startsWith = "@define_object",
        endsWith = "@end_define_object",
        isDefaultNoRaw = false,
        inheritModel = true
    )

    val current = ObjectDeclarationModel(objectName = objectName, parent = model)

    val block = ObjectDeclarationBlockSlice(
        parentBlock = this,
        startPointer = slice.startPointer,
        length = slice.length,
        blockEndPointer = slice.blockEndPointer,
        model = current,
        indentationLevel = indentationLevel + 1
    ).parse()

    return ObjectDeclarationParsedBlock(model = current,codeGens = block.codeGens)

}

fun LazyBlock.parseObjectDeclaration(): ObjectDeclaration? {
    if (source.currentChar == '@' && source.increment("@define_object")) {
        val itemType: KATEType? = if (source.increment('<')) {
            val type = source.parseKATEType(parseMetadata = false)
            if (!source.increment('>')) {
                throw IllegalStateException("expected '>' after type declaration got '${source.currentChar}'")
            }
            type
        } else {
            null
        }
        val objectName = source.parseObjectName()
        return ObjectDeclaration(
            objectName = objectName,
            itemsType = itemType,
            declarationBlock = parseObjectDeclarationSlice(objectName = objectName, itemType = itemType),
            model = model
        )
    }
    return null
}