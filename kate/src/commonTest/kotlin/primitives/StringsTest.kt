package primitives

import com.wakaztahir.kate.GenerateCode
import kotlin.test.Test
import kotlin.test.assertEquals

class StringsTest {

    @Test
    fun testStringConcatenation() {
        assertEquals(
            expected = "hello",
            actual = GenerateCode("@runtime.print_string(\"hel\" @+ \"lo\")")
        )
        assertEquals(
            expected = "hello",
            actual = GenerateCode("@var hel = \"hel\"@runtime.print_string(@var(hel) @+ \"lo\")")
        )
        assertEquals(
            expected = "hello",
            actual = GenerateCode("@var lo = \"lo\"@runtime.print_string(\"hel\" @+ @var(lo))")
        )
    }

    @Test
    fun testStringEscapes(){
        assertEquals("abc\bdef\b", GenerateCode("@var i = \"abc\\bdef\\b\" @var(i)"))
        assertEquals("abc\ndef\n", GenerateCode("@var i = \"abc\\ndef\\n\" @var(i)"))
        assertEquals("abc\rdef\r", GenerateCode("@var i = \"abc\\rdef\\r\" @var(i)"))
        assertEquals("abc\tdef\t", GenerateCode("@var i = \"abc\\tdef\\t\" @var(i)"))
        assertEquals("abc\\def\\", GenerateCode("@var i = \"abc\\\\def\\\\\" @var(i)"))
        assertEquals("abc'def'", GenerateCode("@var i = \"abc\\'def\\'\" @var(i)"))
        assertEquals("abc\"def\"", GenerateCode("@var i = \"abc\\\"def\\\"\" @var(i)"))
    }

}