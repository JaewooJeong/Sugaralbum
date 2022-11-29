
package com.kiwiple.imageframework.filter;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

/**
 * TODO: 사용하지 않는 class. 삭제 필요. 매직아워에서 Overlay 텍스처 여부에 대한 구분을 json으로 관리했지만, 
 * 지금은 {@link com.kiwiple.imageframework.filter.Filter#NOT_OVERLAY_TEXTURE}로 관리
 */
class TextureInfo {
    public String mName;
    public int mOverlay; // 1==true, 0==false

    public void parse(JsonParser jp) throws JsonParseException, IOException {
        while(jp.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jp.getCurrentName();

            if(jp.nextToken() == JsonToken.VALUE_NULL) {
                continue;
            }

            if(fieldName.equals("Name")) {
                mName = jp.getText();
                mName = mName.replace(" ", "_");
            } else if(fieldName.equals("Overlay")) {
                mOverlay = jp.getIntValue();
            }
        }
    }
}
