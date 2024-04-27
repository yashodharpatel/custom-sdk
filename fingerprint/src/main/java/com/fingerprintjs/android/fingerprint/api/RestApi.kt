package com.fingerprintjs.android.fingerprint.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

public interface RestApi {
    @Headers("Content-Type: application/json")
    @POST("send-details")
    public fun addPayload(@Body userData: DataModal): Call<ApiResponse>
}
