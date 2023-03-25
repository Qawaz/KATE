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

        assertEquals("Name", definition.placeholder.placeholderName)
        assertEquals("Name", definition.placeholder.definitionName)
        assertEquals("ElonMusk", definition.placeholder.getValueAsString(context.stream))
        assertEquals("Name", invocation.placeholderName)

        val destination = KotlinLanguageDestination(TextDestinationStream())
        definition.generateTo(context.stream, context.stream, destination)
        assertEquals(definition.placeholder, context.stream.placeholders[0])
        invocation.generateTo(context.stream, context.stream, destination)
        assertEquals("ElonMusk", (destination.stream as TextDestinationStream).getValue())

        assertEquals(expected = "ElonMusk", actual = GenerateCode(code = code))
    }

}