package com.sugarmount.sugaralbum.test;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.sugarmount.sugaralbum.ImageResData;
import com.sugarmount.sugaralbum.R;
import com.sugarmount.sugaralbum.gridView.GridViewer;
import com.sugarmount.sugaralbum.slog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener {
    GridView ImgGridView;
    GridAdapter gridAdapter;
    LoadMediaDataThread mLoadMediaDataThread;
    ImageView mIv, imageView1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        imageView1 = findViewById(R.id.imageView1);

        ImgGridView = findViewById(R.id.ImgGridView);
        ImgGridView.setOnItemClickListener(this);
        gridAdapter = new GridAdapter();

        mLoadMediaDataThread = new LoadMediaDataThread();
        mLoadMediaDataThread.start();

//        ImageResData mediaData;
//        for(int i=0; i<1000; i++) {
//            mediaData = new ImageResData();
//            mediaData._id = (long) i;
//            if(i%4 == 0){
//                mediaData.contentPath = "https://t1.daumcdn.net/daumtop_chanel/op/20170315064553027.png";
//            }else {
//                mediaData.contentPath = "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_92x30dp.png";
//            }
//            gridAdapter.addItem(mediaData);
//        }
//
//        ImgGridView.setAdapter(gridAdapter);
    }

    @SuppressLint("ResourceAsColor")
    private void onSetColorFilter(ImageView iv, boolean b){
        if(b) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                iv.setColorFilter(new BlendModeColorFilter(R.color.transparent_, BlendMode.DST_IN));
            } else {
                iv.setColorFilter(R.color.transparent_, PorterDuff.Mode.DST_IN);
            }
        }else{
            if(iv != null)
                iv.setColorFilter(null);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

        // 이전 선택 cell filter 제외
        onSetColorFilter(mIv, false);

        ImageResData rowData = gridAdapter.getItem(position);
        rowData.setChecked(!rowData.checked);

        // cell 선택
        mIv = arg1.findViewById(R.id.ivImage);
        onSetColorFilter(mIv, true);

//        slog.e("onItemClick: %s, %s", rowData.getContentPath(), rowData.getContentUri().toString());
    }

    class GridAdapter extends BaseAdapter {
        ArrayList<ImageResData> items = new ArrayList<ImageResData>();
        @Override
        public int getCount() {
            return items.size();
        }

        public void addItem(ImageResData ird){
            items.add(ird);
        }

        public ArrayList<ImageResData> getList(){
            return items;
        }

        @Override
        public ImageResData getItem(int i) {
            return items.get(i);
        }

        @Override
        public long getItemId(int i) {
            imageView1.setImageURI(gridAdapter.getItem(i).contentUri);
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            GridViewer sv = new GridViewer(getApplicationContext());
            sv.setItem(items.get(i));
            return sv;
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

            return getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    null);
        }

        @Override
        public void run() {
            gridAdapter.getList().clear();

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

//                    slog.e("id: %d, uri: %s", mediaData._id, mediaData.contentUri.toString());

                    gridAdapter.addItem(mediaData);
                }
            }
            new DownloadImagesTask().execute();
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class DownloadImagesTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            ImgGridView.setAdapter(gridAdapter);
            if(gridAdapter.getCount() > 0 ){
                ImgGridView.setSelection(0);
                imageView1.setImageURI(gridAdapter.getItem(0).contentUri);
            }
        }
    }

}