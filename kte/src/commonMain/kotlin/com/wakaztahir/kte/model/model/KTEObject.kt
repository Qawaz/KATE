package com.wakaztahir.kte.model.model

import com.wakaztahir.kte.dsl.ModelObjectImpl
import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.parser.stream.LanguageDestination

interface KTEObject : KTEValue {

    val objectName: String
    val contained: Map<String, KTEValue>

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

    override fun writeTo(model: KTEObject, destination: LanguageDestination) {
        destination.write(this)
    }

    fun traverse(block: (KTEValue) -> Unit) {
        block(this)
        for (each in contained) {
            when (each.value) {
                is KTEList<*> -> {
                    for (item in each.value as KTEList<*>) block(item)
                }

                is KTEObject -> {
                    (each.value as KTEObject).traverse(block)
                }

                else -> {
                    block(each.value)
                }
            }
        }
    }

}

fun TemplateModel(name: String = "Global", block: MutableKTEObject.() -> Unit): MutableKTEObject {
    val modelObj = ModelObjectImpl(objectName = name)
    block(modelObj)
    return modelObj
}