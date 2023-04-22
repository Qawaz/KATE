package com.wakaztahir.kate.runtime

import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.*

object KATEListImplementation {

    val propertyMap by lazy { hashMapOf<String, KATEValue>().apply { putObjectFunctions() } }

    fun HashMap<String, KATEValue>.putObjectFunctions() {
        with(KATEValueImplementation) { putObjectFunctions() }
        put("get", object : KATEFunction(KATEType.Any, KATEType.Int) {
            override fun invoke(
                model: KATEObject,
                path: List<ModelReference>,
                pathIndex: Int,
                parent: ReferencedOrDirectValue?,
                invokedOn: ReferencedOrDirectValue,
                parameters: List<KATEValue>
            ): KATEValue {
                val index = parameters.getOrNull(0)?.asNullablePrimitive(model)?.value as? Int
                require(index != null) {
                    "list.get(int) expects a single Int parameter instead of ${parameters.size}"
                }
                return (invokedOn.asNullableList(model)!!.collection)[index]
            }

            override fun toString(): String = "get(number) : KTEValue"

        })
        put("size", object : KATEFunction(KATEType.Int) {
            override fun invoke(
                model: KATEObject,
                path: List<ModelReference>,
                pathIndex: Int,
                parent: ReferencedOrDirectValue?,
                invokedOn: ReferencedOrDirectValue,
                parameters: List<KATEValue>
            ): KATEValue {
                return IntValue(invokedOn.asNullableList(model)!!.collection.size)
            }

            override fun toString(): String = "size() : Int"
        })
        put("contains", object : KATEFunction(KATEType.Boolean, KATEType.Any) {
            override fun invoke(
                model: KATEObject,
                path: List<ModelReference>,
                pathIndex: Int,
                parent: ReferencedOrDirectValue?,
                invokedOn: ReferencedOrDirectValue,
                parameters: List<KATEValue>
            ): KATEValue {
                return BooleanValue(invokedOn.asNullableList(model)!!.collection.containsAll(parameters))
            }

            override fun toString(): String = "contains(parameter) : Boolean"

        })
        put("indexOf", object : KATEFunction(KATEType.Int, KATEType.Any) {
            override fun invoke(
                model: KATEObject,
                path: List<ModelReference>,
                pathIndex: Int,
                parent: ReferencedOrDirectValue?,
                invokedOn: ReferencedOrDirectValue,
                parameters: List<KATEValue>
            ): KATEValue {
                require(parameters.size == 1) {
                    "indexOf requires a single parameter"
                }
                return IntValue(invokedOn.asNullableList(model)!!.collection.indexOf(parameters[0]))
            }

            override fun toString(): String = "indexOf(parameter) : Int"

        })
        put("joinToString",
            object : KATEFunction(KATEType.String, KATEType.String, KATEType.Function(KATEType.String, emptyList())) {
                override fun invoke(
                    model: KATEObject,
                    path: List<ModelReference>,
                    pathIndex: Int,
                    parent: ReferencedOrDirectValue?,
                    invokedOn: ReferencedOrDirectValue,
                    parameters: List<KATEValue>
                ): KATEValue {
                    val list = invokedOn.asNullableList(model)
                    require(list != null) { "list is null" }
                    val separator =
                        parameters.getOrNull(0)?.asNullablePrimitive(model)?.value?.let { it as? String } ?: ","
                    val func = parameters.getOrNull(1)?.asNullableFunction(model)
                    return StringValue(list.collection.joinToString(separator) {
                        func?.invoke(model, emptyList(), 0,null, it, listOf(it))?.toString()
                            ?: it.toString()
                    })
                }

                override fun toString(): String = "joinToString(separator : string?) : String"

            })
    }

}

