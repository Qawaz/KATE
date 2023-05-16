package com.wakaztahir.kate.parser.function

import com.wakaztahir.kate.dsl.ScopedModelObject
import com.wakaztahir.kate.model.ModelProvider
import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.parser.ParsedBlock
import com.wakaztahir.kate.parser.stream.DestinationStream

open class MultiInvocationBlock(
    val parentProvider: ModelProvider,
    val provider: ModelProvider.LateInit,
    codeGens: List<CodeGenRange>
) : ParsedBlock(codeGens) {
    var invocationNumber = 0
    private val previousModels by lazy { mutableListOf<MutableKATEObject>() }
    fun startInvocation() {
        invocationNumber++
//        print("Preparing Function $invocationNumber ")
        if (invocationNumber > 1) previousModels.add(provider.model)
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