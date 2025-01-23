package com.github.rezaiyan.kickoff

import com.github.rezaiyan.kickoff.contract.Kickoff.Candidate

sealed class VoteUiState {

    data object Loading : VoteUiState()
    data object MetamaskConnect : VoteUiState()
    data class CandidateList(val walletName: String, val candidates: List<Candidate>) :
        VoteUiState()
}

