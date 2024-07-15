package com.sugarmount.common.listener;


import com.sugarmount.common.env.MvConfig;

/**
 * Created by Jaewoo on 2017-08-29.
 */
public interface DataClickEventListener {
    void onClickEvent(MvConfig.PAGE_DIRECTION page_direction, int position, boolean select);
}
