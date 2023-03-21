package com.wakaztahir.kte.dsl

import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.model.model.ModelList
import com.wakaztahir.kte.model.model.MutableTemplateModel
import com.wakaztahir.kte.model.model.TemplateModel

class UnresolvedValueException(message: String) : Throwable(message)

class ModelValue constructor(val value: PrimitiveValue<*>) {

    constructor(value: Int) : this(IntValue(value))

    constructor(value: Float) : this(FloatValue(value))

    constructor(value: String) : this(StringValue(value))

    constructor(value: Boolean) : this(BooleanValue(value))

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

    override fun putIterable(key: String, value: ModelList<KTEValue>) {
        container[key] = value
    }

    @Suppress("UNCHECKED_CAST")
    override fun getFunction(key: String): ((List<Any>) -> ModelValue)? {
        return container[key]?.let { it as ((List<Any>) -> ModelValue) }
    }

    override fun getObject(key: String): TemplateModel? {
        return container[key]?.let { it as? TemplateModel }
    }

    override fun getNullableObject(model: TemplateModel): TemplateModel {
        return this
    }

    override fun getValue(key: String): PrimitiveValue<*>? {
        return container[key]?.let { it as PrimitiveValue<*> }
    }

    override fun getIterable(key: String): ModelList<KTEValue>? {
        return container[key]?.let {
            @Suppress("UNCHECKED_CAST")
            it as? ModelList<KTEValue>
        }
    }

    override fun removeKey(key: String) {
        container.remove(key)
    }

    override fun toString(): String {
        fun Any.toValue(): KTEValue? {
            return (this as? PrimitiveValue<*>) ?: (this as? TemplateModel) ?: (this as? ModelList<*>)
        }
        return "{\n" + container.map { item -> "\t${item.key} : ${item.value.toValue()}" }.joinToString("\n") + "\n}"
    }

    override fun stringValue(indentationLevel: Int): String {
        fun Any.toValue(): KTEValue? {
            return (this as? PrimitiveValue<*>) ?: (this as? TemplateModel) ?: (this as? ModelList<*>)
        }

        val indent = indentation(indentationLevel)
        return "{\n" + container.map { item ->
            "\t$indent${item.key} : ${
                item.value.toValue()?.stringValue(indentationLevel + 1)
            }"
        }.joinToString("\n") + "\n$indent}"
    }

}

class ScopedModelObject(private val parent: TemplateModel) : ModelObjectImpl() {

    override fun getAnyModelDirectiveValue(directive: ModelDirective): KTEValue? {
        println("REQUIRING ${directive.pathToString()}")
        println("SCOPED VALUE : ${super.getAnyModelDirectiveValue(directive)}")
        println("PARENT VALUE : ${parent.getAnyModelDirectiveValue(directive)}")
//        println("SCOPED MODEL : ${super.stringValue(0)}")
//        println("PARENT MODEL : ${parent.stringValue(0)}")
        return super.getAnyModelDirectiveValue(directive) ?: parent.getAnyModelDirectiveValue(directive)
    }

    override fun toString(): String {
        return stringValue(0)
    }

    override fun stringValue(indentationLevel: Int): String {
        return parent.toString() + '\n' + super.stringValue(indentationLevel)
    }

}