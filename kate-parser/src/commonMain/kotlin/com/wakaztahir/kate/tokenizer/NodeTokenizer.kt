package com.wakaztahir.kate.tokenizer

interface NodeTokenizer<T> {

    val forLoopBreak : T

    val forLoopContinue : T

    val partialRawFunctionCall : T

    val defaultNoRawExpression : T

    val kateParsingError : T

    val block : T

    val multilineComment : T

    val conditionalFor : T

    val iterableFor : T

    val numberedFor : T

    val functionReturn : T

    val defaultNoRawString : T

    val singleIf : T

    val ifStatement : T

    val embeddingDirective : T

    val objectDeclaration : T

    val placeholderDefinition : T

    val placeholderInvocation : T

    val placeholderUse : T

    val defaultNoRawBlock : T

    val rawBlock : T

    val partialRawBlock : T

    val kateUnit : T

    val runtimeWriteString : T

    val runtimeWriteChar : T

    val functionDefinition : T

    val variableAssignment : T

    val variableDeclaration : T

}