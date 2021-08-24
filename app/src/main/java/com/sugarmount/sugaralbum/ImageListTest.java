package com.sugarmount.sugaralbum;
/**
 * Created by Jaewoo on 2017-02-16.
 */

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;

import com.squareup.picasso.Picasso;
import com.sugarmount.common.utils.log;
import com.sugarmount.sugaralbum.model.ImageResData;

import java.util.ArrayList;
import java.util.Comparator;

public class ImageListTest extends Activity implements ListView.OnScrollListener, GridView.OnItemClickListener{

    private String TAG = ImageListTest.class.getSimpleName();
    boolean bUpdate = false;
    private boolean bScroll = false;
    private GridView mGvImageList;
    private ImageAdapter mListAdapter;
    private Button mMovDiaryCreate;
    private ArrayList<ImageResData> mMediaDataList = new ArrayList<ImageResData>();
    private Context mContext = null;
    private LoadMediaDataThread mLoadMediaDataThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

//        ResInfo res = new ResInfo();
//        int pc = res.getPhotoMaxCount();
//        int vc = res.getVideoMaxCount();
//        long vmaxd = res.getVideoMaxDuration();
//        long vmind = res.getVideoMinDuration();
//        long vmw = res.getVideoMaxWidth();
//        long wmh = res.getVideoMaxHeight();
//        long vms = res.getVideoMaxSize();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // Explain to the user why we need to read the contacts
                }
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return;
            }
        }

        cancelLoadMediaDataThread();
        mLoadMediaDataThread = new LoadMediaDataThread();
        mLoadMediaDataThread.start();

        mGvImageList = (GridView)findViewById(R.id.ImgGridView);
        mGvImageList.setOnScrollListener(this);
        mGvImageList.setOnItemClickListener(this);

        mMovDiaryCreate = (Button)findViewById(R.id.gogo);
        mMovDiaryCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<ImageResData> itemData = new ArrayList<ImageResData>();
                ImageResData tmp = null;
                for(int i=0; i < mMediaDataList.size(); i++){
                    tmp = mMediaDataList.get(i);
                    if(tmp.checked) {
                        itemData.add(tmp);
                    }
                }
            }
        });

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount){}

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState){
//        switch (scrollState)
//        {
//            case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
//                //mListAdapter.notifyDataSetChanged();
//                bScroll = false;
//                break;
//            case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
//                bUpdate = true;
//                break;
//            case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
//                bUpdate = true;
//                break;
//        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3){
        ImageAdapter adapter = (ImageAdapter) arg0.getAdapter();
        ImageResData rowData = (ImageResData)adapter.getItem(position);

        boolean curCheckState = rowData.checked;

        rowData.checked = !curCheckState;

        mMediaDataList.set(position, rowData);
        bUpdate = true;
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    static class ImageViewHolder{
        ImageView ivImage;
        CheckBox chkImage;
    }

    public class ImageAdapter extends BaseAdapter {
        private int mCellLayout;
        private LayoutInflater mLiInflater;

        ImageAdapter(Context c, int cellLayout, ArrayList<ImageResData> thumbImageInfoList){
            mContext = c;
            mCellLayout = cellLayout;
            mMediaDataList = thumbImageInfoList;
            mLiInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return mMediaDataList.size();
        }

        public Object getItem(int position)
        {
            return mMediaDataList.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null){
                convertView = mLiInflater.inflate(mCellLayout, parent, false);
                ImageViewHolder holder = new ImageViewHolder();
                holder.ivImage = convertView.findViewById(R.id.thumbnail_iv);
//                holder.chkImage = convertView.findViewById(R.id.chkImage);
                convertView.setTag(holder);
            }else{
                log.e("123213123");
            }

            if(!bScroll) {
                final ImageViewHolder holder = (ImageViewHolder) convertView.getTag();
                if (!bUpdate) {
                    holder.chkImage.setChecked(mMediaDataList.get(position).checked);

                    Picasso.get()
                            .load(mMediaDataList.get(position).contentUri)
                            //.load(mMediaDataList.get(position).getContentPath())
                            .resize(300, 300)
                            .centerCrop()
                            .into(holder.ivImage);

                    holder.ivImage.setVisibility(View.VISIBLE);
                } else {
                    holder.chkImage.setChecked(mMediaDataList.get(position).checked);
                }
            }
            return convertView;
        }

        private String getImageInfo(String ImageData, String Location, String thumbID){
            String imageDataPath = null;
            String[] proj = {MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.SIZE};
            Cursor imageCursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    proj, "_ID='"+ thumbID +"'", null, null);

            if (imageCursor != null && imageCursor.moveToFirst()){
                if (imageCursor.getCount() > 0){
                    int imgData = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    imageDataPath = imageCursor.getString(imgData);
                }
            }
            imageCursor.close();
            return imageDataPath;
        }
    }

    public void updateUI(){
        mListAdapter = new ImageAdapter(this, R.layout.image_cell, mMediaDataList);
        mGvImageList.setAdapter(mListAdapter);
    }

    private void cancelLoadMediaDataThread() {
        if (mLoadMediaDataThread != null) {
            mLoadMediaDataThread.cancel();
            mLoadMediaDataThread = null;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private class LoadMediaDataThread extends Thread {

        private boolean isCancelled = false;

        public void cancel() {
            isCancelled = true;
        }

        private Comparator<ImageResData> mMediaListDateComparator = (lhs, rhs) -> Long.compare(rhs.date, lhs.date);

        private Cursor getMediaCursor() {
            final String[] projection = {
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Images.Media.DISPLAY_NAME};

            return mContext.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    null);
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            mMediaDataList.clear();

//            ImageResData mediaData;
//            for(int i=0; i<60; i++) {
//                mediaData = new ImageResData();
//                mediaData._id = (long) i;
//                if(i%4 == 0){
//                    mediaData.contentPath = "https://t1.daumcdn.net/daumtop_chanel/op/20170315064553027.png";
//                }else {
//                    mediaData.contentPath = "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png";
//                }
//                mMediaDataList.add(mediaData);
//            }

            Cursor mediaCursor = getMediaCursor();
            if (mediaCursor != null) {
                mediaCursor.moveToLast();
                while (mediaCursor.moveToPrevious()) {
                    if (isCancelled) {
                        break;
                    }
                    ImageResData mediaData = new ImageResData();
                    mediaData._id = mediaCursor.getLong(mediaCursor.getColumnIndex(MediaStore.Files.FileColumns._ID));
                    mediaData.contentUri = Uri.withAppendedPath(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            mediaData._id.toString());

                    log.e("id: %d, uri: %s", mediaData._id, mediaData.contentUri.toString());

                    mMediaDataList.add(mediaData);
                }
            }
            // Collections.sort(mMediaDataList, mMediaListDateComparator);

            new DownloadImagesTask().execute();
        }
    }

    public class DownloadImagesTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            updateUI();
        }
    }
}
