import com.wakaztahir.kate.GenerateCode
import com.wakaztahir.kate.TemplateContext
import com.wakaztahir.kate.dsl.UnresolvedValueException
import com.wakaztahir.kate.model.model.KATEListImpl
import com.wakaztahir.kate.model.ModelDirective
import com.wakaztahir.kate.model.StringValue
import com.wakaztahir.kate.model.asPrimitive
import com.wakaztahir.kate.model.model.MutableKTEObject
import com.wakaztahir.kate.parser.ArithmeticOperatorType
import com.wakaztahir.kate.parser.ForLoop
import com.wakaztahir.kate.parser.parseForLoop
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ForLoopTest {
    @Test
    fun parseForLoop() {
        val context = TemplateContext("@for(true) blockValue @endfor")
        val loop = context.stream.parseForLoop()!! as ForLoop.ConditionalFor
        assertEquals("blockValue", loop.blockValue.getValueAsString())
    }

    @Test
    fun parseForLoopIterable() {
        val context = TemplateContext("@for(@var listName : @var(mList)) blockValue @endfor")
        var loop = context.stream.parseForLoop()!! as ForLoop.IterableFor
        assertEquals("listName", loop.elementConstName)
        assertEquals(null, loop.indexConstName)
        assertEquals("blockValue", loop.blockValue.getValueAsString())
        assertEquals("mList", (loop.listProperty as ModelDirective).propertyPath[0].name)
        context.updateStream("@for(@var listName,indexName : @var(list)) blockValue @endfor")
        loop = context.stream.parseForLoop()!! as ForLoop.IterableFor
        assertEquals("indexName", loop.indexConstName)
    }

    @Test
    fun parseForLoopNumbered() {
        val operatorType = ArithmeticOperatorType.Plus
        val code = "@for(@var i=0;i<5;i${operatorType.char}1) blockValue @endfor"
        val context = TemplateContext(code)
        val loop = context.stream.parseForLoop()!! as ForLoop.NumberedFor
        assertEquals("i", loop.variableName)
        assertEquals(0, loop.initializer.asPrimitive(context.stream.model).value)
        assertEquals(5, loop.conditional.asPrimitive(context.stream.model).value)
        assertEquals(operatorType, loop.arithmeticOperatorType)
        assertEquals(1, loop.incrementer.asPrimitive(context.stream.model).value)
        assertEquals(code.length, loop.blockValue.blockEndPointer)
        assertEquals("blockValue", loop.blockValue.getValueAsString())
    }

    @Test
    fun testForLoopGeneration() {
        val context = TemplateContext("@var var1 = 3@for(@var i = @var(var1);i>0;i-1) @var(var1) @endfor")
        assertEquals("333", context.getDestinationAsString())
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
    fun testForLoopBreaking() {
        assertEquals(
            expected = "0123",
            actual = GenerateCode("@for(@var i = 0;i<7;i++) @if(@var(i)==4) @breakfor @else @var(i) @endif @endfor")
        )
        assertEquals(
            expected = "015016017",
            actual = GenerateCode("@for(@var i = 5;i<8;i++) @for(@var j = 0;j<3;j++) @if(@var(j) == 2) @breakfor @else @var(j) @endif @endfor @var(i) @endfor")
        )
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
            expected = "000000000",
            GenerateCode("@for(@var i=0;i<3;i+1) @var g = 0@for(@var j=0;j<3;j++) @var(g) @endfor @endfor")
        )
    }

    @Test
    fun testForLoopGeneration2() {
        assertEquals("012", GenerateCode("@for(@var i=0;i<3;i+1) @var(i) @endfor"))
        assertEquals("xxxxx", GenerateCode("@for(@var i = 0;i<5;i+1) x @endfor"))
    }

    @Test
    fun testForLoopGeneration4() {
        val context = TemplateContext("@for(@var elem : @var(list)) @var(elem) @endfor", MutableKTEObject {
            putValue("list", KATEListImpl(listOf("H", "e", "ll", "o").map { StringValue(it) }))
        })
        assertEquals("Hello", context.getDestinationAsString())
    }

    @Test
    fun testForLoopGeneration5() {
        val kteObject = MutableKTEObject {
            putValue("list", KATEListImpl(listOf("H", "e", "ll", "o").map { StringValue(it) }))
        }
        val context = TemplateContext(
            "@var(list.size())@var(list.get(2))@var(list.contains(\"ll\"))@var(list.contains(\"v\"))",
            kteObject
        )
        assertEquals("4lltruefalse", context.getDestinationAsString())
    }

    @Test
    fun testForLoopIndentation() {
        assertEquals(
            expected = "Line Number 1\nLine Number 2",
            actual = GenerateCode(
                """@for(@var i=0;i<1;i+1)
              |${'\t'}Line Number 1
              |${'\t'}Line Number 2
              |@endfor""".trimMargin("|")
            ))
        assertEquals("",
            actual = GenerateCode(
                """@for(@var i=2;i<2;i+1)
              |${'\t'}Line Number 1
              |${'\t'}Line Number 2
              |@endfor""".trimMargin("|")
            )
        )
        assertEquals(
            expected = "\tLine Number 1\n\tLine Number 2",
            actual = GenerateCode(
                """@for(@var i=0;i<1;i+1)
              |${'\t'}${'\t'}Line Number 1
              |${'\t'}${'\t'}Line Number 2
              |@endfor""".trimMargin("|")
            )
        )
        assertEquals(
            expected = "Line Number 1\nLine Number 2",
            actual = GenerateCode(
                """@for(@var i=0;i<1;i+1)
              |${'\t'}@for(@var j=0;j<1;j++)
              |${'\t'}${'\t'}Line Number 1
              |${'\t'}${'\t'}Line Number 2
              |${'\t'}@endfor
              |@endfor""".trimMargin("|")
            )
        )
    }


}