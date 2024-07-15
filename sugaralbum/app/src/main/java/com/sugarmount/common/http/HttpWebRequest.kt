package com.sugarmount.common.http

import com.sugarmount.common.env.MvConfig
import com.sugarmount.common.env.MvConfig.REQUEST_TYPE
import com.sugarmount.common.model.RequestData
import com.sugarmount.common.utils.log
import org.json.JSONException
import org.json.JSONObject

/**
 * 입력 파라메터, 해더, url에 전송.
 * Async request callback. : regCall
 * @author Jaewoo
 * @Date 2020-04-28
 */
class HttpWebRequest {
    fun httpRequest(item: RequestData) {
        // request
        val http = TransportConnector(
            MvConfig.RELEASE_HOST + item.reqCall,
            item.reqType,
            item.cbm!!,
            item.reqTag
        )
        try {
            if (item.param) {
                if (item.reqParam.isNotEmpty()) {
                    if (item.reqType == REQUEST_TYPE.GET)
                        http.addQueryParam(item.reqParam)
                    else
                        http.addParams(item.reqParam)
                } else {
                    http.addParam(JSONObject().put(item.reqCall, JSONObject()).toString())
                }
            }
            http.addHeaders(item.reqHeader)
            http.request()
        } catch (e: JSONException) {
            log.d("#2:" + e.message)
            e.printStackTrace()
        }
    }
}