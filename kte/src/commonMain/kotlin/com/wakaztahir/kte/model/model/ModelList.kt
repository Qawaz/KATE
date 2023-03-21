package com.wakaztahir.kte.model.model

import com.wakaztahir.kte.model.KTEValue

interface ModelList<T : KTEValue> : List<T>, TemplateModel, KTEValue