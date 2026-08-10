package com.streamhub.tv.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The activation gate shown once on first launch. The valid code is NOT hardcoded in
 * the app - it is read from a small `activation.json` file that lives right next to
 * `channels.json` in the same GitHub repository (same folder), so you can change the
 * code at any time without an app update. Example file:
 * ```json
 * { "code": "1001" }
 * ```
 */
@Serializable
data class ActivationConfig(
    @SerialName("code") val code: String
)
