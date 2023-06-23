package com.wakaztahir.kate.parser.block

import com.wakaztahir.kate.lexer.tokens.StaticTokens
import com.wakaztahir.kate.model.LazyBlock
import com.wakaztahir.kate.model.model.KATEParsingError
import com.wakaztahir.kate.lexer.stream.increment

class BlockParseState(
    var blockLineNumber : Int = 1,
    var hasConsumedFirstLineIndentation : Boolean = false
)

inline fun LazyBlock.parseSingle(
    state : BlockParseState,
    onDirective: (ParsedBlock.CodeGenRange) -> Boolean,
    onDefaultNoRawChar: (Char) -> Unit
) {
    val previous = source.pointer
    val directive = try {
        parseAtDirective()
    } catch (e: Throwable) {
        KATEParsingError(e)
    }
    if (directive != null) {
        if (!onDirective(ParsedBlock.CodeGenRange(gen = directive, start = previous, end = source.pointer))) {
            return
        }
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
            val shouldConsumeLineIndentation = state.blockLineNumber == 1 && (source.currentChar == '\t' || source.currentChar == ' ') && indentationLevel > 0 && !state.hasConsumedFirstLineIndentation
            if(!shouldConsumeLineIndentation){
                onDefaultNoRawChar(source.currentChar)
            } else {
                consumeLineIndentation()
                state.hasConsumedFirstLineIndentation = true
                return
            }
        }
        val isNewLine = source.currentChar == '\n'
        source.incrementPointer()
        if (isNewLine) {
            consumeLineIndentation()
            state.blockLineNumber++
        }
    }
}