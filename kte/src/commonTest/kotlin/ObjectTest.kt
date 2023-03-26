import com.wakaztahir.kte.GenerateCode
import kotlin.test.Test
import kotlin.test.assertEquals

class ObjectTest {

    @Test
    fun testObjectDeclaration() {
        assertEquals(
            expected = """data class MyObject(
                       |	myVar : Int = 5
                       |)""".trimMargin(),
            actual = GenerateCode("@define_object(MyObject) @var myVar = 5 @end_define_object @var(MyObject)")
        )
    }

}