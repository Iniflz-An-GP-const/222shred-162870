package stepup.app222shred.corpapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import stepup.app222shred.corpapp.ui.composable.approot.AppRoot
import stepup.app222shred.corpapp.ui.theme.SPHOPAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SPHOPAppTheme {
                AppRoot()
            }
        }
    }
}
