package com.sugarmount.sugarcamera;

import android.net.Uri;

import com.kiwiple.imageanalysis.database.ImageData;

import java.util.ArrayList;

/**
 * Created by Jaewoo on 2017-02-15.
 */
public class DiaryInfo {

    public ArrayList<ImageData> imageDataArrayList;
    public ArrayList<ImageData> videoDataArrayList;
    public String title;
    public Uri jsonDataUri;
    public JSONTYPE jsonType;
    public enum JSONTYPE {
        PREVIEW,
        NEW
    }

    public ArrayList<ImageData> getImageDataArrayList() {
        return imageDataArrayList;
    }

    public void setImageDataArrayList(ArrayList<ImageData> imageDataArrayList) {
        this.imageDataArrayList = imageDataArrayList;
    }

    public ArrayList<ImageData> getVideoDataArrayList() {
        return videoDataArrayList;
    }

    public void setVideoDataArrayList(ArrayList<ImageData> videoDataArrayList) {
        this.videoDataArrayList = videoDataArrayList;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Uri getJsonDataUri() {
        return jsonDataUri;
    }

    public void setJsonDataUri(Uri jsonDataUri) {
        this.jsonDataUri = jsonDataUri;
    }

    public JSONTYPE getJsonType() {
        return jsonType;
    }

    public void setJsonType(JSONTYPE jsonType) {
        this.jsonType = jsonType;
    }
}
