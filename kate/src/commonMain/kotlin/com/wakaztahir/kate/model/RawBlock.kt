package com.wakaztahir.kate.model

import com.wakaztahir.kate.model.model.KATEObject
import com.wakaztahir.kate.model.model.KATEUnit
import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.parser.parseDefaultNoRaw
import com.wakaztahir.kate.parser.parsePartialRawImplicitDirective
import com.wakaztahir.kate.parser.parseVariableReference
import com.wakaztahir.kate.parser.stream.DestinationStream
import kotlin.jvm.JvmInline

class DefaultNoRawBlock(val value: LazyBlockSlice) : BlockContainer {
    override fun getBlockValue(model: KATEObject): LazyBlock = value
    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        value.generateTo(destination)
    }
}

class RawBlock(val value: LazyBlockSlice) : BlockContainer {
    override fun getBlockValue(model: KATEObject): LazyBlock = value
    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        value.writeValueTo(destination)
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
    isWriteUnprocessedTextEnabled = false,
    indentationLevel = indentationLevel
) {

    override fun parseImplicitDirectives(): CodeGen? {
        parsePartialRawImplicitDirective()?.let { return it }
        return null
    }

    override fun parseNestedAtDirective(block: LazyBlock): CodeGen? {
        block.parseDefaultNoRaw()?.let { return it }
        return super.parseNestedAtDirective(block)
    }

}

@JvmInline
value class PartialRawBlock(val value: PartialRawLazyBlockSlice) : BlockContainer {
    override fun getBlockValue(model: KATEObject): LazyBlock = value
    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        value.generateTo(destination)
    }
}