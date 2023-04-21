package com.wakaztahir.kate.model

sealed class KATEType {

    protected val actualType get() = if (this is NullableKateType) this.actual else this

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

        override fun getKATEType(): kotlin.String = actual.getKATEType() + '?'

        override fun satisfies(type: KATEType): kotlin.Boolean {
            if (type !is NullableKateType) return false
            return this.actual.satisfies(type.actual)
        }

    }

    class Any : KATEType() {

        override fun getKATEType(): kotlin.String = "any"

        override fun satisfies(type: KATEType): kotlin.Boolean = type.actualType is Any

    }

    class Char : KATEType() {

        override fun getKATEType(): kotlin.String = "char"

        override fun satisfies(type: KATEType): kotlin.Boolean = type.actualType is Char

    }

    class String : KATEType() {

        override fun getKATEType(): kotlin.String = "string"

        override fun satisfies(type: KATEType): kotlin.Boolean = type.actualType is String

    }

    class Int : KATEType() {

        override fun getKATEType(): kotlin.String = "int"

        override fun satisfies(type: KATEType): kotlin.Boolean = type.actualType is Int

    }

    class Double : KATEType() {

        override fun getKATEType(): kotlin.String = "double"

        override fun satisfies(type: KATEType): kotlin.Boolean = type.actualType is Double

    }

    class Long : KATEType() {

        override fun getKATEType(): kotlin.String = "long"

        override fun satisfies(type: KATEType): kotlin.Boolean = type.actualType is Long

    }

    class Boolean : KATEType() {

        override fun getKATEType(): kotlin.String = "boolean"

        override fun satisfies(type: KATEType): kotlin.Boolean = type.actualType is Boolean

    }

    open class List : KATEType() {

        override fun getKATEType(): kotlin.String = "list"

        override fun satisfies(type: KATEType): kotlin.Boolean = type.actualType is List

    }

    class MutableList : List() {

        override fun getKATEType(): kotlin.String = "mutable_list"

        override fun satisfies(type: KATEType): kotlin.Boolean = type.actualType is MutableList

    }

    class Object : KATEType() {

        override fun getKATEType(): kotlin.String = "object"

        override fun satisfies(type: KATEType): kotlin.Boolean = type.actualType is Object

    }

}