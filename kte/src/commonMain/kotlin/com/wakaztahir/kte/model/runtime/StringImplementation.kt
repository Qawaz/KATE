package com.wakaztahir.kte.model.runtime

import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.model.model.*

object StringImplementation {

    val propertyMap by lazy { hashMapOf<String, KTEValue>().apply { putObjectFunctions() } }

    private fun HashMap<String, KTEValue>.putObjectFunctions() {
        put("getType", object : KTEFunction() {
            override fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue {
                return StringValue("string")
            }
            override fun toString(): String = "getType() : string"
        })
        put("get", object : KTEFunction() {
            override fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue {
                val string = invokedOn.let { it as? StringValue }?.value
                val index = parameters.getOrNull(0)?.asNullablePrimitive(model)?.value?.let { it as? Int }
                require(string != null) { "string value is null" }
                require(index != null) { "String.get(Int) expects a single parameter to get the value of string" }
                return CharValue(string[index])
            }

            override fun toString(): String = "get(int) : chat"
        })
        put("size", object : KTEFunction() {
            override fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue {
                val string = invokedOn.let { it as? StringValue }?.value
                require(string != null) { "string value is null" }
                return IntValue(string.length)
            }

            override fun toString(): String = "size() : int"
        })
        put("toInt", object : KTEFunction() {
            override fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue {
                val string = invokedOn.let { it as? StringValue }?.value
                require(string != null) { "string value is null" }
                return string.toIntOrNull()?.let { IntValue(it) } ?: KTEUnit
            }

            override fun toString(): String = "toInt() : int"
        })
        put("toDouble", object : KTEFunction() {
            override fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue {
                val string = invokedOn.let { it as? StringValue }?.value
                require(string != null) { "string value is null" }
                return string.toDoubleOrNull()?.let { DoubleValue(it) } ?: KTEUnit
            }

            override fun toString(): String = "toDouble() : double"
        })
        put("substring", object : KTEFunction() {
            override fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue {
                val string = invokedOn.let { it as? StringValue }?.value
                require(string != null) { "string value is null" }
                val firstIndex = parameters.getOrNull(0)?.asNullablePrimitive(model)?.value?.let { it as Int }
                val secondIndex = parameters.getOrNull(1)?.asNullablePrimitive(model)?.value?.let { it as Int }
                require(firstIndex != null && secondIndex != null) {
                    "substring requires two parameters of type int"
                }
                return StringValue(string.substring(firstIndex, secondIndex))
            }

            override fun toString(): String = "substring(int,int) : string"
        })
        put("uppercase", object : KTEFunction() {
            override fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue {
                val string = invokedOn.let { it as? StringValue }?.value
                require(string != null) { "string value is null" }
                return StringValue(string.uppercase())
            }

            override fun toString(): String = "uppercase() : string"
        })
        put("lowercase", object : KTEFunction() {
            override fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue {
                val string = invokedOn.let { it as? StringValue }?.value
                require(string != null) { "string value is null" }
                return StringValue(string.lowercase())
            }

            override fun toString(): String = "lowercase() : string"
        })
        put("capitalize", object : KTEFunction() {
            override fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue {
                val string = invokedOn.let { it as? StringValue }?.value
                require(string != null) { "string value is null" }
                return StringValue(string.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() })
            }

            override fun toString(): String = "capitalize() : string"
        })
        put("decapitalize", object : KTEFunction() {
            override fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue {
                val string = invokedOn.let { it as? StringValue }?.value
                require(string != null) { "string value is null" }
                return StringValue(string.replaceFirstChar { it.lowercase() })
            }

            override fun toString(): String = "decapitalize() : string"
        })
        put("replace", object : KTEFunction() {
            override fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue {
                val string = invokedOn.let { it as? StringValue }?.value
                require(string != null) { "string value is null" }
                val find = parameters.getOrNull(0)?.asNullablePrimitive(model)?.value?.let { it as String }
                val replace = parameters.getOrNull(1)?.asNullablePrimitive(model)?.value?.let { it as String }
                require(find != null && replace != null) {
                    "replace requires two parameters of type string"
                }
                return StringValue(string.replace(find, replace))
            }

            override fun toString(): String = "replace(string,string) : string"
        })
        put("contains", object : KTEFunction() {
            override fun invoke(model: KTEObject, invokedOn: KTEValue, parameters: List<ReferencedValue>): KTEValue {
                val string = invokedOn.let { it as? StringValue }?.value
                require(string != null) { "string value is null" }
                val firstStr = parameters.getOrNull(0)?.asNullablePrimitive(model)?.value?.let { it as String }
                require(firstStr != null) {
                    "contains requires a string parameter"
                }
                return BooleanValue(string.contains(firstStr))
            }

            override fun toString(): String = "contains(string) : string"
        })
    }

}