package com.wakaztahir.kte.parser

import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.model.AtDirective
import com.wakaztahir.kte.model.DeclarationStatement
import com.wakaztahir.kte.model.KTEValue
import com.wakaztahir.kte.model.LazyBlock
import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.SourceStream

fun TemplateContext.generateTo(destination: DestinationStream) {
    stream.generateTo(stream, destination)
}

fun LazyBlock.generateTo(source: SourceStream, destination: DestinationStream) {
    while (canIterate(source)) {
        if (source.currentChar == '<') {
            if (source.parseComment()) {
                continue
            }
        }
        if (source.currentChar == '@') {
            val directive = source.parseAtDirective()
            if (directive != null) {
                if (directive is DeclarationStatement) {
                    directive.storeValue(model)
                }
                directive.generateTo(this, source, destination)
                continue
            }
        }
        destination.write(source.currentChar)
        source.incrementPointer()
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