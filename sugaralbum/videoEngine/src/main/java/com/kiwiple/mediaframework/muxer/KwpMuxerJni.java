package com.kiwiple.mediaframework.muxer;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;

import com.kiwiple.mediaframework.CFFmpegCodecID;
import com.kiwiple.mediaframework.data.MediaFormatJNI;
import com.kiwiple.debug.L;

/**
 * java에서 ffmpeg를 jni를 통하여 Muxer 기능을 담당하는 class
 */
public class KwpMuxerJni {
	
	/** 
	 * Jni에서 해당 Muxer를 사용하기 위해서 필요한 Key값
	 * Jni에서 sp(smart point)를 사용하려 하였으나 적용이 잘 안되서 map으로 처리하면서 생긴 변수 
	 */
	private		long		mKey = -1;
	
	/**	
	 * Muxing 할 data 및 extra data에서 사용될 data copy
	 * bytebuffer를 jni에서 사용시 복잡도가 있어 byte[]로 사용하기로 결정하여 해당 변수가 생김. 
	 */
	private		byte[] 		mData;
	
	/**	Track 정보 관리 :  key: muxer number ,  value: codecId*/
	private     Map<Integer, Integer>   mTrackInfo;

	/**
	 * Construct 
	 * cpp로 작성된 KwpMuxer class를 생성함 
	 */
	public KwpMuxerJni() {
		L.d("Struct Function");
		mKey = NativeCreateMuxer();
		L.d("Struct key:" + mKey);
		mTrackInfo = new HashMap<Integer, Integer>();
	}
	
	/**
	 * Destruct 	
	 * cpp로 작성된 KwpMuxer를 Destory함
	 */
	public void destoryMuxer()
	{
		mTrackInfo.clear();
		NativeDestoryMuxer(mKey);
	}
	
	/**
	 * Muxer Init
	 * @param path		생성될 file path
	 */
	public boolean init(String path)
	{
		L.d("init key:" + mKey);
		mTrackInfo.clear();
		return NativeMuxerInit(mKey, path);
	}
	
	/**
	 * Muxing할 Track 추가 
	 * @param f		추가될 Track Format 정보 
	 * @return	Muxer에서 사용되는 Track number
	 */
	public int addTrack(MediaFormat f)
	{
		CFFmpegCodecID CodecId = CFFmpegCodecID.getMimeToCodecID(
				f.getString(MediaFormat.KEY_MIME));

		int id = CodecId.getId();
		int type = CodecId.getType();
		MediaFormatJNI	formatjni = new MediaFormatJNI(f, type, id);
		
		int size = 0;
		
		if(f.containsKey("extradatasize") && f.containsKey("extradata"))
		{
			size = f.getInteger("extradatasize");
			if(size > 0)
			{
				mData = new byte[size];
				ByteBuffer extradata = f.getByteBuffer("extradata");
				
				extradata.position(0);
				extradata.get(mData);
				
				StringBuffer str = new StringBuffer();
				for (int i = 0; i < mData.length; i++) {
					str.append( Integer.toHexString(0xFF&(mData[i])));
					str.append(" ");
				}
				L.d("Extra data:" + str);
			}
		}
		
		int	trackNum = -1;
		if(id > -1)
		{
			StringBuffer 	strb = new StringBuffer();
			for (int j = 0; j < mData.length; j++) {
                strb.append( Integer.toHexString(0xFF&(mData[j])));
                strb.append(" ");
			}

			L.w("Old ExtraData :" + strb.toString());

			trackNum = NativeAddTrack(mKey, formatjni, mData, size);
			mTrackInfo.put(trackNum, id);
		}
		
		return trackNum;
	}
	
	/**
	 * Muxing 준비
	 */
	public void Start()
	{
		NativeMuxerStart(mKey);
	}

	/**
	 * Muxing이 완료되면 호출하여 File에 마무리 작성하도록하는 기능 
	 */
	public void End()
	{
		NativeMuxerEnd(mKey);
	}
	

	private final static int KEY_FRAME_FLAG =  1;
	private final static int NON_KEY_FRAME_FLAG= 0;

	/**
	 * 실제로 Muxing하는 작업을 하는 함수 
	 * @param trackNum		Muxing할 Track number
	 * @param data			Muxing할 data
	 * @param info			Muxing할 data 정보 
	 */	
	public void Muxing(int trackNum, ByteBuffer data, BufferInfo info)
	{
		mData = new byte[info.size];
		
		data.get(mData);
		int id = mTrackInfo.get(trackNum);
		//H264일 경우에만 처리 
		if(id == 1){
			int datasize = info.size -4;
			mData[0] = (byte)((datasize&0xFF000000)>>24);   
			mData[1] = (byte)((datasize&0xFF0000)>>16);   
			mData[2] = (byte)((datasize&0xFF00)>>8);   
			mData[3] = (byte)(datasize&0xFF);   
			if( (info.flags & MediaCodec.BUFFER_FLAG_SYNC_FRAME) != 0){   // KEY_Frame
				NativeMuxing(mKey, trackNum, mData, info, KEY_FRAME_FLAG);
			}else{ //NON_KEY
				NativeMuxing(mKey, trackNum, mData, info, NON_KEY_FRAME_FLAG);
			}
		}else{   //audio
			NativeMuxing(mKey, trackNum, mData, info, NON_KEY_FRAME_FLAG);
		}
	}
	
	/**
	 * Muxer가 여러개 생성되는지 확인 하기 위한 Test 함수
	 * 결과 파일의 path가 log에 출력됨 
	 */
	public void DisplayPath()
	{
		NativeDisplayPath(mKey);
	}
	
	private static native long NativeCreateMuxer();
	private static native void NativeDestoryMuxer(long key);
	private static native boolean NativeMuxerInit(long key, String path);
	private static native int NativeAddTrack(long key, MediaFormatJNI formatjni, byte[] data, int size);
	private static native void NativeMuxing(long key, int tracnum, byte[] data, BufferInfo info, int flag);
	private static native void NativeMuxerStart(long key);
	private static native void NativeMuxerEnd(long key);
	private static native void NativeDisplayPath(long key);
	
	static {
    	System.loadLibrary("KwpFFmpegMuxer");
    }
}
