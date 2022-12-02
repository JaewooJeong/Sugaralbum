
package com.kiwiple.imageframework.network;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;

/**
 * 서버 API 호출 결과 data를 저장하는 class
 * 
 * @version 1.0
 */
public interface DataParser {
    public int parse(String fieldName, JsonParser jp) throws JsonParseException, IOException;
}
