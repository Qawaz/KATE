package com.wakaztahir.kate.model

import com.wakaztahir.kate.parser.block.ParsedBlock
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.tokenizer.NodeTokenizer

class DefaultNoRawBlock(override val parsedBlock: ParsedBlock) : BlockContainer {
    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.defaultNoRawBlock
    override fun generateTo(destination: DestinationStream) {
        parsedBlock.generateTo(destination)
    }
}
