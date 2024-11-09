@file:Suppress("UNCHECKED_CAST")

package com.iffelse.iastro.utils

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.iffelse.iastro.BuildConfig
import com.iffelse.iastro.model.BaseErrorModel
import com.iffelse.iastro.model.ErrorBody
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.Headers.Companion.toHeaders
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.io.IOException
import java.lang.reflect.Type
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit


object OkHttpNetworkProvider {

    interface NetworkListener<T> {
        fun onResponse(response: T?)
        fun onError(error: BaseErrorModel?)
    }

    private val clientBuilder = OkHttpClient.Builder()

    // Create an instance of HttpLoggingInterceptor
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // Set log level based on whether the app is in debug or release mode
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY // Detailed logging in debug mode
        } else {
            HttpLoggingInterceptor.Level.NONE // No logging in release mode
        }
    }

    // Create OkHttpClient with the logging interceptor
    private val client = clientBuilder
        .addInterceptor(loggingInterceptor)
        .connectTimeout(5, TimeUnit.MINUTES)
        .readTimeout(5, TimeUnit.MINUTES)
        .writeTimeout(5, TimeUnit.MINUTES)
        .build()

    fun <T> get(
        url: String,
        headerMap: Map<String, String>?,
        queryParamMap: Map<String, String>?,
        tag: String?,
        responseType: Type,
        callback: NetworkListener<T>,
    ) {


        val httpUrlBuilder = url.toHttpUrlOrNull()?.newBuilder()

        queryParamMap?.forEach { (key, value) ->
            httpUrlBuilder?.addQueryParameter(key, value)
        }

        val request = httpUrlBuilder?.build()?.let {
            Request.Builder().apply {
                url(url)
                tag(tag)
                if (headerMap != null) {
                    headers(headerMap.toHeaders())
                }
            }.build()
        }

        if (request != null) {
            client.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        if (responseType == JSONObject::class.java) {
                            callback.onResponse(JSONObject(responseBody!!) as T?)
                        } else {
                            callback.onResponse(parseJson<T>(responseBody, responseType))
                        }
                    } else {
                        callback.onError(
                            buildErrorModel(
                                response.code,
                                response.body!!
                            )
                        )
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    val errorMsg = handleFailureResponse(e)
                    val errorModel = BaseErrorModel(
                        errorBody = null,
                        errorCode = null,
                        message = errorMsg,
                        errorDetail = null
                    )
                    callback.onError(errorModel)
                }
            })
        }
    }

    private fun buildErrorModel(responseCode: Int, body: ResponseBody): BaseErrorModel {
        val gson = GsonBuilder().create()
        var mError: ErrorBody? = null
        try {
            mError = gson.fromJson(body.string(), ErrorBody::class.java)
        } catch (e: java.lang.Exception) {
            if (responseCode >= HttpURLConnection.HTTP_BAD_REQUEST && responseCode < HttpURLConnection.HTTP_INTERNAL_ERROR) {
                mError?.message = ErrorConstants.BAD_REQUEST_ERROR
            } else if (responseCode >= HttpURLConnection.HTTP_INTERNAL_ERROR && responseCode < HttpURLConnection.HTTP_INTERNAL_ERROR + 100) {
                mError?.message = ErrorConstants.SERVER_ERROR
            } else {
                mError?.message = ErrorConstants.UNKNOWN_ERROR
            }
        }
        return BaseErrorModel(mError, responseCode, null, null)
    }

    private fun handleFailureResponse(throwable: Throwable): String {
        return if (throwable is IOException) {
            ErrorConstants.SOCKET_ERROR
        } else {
            ErrorConstants.UNKNOWN_ERROR
        }
    }


    interface ErrorConstants {
        companion object {
            const val SERVER_ERROR = "Unable to process your request. \nPlease try again later."
            const val BAD_REQUEST_ERROR =
                "Unable to process your request. \nPlease try again later."
            const val UNKNOWN_ERROR = "Unable to process your request. \nPlease try again later."
            const val SOCKET_ERROR = "Unable to connect with the server.\nPlease try again later."
        }
    }


    fun <T> parseJson(json: String?, type: Type): T? {
        return try {
            Gson().fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }

    fun <T> post(
        url: String,
        jsonObjectBody: JSONObject?,
        headerMap: Map<String, String>,
        okHttpClient: OkHttpClient?,
        tag: Any? = null,
        responseType: Type,
        callback: NetworkListener<T>,
    ) {
        val clientBuilder: OkHttpClient.Builder =
            okHttpClient?.newBuilder() ?: this.clientBuilder

        if (okHttpClient != null) {
            clientBuilder.cookieJar(okHttpClient.cookieJar)
        }

        val client = clientBuilder
            .addInterceptor(loggingInterceptor)
            .connectTimeout(5, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .build()

        // Check if Content-Type is set to x-www-form-urlencoded

        // Create the request body based on the content type
        val requestBody = when (val contentType = headerMap["Content-Type"] ?: "application/json") {
            "application/x-www-form-urlencoded" -> {
                // Convert JSONObject into form-urlencoded format
                jsonObjectBody?.let { json ->
                    val formBodyBuilder = FormBody.Builder()
                    json.keys().forEach {
                        formBodyBuilder.add(it, json.getString(it))
                    }
                    formBodyBuilder.build()
                }
            }
            "application/json" -> {
                // Convert JSONObject to string and then to RequestBody
                jsonObjectBody?.toString()?.toRequestBody(contentType.toMediaTypeOrNull())
            }
            else -> null
        }

        val request = requestBody?.let {
            Request.Builder()
                .url(url)
                .headers(headerMap.toHeaders())
                .post(it)
                .tag(tag)
                .build()
        }

        if (request != null) {
            client.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        if (responseBody.isNullOrEmpty()) {
                            // Handle the empty or null response appropriately
                            Log.e("API Error", "Received empty or null response")
                            callback.onResponse(null) // Call the callback with null or an error response
                        } else {
                            if (responseType == JSONObject::class.java) {
                                callback.onResponse(JSONObject(responseBody) as T?) // Safe cast to T
                            } else {
                                callback.onResponse(parseJson<T>(responseBody, responseType)) // Handle other types
                            }
                        }
                    } else {
                        callback.onError(
                            buildErrorModel(
                                response.code,
                                response.body!!
                            )
                        )
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    val errorMsg = handleFailureResponse(e)
                    val errorModel = BaseErrorModel(
                        errorBody = null,
                        errorCode = null,
                        message = errorMsg,
                        errorDetail = null
                    )
                    callback.onError(errorModel)
                }
            })
        }
    }
}
