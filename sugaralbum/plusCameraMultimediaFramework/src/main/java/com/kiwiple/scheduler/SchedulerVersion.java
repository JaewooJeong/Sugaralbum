package com.kiwiple.scheduler;


import java.io.Serializable;
import java.util.StringTokenizer;

import org.json.JSONException;
import org.json.JSONObject;

import com.kiwiple.debug.L;

public final class SchedulerVersion implements Serializable {

	private static final long serialVersionUID = -6962495161228828680L;

	// // // // // Static variable.
	// // // // //
	private static final String VERSION_NUMBER_SEPARATOR = "_";

	public static final String JSON_NAME_MAJOR = "major";
	public static final String JSON_NAME_MINOR = "minor";
	public static final String JSON_NAME_PATCH = "patch";
	
	public static final String JSON_KEY_SCHEDULER_VERSION = "scheduler_version"; 

	// // // // // Member variable.
	// // // // //
	/**
	 * 주 버전. 기존 버전과 호환되지 않는 경우 증가하는 버전입니다.
	 */
	public final int major;

	/**
	 * 부 버전. 기존 버전과 호환되면서 새로운 기능을 추가한 경우 증가하는 버전입니다.
	 */
	public final int minor;

	/**
	 * 수 버전. 기존 버전과 호환되면서 버그를 수정한 경우 증가하는 버전입니다.
	 */
	public final int patch;

	/**
	 * 개정 번호. 버전 관리 시스템의 업데이트 내역에 따른 개정 횟수에 따라 증가합니다.
	 */
//	public final int revision;

	// // // // // Constructor.
	// // // // //
	public SchedulerVersion(int major, int minor, int patch) {

		this.major = major;
		this.minor = minor;
		this.patch = patch;
	}

	public SchedulerVersion(JSONObject jsonObject) throws JSONException {
		this(jsonObject.getInt(JSON_NAME_MAJOR), jsonObject.getInt(JSON_NAME_MINOR), jsonObject.getInt(JSON_NAME_PATCH));
	}
	
	public SchedulerVersion(String version){
		
		this.major = Integer.parseInt(version.substring(0, 1)); 
		this.minor = Integer.parseInt(version.substring(2, 3)); 
		this.patch = Integer.parseInt(version.substring(4, 5));		
	}

	// // // // // Method.
	// // // // //
	@Override
	public String toString() {

		StringBuilder builder = new StringBuilder();
		builder.append(major);
		builder.append(VERSION_NUMBER_SEPARATOR);
		builder.append(minor);
		builder.append(VERSION_NUMBER_SEPARATOR);
		builder.append(patch);
		return builder.toString();
	}

	public JSONObject toJsonObject() throws JSONException {

		JSONObject jsonObject = new JSONObject();
		jsonObject.put(JSON_NAME_MAJOR, major);
		jsonObject.put(JSON_NAME_MINOR, minor);
		jsonObject.put(JSON_NAME_PATCH, patch);
		return jsonObject;
	}

	/**
	 * 주어진 {@code Version}과 비교하여 보다 상위 버전인지의 여부를 반환합니다.
	 * 
	 * @param other
	 *            비교 대상이 되는 {@code Version} 객체.
	 * @return 상위 버전인 경우 {@code true}.
	 */
	public boolean isAbove(SchedulerVersion other) {

		if (this.major > other.major) {
			return true;
		} else if (this.major < other.major) {
			return false;
		}
		if (this.minor > other.minor) {
			return true;
		} else if (this.minor < other.minor) {
			return false;
		}
		if (this.patch > other.patch) {
			return true;
		} else if (this.patch < other.patch) {
			return false;
		}
		return false;
	}

	/**
	 * 주어진 {@code Version}과 비교하여 보다 하위 버전인지의 여부를 반환합니다.
	 * 
	 * @param other
	 *            비교 대상이 되는 {@code Version} 객체.
	 * @return 하위 버전인 경우 {@code true}.
	 */
	public boolean isBelow(SchedulerVersion other) {

		if (this.major < other.major) {
			return true;
		} else if (this.major > other.major) {
			return false;
		}
		if (this.minor < other.minor) {
			return true;
		} else if (this.minor > other.minor) {
			return false;
		}
		if (this.patch < other.patch) {
			return true;
		} else if (this.patch > other.patch) {
			return false;
		}
		return false;
	}
	
	public boolean isSame(SchedulerVersion other){
		if (this.major == other.major && this.minor == other.minor && this.patch == other.patch) {
			return true;
		}else{
			return false; 
		}
	}
}