package com.wakaztahir.kate.parser.block

import com.wakaztahir.kate.model.CodeGen
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.tokenizer.NodeTokenizer

class DefaultNoRawString(stringValue: String) : CodeGen {

    var stringValue = stringValue
        internal set

    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.defaultNoRawString

    override fun generateTo(destination: DestinationStream) {
        destination.stream.write(stringValue)
    }

}