package com.wakaztahir.kte.model.model

import com.wakaztahir.kte.dsl.ModelIterable
import com.wakaztahir.kte.dsl.ModelObjectImpl
import com.wakaztahir.kte.dsl.ModelValue
import com.wakaztahir.kte.model.*

interface TemplateModel : KTEValue {

    fun getValue(key: String): PrimitiveValue<*>?

    fun getIterable(key: String): ModelIterable<KTEValue>?

    fun getFunction(key: String): ((List<Any>) -> ModelValue)?

    fun getObject(key: String): TemplateModel?

    fun getAnyModelDirectiveValue(directive: ModelDirective): KTEValue? {
        var currentObj: TemplateModel = this
        var currentVal: PrimitiveValue<*>? = null
        val currentIterable: ModelIterable<*>? = null
        for (prop in directive.propertyPath) {
            when (prop) {
                is ModelReference.FunctionCall -> {
                    currentObj.getFunction(prop.name)?.let { call ->
                        currentVal = call(prop.parametersList.map { it.getValue(this).value!! }).value
                    } ?: run {
                        return null
                    }
                }

                is ModelReference.Property -> {
                    currentObj.getObject(prop.name)?.let {
                        currentObj = it
                    } ?: run {
                        currentObj.getIterable(prop.name)
                        null
                    } ?: run {
                        currentVal = currentObj.getValue(prop.name) ?: run {
                            return null
                        }
                    }
                }
            }
        }
        return currentVal ?: currentIterable ?: currentObj.let {
            if (it == this) null else it
        }
    }

    fun getPropertyAsIterable(directive: ModelDirective): ModelIterable<KTEValue>? {
        val value = getAnyModelDirectiveValue(directive = directive)
        @Suppress("UNCHECKED_CAST")
        return value as? ModelIterable<KTEValue>
    }

    fun getPropertyAsObject(directive: ModelDirective): TemplateModel? {
        val value = getAnyModelDirectiveValue(directive = directive)
        return value as? TemplateModel
    }

    fun getModelDirectiveValue(directive: ModelDirective): PrimitiveValue<*>? {
        return getAnyModelDirectiveValue(directive) as? PrimitiveValue<*>
    }

}

fun TemplateModel(block: MutableTemplateModel.() -> Unit): MutableTemplateModel {
    val modelObj = ModelObjectImpl()
    block(modelObj)
    return modelObj
}