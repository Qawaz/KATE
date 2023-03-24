package com.wakaztahir.kte.model

import com.wakaztahir.kte.model.model.KTEList
import com.wakaztahir.kte.model.model.KTEObject
import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.LanguageDestination
import com.wakaztahir.kte.parser.stream.SourceStream

interface KTEValue : CodeGen {

    fun writeTo(model: KTEObject, destination: LanguageDestination)

    override fun generateTo(block: LazyBlock, source: SourceStream, destination: DestinationStream) {
        writeTo(block.model, destination)
    }

    fun indentation(indentationLevel: Int): String {
        var indentation = ""
        repeat(indentationLevel) {
            indentation += '\t'
        }
        return indentation
    }

    fun stringValue(indentationLevel: Int): String

    override fun toString(): String

}