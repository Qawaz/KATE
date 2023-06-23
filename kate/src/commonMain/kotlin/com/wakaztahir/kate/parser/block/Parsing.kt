package com.wakaztahir.kate.parser.block

import com.wakaztahir.kate.lexer.tokens.StaticTokens
import com.wakaztahir.kate.model.LazyBlock
import com.wakaztahir.kate.model.model.KATEParsingError
import com.wakaztahir.kate.lexer.stream.increment

inline fun LazyBlock.parse(
    onDirective : (ParsedBlock.CodeGenRange)->Unit,
    onDefaultNoRawChar : (Char)->Unit
) {
    var blockLineNumber = 1
    var hasConsumedFirstLineIndentation = false
    while (canIterate()) {
        val previous = source.pointer
        val directive = try {
            parseAtDirective()
        } catch (e: Throwable) {
            KATEParsingError(e)
        }
        if (directive != null) {
            onDirective(ParsedBlock.CodeGenRange(gen = directive, start = previous, end = source.pointer))
            if (!source.hasEnded && directive.expectSpaceOrNewLineWithIndentationAfterwards) {
                if (!source.increment(StaticTokens.NewLine)) {
                    source.increment(StaticTokens.SingleSpace)
                } else {
                    consumeLineIndentation()
                }
            } else if (directive.isEmptyWriter) {
                source.increment(StaticTokens.SingleSpace)
            }
        } else {
            if (isDefaultNoRaw) {
                if (blockLineNumber == 1 && (source.currentChar == '\t' || source.currentChar == ' ') && indentationLevel > 0 && !hasConsumedFirstLineIndentation) {
                    consumeLineIndentation()
                    hasConsumedFirstLineIndentation = true
                    continue
                } else {
                    onDefaultNoRawChar(source.currentChar)
                }
            }
            val isNewLine = source.currentChar == '\n'
            source.incrementPointer()
            if (isNewLine) {
                consumeLineIndentation()
                blockLineNumber++
            }
        }
    }
}