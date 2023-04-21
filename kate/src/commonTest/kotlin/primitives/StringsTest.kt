package primitives

import GenerateCode
import GeneratePartialRaw
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
        assertEquals(
            expected = "hello",
            actual = GenerateCode("@var l = \"l\" @var t = \"h\" @set_var t = @var(t) @+ \"e\" @set_var t = @var(t) @+ \"l\" @set_var t = @var(t) @+ @var(l) @set_var t = @var(t) @+ \"o\" @var(t)")
        )
        assertEquals(
            expected = "21hello",
            actual = GenerateCode("@var i = \"1\" @var l = \"l\" @var t = @var(i) @+ \"h\" @set_var t = \"2\" @+ @var(t) @+ \"e\" @set_var t = @var(t) @+ \"l\" @set_var t = @var(t) @+ @var(l) @set_var t = @var(t) @+ \"o\" @var(t)")
        )
        assertEquals(
            expected = "21hello",
            actual = GenerateCode("@var i = \"1\" @var l = \"l\" @var t = @var(i) @+ \"h\" @set_var t = \"2\" @+ @var(t) @+ \"e\" @set_var t += \"l\" @set_var t += @var(l) @set_var t += \"o\" @var(t)")
        )
    }

    @Test
    fun testJoinToString(){
        assertEquals(
            expected = "hxexlxlxox",
            actual = GeneratePartialRaw(
                """@var i = @list("h","e","l","l","o")
                |@function adder(param)
                |@return param + "x"
                |@end_function
                |@default_no_raw @var(i.joinToString("",adder)) @end_default_no_raw
                |""".trimMargin()
            )
        )
    }

    @Test
    fun testOtherStringFunctions(){
        assertEquals(
            expected = "truefalsetruefalselohel",
            actual = GenerateCode("@var i = \"hello\" @var(i.startsWith(\"he\"))@var(i.startsWith(\"so\"))@var(i.endsWith(\"lo\"))@var(i.endsWith(\"ol\"))@var(i.removePrefix(\"hel\"))@var(i.removeSuffix(\"lo\"))")
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

    @Test
    fun testStringType(){
        assertEquals("string",GenerateCode("@var i = \"hello\" @var(i.getType())"))
    }

}