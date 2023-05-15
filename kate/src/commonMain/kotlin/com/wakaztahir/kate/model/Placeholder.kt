package com.wakaztahir.kate.model

import com.wakaztahir.kate.dsl.ScopedModelObject
import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.model.model.ReferencedOrDirectValue
import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.parser.stream.PlaceholderManager
import com.wakaztahir.kate.tokenizer.NodeTokenizer

open class PlaceholderBlock(
    parentBlock: LazyBlock,
    val placeholderName: String,
    val definitionName: String,
    val parameterName: String?,
    startPointer: Int,
    length: Int,
    blockEndPointer: Int,
    override val provider: ModelProvider.Changeable,
    isDefaultNoRaw: Boolean,
    indentationLevel: Int
) : LazyBlockSlice(
    parentBlock = parentBlock,
    startPointer = startPointer,
    length = length,
    blockEndPointer = blockEndPointer,
    provider = provider,
    isDefaultNoRaw = isDefaultNoRaw,
    indentationLevel = indentationLevel
) {

    private var isInvocationModelSet = false
    private var paramValue: KATEValue? = null

    fun setParamValue(value: KATEValue?) {
        this.paramValue = value
    }

    fun setInvocationModel(model: MutableKATEObject) {
        this.provider.model = ScopedModelObject(model)
        isInvocationModelSet = true
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
        super.generateTo(destination)
        if (paramValue != null) {
            model.removeKey(paramName)
        }
        isInvocationModelSet = false
    }

}

class PlaceholderDefinition(val blockValue: PlaceholderBlock, val isOnce: Boolean,val placeholderManager: PlaceholderManager) : BlockContainer {
    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.placeholderDefinition
    override fun generateTo(destination: DestinationStream) {
        placeholderManager.definePlaceholder(placeholder = blockValue, throwIfExists = !isOnce)
    }

    override fun getBlockValue(): LazyBlock = blockValue
}

class PlaceholderInvocation(
    val placeholderName: String,
    val definitionName: String?,
    var paramValue: ReferencedOrDirectValue?,
    val placeholderManager: PlaceholderManager,
    val invocationModel : MutableKATEObject,
) : CodeGen {
    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.placeholderInvocation
    override fun generateTo(destination: DestinationStream) {
        val placeholder = (if (definitionName == null)
            placeholderManager.getPlaceholder(placeholderName = placeholderName)
        else placeholderManager.getPlaceholder(
            placeholderName = placeholderName,
            definitionName = definitionName
        )) ?: throw IllegalStateException("placeholder with name $placeholderName not found")
        placeholder.setParamValue(paramValue?.getKATEValue())
        placeholder.setInvocationModel(invocationModel)
        placeholder.generateTo(destination)
    }
}

class PlaceholderUse(
    private val placeholderName: String,
    private val definitionName: String,
    private val placeholderManager: PlaceholderManager
) : CodeGen {
    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.placeholderUse
    override fun generateTo(destination: DestinationStream) {
        if (!placeholderManager.usePlaceholder(placeholderName, definitionName)) {
            throw IllegalStateException("placeholder with name $placeholderName and definition name $definitionName not found")
        }
    }
}