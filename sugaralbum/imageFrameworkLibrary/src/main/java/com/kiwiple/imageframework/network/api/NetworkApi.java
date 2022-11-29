
package com.kiwiple.imageframework.network.api;

import android.os.Build;

import com.kiwiple.imageframework.Constants;
import com.kiwiple.imageframework.network.DataParser;
import com.kiwiple.imageframework.network.NetworkEventListener;
import com.kiwiple.imageframework.network.NetworkManager;
import com.kiwiple.imageframework.network.NetworkManager.OvjetProtocol;
import com.kiwiple.imageframework.network.ProtocolParam;

public class NetworkApi {
    public static void request(OvjetProtocol proto, NetworkEventListener listener,
            DataParser parser, boolean showLog) {
        if(proto.getRequestMethod() != OvjetProtocol.REQ_METHOD_GET) {
            proto.Param(new ProtocolParam("device", "Android " + Build.VERSION.SDK_INT));
            proto.Param(new ProtocolParam("app_version", Constants.APP_VERSION));
        }
        NetworkManager.getInstance().cancelRequest(listener);

        proto.setTimeout(20000);
        proto.SendReq(listener, parser);
    }
}
