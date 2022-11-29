
#ifndef SPLINE_H

#define SPLINE_H



typedef struct _CurvePoint {
	float input;
	float output;
} CurvePoint;



void makeLookupTable(CurvePoint *points, int len, unsigned char *outputTable);


#endif