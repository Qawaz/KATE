package com.wakaztahir.kate.runtime

import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.*

object StringImplementation {

    val propertyMap by lazy { hashMapOf<String, KATEValue>().apply { putObjectFunctions() } }

    private fun HashMap<String, KATEValue>.putObjectFunctions() {
        with(KATEValueImplementation){ putObjectFunctions() }
        put("getType", object : KATEFunction() {
            override fun invoke(model: KATEObject, invokedOn: KATEValue, parameters: List<ReferencedValue>): KATEValue {
                return StringValue("string")
            }

            override fun toString(): String = "getType() : string"
        })
        put("get", object : KATEFunction() {
            override fun invoke(model: KATEObject, invokedOn: KATEValue, parameters: List<ReferencedValue>): KATEValue {
                val string = invokedOn.let { it as? StringValue }?.value
                val index = parameters.getOrNull(0)?.asNullablePrimitive(model)?.value?.let { it as? Int }
                require(string != null) { "string value is null" }
                require(index != null) { "String.get(Int) expects a single parameter to get the value of string" }
                return CharValue(string[index])
            }

            override fun toString(): String = "get(int) : chat"
        })
        put("size", object : KATEFunction() {
            override fun invoke(model: KATEObject, invokedOn: KATEValue, parameters: List<ReferencedValue>): KATEValue {
                val string = invokedOn.let { it as? StringValue }?.value
                require(string != null) { "string value is null" }
                return IntValue(string.length)
            }

            override fun toString(): String = "size() : int"
        })
        put("indexOf", object : KATEFunction() {
            override fun invoke(model: KATEObject, invokedOn: KATEValue, parameters: List<ReferencedValue>): KATEValue {
                val string = invokedOn.let { it as? StringValue }?.value
                val find = parameters.getOrNull(0)?.asNullablePrimitive(model)?.value?.let { it as String }
                require(find != null) { "indexOf requires a single string parameter" }
                require(string != null) { "string value is null" }
                return IntValue(string.indexOf(find))
            }

            override fun toString(): String = "indexOf(str : string) : int"
        })
        put("split", object : KATEFunction() {
            override fun invoke(model: KATEObject, invokedOn: KATEValue, parameters: List<ReferencedValue>): KATEValue {
                val string = invokedOn.let { it as? StringValue }?.value
                val find = parameters.getOrNull(0)?.asNullablePrimitive(model)?.value?.let { it as String }
                require(find != null) { "split requires a single string parameter" }
                require(string != null) { "string value is null" }
                return KATEListImpl(string.split(find).map { StringValue(it) })
            }

            override fun toString(): String = "split(str : string) : List<string>"
        })
        put("toInt", object : KATEFunction() {
            override fun invoke(model: KATEObject, invokedOn: KATEValue, parameters: List<ReferencedValue>): KATEValue {
                val string = invokedOn.let { it as? StringValue }?.value
                require(string != null) { "string value is null" }
                return string.toIntOrNull()?.let { IntValue(it) } ?: KATEUnit
            }

            override fun toString(): String = "toInt() : int"
        })
        put("toDouble", object : KATEFunction() {
            override fun invoke(model: KATEObject, invokedOn: KATEValue, parameters: List<ReferencedValue>): KATEValue {
                val string = invokedOn.let { it as? StringValue }?.value
                require(string != null) { "string value is null" }
                return string.toDoubleOrNull()?.let { DoubleValue(it) } ?: KATEUnit
            }

            override fun toString(): String = "toDouble() : double"
        })
        put("substring", object : KATEFunction() {
            override fun invoke(model: KATEObject, invokedOn: KATEValue, parameters: List<ReferencedValue>): KATEValue {
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
        put("uppercase", object : KATEFunction() {
            override fun invoke(model: KATEObject, invokedOn: KATEValue, parameters: List<ReferencedValue>): KATEValue {
                val string = invokedOn.let { it as? StringValue }?.value
                require(string != null) { "string value is null" }
                return StringValue(string.uppercase())
            }

            override fun toString(): String = "uppercase() : string"
        })
        put("lowercase", object : KATEFunction() {
            override fun invoke(model: KATEObject, invokedOn: KATEValue, parameters: List<ReferencedValue>): KATEValue {
                val string = invokedOn.let { it as? StringValue }?.value
                require(string != null) { "string value is null" }
                return StringValue(string.lowercase())
            }

            override fun toString(): String = "lowercase() : string"
        })
        put("capitalize", object : KATEFunction() {
            override fun invoke(model: KATEObject, invokedOn: KATEValue, parameters: List<ReferencedValue>): KATEValue {
                val string = invokedOn.let { it as? StringValue }?.value
                require(string != null) { "string value is null" }
                return StringValue(string.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() })
            }

            override fun toString(): String = "capitalize() : string"
        })
        put("decapitalize", object : KATEFunction() {
            override fun invoke(model: KATEObject, invokedOn: KATEValue, parameters: List<ReferencedValue>): KATEValue {
                val string = invokedOn.let { it as? StringValue }?.value
                require(string != null) { "string value is null" }
                return StringValue(string.replaceFirstChar { it.lowercase() })
            }

            override fun toString(): String = "decapitalize() : string"
        })
        put("replace", object : KATEFunction() {
            override fun invoke(model: KATEObject, invokedOn: KATEValue, parameters: List<ReferencedValue>): KATEValue {
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
        put("contains", object : KATEFunction() {
            override fun invoke(model: KATEObject, invokedOn: KATEValue, parameters: List<ReferencedValue>): KATEValue {
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