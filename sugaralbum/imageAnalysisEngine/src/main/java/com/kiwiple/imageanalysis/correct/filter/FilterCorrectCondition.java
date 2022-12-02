
package com.kiwiple.imageanalysis.correct.filter;

/**
 * 필터 보정 데이터에 대한 조건들을 설정한다.<br>
 * 필터 보정 조건은 다음과 같다.<br>
 * 1.필터 미적용 (DEFAULT_CORRECT_NONE) <br>
 * 2.전체 사진에 1가지 필터 적용 (DEFAULT_CORRECT_ENTIRE_ONE) <br>
 * 3.각 사진별 필터 적용 (DEFAULT_CORRECT_EACH) <br>
 * 기본적으로 1.(필터 미적용)이 설정
 */
public class FilterCorrectCondition {

    /**
     * 필터 미적용
     */
    public static final int FILTER_CORRECT_DEFAULT_NONE = 1;
    /**
     * 전체 사진에 1가지 필터 적용
     */
    public static final int FILTER_CORRECT_DEFAULT_ENTIRE_ONE = 2;
    /**
     * 각 사진별 필터 적용
     */
    public static final int FILTER_CORRECT_DEFAULT_EACH = 3;

    private int mFilterCorrectDefaultCondition = FILTER_CORRECT_DEFAULT_NONE;

    /**
     * 필터 보정 조건에 생성자
     * 
     * @param filterCorrectDefaultCondition 기본 조건 설정
     */
    public FilterCorrectCondition(int filterCorrectDefaultCondition) {
        if(filterCorrectDefaultCondition < FILTER_CORRECT_DEFAULT_NONE
                || filterCorrectDefaultCondition > FILTER_CORRECT_DEFAULT_EACH) {
            mFilterCorrectDefaultCondition = FILTER_CORRECT_DEFAULT_NONE;
        } else {
            mFilterCorrectDefaultCondition = filterCorrectDefaultCondition;
        }
    }

    /**
     * 설정된 필터 보정 기본 조건을 반환한다.
     * 
     * @return int 기본 조건 타입 값
     */
    public int getDefaultCorrectCondition() {
        return mFilterCorrectDefaultCondition;
    }
}
