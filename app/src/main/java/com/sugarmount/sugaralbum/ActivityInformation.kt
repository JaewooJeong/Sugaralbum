package com.sugarmount.sugaralbum

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.sugarmount.common.listener.DataClickEventListener
import com.sugarmount.common.model.MvConfig.INFO_TYPE
import com.sugarmount.common.model.MvConfig.PAGE_DIRECTION
import com.sugarmount.common.room.AnyRepository
import com.sugarmount.common.room.info.InfoT
import com.sugarmount.common.utils.CustomAppCompatActivity
import com.sugarmount.common.utils.CustomLinearLayoutManager
import com.sugarmount.common.utils.EndlessRecyclerOnScrollListener
import com.sugarmount.common.utils.log
import com.sugarmount.sugaralbum.adapter.RecyclerAdapterInformation
import kotlinx.android.synthetic.main.activity_information.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class ActivityInformation : CustomAppCompatActivity(), DataClickEventListener,
        SwipeRefreshLayout.OnRefreshListener {
    private var recyclerAdapterInformation: RecyclerAdapterInformation? = null
    private var infoType: INFO_TYPE? = INFO_TYPE.NOTICE
    private var endlessRecyclerOnScrollListener: EndlessRecyclerOnScrollListener? = null
    private var bRefresh = false
    private var isRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_information)

        GlobalApplication.setStatusColor(
            window,
            ContextCompat.getColor(applicationContext, R.color.main2)
        )

        getIntentData()
        initToolbar()
        initView()

        getRequestData(1)
    }

    private fun initToolbar() {
        imageView1.setOnClickListener {
            finish() // close this activity as oppose to navigating up
        }
    }

    private fun initView() {
        progress_bar.indeterminateDrawable.setColorFilter(ContextCompat.getColor(this,
            R.color.holo_red_light
        ), PorterDuff.Mode.MULTIPLY)


        if (swipe_container != null) {
            swipe_container.setColorSchemeResources(
                R.color.holo_red_light,
                R.color.holo_blue_bright,
                R.color.holo_green_light,
                R.color.holo_orange_light
            )
            swipe_container.setOnRefreshListener(this)
        }

        setupRecyclerView()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupRecyclerView(){
        val linearLayoutManager = CustomLinearLayoutManager(
            this,
            CustomLinearLayoutManager.VERTICAL,
            false
        )
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setHasFixedSize(true)

        endlessRecyclerOnScrollListener = object : EndlessRecyclerOnScrollListener(linearLayoutManager) {
            override fun onLoadMore(current_page: Int) {
                AsyncTaskProgress(this@ActivityInformation, current_page).execute()
            }

            override fun onScrollChange(scrollEvent: Boolean, dy: Int) {

            }
        }

        recyclerView.addOnScrollListener(endlessRecyclerOnScrollListener as EndlessRecyclerOnScrollListener)

        recyclerAdapterInformation =
            RecyclerAdapterInformation(PAGE_DIRECTION.INFORMATION, this)
        recyclerView.adapter = recyclerAdapterInformation

        getRequestData(0)

        runOnUiThread {
            recyclerAdapterInformation!!.notifyDataSetChanged()
        }
    }

    @SuppressLint("StaticFieldLeak")
    class AsyncTaskProgress(private var activity: ActivityInformation?, private var current_page: Int) : AsyncTask<Void, Void, Void>() {
        override fun onPreExecute() {
            super.onPreExecute()
            activity?.progress_bar?.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): Void? {
            try {
                var nCount = 0
                while (activity?.isRunning!!) {
                    Thread.sleep(SIMULATED_LOADING_TIME_IN_MS)
                    if (nCount++ >= CONNECT_TIMEOUT) {
                        break
                    }
                }

            } catch (e: InterruptedException) {
                log.e(e.message)
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)

            activity?.progress_bar?.visibility = View.GONE
            activity?.getRequestData(current_page)

        }
    }


    @SuppressLint("NotifyDataSetChanged")
    fun getRequestData(page: Int) {
        isRunning = true
        val repo = AnyRepository(application)

        // room data access
        CoroutineScope(Dispatchers.IO).launch {
            var locale = if(Locale.getDefault() == Locale.KOREA)
                "KR"
            else
                "EN"

            if(infoType == INFO_TYPE.LICENSE)
                locale = "KR"

            val list: List<InfoT> = repo.getInfo(locale, infoType.toString())
            recyclerAdapterInformation!!.mItemList.clear()
            recyclerView.visibility = View.VISIBLE
            list.forEach {
                recyclerAdapterInformation!!.mItemList.add(it)
            }

            runOnUiThread {
                recyclerAdapterInformation?.notifyDataSetChanged()
            }

            swipe_container.isRefreshing = false
            isRunning = false
        }
    }

    private fun getIntentData() {
        val intent = intent
        if (intent != null) {

            when(intent.getIntExtra(EXTRA_INFO_TYPE, INFO_TYPE.NOTICE.rc)){
                INFO_TYPE.NOTICE.rc -> {
                    // Notice
                    toolbarTitle.text = getString(R.string.string_menu_notice)
                    infoType = INFO_TYPE.NOTICE
                }
                INFO_TYPE.FAQ.rc -> {
                    // FAQ
                    toolbarTitle.text = getString(R.string.string_menu_faq)
                    infoType = INFO_TYPE.FAQ
                }
                INFO_TYPE.LICENSE.rc -> {
                    // License
                    toolbarTitle.text = getString(R.string.string_menu_open_source_license)
                    infoType = INFO_TYPE.LICENSE
                }
            }
        }
    }

    override fun onRefresh() {
        if (endlessRecyclerOnScrollListener != null)
            endlessRecyclerOnScrollListener?.reset()

        if (recyclerAdapterInformation != null) {
            bRefresh = true
            setupRecyclerView()
            bRefresh = true
            getRequestData(1)
        }
    }

    override fun onClickEvent(page_direction: PAGE_DIRECTION?, position: Int, select: Boolean) {}

    override fun onSupportNavigateUp(): Boolean {
        finish() // close this activity as oppose to navigating up
        return false
    }
}
