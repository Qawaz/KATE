package com.wakaztahir.kte.model

import com.wakaztahir.kte.parser.stream.DestinationStream

interface CodeGen {

    fun generateTo(block: LazyBlock,destination: DestinationStream)

}