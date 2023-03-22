package com.wakaztahir.kte


@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "Must be used with care"
)
annotation class KTEDelicateFunction

@Suppress("FunctionName")
internal fun GenerateCode(code: String): String = TemplateContext(code).getDestinationAsString()