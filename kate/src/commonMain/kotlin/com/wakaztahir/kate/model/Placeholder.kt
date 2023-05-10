package com.wakaztahir.kate.model

import com.wakaztahir.kate.dsl.ScopedModelObject
import com.wakaztahir.kate.model.model.KATEObject
import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.model.model.ReferencedOrDirectValue
import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.parser.parsePartialRawImplicitDirective
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.parser.stream.PlaceholderManager
import com.wakaztahir.kate.parser.stream.SourceStream
import com.wakaztahir.kate.parser.stream.TextSourceStream
import com.wakaztahir.kate.tokenizer.NodeTokenizer

open class PlaceholderBlock(
    parentBlock: LazyBlock,
    val placeholderName: String,
    val definitionName: String,
    val parameterName: String?,
    startPointer: Int,
    length: Int,
    blockEndPointer: Int,
    override var model: MutableKATEObject,
    isDefaultNoRaw: Boolean,
    indentationLevel: Int
) : LazyBlockSlice(
    parentBlock = parentBlock,
    startPointer = startPointer,
    length = length,
    blockEndPointer = blockEndPointer,
    model = model,
    isDefaultNoRaw = isDefaultNoRaw,
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
        if (isDefaultNoRaw) {
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
    isDefaultNoRaw = false,
    indentationLevel = 0,
    parameterName = parameterName
) {
    override fun generateActual(destination: DestinationStream) {
        TextSourceStream(
            sourceCode = text,
            model = model,
            placeholderManager = source.placeholderManager,
            embeddingManager = source.embeddingManager,
            initialize = false
        ).block.generateTo(destination)
    }
}

class PlaceholderDefinition(val blockValue: PlaceholderBlock, val isOnce: Boolean,val placeholderManager: PlaceholderManager) : BlockContainer {
    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.placeholderDefinition
    override fun generateTo(model: MutableKATEObject, destination: DestinationStream) {
        placeholderManager.definePlaceholder(placeholder = blockValue, throwIfExists = !isOnce)
    }

    override fun getBlockValue(model: KATEObject): LazyBlock = blockValue
}

class PlaceholderInvocation(
    val placeholderName: String,
    val definitionName: String?,
    var paramValue: ReferencedOrDirectValue?,
    val placeholderManager: PlaceholderManager
) : CodeGen {
    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.placeholderInvocation
    override fun generateTo(model: MutableKATEObject, destination: DestinationStream) {
        val placeholder = (if (definitionName == null)
            placeholderManager.getPlaceholder(placeholderName = placeholderName)
        else placeholderManager.getPlaceholder(
            placeholderName = placeholderName,
            definitionName = definitionName
        )) ?: throw IllegalStateException("placeholder with name $placeholderName not found")
        placeholder.setParamValue(paramValue?.getKATEValue(model))
        placeholder.setInvocationModel(model)
        placeholder.generateTo(destination)
    }
}

class PlaceholderUse(
    private val placeholderName: String,
    private val definitionName: String,
    private val placeholderManager: PlaceholderManager
) : CodeGen {
    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.placeholderUse
    override fun generateTo(model: MutableKATEObject, destination: DestinationStream) {
        if (!placeholderManager.usePlaceholder(placeholderName, definitionName)) {
            throw IllegalStateException("placeholder with name $placeholderName and definition name $definitionName not found")
        }
    }
}