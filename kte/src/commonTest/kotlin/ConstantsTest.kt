import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.parser.parseConstantDeclaration
import com.wakaztahir.kte.parser.parseConstantReference
import com.wakaztahir.kte.parser.stream.TextStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ConstantsTest {
    @Test
    fun testParseConstantReference() {
        val context = TemplateContext(TextStream("@const(myVar)"))
        val ref = context.parseConstantReference()
        assertNotEquals(null, ref)
        assertEquals(ref!!.variableName, "myVar")
    }

    @Test
    fun testParseConstantDeclaration() {
        val context = TemplateContext(TextStream("@const myVar = 'someValue'"))
        val ref = context.parseConstantDeclaration()
        assertNotEquals(null, ref)
        assertEquals("myVar", ref!!.variableName)
        assertEquals("someValue", ref.variableValue.getValue(context))
    }

    @Test
    fun testDeclarationAndReference() {
        val decContext = TemplateContext(TextStream("@const myVar = 'someValue'"))
        val dec = decContext.parseConstantDeclaration()
        decContext.updateStream(TextStream("@const myVar2 = @const(myVar)"))
        dec!!.storeValue(decContext)
        val refDec = decContext.parseConstantDeclaration()
        assertEquals(dec.variableValue.getValue(decContext), refDec!!.variableValue.getValue(decContext))
    }

}