package com.wakaztahir.kate.lexer.lexers.value

import com.wakaztahir.kate.lexer.model.DynamicTokenLexer
import com.wakaztahir.kate.lexer.model.ValueLexer
import com.wakaztahir.kate.lexer.stream.SourceStream
import com.wakaztahir.kate.lexer.stream.increment
import com.wakaztahir.kate.lexer.stream.incrementDirective
import com.wakaztahir.kate.lexer.stream.parseTextWhile
import com.wakaztahir.kate.lexer.tokens.StaticTokens
import com.wakaztahir.kate.lexer.tokens.dynamic.AccessChainToken
import com.wakaztahir.kate.lexer.tokens.dynamic.ValueToken

class AccessChainLexer(val parseDirectRefs: Boolean) : ValueLexer<AccessChainToken> {

    private fun Char.isVariableName(): Boolean = this.isLetterOrDigit() || this == '_'

    private fun SourceStream.parseFunctionParameters(): List<ValueToken>? {
        val source = this
        if (source.increment(StaticTokens.LeftParenthesis)) {
            if (source.increment(StaticTokens.RightParenthesis)) {
                return emptyList()
            }
            val parameters = mutableListOf<ValueToken>()
            do {
                // any expression or value can be lexed (but implements expressions only, missing lists and mutable lists, meant to be replaced by array notation)
                val parameter = ExpressionLexer(DefaultExpressionValueLexer(
                    parseDirectRefs = true
                )).lex(this)
                if (parameter != null) {
                    parameters.add(parameter)
                } else {
                    break
                }
            } while (source.increment(StaticTokens.Comma))
            if (!source.increment(StaticTokens.RightParenthesis)) {
//                TODO source.printErrorLineNumberAndCharacterIndex()
                throw IllegalStateException("a function call must end with ')' but instead found ${source.currentChar}")
            }
            return parameters
        }
        return null
    }

    private fun SourceStream.parseIndexingOperatorValue(parseDirectRefs: Boolean): ValueToken? {
        NumberValueLexer.lex(this)?.let { return it }
        StringValueLexer.lex(this)?.let { return it }
        parseVariableReference(parseDirectRefs = parseDirectRefs)?.let { return it }
        return null
    }

    fun SourceStream.parseIndexingOperatorCall(
        parseDirectRefs: Boolean,
    ): AccessChainToken.ChainMemberToken.FunctionCall? {
        if (increment(StaticTokens.LeftBracket)) {
            val indexingValue = parseIndexingOperatorValue(parseDirectRefs)
                ?: throw IllegalStateException("couldn't get indexing value inside indexing operator")
            if (increment(StaticTokens.RightBracket)) {
                return AccessChainToken.ChainMemberToken.FunctionCall(
                    name = "get",
                    parameters = listOf(indexingValue)
                )
            } else {
                throw IllegalStateException("indexing operator must end with ']'")
            }
        }
        return null
    }

    fun SourceStream.parseDotReferencesInto(
        parseDirectRefs: Boolean,
        throwOnEmptyVariableName: Boolean,
    ): MutableList<AccessChainToken.ChainMemberToken>? {
        val source = this
        return lookAhead {
            var propertyPath: MutableList<AccessChainToken.ChainMemberToken>? = null
            do {
                if (source.currentChar.isDigit()) {
                    throw IllegalStateException("variable name cannot begin with a digit")
                }
                val propertyName = source.parseTextWhile { currentChar.isVariableName() }
                if (propertyName.isEmpty()) {
                    if (throwOnEmptyVariableName || propertyPath != null) {
                        throw IllegalStateException("variable name cannot be empty")
                    } else {
                        restorePosition()
                        return@lookAhead null
                    }
                }
                val parameters = parseFunctionParameters()
                if (propertyPath == null) propertyPath = mutableListOf()
                if (parameters != null) {
                    propertyPath.add(AccessChainToken.ChainMemberToken.FunctionCall(propertyName, parameters))
                } else {
                    propertyPath.add(AccessChainToken.ChainMemberToken.Property(propertyName))
                }
                parseIndexingOperatorCall(parseDirectRefs = parseDirectRefs)?.let { propertyPath.add(it) }
            } while (source.increment(StaticTokens.Dot))
            propertyPath
        }
    }

    fun SourceStream.parseModelDirective(
        parseDirectRefs: Boolean,
        throwOnEmptyVariableName: Boolean
    ): AccessChainToken? {
        parseDotReferencesInto(
            parseDirectRefs = parseDirectRefs,
            throwOnEmptyVariableName = throwOnEmptyVariableName,
        )?.let { return AccessChainToken(it) }
        return null
    }

    fun SourceStream.parseVariableReference(parseDirectRefs: Boolean): AccessChainToken? {
        val source = this
        if (source.incrementDirective(StaticTokens.Var, StaticTokens.LeftParenthesis)) {
            val directive = parseModelDirective(parseDirectRefs = true, throwOnEmptyVariableName = true)
            if (!source.increment(StaticTokens.RightParenthesis)) {
//              TODO source.printErrorLineNumberAndCharacterIndex()
                throw IllegalStateException("expected ${StaticTokens.RightParenthesis} got ${source.currentChar} at ${source.pointer}")
            }
            return directive
        }
        if (parseDirectRefs) return parseModelDirective(
            parseDirectRefs = true,
            throwOnEmptyVariableName = false
        )?.let { return it }
        return null
    }

    override fun lex(stream: SourceStream): AccessChainToken? {
        return stream.parseVariableReference(parseDirectRefs = parseDirectRefs)
    }
}