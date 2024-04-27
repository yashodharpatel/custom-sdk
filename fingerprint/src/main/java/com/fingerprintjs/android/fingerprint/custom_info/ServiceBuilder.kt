package com.fingerprintjs.android.fingerprint.custom_info

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

public object ServiceBuilder {
    private val client = OkHttpClient.Builder().build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://test-api-e55b.onrender.com") // change this IP for testing by your actual machine IP
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    public fun<T> buildService(service: Class<T>): T{
        return retrofit.create(service)
    }
}
