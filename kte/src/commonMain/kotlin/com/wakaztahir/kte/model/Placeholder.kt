package com.wakaztahir.kte.model

import com.wakaztahir.kte.dsl.ScopedModelObject
import com.wakaztahir.kte.model.model.KTEObject
import com.wakaztahir.kte.model.model.MutableKTEObject
import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.SourceStream

class PlaceholderBlock(
    parentBlock : LazyBlock,
    val placeholderName: String,
    val definitionName: String,
    startPointer: Int,
    length: Int,
    blockEndPointer: Int,
    parent: MutableKTEObject,
    allowTextOut: Boolean
) : LazyBlockSlice(
    parentBlock = parentBlock,
    startPointer = startPointer,
    length = length,
    blockEndPointer = blockEndPointer,
    model = parent,
    allowTextOut = allowTextOut
) {

    private var isGenerationModelSet = false

    override var model: MutableKTEObject = parent
        private set

    fun setGenerationModel(model: MutableKTEObject) {
        this.model = model
        this.isGenerationModelSet = true
    }

    override fun generateTo(destination: DestinationStream) {
        require(isGenerationModelSet) {
            "Generation Model should be set using setGenerationModel before calling generateTo"
        }
        super.generateTo(destination)
        this.isGenerationModelSet = false
    }

}

class PlaceholderDefinition(val blockValue: PlaceholderBlock) : BlockContainer {
    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        block.source.placeholderManager.definePlaceholder(blockValue)
    }

    override fun getBlockValue(model: KTEObject): LazyBlock = blockValue
}

class PlaceholderInvocation(
    val placeholderName: String,
    val generationObject : MutableKTEObject,
    val invocationEndPointer: Int
) : CodeGen {
    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        val placeholder = block.source.placeholderManager.getPlaceholder(placeholderName = placeholderName)
            ?: throw IllegalStateException("placeholder with name $placeholderName not found")
        placeholder.setGenerationModel(generationObject)
        placeholder.generateTo(destination)
        block.source.setPointerAt(invocationEndPointer)
    }
}

class PlaceholderUse(
    private val placeholderName: String,
    private val definitionName: String
) : CodeGen {
    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        if (!block.source.placeholderManager.usePlaceholder(placeholderName, definitionName)) {
            throw IllegalStateException("placeholder with name $placeholderName and definition name $definitionName not found")
        }
    }
}