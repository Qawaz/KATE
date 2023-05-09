package com.wakaztahir.kate.parser

import com.wakaztahir.kate.model.*
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

private fun LazyBlock.parseObjectDeclarationSlice(objectName: String, itemType: KATEType?): ObjectDeclarationBlockSlice {
    val slice = parseBlockSlice(
        startsWith = "@define_object",
        endsWith = "@end_define_object",
        isDefaultNoRaw = false,
        inheritModel = true
    )
    return ObjectDeclarationBlockSlice(
        parentBlock = this,
        startPointer = slice.startPointer,
        length = slice.length,
        blockEndPointer = slice.blockEndPointer,
        model = ObjectDeclarationModel(objectName = objectName, parent = model),
        indentationLevel = indentationLevel + 1
    ).also { it.prepare() }
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
            declarationBlock = parseObjectDeclarationSlice(objectName = objectName, itemType = itemType)
        )
    }
    return null
}