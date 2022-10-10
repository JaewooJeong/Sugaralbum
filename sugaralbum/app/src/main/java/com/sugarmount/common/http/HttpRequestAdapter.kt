package com.sugarmount.common.http

import android.app.Activity
import android.widget.Toast
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.sugarmount.common.model.HttpKeyValue
import com.sugarmount.common.model.MvConfig.REQUEST_CALLBACK
import com.sugarmount.common.model.MvConfig.REQUEST_TYPE
import com.sugarmount.common.model.RequestData
import com.sugarmount.common.model.ResponseData
import com.sugarmount.common.utils.JsonUtil
import com.sugarmount.common.utils.log
import com.sugarmount.sugaralbum.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*

class HttpRequestAdapter(private val activity: Activity, val fireListener: (result:Int, item: Any?, type:REQUEST_CALLBACK?) -> Unit) {
    private var requestData = RequestData()
    private val reqData: HashMap<String, Any> = HashMap()
    private val reqHeader: HashMap<String, String> = HashMap()
    private var running = false

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Request functions
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    fun build(
        request_callback: REQUEST_CALLBACK,
        request_type: REQUEST_TYPE
    ) {
        requestData.reqCall = REQUEST_CALLBACK.valueOfStr(request_callback)
        requestData.reqTag = request_callback.name
        requestData.reqType = request_type
        if (request_type != REQUEST_TYPE.DELETE) {
            requestData.reqParam = reqData
            requestData.reqHeader = reqHeader
        }
        start()
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Common data set
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    fun putParam(key: String, value: Any) {
        reqData[key] = value
    }

    fun putHeader(key: String, value: String) {
        reqHeader[key] = value
    }

    fun init() {
        requestData = RequestData()
        reqData.clear()
        reqHeader.clear()
        running = true
    }

    fun start() {
        activity.runOnUiThread {
            running = true
            val http = HttpWebRequest()
            requestData.cbm = callBackMessage
            http.httpRequest(requestData)
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Callback response
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private var callBackMessage: Callback = object : Callback {
        var msg: String? = null
        override fun onFailure(call: Call, e: IOException) {
            msg = e.localizedMessage
            log.e("########### onFailure:$msg")
            activity.runOnUiThread {
                Toast.makeText(
                    activity,
                    activity.getString(R.string.string_common_error_handler_network)
                        .replace("##code", "APP01"),
                    Toast.LENGTH_LONG
                ).show()
                fireListener(Activity.RESULT_CANCELED, null, null)
            }
        }

        fun runFailure(code: Int, msg: String?) {
            runFailure(code, Exception(msg))
        }

        fun runFailure(code: Int, e: Exception) {
            activity.runOnUiThread {
                e.printStackTrace()
                log.e(String.format("#runFailure [%d]-[%s]", code, e.message))
                Toast.makeText(
                    activity,
                    activity.getString(R.string.string_common_error_handler_network)
                        .replace("##code", code.toString()),
                    Toast.LENGTH_LONG
                ).show()
                fireListener(Activity.RESULT_CANCELED, null, null)
            }
        }

        fun runCheckSum(method: String?, jsonObject: JSONObject) {
            activity.runOnUiThread {
                try {
                    if (jsonObject.getString(HttpKeyValue.STATUS) == HttpKeyValue.OK) {
                        if (!jsonObject.isNull(HttpKeyValue.ID)) {
                            val str = arrayOfNulls<String>(2)
                            str[0] = method
                            str[1] = jsonObject.getString(HttpKeyValue.ID)
                            fireListener(Activity.RESULT_OK, str, null)
                        } else {
                            fireListener(Activity.RESULT_OK, true, null)
                        }
                    } else {
                        runFailure(3001, "Fail")
                    }
                } catch (e: Exception) {
                    runFailure(3000, e)
                }
            }
        }

        fun runJsonToObject(reqCallBack: REQUEST_CALLBACK, jsonObject: JSONObject) {
            activity.runOnUiThread {
                try {
                    val parser = JsonParser()
                    var rootObject: JsonElement? = null
                    when(reqCallBack) {
                        REQUEST_CALLBACK.NAVER -> {
                            val g = GsonBuilder().create()
                            rootObject = parser.parse(jsonObject.toString())
                            val value: ResponseData = g.fromJson(rootObject, ResponseData::class.java)
                            
                            // 변환
                            value.message.result.setHtml()

                            fireListener(Activity.RESULT_OK, value, reqCallBack)
                            log.e("log")
                        }
                        REQUEST_CALLBACK.DAUM -> {
                            val g = GsonBuilder().create()
                            rootObject = parser.parse(jsonObject.toString())
                            val value = g.fromJson(rootObject, ResponseData::class.java)

                            fireListener(Activity.RESULT_OK, value, reqCallBack)
                            log.e("log")
                        }
                        else -> {

                        }

                    }

                } catch (e: Exception) {
                    log.e(e.message)
                }
            }
        }

        override fun onResponse(call: Call, response: Response) {
            val tag = call.request().tag().toString()
            try {
                log.d(String.format(
                        "tag:%s, url:%s, code:%d, msg:%s",
                        tag,
                        response.request().url().toString(),
                        response.code(),
                        response.message()
                    )
                )
                if (response.code() == 200) {
                    val method = response.request().method()
                    var jsonStr = response.body()!!.string()
                    if(jsonStr.substring(0) != "{"){
                        val regex = "(\\{.+)\\}".toRegex()
                        regex.find(jsonStr).let {
                            if(it != null)
                                jsonStr = it.value
                            else
                                throw Exception("Cannot pars json data. Please contact admin.")
                        }
                    }
                    val json = JsonUtil.getJSONObjectFrom(jsonStr)

                    runJsonToObject(REQUEST_CALLBACK.valueOf(tag), json)
                } else {
                    runFailure(response.code(), response.message())
                    if (response.code() == 401 || response.code() == 412) {
                        //GlobalApplication.setAccess_token("");
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runFailure(response.code(), response.message())

                } catch (e1: JSONException) {
                    e1.printStackTrace()
                }
            }
        }

}