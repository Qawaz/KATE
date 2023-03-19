package com.wakaztahir.kte.parser

import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.model.AtDirective
import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.SourceStream

fun TemplateContext.generateTo(destination: DestinationStream) {
    while (!stream.hasEnded) {
        if (stream.currentChar == '<') {
            if (stream.parseComment()) {
                continue
            }
        }
        if (stream.currentChar == '@') {
            val directive = stream.parseAtDirective()
            if (directive != null) {
                directive.generateTo(this, destination)
                continue
            }
        }
        destination.write(stream.currentChar)
        stream.incrementPointer()
    }
}

fun SourceStream.parseAtDirective(): AtDirective? {
    parseEmbedding()?.let { return it }
    parseConstantReference()?.let { return it }
    parseConstantDeclaration()?.let { return it }
    parseConstantReference()?.let { return it }
    parseModelDirective()?.let { return it }
    parseIfStatement()?.let { return it }
    parseForLoop()?.let { return it }
    parseRawBlock()?.let { return it }
    return null
}