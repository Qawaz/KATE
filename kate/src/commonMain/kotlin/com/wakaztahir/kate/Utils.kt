package com.wakaztahir.kate

import com.wakaztahir.kate.model.ModelReference
import com.wakaztahir.kate.model.model.ReferencedValue


@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "Must be used with care"
)
annotation class KATEDelicateFunction

internal const val GlobalModelObjectName = "Global"
internal const val ScopedModelObjectName = "ScopedObject"

val EmptyReferencedValuesList = emptyList<ReferencedValue>()

val GetTypeModelReference = ModelReference.FunctionCall(
    name = "getType",
    parametersList = EmptyReferencedValuesList
)