import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.model.ConditionType
import com.wakaztahir.kte.model.model.ModelListImpl
import com.wakaztahir.kte.model.ModelDirective
import com.wakaztahir.kte.model.StringValue
import com.wakaztahir.kte.model.model.TemplateModel
import com.wakaztahir.kte.parser.ArithmeticOperatorType
import com.wakaztahir.kte.parser.ForLoop
import com.wakaztahir.kte.parser.parseForLoop
import kotlin.test.Test
import kotlin.test.assertEquals

class ForLoopTest {
    @Test
    fun parseForLoop() {
        val context = TemplateContext("@for(true) blockValue @endfor")
        val loop = context.stream.parseForLoop(context.stream)!! as ForLoop.ConditionalFor
        assertEquals("blockValue", loop.parser.getValueAsString(context.stream))
    }

    @Test
    fun parseForLoopIterable() {
        val context = TemplateContext("@for(@const listName : @model.mList) blockValue @endfor")
        var loop = context.stream.parseForLoop(context.stream)!! as ForLoop.IterableFor
        assertEquals("listName", loop.elementConstName)
        assertEquals(null, loop.indexConstName)
        assertEquals("blockValue", loop.parser.getValueAsString(context.stream))
        assertEquals("mList", (loop.listProperty as ModelDirective).propertyPath[0].name)
        context.updateStream("@for(@const listName,indexName : @model.list) blockValue @endfor")
        loop = context.stream.parseForLoop(context.stream)!! as ForLoop.IterableFor
        assertEquals("indexName", loop.indexConstName)
    }

    @Test
    fun parseForLoopNumbered() {
        val operatorType = ArithmeticOperatorType.Plus
        val code = "@for(@const i=0;i<5;i${operatorType.char}1) blockValue @endfor"
        val context = TemplateContext(code)
        val loop = context.stream.parseForLoop(context.stream)!! as ForLoop.NumberedFor
        assertEquals("i", loop.variableName)
        assertEquals(0, loop.initializer.getPrimitive(context.stream.model).value)
        assertEquals(5, loop.conditional.getPrimitive(context.stream.model).value)
        assertEquals(operatorType, loop.arithmeticOperatorType)
        assertEquals(1, loop.incrementer.getPrimitive(context.stream.model).value)
        assertEquals("blockValue", loop.parser.getValueAsString(context.stream))
    }

    @Test
    fun testConditionalType() {
        assertEquals(true,ConditionType.Equals.verifyCompare(1.compareTo(1)))
        assertEquals(true,ConditionType.NotEquals.verifyCompare(1.compareTo(2)))
        assertEquals(true,ConditionType.GreaterThan.verifyCompare(2.compareTo(1)))
        assertEquals(true,ConditionType.LessThan.verifyCompare(1.compareTo(2)))
        assertEquals(true,ConditionType.GreaterThanEqualTo.verifyCompare(1.compareTo(1)))
        assertEquals(true,ConditionType.GreaterThanEqualTo.verifyCompare(2.compareTo(1)))
        assertEquals(true,ConditionType.LessThanEqualTo.verifyCompare(1.compareTo(1)))
        assertEquals(true,ConditionType.LessThanEqualTo.verifyCompare(1.compareTo(2)))
    }

    @Test
    fun testForLoopGeneration() {
        val context = TemplateContext("@const var1 = 3@for(@const i = @const(var1);i>0;i-1) @const(var1) @endfor")
        assertEquals("333", context.getDestinationAsString())
    }

    @Test
    fun testMultipleForBlocks() {
        val context = TemplateContext("@for(@const i=0;i<3;i+1) @for(@const j=0;j<3;j+1) @const(i) @endfor @endfor")
        assertEquals("2", context.getDestinationAsString())
    }

    @Test
    fun testForLoopGeneration2() {
        val context = TemplateContext("@for(@const i=0;i<3;i+1) @const(i) @endfor")
        assertEquals("012", context.getDestinationAsString())
    }


    @Test
    fun testForLoopGeneration3() {
        val context = TemplateContext("@for(@const i = 0;i<5;i+1) x @endfor")
        assertEquals("xxxxx", context.getDestinationAsString())
    }

    @Test
    fun testForLoopGeneration4() {
        val context = TemplateContext("@for(@const elem : @model.list) @const(elem) @endfor", TemplateModel {
            putIterable("list", ModelListImpl(listOf("H", "e", "ll", "o").map { StringValue(it) }))
        })
        assertEquals("Hello", context.getDestinationAsStringWithReset())
    }

    @Test
    fun testForLoopGeneration5() {
        val context = TemplateContext("@model.list.size", TemplateModel {
            putIterable("list", ModelListImpl(listOf("H", "e", "ll", "o").map { StringValue(it) }))
        })
        assertEquals("4", context.getDestinationAsStringWithReset())
    }

    @Test
    fun testForLoopGeneration6() {
        val context = TemplateContext("@model.arithmetic.funName", TemplateModel {
            putObject("arithmetic") {
                putValue("funName", "seriouslyHere")
            }
        })
        assertEquals("seriouslyHere", context.getDestinationAsStringWithReset())
    }

    @Test
    fun parseMultilineForLoop() {
        val context = TemplateContext(
            """@for(@const i=0;i<2;i+1)
              | Line Number 1
              | Line Number 2
              |@endfor""".trimMargin("|")
        )
        assertEquals("Line Number 1\nLine Number 2", context.getDestinationAsStringWithReset())
        val context2 = TemplateContext(
            """@for(@const i=2;i<2;i+1)
              | Line Number 1
              | Line Number 2
              |@endfor""".trimMargin("|")
        )
        assertEquals("", context2.getDestinationAsStringWithReset())
    }


}