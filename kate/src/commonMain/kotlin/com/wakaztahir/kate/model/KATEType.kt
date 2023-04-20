package com.wakaztahir.kate.model

sealed class KATEType(val isNullable: kotlin.Boolean) {

    abstract fun getNonNullableKATEType(): kotlin.String

    abstract fun equalsWithoutNullable(other: KATEType): kotlin.Boolean

    override fun toString(): kotlin.String =
        if (!isNullable) getNonNullableKATEType() else (getNonNullableKATEType() + '?')

    override fun equals(other: kotlin.Any?): kotlin.Boolean =
        (other?.let { it as? KATEType }?.let { equalsWithoutNullable(it) && isNullable == it.isNullable } ?: false)

    override fun hashCode(): kotlin.Int =
        this::class.hashCode() * isNullable.hashCode()

    fun satisfyOrThrow(type : KATEType) {
        if (!type.isNullable && isNullable) {
            throw Throwable("type $this is nullable and cannot satisfy non-nullable type $type")
        }
        if (!equalsWithoutNullable(type)) {
            throw Throwable("type $this cannot satisfy type $type")
        }
    }

    class Any(isNullable: kotlin.Boolean) : KATEType(isNullable) {

        override fun getNonNullableKATEType(): kotlin.String = "any"

        override fun equalsWithoutNullable(other: KATEType): kotlin.Boolean = other is Any

        override fun equals(other: kotlin.Any?): kotlin.Boolean {
            if (other === this) return true
            return other is Any && isNullable == other.isNullable
        }

        override fun hashCode(): kotlin.Int {
            return this::class.hashCode() * isNullable.hashCode()
        }

    }

    class Char(isNullable: kotlin.Boolean) : KATEType(isNullable) {

        override fun getNonNullableKATEType(): kotlin.String = "char"

        override fun equalsWithoutNullable(other: KATEType): kotlin.Boolean = other is Char

    }

    class String(isNullable: kotlin.Boolean) : KATEType(isNullable) {

        override fun getNonNullableKATEType(): kotlin.String = "string"

        override fun equalsWithoutNullable(other: KATEType): kotlin.Boolean = other is String

    }

    class Int(isNullable: kotlin.Boolean) : KATEType(isNullable) {

        override fun getNonNullableKATEType(): kotlin.String = "int"

        override fun equalsWithoutNullable(other: KATEType): kotlin.Boolean = other is Int

    }

    class Double(isNullable: kotlin.Boolean) : KATEType(isNullable) {

        override fun getNonNullableKATEType(): kotlin.String = "double"

        override fun equalsWithoutNullable(other: KATEType): kotlin.Boolean = other is Double

    }

    class Long(isNullable: kotlin.Boolean) : KATEType(isNullable) {

        override fun getNonNullableKATEType(): kotlin.String = "long"

        override fun equalsWithoutNullable(other: KATEType): kotlin.Boolean = other is Long

    }

    class Boolean(isNullable: kotlin.Boolean) : KATEType(isNullable) {

        override fun getNonNullableKATEType(): kotlin.String = "boolean"

        override fun equalsWithoutNullable(other: KATEType): kotlin.Boolean = other is Boolean

    }

    open class List(isNullable: kotlin.Boolean) : KATEType(isNullable) {

        override fun getNonNullableKATEType(): kotlin.String = "list"

        override fun equalsWithoutNullable(other: KATEType): kotlin.Boolean = other is List

    }

    class MutableList(isNullable: kotlin.Boolean) : List(isNullable) {

        override fun getNonNullableKATEType(): kotlin.String = "mutable_list"

        override fun equalsWithoutNullable(other: KATEType): kotlin.Boolean = other is MutableList

    }

    class Object(isNullable: kotlin.Boolean) : KATEType(isNullable){

        override fun getNonNullableKATEType(): kotlin.String = "object"

        override fun equalsWithoutNullable(other: KATEType): kotlin.Boolean = other is Object

    }

}