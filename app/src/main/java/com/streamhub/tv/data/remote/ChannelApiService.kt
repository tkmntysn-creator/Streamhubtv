package com.streamhub.tv.data.remote

import com.streamhub.tv.data.model.ChannelsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

/**
 * Downloads the channel list JSON from whatever GitHub raw URL is currently configured
 * by the user (see RepositoryConfigScreen). The URL is dynamic, so we use @Url instead
 * of a fixed @GET path - this lets users repoint the app at their own fork/mirror of
 * channels.json without any code change.
 */
interface ChannelApiService {
    @GET
    suspend fun getChannels(@Url url: String): Response<ChannelsResponse>
}
