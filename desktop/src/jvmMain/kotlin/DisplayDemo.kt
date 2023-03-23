import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.wakaztahir.kte.TemplateContext
import com.wakaztahir.kte.model.model.TemplateModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayDemo() {

    val scope = rememberCoroutineScope()
    var codeJob by remember { mutableStateOf<Job?>(null) }


    var text by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf("") }

    Column {
        if(errorText.isNotEmpty()){
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.onError
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            fun updateOutput(text: String) {
                if (codeJob != null && codeJob?.isActive == true) {
                    codeJob?.cancel()
                }
                codeJob = scope.launch {
                    try {
                        output = TemplateContext(text).getDestinationAsString()
                    } catch (e: Throwable) {
                        errorText = e.message ?: ""
                    }
                }
            }
            OutlinedTextField(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                value = text,
                onValueChange = {
                    text = it
                    updateOutput(it)
                }
            )
            Text(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                text = output
            )
        }
    }
}