import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.model.StringValue
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

    private fun evaluate(i : String,j : String,char : Char) : String {
        val context = TemplateContext("@var i = $i@var j = @var(i) @$char $j@var(j)")
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
        val context = TemplateContext("@var i=0@var i=2@var(i)")
        assertEquals("2",context.getDestinationAsString())
        val context2 = TemplateContext("@var i=10@var i=@var(i) @+ 1@var(i)")
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