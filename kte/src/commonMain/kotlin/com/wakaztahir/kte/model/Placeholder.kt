package com.wakaztahir.kte.model

import com.wakaztahir.kte.model.model.KTEObject
import com.wakaztahir.kte.model.model.KTEValue
import com.wakaztahir.kte.model.model.MutableKTEObject
import com.wakaztahir.kte.model.model.ReferencedValue
import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.TextSourceStream

open class PlaceholderBlock(
    parentBlock: LazyBlock,
    val placeholderName: String,
    val definitionName: String,
    startPointer: Int,
    length: Int,
    blockEndPointer: Int,
    parent: MutableKTEObject,
    allowTextOut: Boolean,
    indentationLevel: Int
) : LazyBlockSlice(
    parentBlock = parentBlock,
    startPointer = startPointer,
    length = length,
    blockEndPointer = blockEndPointer,
    model = parent,
    isWriteUnprocessedTextEnabled = allowTextOut,
    indentationLevel = indentationLevel
) {

    private var isGenerationModelSet = false

    override var model: MutableKTEObject = parent
        protected set

    private var paramValue: KTEValue? = null

    fun setGenerationModel(model: MutableKTEObject) {
        this.model = model
        this.isGenerationModelSet = true
    }

    fun setParamValue(value: KTEValue) {
        this.paramValue = value
    }

    fun generateTo(model: MutableKTEObject, value: KTEValue?, destination: DestinationStream) {
        setGenerationModel(model)
        if (value != null) setParamValue(value)
        generateTo(destination)
    }

    protected open fun generateActual(destination: DestinationStream) {
        super.generateTo(destination)
    }

    override fun generateTo(destination: DestinationStream) {
        require(isGenerationModelSet) {
            "Generation Model should be set using setGenerationModel before calling generateTo"
        }
        if (paramValue != null) {
            require(!model.contains("__param__")) {
                "when passing @var(this) value to placeholder invocation , defining value with same name \"__param__\" is not allowed"
            }
            (paramValue as? ModelDirective)?.propertyPath?.lastOrNull()?.name?.let {
                model.putValue("__kte_param_name__", it)
            }
            model.putValue("__param__", paramValue!!)
        }
        generateActual(destination)
        if (paramValue != null) {
            model.removeKey("__param__")
            if (paramValue is ModelDirective) {
                (paramValue as? ModelDirective)?.propertyPath?.lastOrNull()?.name?.let {
                    model.removeKey("__kte_param_name__")
                }
            }
        }
        this.isGenerationModelSet = false
    }

}

class TextPlaceholderBlock(
    val text: String,
    parent: LazyBlock,
    placeholderName: String,
    definitionName: String,
) : PlaceholderBlock(
    parentBlock = parent,
    placeholderName = placeholderName,
    definitionName = definitionName,
    startPointer = 0,
    blockEndPointer = 0,
    length = text.length,
    parent = parent.model,
    allowTextOut = false,
    indentationLevel = 0
) {
    override fun generateActual(destination: DestinationStream) {
        TextSourceStream(
            sourceCode = text,
            model = model,
            placeholderManager = source.placeholderManager,
            embeddingManager = source.embeddingManager
        ).generateTo(destination)
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
    val generationObject: MutableKTEObject,
    var paramValue: KTEValue?,
    val invocationEndPointer: Int
) : CodeGen {
    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        val placeholder = block.source.placeholderManager.getPlaceholder(placeholderName = placeholderName)
            ?: throw IllegalStateException("placeholder with name $placeholderName not found")
        placeholder.generateTo(generationObject, paramValue, destination)
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