
package com.kiwiple.imageframework.gpuimage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.content.Context;
import android.opengl.GLES20;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import androidx.collection.LruCache;

public class ShaderUtils {
    static final String TAG = ShaderUtils.class.getName();
    public static boolean USE_CACHE = true;

    // ---------------------------------------------------------
    static public class Size {
        public Size() {
            width = 0;
            height = 0;
        }

        public Size(final float width, final float height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public boolean equals(Object o) {
            if(o instanceof Size) {
                Size s = (Size)o;
                if(this.width == s.width && this.height == s.height)
                    return true;
            }
            return false;

        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        public float width;
        public float height;
    }

    public static LruCache<String, Integer> sProgramCache = new LruCache<String, Integer>((30)) {
        @Override
        protected int sizeOf(String key, Integer value) {
            return 1;
        }

        @Override
        protected void entryRemoved(boolean evicted, String key, Integer oldValue, Integer newValue) {
        }
    };

    /**
     * @brief 쉐이더 프로그램을 로드하고 컴파일 & 링크한다.
     * @param vertexSource 버텍스쉐이더 코드이다.
     * @param fragmentSource 플래그먼트쉐이더 코드이다.
     * @return 쉐이더 프로그램 식별자 정수이다.
     */
    static public int createProgram(String vertexSource, String fragmentSource) {
        String lookupKye = new StringBuilder().append(Thread.currentThread().getName())
                                              .append("V: ").append(vertexSource).append(" - F: ")
                                              .append(fragmentSource).toString();
        Integer program = null;
        if(USE_CACHE) {
            program = sProgramCache.get(lookupKye);
        }
        int[] linkStatus = new int[1];
        if(program != null) {
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if(linkStatus[0] == GLES20.GL_TRUE) {
                GLES20.glGetProgramiv(program, GLES20.GL_ATTACHED_SHADERS, linkStatus, 0);
                if(linkStatus[0] == 2) {
                    GLES20.glGetProgramiv(program, GLES20.GL_DELETE_STATUS, linkStatus, 0);
                    if(linkStatus[0] == GLES20.GL_FALSE) {
                        Log.i(TAG, "program cache hit");
                        return program;
                    }
                }
            }
            Log.e(TAG, "Program invalid");
            GLES20.glDeleteProgram(program);
            sProgramCache.remove(lookupKye);
        }

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if(vertexShader == 0)
            return 0;
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if(pixelShader == 0)
            return 0;

        program = GLES20.glCreateProgram();
        if(program != 0) {
            GLES20.glAttachShader(program, vertexShader);
            GLES20.glAttachShader(program, pixelShader);
            GLES20.glLinkProgram(program);
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if(linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link program: ");
                Log.e(TAG, GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }

        if(program != 0) {
            sProgramCache.put(lookupKye, program);
        }

        return program;
    }

    /**
     * @brief 쉐이더 프로그램을 로드하고 컴파일 & 링크한다.
     * @param shaderType 쉐이더 타입니다.
     * @param source 쉐이더 코드이다.
     * @return 로드한 쉐이더 프로그램 컴파일 식별자이다.
     */
    static public int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if(shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if(compiled[0] == 0) {
                Log.e(TAG, "Could not compile shader " + shaderType + ":");
                Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    /**
     * @brief 입력 이미지 크기를 기준으로 현재 GPU가 지원하는 최대 텍스처 크기에 맞는 크기를 계산한다.
     * @param inputSize 입력 이미지 크기
     * @return 최대 텍스처 크기에 맞게 계산한 크기
     */
    static public Size sizeThatFitsWithinATextureForSize(final Size inputSize) {
        // --------------------------------------------------------------------------------
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4);
        byteBuffer.order(ByteOrder.nativeOrder());
        GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, byteBuffer.asIntBuffer());
        byteBuffer.position(0);
        final int maxTextureSize = byteBuffer.getInt(); // GPU가 지원하는 최대 텍스쳐 크기
        // --------------------------------------------------------------------------------
        if((inputSize.width < maxTextureSize) && (inputSize.height < maxTextureSize))
            return inputSize;

        Size adjustedSize = new Size();
        if(inputSize.width > inputSize.height) {
            adjustedSize.width = maxTextureSize;
            adjustedSize.height = (int)((maxTextureSize / inputSize.width) * inputSize.height);
        } else {
            adjustedSize.height = maxTextureSize;
            adjustedSize.width = (int)((maxTextureSize / inputSize.height) * inputSize.width);
        }

        return adjustedSize;
    }

    private static LruCache<String, String> sShaderCache = new LruCache<String, String>((30)) {
        @Override
        protected int sizeOf(String key, String value) {
            return 1;
        }

        @Override
        protected void entryRemoved(boolean evicted, String key, String oldValue, String newValue) {
        }
    };

    public static String getShaderStringFromResource(Context context, String shaderResName) {
        if(TextUtils.isEmpty(shaderResName)) {
            Log.i(TAG, "getShaderStringFromAsset shader is null");
        }
        String vertexShader = null;
        if(USE_CACHE) {
            vertexShader = sShaderCache.get(shaderResName);
        }
        if(!TextUtils.isEmpty(vertexShader)) {
            Log.i(TAG, "shader cache hit");
            return new String(Base64.decode(vertexShader, Base64.DEFAULT));
        }
        vertexShader = new String();
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            inputStream = context.getResources()
                                 .openRawResource(context.getResources()
                                                         .getIdentifier(shaderResName, "raw",
                                                                        context.getPackageName()));
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = reader.readLine();
            while(line != null) {
                vertexShader += line + '\n';
                line = reader.readLine();
            }
        } catch(IOException e) {
        } finally {
            try {
                if(inputStream != null) {
                    inputStream.close();
                }
                if(reader != null) {
                    reader.close();
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        sShaderCache.put(shaderResName, vertexShader);
        // use base64 only 3rd-party library.
        return new String(Base64.decode(vertexShader, Base64.DEFAULT));
    }

    public static interface ImageInput {
        public void newFrameReadyAtTime(final int frameTime, int textureIndex);

        /**
         * @brief 필터의 입력 텍스처 OpenGL ES 바인드 id를 설정한다.
         * @param newInputTexture 입력 소스로 사용할 OpenGL ES 텍스처 바인드 id이다.
         * @return 없음
         */
        public void setInputTexture(int newInputTexture, int textureIndex);

        public int nextAvailableTextureIndex();

        /**
         * @brief 입력 이미지의 크기에 맞게 필터 프레임 퍼퍼의 다시 생성한다.
         * @param gl OpenGL ES 객체
         * @return 없음
         */
        public void setInputSize(Size newSize, int textureIndex);

        /**
         * @brief 출력 최대 크기를 반환한다.
         * @return Size 객체
         */
        public Size maximumOutputSize();

        public void endProcessing();

        /**
         * @brief 출력 텍스처(결과)의 폭을 얻는다.
         * @return 출력 텍스처의 폭
         */
        public float getOutputWidth();

        /**
         * @brief 출력 텍스처(결과)의 높이를 얻는다.
         * @return 출력 텍스처의 높이
         */
        public float getOutputHeight();
    }
}
