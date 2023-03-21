package com.wakaztahir.kte.model

import com.wakaztahir.kte.model.model.ModelList
import com.wakaztahir.kte.dsl.UnresolvedValueException
import com.wakaztahir.kte.model.model.TemplateModel
import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.SourceStream

interface PrimitiveValue<T> : CodeGen, ReferencedValue {

    val value: T

    operator fun compareTo(other: PrimitiveValue<T>): Int

    @Suppress("UNCHECKED_CAST")
    fun compareAny(other: PrimitiveValue<*>): Int {
        return compareTo(other as PrimitiveValue<T>)
    }

    override fun getValue(model: TemplateModel): PrimitiveValue<*> {
        return this
    }

    override fun getIterable(model: TemplateModel): ModelList<KTEValue> {
        throw UnresolvedValueException("primitive value is not iterable")
    }

    override fun getObject(model: TemplateModel): TemplateModel {
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

    override fun generateTo(block: LazyBlock, source: SourceStream, destination: DestinationStream) {
        destination.write(this@IntValue)
    }

    override fun toString(): String = value.toString()

}

class FloatValue(override val value: Float) : PrimitiveValue<Float> {
    override fun compareTo(other: PrimitiveValue<Float>): Int {
        return value.compareTo(other.value)
    }

    override fun generateTo(block: LazyBlock, source: SourceStream, destination: DestinationStream) {
        destination.write(this@FloatValue)
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

    override fun generateTo(block: LazyBlock, source: SourceStream, destination: DestinationStream) {
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

    override fun generateTo(block: LazyBlock, source: SourceStream, destination: DestinationStream) {
        destination.write(this@StringValue)
    }

    override fun toString(): String = value

}