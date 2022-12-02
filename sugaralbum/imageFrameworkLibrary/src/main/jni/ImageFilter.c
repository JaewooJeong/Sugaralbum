
#include <stdlib.h>
#include <math.h>
#include "com_kiwiple_imageframework_filter_NativeImageFilter.h"
#include "Spline.h"
#include <android/log.h>


void makeCurveImage(unsigned char *ori, unsigned char **curvesLookUp, int width, int height) {
	unsigned char *input = ori;
	
	unsigned char **lookUpTable = curvesLookUp;
	
	register int value;
	
	// index
	int x;
	
	x = width * height * 4 - 4;
	
	do
	{
		value = (int)input[x + 0];
		input[x + 0] = lookUpTable[0][value];
		// lookuptable
		value = (int)input[x + 0];
		input[x + 0] = lookUpTable[1][value];
		
		value = (int)input[x + 1];
		input[x + 1] = lookUpTable[0][value];
		// lookuptable
		value = (int)input[x + 1];
		input[x + 1] = lookUpTable[2][value];
		
		value = (int)input[x + 2];
		input[x + 2] = lookUpTable[0][value];
		// lookuptable
		value = (int)input[x + 2];
		input[x + 2] = lookUpTable[3][value];
		
		input[x + 3] = (unsigned char)0xff;
		x-=4;
		
	} while (x >= 0);
}



void makeLookupTableFromCurvePoints(short *points, int len, unsigned char *outputTable) {
	CurvePoint *curves = (CurvePoint *)malloc(sizeof(CurvePoint) * len / 2);
	
	//__android_log_write(ANDROID_LOG_INFO,"point length : ", buf);

	char buf[50];
	int i;
	for(i = 0; i < len; i+=2) {
		curves[i / 2].input = (float)(points[i]);
		curves[i / 2].output = (float)(points[i + 1]);
	}
	
	makeLookupTable(curves, len / 2, outputTable);
	
	free(curves);
}


// Curve
JNIEXPORT void JNICALL Java_com_kiwiple_imageframework_filter_NativeImageFilter_curveProcessing
(JNIEnv *env, jclass jobj, jobject ori, jint width, jint height, jshortArray all, jshortArray r, jshortArray g, jshortArray b) {
	
	unsigned char *input = (unsigned char *)(*env)->GetDirectBufferAddress(env, ori);
	
	int allLen = (*env)->GetArrayLength(env, all);
	int rLen = (*env)->GetArrayLength(env, r);
	int gLen = (*env)->GetArrayLength(env, g);
	int bLen = (*env)->GetArrayLength(env, b);
	
	unsigned char **lookupTable = (unsigned char **)malloc(sizeof(unsigned char *) * 4);
	
	int i;
	
	for(i = 0; i < 4; i++) {
		lookupTable[i] = (unsigned char *)malloc(sizeof(unsigned char) * 256);
	}
	
	short *d = (short *)(*env)->GetShortArrayElements(env, all, 0);
	makeLookupTableFromCurvePoints(d, allLen, lookupTable[0]);
	(*env)->ReleaseShortArrayElements(env, all, d, JNI_ABORT);
	d = (short *)(*env)->GetShortArrayElements(env, r, 0);
	makeLookupTableFromCurvePoints(d, rLen, lookupTable[1]);
	(*env)->ReleaseShortArrayElements(env, r, d, JNI_ABORT);
	d = (short *)(*env)->GetShortArrayElements(env, g, 0);
	makeLookupTableFromCurvePoints(d, gLen, lookupTable[2]);
	(*env)->ReleaseShortArrayElements(env, g, d, JNI_ABORT);
	d = (short *)(*env)->GetShortArrayElements(env, b, 0);
	makeLookupTableFromCurvePoints(d, bLen, lookupTable[3]);
	(*env)->ReleaseShortArrayElements(env, b, d, JNI_ABORT);

	makeCurveImage(input, lookupTable, width, height);
	
	for(i = 0; i < 4; i++) {
		free(lookupTable[i]);
	}
	
	free(lookupTable);
}


// Contrast
JNIEXPORT void JNICALL Java_com_kiwiple_imageframework_filter_NativeImageFilter_contrastProcessing
(JNIEnv *env, jclass jobj, jobject ori, jint width, jint height, jfloat contrast) {
	
	//float temp;
	float _contrast = contrast;
	
	// index
	register int x;
	
	_contrast *= _contrast;
	
	static float constant = 127.5;
	
	unsigned char *input = (unsigned char *)(*env)->GetDirectBufferAddress(env, ori);
	
	x = 255;
	
	
	char buf[50];
	
	unsigned char contrastLookUpTable[256];
	do
	{
		register float value = x;
		
		value -= constant;
		value *= _contrast;
		value += constant;
				
		if(value < 0) {
			contrastLookUpTable[x] = 0;
		}
		else if(value > 255) {
			contrastLookUpTable[x] = 255;
		}
		else {
			contrastLookUpTable[x] = (unsigned char)value;
		}
		
		x--;
	} while (x >= 0);
	
	x = width * height * 4 - 4;
	do
	{
		input[x] = (unsigned char)contrastLookUpTable[input[x]];
		input[x + 1] = (unsigned char)contrastLookUpTable[input[x + 1]];
		input[x + 2] = (unsigned char)contrastLookUpTable[input[x + 2]];
		input[x + 3] = (unsigned char)0xff;

		x-=4;
	} while (x >= 0);
	
}


JNIEXPORT void JNICALL Java_com_kiwiple_imageframework_filter_NativeImageFilter_brightnessProcessing
(JNIEnv *env, jclass jobj, jobject ori, jint width, jint height, jint brightness) {
	
	register int temp;
	
	unsigned char *input = (unsigned char *)(*env)->GetDirectBufferAddress(env, ori);
	
	// index
	register int x;
	
	x = width * height * 4 - 4;
	
	do
	{
		// alpha
		input[x + 3] = (unsigned char)0xff;
		
		//r
		temp = (int)input[x] + brightness;
		if (temp > 255)
			input[x] = (unsigned char)255;
		else if (temp < 0)
			input[x] = (unsigned char)0;
		else
			input[x] = (unsigned char)temp;
		
		//g
		temp = (int)input[x + 1] + brightness;
		if (temp > 255)
			input[x + 1] = (unsigned char)255;
		else if (temp < 0)
			input[x + 1] = (unsigned char)0;
		else
			input[x + 1] = (unsigned char)temp;
		
		//b
		temp = (int)input[x + 2] + brightness;
		if (temp > 255)
			input[x + 2] = (unsigned char)255;
		else if (temp < 0)
			input[x + 2] = (unsigned char)0;
		else
			input[x + 2] = (unsigned char)temp;
		
		x -= 4;
	} while (x >= 0);	
}



JNIEXPORT void JNICALL Java_com_kiwiple_imageframework_filter_NativeImageFilter_textureProcessing
(JNIEnv *env, jclass jobj, jobject ori, jobject texture, jint width, jint height, jint alpha) {
	
	unsigned char *input = (unsigned char *)(*env)->GetDirectBufferAddress(env, ori);
	unsigned char *tex = (unsigned char *)(*env)->GetDirectBufferAddress(env, texture);
	
	// index
	register int x;
	int a = 0;
		
	x = width * height * 4 - 4;
	
	do
	{	
		a = alpha * tex[x + 3] >> 8;
		
		// alpha
		input[x + 3] = (unsigned char)0xff;
		
		//r
		//output[x] = (input[x] < 128) ? ((2 * tex[x] * alpha * input[x]) / 65025) :
		//(alpha - 2 * (255 - input[x]) * (255 - tex[x]) * alpha / 65025 + input[x] * (255 - alpha) / 255);
		
		//output[x] = (input[x] < 128) ? ((2 * tex[x] * a * input[x]) >> 16) :
		//(a - (2 * (255 - input[x]) * (255 - tex[x]) * a >> 16) + (input[x] * (255 - a) >> 8));
		
		/*
		if(input[x] < 128) {
			output[x] = ((2 * tex[x] * alpha * input[x]) / 65025) + input[x] * (255 - alpha) / 255;
		}
		else {
			output[x] = (alpha - 2 * (255 - input[x]) * (255 - tex[x]) * alpha / 65025 + input[x] * (255 - alpha) / 255);
		}
		 */
		
		if(input[x] < 128) {
			input[x] = ((2 * tex[x] * a * input[x]) >> 16) + (input[x] * (255 - a) >> 8);
		}
		else {
			input[x] = (a - (2 * (255 - input[x]) * (255 - tex[x]) * a >> 16) + (input[x] * (255 - a) >> 8));
		}
		
		
		//g
		/*
		if(input[x + 1] < 128) {
			output[x + 1] = ((2 * tex[x + 1] * alpha * input[x + 1]) / 65025) + input[x + 1] * (255 - alpha) / 255;
		}
		else {
			output[x + 1] = (alpha - 2 * (255 - input[x + 1]) * (255 - tex[x + 1]) * alpha / 65025 + input[x + 1] * (255 - alpha) / 255);
		}
		 */
		
		if(input[x + 1] < 128) {
			input[x + 1] = ((2 * tex[x + 1] * a * input[x + 1]) >> 16) + (input[x + 1] * (255 - a) >> 8);
		}
		else {
			input[x + 1] = (a - (2 * (255 - input[x + 1]) * (255 - tex[x + 1]) * a >> 16) + (input[x + 1] * (255 - a) >> 8));
		}
		
		
		//b
		/*
		if(input[x + 2] < 128) {
			output[x + 2] = ((2 * tex[x + 2] * alpha * input[x + 2]) / 65025) + input[x + 2] * (255 - alpha) / 255;
		}
		else {
			output[x + 2] = (alpha - 2 * (255 - input[x + 2]) * (255 - tex[x + 2]) * alpha / 65025 + input[x + 2] * (255 - alpha) / 255);
		}
		 */
		
		if(input[x + 2] < 128) {
			input[x + 2] = ((2 * tex[x + 2] * a * input[x + 2]) >> 16) + (input[x + 2] * (255 - a) >> 8);
		}
		else {
			input[x + 2] = (a - (2 * (255 - input[x + 2]) * (255 - tex[x + 2]) * a >> 16) + (input[x + 2] * (255 - a) >> 8));
		}
		
		x -= 4;
	} while (x >= 0);	
}



JNIEXPORT jshortArray JNICALL Java_com_kiwiple_imageframework_filter_NativeImageFilter_getSpline
(JNIEnv *env, jclass jobj, jshortArray points) {
	int len = (*env)->GetArrayLength(env, points);
	
	unsigned char *lookupTable = (unsigned char *)malloc(sizeof(unsigned char) * 256);
	
	short *d = (short *)(*env)->GetShortArrayElements(env, points, 0);
	makeLookupTableFromCurvePoints(d, len, lookupTable);
	(*env)->ReleaseShortArrayElements(env, points, d, JNI_ABORT);
	
	short *result = (short *)malloc(sizeof(short) * 256);
	
	register int i = 0;
	
	for(i = 0; i < 256; i++) {
		result[i] = lookupTable[i];
	}
	
	jshortArray resultArray = (*env)->NewShortArray(env, 256);
	(*env)->SetShortArrayRegion(env, resultArray, 0, 256, result);
	
	free(lookupTable);
	free(result);
	
	return resultArray;
}



JNIEXPORT void JNICALL Java_com_kiwiple_imageframework_filter_NativeImageFilter_grayProcessing
(JNIEnv *env, jclass jobj, jobject ori, jint width, jint height) {
	
	register int temp;
	
	unsigned char *input = (unsigned char *)(*env)->GetDirectBufferAddress(env, ori);
	
	// index
	register int x;
	
	x = width * height * 4 - 4;
	
	do
	{
		// alpha
		input[x + 3] = (unsigned char)0xff;
		
		temp = (input[x] + input[x + 1] + input[x + 2]) / 3;
		
		input[x] = temp;
		input[x + 1] = temp;
		input[x + 2] = temp;
		
		x -= 4;
	} while (x >= 0);
	
}


int absi(int input) {
	if(input < 0) {
		return -input;
	}
	
	return input;
}

unsigned int newt_sqrt(unsigned int input)
{
	int nv, v = input>>1, c = 0;
	if (!v)
		return input;
	do
	{
		nv = (v + input/v)>>1;
		if (absi(v - nv) <= 1) // I have an available fast absolute value in this forum. If you have it. use the next one.
			//if (absi(v - nv) <= 1)
			return nv;
		v = nv;
	}
	while (c++ < 25);
	return nv;
}


unsigned int mborg_isqrt4(unsigned long val)
{
	unsigned int temp, g = 0;

	if (val >= 0x40000000)
	{
		g = 0x8000;
		val -= 0x40000000;
	}

#define INNER_MBGSQRT(s)	\
	temp = (g << (s)) + (1 << (((s) << 1) - 2));	\
	if (temp <= val)	\
	{	\
		g += 1 << ((s) - 1);	\
		val -= temp;	\
	}

	INNER_MBGSQRT (15)
	INNER_MBGSQRT (14)
	INNER_MBGSQRT (13)
	INNER_MBGSQRT (12)
	INNER_MBGSQRT (11)
	INNER_MBGSQRT (10)
	INNER_MBGSQRT ( 9)
	INNER_MBGSQRT ( 8)
	INNER_MBGSQRT ( 7)
	INNER_MBGSQRT ( 6)
	INNER_MBGSQRT ( 5)
	INNER_MBGSQRT ( 4)
	INNER_MBGSQRT ( 3)
	INNER_MBGSQRT ( 2)

#undef INNER_MBGSQRT

	temp = g + g + 1;
	if (temp <= val) g++;
	
	return g;
}

// Saturation
JNIEXPORT void JNICALL Java_com_kiwiple_imageframework_filter_NativeImageFilter_saturationProcessing
(JNIEnv *env, jclass jobj, jobject ori, jint width, jint height, jfloat saturation) {

	unsigned char *input = (unsigned char *)(*env)->GetDirectBufferAddress(env, ori);

	// index
	register int x;

	int redLookUpTable[256];
	int greenLookUpTable[256];
	int blueLookUpTable[256];

	int mSaturationLookUpTable[256];
	//float inverseSaturation = 1.f - saturation;
	int inverseSaturation = 255 - (int)(255 * saturation);
	int s = (int)(255 * saturation);
	int x2;

	x = 255;

	do
	{
		x2 = x * x;

		#define Pr	.241f
		#define Pg	.691f
		#define Pb	.068f

		redLookUpTable[x] = x2 * 241 / 1000;
		greenLookUpTable[x] = x2 * 691 / 1000;
		blueLookUpTable[x] = x2 * 68 / 1000;

		mSaturationLookUpTable[x] = s * x >> 8;

		x -= 1;
	} while (x >= 0);

	int p;
	register int tempColor;
	int isp;

	x = width * height * 4 - 4;

	do
	{
		// alpha
		input[x + 3] = (unsigned char) 0xff;

		p = mborg_isqrt4((unsigned long)redLookUpTable[input[x + 0]] + (unsigned long)greenLookUpTable[input[x + 1]] + (unsigned long)blueLookUpTable[input[x + 2]]);

		isp = inverseSaturation * p >> 8;

		// r
		tempColor = isp + mSaturationLookUpTable[input[x + 0]];
		if (tempColor > 255)
			input[x + 0] = (unsigned char) 255;
		else if (tempColor < 0)
			input[x + 0] = (unsigned char) 0;
		else
			input[x + 0] = (unsigned char) tempColor;

		// g
		tempColor = isp + mSaturationLookUpTable[input[x + 1]];
		if (tempColor > 255)
			input[x + 1] = (unsigned char) 255;
		else if (tempColor < 0)
			input[x + 1] = (unsigned char) 0;
		else
			input[x + 1] = (unsigned char) tempColor;

		// b
		tempColor = isp + mSaturationLookUpTable[input[x + 2]];
		if (tempColor > 255)
			input[x + 2] = (unsigned char) 255;
		else if (tempColor < 0)
			input[x + 2] = (unsigned char) 0;
		else
			input[x + 2] = (unsigned char) tempColor;

		x -= 4;
	} while (x >= 0);
}



JNIEXPORT jobject JNICALL Java_com_kiwiple_imageframework_filter_NativeImageFilter_allocByteBuffer(JNIEnv *env, jclass jobj, jint size)
{
    void* buffer = malloc(size);
    jobject directBuffer = (*env)->NewDirectByteBuffer(env, buffer, size);
    //jobject globalRef = (*env)->NewGlobalRef(env, directBuffer);
	
    //return globalRef;
	
	return directBuffer;
}

JNIEXPORT void JNICALL Java_com_kiwiple_imageframework_filter_NativeImageFilter_freeByteBuffer(JNIEnv *env, jclass jobj, jobject globalRef)
{
    void *buffer = (*env)->GetDirectBufferAddress(env, globalRef);
	
    //(*env)->DeleteGlobalRef(env, globalRef);
    free(buffer);
}

static int w, h, allLen;
static int frameSize;
static int uvp, u, v;
static int i, j, yp;
static int y1192, r, g, b;

JNIEXPORT void JNICALL Java_com_kiwiple_imageframework_filter_NativeImageFilter_YUVtoRBG(JNIEnv * env, jobject obj, jintArray rgb, jbyteArray yuv420sp, jint width, jint height)
{
	w = width;
	h = height;
	frameSize = width * height;

	jbyte *yuv = (*env)->GetByteArrayElements(env, yuv420sp, 0);
	jint *pixels = (*env)->GetIntArrayElements(env, rgb, 0);
	allLen = (*env)->GetArrayLength(env, rgb);

	for (j = 0, yp = 0; j < height; j++) {
		uvp = frameSize + (j >> 1) * width;
		u = 0;
		v = 0;
		for (i = 0; i < width; i++, yp++) {
			int y = (0xff & yuv[yp]) - 16;
			if (y < 0) y = 0;
			if ((i & 1) == 0) {
				v = (0xff & yuv[uvp++]) - 128;
				u = (0xff & yuv[uvp++]) - 128;
			}

			y1192 = 1192 * y;
			r = (y1192 + 1634 * v);
			g = (y1192 - 833 * v - 400 * u);
			b = (y1192 + 2066 * u);

			if (r < 0) r = 0; else if (r > 262143) r = 262143;
			if (g < 0) g = 0; else if (g > 262143) g = 262143;
			if (b < 0) b = 0; else if (b > 262143) b = 262143;

			if(yp < allLen) {
				pixels[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
			}
		}
	}
}

int R, G, B;
int Y, U, V;
int idx, yIndex, uIndex, vIndex;
int chromasize;

//http://embedav.blogspot.kr/2013/06/convert-rgb-to-yuv420-planar-format-in.html
JNIEXPORT void JNICALL Java_com_kiwiple_imageframework_filter_NativeImageFilter_RGBtoYUV(JNIEnv * env, jobject obj, jintArray rgb, jbyteArray yuv420sp, jint width, jint height)
{
        jbyte *yuv = (*env)->GetByteArrayElements(env, yuv420sp, 0);
        jint *pixels = (*env)->GetIntArrayElements(env, rgb, 0);

        frameSize = width * height;
        chromasize = frameSize / 4;

        yIndex = 0;
        uIndex = frameSize;
        vIndex = frameSize + chromasize;

        idx = 0;
        for (j = 0; j < height; j++) {
            for (i = 0; i < width; i++) {
                //a = (aRGB[idx] & 0xff000000) >> 24; //not using it right now
                R = (pixels[idx] & 0xff0000) >> 16;
                G = (pixels[idx] & 0xff00) >> 8;
                B = (pixels[idx] & 0xff) >> 0;

                Y = ((66 * R + 129 * G +  25 * B + 128) >> 8) +  16;
                U = (( -38 * R -  74 * G + 112 * B + 128) >> 8) + 128;
                V = (( 112 * R -  94 * G -  18 * B + 128) >> 8) + 128;

                yuv[yIndex++] = (jbyte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));

                if (j % 2 == 0 && idx % 2 == 0)
                {
                    yuv[uIndex++] = (jbyte)((V < 0) ? 0 : ((V > 255) ? 255 : V));
                    yuv[uIndex++] = (jbyte)((U < 0) ? 0 : ((U > 255) ? 255 : U));
                }
                idx++;
            }
        }
        (*env)->ReleaseIntArrayElements(env, rgb, pixels, 0);
        (*env)->ReleaseByteArrayElements(env, yuv420sp, yuv, 0);
}


