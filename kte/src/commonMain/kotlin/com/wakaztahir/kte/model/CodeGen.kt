package com.wakaztahir.kte.model

import com.wakaztahir.kte.model.model.MutableTemplateModel
import com.wakaztahir.kte.parser.stream.DestinationStream
import com.wakaztahir.kte.parser.stream.SourceStream

interface CodeGen {

    fun generateTo(model : MutableTemplateModel, source : SourceStream,destination  :DestinationStream)

}