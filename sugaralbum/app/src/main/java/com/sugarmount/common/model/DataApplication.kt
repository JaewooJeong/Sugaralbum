package com.sugarmount.common.model

import lombok.Data
import java.io.Serializable

/**
 * Created by Jaewoo on 2019-12-08.
 */
@Data
class DataApplication : Serializable {
    var id: Int = 0
    var appName: String = ""
    var packageName: String = ""
    var activityName: String = ""
    var clipType: MvConfig.CLIP_TYPE = MvConfig.CLIP_TYPE.NONE
}