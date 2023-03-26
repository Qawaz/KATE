package com.wakaztahir.kte.model

import com.wakaztahir.kte.parser.stream.DestinationStream

interface CodeGen {

    val isEmptyWriter get() = false

    fun generateTo(block: LazyBlock, destination: DestinationStream)

}