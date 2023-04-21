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
        val parametersList: List<ReferencedValue>
    ) : ModelReference {
        override fun toString(): String {
            return name + '(' + parametersList.joinToString(",") + ')'
        }
    }

}

open class ModelDirective(val propertyPath: List<ModelReference>) : ReferencedValue {

    init {
        require(propertyPath.isNotEmpty()){
            "model directive with empty path is not allowed"
        }
    }

    override fun compareTo(model: KATEObject, other: KATEValue): Int {
        return getKATEValue(model).compareTo(model, other)
    }

    override fun getModelReference(reference: ModelReference): KATEValue? {
        return null
    }

    fun toEmptyPlaceholderInvocation(model: MutableKATEObject, endPointer: Int): PlaceholderInvocation {
        model.getModelReferenceValue(model = model, path = propertyPath)
        return PlaceholderInvocation(
            placeholderName = KATEUnit.getKateType(model),
            definitionName = null,
            paramValue = KATEUnit,
            invocationEndPointer = endPointer
        )
    }

    override fun getKATEValue(model: KATEObject): KATEValue {
        return model.getModelReferenceValue(model = model, path = propertyPath)
    }

    override fun toString(): String = propertyPath.joinToString(".")

}