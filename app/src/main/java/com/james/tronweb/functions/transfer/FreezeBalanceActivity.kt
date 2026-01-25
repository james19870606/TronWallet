package com.james.tronweb.functions.transfer

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

class FreezeBalanceActivity : ComponentActivity() {

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
                            title = { Text("Freeze TRX (Stake 2.0)") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                                }
                            }
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    FreezeBalanceScreen(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreezeBalanceScreen(
    modifier: Modifier = Modifier,
    selectedNode: String,
    tronWeb: TronWeb,
    scope: kotlinx.coroutines.CoroutineScope
) {
    val context = LocalContext.current
    var amount by remember { mutableStateOf("") }
    var resourceType by remember { mutableStateOf(0) } // 0: ENERGY, 1: BANDWIDTH
    var privateKey by remember { mutableStateOf("") }
    
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

        // Amount
        Text(
            text = "Amount to Freeze (TRX):",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            placeholder = { Text("Min 1 TRX") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Resource Type
        Text(
            text = "Obtain Resource Type:",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = resourceType == 0,
                onClick = { resourceType = 0 },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) {
                Text("ENERGY")
            }
            SegmentedButton(
                selected = resourceType == 1,
                onClick = { resourceType = 1 },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) {
                Text("BANDWIDTH")
            }
        }

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

        Spacer(modifier = Modifier.height(32.dp))

        // Freeze Button
        Button(
            onClick = {
                val amt = amount.toDoubleOrNull() ?: 0.0
                if (privateKey.trim().isEmpty() || amt <= 0) {
                    Toast.makeText(context, "Please enter valid amount and private key", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                scope.launch {
                    isLoading = true
                    lastTxid = null
                    resultText = "Staking TRX..."
                    val resType = if (resourceType == 0) "ENERGY" else "BANDWIDTH"
                    if (!tronWeb.isInitialized) {
                        val (success, error) = tronWeb.setupAsync(privateKey = privateKey.trim(), node = selectedNode)
                        if (!success) {
                            resultText = "Setup Failed: $error"
                            isLoading = false
                            return@launch
                        }
                    }
                    val response = tronWeb.freezeBalanceAsync(amt, resType, privateKey.trim())
                    isLoading = false
                    if (response != null) {
                        resultText = JSONObject(response).toString(2)
                        val resultObj = response["result"] as? Map<*, *>
                        lastTxid = (resultObj?.get("txid") as? String) ?: (response["txid"] as? String)
                    } else {
                        resultText = "Failed to receive response"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !isLoading,
            shape = RoundedCornerShape(8.dp)
        ) {
            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp)) else Text("Freeze TRX")
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
        if (lastTxid != null) {
            Button(
                onClick = {
                    val baseUrl = if (isMainnet) "https://tronscan.org" else "https://nile.tronscan.org"
                    val url = "$baseUrl/#/transaction/$lastTxid"
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("View on TronScan", color = Color.White)
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
        
        TextButton(
            onClick = {
                if (resultText.isNotEmpty()) {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("TronWeb Result", resultText))
                    Toast.makeText(context, "Result copied to clipboard", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = resultText.isNotEmpty()
        ) {
            Text("Copy JSON Result")
        }
    }
}
