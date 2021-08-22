package com.sugarmount.sugaralbum.test;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

import com.kiwiple.multimedia.canvas.Resolution;
import com.lguplus.pluscamera.api.MovieContentApi;
import com.lguplus.pluscamera.story.gallery.ConstantsGallery;
import com.lguplus.pluscamera.story.gallery.SelectManager;
import com.lguplus.pluscamera.story.movie.MovieEditMainActivity;
import com.sugarmount.sugaralbum.ImageResData;
import com.sugarmount.sugaralbum.R;
import com.sugarmount.sugaralbum.gridView.GridViewer;
import com.sugarmount.sugaralbum.slog;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener {
    GridView ImgGridView;
    GridAdapter gridAdapter;
    LoadMediaDataThread mLoadMediaDataThread;
    ImageView mIv, imageView1;
    Button mMovDiaryCreate;

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

        mMovDiaryCreate = (Button)findViewById(R.id.gogo);
        mMovDiaryCreate.setOnClickListener(view -> {
            ArrayList<com.lguplus.pluscamera.ImageResData> itemData = new ArrayList<>();

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

            for(int k=0; k<5; k++){
                com.lguplus.pluscamera.ImageResData tmp = new com.lguplus.pluscamera.ImageResData();
                ImageResData it = gridAdapter.getItem(k);
                tmp._id = it._id;
                tmp.checked = it.checked;
                tmp.date = it.date;
                tmp.contentPath = it.contentUri.toString();
                tmp.isVideo = it.isVideo;
                itemData.add(tmp);
            }

            MovieEditMainActivity.ERROR_HANDLER er = MovieContentApi.checkDataValidate(getApplicationContext(), itemData);

            switch (er) {
                case SUCCESS:
                    slog.e("SUCCESS");
                    Intent intent = new Intent(getApplicationContext(), MovieEditMainActivity.class);
                    intent.putExtra(SelectManager.SELECTED_VIDEOS, MovieContentApi.videoData);
                    intent.putExtra(SelectManager.SELECTED_IMAGES, MovieContentApi.photoData);
                    intent.putExtra(SelectManager.SELECTED_RESOLUTION, Resolution.NHD);
                    intent.putExtra(SelectManager.OUTPUT_DIR,
                            String.format("%s/%s", getApplicationContext().getExternalMediaDirs()[0].getPath(), getString(R.string.app_name)));
                    startActivityForResult(intent, ConstantsGallery.REQ_CODE_CONTENT_DETAIL);
                    break;
                case UNKNOWN_ERROR:
                    slog.e("UNKNOWN_ERROR");
                    break;
                case ITEM_COUNT:
                    slog.e("ITEM_COUNT");
                    break;
                case VIDEO_COUNT:
                    slog.e("VIDEO_COUNT");
                    break;
                case IMAGE_COUNT:
                    slog.e("IMAGE_COUNT");
                    break;
                case VIDEO_MAX_DURATION:
                    slog.e("VIDEO_MAX_DURATION");
                    break;
                case VIDEO_MIN_DURATION:
                    slog.e("VIDEO_MIN_DURATION");
                    break;
                case NOT_FOUND:
                    slog.e("NOT_FOUND");
                    break;
                case CODEC_ERROR:
                    slog.e("CODEC_ERROR");
                    break;
                default:
                    break;
            }
        });

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
        ArrayList<ImageResData> items = new ArrayList<>();
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
                    MediaStore.Images.Media.DISPLAY_NAME
            };

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
                mediaCursor.close();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (data != null) {
                MovieEditMainActivity.ERROR_HANDLER er = (MovieEditMainActivity.ERROR_HANDLER) data.getSerializableExtra(SelectManager.ERROR_CODE);

                switch (er) {
                    case SUCCESS:
                        String file_uri = (String) data.getSerializableExtra(SelectManager.FILE_URI);
                        slog.e("SUCCESS: " + file_uri);
                        
                        break;
                    case UNKNOWN_ERROR:
                        slog.e("UNKNOWN_ERROR");
                        break;
                    case ITEM_COUNT:
                        slog.e("ITEM_COUNT");
                        break;
                    case VIDEO_COUNT:
                        slog.e("VIDEO_COUNT");
                        break;
                    case IMAGE_COUNT:
                        slog.e("IMAGE_COUNT");
                        break;
                    case VIDEO_MAX_DURATION:
                        slog.e("VIDEO_MAX_DURATION");
                        break;
                    case VIDEO_MIN_DURATION:
                        slog.e("VIDEO_MIN_DURATION");
                        break;
                    case NOT_FOUND:
                        slog.e("NOT_FOUND");
                        break;
                    case CODEC_ERROR:
                        slog.e("CODEC_ERROR");
                        break;
                    default:
                        slog.e("??");
                        break;
                }
//                new test_v2().execute();
            }
        }catch(Exception e){
            slog.e("############### ex:" + e);
        }


    }
}