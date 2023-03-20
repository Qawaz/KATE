package com.wakaztahir.kte.model

import com.wakaztahir.kte.dsl.ModelIterable
import com.wakaztahir.kte.dsl.UnresolvedValueException
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

    override fun generateTo(block: LazyBlock, source: SourceStream, destination: DestinationStream) {
        getValue(block.model).generateTo(block, source, destination)
    }

    fun pathToString(until: ModelReference): String {
        return propertyPath.joinToString(".", limit = propertyPath.indexOf(until) + 1) { it.name }
    }

    fun pathToString(): String {
        return propertyPath.joinToString(".") { it.name }
    }

    override fun getValue(model: TemplateModel): PrimitiveValue<*> {
        return model.getModelDirectiveValue(this)
            ?: throw UnresolvedValueException(pathToString() + " unresolved model directive")
    }

    override fun getIterable(model: TemplateModel): ModelIterable<KTEValue> {
        return model.getPropertyAsIterable(this)
            ?: throw UnresolvedValueException(pathToString() + " unresolved model directive")
    }

    override fun getObject(model: TemplateModel): TemplateModel {
        return model.getPropertyAsIterable(this)
            ?: throw UnresolvedValueException(pathToString() + " unresolved model directive")
    }

}