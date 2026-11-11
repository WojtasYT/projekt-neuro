package lab.neurotrino.app

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lab.neurotrino.app.ui.ShellViewModel
import lab.neurotrino.app.ui.navigation.NeurotrinoRoot
import lab.neurotrino.app.ui.theme.NeurotrinoTheme

class MainActivity : ComponentActivity() {
    private val vm: ShellViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val incoming = extractSharedText(intent)
        val openCapture = intent?.action == "lab.neurotrino.app.CAPTURE"
        val openResume = intent?.action == "lab.neurotrino.app.RESUME"
        setContent {
            val profile by vm.profile.collectAsStateWithLifecycle()
            val online = remember { mutableStateOf(isOnline()) }
            NeurotrinoTheme(dark = profile.theme != "light") {
                NeurotrinoRoot(
                    vm = vm,
                    openCapture = openCapture,
                    openResume = openResume,
                    incomingText = incoming,
                    online = online.value,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        extractSharedText(intent)?.let { vm.handleIncomingText(it) }
        if (intent.action == "lab.neurotrino.app.CAPTURE") vm.openCapture()
    }

    private fun extractSharedText(intent: Intent?): String? {
        if (intent == null) return null
        return when (intent.action) {
            Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)
            Intent.ACTION_PROCESS_TEXT -> intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
            else -> null
        }?.trim()?.takeIf { it.isNotEmpty() }
    }

    private fun isOnline(): Boolean {
        val cm = getSystemService(ConnectivityManager::class.java) ?: return true
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
