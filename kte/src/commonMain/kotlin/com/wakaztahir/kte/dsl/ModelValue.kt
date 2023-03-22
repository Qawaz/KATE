package com.wakaztahir.kte.dsl

import com.wakaztahir.kte.model.*

class ModelValue constructor(val value: PrimitiveValue<*>) {

    constructor(value: Int) : this(IntValue(value))

    constructor(value: Float) : this(FloatValue(value))

    constructor(value: String) : this(StringValue(value))

    constructor(value: Boolean) : this(BooleanValue(value))

}