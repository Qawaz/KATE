package com.wakaztahir.kte.parser

import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.parser.stream.DestinationStream

fun TemplateContext.parse() {
    while (!stream.hasEnded) {
        when (stream.currentChar) {
            '<' -> {
                stream.parseComment()
            }

            '@' -> {

            }

            else -> {

            }

        }
    }
}