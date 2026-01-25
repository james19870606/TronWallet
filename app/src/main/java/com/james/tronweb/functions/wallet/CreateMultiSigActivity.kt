package com.james.tronweb.functions.wallet

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.james.sdk.tronweb.TronWeb
import com.james.tronweb.ui.theme.TronWebTheme
import kotlinx.coroutines.launch
import org.json.JSONObject

class CreateMultiSigActivity : ComponentActivity() {

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
                            title = { Text("Create Multi-Sig") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                                }
                            }
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    CreateMultiSigScreen(
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
fun CreateMultiSigScreen(
    modifier: Modifier = Modifier,
    selectedNode: String,
    tronWeb: TronWeb,
    scope: kotlinx.coroutines.CoroutineScope
) {
    val context = LocalContext.current
    var ownerAddress by remember { mutableStateOf("") }
    var privateKey by remember { mutableStateOf("") }
    var ownersText by remember { mutableStateOf("") }
    var requiredSignatures by remember { mutableStateOf("") }
    
    var resultText by remember { mutableStateOf("") }
    var lastTxid by remember { mutableStateOf<String?>(null) }
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

        // Owner Address
        Text(
            text = "Account Address (to update):",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = ownerAddress,
            onValueChange = { ownerAddress = it },
            placeholder = { Text("T...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Private Key
        Text(
            text = "Owner Private Key (Hex):",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = privateKey,
            onValueChange = { privateKey = it },
            placeholder = { Text("64-digit hex") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Owners List
        Text(
            text = "Multi-Sig Owners (One per line):",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = ownersText,
            onValueChange = { ownersText = it },
            placeholder = { Text("Address 1\nAddress 2") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
        )
        Text(
            text = "Enter at least 2 addresses",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Required Threshold
        Text(
            text = "Threshold (Required Signatures):",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = requiredSignatures,
            onValueChange = { requiredSignatures = it },
            placeholder = { Text("e.g. 2") },
            modifier = Modifier.width(120.dp).align(Alignment.Start),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Create Button
        Button(
            onClick = {
                val owners = ownersText.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                val required = requiredSignatures.trim().toIntOrNull() ?: 0

                if (ownerAddress.trim().isEmpty() || privateKey.trim().isEmpty() || owners.size < 2 || required < 1) {
                    Toast.makeText(context, "Please fill all fields correctly. At least 2 owners required.", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                
                scope.launch {
                    isLoading = true
                    lastTxid = null
                    resultText = "Creating Multi-Sig address... (This may cost 100 TRX)"
                    
                    if (!tronWeb.isInitialized) {
                        val (success, error) = tronWeb.setupAsync(privateKey = privateKey.trim(), node = selectedNode)
                        if (!success) {
                            resultText = "Setup Failed: $error"
                            isLoading = false
                            return@launch
                        }
                    }
                    
                    val response = tronWeb.createMultiSigAddressAsync(
                        ownerAddress.trim(),
                        owners,
                        required,
                        privateKey.trim()
                    )
                    isLoading = false
                    
                    if (response != null) {
                        resultText = JSONObject(response).toString(2)
                        
                        // Extract txid
                        var capturedTxid: String? = null
                        val resultObj = response["result"] as? Map<*, *>
                        if (resultObj != null) {
                            capturedTxid = resultObj["txid"] as? String
                        } else {
                            capturedTxid = response["txid"] as? String
                        }
                        
                        if (capturedTxid != null) {
                            lastTxid = capturedTxid
                        }
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
                Text("Create Multi-Sig Address")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Result TextView
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
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

        // Bottom Buttons
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (lastTxid != null) {
                Button(
                    onClick = {
                        val baseUrl = if (isMainnet) "https://tronscan.org" else "https://nile.tronscan.org"
                        val url = "$baseUrl/#/transaction/$lastTxid"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("View on TronScan", color = Color.White)
                }
            }
            
            TextButton(
                onClick = {
                    if (resultText.isNotEmpty()) {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = android.content.ClipData.newPlainText("TronWeb Result", resultText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Result copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally),
                enabled = resultText.isNotEmpty() && !isLoading
            ) {
                Text("Copy JSON Result")
            }
        }
    }
}
