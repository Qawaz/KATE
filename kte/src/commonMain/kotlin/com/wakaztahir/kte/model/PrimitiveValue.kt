package com.wakaztahir.kte.model

import com.wakaztahir.kte.model.model.KTEObject
import com.wakaztahir.kte.model.model.ReferencedValue
import com.wakaztahir.kte.parser.ArithmeticOperatorType
import com.wakaztahir.kte.parser.stream.DestinationStream
import kotlin.jvm.JvmInline

interface PrimitiveValue<T> : CodeGen, ReferencedValue {

    val value: T

    fun compareTo(other: PrimitiveValue<T>): Int

    fun compareOther(other: PrimitiveValue<*>): Int

    fun operate(type: ArithmeticOperatorType, value2: PrimitiveValue<T>): PrimitiveValue<*>

    fun operateOther(type: ArithmeticOperatorType, value2: PrimitiveValue<*>): PrimitiveValue<*>

    override fun asNullablePrimitive(model: KTEObject): PrimitiveValue<*>? {
        return this
    }

    override fun stringValue(indentationLevel: Int): String {
        return toString()
    }

}

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
inline fun <T> PrimitiveValue<T>.operateAny(
    operatorType: ArithmeticOperatorType,
    other: PrimitiveValue<*>
): PrimitiveValue<*> {
    (other as? PrimitiveValue<T>)?.let { return operate(operatorType, it) }
    return operateOther(operatorType, other)
}

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
inline fun <T> PrimitiveValue<T>.compareAny(other: PrimitiveValue<*>): Int {
    (other as? PrimitiveValue<T>)?.let { return compareTo(it) }
    return compareOther(other)
}

@JvmInline
value class CharValue(override val value: Char) : PrimitiveValue<Char> {

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

    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        destination.write(this)
    }

}

@JvmInline
value class IntValue(override val value: Int) : PrimitiveValue<Int> {

    override fun compareTo(other: PrimitiveValue<Int>): Int {
        return value.compareTo(other.value)
    }

    override fun compareOther(other: PrimitiveValue<*>): Int {
        if (other is DoubleValue) {
            return value.compareTo(other.value)
        } else {
            throw IllegalStateException("value of type int cannot be compared to unknown value type")
        }
    }

    override fun operate(type: ArithmeticOperatorType, value2: PrimitiveValue<Int>): PrimitiveValue<Int> {
        return IntValue(type.operate(value, value2.value))
    }

    override fun operateOther(type: ArithmeticOperatorType, value2: PrimitiveValue<*>): PrimitiveValue<*> {
        if (value2 is DoubleValue) {
            return DoubleValue(type.operate(value, value2.value))
        } else {
            throw IllegalStateException("value of type cannot be operated with an unknown value type")
        }
    }

    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        destination.write(this@IntValue)
    }

    override fun toString(): String = value.toString()

}

@JvmInline
value class DoubleValue(override val value: Double) : PrimitiveValue<Double> {

    override fun compareTo(other: PrimitiveValue<Double>): Int {
        return value.compareTo(other.value)
    }

    override fun compareOther(other: PrimitiveValue<*>): Int {
        if (other is IntValue) {
            return value.compareTo(other.value)
        } else {
            throw IllegalStateException("value of type double cannot be compared to value of unknown type")
        }
    }

    override fun operate(type: ArithmeticOperatorType, value2: PrimitiveValue<Double>): PrimitiveValue<Double> {
        return DoubleValue(type.operate(value, value2.value))
    }

    override fun operateOther(type: ArithmeticOperatorType, value2: PrimitiveValue<*>): PrimitiveValue<*> {
        if (value2 is IntValue) {
            return DoubleValue(type.operate(value, value2.value))
        } else {
            throw IllegalStateException("value of type double cannot be operated with value of unknown type")
        }
    }

    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        destination.write(this@DoubleValue)
    }

    override fun toString(): String = value.toString()

}

@JvmInline
value class BooleanValue(override val value: Boolean) : PrimitiveValue<Boolean> {

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
        throw IllegalStateException("operator '${type.char}' cannot be applied with a boolean value")
    }

    override fun operateOther(type: ArithmeticOperatorType, value2: PrimitiveValue<*>): PrimitiveValue<*> {
        throw IllegalStateException("boolean value cannot ${type.char} to any other value")
    }

    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        destination.write(this@BooleanValue)
    }

    override fun toString(): String = value.toString()

}

@JvmInline
value class StringValue(override val value: String) : PrimitiveValue<String> {

    override fun compareTo(other: PrimitiveValue<String>): Int {
        return if (value == other.value) {
            0
        } else {
            -1
        }
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

    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        destination.write(this@StringValue)
    }

    override fun toString(): String = value

}