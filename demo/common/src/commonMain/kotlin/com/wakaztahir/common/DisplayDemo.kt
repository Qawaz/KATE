package com.wakaztahir.common

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
    Row(modifier = Modifier.fillMaxWidth()) {
        var text by remember { mutableStateOf("") }
        var output by remember { mutableStateOf("") }
        fun updateOutput(text: String) {
            if (codeJob != null && codeJob?.isActive == true) {
                codeJob?.cancel()
            }
            codeJob = scope.launch {
                output = TemplateContext(text).getDestinationAsString()
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