package com.wakaztahir.kte.model

import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.SourceStream

class RawBlock(val value: String) : AtDirective {
    override fun generateTo(block: LazyBlock, source: SourceStream, destination: DestinationStream) {
        destination.stream.write(value)
    }
}