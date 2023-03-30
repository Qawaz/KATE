package com.wakaztahir.kate.dsl

import com.wakaztahir.kate.model.ModelReference
import com.wakaztahir.kate.model.model.*
import com.wakaztahir.kate.runtime.KTEObjectImplementation

open class ModelObjectImpl(override var objectName: String, override val parent: KTEObject? = null) : MutableKTEObject {

    private val container: MutableMap<String, KTEValue> by lazy { hashMapOf() }

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

    override fun changeName(name: String) {
        this.objectName = name
    }

    override fun rename(key: String, other: String) {
        val item = container[key]
        if (item != null) {
            container.remove(key)
            if (item is MutableKTEObject) item.changeName(other)
            container[other] = item
        }
    }

    override fun removeKey(key: String) {
        container.remove(key)
    }

    override fun removeAll() {
        container.clear()
    }

    override fun toString(): String {
        if (container.isEmpty()) return "{}"
        var str = "{"
        for (item in container) {
            if (item.value == this) continue
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

}