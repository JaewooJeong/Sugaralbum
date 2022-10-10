package com.sugarmount.common.utils

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.*
import android.net.Uri
import androidx.annotation.AnyRes
import com.sugarmount.sugaralbum.R


class shareSocial {

    companion object {
        /**
         * 공유 intent의 package와 name을 가져온다.
         */
        @SuppressLint("WrongConstant")
        fun shareInfo(context: Context): ComponentName {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            val resInfo = context.packageManager.queryIntentActivities(intent, MATCH_DIRECT_BOOT_UNAWARE)

            for (info in resInfo) {
                if (info.activityInfo.packageName.indexOf("kakao") != -1 &&
                    info.activityInfo.name.indexOf("MemoChatConnectActivity") != -1) {
                    //return ComponentName(info.activityInfo.applicationInfo.packageName, info.activityInfo.name)
                }

                log.e("package:%s, name:%s", info.activityInfo.packageName, info.activityInfo.name)
            }

            return ComponentName("", "")
        }

        fun getUriToDrawable(
                context: Context,
                @AnyRes drawableId: Int
        ): Uri {
            return Uri.parse(
                    ContentResolver.SCHEME_ANDROID_RESOURCE +
                            "://" + context.resources.getResourcePackageName(drawableId)
                            + '/' + context.resources.getResourceTypeName(drawableId)
                            + '/' + context.resources.getResourceEntryName(drawableId)
            )
        }

        fun sendClipShare(context: Context, name: ComponentName, subject: String, clip: String) {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            //shareIntent.type = "image/*"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
            shareIntent.putExtra(Intent.EXTRA_TEXT, clip)
            //shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.component = name

            val intentChooser = Intent.createChooser(shareIntent, context.getString(R.string.string_clip_share))
            intentChooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intentChooser)
        }

    }
}