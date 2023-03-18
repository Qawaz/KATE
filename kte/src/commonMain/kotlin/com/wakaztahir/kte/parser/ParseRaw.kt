package com.wakaztahir.kte.parser

import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.model.RawBlock
import com.wakaztahir.kte.parser.stream.increment
import com.wakaztahir.kte.parser.stream.parseTextUntil

fun TemplateContext.parseRawBlock(): RawBlock? {
    return if (stream.currentChar == '@' && stream.increment("@raw")) {
        val rawText = stream.parseTextUntil("@endraw")
        RawBlock(rawText)
    } else {
        null
    }
}