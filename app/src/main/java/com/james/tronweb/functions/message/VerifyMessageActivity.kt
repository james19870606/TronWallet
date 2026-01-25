package com.james.tronweb.functions.message

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.james.sdk.tronweb.TronWeb
import com.james.tronweb.ui.theme.TronWebTheme
import kotlinx.coroutines.launch
import org.json.JSONObject

class VerifyMessageActivity : ComponentActivity() {

    private lateinit var tronWeb: TronWeb
    private var selectedNode: String = TronWeb.TRON_NILE_NET

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        selectedNode = intent.getStringExtra("node") ?: TronWeb.TRON_NILE_NET
        tronWeb = TronWeb(this)

        setContent {
            TronWebTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Verify Message V2") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                                }
                            }
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    VerifyMessageScreen(
                        modifier = Modifier.padding(innerPadding),
                        selectedNode = selectedNode,
                        tronWeb = tronWeb,
                        scope = lifecycleScope
                    )
                }
            }
        }
    }
}

@Composable
fun VerifyMessageScreen(
    modifier: Modifier = Modifier,
    selectedNode: String,
    tronWeb: TronWeb,
    scope: kotlinx.coroutines.CoroutineScope
) {
    val context = LocalContext.current
    var message by remember { mutableStateOf("") }
    var signature by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Network Status
        val isMainnet = selectedNode == TronWeb.TRON_MAINNET
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isMainnet) Color(0xFF4CAF50) else Color(0xFFE91E63),
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isMainnet) "NETWORK: MAINNET" else "NETWORK: NILE TESTNET",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Message Input
        Text(
            text = "Original Message:",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            placeholder = { Text("Enter the original message...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Signature Input
        Text(
            text = "Signature (Hex):",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = signature,
            onValueChange = { signature = it },
            placeholder = { Text("Enter signature hex...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Address Input
        Text(
            text = "Expected Signer Address:",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            placeholder = { Text("T...") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Verify Button
        Button(
            onClick = {
                if (message.isEmpty() || signature.trim().isEmpty() || address.trim().isEmpty()) {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                scope.launch {
                    isLoading = true
                    resultText = "Verifying signature..."
                    
                    if (!tronWeb.isInitialized) {
                        val (success, error) = tronWeb.setupAsync(privateKey = "01", node = selectedNode)
                        if (!success) {
                            resultText = "Setup Failed: $error"
                            isLoading = false
                            return@launch
                        }
                    }
                    
                    val response = tronWeb.verifyMessageV2Async(message, signature.trim(), address.trim())
                    isLoading = false
                    
                    if (response != null) {
                        resultText = JSONObject(response).toString(2)
                    } else {
                        resultText = "Failed to receive response from TronWeb"
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoading,
            shape = RoundedCornerShape(8.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Verify Signature")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Result TextView
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                .padding(8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = resultText,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Copy Button
        TextButton(
            onClick = {
                if (resultText.isNotEmpty()) {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = android.content.ClipData.newPlainText("TronWeb Result", resultText)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Result copied to clipboard", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = resultText.isNotEmpty() && !isLoading
        ) {
            Text("Copy JSON Result")
        }
    }
}
