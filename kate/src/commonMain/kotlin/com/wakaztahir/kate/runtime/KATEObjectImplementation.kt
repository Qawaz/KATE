package com.wakaztahir.kate.runtime

import com.wakaztahir.kate.model.BooleanValue
import com.wakaztahir.kate.model.StringValue
import com.wakaztahir.kate.model.model.*

object KATEObjectImplementation {

    val propertyMap by lazy { hashMapOf<String, KATEValue>().apply { putObjectFunctions() } }

    private fun HashMap<String, KATEValue>.putObjectFunctions() {
        with(KATEValueImplementation){ putObjectFunctions() }
        put("get", object : KATEFunction() {
            override fun invoke(model: KATEObject, invokedOn: KATEValue, parameters: List<ReferencedValue>): KATEValue {
                val value = invokedOn as? KATEObject
                require(value != null) { "invoked on object cannot be null" }
                val required = parameters.getOrNull(0)?.asNullablePrimitive(model)?.value?.let { it as String }
                require(required != null) { "get requires a single parameter" }
                return value.get(required) ?: KATEUnit
            }

            override fun toString(): String = "get() : KATEValue"
        })
        put("getName", object : KATEFunction() {
            override fun invoke(model: KATEObject, invokedOn: KATEValue, parameters: List<ReferencedValue>): KATEValue {
                val value = invokedOn as? KATEObject
                require(value != null) { "invoked on object cannot be null" }
                return StringValue(value.objectName)
            }

            override fun toString(): String = "getName() : string"
        })
        put("getType", object : KATEFunction() {
            override fun invoke(model: KATEObject, invokedOn: KATEValue, parameters: List<ReferencedValue>): KATEValue {
                return StringValue("object")
            }

            override fun toString(): String = "getType() : string"
        })
        put("getKeys", object : KATEFunction() {
            override fun invoke(model: KATEObject, invokedOn: KATEValue, parameters: List<ReferencedValue>): KATEValue {
                val value = invokedOn as? KATEObject
                require(value != null) { "invoked on object cannot be null" }
                return KATEListImpl(value.contained.keys.map { StringValue(it) })
            }

            override fun toString(): String = "getKeys() : List<string>"

        })
        put("getValues", object : KATEFunction() {
            override fun invoke(model: KATEObject, invokedOn: KATEValue, parameters: List<ReferencedValue>): KATEValue {
                val value = invokedOn as? KATEObject
                require(value != null) { "invoked on object cannot be null" }
                return KATEListImpl(value.contained.values.toList())
            }

            override fun toString(): String = "getValues() : List<KTEValue>"

        })
        put("contains", object : KATEFunction() {
            override fun invoke(model: KATEObject, invokedOn: KATEValue, parameters: List<ReferencedValue>): KATEValue {
                val value = invokedOn.asNullableObject(model)
                val required = parameters.getOrNull(0)?.asNullablePrimitive(model)?.value?.let { it as String }
                require(value != null) { "invoked on object cannot be null" }
                require(required != null) { "contains require a single parameter by the name of object" }
                return BooleanValue(value.contains(required))
            }

            override fun toString(): String = "contains(name : string) : boolean"

        })
        put("containsInAncestors", object : KATEFunction() {
            override fun invoke(model: KATEObject, invokedOn: KATEValue, parameters: List<ReferencedValue>): KATEValue {
                val value = invokedOn.asNullableObject(model)
                val required = parameters.getOrNull(0)?.asNullablePrimitive(model)?.value?.let { it as String }
                require(value != null) { "invoked on object cannot be null" }
                require(required != null) { "containsInAncestors require a single parameter by the name of object" }
                return BooleanValue(value.containsInAncestors(required))
            }

            override fun toString(): String = "containsInAncestors(name : string) : boolean"

        })
        put("rename", object : KATEFunction() {
            override fun invoke(model: KATEObject, invokedOn: KATEValue, parameters: List<ReferencedValue>): KATEValue {
                val value = invokedOn as? MutableKATEObject
                val key = parameters.getOrNull(0)?.asNullablePrimitive(model)?.value?.let { it as String }
                val other = parameters.getOrNull(1)?.asNullablePrimitive(model)?.value?.let { it as String }
                require(value != null) { "invoked on object cannot be null" }
                require(key != null) { "rename requires the 1st key parameter" }
                require(other != null) { "rename requires the 2nd replace parameter" }
                value.rename(key, other)
                return KATEUnit
            }

            override fun toString(): String = "delete(name : string) : unit"
        })
        put("delete", object : KATEFunction() {
            override fun invoke(model: KATEObject, invokedOn: KATEValue, parameters: List<ReferencedValue>): KATEValue {
                val value = invokedOn as? MutableKATEObject
                val required = parameters.getOrNull(0)?.asNullablePrimitive(model)?.value?.let { it as String }
                require(value != null) { "invoked on object cannot be null" }
                require(required != null) { "exists require a single parameter by the name of object" }
                value.removeKey(required)
                return KATEUnit
            }

            override fun toString(): String = "delete(name : string) : unit"
        })
    }

}