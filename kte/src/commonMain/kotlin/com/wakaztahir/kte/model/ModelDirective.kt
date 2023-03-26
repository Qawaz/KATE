package com.wakaztahir.kte.model

import com.wakaztahir.kte.model.model.KTEList
import com.wakaztahir.kte.dsl.UnresolvedValueException
import com.wakaztahir.kte.model.model.KTEFunction
import com.wakaztahir.kte.model.model.KTEObject
import com.wakaztahir.kte.model.model.MutableKTEObject
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
            if (value is KTEFunction && propertyPath.lastOrNull() is ModelReference.FunctionCall) {
                value.parameters.addAll((propertyPath.last() as ModelReference.FunctionCall).parametersList)
            }

            value.generateTo(block, destination)
        } else {
            throwIt(block.model)
        }
    }

    private fun <T> throwIt(model: KTEObject): T {
        throw UnresolvedValueException(propertyPath.joinToString(".") + " unresolved model directive")
    }

    override fun asPrimitive(model: KTEObject): PrimitiveValue<*> {
        return model.getModelDirectiveAsPrimitive(this) ?: throwIt(model)
    }

    override fun asNullablePrimitive(model: KTEObject): PrimitiveValue<*>? {
        return model.getModelDirectiveAsPrimitive(this)
    }

    override fun asNullableList(model: KTEObject): KTEList<KTEValue>? {
        return model.getPropertyAsIterable(this)
    }

    override fun asNullableObject(model: KTEObject): KTEObject? {
        return model.getPropertyAsObject(this)
    }

    override fun asNullableMutableObject(model: KTEObject): MutableKTEObject? {
        return model.getPropertyAsMutableObject(this)
    }

    override fun toString(): String = propertyPath.joinToString(".")

    override fun stringValue(indentationLevel: Int): String {
        return indentation(indentationLevel) + toString()
    }

}