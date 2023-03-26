package com.wakaztahir.kte.model

import com.wakaztahir.kte.model.model.KTEObject
import com.wakaztahir.kte.model.model.MutableKTEObject
import com.wakaztahir.kte.parser.parseDefaultNoRaw
import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.SourceStream
import com.wakaztahir.kte.parser.stream.increment

class DefaultNoRawBlock(val value: LazyBlockSlice) : AtDirective, BlockContainer {
    override fun getBlockValue(model: KTEObject): LazyBlock = value
    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        value.generateTo(destination)
    }
}

class RawBlock(val value: LazyBlockSlice) : AtDirective, BlockContainer {
    override fun getBlockValue(model: KTEObject): LazyBlock = value
    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        value.writeValueTo(destination)
    }
}

class PartialRawLazyBlockSlice(
    source: SourceStream,
    startPointer: Int,
    length: Int,
    blockEndPointer: Int,
    model: MutableKTEObject,
) : LazyBlockSlice(
    source = source,
    startPointer = startPointer,
    length = length,
    blockEndPointer = blockEndPointer,
    model = model,
    allowTextOut = false
) {
    override fun parseAtDirective(): CodeGen? {
        parseDefaultNoRaw()?.let { return it }
        super.parseAtDirective()?.let { return it }
        return null
    }
}

class PartialRawBlock(val value: PartialRawLazyBlockSlice) : AtDirective {
    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        value.generateTo(destination)
    }
}