package com.sugarmount.sugaralbum

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.sugarmount.common.listener.FinishClickEventListener
import com.sugarmount.common.model.HttpKeyValue.*
import com.sugarmount.common.env.MvConfig
import com.sugarmount.common.env.MvConfig.EXTRA_PERMISSION
import com.sugarmount.common.env.MvConfig.PERMISSIONS
import com.sugarmount.common.env.MvConfig.PERMISSIONS33
import com.sugarmount.common.env.MvConfig.POPUP_TYPE
import com.sugarmount.common.room.AnyRepository
import com.sugarmount.common.room.info.InfoT
import com.sugarmount.common.room.version.VersionT
import com.sugarmount.common.utils.*
import com.sugarmount.common.view.PopupDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.math.BigInteger

class ActivityEmpty : CustomAppCompatActivity(), FinishClickEventListener {
    private var context: Context? = null
    private lateinit var remoteConfig: FirebaseRemoteConfig
    private var popupDialog: PopupDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_empty)
        setInsetView(findViewById(R.id.relativeLayout))

        context = this

        GlobalApplication.setNavigationColor(
            window, ContextCompat.getColor(
                applicationContext,
                R.color.black
            )
        )

        // 버전확인
        runFirebaseConfig()

        val tv = TypedValue()
        if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metrics)
            GlobalApplication.setPoint(Point(metrics.widthPixels, metrics.heightPixels))
        }
    }

    private fun checkMyPermission() {
        val readImagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) PERMISSIONS33 else PERMISSIONS
        when (grantPermission(readImagePermission)) {
            true -> {
                goIntent(false)
            }
            false -> {
                val thread = Thread {
                    runOnUiThread {
                        popupDialog = PopupDialog(context)
                        popupDialog?.setOnFinishClickEvent(this)
                        popupDialog?.showDialog(POPUP_TYPE.PERMISSION, false)
                    }
                }
                thread.start()
            }
        }
    }

    override fun onFinishEvent(b: Boolean) {
        when(popupDialog?.popupType){
            POPUP_TYPE.PERMISSION -> {
                when (b) {
                    true -> {
                        goIntent(true)
                    }
                    false -> finish()
                }
            }
            POPUP_TYPE.VERSION -> {
                when (b) {
                    true -> finish()
                    false -> {
                        val i = Intent(Intent.ACTION_VIEW)
                        val u =
                            Uri.parse("https://play.google.com/store/apps/details?id=com.sugarmount.sugaralbum")
                        i.data = u
                        startActivity(i)
                        finish()
                    }
                }
            }
            else -> {}
        }
    }

    private fun goIntent(b: Boolean) {
        val intent = Intent(applicationContext, ActivityIntro::class.java)
        intent.putExtra(EXTRA_PERMISSION, b)
        startActivity(intent)
        finish()
    }

    @Suppress("DEPRECATION")
    private fun runFirebaseConfig() {
        FirebaseApp.initializeApp(this)

        // Get Remote Config instance.
        // [START get_remote_config_instance]
        remoteConfig = Firebase.remoteConfig
        // [END get_remote_config_instance]

        // Create a Remote Config Setting to enable developer mode, which you can use to increase
        // the number of fetches available per hour during development. Also use Remote Config
        // Setting to set the minimum fetch interval.
        // [START enable_dev_mode]
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        // [END enable_dev_mode]

        // Set default Remote Config parameter values. An app uses the in-app default values, and
        // when you need to adjust those defaults, you set an updated value for only the values you
        // want to change in the Firebase console. See Best Practices in the README for more
        // information.
        // [START set_default_values]
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        // [END set_default_values]

        // [START fetch_config_with_callback]
        remoteConfig.fetchAndActivate().addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                log.e("Config params updated: $task.result")
                var goMarket = false
                val appVer: String
                var updateVer: String
                val infoVer: String
                var force:Boolean

                try {
                    val versionObject = remoteConfig.getString(LOADING_VERSION_KEY)
                    var json = JsonUtil.getJSONObjectFrom(versionObject)
                    if(versionObject.isNotEmpty()) {
                        val pInfo = packageManager.getPackageInfo(packageName, 0)
                        appVer = pInfo.versionName.toString()
                        updateVer = json.getString("app_version")
                        infoVer = json.getString("info_version")
                        force = json.getBoolean("force")

                        //xx debug
//                        updateVer = "1.0.019"
//                        force = true

                        // 1. 앱 버전 확인
                        if (appVer == updateVer) {
                            goMarket = false
                            force = false
                        } else {
                            val partRegex = Regex("""^(\d+)""")

                            fun parseParts(v: String): List<BigInteger> =
                                v.split(".")
                                    .map { partRegex.find(it)?.groupValues?.get(1) ?: "0" } // 숫자만 취득
                                    .map { it.trimStart('0').ifEmpty { "0" } }              // 001 -> 1
                                    .map { BigInteger(it) }

                            fun compareVersions(a: String, b: String): Int {
                                val aa = parseParts(a)
                                val bb = parseParts(b)
                                val len = maxOf(aa.size, bb.size)
                                for (i in 0 until len) {
                                    val va = aa.getOrNull(i) ?: BigInteger.ZERO
                                    val vb = bb.getOrNull(i) ?: BigInteger.ZERO
                                    val cmp = va.compareTo(vb)
                                    if (cmp != 0) return cmp   // a<b -> -1, a>b -> 1
                                }
                                return 0                       // 완전히 동일
                            }


                            val arrayAppVer =
                                appVer.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                                    .toTypedArray()
                            val arrayUpdateVer =
                                updateVer.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                                    .toTypedArray()

                            goMarket = compareVersions(appVer, updateVer) < 0
                        }

                        if (force) {
                            goMarket = true
                        } else {
                            val repo = AnyRepository(application)

                            // room data access
                            CoroutineScope(Dispatchers.IO).launch {
                                val res = repo.getVersion()
                                var insert = false
                                if (res.isNotEmpty()) {
                                    if (res[0].infoVersion > infoVer) {
                                        insert = true
                                    }
                                } else {
                                    // 최초실행 혹은 앱 데이터 삭제.
                                    // version 및 contents 데이터 db 생성.
                                    repo.insert(VersionT(infoVer))
                                    insert = true
                                }

                                // 데이터 저장
                                if (insert) {
                                    val contentsObject = remoteConfig.getString(LOADING_CONTENTS_KEY)
                                    if(contentsObject.isNotEmpty()) {
                                        for (n in 1..2) {
                                            json = JsonUtil.getJSONObjectFrom(contentsObject)
                                            var infoType: MvConfig.INFO_TYPE
                                            var type = JSONObject()
                                            when (n) {
                                                1 -> {
                                                    type = json.getJSONObject(FAQ)
                                                    infoType = MvConfig.INFO_TYPE.FAQ
                                                }
                                                2 -> {
                                                    type = json.getJSONObject(NOTICE)
                                                    infoType = MvConfig.INFO_TYPE.NOTICE
                                                }
                                                else -> {
                                                    infoType = MvConfig.INFO_TYPE.NOTICE
                                                }
                                            }

                                            val data = type.getJSONArray(DATA)

                                            for (i in 0 until data.length()) {
                                                val item = data.getJSONArray(i)
                                                when (item.length()) {
                                                    3, 4 -> {
                                                        // Faq, Notice
                                                        repo.insert(
                                                            InfoT(
                                                                item.get(0).toString(),
                                                                infoType.toString(),
                                                                item.get(1).toString(),
                                                                item.get(2).toString(),
                                                                item.get(3).toString(),
                                                                ""
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // 라이선스 불러오기
                                    LicenseCreate.getLicense()
                                }
                            }
                        }
                    }
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                    // if have exception play the application.
                    goMarket = false
                } catch (e: JSONException) {
                    e.printStackTrace()
                    goMarket = false
                }

                if (goMarket) {
                    popupDialog = PopupDialog(context)
                    popupDialog?.setOnFinishClickEvent(this)
                    popupDialog?.showDialog(POPUP_TYPE.VERSION, false)
                } else {
                    // 권한요청
                    checkMyPermission()
                }
            } else {
                log.e("Fetch failed.")

                // 권한요청
                checkMyPermission()
            }
        }
        // [END fetch_config_with_callback]
    }

    companion object {
        const val LOADING_CONTENTS_KEY  = "contents"
        const val LOADING_VERSION_KEY   = "version"
    }
}
