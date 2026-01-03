#include <KwpMuxer.h>

#define TAG		"KwpMuxer_CPP"

#include <string.h>
#include <KwpLogs.h>
#include <KwpTranceTypeManager.h>
#include <MediaFormatJNI.h>

/** construct */
KwpMuxer::KwpMuxer()
{
	Logw("Call Constructur");
	mPath = NULL;
	mMuxerCtx = NULL;
	mMuxerFmt = NULL;
	mIsMuxing = FALSE;
}

/** destruct */
KwpMuxer::~KwpMuxer()
{
	Logw("Call Destructur");
	if(mPath != NULL){
		free(mPath);
		mPath = NULL;
	}

	for(int i = 0; i < mMuxerCtx->nb_streams; i++)
	{
		avcodec_close(mMuxerCtx->streams[i]->codec);
	}

	if(mMuxerCtx && !(mMuxerFmt->flags & AVFMT_NOFILE))
		avio_close(mMuxerCtx->pb);

	avformat_free_context(mMuxerCtx);
	mMuxerCtx = NULL;

	// invalidate addr
	//	av_free(mMuxerFmt);
	mMuxerFmt = NULL;
	mIsMuxing = FALSE;
}

/** Muxer init */
bool 	KwpMuxer::init(const char * path)
{
	Logw("Call init()");
	if(path == NULL)
	{
		Loge("path is null");
		return false;
	}
	if(strlen(path) < 1)
	{
		Loge("path is empty");
		return false;
	}

	/* Initialize libavcodec, and register all codecs and formats. */
	av_register_all();

	if(mPath != NULL){
		free(mPath);
		mPath = NULL;
	}

	Logw("mPaht malloc size:%d  str:%s" , strlen(path) +1, path);
	mPath = (char*)malloc(strlen(path) +1);
	strcpy(mPath, path);

	AVOutputFormat * outFmt = av_guess_format("mp4", NULL, NULL);
	avformat_alloc_output_context2(&mMuxerCtx, outFmt, "tmp", path);
	if(!mMuxerCtx)
	{
		Loge("Could not create FFmpeg Muxer");
		return false;
	}

	mMuxerFmt = mMuxerCtx->oformat;
	mTrackNumber = 0;
	return true;
}

/**
* @param	*data		extra data
* @param	datasize	extra data size
* @reutrn  TrackNumber
*/
int	KwpMuxer::addTrack( MediaFormatJNI &format, uint8_t *data, int datasize)
{
	Loge("addTrack ....");
	// outTrac malloc
	AVStream		*avstream;
	AVCodec *codec = avcodec_find_encoder(format.mCodecId);
	int bitrate = 0;
	int	framesize = 0;

	if(codec == NULL)
	{
		codec = avcodec_find_decoder(format.mCodecId);
		if(codec == NULL)
		{
			Loge("Could not find encoder for '%s'\n", avcodec_get_name(format.mCodecId));
			return -1;
		}
	}

	avstream = avformat_new_stream(mMuxerCtx, codec);

	switch(format.mMediaType)
	{
		case	AVMEDIA_TYPE_AUDIO:
			Loge("channel : %d", format.mChannel);
			Loge("sample rate : %d", format.mSampleRate);
			Loge("bit rate : %d", format.mBitRate);

			avstream->codec->channel_layout = AV_CH_LAYOUT_STEREO;
		    avstream->codec->channels = format.mChannel;
		    avstream->codec->sample_rate = format.mSampleRate;
		    avstream->codec->bit_rate = 0;

		    // avstream->codec->block_align	=	format.mChannel * 2; // bitfersample size 2byte로 fix
		    // avstream->codec->bits_per_coded_sample		// Caps : depth  h264 aac mp3에서는 사용안하는듯..

		    avstream->codec->time_base.den = format.mSampleRate;
		    avstream->codec->time_base.num	= 1;
		    avstream->codec->sample_fmt = AV_SAMPLE_FMT_S16;		// 사용 안할듯 일단 임시 작성

		    avstream->id = mTrackNumber;
		    avstream->codec->codec_type = format.mMediaType;
		    avstream->codec->codec_id = format.mCodecId;  /* this is a check afterwards */

			break;
        case	AVMEDIA_TYPE_VIDEO:
		    framesize = 1152;
		    	avstream->codec->width = format.mWidth;
		    	avstream->codec->height = format.mHeight;
//		    	avstream->codec->bits_per_coded_sample  		// Caps : depth  h264 aac mp3에서는 사용안하는듯..

		    // Frame Rate		30/1 = 30 fps
//		    	avstream->codec->time_base.den = 32;
//		    	avstream->codec->time_base.num	= 1;
		    	avstream->codec->ticks_per_frame = 1;
//		    	avstream->codec->time_base.den = format.mFrameRate;
		    	avstream->codec->time_base.den = 90000;
		    	avstream->codec->time_base.num	= 1;

		    	avstream->id = mTrackNumber;
		    	avstream->codec->codec_type = format.mMediaType;
		    	avstream->codec->codec_id = format.mCodecId;  /* this is a check afterwards */
		    	avstream->codec->bit_rate = format.mBitRate;

		    //	avstream->codec->pix_fmt = PIX_FMT_YUV420P;   // 사용 안할듯 일단 임시 작성

			break;
        default:
        	Loge("Could not find TrackType \n");
        	return -1;
	}


//	avstream = av_new_stream(mMuxerCtx, mTrackNumber);
//	avstream->id = mTrackNumber;
//	avstream->codec->codec_type = format.mMediaType;
//	avstream->codec->codec_id = format.mCodecId;  /* this is a check afterwards */
//	avstream->codec->bit_rate = format.mBitRate;
//	avstream->codec->frame_size = framesize;

	// extra data setting
    Loge("set ExtraData ..  size: %d ", datasize);
	if(datasize > 0)
	{
		avstream->codec->extradata = data;
		avstream->codec->extradata_size = datasize;
	}


	mTrackNumber ++;
	return mTrackNumber-1;
}

/**
 * 각 Track의 base titme을 가져옴
 * (이함수를 이용하여 android에서 사용 되는 기준 시간과 ffmpeg에서 사용되는 기준 시간은 맞춤)
 * @param		tracknum		track number
 */
AVRational 	KwpMuxer::getBaseTime(int tracknum)
{
	return mMuxerCtx->streams[tracknum]->codec->time_base;
}

/**
 * Muxing 하기전에 호출 format의 header 부분등을 작성해줌
 */
bool	KwpMuxer::start()
{
	mIsMuxing = TRUE;
	av_dump_format(mMuxerCtx, 0, mPath, 1);
	if(!(mMuxerFmt->flags & AVFMT_NOFILE))
	{
		int ret = avio_open(&mMuxerCtx->pb, mPath, AVIO_FLAG_WRITE);
		if( ret < 0)
		{
			Loge("occurred when opening output file");
			return false;
		}
		ret = avformat_write_header(mMuxerCtx, NULL);
	}
	else
		Loge("Error MuxerFmt flags is AVFMT_NOFILE");

	return true;
}

/**
 * 실제 Track들의 data를 Muxing하는 함수
 * @param	pkt		muxing할 AVPacket
 */
void 	KwpMuxer::Muxing(AVPacket	*pkt)
{
//	Logw("KwpMuxer::Muxing start ...");
	int ret = av_write_frame(mMuxerCtx, pkt);
//	int ret = av_interleaved_write_frame(mMuxerCtx, pkt);
	if(ret < 0)
		Loge("Error Muxing packet ...");

//	Logw("KwpMuxer::Muxing end ...");
}

/**
 * Muxing이 모두 완료되면 호출하여 마무리 작업을 함
 */
void	KwpMuxer::end()
{
	if(mMuxerCtx != NULL && mIsMuxing){
		Loge("Muxing end().");
		av_write_trailer(mMuxerCtx);
	}
}

/** Muxer가 여러개 호출 되는게 가능한지 보기위하여 Test한 함수  */
void 	KwpMuxer::displayPaht()
{
	Logw("path: %s", mPath);
}
