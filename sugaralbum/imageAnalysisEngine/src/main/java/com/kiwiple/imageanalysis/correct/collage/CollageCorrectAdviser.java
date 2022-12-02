
package com.kiwiple.imageanalysis.correct.collage;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextUtils;

import com.kiwiple.imageanalysis.correct.ImageCorrectData;
import com.kiwiple.imageanalysis.database.FacePointF;
import com.kiwiple.imageanalysis.database.ImageData;
import com.kiwiple.imageanalysis.utils.SmartLog;
import com.kiwiple.imageframework.collage.CollageView;
import com.kiwiple.imageframework.collage.DesignTemplateManager;
import com.kiwiple.imageframework.collage.TemplateInfo;

/**
 * 콜라주 추천을 위한 클래스 <br>
 * <br>
 * 1. 받아온 이미지 배열에서 콜라주로 쓰일 사진을 조건에 맞춰 배열을 재구성한다.<br>
 * 2. 프레임 갯수를 입력받아 이미지에서 보여질 영역을 예측하여 적절한 배치를 이룰 수 있는 프레임을 결정한다. <br>
 * 3. 결정된 프레임에 대한 CorrectData를 이미지 배열에 추가하고, 적절한 배치의 순서로 이미지 배열 순서를 바꾼다.
 */
public class CollageCorrectAdviser {

    private static final String TAG = CollageCorrectAdviser.class.getSimpleName();
    private static final int COLLAGE_DEFAULT_IMAGE_SIZE = 1200;

    private static final String THEME_NAME_CLEAN = "Clean";

    private Context mApplicationContext;

    private int mCollageImageWidth = 0;
    private int mCollageImageHeight = 0;

    /**
     * 콜라주 추천 생성자
     * 
     * @param applicationContext ApplicationContext
     * @param imageDatas 콜라주를 이루기 위한 후보군 이미지 데이터 배열
     */
    public CollageCorrectAdviser(Context applicationContext) {
        mApplicationContext = applicationContext;
        mCollageImageWidth = COLLAGE_DEFAULT_IMAGE_SIZE;
    }

    /**
     * 해당 테마에 맞는 템플릿 배열을 반환한다.
     * 
     * @param theme 테마에 맞는 템플릿 배열
     * @return 해당 조건에 맞는 템플릿 배열
     */
    public ArrayList<TemplateInfo> getTemplateArrayForTheme(String theme) {
        return DesignTemplateManager.getInstance(mApplicationContext)
                                    .getThemeTemplateArrayByName(theme);
    }

    /**
     * 특정 테마의 특정 프레임 갯수 배열을 반환한다.
     * 
     * @param theme 테마 이름
     * @param frameCount 프레임 갯수
     * @return 해당 조건에 맞는 템플릿 배열
     */
    public ArrayList<TemplateInfo> getTemplateArrayForThemeAndFrameCount(String theme,
            int frameCount) {
        return DesignTemplateManager.getInstance(mApplicationContext).getTemplateArray(frameCount,
                                                                                       theme);
    }

    /**
     * 해당 테마에 맞는 템플릿 배열을 반환한다.
     * 
     * @param theme 테마에 맞는 템플릿 배열
     * @return 해당 조건에 맞는 템플릿 배열
     */
    public ArrayList<TemplateInfo> getTemplateArrayForTheme(String theme, boolean isSingleFrame) {
        ArrayList<TemplateInfo> themeTemlateArray = DesignTemplateManager.getInstance(mApplicationContext)
                                                                         .getThemeTemplateArrayByName(theme);
        ArrayList<TemplateInfo> templateArray = new ArrayList<TemplateInfo>();
        for(TemplateInfo templateInfo : themeTemlateArray) {
            if(isSingleFrame) {
                if(theme.equals(templateInfo.getTheme()) && templateInfo.getFrameCount() == 1) {
                    templateArray.add(templateInfo);
                }
            } else {
                if(theme.equals(templateInfo.getTheme()) && templateInfo.getFrameCount() != 1) {
                    templateArray.add(templateInfo);
                }
            }
        }
        return templateArray;
    }

    // ----------------------------------------------------------------------
    // -------------------------- 콜라주 프로세싱 관련 ---------------------------
    // ----------------------------------------------------------------------

    /**
     * 다음과 같은 로직으로 콜라주를 추천하여 각 콜라주를 하나의 배열로 묶어서 반환한다.<br>
     * 1. 입력받은 필요 콜라주 갯수를 3등분하여 personId, yaw, color 카테고리의 콜라주의 갯수로 할당.<br>
     * 2. personId로 그룹화하여 가장 많은 인물의 그룹에서 랜덤으로 추출하여 콜라주를 생성한다.<br>
     * 3. 2.에서 사용된 이미지를 제외하고 yaw값으로 그룹화하여 가장 많은 그룹의 이미지를 받아 랜덤으로 추출하여 콜라주를 생성.<br>
     * 4. 2~3에서 사용된 이미지를 제외하고 대표색상으로 그룹화하여 가장 많은 그룹의 이미지를 받아 랜덤으로 추출하여 콜라주를 생성.<br>
     * 5. 필요 콜라주 갯수가 3개이상이라면 2~4를 반복한다.<br>
     * 6. 이를 통해 필요 콜라주 갯수만큼 콜라주를 생성하되, 만일 필요 콜라주 갯수만큼 생성되지 않았다면 인물로 그룹을 하여 콜라주를 생성한다.<br>
     * 7. 6.의 과정을 통해서도 콜라주가 충분하지 않다면, 만들어진 콜라주 배열을 그대로 묶어 반환. 8. 2~6의 과정을 통해 충분한 콜라주 배열이 완성되었다면 그대로
     * 묶어서 반환.
     * 
     * @param inputImageDatas 이미지 데이터 배열
     * @param collageCount 필요 콜라주 갯수
     * @param themeName 테마 이름
     * @param imageWidth 추천받을 콜라주의 가로 길이
     * @return ArrayList collageCorrectData가 입력된 이미지 데이터 배열
     */
    public ArrayList<ArrayList<ImageData>> getCollageCorrectData(
            ArrayList<ImageData> inputImageDatas, int collageCount, String themeName, int imageWidth) {
        if(inputImageDatas == null || inputImageDatas.isEmpty() || collageCount < 1
                || imageWidth < 0) {
            return null;
        }

        if(TextUtils.isEmpty(themeName)) {
            themeName = THEME_NAME_CLEAN;
        }

        mCollageImageWidth = imageWidth;

        // 남은 필요 콜라주 갯수
        int remainCollageCount = collageCount;

        // 입력받은 이미지 데이터 배열
        ArrayList<ImageData> imageDatas = new ArrayList<ImageData>(inputImageDatas);

        // 각 콜라주별 배열 (최종 output)
        ArrayList<ArrayList<ImageData>> collageImageDatas = new ArrayList<ArrayList<ImageData>>();

        // 필요 콜라주 갯수에 따라 각 그룹에서 몇개정도가 최적일지 구해보자.
        // personId, Yaw, Color를 균등하게 뽑자. (우선 순위는 personId, Yaw, Color순서가 됨)
        int perCategoryCollageCount = remainCollageCount / 3;
        int personIdCollageCount = remainCollageCount % 3 > 0 ? perCategoryCollageCount + 1
                : perCategoryCollageCount;
        int yawCollageCount = remainCollageCount % 3 > 1 ? perCategoryCollageCount + 1
                : perCategoryCollageCount;
        int colorCollageCount = perCategoryCollageCount;

        // 각 카테고리로 더이상 뽑을 수 없는지 체크한다.
        // 전체 카테고리 체크
        boolean isCategoryCollageFinish = false;
        // 각 카테고리별 체크
        boolean isPersonIdCollageFinish = false;
        boolean isYawCollageFinish = false;
        boolean isColorCollageFinish = false;

        // 콜라주 카운트가 소모되거나 입력받은 이미지 데이터 배열이 소모될 경우 종료한다.
        while(!isCategoryCollageFinish) {
            // 우선 순위에 따라 personId 콜라주를 생성
            if(!isPersonIdCollageFinish) {
                if(personIdCollageCount > 0 && remainCollageCount > 0) {
                    ArrayList<ImageData> personIdCollageImageDatas = getCollageImageDatasWithCategory(imageDatas,
                                                                                                      CollageHelper.CATEGORY_COLLAGE_PERSON_ID,
                                                                                                      themeName);
                    if(personIdCollageImageDatas != null && !personIdCollageImageDatas.isEmpty()) {
                        // 추천 콜라주가 있다면 콜라주 배열에 추가하고
                        collageImageDatas.add(personIdCollageImageDatas);
                        // 필요 카운트를 줄여준 후,
                        personIdCollageCount -= 1;
                        remainCollageCount -= 1;
                        // 입력받은 이미지 데이터가 중복되지 않도록 처리
                        removeDuplicateArrayData(imageDatas, personIdCollageImageDatas);
                    } else {
                        // 해당 카테고리로 추천 콜라주가 없다면 카운트를 0으로 내리고
                        personIdCollageCount = 0;
                        isPersonIdCollageFinish = true;
                    }
                } else {
                    isPersonIdCollageFinish = true;
                }
            }

            // 우선 순위에 따라 yaw 콜라주 생성
            if(!isYawCollageFinish) {
                if(yawCollageCount > 0 && remainCollageCount > 0) {
                    ArrayList<ImageData> yawCollageImageDatas = getCollageImageDatasWithCategory(imageDatas,
                                                                                                 CollageHelper.CATEGORY_COLLAGE_YAW,
                                                                                                 themeName);
                    if(yawCollageImageDatas != null && !yawCollageImageDatas.isEmpty()) {
                        // 추천 콜라주가 있다면 콜라주 배열에 추가
                        collageImageDatas.add(yawCollageImageDatas);
                        // 필요 카운트 감소 시키고
                        yawCollageCount -= 1;
                        remainCollageCount -= 1;
                        // 입력받은 이미지에서 데이터가 중복되지 않도록 처리
                        removeDuplicateArrayData(imageDatas, yawCollageImageDatas);
                    } else {
                        // 해당 카테고리로 추천 콜라주가 없다면 카운트를 0으로 내리고
                        yawCollageCount = 0;
                        isYawCollageFinish = true;
                    }
                } else {
                    isYawCollageFinish = true;
                }
            }

            // 우선 순위에 따라 color 콜라주 생성
            if(!isColorCollageFinish) {
                if(colorCollageCount > 0 && remainCollageCount > 0) {
                    ArrayList<ImageData> colorCollageImageDatas = getCollageImageDatasWithCategory(imageDatas,
                                                                                                   CollageHelper.CATEGORY_COLLAGE_COLOR,
                                                                                                   themeName);
                    if(colorCollageImageDatas != null && !colorCollageImageDatas.isEmpty()) {
                        // 추천 콜라주가 있다면 콜라주 배열에 추가
                        collageImageDatas.add(colorCollageImageDatas);
                        // 필요 카운트 감소 시키고
                        colorCollageCount -= 1;
                        remainCollageCount -= 1;
                        // 입력받은 이미지에서 데이터가 중복되지 않도록 처리
                        removeDuplicateArrayData(imageDatas, colorCollageImageDatas);
                    } else {
                        // 해당 카테고리로 추천 콜라주가 없다면 카운트를 0으로 내리고
                        colorCollageCount = 0;
                        isColorCollageFinish = true;
                    }
                } else {
                    isColorCollageFinish = true;
                }
            }

            // 전체 카테고리에서 전부 추출했는지 체크
            if(isPersonIdCollageFinish && isYawCollageFinish && isColorCollageFinish) {
                isCategoryCollageFinish = true;
            }
        }

        // 3개의 추천 콜라주가 완성되었으나, 필요 콜라주만큼 채워지지 않았다면
        // 인물에서 콜라주를 생성해주어야한다.
        boolean isFaceCollageFinish = false;
        while(!isFaceCollageFinish) {
            // 남아있는 콜라주가 있다면
            if(remainCollageCount > 0 && remainCollageCount < collageImageDatas.size()) {
                ArrayList<ImageData> faceCollageImageData = getCollageImageDatasWithCategory(imageDatas,
                                                                                             CollageHelper.CATEGORY_COLLAGE_FACE_PHOTO,
                                                                                             themeName);
                if(faceCollageImageData != null && !faceCollageImageData.isEmpty()) {
                    // 추천 콜라주가 있다면 콜라주 배열에 추가
                    collageImageDatas.add(faceCollageImageData);
                    // 필요 카운트를 감소
                    remainCollageCount -= 1;
                    // 입력받은 이미지에서 데이터가 중복되지 않도록 처리
                    removeDuplicateArrayData(imageDatas, faceCollageImageData);
                } else {
                    // 인물 콜라주마저 나오지 않음.. 따라서 종료
                    isFaceCollageFinish = true;
                }
            } else {
                // 이미 콜라주가 완성되어있다면 바로 넘어가자.
                isFaceCollageFinish = true;
            }
        }

        return collageImageDatas;
    }

    private void removeDuplicateArrayData(ArrayList<ImageData> targetArray,
            ArrayList<ImageData> usedArray) {

        if(targetArray != null && usedArray != null) {
            for(int i = 0; i < usedArray.size(); i++) {
                targetArray.remove(usedArray.get(i));
            }
        }
    }

    /**
     * input 이미지 데이터를 특정 템플릿으로 매칭시켜 추천 이동 범위를 구한다.<br>
     * 주어지는 templateId의 프레임 갯수와 이미지 데이터 갯수가 일치해야한다.<br>
     * <br>
     * 이미지 데이터가 유효하지 않거나, 템플릿 아이디가 유효하지 않거나,<br>
     * 프레임 갯수와 이미지 갯수가 동일하지 않을 시 null을 반환.
     * 
     * @param inputImageDatas 이미지 데이터 배열
     * @param templateId 특정 템플릿 고유 ID
     * @param collageWidth 추천 콜라주가 될 가로 길이 (이 길이에 따라 collageFrame 크기가 결정됨)
     * @return parameter인 이미지 데이터 배열 내에 추천 이동 위치가 포함되어 반환
     */
    public ArrayList<ImageData> getCollageImageDatasWithTemplateId(
            ArrayList<ImageData> inputImageDatas, int templateId, int collageWidth) {

        if(inputImageDatas == null || inputImageDatas.isEmpty() || collageWidth < 1) {
            initCollageData(inputImageDatas);
            return inputImageDatas;
        }

        // 템플릿 id로 템플릿 정보를 가져온다.
        TemplateInfo templateInfo = DesignTemplateManager.getInstance(mApplicationContext)
                                                         .getTemplateInfo(templateId);
        if(templateInfo == null) {
            // 템플릿이 유효하지 않다면
            initCollageData(inputImageDatas);
            return inputImageDatas;
        }

        // 콜라주의 길이
        int collageHeight = (int)(collageWidth * templateInfo.getAspectRatio());

        // 각 프레임 정보를 얻기 위한 콜라주 생성
        CollageView collageView = new CollageView(mApplicationContext, templateInfo, collageWidth,
                                                  collageHeight);
        collageView.setFrameImageScale(1.f, 5.f);

        // 프레임 정보를 추출
        ArrayList<Rect> frameBounds = collageView.getFrameBounds();
        ArrayList<ArrayList<RectF>> faceBounds = collageView.getFaceBounds();

        if(frameBounds == null || frameBounds.isEmpty()
                || inputImageDatas.size() != frameBounds.size()) {
            initCollageData(inputImageDatas);
            return inputImageDatas;
        }

        // 각 프레임 별로 추천 이동 범위를 판단
        for(int i = 0; i < frameBounds.size(); i++) {
            // 이미지 데이터
            ImageData imageData = inputImageDatas.get(i);
            if(imageData.imageCorrectData == null) {
                imageData.imageCorrectData = new ImageCorrectData();
            }

            // 콜라주의 프레임은 좌표가 0으로 초기화 되어야함
            Rect collageFrame = frameBounds.get(i);
            RectF collageBound = new RectF(0.f, 0.f, collageFrame.width(), collageFrame.height());

            PointF translatePoint = null;
            if(faceBounds.get(i).isEmpty()) {
                // 권장 이동 값이 있을 경우에만 해당 프레임에 적절한 배치 가능함
                translatePoint = getAutoMoveValueFromImage(imageData, collageBound, null);
            } else {
                // 콜라주 템플릿에 정의된 얼굴 영역이 있을 때.
                for(RectF bounds : faceBounds.get(i)) {
                    translatePoint = getAutoMoveValueFromImage(imageData, collageBound, bounds);
                    if(translatePoint != null) {
                        // 권장 이동 값이 있을 경우에만 해당 프레임에 적절한 배치 가능함
                        translatePoint = getAutoMoveValueFromImage(imageData, collageBound, null);
                        // 얼굴 영역에 매치되면 다음 얼굴 영역은 확인하지 않음
                        break;
                    }
                }
            }

            if(translatePoint != null) {
                imageData.imageCorrectData.collageCoordinate = new FacePointF(translatePoint.x,
                                                                              translatePoint.y);
            } else {
                imageData.imageCorrectData.collageCoordinate = new FacePointF(0.f, 0.f);
            }
            imageData.imageCorrectData.collageRotate = 0.f;
            imageData.imageCorrectData.collageTempletId = templateId;
            imageData.imageCorrectData.collageWidth = collageWidth;
            imageData.imageCorrectData.collageHeight = collageHeight;
        }

        return inputImageDatas;
    }

    private void initCollageData(ArrayList<ImageData> inputImageDatas) {
        if(inputImageDatas == null || inputImageDatas.isEmpty()) {
            return;
        }

        for(ImageData imageData : inputImageDatas) {
            if(imageData.imageCorrectData == null) {
                imageData.imageCorrectData = new ImageCorrectData();
            }

            imageData.imageCorrectData.initailizeCollageData();
        }
    }

    /**
     * 각 카테고리별로 콜라주 최종 배열을 반환<br>
     * 없다면, null이 반환
     * 
     * @param inputImageDatas 이미지 데이터 배열
     * @param category 카테고리 그룹 인덱스 CollageHelper.CATEGORY_XXX 참조.
     * @param themeName 테마 이름
     * @return ArrayList 각 카테고리 별 완성된 콜라주 배열
     */
    private ArrayList<ImageData> getCollageImageDatasWithCategory(
            ArrayList<ImageData> inputImageDatas, int category, String themeName) {
        // 가장 많이 등장한 인물의 이미지를 가져오자.
        ArrayList<ImageData> maxSizeGroupImageDatas = CollageHelper.getGroupImageDataWithCategory(inputImageDatas,
                                                                                                  category);

        if(maxSizeGroupImageDatas == null || maxSizeGroupImageDatas.isEmpty()) {
            return null;
        }

        // 콜라주로 쓰일 이미지 갯수를 랜덤으로 추출 (2~6 클리어테마, 2~6 디자인 테마) 5개짜리 프레임은 모두 배제한다.
        int randomFrameCount = (int)(Math.random() * 6) + 2;
        // 만약 5개짜리 프레임이 걸렸다면 다시 돌린다.
        while(randomFrameCount == 5) {
            randomFrameCount = (int)(Math.random() * 6) + 2;
        }
        // 랜덤 프레임 수 보다 콜라주로 쓰일 이미지가 적다면 이미지 갯수가 프레임 갯수가 됨
        if(maxSizeGroupImageDatas.size() < randomFrameCount) {
            randomFrameCount = maxSizeGroupImageDatas.size();
        }

        // 이미지가 부족하다면
        if(randomFrameCount < 2) {
            return null;
        }

        ArrayList<ImageData> collageImageDatas = getMatchedImageDatas(maxSizeGroupImageDatas,
                                                                      randomFrameCount,
                                                                      mCollageImageWidth, themeName);

        return collageImageDatas;
    }

    // ----------------------------------------------------------------------
    // --------------------------- 콜라주 매칭 관련 -----------------------------
    // ----------------------------------------------------------------------
    /**
     * 콜라주를 구성할 이미지 배열을 입력받아 최종적으로 프레임 순서로 배열 순서를 변경하고 콜라주 CorrectData를 셋팅하여 반환. <br>
     * 콜라주 구성에 실패하였을 경우 및 잘못된 파라미터가 넘어올 경우 null을 반환
     * 
     * @param inputImageDatas 콜라주를 구성할 이미지 배열
     * @param frameCount 프레임 갯수 (2~6개로 한정)
     * @param width 콜라주 이미지의 가로 길이
     * @param themeName 테마 이름
     * @return ArrayList 콜라주가 구성된 이미지 배열
     */
    public ArrayList<ImageData> getMatchedImageDatas(ArrayList<ImageData> inputImageDatas,
            int frameCount, int width, String themeName) {

        if(inputImageDatas == null || inputImageDatas.isEmpty()) {
            return null;
        }

        if(width > 0) {
            mCollageImageWidth = width;
        }

        if(THEME_NAME_CLEAN.equals(themeName) && frameCount < 2) {
            frameCount = 2;
        } else if(!THEME_NAME_CLEAN.equals(themeName) && frameCount < 1) {
            frameCount = 1;
        }

        if(frameCount > 6) {
            frameCount = 6;
        }

        // 프레임 갯수에 맞춰 템플릿들을 가져오고
        ArrayList<TemplateInfo> templateInfos = getTemplateArrayForThemeAndFrameCount(themeName,
                                                                                      frameCount);
        if(templateInfos == null || templateInfos.isEmpty()) {
            SmartLog.e(TAG, "not search collage frame");
            return null;
        }

        // 템플릿에서 최적의 템플릿을 찾아냄.
        TemplateInfo templateInfo = null;
        for(int i = 0; i < templateInfos.size(); i++) {
            templateInfo = templateInfos.get(i);

            // 특정 템플릿에 매칭 될 수 있는 이미지 후보군 정보를 담은 배열을 받아옴
            ArrayList<CollageFrameMatchInfo> collageFrameMatchInfoArr = getCollageFrameMatchInfo(templateInfo,
                                                                                                 inputImageDatas,
                                                                                                 width);
            if(collageFrameMatchInfoArr == null || collageFrameMatchInfoArr.isEmpty()) {
                return null;
            }

            // 이미지 후보군 정보에서 최종적으로 결정된 매칭 정보 배열을 가져온다. (i번째 프레임에 대한 정보)
            ArrayList<CollageMatchInfo> collageMatchInfoArr = getCollageMatchInfo(collageFrameMatchInfoArr);
            if(collageMatchInfoArr == null || collageMatchInfoArr.isEmpty()) {
                return null;
            }

            boolean isCollageMatchFrame = true;
            for(int j = 0; j < collageMatchInfoArr.size(); j++) {
                CollageMatchInfo collageMatchInfo = collageMatchInfoArr.get(j);
                // 하나라도 매칭되지 않는 녀석이 있다면
                if(collageMatchInfo.resultMatchIndex == -1
                        || collageMatchInfo.resultMatchTranslatePoint == null) {
                    isCollageMatchFrame = false;
                }
            }

            // 매칭 되는 녀석이 있다면 TemplateInfo
            if(isCollageMatchFrame) {
                ArrayList<ImageData> resultImageDatas = new ArrayList<ImageData>();
                // k번째 프레임에 들어갈 이미지 데이터와 좌표값을 가져오자
                for(int k = 0; k < collageMatchInfoArr.size(); k++) {
                    mCollageImageHeight = (int)(templateInfo.getAspectRatio() * mCollageImageWidth);
                    CollageMatchInfo collageMatchInfo = collageMatchInfoArr.get(k);
                    ImageData imageData = inputImageDatas.get(collageMatchInfo.resultMatchIndex);
                    ImageCorrectData imageCorrectData = imageData.imageCorrectData;
                    if(imageCorrectData == null) {
                        imageCorrectData = new ImageCorrectData();
                    }
                    imageCorrectData.collageCoordinate = collageMatchInfo.resultMatchTranslatePoint;
                    imageCorrectData.collageTempletId = templateInfo.getId();
                    imageCorrectData.collageWidth = mCollageImageWidth;
                    imageCorrectData.collageHeight = mCollageImageHeight;
                    resultImageDatas.add(imageData);
                }

                return resultImageDatas;
            }
        }

        return null;
    }

    /**
     * 콜라주의 각 프레임 매칭 정보를 얻어낸다.<br>
     * 가상의 콜라주를 그려 각 프레임의 Rect에 이미지가 적절히 들어갈 지 예측함으로 반드시 width, height값이 있어야한다.<br>
     * 각 프레임별 매칭 정보인 CollageFrameMatchInfo 배열을 프레임 갯수만큼 반환한다.<br>
     * 매칭이 전혀 되지 않는다거나 프레임에 대한 정보가 없거나 imageData 값이 적절하지 않을 경우 null을 반환한다. <br>
     * 콜라주의 Height 값은 Width값에 따라 결정된다.
     * 
     * @param templateInfo 매칭할 프레임 정보
     * @param imageDatas 매칭할 이미지 배열
     * @param collageWidth 콜라주 가로 길이 정보
     * @return ArrayList 콜라주 매칭 정보 배열 (프레임 갯수)
     */
    private ArrayList<CollageFrameMatchInfo> getCollageFrameMatchInfo(TemplateInfo templateInfo,
            ArrayList<ImageData> imageDatas, int collageWidth) {
        if(templateInfo == null || imageDatas == null || imageDatas.isEmpty() || collageWidth < 1) {
            return null;
        }

        int collageHeight = (int)(collageWidth * templateInfo.getAspectRatio());

        // 각 프레임 정보를 얻기 위한 콜라주 생성
        CollageView collageView = new CollageView(mApplicationContext, templateInfo, collageWidth,
                                                  collageHeight);
        collageView.setFrameImageScale(1.f, 5.f);

        // 프레임 정보를 추출
        ArrayList<Rect> frameBounds = collageView.getFrameBounds();
        ArrayList<ArrayList<RectF>> faceBounds = collageView.getFaceBounds();

        if(frameBounds == null || frameBounds.isEmpty()) {
            return null;
        }

        /**
         * 매칭 정보를 기록.
         */
        ArrayList<CollageFrameMatchInfo> frameMatchInfo = new ArrayList<CollageFrameMatchInfo>();
        // 각 프레임 별로 어느 이미지가 매칭이 되는지 판단
        for(int i = 0; i < frameBounds.size(); i++) {
            Rect collageFrame = frameBounds.get(i);
            // 콜라주의 프레임은 좌표가 0으로 초기화 되어야함
            RectF collageBound = new RectF(0.f, 0.f, collageFrame.width(), collageFrame.height());

            // 해당 이미지가 몇번 프레임에 맞는지 저장될 배열
            CollageFrameMatchInfo collageMatchInfo = new CollageFrameMatchInfo();
            // 해당 프레임에 매칭되는 이미지 인덱스 배열
            collageMatchInfo.matchImageIndexArr = new ArrayList<Integer>();
            // 해당 프레임에 매칭되는 이미지 이동 좌표 배열
            collageMatchInfo.matchFrameTranslatePointArr = new ArrayList<PointF>();

            for(int j = 0; j < imageDatas.size(); j++) {
                ImageData imageData = imageDatas.get(j);
                PointF translatePoint = null;
                if(faceBounds.get(i).isEmpty()) {
                    // 권장 이동 값이 있을 경우에만 해당 프레임에 적절한 배치 가능함
                    translatePoint = getAutoMoveValueFromImage(imageData, collageBound, null);
                    if(translatePoint != null) {
                        collageMatchInfo.matchImageIndexArr.add(j);
                        collageMatchInfo.matchFrameTranslatePointArr.add(translatePoint);
                    }
                } else {
                    // 콜라주 템플릿에 정의된 얼굴 영역이 있을 때.
                    for(RectF bounds : faceBounds.get(i)) {
                        translatePoint = getAutoMoveValueFromImage(imageData, collageBound, bounds);
                        if(translatePoint != null) {
                            collageMatchInfo.matchImageIndexArr.add(j);
                            collageMatchInfo.matchFrameTranslatePointArr.add(translatePoint);
                            // 얼굴 영역에 매치되면 다음 얼굴 영역은 확인하지 않음
                            break;
                        }
                    }
                }
            }

            frameMatchInfo.add(collageMatchInfo);
        }
        return frameMatchInfo;
    }

    /**
     * 콜라주 각 프레임별 매칭 후보군들을 입력받아 최종적으로 결정된 매칭 결과를 반환한다.<br>
     * 
     * @param collageFrameMatchInfoArr 각 프레임별 매칭 이미지 후보군 정보 배열
     * @return collageMatchInfo 각 프레임별 매칭 최종 결과 정보 배열
     */
    private ArrayList<CollageMatchInfo> getCollageMatchInfo(
            ArrayList<CollageFrameMatchInfo> collageFrameMatchInfoArr) {

        // 매칭 정보가 없다면 그냥 리턴하자
        if(collageFrameMatchInfoArr == null || collageFrameMatchInfoArr.isEmpty()) {
            return null;
        }

        // 최종 매칭 정보
        int[] matchIndexToFrame = new int[collageFrameMatchInfoArr.size()];
        for(int i = 0; i < matchIndexToFrame.length; i++) {
            matchIndexToFrame[i] = -1;
        }

        // 사용된 이미지 인덱스
        ArrayList<Integer> usedImageIndexArr = new ArrayList<Integer>();
        ArrayList<CollageMatchInfo> collageMatchInfoArr = new ArrayList<CollageMatchInfo>();
        // 매칭정보를 통하여 적절한 배치를 해보자
        for(int i = 0; i < collageFrameMatchInfoArr.size(); i++) {
            // i번째 프레임에 대한 정보
            CollageFrameMatchInfo collageFrameMatchInfo = collageFrameMatchInfoArr.get(i);
            CollageMatchInfo collageMatchInfo = new CollageMatchInfo();
            if(collageFrameMatchInfo.matchImageIndexArr.isEmpty()) {
                // 매칭 이미지가 없다면 해당 프레임의 배치는 종료
                collageMatchInfo.resultMatchIndex = -1;
                collageMatchInfo.resultMatchTranslatePoint = null;
                collageMatchInfoArr.add(collageMatchInfo);
                continue;
            }

            // i번째 프레임에 어느 이미지가 매칭이 될지 예상
            int selectedImageIndex = matchIndexToFrame[i];
            FacePointF selectedImageTranslatePoint = null;
            // 매칭 정보가 있다고 가정하고 각 프레임별 어떤 이미지가 매칭되는지 보자
            for(int j = 0; j < collageFrameMatchInfo.matchImageIndexArr.size(); j++) {
                Integer imageIndex = collageFrameMatchInfo.matchImageIndexArr.get(j);
                // 아직 결정된 녀석이 없고 사용한 적도 없다면
                if(selectedImageIndex == -1 && !usedImageIndexArr.contains(imageIndex)) {
                    selectedImageIndex = imageIndex;
                    usedImageIndexArr.add(imageIndex);
                    selectedImageTranslatePoint = new FacePointF(
                                                                 collageFrameMatchInfo.matchFrameTranslatePointArr.get(j).x,
                                                                 collageFrameMatchInfo.matchFrameTranslatePointArr.get(j).y);
                }
            }
            matchIndexToFrame[i] = selectedImageIndex;

            collageMatchInfo.resultMatchIndex = selectedImageIndex;
            collageMatchInfo.resultMatchTranslatePoint = selectedImageTranslatePoint;
            collageMatchInfoArr.add(collageMatchInfo);
        }

        return collageMatchInfoArr;
    }

    /**
     * 콜라주에 들어갈 이미지가 적절히 배치되는 권장 이동 값을 반환한다.<br>
     * 이미지의 얼굴 영역을 추출하여 해당 얼굴이 잘리지 않도록 권장 이동값을 결정한다.<br>
     * 단, 적절한 배치가 불가능 할 경우 null을 반환
     * 
     * @param imageData 콜라주에 들어갈 이미지 정보
     * @param collageBound 콜라주의 특정 프레임의 bound
     * @return PointF 적절한 배치를 위한 권장 이동값
     */
    private PointF getAutoMoveValueFromImage(ImageData imageData, RectF collageBound,
            RectF faceBound) {

        PointF moveCoordinate = new PointF(0, 0);

        // 이미지에 얼굴 정보가 있을 경우에만 프레임 비교를 할 수 있으므로
        // 얼굴 정보가 없다면 그대로 배치하도록 한다.
        if(imageData == null || imageData.faceDataItems == null
                || imageData.faceDataItems.isEmpty()) {
            return moveCoordinate;
        }

        // 이미지에서 콜라주로 보일 얼굴 영역
        RectF imageCollageRect = getShowCollageRectFromImage(collageBound, imageData);
        // 이미지에서 콜라주 프레임의 얼굴 영역에 보일 영역
        RectF imageFaceRect = getShowCollageFaceFromImage(collageBound, faceBound, imageData);

        // 이미지에서 얼굴 전체를 포함하는 최소한의 영역 (실제 크기 기준)
        RectF entireFaceRect = FaceInfomation.getFaceRectFromImageRealSize(imageData);

        // 얼굴 전체 영역이 콜라주에 보여질 부분 안에 들어가 있다면
        if(imageFaceRect.contains(entireFaceRect)) {
            return new PointF(0.f, 0.f);
        }

        // 얼굴 전체 영역이 콜라주에 보여질 부분에 포함되어 있지 않다면 얼굴 영역이 더 큰지 여부 판단
        // 얼굴 영역의 가로 또는 세로가 콜라주로 보일 영역보다 더 크다면 더이상 이동 불가능
        if(entireFaceRect.width() > imageFaceRect.width()
                || entireFaceRect.height() > imageFaceRect.height()) {
            return null;
        }

        // 그렇진 않으면 어느 쪽이 잘려나갔는지 판단하여 이동 값을 결정하자.
        float distance = 0.f;
        // 결정된 distance값은 실제 사진에서의 이동 거리이므로 collage의 비율에 맞춰야한다.
        float ratioImageByCollage = collageBound.width() / imageCollageRect.width();

        // 왼쪽 얼굴이 잘려나갔음
        if(entireFaceRect.left < imageFaceRect.left) {
            distance = Math.abs(imageFaceRect.left - entireFaceRect.left);
            distance *= ratioImageByCollage;
            moveCoordinate.x = distance;
        }
        // 오른쪽 판단
        if(entireFaceRect.right > imageFaceRect.right) {
            distance = Math.abs(imageFaceRect.right - entireFaceRect.right);
            distance *= ratioImageByCollage;
            moveCoordinate.x = -distance;
        }
        // 위쪽 판단
        if(entireFaceRect.top < imageFaceRect.top) {
            distance = Math.abs(imageFaceRect.top - entireFaceRect.top);
            distance *= ratioImageByCollage;
            moveCoordinate.y = distance;
        }
        // 아래쪽 판단
        if(entireFaceRect.bottom > imageFaceRect.bottom) {
            distance = Math.abs(imageFaceRect.bottom - entireFaceRect.bottom);
            distance *= ratioImageByCollage;
            moveCoordinate.y = -distance;
        }

        // 이미지가 콜라주 프레임 영역을 벗어나는지 확인
        if(imageCollageRect.top - moveCoordinate.y / ratioImageByCollage < 0
                || imageCollageRect.bottom - moveCoordinate.y / ratioImageByCollage > imageData.height
                || imageCollageRect.left - moveCoordinate.x / ratioImageByCollage < 0
                || imageCollageRect.right - moveCoordinate.x / ratioImageByCollage > imageData.width) {
            return null;
        }

        return moveCoordinate;
    }

    /**
     * 이미지 데이터 정보를 가지고 콜라주에서 보여질 이미지 영역을 검출한다
     * 
     * @param collageBound 콜라주 프레임 정보 (0,0,rigth=width,bottom=height)
     * @param imageData 이미지 데이터 정보
     * @return RectF 현재 이미지에서 콜라주로 보여질 영역
     */
    private RectF getShowCollageRectFromImage(RectF collageBound, ImageData imageData) {

        float imageWidth = (float)imageData.width;
        float imageHeight = (float)imageData.height;
        // 회전 결과에 따라 회전시켜 콜라주를 적용할 것이므로
        String orientation = imageData.orientation;
        if(orientation != null && "90".equals(orientation) || "270".equals(orientation)) {
            // 좌측 또는 우측으로 90도 기운 사진이라면 길이와 높이가 바뀌므로 변경해준다.
            imageWidth = (float)imageData.height;
            imageHeight = (float)imageData.width;
        }

        float widthScale = imageWidth / collageBound.width();
        float heightScale = imageHeight / collageBound.height();
        float scaleRatio = widthScale < heightScale ? widthScale : heightScale;
        PointF size = new PointF(collageBound.width() * scaleRatio, collageBound.height()
                * scaleRatio);

        float xPoint = (imageWidth - size.x) / 2;
        float yPoint = (imageHeight - size.y) / 2;
        RectF imageRect = new RectF(xPoint, yPoint, xPoint + size.x, yPoint + size.y);

        return imageRect;
    }

    /**
     * 이미지 데이터 정보를 가지고 콜라주 프레임의 얼굴영역에서 보여질 이미지 영역을 검출한다
     * 
     * @param collageBound 콜라주 프레임 정보 (0,0,rigth=width,bottom=height)
     * @param faceBound 콜라주 프레임의 얼굴영역 정보 (0,0,rigth=width,bottom=height)
     * @param imageData 이미지 데이터 정보
     * @return RectF 현재 이미지에서 콜라주로 보여질 영역
     */
    private RectF getShowCollageFaceFromImage(RectF collageBound, RectF faceBound,
            ImageData imageData) {

        float imageWidth = (float)imageData.width;
        float imageHeight = (float)imageData.height;
        // 회전 결과에 따라 회전시켜 콜라주를 적용할 것이므로
        String orientation = imageData.orientation;
        if(orientation != null && "90".equals(orientation) || "270".equals(orientation)) {
            // 좌측 또는 우측으로 90도 기운 사진이라면 길이와 높이가 바뀌므로 변경해준다.
            imageWidth = (float)imageData.height;
            imageHeight = (float)imageData.width;
        }

        float widthScale = imageWidth / collageBound.width();
        float heightScale = imageHeight / collageBound.height();
        float scaleRatio = widthScale < heightScale ? widthScale : heightScale;
        PointF size = new PointF(collageBound.width() * scaleRatio, collageBound.height()
                * scaleRatio);

        float xPoint = (imageWidth - size.x) / 2;
        float yPoint = (imageHeight - size.y) / 2;
        RectF imageRect = new RectF(xPoint, yPoint, xPoint + size.x, yPoint + size.y);
        // 얼굴 영역이 있을 경우
        if(faceBound != null) {
            imageRect.left = imageRect.left + faceBound.left * scaleRatio;
            imageRect.top = imageRect.top + faceBound.top * scaleRatio;
            imageRect.right = imageRect.left + faceBound.width() * scaleRatio;
            imageRect.bottom = imageRect.top + faceBound.height() * scaleRatio;
        }

        return imageRect;
    }

    /**
     * 콜라주 매칭 정보를 저장하기 위한 클래스<br>
     * 각 프레임별 매칭되는 이미지 후보군의 인덱스와 각 이미지의 추천 이동 좌표를 갖는다.
     */
    private class CollageFrameMatchInfo {
        /**
         * 매칭되는 후보 이미지 Index의 배열
         */
        public ArrayList<Integer> matchImageIndexArr;
        /**
         * 매칭되는 후보 이미지 이동 좌표 배열
         */
        public ArrayList<PointF> matchFrameTranslatePointArr;
    }

    /**
     * 콜라주 매칭 정보를 저장하기 위한 클래스<br>
     * 각 프레임 별로 최종적으로 매칭되는 ImageData 배열의 인덱스와 이동시킬 포인트를 갖는다.
     */
    private class CollageMatchInfo {
        /**
         * 매칭되는 후보 이미지들 중 최종적으로 결정된 Index
         */
        public int resultMatchIndex;
        /**
         * 매칭되는 후보 이미지들 중 최종적으로 결정된 추천 이동 좌표
         */
        public FacePointF resultMatchTranslatePoint;
    }

}
