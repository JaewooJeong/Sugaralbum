package com.sugarmount.sugarcamera;

import com.sugarmount.sugarcamera.story.gallery.RULES;

/**
 * Created by Jaewoo on 2017-02-22.
 */
public class ResInfo {

    public ResInfo(){
        RULES.init();
    }

    public int photoMaxCount = 0;
    public int videoMaxCount = 0;
    public long videoMinDuration = RULES.MIN_DURATION_PER_VIDEO;
    public long videoMaxDuration = RULES.MAX_DURATION_PER_VIDEO;
    public int videoMaxWidth = RULES.FULL_HD_WIDTH;
    public int videoMaxHeight = RULES.FULL_HD_HEIGHT;
    public Long videoMaxSize = Long.MAX_VALUE;


    public int getPhotoMaxCount() {
        return photoMaxCount = RULES.getMakeClipMaxPhotoCount();
    }

    public int getVideoMaxCount() {
        return videoMaxCount = RULES.getMakeClipMaxVideoCount();
    }

    public long getVideoMinDuration() {
        return videoMinDuration;
    }

    public long getVideoMaxDuration() {
        return videoMaxDuration;
    }

    public int getVideoMaxWidth() {
        return videoMaxWidth;
    }

    public int getVideoMaxHeight() {
        return videoMaxHeight;
    }

    public Long getVideoMaxSize() {
        return videoMaxSize;
    }

}
