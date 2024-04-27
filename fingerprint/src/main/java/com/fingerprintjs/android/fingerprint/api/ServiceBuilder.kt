package com.fingerprintjs.android.fingerprint.api

import com.fingerprintjs.android.fingerprint.BaseUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

public object ServiceBuilder {
    private val client = OkHttpClient.Builder().build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BaseUrl.BASE_URL) // change this IP for testing by your actual machine IP
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    public fun<T> buildService(service: Class<T>): T{
        return retrofit.create(service)
    }
}
