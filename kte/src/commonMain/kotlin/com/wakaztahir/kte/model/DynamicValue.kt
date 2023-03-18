package com.wakaztahir.kte.model

interface DynamicValue<T> {
    val value: T

    operator fun compareTo(other: DynamicValue<T>): Int

    @Suppress("UNCHECKED_CAST")
    fun compareAny(other: DynamicValue<*>): Int {
        return compareTo(other as DynamicValue<T>)
    }

    fun getValueAsString(): String
}

class IntValue(override val value: Int) : DynamicValue<Int> {
    override fun compareTo(other: DynamicValue<Int>): Int {
        return value.compareTo(other.value)
    }

    override fun getValueAsString(): String = value.toString()
    override fun toString(): String = getValueAsString()
}

class FloatValue(override val value: Float) : DynamicValue<Float> {
    override fun compareTo(other: DynamicValue<Float>): Int {
        return value.compareTo(other.value)
    }

    override fun getValueAsString(): String = value.toString() + 'f'
    override fun toString(): String = getValueAsString()
}

class BooleanValue(override val value: Boolean) : DynamicValue<Boolean> {
    override fun compareTo(other: DynamicValue<Boolean>): Int {
        return if (value == other.value) {
            0
        } else {
            -1
        }
    }

    override fun getValueAsString(): String = if (value) "true" else "false"
    override fun toString(): String = getValueAsString()
}

class StringValue(override val value: String) : DynamicValue<String> {
    override fun compareTo(other: DynamicValue<String>): Int {
        return if (value == other.value) {
            0
        } else {
            -1
        }
    }

    override fun getValueAsString(): String = '\"' + value + '\"'
    override fun toString(): String = getValueAsString()
}