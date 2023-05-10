package com.wakaztahir.kate.model

import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.tokenizer.NodeTokenizer

interface CodeGen {

    val isEmptyWriter get() = false

    fun <T> selectNode(tokenizer: NodeTokenizer<T>) : T

    fun generateTo(model: MutableKATEObject, destination: DestinationStream)

}