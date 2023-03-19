package com.wakaztahir.kte.model

import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.parser.stream.DestinationStream

internal sealed interface ModelReference {

    val name: String

    class Property(override val name: String) : ModelReference

    class FunctionCall(
        override val name: String,
        val parametersList: List<DynamicProperty>
    ) : ModelReference

}

internal class ModelDirective(
    val propertyPath: List<ModelReference>
) : ReferencedValue, AtDirective {
    override fun getValue(context: TemplateContext): DynamicValue<*>? {
        return context.getModelDirectiveValue(this)
    }

    override fun generateTo(context: TemplateContext, stream: DestinationStream) {
        getValue(context)!!.generateTo(context, stream)
    }
}