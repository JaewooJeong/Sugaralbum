/*!    
	@file		FilterManager.java
	@date		2012-09-19
 */

package com.kiwiple.imageframework.gpuimage;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGL11;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

import android.util.Log;

/**
 * OpenGL을 초기화 해주는 클래스
 */
public class ArtFilterManager {
    final static String TAG = ArtFilterManager.class.getName();

    private EGL10 mEGL;
    private EGLDisplay mEGLDisplay;
    private EGLConfig mEGLConfig;
    private EGLContext mEGLContext;
    private EGLSurface mEGLSurface;

    private static ArtFilterManager mArtFilterManager;

    public static ArtFilterManager getInstance() {
        if(mArtFilterManager == null) {
            mArtFilterManager = new ArtFilterManager();
        }
        return mArtFilterManager;
    }

    public void initGL(int width, int height) {
        mEGL = (EGL10)EGLContext.getEGL();

        mEGLDisplay = mEGL.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        if(mEGLDisplay == EGL10.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglGetDisplay failed "
                    + getEGLErrorString(mEGL.eglGetError()));
        }

        int[] version = new int[2];
        if(!mEGL.eglInitialize(mEGLDisplay, version)) {
            throw new RuntimeException("eglInitialize failed "
                    + getEGLErrorString(mEGL.eglGetError()));
        }

        mEGLConfig = chooseEglConfig();
        if(mEGLConfig == null) {
            throw new RuntimeException("eglConfig not initialized");
        }

        mEGLContext = createContext(mEGL, mEGLDisplay, mEGLConfig);
        if(mEGLConfig == null) {
            throw new RuntimeException("create eglContext failted");
        }

        int[] surfaceSize = {
                EGL10.EGL_WIDTH, width, EGL10.EGL_HEIGHT, height, EGL10.EGL_NONE
        };
        mEGLSurface = mEGL.eglCreatePbufferSurface(mEGLDisplay, mEGLConfig, surfaceSize);

        if(mEGLSurface == null || mEGLSurface == EGL10.EGL_NO_SURFACE) {
            int error = mEGL.eglGetError();
            if(error == EGL10.EGL_BAD_NATIVE_WINDOW) {
                Log.e("test", "createWindowSurface returned EGL_BAD_NATIVE_WINDOW.");
                return;
            }
            throw new RuntimeException("createWindowSurface failed " + getEGLErrorString(error));
        }

        if(!mEGL.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext)) {
            throw new RuntimeException("eglMakeCurrent failed "
                    + getEGLErrorString(mEGL.eglGetError()));
        }
    }

    private static String getEGLErrorString(int error) {
        switch(error) {
            case EGL10.EGL_SUCCESS:
                return "EGL_SUCCESS";
            case EGL10.EGL_NOT_INITIALIZED:
                return "EGL_NOT_INITIALIZED";
            case EGL10.EGL_BAD_ACCESS:
                return "EGL_BAD_ACCESS";
            case EGL10.EGL_BAD_ALLOC:
                return "EGL_BAD_ALLOC";
            case EGL10.EGL_BAD_ATTRIBUTE:
                return "EGL_BAD_ATTRIBUTE";
            case EGL10.EGL_BAD_CONFIG:
                return "EGL_BAD_CONFIG";
            case EGL10.EGL_BAD_CONTEXT:
                return "EGL_BAD_CONTEXT";
            case EGL10.EGL_BAD_CURRENT_SURFACE:
                return "EGL_BAD_CURRENT_SURFACE";
            case EGL10.EGL_BAD_DISPLAY:
                return "EGL_BAD_DISPLAY";
            case EGL10.EGL_BAD_MATCH:
                return "EGL_BAD_MATCH";
            case EGL10.EGL_BAD_NATIVE_PIXMAP:
                return "EGL_BAD_NATIVE_PIXMAP";
            case EGL10.EGL_BAD_NATIVE_WINDOW:
                return "EGL_BAD_NATIVE_WINDOW";
            case EGL10.EGL_BAD_PARAMETER:
                return "EGL_BAD_PARAMETER";
            case EGL10.EGL_BAD_SURFACE:
                return "EGL_BAD_SURFACE";
            case EGL11.EGL_CONTEXT_LOST:
                return "EGL_CONTEXT_LOST";
            default:
                return "0x" + Integer.toHexString(error);
        }
    }

    private static int[] getConfig() {
        return new int[] {
                EGL10.EGL_RENDERABLE_TYPE, 4, EGL10.EGL_RED_SIZE, 8, EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8, EGL10.EGL_ALPHA_SIZE, 8, EGL10.EGL_DEPTH_SIZE, 0,
                EGL10.EGL_STENCIL_SIZE, 0, EGL10.EGL_NONE
        };
    }

    private EGLConfig chooseEglConfig() {
        int[] configsCount = new int[1];
        EGLConfig[] configs = new EGLConfig[1];
        int[] configSpec = getConfig();
        if(!mEGL.eglChooseConfig(mEGLDisplay, configSpec, configs, 1, configsCount)) {
            throw new IllegalArgumentException("eglChooseConfig failed "
                    + getEGLErrorString(mEGL.eglGetError()));
        } else if(configsCount[0] > 0) {
            return configs[0];
        }
        return null;
    }

    private static EGLContext createContext(EGL10 egl, EGLDisplay eglDisplay, EGLConfig eglConfig) {
        int[] attrib_list = {
                0x3098, 2, EGL10.EGL_NONE
        };
        return egl.eglCreateContext(eglDisplay, eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list);
    }

    public void deinitGL() {
        if(mEGLSurface != null)
            mEGL.eglDestroySurface(mEGLDisplay, mEGLSurface);
        mEGLSurface = null;
        if(mEGLContext != null)
            mEGL.eglDestroyContext(mEGLDisplay, mEGLContext);
        mEGLContext = null;
    }
}
