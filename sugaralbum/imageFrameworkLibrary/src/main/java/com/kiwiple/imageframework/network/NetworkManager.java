
package com.kiwiple.imageframework.network;

import static com.kiwiple.imageframework.network.NetworkError.NERR_AUTH_FAIL;
import static com.kiwiple.imageframework.network.NetworkError.NERR_DUPLICATE_SIGN;
import static com.kiwiple.imageframework.network.NetworkError.NERR_DUPLICATE_TITLE;
import static com.kiwiple.imageframework.network.NetworkError.NERR_HOST_FAIL;
import static com.kiwiple.imageframework.network.NetworkError.NERR_PARSE_FAIL;
import static com.kiwiple.imageframework.network.NetworkError.NERR_REQUEST_FAIL;
import static com.kiwiple.imageframework.network.NetworkError.NERR_SUCCESS;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import android.os.Handler;

import com.kiwiple.imageframework.Constants;
import com.kiwiple.imageframework.network.util.SSLUtilities;
import com.kiwiple.imageframework.util.SmartLog;

/**
 * 서버 API를 호출하기 위한 class <br>
 * {@link #getInstance} 함수로 instance를 생성하고 singleton으로 동작한다.
 * 
 * @version 1.0
 */
public class NetworkManager {
    private static final String TAG = NetworkManager.class.getSimpleName();
    private static NetworkManager sInstance;

    // Server error state
    private static final int SERVER_ERR_SUCCESS = 0;
    private static final int SERVER_ERR_AUTH_FAIL = 4;
    private static final int SERVER_ERR_DUPLICATE_TITLE = 1303;
    private static final int SERVER_ERR_DUPLICATE_SIGN = 1304;

    // default state
    private static final String STATE_NONE = "BDSTATE_NONE";

    private static final String SERVER_URL_HTTP = "http://magichour.me";
    private static final String DEV_SERVER_URL_HTTP = "http://222.231.27.171:4000";
//    private static final String DEV_SERVER_URL_HTTP = "http://stage.filtercamera.com:9000";
    private static final int REQUEST_RETRY_COUNT = 1;

    private Handler mHandler = new Handler();

    private String mLastState;
    private Integer mLastError;

    private ArrayList<HttpConnectionThread> mNetworkThreadList = new ArrayList<HttpConnectionThread>();

    /**
     * @return {@link #NetworkManager}의 인스턴스 반환
     * @version 1.0
     */
    public static NetworkManager getInstance() {
        if(sInstance == null) {
            sInstance = new NetworkManager();
        }
        return sInstance;
    }

    private NetworkManager() {
        mLastState = STATE_NONE;
        SSLUtilities.allowAllSSL();
    }

    /**
     * 모든 서버 API 요청을 취소한다.
     * 
     * @version 1.0
     */
    public void cancelAllRequest() {

        for(int i = mNetworkThreadList.size() - 1; i >= 0; i--) {
            HttpConnectionThread thread = mNetworkThreadList.get(i);

            thread.cancel();
            thread.interrupt();
        }

        mNetworkThreadList.clear();
    }

    /**
     * 특정 서버 API 요청을 취소한다.
     * 
     * @param listener 취소할 서버 API와 연결된 NetworkEventListener
     * @version 1.0
     */
    public void cancelRequest(NetworkEventListener listener) {

        for(int i = mNetworkThreadList.size() - 1; i >= 0; i--) {
            HttpConnectionThread thread = mNetworkThreadList.get(i);

            if(thread.getListener() == listener) {
                thread.cancel();
                thread.interrupt();

                mNetworkThreadList.remove(thread);
            }
        }
    }

    // can be called by sub-threads
    synchronized private void setLastError(Integer err) {
        SmartLog.e(TAG, "[NETERROR] " + err);
        mLastError = err;
    }

    synchronized private void setLastState(String state) {
        mLastState = state;
    }

    synchronized private void setErrorState(HttpConnectionThread thread, String state, Integer err) {
        if(!thread.isCanceled()) {
            setLastError(err);
            setLastState(state);
            notifyResult(thread);
        }
    }

    private void notifyResult(HttpConnectionThread thread) {
        NetworkEventListener listener = thread.getListener();

        if(listener != null) {
            listener.onNetworkEvent(mLastState, mLastError);
        }

        mNetworkThreadList.remove(thread);
    }

    private abstract class HttpConnectionThread extends Thread {
        private final static int TIMEOUT_TIME = 20000;
        public final static int METHOD_GET = 0;
        public final static int METHOD_POST = 1;

        protected BaseHttpConnection mConn;
        protected int mMethod = METHOD_GET;

        protected String mSuccessState = STATE_NONE;
        protected String mErrorState = STATE_NONE;

        protected ArrayList<NameValuePair> _valuePair;
        protected MultipartEntity _entity;

        private DataParser mParser = null;

        private Timer mTimeoutTimer;
        private boolean mTimeout = false;
        private int mTimeoutTime = TIMEOUT_TIME;

        private NetworkEventListener mListener;
        private boolean mCanceled = false;

        protected boolean mIsMultipart = false;
        protected boolean mIsSSL = false;

        public HttpConnectionThread(int method, NetworkEventListener listener) {
            mMethod = method;
            mListener = listener;

            mNetworkThreadList.add(this);
        }

        public NetworkEventListener getListener() {
            return mListener;
        }

        protected void setTimeoutTime(int time) {
            mTimeoutTime = time;
        }

        protected void setParser(DataParser parser) {
            mParser = parser;
        }

        public void cancel() {
            if(mConn != null) {

                try {
                    mConn.disconnect();
                } catch(Exception e) {

                }

                mCanceled = true;
            }
        }

        public boolean isCanceled() {
            return mCanceled;
        }

        protected void connectTargetURL(String url) {
            try {
                String _url = null;

                _url = url;

                SmartLog.i(TAG, "Url : " + _url);

                if(mMethod == METHOD_GET) {
                    mConn = BaseHttpConnection.newInstance("GET", _url);
                    mConn.acceptGZipResponse();

                    mConn.setTimeoutTime(10000, mTimeoutTime);
                } else {
                    mConn = BaseHttpConnection.newInstance("POST", _url);
                    mConn.acceptGZipResponse();
                    mConn.setTimeoutTime(10000, mTimeoutTime);
                }

                mTimeout = false;
                mTimeoutTimer = new Timer();

                mTimeoutTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        SmartLog.i(TAG, "Request Timeout!!!!");

                        _setErrorState(NetworkError.NERR_TIME_OUT);
                        mTimeout = true;
                        mTimeoutTimer.cancel();
                        mTimeoutTimer = null;

                        mConn.disconnect();
                    }
                }, mTimeoutTime + 2000);

            } catch(Exception e) {
                SmartLog.e(TAG, "connectTargetURL", e);
            }
        }

        protected void connectURL(String url) {
            try {
                String _url = null;

                if(!Constants.DEV_SERVER) {
                    _url = String.format("%s%s", SERVER_URL_HTTP, url);

                    if(mIsSSL) {
                        if(!_url.contains("https")) {
                            _url = _url.replace("http", "https");
                        }
                    }
                } else {
                    _url = String.format("%s%s", DEV_SERVER_URL_HTTP, url);
                }

                SmartLog.i(TAG, "Url : " + _url);

                if(mMethod == METHOD_GET) {
                    mConn = BaseHttpConnection.newInstance("GET", _url);
                    mConn.acceptGZipResponse();

                    mConn.setTimeoutTime(10000, mTimeoutTime);
                } else {
                    mConn = BaseHttpConnection.newInstance("POST", _url);
                    mConn.acceptGZipResponse();

                    mConn.setTimeoutTime(10000, mTimeoutTime);
                }

                mTimeout = false;
                if(mTimeoutTimer != null) {
                    mTimeoutTimer.cancel();
                }

                mTimeoutTimer = new Timer();

                mTimeoutTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        SmartLog.i(TAG, "Request Timeout!!!!");

                        _setErrorState(NetworkError.NERR_TIME_OUT);
                        mTimeout = true;
                        mTimeoutTimer.cancel();
                        mTimeoutTimer = null;

                        mConn.disconnect();
                    }
                }, mTimeoutTime + 5000);

            } catch(Exception e) {
                SmartLog.e(TAG, "connectURL", e);
            }
        }

        protected void openOutputStream() {

            if(mIsMultipart) {
                // mConn.setMultipart();
                _entity = new MultipartEntity(HttpMultipartMode.STRICT);
            } else {
                _valuePair = new ArrayList<NameValuePair>();
            }
        }

        protected void addData(String name, String value) {

            try {
                if(mIsMultipart) {
                    _entity.addPart(name,
                                    new StringBody(value, "text/plain", Charset.forName("UTF-8")));
                } else {
                    _valuePair.add(new BasicNameValuePair(name, value));
                }
            } catch(Exception e) {
            }
        }

        protected void addDataImage(String name, String fileName) {
            // 이미지 업로드는 multipart에서만 지원.
            if(!mIsMultipart) {
                return;
            }

            try {
                File file = new File(fileName);

                ContentBody f = new FileBody(file, "image/jpg");

                _entity.addPart(name, f);
            } catch(Exception e) {
            }
        }

        protected void closeOutputStream() {
            try {
                if(_valuePair != null) {

                    // ByteArrayOutputStream out = new ByteArrayOutputStream();
                    //
                    // new UrlEncodedFormEntity(_valuePair, "UTF-8").writeTo(out);
                    //
                    // SmartLog.i(TAG, out.toString());

                    mConn.setEntity(new UrlEncodedFormEntity(_valuePair, "UTF-8"));
                } else if(_entity != null) {
                    mConn.setEntity(_entity);
                }
            } catch(Exception e) {
            }
        }

        protected InputStream getInputStream() {
            if(mConn != null) {
                try {
                    return mConn.getInputStream();
                } catch(Exception e) {
                    SmartLog.e(TAG, "getInputStream", e);
                }
            }
            return null;
        }

        protected int getResponseCode() throws IOException {
            if(mConn != null) {
                int code = mConn.getResponseCode();

                if(mTimeout) {
                    return HttpStatus.SC_REQUEST_TIMEOUT;
                }
                mTimeoutTimer.cancel();
                mTimeoutTimer = null;
                return code;
            }
            return HttpStatus.SC_SERVICE_UNAVAILABLE;
        }

        protected void _setErrorState(final int err) {
            if(mCanceled) {
                return;
            }

            if(err == NERR_SUCCESS) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setErrorState(HttpConnectionThread.this, mSuccessState, err);
                    }
                });
            } else {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setErrorState(HttpConnectionThread.this, mErrorState, err);
                    }
                });
            }
        }

        protected final boolean parseResult() {
            try {
                int code;

                if(mCanceled) {
                    return true;
                }

                code = getResponseCode();

                if(mCanceled) {
                    return true;
                }

                if(mTimeout) {
                    return true;
                }

                if(code == -1) {
                    return false;
                }

                if(code == HttpStatus.SC_OK) {
                    InputStream in = getInputStream();

                    if(in == null) {
                        _setErrorState(NERR_HOST_FAIL);
                        return true;
                    }
                    _setErrorState(parse(in));
                } else if(code == HttpStatus.SC_UNAUTHORIZED) {
                    SmartLog.e(TAG, "HttpStatus Error Code: " + code);
                    _setErrorState(NERR_AUTH_FAIL);
                } else {
                    SmartLog.e(TAG, "HttpStatus Error Code: " + code);
                    _setErrorState(NERR_REQUEST_FAIL);
                }

                return true;
            } catch(SocketTimeoutException e) {
                SmartLog.e(TAG, "Timeout Exception!! : ", e);

                if(mTimeoutTimer != null) {
                    mTimeoutTimer.cancel();
                    mTimeoutTimer = null;
                }

                if(!mTimeout && !mCanceled) {
                    _setErrorState(NetworkError.NERR_TIME_OUT);
                }

                return true;
            } catch(IOException e) {
                SmartLog.e(TAG, "IOException!! : ", e);
                if(mTimeoutTimer != null) {
                    mTimeoutTimer.cancel();
                    mTimeoutTimer = null;
                }

                if(!mTimeout && !mCanceled) {
                    return false;
                }
                return true;
            } catch(Exception e) {
                SmartLog.e(TAG, "Exception!! : ", e);

                if(mTimeoutTimer != null) {
                    mTimeoutTimer.cancel();
                    mTimeoutTimer = null;
                }

                if(!mTimeout && !mCanceled) {
                    _setErrorState(NERR_REQUEST_FAIL);
                }
            }

            return true;
        }

        @Override
        public void run() {
            tryRequest();
        }

        protected void tryRequest() {
            int count = 0;

            try {
                while(count < REQUEST_RETRY_COUNT) {
                    if(request()) {
                        return;
                    }

                    count++;
                }
            } catch(Exception e) {
            }
            _setErrorState(NERR_REQUEST_FAIL);
        }

        protected boolean request() {
            return false;
        }

        private final int parse(InputStream in) {
            JsonFactory f = new JsonFactory();
            try {
                JsonParser jp = f.createJsonParser(in);

                jp.nextToken(); // will return JsonToken.START_OBJECT (verify?)

                int code = 0;
                String errMsg;
                int err;

                while(jp.nextToken() != JsonToken.END_OBJECT && !jp.isClosed()) {
                    String fieldName = jp.getCurrentName();
                    jp.nextToken();

                    if("ErrorCode".equals(fieldName)) {
                        code = jp.getIntValue();
                    } else if("ErrorMessage".equals(fieldName)) {
                        errMsg = jp.getText();

                        if(code != 0) {
                            SmartLog.e(TAG, errMsg);
                            // 종류에 맞게 에러 return
                            return changeToNetworkError(code);
                        }
                    } else {
                        if(mParser != null) {
                            err = mParser.parse(fieldName, jp);
                        } else {
                            err = NERR_SUCCESS;
                        }

                        if(err != NERR_SUCCESS) {
                            return err;
                        }
                    }
                }
            } catch(Exception e) {
                SmartLog.e(TAG, "Json Parser Error : ", e);
                return NERR_PARSE_FAIL;
            }
            return NERR_SUCCESS;
        }

        private int changeToNetworkError(int serverErr) {
            switch(serverErr) {
                case SERVER_ERR_SUCCESS:
                    return NERR_SUCCESS;
                case SERVER_ERR_AUTH_FAIL:
                    return NERR_AUTH_FAIL;
                case SERVER_ERR_DUPLICATE_TITLE:
                    return NERR_DUPLICATE_TITLE;
                case SERVER_ERR_DUPLICATE_SIGN:
                    return NERR_DUPLICATE_SIGN;

                default:
                    return NERR_REQUEST_FAIL;
            }
        }
    }

    /**
     * 호출한 서버 API에 관련된 정보를 저장하는 class
     * 
     * @version 2.0
     */
    public class OvjetProtocol {
        /**
         * 호출할 서버 API의 method를 get방식으로 설정한다.
         * 
         * @version 1.0
         */
        public static final int REQ_METHOD_GET = 1;
        /**
         * 호출할 서버 API의 method를 post방식으로 설정한다.
         * 
         * @version 1.0
         */
        public static final int REQ_METHOD_POST = 2;

        String STATE_COMPLETE;
        String STATE_FAIL;
        String REQ_URL;

        int mReqMethod = REQ_METHOD_POST;
        boolean mDirectUrl = false;
        boolean mPrinteHeader = false;
        int mTimeout = 0;
        boolean mIsMultipart_ = false;
        StringBuffer mBuffer = new StringBuffer();

        ArrayList<ProtocolParam> mParams = new ArrayList<ProtocolParam>();

        /**
         * 호출할 서버 API의 url, 성공/실패시 메세지를 설정한다.
         * 
         * @param url 서버 API의 url
         * @param complete 성공시 메세지
         * @param fail 실패시 메세지
         * @version 1.0
         */
        public OvjetProtocol(String url, String complete, String fail) {
            REQ_URL = url;
            STATE_COMPLETE = complete;
            STATE_FAIL = fail;
        }

        /**
         * 호출할 서버 API의 url, 성공/실패시 메세지, method를 설정한다.
         * 
         * @param url 서버 API의 url
         * @param complete 성공시 메세지
         * @param fail 실패시 메세지
         * @param method 서버 API 호출시 사용할 method
         * @version 1.0
         */
        public OvjetProtocol(String url, String complete, String fail, int method) {
            REQ_URL = url;
            STATE_COMPLETE = complete;
            STATE_FAIL = fail;

            mReqMethod = method;
        }

        /**
         * 서버 API 호출시 포함할 parameter를 추가한다.
         * 
         * @param p 추가할 parameter
         * @version 1.0
         */
        public void Param(ProtocolParam p) {

            if(mReqMethod == REQ_METHOD_POST) {
                mParams.add(p);
                if(p.isFile()) {
                    mIsMultipart_ = true;
                }
            } else {
                mBuffer.append(p.bufferGETMethod());
            }
        }

        /**
         * 서버 API 호출시 timeout 시간을 설정한다.
         * 
         * @param time timeout 시간(ms)
         * @version 1.0
         */
        public void setTimeout(int time) {
            mTimeout = time;
        }

        /**
         * MagicHour 서버 이외의 API를 호출할 때 설정한다.
         * 
         * @param dir true로 설정하면 외부 서버 API를 호출
         * @version 1.0
         */
        public void setDirectUrl(boolean dir) {
            mDirectUrl = dir;
        }

        /**
         * 서버 API를 호출한다.
         * 
         * @param listener 서버 API 호출 완료시 결과를 반환받기 위한 콜백 함수
         * @param parser 서버 API 호출 결과를 저장하기 위한 DataParser
         * @version 1.0
         */
        public void SendReq(NetworkEventListener listener, DataParser parser) {
            if(mReqMethod == REQ_METHOD_POST) {
                new reqProc(listener, parser, HttpConnectionThread.METHOD_POST).start();
            } else {
                new reqProc(listener, parser, HttpConnectionThread.METHOD_GET).start();
            }
        }

        /**
         * 현재 설정된 method를 반환한다.
         * 
         * @return 현재 설정된 method
         * @version 1.0
         */
        public int getRequestMethod() {
            return mReqMethod;
        }

        private class reqProc extends HttpConnectionThread {
            public reqProc(NetworkEventListener listener, DataParser parser, int method) {
                super(method, listener);

                setParser(parser);

                mSuccessState = STATE_COMPLETE;
                mErrorState = STATE_FAIL;

                mIsMultipart = mIsMultipart_;

                if(mTimeout != 0) {
                    setTimeoutTime(mTimeout);
                }
            }

            @Override
            protected boolean request() {
                SmartLog.d(TAG, REQ_URL + ".run");

                String url = REQ_URL;

                SmartLog.d(TAG, "START-HTTP : " + url);

                if(mMethod == METHOD_GET) {
                    url += mBuffer.toString();
                }

                if(!mDirectUrl) {
                    connectURL(url);
                } else {
                    connectTargetURL(url);
                }

                if(mMethod == METHOD_POST) {
                    openOutputStream();

                    for(ProtocolParam param : mParams) {
                        if(!param.isFile()) {
                            addData(param.mName, param.val());
                        } else {
                            if(param.isImageFile()) {
                                addDataImage(param.mName, param.val());
                            }
                        }
                    }

                    closeOutputStream();
                } else {

                }

                if(mPrinteHeader) {
                    mConn.printHeaders();
                }

                return parseResult();
            }
        }
    } // end of ovjetProcotocol Class
}
