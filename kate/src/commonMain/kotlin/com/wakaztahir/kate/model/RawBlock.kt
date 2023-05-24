package com.wakaztahir.kate.model

import com.wakaztahir.kate.parser.block.ParsedBlock
import com.wakaztahir.kate.parser.PartialRawParsedBlock
import com.wakaztahir.kate.parser.parseDefaultNoRaw
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.tokenizer.NodeTokenizer
import kotlin.jvm.JvmInline

class DefaultNoRawBlock(override val parsedBlock: ParsedBlock) : BlockContainer {
    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.defaultNoRawBlock
    override fun generateTo(destination: DestinationStream) {
        parsedBlock.generateTo(destination)
    }
}

class RawBlock(val value: String) : CodeGen {
    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.rawBlock
    override fun generateTo(destination: DestinationStream) {
        destination.stream.write(value)
    }
}

class PartialRawLazyBlockSlice(
    parentBlock: LazyBlock,
    startPointer: Int,
    length: Int,
    blockEndPointer: Int,
    provider: ModelProvider,
    indentationLevel: Int
) : LazyBlockSlice(
    parentBlock = parentBlock,
    startPointer = startPointer,
    length = length,
    blockEndPointer = blockEndPointer,
    provider = provider,
    isDefaultNoRaw = false,
    indentationLevel = indentationLevel
) {

    override fun parseNestedAtDirective(block: LazyBlock): CodeGen? {
        block.parseDefaultNoRaw()?.let { return it }
        return super.parseNestedAtDirective(block)
    }

}

@JvmInline
value class PartialRawBlock(override val parsedBlock: PartialRawParsedBlock) : BlockContainer {
    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.partialRawBlock
    override fun generateTo(destination: DestinationStream) {
        parsedBlock.generateTo(destination)
    }
}