package runtime

import GenerateCode
import GeneratePartialRaw
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RuntimeTest {

    @Test
    fun testRuntimePrint() {
        assertEquals("5", GenerateCode("@var x = '5' @write(@var(x))"))
        assertEquals("hello", GenerateCode("@var x = \"hello\" @write(@var(x))"))
        assertEquals("1223", GenerateCode("@var x = 1223 @write(@var(x.toString()))"))
        assertEquals("1223.0", GenerateCode("@var x = 1223.0 @write(@var(x.toString()))"))
        assertEquals("1223.0", GenerateCode("@var x = 1223.0 @write(x.toString())"))
    }

    @Test
    fun testThrow() {
        assertFailsWith(RuntimeException::class) {
            GeneratePartialRaw("throw(\"this code fails\")")
        }
        val message = "This code will fail"
        try {
            GeneratePartialRaw("throw(\"${message}\")")
        }catch (ex : RuntimeException){
            assertEquals(ex.message,message)
        }
    }

    @Test
    fun testLog(){
        GeneratePartialRaw("console.log(\"This will print to console\")")
    }

}