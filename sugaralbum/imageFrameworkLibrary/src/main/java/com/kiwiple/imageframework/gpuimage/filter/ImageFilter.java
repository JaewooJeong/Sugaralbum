
package com.kiwiple.imageframework.gpuimage.filter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLES20;
import android.util.Log;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ImageOutput;
import com.kiwiple.imageframework.gpuimage.ShaderUtils;
import com.kiwiple.imageframework.gpuimage.ShaderUtils.ImageInput;
import com.kiwiple.imageframework.gpuimage.ShaderUtils.Size;

public class ImageFilter extends ImageOutput implements ImageInput {
    static final String TAG = ImageFilter.class.getName();
    // TODO: 파라미터 이름 정리 필요, 상수 정의 안된 부분도 많이 있음. 또는 상수 관련해서 별도 class로 관리하는 것도 고민...
    public static final String BLURSIZE = "Blur size";
    public static final String THRESHOLD = "Threshold";
    public static final String LINEWIDTH = "Line width";
    public static final String INTENSITY = "Intensity";

    /**
     * OpenGL. 쉐이더 프로그램
     */
    protected int mProgram;
    /**
     * Shader code 변수 index. Vertex 포지션
     */
    protected int mFilterPositionAttribute;
    /**
     * Shader code 변수 index. Texture 포지션
     */
    protected int mFilterTextureCoordinateAttribute;
    /**
     * Shader code 변수 index. Texture
     */
    protected int mFilterInputTextureUniform;

    /**
     * OpenGL. Frame buffer id
     */
    private int[] mFilterFramebuffer = new int[1];
    /**
     * OpenGL. Texture id 
     */
    protected int mFilterSourceTexture;

    public void initWithVertexShaderFromResource(Context context, String vertexShaderResName,
            String fragmentShaderResName) {
        super.init(context);
        String vertexShader = ShaderUtils.getShaderStringFromResource(context, vertexShaderResName);
        String fragmentShader = ShaderUtils.getShaderStringFromResource(context,
                                                                        fragmentShaderResName);

        mProgram = ShaderUtils.createProgram(vertexShader, fragmentShader);
        if(mProgram == 0)
            return;

        mFilterPositionAttribute = GLES20.glGetAttribLocation(mProgram, "position");
        if(mFilterPositionAttribute == -1)
            throw new RuntimeException("Could not get attrib location for mFilterPositionAttribute");

        mFilterTextureCoordinateAttribute = GLES20.glGetAttribLocation(mProgram,
                                                                       "inputTextureCoordinate");
        if(mFilterTextureCoordinateAttribute == -1)
            throw new RuntimeException("Could not get attrib location for inputTextureCoordinate");

        mFilterInputTextureUniform = GLES20.glGetUniformLocation(mProgram, "inputImageTexture");

        GLES20.glUseProgram(mProgram);

        GLES20.glEnableVertexAttribArray(mFilterPositionAttribute);
        GLES20.glEnableVertexAttribArray(mFilterTextureCoordinateAttribute);
    }

    public void initWithFragmentShaderFromResource(Context context, String fragmentShaderResName) {
        initWithVertexShaderFromResource(context, "vertex", fragmentShaderResName);
    }

    @Override
    public void init(Context context) {
        initWithFragmentShaderFromResource(context, "passthrough_fragment");
    }

    public void setupFilterForSize(Size filterFrameSize) {
    }

    @Override
    public void destroyAll() {
        super.destroyAll();
        destroyFilterFBO();
    }

    public Size sizeOfFBO() {
        Size outputSize = maximumOutputSize();
        if((outputSize.width == 0 && outputSize.height == 0)
                || (mInputTextureSize.width < outputSize.width)) {
            return mInputTextureSize;
        }
        return outputSize;
    }

    /**
     * Frame buffer를 생성하고 Texture에 binding한다.
     */
    public void createFilterFBOofSize(Size currentFBOSize) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glGenFramebuffers(1, mFilterFramebuffer, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFilterFramebuffer[0]);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mOutputTexture[0]);
        mOutputWidth = currentFBOSize.width;
        mOutputHeight = currentFBOSize.height;
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, (int)currentFBOSize.width,
                            (int)currentFBOSize.height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
                            null);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                                      GLES20.GL_TEXTURE_2D, mOutputTexture[0], 0);

        notifyTargetsAboutNewOutputTexture();

        if(GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.i(TAG, "Incomplete filter FBO");
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    public void destroyFilterFBO() {
        if(mFilterFramebuffer[0] != 0) {
            GLES20.glDeleteFramebuffers(1, mFilterFramebuffer, 0);
            mFilterFramebuffer[0] = 0;
        }
    }

    /**
     * Frame buffer가 생성 되지 않았으며 생성하고, binding한다.
     */
    public void setFilterFBO() {
        if(mFilterFramebuffer[0] == 0) {
            Size currentFBOSize = sizeOfFBO();
            createFilterFBOofSize(currentFBOSize);
            setupFilterForSize(currentFBOSize);
        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFilterFramebuffer[0]);

        Size currentFBOSize = sizeOfFBO();
        GLES20.glViewport(0, 0, (int)currentFBOSize.width, (int)currentFBOSize.height);
    }

    public void setOutputFBO() {
        // Override this for filters that have multiple framebuffers
        setFilterFBO();
    }

    /**
     * Rendering...
     */
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

        GLES20.glVertexAttribPointer(mFilterPositionAttribute, 2, GLES20.GL_FLOAT, false, 0,
                                     vertices);

        GLES20.glVertexAttribPointer(mFilterTextureCoordinateAttribute, 2, GLES20.GL_FLOAT, false,
                                     0, textureCoordinates);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    public void setUniformsForProgramAtIndex(int programIndex) {
    }

    public void informTargetsAboutNewFrameAtTime(final int frameTime) {
        for(ImageInput currentTarget : mTargets) {
            int indexOfObject = mTargets.indexOf(currentTarget);
            int textureIndex = mTargetTextureIndices.get(indexOfObject);

            // setInputTextureForTarget(currentTarget, textureIndex);
            currentTarget.setInputSize(outputFrameSize(), textureIndex);
            currentTarget.newFrameReadyAtTime(frameTime, textureIndex);
        }
    }

    private Size outputFrameSize() {
        return mInputTextureSize;
    }

    public void setFloat(float newFloat, String uniformName) {
        setFloat(newFloat, GLES20.glGetUniformLocation(mProgram, uniformName), mProgram);
    }

    public void setPoint(PointF pointValue, String uniformName) {
        setPoint(pointValue, GLES20.glGetUniformLocation(mProgram, uniformName), mProgram);
    }

    public void setPoint(PointF pointValue, int uniformName) {
        setPoint(pointValue, uniformName, mProgram);
    }

    public void setInteger(int intValue, int uniformName) {
        GLES20.glUseProgram(mProgram);
        GLES20.glUniform1i(uniformName, intValue);
    }

    public void setVec3(Vector3 vectorValue, int uniformName) {
        GLES20.glUseProgram(mProgram);
        GLES20.glUniform3f(uniformName, vectorValue.one, vectorValue.one, vectorValue.one);
    }

    public void setSize(Size sizeValue, int uniformName) {
        setSize(sizeValue, uniformName, mProgram);
    }

    public static void setFloat(float newFloat, int uniformName, int shaderProgram) {
        GLES20.glUseProgram(shaderProgram);
        GLES20.glUniform1f(uniformName, newFloat);
    }

    public static void setMatrix3(float[] newFloat, int uniformName, int shaderProgram) {
        GLES20.glUseProgram(shaderProgram);
        GLES20.glUniformMatrix3fv(uniformName, 1, false, newFloat, 0);
    }

    public static void setPoint(PointF pointValue, int uniformName, int shaderProgram) {
        GLES20.glUseProgram(shaderProgram);
        GLES20.glUniform2fv(uniformName, 1, new float[] {
                pointValue.x, pointValue.y
        }, 0);
    }

    public static void setInteger(int intValue, int uniformName, int shaderProgram) {
        GLES20.glUseProgram(shaderProgram);
        GLES20.glUniform1i(uniformName, intValue);
    }

    public static void setVec3(Vector3 vectorValue, int uniformName, int shaderProgram) {
        GLES20.glUseProgram(shaderProgram);
        GLES20.glUniform3f(uniformName, vectorValue.one, vectorValue.two, vectorValue.three);
    }

    public static void setSize(Size sizeValue, int uniformName, int shaderProgram) {
        GLES20.glUseProgram(shaderProgram);
        GLES20.glUniform2fv(uniformName, 1, new float[] {
                sizeValue.width, sizeValue.height
        }, 0);
    }

    protected static final float imageVertices_float[] = {
            -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f,
    };
    protected static final FloatBuffer imageVertices;
    static {
        ByteBuffer byteBuf = ByteBuffer.allocateDirect(8 * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        imageVertices = byteBuf.asFloatBuffer();
        imageVertices.put(imageVertices_float);
        imageVertices.position(0);
    }

    protected static final float noRotationTextureCoordinates_float[] = {
            0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f,
    };
    protected static final FloatBuffer noRotationTextureCoordinates;
    static {
        ByteBuffer byteBuf = ByteBuffer.allocateDirect(8 * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        noRotationTextureCoordinates = byteBuf.asFloatBuffer();
        noRotationTextureCoordinates.put(noRotationTextureCoordinates_float);
        noRotationTextureCoordinates.position(0);
    }

    @Override
    public void newFrameReadyAtTime(final int frameTime, int textureIndex) {
        renderToTextureWithVertices(imageVertices, noRotationTextureCoordinates,
                                    mFilterSourceTexture);
        informTargetsAboutNewFrameAtTime(frameTime);
    }

    @Override
    public int nextAvailableTextureIndex() {
        return 0;
    }

    @Override
    public void setInputTexture(int newInputTexture, int textureIndex) {
        mFilterSourceTexture = newInputTexture;
    }

    /**
     * Frame buffer를 재생성한다.
     */
    public void recreateFilterFBO() {
        if(mFilterFramebuffer[0] == 0) {
            return;
        }

        destroyFilterFBO();

        deleteOutputTexture();
        initializeOutputTexture();

        setFilterFBO();
    }

    @Override
    public void setInputSize(Size newSize, int textureIndex) {
        if(mOverrideInputSize) {
            if(mForcedMaximumSize.equals(new Size(0.f, 0.f))) {
                return;
            }
            return;
        }

        if(newSize.equals(new Size(0.f, 0.f))) {
            mInputTextureSize = newSize;
        } else if(!mInputTextureSize.equals(newSize)) {
            mInputTextureSize = newSize;
            recreateFilterFBO();
        }
    }

    @Override
    public void forceProcessingAtSize(Size frameSize) {
        if(frameSize.equals(new Size(0.f, 0.f))) {
            mOverrideInputSize = false;
        } else {
            mOverrideInputSize = true;
            mInputTextureSize = frameSize;
            mForcedMaximumSize = new Size(0.f, 0.f);
        }

        destroyFilterFBO();

        for(ImageInput currentTarget : mTargets) {
            if(currentTarget instanceof ImageTwoPassFilter) {
                ((ImageTwoPassFilter)currentTarget).destroyFilterFBO();
            }
        }
    }

    @Override
    public Size maximumOutputSize() {
        return new Size(0.f, 0.f);
    }

    @Override
    public void endProcessing() {
        for(ImageInput currentTarget : mTargets) {
            currentTarget.endProcessing();
        }
    }

    @Override
    public float getOutputWidth() {
        return mOutputWidth;
    }

    @Override
    public float getOutputHeight() {
        return mOutputHeight;
    }

    protected float mOutputWidth = 0;
    protected float mOutputHeight = 0;

    public class Vector3 {
        public Vector3(float one, float two, float three) {
            this.one = one;
            this.two = two;
            this.three = three;
        }

        public Vector3() {
        }

        public float one;
        public float two;
        public float three;
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        return null;
    }

    @Override
    public ArtFilterInfo getSecondFilterInfo() {
        return null;
    }
}
