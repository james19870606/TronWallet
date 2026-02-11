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

class TRC20TransferActivity : ComponentActivity() {

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
                            title = { Text("TRC20 Token Transfer") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                                }
                            }
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    TRC20TransferScreen(
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
fun TRC20TransferScreen(
    modifier: Modifier = Modifier,
    selectedNode: String,
    tronWeb: TronWeb,
    scope: kotlinx.coroutines.CoroutineScope
) {
    val context = LocalContext.current
    var contractAddress by remember { mutableStateOf("TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t") } // Default USDT
    var fromAddress by remember { mutableStateOf("") }
    var toAddress by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var remark by remember { mutableStateOf("") }
    var privateKey by remember { mutableStateOf("") }
    
    var resultText by remember { mutableStateOf("") }
    var lastTxid by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isEstimating by remember { mutableStateOf(false) }

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

        // Contract Address
        Text(
            text = "Contract Address (e.g. USDT):",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = contractAddress,
            onValueChange = { contractAddress = it },
            placeholder = { Text("TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // From Address
        Text(
            text = "From Address (for Estimation):",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = fromAddress,
            onValueChange = { fromAddress = it },
            placeholder = { Text("T... (Used for fee estimation)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // To Address
        Text(
            text = "To Address:",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = toAddress,
            onValueChange = { toAddress = it },
            placeholder = { Text("T...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Amount
        Text(
            text = "Amount (Tokens):",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            placeholder = { Text("0.0") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Remark
        Text(
            text = "Remark (Memo):",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = remark,
            onValueChange = { remark = it },
            placeholder = { Text("Optional memo") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Private Key
        Text(
            text = "Sender Private Key (Hex):",
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

        // Action Buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Estimate Button
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    if (contractAddress.trim().isEmpty() || fromAddress.trim().isEmpty() || toAddress.trim().isEmpty() || amt <= 0) {
                        Toast.makeText(context, "Please fill Contract, From, To addresses and valid amount", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    scope.launch {
                        isEstimating = true
                        resultText = "Estimating fee..."
                        if (!tronWeb.isInitialized) {
                            tronWeb.setupAsync(privateKey = "01", node = selectedNode)
                        }
                        val response = tronWeb.estimateTrc20FeeAsync(contractAddress.trim(), toAddress.trim(), amt, fromAddress.trim(), remark.trim().ifEmpty { null })
                        isEstimating = false
                        if (response != null) {
                            resultText = JSONObject(response).toString(2)
                        } else {
                            resultText = "Failed to receive fee estimation"
                        }
                    }
                },
                modifier = Modifier.weight(1f).height(50.dp),
                enabled = !isEstimating && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray, contentColor = Color.Blue),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isEstimating) CircularProgressIndicator(modifier = Modifier.size(24.dp)) else Text("Estimate Fee")
            }

            // Transfer Button
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    if (contractAddress.trim().isEmpty() || toAddress.trim().isEmpty() || privateKey.trim().isEmpty() || amt <= 0) {
                        Toast.makeText(context, "Please fill Contract, To address, private key and valid amount", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    scope.launch {
                        isLoading = true
                        lastTxid = null
                        resultText = "Broadcasting TRC20 transaction..."
                        if (!tronWeb.isInitialized) {
                            val (success, error) = tronWeb.setupAsync(privateKey = privateKey.trim(), node = selectedNode)
                            if (!success) {
                                resultText = "Setup Failed: $error"
                                isLoading = false
                                return@launch
                            }
                        }
                        val response = tronWeb.trc20TransferAsync(contractAddress.trim(), toAddress.trim(), amt, privateKey.trim(), remark.trim().ifEmpty { null })
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
                modifier = Modifier.weight(1f).height(50.dp),
                enabled = !isLoading && !isEstimating,
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp)) else Text("Send TRC20")
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
