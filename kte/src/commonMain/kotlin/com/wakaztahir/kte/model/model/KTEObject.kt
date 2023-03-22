package com.wakaztahir.kte.model.model

import com.wakaztahir.kte.dsl.ModelObjectImpl
import com.wakaztahir.kte.model.*

interface KTEObject : KTEValue {

    override fun asPrimitive(model: KTEObject): PrimitiveValue<*> {
        throw IllegalStateException("object is not a primitive value")
    }

    override fun asList(model: KTEObject): KTEList<KTEValue> {
        throw IllegalStateException("object is not an iterable")
    }

    override fun asObject(model: KTEObject): KTEObject {
        return this
    }

    fun getModelReference(reference: ModelReference): KTEValue?

    fun getModelDirectiveValue(directive: ModelDirective): KTEValue? {
        var currentObj: KTEObject = this
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
                    if (value is KTEObject) {
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

    fun getPropertyAsIterable(directive: ModelDirective): KTEList<KTEValue>? {
        val value = getModelDirectiveValue(directive = directive)
        @Suppress("UNCHECKED_CAST")
        return value as? KTEList<KTEValue>
    }

    fun getPropertyAsObject(directive: ModelDirective): KTEObject? {
        val value = getModelDirectiveValue(directive = directive)
        return value as? KTEObject
    }

    fun getModelDirectiveAsPrimitive(directive: ModelDirective): PrimitiveValue<*>? {
        return getModelDirectiveValue(directive) as? PrimitiveValue<*>
    }

}

fun TemplateModel(block: MutableKTEObject.() -> Unit): MutableKTEObject {
    val modelObj = ModelObjectImpl()
    block(modelObj)
    return modelObj
}