@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.rezaiyan.kickoff

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.github.rezaiyan.kickoff.contract.Kickoff.Candidate
import com.github.rezaiyan.kickoff.ui.theme.KickoffTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.math.BigInteger

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KickoffTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    val viewModel: VoteViewModel = hiltViewModel()
                    val uiSate = viewModel.uiState.collectAsState()

                    LaunchedEffect(Unit) {
                        lifecycleScope.launch {
                            repeatOnLifecycle(Lifecycle.State.STARTED) {
                                viewModel.uiEvent.collect { event ->
                                    handleUiEvent(event)
                                }
                            }
                        }
                    }

                    VoteScreen(
                        modifier = Modifier.padding(innerPadding),
                        state = uiSate.value,
                        onConnect = {
                            viewModel.onWalletConnect()
                        },
                        onCandidateSelected = viewModel::onCastVote
                    )
                }
            }
        }
    }

    private fun handleUiEvent(event: UiEvent) {
        when (event) {
            is UiEvent.ShowToast -> {
                Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show()

            }
        }
    }
}

@Composable
fun VoteScreen(
    modifier: Modifier = Modifier, state: VoteUiState,
    onConnect: () -> Unit,
    onCandidateSelected: (Int) -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (state) {
            VoteUiState.Loading -> {
                LoadingScreen()
            }

            VoteUiState.MetamaskConnect -> {
                ConnectWalletScreen(onConnect = onConnect)
            }

            is VoteUiState.CandidateList -> {
                CandidateListScreen(
                    wallet = state.walletName,
                    candidates = state.candidates,
                    onCandidateSelected = onCandidateSelected
                )
            }
        }
    }
}

@Composable
fun LoadingScreen(message: String = "Loading...") {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(50.dp),
                strokeWidth = 4.dp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray)
            )
        }
    }
}

@Composable
fun ErrorScreen(
    errorMessage: String = "Something went wrong",
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.Red),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onRetry) {
                Text(text = "Retry")
            }
        }
    }
}


@Composable
fun ConnectWalletScreen(onConnect: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Connect Your Wallet",
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { onConnect() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Connect with MetaMask")
        }
    }
}

@Composable
fun CandidateListScreen(
    wallet: String,
    candidates: List<Candidate>,
    onCandidateSelected: (Int) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wallet is connected ($wallet)") },
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(candidates) { index, candidate ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCandidateSelected(index) },
                ) {
                    Row {
                        Text(
                            text = candidate.name,
                            modifier = Modifier
                                .weight(1F)
                                .padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${candidate.votes}",
                            modifier = Modifier
                                .padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview(@PreviewParameter(ParamsProvider::class) state: VoteUiState) {
    KickoffTheme {
        VoteScreen(
            state = state,
            onConnect = {},
            onCandidateSelected = { candidate ->
            },
        )
    }
}

class ParamsProvider : PreviewParameterProvider<VoteUiState> {

    override val values: Sequence<VoteUiState> = sequenceOf(
        VoteUiState.Loading,
        VoteUiState.MetamaskConnect,
        VoteUiState.CandidateList(
            walletName = "Main Wallet",
            candidates = listOf(
                Candidate("Ronaldo", BigInteger.ZERO),
                Candidate("Messi", BigInteger.ZERO),
                Candidate("Neymar JR", BigInteger.ZERO),
            )
        ),
    )

}