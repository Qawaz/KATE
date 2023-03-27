package com.wakaztahir.kte

import com.wakaztahir.kte.model.model.MutableKTEObject


@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "Must be used with care"
)
annotation class KTEDelicateFunction

@Suppress("FunctionName")
internal fun GenerateCode(code: String): String = TemplateContext(code).getDestinationAsString()

@Suppress("FunctionName")
internal fun GenerateCode(code: String, model: MutableKTEObject) = TemplateContext(code, model).getDestinationAsString()