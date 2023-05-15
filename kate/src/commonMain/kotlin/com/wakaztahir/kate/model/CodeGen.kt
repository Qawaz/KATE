package com.wakaztahir.kate.model

import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.tokenizer.NodeTokenizer

interface CodeGen {

    val isEmptyWriter get() = false

    val expectSpaceOrNewLineWithIndentationAfterwards get() = false

    fun <T> selectNode(tokenizer: NodeTokenizer<T>) : T

    fun generateTo(destination: DestinationStream)

}