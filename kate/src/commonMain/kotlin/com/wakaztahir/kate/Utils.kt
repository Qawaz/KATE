package com.wakaztahir.kate

import com.wakaztahir.kate.model.ModelReference
import com.wakaztahir.kate.model.model.MutableKTEObject
import com.wakaztahir.kate.model.model.ReferencedValue


@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "Must be used with care"
)
annotation class KTEDelicateFunction

const val GlobalModelObjectName = "Global"
const val ScopedModelObjectName = "ScopedObject"

val EmptyReferencedValuesList = emptyList<ReferencedValue>()

val GetTypeModelReference = ModelReference.FunctionCall(
    name = "getType",
    invokeOnly = false,
    parametersList = EmptyReferencedValuesList
)

@Suppress("FunctionName")
internal fun GenerateCode(code: String): String = TemplateContext(code).getDestinationAsString()

@Suppress("FunctionName")
internal fun GeneratePartialRaw(code: String) = GenerateCode("@partial_raw $code @end_partial_raw")

@Suppress("FunctionName")
internal fun GenerateCode(code: String, model: MutableKTEObject) = TemplateContext(code, model).getDestinationAsString()