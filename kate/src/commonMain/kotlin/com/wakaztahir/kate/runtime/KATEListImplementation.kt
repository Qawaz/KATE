package com.wakaztahir.kate.runtime

import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.*

object KATEListImplementation {

    val propertyMap by lazy { hashMapOf<String, KATEValue>().apply { putObjectFunctions() } }

    private val KATEValue.kateList: KATEList<*>
        get() {
            return this as? KATEList<*>
                ?: throw IllegalStateException("value of type ${getKnownKATEType()} is not a list")
        }

    fun HashMap<String, KATEValue>.putObjectFunctions() {
        with(KATEValueImplementation) { putObjectFunctions() }
        put("get", object : KATEFunction(KATEType.Any, KATEType.Int) {
            override fun invoke(
                model: KATEObject,
                invokedOn: KATEValue,
                explicitType: KATEType?,
                parameters: List<ReferencedOrDirectValue>
            ): ReferencedOrDirectValue {
                val index = parameters.getOrNull(0)?.asNullablePrimitive()?.value as? Int
                require(index != null) {
                    "list.get(int) expects a single Int parameter instead of ${parameters.size}"
                }
                val list = invokedOn.kateList
                val value = (list.collection)[index]
                return list.getExplicitType(index)?.let { ValueWithType(value, it) } ?: value
            }

            override fun toString(): String = "get(number) : KTEValue"

        })
        put("size", object : KATEFunction(KATEType.Int) {
            override fun invoke(
                model: KATEObject,
                invokedOn: KATEValue,
                explicitType: KATEType?,
                parameters: List<ReferencedOrDirectValue>
            ): ReferencedOrDirectValue {
                return IntValue(invokedOn.kateList.collection.size)
            }

            override fun toString(): String = "size() : Int"
        })
        put("contains", object : KATEFunction(KATEType.Boolean, KATEType.Any) {
            override fun invoke(
                model: KATEObject,
                invokedOn: KATEValue,
                explicitType: KATEType?,
                parameters: List<ReferencedOrDirectValue>
            ): ReferencedOrDirectValue {
                return BooleanValue(invokedOn.kateList.collection.containsAll(parameters))
            }

            override fun toString(): String = "contains(parameter) : Boolean"

        })
        put("indexOf", object : KATEFunction(KATEType.Int, KATEType.Any) {
            override fun invoke(
                model: KATEObject,
                invokedOn: KATEValue,
                explicitType: KATEType?,
                parameters: List<ReferencedOrDirectValue>
            ): ReferencedOrDirectValue {
                require(parameters.size == 1) {
                    "indexOf requires a single parameter"
                }
                return IntValue(invokedOn.kateList.collection.indexOf(parameters[0]))
            }

            override fun toString(): String = "indexOf(parameter) : Int"

        })
        put("joinToString",
            object : KATEFunction(KATEType.String, KATEType.String, KATEType.Function(KATEType.String, emptyList())) {
                override fun invoke(
                    model: KATEObject,
                    invokedOn: KATEValue,
                    explicitType: KATEType?,
                    parameters: List<ReferencedOrDirectValue>
                ): ReferencedOrDirectValue {
                    val list = invokedOn.kateList
                    val separator =
                        parameters.getOrNull(0)?.asNullablePrimitive()?.value?.let { it as? String } ?: ","
                    val func = parameters.getOrNull(1)?.asNullableFunction()
                    return StringValue(list.collection.joinToString(separator) {
                        func?.invoke(model, invokedOn = it, explicitType = null, parameters = listOf(it))?.toString()
                            ?: it.toString()
                    })
                }

                override fun toString(): String = "joinToString(separator : string?) : String"

            })
    }

}

