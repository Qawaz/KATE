import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class ObjectTest {

    @Test
    fun testObjectDeclaration() {
        assertEquals(
            expected = """{
                    |	myVar : int = 5
                    |}""".trimMargin(),
            actual = GenerateCode("@define_object(MyObject) @var myVar = 5 @end_define_object @var(MyObject)")
        )
        assertEquals(
            expected = "5",
            actual = GenerateCode("@var another = 5 @define_object(MyObject) @var myVar = @var(another) @end_define_object @var(MyObject.myVar)")
        )
    }

    @Test
    fun testObjectAccessProperly(){
        val objectCode = "@define_object(MyObject) @var myVar = 5 @define_object(Nested) @var other = \"something\" @end_define_object @end_define_object "
        assertEquals(
            expected = "5",
            actual = GenerateCode("$objectCode@var(MyObject.myVar)")
        )
        assertEquals(
            expected = "something",
            actual = GenerateCode("$objectCode@var(MyObject.Nested.other)")
        )
        assertFails {
            print("succeeded with output : " + GenerateCode("$objectCode@var(MyObject.Nested.myVar)") + " , should've failed")
        }
        assertEquals(
            expected = "5",
            actual = GenerateCode("$objectCode@var(MyObject.Nested.parent.myVar)")
        )
        assertEquals(
            expected = "something",
            actual = GenerateCode("$objectCode@var(MyObject.Nested.parent.Nested.other)")
        )
    }

}