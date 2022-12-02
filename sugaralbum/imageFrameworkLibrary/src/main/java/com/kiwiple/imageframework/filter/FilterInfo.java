
package com.kiwiple.imageframework.filter;

/**
 * 필터 정보
 * 
 * @version 1.0
 */
public class FilterInfo {
    private String filterTitle;
    private String filterDesc;
    private int filterId;

    FilterInfo(String filterTitle, String filterDesc, int filterId) {
        super();
        this.filterTitle = filterTitle;
        this.filterDesc = filterDesc;
        this.filterId = filterId;
    }

    /**
     * @return 필터의 제목
     * @version 1.0
     */
    public String getFilterTitle() {
        return filterTitle;
    }

    /**
     * @return 필터 내용
     * @version 1.0
     */
    public String getFilterDesc() {
        return filterDesc;
    }

    /**
     * @return 필터 id
     * @version 1.0
     */
    public int getFilterId() {
        return filterId;
    }
}
