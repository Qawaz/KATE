package com.wakaztahir.kate.model

import com.wakaztahir.kate.dsl.ModelObjectImpl
import com.wakaztahir.kate.model.model.KTEObject
import com.wakaztahir.kate.model.model.KTEValue
import com.wakaztahir.kate.model.model.MutableKTEObject
import com.wakaztahir.kate.parser.parseFunctionDefinition
import com.wakaztahir.kate.parser.parseObjectDeclaration
import com.wakaztahir.kate.parser.parseVariableDeclaration
import com.wakaztahir.kate.parser.stream.DestinationStream

class ObjectDeclarationModel(
    objectName: String,
    override val parent: MutableKTEObject
) : ModelObjectImpl(objectName) {
    override fun getModelReference(reference: ModelReference): KTEValue? {
        return super.getModelReference(reference) ?: parent.getModelReference(reference)
    }
}

class ObjectDeclarationBlockSlice(
    parentBlock: LazyBlock,
    startPointer: Int,
    length: Int,
    blockEndPointer: Int,
    override val model: ObjectDeclarationModel,
    indentationLevel: Int
) : LazyBlockSlice(
    parentBlock = parentBlock,
    startPointer = startPointer,
    length = length,
    blockEndPointer = blockEndPointer,
    model = model,
    isWriteUnprocessedTextEnabled = false,
    indentationLevel = indentationLevel
) {

    override fun parseAtDirective(): CodeGen? {
        parseVariableDeclaration()?.let { return it }
        parseFunctionDefinition()?.let { return it }
        parseObjectDeclaration()?.let { return it }
        return null
    }
}

class ObjectDeclaration(val objectName: String, val declarationBlock: ObjectDeclarationBlockSlice) : BlockContainer {

    override val isEmptyWriter: Boolean
        get() = true

    override fun getBlockValue(model: KTEObject): LazyBlock = declarationBlock
    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        declarationBlock.generateTo(destination)
        block.model.putValue(objectName, declarationBlock.model)
    }
}