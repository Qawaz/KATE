package com.wakaztahir.kte.model

import com.wakaztahir.kte.model.model.ModelList
import com.wakaztahir.kte.dsl.UnresolvedValueException
import com.wakaztahir.kte.model.model.TemplateModel
import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.SourceStream

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

    override fun generateTo(block: LazyBlock, source: SourceStream, destination: DestinationStream) {
        val value = block.model.getModelDirectiveValue(this)
        if (value != null) {
            if (value is KTEFunction && propertyPath.lastOrNull() is ModelReference.FunctionCall) {
                value.invoke(
                    model = block.model,
                    parameters = (propertyPath.last() as ModelReference.FunctionCall).parametersList
                ).value.generateTo(block = block, source = source, destination = destination)
            } else {
                value.getValue(block.model).generateTo(block,source,destination)
            }
        } else {
            throwIt(block.model)
        }
    }

    private fun <T> throwIt(model: TemplateModel): T {
        throw UnresolvedValueException(propertyPath.joinToString(".") + " unresolved model directive")
    }

    override fun getValue(model: TemplateModel): PrimitiveValue<*> {
        return model.getModelDirectiveValue(this)?.getValue(model) ?: throwIt(model)
    }

    override fun getIterable(model: TemplateModel): ModelList<KTEValue> {
        return model.getPropertyAsIterable(this) ?: throwIt(model)
    }

    override fun getObject(model: TemplateModel): TemplateModel {
        return model.getPropertyAsIterable(this) ?: throwIt(model)
    }

    override fun toString(): String = propertyPath.joinToString(".")

    override fun stringValue(indentationLevel: Int): String {
        return indentation(indentationLevel) + toString()
    }

}