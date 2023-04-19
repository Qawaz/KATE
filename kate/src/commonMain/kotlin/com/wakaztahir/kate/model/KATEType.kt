package com.wakaztahir.kate.model

sealed class KATEType(val value: kotlin.String) {

    object Int : KATEType("int")
    object Double : KATEType("double")
    object Char : KATEType("char")
    object String : KATEType("string")
    object Boolean : KATEType("boolean")

    class Object(private val itemType: KATEType) : KATEType("object<${itemType.value}>")
    class List(private val itemType: KATEType) : KATEType("list<${itemType.value}>")

}