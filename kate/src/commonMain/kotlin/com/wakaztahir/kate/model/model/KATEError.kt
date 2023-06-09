package com.wakaztahir.kate.model.model

import com.wakaztahir.kate.model.CodeGen
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.tokenizer.NodeTokenizer

class KATEParsingError(val throwable: Throwable) : CodeGen,ReferencedOrDirectValue {

    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.kateParsingError

    override fun generateTo(destination: DestinationStream) {
        throw throwable
    }

    override fun getKATEValue(): KATEValue {
        throw throwable
    }
}