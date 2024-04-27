package com.fingerprintjs.android.fingerprint.custom_info

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

public class RestApiService {
    public fun addPayload(userData: DataModal, onResult: (ApiResponse?) -> Unit){
        val retrofit = ServiceBuilder.buildService(RestApi::class.java)
        retrofit.addPayload(userData).enqueue(
            object : Callback<ApiResponse> {
                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    onResult(null)
                }
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    val addedUser = response.body()
                    Log.i("onResponse",response.toString())
                    onResult(addedUser)
                }
            }
        )
    }
}
