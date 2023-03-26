package com.wakaztahir.kte.model

import com.wakaztahir.kte.KTEDelicateFunction
import com.wakaztahir.kte.model.model.MutableKTEObject
import com.wakaztahir.kte.parser.*
import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.SourceStream
import com.wakaztahir.kte.parser.stream.TextDestinationStream
import com.wakaztahir.kte.parser.stream.increment
import com.wakaztahir.kte.parser.stream.languages.KotlinLanguageDestination

interface LazyBlock {

    val source: SourceStream
    val model: MutableKTEObject
    val allowTextOut : Boolean

    fun canIterate(): Boolean

    fun generateTo(destination: DestinationStream) {
        while (canIterate()) {
            if (source.skipMultilineComments()) {
                continue
            }
            val directive = parseAtDirective()
            if (directive != null) {
                writeDirective(directive = directive, destination = destination)
                continue
            }
            if(allowTextOut) destination.stream.write(source.currentChar)
            source.incrementPointer()
        }
    }

    fun writeDirective(directive: CodeGen, destination: DestinationStream) {
        directive.generateTo(this, destination)
        if (!source.hasEnded && directive is BlockContainer) {
            source.increment('\n')
        }
    }

    fun parseAtDirective(): CodeGen? {
        source.parseRawBlock()?.let { return it }
        parsePartialRaw()?.let { return it }
        source.parseEmbedding()?.let { return it }
        source.parseExpression()?.let { return it }
        source.parseVariableDeclaration()?.let { return it }
        parseIfStatement()?.let { return it }
        parseForLoop()?.let { return it }
        parsePlaceholderDefinition()?.let { return it }
        parsePlaceholderInvocation()?.let { return it }
        parsePlaceholderUse()?.let { return it }
        return null
    }

    @KTEDelicateFunction
    fun getDestinationString(): String {
        val destination = KotlinLanguageDestination(this, TextDestinationStream())
        generateTo(destination)
        return (destination.stream as TextDestinationStream).getValue()
    }

    @KTEDelicateFunction
    fun getDestinationStringWithReset(): String {
        val previous = source.pointer
        val value = getDestinationString()
        source.setPointerAt(previous)
        return value
    }

}


open class LazyBlockSlice(
    override val source: SourceStream,
    val startPointer: Int,
    val length: Int,
    val blockEndPointer: Int,
    override val model: MutableKTEObject,
    override val allowTextOut: Boolean
) : LazyBlock {

    override fun canIterate(): Boolean {
        return source.pointer < startPointer + length
    }

    override fun generateTo(destination: DestinationStream) {
        source.setPointerAt(startPointer)
        super.generateTo(destination)
        source.setPointerAt(blockEndPointer)
    }

    fun getValueAsString(): String {
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

    fun writeValueTo(destination: DestinationStream) {
        val previous = source.pointer
        source.setPointerAt(startPointer)
        while (canIterate()) {
            destination.stream.write(source.currentChar)
            source.incrementPointer()
        }
        source.setPointerAt(previous)
    }

}