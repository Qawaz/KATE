package com.wakaztahir.kte.dsl

import com.wakaztahir.kte.model.KTEValue
import com.wakaztahir.kte.model.ModelReference
import com.wakaztahir.kte.model.PrimitiveValue
import com.wakaztahir.kte.model.model.KTEList
import com.wakaztahir.kte.model.model.MutableKTEObject
import com.wakaztahir.kte.model.model.KTEObject

open class ModelObjectImpl(override val objectName: String) : MutableKTEObject() {

    private val container: MutableMap<String, KTEValue> = hashMapOf()
    override val contained: Map<String, KTEValue>
        get() = container

    // ----- Getters

    override fun contains(key: String): Boolean {
        return container.containsKey(key)
    }

    override fun getModelReference(reference: ModelReference): KTEValue? {
        return container[reference.name]
    }

    // ----- Putters

    override fun putValue(key: String, value: KTEValue) {
        container[key] = value
    }

    override fun removeKey(key: String) {
        container.remove(key)
    }

    override fun toString(): String {
        fun Any.toValue(): KTEValue? {
            return (this as? PrimitiveValue<*>) ?: (this as? KTEObject) ?: (this as? KTEList<*>)
        }
        return "{\n" + container.map { item -> "\t${item.key} : ${item.value.toValue()}" }.joinToString("\n") + "\n}"
    }

    override fun stringValue(indentationLevel: Int): String {
        fun Any.toValue(): KTEValue? {
            return (this as? PrimitiveValue<*>) ?: (this as? KTEObject) ?: (this as? KTEList<*>)
        }

        val indent = indentation(indentationLevel)
        return "{\n" + container.map { item ->
            "\t$indent${item.key} : ${
                item.value.toValue()?.stringValue(indentationLevel + 1)
            }"
        }.joinToString("\n") + "\n$indent}"
    }

}