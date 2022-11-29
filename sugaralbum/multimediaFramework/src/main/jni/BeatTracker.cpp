#include "BeatTracker.h"
#include "masp/include/ShotDetection.h"

JNIEXPORT jdoubleArray JNICALL Java_com_kiwiple_multimedia_audio_BeatTracker_nativeTrack
(JNIEnv *env, jclass jobj, jshortArray pcm, jint samplingRate)
{
	jshort *pcmPtr = env->GetShortArrayElements(pcm, 0);

	int pcmSize = env->GetArrayLength(pcm);
	std::vector<jdouble> beatVector = find_shot_change_loc(pcmPtr, pcmSize);

	int beatLength = beatVector.size();
	jdouble *beats = &beatVector[0];
	jdoubleArray result = env->NewDoubleArray(beatLength);

	env->SetDoubleArrayRegion(result, 0, beatLength, beats);
	env->ReleaseShortArrayElements(pcm, pcmPtr, 0);

	return result;
}
