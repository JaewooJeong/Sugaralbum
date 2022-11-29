package com.kiwiple.mediaframework.util;

import com.kiwiple.mediaframework.encoder.AACEncoder.AACINFO;

public class ADTSManager {
	
	/**
	 * AAC Type ENum
	 */
	public enum AAC_PROFILE {
		/**	Default Type*/
		OMX_AUDIO_AACObjectLC(2), // 2
		/**	HE Type*/
		OMX_AUDIO_AACObjectHE(5), // 5
		/**	ELD Type*/
		OMX_AUDIO_AACObjectELD(39),	//39
		UN_KNOWN(100);
		int key;

		private AAC_PROFILE(int _key) {
			key = _key;
		}
		
		public int getKey()
		{
			return key;
		}
	}
	
	/**
	 * 오디오의 Frequency 값에 대한 index 반환 
	 * 
	 * @param freq Frequency 값
	 *            
	 */	
	static public int getFrequencyIdx(int	freq) {
		switch (freq) {
		case 96000:		//96000 hz
			return 0;
		case 88200:		//88200 hz
			return 1;
		case 64000:		//64000 hz
			return 2;
		case 48000:		//48000 hz
			return 3;
		case 44100:		//44100 hz
			return 4;
		case 32000:		//32000 hz
			return 5;
		case 24000:		//24000 hz
			return 6;
		case 22050:		//22050 hz
			return 7;
		case 16000:		//16000 hz
			return 8;
		case 12000:		//12000 hz
			return 9;
		case 11025:		//11025 hz
			return 10;
		case 8000:		//8000 hz
			return 11;
		case 7350:		//7350 hz
			return 12;
		default:
			return -1;
		}
	}
	
	/**
	 * set Adts data
	 * 
	 * @param packetLen
	 *            Packte size
	 */
	static public void setADTSByte(AACINFO info, byte[] data, int packetLen) {
		
		int profile = info.mProfile.getKey(); // AAC LC
		int freqIdx = info.mFreqIdx; // 44.1KHz
		int chanCfg = info.mChannel; // channel

		// fill in ADTS data
		data[0] = (byte) 0xFF;
		data[1] = (byte) 0xF9;
		data[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
		data[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
		data[4] = (byte) ((packetLen & 0x7FF) >> 3);
		data[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
		data[6] = (byte) 0xFC;
	}
}
