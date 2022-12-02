
package com.kiwiple.imageframework.gpuimage.filter;

import java.nio.FloatBuffer;

import android.content.Context;
import android.opengl.GLES20;

import com.kiwiple.imageframework.gpuimage.ShaderUtils.Size;

/**
 * 두 개의 input을 처리 하는 필터. 일반적으로 blend 필터에서 상속 받아 사용한다.
 */
public class ImageTwoInputFilter extends ImageFilter {
    protected int mFilterSecondTextureCoordinateAttribute;
    protected int mFilterInputTextureUniform2;

    /**
     * 두 번째 텍스처
     */
    private int mFilterSourceTexture2;

    /**
     * 첫 번째 텍스처가 설정 되었는지 여부
     */
    private boolean mHasSetFirstTexture;
    /**
     * 첫 번째 프레임이 입력 되었는지 여부
     */
    private boolean mHasReceivedFirstFrame;
    /**
     * 두 번째 프레임이 입력 되었는지 여부
     */
    private boolean mHasReceivedSecondFrame;
    /**
     * 두 번째 프레임 비활성화 여부
     */
    private boolean mSecondFrameCheckDisabled;

    @Override
    public void initWithFragmentShaderFromResource(Context context, String fragmentShaderResName) {
        initWithVertexShaderFromResource(context, "two_input_texture_vertex", fragmentShaderResName);
    }

    @Override
    public void initWithVertexShaderFromResource(Context context, String vertexShaderResName,
            String fragmentShaderResName) {
        super.initWithVertexShaderFromResource(context, vertexShaderResName, fragmentShaderResName);

        mHasSetFirstTexture = false;

        mHasReceivedFirstFrame = false;
        mHasReceivedSecondFrame = false;

        mSecondFrameCheckDisabled = false;

        mFilterSecondTextureCoordinateAttribute = GLES20.glGetAttribLocation(mProgram,
                                                                             "inputTextureCoordinate2");
        if(mFilterSecondTextureCoordinateAttribute == -1)
            throw new RuntimeException("Could not get attrib location for inputTextureCoordinate2");

        mFilterInputTextureUniform2 = GLES20.glGetUniformLocation(mProgram, "inputImageTexture2");

        GLES20.glEnableVertexAttribArray(mFilterSecondTextureCoordinateAttribute);
    }

    public void disableSecondFrameCheck() {
        mSecondFrameCheckDisabled = true;
    }

    @Override
    public void renderToTextureWithVertices(FloatBuffer vertices, FloatBuffer textureCoordinates,
            final int sourceTexture) {
        GLES20.glUseProgram(mProgram);
        // change order..
        setFilterFBO();
        setUniformsForProgramAtIndex(0);

        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, sourceTexture);
        GLES20.glUniform1i(mFilterInputTextureUniform, 2);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFilterSourceTexture2);
        GLES20.glUniform1i(mFilterInputTextureUniform2, 3);

        GLES20.glVertexAttribPointer(mFilterPositionAttribute, 2, GLES20.GL_FLOAT, false, 0,
                                     vertices);

        GLES20.glVertexAttribPointer(mFilterTextureCoordinateAttribute, 2, GLES20.GL_FLOAT, false,
                                     0, textureCoordinates);

        GLES20.glVertexAttribPointer(mFilterSecondTextureCoordinateAttribute, 2, GLES20.GL_FLOAT,
                                     false, 0, textureCoordinates);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    @Override
    public int nextAvailableTextureIndex() {
        if(mHasSetFirstTexture) {
            return 1;
        }
        return 0;
    }

    @Override
    public void setInputTexture(int newInputTexture, int textureIndex) {
        if(textureIndex == 0) {
            mFilterSourceTexture = newInputTexture;
            mHasSetFirstTexture = true;
        } else {
            mFilterSourceTexture2 = newInputTexture;
        }
    }

    @Override
    public void setInputSize(Size newSize, int textureIndex) {
        if(textureIndex == 0) {
            super.setInputSize(newSize, textureIndex);

            if(newSize.height == 0 && newSize.width == 0) {
                mHasSetFirstTexture = false;
            }
        }
    }

    @Override
    public void newFrameReadyAtTime(final int frameTime, int textureIndex) {
        if(mHasReceivedFirstFrame && mHasReceivedSecondFrame) {
            return;
        }

        // 2개의 texture가 모두 준비되면 rendering한다.
        if(textureIndex == 0) {
            mHasReceivedFirstFrame = true;
            if(mSecondFrameCheckDisabled) {
                mHasReceivedSecondFrame = true;
            }
        } else {
            mHasReceivedSecondFrame = true;
        }
        if(mHasReceivedFirstFrame && mHasReceivedSecondFrame) {
            super.newFrameReadyAtTime(frameTime, 0);
            mHasReceivedFirstFrame = false;
            mHasReceivedSecondFrame = false;
        }
    }
}
