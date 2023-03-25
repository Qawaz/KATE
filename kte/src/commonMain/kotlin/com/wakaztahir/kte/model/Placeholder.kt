package com.wakaztahir.kte.model

import com.wakaztahir.kte.dsl.ScopedModelObject
import com.wakaztahir.kte.model.model.KTEObject
import com.wakaztahir.kte.model.model.MutableKTEObject
import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.SourceStream

class PlaceholderBlock(
    val placeholderName: String,
    val definitionName: String,
    startPointer: Int,
    length: Int,
    blockEndPointer: Int,
    parent: MutableKTEObject
) : LazyBlockSlice(
    startPointer = startPointer,
    length = length,
    blockEndPointer = blockEndPointer,
    parent = parent
) {

    private var isGenerationModelSet = false

    override var model: MutableKTEObject = parent
        private set

    fun setGenerationModel(parent: MutableKTEObject) {
        this.model = ScopedModelObject(parent)
        this.isGenerationModelSet = true
    }

    override fun generateTo(source: SourceStream, destination: DestinationStream) {
        require(isGenerationModelSet) {
            "Generation Model should be set using setGenerationModel before calling generateTo"
        }
        super.generateTo(source, destination)
        this.isGenerationModelSet = false
    }

}

class PlaceholderDefinition(val blockValue: PlaceholderBlock) : BlockContainer {
    override fun generateTo(block: LazyBlock, source: SourceStream, destination: DestinationStream) {
        source.definePlaceholder(blockValue)
    }
    override fun getBlockValue(model: KTEObject): LazyBlock = blockValue
}

class PlaceholderInvocation(
    val placeholderName: String,
    val invocationEndPointer: Int
) : CodeGen {
    override fun generateTo(block: LazyBlock, source: SourceStream, destination: DestinationStream) {
        val placeholder = source.getPlaceholder(placeholderName = placeholderName)
            ?: throw IllegalStateException("placeholder with name $placeholderName not found")
        placeholder.setGenerationModel(block.model)
        placeholder.generateTo(source, destination)
        source.setPointerAt(invocationEndPointer)
    }
}

class PlaceholderUse(
    private val placeholderName: String,
    private val definitionName: String
) : CodeGen {
    override fun generateTo(block: LazyBlock, source: SourceStream, destination: DestinationStream) {
        if (!source.usePlaceholder(placeholderName, definitionName)) {
            throw IllegalStateException("placeholder with name $placeholderName and definition name $definitionName not found")
        }
    }
}