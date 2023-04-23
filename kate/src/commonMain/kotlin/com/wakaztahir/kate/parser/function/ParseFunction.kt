package com.wakaztahir.kate.parser.function

import com.wakaztahir.kate.dsl.ScopedModelObject
import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.*
import com.wakaztahir.kate.parser.parseAnyExpressionOrValue
import com.wakaztahir.kate.parser.parseBlockSlice
import com.wakaztahir.kate.parser.stream.*
import com.wakaztahir.kate.parser.stream.increment
import com.wakaztahir.kate.parser.stream.parseTextWhile
import com.wakaztahir.kate.parser.variable.isVariableName
import com.wakaztahir.kate.parser.variable.parseKATEType

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
    var onReturnValueFound: (ReferencedOrDirectValue) -> Unit = {}

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

abstract class KATERecursiveFunction(
    val slice: FunctionSlice,
    val parameterNames: List<String>?,
    parameterTypes: List<KATEType>?,
    returnedType: KATEType,
) : KATEFunction(returnedType, parameterTypes) {

    constructor(
        slice: FunctionSlice,
        parameterNameAndTypes: Map<String, KATEType>?,
        returnedType: KATEType,
    ) : this(
        slice = slice,
        parameterNames = parameterNameAndTypes?.keys?.toList(),
        parameterTypes = parameterNameAndTypes?.values?.toList(),
        returnedType = returnedType
    )

    var destination: DestinationStream? = null

    private var invocationNumber = 0
    private var returnedValues = hashMapOf<Int, ReferencedOrDirectValue>()
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

    private inline fun KATEObject.forEachParam(block: MutableKATEObject.(String, KATEType, Int) -> Unit) {
        (this as? MutableKATEObject)?.let {
            if (parameterNames != null) {
                var i = 0
                while (i < parameterNames.size) {
                    val param = parameterNames[i]
                    block(it, param, parameterTypes?.get(i)!!, i)
                    i++
                }
            }
        }
    }

    private fun startInvocation(model: KATEObject, parameters: List<ReferencedOrDirectValue>) {
        invocationNumber++
        if (invocationNumber > 1) {
            previousModels.add(slice.model)
            previousPointers[invocationNumber - 1] = slice.source.pointer
        }
        slice.model = ScopedModelObject(slice.parentBlock.model)
        slice.model.forEachParam { paramName, paramType, index ->
            if (index < parameters.size) {
                insertValue(paramName, parameters[index].getKATEValue(model))
            } else {
                throw IllegalStateException("function expects ${parameterNames?.size} parameters and not ${parameters.size}")
            }
        }
    }

    private fun endInvocation(): KATEValue {
        val returnedValue = returnedValues.remove(invocationNumber)?.getKATEValue(slice.model) ?: KATEUnit
        invocationNumber--
        if (previousModels.isNotEmpty()) slice.model = previousModels.removeLast()
        previousPointers.remove(invocationNumber)?.let {
            slice.source.setPointerAt(it)
        }
        return returnedValue
    }

    fun generateNow(model: KATEObject, parameters: List<ReferencedOrDirectValue>): KATEValue {
        startInvocation(model = model, parameters = parameters)
        slice.generateTo(destination!!)
        return endInvocation()
    }


}

class FunctionDefinition(
    val slice: FunctionSlice,
    val functionName: String,
    parameterNames: Map<String, KATEType>?,
    returnedType: KATEType
) : CodeGen, BlockContainer {

    val definition = object : KATERecursiveFunction(slice, parameterNames, returnedType) {
        override fun invoke(
            model: KATEObject,
            path: List<ModelReference>,
            pathIndex: Int,
            parent: ReferencedOrDirectValue?,
            invokedOn: KATEValue,
            parameters: List<KATEValue>
        ): KATEValue {
            return generateNow(model = model, parameters)
        }

        override fun toString(): String = functionName + ' ' + super.toString()
    }

    override fun getBlockValue(model: KATEObject): LazyBlock {
        return slice
    }

    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        definition.destination = destination
        block.model.insertValue(functionName, definition)
    }
}

private fun LazyBlock.parseFunctionReturn(): ReferencedOrDirectValue? {
    if (source.currentChar == '@' && source.increment("@return ")) {
        return source.parseAnyExpressionOrValue(
            parseFirstStringOrChar = true,
            parseNotFirstStringOrChar = true,
            parseDirectRefs = true,
            allowAtLessExpressions = true
        ) ?: KATEUnit
    }
    return null
}

private fun SourceStream.parseFunctionParameters(): Map<String, KATEType>? {
    if (increment('(')) {
        val parameters = mutableMapOf<String, KATEType>()
        do {
            val paramName = parseTextWhile { currentChar.isVariableName() }
            if (paramName.isEmpty()) continue
            escapeSpaces()
            val paramType = if (increment(':')) {
                escapeSpaces()
                parseKATEType() ?: KATEType.Any
            } else KATEType.Any
            parameters[paramName] = paramType
        } while (increment(','))
        if (!increment(')')) {
            throw IllegalStateException("expected ')' got ${source.currentChar} in function parameter definition")
        }
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
        val parameters = source.parseFunctionParameters()

        val afterParametersPointer = source.pointer

        source.escapeSpaces()
        val returnedType = if (source.increment("->")) {
            source.escapeSpaces()
            source.parseKATEType()
                ?: throw IllegalStateException("expected a type after \"->\" but got ${source.currentChar}")
        } else {
            source.setPointerAt(afterParametersPointer)
            KATEType.Any
        }

        val slice = parseBlockSlice(
            startsWith = "@function",
            endsWith = "@end_function",
            allowTextOut = false,
            inheritModel = true
        )
        return FunctionDefinition(
            slice = FunctionSlice(slice = slice),
            functionName = functionName,
            parameterNames = parameters,
            returnedType = returnedType
        )
    }
    return null
}

fun interface KATEInvocation {
    fun invoke(
        model: KATEObject,
        path: List<ModelReference>,
        pathIndex: Int,
        invokedOn: ReferencedOrDirectValue,
        parameters: List<ReferencedOrDirectValue>
    ): KATEValue
}

fun KATEParsedFunction(
    typeDefinition: String,
    invoke: KATEInvocation
): KATEFunction {
    val source = TextSourceStream("@function $typeDefinition @end_function")
    val parsed = source.parseFunctionDefinition(anonymousFunctionName = null)!!
    val parsedDef = parsed.definition
    return object : KATEFunction(parsedDef.returnedType, parsedDef.parameterTypes) {
        override fun invoke(
            model: KATEObject,
            path: List<ModelReference>,
            pathIndex: Int,
            parent: ReferencedOrDirectValue?,
            invokedOn: KATEValue,
            parameters: List<KATEValue>
        ): KATEValue {
            return invoke.invoke(model, path, pathIndex, invokedOn, parameters)
        }

        override fun toString(): String = parsed.functionName + ' ' + super.toString()
    }
}