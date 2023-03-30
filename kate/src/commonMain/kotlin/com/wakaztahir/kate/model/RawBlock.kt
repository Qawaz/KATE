package com.wakaztahir.kate.model

import com.wakaztahir.kate.model.model.KTEObject
import com.wakaztahir.kate.model.model.MutableKTEObject
import com.wakaztahir.kate.parser.parseDefaultNoRaw
import com.wakaztahir.kate.parser.parseVariableReference
import com.wakaztahir.kate.parser.stream.DestinationStream
import kotlin.jvm.JvmInline

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

open class PartialRawLazyBlockSlice(
    parentBlock: LazyBlock,
    startPointer: Int,
    length: Int,
    blockEndPointer: Int,
    model: MutableKTEObject,
    indentationLevel: Int
) : LazyBlockSlice(
    parentBlock = parentBlock,
    startPointer = startPointer,
    length = length,
    blockEndPointer = blockEndPointer,
    model = model,
    isWriteUnprocessedTextEnabled = false,
    indentationLevel = indentationLevel
) {

    override fun parseImplicitDirectives(): CodeGen? {
        source.parseVariableReference()?.let {
            throw IllegalStateException("variable reference $it cannot be used inside @partial_raw")
        }
        return null
    }

    override fun parseNestedAtDirective(block: LazyBlock): CodeGen? {
        block.parseDefaultNoRaw()?.let { return it }
        return super.parseNestedAtDirective(block)
    }

}

@JvmInline
value class PartialRawBlock(val value: PartialRawLazyBlockSlice) : BlockContainer {
    override fun getBlockValue(model: KTEObject): LazyBlock = value
    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        value.generateTo(destination)
    }
}