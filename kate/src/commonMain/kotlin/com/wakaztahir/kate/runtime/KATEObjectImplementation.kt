package com.wakaztahir.kate.runtime

import com.wakaztahir.kate.model.BooleanValue
import com.wakaztahir.kate.model.KATEType
import com.wakaztahir.kate.model.StringValue
import com.wakaztahir.kate.model.model.*

object KATEObjectImplementation {

    val propertyMap by lazy { hashMapOf<String, KATEValue>().apply { putObjectFunctions() } }

    private val KATEValue.kateObject: KATEObject
        get() {
            return this as? KATEObject
                ?: throw IllegalStateException("value of type ${getKnownKATEType()} is not an object")
        }

    private val KATEValue.mutableKateObject: MutableKATEObject
        get() {
            return this as? MutableKATEObject
                ?: throw IllegalStateException("value of type ${getKnownKATEType()} is not a mutable object")
        }

    private fun HashMap<String, KATEValue>.putObjectFunctions() {
        with(KATEValueImplementation) { putObjectFunctions() }
        put("get", object : KATEFunction(KATEType.Any, KATEType.String) {
            override fun invoke(
                model: KATEObject,
                invokedOn: KATEValue,
                explicitType: KATEType?,
                parameters: List<ReferencedOrDirectValue>
            ): ReferencedOrDirectValue {
                val value = invokedOn.kateObject
                val required = parameters.getOrNull(0)?.asNullablePrimitive(model)?.value?.let { it as String }
                require(required != null) { "get requires a single parameter" }
                val ret = value.get(required) ?: KATEUnit
                return value.getExplicitType(required)?.let { ValueWithType(ret, it) } ?: ret
            }

            override fun toString(): String = "get() : KATEValue"
        })
        put("getName", object : KATEFunction(KATEType.String) {
            override fun invoke(
                model: KATEObject,
                invokedOn: KATEValue,
                explicitType: KATEType?,
                parameters: List<ReferencedOrDirectValue>
            ): ReferencedOrDirectValue {
                return StringValue(invokedOn.kateObject.objectName)
            }

            override fun toString(): String = "getName() : string"
        })
        put("getMetaProperty", object : KATEFunction(KATEType.String, KATEType.Any) {
            override fun invoke(
                model: KATEObject,
                invokedOn: KATEValue,
                explicitType: KATEType?,
                parameters: List<ReferencedOrDirectValue>
            ): ReferencedOrDirectValue {
                if(explicitType == null) return StringValue("")
                val param = parameters.getOrNull(0)?.getKATEValue(model)?.getKotlinValue()?.let { it as? String }
                val metaName = parameters.getOrNull(1)?.getKATEValue(model)?.getKotlinValue()?.let { it as? String }
                require(param != null) { "meta property requires two string parameters , the name property and name of meta property" }
                require(metaName != null){ "meta name cannot be null" }
                val type = ((explicitType as? KATEType.Object)?.itemsType as? KATEType.Class)
                fun KATEType.meta() = if(this is KATEType.TypeWithMetadata) meta else null
                return type?.members?.get(param)?.meta()?.get(metaName) ?: StringValue("")
            }
        })
        put("getKeys", object : KATEFunction(KATEType.List(KATEType.String)) {
            override fun invoke(
                model: KATEObject,
                invokedOn: KATEValue,
                explicitType: KATEType?,
                parameters: List<ReferencedOrDirectValue>
            ): ReferencedOrDirectValue {
                return KATEListImpl(invokedOn.kateObject.getKeys().map { StringValue(it) }, itemType = KATEType.String)
            }

            override fun toString(): String = "getKeys() : List<string>"

        })
        put("getValues", object : KATEFunction(KATEType.List(KATEType.Any)) {
            override fun invoke(
                model: KATEObject,
                invokedOn: KATEValue,
                explicitType: KATEType?,
                parameters: List<ReferencedOrDirectValue>
            ): ReferencedOrDirectValue {
                val value = invokedOn.kateObject
                return KATEListImpl(value.getValues().toList(), itemType = value.getItemsType())
            }

            override fun toString(): String = "getValues() : List<KTEValue>"

        })
        put("contains", object : KATEFunction(KATEType.Boolean, KATEType.String) {
            override fun invoke(
                model: KATEObject,
                invokedOn: KATEValue,
                explicitType: KATEType?,
                parameters: List<ReferencedOrDirectValue>
            ): ReferencedOrDirectValue {
                val value = invokedOn.kateObject
                val required = parameters.getOrNull(0)?.asNullablePrimitive(model)?.value?.let { it as String }
                require(required != null) { "contains require a single parameter by the name of object" }
                return BooleanValue(value.contains(required))
            }

            override fun toString(): String = "contains(name : string) : boolean"

        })
        put("containsInAncestors", object : KATEFunction(KATEType.Boolean, KATEType.String) {
            override fun invoke(
                model: KATEObject,
                invokedOn: KATEValue,
                explicitType: KATEType?,
                parameters: List<ReferencedOrDirectValue>
            ): ReferencedOrDirectValue {
                val value = invokedOn.kateObject
                val required = parameters.getOrNull(0)?.asNullablePrimitive(model)?.value?.let { it as String }
                require(required != null) { "containsInAncestors require a single parameter by the name of object" }
                return BooleanValue(value.containsInAncestors(required))
            }

            override fun toString(): String = "containsInAncestors(name : string) : boolean"

        })
        put("rename", object : KATEFunction(KATEType.Unit, KATEType.String, KATEType.String) {
            override fun invoke(
                model: KATEObject,
                invokedOn: KATEValue,
                explicitType: KATEType?,
                parameters: List<ReferencedOrDirectValue>
            ): ReferencedOrDirectValue {
                val value = invokedOn.mutableKateObject
                val key = parameters.getOrNull(0)?.asNullablePrimitive(model)?.value?.let { it as String }
                val other = parameters.getOrNull(1)?.asNullablePrimitive(model)?.value?.let { it as String }
                require(key != null) { "rename requires the 1st key parameter" }
                require(other != null) { "rename requires the 2nd replace parameter" }
                value.rename(key, other)
                return KATEUnit
            }

            override fun toString(): String = "delete(name : string) : unit"
        })
        put("delete", object : KATEFunction(KATEType.Unit, KATEType.String) {
            override fun invoke(
                model: KATEObject,
                invokedOn: KATEValue,
                explicitType: KATEType?,
                parameters: List<ReferencedOrDirectValue>
            ): ReferencedOrDirectValue {
                val value = invokedOn.mutableKateObject
                val required = parameters.getOrNull(0)?.asNullablePrimitive(model)?.value?.let { it as String }
                require(required != null) { "exists require a single parameter by the name of object" }
                value.removeKey(required)
                return KATEUnit
            }

            override fun toString(): String = "delete(name : string) : unit"
        })
    }

}