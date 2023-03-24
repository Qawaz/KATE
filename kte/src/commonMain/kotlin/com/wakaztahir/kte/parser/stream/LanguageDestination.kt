package com.wakaztahir.kte.parser.stream

import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.model.model.KTEList
import com.wakaztahir.kte.model.model.KTEObject

interface LanguageDestination {

    val stream: WritableStream

    fun write(value: IntValue)
    fun write(value: DoubleValue)
    fun write(value: BooleanValue)
    fun write(value: StringValue)
    fun write(model: KTEObject, value: KTEList<KTEValue>)
    fun write(value: KTEObject)

}