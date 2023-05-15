package com.wakaztahir.kate.parser.function

import com.wakaztahir.kate.dsl.ScopedModelObject
import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.*
import com.wakaztahir.kate.parser.*
import com.wakaztahir.kate.parser.parseAnyExpressionOrValue
import com.wakaztahir.kate.parser.stream.*
import com.wakaztahir.kate.parser.stream.increment
import com.wakaztahir.kate.parser.stream.parseTextWhile
import com.wakaztahir.kate.parser.variable.isVariableName
import com.wakaztahir.kate.parser.variable.parseKATEType
import com.wakaztahir.kate.tokenizer.NodeTokenizer

class FunctionReturn(val slice: FunctionParsedBlock, val value: ReferencedOrDirectValue) : CodeGen {

    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.functionReturn

    override fun generateTo(destination: DestinationStream) {
        slice.onReturnValueFound(value.getKATEValue())
        slice.hasReturned = true
        if (slice.currentGen is BlockContainer) {
            (slice.currentGen as BlockContainer).parsedBlock.haltGenFlag = true
            if(slice.currentGen is ForLoop){
                (slice.currentGen as ForLoop).parsedBlock.hasBroken = true
            }
        }
    }

}

class FunctionParsedBlock(val provider: ModelProvider.LateInit, codeGens: List<CodeGenRange>) : ParsedBlock(codeGens) {
    var hasReturned = false
    var onReturnValueFound: (KATEValue) -> Unit = {}
    var currentGen: CodeGen? = null
    override fun generateTo(destination: DestinationStream) {
        hasReturned = false
        for (gen in codeGens) {
            if (hasReturned) break
            currentGen = gen.gen
            gen.gen.generateTo(destination)
        }
    }
}

class FunctionSlice(
    parentBlock: LazyBlock,
    startPointer: Int,
    length: Int,
    blockEndPointer: Int,
    override val provider: ModelProvider.LateInit,
    indentationLevel: Int
) : LazyBlockSlice(
    parentBlock = parentBlock,
    startPointer = startPointer,
    length = length,
    blockEndPointer = blockEndPointer,
    provider = provider,
    indentationLevel = indentationLevel,
    isDefaultNoRaw = false
) {

    private var parseTimes = 0

    private val parsedBlock = FunctionParsedBlock(provider = provider, codeGens = mutableListOf())

    constructor(slice: LazyBlockSlice) : this(
        parentBlock = slice.parentBlock,
        startPointer = slice.startPointer,
        length = slice.length,
        blockEndPointer = slice.blockEndPointer,
        provider = slice.provider as ModelProvider.LateInit,
        indentationLevel = slice.indentationLevel
    )

    override fun parse(): FunctionParsedBlock {
        parseTimes++
        if (parseTimes > 2) throw IllegalStateException("one instance can parse one block")
        val parsed = super.parse()
        (parsedBlock.codeGens as MutableList).addAll(parsed.codeGens)
        return parsedBlock
    }

    override fun parseNestedAtDirective(block: LazyBlock): CodeGen? {
        block.parseFunctionReturn()?.let { return FunctionReturn(parsedBlock, it) }
        block.parseDefaultNoRaw()?.let { return it }
        return super.parseNestedAtDirective(block)
    }

}

abstract class KATERecursiveFunction(
    val parentProvider: ModelProvider,
    val slice: FunctionParsedBlock,
    val parameterNames: List<String>?,
    parameterTypes: List<KATEType>?,
    returnedType: KATEType,
) : KATEFunction(returnedType, parameterTypes) {

    constructor(
        parentProvider: ModelProvider,
        slice: FunctionParsedBlock,
        parameterNameAndTypes: Map<String, KATEType>?,
        returnedType: KATEType,
    ) : this(
        parentProvider = parentProvider,
        slice = slice,
        parameterNames = parameterNameAndTypes?.keys?.toList(),
        parameterTypes = parameterNameAndTypes?.values?.toList(),
        returnedType = returnedType
    )

    var destination: DestinationStream? = null

    private var invocationNumber = 0
    private var returnedValues = hashMapOf<Int, KATEValue>()
    private val previousModels = mutableListOf<MutableKATEObject>()
//    private val previousPointers = hashMapOf<Int, Int>()

    init {
        slice.onReturnValueFound = {
            returnedValues[invocationNumber] = it
        }
//        slice.keepIterating = {
//            !returnedValues.containsKey(invocationNumber)
//        }
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

    private fun startInvocation(parameters: List<Pair<KATEValue, KATEType?>>) {
        invocationNumber++
//        print("Preparing Function $invocationNumber ")
        if (invocationNumber > 1) {
            previousModels.add(slice.provider.model)
//            previousPointers[invocationNumber - 1] = slice.source.pointer
        }
        slice.provider.model = ScopedModelObject(parentProvider.model)
        slice.provider.model.forEachParam { paramName, paramType, index ->
            if (index < parameters.size) {
                parameters[index].let {
                    require(insertValue(paramName, it.first)) {
                        "couldn't insert function parameter into model with name $paramName"
                    }
//                    print("PARAM $paramName = ${it.first} FROM (${(parameters[index])}) , ")
                    it.second?.let { type -> setExplicitType(paramName, type) }
                }
//                println()
            } else {
                throw IllegalStateException("function expects ${parameterNames?.size} parameters and not ${parameters.size}")
            }
        }
    }

    private fun endInvocation(): KATEValue {
        val returnedValue = returnedValues.remove(invocationNumber)?.getKATEValue() ?: KATEUnit
        invocationNumber--
        if (previousModels.isNotEmpty()) slice.provider.model = previousModels.removeLast()
//        previousPointers.remove(invocationNumber)?.let {
//            slice.source.setPointerAt(it)
//        }
        return returnedValue
    }

    private fun generateNow(parameters: List<Pair<KATEValue, KATEType?>>): KATEValue {
        startInvocation(parameters = parameters)
        slice.generateTo(destination!!)
        return endInvocation()
    }

    override fun invoke(
        model: KATEObject,
        invokedOn: KATEValue,
        explicitType: KATEType?,
        parameters: List<ReferencedOrDirectValue>
    ): ReferencedOrDirectValue {
        return generateNow(parameters.map { it.getKATEValueAndType() })
    }

}

class FunctionDefinition(
    override val parsedBlock: FunctionParsedBlock,
    val functionName: String,
    val definitionModel: ModelProvider,
    parameterNames: Map<String, KATEType>?,
    returnedType: KATEType
) : BlockContainer {

    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.functionDefinition

    val definition =
        object : KATERecursiveFunction(parentProvider = definitionModel, slice = parsedBlock, parameterNames, returnedType) {
            override fun toString(): String = functionName + ' ' + super.toString()
        }

    override fun generateTo(destination: DestinationStream) {
        definition.destination = destination
        definitionModel.model.insertValue(functionName, definition)
    }
}

private fun LazyBlock.parseFunctionReturn(): ReferencedOrDirectValue? {
    if (source.currentChar == '@' && source.increment("@return ")) {
        return parseAnyExpressionOrValue(
            parseDirectRefs = true
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
                parseKATEType(parseMetadata = false) ?: KATEType.Any
            } else KATEType.Any
            parameters[paramName] = paramType
        } while (increment(','))
        if (!increment(')')) {
            throw IllegalStateException("expected ')' got $currentChar in function parameter definition")
        }
        if (parameters.isEmpty()) return null
        return parameters
    }
    if (!increment(')')) {
        throw IllegalStateException("expected ')' got $currentChar in function parameter definition")
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
            source.parseKATEType(parseMetadata = false)
                ?: throw IllegalStateException("expected a type after \"->\" but got ${source.currentChar}")
        } else {
            source.setPointerAt(afterParametersPointer)
            KATEType.Any
        }

        val slice = parseBlockSlice(
            startsWith = "@function",
            endsWith = "@end_function",
            isDefaultNoRaw = false,
            provider = ModelProvider.LateInit()
        )

        val blockParser = FunctionSlice(slice = slice)

        return FunctionDefinition(
            parsedBlock = blockParser.parse(),
            definitionModel = provider,
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
        invokedOn: ReferencedOrDirectValue,
        explicitType: KATEType?,
        parameters: List<ReferencedOrDirectValue>
    ): KATEValue
}

fun KATEParsedFunction(
    typeDefinition: String,
    invoke: KATEInvocation
): KATEFunction {
    val source = TextSourceStream("@function $typeDefinition @end_function")
    val parsed = source.block.parseFunctionDefinition(anonymousFunctionName = null)!!
    val parsedDef = parsed.definition
    return object : KATEFunction(parsedDef.returnedType, parsedDef.parameterTypes) {
        override fun invoke(
            model: KATEObject,
            invokedOn: KATEValue,
            explicitType: KATEType?,
            parameters: List<ReferencedOrDirectValue>
        ): ReferencedOrDirectValue {
            return invoke.invoke(model, invokedOn, null, parameters)
        }

        override fun toString(): String = parsed.functionName + ' ' + super.toString()
    }
}