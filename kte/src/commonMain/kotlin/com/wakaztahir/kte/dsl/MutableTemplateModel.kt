package com.wakaztahir.kte.dsl

import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.model.model.MutableTemplateModel
import com.wakaztahir.kte.model.model.TemplateModel

class UnresolvedValueException(message: String) : Throwable(message)

class ModelValue constructor(val value: PrimitiveValue<*>) {

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

    override fun getValue(key: String): PrimitiveValue<*>? {
        return props[key]?.let { return it(null).value }
    }

    override fun getIterable(key: String): ModelIterable<KTEValue>? {
        return null
    }

    override fun getFunction(key: String): ((List<Any>) -> ModelValue)? {
        return props[key]
    }

    override fun getObject(key: String): TemplateModel? {
        return null
    }

    override fun toString(): String {
        return '[' + collection.joinToString(",") + ']'
    }

}

open class ModelObjectImpl : MutableTemplateModel {

    private val container: MutableMap<String, Any> = hashMapOf()

    override fun putValue(key: String, value: PrimitiveValue<*>) {
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

    override fun getObject(model: TemplateModel): TemplateModel {
        TODO("Not yet implemented")
    }

    override fun getValue(key: String): PrimitiveValue<*>? {
        return container[key]?.let { it as PrimitiveValue<*> }
    }

    override fun getValue(model: TemplateModel): PrimitiveValue<*> {
        throw UnresolvedValueException("object is not a primitive")
    }

    override fun getIterable(key: String): ModelIterable<KTEValue>? {
        return container[key]?.let {
            @Suppress("UNCHECKED_CAST")
            it as? ModelIterable<KTEValue>
        }
    }

    override fun getIterable(model: TemplateModel): ModelIterable<KTEValue> {
        throw UnresolvedValueException("object is not iterable")
    }

    override fun removeKey(key: String) {
        container.remove(key)
    }

    override fun toString(): String {
        fun Any.toValue(): KTEValue? {
            return (this as? PrimitiveValue<*>) ?: (this as? TemplateModel) ?: (this as? ModelIterable<*>)
        }
        return "{\n" + container.map { item -> "\t${item.key} : ${item.value.toValue()}" }.joinToString("\n") + "\n}"
    }

}

class ScopedModelObject(private val parent: TemplateModel) : ModelObjectImpl() {
    override fun getAnyModelDirectiveValue(directive: ModelDirective): KTEValue? {
        return super.getAnyModelDirectiveValue(directive) ?: parent.getAnyModelDirectiveValue(directive)
    }

    override fun toString(): String {
        return parent.toString()  + '\n' + '\t' + super.toString()
    }
}