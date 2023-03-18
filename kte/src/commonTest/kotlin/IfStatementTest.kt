import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.model.FloatValue
import com.wakaztahir.kte.parser.parseCondition
import com.wakaztahir.kte.parser.parseConstantDeclaration
import com.wakaztahir.kte.parser.stream.TextStream
import com.wakaztahir.kte.parser.stream.printLeft
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IfStatementTest {

    private fun TemplateContext.evaluateConstants(var1: String, condition: String, var2: String): Boolean {
        updateStream(TextStream("@const var1 = $var1"))
        parseConstantDeclaration()!!.storeValue(this)
        updateStream(TextStream("@const var2 = $var2"))
        parseConstantDeclaration()!!.storeValue(this)
        updateStream(TextStream("@const(var1) $condition @const(var2)"))
        return parseCondition()!!.evaluate(this)
    }

    private fun evaluate(var1: String, condition: String, var2: String, constants: Boolean = true): Boolean {
        val context = TemplateContext(TextStream("$var1 $condition $var2"))
        val result = context.parseCondition()!!.evaluate(context)
        val result2 = if (constants) context.evaluateConstants(var1, condition, var2) else true
        return result && result2
    }

    @Test
    fun testUnequalCondition() {
        val context = TemplateContext(TextStream("\"ValueOne\" == \"SecondValue\""))
        val condition = context.parseCondition()!!
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
        val condition = context.parseCondition()!!
        assertEquals("ValueOne", condition.propertyFirst.getValue(context)!!.value)
        assertEquals("SecondValue", condition.propertySecond.getValue(context)!!.value)
    }

}