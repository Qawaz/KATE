package com.wakaztahir.kate.parser.stream

import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.KTEList
import com.wakaztahir.kate.model.model.KTEMutableList
import com.wakaztahir.kate.model.model.KTEObject
import com.wakaztahir.kate.model.model.KTEValue

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