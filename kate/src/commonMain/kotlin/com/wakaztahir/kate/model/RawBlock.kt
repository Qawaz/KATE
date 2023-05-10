package com.wakaztahir.kate.model

import com.wakaztahir.kate.model.model.KATEObject
import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.parser.parseDefaultNoRaw
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.tokenizer.NodeTokenizer
import kotlin.jvm.JvmInline

class DefaultNoRawBlock(val value: LazyBlockSlice) : BlockContainer {
    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.defaultNoRawBlock
    override fun getBlockValue(model: KATEObject): LazyBlock = value
    override fun generateTo(model: MutableKATEObject, destination: DestinationStream) {
        value.generateTo(destination)
    }
}

class RawBlock(val value: String) : CodeGen {
    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.rawBlock
    override fun generateTo(model: MutableKATEObject, destination: DestinationStream) {
        destination.stream.write(value)
    }
}

open class PartialRawLazyBlockSlice(
    parentBlock: LazyBlock,
    startPointer: Int,
    length: Int,
    blockEndPointer: Int,
    model: MutableKATEObject,
    indentationLevel: Int
) : LazyBlockSlice(
    parentBlock = parentBlock,
    startPointer = startPointer,
    length = length,
    blockEndPointer = blockEndPointer,
    model = model,
    isDefaultNoRaw = false,
    indentationLevel = indentationLevel
) {

    override fun parseNestedAtDirective(block: LazyBlock): CodeGen? {
        block.parseDefaultNoRaw()?.let { return it }
        return super.parseNestedAtDirective(block)
    }

}

@JvmInline
value class PartialRawBlock(val value: PartialRawLazyBlockSlice) : BlockContainer {
    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.partialRawBlock
    override fun getBlockValue(model: KATEObject): LazyBlock = value
    override fun generateTo(model: MutableKATEObject, destination: DestinationStream) {
        value.generateTo(destination)
    }
}