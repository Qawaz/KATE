package com.wakaztahir.kte.model

import com.wakaztahir.kte.dsl.UnresolvedValueException
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
        val value = block.model.getModelDirectiveValue(this)
        if (value != null) {

            // Adding parameters to function parameters list
            if (value is KTEFunction) {
                propertyPath.lastOrNull()?.let { it as? ModelReference.FunctionCall }?.let { call ->
                    value.parameters.addAll(call.parametersList)
                    value.invokeOnly = call.invokeOnly
                }
            }

            value.generateTo(block, destination)
        } else {
            throwIt(block.model)
        }
    }

    private fun <T> throwIt(model: KTEObject): T {
        throw UnresolvedValueException("could not resolve '" + propertyPath.joinToString(".") + "' model directive in model $model")
    }

    override fun getKTEValue(model: KTEObject): KTEValue {
        return model.getModelDirectiveValue(this) ?: throwIt(model)
    }

    override fun toString(): String = propertyPath.joinToString(".")

    override fun stringValue(indentationLevel: Int): String {
        return indentation(indentationLevel) + toString()
    }

}