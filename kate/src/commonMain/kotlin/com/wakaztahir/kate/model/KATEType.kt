package com.wakaztahir.kate.model

sealed class KATEType {

    protected val isNullable get() = this is NullableKateType

    abstract fun getKATEType(): kotlin.String

    abstract fun equalsWithoutNullable(other: KATEType): kotlin.Boolean

    override fun toString(): kotlin.String = getKATEType()

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

    class NullableKateType(val actual : KATEType) : KATEType(){

        override fun getKATEType(): kotlin.String = actual.getKATEType() + '?'

        override fun equalsWithoutNullable(other: KATEType): kotlin.Boolean = other is Any

        override fun equals(other: kotlin.Any?): kotlin.Boolean {
            if (other === this) return true
            return other is Any && isNullable == other.isNullable
        }

        override fun hashCode(): kotlin.Int {
            return this::class.hashCode() * isNullable.hashCode()
        }

    }

    class Any : KATEType() {

        override fun getKATEType(): kotlin.String = "any"

        override fun equalsWithoutNullable(other: KATEType): kotlin.Boolean = other is Any

        override fun equals(other: kotlin.Any?): kotlin.Boolean {
            if (other === this) return true
            return other is Any && isNullable == other.isNullable
        }

        override fun hashCode(): kotlin.Int {
            return this::class.hashCode() * isNullable.hashCode()
        }

    }

    class Char : KATEType() {

        override fun getKATEType(): kotlin.String = "char"

        override fun equalsWithoutNullable(other: KATEType): kotlin.Boolean = other is Char

    }

    class String : KATEType() {

        override fun getKATEType(): kotlin.String = "string"

        override fun equalsWithoutNullable(other: KATEType): kotlin.Boolean = other is String

    }

    class Int : KATEType() {

        override fun getKATEType(): kotlin.String = "int"

        override fun equalsWithoutNullable(other: KATEType): kotlin.Boolean = other is Int

    }

    class Double : KATEType() {

        override fun getKATEType(): kotlin.String = "double"

        override fun equalsWithoutNullable(other: KATEType): kotlin.Boolean = other is Double

    }

    class Long : KATEType() {

        override fun getKATEType(): kotlin.String = "long"

        override fun equalsWithoutNullable(other: KATEType): kotlin.Boolean = other is Long

    }

    class Boolean : KATEType() {

        override fun getKATEType(): kotlin.String = "boolean"

        override fun equalsWithoutNullable(other: KATEType): kotlin.Boolean = other is Boolean

    }

    open class List : KATEType() {

        override fun getKATEType(): kotlin.String = "list"

        override fun equalsWithoutNullable(other: KATEType): kotlin.Boolean = other is List

    }

    class MutableList : List() {

        override fun getKATEType(): kotlin.String = "mutable_list"

        override fun equalsWithoutNullable(other: KATEType): kotlin.Boolean = other is MutableList

    }

    class Object : KATEType(){

        override fun getKATEType(): kotlin.String = "object"

        override fun equalsWithoutNullable(other: KATEType): kotlin.Boolean = other is Object

    }

}