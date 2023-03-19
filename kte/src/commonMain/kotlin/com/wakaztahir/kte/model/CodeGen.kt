package com.wakaztahir.kte.model

import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.parser.stream.DestinationStream

interface CodeGen {

    fun generateTo(context : TemplateContext,stream: DestinationStream)

}