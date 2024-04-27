package com.fingerprintjs.android.fingerprint.api

import com.google.gson.annotations.SerializedName

public data class DataModal (
    @SerializedName("drona_android_payload") val payload: String?,
)

public data class ApiResponse (
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: Data?,
)

public data class Data(
    @SerializedName("_id") val id: String?,
    @SerializedName("drona_android_payload") val payload: String?,
    @SerializedName("__v") val v: Int?
)
