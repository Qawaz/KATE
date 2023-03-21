package com.wakaztahir.kte.model

import com.wakaztahir.kte.model.model.ModelList
import com.wakaztahir.kte.model.model.MutableTemplateModel
import com.wakaztahir.kte.model.model.TemplateModel
import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.SourceStream

sealed interface ModelReference {

    val name: String

    class Property(override val name: String) : ModelReference

    class FunctionCall(
        override val name: String,
        val parametersList: List<ReferencedValue>
    ) : ModelReference

}

class ModelDirective(val propertyPath: List<ModelReference>) : ReferencedValue, AtDirective {

    override fun generateTo(model: MutableTemplateModel, source: SourceStream, destination: DestinationStream) {
        getNullablePrimitive(model)?.generateTo(model, source, destination) ?: run {
            throw IllegalStateException("primitive value with path ${pathToString()} doesn't exist")
        }
    }

    fun pathToString(until: ModelReference): String {
        return propertyPath.joinToString(".", limit = propertyPath.indexOf(until) + 1) { it.name }
    }

    fun pathToString(): String {
        return propertyPath.joinToString(".") { it.name }
    }

    override fun getNullablePrimitive(model: TemplateModel): PrimitiveValue<*>? {
        return model.getModelDirectiveValue(this)
    }

    override fun getNullableIterable(model: TemplateModel): ModelList<KTEValue>? {
        return model.getPropertyAsIterable(this)
    }

    override fun getNullableObject(model: TemplateModel): TemplateModel? {
        return model.getPropertyAsIterable(this)
    }

    override fun toString(): String = pathToString()

    override fun stringValue(indentationLevel: Int): String {
        return indentation(indentationLevel) + pathToString()
    }

}