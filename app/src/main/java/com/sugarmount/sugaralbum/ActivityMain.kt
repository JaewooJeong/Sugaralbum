package com.sugarmount.sugaralbum

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.View
import android.widget.*
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
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
import com.sugarmount.common.utils.PhotoGridDivider
import com.sugarmount.common.utils.log
import com.sugarmount.common.view.PopupDialog
import com.sugarmount.sugaralbum.adapter.ImageAdapter
import com.sugarmount.sugaralbum.model.ImageResData
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.activity_nav.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.ArrayList
import kotlin.math.roundToInt

class ActivityMain : CustomAppCompatActivity(), View.OnClickListener {
    var mLoadMediaDataThread: LoadMediaDataThread? = null
    var backKeyPressedTime: Long = 0
    lateinit var popupDialog: PopupDialog
    private val compositeDisposable = CompositeDisposable()
    private val circularDrawable by lazy { CircularProgressDrawable(this) }
    private val photos = mutableListOf<ImageResData>()
    private val iSize by lazy { getImageSize() }

    // init
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
        initRecyclerView()
    }
    private fun init() {
        popupDialog = PopupDialog(this)
        popupDialog.setOnFinishClickEvent{}

        // room data access (version)
        val repo = AnyRepository(application)
        CoroutineScope(Dispatchers.IO).launch {
            val list: List<VersionT> = repo.getVersion()
            if (list.isNotEmpty()) {
//                log.e("list:", list)
                runOnUiThread {
                    temp12.text = "(${list[0].infoVersion})"
                }
            }
        }

        relativeLayout6.isEnabled = false

        mLoadMediaDataThread = LoadMediaDataThread()
        mLoadMediaDataThread!!.start()

        relativeLayout1.setOnClickListener(this)
        relativeLayout5.setOnClickListener(this)
        relativeLayout6.setOnClickListener(this)
        relativeLayout8.setOnClickListener(this)
        relativeLayout9.setOnClickListener(this)
        relativeLayout10.setOnClickListener(this)

        imageView1.setOnClickListener(this)
        switch1.setOnClickListener(this)
        selectImage.setOnClickListener(this)
        selectImage.setOnTouchListener(selectImage.onTouch)
        selectRotateLeft.setOnClickListener(this)
        selectRotateRight.setOnClickListener(this)
        send.setOnClickListener(this)
        selectRestore.setOnClickListener(this)
    }
    private fun initRecyclerView() {
        (recycler_view.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        recycler_view.addItemDecoration(PhotoGridDivider((iSize * 0.02).roundToInt(), 4))
    }
    private fun setRecyclerView(data: List<ImageResData>) {
        ImageAdapter.itemCounter = 0
        val imageAdapter = ImageAdapter(data, iSize, { adapterOnClick(it) }, { adapterOnCounterUpdate(it) } )
        val concatAdapter = ConcatAdapter(imageAdapter)
        recycler_view.adapter = concatAdapter
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

    // listener
    @SuppressLint("NotifyDataSetChanged")
    private fun adapterOnClick(image: ImageResData) {
        if(image.checked)
            showImage(image)
        else{
            photos.filter { it.checked && it.selectOrder == 1 }.forEach { data ->
                showImage(data)
            }
            selectImage.restore()
        }
    }
    private fun adapterOnCounterUpdate(image: ImageResData) {
        recycler_view.adapter?.notifyItemChanged(photos.indexOf(image))
    }

    // function
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
    private fun setDrawerLayoutParams(v: View) {
        val p = v.layoutParams as DrawerLayout.LayoutParams
        p.width = (GlobalApplication.getPoint().x * 0.999).toInt()
        v.layoutParams = p
    }
    private fun showData(data: List<ImageResData>) {
        recycler_view.visibility = View.VISIBLE
        val image = data[0]
        showImage(image)
        setRecyclerView(data)
    }
    private fun showImage(image: ImageResData) {
        selectImage.setImagePosition(image)
        Glide.with(this)
            .load(image.contentUri)
            .placeholder(circularDrawable)
            .into(selectImage)
//        selectImage.setImageURI(uri)
    }
    private fun getImageSize(): Int {
        val displayMetrics: DisplayMetrics = this.resources.displayMetrics
        val width = displayMetrics.widthPixels
        val dividers = 5 * 0.02
        return (width / (4 + dividers)).roundToInt()
    }

    // thread
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    inner class LoadMediaDataThread : Thread() {
        private var isCancelled = false
        fun cancel() {
            isCancelled = true
        }

        @SuppressLint("Range")
        private fun retrieveFiles(): MutableList<ImageResData> {
            val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val cursor = applicationContext.contentResolver.query(uri, projection, null, null, null) ?: return mutableListOf()
            val listOfAllImages = ArrayList<ImageResData>()

            cursor.moveToLast()
            while (cursor.moveToPrevious()) {
                if (isCancelled) {
                    break
                }
                val mediaData = ImageResData()
                mediaData._id = cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID))
                mediaData.contentUri = Uri.withAppendedPath(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    mediaData._id.toString()
                )

                listOfAllImages.add(mediaData)

            }
            cursor.close()
            return listOfAllImages
        }

        override fun run() {
            compositeDisposable.add(
                Single.fromCallable { retrieveFiles() }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            photos.clear()
                            photos.addAll(it)
                            showData(photos)
                        },
                        {
                            log.e("Error while retrieving data")
                        }
                    )
            )
        }
    }

    // override function
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (data != null) {
                when (data.getSerializableExtra(SelectManager.ERROR_CODE) as ERROR_HANDLER?) {
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
            relativeLayout1, imageView1 -> {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    drawerLayout.openDrawer(GravityCompat.START)
                }
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
            selectRotateLeft -> {
                selectImage.rotateLeft()
            }
            selectRotateRight -> {
                selectImage.rotateRight()
            }
            send -> {
                val images =
                    ArrayList<com.lguplus.pluscamera.ImageResData>()

                photos.filter { it.checked }.forEach { data ->
                    val tmp = com.lguplus.pluscamera.ImageResData()
                    tmp._id = data._id
                    tmp.checked = data.checked
                    tmp.date = data.date
                    tmp.contentUri = data.contentUri
                    tmp.isVideo = data.isVideo
                    tmp.selectOrder = data.selectOrder
                    images.add(tmp)
                }
                images.sortBy { it.selectOrder }

                when (MovieContentApi.checkDataValidate(applicationContext, images)) {
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
        }
    }
}