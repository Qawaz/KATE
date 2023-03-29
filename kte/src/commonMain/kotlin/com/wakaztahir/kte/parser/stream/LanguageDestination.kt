package com.wakaztahir.kte.parser.stream

import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.model.model.KTEList
import com.wakaztahir.kte.model.model.KTEMutableList
import com.wakaztahir.kte.model.model.KTEObject
import com.wakaztahir.kte.model.model.KTEValue

interface LanguageDestination {



    fun write(block: LazyBlock,value : CharValue)
    fun write(block: LazyBlock,value: IntValue)
    fun write(block: LazyBlock,value: DoubleValue)
    fun write(block: LazyBlock,value: BooleanValue)
    fun write(block: LazyBlock,value: StringValue)
    fun write(block: LazyBlock,value: KTEList<out KTEValue>)
    fun write(block: LazyBlock,value: KTEMutableList<out KTEValue>)
    fun write(block: LazyBlock,value: KTEObject)

}