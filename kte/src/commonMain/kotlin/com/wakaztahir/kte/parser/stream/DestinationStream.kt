package com.wakaztahir.kte.parser.stream

import com.wakaztahir.kte.model.BooleanValue
import com.wakaztahir.kte.model.FloatValue
import com.wakaztahir.kte.model.IntValue
import com.wakaztahir.kte.model.StringValue

abstract class DestinationStream {

    abstract fun write(char: Char)
    abstract fun write(str: String)

    open fun write(value: IntValue) = write(value.value.toString())
    open fun write(value: FloatValue) = write(value.value.toString() + 'f')
    open fun write(value: BooleanValue) = write(if (value.value) "true" else "false")
    open fun write(value: StringValue) = write(value.value)

}