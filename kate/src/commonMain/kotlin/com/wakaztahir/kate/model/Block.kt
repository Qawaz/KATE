package com.wakaztahir.kate.model

import com.wakaztahir.kate.KTEDelicateFunction
import com.wakaztahir.kate.model.model.KATEUnit
import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.parser.*
import com.wakaztahir.kate.parser.stream.*
import com.wakaztahir.kate.parser.stream.increment

interface LazyBlock {

    val source: SourceStream
    val model: MutableKATEObject

    // Text that couldn't be processed by the compiler is written to stream as it is
    val isWriteUnprocessedTextEnabled: Boolean

    // Indentation level should increase , which will be consumed as code is parsed
    val indentationLevel: Int

    fun canIterate(): Boolean

    fun generateTo(destination: DestinationStream) {
        var blockLineNumber = 1
        var hasConsumedFirstLineIndentation = false
        while (canIterate()) {
            if (source.skipMultilineComments()) {
                continue
            }
            val directive = parseAtDirective()
            if (directive != null) {
                writeDirective(directive = directive, destination = destination)
                continue
            }
            if (isWriteUnprocessedTextEnabled) {
                if (blockLineNumber == 1 && (source.currentChar == '\t' || source.currentChar == ' ') && indentationLevel > 0 && !hasConsumedFirstLineIndentation) {
                    consumeLineIndentation()
                    hasConsumedFirstLineIndentation = true
                    continue
                } else {
                    destination.stream.write(source.currentChar)
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

    fun consumeLineIndentation(): Int {
        if (indentationLevel < 1) return indentationLevel
        var x = 0
        var consumed = 0
        while (x < indentationLevel) {
            if (source.increment('\t') || source.increment("    ")) consumed++
            x++
        }
        return consumed
    }

    fun writeDirective(directive: CodeGen, destination: DestinationStream) {
        directive.generateTo(this, destination)
        if (!source.hasEnded && directive is BlockContainer) {
            if (!source.increment('\n')) {
                source.increment(' ')
            } else {
                consumeLineIndentation()
            }
        } else if (directive.isEmptyWriter) {
            source.increment(' ')
        }
    }

    fun parseNestedAtDirective(block: LazyBlock): CodeGen? {
        return null
    }

    fun parseImplicitDirectives(): CodeGen? {
        parseExpression(
            parseFirstStringOrChar = false,
            parseNotFirstStringOrChar = true,
            parseDirectRefs = false
        )?.let { return it.toPlaceholderInvocation(model, source.pointer) ?: KATEUnit }
        return null
    }

    fun parseAtDirective(): CodeGen? {
        parseRuntimeGen()?.let { return it }
        parseImplicitDirectives()?.let { return it }
        parseNestedAtDirective(this)?.let { return it }
        parseRawBlock()?.let { return it }
        parsePartialRaw()?.let { return it }
        parseEmbedding()?.let { return it }
        parseVariableDeclaration()?.let { return it }
        parseObjectDeclaration()?.let { return it }
        parseIfStatement()?.let { return it }
        parseForLoop()?.let { return it }
        parsePlaceholderDefinition()?.let { return it }
        parsePlaceholderInvocation()?.let { return it }
        parseFunctionDefinition(anonymousFunctionName = null)?.let { return it }
        parsePlaceholderUse()?.let { return it }
        return null
    }

    fun getValueAsString(startPointer: Int = 0): String {
        val previous = source.pointer
        source.setPointerAt(startPointer)
        var text = ""
        while (canIterate()) {
            text += source.currentChar
            source.incrementPointer()
        }
        source.setPointerAt(previous)
        return text

    }

    @KTEDelicateFunction
    fun getDestinationString(): String {
        val destination = TextDestinationStream()
        generateTo(destination)
        return (destination.stream as TextDestinationStream).getValue()
    }

}


open class LazyBlockSlice(
    val parentBlock: LazyBlock,
    val startPointer: Int,
    val length: Int,
    val blockEndPointer: Int,
    override val model: MutableKATEObject,
    override val isWriteUnprocessedTextEnabled: Boolean,
    override val indentationLevel: Int
) : LazyBlock {

    override val source: SourceStream = parentBlock.source

    override fun canIterate(): Boolean {
        return source.pointer < startPointer + length
    }

    override fun generateTo(destination: DestinationStream) {
        source.setPointerAt(startPointer)
        super.generateTo(destination)
        source.setPointerAt(blockEndPointer)
    }

    override fun parseNestedAtDirective(block: LazyBlock): CodeGen? {
        return parentBlock.parseNestedAtDirective(block)
    }

    fun getValueAsString(): String {
        return getValueAsString(startPointer)
    }

    fun writeValueTo(destination: DestinationStream) {
        val previous = source.pointer
        var blockLineNumber = 1
        var hasConsumedFirstLineIndentation = false
        source.setPointerAt(startPointer)
        while (canIterate()) {
            if (blockLineNumber == 1 && (source.currentChar == '\t' || source.currentChar == ' ') && indentationLevel > 0 && !hasConsumedFirstLineIndentation) {
                consumeLineIndentation()
                hasConsumedFirstLineIndentation = true
                continue
            } else {
                destination.stream.write(source.currentChar)
            }
            val isNewLine = source.currentChar == '\n'
            source.incrementPointer()
            if (isNewLine) {
                consumeLineIndentation()
                blockLineNumber++
            }
        }
        source.setPointerAt(previous)
    }

}