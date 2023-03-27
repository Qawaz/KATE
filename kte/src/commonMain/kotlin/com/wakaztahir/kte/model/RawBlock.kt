package com.wakaztahir.kte.model

import com.wakaztahir.kte.model.model.KTEObject
import com.wakaztahir.kte.model.model.MutableKTEObject
import com.wakaztahir.kte.parser.parseDefaultNoRaw
import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.SourceStream
import com.wakaztahir.kte.parser.stream.increment

class DefaultNoRawBlock(val value: LazyBlockSlice) : BlockContainer {
    override fun getBlockValue(model: KTEObject): LazyBlock = value
    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        value.generateTo(destination)
    }
}

class RawBlock(val value: LazyBlockSlice) : BlockContainer {
    override fun getBlockValue(model: KTEObject): LazyBlock = value
    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        value.writeValueTo(destination)
    }
}

class PartialRawLazyBlockSlice(
    parentBlock : LazyBlock,
    startPointer: Int,
    length: Int,
    blockEndPointer: Int,
    model: MutableKTEObject,
) : LazyBlockSlice(
    parentBlock = parentBlock,
    startPointer = startPointer,
    length = length,
    blockEndPointer = blockEndPointer,
    model = model,
    allowTextOut = false
) {

    override fun parseNestedAtDirective(block: LazyBlock): CodeGen? {
        block.parseDefaultNoRaw()?.let { return it }
        return super.parseNestedAtDirective(block)
    }

}

class PartialRawBlock(val value: PartialRawLazyBlockSlice) : BlockContainer {
    override fun getBlockValue(model: KTEObject): LazyBlock = value
    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        value.generateTo(destination)
    }
}