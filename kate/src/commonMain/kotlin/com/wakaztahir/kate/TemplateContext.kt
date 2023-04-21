package com.wakaztahir.kate

import com.wakaztahir.kate.dsl.ModelObjectImpl
import com.wakaztahir.kate.model.KATEType
import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.parser.stream.SourceStream
import com.wakaztahir.kate.parser.stream.TextSourceStream

class TemplateContext(stream: SourceStream) {

    constructor(text: String, model: MutableKATEObject = ModelObjectImpl(GlobalModelObjectName,itemType = KATEType.Any)) : this(
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

    private val embedMap = hashMapOf<String, SourceStream>()

    fun embedStream(path: String, stream: SourceStream) {
        embedMap[path] = stream
    }

    fun getEmbeddedStream(path: String): SourceStream? {
        return embedMap[path]
    }

    @OptIn(KATEDelicateFunction::class)
    fun getDestinationAsString(): String {
        return stream.getDestinationString()
    }

    fun generateTo(destination: DestinationStream) {
        stream.generateTo(destination)
    }

}