package com.wakaztahir.kate.parser

import com.wakaztahir.kate.model.LazyBlock
import com.wakaztahir.kate.model.ObjectDeclaration
import com.wakaztahir.kate.model.ObjectDeclarationBlockSlice
import com.wakaztahir.kate.model.ObjectDeclarationModel
import com.wakaztahir.kate.parser.stream.SourceStream
import com.wakaztahir.kate.parser.stream.increment
import com.wakaztahir.kate.parser.stream.parseTextWhile

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
        parentBlock = this,
        startPointer = slice.startPointer,
        length = slice.length,
        blockEndPointer = slice.blockEndPointer,
        model = ObjectDeclarationModel(objectName = objectName, parent = model),
        indentationLevel = indentationLevel + 1
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