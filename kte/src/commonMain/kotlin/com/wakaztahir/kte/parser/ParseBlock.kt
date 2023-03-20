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