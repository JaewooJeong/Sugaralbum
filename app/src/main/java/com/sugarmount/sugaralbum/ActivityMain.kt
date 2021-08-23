package com.sugarmount.sugaralbum

import android.R.attr
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.database.Cursor
import android.graphics.*
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.kiwiple.multimedia.canvas.Resolution
import com.lguplus.pluscamera.api.MovieContentApi
import com.lguplus.pluscamera.story.gallery.ConstantsGallery
import com.lguplus.pluscamera.story.gallery.SelectManager
import com.lguplus.pluscamera.story.movie.MovieEditMainActivity
import com.lguplus.pluscamera.story.movie.MovieEditMainActivity.ERROR_HANDLER
import com.sugarmount.common.model.MvConfig
import com.sugarmount.common.room.AnyRepository
import com.sugarmount.common.room.version.VersionT
import com.sugarmount.common.utils.CustomAppCompatActivity
import com.sugarmount.common.utils.log
import com.sugarmount.common.view.GridViewer
import com.sugarmount.common.view.PopupDialog
import com.sugarmount.sugaralbum.model.ImageResData
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_nav.*
import kotlinx.android.synthetic.main.image_cell.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.ArrayList
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.graphics.PointF
import com.sugarmount.common.view.TouchImageView
import kotlin.math.atan2
import kotlin.math.sqrt
import android.R.attr.thumbnail

import android.graphics.BitmapFactory
import android.R.attr.bitmap

import android.media.ThumbnailUtils

import android.graphics.Bitmap








class ActivityMain : CustomAppCompatActivity(), View.OnClickListener,
    AdapterView.OnItemClickListener {
    var gridAdapter: GridAdapter? = null
    var mLoadMediaDataThread: LoadMediaDataThread? = null
    var backKeyPressedTime: Long = 0
    lateinit var popupDialog: PopupDialog
    var mIv: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nav)
        // 배너
//        val adRequest = AdRequest.Builder().build()
//        adView.loadAd(adRequest)

        createView()
    }

    private fun createView() {
        log.e("#Activity_Main Call")
        init()
        initDrawerLayout()
    }

    private fun init() {
        popupDialog = PopupDialog(this)
        popupDialog.setOnFinishClickEvent{}

        // room data access (version)
        val repo = AnyRepository(application)
        CoroutineScope(Dispatchers.IO).launch {
            val list: List<VersionT> = repo.getVersion()
            if (list.isNotEmpty()) {
                log.e("list:", list)
                runOnUiThread {
                    temp12.text = "(${list[0].infoVersion})"
                }
            }
        }

        relativeLayout5.setOnClickListener(this)
        relativeLayout6.setOnClickListener(this)
        relativeLayout8.setOnClickListener(this)
        relativeLayout9.setOnClickListener(this)
        relativeLayout10.setOnClickListener(this)

        switch1.setOnClickListener(this)
        selectImage.setOnClickListener(this)
        ImgGridView.onItemClickListener = this
        gridAdapter = GridAdapter()

        relativeLayout6.isEnabled = false

        mLoadMediaDataThread = LoadMediaDataThread()
        mLoadMediaDataThread!!.start()

        send.setOnClickListener {
            val itemData =
                ArrayList<com.lguplus.pluscamera.ImageResData>()

//            gridAdapter.getList().forEach(it -> {
//                if(it.isChecked()) {
//                    com.lguplus.pluscamera.ImageResData tmp = new com.lguplus.pluscamera.ImageResData();
//                    tmp._id = it._id;
//                    tmp.checked = it.checked;
//                    tmp.date = it.date;
//                    tmp.contentPath = it.contentPath;
//                    tmp.isVideo = it.isVideo;
//                    itemData.add(tmp);
//                }
//            });
            for (k in 0..4) {
                val tmp = com.lguplus.pluscamera.ImageResData()
                val it = gridAdapter!!.getItem(k)
                tmp._id = it._id
                tmp.checked = it.checked
                tmp.date = it.date
                tmp.contentPath = it.contentUri.toString()
                tmp.isVideo = it.isVideo
                itemData.add(tmp)
            }
            when (MovieContentApi.checkDataValidate(applicationContext, itemData)) {
                ERROR_HANDLER.SUCCESS -> {
                    log.e("SUCCESS")
                    val intent =
                        Intent(applicationContext, MovieEditMainActivity::class.java)
                    intent.putExtra(SelectManager.SELECTED_VIDEOS, MovieContentApi.videoData)
                    intent.putExtra(SelectManager.SELECTED_IMAGES, MovieContentApi.photoData)
                    intent.putExtra(
                        SelectManager.SELECTED_RESOLUTION,
                        Resolution.NHD
                    )
                    intent.putExtra(
                        SelectManager.OUTPUT_DIR,
                        String.format(
                            "%s/%s",
                            applicationContext.externalMediaDirs[0].path,
                            getString(R.string.app_name)
                        )
                    )
                    startActivityForResult(intent, ConstantsGallery.REQ_CODE_CONTENT_DETAIL)
                }
                ERROR_HANDLER.UNKNOWN_ERROR -> log.e("UNKNOWN_ERROR")
                ERROR_HANDLER.ITEM_COUNT -> log.e("ITEM_COUNT")
                ERROR_HANDLER.VIDEO_COUNT -> log.e("VIDEO_COUNT")
                ERROR_HANDLER.IMAGE_COUNT -> log.e("IMAGE_COUNT")
                ERROR_HANDLER.VIDEO_MAX_DURATION -> log.e("VIDEO_MAX_DURATION")
                ERROR_HANDLER.VIDEO_MIN_DURATION -> log.e("VIDEO_MIN_DURATION")
                ERROR_HANDLER.NOT_FOUND -> log.e("NOT_FOUND")
                ERROR_HANDLER.CODEC_ERROR -> log.e("CODEC_ERROR")
                else -> {
                }
            }
        }

        selectImage.setOnTouchListener(selectImage.onTouch)
//        selectImage.scaleType = ImageView.ScaleType.MATRIX
        selectRestore.setOnClickListener(this)
    }

    private fun showToast(id: Int){
        runOnUiThread {
            GlobalApplication.setToast(
                Toast.makeText(
                    applicationContext,
                    getString(id),
                    Toast.LENGTH_SHORT
                )
            )
            GlobalApplication.getToast().show()
        }
    }

    private fun initDrawerLayout() {
        setDrawerLayoutParams(navigationView1)

        // Set the drawer toggle as the DrawerListener
        drawerLayout.setDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(
                drawerView: View,
                slideOffset: Float
            ) {
            }

            override fun onDrawerOpened(drawerView: View) {}
            override fun onDrawerClosed(drawerView: View) {}
            override fun onDrawerStateChanged(newState: Int) {
                if (drawerLayout.isDrawerOpen(GravityCompat.END) && newState == 0) {
                } else if (drawerLayout.isDrawerOpen(
                        GravityCompat.START
                    ) && newState == 0
                ) {
                }
            }
        })
    }

    private fun setDrawerLayoutParams(v: View) {
        val p = v.layoutParams as DrawerLayout.LayoutParams
        p.width = (GlobalApplication.getPoint().x * 0.999).toInt()
        v.layoutParams = p
    }

    inner class GridAdapter : BaseAdapter() {
        var list = ArrayList<ImageResData>()

        override fun getCount(): Int {
            return list.size
        }

        fun addItem(ird: ImageResData) {
            list.add(ird)
        }

        override fun getItem(i: Int): ImageResData {
            return list[i]
        }

        override fun getItemId(i: Int): Long {
            gridAdapter.let {
                selectImage.setImageURI(it?.getItem(i)?.contentUri)
            }
            return i.toLong()
        }

        override fun getView(i: Int, view: View?, viewGroup: ViewGroup?): View {
            val sv = GridViewer(applicationContext)
            sv.setItem(list[i])
            return sv
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun onSetColorFilter(iv: ImageView?, b: Boolean) {
        if (b) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                iv!!.colorFilter = BlendModeColorFilter(R.color.transparent_, BlendMode.DST_IN)
            } else {
                iv!!.setColorFilter(R.color.transparent_, PorterDuff.Mode.DST_IN)
            }
        } else {
            if (iv != null) iv.colorFilter = null
        }
    }

    @SuppressLint("StaticFieldLeak")
    inner class DownloadImagesTask : AsyncTask<String?, Void?, Void?>() {
        override fun onPostExecute(v: Void?) {
            ImgGridView.adapter = gridAdapter
            if (gridAdapter?.count!! > 0) {
                ImgGridView.setSelection(0)
                selectImage.setImageURI(gridAdapter!!.getItem(0).contentUri)
            }
        }

        override fun doInBackground(vararg params: String?): Void? {
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    inner class LoadMediaDataThread : Thread() {
        private var isCancelled = false
        fun cancel() {
            isCancelled = true
        }

        private val mediaCursor: Cursor?
            private get() {
                val projection = arrayOf(
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Images.Media.DISPLAY_NAME
                )
                return contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    null
                )
            }


        override fun run() {
            gridAdapter?.list?.clear()
            val mediaCursor: Cursor? = mediaCursor
            if (mediaCursor != null) {
                mediaCursor.moveToLast()
                while (mediaCursor.moveToPrevious()) {
                    if (isCancelled) {
                        break
                    }
                    val mediaData = ImageResData()
                    mediaData._id =
                        mediaCursor.getLong(mediaCursor.getColumnIndex(MediaStore.Files.FileColumns._ID))
                    mediaData.contentUri = Uri.withAppendedPath(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        mediaData._id.toString()
                    )

//                    log.e("id: %d, uri: %s", mediaData._id, mediaData.contentUri.toString());
                    gridAdapter?.addItem(mediaData)
                }
                mediaCursor.close()
            }
            DownloadImagesTask().execute()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (data != null) {
                val er = data.getSerializableExtra(SelectManager.ERROR_CODE) as ERROR_HANDLER?
                when (er) {
                    ERROR_HANDLER.SUCCESS -> {
                        val file_uri = data.getSerializableExtra(SelectManager.FILE_URI) as String?
                        log.e("SUCCESS: $file_uri")
                    }
                    ERROR_HANDLER.UNKNOWN_ERROR -> log.e("UNKNOWN_ERROR")
                    ERROR_HANDLER.ITEM_COUNT -> log.e("ITEM_COUNT")
                    ERROR_HANDLER.VIDEO_COUNT -> log.e("VIDEO_COUNT")
                    ERROR_HANDLER.IMAGE_COUNT -> log.e("IMAGE_COUNT")
                    ERROR_HANDLER.VIDEO_MAX_DURATION -> log.e("VIDEO_MAX_DURATION")
                    ERROR_HANDLER.VIDEO_MIN_DURATION -> log.e("VIDEO_MIN_DURATION")
                    ERROR_HANDLER.NOT_FOUND -> log.e("NOT_FOUND")
                    ERROR_HANDLER.CODEC_ERROR -> log.e("CODEC_ERROR")
                    else -> log.e("??")
                }
                //                new test_v2().execute();
            }
        } catch (e: Exception) {
            log.e("############### ex:$e")
        }
    }

    override fun onBackPressed() {
        val drawer: DrawerLayout = findViewById(R.id.drawerLayout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else if (drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END)
        } else {
            if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
                backKeyPressedTime = System.currentTimeMillis()

                showToast(R.string.string_common_back_button)
                return
            }
            if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
                super.onBackPressed()
                GlobalApplication.getToast().cancel()
                GlobalApplication.setToast(null)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        popupDialog.stopDialog()
    }

    override fun onClick(v: View?) {
        when(v){
            relativeLayout6, switch1 -> {
                if (v != switch1)
                    switch1.isChecked = !switch1.isChecked
            }
            relativeLayout8, relativeLayout9, relativeLayout10 -> {
                intent = Intent(applicationContext, ActivityInformation::class.java)
                var infoType: MvConfig.INFO_TYPE = MvConfig.INFO_TYPE.NOTICE
                // notice
                when (v) {
                    relativeLayout8 -> infoType = MvConfig.INFO_TYPE.NOTICE
                    relativeLayout9 -> infoType = MvConfig.INFO_TYPE.FAQ
                    relativeLayout10 -> infoType = MvConfig.INFO_TYPE.LICENSE
                }
                intent.putExtra(EXTRA_INFO_TYPE, infoType.rc)
                startActivity(intent)
            }
            selectRestore -> {
                selectImage.restore()
            }
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        // 이전 선택 cell filter 제외
        onSetColorFilter(mIv, false)

        val rowData = gridAdapter!!.getItem(position)
        rowData.isChecked = !rowData.checked

        // cell 선택
        mIv = view?.findViewById(R.id.ivImage)
        onSetColorFilter(mIv, true)
    }
}