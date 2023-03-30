interface TestInterface {
	val nestedObject : Object
	val testList : List
	val testMList : Mutable_list
	val testBoolean : Boolean
	val testChar : Char
	val testVariable : String
	val testDouble : Double
	val testLong : Long
}

data class TestDataClass (
    val nestedObject : Object,
    val testList : List,
    val testMList : Mutable_list,
    val testBoolean : Boolean,
    val testChar : Char,
    val testVariable : String,
    val testDouble : Double,
    val testLong : Long,
)

data class TestOverriding (
    override val nestedObject : Object,
    override val testList : List,
    override val testMList : Mutable_list,
    override val testBoolean : Boolean,
    override val testChar : Char,
    override val testVariable : String,
    override val testDouble : Double,
    override val testLong : Long,
) : TestInterface