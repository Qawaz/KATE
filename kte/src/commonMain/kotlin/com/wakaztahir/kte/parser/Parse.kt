package com.wakaztahir.kte.parser

import com.wakaztahir.kte.TemplateContext

fun TemplateContext.parse() {
    while (!stream.hasEnded) {
        when (stream.currentChar) {
            '<' -> {
                stream.skipMultilineComments()
            }

            '@' -> {

            }

            else -> {

            }

        }
    }
}