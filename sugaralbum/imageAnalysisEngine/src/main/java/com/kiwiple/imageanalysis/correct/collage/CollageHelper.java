
package com.kiwiple.imageanalysis.correct.collage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.kiwiple.imageanalysis.Global;
import com.kiwiple.imageanalysis.database.ImageData;

/**
 * 콜라주에 쓰일 이미지를 선택할 HelperClass
 */
public class CollageHelper {

    private static final int DEFAULT_YAW_RANGE = 10;

    public static final int CATEGORY_COLLAGE_PERSON_ID = 1;
    public static final int CATEGORY_COLLAGE_YAW = 2;
    public static final int CATEGORY_COLLAGE_FACE_PHOTO = 3;
    public static final int CATEGORY_COLLAGE_COLOR = 4;
    
    private CollageHelper() {
        
    }

    // ----------------------------------------------------------------------
    // -------------------------- 콜라주 그룹핑 관련 -----------------------------
    // ----------------------------------------------------------------------
    /**
     * inputImageDatas를 CATEGORY에 맞게 그룹화하고, 가장 빈도수가 많은 그룹을 반환한다.<br>
     * 그룹화에 실패하거나 그룹화할 수 없는 경우 null을 반환
     * 
     * @param inputImageDatas 그룹화할 이미지 데이터
     * @param category 그룹 카테고리 static valiable 참조
     * @return 각 그룹에서 최적화된 그룹화 배열
     */
    public static ArrayList<ImageData> getGroupImageDataWithCategory(
            ArrayList<ImageData> inputImageDatas, int category) {

        if(inputImageDatas == null || inputImageDatas.isEmpty()) {
            return null;
        }

        // 각 카테고리별 가장 배열이 큰(이미지가 많은) 그룹을 꺼낸다.
        ArrayList<ImageData> collgeImageDatas = null;
        if(category == CATEGORY_COLLAGE_PERSON_ID) {
            ArrayList<ArrayList<ImageData>> personGroupImageDatas = getImageDataGroupPersonId(inputImageDatas);
            ArrayList<ArrayList<ImageData>> sortedPersonGroupImageDatas = getSortSizeGroupImageData(personGroupImageDatas);
            if(sortedPersonGroupImageDatas != null && !sortedPersonGroupImageDatas.isEmpty()) {
                collgeImageDatas = new ArrayList<ImageData>(sortedPersonGroupImageDatas.get(0));
            }
        } else if(category == CATEGORY_COLLAGE_YAW) {
            ArrayList<ArrayList<ImageData>> yawGroupImageDatas = getImageDataGroupYaw(inputImageDatas,
                                                                                      DEFAULT_YAW_RANGE);
            ArrayList<ArrayList<ImageData>> sortedYawGroupImageDatas = getSortSizeGroupImageData(yawGroupImageDatas);
            if(sortedYawGroupImageDatas != null && !sortedYawGroupImageDatas.isEmpty()) {
                collgeImageDatas = new ArrayList<ImageData>(sortedYawGroupImageDatas.get(0));
            }
        } else if(category == CATEGORY_COLLAGE_FACE_PHOTO) {
            ArrayList<ImageData> facePhotoImageDatas = getImageDataGroupFacePhoto(inputImageDatas);
            if(facePhotoImageDatas != null && !facePhotoImageDatas.isEmpty()) {
                return facePhotoImageDatas;
            }
        } else if(category == CATEGORY_COLLAGE_COLOR) {
            ArrayList<ArrayList<ImageData>> colorGroupImageDatas = getImageDataGroupRepresentColorName(inputImageDatas);
            ArrayList<ArrayList<ImageData>> sortedColorGroupImageDatas = getSortSizeGroupImageData(colorGroupImageDatas);
            if(sortedColorGroupImageDatas != null && !sortedColorGroupImageDatas.isEmpty()) {
                collgeImageDatas = new ArrayList<ImageData>(sortedColorGroupImageDatas.get(0));
            }
        }

        return collgeImageDatas;
    }

    /**
     * 파라미터로 주어진 이미지 후보군에서 Yaw값을 범위(range)로 묶어 그룹화한다. <br>
     * 값의 범위가 10이라면, -90 ~ 90 사이의 값을 10단위로 끊어 각기 그룹화한다. <br>
     * 단체사진 (numberOfFaces가 2이상)인 경우 모든 그룹에서 제외된다. <br>
     * 
     * @param inputImageDatas 이미지 후보군
     * @param range 값의 범위 (0이하 일 경우 기본값 10으로 설정)
     * @return 각 그룹을 인자로 가지고 있는 이중 배열
     */
    public static ArrayList<ArrayList<ImageData>> getImageDataGroupYaw(
            ArrayList<ImageData> inputImageDatas, int range) {

        if(inputImageDatas == null || inputImageDatas.isEmpty()) {
            return null;
        }

        if(range < 1) {
            range = 10;
        }

        // 범위에 따라 그룹이 몇개가 될지 계산
        // 총 범위는 -90~90까지 180이다.
        int rangeValue = 180;
        int groupCount = rangeValue / range;
        if(rangeValue % range > 0) {
            groupCount += 1;
        }

        ArrayList<ArrayList<ImageData>> groupImageData = new ArrayList<ArrayList<ImageData>>();
        // 각 범위의 최대 최소값을 구하고 범위 내의 이미지를 넣는다.
        for(int i = 0; i < groupCount; i++) {
            // 값의 절대값 범위
            int rangeMinValue = -90 + i * range;
            ArrayList<ImageData> rangeImageDatas = new ArrayList<ImageData>();
            for(int j = 0; j < inputImageDatas.size(); j++) {
                ImageData imageData = inputImageDatas.get(j);
                // 단독 사진만 체크한다.
                if(imageData.numberOfFace == 1 && imageData.faceDataItems != null
                        && !imageData.faceDataItems.isEmpty()) {
                    int yawValue = imageData.faceDataItems.get(0).yaw;
                    if(yawValue >= rangeMinValue && yawValue < rangeMinValue + range) {
                        rangeImageDatas.add(imageData);
                    }
                }
            }

            // 최소 2장이상 필요하다.
            if(rangeImageDatas.size() > 1) {
                groupImageData.add(rangeImageDatas);
            }
        }

        return groupImageData;
    }

    /**
     * 파라미터로 주어진 이미지 후보군에서 같은 인물(personId)끼리 묶어 그룹화한다. <br>
     * 단체사진 (numberOfFaces가 2이상)인 경우 각 그룹에 모두 포함된다. <br>
     * 
     * @param inputImageDatas 이미지 후보군
     * @return 각 그룹을 인자로 가지고 있는 이중 배열
     */
    public static ArrayList<ArrayList<ImageData>> getImageDataGroupPersonId(
            ArrayList<ImageData> inputImageDatas) {

        if(inputImageDatas == null || inputImageDatas.isEmpty()) {
            return null;
        }

        // 그룹화된 personId가 어느 인덱스에 들어가있는지 판단하기 위한 배열
        ArrayList<Integer> groupPersonIdValueArray = new ArrayList<Integer>();
        ArrayList<ArrayList<ImageData>> groupImageDataArray = new ArrayList<ArrayList<ImageData>>();
        for(int i = 0; i < inputImageDatas.size(); i++) {
            ImageData imageData = inputImageDatas.get(i);
            if(imageData.faceDataItems != null && !imageData.faceDataItems.isEmpty()) {
                for(int j = 0; j < imageData.faceDataItems.size(); j++) {
                    ArrayList<ImageData> groupImageData = new ArrayList<ImageData>();
                    // personId가 유효한 값인지 체크
                    int personId = imageData.faceDataItems.get(j).personId;
                    if(personId != -111) {
                        // personId가 이미 분류되어 있는지 체크
                        int groupIndex = groupPersonIdValueArray.indexOf(personId);
                        if(groupIndex != -1) {
                            // 이미 존재함
                            groupImageDataArray.get(groupIndex).add(imageData);
                        } else {
                            // 존재하지 않음 처음보이는 groupId
                            groupPersonIdValueArray.add(personId);
                            groupImageData.add(imageData);
                        }
                    }
                    if(!groupImageData.isEmpty()) {
                        groupImageDataArray.add(groupImageData);
                    }
                }
            }
        }

        return groupImageDataArray;
    }

    /**
     * 파라미터로 주어진 이미지 후보군에서 인물 사진만 추려서 반환한다. <br>
     * 
     * @param inputImageDatas 이미지 후보군
     * @return 이미지 후보군에서 인물사진만 묶은 배열
     */
    public static ArrayList<ImageData> getImageDataGroupFacePhoto(
            ArrayList<ImageData> inputImageDatas) {

        if(inputImageDatas == null || inputImageDatas.isEmpty()) {
            return null;
        }

        ArrayList<ImageData> groupImageDataArray = new ArrayList<ImageData>();
        // 각 범위의 최대 최소값을 구하고 범위 내의 이미지를 넣는다.
        for(int i = 0; i < inputImageDatas.size(); i++) {
            ImageData imageData = inputImageDatas.get(i);
            if(imageData.faceDataItems != null && !imageData.faceDataItems.isEmpty()) {
                groupImageDataArray.add(imageData);
            }
        }

        return groupImageDataArray;
    }

    /**
     * 파라미터로 주어진 이미지 후보군에서 같은 대표색(representColorName)끼리 묶어 그룹화한다. <br>
     * 
     * @param inputImageDatas 이미지 후보군
     * @return 각 그룹을 인자로 가지고 있는 이중 배열
     */
    public static ArrayList<ArrayList<ImageData>> getImageDataGroupRepresentColorName(
            ArrayList<ImageData> inputImageDatas) {

        if(inputImageDatas == null || inputImageDatas.isEmpty()) {
            return null;
        }

        // 그룹화된 대표색(representColorName)이 어느 인덱스에 들어가있는지 판단하기 위한 배열
        ArrayList<String> groupColorNameValueArray = new ArrayList<String>();
        ArrayList<ArrayList<ImageData>> groupImageDataArray = new ArrayList<ArrayList<ImageData>>();
        for(int i = 0; i < inputImageDatas.size(); i++) {
            String representColorName = inputImageDatas.get(i).representColorName;
            ArrayList<ImageData> groupImageData = new ArrayList<ImageData>();
            if(!Global.isNullString(representColorName)) {
                // 대표색이 있다면 분류하자
                int groupIndex = groupColorNameValueArray.indexOf(representColorName);
                if(groupIndex != -1) {
                    // 이미 존재함
                    groupImageDataArray.get(groupIndex).add(inputImageDatas.get(i));
                } else {
                    // 존재하지 않음 처음보이는 groupId
                    groupColorNameValueArray.add(representColorName);
                    groupImageData.add(inputImageDatas.get(i));
                }
            }

            if(!groupImageData.isEmpty()) {
                groupImageDataArray.add(groupImageData);
            }
        }

        return groupImageDataArray;
    }

    /**
     * 이중 배열 내에서 인자요소인 배열의 크기가 큰 순서로 소팅하는 메소드
     * 
     * @param inputGroupImageDatas 정렬할 2중 배열
     * @return ArrayList 내부 배열 Size가 큰 순서로 정렬된 2중 배열
     */
    public static ArrayList<ArrayList<ImageData>> getSortSizeGroupImageData(
            ArrayList<ArrayList<ImageData>> inputGroupImageDatas) {

        if(inputGroupImageDatas == null || inputGroupImageDatas.isEmpty()) {
            return null;
        }

        ArrayList<ArrayList<ImageData>> sortGroupImageData = new ArrayList<ArrayList<ImageData>>(
                                                                                                 inputGroupImageDatas);
        Comparator<ArrayList<ImageData>> comparator = new Comparator<ArrayList<ImageData>>() {
            @Override
            public int compare(ArrayList<ImageData> lhs, ArrayList<ImageData> rhs) {
                int lhsCount = lhs == null || lhs.isEmpty() ? 0
                        : lhs.size();
                int rhsCount = rhs == null || rhs.isEmpty() ? 0
                        : rhs.size();

                if(lhsCount < rhsCount) {
                    return 1;
                } else if(lhsCount == rhsCount) {
                    return 0;
                } else {
                    return -1;
                }
            }
        };
        Collections.sort(sortGroupImageData, comparator);
        return sortGroupImageData;
    }
}
