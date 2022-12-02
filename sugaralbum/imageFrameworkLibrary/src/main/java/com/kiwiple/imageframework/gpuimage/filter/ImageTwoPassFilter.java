
package com.kiwiple.imageframework.gpuimage.filter;

import java.nio.FloatBuffer;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.kiwiple.imageframework.gpuimage.ShaderUtils;
import com.kiwiple.imageframework.gpuimage.ShaderUtils.Size;

/**
 * gaussian blur, edge detection과 같이 가로/세로 방향으로 2번 rendering하는 필터들의 부모 클래스
 */
public class ImageTwoPassFilter extends ImageFilter {
    /**
     * OpenGL. 가로 방향 rendering을 위한 shader program
     */
    protected int mSecondProgram;
    /**
     * Shader code 변수 index. Vertex 포지션
     */
    protected int mSecondFilterPositionAttribute;
    /**
     * Shader code 변수 index. Texture 포지션
     */
    protected int mSecondFilterTextureCoordinateAttribute;
    /**
     * Shader code 변수 index. Texture
     */
    protected int mSecondFilterInputTextureUniform;
    /**
     * TODO: 사용하지 않는듯... 삭제 필요
     */
    protected int mSecondFilterInputTextureUniform2;

    /**
     * OpenGL. Texture id 
     */
    public int[] mSecondFilterOutputTexture = new int[1];

    /**
     * OpenGL. Frame buffer id
     */
    private int[] mSecondFilterFramebuffer = new int[1];

    public void initWithFirstStageVertexShaderFromResource(Context context,
            String firstStageVertexShaderResName, String firstStageFragmentShaderResName,
            String secondStageVertexShaderResName, String secondStageFragmentShaderResName) {
        // 가로 방향
        super.initWithVertexShaderFromResource(context, firstStageVertexShaderResName,
                                               firstStageFragmentShaderResName);

        // 세로 방향
        String vertexShader = ShaderUtils.getShaderStringFromResource(context,
                                                                      secondStageVertexShaderResName);
        String fragmentShader = ShaderUtils.getShaderStringFromResource(context,
                                                                        secondStageFragmentShaderResName);

        mSecondProgram = ShaderUtils.createProgram(vertexShader, fragmentShader);
        if(mSecondProgram == 0)
            return;

        mSecondFilterPositionAttribute = GLES20.glGetAttribLocation(mSecondProgram, "position");
        if(mSecondFilterPositionAttribute == -1)
            throw new RuntimeException("Could not get attrib location for mFilterPositionAttribute");

        mSecondFilterTextureCoordinateAttribute = GLES20.glGetAttribLocation(mSecondProgram,
                                                                             "inputTextureCoordinate");
        if(mSecondFilterTextureCoordinateAttribute == -1)
            throw new RuntimeException("Could not get attrib location for inputTextureCoordinate");

        mSecondFilterInputTextureUniform = GLES20.glGetUniformLocation(mSecondProgram,
                                                                       "inputImageTexture");
        mSecondFilterInputTextureUniform2 = GLES20.glGetUniformLocation(mSecondProgram,
                                                                        "inputImageTexture2");

        GLES20.glUseProgram(mSecondProgram);

        GLES20.glEnableVertexAttribArray(mSecondFilterPositionAttribute);
        GLES20.glEnableVertexAttribArray(mSecondFilterTextureCoordinateAttribute);
    }

    public void initWithFirstStageFragmentShaderFromResource(Context context,
            String firstStageFragmentShaderResName, String secondStageFragmentShaderResName) {
        initWithFirstStageVertexShaderFromResource(context, "vertex",
                                                   firstStageFragmentShaderResName, "vertex",
                                                   secondStageFragmentShaderResName);
    }

    @Override
    public int textureForOutput() {
        return mSecondFilterOutputTexture[0];
    }

    @Override
    public void initializeOutputTexture() {
        // 가로 방향
        super.initializeOutputTexture();

        // 세로 방향
        GLES20.glGenTextures(1, mSecondFilterOutputTexture, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mSecondFilterOutputTexture[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        // This is necessary for non-power-of-two textures
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                               GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                               GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    @Override
    public void deleteOutputTexture() {
        // 가로 방향
        super.deleteOutputTexture();

        // 세로 방향
        if(mSecondFilterOutputTexture[0] != 0) {
            GLES20.glDeleteTextures(1, mSecondFilterOutputTexture, 0);
            mSecondFilterOutputTexture[0] = 0;
        }
    }

    @Override
    public void createFilterFBOofSize(Size currentFBOSize) {
        // 가로 방향
        super.createFilterFBOofSize(currentFBOSize);

        // 세로 방향
        GLES20.glGenFramebuffers(1, mSecondFilterFramebuffer, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mSecondFilterFramebuffer[0]);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mSecondFilterOutputTexture[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, (int)currentFBOSize.width,
                            (int)currentFBOSize.height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
                            null);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                                      GLES20.GL_TEXTURE_2D, mSecondFilterOutputTexture[0], 0);

        notifyTargetsAboutNewOutputTexture();

        if(GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.i(TAG, "Incomplete filter FBO");
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    @Override
    public void destroyFilterFBO() {
        // 가로 방향
        super.destroyFilterFBO();
        
        // 세로 방향
        if(mSecondFilterFramebuffer[0] != 0) {
            GLES20.glDeleteFramebuffers(1, mSecondFilterFramebuffer, 0);
            mSecondFilterFramebuffer[0] = 0;
        }
    }

    public void setSecondFilterFBO() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mSecondFilterFramebuffer[0]);
    }

    @Override
    public void setOutputFBO() {
        // Override this for filters that have multiple framebuffers
        setSecondFilterFBO();
    }

    @Override
    public void renderToTextureWithVertices(FloatBuffer vertices, FloatBuffer textureCoordinates,
            final int sourceTexture) {
        // 세로 방향 rendering
        super.renderToTextureWithVertices(vertices, textureCoordinates, sourceTexture);
        
        // 가로 방향 rendering
        setSecondFilterFBO();

        GLES20.glUseProgram(mSecondProgram);
        setUniformsForProgramAtIndex(1);

        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mOutputTexture[0]);

        GLES20.glUniform1i(mSecondFilterInputTextureUniform, 3);

        GLES20.glVertexAttribPointer(mSecondFilterPositionAttribute, 2, GLES20.GL_FLOAT, false, 0,
                                     vertices);

        GLES20.glVertexAttribPointer(mSecondFilterTextureCoordinateAttribute, 2, GLES20.GL_FLOAT,
                                     false, 0, textureCoordinates);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }
}
