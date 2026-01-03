#ifndef _CLASS_KWP_MUXER_h
#define _CLASS_KWP_MUXER_h

extern "C"
{
#include <libavutil/avutil.h>
#include <libavformat/avformat.h>
}

#include <MediaFormatJNI.h>

#define FALSE 0
#define TRUE !FALSE

/**
 * ffmpeg Muxer를 custom한 class
 */
class KwpMuxer
{
private:
	/** Muxing한 결과 file path */
	char* mPath;

	/** ffmpeg Muxer */
	AVFormatContext 	*mMuxerCtx;

	/** ffmpeg Muxer */
	AVOutputFormat		*mMuxerFmt;

	/** Muxer 기준의 TrackIndex (Track이 추가되면 하나씩 늘어남) */
	int	mTrackNumber;

	/** 저장될 임시파일 생성 및 header writing 유무  */
	int 	mIsMuxing;



public:
	/** construct */
	KwpMuxer();
	/** destruct */
	~KwpMuxer();

	/** Muxer init */
	bool 	init(const char * path);

	/**
	 * Muxing 할 Track 추가
	 * @param	*data		extra data
	 * @param	datasize	extra data size
	 * @reutrn  TrackNumber
	 */
	int		addTrack(MediaFormatJNI &format, uint8_t *data, int datasize);

	/**
	 * Muxing 하기전에 호출 format의 header 부분등을 작성해줌
	 */
	bool	start();

	/**
	 * 실제 Track들의 data를 Muxing하는 함수
	 * @param	pkt		muxing할 AVPacket
	 */
	void 	Muxing(AVPacket *pkt);

	/**
	 * 각 Track의 base titme을 가져옴
	 * (이함수를 이용하여 android에서 사용 되는 기준 시간과 ffmpeg에서 사용되는 기준 시간은 맞춤)
	 * @param		tracknum		track number
	 */
	AVRational		getBaseTime(int tracknum);

	/**
	 * Muxing이 모두 완료되면 호출하여 마무리 작업을 함
	 */
	void	end();

	/** Muxer가 여러개 호출 되는게 가능한지 보기위하여 Test한 함수  */
	void 	displayPaht();
};


#endif
