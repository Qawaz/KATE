package com.wakaztahir.kate.model

import com.wakaztahir.kate.dsl.ScopedModelObject
import com.wakaztahir.kate.model.model.KATEObject
import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.model.model.ReferencedOrDirectValue
import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.parser.parsePartialRawImplicitDirective
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.parser.stream.TextSourceStream

open class PlaceholderBlock(
    parentBlock: LazyBlock,
    val placeholderName: String,
    val definitionName: String,
    val parameterName: String?,
    startPointer: Int,
    length: Int,
    blockEndPointer: Int,
    override var model: MutableKATEObject,
    allowTextOut: Boolean,
    indentationLevel: Int
) : LazyBlockSlice(
    parentBlock = parentBlock,
    startPointer = startPointer,
    length = length,
    blockEndPointer = blockEndPointer,
    model = model,
    isWriteUnprocessedTextEnabled = allowTextOut,
    indentationLevel = indentationLevel
) {

    private var isInvocationModelSet = false
    private var paramValue: KATEValue? = null

    fun setParamValue(value: KATEValue?) {
        this.paramValue = value
    }

    fun setInvocationModel(model: MutableKATEObject) {
        this.model = ScopedModelObject(model)
        isInvocationModelSet = true
    }

    protected open fun generateActual(destination: DestinationStream) {
        super.generateTo(destination)
    }

    override fun parseImplicitDirectives(): CodeGen? {
        if (isWriteUnprocessedTextEnabled) {
            super.parseImplicitDirectives()?.let { return it }
        } else {
            parsePartialRawImplicitDirective()?.let { return it }
        }
        return null
    }

    override fun generateTo(destination: DestinationStream) {
        val paramName = parameterName ?: "__param__"
        require(isInvocationModelSet) {
            "invocation model should be set before invoking placeholder($placeholderName,$definitionName,$paramName)"
        }
        if (paramValue != null) {
            require(model.insertValue(paramName, paramValue!!)) {
                "couldn't insert value by the name $paramName and $paramValue for placeholder invocation placeholder($placeholderName,$definitionName,$paramName)"
            }
        }
        generateActual(destination)
        if (paramValue != null) {
            model.removeKey(paramName)
        }
        isInvocationModelSet = false
    }

}

class TextPlaceholderBlock(
    val text: String,
    parent: LazyBlock,
    placeholderName: String,
    definitionName: String,
    parameterName: String?,
) : PlaceholderBlock(
    parentBlock = parent,
    placeholderName = placeholderName,
    definitionName = definitionName,
    startPointer = 0,
    blockEndPointer = 0,
    length = text.length,
    model = parent.model,
    allowTextOut = false,
    indentationLevel = 0,
    parameterName = parameterName
) {
    override fun generateActual(destination: DestinationStream) {
        TextSourceStream(
            sourceCode = text,
            model = model,
            placeholderManager = source.placeholderManager,
            embeddingManager = source.embeddingManager
        ).block.generateTo(destination)
    }
}

class PlaceholderDefinition(val blockValue: PlaceholderBlock) : BlockContainer {
    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        block.source.placeholderManager.definePlaceholder(blockValue)
    }

    override fun getBlockValue(model: KATEObject): LazyBlock = blockValue
}

class PlaceholderInvocation(
    val placeholderName: String,
    val definitionName: String?,
    var paramValue: ReferencedOrDirectValue?,
    val invocationEndPointer: Int
) : CodeGen {
    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        val placeholder = (if (definitionName == null)
            block.source.placeholderManager.getPlaceholder(placeholderName = placeholderName)
        else block.source.placeholderManager.getPlaceholder(
            placeholderName = placeholderName,
            definitionName = definitionName
        )) ?: throw IllegalStateException("placeholder with name $placeholderName not found")
        placeholder.setParamValue(paramValue?.getKATEValue(block.model))
        placeholder.setInvocationModel(block.model)
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