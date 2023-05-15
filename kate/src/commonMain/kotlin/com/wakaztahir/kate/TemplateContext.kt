package com.wakaztahir.kate

import com.wakaztahir.kate.dsl.ModelObjectImpl
import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.parser.stream.SourceStream
import com.wakaztahir.kate.parser.stream.TextSourceStream

class TemplateContext(stream: SourceStream) {

    constructor(text: String, model: MutableKATEObject = ModelObjectImpl(GlobalModelObjectName)) : this(
        TextSourceStream(
            text,
            model
        )
    )

    var stream: SourceStream = stream
        private set

    fun updateStream(stream: SourceStream) {
        this.stream = stream
    }

    fun updateStream(text: String) {
        this.stream = TextSourceStream(text, this.stream.model)
    }

    @OptIn(KATEDelicateFunction::class)
    fun getDestinationAsString(): String {
        return stream.block.getDestinationString()
    }

    fun generateTo(destination: DestinationStream) {
        stream.block.parse().generateTo(destination)
    }

}