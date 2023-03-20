package com.wakaztahir.kte.model

import com.wakaztahir.kte.dsl.ModelProvider
import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.SourceStream

data class ConstantReference(val name: String) : ReferencedValue, AtDirective {
    override fun getValue(model: ModelProvider): DynamicValue<*> {
        return model.getConstantReference(this)
    }

    override fun generateTo(block: LazyBlock, source: SourceStream, destination: DestinationStream) {
        getValue(block.model).generateTo(block, source, destination)
    }
}