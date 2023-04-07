package com.wakaztahir.kate.model

import com.wakaztahir.kate.model.model.KATEObject
import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.parser.parsePartialRawImplicitDirective
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.parser.stream.TextSourceStream

open class PlaceholderBlock(
    parentBlock: LazyBlock,
    val placeholderName: String,
    val definitionName: String,
    startPointer: Int,
    length: Int,
    blockEndPointer: Int,
    parent: MutableKATEObject,
    allowTextOut: Boolean,
    indentationLevel: Int
) : LazyBlockSlice(
    parentBlock = parentBlock,
    startPointer = startPointer,
    length = length,
    blockEndPointer = blockEndPointer,
    model = parent,
    isWriteUnprocessedTextEnabled = allowTextOut,
    indentationLevel = indentationLevel
) {

    override var model: MutableKATEObject = parent
        protected set

    private var paramValue: KATEValue? = null

    fun setParamValue(value: KATEValue) {
        this.paramValue = value
    }

    protected open fun generateActual(destination: DestinationStream) {
        super.generateTo(destination)
    }

    override fun parseImplicitDirectives(): CodeGen? {
        super.parseImplicitDirectives()?.let { return it }
        if (!isWriteUnprocessedTextEnabled) {
            parsePartialRawImplicitDirective()?.let { return it }
        }
        return null
    }

    override fun generateTo(destination: DestinationStream) {
        if (paramValue != null) {
            require(!model.contains("__param__")) {
                "when passing @var(__param__) value to placeholder invocation , defining value with same name \"__param__\" is not allowed \n this can also happen " +
                        "if you invoke a placeholder inside a placeholder definition , placeholders are not recursive , solution is to call a recursive function inside a placeholder"
            }
//            (paramValue as? ModelDirective)?.propertyPath?.lastOrNull()?.name?.let {
//                model.putValue("__kte_param_name__", it)
//            }
            model.putValue("__param__", paramValue!!)
        }
        generateActual(destination)
        if (paramValue != null) {
            model.removeKey("__param__")
//            if (paramValue is ModelDirective) {
//                (paramValue as? ModelDirective)?.propertyPath?.lastOrNull()?.name?.let {
//                    model.removeKey("__kte_param_name__")
//                }
//            }
        }
    }

}

class TextPlaceholderBlock(
    val text: String,
    parent: LazyBlock,
    placeholderName: String,
    definitionName: String,
) : PlaceholderBlock(
    parentBlock = parent,
    placeholderName = placeholderName,
    definitionName = definitionName,
    startPointer = 0,
    blockEndPointer = 0,
    length = text.length,
    parent = parent.model,
    allowTextOut = false,
    indentationLevel = 0
) {
    override fun generateActual(destination: DestinationStream) {
        TextSourceStream(
            sourceCode = text,
            model = model,
            placeholderManager = source.placeholderManager,
            embeddingManager = source.embeddingManager
        ).generateTo(destination)
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
    var paramValue: KATEValue?,
    val invocationEndPointer: Int
) : CodeGen {
    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        val placeholder = block.source.placeholderManager.getPlaceholder(placeholderName = placeholderName)
            ?: throw IllegalStateException("placeholder with name $placeholderName not found")
        placeholder.setParamValue(paramValue?.getKTEValue(block.model) ?: block.model)
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