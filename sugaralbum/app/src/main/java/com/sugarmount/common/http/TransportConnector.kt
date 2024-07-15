package com.sugarmount.common.http

import com.sugarmount.common.env.MvConfig
import com.sugarmount.common.env.MvConfig.REQUEST_TYPE
import com.sugarmount.common.utils.log
import okhttp3.*
import org.json.JSONObject
import java.net.URLEncoder
import java.util.HashMap
import java.util.concurrent.TimeUnit

/**
 * 전송 모듈
 * Async request callback. : regCall
 * @author Jaewoo
 * @Date 2020-04-28
 */
class TransportConnector(
    private var url: String,
    private var reqType: REQUEST_TYPE,
    private var callback: Callback,
    private var tag: String
) {
    private lateinit var client: OkHttpClient
    private lateinit var body: RequestBody
    private lateinit var headers: Headers

    private val jsonMediaType =
        MediaType.parse("application/json; charset=utf-8")

    fun addParams(params: Map<String, Any>) {
        val json = JSONObject()
        for ((key, value) in params) {
            try {
                log.d("Key:$key, Value:$value")
                json.put(key, value)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        body = RequestBody.create(jsonMediaType, json.toString())
    }

    fun addQueryParam(params: Map<String, Any>) {
        val sb = StringBuffer(url).append("?")
        for ((key, value) in params) {
            sb.append(key).append("=").append(URLEncoder.encode(value.toString(), "utf-8")).append("&")
        }
        url = sb.toString()
    }

    fun addParam(json: String) {
        body = RequestBody.create(jsonMediaType, json)
    }


    fun getParam(): RequestBody {
        return body
    }


    fun addHeaders(headers: HashMap<String, String>) {
        headers["Content-Type"] = jsonMediaType.toString()
        this.headers = Headers.of(headers)
    }

    /**
     * Request
     * http request - GET, POST
     * media type - JSON, TEXT, Form-data
     */
    fun request(): Boolean {
        client = OkHttpClient.Builder()
            .connectTimeout(
                MvConfig.CONNECT_TIMEOUT.toLong(),
                TimeUnit.SECONDS
            )
            .writeTimeout(MvConfig.WRITE_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .readTimeout(MvConfig.READ_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .build()
        try {
            client.newCall(
                when (reqType) {
                    REQUEST_TYPE.GET -> {
                        Request.Builder()
                            .headers(headers)
                            .url(url)
                            .tag(tag)
                            .build()
                    }
                    REQUEST_TYPE.POST -> {
                        Request.Builder()
                            .headers(headers)
                            .url(url)
                            .post(getParam())
                            .tag(tag)
                            .build()
                    }
                    REQUEST_TYPE.PUT -> {
                        Request.Builder()
                            .headers(headers)
                            .url(url)
                            .put(getParam())
                            .tag(tag)
                            .build()
                    }
                    REQUEST_TYPE.DELETE -> {
                        Request.Builder()
                            .headers(headers)
                            .url(url)
                            .delete(getParam())
                            .tag(tag)
                            .build()
                    }
                }
            ).enqueue(callback)

            return true
            // Do something with the response.
        } catch (e: Exception) {
            log.e(e.message)
        }
        return false
    }
}