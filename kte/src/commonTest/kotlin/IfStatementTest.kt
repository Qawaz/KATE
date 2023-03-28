import com.wakaztahir.kte.GenerateCode
import com.wakaztahir.kte.KTEDelicateFunction
import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.model.LogicalCondition
import com.wakaztahir.kte.model.asPrimitive
import com.wakaztahir.kte.parser.*
import com.wakaztahir.kte.parser.parseCondition
import com.wakaztahir.kte.parser.parseVariableDeclaration
import com.wakaztahir.kte.parser.parseIfStatement
import kotlin.test.*

class IfStatementTest {

    private fun TemplateContext.evaluateConstants(var1: String, condition: String, var2: String): Boolean {
        updateStream("@var var1 = $var1")
        stream.parseVariableDeclaration()!!.storeValue(stream.model)
        updateStream("@var var2 = $var2")
        stream.parseVariableDeclaration()!!.storeValue(stream.model)
        updateStream("@var(var1) $condition @var(var2)")
        return stream.parseCondition()!!.evaluate(stream.model)
    }

    private fun evaluate(var1: String, condition: String, var2: String, constants: Boolean = true): Boolean {
        val context = TemplateContext(("$var1 $condition $var2"))
        val result = context.stream.parseCondition()!!.evaluate(context)
        val result2 = if (constants) context.evaluateConstants(var1, condition, var2) else true
        return result && result2
    }

    @Test
    fun testConstRef() {
        val context = TemplateContext("@if(@var(var1)) blockValue @endif")
        context.stream.model.putValue("var1", true)
        assertEquals("blockValue", context.getDestinationAsString())
    }

    @Test
    fun testInfamousIssue() {
        assertEquals(
            expected = "45",
            actual = GenerateCode("@var j = 4 @if(true) @var i = 5 @if(true) @var l = 5 @var(j)@var(i) @endif @endif")
        )
    }

    @Test
    fun testConstRefParsing() {
        val text = "@if(@var(var1)) blockValue @endif"
        val context = TemplateContext(text)
        context.stream.model.putValue("var1", true)
        val statement = context.stream.parseIfStatement()!!
        assertEquals("blockValue", statement.singleIfs[0].blockValue.getValueAsString())
        assertNotEquals(null, statement.evaluate(context))
        assertEquals(text.length, context.stream.pointer)
    }

    @Test
    fun testUnequalCondition() {
        val context = TemplateContext(("\"ValueOne\" == \"SecondValue\""))
        val condition = context.stream.parseCondition()!! as LogicalCondition
        assertEquals("ValueOne", condition.propertyFirst.asPrimitive(context.stream.model).value)
        assertEquals("SecondValue", condition.propertySecond.asPrimitive(context.stream.model).value)
    }

    @Test
    fun testNestedIfReference() {
        val context = TemplateContext("@var i = 4@if(true) @if(true) @var(i) @endif @endif")
        assertEquals("4", context.getDestinationAsString())
    }

    @Test
    fun testStringEquality() {
        assertTrue(evaluate("\"SecondValue\"", "!=", "\"FirstValue\""))
        assertTrue(evaluate("\"FirstValue\"", "==", "\"FirstValue\""))
    }

    @Test
    fun testBooleansEquality() {
        assertTrue(evaluate("true", "!=", "false"))
        assertTrue(evaluate("true", "==", "true"))
        assertTrue(evaluate("false", "==", "false"))
        assertTrue(evaluate("false", "!=", "true"))
        assertFalse(evaluate("true", "==", "false"))
        assertFalse(evaluate("true", "!=", "true"))
        assertFalse(evaluate("false", "!=", "false"))
        assertFalse(evaluate("false", "==", "true"))
    }

    @Test
    fun testInts() {
        assertTrue(evaluate("1", "==", "1"))
        assertTrue(evaluate("0", "!=", "1"))
        assertTrue(evaluate("1", "!=", "0"))
        assertTrue(evaluate("0", "==", "0"))
        assertTrue(evaluate("2", "> ", "1"))
        assertTrue(evaluate("1", "< ", "2"))
        assertTrue(evaluate("2", ">=", "1"))
        assertTrue(evaluate("1", "<=", "2"))
    }

    @Test
    fun testFloats() {
        assertTrue(evaluate("1.0", "==", "1.0"))
        assertTrue(evaluate("1.0000021", "!=", "1.222222"))
        assertTrue(evaluate("1.0", "!=", "0.0"))
        assertTrue(evaluate("0.0", "==", "0.0"))
        assertTrue(evaluate("2.0", "> ", "1.0"))
        assertTrue(evaluate("1.0", "< ", "2.0"))
        assertTrue(evaluate("2.0", ">=", "1.0"))
        assertTrue(evaluate("1.0", "<=", "2.0"))
    }

    @Test
    fun testCompareLists() {
        assertEquals(true, evaluate("@list(1,2,3)", "==", "@list(1,2,3)"))
        assertEquals(false, evaluate("@list(1,2,3)", "==", "@list(1,3,2)"))
        assertEquals(false, evaluate("@list(1,3,2)", "==", "@list(1,2,3)"))
        assertEquals(false, evaluate("@list(0,1,3,2)", "==", "@list(1,2,3)"))
        assertEquals(false, evaluate("@list(1,3,2)", "==", "@list(1,2,3,4)"))
    }

    @Test
    fun testConstantsRefs() {
        val context = TemplateContext(("\"ValueOne\" == \"SecondValue\""))
        val condition = context.stream.parseCondition()!! as LogicalCondition
        assertEquals("ValueOne", condition.propertyFirst.asPrimitive(context.stream.model).value)
        assertEquals("SecondValue", condition.propertySecond.asPrimitive(context.stream.model).value)
    }

    private fun testIfy(firstIf: Boolean, firstElseIf: Boolean, secondElseIf: Boolean): String {
        fun Boolean.s() = if (this) "true" else "false"
        return "@if(${firstIf.s()}) MyFirstValue @elseif(${firstElseIf.s()}) MySecondValue @elseif(${secondElseIf.s()}) MyThirdValue @else MyFourthValue @endif"
    }

    @Test
    fun testParseIf() {
        val iffy = testIfy(firstIf = true, firstElseIf = false, secondElseIf = false)
        val context = TemplateContext(iffy)
        val ifStatement = context.stream.parseIfStatement()
        assertEquals(12, ifStatement!!.evaluate(context)!!.blockValue.length)
        assertEquals("MyFirstValue", ifStatement.evaluate(context)!!.blockValue.getValueAsString())
        assertEquals("MyFirstValue", GenerateCode(iffy))
        assertEquals(iffy.length, context.stream.pointer)
    }

    @Test
    fun testMultiIf() {
        assertEquals("blockValue", GenerateCode("@if(true) @if(true) @if(true) blockValue @endif @endif @endif"))
    }

    @Test
    fun testParseIf2() {
        val iffy = testIfy(firstIf = false, firstElseIf = true, secondElseIf = false)
        val context = TemplateContext((iffy))
        val ifStatement = context.stream.parseIfStatement()
        assertEquals("MySecondValue", ifStatement!!.evaluate(context)!!.blockValue.getValueAsString())
        assertEquals("MySecondValue", GenerateCode(iffy))
        assertEquals(iffy.length, context.stream.pointer)
    }

    @Test
    fun testParseIf3() {
        val iffy = testIfy(firstIf = false, firstElseIf = false, secondElseIf = true)
        val context = TemplateContext((iffy))
        val ifStatement = context.stream.parseIfStatement()
        assertEquals("MyThirdValue", ifStatement!!.evaluate(context)!!.blockValue.getValueAsString())
        assertEquals("MyThirdValue", GenerateCode(iffy))
        assertEquals(iffy.length, context.stream.pointer)
    }

    @Test
    fun testParseIf4() {
        val iffy = testIfy(firstIf = false, firstElseIf = false, secondElseIf = false)
        val context = TemplateContext((iffy))
        val ifStatement = context.stream.parseIfStatement()
        assertNotEquals(null, ifStatement)
        assertEquals("MyFourthValue", GenerateCode(iffy))
        assertEquals("MyFourthValue", ifStatement!!.evaluate(context)!!.blockValue.getValueAsString())
        assertEquals(iffy.length, context.stream.pointer)
    }

    @Test
    fun parseMultilineIfBlock() {
        assertEquals(
            "Line Number 1\nLine Number 2", GenerateCode(
                """@if(true)
              |${'\t'}Line Number 1
              |${'\t'}Line Number 2
              |@endif""".trimMargin("|")
            )
        )
        assertEquals(
            "", GenerateCode(
                """@if(false)
              |${'\t'}Line Number 1
              |${'\t'}Line Number 2
              |@endif""".trimMargin("|")
            )
        )
        assertEquals(
            "\tLine Number 1\n\tLine Number 2", GenerateCode(
                """@if(true)
              |${'\t'}${'\t'}Line Number 1
              |${'\t'}${'\t'}Line Number 2
              |@endif""".trimMargin("|")
            )
        )
        assertEquals(
            "Line Number 1\nLine Number 2", GenerateCode(
                """@if(true)
              |${'\t'}@if(true)
              |${'\t'}${'\t'}Line Number 1
              |${'\t'}${'\t'}Line Number 2
              |${'\t'}@endif
              |@endif""".trimMargin("|")
            )
        )
    }

}