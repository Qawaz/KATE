package com.wakaztahir.kte

import com.wakaztahir.kte.model.ModelReference
import com.wakaztahir.kte.model.model.MutableKTEObject
import com.wakaztahir.kte.model.model.ReferencedValue


@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "Must be used with care"
)
annotation class KTEDelicateFunction

val EmptyReferencedValuesList = emptyList<ReferencedValue>()

val GetTypeModelReference = ModelReference.FunctionCall(
    name = "getType",
    invokeOnly = false,
    parametersList = EmptyReferencedValuesList
)

@Suppress("FunctionName")
internal fun GenerateCode(code: String): String = TemplateContext(code).getDestinationAsString()

@Suppress("FunctionName")
internal fun GenerateCode(code: String, model: MutableKTEObject) = TemplateContext(code, model).getDestinationAsString()