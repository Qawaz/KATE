package com.wakaztahir.kte.model

import com.wakaztahir.kte.KTEDelicateFunction
import com.wakaztahir.kte.dsl.ModelObjectImpl
import com.wakaztahir.kte.dsl.ScopedModelObject
import com.wakaztahir.kte.model.model.MutableTemplateModel
import com.wakaztahir.kte.model.model.TemplateModel
import com.wakaztahir.kte.parser.generateTo
import com.wakaztahir.kte.parser.parseAtDirective
import com.wakaztahir.kte.parser.parseComment
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
                val directive = source.parseAtDirective()
                if (directive != null) {
                    directive.generateTo(this, source, destination)
                    continue
                }
            }
            destination.write(source.currentChar)
            source.incrementPointer()
        }
    }

    @KTEDelicateFunction
    fun getDestinationAsString(source: SourceStream): String {
        val previous = source.pointer
        val destination = TextDestinationStream()
        generateTo(source, destination)
        source.setPointerAt(previous)
        return destination.getValue()
    }

}


class LazyBlockSlice(
    val startPointer: Int,
    val length: Int,
    parent: MutableTemplateModel
) : LazyBlock {

    override val model: MutableTemplateModel = ScopedModelObject(parent = parent)

    override fun canIterate(stream: SourceStream): Boolean {
        return stream.pointer < startPointer + length
    }

    override fun generateTo(source: SourceStream, destination: DestinationStream) {
        val previous = source.pointer
        source.setPointerAt(startPointer)
        super.generateTo(source, destination)
        source.setPointerAt(previous)
    }

    fun getValueAsString(source: SourceStream): String {
        val previous = source.pointer
        source.setPointerAt(startPointer)
        var text = ""
        while (source.pointer < startPointer + length) {
            text += source.currentChar
            source.incrementPointer()
        }
        source.setPointerAt(previous)
        return text

    }
}