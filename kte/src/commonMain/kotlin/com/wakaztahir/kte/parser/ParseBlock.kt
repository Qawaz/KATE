package com.wakaztahir.kte.parser

import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.parser.stream.DestinationStream

fun TemplateContext.generateTo(destination: DestinationStream) {
    stream.generateTo(stream, destination)
}