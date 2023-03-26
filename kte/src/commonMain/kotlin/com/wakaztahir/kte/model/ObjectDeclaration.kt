package com.wakaztahir.kte.model

import com.wakaztahir.kte.dsl.ModelObjectImpl
import com.wakaztahir.kte.model.model.KTEObject
import com.wakaztahir.kte.model.model.KTEValue
import com.wakaztahir.kte.model.model.MutableKTEObject
import com.wakaztahir.kte.parser.parseObjectDeclaration
import com.wakaztahir.kte.parser.parseVariableDeclaration
import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.SourceStream

class ObjectDeclarationModel(
    objectName: String,
    val parent: MutableKTEObject
) : ModelObjectImpl(objectName) {
    override fun getModelReference(reference: ModelReference): KTEValue? {
        return super.getModelReference(reference) ?: parent.getModelReference(reference)
    }
}

class ObjectDeclarationBlockSlice(
    source: SourceStream,
    startPointer: Int,
    length: Int,
    blockEndPointer: Int,
    override val model: ObjectDeclarationModel,
) : LazyBlockSlice(
    source = source,
    startPointer = startPointer,
    length = length,
    blockEndPointer = blockEndPointer,
    model = model,
    allowTextOut = false,
) {

    override fun parseAtDirective(): CodeGen? {
        source.parseVariableDeclaration()?.let { return it }
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