package com.wakaztahir.kate.dsl

import com.wakaztahir.kate.model.KATEType
import com.wakaztahir.kate.model.ModelReference
import com.wakaztahir.kate.model.model.*
import com.wakaztahir.kate.runtime.KATEObjectImplementation

open class ModelObjectImpl(override var objectName: String, override val parent: MutableKATEObject? = null) :
    MutableKATEObject {

    private val container: MutableMap<String, KATEValue> by lazy { hashMapOf() }

    private val explicitTypes: MutableMap<String, KATEType> by lazy { hashMapOf() }

    override val contained: Map<String, KATEValue>
        get() = container

    override fun getKATEType(model: KATEObject): KATEType = KATEType.Object()

    override fun getKateType(model: KATEObject): String? = "object"

    // ----- Getters

    override fun get(key: String): KATEValue? {
        return container[key]
    }

    override fun getExplicitTypeInTreeUpwards(key: String): KATEType? {
        return explicitTypes[key] ?: parent?.getExplicitTypeInTreeUpwards(key)
    }

    override fun getExplicitType(key: String): KATEType? {
        return explicitTypes[key]
    }

    override fun contains(key: String): Boolean {
        return container.containsKey(key)
    }

    override fun containsInAncestors(key: String): Boolean {
        return if (contains(key)) {
            true
        } else {
            parent?.containsInAncestors(key) ?: false
        }
    }

    override fun getModelReference(reference: ModelReference): KATEValue? {
        return container[reference.name] ?: KATEObjectImplementation.propertyMap[reference.name]
    }

    override fun setValue(key: String, value: KATEValue): Boolean {
        return if (contains(key)) {
            false
        } else {
            container[key] = value
            true
        }
    }

    override fun setValueInTreeUpwardsTypeSafely(key: String, value: KATEValue): Boolean {
        return container[key]?.let { oldValue ->
            (explicitTypes[key] ?: oldValue.getKATEType(this)).let { explicitType ->
                if (!value.getKATEType(this).satisfies(explicitType)) {
                    throw IllegalStateException("variable type ${value.getKATEType(this)} does not satisfy type $explicitType")
                }
            }
            container[key] = value
            true
        } ?: parent?.setValueInTreeUpwardsTypeSafely(key, value) ?: false
    }

    // ----- Putters

    override fun putValue(key: String, value: KATEValue) {
        container[key] = value
    }

    override fun setExplicitType(key: String, type: KATEType) {
        explicitTypes[key] = type
    }

    override fun changeName(name: String) {
        this.objectName = name
    }

    override fun rename(key: String, other: String) {
        val item = container[key]
        if (item != null) {
            container.remove(key)
            if (item is MutableKATEObject) item.changeName(other)
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
            str += if (item.value is KATEObject) {
                item.value.toString().replace("\n", "\n\t")
            } else {
                item.value.toString()
            }
        }
        str += "\n}"
        return str
    }

    override fun compareTo(model: KATEObject, other: KATEValue): Int {
        if (other is ModelObjectImpl) {
            if (this.container.isEmpty() && other.container.isEmpty()) return -1
            if (this.container.size != other.container.size) return -1
            if (this.container != other.container) return -1
            return 0
        }
        return -1
    }

}