package com.wakaztahir.kate.model

import com.wakaztahir.kate.model.model.KATEValue

sealed class KATEType {

    abstract fun getPlaceholderName(): kotlin.String

    abstract fun getKATEType(): kotlin.String

    abstract fun satisfiedBy(valueOfType: KATEType): kotlin.Boolean

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

        override fun satisfiedBy(valueOfType: KATEType): kotlin.Boolean = actual.satisfiedBy(valueOfType)

    }

    class TypeWithMetadata(val actual: KATEType, val meta: Map<kotlin.String, KATEValue>) : KATEType() {

        override fun getPlaceholderName(): kotlin.String = actual.getPlaceholderName()

        private fun metaProps() =
            meta.entries.joinToString(separator = ",", prefix = "`", postfix = "`") { it.key + "=" + it.value }

        override fun getKATEType(): kotlin.String = actual.getKATEType() + metaProps()

        override fun satisfiedBy(valueOfType: KATEType): kotlin.Boolean = actual.satisfiedBy(valueOfType)

    }

    object Any : KATEType() {

        override fun getPlaceholderName(): kotlin.String = "any"

        override fun getKATEType(): kotlin.String = "any"

        override fun satisfiedBy(valueOfType: KATEType): kotlin.Boolean =
            valueOfType !is NullableKateType && valueOfType !is Unit

    }

    object Unit : KATEType() {

        override fun getPlaceholderName(): kotlin.String = "unit"

        override fun getKATEType(): kotlin.String = "unit"

        override fun satisfiedBy(valueOfType: KATEType): kotlin.Boolean = valueOfType is Unit

    }

    object Char : KATEType() {

        override fun getPlaceholderName(): kotlin.String = "char"

        override fun getKATEType(): kotlin.String = "char"

        override fun satisfiedBy(valueOfType: KATEType): kotlin.Boolean = valueOfType is Char

    }

    object String : KATEType() {

        override fun getPlaceholderName(): kotlin.String = "string"

        override fun getKATEType(): kotlin.String = "string"

        override fun satisfiedBy(valueOfType: KATEType): kotlin.Boolean = valueOfType is String

    }

    object Int : KATEType() {

        override fun getPlaceholderName(): kotlin.String = "int"

        override fun getKATEType(): kotlin.String = "int"

        override fun satisfiedBy(valueOfType: KATEType): kotlin.Boolean = valueOfType is Int

    }

    object Double : KATEType() {

        override fun getPlaceholderName(): kotlin.String = "double"

        override fun getKATEType(): kotlin.String = "double"

        override fun satisfiedBy(valueOfType: KATEType): kotlin.Boolean = valueOfType is Double

    }

    object Long : KATEType() {

        override fun getPlaceholderName(): kotlin.String = "long"

        override fun getKATEType(): kotlin.String = "long"

        override fun satisfiedBy(valueOfType: KATEType): kotlin.Boolean = valueOfType is Long

    }

    object Boolean : KATEType() {

        override fun getPlaceholderName(): kotlin.String = "boolean"

        override fun getKATEType(): kotlin.String = "boolean"

        override fun satisfiedBy(valueOfType: KATEType): kotlin.Boolean = valueOfType is Boolean

    }

    open class List(val itemType: KATEType) : KATEType() {

        override fun getPlaceholderName(): kotlin.String = "list"

        override fun getKATEType(): kotlin.String = "list<${itemType.getKATEType()}>"

        override fun satisfiedBy(valueOfType: KATEType): kotlin.Boolean = valueOfType is List

    }

    class MutableList(itemType: KATEType) : List(itemType) {

        override fun getPlaceholderName(): kotlin.String = "mutable_list"

        override fun getKATEType(): kotlin.String = "mutable_list<${itemType.getKATEType()}>"

        override fun satisfiedBy(valueOfType: KATEType): kotlin.Boolean = valueOfType is MutableList

    }

    open class Class(val members: Map<kotlin.String, KATEType>) : KATEType() {

        override fun getPlaceholderName(): kotlin.String = "class"

        override fun getKATEType(): kotlin.String = members.entries.joinToString(
            separator = ";",
            prefix = "{",
            postfix = "}"
        ) { "${it.key}:${it.value.getKATEType()}" }

        override fun satisfiedBy(valueOfType: KATEType): kotlin.Boolean =
            valueOfType is Class && members.all {
                it.value.satisfiedBy(valueOfType.members[it.key] ?: return@all false)
            }
    }

    open class Object(val itemsType: KATEType) : KATEType() {

        override fun getPlaceholderName(): kotlin.String = "object"

        override fun getKATEType(): kotlin.String = "object<${itemsType.getKATEType()}>"

        override fun satisfiedBy(valueOfType: KATEType): kotlin.Boolean =
            valueOfType is Object && itemsType.satisfiedBy(valueOfType.itemsType)

    }

    class Function(val returnedType: KATEType, val parameterTypes: kotlin.collections.List<KATEType>?) : KATEType() {

        override fun getPlaceholderName(): kotlin.String = "function"

        private fun parametersTypes() = (parameterTypes?.joinToString(",") { it.getKATEType() }) ?: ""

        override fun getKATEType(): kotlin.String = "(${parametersTypes()}) -> ${returnedType.getKATEType()}"

        override fun satisfiedBy(valueOfType: KATEType): kotlin.Boolean =
            valueOfType is Function && valueOfType.returnedType == returnedType && valueOfType.parameterTypes == parameterTypes

    }

}