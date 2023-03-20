package com.wakaztahir.kte.model

import com.wakaztahir.kte.dsl.ModelProvider
import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.SourceStream

sealed interface ModelReference {

    val name: String

    class Property(override val name: String) : ModelReference

    class FunctionCall(
        override val name: String,
        val parametersList: List<DynamicProperty>
    ) : ModelReference

}

class ModelDirective(
    val propertyPath: List<ModelReference>
) : ReferencedValue, AtDirective {
    override fun getValue(model: ModelProvider): DynamicValue<*> {
        return model.getModelDirectiveValue(this)
    }

    override fun generateTo(block: LazyBlock, source: SourceStream, destination: DestinationStream) {
        getValue(block.model).generateTo(block, source, destination)
    }

    fun pathToString(until: ModelReference): String {
        return propertyPath.joinToString(".", limit = propertyPath.indexOf(until) + 1) { it.name }
    }

    fun pathToString(): String {
        return propertyPath.joinToString(".") { it.name }
    }
}