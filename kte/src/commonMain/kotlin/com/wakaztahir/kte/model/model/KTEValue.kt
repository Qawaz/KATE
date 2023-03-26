package com.wakaztahir.kte.model.model

import com.wakaztahir.kte.model.CodeGen

interface KTEValue : CodeGen {

    fun indentation(indentationLevel: Int): String {
        var indentation = ""
        repeat(indentationLevel) {
            indentation += '\t'
        }
        return indentation
    }

    fun stringValue(indentationLevel: Int): String

    override fun toString(): String

}