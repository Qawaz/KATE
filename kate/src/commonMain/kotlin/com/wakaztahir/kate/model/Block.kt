package com.wakaztahir.kate.model

import com.wakaztahir.kate.KATEDelicateFunction
import com.wakaztahir.kate.lexer.tokens.StaticTokens
import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.parser.*
import com.wakaztahir.kate.parser.function.parseFunctionDefinition
import com.wakaztahir.kate.lexer.stream.*
import com.wakaztahir.kate.lexer.stream.increment
import com.wakaztahir.kate.parser.block.*
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.parser.stream.ParserSourceStream
import com.wakaztahir.kate.parser.stream.TextDestinationStream
import com.wakaztahir.kate.parser.variable.parseVariableAssignment
import com.wakaztahir.kate.parser.variable.parseVariableDeclaration
import com.wakaztahir.kate.parser.variable.parseVariableReferenceAsExpression

interface LazyBlock {

    val source: ParserSourceStream

    val provider: ModelProvider

    val model: MutableKATEObject get() = provider.model

    // Text that couldn't be processed by the parser is written to destination stream as it is
    val isDefaultNoRaw: Boolean

    // Indentation level should increase when nesting blocks
    val indentationLevel: Int

    fun canIterate(): Boolean

    fun MutableList<ParsedBlock.CodeGenRange>.appendChar(char: Char) {
        if (isNotEmpty() && last().gen is DefaultNoRawString) {
            last().incrementEndForDefaultNoRawStringGen(char)
        } else {
            add(
                ParsedBlock.CodeGenRange(
                    gen = DefaultNoRawString("$char"),
                    start = source.pointer,
                    end = source.pointer + 1
                )
            )
        }
    }

    fun parseSingleNode(
        state: BlockParseState,
        onDirective: (ParsedBlock.CodeGenRange) -> Boolean,
        onDefaultNoRawChar: (Char) -> Unit
    ) = parseSingle(
        state = state,
        onDirective = onDirective,
        onDefaultNoRawChar = onDefaultNoRawChar,
    )

    fun parse(): ParsedBlock {
        val gens = mutableListOf<ParsedBlock.CodeGenRange>()
        val state = BlockParseState()
        while (canIterate()) {
            parseSingle(
                state = state,
                onDirective = { gens.add(it) },
                onDefaultNoRawChar = { gens.appendChar(it) }
            )
        }
        return ParsedBlock(codeGens = gens)
    }

    fun consumeLineIndentation(): Int {
        if (indentationLevel < 1) return indentationLevel
        var x = 0
        var consumed = 0
        while (x < indentationLevel) {
            if (source.increment(StaticTokens.Tab) || source.increment(StaticTokens.FourSpaces)) consumed++
            x++
        }
        return consumed
    }

    fun writeCurrentChar(destination: DestinationStream) {
        destination.stream.write(source.currentChar)
    }

    fun writeDirective(previous: Int, directive: CodeGen, destination: DestinationStream) {
        directive.generateTo(destination)
    }

    fun parseNestedAtDirective(block: LazyBlock): CodeGen? {
        return null
    }

    fun parseImplicitDirectives(): CodeGen? {
        parseVariableReferenceAsExpression(parseDirectRefs = !isDefaultNoRaw)?.let {
            if (isDefaultNoRaw || it.first) {
                return DefaultNoRawExpression(source = source, value = it.second, provider = provider)
            } else {
                return PartialRawFunctionCall(it.second)
            }
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
        parse().generateTo(destination)
        return (destination.stream as TextDestinationStream).getValue()
    }

}


open class LazyBlockSlice(
    val parentBlock: LazyBlock,
    val startPointer: Int,
    val length: Int,
    val blockEndPointer: Int,
    override val provider: ModelProvider,
    override val isDefaultNoRaw: Boolean,
    override val indentationLevel: Int
) : LazyBlock {

    override val source: ParserSourceStream = parentBlock.source

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