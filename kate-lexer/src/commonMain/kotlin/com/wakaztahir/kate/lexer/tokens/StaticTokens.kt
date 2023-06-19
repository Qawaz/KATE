package com.wakaztahir.kate.lexer.tokens

object StaticTokens {

    private fun token(text: String): StringStaticToken {
        require(text.length > 1) // TODO remove this once all tokens have been made
        return StringStaticToken(text)
    }

    private fun token(char: Char) = CharStaticToken(char)

    val AtDirective = token('@')

    val Equals = token("==")

    val SingleEqual = token('=')

    val NotEqual = token("!=")

    val LessThan = token('<')

    val BiggerThan = token('>')

    val GreaterThanOrEqualTo = token(">=")

    val LessThanOrEqualTo = token("<=")

    val SingleSpace = token(' ')

    val SingleQuote = token('\'')

    val DoubleQuote = token('"')

    val NewLine = token('\n')

    val Tab = token('\t')

    val Plus = token('+')

    val Minus = token('-')

    val DefinePlaceholder = token("define_placeholder")

    val EndDefinePlaceholder = token("end_define_placeholder")

    val PlaceholderCall = token("placeholder")

    val PlaceholderUse = token("use_placeholder")

    val DefaultNoRaw = token("default_no_raw")

    val PartialRaw = token("partial_raw")

    val EndPartialRaw = token("end_partial_raw")

    val FunctionReturnTypeLeader = token("->")

    val Raw = token("raw")

    val EndRaw = token("endraw")

    val EndDefaultNoRaw = token("end_default_no_raw")

    val True = token("true")

    val False = token("false")

    val Function = token("function")

    val EndFunction = token("end_function")

    val For = token("for")

    val Continue = token("continue")

    val Break = token("break")

    val Return = token("return")

    val EndFor = token("endfor")

    val Embed = token("embed")

    val UnderscoreOnce = token("_once")

    val If = token("if")

    val ElseIf = token("elseif")

    val Else = token("else")

    val EndIf = token("endif")

    val List = token("list")

    val MutableList = token("mutable_list")

    val DefineObject = token("define_object")

    val EndDefineObject = token("end_define_object")

    val CommentStart = token("<%--")

    val CommentEnd = token("--%>")

    val FourSpaces = token("    ")

    val Comma = token(',')

    val Backtick = token('`')

    val Colon = token(':')

    val SemiColon = token(';')

    val NullableChar = token('?')

    val Var = token("var")

    val SetVar = token("set_var")

    val LeftParenthesis = token('(')

    val RightParenthesis = token(')')

    val NegativeValueDash = token('-')

    val Dot = token('.')

    val LongEnder = token('L')

    val AndOperator = token("&&")

    val OrOperator = token("||")

    val LeftBracket = token('[')

    val RightBracket = token(']')

    val LeftBrace = token('{')

    val RightBrace = token('}')

    val RuntimeWriteChar = token("runtime.print_char")

    val RuntimeWriteString = token("runtime.print_string")




}