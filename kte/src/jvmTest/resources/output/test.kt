data class TestObject (
    val nestedObject : String,
    val testList : String,
    val testMList : String,
    val testBoolean : String,
    val testChar : String,
    val testVariable : String,
    val testDouble : String,
    val testLong : String,
)

interface TestObject {
	val nestedObject : String
	val testList : String
	val testMList : String
	val testBoolean : String
	val testChar : String
	val testVariable : String
	val testDouble : String
	val testLong : String
}