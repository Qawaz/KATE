import com.wakaztahir.kte.TemplateContext
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
        assertEquals("blockValue", loop.blockValue.getValueAsString(context.stream))
    }

    @Test
    fun parseForLoopIterable() {
        val context = TemplateContext("@for(@const listName : @model.mList) blockValue @endfor")
        var loop = context.stream.parseForLoop(context.stream)!! as ForLoop.IterableFor
        assertEquals("listName", loop.elementConstName)
        assertEquals(null, loop.indexConstName)
        assertEquals("blockValue", loop.blockValue.getValueAsString(context.stream))
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
        assertEquals(0, loop.initializer.getValue(context.stream.model).value)
        assertEquals(5, loop.conditional.getValue(context.stream.model).value)
        assertEquals(operatorType, loop.arithmeticOperatorType)
        assertEquals(1, loop.incrementer.getValue(context.stream.model).value)
        assertEquals(code.length,loop.blockValue.blockEndPointer)
        assertEquals("blockValue", loop.blockValue.getValueAsString(context.stream))
    }

    @Test
    fun testForLoopGeneration() {
        val context = TemplateContext("@const var1 = 3@for(@const i = @const(var1);i>0;i-1) @const(var1) @endfor")
        assertEquals("333", context.getDestinationAsStringWithReset())
    }

    @Test
    fun testForLoopGeneration1() {
        val context = TemplateContext("@for(@const i=0;i<3;i+1) @if(@const(i)==2) @const(i) @endif @endfor")
        assertEquals("2", context.getDestinationAsStringWithReset())
    }

    @Test
    fun testForLoopRuns(){
        val context = TemplateContext("@const runs = 0@for(@const i=0;i<3;i+1) @const runs = @const(runs) @+ 1 @endfor@const(runs)")
        assertEquals("2", context.getDestinationAsStringWithReset())
    }

    @Test
    fun testMultiForLoop(){
        val context = TemplateContext("@for(@const i=0;i<3;i+1) @for(@const j=0;j<3;j++) 0 @endfor @endfor")
        assertEquals("000000000", context.getDestinationAsStringWithReset())
    }

    @Test
    fun testForLoopGeneration2() {
        val context = TemplateContext("@for(@const i=0;i<3;i+1) @const(i) @endfor")
        assertEquals("012", context.getDestinationAsStringWithReset())
    }


    @Test
    fun testForLoopGeneration3() {
        val context = TemplateContext("@for(@const i = 0;i<5;i+1) x @endfor")
        assertEquals("xxxxx", context.getDestinationAsStringWithReset())
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