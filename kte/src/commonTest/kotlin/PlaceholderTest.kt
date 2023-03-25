import com.wakaztahir.kte.GenerateCode
import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.parser.parsePlaceholderDefinition
import com.wakaztahir.kte.parser.parsePlaceholderInvocation
import com.wakaztahir.kte.parser.stream.TextDestinationStream
import com.wakaztahir.kte.parser.stream.languages.KotlinLanguageDestination
import kotlin.test.Test
import kotlin.test.assertEquals

class PlaceholderTest {

    @Test
    fun testPlaceholder() {
        val definitionText = "@define_placeholder(Name) ElonMusk @end_define_placeholder"
        val code = "$definitionText@placeholder(Name)"
        val context = TemplateContext(code)
        val definition = context.stream.parsePlaceholderDefinition()!!

        assertEquals(definitionText.length, context.stream.pointer)

        val invocation = context.stream.parsePlaceholderInvocation()!!

        assertEquals(code.length, context.stream.pointer)

        assertEquals("Name", definition.blockValue.placeholderName)
        assertEquals("Name", definition.blockValue.definitionName)
        assertEquals("ElonMusk", definition.blockValue.getValueAsString())
        assertEquals("Name", invocation.placeholderName)

        val destination = KotlinLanguageDestination(TextDestinationStream())
        definition.generateTo(context.stream, destination)
        assertEquals(definition.blockValue, context.stream.placeholders[0])
        invocation.generateTo(context.stream, destination)
        assertEquals("ElonMusk", (destination.stream as TextDestinationStream).getValue())

        assertEquals(expected = "ElonMusk", actual = GenerateCode(code = code))
    }

}