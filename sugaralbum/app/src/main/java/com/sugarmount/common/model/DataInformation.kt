package com.sugarmount.common.model

import com.sugarmount.common.model.MvConfig.INFO_TYPE

class DataInformation {
    var id: Int? = null
    var infoType: INFO_TYPE? = null
    var subject: String? = null
    var content: String? = null
    var updatedAt: String? = null
    var url: String? = null

    constructor() {
        id = 0
        infoType = INFO_TYPE.NOTICE
        subject = ""
        content = ""
        updatedAt = ""
        url = ""
    }
}