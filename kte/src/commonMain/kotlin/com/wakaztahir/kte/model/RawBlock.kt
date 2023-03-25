package com.wakaztahir.kte.model

import com.wakaztahir.kte.parser.stream.DestinationStream

class RawBlock(val value: String) : AtDirective {
    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        destination.stream.write(value)
    }
}