package com.sugarmount.sugaralbum;

import android.net.Uri;
import lombok.Data;

/**
 * Created by Jaewoo on 2021-08-30.
 */
@Data
public class ImageResData {
    public Long _id;	// cursor id
    public long date;
    public boolean isVideo;
    public boolean checked;
    public Uri contentUri;

    public Long get_id() {
        return _id;
    }

    public void set_id(Long _id) {
        this._id = _id;
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
