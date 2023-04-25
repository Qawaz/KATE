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

open class ModelDirective(val propertyPath: List<ModelReference>) : ReferencedValue {

    init {
        require(propertyPath.isNotEmpty()) {
            "model directive with empty path is not allowed"
        }
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
        return model.getModelReferenceValue(path = propertyPath)
    }

    override fun getKATEValueAndType(model: KATEObject): Pair<KATEValue, KATEType?> {
        return model.getModelReferenceValueAndType(path = propertyPath)
    }

    override fun toString(): String = propertyPath.joinToString(".")

}