package com.wakaztahir.kte.model

import com.wakaztahir.kte.model.model.MutableTemplateModel
import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.SourceStream

class RawBlock(val value: String) : AtDirective {
    override fun generateTo(model: MutableTemplateModel, source: SourceStream, destination: DestinationStream) {
        destination.write(value)
    }
}