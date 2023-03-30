package com.wakaztahir.kate.model

import com.wakaztahir.kate.parser.stream.DestinationStream

interface CodeGen {

    val isEmptyWriter get() = false

    fun generateTo(block: LazyBlock, destination: DestinationStream)

}