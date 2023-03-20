package com.wakaztahir.kte.dsl

import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.model.model.MutableTemplateModel
import com.wakaztahir.kte.model.model.TemplateModel

class UnresolvedValueException(message: String) : Throwable(message)

class ModelValue constructor(val value: DynamicValue<*>) {

    constructor(value: Int) : this(IntValue(value))

    constructor(value: Float) : this(FloatValue(value))

    constructor(value: String) : this(StringValue(value))

    constructor(value: Boolean) : this(BooleanValue(value))

}

interface ModelIterable<T : KTEValue> : List<T>, TemplateModel, KTEValue

class ModelListImpl<T : KTEValue>(val collection: List<T>) : List<T> by collection, ModelIterable<T> {

    private val props: MutableMap<String, (List<Any>?) -> ModelValue> by lazy {
        hashMapOf<String, (List<Any>?) -> ModelValue>().apply {
            put("size") { ModelValue(collection.size) }
            put("contains") {
                if (it == null || it.size > 1) {
                    throw IllegalStateException("contains expect's a single parameter")
                }
                @Suppress("UNCHECKED_CAST")
                ModelValue(collection.contains(it[0] as T))
            }
        }
    }

    override fun getValue(key: String): DynamicValue<*>? {
        return props[key]?.let { return it(null).value }
    }

    override fun <T : KTEValue> getIterable(key: String): ModelIterable<T>? {
        throw UnresolvedValueException("property $key doesn't exist on collection")
    }

    override fun getFunction(key: String): ((List<Any>) -> ModelValue)? {
        return props[key]
    }

    override fun getObject(key: String): TemplateModel? {
        throw UnresolvedValueException("property $key doesn't exist on collection")
    }

}

class ModelObjectImpl : MutableTemplateModel {

    private val container: MutableMap<String, Any> = hashMapOf()

    override fun putValue(key: String, value: DynamicValue<*>) {
        container[key] = value
    }

    override fun putObject(key: String, obj: TemplateModel) {
        container[key] = obj
    }

    override fun putFunction(key: String, block: (parameters: List<Any>) -> ModelValue) {
        container[key] = block
    }

    override fun <T : KTEValue> putIterable(key: String, value: ModelIterable<T>) {
        container[key] = value
    }

    @Suppress("UNCHECKED_CAST")
    override fun getFunction(key: String): ((List<Any>) -> ModelValue)? {
        return container[key]?.let { it as ((List<Any>) -> ModelValue) }
    }

    override fun getObject(key: String): TemplateModel? {
        return container[key]?.let { it as? TemplateModel }
    }

    override fun getValue(key: String): DynamicValue<*>? {
        return container[key]?.let { it as DynamicValue<*> }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : KTEValue> getIterable(key: String): ModelIterable<T>? {
        return container[key]?.let { it as? ModelIterable<T> }
    }

    override fun removeKey(key: String) {
        container.remove(key)
    }

}

class ScopedModelObject(
    private val parent: MutableTemplateModel,
    private val current: MutableTemplateModel = ModelObjectImpl(),
) : MutableTemplateModel by current {
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