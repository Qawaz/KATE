package com.wakaztahir.kte.model

import com.wakaztahir.kte.TemplateContext

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
) : ReferencedValue {
    override fun getValue(context: TemplateContext): DynamicValue<*>? {
        return context.getModelDirectiveValue(this)
    }
}