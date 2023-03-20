package com.wakaztahir.kte.model.model

import com.wakaztahir.kte.dsl.ModelIterable
import com.wakaztahir.kte.dsl.ModelObjectImpl
import com.wakaztahir.kte.dsl.ModelValue
import com.wakaztahir.kte.dsl.UnresolvedValueException
import com.wakaztahir.kte.model.*

interface TemplateModel : KTEValue {
    fun getValue(key: String): DynamicValue<*>?

    fun <T : KTEValue> getIterable(key: String): ModelIterable<T>?

    fun getFunction(key: String): ((List<Any>) -> ModelValue)?

    fun getObject(key: String): TemplateModel?

    private fun getAnyModelDirectiveValue(directive: ModelDirective): Any {
        var currentObj: TemplateModel = this
        var currentVal: DynamicValue<*>? = null
        for (prop in directive.propertyPath) {
            when (prop) {
                is ModelReference.FunctionCall -> {
                    currentObj.getFunction(prop.name)?.let { call ->
                        currentVal = call(prop.parametersList.map { it.getValue(this).value!! }).value
                    } ?: run {
                        throw UnresolvedValueException("couldn't resolve model path : " + directive.pathToString(until = prop))
                    }
                }

                is ModelReference.Property -> {
                    currentObj.getObject(prop.name)?.let {
                        currentObj = it
                    } ?: run {
                        currentVal = currentObj.getValue(prop.name) ?: run {
                            throw UnresolvedValueException(
                                "couldn't resolve model path : " + directive.pathToString(
                                    until = prop
                                )
                            )
                        }
                    }
                }
            }
        }
        return currentVal ?: currentObj.let {
            if (it == this) null else it
        } ?: throw UnresolvedValueException("couldn't resolve model path : " + directive.pathToString())
    }

    fun <T : KTEValue> getPropertyAsIterable(directive: ModelDirective): ModelIterable<T>? {
        val value = getAnyModelDirectiveValue(directive = directive)
        @Suppress("UNCHECKED_CAST")
        return value as? ModelIterable<T>
    }

    fun getModelDirectiveValue(directive: ModelDirective): DynamicValue<*> {
        return getAnyModelDirectiveValue(directive) as DynamicValue<*>
    }

    fun getConstantReference(reference: ConstantReference): DynamicValue<*> {
        return getValue(reference.name) ?: run {
            throw UnresolvedValueException("couldn't get constant reference by name ${reference.name}")
        }
    }

}

fun TemplateModel(block: MutableTemplateModel.() -> Unit): MutableTemplateModel {
    val modelObj = ModelObjectImpl()
    block(modelObj)
    return modelObj
}