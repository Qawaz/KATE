package com.wakaztahir.kte.model.model

import com.wakaztahir.kte.dsl.ModelObjectImpl
import com.wakaztahir.kte.model.*

interface TemplateModel : KTEValue {

    override fun getValue(model: TemplateModel): PrimitiveValue<*> {
        throw IllegalStateException("object is not a primitive value")
    }

    override fun getIterable(model: TemplateModel): ModelList<KTEValue> {
        throw IllegalStateException("object is not an iterable")
    }

    override fun getObject(model: TemplateModel): TemplateModel {
        return this
    }

    fun getModelReference(reference: ModelReference): KTEValue?

    fun getModelDirectiveValue(directive: ModelDirective): KTEValue? {
        var currentObj: TemplateModel = this
        var currentVal: KTEValue? = null
        for (prop in directive.propertyPath) {
            when (prop) {
                is ModelReference.FunctionCall -> {
                    (currentObj.getModelReference(prop) as? KTEFunction)?.let { currentVal = it } ?: run {
                        throw IllegalStateException(
                            "function ${
                                directive.propertyPath.joinToString(
                                    separator = ".",
                                    limit = directive.propertyPath.indexOf(prop) + 1
                                )
                            } does not exist"
                        )
                    }
                }

                is ModelReference.Property -> {
                    val value = currentObj.getModelReference(prop) ?: return null
                    if (value is TemplateModel) {
                        currentObj = value
                        currentVal = value
                    } else {
                        currentVal = value
                    }
                }
            }
        }
        return currentVal
    }

    fun getPropertyAsIterable(directive: ModelDirective): ModelList<KTEValue>? {
        val value = getModelDirectiveValue(directive = directive)
        @Suppress("UNCHECKED_CAST")
        return value as? ModelList<KTEValue>
    }

    fun getPropertyAsObject(directive: ModelDirective): TemplateModel? {
        val value = getModelDirectiveValue(directive = directive)
        return value as? TemplateModel
    }

    fun getModelDirectiveAsPrimitive(directive: ModelDirective): PrimitiveValue<*>? {
        return getModelDirectiveValue(directive) as? PrimitiveValue<*>
    }

}

fun TemplateModel(block: MutableTemplateModel.() -> Unit): MutableTemplateModel {
    val modelObj = ModelObjectImpl()
    block(modelObj)
    return modelObj
}