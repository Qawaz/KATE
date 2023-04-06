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
        var invokeOnly: Boolean = true,
        val parametersList: List<ReferencedValue>
    ) : ModelReference {
        override fun toString(): String {
            return name + '(' + parametersList.joinToString(",") + ')'
        }
    }

}

open class ModelDirective(val propertyPath: List<ModelReference>) : ReferencedValue {

    override fun compareTo(model: KATEObject, other: KATEValue): Int {
        return getKTEValue(model).compareTo(model, other)
    }

    override fun getModelReference(reference: ModelReference): KATEValue? {
        return null
    }

    override fun getKTEValue(model: KATEObject): KATEValue {
        return model.getModelReferenceValue(model = model, path = propertyPath, callFunctions = true)
    }

    override fun toString(): String = propertyPath.joinToString(".")

}