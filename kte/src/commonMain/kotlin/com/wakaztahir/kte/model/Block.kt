package com.wakaztahir.kte.model

import com.wakaztahir.kte.KTEDelicateFunction
import com.wakaztahir.kte.dsl.ScopedModelObject
import com.wakaztahir.kte.model.model.MutableKTEObject
import com.wakaztahir.kte.parser.*
import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.SourceStream
import com.wakaztahir.kte.parser.stream.TextDestinationStream
import com.wakaztahir.kte.parser.stream.increment
import com.wakaztahir.kte.parser.stream.languages.KotlinLanguageDestination

interface LazyBlock {

    val model: MutableKTEObject

    fun canIterate(stream: SourceStream): Boolean

    fun generateTo(source: SourceStream, destination: DestinationStream) {
        while (canIterate(source)) {
            if (source.skipMultilineComments()) {
                continue
            }
            val directive = parseAtDirective(source)
            if (directive != null) {
                directive.generateTo(this, source, destination)
                continue
            }
            destination.stream.write(source.currentChar)
            source.incrementPointer()
        }
    }

    fun parseAtDirective(source: SourceStream): CodeGen? {
        source.parseEmbedding()?.let { return it }
        source.parseExpression()?.let { return it }
        source.parseVariableDeclaration()?.let { return it }
        parseIfStatement(source)?.let { return it }
        parseForLoop(source)?.let { return it }
        source.parseRawBlock()?.let { return it }
        return null
    }

    @KTEDelicateFunction
    fun getDestinationString(source: SourceStream): String {
        val destination = KotlinLanguageDestination(TextDestinationStream())
        generateTo(source, destination)
        return (destination.stream as TextDestinationStream).getValue()
    }

    @KTEDelicateFunction
    fun getDestinationStringWithReset(source: SourceStream): String {
        val previous = source.pointer
        val value = getDestinationString(source)
        source.setPointerAt(previous)
        return value
    }

}


open class LazyBlockSlice(
    val startPointer: Int,
    val length: Int,
    val blockEndPointer: Int,
    parent: MutableKTEObject
) : LazyBlock {

    override val model: MutableKTEObject = ScopedModelObject(parent = parent)

    override fun canIterate(stream: SourceStream): Boolean {
        return stream.pointer < startPointer + length
    }

    override fun generateTo(source: SourceStream, destination: DestinationStream) {
        source.setPointerAt(startPointer)
        super.generateTo(source, destination)
        source.setPointerAt(blockEndPointer)
    }

    fun getValueAsString(source: SourceStream): String {
        val previous = source.pointer
        source.setPointerAt(startPointer)
        var text = ""
        while (canIterate(source)) {
            text += source.currentChar
            source.incrementPointer()
        }
        source.setPointerAt(previous)
        return text

    }
}