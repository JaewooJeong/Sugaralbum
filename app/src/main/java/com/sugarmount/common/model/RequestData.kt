package com.sugarmount.common.model

import com.sugarmount.common.model.MvConfig.MAX_ITEMS_PER_REQUEST
import com.sugarmount.common.model.MvConfig.REQUEST_TYPE
import lombok.Data
import okhttp3.Callback
import java.util.HashMap

@Data
class RequestData {

    var reqCall: String = ""
    var reqTag: String = ""
    var reqParam: HashMap<String, Any>
    var reqHeader: HashMap<String, String>
    var cbm: Callback? = null
    var reqType: REQUEST_TYPE
    var param = false
    var count = 0
    var page = 0

    init {
        this.reqCall = ""
        this.reqTag = ""
        this.reqParam = HashMap()
        this.reqHeader = HashMap()
        this.cbm = null
        this.reqType = REQUEST_TYPE.POST
        this.param = true
        this.count = MAX_ITEMS_PER_REQUEST
        this.page = 0
    }

}