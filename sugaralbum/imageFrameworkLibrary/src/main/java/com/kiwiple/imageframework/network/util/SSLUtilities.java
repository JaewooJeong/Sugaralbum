
package com.kiwiple.imageframework.network.util;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

import com.kiwiple.imageframework.util.SmartLog;

public final class SSLUtilities {
    private static final String TAG = SSLUtilities.class.getSimpleName();

    private static TrustManager[] trustManagers;

    public static class _FakeX509TrustManager implements javax.net.ssl.X509TrustManager {
        private static final X509Certificate[] _AcceptedIssuers = new X509Certificate[] {};

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1)
                throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1)
                throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return (_AcceptedIssuers);
        }
    }

    public static void allowAllSSL() {
        javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {

                SmartLog.i(TAG, "Host name : " + hostname + " ");

                String names[] = session.getValueNames();

                for(String name : names) {
                    SmartLog.i(TAG, "Name : " + name + " " + "Value : " + session.getValue(name));
                }

                return true;
            }
        });
        javax.net.ssl.SSLContext context = null;
        if(trustManagers == null) {
            trustManagers = new javax.net.ssl.TrustManager[] {
                new _FakeX509TrustManager()
            };
        }
        try {
            context = javax.net.ssl.SSLContext.getInstance("TLS");
            context.init(null, trustManagers, new SecureRandom());
            javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        } catch(NoSuchAlgorithmException e) {
            SmartLog.e(TAG, "allowAllSSL", e);
        } catch(KeyManagementException e) {
            SmartLog.e(TAG, "allowAllSSL", e);
        }
    }
}
