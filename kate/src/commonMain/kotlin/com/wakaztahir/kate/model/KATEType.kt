package com.wakaztahir.kate.model

import com.wakaztahir.kate.model.model.KATEValue

sealed class KATEType {

    protected val actualType get() = if (this is NullableKateType) this.actual else this

    abstract fun getPlaceholderName(): kotlin.String

    abstract fun getKATEType(): kotlin.String

    abstract fun satisfies(type: KATEType): kotlin.Boolean

    override fun toString(): kotlin.String = getKATEType()

    override fun equals(other: kotlin.Any?): kotlin.Boolean {
        if (this === other) return true
        if (other !is KATEType) return false
        return this::class == other::class
    }

    override fun hashCode(): kotlin.Int = this::class.hashCode()


    class NullableKateType(val actual: KATEType) : KATEType() {

        override fun getPlaceholderName(): kotlin.String = actual.getPlaceholderName()

        override fun getKATEType(): kotlin.String = actual.getKATEType() + '?'

        override fun satisfies(type: KATEType): kotlin.Boolean {
            if (type !is NullableKateType) return false
            return this.actual.satisfies(type.actual)
        }

    }

    object Any : KATEType() {

        override fun getPlaceholderName(): kotlin.String = "any"

        override fun getKATEType(): kotlin.String = "any"

        override fun satisfies(type: KATEType): kotlin.Boolean = type.actualType is Any

    }

    object Unit : KATEType() {

        override fun getPlaceholderName(): kotlin.String = "unit"

        override fun getKATEType(): kotlin.String = "unit"

        override fun satisfies(type: KATEType): kotlin.Boolean = type.actualType is Unit

    }

    object Char : KATEType() {

        override fun getPlaceholderName(): kotlin.String = "char"

        override fun getKATEType(): kotlin.String = "char"

        override fun satisfies(type: KATEType): kotlin.Boolean = type.actualType.let { it is Any || it is Char }

    }

    object String : KATEType() {

        override fun getPlaceholderName(): kotlin.String = "string"

        override fun getKATEType(): kotlin.String = "string"

        override fun satisfies(type: KATEType): kotlin.Boolean = type.actualType.let { it is Any || it is String }

    }

    object Int : KATEType() {

        override fun getPlaceholderName(): kotlin.String = "int"

        override fun getKATEType(): kotlin.String = "int"

        override fun satisfies(type: KATEType): kotlin.Boolean = type.actualType.let { it is Any || it is Int }

    }

    object Double : KATEType() {

        override fun getPlaceholderName(): kotlin.String = "double"

        override fun getKATEType(): kotlin.String = "double"

        override fun satisfies(type: KATEType): kotlin.Boolean = type.actualType.let { it is Any || it is Double }

    }

    object Long : KATEType() {

        override fun getPlaceholderName(): kotlin.String = "long"

        override fun getKATEType(): kotlin.String = "long"

        override fun satisfies(type: KATEType): kotlin.Boolean = type.actualType.let { it is Any || it is Long }

    }

    object Boolean : KATEType() {

        override fun getPlaceholderName(): kotlin.String = "boolean"

        override fun getKATEType(): kotlin.String = "boolean"

        override fun satisfies(type: KATEType): kotlin.Boolean = type.actualType.let { it is Any || it is Boolean }

    }

    open class List(val itemType: KATEType) : KATEType() {

        override fun getPlaceholderName(): kotlin.String = "list"

        override fun getKATEType(): kotlin.String = "list<${itemType.getKATEType()}>"

        override fun satisfies(type: KATEType): kotlin.Boolean = type.actualType.let { it is Any || it is List }

    }

    class MutableList(itemType: KATEType) : List(itemType) {

        override fun getPlaceholderName(): kotlin.String = "mutable_list"

        override fun getKATEType(): kotlin.String = "mutable_list<${itemType.getKATEType()}>"

        override fun satisfies(type: KATEType): kotlin.Boolean = type.actualType.let { it is Any || it is List }

    }

    open class Class(val members: Map<kotlin.String, Property>) : KATEType() {

        class Property(val type: KATEType, val meta: Map<kotlin.String, KATEValue>?)

        override fun getPlaceholderName(): kotlin.String = "class"

        private fun Map<kotlin.String, KATEValue>.string() =
            entries.joinToString(separator = ",") { it.key + "=" + it.value }

        override fun getKATEType(): kotlin.String = members.entries.joinToString(
            separator = ";",
            prefix = "{",
            postfix = "}"
        ) { "${it.key}${it.value.meta?.string()?.let { "`$it`" } ?: ""}:${it.value.type.getKATEType()}" }

        override fun satisfies(type: KATEType): kotlin.Boolean =
            type.actualType.let { it is Any || (it is Class && it.members == members) }

    }

    open class Object(val itemsType: KATEType) : KATEType() {

        override fun getPlaceholderName(): kotlin.String = "object"

        override fun getKATEType(): kotlin.String = "object<${itemsType.getKATEType()}>"

        override fun satisfies(type: KATEType): kotlin.Boolean = type.actualType.let { it is Any || it is Object }

    }

    class Function(val returnedType: KATEType, val parameterTypes: kotlin.collections.List<KATEType>?) : KATEType() {

        override fun getPlaceholderName(): kotlin.String = "function"

        private fun parametersTypes() = (parameterTypes?.joinToString(",") { it.getKATEType() }) ?: ""

        override fun getKATEType(): kotlin.String = "(${parametersTypes()}) -> ${returnedType.getKATEType()}"

        override fun satisfies(type: KATEType): kotlin.Boolean = type.actualType.let {
            it is Any || (it is Function && returnedType == it.returnedType && parameterTypes == it.parameterTypes)
        }

    }

}