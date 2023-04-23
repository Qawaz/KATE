package com.wakaztahir.kate.model

import com.wakaztahir.kate.model.model.*

sealed interface ModelReference {

    val name: String

    class Property(override val name: String) : ModelReference {
        override fun toString(): String {
            return name
        }
    }

    class FunctionCall(
        override val name: String,
        val parametersList: List<ReferencedOrDirectValue>
    ) : ModelReference {
        override fun toString(): String {
            return name + '(' + parametersList.joinToString(",") + ')'
        }
    }

}

open class ModelDirective(val propertyPath: List<ModelReference>) : ReferencedOrDirectValue {

    init {
        require(propertyPath.isNotEmpty()) {
            "model directive with empty path is not allowed"
        }
    }

    override fun compareTo(model: KATEObject, other: ReferencedOrDirectValue): Int {
        return getKATEValue(model).compareTo(model, other)
    }

    fun toEmptyPlaceholderInvocation(model: MutableKATEObject, endPointer: Int): PlaceholderInvocation {
        model.getModelReferenceValue(path = propertyPath)
        return PlaceholderInvocation(
            placeholderName = KATEType.Unit.getKATEType(),
            definitionName = null,
            paramValue = KATEUnit,
            invocationEndPointer = endPointer
        )
    }

    override fun getKATEValue(model: KATEObject): KATEValue {
        return model.getModelReferenceValue(path = propertyPath).getKATEValue(model)
    }

    override fun toString(): String = propertyPath.joinToString(".")

}