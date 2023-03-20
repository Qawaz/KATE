package com.wakaztahir.kte.model

import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.SourceStream

interface CodeGen {

    fun generateTo(block: LazyBlock,source : SourceStream, destination: DestinationStream)

}