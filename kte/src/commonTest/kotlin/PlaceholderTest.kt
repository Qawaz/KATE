import com.wakaztahir.kte.GenerateCode
import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.parser.parsePlaceholderDefinition
import com.wakaztahir.kte.parser.parsePlaceholderInvocation
import com.wakaztahir.kte.parser.stream.TextDestinationStream
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

        val destination = TextDestinationStream()
        definition.generateTo(context.stream, destination)
        assertEquals(definition.blockValue, context.stream.placeholderManager.getPlaceholder("Name"))
        invocation.generateTo(context.stream, destination)
        assertEquals("ElonMusk", (destination.stream as TextDestinationStream).getValue())

        assertEquals(expected = "ElonMusk", actual = GenerateCode(code = code))
    }

    @Test
    fun testPlaceholderScopeInheritance() {
        assertEquals(
            expected = "5",
            actual = GenerateCode("@define_placeholder(Name) @var i = 5 @end_define_placeholder @placeholder(Name)@var(i)")
        )
        assertEquals(
            expected = "01234",
            actual = GenerateCode("@define_placeholder(Name) @var(i) @end_define_placeholder @for(@var i=0;i<5;i++) @placeholder(Name) @endfor")
        )
        val objectDefinition = "@define_object(MyObject) @var i = 5 @end_define_object"
        val placeholderDefinition = "@define_placeholder(MyPH) @var(i) @end_define_placeholder"
        val invocation = "@placeholder(MyPH,@var(MyObject))"
        assertEquals("5", GenerateCode("$placeholderDefinition $objectDefinition $invocation"))
        assertEquals("5", GenerateCode("$objectDefinition $placeholderDefinition $invocation"))
    }

    @Test
    fun testPlaceholderLanguage() {
        assertEquals("5123", GenerateCode("@var i = 5123 @var(i)"))
        assertEquals("12.34", GenerateCode("@var i = 12.34 @var(i)"))
        assertEquals("hello", GenerateCode("@var i = \"hello\" @var(i)"))
        assertEquals("f", GenerateCode("@var i = 'f' @var(i)"))
        assertEquals("true", GenerateCode("@var i = true @var(i)"))
        assertEquals("false", GenerateCode("@var i = false @var(i)"))
        assertEquals("1,2,3", GenerateCode("@var i = @list(1,2,3) @var(i.joinToString())"))
        assertEquals("1,2,3", GenerateCode("@var i = @list(1,2,3) @var(i)"))
        assertEquals("1.2.3",
            GenerateCode("@var i = @mutable_list(1,2,3) @var(i.joinToString(\".\"))")
        )
        assertEquals("1,2,3", GenerateCode("@var i = @mutable_list(1,2,3) @var(i)"))
        assertEquals(
            expected = "{\n\ti : 5\n\tl : 1,2,3\n}",
            GenerateCode("@define_object(MyObj) @var i = 5 @var l = @list(1,2,3) @end_define_object @var(MyObj)")
        )
    }

}