package com.kiwiple.mediaframework.demuxer;

import android.content.res.AssetFileDescriptor;

public class DemuxerManager {

	/**
	 * Demuxer 생성 
	 * @param path 입력 컨텐츠의 경로
	 * @param starttime 컨텐츠에서 Demuxer가 분석할 부분의 시작 시간 
	 * @param endtime   컨텐츠에서 Demuxer가 분석할 부분의 마지막 시간
	 * @return	DemuxerApi 
	 */
	static public DemuxerApi	CreateDemuxer(String path, long starttime, long endtitme)
	{
		DemuxerApi	result = null;
		result = new HWDemuxer();
		if(result.init(path, starttime, endtitme))
		{
			return result;
		}	
		else
		{
			// if(ffmpeg demuxer check)
			result.clear();
			return null;
		}
	}
	
	/**
	 * Demuxer 생성 
	 * @param afd asset에 입력 컨텐츠를 받기 위한 파라미터  
	 * @param starttime 컨텐츠에서 Demuxer가 분석할 부분의 시작 시간 
	 * @param endtime   컨텐츠에서 Demuxer가 분석할 부분의 마지막 시간
	 * @return	DemuxerApi 
	 */
	static public DemuxerApi CreateDemuxer(AssetFileDescriptor afd, long starttime, long endtime){
		DemuxerApi	result = null;
		result = new HWDemuxer();
		if(result.init(afd, starttime, endtime))
		{
			return result;
		}	
		else
		{
			// if(ffmpeg demuxer check)
			result.clear();
			return null;
		}
	}
}
