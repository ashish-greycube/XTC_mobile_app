package com.xtc_gelato.server_api_calls

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

@JvmSuppressWildcards
interface API_Server {

    @FormUrlEncoded
    @POST("api/method/login")
    fun userLogin(
        @Field("usr") username_email: String,
        @Field("pwd") password: String,
    ): Call<ResponseBody>

    @POST("api/method/wms_mobile.update_serial_no")
    fun updateSerialNo(@Body body: Map<String, Any>): Call<ResponseBody>

    @POST("api/method/xtc_mobile_api.create_dn_based_on_picked_details")
    fun uploadPickupDetails(
        @Header("Cookie") cookie: String?,
        @Body body: Map<String, Any>
    ): Call<ResponseBody>

    @GET
    fun commonGetApiCall(
        @Url url: String,
    ): Call<ResponseBody>


    @GET
    fun getHelp(
        @Url url: String,
        @Header("Cookie") cookie: String?,
    ): Call<ResponseBody>

}