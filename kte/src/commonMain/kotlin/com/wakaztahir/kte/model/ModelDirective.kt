package com.wakaztahir.kte.model

import com.wakaztahir.kte.model.model.*
import com.wakaztahir.kte.parser.stream.DestinationStream

sealed interface ModelReference {

    val name: String


    class Property(override val name: String) : ModelReference {
        override fun toString(): String {
            return name
        }
    }

    class FunctionCall(
        override val name: String,
        val invokeOnly: Boolean = true,
        val parametersList: List<ReferencedValue>
    ) : ModelReference {
        override fun toString(): String {
            return name + '(' + parametersList.joinToString(",") + ')'
        }
    }

}

class ModelDirective(val propertyPath: List<ModelReference>) : ReferencedValue, AtDirective {

    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        getKTEValue(block.model).generateTo(block, destination)
    }

    override fun compareTo(model: KTEObject, other: KTEValue): Int {
        return getKTEValue(model).compareTo(model,other)
    }

    override fun getKTEValue(model: KTEObject): KTEValue {
        return model.getModelReferenceValue(model = model, path = propertyPath, callFunctions = true)
    }

    override fun toString(): String = propertyPath.joinToString(".")

    override fun stringValue(indentationLevel: Int): String {
        return indentation(indentationLevel) + toString()
    }

}