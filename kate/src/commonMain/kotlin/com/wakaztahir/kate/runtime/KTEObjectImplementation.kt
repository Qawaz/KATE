package com.wakaztahir.kate.runtime

import com.wakaztahir.kate.model.BooleanValue
import com.wakaztahir.kate.model.StringValue
import com.wakaztahir.kate.model.model.*

object KTEObjectImplementation {

    val propertyMap by lazy { hashMapOf<String, KTEValue>().apply { putObjectFunctions() } }

    private fun HashMap<String, KTEValue>.putObjectFunctions() {
        put("getName", object : KTEFunction() {
            override fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue {
                val value = invokedOn as? KTEObject
                require(value != null) { "invoked on object cannot be null" }
                return StringValue(value.objectName)
            }

            override fun toString(): String = "getName() : string"
        })
        put("getType", object : KTEFunction() {
            override fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue {
                return StringValue("object")
            }

            override fun toString(): String = "getType() : string"
        })
        put("getKeys", object : KTEFunction() {
            override fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue {
                val value = invokedOn as? KTEObject
                require(value != null) { "invoked on object cannot be null" }
                return KTEListImpl(value.contained.keys.map { StringValue(it) })
            }

            override fun toString(): String = "getKeys() : List<string>"

        })
        put("getValues", object : KTEFunction() {
            override fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue {
                val value = invokedOn as? KTEObject
                require(value != null) { "invoked on object cannot be null" }
                return KTEListImpl(value.contained.values.toList())
            }

            override fun toString(): String = "getValues() : List<KTEValue>"

        })
        put("contains", object : KTEFunction() {
            override fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue {
                val value = invokedOn as? KTEObject
                val required = parameters.getOrNull(0)?.asNullablePrimitive(model)?.value?.let { it as String }
                require(value != null) { "invoked on object cannot be null" }
                require(required != null) { "exists require a single parameter by the name of object" }
                return BooleanValue(value.contains(required))
            }

            override fun toString(): String = "contains(name : string) : boolean"

        })
        put("rename", object : KTEFunction() {
            override fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue {
                val value = invokedOn as? MutableKTEObject
                val key = parameters.getOrNull(0)?.asNullablePrimitive(model)?.value?.let { it as String }
                val other = parameters.getOrNull(1)?.asNullablePrimitive(model)?.value?.let { it as String }
                require(value != null) { "invoked on object cannot be null" }
                require(key != null) { "rename requires the 1st key parameter" }
                require(other != null) { "rename requires the 2nd replace parameter" }
                value.rename(key, other)
                return KTEUnit
            }

            override fun toString(): String = "delete(name : string) : unit"
        })
        put("delete", object : KTEFunction() {
            override fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue {
                val value = invokedOn as? MutableKTEObject
                val required = parameters.getOrNull(0)?.asNullablePrimitive(model)?.value?.let { it as String }
                require(value != null) { "invoked on object cannot be null" }
                require(required != null) { "exists require a single parameter by the name of object" }
                value.removeKey(required)
                return KTEUnit
            }

            override fun toString(): String = "delete(name : string) : unit"
        })
        put("toString", object : KTEFunction() {
            override fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue {
                val obj = invokedOn.asNullableObject(model)
                require(obj != null) { "object is null" }
                return StringValue(obj.toString())
            }

            override fun toString(): String = "toString() : String"

        })
    }

}