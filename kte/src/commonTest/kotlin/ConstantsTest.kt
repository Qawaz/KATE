import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.model.StringValue
import com.wakaztahir.kte.parser.parseConstantDeclaration
import com.wakaztahir.kte.parser.parseConstantReference
import com.wakaztahir.kte.parser.parseExpression
import com.wakaztahir.kte.parser.parseStringValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ConstantsTest {


    @Test
    fun testParseConstantReference() {
        val context = TemplateContext(("@const(myVar)"))
        val ref = context.stream.parseConstantReference()
        assertNotEquals(null, ref)
        assertEquals(ref!!.propertyPath[0].name, "myVar")
        context.stream.model.putValue("myVar", StringValue("someValue"))
        assertEquals("someValue", ref.getValue(context.stream.model).value)
    }

    @Test
    fun testParseConstantDeclaration() {
        val context = TemplateContext(("@const myVar = \"someValue\""))
        val ref = context.stream.parseConstantDeclaration()
        assertNotEquals(null, ref)
        assertEquals("myVar", ref!!.variableName)
        assertEquals("someValue", ref.variableValue.getValue(context.stream.model).value)
    }

    @Test
    fun testParseConstantGeneration() {
        val text = "@const myVar = \"someValue\"@const(myVar)"
        val context = TemplateContext(text)
        assertEquals("someValue", context.getDestinationAsString())
        assertEquals(text.length, context.stream.pointer)
    }

    private fun evaluate(i : String,j : String,char : Char) : String {
        val context = TemplateContext("@const i = $i@const j = @const(i) @$char $j@const(j)")
        return context.getDestinationAsString()
    }

    @Test
    fun testExpressions(){
        assertEquals("2",evaluate("0","2",'+'))
        assertEquals("2",evaluate("2","0",'+'))
        assertEquals("5",evaluate("10","5",'-'))
        assertEquals("-5",evaluate("5","10",'-'))
        assertEquals("10",evaluate("5","2",'*'))
        assertEquals("10",evaluate("2","5",'*'))
        assertEquals("5",evaluate("10","2",'/'))
        assertEquals("0",evaluate("2","2",'%'))
    }

    @Test
    fun testReassignment(){
        val context = TemplateContext("@const i=0@const i=2@const(i)")
        assertEquals("2",context.getDestinationAsString())
        val context2 = TemplateContext("@const i=10@const i=@const(i) @+ 1@const(i)")
        assertEquals("11",context2.getDestinationAsString())
    }

    @Test
    fun testParseStringValue() {
        val context = TemplateContext(("\"someValue\""))
        val value = context.stream.parseStringValue()!!.value
        assertEquals("someValue", value)
    }

    @Test
    fun testParseDynamicProperty() {
        val context = TemplateContext(("@const(myVar)"))
        context.stream.model.putValue("myVar", StringValue("someValue"))
        val property = context.stream.parseExpression()
        assertEquals(property!!.getValue(context.stream.model).value, "someValue")
    }

    @Test
    fun testDeclarationAndReference() {
        val decContext = TemplateContext(("@const myVar = \"someValue\""))
        val dec = decContext.stream.parseConstantDeclaration()
        assertEquals("someValue", dec!!.variableValue.getValue(decContext.stream.model).value)
        decContext.updateStream("@const myVar2 = @const(myVar)")
        dec!!.storeValue(decContext.stream.model)
        val refDec = decContext.stream.parseConstantDeclaration()
        assertEquals(
            dec.variableValue.getValue(decContext.stream.model).value,
            refDec!!.variableValue.getValue(decContext.stream.model).value
        )
    }

}