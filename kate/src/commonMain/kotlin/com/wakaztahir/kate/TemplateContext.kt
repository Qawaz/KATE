package com.wakaztahir.kate

import com.wakaztahir.kate.dsl.ModelObjectImpl
import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.parser.stream.ParserSourceStream
import com.wakaztahir.kate.parser.stream.TextParserSourceStream

class TemplateContext(stream: ParserSourceStream) {

    constructor(text: String, model: MutableKATEObject = ModelObjectImpl(GlobalModelObjectName)) : this(
        TextParserSourceStream(
            text,
            model = model
        )
    )

    var stream: ParserSourceStream = stream
        private set

    fun updateStream(stream: ParserSourceStream) {
        this.stream = stream
    }

    fun updateStream(text: String) {
        this.stream = TextParserSourceStream(text, model = this.stream.model)
    }

    @OptIn(KATEDelicateFunction::class)
    fun getDestinationAsString(): String {
        return stream.block.getDestinationString()
    }

    fun generateTo(destination: DestinationStream) {
        stream.block.parse().generateTo(destination)
    }

}