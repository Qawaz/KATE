package com.wakaztahir.kate.parser.stream

import com.wakaztahir.kate.model.PlaceholderBlock

interface PlaceholderManager {

    val placeholders: MutableList<PlaceholderBlock>
    val undefinedPlaceholders: MutableList<PlaceholderBlock>
    val placeholderListeners: MutableMap<String, PlaceholderEventListener>

    interface PlaceholderEventListener {
        fun onPlaceholderUndefined()
        fun onPlaceholderDefined(defined: PlaceholderBlock)
    }

    fun setPlaceholderEventListener(placeholderName: String, listener: PlaceholderEventListener) {
        placeholderListeners[placeholderName] = listener
    }

    fun definePlaceholder(placeholder: PlaceholderBlock, throwIfExists: Boolean) {
        val iterator = placeholders.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next.placeholderName == placeholder.placeholderName) {
                if (next.definitionName == placeholder.definitionName) {
                    if (throwIfExists) {
                        throw IllegalStateException("placeholder with name ${placeholder.placeholderName} and definition name ${placeholder.definitionName} already exists , please use a different definition name")
                    } else {
                        return
                    }
                }
                undefinedPlaceholders.add(next)
                iterator.remove()
                placeholderListeners[next.placeholderName]?.onPlaceholderUndefined()
            }
        }
        placeholders.add(placeholder)
        placeholderListeners[placeholder.placeholderName]?.onPlaceholderDefined(placeholder)
    }

    fun getPlaceholder(placeholderName: String): PlaceholderBlock? {
        return placeholders.find { it.placeholderName == placeholderName }
    }

    fun getPlaceholder(placeholderName: String, definitionName: String): PlaceholderBlock? {
        placeholders.find { it.placeholderName == placeholderName && it.definitionName == definitionName }
            ?.let { return it }
        undefinedPlaceholders.find { it.placeholderName == placeholderName && it.definitionName == definitionName }
            ?.let { return it }
        return null
    }

    fun checkIsBeingUsed(placeholderName: String, definitionName: String): Boolean {
        return placeholders.any { it.placeholderName == placeholderName && it.definitionName == definitionName }
    }

    fun usePlaceholder(placeholderName: String, definitionName: String): Boolean {
        val index = undefinedPlaceholders.indexOfFirst {
            it.placeholderName == placeholderName && it.definitionName == definitionName
        }
        return if (index > -1) {
            definePlaceholder(undefinedPlaceholders.removeAt(index), throwIfExists = true)
            true
        } else {
            return checkIsBeingUsed(placeholderName, definitionName)
        }
    }

    fun removePlaceholder(placeholderName: String, definitionName: String): Boolean {
        val index = placeholders.indexOfFirst {
            it.placeholderName == placeholderName && it.definitionName == definitionName
        }
        return if (index > -1) {
            placeholders.removeAt(index).also {
                placeholderListeners[it.placeholderName]?.onPlaceholderUndefined()
            }
            true
        } else {
            val undefinedIndex = undefinedPlaceholders.indexOfFirst {
                it.placeholderName == placeholderName && it.definitionName == definitionName
            }
            if (undefinedIndex > -1) {
                undefinedPlaceholders.removeAt(undefinedIndex)
                true
            } else {
                false
            }
        }
    }

}