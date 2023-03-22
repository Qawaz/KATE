import com.wakaztahir.kte.GenerateCode
import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.dsl.ScopedModelObject
import com.wakaztahir.kte.dsl.UnresolvedValueException
import com.wakaztahir.kte.model.model.ModelListImpl
import com.wakaztahir.kte.model.ModelDirective
import com.wakaztahir.kte.model.StringValue
import com.wakaztahir.kte.model.model.TemplateModel
import com.wakaztahir.kte.parser.ArithmeticOperatorType
import com.wakaztahir.kte.parser.ForLoop
import com.wakaztahir.kte.parser.parseForLoop
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ForLoopTest {
    @Test
    fun parseForLoop() {
        val context = TemplateContext("@for(true) blockValue @endfor")
        val loop = context.stream.parseForLoop(context.stream)!! as ForLoop.ConditionalFor
        assertEquals("blockValue", loop.blockValue.getValueAsString(context.stream))
    }

    @Test
    fun parseForLoopIterable() {
        val context = TemplateContext("@for(@var listName : @model.mList) blockValue @endfor")
        var loop = context.stream.parseForLoop(context.stream)!! as ForLoop.IterableFor
        assertEquals("listName", loop.elementConstName)
        assertEquals(null, loop.indexConstName)
        assertEquals("blockValue", loop.blockValue.getValueAsString(context.stream))
        assertEquals("mList", (loop.listProperty as ModelDirective).propertyPath[0].name)
        context.updateStream("@for(@var listName,indexName : @model.list) blockValue @endfor")
        loop = context.stream.parseForLoop(context.stream)!! as ForLoop.IterableFor
        assertEquals("indexName", loop.indexConstName)
    }

    @Test
    fun parseForLoopNumbered() {
        val operatorType = ArithmeticOperatorType.Plus
        val code = "@for(@var i=0;i<5;i${operatorType.char}1) blockValue @endfor"
        val context = TemplateContext(code)
        val loop = context.stream.parseForLoop(context.stream)!! as ForLoop.NumberedFor
        assertEquals("i", loop.variableName)
        assertEquals(0, loop.initializer.getValue(context.stream.model).value)
        assertEquals(5, loop.conditional.getValue(context.stream.model).value)
        assertEquals(operatorType, loop.arithmeticOperatorType)
        assertEquals(1, loop.incrementer.getValue(context.stream.model).value)
        assertEquals(code.length, loop.blockValue.blockEndPointer)
        assertEquals("blockValue", loop.blockValue.getValueAsString(context.stream))
    }

    @Test
    fun testForLoopGeneration() {
        val context = TemplateContext("@var var1 = 3@for(@var i = @var(var1);i>0;i-1) @var(var1) @endfor")
        assertEquals("333", context.getDestinationAsString())
    }

    @Test
    fun testModelHierarchy() {
        val model = TemplateModel { }
        val scoped = ScopedModelObject(model)
        val scopedAnother = ScopedModelObject(scoped)
        model.putValue("hello", "yes")
        assertEquals("yes", TemplateContext("@var(hello)", scopedAnother).getDestinationAsString())
    }

    @Test
    fun testForLoopGeneration1() {
        val context = TemplateContext("@for(@var i=0;i<3;i+1) @if(@var(i)==2) @var(i) @endif @endfor")
        assertEquals("2", context.getDestinationAsString())
    }

    @Test
    fun testForLoopRuns() {
        val context = TemplateContext("@var runs = 0@for(@var i=0;i<3;i+1) @var runs = @var(i) @endfor@var(runs)")
        assertEquals("2", context.getDestinationAsString())
    }

    @Test
    fun testLoopVariables() {
        assertFailsWith(UnresolvedValueException::class) {
            GenerateCode("@for(@var i=0;i<3;i+1)@endfor@var(i)")
        }
        assertEquals("3", GenerateCode("@var x=3@for(@var i=0;i<3;i+1)@endfor@var(x)"))
        assertFailsWith(UnresolvedValueException::class) {
            GenerateCode("@for(@var i=0;i<3;i+1)@var f = 4@endfor@var(f)")
        }
        assertEquals(
            expected = "000111222",
            GenerateCode("@for(@var i=0;i<3;i+1) @var g = 0@for(@var j=0;j<3;j++) @var(g) @endfor @endfor")
        )
    }

    @Test
    fun testForLoopGeneration2() {
        val context = TemplateContext("@for(@var i=0;i<3;i+1) @var(i) @endfor")
        assertEquals("012", context.getDestinationAsStringWithReset())
    }


    @Test
    fun testForLoopGeneration3() {
        val context = TemplateContext("@for(@var i = 0;i<5;i+1) x @endfor")
        assertEquals("xxxxx", context.getDestinationAsStringWithReset())
    }

    @Test
    fun testForLoopGeneration4() {
        val context = TemplateContext("@for(@var elem : @model.list) @var(elem) @endfor", TemplateModel {
            putValue("list", ModelListImpl(listOf("H", "e", "ll", "o").map { StringValue(it) }))
        })
        assertEquals("Hello", context.getDestinationAsStringWithReset())
    }

    @Test
    fun testForLoopGeneration5() {
        val context = TemplateContext("@model.list.size", TemplateModel {
            putValue("list", ModelListImpl(listOf("H", "e", "ll", "o").map { StringValue(it) }))
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
            """@for(@var i=0;i<2;i+1)
              | Line Number 1
              | Line Number 2
              |@endfor""".trimMargin("|")
        )
        assertEquals("Line Number 1\nLine Number 2", context.getDestinationAsStringWithReset())
        val context2 = TemplateContext(
            """@for(@var i=2;i<2;i+1)
              | Line Number 1
              | Line Number 2
              |@endfor""".trimMargin("|")
        )
        assertEquals("", context2.getDestinationAsStringWithReset())
    }


}