package com.wakaztahir.kate.parser.function

import com.wakaztahir.kate.dsl.ScopedModelObject
import com.wakaztahir.kate.model.ModelProvider
import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.parser.ParsedBlock
import com.wakaztahir.kate.parser.stream.DestinationStream

abstract class NestableInvocationBlock(
    val parentProvider: ModelProvider,
    val provider: ModelProvider.Changeable,
    codeGens: List<CodeGenRange>
) : ParsedBlock(codeGens) {

    /**
     * this counts the total invocations , every invocation counts as an increase
     */
    private var totalInvocations = 0

    /**
     * this variable keeps track of nestable invocations
     * when first invocation starts , it increments to 1
     * when first invocation ends , it decrements back to 0
     *
     * say if the first invocation contains another nested invocation
     * with parent invocation , it will increment to 1
     * when the nested invocation starts , it will increment to 2
     * and then the nested invocation will end and decrement back to 1
     * and then the parent invocation will end decrementing back to 0
     *
     * works like a stack counter , where increment is like a push and decrement like a pop
     * where push indicates start of invocation and pop end of invocation
     */
    var invocationNumber = 0
        private set
    private val previousModels by lazy { mutableListOf<MutableKATEObject>() }
    fun startInvocation() {
        invocationNumber++
        totalInvocations++
        if (invocationNumber > 1) previousModels.add(provider.model)
        if (totalInvocations > 1) provider.model = ScopedModelObject(parentProvider.model)
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