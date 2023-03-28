package com.wakaztahir.kte.dsl

import com.wakaztahir.kte.model.ModelReference
import com.wakaztahir.kte.model.PrimitiveValue
import com.wakaztahir.kte.model.indentation
import com.wakaztahir.kte.model.model.*
import com.wakaztahir.kte.model.runtime.KTEObjectImplementation

open class ModelObjectImpl(override val objectName: String) : MutableKTEObject {

    private val container: MutableMap<String, KTEValue> = hashMapOf()

    override val contained: Map<String, KTEValue>
        get() = container

    // ----- Getters

    override fun contains(key: String): Boolean {
        return container.containsKey(key)
    }

    override fun getModelReference(reference: ModelReference): KTEValue? {
        return container[reference.name] ?: KTEObjectImplementation.propertyMap[reference.name]
    }

    // ----- Putters

    override fun putValue(key: String, value: KTEValue) {
        container[key] = value
    }

    override fun removeKey(key: String) {
        container.remove(key)
    }

    override fun toString(): String {
        if (container.isEmpty()) return "{}"
        var str = "{"
        for (item in container) {
            if(item.value == this) continue
            str += "\n\t"
            str += item.key
            str += " : "
            str += if (item.value is KTEObject) {
                item.value.toString().replace("\n", "\n\t")
            } else {
                item.value.toString()
            }
        }
        str += "\n}"
        return str
    }

    override fun compareTo(model: KTEObject, other: KTEValue): Int {
        if (other is ModelObjectImpl) {
            if (this.container.isEmpty() && other.container.isEmpty()) return -1
            if (this.container.size != other.container.size) return -1
            if (this.container != other.container) return -1
            return 0
        }
        return -1
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