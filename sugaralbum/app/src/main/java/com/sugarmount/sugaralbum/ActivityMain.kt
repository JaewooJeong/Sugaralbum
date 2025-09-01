package com.sugarmount.sugaralbum

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.kiwiple.multimedia.canvas.Resolution
import com.sugarmount.sugarcamera.api.MovieContentApi
import com.sugarmount.sugarcamera.story.gallery.ConstantsGallery
import com.sugarmount.sugarcamera.story.gallery.SelectManager
import com.sugarmount.sugarcamera.story.movie.MovieEditMainActivity
import com.sugarmount.sugarcamera.story.movie.MovieEditMainActivity.ERROR_HANDLER
import com.sugarmount.common.env.MvConfig
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
import android.widget.FrameLayout
import androidx.appcompat.widget.SwitchCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.sugarmount.common.view.TouchImageView
import com.sugarmount.common.env.MvConfig.EXTRA_INFO_TYPE
import com.sugarmount.common.env.MvConfig.EXTRA_URI_INFO
import com.sugarmount.common.env.MvConfig.MY_FINISH_REQUEST
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.ArrayList
import kotlin.math.roundToInt

class ActivityMain : CustomAppCompatActivity(), View.OnClickListener {
    private lateinit var adView: AdView
    var mLoadMediaDataThread: LoadMediaDataThread? = null
    var backKeyPressedTime: Long = 0
    lateinit var popupDialog: PopupDialog
    private val compositeDisposable = CompositeDisposable()
    private val circularDrawable by lazy { CircularProgressDrawable(this) }
    private val photos = mutableListOf<ImageResData>()
    private val iSize by lazy { getImageSize() }
    private var initialLayoutComplete = false
    private var imageAdapter: ImageAdapter? = null
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView1: NavigationView
    private lateinit var relativeLayout5: RelativeLayout
    private lateinit var relativeLayout6: RelativeLayout
    private lateinit var relativeLayout8: RelativeLayout
    private lateinit var relativeLayout9: RelativeLayout
    private lateinit var relativeLayout10: RelativeLayout
    private lateinit var imageView1: ImageView
    private lateinit var imageView2: ImageView
    private lateinit var switch1: SwitchCompat
    private lateinit var temp12: TextView
    
    // Views from activity_main.xml
    private lateinit var selectImage: TouchImageView
    private lateinit var selectRotateLeft: ImageView
    private lateinit var selectRotateRight: ImageView
    private lateinit var resetSelected: ImageView
    private lateinit var send: ImageView
    private lateinit var selectRestore: ImageView
    private lateinit var recycler_view: RecyclerView
    private lateinit var ad_view_container: FrameLayout

    // init
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nav)
        setInsetView(this.findViewById(R.id.appBarLayout))
        setInsetView(this.findViewById(R.id.navigationView1))
        
        initViews()
        createView()
        
        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, 
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {
                // Show explanation dialog first
                AlertDialog.Builder(this)
                    .setTitle("알림 권한 필요")
                    .setMessage("동영상 생성 진행 상황을 알려드리기 위해 알림 권한이 필요합니다. 설정에서 알림을 허용해주세요.")
                    .setPositiveButton("설정으로 이동") { _, _ ->
                        // Open app notification settings
                        val intent = Intent().apply {
                            action = "android.settings.APP_NOTIFICATION_SETTINGS"
                            putExtra("android.provider.extra.APP_PACKAGE", packageName)
                        }
                        startActivity(intent)
                    }
                    .setNegativeButton("나중에") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }
    }
    
    private fun initViews() {
        // Views from activity_nav.xml
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView1 = findViewById(R.id.navigationView1)
        relativeLayout5 = findViewById(R.id.relativeLayout5)
        relativeLayout6 = findViewById(R.id.relativeLayout6)
        relativeLayout8 = findViewById(R.id.relativeLayout8)
        relativeLayout9 = findViewById(R.id.relativeLayout9)
        relativeLayout10 = findViewById(R.id.relativeLayout10)
        imageView1 = findViewById(R.id.imageView1)
        imageView2 = findViewById(R.id.imageView2)
        switch1 = findViewById(R.id.switch1)
        temp12 = findViewById(R.id.temp12)
        
        // Views from activity_main.xml
        selectImage = findViewById(R.id.selectImage)
        selectRotateLeft = findViewById(R.id.selectRotateLeft)
        selectRotateRight = findViewById(R.id.selectRotateRight)
        resetSelected = findViewById(R.id.resetSelected)
        send = findViewById(R.id.send)
        selectRestore = findViewById(R.id.selectRestore)
        recycler_view = findViewById(R.id.recycler_view)
        ad_view_container = findViewById(R.id.ad_view_container)
    }
    
    private fun createView() {
        log.e("#Activity_Main Call")
        initAds()
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

        relativeLayout5.setOnClickListener(this)
        relativeLayout6.setOnClickListener(this)
        relativeLayout8.setOnClickListener(this)
        relativeLayout9.setOnClickListener(this)
        relativeLayout10.setOnClickListener(this)

        imageView1.setOnClickListener(this)
        imageView2.setOnClickListener(this)
        switch1.setOnClickListener(this)

        // 221015 차후에 자유변형 저장 추가
//        selectImage.setOnClickListener(this)
//        selectImage.setOnTouchListener(selectImage.onTouch)
        selectRotateLeft.setOnClickListener(this)
        selectRotateRight.setOnClickListener(this)
        resetSelected.setOnClickListener(this)
        send.setOnClickListener(this)
        selectRestore.setOnClickListener(this)
    }
    private fun initRecyclerView() {
        (recycler_view.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        recycler_view.addItemDecoration(PhotoGridDivider((iSize * 0.02).roundToInt(), 4))
    }
    private fun setRecyclerView(data: List<ImageResData>) {
        ImageAdapter.itemCounter = 0
        imageAdapter = ImageAdapter(data, iSize, { adapterOnClick(it) }, { adapterOnCounterUpdate(it) } )
        val concatAdapter = ConcatAdapter(imageAdapter)
        recycler_view.adapter = concatAdapter
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun resetSelectedImage() {
        if(imageAdapter != null)
            imageAdapter?.resetCount()

        recycler_view.adapter?.notifyDataSetChanged()
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
    private fun initAds() {
        // 배너
        adView = AdView(this)
        ad_view_container.addView(adView)
        // Since we're loading the banner based on the adContainerView size, we need to wait until this
        // view is laid out before we can get the width.
        ad_view_container.viewTreeObserver.addOnGlobalLayoutListener {
            if (!initialLayoutComplete) {
                initialLayoutComplete = true
                loadBanner()
            }
        }
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
    // Determine the screen width (less decorations) to use for the ad width.
    // If the ad hasn't been laid out, default to the full screen width.
    private val adSize: AdSize
        get() {
            val display = windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)

            val density = outMetrics.density

            var adWidthPixels = ad_view_container.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }

            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
        }
    private fun loadBanner() {
        adView.adUnitId = application.getString(if (MvConfig.debug) R.string.banner_ad_unit_id_test else R.string.banner_ad_unit_id)
        adView.setAdSize(adSize)

        // Create an ad request.
        val adRequest = AdRequest.Builder().build()

        // Start loading the ad in the background.
        adView.loadAd(adRequest)
    }

    // thread
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
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.SIZE)

            // Display videos in alphabetical order based on their display name.
            val sortOrder = "${MediaStore.Files.FileColumns._ID} DESC"
            val query = applicationContext.contentResolver.query(uri, projection, null, null, sortOrder) ?: return mutableListOf()

            val listOfAllImages = mutableListOf<ImageResData>()

            query.use { cursor ->
                while (query.moveToNext()) {
                    if (isCancelled)
                        break
                    val mediaData = ImageResData()
                    mediaData._id = cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID))
                    mediaData.contentUri = Uri.withAppendedPath(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        mediaData._id.toString()
                    )
                    listOfAllImages += mediaData
                }
            }
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
                        val fileUri = data.getSerializableExtra(SelectManager.FILE_URI) as String?
                        log.e("SUCCESS: $fileUri")
                        intent = Intent(applicationContext, ActivityFinish::class.java)
                        intent.putExtra(EXTRA_URI_INFO, fileUri)
                        startActivityForResult(intent, MY_FINISH_REQUEST)
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
            }else{
                when(requestCode) {
                    MY_FINISH_REQUEST -> {
                        // 선택 초기화
                        resetSelectedImage();
                    }
                }
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
            imageView2, imageView1 -> {
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
            resetSelected -> {
                resetSelectedImage()
            }
            send -> {
                val images =
                    ArrayList<com.sugarmount.sugarcamera.ImageResData>()

                photos.filter { it.checked }.forEach { data ->
                    val tmp = com.sugarmount.sugarcamera.ImageResData()
                    tmp._id = data._id
                    tmp.checked = data.checked
                    tmp.date = data.date
                    tmp.contentUri = data.contentUri
                    tmp.isVideo = data.isVideo
                    tmp.selectOrder = data.selectOrder
                    images.add(tmp)
                }
                images.sortBy { it.selectOrder }

                // 5장 이상 40장 이하.
                if(images.count() in 5..40) {
                    when (MovieContentApi.checkDataValidate(applicationContext, images)) {
                        ERROR_HANDLER.SUCCESS -> {
                            log.e("SUCCESS")
                            send.isClickable = false
                            val intent =
                                Intent(applicationContext, MovieEditMainActivity::class.java)
                            intent.putExtra(
                                SelectManager.SELECTED_VIDEOS,
                                MovieContentApi.videoData
                            )
                            intent.putExtra(
                                SelectManager.SELECTED_IMAGES,
                                MovieContentApi.photoData
                            )
                            intent.putExtra(
                                SelectManager.SELECTED_RESOLUTION,
                                Resolution.NHD
                            )
                            intent.putExtra(
                                SelectManager.OUTPUT_DIR,
                                String.format(
                                    "%s/%s",
                                    applicationContext.getExternalFilesDir(android.os.Environment.DIRECTORY_MOVIES)?.path ?: applicationContext.externalMediaDirs[0].path,
                                    getString(R.string.app_name)
                                )
                            )
                            startActivityForResult(intent, ConstantsGallery.REQ_CODE_CONTENT_DETAIL)
//                            startActivity(intent)
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
                }else{
                    showToast(R.string.string_common_selected_count)
                    send.isClickable = true
                }
            }
        }
    }
    override fun onPause() {
        super.onPause()
//        if(mLoadMediaDataThread != null) {
//            mLoadMediaDataThread = LoadMediaDataThread()
//            mLoadMediaDataThread!!.start()
//        }
    }

    override fun onStart() {
        super.onStart()

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        if(send != null)
            send.isClickable = true
    }
}