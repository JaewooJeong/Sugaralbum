package com.sugarmount.sugaralbum

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

class ImageListV2: Activity(), AbsListView.OnScrollListener, AdapterView.OnItemClickListener {
    companion object {
        private lateinit var ImgGridView2 : GridView
        private lateinit var mListAdapter: ImageAdapter
        private var mMediaDataList = ArrayList<ImageResData>()
        private var bUpdate = false
        private var bScroll = false
        private lateinit var mContext: Context
        private val TAG = ImageListV2::class.java.simpleName
        private var mMovDiaryCreate: Button? = null
        private var mLoadMediaDataThread: LoadMediaDataThread? = null

        fun updateUI(){
            mListAdapter = ImageAdapter(mContext, R.layout.image_cell, mMediaDataList)
            ImgGridView2.adapter = mListAdapter
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mContext = this
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) { // Should we show an explanation?
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) { // Explain to the user why we need to read the contacts
                }
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    1
                )
                return
            }
        }

        cancelLoadMediaDataThread()
        mLoadMediaDataThread = LoadMediaDataThread()
        mLoadMediaDataThread!!.start()

        ImgGridView2 = findViewById(R.id.ImgGridView)
        ImgGridView.setOnScrollListener(this)
        ImgGridView.onItemClickListener = this
        mMovDiaryCreate = findViewById(R.id.gogo)
        mMovDiaryCreate!!.setOnClickListener {
            val itemData =
                ArrayList<ImageResData?>()
            var tmp: ImageResData? = null
            for (i in mMediaDataList.indices) {
                tmp = mMediaDataList[i]
                if (tmp.checked) {
                    itemData.add(tmp)
                }
            }
        }
    }

    override fun onScroll(
        view: AbsListView?,
        firstVisibleItem: Int,
        visibleItemCount: Int,
        totalItemCount: Int
    ) {
    }

    override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {
        when (scrollState) {
            AbsListView.OnScrollListener.SCROLL_STATE_IDLE ->  //mListAdapter.notifyDataSetChanged();
                bScroll = false
            AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL -> bUpdate = true
            AbsListView.OnScrollListener.SCROLL_STATE_FLING -> bUpdate = true
        }
    }

    override fun onItemClick(
        arg0: AdapterView<*>,
        arg1: View?,
        position: Int,
        arg3: Long
    ) {
        val adapter = arg0.adapter as ImageAdapter
        val rowData = adapter.getItem(position) as ImageResData
        val curCheckState = rowData.checked
        rowData.checked = !curCheckState
        mMediaDataList[position] = rowData
        bUpdate = true
        adapter.notifyDataSetChanged()
    }

    internal class ImageViewHolder {
        var ivImage: ImageView? = null
        var chkImage: CheckBox? = null
    }

    class ImageAdapter internal constructor(
        c: Context,
        cellLayout: Int,
        thumbImageInfoList: ArrayList<ImageResData>
    ) :
        BaseAdapter() {
        private val mCellLayout: Int
        private val mLiInflater: LayoutInflater
        override fun getCount(): Int {
            return mMediaDataList.size
        }

        override fun getItem(position: Int): Any {
            return mMediaDataList.get(position)
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(
            position: Int,
            convertView: View,
            parent: ViewGroup
        ): View {
            var convertView = convertView
            if (convertView == null) {
                convertView = mLiInflater.inflate(mCellLayout, parent, false)
                val holder = ImageViewHolder()
                holder.ivImage = convertView.findViewById(R.id.ivImage)
//                holder.chkImage = convertView.findViewById(R.id.chkImage)
                convertView.tag = holder
            }
            if (!bScroll) {
                val holder = convertView.tag as ImageViewHolder
                if (!bUpdate) {
                    holder.chkImage!!.isChecked = mMediaDataList.get(position).checked
                    //                    BitmapFactory.Options bo = new BitmapFactory.Options();
//                    bo.inSampleSize = 8;
//                    Bitmap bmp = BitmapFactory.decodeFile(mMediaDataList.get(position).contentPath, bo);
//                    holder.ivImage.setImageBitmap(bmp);
                    Picasso.get()
                        .load(mMediaDataList[position].contentUri)
                        .resize(300, 300)
                        .centerCrop()
                        .into(holder.ivImage)
                    holder.ivImage!!.visibility = View.VISIBLE
                } else {
                    holder.chkImage!!.isChecked = mMediaDataList.get(position).checked
                }
            }
            return convertView
        }

        init {
            mContext = c
            mCellLayout = cellLayout
            mMediaDataList = thumbImageInfoList
            mLiInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        }
    }

    private fun cancelLoadMediaDataThread() {
        if(mLoadMediaDataThread != null)
            mLoadMediaDataThread!!.cancel()
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private class LoadMediaDataThread : Thread() {
        private var isCancelled = false
        fun cancel() {
            isCancelled = true
        }

        private val mMediaListDateComparator =
            Comparator<ImageResData> { lhs, rhs ->
                // TODO Auto-generated method stub
                if (lhs.date > rhs.date) -1 else if (lhs.date < rhs.date) 1 else 0
            }// 1

        @SuppressLint("InlinedApi")
        override fun run() {
            mMediaDataList.clear()

            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_TAKEN
            )
            val selection = "${MediaStore.Images.Media.DATE_TAKEN} >= ?"
            val selectionArgs = arrayOf(
                dateToTimestamp(day = 1, month = 1, year = 1970).toString()
            )
            val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

            val cursor = mContext?.contentResolver?.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )

            cursor?.use {
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val dateTakenColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
                val displayNameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                while (cursor.moveToNext()) {

                    var mediaData = ImageResData()
                    val id = cursor.getLong(idColumn)
                    val dateTaken = Date(cursor.getLong(dateTakenColumn))
                    val displayName = cursor.getString(displayNameColumn)
                    val contentUri = Uri.withAppendedPath(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id.toString()
                    )
                    Log.d(
                        TAG, "id: $id, display_name: $displayName, date_taken: " +
                                "$dateTaken, content_uri: $contentUri"
                    )

                    mediaData._id = id
                    mediaData.contentUri = contentUri
                    mMediaDataList.add(mediaData)
                }
            }

            Collections.sort(mMediaDataList, mMediaListDateComparator)
            //DownloadImagesTask().execute()
            updateUI()
        }

        fun dateToTimestamp(day: Int, month: Int, year: Int): Long =
            SimpleDateFormat("dd.MM.yyyy").let { formatter ->
                formatter.parse("$day.$month.$year")?.time ?: 0
            }
    }

}