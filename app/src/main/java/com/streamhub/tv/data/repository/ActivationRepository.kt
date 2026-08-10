package com.streamhub.tv.data.repository

import com.streamhub.tv.data.local.PreferencesManager
import com.streamhub.tv.data.remote.ChannelApiService
import com.streamhub.tv.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles the one-time "activation code" gate shown on first launch.
 *
 * The valid code is never hardcoded in the app. It is read from a small
 * `activation.json` file that must sit right next to `channels.json`, in the SAME
 * GitHub folder the user configured in Settings -> Repository Configuration. For
 * example, if channels.json is at:
 *   https://raw.githubusercontent.com/USER/REPO/main/channels.json
 * then the app looks for the code at:
 *   https://raw.githubusercontent.com/USER/REPO/main/activation.json
 * containing:
 *   { "code": "1001" }
 *
 * This means changing the activation code any time only requires editing that file -
 * no app update needed, exactly like the channel list itself.
 */
@Singleton
class ActivationRepository @Inject constructor(
    private val api: ChannelApiService,
    private val preferencesManager: PreferencesManager
) {
    val isActivated: Flow<Boolean> = preferencesManager.isActivated

    /** Builds the activation.json URL by swapping the filename on the configured channels URL. */
    private suspend fun activationUrl(): String {
        val channelsUrl = preferencesManager.repoUrl.first()
        val folder = channelsUrl.substringBeforeLast('/', missingDelimiterValue = channelsUrl)
        return "$folder/activation.json"
    }

    /** Downloads activation.json and compares the entered code against it. */
    suspend fun verifyCode(enteredCode: String): Resource<Boolean> {
        return try {
            val response = api.getActivationConfig(activationUrl())
            if (response.isSuccessful && response.body() != null) {
                val correct = response.body()!!.code.trim() == enteredCode.trim()
                if (correct) preferencesManager.setActivated(true)
                Resource.Success(correct)
            } else {
                Resource.Error("Couldn't reach the activation server (code ${response.code()})")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error while verifying the code")
        }
    }

    suspend fun resetActivation() = preferencesManager.setActivated(false)
}
