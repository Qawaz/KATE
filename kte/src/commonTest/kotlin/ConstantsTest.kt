import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.parser.parseConstantDeclaration
import com.wakaztahir.kte.parser.parseConstantReference
import com.wakaztahir.kte.parser.parseDynamicProperty
import com.wakaztahir.kte.parser.parseStringValue
import com.wakaztahir.kte.parser.stream.TextStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ConstantsTest {
    @Test
    fun testParseConstantReference() {
        val context = TemplateContext(TextStream("@const(myVar)"))
        val ref = context.stream.parseConstantReference()
        assertNotEquals(null, ref)
        assertEquals(ref!!.variableName, "myVar")
    }

    @Test
    fun testParseConstantDeclaration() {
        val context = TemplateContext(TextStream("@const myVar = \"someValue\""))
        val ref = context.parseConstantDeclaration()
        assertNotEquals(null, ref)
        assertEquals("myVar", ref!!.variableName)
        assertEquals("someValue", ref.variableValue.getValue(context))
    }

    @Test
    fun testParseStringValue() {
        val context = TemplateContext(TextStream("\"someValue\""))
        val value = context.stream.parseStringValue()!!.getValue(context)
        assertEquals("someValue", value)
    }

    @Test
    fun testParseDynamicProperty() {
        val context = TemplateContext(TextStream("@const(myVar)"))
        context.storeValue("myVar", "someValue")
        val property = context.stream.parseDynamicProperty()
        assertEquals(property!!.getValue(context), "someValue")
    }

    @Test
    fun testDeclarationAndReference() {
        val decContext = TemplateContext(TextStream("@const myVar = \"someValue\""))
        val dec = decContext.parseConstantDeclaration()
        decContext.updateStream(TextStream("@const myVar2 = @const(myVar)"))
        dec!!.storeValue(decContext)
        val refDec = decContext.parseConstantDeclaration()
        assertEquals(dec.variableValue.getValue(decContext), refDec!!.variableValue.getValue(decContext))
    }

}