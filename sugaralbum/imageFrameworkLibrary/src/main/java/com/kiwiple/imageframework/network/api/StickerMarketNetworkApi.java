
package com.kiwiple.imageframework.network.api;

import com.kiwiple.imageframework.network.DataParser;
import com.kiwiple.imageframework.network.NetworkEventListener;
import com.kiwiple.imageframework.network.NetworkManager;
import com.kiwiple.imageframework.network.NetworkManager.OvjetProtocol;
import com.kiwiple.imageframework.network.ProtocolParam;
import com.kiwiple.imageframework.network.api.NetworkApi;

public class StickerMarketNetworkApi extends NetworkApi {
    
    /**
     * 스티커 마켓 리스트 호출 성공
     * 
     * @version 1.0
     */
    public static final String STATE_STICKER_MARKET_LIST_REQUSET_COMPLETE = "NETWORK_STICKER_COMPLETE";
    /**
     * 스티커 마켓 리스트 호출 실패
     * 
     * @version 1.0
     */
    public static final String STATE_STICKER_MARKET_LIST_REQUSET_FAIL = "NETWORK_STICKER_FAIL";

    /**
     * Desc : 스티커 마켓 리스트
     * 
     * @param listener : MHNetworkEventListener 사용
     * @param count : 리스트 호출 갯수
     * @param skip : 호출 넘버 인덱스
     * @param isContainAnimatedSticker : 움직이는 스티커 패키지를 포함하여 받을지 여부
     * @version 1.0
     */
    public static void reqStickerMarketList(NetworkEventListener listener, DataParser parser,
            int count, int skip, boolean showLog, boolean isContainAnimatedSticker) {
        OvjetProtocol proto = NetworkManager.getInstance().new OvjetProtocol(
                                                                             "/magicapi/getStickerUplusList",
                                                                             STATE_STICKER_MARKET_LIST_REQUSET_COMPLETE,
                                                                             STATE_STICKER_MARKET_LIST_REQUSET_FAIL);
        proto.Param(new ProtocolParam("count", count));
        proto.Param(new ProtocolParam("skip", skip));
        
        // 20150729 sawo : Animated Sticker 포함 구분자 추가
        if (isContainAnimatedSticker) {
            proto.Param(new ProtocolParam("ani_uplus_gb", 1));    
        }
        
        request(proto, listener, parser, showLog);
    }

}
