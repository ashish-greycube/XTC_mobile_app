package com.xtc_gelato.server_api_calls

import com.xtc_gelato.constent_classes.AppConstants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object API {

    private var retrofitMain: Retrofit? = null

    fun getAPI(): API_Server {

        val httpClient: OkHttpClient = OkHttpClient.Builder()
            .readTimeout(3, TimeUnit.MINUTES)
            .connectTimeout(3, TimeUnit.MINUTES)
            .cookieJar(MemoryCookieJar())
            .addNetworkInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            .build()

        retrofitMain = Retrofit.Builder()
            .baseUrl(AppConstants.MainURL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofitMain!!.create(API_Server::class.java)

    }



}