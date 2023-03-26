package com.wakaztahir.kte.model

import com.wakaztahir.kte.dsl.UnresolvedValueException
import com.wakaztahir.kte.model.model.*

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
        val parametersList: List<KTEValue>
    ) : ModelReference {
        override fun toString(): String {
            return name + '(' + parametersList.joinToString(",") + ')'
        }
    }

}

class ModelDirective(val propertyPath: List<ModelReference>) : ReferencedValue {

    private fun <T> throwIt(model: KTEObject): T {
        throw UnresolvedValueException("could not resolve '" + propertyPath.joinToString(".") + "' model directive in model $model")
    }

    override fun getKTEValue(model: KTEObject): KTEValue {
        return model.getModelDirectiveValue(this)?.also { value ->
            if (value is KTEFunction) {
                propertyPath.lastOrNull()?.let { it as? ModelReference.FunctionCall }?.let { call ->
                    value.parameters.addAll(call.parametersList)
                    value.invokeOnly = call.invokeOnly
                }
            }
        } ?: throwIt(model)
    }

    override fun toString(): String = propertyPath.joinToString(".")

}