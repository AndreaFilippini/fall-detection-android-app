package com.example.falldetection

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class MessageRequest(val message: String)
data class MessageResponse(val status: String, val message: String)

interface ApiService {
    @POST("/message")
    fun sendMessage(@Body request: MessageRequest): Call<MessageResponse>
}