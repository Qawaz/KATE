package com.wakaztahir.kate.parser

import com.wakaztahir.kate.dsl.ScopedModelObject
import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.*
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.parser.stream.SourceStream
import com.wakaztahir.kate.parser.stream.increment
import com.wakaztahir.kate.parser.stream.parseTextWhile

class FunctionSlice(
    parentBlock: LazyBlock,
    startPointer: Int,
    length: Int,
    blockEndPointer: Int,
    model: MutableKATEObject,
    indentationLevel: Int
) : PartialRawLazyBlockSlice(
    parentBlock = parentBlock,
    startPointer = startPointer,
    length = length,
    blockEndPointer = blockEndPointer,
    model = model,
    indentationLevel = indentationLevel
) {

    override var model: MutableKATEObject = parentBlock.model
    var keepIterating: () -> Boolean = { true }
    var onReturnValueFound: (ReferencedValue) -> Unit = {}

    override fun canIterate(): Boolean = super.canIterate() && keepIterating()

    constructor(slice: LazyBlockSlice) : this(
        parentBlock = slice.parentBlock,
        startPointer = slice.startPointer,
        length = slice.length,
        blockEndPointer = slice.blockEndPointer,
        model = slice.model,
        indentationLevel = slice.indentationLevel
    )

    override fun parseNestedAtDirective(block: LazyBlock): CodeGen? {
        block.parseFunctionReturn()?.let {
            onReturnValueFound(it)
            return KATEUnit
        }
        return super.parseNestedAtDirective(block)
    }

}

abstract class KATERecursiveFunction(val slice: FunctionSlice, val parameterNames: List<String>?) : KATEFunction() {

    var destination: DestinationStream? = null

    private var invocationNumber = 0
    private var returnedValues = hashMapOf<Int, KATEValue>()
    private val previousModels = mutableListOf<MutableKATEObject>()
    private val previousPointers = hashMapOf<Int, Int>()

    init {
        slice.onReturnValueFound = {
            returnedValues[invocationNumber] = it
        }
        slice.keepIterating = {
            !returnedValues.containsKey(invocationNumber)
        }
    }

    private inline fun KATEObject.forEachParam(block: MutableKATEObject.(String, Int) -> Unit) {
        (this as? MutableKATEObject)?.let {
            if (parameterNames != null) {
                var i = 0
                for (param in parameterNames) {
                    block(it, param, i)
                    i++
                }
            }
        }
    }

    private fun startInvocation(model: KATEObject, parameters: List<ReferencedValue>) {
        invocationNumber++
        if (invocationNumber > 1) {
            previousModels.add(slice.model)
            previousPointers[invocationNumber - 1] = slice.source.pointer
        }
        slice.model = ScopedModelObject(slice.parentBlock.model)
        slice.model.forEachParam { paramName, index ->
            if (index < parameters.size) {
                putValue(paramName, parameters[index].getKTEValue(model))
            } else {
                throw IllegalStateException("function expects ${parameterNames?.size} parameters and not ${parameters.size}")
            }
        }
    }

    private fun endInvocation(): KATEValue {
        val returnedValue = returnedValues.remove(invocationNumber)?.getKTEValue(slice.model) ?: KATEUnit
        invocationNumber--
        if (previousModels.isNotEmpty()) slice.model = previousModels.removeLast()
        previousPointers.remove(invocationNumber)?.let {
            slice.source.setPointerAt(it)
        }
        return returnedValue
    }

    fun generateNow(model: KATEObject, parameters: List<ReferencedValue>): KATEValue {
        startInvocation(model = model, parameters = parameters)
        slice.generateTo(destination!!)
        return endInvocation()
    }


}

class FunctionDefinition(val slice: FunctionSlice, val functionName: String, val parameterNames: List<String>?) :
    CodeGen, BlockContainer {

    private val definition = object : KATERecursiveFunction(slice, parameterNames) {
        override fun invoke(model: KATEObject, invokedOn: KATEValue, parameters: List<ReferencedValue>): KATEValue {
            return generateNow(model = model, parameters)
        }

        override fun toString(): String = "$functionName()"
    }

    override fun getBlockValue(model: KATEObject): LazyBlock {
        return slice
    }

    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        definition.destination = destination
        block.model.putValue(functionName, definition)
    }
}

private fun LazyBlock.parseFunctionReturn(): ReferencedValue? {
    if (source.currentChar == '@' && source.increment("@return ")) {
        return source.parseAnyExpressionOrValue() ?: KATEUnit
    }
    return null
}

private fun SourceStream.parseParametersNames(): List<String>? {
    if (increment('(')) {
        val parameters = mutableListOf<String>()
        do {
            val paramName = parseTextWhile { currentChar.isVariableName() }
            if (paramName.isNotEmpty()) parameters.add(paramName)
        } while (increment(','))
        if (parameters.isEmpty()) return null
        return parameters
    }
    if (!increment(')')) {
        throw IllegalStateException("expected ')' got ${source.currentChar} in function parameter definition")
    }
    return null
}

fun LazyBlock.parseFunctionDefinition(anonymousFunctionName: String?): FunctionDefinition? {
    if (source.currentChar == '@' && source.increment("@function")) {
        source.increment(' ')
        val functionName: String = anonymousFunctionName
            ?: source.parseTextWhile { currentChar.isVariableName() }.also {
                if (it.isEmpty()) {
                    throw IllegalStateException("functionName cannot be empty")
                }
            }
        source.increment(' ')
        val parameters = source.parseParametersNames()
        val slice = parseBlockSlice(
            startsWith = "@function",
            endsWith = "@end_function",
            allowTextOut = false,
            inheritModel = true
        )
        return FunctionDefinition(
            slice = FunctionSlice(slice = slice),
            functionName = functionName,
            parameterNames = parameters
        )
    }
    return null
}