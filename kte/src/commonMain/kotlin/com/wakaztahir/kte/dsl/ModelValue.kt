package com.wakaztahir.kte.dsl

import com.wakaztahir.kte.model.*

class ModelValue constructor(val value: KTEValue) {

    constructor(value: Int) : this(IntValue(value))

    constructor(value: Double) : this(DoubleValue(value))

    constructor(value: String) : this(StringValue(value))

    constructor(value: Boolean) : this(BooleanValue(value))

}