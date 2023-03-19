package com.wakaztahir.kte.model

import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.parser.stream.DestinationStream

class RawBlock(val value: String) : AtDirective {
    override fun generateTo(context: TemplateContext, stream: DestinationStream) {
        stream.write(value)
    }
}