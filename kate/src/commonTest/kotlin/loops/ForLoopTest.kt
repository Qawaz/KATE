package loops

import GenerateCode
import GeneratePartialRaw
import com.wakaztahir.kate.TemplateContext
import com.wakaztahir.kate.dsl.UnresolvedValueException
import com.wakaztahir.kate.model.*
import com.wakaztahir.kate.model.model.KATEListImpl
import com.wakaztahir.kate.model.model.MutableKATEObject
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
        val loop = context.stream.block.parseForLoop()!! as ForLoop.ConditionalFor
        assertEquals("blockValue", loop.forLoopBlock.generateToText())
    }

    @Test
    fun parseForLoopIterable() {
        val context = TemplateContext("@for(@var listName : @var(mList)) blockValue @endfor")
        context.stream.model.insertValue(
            "mList",
            KATEListImpl(listOf(1, 2, 3).map { IntValue(it) }, itemType = KATEType.Int)
        )
        var loop = context.stream.block.parseForLoop()!! as ForLoop.IterableFor
        assertEquals("listName", loop.elementConstName)
        assertEquals(null, loop.indexConstName)
        assertEquals("blockValue", loop.forLoopBlock.generateToText())
        assertEquals("mList", (loop.listProperty as ModelDirective).propertyPath[0].name)
        context.updateStream("@for(@var listName,indexName : @var(mList)) blockValue @endfor")
        loop = context.stream.block.parseForLoop()!! as ForLoop.IterableFor
        assertEquals("indexName", loop.indexConstName)
    }

    @Test
    fun parseForLoopNumbered() {
        val operatorType = ArithmeticOperatorType.Plus
        val code = "@for(@var i=0;i<5;i${operatorType.char}1) blockValue @endfor"
        val context = TemplateContext(code)
        val loop = context.stream.block.parseForLoop()!! as ForLoop.NumberedFor
        assertEquals("i", loop.variableName)
        assertEquals(0, loop.initializer.asPrimitive().value)
        assertEquals(5, loop.conditional.asPrimitive().value)
        assertEquals(operatorType, loop.arithmeticOperatorType)
        assertEquals(1, loop.incrementer.asPrimitive().value)
        assertEquals("blockValue", loop.forLoopBlock.generateToText())
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
        val context = TemplateContext("@var runs = 0@for(@var i=0;i<3;i+1) @set_var runs = @var(i) @endfor@var(runs)")
        assertEquals("2", context.getDestinationAsString())
    }

    @Test
    fun testForLoopBreaking() {
        assertEquals(
            expected = "0123",
            actual = GenerateCode("@for(@var i = 0;i<7;i++) @if(@var(i)==4) @break @else @var(i) @endif @endfor")
        )
        assertEquals(
            expected = "015016017",
            actual = GenerateCode("@for(@var i = 5;i<8;i++) @for(@var j = 0;j<3;j++) @if(@var(j) == 2) @break @else @var(j) @endif @endfor @var(i) @endfor")
        )
    }

    @Test
    fun testLoopModelClear() {
        assertEquals("01",GenerateCode("@for(@var i = 0;i<2;i++) @if(true) @var x = i @var(x) @endif @endfor"))
        assertEquals("543", GenerateCode("@var i = 5 @for(@var(i) > 2) @var(i)@set_var i = i - 1 @endfor"))
        assertEquals("012", GenerateCode("@for(@var i = 0;i<3;i++) @var f = @var(i) @var(f) @endfor"))
        assertEquals("012", GenerateCode("@for(@var i : @list(0,1,2)) @var f = @var(i) @var(f) @endfor"))
        assertEquals("012345",
            GenerateCode("@var i = 0 @for(true) @if(i > 5) @break @endif @var f = @var(i) @set_var i = i + 1 @var(f) @endfor")
        )
    }

    @Test
    fun testLoopContinue() {
        assertEquals("0123567",GenerateCode("@for(@var i = 0;i < 8;i++) @if(i == 4) @continue @endif @var(i) @endfor"))
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
        assertEquals("xxxxx", GenerateCode("@var j = 5 @for(@var i = 0;i<j;i+1) x @endfor"))
        assertEquals("xxxxxx", GenerateCode("@var j = 5 @for(@var i = 0;i<j+1;i+1) x @endfor"))
    }

    @Test
    fun testForLoopGeneration4() {
        val context = TemplateContext("@for(@var elem : @var(list)) @var(elem) @endfor", MutableKATEObject {
            insertValue(
                "list",
                KATEListImpl(listOf("H", "e", "ll", "o").map { StringValue(it) }, itemType = KATEType.String)
            )
        })
        assertEquals("Hello", context.getDestinationAsString())
    }

    @Test
    fun testForLoopGeneration5() {
        val kteObject = MutableKATEObject {
            insertValue(
                "list",
                KATEListImpl(listOf("H", "e", "ll", "o").map { StringValue(it) }, itemType = KATEType.String)
            )
        }
        val context = TemplateContext(
            "@var(list.size())@var(list.get(2))@var(list.contains(\"ll\"))@var(list.contains(\"v\"))",
            kteObject
        )
        assertEquals("4lltruefalse", context.getDestinationAsString())
    }

    @Test
    fun testForLoopGenerationWithDirectReference() {
        val context = TemplateContext("@for(@var elem : list) @var(elem) @endfor", MutableKATEObject {
            insertValue(
                key = "list",
                value = KATEListImpl(listOf("H", "e", "ll", "o").map { StringValue(it) }, KATEType.String)
            )
        })
        assertEquals("Hello", context.getDestinationAsString())
    }

    @Test
    fun testForLoopGenerationWithList() {
        assertEquals("123", GenerateCode("@for(@var elem : @list(1,2,3)) @var(elem) @endfor"))
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
            )
        )
        assertEquals(
            "",
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

    @Test
    fun testForLoopPropagatesPartialRaw() {
        assertEquals(
            expected = "xx",
            actual = GeneratePartialRaw(
                """@function print() @default_no_raw x @end_default_no_raw @end_function
            @for(@var i = 0;i<2;i++) print() @endfor
            """.trimIndent()
            )
        )
    }


}