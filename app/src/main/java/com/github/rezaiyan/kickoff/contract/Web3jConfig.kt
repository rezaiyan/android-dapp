package com.github.rezaiyan.kickoff.contract


import com.github.rezaiyan.kickoff.BuildConfig
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.tx.gas.StaticGasProvider
import java.math.BigInteger

private const val privateKey = BuildConfig.PRIVATE_KEY
const val contractAddress = BuildConfig.CONTRACT_ADDRESS
val gasPrice = BigInteger.ZERO
val gasLimit = BigInteger.valueOf(16000000)

val ownerContract: Kickoff by lazy {
    val credentials = Credentials.create(privateKey)
    val web3j: Web3j =
        Web3j.build(HttpService("https://sepolia.infura.io/v3/${BuildConfig.INFURA_KEY}"))
    val gasProvider = StaticGasProvider(gasPrice, gasLimit)
    Kickoff.load(contractAddress, web3j, credentials, gasProvider)
}