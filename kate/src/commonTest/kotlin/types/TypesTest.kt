package types

import GenerateCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class TypesTest {

    @Test
    fun testStringType() {
        assertEquals("string", GenerateCode("@var i = \"\" @var(i.getType())"))
        assertEquals("string", GenerateCode("@var i : string = \"\" @var(i.getType())"))
        assertEquals("string?", GenerateCode("@var i : string? = \"\" @var(i.getType())"))
        assertFails { GenerateCode("@var i : string = 0") }
        assertFails { GenerateCode("@var i : string = 0.0") }
        assertFails { GenerateCode("@var i : string = 0L") }
        assertFails { GenerateCode("@var i : string = true") }
        assertFails { GenerateCode("@var i : string = false") }
        assertFails { GenerateCode("@var i : string = 'x'") }
    }

    @Test
    fun testIntType() {
        assertEquals("int", GenerateCode("@var i = 55 @var(i.getType())"))
        assertEquals("int", GenerateCode("@var i : int = 55 @var(i.getType())"))
        assertEquals("int?", GenerateCode("@var i : int? = 55 @var(i.getType())"))
        assertFails { GenerateCode("@var i : int = \"\"") }
        assertFails { GenerateCode("@var i : int = 0.0") }
        assertFails { GenerateCode("@var i : int = 0L") }
        assertFails { GenerateCode("@var i : int = true") }
        assertFails { GenerateCode("@var i : int = false") }
        assertFails { GenerateCode("@var i : int = 'x'") }
    }

    @Test
    fun testDoubleType() {
        assertEquals("double", GenerateCode("@var i = 55. @var(i.getType())"))
        assertEquals("double", GenerateCode("@var i : double = 55. @var(i.getType())"))
        assertEquals("double?", GenerateCode("@var i : double? = 55. @var(i.getType())"))
        assertFails { GenerateCode("@var i : double = \"\"") }
        assertFails { GenerateCode("@var i : double = 0") }
        assertFails { GenerateCode("@var i : double = 0L") }
        assertFails { GenerateCode("@var i : double = true") }
        assertFails { GenerateCode("@var i : double = false") }
        assertFails { GenerateCode("@var i : double = 'x'") }
    }

    @Test
    fun testBooleanType() {
        assertEquals("boolean", GenerateCode("@var i = true @var(i.getType())"))
        assertEquals("boolean", GenerateCode("@var i = false @var(i.getType())"))
        assertEquals("boolean", GenerateCode("@var i : boolean = true @var(i.getType())"))
        assertEquals("boolean", GenerateCode("@var i : boolean = false @var(i.getType())"))
        assertEquals("boolean?", GenerateCode("@var i : boolean? = true @var(i.getType())"))
        assertEquals("boolean?", GenerateCode("@var i : boolean? = false @var(i.getType())"))
        assertFails { GenerateCode("@var i : boolean = \"\"") }
        assertFails { GenerateCode("@var i : boolean = 0") }
        assertFails { GenerateCode("@var i : boolean = 0L") }
        assertFails { GenerateCode("@var i : boolean = 0.0") }
        assertFails { GenerateCode("@var i : boolean = 'x'") }
    }

    @Test
    fun testLongType() {
        assertEquals("long", GenerateCode("@var i = 55L @var(i.getType())"))
        assertEquals("long", GenerateCode("@var i : long = 55L @var(i.getType())"))
        assertEquals("long", GenerateCode("@var i : long = 55 @var(i.getType())"))
        assertEquals("long?", GenerateCode("@var i : long? = 55L @var(i.getType())"))
        assertEquals("long?", GenerateCode("@var i : long? = 55 @var(i.getType())"))
        assertFails { GenerateCode("@var i : long = \"\"") }
        assertFails { GenerateCode("@var i : long = 0.0") }
        assertFails { GenerateCode("@var i : long = true") }
        assertFails { GenerateCode("@var i : long = false") }
        assertFails { GenerateCode("@var i : long = 'x'") }
    }

    @Test
    fun testCharType() {
        assertEquals("char", GenerateCode("@var i = 's' @var(i.getType())"))
        assertEquals("char", GenerateCode("@var i : char = 's' @var(i.getType())"))
        assertEquals("char?", GenerateCode("@var i : char? = 's' @var(i.getType())"))
        assertFails { GenerateCode("@var i : char = \"\"") }
        assertFails { GenerateCode("@var i : char = 0") }
        assertFails { GenerateCode("@var i : char = 0.0") }
        assertFails { GenerateCode("@var i : char = 0L") }
        assertFails { GenerateCode("@var i : char = true") }
        assertFails { GenerateCode("@var i : char = false") }
    }

    @Test
    fun testListType() {
        assertEquals("list", GenerateCode("@var i = @list(1,2,3) @var(i.getType())"))
    }

    @Test
    fun testMutableListType() {
        assertEquals("mutable_list", GenerateCode("@var i = @mutable_list(1,2,3) @var(i.getType())"))
    }

    @Test
    fun testObjectType() {
        assertEquals("object", GenerateCode("@define_object(MyObj) @end_define_object @var(MyObj.getType())"))
    }

}