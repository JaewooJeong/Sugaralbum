
package com.kiwiple.imageframework.gpuimage;

import java.util.ArrayList;

/**
 * 필터 정보
 */
public class ArtFilterInfo {
    /**
     * 필터 이름
     */
    public String filterName;
    /**
     * 필터가 가지는 파라미터 값들
     */
    public ArrayList<ProgressInfo> progressInfo;
    /**
     * 필터카메라용 인앱 아이디
     */
    public String tstoreId = "";
    public String ollehId = "";
    public String playId = "";
    public boolean customFilter;

    public ArtFilterInfo(String filterName, ArrayList<ProgressInfo> progressInfo) {
        super();
        this.filterName = filterName;
        this.progressInfo = progressInfo;
        customFilter = false;

    }

    public ArtFilterInfo(String filterName, ArrayList<ProgressInfo> progressInfo, String tstoreId,
            String ollehId, String playId) {
        super();
        this.filterName = filterName;
        this.progressInfo = progressInfo;
        this.tstoreId = tstoreId;
        this.ollehId = ollehId;
        this.playId = playId;
        customFilter = true;
    }
}
