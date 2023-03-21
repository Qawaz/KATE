import com.wakaztahir.kte.KTEDelicateFunction
import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.model.LogicalCondition
import com.wakaztahir.kte.parser.*
import com.wakaztahir.kte.parser.parseCondition
import com.wakaztahir.kte.parser.parseConstantDeclaration
import com.wakaztahir.kte.parser.parseIfStatement
import kotlin.test.*

class IfStatementTest {

    private fun TemplateContext.evaluateConstants(var1: String, condition: String, var2: String): Boolean {
        updateStream("@const var1 = $var1")
        stream.parseConstantDeclaration()!!.storeValue(stream.model)
        updateStream("@const var2 = $var2")
        stream.parseConstantDeclaration()!!.storeValue(stream.model)
        updateStream("@const(var1) $condition @const(var2)")
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
        val context = TemplateContext("@if(@const(var1)) blockValue @endif")
        context.stream.model.putValue("var1", true)
        assertEquals("blockValue", context.getDestinationAsStringWithReset())
    }

    @Test
    fun testConstRefParsing() {
        val text = "@if(@const(var1)) blockValue @endif"
        val context = TemplateContext(text)
        context.stream.model.putValue("var1", true)
        val statement = context.stream.parseIfStatement(context.stream)!!
        assertEquals("blockValue", statement.singleIfs[0].blockValue.getValueAsString(context.stream))
        assertNotEquals(null, statement.evaluate(context))
        assertEquals(text.length, context.stream.pointer)
    }

    @Test
    fun testUnequalCondition() {
        val context = TemplateContext(("\"ValueOne\" == \"SecondValue\""))
        val condition = context.stream.parseCondition()!! as LogicalCondition
        assertEquals("ValueOne", condition.propertyFirst.getValue(context.stream.model).value)
        assertEquals("SecondValue", condition.propertySecond.getValue(context.stream.model).value)
    }

    @Test
    fun testNestedIfReference(){
        val context = TemplateContext("@const i = 4@if(true) @if(true) @const(i) @endif @endif")
        assertEquals("4",context.getDestinationAsString())
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
        assertTrue(evaluate("1.0f", "==", " 1.0f"))
        assertTrue(evaluate("1.0000021f", "!=", " 1.222222f"))
        assertTrue(evaluate("1.0f", "!=", " 0.0f"))
        assertTrue(evaluate("0.0f", "==", " 0.0f"))
        assertTrue(evaluate("2.0f", "> ", "1.0f"))
        assertTrue(evaluate("1.0f", "< ", "2.0f"))
        assertTrue(evaluate("2.0f", ">=", " 1.0f"))
        assertTrue(evaluate("1.0f", "<=", " 2.0f"))
    }

    @Test
    fun testConstantsRefs() {
        val context = TemplateContext(("\"ValueOne\" == \"SecondValue\""))
        val condition = context.stream.parseCondition()!! as LogicalCondition
        assertEquals("ValueOne", condition.propertyFirst.getValue(context.stream.model).value)
        assertEquals("SecondValue", condition.propertySecond.getValue(context.stream.model).value)
    }

    private fun testIfy(firstIf: Boolean, firstElseIf: Boolean, secondElseIf: Boolean): String {
        fun Boolean.s() = if (this) "true" else "false"
        return "@if(${firstIf.s()}) MyFirstValue @elseif(${firstElseIf.s()}) MySecondValue @elseif(${secondElseIf.s()}) MyThirdValue @else MyFourthValue @endif"
    }

    private fun codeGenerated(statement: String): String {
        val context = TemplateContext(statement)
        return context.getDestinationAsString()
    }

    @Test
    fun testParseIf() {
        val iffy = testIfy(firstIf = true, firstElseIf = false, secondElseIf = false)
        val context = TemplateContext(iffy)
        val ifStatement = context.stream.parseIfStatement(context.stream)
        assertEquals(12, ifStatement!!.evaluate(context)!!.blockValue.length)
        assertEquals("MyFirstValue", ifStatement.evaluate(context)!!.blockValue.getValueAsString(context.stream))
        assertEquals("MyFirstValue", codeGenerated(iffy))
        assertEquals(iffy.length, context.stream.pointer)
    }

    @Test
    fun testMultiIf() {
        assertEquals("blockValue", codeGenerated("@if(true) @if(true) @if(true) blockValue @endif @endif @endif"))
    }

    @Test
    fun testParseIf2() {
        val iffy = testIfy(firstIf = false, firstElseIf = true, secondElseIf = false)
        val context = TemplateContext((iffy))
        val ifStatement = context.stream.parseIfStatement(context.stream)
        assertEquals("MySecondValue", ifStatement!!.evaluate(context)!!.blockValue.getValueAsString(context.stream))
        assertEquals("MySecondValue", codeGenerated(iffy))
        assertEquals(iffy.length, context.stream.pointer)
    }

    @Test
    fun testParseIf3() {
        val iffy = testIfy(firstIf = false, firstElseIf = false, secondElseIf = true)
        val context = TemplateContext((iffy))
        val ifStatement = context.stream.parseIfStatement(context.stream)
        assertEquals("MyThirdValue", ifStatement!!.evaluate(context)!!.blockValue.getValueAsString(context.stream))
        assertEquals("MyThirdValue", codeGenerated(iffy))
        assertEquals(iffy.length, context.stream.pointer)
    }

    @Test
    fun testParseIf4() {
        val iffy = testIfy(firstIf = false, firstElseIf = false, secondElseIf = false)
        val context = TemplateContext((iffy))
        val ifStatement = context.stream.parseIfStatement(context.stream)
        assertNotEquals(null, ifStatement)
        assertEquals("MyFourthValue", codeGenerated(iffy))
        assertEquals("MyFourthValue", ifStatement!!.evaluate(context)!!.blockValue.getValueAsString(context.stream))
        assertEquals(iffy.length, context.stream.pointer)
    }

    @OptIn(KTEDelicateFunction::class)
    @Test
    fun parseMultilineIfBlock() {
        val context = TemplateContext(
            """@if(true)
              | Line Number 1
              | Line Number 2
              |@endif""".trimMargin("|")
        )
        assertEquals("Line Number 1\nLine Number 2", context.getDestinationAsStringWithReset())
        val context2 = TemplateContext(
            """@if(false)
              | Line Number 1
              | Line Number 2
              |@endif""".trimMargin("|")
        )
        assertEquals("", context2.getDestinationAsStringWithReset())
    }

}