package com.wakaztahir.kate.model

import com.wakaztahir.kate.model.model.KATEValue
import com.wakaztahir.kate.model.model.ReferencedOrDirectValue
import com.wakaztahir.kate.parser.ParsedBlock
import com.wakaztahir.kate.parser.stream.DestinationStream
import com.wakaztahir.kate.tokenizer.NodeTokenizer


enum class ConditionType {
    ReferentiallyEquals {
        override fun compare(first: KATEValue, second: KATEValue): Boolean =
            first === second

        override fun verifyCompare(result: Int) = result == 0
    },
    Equals {
        override fun compare(first: KATEValue, second: KATEValue) =
            first.compareTo(second) == 0

        override fun verifyCompare(result: Int) = result == 0
    },
    NotEquals {
        override fun compare(first: KATEValue, second: KATEValue) =
            first.compareTo(second) != 0

        override fun verifyCompare(result: Int) = result != 0
    },
    GreaterThan {
        override fun compare(first: KATEValue, second: KATEValue) =
            first.compareTo(second) > 0

        override fun verifyCompare(result: Int) = result == 1
    },
    LessThan {
        override fun compare(first: KATEValue, second: KATEValue) =
            first.compareTo(second) < 0

        override fun verifyCompare(result: Int) = result == -1
    },
    GreaterThanEqualTo {
        override fun compare(first: KATEValue, second: KATEValue) =
            first.compareTo(second).let { it > 0 || it == 0 }

        override fun verifyCompare(result: Int) = result == 1 || result == 0
    },
    LessThanEqualTo {
        override fun compare(first: KATEValue, second: KATEValue) =
            first.compareTo(second).let { it < 0 || it == 0 }

        override fun verifyCompare(result: Int) = result == -1 || result == 0
    };

    abstract fun compare(first: KATEValue, second: KATEValue): Boolean
    abstract fun verifyCompare(result: Int): Boolean

}

interface Condition : ReferencedOrDirectValue {

    fun evaluate(): Boolean

    override fun getKATEValue(): KATEValue {
        return BooleanValue(evaluate())
    }

}

internal class LogicalCondition(
    val propertyFirst: ReferencedOrDirectValue,
    val type: ConditionType,
    val propertySecond: ReferencedOrDirectValue
) : Condition {
    override fun evaluate(): Boolean {
        return type.compare(propertyFirst.getKATEValue(), propertySecond.getKATEValue())
    }
}

enum class IfType(val order: Int) {
    If(0),
    ElseIf(1),
    Else(2)
}

class IfParsedBlock(val provider: ModelProvider, codeGens: List<CodeGenRange>) : ParsedBlock(codeGens) {
    override fun generateTo(destination: DestinationStream) {
        provider.model.removeAll()
        super.generateTo(destination)
    }
}

class SingleIf(
    val condition: ReferencedOrDirectValue,
    val type: IfType,
    override val parsedBlock: IfParsedBlock,
) : BlockContainer {
    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.singleIf
    override fun generateTo(destination: DestinationStream) {
        parsedBlock.generateTo(destination = destination)
    }
}


class IfStatement(private val ifs: MutableList<SingleIf>) : MultipleBlocksContainer {

    val singleIfs: List<SingleIf> get() = ifs

    override fun <T> selectNode(tokenizer: NodeTokenizer<T>): T = tokenizer.ifStatement

    private fun sortByOrder() {
        ifs.sortBy { it.type.order }
    }

    init {
        sortByOrder()
    }

    fun evaluate(): SingleIf? {
        for (iffy in ifs) {
            if ((iffy.condition.asNullablePrimitive()!! as BooleanValue).value) {
                return iffy
            }
        }
        return null
    }

    override fun generateTo(destination: DestinationStream) {
        evaluate()?.generateTo(destination)
    }
}