package com.wakaztahir.kte.model

import com.wakaztahir.kte.dsl.ModelIterable
import com.wakaztahir.kte.model.model.TemplateModel
import com.wakaztahir.kte.dsl.UnresolvedValueException
import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.SourceStream

interface DynamicValue<T> : CodeGen, ReferencedValue {

    val value: T

    operator fun compareTo(other: DynamicValue<T>): Int

    @Suppress("UNCHECKED_CAST")
    fun compareAny(other: DynamicValue<*>): Int {
        return compareTo(other as DynamicValue<T>)
    }

    override fun getValue(model: TemplateModel): DynamicValue<T> {
        return this
    }

    override fun getIterable(model: TemplateModel): ModelIterable<KTEValue>? {
        throw UnresolvedValueException("primitive value is not a collection")
    }

}

class IntValue(override val value: Int) : DynamicValue<Int> {

    override fun compareTo(other: DynamicValue<Int>): Int {
        return value.compareTo(other.value)
    }

    override fun generateTo(block: LazyBlock, source: SourceStream, destination: DestinationStream) {
        destination.write(this@IntValue)
    }

}

class FloatValue(override val value: Float) : DynamicValue<Float> {
    override fun compareTo(other: DynamicValue<Float>): Int {
        return value.compareTo(other.value)
    }

    override fun generateTo(block: LazyBlock, source: SourceStream, destination: DestinationStream) {
        destination.write(this@FloatValue)
    }

}

class BooleanValue(override val value: Boolean) : DynamicValue<Boolean> {
    override fun compareTo(other: DynamicValue<Boolean>): Int {
        return if (value == other.value) {
            0
        } else {
            -1
        }
    }

    override fun generateTo(block: LazyBlock, source: SourceStream, destination: DestinationStream) {
        destination.write(this@BooleanValue)
    }

}

class StringValue(override val value: String) : DynamicValue<String> {
    override fun compareTo(other: DynamicValue<String>): Int {
        return if (value == other.value) {
            0
        } else {
            -1
        }
    }

    override fun generateTo(block: LazyBlock, source: SourceStream, destination: DestinationStream) {
        destination.write(this@StringValue)
    }

}