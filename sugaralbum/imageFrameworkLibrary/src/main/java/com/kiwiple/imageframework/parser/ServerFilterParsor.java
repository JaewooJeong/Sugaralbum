
package com.kiwiple.imageframework.parser;

import java.io.IOException;
import java.util.ArrayList;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import com.kiwiple.imageframework.filter.FilterData;
import com.kiwiple.imageframework.network.DataParser;
import com.kiwiple.imageframework.network.NetworkError;

/**
 * 서버 API 호출 결과 필터 목록을 저장하기 위한 class
 * 
 * @version 1.0
 */
public class ServerFilterParsor implements DataParser {
    /**
     * 필터 목록
     * 
     * @version 1.0
     */
    public ArrayList<FilterData> mResult = new ArrayList<FilterData>();

    @Override
    public int parse(String fieldName, JsonParser jp) throws JsonParseException, IOException {
        if(fieldName.equals("FilterList")) {
            while(jp.nextToken() != JsonToken.END_ARRAY) {
                FilterData data = new FilterData();
                data.parse(jp);
                if(data.mFilter != null) {
                    mResult.add(data);
                }
            }
        }
        return NetworkError.NERR_SUCCESS;
    }
}
