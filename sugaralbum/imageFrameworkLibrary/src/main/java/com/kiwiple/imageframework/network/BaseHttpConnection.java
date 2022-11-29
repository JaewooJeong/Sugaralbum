
package com.kiwiple.imageframework.network;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import com.kiwiple.imageframework.util.SmartLog;

public class BaseHttpConnection {
    private static final String TAG = BaseHttpConnection.class.getSimpleName();

    private HttpClient mHttpClient;
    private HttpRequestBase mHttpRequest;
    private HttpResponse mResponse;

    protected BaseHttpConnection(String method, String spec) throws IllegalArgumentException {

        mHttpClient = new DefaultHttpClient();

        if(method.equals("GET")) {
            mHttpRequest = new HttpGet(spec);
        } else {
            mHttpRequest = new HttpPost(spec);
        }

        Locale loc = Locale.getDefault();

        mHttpRequest.addHeader("Accept-Language", loc.getLanguage());
    }

    public static BaseHttpConnection newInstance(String method, String spec)
            throws IllegalArgumentException {
        return new BaseHttpConnection(method, spec);
    }

    public void addHeader(Header h) {
        mHttpRequest.addHeader(h);
    }

    public void acceptGZipResponse() {
        mHttpRequest.addHeader("Accept-Encoding", "gzip");
    }

    public int getResponseCode() throws IOException {

        mResponse = mHttpClient.execute(mHttpRequest);

        return mResponse.getStatusLine().getStatusCode();
    }

    public String getResponseMessage() {
        return mResponse.getStatusLine().getReasonPhrase();
    }

    private static class PrintRequestInterceptor implements HttpRequestInterceptor {

        @Override
        public void process(HttpRequest arg0, HttpContext arg1) throws HttpException, IOException {
            Header[] h = arg0.getAllHeaders();

            for(Header header : h) {
                SmartLog.i(TAG, "Name : " + header.getName() + "  Value : " + header.getValue());
            }
        }

    }

    public void printHeaders() {
        ((DefaultHttpClient)mHttpClient).addRequestInterceptor(new PrintRequestInterceptor());
    }

    public void setEntity(HttpEntity entity) {
        if(mHttpRequest.getMethod().equals("GET")) {
            SmartLog.i(TAG, "GET Method is not support entity.");
        } else {
            ((HttpPost)mHttpRequest).setEntity(entity);
        }
    }

    public void setEntity(MultipartEntity entity) {
        if(mHttpRequest.getMethod().equals("GET")) {
            SmartLog.i(TAG, "GET Method is not support entity.");
        } else {
            ((HttpPost)mHttpRequest).setEntity(entity);
        }
    }

    public void setTimeoutTime(int connectionTimout, int socketTimeout) {
        HttpParams httpParameters = new BasicHttpParams();

        HttpConnectionParams.setConnectionTimeout(httpParameters, connectionTimout);
        HttpConnectionParams.setSoTimeout(httpParameters, socketTimeout);

        ((DefaultHttpClient)mHttpClient).setParams(httpParameters);
    }

    public void disconnect() {
        mHttpClient.getConnectionManager().shutdown();
    }

    public HttpRequest getRequest() {
        return mHttpRequest;
    }

    public long getContentLength() {
        return mResponse.getEntity().getContentLength();
    }

    public InputStream getInputStream() throws IOException {
        InputStream responseStream = mResponse.getEntity().getContent();

        if(responseStream == null) {
            return responseStream;
        }

        Header h = mResponse.getEntity().getContentEncoding();
        if(h == null || !h.getValue().contains("gzip")) {
            return responseStream;
        }

        if(h.getValue().contains("gzip")) {
            responseStream = new GZIPInputStream(responseStream);
        }
        return responseStream;
    }
}
