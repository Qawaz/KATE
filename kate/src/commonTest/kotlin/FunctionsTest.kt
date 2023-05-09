import com.wakaztahir.kate.TemplateContext
import com.wakaztahir.kate.parser.stream.TextDestinationStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotNull

class FunctionsTest {

    @Test
    fun testFunctionExistence() {
        val context = TemplateContext("@function myFunc() @return 6 @end_function")
        context.generateTo(TextDestinationStream())
        val value = context.stream.model.get("myFunc")
        if (value == null) {
            println("FUNCTION NOT PRESENT IN ${context.stream.model}")
        }
        assertNotNull(value)

    }

    @Test
    fun testCallFunctionByVar() {
        assertEquals(
            expected = "hello",
            actual = GeneratePartialRaw(
                """@function myFunc()
                |@runtime.print_string("hello")
                |@end_function
                |@var another = myFunc
                |another()""".trimMargin()
            )
        )
        assertEquals(
            expected = "hello",
            actual = GeneratePartialRaw(
                """@function myFunc(otherFunc)
                |otherFunc()
                |@end_function
                |@function call()
                |@runtime.print_string("hello")
                |@end_function
                |myFunc(call)""".trimMargin()
            )
        )
    }

    @Test
    fun testFunctionDefinition() {
        assertEquals(
            expected = "hello1",
            actual = GenerateCode(
                """@function myFunc()
                |@runtime.print_string("hello1")
                |@end_function
                |@var(myFunc())""".trimMargin()
            )
        )
        assertEquals(
            expected = "hello2",
            actual = GeneratePartialRaw(
                """
                |@function First()
                |@runtime.print_string("hello2")
                |@end_function
                |@var(First())
                """.trimMargin()
            )
        )
        assertFails {
            GeneratePartialRaw(
                """
                |@function Second(param1)
                |@runtime.print_string(@var(param1))
                |@end_function
                |@var(Second())
                """.trimMargin()
            )
        }
        assertEquals(
            expected = "world",
            actual = GeneratePartialRaw(
                """
                |@function MyFunc(param1)
                |@runtime.print_string(param1)
                |@end_function
                |MyFunc("world")
                """.trimMargin()
            )
        )
        assertEquals(
            expected = "\t\t\t",
            actual = GeneratePartialRaw(
                """
                |@function indent(indentation)
                |   @for(@var i = 0;i<indentation;i++)
                |      @runtime.print_char('\t')
                |   @endfor
                |@end_function
                |indent(3)
                """.trimMargin()
            )
        )
        assertEquals(
            expected = "world",
            actual = GeneratePartialRaw(
                """
                |@function MyFunc(param1)
                |@runtime.print_string(@var(param1))
                |@end_function
                |@var(MyFunc("world"))
                """.trimMargin()
            )
        )
        assertEquals(
            expected = "world",
            actual = GeneratePartialRaw(
                """
                |@function MyFunc(param1)
                |@return @var(param1)
                |@end_function
                |@default_no_raw @var(MyFunc("world")) @end_default_no_raw
                """.trimMargin()
            )
        )
    }

    @Test
    fun testDirectRefsInFunctions() {
        assertEquals(
            expected = "world",
            actual = GeneratePartialRaw(
                """
                |@function MyFunc(param1)
                |@return param1
                |@end_function
                |@var world = "world"
                |@default_no_raw @var(MyFunc(world)) @end_default_no_raw
                """.trimMargin()
            )
        )
        assertEquals(
            expected = "world",
            actual = GeneratePartialRaw(
                """
                |@function MyFunc(param1)
                |@default_no_raw @var(param1) @end_default_no_raw
                |@end_function
                |@var world = "world"
                |MyFunc(world)
                """.trimMargin()
            )
        )
    }

    @Test
    fun testFunctionReturnWorks(){
        assertEquals(
            expected = "9",
            actual = GeneratePartialRaw(
                """
                |@function MyFunc(param1)
                |@if(param1 > 5)
                |   @return param1
                |@endif
                |@return 343434
                |@end_function
                |@default_no_raw @var(MyFunc(9)) @end_default_no_raw
                """.trimMargin()
            )
        )
    }

    @Test
    fun testFunctionRecursion() {
        assertEquals(
            expected = "5",
            actual = GeneratePartialRaw(
                """
                |@function MyFunc(param1)
                |@if(@var(param1) > 5)
                |   @return @var(MyFunc(@var(param1) - 1)) 
                |@else
                |   @return @var(param1) @endif
                |@end_function
                |@default_no_raw @var(MyFunc(9)) @end_default_no_raw
                """.trimMargin()
            )
        )
        assertEquals(
            expected = "5040",
            actual = GenerateExpression("7 * 6 * 5 * 4 * 3 * 2 * 1")
        )
        assertEquals(
            expected = "120245040362880",
            actual = GeneratePartialRaw(
                """
                |@function fact(n)
                |@if(@var(n) <= 1)
                |   @return 1
                |@else
                |   @return n * @var(fact(n - 1)) @endif
                |@end_function
                |@default_no_raw @var(fact(5)) @end_default_no_raw
                |@default_no_raw @var(fact(4)) @end_default_no_raw
                |@default_no_raw @var(fact(7)) @end_default_no_raw
                |@default_no_raw @var(fact(9)) @end_default_no_raw
                """.trimMargin()
            )
        )
    }

}