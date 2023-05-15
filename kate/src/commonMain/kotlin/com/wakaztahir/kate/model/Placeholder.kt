package com.wakaztahir.kate.model

import com.wakaztahir.kate.dsl.ScopedModelObject
import com.wakaztahir.kate.model.model.ReferencedOrDirectValue
import com.wakaztahir.kate.parser.ParsedBlock
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.parser.stream.PlaceholderManager
import com.wakaztahir.kate.tokenizer.NodeTokenizer

class PlaceholderParsedBlock(
    val provider: ModelProvider.LateInit,
    val placeholderName: String,
    val definitionName: String,
    val parameterName: String,
    codeGens: List<CodeGenRange>
) : ParsedBlock(codeGens)

class PlaceholderDefinition(
    override val parsedBlock: PlaceholderParsedBlock,
    val isOnce: Boolean,
    val placeholderManager: PlaceholderManager
) : BlockContainer {
    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.placeholderDefinition
    override fun generateTo(destination: DestinationStream) {
        placeholderManager.definePlaceholder(placeholder = parsedBlock, throwIfExists = !isOnce)
    }
}

class PlaceholderInvocation(
    val placeholderName: String,
    val definitionName: String?,
    var paramValue: ReferencedOrDirectValue?,
    val placeholderManager: PlaceholderManager,
    val invocationProvider: ModelProvider,
) : CodeGen {
    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.placeholderInvocation
    override fun generateTo(destination: DestinationStream) {
        val invocationModel = invocationProvider.model
        val placeholder = (if (definitionName == null)
            placeholderManager.getPlaceholder(placeholderName = placeholderName)
        else placeholderManager.getPlaceholder(
            placeholderName = placeholderName,
            definitionName = definitionName
        )) ?: throw IllegalStateException("placeholder with name $placeholderName not found")
        placeholder.provider.model = ScopedModelObject(invocationModel)
        paramValue?.getKATEValue()?.let {
            require(invocationModel.insertValue(placeholder.parameterName, it)) {
                "couldn't insert value by the name ${placeholder.parameterName} and $paramValue for placeholder invocation placeholder($placeholderName,$definitionName,${placeholder.parameterName})"
            }
        }
        placeholder.generateTo(destination)
        if (paramValue != null) {
            invocationModel.removeKey(placeholder.parameterName)
        }
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