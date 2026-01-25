package com.james.tronweb

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.james.sdk.tronweb.TronWeb
import com.james.tronweb.functions.wallet.*
import com.james.tronweb.functions.query.*
import com.james.tronweb.functions.transfer.*
import com.james.tronweb.functions.message.*
import com.james.tronweb.ui.theme.TronWebTheme

data class FunctionItem(val title: String, val activityClass: Class<out ComponentActivity>)
data class Section(val title: String, val items: List<FunctionItem>)

class MainActivity : ComponentActivity() {

    private val sections = listOf(
        Section("Wallet Management", listOf(
            FunctionItem("Create Random Wallet", CreateRandomWalletActivity::class.java),
            FunctionItem("Import Account from Private Key", ImportPrivateKeyActivity::class.java),
            FunctionItem("Import Account from Mnemonic", ImportMnemonicActivity::class.java),
            FunctionItem("Switch Wallet (Reset PrivateKey)", SwitchWalletActivity::class.java),
            FunctionItem("Create Multi-Sig Address", CreateMultiSigActivity::class.java)
        )),
        Section("Account Query", listOf(
            FunctionItem("Get Account Info (Activation Check)", GetAccountInfoActivity::class.java),
            FunctionItem("Get TRX Balance", GetTRXBalanceActivity::class.java),
            FunctionItem("Get TRC20 Token Balance", GetTRC20BalanceActivity::class.java),
            FunctionItem("Get Account Resources (Energy/Bandwidth)", GetAccountResourcesActivity::class.java),
            FunctionItem("Get Chain Parameters", GetChainParametersActivity::class.java)
        )),
        Section("Transaction Operations", listOf(
            FunctionItem("TRX Transfer", TRXTransferActivity::class.java),
            FunctionItem("TRC20 Token Transfer", TRC20TransferActivity::class.java),
            FunctionItem("Multi-Sig TRX Transfer", MultiSigTRXTransferActivity::class.java),
            FunctionItem("Multi-Sig TRC20 Transfer", MultiSigTRC20TransferActivity::class.java),
            FunctionItem("Freeze TRX for Resources (Stake 2.0)", FreezeBalanceActivity::class.java),
            FunctionItem("Unfreeze TRX (Stake 2.0)", UnfreezeBalanceActivity::class.java),
            FunctionItem("Delegate Resources", DelegateResourceActivity::class.java)
        )),
        Section("Message Signing & Verification", listOf(
            FunctionItem("Sign Message V2", SignMessageActivity::class.java),
            FunctionItem("Verify Message V2", VerifyMessageActivity::class.java)
        ))
    )

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TronWebTheme {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text("TRON Wallet Demo", fontWeight = FontWeight.Bold) }
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        sections = sections,
                        onItemClick = { item, node ->
                            val intent = Intent(this, item.activityClass)
                            intent.putExtra("node", node)
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    sections: List<Section>,
    onItemClick: (FunctionItem, String) -> Unit
) {
    var selectedNetwork by remember { mutableStateOf(1) } // Default to Nile for safety

    val currentNode = if (selectedNetwork == 0) TronWeb.TRON_MAINNET else TronWeb.TRON_NILE_NET

    Column(modifier = modifier.fillMaxSize()) {
        // Network Selector (Segmented Control equivalent)
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            SegmentedButton(
                selected = selectedNetwork == 0,
                onClick = { selectedNetwork = 0 },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) {
                Text("Mainnet")
            }
            SegmentedButton(
                selected = selectedNetwork == 1,
                onClick = { selectedNetwork = 1 },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) {
                Text("Nile Testnet")
            }
        }

        // Functions List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            sections.forEach { section ->
                item {
                    SectionHeader(title = section.title)
                }
                items(section.items) { item ->
                    FunctionRow(item = item, onClick = { onItemClick(item, currentNode) })
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = Color.LightGray
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.Gray,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun FunctionRow(item: FunctionItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = item.title,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.LightGray
        )
    }
}
