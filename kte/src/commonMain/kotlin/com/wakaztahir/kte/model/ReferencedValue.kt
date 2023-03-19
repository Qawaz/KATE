package com.wakaztahir.kte.model

import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.parser.parseDynamicValue
import com.wakaztahir.kte.parser.stream.TextStream

internal interface ReferencedValue {

    fun getValue(context: TemplateContext): DynamicValue<*>?

}