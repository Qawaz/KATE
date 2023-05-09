package com.wakaztahir.kate.parser

import com.wakaztahir.kate.dsl.ScopedModelObject
import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.MutableKATEObject
import com.wakaztahir.kate.parser.stream.*
import com.wakaztahir.kate.parser.stream.increment
import com.wakaztahir.kate.parser.variable.parseVariableReference

fun LazyBlock.parseBlockSlice(
    startsWith: String,
    endsWith: String,
    isDefaultNoRaw: Boolean,
    model: MutableKATEObject,
    indentationLevel: Int = this.indentationLevel + 1
): LazyBlockSlice {

    escapeBlockSpacesForward()

    val previous = source.pointer

    val ender: String = source.incrementUntilDirectiveWithSkip(startsWith) {
        if (source.increment(endsWith)) endsWith else null
    } ?: throw IllegalStateException("$startsWith must end with $endsWith")


    source.decrementPointer(ender.length)

    val pointerBeforeEnder = source.pointer

    escapeBlockSpacesBackward()

    val length = source.pointer - previous

    source.setPointerAt(pointerBeforeEnder + ender.length)

    return LazyBlockSlice(
        parentBlock = this,
        startPointer = previous,
        length = length,
        model = model,
        blockEndPointer = source.pointer,
        isDefaultNoRaw = isDefaultNoRaw,
        indentationLevel = indentationLevel
    )

}

fun LazyBlock.parseBlockSlice(
    startsWith: String,
    endsWith: String,
    isDefaultNoRaw: Boolean,
    inheritModel: Boolean,
    indentationLevel: Int = this.indentationLevel + 1
): LazyBlockSlice = parseBlockSlice(
    startsWith = startsWith,
    endsWith = endsWith,
    isDefaultNoRaw = isDefaultNoRaw,
    model = if (inheritModel) model else ScopedModelObject(model),
    indentationLevel = indentationLevel
)

fun String.deIndented(indentationLevel: Int): String {
    var i = 0
    var text = ""
    while (i < length) {
        if ((i == 0 || this[i - 1] == '\n') &&
            (this[i] == '\t' || (i + 4 < this.length && this.substring(i, i + 5) == "    "))
            && indentationLevel > 0
        ) {
            var x = 0
            while (x < indentationLevel && i < length) {
                if (this[i] == '\t') {
                    i++
                } else if (i + 5 < this.length && this.substring(i, i + 5) == "    ") {
                    i += 4
                }
                x++
            }
            continue
        } else {
            text += this[i]
        }
        i++
    }
    return text
}

fun LazyBlock.parseRawBlockText(): String {
    escapeBlockSpacesForward()
    var text = source.parseTextUntilConsumedNew("@endraw")
        ?: throw IllegalStateException("@raw must end with @endraw")
    text = text.deIndented(indentationLevel + 1)
    return text.escapeBlockSpacesBackward(indentationLevel)
}

fun LazyBlock.parseRawBlock(): RawBlock? {
    if (source.currentChar == '@' && source.increment("@raw")) {
        return RawBlock(parseRawBlockText())
    }
    return null
}

fun LazyBlock.parsePartialRawImplicitDirective(): CodeGen? {
    parseVariableReference(parseDirectRefs = true)?.let {
        it.propertyPath.lastOrNull()?.let { c -> c as? ModelReference.FunctionCall }?.let { call ->
            return PartialRawFunctionCall(it)
        } ?: run {
            throw IllegalStateException("variable reference $it cannot be used inside @partial_raw")
        }

    }
    return null
}

fun LazyBlock.parsePartialRaw(): PartialRawBlock? {
    if (source.currentChar == '@' && source.increment("@partial_raw")) {
        val slice = parseBlockSlice(
            startsWith = "@partial_raw",
            endsWith = "@end_partial_raw",
            isDefaultNoRaw = false,
            inheritModel = true
        )
        return PartialRawBlock(
            value = PartialRawLazyBlockSlice(
                parentBlock = this,
                startPointer = slice.startPointer,
                length = slice.length,
                blockEndPointer = slice.blockEndPointer,
                model = slice.model,
                indentationLevel = slice.indentationLevel
            )
        )
    }
    return null
}

fun LazyBlock.parseDefaultNoRaw(): DefaultNoRawBlock? {
    if (source.currentChar == '@' && source.increment("@default_no_raw")) {
        return DefaultNoRawBlock(
            parseBlockSlice(
                startsWith = "@default_no_raw",
                endsWith = "@end_default_no_raw",
                isDefaultNoRaw = true,
                inheritModel = true
            )
        )
    }
    return null
}