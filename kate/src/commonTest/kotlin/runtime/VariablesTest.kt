package runtime

import GenerateCode
import GeneratePartialRaw
import com.wakaztahir.kate.TemplateContext
import com.wakaztahir.kate.model.asPrimitive
import com.wakaztahir.kate.parser.variable.parseVariableDeclaration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class VariablesTest {

    @Test
    fun testParseVariableDeclaration() {
        val context = TemplateContext(("@var myVar = \"someValue\""))
        val ref = context.stream.parseVariableDeclaration()
        assertNotEquals(null, ref)
        assertEquals("myVar", ref!!.variableName)
        assertEquals("someValue", ref.variableValue.asPrimitive(context.stream.model).value)
    }

    @Test
    fun testVariableAssignment() {
        assertEquals("1", GenerateCode("@var i = 0 @set_var i = 1 @var(i)"))
        assertEquals("15", GenerateCode("@var i = 5 @set_var i *= 3 @var(i)"))
        assertEquals("8", GenerateCode("@var i = 5 @set_var i += 3 @var(i)"))
        assertEquals("2", GenerateCode("@var i = 5 @set_var i -= 3 @var(i)"))
        assertEquals("5", GenerateCode("@var i = 15 @set_var i /= 3 @var(i)"))
        assertEquals("1", GenerateCode("@var i = 16 @set_var i %= 3 @var(i)"))
    }

    @Test
    fun testDirectVariableAssignment(){
        assertEquals("1", GeneratePartialRaw("@var i = 0 @set_var i = 1 @default_no_raw @var(i) @end_default_no_raw"))
        assertEquals("1", GeneratePartialRaw("@var i = 0 i = 1 @default_no_raw @var(i) @end_default_no_raw"))
        assertEquals("15", GeneratePartialRaw("@var i = 5 i *= 3 @default_no_raw @var(i) @end_default_no_raw"))
        assertEquals("8", GeneratePartialRaw("@var i = 5 i += 3 @default_no_raw @var(i) @end_default_no_raw"))
        assertEquals("2", GeneratePartialRaw("@var i = 5 i -= 3 @default_no_raw @var(i) @end_default_no_raw"))
        assertEquals("5", GeneratePartialRaw("@var i = 15 i /= 3 @default_no_raw @var(i) @end_default_no_raw"))
        assertEquals("1", GeneratePartialRaw("@var i = 16 i %= 3 @default_no_raw @var(i) @end_default_no_raw"))
    }
}