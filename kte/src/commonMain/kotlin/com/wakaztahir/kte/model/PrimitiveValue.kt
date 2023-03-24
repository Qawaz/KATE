package com.wakaztahir.kte.model

import com.wakaztahir.kte.model.model.KTEList
import com.wakaztahir.kte.dsl.UnresolvedValueException
import com.wakaztahir.kte.model.model.KTEObject
import com.wakaztahir.kte.parser.ArithmeticOperatorType
import com.wakaztahir.kte.parser.stream.LanguageDestination

interface PrimitiveValue<T> : CodeGen, ReferencedValue {

    val value: T

    operator fun compareTo(other: PrimitiveValue<T>): Int

    @Suppress("UNCHECKED_CAST")
    fun compareAny(other: PrimitiveValue<*>): Int {
        return compareTo(other as PrimitiveValue<T>)
    }

    override fun asPrimitive(model: KTEObject): PrimitiveValue<*> {
        return this
    }

    fun operate(operatorType: ArithmeticOperatorType, value2: PrimitiveValue<*>): PrimitiveValue<*> {

        if (this is StringValue || value2 is StringValue) {
            if (operatorType == ArithmeticOperatorType.Plus) {
                return StringValue(value.toString() + value2.value.toString())
            } else {
                throw IllegalStateException("operator '${operatorType.char}' cannot be applied with a string value")
            }
        }

        if (this is BooleanValue || value2 is BooleanValue) {
            throw IllegalStateException("operator '${operatorType.char}' cannot be applied with a boolean value")
        }

        val resultIsFloat: Boolean = ((this as? DoubleValue) ?: (value2 as? DoubleValue)) != null

        return if (resultIsFloat) {
            DoubleValue(
                operatorType.operate(
                    value1 = (this as? DoubleValue)?.value ?: (this as IntValue).value.toDouble(),
                    value2 = (value2 as? DoubleValue)?.value ?: (value2 as IntValue).value.toDouble()
                )
            )
        } else {
            IntValue(
                operatorType.operate(
                    value1 = (this as IntValue).value,
                    value2 = (value2 as IntValue).value
                )
            )
        }

    }

    override fun asList(model: KTEObject): KTEList<KTEValue> {
        throw UnresolvedValueException("primitive value is not iterable")
    }

    override fun asObject(model: KTEObject): KTEObject {
        throw UnresolvedValueException("primitive value is not an object")
    }

    override fun stringValue(indentationLevel: Int): String {
        return toString()
    }

}

class IntValue(override val value: Int) : PrimitiveValue<Int> {

    override fun compareTo(other: PrimitiveValue<Int>): Int {
        return value.compareTo(other.value)
    }

    override fun writeTo(model: KTEObject, destination: LanguageDestination) {
        destination.write(this@IntValue)
    }

    override fun toString(): String = value.toString()

}

class DoubleValue(override val value: Double) : PrimitiveValue<Double> {

    override fun compareTo(other: PrimitiveValue<Double>): Int {
        return value.compareTo(other.value)
    }

    override fun writeTo(model: KTEObject, destination: LanguageDestination) {
        destination.write(this@DoubleValue)
    }

    override fun toString(): String = value.toString()

}

class BooleanValue(override val value: Boolean) : PrimitiveValue<Boolean> {
    override fun compareTo(other: PrimitiveValue<Boolean>): Int {
        return if (value == other.value) {
            0
        } else {
            -1
        }
    }

    override fun writeTo(model: KTEObject, destination: LanguageDestination) {
        destination.write(this@BooleanValue)
    }

    override fun toString(): String = value.toString()

}

class StringValue(override val value: String) : PrimitiveValue<String> {

    override fun compareTo(other: PrimitiveValue<String>): Int {
        return if (value == other.value) {
            0
        } else {
            -1
        }
    }

    override fun writeTo(model: KTEObject, destination: LanguageDestination) {
        destination.write(this@StringValue)
    }

    override fun toString(): String = value

}