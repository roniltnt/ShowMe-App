package com.example.showme.interfaces

import com.example.showme.data.YouTubeResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

// retrofit make network requests easier

interface YouTubeApi {

    @GET("youtube/v3/search")
    fun searchVideos(
        @Query("key") apiKey: String,
        @Query("q") query: String,
        @Query("part") part: String = "snippet",
        @Query("type") type: String = "video,playlist",
        @Query("maxResults") maxResults: Int = 1 // Only first result is needed
    ): Call<YouTubeResponse>
}