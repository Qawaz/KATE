interface TestInterface {
	val nestedObject : String
	val testList : String
	val testMList : String
	val testBoolean : String
	val testChar : String
	val testVariable : String
	val testDouble : String
	val testLong : String
}

data class TestDataClass (
    val nestedObject : String,
    val testList : String,
    val testMList : String,
    val testBoolean : String,
    val testChar : String,
    val testVariable : String,
    val testDouble : String,
    val testLong : String,
)

data class TestOverriding (
    override val nestedObject : String,
    override val testList : String,
    override val testMList : String,
    override val testBoolean : String,
    override val testChar : String,
    override val testVariable : String,
    override val testDouble : String,
    override val testLong : String,
) : TestInterface