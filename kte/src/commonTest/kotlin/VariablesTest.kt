import com.wakaztahir.kte.GenerateCode
import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.model.StringValue
import com.wakaztahir.kte.model.asPrimitive
import com.wakaztahir.kte.parser.parseVariableDeclaration
import com.wakaztahir.kte.parser.parseVariableReference
import com.wakaztahir.kte.parser.parseExpression
import com.wakaztahir.kte.parser.parseStringValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class VariablesTest {


    @Test
    fun testParseVariableReference() {
        val context = TemplateContext(("@var(myVar)"))
        val ref = context.stream.parseVariableReference()
        assertNotEquals(null, ref)
        assertEquals(ref!!.propertyPath[0].name, "myVar")
        context.stream.model.putValue("myVar", StringValue("someValue"))
        assertEquals("someValue", ref.asPrimitive(context.stream.model).value)
    }

    @Test
    fun testParseVariableDeclaration() {
        val context = TemplateContext(("@var myVar = \"someValue\""))
        val ref = context.stream.parseVariableDeclaration()
        assertNotEquals(null, ref)
        assertEquals("myVar", ref!!.variableName)
        assertEquals("someValue", ref.variableValue.asPrimitive(context.stream.model).value)
    }

    @Test
    fun testParseVariableGeneration() {
        val text = "@var myVar = \"someValue\"@var(myVar)"
        val context = TemplateContext(text)
        assertEquals("someValue", context.getDestinationAsString())
        assertEquals(text.length, context.stream.pointer)
    }

    private fun evaluate(i: String, j: String, char: Char, expect: String) {
        assertEquals(expect, GenerateCode("@var i = $i@var j = @var(i) @$char $j@var(j)"))
        assertEquals(expect, GenerateCode("$i @$char $j"))
        assertEquals(expect, GenerateCode("@var j = $j $i @$char @var(j)"))
        assertEquals(expect, GenerateCode("@var i = $i @var(i) @$char $j"))
    }

    @Test
    fun testExpressions() {
        evaluate("0", "2", '+', "2")
        evaluate("2", "0", '+', "2")
        evaluate("10", "5", '-', "5")
        evaluate("5", "10", '-', "-5")
        evaluate("5", "2", '*', "10")
        evaluate("2", "5", '*', "10")
        evaluate("10", "2", '/', "5")
        evaluate("2", "2", '%', "0")
    }

    @Test
    fun testBodmasRule() {
        assertEquals("3", GenerateCode("1 @+ 2"))
        assertEquals("1 + 2", GenerateCode("1 + 2"))
        assertEquals("6", GenerateCode("1 @+ 2 @+ 3"))
        assertEquals("4", GenerateCode("2 @+ 4 @/ 2"))
        assertEquals("4", GenerateCode("4 @/ 2 @+ 2"))
        // Addition and Subtraction tests
        assertEquals("3", GenerateCode("1 @+ 2"))
        assertEquals("-1", GenerateCode("1 @- 2"))
        assertEquals("0", GenerateCode("1 @+ 2 @- 3"))
        assertEquals("5", GenerateCode("1 @- 2 @+ 6"))
        // Multiplication and Division tests
        assertEquals("4", GenerateCode("2 @* 2"))
        assertEquals("3", GenerateCode("6 @/ 2"))
        assertEquals("6", GenerateCode("2 @* 3 @/ 1"))
        assertEquals("8", GenerateCode("8 @/ 2 @* 2"))
        // BODMAS rule tests
        assertEquals("6", GenerateCode("2 @+ 2 @* 2"))
        assertEquals("8", GenerateCode("2 @* 2 @+ 4"))
        assertEquals("10", GenerateCode("2 @+ 2 @* 5 @- 2"))
        assertEquals("-10", GenerateCode("2 @- 2 @* 5 @- 2"))
        assertEquals("-5", GenerateCode("2 @- 2 @/ 2 @- 3 @* 2"))
    }

    @Test
    fun testReassignment() {
        val context = TemplateContext("@var i=0@var i=2@var(i)")
        assertEquals("2", context.getDestinationAsString())
        val context2 = TemplateContext("@var i=10@var i=@var(i) @+ 1@var(i)")
        assertEquals("11", context2.getDestinationAsString())
    }

    @Test
    fun testParseStringValue() {
        val context = TemplateContext(("\"someValue\""))
        val value = context.stream.parseStringValue()!!.value
        assertEquals("someValue", value)
    }

    @Test
    fun testParseDynamicProperty() {
        val context = TemplateContext(("@var(myVar)"))
        context.stream.model.putValue("myVar", StringValue("someValue"))
        val property = context.stream.parseExpression()
        assertEquals(property!!.asPrimitive(context.stream.model).value, "someValue")
    }

    @Test
    fun testDeclarationAndReference() {
        val decContext = TemplateContext(("@var myVar = \"someValue\""))
        val dec = decContext.stream.parseVariableDeclaration()
        assertEquals("someValue", dec!!.variableValue.asPrimitive(decContext.stream.model).value)
        decContext.updateStream("@var myVar2 = @var(myVar)")
        dec.storeValue(decContext.stream.model)
        val refDec = decContext.stream.parseVariableDeclaration()
        assertEquals(
            dec.variableValue.asPrimitive(decContext.stream.model).value,
            refDec!!.variableValue.asPrimitive(decContext.stream.model).value
        )
    }

}