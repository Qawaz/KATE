package com.wakaztahir.kate.parser.stream

import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.KATEList
import com.wakaztahir.kate.model.model.KATEMutableList
import com.wakaztahir.kate.model.model.KATEObject
import com.wakaztahir.kate.model.model.KATEValue

interface LanguageDestination {



    fun write(block: LazyBlock,value : CharValue)
    fun write(block: LazyBlock,value: IntValue)
    fun write(block: LazyBlock,value: DoubleValue)
    fun write(block: LazyBlock,value: BooleanValue)
    fun write(block: LazyBlock,value: StringValue)
    fun write(block: LazyBlock,value: KATEList<out KATEValue>)
    fun write(block: LazyBlock,value: KATEMutableList<out KATEValue>)
    fun write(block: LazyBlock,value: KATEObject)

}