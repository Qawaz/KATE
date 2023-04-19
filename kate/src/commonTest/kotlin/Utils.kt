import com.wakaztahir.kate.TemplateContext
import com.wakaztahir.kate.model.model.MutableKATEObject
import kotlin.test.Ignore

@Suppress("TestFunctionName")
@Ignore
internal fun GenerateCode(code: String): String = TemplateContext(code).getDestinationAsString()

@Suppress("TestFunctionName")
@Ignore
internal fun GeneratePartialRaw(code: String) = GenerateCode("@partial_raw $code @end_partial_raw")

@Suppress("TestFunctionName")
@Ignore
internal fun GenerateCode(code: String, model: MutableKATEObject) = TemplateContext(code, model).getDestinationAsString()