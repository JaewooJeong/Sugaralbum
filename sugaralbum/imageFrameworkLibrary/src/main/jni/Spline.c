
#include <stdlib.h>
#include "Spline.h"
#include <android/log.h>

float *calculateSpline(CurvePoint *points, int length, float *resultBuf) {
	
	int n;
	n = length;
	
	float *u = NULL;
	
	// Initialise the spline with "natural" lower boundary condition.
	u = (float *)malloc(sizeof(float) * n);
	resultBuf[0] = u[0] = 0.0f;
	
	register int i;
	for (i = 0; i < n; i++) {
		resultBuf[i] = u[i] = 0.0f;
	}
	
	// This is the decomposition loop of the tridiagonal algorithm.
	for (i = 1; i < n - 1; ++i)
	{
		float sig = (points[i].input - points[i - 1].input) / (points[i + 1].input - points[i - 1].input);
		float p = sig * resultBuf[i - 1] + 2.0f;
		resultBuf[i] = (sig - 1.0f) / p;
		
		u[i] = (points[i + 1].output - points[i].output) / (points[i + 1].input - points[i].input) -
		(points[i].output - points[i - 1].output) / (points[i].input - points[i - 1].input);
		
		u[i] = (6.0f * u[i] / (points[i + 1].input - points[i - 1].input) - sig * u[i - 1]) / p;
	}
	
	resultBuf[n - 1] = 0.0f;
	
	int k;
	
	// This is the backsubstitution loop of the tridiagonal algorithm.
	for (k = n - 2; k >= 0; --k)
	{
		resultBuf[k] = resultBuf[k] * resultBuf[k + 1] + u[k];		
	}
	
	free(u);
}


float interpolateSpline(CurvePoint *points, int len, float *derivatives, float value) {
	
	int n;
	n = len;
	
	// We will find the right place in the table by means of bisection.
	int klo = 0;
	int khi = n - 1;
	while (khi - klo > 1)
	{
		int k = (khi + klo) >> 1;
		if (points[k].input > value)
			khi = k;
		else
			klo = k;
	}
	
	// klo and khi now bracket the input value of x.
	float h = points[khi].input - points[klo].input;
	
	if (h == 0.0f)
	{
		//printf("Bad x input. The x values must be distinct.");
		exit(0);
	}
	
	float a = (points[khi].input - value) / h;
	float b = (value - points[klo].input) / h;
	
	// Cubic spline polynomial is now evaluated.
	return a * points[klo].output + b * points[khi].output + 
	((a * a * a - a) * derivatives[klo] + (b * b * b - b) * derivatives[khi]) * (h * h) / 6.0f;
}


void makeLookupTable(CurvePoint *points, int len, unsigned char *outputTable) {
	int startPointX, endPointX;
	float value, *derivatives;
	
	startPointX = points[0].input;
	endPointX = points[len - 1].input;
		
	// Calcullate Spline
	derivatives = (float *)malloc(sizeof(float) * len);
	calculateSpline(points, len, derivatives);
	
	// Interpolate
	register int y;
	for (y = 0; y < 256; y++) 
	{
		value = interpolateSpline(points, len, derivatives, (float)y);
		if (value > 255.0)
			value = 255.0;
		else if (value < 0.0)
			value = 0.0;
		
		if (y <= startPointX)
			outputTable[y] = points[0].output;
		else if (y >= endPointX)
			outputTable[y] = points[len - 1].output;
		else
			outputTable[y] = value;
	}
	
	free(derivatives);
}