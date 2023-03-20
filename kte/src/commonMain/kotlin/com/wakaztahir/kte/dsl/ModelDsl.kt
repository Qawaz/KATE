package com.wakaztahir.kte.dsl

import com.wakaztahir.kte.model.*

class UnresolvedValueException(message: String) : Throwable(message)

class ModelValue constructor(val value: DynamicValue<*>) {

    constructor(value: Int) : this(IntValue(value))

    constructor(value: Float) : this(FloatValue(value))

    constructor(value: String) : this(StringValue(value))

    constructor(value: Boolean) : this(BooleanValue(value))

}

interface ModelProvider {

    fun getConstantReference(reference: ConstantReference): DynamicValue<*>
    fun getModelDirectiveValue(directive: ModelDirective): DynamicValue<*>

}

interface ModelDsl {

    fun putValue(key: String, value: ModelValue)

    fun putValue(key: String, value: DynamicProperty)

    fun putValue(key: String, value: String) {
        putValue(key, ModelValue(value))
    }

    fun putValue(key: String, value: Int) {
        putValue(key, ModelValue(value))
    }

    fun putValue(key: String, value: Float) {
        putValue(key, ModelValue(value))
    }

    fun putValue(key: String, value: Boolean) {
        putValue(key, ModelValue(value))
    }

    fun putValue(key: String, value: DynamicValue<*>)

    fun putObject(key: String, inside: ModelDsl.() -> Unit)

    fun putFunction(key: String, block: (parameters: List<Any>) -> ModelValue)

}

interface TemplateModel : ModelDsl, ModelProvider

class ModelObject : TemplateModel {

    private val objects = mutableMapOf<String, ModelObject>()
    private val values = mutableMapOf<String, DynamicValue<*>>()
    private val functions = mutableMapOf<String, (List<Any>) -> ModelValue>()

    override fun putValue(key: String, value: ModelValue) {
        values[key] = value.value
    }

    override fun putValue(key: String, value: DynamicValue<*>) {
        values[key] = value
    }

    override fun putObject(key: String, inside: ModelDsl.() -> Unit) {
        val newObj = ModelObject()
        objects[key] = newObj
        inside(newObj)
    }

    override fun putFunction(key: String, block: (parameters: List<Any>) -> ModelValue) {
        functions[key] = block
    }

    override fun getModelDirectiveValue(directive: ModelDirective): DynamicValue<*> {
        var currentObj = this
        var currentVal: DynamicValue<*>? = null
        for (prop in directive.propertyPath) {
            when (prop) {
                is ModelReference.FunctionCall -> {
                    currentObj.functions[prop.name]?.let { call ->
                        currentVal = call(prop.parametersList.map { it.getValue(this).value!! }).value
                    } ?: run {
                        throw UnresolvedValueException("couldn't resolve model path : " + directive.pathToString(until = prop))
                    }
                }

                is ModelReference.Property -> {
                    currentObj.objects[prop.name]?.let {
                        currentObj = it
                    } ?: run {
                        currentVal = currentObj.values[prop.name] ?: run {
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
        return currentVal ?: throw UnresolvedValueException("couldn't resolve model path : " + directive.pathToString())
    }

    override fun getConstantReference(reference: ConstantReference): DynamicValue<*> {
        return values[reference.name] ?: run {
            throw UnresolvedValueException("couldn't get constant reference by name ${reference.name}")
        }
    }

    override fun putValue(key: String, value: DynamicProperty) {
        putValue(key, value.getValue(this))
    }

}

class ScopedModelObject(
    private val parent: TemplateModel,
    private val current: TemplateModel = ModelObject(),
) : TemplateModel, ModelDsl by current {
    override fun getConstantReference(reference: ConstantReference): DynamicValue<*> {
        return try {
            current.getConstantReference(reference)
        } catch (e: UnresolvedValueException) {
            try {
                parent.getConstantReference(reference)
            } catch (_: UnresolvedValueException) {
                throw e
            }
        }
    }

    override fun getModelDirectiveValue(directive: ModelDirective): DynamicValue<*> {
        return try {
            current.getModelDirectiveValue(directive)
        } catch (e: UnresolvedValueException) {
            try {
                parent.getModelDirectiveValue(directive)
            } catch (_: UnresolvedValueException) {
                throw e
            }
        }
    }
}

fun TemplateModel(block: ModelDsl.() -> Unit): TemplateModel {
    val modelObj = ModelObject()
    block(modelObj)
    return modelObj
}

