package types

import GenerateCode
import com.wakaztahir.kate.model.KATEType
import com.wakaztahir.kate.model.StringValue
import com.wakaztahir.kate.model.model.MutableKATEObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class TypesTest {

    @Test
    fun testUsingModel() {
        assertEquals(
            expected = "string?",
            actual = GenerateCode("@var(i.getType())", MutableKATEObject {
                insertValue("i", StringValue("hello"))
                setExplicitType("i", KATEType.NullableKateType(KATEType.String))
            })
        )
    }

    @Test
    fun testTypeInsideAFunction() {
        assertEquals(
            expected = "string?",
            actual = GenerateCode("@var i : string? = \"x\" @function type(e) @return e.getType() @end_function @var(type(i))")
        )
    }

    @Test
    fun testTypesGetCopied() {
        assertEquals(
            expected = "string?",
            actual = GenerateCode("@var x : string? = \"hello\" @var y = @var(x) @var(y.getType())")
        )
        assertEquals(
            expected = "string?",
            actual = GenerateCode("@define_object(MyObject) @var x : string? = \"hello\" @end_define_object @var(MyObject[\"x\"].getType())")
        )
        assertEquals(
            expected = "string?",
            actual = GenerateCode("@var x : string? = \"hello\" @var y = @list(1,2,@var(x)) @var(y[2].getType())")
        )
    }

    @Test
    fun testAnyType() {
        assertEquals(expected = "any", actual = GenerateCode("@var i : any = \"hello\" @var(i.getType())"))
        assertEquals(expected = "any", actual = GenerateCode("@var i : any = 0 @var(i.getType())"))
        assertEquals(expected = "any", actual = GenerateCode("@var i : any = 0.0 @var(i.getType())"))
        assertEquals(expected = "any", actual = GenerateCode("@var i : any = 0L @var(i.getType())"))
        assertEquals(expected = "any", actual = GenerateCode("@var i : any = true @var(i.getType())"))
        assertEquals(expected = "any", actual = GenerateCode("@var i : any = false @var(i.getType())"))
        assertEquals(expected = "any", actual = GenerateCode("@var i : any = 'x' @var(i.getType())"))
        assertEquals(expected = "any", actual = GenerateCode("@var i : any = @list(1,2,3) @var(i.getType())"))
        assertEquals(expected = "any", actual = GenerateCode("@var i : any =  @mutable_list(1,2,3) @var(i.getType())"))
    }

    @Test
    fun testStringType() {

        // Assigning correct value to explicit type succeeds
        assertEquals("string", GenerateCode("@var i = \"\" @var(i.getType())"))
        assertEquals("string", GenerateCode("@var i : string = \"\" @var(i.getType())"))
        assertEquals("string?", GenerateCode("@var i : string? = \"\" @var(i.getType())"))

        // Assigning wrong value to explicit type fails
        assertFails { GenerateCode("@var i : string = 0") }
        assertFails { GenerateCode("@var i : string = 0.0") }
        assertFails { GenerateCode("@var i : string = 0L") }
        assertFails { GenerateCode("@var i : string = true") }
        assertFails { GenerateCode("@var i : string = false") }
        assertFails { GenerateCode("@var i : string = 'x'") }

        // Assigning referenced value with correct type succeeds
        assertEquals("0", GenerateCode("@var i : string = \"0\" @var j : string = @var(i) @var(j)"))

        // Assigning referenced value with wrong type fails
        assertFails { GenerateCode("@var i = 0 @var j : string = @var(i)") }
        assertFails { GenerateCode("@var i = 0.0 @var j : string = @var(i)") }
        assertFails { GenerateCode("@var i = 0L @var j : string = @var(i)") }
        assertFails { GenerateCode("@var i = true @var j : string = @var(i)") }
        assertFails { GenerateCode("@var i = false @var j : string = @var(i)") }
        assertFails { GenerateCode("@var i = 'x' @var j : string = @var(i)") }

        // Reassigning a value with value of same type succeeds
        assertEquals("empty", GenerateCode("@var i : string = \"\" @set_var i = \"empty\" @var(i)"))

        // Reassigning a value with value of different type fails
        assertFails { GenerateCode("@var i : string = \"\" @set_var i = 0") }
        assertFails { GenerateCode("@var i : string = \"\" @set_var i = 0.0") }
        assertFails { GenerateCode("@var i : string = \"\" @set_var i = 0L") }
        assertFails { GenerateCode("@var i : string = \"\" @set_var i = true") }
        assertFails { GenerateCode("@var i : string = \"\" @set_var i = false") }
        assertFails { GenerateCode("@var i : string = \"\" @set_var i = 'x'") }
    }

    @Test
    fun testIntType() {
        assertEquals("int", GenerateCode("@var i = 55 @var(i.getType())"))
        assertEquals("int", GenerateCode("@var i : int = 55 @var(i.getType())"))
        assertEquals("int?", GenerateCode("@var i : int? = 55 @var(i.getType())"))

        assertFails { GenerateCode("@var i : int = 55 @var j : string = @var(i)") }
        assertFails { GenerateCode("@var i : int = \"\"") }
        assertFails { GenerateCode("@var i : int = 0.0") }
        assertFails { GenerateCode("@var i : int = 0L") }
        assertFails { GenerateCode("@var i : int = true") }
        assertFails { GenerateCode("@var i : int = false") }
        assertFails { GenerateCode("@var i : int = 'x'") }

        assertEquals("55", GenerateCode("@var i : int = 55 @var j : int = @var(i) @var(j)"))

        assertFails { GenerateCode("@var i = \"\" @var j : int = @var(i)") }
        assertFails { GenerateCode("@var i = 0.0 @var j : int = @var(i)") }
        assertFails { GenerateCode("@var i = 0L @var j : int = @var(i)") }
        assertFails { GenerateCode("@var i = true @var j : int = @var(i)") }
        assertFails { GenerateCode("@var i = false @var j : int = @var(i)") }
        assertFails { GenerateCode("@var i = 'x' @var j : int = @var(i)") }

        assertEquals("15", GenerateCode("@var i : int = 3 @set_var i = 15 @var(i)"))

        assertFails { GenerateCode("@var i : int = 5 @set_var i = \"\"") }
        assertFails { GenerateCode("@var i : int = 5 @set_var i = 0.0") }
        assertFails { GenerateCode("@var i : int = 5 @set_var i = 0L") }
        assertFails { GenerateCode("@var i : int = 5 @set_var i = true") }
        assertFails { GenerateCode("@var i : int = 5 @set_var i = false") }
        assertFails { GenerateCode("@var i : int = 5 @set_var i = 'x'") }
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

        assertEquals("55.0", GenerateCode("@var i : double = 55. @var j : double = @var(i) @var(j)"))

        assertFails { GenerateCode("@var i = \"\" @var j : double = @var(i)") }
        assertFails { GenerateCode("@var i = 0 @var j : double = @var(i)") }
        assertFails { GenerateCode("@var i = 0L @var j : double = @var(i)") }
        assertFails { GenerateCode("@var i = true @var j : double = @var(i)") }
        assertFails { GenerateCode("@var i = false @var j : double = @var(i)") }
        assertFails { GenerateCode("@var i = 'x' @var j : double = @var(i)") }

        assertEquals("5.0", GenerateCode("@var i : double = 3. @set_var i = 5. @var(i)"))

        assertFails { GenerateCode("@var i : double = 3. @set_var i = \"\"") }
        assertFails { GenerateCode("@var i : double = 3. @set_var i = 0") }
        assertFails { GenerateCode("@var i : double = 3. @set_var i = 0L") }
        assertFails { GenerateCode("@var i : double = 3. @set_var i = true") }
        assertFails { GenerateCode("@var i : double = 3. @set_var i = false") }
        assertFails { GenerateCode("@var i : double = 3. @set_var i = 'x'") }
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

        assertEquals("true", GenerateCode("@var i : boolean = true @var j : boolean = @var(i) @var(j)"))
        assertEquals("false", GenerateCode("@var i : boolean = false @var j : boolean = @var(i) @var(j)"))

        assertFails { GenerateCode("@var i = \"\" @var j : boolean = @var(i)") }
        assertFails { GenerateCode("@var i = 0 @var j : boolean = @var(i)") }
        assertFails { GenerateCode("@var i = 0L @var j : boolean = @var(i)") }
        assertFails { GenerateCode("@var i = 0.0 @var j : boolean = @var(i)") }
        assertFails { GenerateCode("@var i = 'x' @var j : boolean = @var(i)") }

        assertEquals("false", GenerateCode("@var i : boolean = true @set_var i = false @var(i)"))
        assertEquals("true", GenerateCode("@var i : boolean = false @set_var i = true @var(i)"))

        assertFails { GenerateCode("@var i : boolean = true @set_var i = \"\"") }
        assertFails { GenerateCode("@var i : boolean = true @set_var i = 0") }
        assertFails { GenerateCode("@var i : boolean = true @set_var i = 0L") }
        assertFails { GenerateCode("@var i : boolean = true @set_var i = 0.0") }
        assertFails { GenerateCode("@var i : boolean = true @set_var i = 'x'") }

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

        assertEquals("long", GenerateCode("@var i : long = 55 @var j = @var(i) @var(j.getType())"))
        assertEquals("55", GenerateCode("@var i : long = 55L @var j : long = @var(i) @var(j)"))

        assertFails { GenerateCode("@var i = \"\" @var j : long = @var(i)") }
        assertFails { GenerateCode("@var i = 0.0 @var j : long = @var(i)") }
        assertFails { GenerateCode("@var i = true @var j : long = @var(i)") }
        assertFails { GenerateCode("@var i = false @var j : long = @var(i)") }
        assertFails { GenerateCode("@var i = 'x' @var j : long = @var(i)") }
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

        assertEquals("s", GenerateCode("@var i : char = 's' @var j : char = @var(i) @var(j)"))

        assertFails { GenerateCode("@var i = \"\" @var j : char = @var(i)") }
        assertFails { GenerateCode("@var i = 0 @var j : char = @var(i)") }
        assertFails { GenerateCode("@var i = 0.0 @var j : char = @var(i)") }
        assertFails { GenerateCode("@var i = 0L @var j : char = @var(i)") }
        assertFails { GenerateCode("@var i = true @var j : char = @var(i)") }
        assertFails { GenerateCode("@var i = false @var j : char = @var(i)") }

        assertEquals("s", GenerateCode("@var i : char = 'x' @set_var i = 's' @var(i)"))

        assertFails { GenerateCode("@var i : char = 's' @set_var i = \"\"") }
        assertFails { GenerateCode("@var i : char = 's' @set_var i = 0") }
        assertFails { GenerateCode("@var i : char = 's' @set_var i = 0.0") }
        assertFails { GenerateCode("@var i : char = 's' @set_var i = 0L") }
        assertFails { GenerateCode("@var i : char = 's' @set_var i = true") }
        assertFails { GenerateCode("@var i : char = 's' @set_var i = false") }
    }

    @Test
    fun testListType() {

        // Assigning correct value to explicit type succeeds
        assertEquals("list<any>", GenerateCode("@var i = @list() @var(i.getType())"))
        assertEquals("list<int>", GenerateCode("@var i = @list(1,2,3) @var(i.getType())"))
        assertEquals("list<int>", GenerateCode("@var i : list<int> = @mutable_list(1,2,3) @var(i.getType())"))
        assertEquals("list<int>", GenerateCode("@var i : list<int> = @list(1,2,3) @var(i.getType())"))
        assertEquals("list<int>?", GenerateCode("@var i : list<int>? = @list(1,2,3) @var(i.getType())"))
        assertEquals("list<any>?", GenerateCode("@var i : list<any>? = @list(1,'2',\"3\") @var(i.getType())"))

        // Assigning wrong value to explicit type fails
        assertFails { GenerateCode("@var i : list<int> = \"0\"") }
        assertFails { GenerateCode("@var i : list<int> = 0") }
        assertFails { GenerateCode("@var i : list<int> = 0.0") }
        assertFails { GenerateCode("@var i : list<int> = 0L") }
        assertFails { GenerateCode("@var i : list<int> = true") }
        assertFails { GenerateCode("@var i : list<int> = false") }
        assertFails { GenerateCode("@var i : list<int> = 'x'") }
        assertFails { GenerateCode("@var i : list<int> = @list(\"hello\")") }

        // Assigning referenced value with correct type succeeds
        assertEquals("1,2,3", GenerateCode("@var i : list<int> = @list(1,2,3) @var j : list<int> = @var(i) @var(j)"))

        // Assigning referenced value with wrong type fails
        assertFails { GenerateCode("@var i = 0 @var j : list<any> = @var(i)") }
        assertFails { GenerateCode("@var i = 0.0 @var j : list<any> = @var(i)") }
        assertFails { GenerateCode("@var i = 0L @var j : list<any> = @var(i)") }
        assertFails { GenerateCode("@var i = true @var j : list<any> = @var(i)") }
        assertFails { GenerateCode("@var i = false @var j : list<any> = @var(i)") }
        assertFails { GenerateCode("@var i = 'x' @var j : list<any> = @var(i)") }

        // Reassigning a value with value of same type succeeds
        assertEquals("4,5,6", GenerateCode("@var i : list<any> = @list(1,2,3) @set_var i = @list(4,5,6) @var(i)"))

        // Reassigning a value with value of different type fails
        assertFails { GenerateCode("@var i : list<any> = @list(1,2,3) @set_var i = 0") }
        assertFails { GenerateCode("@var i : list<any> = @list(1,2,3) @set_var i = 0.0") }
        assertFails { GenerateCode("@var i : list<any> = @list(1,2,3) @set_var i = 0L") }
        assertFails { GenerateCode("@var i : list<any> = @list(1,2,3) @set_var i = true") }
        assertFails { GenerateCode("@var i : list<any> = @list(1,2,3) @set_var i = false") }
        assertFails { GenerateCode("@var i : list<any> = @list(1,2,3) @set_var i = 'x'") }
    }

    @Test
    fun testMutableListType() {

        // Assigning correct value to explicit type succeeds
        assertEquals("mutable_list<any>", GenerateCode("@var i = @mutable_list() @var(i.getType())"))
        assertEquals("mutable_list<int>", GenerateCode("@var i = @mutable_list(1,2,3) @var(i.getType())"))
        assertEquals(
            "mutable_list<int>",
            GenerateCode("@var i : mutable_list<int> = @mutable_list(1,2,3) @var(i.getType())")
        )
        assertEquals(
            "mutable_list<int>?",
            GenerateCode("@var i : mutable_list<int>? = @mutable_list(1,2,3) @var(i.getType())")
        )
        assertEquals(
            "mutable_list<any>?",
            GenerateCode("@var i : mutable_list<any>? = @mutable_list(1,'2',\"3\") @var(i.getType())")
        )

        // Assigning wrong value to explicit type fails
        assertFails { GenerateCode("@var i : mutable_list<int> = \"0\"") }
        assertFails { GenerateCode("@var i : mutable_list<int> = 0") }
        assertFails { GenerateCode("@var i : mutable_list<int> = 0.0") }
        assertFails { GenerateCode("@var i : mutable_list<int> = 0L") }
        assertFails { GenerateCode("@var i : mutable_list<int> = true") }
        assertFails { GenerateCode("@var i : mutable_list<int> = false") }
        assertFails { GenerateCode("@var i : mutable_list<int> = 'x'") }
        assertFails { GenerateCode("@var i : mutable_list<int> = @mutable_list(\"hello\")") }
        assertFails { GenerateCode("@var i : mutable_list<any> = @list(\"hello\")") }

        // Assigning referenced value with correct type succeeds
        assertEquals(
            "1,2,3",
            GenerateCode("@var i : mutable_list<int> = @mutable_list(1,2,3) @var j : mutable_list<int> = @var(i) @var(j)")
        )

        // Assigning referenced value with wrong type fails
        assertFails { GenerateCode("@var i = 0 @var j : mutable_list<any> = @var(i)") }
        assertFails { GenerateCode("@var i = 0.0 @var j : mutable_list<any> = @var(i)") }
        assertFails { GenerateCode("@var i = 0L @var j : mutable_list<any> = @var(i)") }
        assertFails { GenerateCode("@var i = true @var j : mutable_list<any> = @var(i)") }
        assertFails { GenerateCode("@var i = false @var j : mutable_list<any> = @var(i)") }
        assertFails { GenerateCode("@var i = 'x' @var j : mutable_list<any> = @var(i)") }

        // Reassigning a value with value of same type succeeds
        assertEquals(
            "4,5,6",
            GenerateCode("@var i : mutable_list<any> = @mutable_list(1,2,3) @set_var i = @mutable_list(4,5,6) @var(i)")
        )

        // Reassigning a value with value of different type fails
        assertFails { GenerateCode("@var i : mutable_list<any> = @mutable_list(1,2,3) @set_var i = 0") }
        assertFails { GenerateCode("@var i : mutable_list<any> = @mutable_list(1,2,3) @set_var i = 0.0") }
        assertFails { GenerateCode("@var i : mutable_list<any> = @mutable_list(1,2,3) @set_var i = 0L") }
        assertFails { GenerateCode("@var i : mutable_list<any> = @mutable_list(1,2,3) @set_var i = true") }
        assertFails { GenerateCode("@var i : mutable_list<any> = @mutable_list(1,2,3) @set_var i = false") }
        assertFails { GenerateCode("@var i : mutable_list<any> = @mutable_list(1,2,3) @set_var i = 'x'") }
    }

    @Test
    fun testObjectType() {
        assertEquals("object<any>", GenerateCode("@define_object(MyObj) @end_define_object @var(MyObj.getType())"))
        assertEquals("object<int>", GenerateCode("@define_object<int>(MyObj) @end_define_object @var(MyObj.getType())"))
        assertEquals(
            "object<int>",
            GenerateCode("@define_object<int>(MyObj) @var i = 5 @end_define_object @var(MyObj.getType())")
        )
        assertEquals(
            "object<int?>",
            GenerateCode("@define_object<int?>(MyObj) @var i = 5 @end_define_object @var(MyObj.getType())")
        )
    }

    @Test
    fun testObjectListType() {
        assertEquals(
            expected = "list<int>",
            actual = GenerateCode("@define_object(Test) @var list = @list(1,2,3) @end_define_object @var(Test.list.getType())")
        )
    }

    @Test
    fun testFunctionType() {
        assertEquals(
            expected = "(object<any>?,list<any>,int?,object<string?>) -> list<object<any>>",
            actual = GenerateCode("@function some (val1 : object<any>?,val2 : list<any>,val3 : int?,val4 : object<string?>) -> list<object<any>> @end_function @var(some.getType())")
        )
    }

}