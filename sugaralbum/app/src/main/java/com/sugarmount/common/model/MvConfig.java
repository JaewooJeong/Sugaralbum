package com.sugarmount.common.model;


import android.Manifest;
import android.os.Build;

import androidx.annotation.RequiresApi;

/**
 * Created by Jaewoo on 2016-08-19.
 */
public interface MvConfig {
    boolean debug = false;

    String TAG = "SugarAlbum";
    String RELEASE_HOST = "";

    String EXTRA_PERMISSION         = "SugarAlbum.permmission";
    String EXTRA_INFO_TYPE          = "SugarAlbum.info_type";
    String EXTRA_URI_INFO           = "SugarAlbum.uri_info";

    int MY_PERMISSION_REQUEST = 10010;
    int MY_VIDEO_REQUEST = 10011;
    int MY_FINISH_REQUEST = 10012;

    String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE };

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    String[] PERMISSIONS33 = {
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.WRITE_EXTERNAL_STORAGE };

    int READ_TIMEOUT = 60;
    int WRITE_TIMEOUT = 5;
    int CONNECT_TIMEOUT = 5;
    int MAX_ITEMS = 20;
    long SIMULATED_LOADING_TIME_IN_MS = 100;
    long INTRO_TIME = 2000;
    long PROGRESS_TIME = 1000;
    long SEARCH_OPEN_TIME = 300;
    int MAX_ITEMS_PER_REQUEST = 10;
    int MAX_LIST_STRING_COUNT = 700;
    int MAX_STRING_COUNT = 3500;

    int UPDATE_MAIN_INIT = 12204;
    int UPDATE_OFFLINE = 12206;
    int UPDATE_DRAWER_CLOSE = 12207;
    int UPDATE_LOGIN = 12208;
    int UPDATE_LOGOUT = 12209;
    int UPDATE_PROFILE = 12210;
    int UPDATE_SEARCH = 12211;
    int UPDATE_SEARCH_UPDATE = 12212;
    int UPDATE_CLIP = 12213;
    int UPDATE_PROGRESS = 12214;
    int UPDATE_TEXT = 12215;
    int UPDATE_TEXTS = 12216;

    enum SEARCH_MODE {
        SEARCH,
        DEFAULT;
    }

    enum CLIP_TYPE {
        NONE(0),
        KAKAO(1),
        KEEP(2),
        ONENOTE(3),
        EVERNOTE(4);

        public int rc;

        CLIP_TYPE(int rc) {
            this.rc = rc;
        }

        public static String valueOfStr(CLIP_TYPE rc) {
            switch (rc) {
                case KAKAO:
                    return "kakao";
                case KEEP:
                    return "keep";
                case ONENOTE:
                    return "onenote";
                case EVERNOTE:
                    return "evernote";
                default:
                    return "";
            }
        }

        public static CLIP_TYPE strOfValue(String rc) {
            switch (rc) {
                case "kakao":
                    return KAKAO;
                case "keep":
                    return KEEP;
                case "onenote":
                    return ONENOTE;
                case "evernote":
                    return EVERNOTE;
                default:
                    return NONE;
            }
        }
    }

    enum SNS_TYPE {
        NONE(0),
        KAKAO(1),
        LINE(2),
        FACEBOOK(3),
        INSTARGRAM(4);

        public int rc;

        SNS_TYPE(int rc) {
            this.rc = rc;
        }
    }

    enum CONTENTS_DOWNLOAD_TYPE {
        INTERNAL,
        EXTERNAL
    }

    enum INFO_TYPE {
        NOTICE(1),
        FAQ(2),
        LICENSE(3);

        public int rc;

        INFO_TYPE(int rc) {
            this.rc = rc;
        }
    }

    enum POPUP_TYPE {
        PERMISSION,
        NETWORK,
        DELETE,
        NFC,
        VIDEO,
        DOWNLOAD,
        VERSION,
        EMAIL,
        ADMOB,
        PROGRESS,
    }

    enum PAGE_DIRECTION {
        MAIN(2),
        INFORMATION(7),
        NAV(8),
        LOGOUT(9),
        CLIP(10);
        public int rc;

        PAGE_DIRECTION(int rc) {
            this.rc = rc;
        }
    }

    enum REQUEST_TYPE {
        GET,
        POST,
        PUT,
        DELETE;

        public static String valueOf(REQUEST_TYPE rc) {
            switch (rc) {
                case GET:
                    return "GET";
                case POST:
                    return "POST";
                case PUT:
                    return "PUT";
                case DELETE:
                    return "DELETE";
                default:
                    return "";
            }
        }
    }

    enum REQUEST_CALLBACK {
        NULL_LIST(1),
        NAVER(2),
        DAUM(3);
        public int rc;

        REQUEST_CALLBACK(int rc) {
            this.rc = rc;
        }

        public static String valueOfStr(REQUEST_CALLBACK rc) {
            switch (rc) {
                case NAVER:
                    return "https://m.search.naver.com/p/csearch/ocontent/spellchecker.nhn";
                case DAUM:
                    return "https://search.daum.net/qsearch";
                default:
                    return "";
            }
        }

        public static REQUEST_CALLBACK valueOf(int rc) {
            for (int i = 0; i < values().length; i++) {
                if (values()[i].rc == rc) {
                    return values()[i];
                }
            }
            return NULL_LIST;
        }
    }
}
