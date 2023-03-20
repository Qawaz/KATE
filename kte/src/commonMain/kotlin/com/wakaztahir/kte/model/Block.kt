package com.wakaztahir.kte.model

import com.wakaztahir.kte.dsl.ModelObjectImpl
import com.wakaztahir.kte.dsl.ScopedModelObject
import com.wakaztahir.kte.model.model.MutableTemplateModel
import com.wakaztahir.kte.parser.stream.SourceStream

interface LazyBlock {

    val model: MutableTemplateModel

    fun canIterate(stream: SourceStream): Boolean

}


class LazyBlockSlice(
    val startPointer: Int,
    val length: Int,
    parent: MutableTemplateModel
) : LazyBlock {

    override val model: MutableTemplateModel = ScopedModelObject(parent = parent, ModelObjectImpl())

    override fun canIterate(stream: SourceStream): Boolean {
        return stream.pointer < startPointer + length
    }

    fun getValueAsString(stream: SourceStream): String {
        val previous = stream.pointer
        stream.setPointerAt(startPointer)
        var text = ""
        while (stream.pointer < startPointer + length) {
            text += stream.currentChar
            stream.incrementPointer()
        }
        stream.setPointerAt(previous)
        return text
    }
}