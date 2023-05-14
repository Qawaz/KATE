package com.wakaztahir.kate.model

interface BlockContainer : AtDirective {

    fun getBlockValue() : LazyBlock? = null

}