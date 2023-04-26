package com.wakaztahir.kate.model

import com.wakaztahir.kate.model.model.KATEObject
import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.parser.ArithmeticOperatorType
import com.wakaztahir.kate.runtime.*
import kotlin.jvm.JvmInline

interface PrimitiveValue<T> : KATEValue {

    val value: T

    override fun getKnownKATEType(): KATEType

    override fun toString(): String

    fun compareTo(other: PrimitiveValue<T>): Int

    fun compareOther(other: PrimitiveValue<*>): Int

    override fun compareTo(other: KATEValue): Int {
        @Suppress("UNCHECKED_CAST")
        (other as? PrimitiveValue<T>)?.let { return compareTo(it) }
        (other as? PrimitiveValue<*>)?.let { return compareOther(it) }
        throw IllegalStateException("couldn't compare between $this and $other")
    }

    fun operate(type: ArithmeticOperatorType, value2: PrimitiveValue<T>): PrimitiveValue<*>

    fun operateOther(type: ArithmeticOperatorType, value2: PrimitiveValue<*>): PrimitiveValue<*>

    override fun operate(operator: ArithmeticOperatorType, other: KATEValue): KATEValue {
        return operateAny(operator,other as PrimitiveValue<*>)
    }

    fun operateAny(type: ArithmeticOperatorType, other: PrimitiveValue<*>): PrimitiveValue<*> {
        @Suppress("UNCHECKED_CAST")
        (other as? PrimitiveValue<T>)?.let { return operate(type, it) }
        (other as? PrimitiveValue<*>)?.let { return operateOther(type, it) }
        throw IllegalStateException("couldn't operate ${type.char} between $this and $other")
    }

    override fun asNullablePrimitive(model: KATEObject): PrimitiveValue<*>? {
        return this
    }

}

@JvmInline
value class CharValue(override val value: Char) : PrimitiveValue<Char> {

    override fun getKnownKATEType() = KATEType.Char

    override fun compareTo(other: PrimitiveValue<Char>): Int {
        return value.compareTo(other.value)
    }

    override fun compareOther(other: PrimitiveValue<*>): Int {
        throw IllegalStateException("a character value can only be compared with other character values")
    }

    override fun operateOther(type: ArithmeticOperatorType, value2: PrimitiveValue<*>): PrimitiveValue<*> {
        return when (value2) {
            is StringValue -> {
                StringValue(type.operate(value, value2.value))
            }

            is IntValue -> {
                CharValue(type.operate(value, value2.value))
            }

            else -> {
                throw IllegalStateException("operation ${type.char} is not possible between char value and an unknown value")
            }
        }
    }

    override fun operate(type: ArithmeticOperatorType, value2: PrimitiveValue<Char>): PrimitiveValue<*> {
        return IntValue(type.operate(value, value2.value))
    }

    override fun getModelReference(reference: ModelReference): KATEValue? {
        return CharImplementation.propertyMap[reference.name]
    }

    override fun toString(): String {
        return value.toString()
    }

}

@JvmInline
value class IntValue(override val value: Int) : PrimitiveValue<Int> {

    override fun getKnownKATEType() = KATEType.Int

    override fun compareTo(other: PrimitiveValue<Int>): Int {
        return value.compareTo(other.value)
    }

    override fun compareOther(other: PrimitiveValue<*>): Int {
        return when (other) {
            is DoubleValue -> {
                value.compareTo(other.value)
            }

            is LongValue -> {
                value.compareTo(other.value)
            }

            else -> {
                throw IllegalStateException("value of type int cannot be compared to unknown value type")
            }
        }
    }

    override fun operate(type: ArithmeticOperatorType, value2: PrimitiveValue<Int>): PrimitiveValue<Int> {
        return IntValue(type.operate(value, value2.value))
    }

    override fun operateOther(type: ArithmeticOperatorType, value2: PrimitiveValue<*>): PrimitiveValue<*> {
        return when (value2) {
            is DoubleValue -> {
                DoubleValue(type.operate(value, value2.value))
            }

            is LongValue -> {
                LongValue(type.operate(value, value2.value))
            }

            else -> {
                throw IllegalStateException("value of type cannot be operated with an unknown value type")
            }
        }
    }

    override fun getModelReference(reference: ModelReference): KATEValue? {
        return IntImplementation.propertyMap[reference.name]
    }

    override fun toString(): String = value.toString()

}

@JvmInline
value class DoubleValue(override val value: Double) : PrimitiveValue<Double> {

    override fun getKnownKATEType() = KATEType.Double

    override fun compareTo(other: PrimitiveValue<Double>): Int {
        return value.compareTo(other.value)
    }

    override fun compareOther(other: PrimitiveValue<*>): Int {
        return when (other) {
            is IntValue -> {
                value.compareTo(other.value)
            }

            is LongValue -> {
                value.compareTo(other.value)
            }

            else -> {
                throw IllegalStateException("value of type double cannot be compared to value of unknown type")
            }
        }
    }

    override fun operate(type: ArithmeticOperatorType, value2: PrimitiveValue<Double>): PrimitiveValue<Double> {
        return DoubleValue(type.operate(value, value2.value))
    }

    override fun operateOther(type: ArithmeticOperatorType, value2: PrimitiveValue<*>): PrimitiveValue<*> {
        return when (value2) {
            is IntValue -> {
                DoubleValue(type.operate(value, value2.value))
            }

            is LongValue -> {
                DoubleValue(type.operate(value, value2.value))
            }

            else -> {
                throw IllegalStateException("value of type double cannot be operated with value of unknown type")
            }
        }
    }

    override fun getModelReference(reference: ModelReference): KATEValue? {
        return DoubleImplementation.propertyMap[reference.name]
    }

    override fun toString(): String = value.toString()

}

@JvmInline
value class LongValue(override val value: Long) : PrimitiveValue<Long> {

    override fun getKnownKATEType() = KATEType.Long

    override fun compareTo(other: PrimitiveValue<Long>): Int {
        return value.compareTo(other.value)
    }

    override fun compareOther(other: PrimitiveValue<*>): Int {
        return when (other) {
            is IntValue -> {
                value.compareTo(other.value)
            }

            is DoubleValue -> {
                value.compareTo(other.value)
            }

            else -> {
                throw IllegalStateException("value of type double cannot be compared to value of unknown type")
            }
        }
    }

    override fun operate(type: ArithmeticOperatorType, value2: PrimitiveValue<Long>): PrimitiveValue<Long> {
        return LongValue(type.operate(value, value2.value))
    }

    override fun operateOther(type: ArithmeticOperatorType, value2: PrimitiveValue<*>): PrimitiveValue<*> {
        return when (value2) {
            is IntValue -> {
                LongValue(type.operate(value, value2.value))
            }

            is DoubleValue -> {
                DoubleValue(type.operate(value, value2.value))
            }

            else -> {
                throw IllegalStateException("value of type double cannot be operated with value of unknown type")
            }
        }
    }

    override fun getModelReference(reference: ModelReference): KATEValue? {
        return LongImplementation.propertyMap[reference.name]
    }

    override fun toString(): String = value.toString()

}

@JvmInline
value class BooleanValue(override val value: Boolean) : PrimitiveValue<Boolean> {

    override fun getKnownKATEType() = KATEType.Boolean

    override fun compareTo(other: PrimitiveValue<Boolean>): Int {
        return if (value == other.value) {
            0
        } else {
            -1
        }
    }

    override fun compareOther(other: PrimitiveValue<*>): Int {
        throw IllegalStateException("boolean value cannot be compared to any other value")
    }

    override fun operate(type: ArithmeticOperatorType, value2: PrimitiveValue<Boolean>): PrimitiveValue<Boolean> {
        return BooleanValue(type.operate(value,value2.value))
    }

    override fun operateOther(type: ArithmeticOperatorType, value2: PrimitiveValue<*>): PrimitiveValue<*> {
        throw IllegalStateException("boolean value cannot ${type.char} to any other value")
    }

    override fun getModelReference(reference: ModelReference): KATEValue? {
        return BooleanImplementation.propertyMap[reference.name]
    }

    override fun toString(): String = value.toString()

}

@JvmInline
value class StringValue(override val value: String) : PrimitiveValue<String> {

    override fun getKnownKATEType() = KATEType.String

    override fun compareTo(other: PrimitiveValue<String>): Int {
        return if (value == other.value) {
            0
        } else {
            -1
        }
    }

    override fun getModelReference(reference: ModelReference): KATEValue? {
        return StringImplementation.propertyMap[reference.name]
    }

    override fun compareOther(other: PrimitiveValue<*>): Int {
        throw IllegalStateException("string value can only be compared to a string")
    }

    override fun operate(type: ArithmeticOperatorType, value2: PrimitiveValue<String>): PrimitiveValue<String> {
        if (type == ArithmeticOperatorType.Plus) {
            return StringValue(value + value2.value)
        } else {
            throw IllegalStateException("operator '${type.char}' cannot be applied with a string value")
        }
    }

    override fun operateOther(type: ArithmeticOperatorType, value2: PrimitiveValue<*>): PrimitiveValue<*> {
        return when (value2) {
            is IntValue -> {
                StringValue(type.operate(value, value2.value))
            }

            is DoubleValue -> {
                StringValue(type.operate(value, value2.value))
            }

            is CharValue -> {
                StringValue(type.operate(value, value2.value))
            }

            else -> {
                throw IllegalStateException("operator '${type.char}' cannot be applied with an unknown value")
            }
        }
    }

    override fun toString(): String = value

}