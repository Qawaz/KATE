package com.wakaztahir.kte.parser

import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.model.RawBlock
import com.wakaztahir.kte.parser.stream.SourceStream
import com.wakaztahir.kte.parser.stream.increment
import com.wakaztahir.kte.parser.stream.parseTextUntilConsumed

fun SourceStream.parseRawBlock(): RawBlock? {
    return if (currentChar == '@' && increment("@raw")) {
        increment(' ')
        val rawText = parseTextUntilConsumed("@endraw")
        RawBlock(if(rawText.lastOrNull() == ' ') rawText.substringBeforeLast(' ') else rawText)
    } else {
        null
    }
}