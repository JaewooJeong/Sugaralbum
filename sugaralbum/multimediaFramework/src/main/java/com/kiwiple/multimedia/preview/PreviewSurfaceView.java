package com.kiwiple.multimedia.preview;

import static com.kiwiple.multimedia.preview.PreviewManager.DEFAULT_PREVIEW_RESOLUTION;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES11;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.canvas.PixelCanvas;

/**
 * {@link PreviewManager}와 연동하여 일련의 이미지를 연속적으로 출력하기 위한 {@link View} 클래스.
 * 
 * @see GLSurfaceView
 */
public class PreviewSurfaceView extends GLSurfaceView {

	private BufferPool mBufferPool = BufferPool.NULL_OBJECT;
	private int maVertices;
	private int maTextureCoor;

	/**
	 * 새로운 {@code PreviewSurfaceView}를 생성합니다.
	 */
	public PreviewSurfaceView(Context context) {
		super(context);
		initialize(context);
	}

	/**
	 * XML 전개를 통해 새로운 {@code PreviewSurfaceView}를 생성합니다.
	 */
	public PreviewSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(context);
	}

	private void initialize(Context context) {
		Precondition.checkNotNull(context);
		
		setEGLContextClientVersion(2);
		setRenderer(mRenderer);
		
	}

	void bindBufferPool(BufferPool bufferPool) {
		Precondition.checkNotNull(bufferPool);
		mBufferPool = bufferPool;
	}

	/**
	 * 화면을 새롭게 갱신합니다.
	 */
	public void requestInvalidate() {
		mRenderingHandler.sendEmptyMessage(0);
	}


    private final String vertexShaderCode =
            "attribute vec4 aVertices;\n" +
            "attribute vec2 aTextureCoord;\n" +
            "varying vec2 vTextureCoord;\n" +
            "void main() {\n" +
            "  gl_Position = aVertices;\n" +
            "  vTextureCoord = aTextureCoord;\n" +
            "}\n";

        private final String fragmentShaderCode =
            "precision mediump float;\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform sampler2D sTexture;\n" +
            "void main() {\n" +
            "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
            "}\n";
	
	private final GLSurfaceView.Renderer mRenderer = new Renderer() {

		private FloatBuffer mVertexBuffer;
		private FloatBuffer mTextureCoordinateBuffer;

		private int mTextureId;
		private int mPreviewBufferWidth;
		private int mPreviewBufferHeight;
		private int mImageTexture;

		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			setRenderMode(RENDERMODE_WHEN_DIRTY);

			mPreviewBufferWidth = DEFAULT_PREVIEW_RESOLUTION.width;
			mPreviewBufferHeight = DEFAULT_PREVIEW_RESOLUTION.height;

			final int progId = PreviewSurfaceViewGraphicsUtils.createProgram(vertexShaderCode, fragmentShaderCode);
			PreviewSurfaceViewGraphicsUtils.activateProgram(progId);
			
			GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
			
			maVertices = GLES20.glGetAttribLocation(progId, "aVertices");
			GLES20.glEnableVertexAttribArray(maVertices);
			maTextureCoor = GLES20.glGetAttribLocation(progId, "aTextureCoord");
			GLES20.glEnableVertexAttribArray(maTextureCoor);
			mImageTexture = GLES20.glGetUniformLocation(progId, "sTexture");

//			GLES20.glUniform1f(mImageTexture, 2);
			// sampler to texture to 0    cause texture bind error 
			GLES20.glUniform1i(mImageTexture, 0);
			
			
			mTextureId = PreviewSurfaceViewGraphicsUtils.getCurrentTestureId();
			// Texture ID 바인딩
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
			PreviewSurfaceViewGraphicsUtils.checkGlError("glBindTexture");

			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			// This is necessary for non-power-of-two textures, 부재시 npot 사이즈 display 안됨 
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

			prepareTextureCoordinateBuffer();
		}

		@Override
		public void onSurfaceChanged(GL10 gl, int width, int height) {
			GLES20.glViewport(0, 0, width, height);
			PreviewSurfaceViewGraphicsUtils.checkGlError("glViewport");
			prepareVertexBuffer(width, height);
			// 텍셀 정보 저장 
			GLES20.glVertexAttribPointer(maTextureCoor,2, GLES20.GL_FLOAT, false, 0,  mTextureCoordinateBuffer);
			PreviewSurfaceViewGraphicsUtils.checkGlError("glVertexAttribPointer");
			// 텍셀 정보 저
			GLES20.glVertexAttribPointer(maVertices, 2, GLES20.GL_FLOAT, false, 0,mVertexBuffer);
			PreviewSurfaceViewGraphicsUtils.checkGlError("glVertexAttribPointer");
		}

		@Override
		public void onDrawFrame(GL10 gl) {

			PixelCanvas canvas = mBufferPool.getLastReadBufferWithLock();
			if (canvas == null) {
				return;
			}
			//런타임중 픽셀 변경시 퍼포먼스 문제 예상되어 GLES extension 사용 
			
			synchronized (canvas) {
				GLES11.glTexImage2D(GLES11.GL_TEXTURE_2D, 0, GLES11Ext.GL_BGRA, mPreviewBufferWidth, mPreviewBufferHeight, 0, GLES11Ext.GL_BGRA, GLES11.GL_UNSIGNED_BYTE, canvas.buffer);
				mBufferPool.unlockReadBuffer();
				GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mVertexBuffer.capacity() / 2);
				if (mBufferPool.isReadBufferReady()) {
					mRenderingHandler.sendEmptyMessage(0);
				}
			}
		}


		private void prepareVertexBuffer(int width, int height) {
			float vertices[] = {
					 -1.0f, -1.0f,
					 1.0f, -1.0f, 
					 -1.0f, 1.0f, 
					 1.0f, 1.0f 
			};

			ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
			byteBuffer.order(ByteOrder.nativeOrder());
			mVertexBuffer = byteBuffer.asFloatBuffer();
			mVertexBuffer.put(vertices);
			mVertexBuffer.position(0);
		}

		private void prepareTextureCoordinateBuffer() {

			//반전 영상 원복 
			float textureCoordinates[] = {
					0.0f, 1.0f, 
					1.0f, 1.0f,
					0.0f, 0.0f, 
					1.0f, 0.0f
			};

			ByteBuffer byteBuffer = ByteBuffer.allocateDirect(textureCoordinates.length * 4);
			byteBuffer.order(ByteOrder.nativeOrder());
			mTextureCoordinateBuffer = byteBuffer.asFloatBuffer();
			mTextureCoordinateBuffer.put(textureCoordinates);
			mTextureCoordinateBuffer.position(0);
		}
	};

	private final Handler mRenderingHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (mBufferPool.isReadBufferReady()) {
				PreviewSurfaceView.this.requestRender();
			}
		}
	};
}