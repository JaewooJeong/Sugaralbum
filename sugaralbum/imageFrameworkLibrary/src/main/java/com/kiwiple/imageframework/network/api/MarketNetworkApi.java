
package com.kiwiple.imageframework.network.api;

import java.util.Locale;

import com.kiwiple.imageframework.network.DataParser;
import com.kiwiple.imageframework.network.NetworkEventListener;
import com.kiwiple.imageframework.network.NetworkManager;
import com.kiwiple.imageframework.network.NetworkManager.OvjetProtocol;
import com.kiwiple.imageframework.network.ProtocolParam;
import com.kiwiple.imageframework.network.util.Base64Coder;

/**
 * MagicHour 서버 API를 호출하기 위한 class
 * 
 * @version 1.0
 */
public class MarketNetworkApi extends NetworkApi {
    /**
     * 다운로드 순위 100위 안에 드는 필터 목록을 가져온다.
     * 
     * @version 1.0
     */
    public static final String TYPE_TOP_100 = "download";
    /**
     * 신규로 올라온 필터 목록을 가져온다.
     * 
     * @version 1.0
     */
    public static final String TYPE_NEW_FILTER = "new";

    /**
     * 다운로드 순위 100위 필터 목록 요청 성공 메세지
     * 
     * @version 1.0
     */
    public static final String STATE_TOP100_LIST_REQUSET_COMPLETE = "NETWORK_S_DOWNLOAD_LIST_REQUSET_COMPLETE";
    /**
     * 다운로드 순위 100위 필터 목록 요청 실패 메세지
     * 
     * @version 1.0
     */
    public static final String STATE_TOP100_LIST_REQUSET_FAIL = "NETWORK_S_DOWNLOAD_LIST_REQUSET_FAIL";

    /**
     * 신규 필터 목록 요청 성공 메세지
     * 
     * @version 1.0
     */
    public static final String STATE_NEW_LIST_REQUSET_COMPLETE = "NETWORK_S_NEW_LIST_REQUSET_COMPLETE";
    /**
     * 신규 필터 목록 요청 실패 메세지
     * 
     * @version 1.0
     */
    public static final String STATE_NEW_LIST_REQUSET_FAIL = "NETWORK_S_NEW_LIST_REQUSET_FAIL";

    /**
     * 필터 다운로드 로그 저장 성공 메세지
     * 
     * @version 1.0
     */
    public static final String STATE_LOG_COMPLETE = "NETWORK_S_LOG_COMPLETE";
    /**
     * 필터 다운로드 로그 저장 실패 메세지
     * 
     * @version 1.0
     */
    public static final String STATE_LOG_FAIL = "NETWORK_S_LOG_FAIL";

    /**
     * Desc : 필터 마켓에서 필터 목록 받아오기
     * 
     * @Method Name : reqMarketFilterList
     * @param listener : MHNetworkEventListener 사용
     * @param parser : ServerFilterParsor 사용
     * @param type : featured / popular / new
     * @param count : 0으로 사용 (count 가 0일 경우 기본 10개!!)
     * @param startId : 0으로 사용 (startId 없으면 무시 -> 더보기에서만 사용)
     * @version 1.0
     */
    public static void reqMarketFilterList(NetworkEventListener listener, DataParser parser,
            String type, int count, int startId, boolean showLog) {
        String complete;
        String fail;
        if(type.equals(TYPE_TOP_100)) {
            complete = STATE_TOP100_LIST_REQUSET_COMPLETE;
            fail = STATE_TOP100_LIST_REQUSET_FAIL;
        } else if(type.equals(TYPE_NEW_FILTER)) {
            complete = STATE_NEW_LIST_REQUSET_COMPLETE;
            fail = STATE_NEW_LIST_REQUSET_FAIL;
        } else {
            return;
        }

        OvjetProtocol proto = NetworkManager.getInstance().new OvjetProtocol(
                                                                             "/magicapi/getfilterlist",
                                                                             complete, fail);
        proto.Param(new ProtocolParam("gubun", type));

        if(count != 0) {
            proto.Param(new ProtocolParam("count", count));
        }
        if(startId != 0) {
            proto.Param(new ProtocolParam("startid", startId));
        }
        proto.Param(new ProtocolParam("platform", "android"));

        proto.Param(new ProtocolParam("filter_version", 1));
        
        request(proto, listener, parser, showLog);
    }

    /**
     * Desc : 필터 다운로드 로그
     * 
     * @Method Name : sendDownloadCountLog
     * @param listener : MHNetworkEventListener 사용
     * @param marketId : 다운받은 필터 ID
     * @version 1.0
     */
    public static void sendDownloadCountLog(NetworkEventListener listener, int marketId,
            boolean showLog) {
        OvjetProtocol proto = NetworkManager.getInstance().new OvjetProtocol(
                                                                             "/magicapi/marketdownloadlog",
                                                                             STATE_LOG_COMPLETE,
                                                                             STATE_LOG_FAIL);
        proto.Param(new ProtocolParam("market_id", marketId));

        // key..
        String key = Base64Coder.getMD5HashString("LGU_ShareCamera_" + marketId)
                                .toUpperCase(Locale.getDefault());
        proto.Param(new ProtocolParam("key", key));

        request(proto, listener, null, showLog);
    }
}
