import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application


fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "KTE-DEV"
    ) {
        MaterialTheme {
            Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                DisplayDemo()
            }
        }
    }
}