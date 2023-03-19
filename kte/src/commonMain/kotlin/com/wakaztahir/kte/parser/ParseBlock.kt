package com.wakaztahir.kte.parser

import com.wakaztahir.kte.model.AtDirective
import com.wakaztahir.kte.parser.stream.SourceStream

private fun SourceStream.unknownCharacter() {

}

fun SourceStream.parseBlock(): Any? {
    while (!hasEnded) {
        if (currentChar == '<') {
            if (!parseComment()) {
                unknownCharacter()
            }
        }
        if (currentChar == '@') {
            val directive = parseAtDirective()
            if (directive == null) {
                unknownCharacter()
            } else {

            }
        }
    }
    return null
}

fun SourceStream.parseAtDirective(): AtDirective? {
    parseEmbedding()?.let { return it }
    parseConstantDeclaration()?.let { return it }
    parseConstantReference()?.let { return it }
    parseModelDirective()?.let { return it }
    parseIfStatement()?.let { return it }
    parseForLoop()?.let { return it }
    parseRawBlock()?.let { return it }
    return null
}