package com.wakaztahir.kate.model

import com.wakaztahir.kate.KATEDelicateFunction
import com.wakaztahir.kate.model.block.DefaultNoRawString
import com.wakaztahir.kate.model.model.KATEParsingError
import com.wakaztahir.kate.model.model.KATEUnit
import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.parser.*
import com.wakaztahir.kate.parser.function.parseFunctionDefinition
import com.wakaztahir.kate.parser.stream.*
import com.wakaztahir.kate.parser.stream.increment
import com.wakaztahir.kate.parser.variable.parseVariableAssignment
import com.wakaztahir.kate.parser.variable.parseVariableDeclaration
import com.wakaztahir.kate.parser.variable.parseVariableReferenceAsExpression

interface LazyBlock {

    val source: SourceStream

    val model: MutableKATEObject

    // Text that couldn't be processed by the parser is written to destination stream as it is
    val isDefaultNoRaw: Boolean

    // Indentation level should increase when nesting blocks
    val indentationLevel: Int

    fun canIterate(): Boolean

    private fun MutableList<ParsedBlock.CodeGenRange>.appendCurrentChar() {
        if (isNotEmpty() && last().gen is DefaultNoRawString) {
            val defaultNoRawString = removeLast()
            add(
                ParsedBlock.CodeGenRange(
                    gen = defaultNoRawString.gen.also { (it as DefaultNoRawString).stringValue += source.currentChar },
                    start = defaultNoRawString.start,
                    end = source.pointer + 1
                )
            )
        } else {
            add(
                ParsedBlock.CodeGenRange(
                    gen = DefaultNoRawString("${source.currentChar}"),
                    start = source.pointer,
                    end = source.pointer + 1
                )
            )
        }
    }

    fun parse(): ParsedBlock {
        val gens = mutableListOf<ParsedBlock.CodeGenRange>()
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
                gens.add(ParsedBlock.CodeGenRange(gen = directive, start = previous, end = source.pointer))
                if (!source.hasEnded && directive is BlockContainer) {
                    if (!source.increment('\n')) {
                        source.increment(' ')
                    } else {
                        consumeLineIndentation()
                    }
                } else if (directive.isEmptyWriter) {
                    source.increment(' ')
                }
            } else {
                if (isDefaultNoRaw) {
                    if (blockLineNumber == 1 && (source.currentChar == '\t' || source.currentChar == ' ') && indentationLevel > 0 && !hasConsumedFirstLineIndentation) {
                        consumeLineIndentation()
                        hasConsumedFirstLineIndentation = true
                        continue
                    } else {
                        gens.appendCurrentChar()
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
        return ParsedBlock(codeGens = gens)
    }

    fun generateTo(destination: DestinationStream) {
        parse().generateTo(this, destination)
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

    fun writeCurrentChar(destination: DestinationStream) {
        destination.stream.write(source.currentChar)
    }

    fun writeDirective(previous: Int, directive: CodeGen, destination: DestinationStream) {
        directive.generateTo(this, destination)
    }

    fun parseNestedAtDirective(block: LazyBlock): CodeGen? {
        return null
    }

    fun parseImplicitDirectives(): CodeGen? {
        parseVariableReferenceAsExpression(parseDirectRefs = !isDefaultNoRaw)?.let {
            return DefaultNoRawExpression(it)
        }
        return null
    }

    fun parseAtDirective(): CodeGen? {
        parseMultilineComment()?.let { return it }
        parseRuntimeGen()?.let { return it }
        parseNestedAtDirective(this)?.let { return it }
        parseRawBlock()?.let { return it }
        parsePartialRaw()?.let { return it }
        parseEmbedding()?.let { return it }
        parseObjectDeclaration()?.let { return it }
        parseIfStatement()?.let { return it }
        parseForLoop()?.let { return it }
        parsePlaceholderDefinition()?.let { return it }
        parsePlaceholderInvocation()?.let { return it }
        parseFunctionDefinition(anonymousFunctionName = null)?.let { return it }
        parsePlaceholderUse()?.let { return it }
        parseVariableDeclaration()?.let { return it }
        parseVariableAssignment()?.let { return it }
        parseImplicitDirectives()?.let { return it }
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

    @KATEDelicateFunction
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
    override val isDefaultNoRaw: Boolean,
    override val indentationLevel: Int
) : LazyBlock {

    protected val parsedBlock: ParsedBlock by lazy { parse() }

    fun prepare() = parsedBlock

    override val source: SourceStream = parentBlock.source

    override fun canIterate(): Boolean {
        return source.pointer < startPointer + length
    }

    override fun parse(): ParsedBlock {
        source.setPointerAt(startPointer)
        val parsed = super.parse()
        source.setPointerAt(blockEndPointer)
        return parsed
    }

    override fun parseNestedAtDirective(block: LazyBlock): CodeGen? {
        return parentBlock.parseNestedAtDirective(block)
    }

    fun getValueAsString(): String {
        return getValueAsString(startPointer)
    }

}