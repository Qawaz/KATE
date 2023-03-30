import com.wakaztahir.kate.GenerateCode
import kotlin.test.Test
import kotlin.test.assertEquals

class RuntimeTest {

    @Test
    fun testRuntimePrint() {
        assertEquals("5", GenerateCode("@var x = '5' @runtime.print_char(@var(x))"))
        assertEquals("hello", GenerateCode("@var x = \"hello\" @runtime.print_string(@var(x))"))
        assertEquals("1223", GenerateCode("@var x = 1223 @runtime.print_string(@var(x.toString()))"))
        assertEquals("1223.0", GenerateCode("@var x = 1223.0 @runtime.print_string(@var(x.toString()))"))
    }

}