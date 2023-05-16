package com.wakaztahir.kate.parser.function

import com.wakaztahir.kate.dsl.ScopedModelObject
import com.wakaztahir.kate.model.ModelProvider
import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.parser.ParsedBlock
import com.wakaztahir.kate.parser.stream.DestinationStream

open class NestableInvocationBlock(
    val parentProvider: ModelProvider,
    val provider: ModelProvider.Changeable,
    codeGens: List<CodeGenRange>
) : ParsedBlock(codeGens) {
    var invocationNumber = 0
    private set
    private val previousModels by lazy { mutableListOf<MutableKATEObject>() }
    fun startInvocation() {
        invocationNumber++
        if (invocationNumber > 1) {
            previousModels.add(provider.model)
        }
        provider.model = ScopedModelObject(parentProvider.model)
    }

    fun endInvocation() {
        invocationNumber--
        if (invocationNumber > 0) provider.model = previousModels.removeLast()
    }

    override fun generateTo(destination: DestinationStream) {
        require(invocationNumber > 0)
        super.generateTo(destination)
    }
}