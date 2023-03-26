package com.wakaztahir.kte.model

import com.wakaztahir.kte.model.model.KTEObject
import com.wakaztahir.kte.model.model.MutableKTEObject
import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.SourceStream
import com.wakaztahir.kte.parser.stream.increment

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

}