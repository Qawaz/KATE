import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.model.FloatValue
import com.wakaztahir.kte.model.LogicalCondition
import com.wakaztahir.kte.parser.parseCondition
import com.wakaztahir.kte.parser.parseConstantDeclaration
import com.wakaztahir.kte.parser.parseIfStatement
import com.wakaztahir.kte.parser.stream.TextStream
import com.wakaztahir.kte.parser.stream.printLeft
import kotlin.test.*

class IfStatementTest {

    private fun TemplateContext.evaluateConstants(var1: String, condition: String, var2: String): Boolean {
        updateStream(TextStream("@const var1 = $var1"))
        parseConstantDeclaration()!!.storeValue(this)
        updateStream(TextStream("@const var2 = $var2"))
        parseConstantDeclaration()!!.storeValue(this)
        updateStream(TextStream("@const(var1) $condition @const(var2)"))
        return stream.parseCondition()!!.evaluate(this)
    }

    private fun evaluate(var1: String, condition: String, var2: String, constants: Boolean = true): Boolean {
        val context = TemplateContext(TextStream("$var1 $condition $var2"))
        val result = context.stream.parseCondition()!!.evaluate(context)
        val result2 = if (constants) context.evaluateConstants(var1, condition, var2) else true
        return result && result2
    }

    @Test
    fun testUnequalCondition() {
        val context = TemplateContext(TextStream("\"ValueOne\" == \"SecondValue\""))
        val condition = context.stream.parseCondition()!! as LogicalCondition
        assertEquals("ValueOne", condition.propertyFirst.getValue(context)!!.value)
        assertEquals("SecondValue", condition.propertySecond.getValue(context)!!.value)
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
        val context = TemplateContext(TextStream("\"ValueOne\" == \"SecondValue\""))
        val condition = context.stream.parseCondition()!! as LogicalCondition
        assertEquals("ValueOne", condition.propertyFirst.getValue(context)!!.value)
        assertEquals("SecondValue", condition.propertySecond.getValue(context)!!.value)
    }

    private fun testIfy(firstIf: Boolean, firstElseIf: Boolean, secondElseIf: Boolean): String {
        fun Boolean.s() = if (this) "true == true" else "true != true"
        return "@if(${firstIf.s()})MyFirstValue@elseif(${firstElseIf.s()})MySecondValue@elseif(${secondElseIf.s()})MyThirdValue@elseMyFourthValue@endif"
    }

    @Test
    fun testParseIf() {
        val iffy = testIfy(firstIf = true, firstElseIf = false, secondElseIf = false)
        val context = TemplateContext(TextStream(iffy))
        val ifStatement = context.stream.parseIfStatement()
        assertEquals("MyFirstValue", ifStatement!!.evaluate(context)!!.blockValue)
        assertEquals(iffy.length,context.stream.pointer)
    }

    @Test
    fun testParseIf2() {
        val iffy = testIfy(firstIf = false, firstElseIf = true, secondElseIf = false)
        val context = TemplateContext(TextStream(iffy))
        val ifStatement = context.stream.parseIfStatement()
        assertEquals("MySecondValue", ifStatement!!.evaluate(context)!!.blockValue)
        assertEquals(iffy.length,context.stream.pointer)
    }

    @Test
    fun testParseIf3() {
        val iffy = testIfy(firstIf = false, firstElseIf = false, secondElseIf = true)
        val context = TemplateContext(TextStream(iffy))
        val ifStatement = context.stream.parseIfStatement()
        assertEquals("MyThirdValue", ifStatement!!.evaluate(context)!!.blockValue)
        assertEquals(iffy.length,context.stream.pointer)
    }

    @Test
    fun testParseIf4() {
        val iffy = testIfy(firstIf = false, firstElseIf = false, secondElseIf = false)
        val context = TemplateContext(TextStream(iffy))
        val ifStatement = context.stream.parseIfStatement()
        assertNotEquals(null,ifStatement)
        assertEquals("MyFourthValue", ifStatement!!.evaluate(context)!!.blockValue)
        assertEquals(iffy.length,context.stream.pointer)
    }

}