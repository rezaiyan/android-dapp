package com.github.rezaiyan.kickoff.di

import android.content.Context
import com.github.rezaiyan.kickoff.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.metamask.androidsdk.DappMetadata
import io.metamask.androidsdk.Ethereum
import io.metamask.androidsdk.SDKOptions
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class VoteModule {

    @Provides
    @Singleton
    fun provideEthereum(@ApplicationContext context: Context): Ethereum {
        return Ethereum(
            context = context,
            dappMetadata = DappMetadata(
                name = context.applicationInfo.name,
                url = "https://${context.applicationInfo.name}.com",
                iconUrl = "https://raw.githubusercontent.com/rezaiyan/MetamaskConnect/refs/heads/main/football-player-15446.png"
            ),
            sdkOptions = SDKOptions(infuraAPIKey = BuildConfig.INFURA_KEY)
        )
    }

}
