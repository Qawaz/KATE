package com.wakaztahir.kte.model.model

import com.wakaztahir.kte.dsl.UnresolvedValueException
import com.wakaztahir.kte.model.*
import com.wakaztahir.kte.parser.stream.DestinationStream

interface KTEObject : ReferencedValue {

    val objectName: String
    val contained: Map<String, KTEValue>

    fun getModelDirectiveValue(directive: ModelDirective): KTEValue {
        var currentVal: KTEValue = this
        for (prop in directive.propertyPath) {
            when (prop) {
                is ModelReference.FunctionCall -> {
                    (currentVal.getModelReference(prop) as? KTEFunction)?.let { func ->
                        func.parameters.clear()
                        func.parameters.addAll(prop.parametersList)
                        func.invokedOn = currentVal
                        func.invokeOnly = prop.invokeOnly
                        currentVal = func
                    } ?: run {
                        throw UnresolvedValueException("function ${directive.pathUntil(prop)} does not exist")
                    }
                }

                is ModelReference.Property -> {
                    currentVal = currentVal.getModelReference(prop) ?: run {
                        if (directive.propertyPath.size == 1 && prop.name == "this") {
                            currentVal
                        } else {
                            throw UnresolvedValueException("property ${directive.pathUntil(prop)} does not exist")
                        }
                    }
                }
            }
        }
        return currentVal
    }

    override fun generateTo(block: LazyBlock, destination: DestinationStream) {
        destination.write(block, this)
    }

    fun traverse(block: (KTEValue) -> Unit) {
        block(this)
        for (each in contained) {
            when (each.value) {
                is KTEList<*> -> {
                    for (item in (each.value as KTEList<*>).collection) block(item)
                }

                is KTEObject -> {
                    (each.value as KTEObject).traverse(block)
                }

                else -> {
                    block(each.value)
                }
            }
        }
    }

}