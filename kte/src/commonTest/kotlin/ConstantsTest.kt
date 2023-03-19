import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.model.StringValue
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
        assertEquals(ref!!.name, "myVar")
        context.storeValue("myVar", StringValue("someValue"))
        assertEquals("someValue", ref.getValue(context)!!.value)
    }

    @Test
    fun testParseConstantDeclaration() {
        val context = TemplateContext(TextStream("@const myVar = \"someValue\""))
        val ref = context.stream.parseConstantDeclaration()
        assertNotEquals(null, ref)
        assertEquals("myVar", ref!!.variableName)
        assertEquals("someValue", ref.variableValue.getValue(context)!!.value)
    }

    @Test
    fun testParseStringValue() {
        val context = TemplateContext(TextStream("\"someValue\""))
        val value = context.stream.parseStringValue()!!.value
        assertEquals("someValue", value)
    }

    @Test
    fun testParseDynamicProperty() {
        val context = TemplateContext(TextStream("@const(myVar)"))
        context.storeValue("myVar", StringValue("someValue"))
        val property = context.stream.parseDynamicProperty()
        assertEquals(property!!.getValue(context)!!.value, "someValue")
    }

    @Test
    fun testDeclarationAndReference() {
        val decContext = TemplateContext(TextStream("@const myVar = \"someValue\""))
        val dec = decContext.stream.parseConstantDeclaration()
        decContext.updateStream(TextStream("@const myVar2 = @const(myVar)"))
        dec!!.storeValue(decContext)
        val refDec = decContext.stream.parseConstantDeclaration()
        assertEquals(
            dec.variableValue.getValue(decContext)!!.value,
            refDec!!.variableValue.getValue(decContext)!!.value
        )
    }

}