import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.window.singleWindowApplication
import com.wakaztahir.common.DisplayDemo

fun main() = singleWindowApplication {
    MaterialTheme(colorScheme = darkColorScheme()) {
        DisplayDemo()
    }
}