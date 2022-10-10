package com.sugarmount.common.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.ContextCompat
//import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.sugarmount.common.listener.FinishClickEventListener
import com.sugarmount.common.model.MvConfig
import com.sugarmount.common.model.MvConfig.POPUP_TYPE
import com.sugarmount.common.room.AnyRepository
import com.sugarmount.common.utils.shareSocial
import com.sugarmount.sugaralbum.GlobalApplication
import com.sugarmount.sugaralbum.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class PopupDialog(context: Context?) : View(context), MvConfig, View.OnClickListener {

    private lateinit var dialog: Dialog
    lateinit var popupType: POPUP_TYPE
    private lateinit var mListener: FinishClickEventListener
    lateinit var text: String
//    private lateinit var adLoader: AdLoader
//    private lateinit var template: TemplateView

    fun setOnFinishClickEvent(listener: FinishClickEventListener) {
        mListener = listener
//        adLoader = AdLoader.Builder(context, context.getString(if(MvConfig.debug) R.string.native_ad_unit_id_test else R.string.native_ad_unit_id))
//                .forUnifiedNativeAd { unifiedNativeAd: UnifiedNativeAd? ->
//                    // Show the ad.
//                    template.setNativeAd(unifiedNativeAd)
//                }
//                .withAdListener(object : AdListener() {
//                    override fun onAdFailedToLoad(errorCode: Int) {
//                        // Handle the failure by logging, altering the UI, and so on.
//                    }
//                })
//                .withNativeAdOptions(NativeAdOptions.Builder() // Methods in the NativeAdOptions.Builder class can be
//                        // used here to specify individual options settings.
//                        .build())
//                .build()

    }

    fun showDialog(pt: POPUP_TYPE, isCancelable: Boolean) {
        popupType = pt
        dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(isCancelable)
        dialog.setCanceledOnTouchOutside(isCancelable)
        val param = WindowManager.LayoutParams()
        //팝업 외부 뿌연 효과
        param.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
        //뿌연 효과 정도
        param.dimAmount = 0.8f
        //background transparent
        Objects.requireNonNull(dialog.window)?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        when (pt) {
            POPUP_TYPE.PERMISSION -> {
                dialog.setContentView(R.layout.popup_permission_menu)
                param.width = (GlobalApplication.getPoint().x * 0.93).toInt()
                param.height = (GlobalApplication.getPoint().y * 0.6).toInt()
                permissionDialog()
            }
            POPUP_TYPE.NETWORK -> {
                dialog.setContentView(R.layout.popup_dlg_menu)
                param.width = (GlobalApplication.getPoint().x * 0.75).toInt()
                param.height = ViewGroup.LayoutParams.WRAP_CONTENT
                networkCheckDialog()
            }
            POPUP_TYPE.DELETE -> {
                dialog.setContentView(R.layout.popup_dlg_menu)
                param.width = (GlobalApplication.getPoint().x * 0.75).toInt()
                param.height = ViewGroup.LayoutParams.WRAP_CONTENT
                transparentDialog()
            }
            POPUP_TYPE.VIDEO -> {
                dialog.setContentView(R.layout.popup_dlg_menu)
                param.width = (GlobalApplication.getPoint().x * 0.75).toInt()
                param.height = ViewGroup.LayoutParams.WRAP_CONTENT
                videoCheckDialog()
            }
            POPUP_TYPE.DOWNLOAD -> {
                dialog.setContentView(R.layout.popup_dlg_menu)
                param.width = (GlobalApplication.getPoint().x * 0.75).toInt()
                param.height = ViewGroup.LayoutParams.WRAP_CONTENT
                downloadCheckDialog()
            }
            POPUP_TYPE.VERSION -> {
                dialog.setContentView(R.layout.popup_version_menu)
                param.width = (GlobalApplication.getPoint().x * 0.93).toInt()
                param.height = (GlobalApplication.getPoint().y * 0.6).toInt()
                dialog.setCancelable(false)
                dialog.findViewById<View>(R.id.button1).setOnClickListener(this)
                dialog.findViewById<View>(R.id.button2).setOnClickListener(this)
            }
            POPUP_TYPE.PROGRESS -> {
//                adLoader.loadAd(AdRequest.Builder().build())
                dialog.setContentView(R.layout.popup_progress_menu)
                param.width = (GlobalApplication.getPoint().x * 0.93).toInt()
                param.height = (GlobalApplication.getPoint().y * 0.8).toInt()
            }
            POPUP_TYPE.ADMOB -> {
//                adLoader.loadAd(AdRequest.Builder().build())
//                dialog.setContentView(R.layout.popup_admob_menu)
//                param.width = (GlobalApplication.getPoint().x * 0.93).toInt()
//                param.height = ViewGroup.LayoutParams.WRAP_CONTENT
//                dialog.findViewById<View>(R.id.button1).setOnClickListener(this)
//                dialog.findViewById<View>(R.id.kakaotalk).setOnClickListener(this)
//                dialog.findViewById<View>(R.id.keep).setOnClickListener(this)
//                dialog.findViewById<View>(R.id.onenote).setOnClickListener(this)
//                dialog.findViewById<View>(R.id.evernote).setOnClickListener(this)
//                dialog.findViewById<View>(R.id.more).setOnClickListener(this)
//                // dialog 초기화 이후에
//                template = dialog.findViewById(R.id.my_template)
            }
            else -> {}
        }

        // set LayoutParams
        dialog.window!!.attributes = param

        // it show the dialog box
        dialog.show()
    }

    private fun downloadCheckDialog() {
        val tv = dialog.findViewById<TextView>(R.id.textView1)
        tv.setText(R.string.string_common_download_check)
        val b1 = dialog.findViewById<TextView>(R.id.button1)
        b1.setOnClickListener(this)
        val b2 = dialog.findViewById<TextView>(R.id.button2)
        b2.setTextColor(ContextCompat.getColor(context, R.color.main1))
        b2.setOnClickListener(this)
        b2.setText(R.string.string_common_ok)
    }

    private fun videoCheckDialog() {
        val tv = dialog.findViewById<TextView>(R.id.textView1)
        tv.setText(R.string.string_common_video_check)
        val b1 = dialog.findViewById<TextView>(R.id.button1)
        b1.visibility = GONE
        val b2 = dialog.findViewById<TextView>(R.id.button2)
        b2.setOnClickListener(this)
        b2.setText(R.string.string_common_ok)
    }

    private fun networkCheckDialog() {
        val tv = dialog.findViewById<TextView>(R.id.textView1)
        tv.setText(R.string.string_common_error_network)
        val b1 = dialog.findViewById<TextView>(R.id.button1)
        b1.setOnClickListener(this)
        b1.setText(R.string.string_common_exit)
        val b2 = dialog.findViewById<TextView>(R.id.button2)
        b2.setTextColor(ContextCompat.getColor(context, R.color.main1))
        b2.setOnClickListener(this)
        b2.setText(R.string.string_common_retry)
    }

    private fun transparentDialog() {
        val tv = dialog.findViewById<TextView>(R.id.textView1)
        tv.setText(R.string.string_common_delete_clip)
        val b1 = dialog.findViewById<TextView>(R.id.button1)
        b1.setOnClickListener(this)
        b1.setText(R.string.string_common_cancel)
        val b2 = dialog.findViewById<TextView>(R.id.button2)
        b2.setTextColor(ContextCompat.getColor(context, R.color.main1))
        b2.setOnClickListener(this)
        b2.setText(R.string.string_common_ok)
    }

    private fun permissionDialog() {
        dialog.findViewById<View>(R.id.button1).setOnClickListener(this)
        dialog.findViewById<View>(R.id.button2).setOnClickListener(this)
    }

    fun stopDialog() {
        if(this::dialog.isInitialized){
            dialog.dismiss()
        }
    }

    @SuppressLint("NonConstantResourceId")
    override fun onClick(view: View) {
        var share = false
        var name = ""
        when (view.id) {
            R.id.button1 -> mListener.onFinishEvent(false)
            R.id.button2 -> mListener.onFinishEvent(true)
            R.id.kakaotalk -> {
                share = true
                name = "kakao"
            }
            R.id.keep -> {
                share = true
                name = "keep"
            }
            R.id.onenote -> {
                share = true
                name = "onenote"
            }
            R.id.evernote -> {
                share = true
                name = "evernote"
            }
            R.id.more -> {
                share = true
                // share
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(Intent.EXTRA_TEXT, text)
                sendIntent.type = "text/plain"
                context.startActivity(Intent.createChooser(sendIntent, context.getString(R.string.string_clip_share)))

                //xx debug
                //shareSocial.shareInfo(context)
            }
        }

        if(!share)
            stopDialog()
        else {
            if (name != "") {
//                CoroutineScope(Dispatchers.IO).launch {
//                    val repo = AnyRepository(GlobalApplication.getInstance())
//                    val app = repo.getApplication(name)
//                    val componentName = ComponentName(app.packageName,app.activityName)
//
//                    shareSocial.sendClipShare(
//                            GlobalApplication.getGlobalApplicationContext(),
//                            componentName,
//                            "[SugarAlbum]",
//                            text
//                    )
//                }
            }
        }
    }

}