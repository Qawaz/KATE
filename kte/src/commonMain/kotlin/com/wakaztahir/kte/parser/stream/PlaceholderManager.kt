package com.wakaztahir.kte.parser.stream

import com.wakaztahir.kte.model.PlaceholderBlock

interface PlaceholderManager {

    val placeholders: MutableList<PlaceholderBlock>
    val undefinedPlaceholders: MutableList<PlaceholderBlock>

    fun definePlaceholder(placeholder: PlaceholderBlock) {
        val iterator = placeholders.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next.placeholderName == placeholder.placeholderName) {
                if (next.definitionName == placeholder.definitionName) {
                    throw IllegalStateException("placeholder with name ${placeholder.placeholderName} and definition name ${placeholder.definitionName} already exists , please use a different definition name")
                }
                undefinedPlaceholders.add(next)
                iterator.remove()
            }
        }
        placeholders.add(placeholder)
    }

    fun getPlaceholder(placeholderName: String): PlaceholderBlock? {
        val index = placeholders.indexOfFirst {
            it.placeholderName == placeholderName
        }
        return if (index > -1) {
            placeholders[index]
        } else {
            null
        }
    }

    fun checkIsBeingUsed(placeholderName: String, definitionName: String): Boolean {
        return placeholders.any { it.placeholderName == placeholderName && it.definitionName == definitionName }
    }

    fun usePlaceholder(placeholderName: String, definitionName: String): Boolean {
        val index = undefinedPlaceholders.indexOfFirst {
            it.placeholderName == placeholderName && it.definitionName == definitionName
        }
        return if (index > -1) {
            definePlaceholder(undefinedPlaceholders.removeAt(index))
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
            placeholders.removeAt(index)
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