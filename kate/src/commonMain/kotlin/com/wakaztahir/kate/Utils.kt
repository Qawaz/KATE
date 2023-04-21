package com.wakaztahir.kate

@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "Must be used with care"
)
annotation class KATEDelicateFunction

internal const val GlobalModelObjectName = "Global"
internal const val ScopedModelObjectName = "ScopedObject"