package com.kiwiple.mediaframework.muxer;

import com.kiwiple.debug.L;

public class MuxerManager {
	
	/**
	 * H/W S/W Create Muxer
	 * @param path		create file path
	 * @return		MuxerApi    fail is null
	 */
	static public MuxerApi CreateMuxer(String path) {
		
		MuxerApi result = null;

		L.d("new S/W Muxer");
		result = new SWMuxer();
		if (!result.init(path)) {
			result.destoryMuxer();
			result = null;
			L.e("Can not Support Muxer");
		}
		return result;
	}
}
