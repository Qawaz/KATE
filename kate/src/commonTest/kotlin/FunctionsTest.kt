import com.wakaztahir.kate.GenerateCode
import com.wakaztahir.kate.GeneratePartialRaw
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class FunctionsTest {

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
        assertEquals(
            expected = "10",
            actual = GeneratePartialRaw(
                """
                |@function MyFunc(param1)
                |@if(@var(param1) < 5) @return @var(MyFunc(@var(param1) @+ 1)) @else @return @var(param1) @endif
                |@end_function
                |@default_no_raw @var(MyFunc(0)) @end_default_no_raw
                """.trimMargin()
            )
        )
    }

}