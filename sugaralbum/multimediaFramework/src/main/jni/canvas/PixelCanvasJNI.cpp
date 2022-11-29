#include <jni.h>

#include "PixelCanvas.h"
#include "CanvasScope.h"
#include "LogNDK.h"

class PixelCanvasJNI final {

public:
	inline static PixelCanvas * getCanvas(jlong id) {
		return reinterpret_cast<PixelCanvas *>(id);
	}

	static jlong initialize(JNIEnv *env, jobject canvas, jintArray jArray) {
		return reinterpret_cast<jlong>(new PixelCanvas(env, jArray));
	}

	static void finalize(JNIEnv *env, jobject canvas, jlong id) {
		getCanvas(id)->finalize(env);
		delete getCanvas(id);
	}

	static void setImageSize(JNIEnv *env, jobject canvas, jlong id, jint width, jint height) {
		getCanvas(id)->setImageSize(width, height);
	}

	static int getImageWidth(JNIEnv *env, jobject canvas, jlong id) {
		return getCanvas(id)->getWidth();
	}

	static int getImageHeight(JNIEnv *env, jobject canvas, jlong id) {
		return getCanvas(id)->getHeight();
	}

	static void setOffset(JNIEnv *env, jobject canvas, jlong id, jint offset) {
		getCanvas(id)->setOffset(offset);
	}

	static int getOffset(JNIEnv *env, jobject canvas, jlong id) {
		return getCanvas(id)->getOffset();
	}

	static void drawPoint(JNIEnv *env, jobject canvas, jlong id, jint color, jint x, jint y) {
		getCanvas(id)->drawPoint(color, x, y);
	}

	static void drawLine(JNIEnv *env, jobject canvas, jlong id, jint color, jint startX, jint startY, jint endX, jint endY, jfloat thickness) {
		getCanvas(id)->drawLine(color, startX, startY, endX, endY, thickness);
	}

	static void drawRect(JNIEnv *env, jobject canvas, jlong id, jint color, jint startX, jint startY, jint endX, jint endY, jfloat thickness) {
		getCanvas(id)->drawRect(color, startX, startY, endX, endY, thickness);
	}

	static void drawOval(JNIEnv *env, jobject canvas, jlong id, jint color, jint x, jint y, jint xRadius, jint yRadius, jint thickness) {
		getCanvas(id)->drawOval(color, x, y, xRadius, yRadius, thickness);
	}

	static void fillOval(JNIEnv *env, jobject canvas, jlong id, jint color, jint x, jint y, jint xRadius, jint yRadius) {
		getCanvas(id)->fillOval(color, x, y, xRadius, yRadius);
	}

	static void rotate(JNIEnv *env, jobject canvas, jlong id, jint degree) {
		getCanvas(id)->rotate(degree);
	}

	static void clear(JNIEnv *env, jobject canvas, jlong id, jint color) {
		getCanvas(id)->clear(color);
	}

	static void clear(JNIEnv *env, jobject canvas, jlong id, jint color, jint x, jint y, jint width, jint height) {
		CanvasScope scope(x, y, width, height, true);
		getCanvas(id)->trimScope(scope);
		getCanvas(id)->clear(color, scope);
	}

	static void tint(JNIEnv *env, jobject canvas, jlong id, jint color) {
		getCanvas(id)->tint(color);
	}

	static void tint(JNIEnv *env, jobject canvas, jlong id, jint color, jint x, jint y, jint width, jint height) {
		CanvasScope scope(x, y, width, height, true);
		getCanvas(id)->tint(color, scope);
	}

	static void blend(JNIEnv *env, jobject canvas, jlong srcId, jlong dstId, jfloat multiplier) {
		getCanvas(srcId)->blend(getCanvas(dstId), multiplier);
	}

	static void blend(JNIEnv *env, jobject canvas, jlong srcId, jlong dstId, jint dstX, jint dstY, jfloat multiplier) {
		getCanvas(srcId)->blend(getCanvas(dstId), dstX, dstY, multiplier);
	}

	static void blend(JNIEnv *env, jobject canvas, jlong srcId, jlong dstId, jint srcX, jint srcY, jint dstX, jint dstY, jint width, jint height, jfloat multiplier) {
		CanvasScope srcScope(srcX, srcY, width, height, true);
		CanvasScope dstScope(dstX, dstY, width, height, true);
		getCanvas(srcId)->blend(getCanvas(dstId), srcScope, dstScope, multiplier);
	}

	static void blendWithMask(JNIEnv *env, jobject canvas, jlong srcId, jlong dstId, jlong maskId) {
		getCanvas(srcId)->blendWithMask(getCanvas(dstId), getCanvas(maskId));
	}

	static void blendWithMask(JNIEnv *env, jobject canvas, jlong srcId, jlong dstId, jlong maskId, jint x, jint y) {
		getCanvas(srcId)->blendWithMask(getCanvas(dstId), getCanvas(maskId), x, y);
	}

	static void copy(JNIEnv *env, jobject canvas, jlong srcId, jlong dstId) {
		getCanvas(srcId)->copy(getCanvas(dstId));
	}

	static void copy(JNIEnv *env, jobject canvas, jlong srcId, jlong dstId, jint srcX, jint srcY, jint dstX, jint dstY, jint width, jint height) {
		getCanvas(srcId)->copy(getCanvas(dstId), srcX, srcY, dstX, dstY, width, height);
	}

	static void copyWithScale(JNIEnv *env, jobject canvas, jlong srcId, jlong dstId, jfloat dstX, jfloat dstY, jfloat scale) {
		getCanvas(srcId)->copyWithScale(getCanvas(dstId), dstX, dstY, scale);
	}

	static const JNINativeMethod methods[];
};

const JNINativeMethod PixelCanvasJNI::methods[] = {
	 {"nativeInitialize","([I)J", (void *) initialize},
	 {"nativeFinalize", "(J)V", (void *) finalize},
	 {"nativeSetImageSize", "(JII)V", (void *) setImageSize},
	 {"nativeGetImageWidth", "(J)I", (void *) getImageWidth},
	 {"nativeGetImageHeight", "(J)I", (void *) getImageHeight},
	 {"nativeSetOffset", "(JI)V", (void *) setOffset},
	 {"nativeGetOffset", "(J)I", (void *) getOffset},
	 {"nativeDrawPoint", "(JIII)V", (void *) drawPoint},
	 {"nativeDrawLine", "(JIIIIIF)V", (void *) drawLine},
	 {"nativeDrawRect", "(JIIIIIF)V", (void *) drawRect},
	 {"nativeDrawOval", "(JIIIIII)V", (void *) drawOval},
	 {"nativeFillOval", "(JIIIII)V", (void *) fillOval},
	 {"nativeRotate", "(JI)V", (void *) rotate},
	 {"nativeTint", "(JI)V", (void *) (void (*)(JNIEnv *, jobject, jlong, jint)) tint},
	 {"nativeTint", "(JIIIII)V", (void *) (void (*)(JNIEnv *, jobject, jlong, jint, jint, jint, jint, jint)) tint},
	 {"nativeBlend", "(JJF)V", (void *) (void (*)(JNIEnv *, jobject, jlong, jlong, jfloat)) blend},
	 {"nativeBlend", "(JJIIF)V", (void *) (void (*)(JNIEnv *, jobject, jlong, jlong, jint, jint, jfloat)) blend},
	 {"nativeBlend", "(JJIIIIIIF)V", (void *) (void (*)(JNIEnv *, jobject, jlong, jlong, jint, jint, jint, jint, jint, jint, jfloat)) blend},
	 {"nativeBlendWithMask", "(JJJ)V", (void *) (void (*)(JNIEnv *, jobject, jlong, jlong, jlong)) blendWithMask},
	 {"nativeBlendWithMask", "(JJJII)V", (void *) (void (*)(JNIEnv *, jobject, jlong, jlong, jlong, jint, jint)) blendWithMask},
	 {"nativeClear", "(JI)V", (void *) (void (*)(JNIEnv *, jobject, jlong, jint)) clear},
	 {"nativeClear", "(JIIIII)V", (void *) (void (*)(JNIEnv *, jobject, jlong, jint, jint, jint, jint, jint)) clear},
	 {"nativeCopy", "(JJ)V", (void *) (void (*)(JNIEnv *, jobject, jlong, jlong)) copy},
	 {"nativeCopy", "(JJIIIIII)V", (void *) (void (*)(JNIEnv *, jobject, jlong, jlong, jint, jint, jint, jint, jint, jint)) copy},
	 {"nativeCopyWithScale", "(JJFFF)V", (void *) copyWithScale}
};

extern "C" JNIEXPORT void JNICALL Java_com_kiwiple_multimedia_canvas_PixelCanvas_nativeInitializeJNI(JNIEnv *env, jclass canvasClass) {
	env->RegisterNatives(canvasClass, PixelCanvasJNI::methods, sizeof(PixelCanvasJNI::methods) / sizeof(JNINativeMethod));
}
