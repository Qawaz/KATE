package com.wakaztahir.kte.parser

import com.wakaztahir.kte.model.LazyBlock
import com.wakaztahir.kte.model.ObjectDeclaration
import com.wakaztahir.kte.model.ObjectDeclarationBlockSlice
import com.wakaztahir.kte.model.ObjectDeclarationModel
import com.wakaztahir.kte.parser.stream.SourceStream
import com.wakaztahir.kte.parser.stream.increment
import com.wakaztahir.kte.parser.stream.parseTextWhile

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

private fun LazyBlock.parseObjectDeclarationSlice(objectName: String): ObjectDeclarationBlockSlice {
    val slice = parseBlockSlice(
        startsWith = "@define_object",
        endsWith = "@end_define_object",
        allowTextOut = false,
        inheritModel = true
    )
    return ObjectDeclarationBlockSlice(
        source = slice.source,
        startPointer = slice.startPointer,
        length = slice.length,
        blockEndPointer = slice.blockEndPointer,
        model = ObjectDeclarationModel(objectName = objectName, parent = model)
    )
}

fun LazyBlock.parseObjectDeclaration(): ObjectDeclaration? {
    if (source.currentChar == '@' && source.increment("@define_object")) {
        val objectName = source.parseObjectName()
        return ObjectDeclaration(
            objectName = objectName,
            declarationBlock = parseObjectDeclarationSlice(objectName)
        )
    }
    return null
}