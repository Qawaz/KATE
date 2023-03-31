interface TestInterface {
	val nestedObject : NestedObject
	val testList : List<Int>
	val testMList : List<Double>
	val testBoolean : Boolean
	val testChar : Char
	val testVariable : String
	val testListList : List<List<Int>>
	val testDouble : Double
	val testLong : Long
}

data class TestDataClass (
    val nestedObject : NestedObject,
    val testList : List<Int>,
    val testMList : List<Double>,
    val testBoolean : Boolean,
    val testChar : Char,
    val testVariable : String,
    val testListList : List<List<Int>>,
    val testDouble : Double,
    val testLong : Long,
)

data class TestOverriding (
    override val nestedObject : NestedObject,
    override val testList : List<Int>,
    override val testMList : List<Double>,
    override val testBoolean : Boolean,
    override val testChar : Char,
    override val testVariable : String,
    override val testListList : List<List<Int>>,
    override val testDouble : Double,
    override val testLong : Long,
) : TestInterface