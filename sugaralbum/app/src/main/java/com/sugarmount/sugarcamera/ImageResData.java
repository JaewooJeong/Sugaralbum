package com.sugarmount.sugarcamera;

import android.net.Uri;

import java.io.Serializable;

/**
 * Created by Jaewoo on 2017-02-16.
 */
public class ImageResData implements Serializable {
    public Long _id;	// cursor id
    public String contentPath;
    public long date;
    public boolean isVideo;
    public boolean checked;
    public Uri contentUri;
    public Integer selectOrder;

    public Long get_id() {
        return _id;
    }

    public void set_id(Long _id) {
        this._id = _id;
    }

    public String getContentPath() {
        return contentPath;
    }

    public void setContentPath(String contentPath) {
        this.contentPath = contentPath;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public void setVideo(boolean video) {
        isVideo = video;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public Uri getContentUri() {
        return contentUri;
    }

    public void setContentUri(Uri contentUri) {
        this.contentUri = contentUri;
    }
}
