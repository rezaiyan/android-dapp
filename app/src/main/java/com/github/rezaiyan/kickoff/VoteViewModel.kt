package com.github.rezaiyan.kickoff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.rezaiyan.kickoff.contract.Kickoff
import com.github.rezaiyan.kickoff.contract.Kickoff.Candidate
import com.github.rezaiyan.kickoff.contract.contractAddress
import com.github.rezaiyan.kickoff.contract.gasLimit
import com.github.rezaiyan.kickoff.contract.gasPrice
import dagger.hilt.android.lifecycle.HiltViewModel
import io.metamask.androidsdk.Ethereum
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.tx.ClientTransactionManager
import org.web3j.tx.gas.StaticGasProvider
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@HiltViewModel
class VoteViewModel @Inject constructor(private val ethereum: Ethereum) : ViewModel() {

    private var contract: Kickoff? = null

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent

    private val _uiState = MutableStateFlow<VoteUiState>(VoteUiState.MetamaskConnect)
    val uiState: StateFlow<VoteUiState> = _uiState

    fun onWalletConnect() {
        launch {
            ethereum.connect {
                val address = ethereum.selectedAddress
                if (address.isNotBlank()) {
                    createContract(address)
                    contract?.let { getCandidates(it) }
                        ?: emitToast("Failed to connect to the smart contract!")
                } else {
                    emitToast("Failed to connect to Metamask wallet!")
                }
            }
        }
    }

    fun onCastVote(candidateIndex: Int) {
        launch(Dispatchers.IO) {
            contract?.let {
                try {
                    val transaction = it.vote(candidateIndex.toBigInteger()).send()
                    if (transaction.isStatusOK) {
                        getCandidates(it)
                        emitToast("You have casted your vote successfully!")
                    } else {
                        emitToast("Casting vote was not successful!")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    emitToast("Failed to cast vote!")
                }
            } ?: emitToast("Failed to connect to the smart contract!")
        }
    }

    private fun createContract(address: String) {
        if (contract == null) {
            val httpService = HttpService("https://sepolia.infura.io/v3/${BuildConfig.INFURA_KEY}")
            val web3j = Web3j.build(httpService)
            contract = Kickoff.load(
                contractAddress,
                web3j,
                ClientTransactionManager(web3j, address),
                StaticGasProvider(gasPrice, gasLimit)
            )
        }
    }

    private fun getCandidates(contract: Kickoff) {
        launch(Dispatchers.IO) {
            try {
                val candidates = contract.candidates.send() as List<Candidate>

                _uiState.update {
                    VoteUiState.CandidateList(
                        walletName = shortenWalletAddress(ethereum.selectedAddress),
                        candidates = candidates
                    )
                }

            } catch (e: Exception) {
                emitToast("Failed to fetch candidates!")
            }
        }
    }

    private fun emitToast(message: String) {
        launch {
            _uiEvent.emit(UiEvent.ShowToast(message))
        }
    }

}

private fun ViewModel.launch(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> Unit,
) {
    viewModelScope.launch(context) {
        block()
    }
}

fun shortenWalletAddress(address: String, prefixLength: Int = 6, suffixLength: Int = 4): String {
    if (address.length <= prefixLength + suffixLength) {
        return address
    }

    val prefix = address.take(prefixLength)
    val suffix = address.takeLast(suffixLength)
    return "$prefix...$suffix"
}

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
}