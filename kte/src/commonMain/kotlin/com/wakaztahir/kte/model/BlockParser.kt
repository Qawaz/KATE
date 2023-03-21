package com.wakaztahir.kte.model

import com.wakaztahir.kte.KTEDelicateFunction
import com.wakaztahir.kte.model.model.MutableTemplateModel
import com.wakaztahir.kte.parser.*
import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.SourceStream
import com.wakaztahir.kte.parser.stream.TextDestinationStream

interface BlockParser {

    val model: MutableTemplateModel

    fun hasNext(stream: SourceStream): Boolean

    fun generateTo(source: SourceStream, destination: DestinationStream) {
        while (hasNext(source)) {
            if (source.currentChar == '<') {
                if (source.parseComment()) {
                    continue
                }
            }
            if (source.currentChar == '@') {
                val directive = parseAtDirective(source)
                if (directive != null) {
                    directive.generateTo(model, source, destination)
                    continue
                }
            }
            destination.write(source.currentChar)
            source.incrementPointer()
        }
    }

    fun parseAtDirective(source: SourceStream): AtDirective? = with(source) {
        parseEmbedding()?.let { return it }
        parseConstantReference()?.let { return it }
        parseConstantDeclaration()?.let { return it }
        parseConstantReference()?.let { return it }
        parseModelDirective()?.let { return it }
        parseIfStatement(source)?.let { return it }
        parseForLoop(source)?.let { return it }
        parseRawBlock()?.let { return it }
        return null
    }

    @KTEDelicateFunction
    fun getDestinationString(source: SourceStream): String {
        val destination = TextDestinationStream()
        generateTo(source, destination)
        return destination.getValue()
    }

    fun getValueAsString(source: SourceStream): String {
        val previous = source.pointer
        var text = ""
        while (hasNext(source)) {
            text += source.currentChar
            source.incrementPointer()
        }
        source.setPointerAt(previous)
        return text
    }

}