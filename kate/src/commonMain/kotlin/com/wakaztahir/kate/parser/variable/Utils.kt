package com.wakaztahir.kate.parser.variable

internal fun Char.isVariableName(): Boolean = this.isLetterOrDigit() || this == '_'

private fun isLanguageKeyword(varName: String): Boolean {
    return when (varName) {
        "this" -> true
        "parent" -> true
        else -> false
    }
}

internal fun isValidVariableName(name: String): Result<Boolean> {
    if (name.isEmpty()) {
        return Result.failure(Throwable("variable name cannot be empty"))
    }
    if (name[0].isDigit()) {
        return Result.failure(Throwable("variable name cannot begin with a digit"))
    }
    if (isLanguageKeyword(name)) {
        return Result.failure(Throwable("variable name \"$name\" is a language keyword"))
    }
    return Result.success(true)
}