package com.streamhub.tv.di

import android.content.Context
import com.streamhub.tv.util.NetworkConnectivityObserver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideNetworkConnectivityObserver(
        @ApplicationContext context: Context
    ): NetworkConnectivityObserver = NetworkConnectivityObserver(context)
}
