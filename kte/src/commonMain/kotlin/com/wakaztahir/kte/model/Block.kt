package com.wakaztahir.kte.model

import com.wakaztahir.kte.KTEDelicateFunction
import com.wakaztahir.kte.dsl.ScopedModelObject
import com.wakaztahir.kte.model.model.MutableTemplateModel
import com.wakaztahir.kte.parser.*
import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.SourceStream
import com.wakaztahir.kte.parser.stream.TextDestinationStream

interface LazyBlock {

    val model: MutableTemplateModel

    fun canIterate(stream: SourceStream): Boolean

    fun generateTo(source: SourceStream, destination: DestinationStream) {
        while (canIterate(source)) {
            if (source.currentChar == '<') {
                if (source.parseComment()) {
                    continue
                }
            }
            if (source.currentChar == '@') {
                val directive = parseAtDirective(source)
                if (directive != null) {
                    directive.generateTo(this, source, destination)
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
        parseIfStatement()?.let { return it }
        parseForLoop()?.let { return it }
        parseRawBlock()?.let { return it }
        return null
    }

    @KTEDelicateFunction
    fun getDestinationString(source: SourceStream): String {
        val destination = TextDestinationStream()
        generateTo(source, destination)
        return destination.getValue()
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
    parent: MutableTemplateModel
) : LazyBlock {

    override val model: MutableTemplateModel = ScopedModelObject(parent = parent)

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