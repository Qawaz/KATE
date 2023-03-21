package com.wakaztahir.kte.model

import com.wakaztahir.kte.model.model.MutableTemplateModel
import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.SourceStream

class EmbeddingDirective(val path: String) : AtDirective {
    override fun generateTo(model: MutableTemplateModel, source: SourceStream, destination: DestinationStream) {
        TODO("Not yet implemented")
    }

}