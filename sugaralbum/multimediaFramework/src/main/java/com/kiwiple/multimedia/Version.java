package com.kiwiple.multimedia;

import org.json.JSONException;

import com.kiwiple.debug.L;
import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.json.IJsonConvertible;
import com.kiwiple.multimedia.json.JsonObject;

/**
 * MultimediaFramework 라이브러리 버전 관리를 위한 클래스.
 */
public final class Version implements IJsonConvertible {

	// // // // // Static variable.
	// // // // //
	/*
	 * current.revision은 디버깅 편의를 위해 저장하는 정보입니다. revision 값은 git 저장소의 전체 commit 횟수의 누계를 사용하도록 하며, 이는
	 * 다음 명령어를 통해 확인할 수 있습니다: git rev-list --remotes --count
	 */
	public static final Version current = new Version(1, 4, 0, 2510, false);

	public static final String JSON_NAME_MAJOR = "major";
	public static final String JSON_NAME_MINOR = "minor";
	public static final String JSON_NAME_PATCH = "patch";
	public static final String JSON_NAME_REVISION = "revision";
	public static final String JSON_NAME_IS_DEV = "is_dev";

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
	public final int revision;

	public final boolean isDev;

	// // // // // Constructor.
	// // // // //
	static {
		L.i("Multimedia Framework version: " + current.toString());
	}

	public Version(int major, int minor, int patch) {
		this(major, minor, patch, 0, false);
	}

	public Version(int major, int minor, int patch, int revision, boolean isDev) {
		Precondition.checkNotNegative(major, minor, patch, revision);

		this.major = major;
		this.minor = minor;
		this.patch = patch;
		this.revision = revision;
		this.isDev = isDev;
	}

	public Version(JsonObject jsonObject) throws JSONException {
		this( /**/
				jsonObject.getInt(JSON_NAME_MAJOR), /**/
				jsonObject.getInt(JSON_NAME_MINOR), /**/
				jsonObject.getInt(JSON_NAME_PATCH), /**/
				jsonObject.optInt(JSON_NAME_REVISION, 0), /**/
				jsonObject.optBoolean(JSON_NAME_IS_DEV, false) /**/
		);
	}

	// // // // // Method.
	// // // // //
	@Override
	public String toString() {

		StringBuilder builder = new StringBuilder();
		builder.append(major);
		builder.append('.');
		builder.append(minor);
		builder.append('.');
		builder.append(patch);
		builder.append('-');
		builder.append(revision);

		if (isDev)
			builder.append(".dev");
		return builder.toString();
	}

	@Override
	public JsonObject toJsonObject() throws JSONException {

		JsonObject jsonObject = new JsonObject();
		jsonObject.put(JSON_NAME_MAJOR, major);
		jsonObject.put(JSON_NAME_MINOR, minor);
		jsonObject.put(JSON_NAME_PATCH, patch);
		jsonObject.put(JSON_NAME_REVISION, revision);

		if (isDev)
			jsonObject.put(JSON_NAME_IS_DEV, isDev);
		return jsonObject;
	}

	/**
	 * 주어진 {@code Version}과 비교하여 보다 상위 버전인지의 여부를 반환합니다.
	 * 
	 * @param other
	 *            비교 대상이 되는 {@code Version} 객체.
	 * @return 상위 버전인 경우 {@code true}.
	 */
	public boolean isAbove(Version other) {
		Precondition.checkNotNull(other);

		if (this.major > other.major)
			return true;
		else if (this.major < other.major)
			return false;

		if (this.minor > other.minor)
			return true;
		else if (this.minor < other.minor)
			return false;

		if (this.patch > other.patch)
			return true;
		else if (this.patch < other.patch)
			return false;

		if (isDev || other.isDev) {
			if (this.revision > other.revision)
				return true;
			else if (this.revision < other.revision)
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
	public boolean isBelow(Version other) {
		Precondition.checkNotNull(other);

		if (this.major < other.major)
			return true;
		else if (this.major > other.major)
			return false;

		if (this.minor < other.minor)
			return true;
		else if (this.minor > other.minor)
			return false;

		if (this.patch < other.patch)
			return true;
		else if (this.patch > other.patch)
			return false;

		if (isDev || other.isDev) {
			if (this.revision < other.revision)
				return true;
			else if (this.revision > other.revision)
				return false;
		}
		return false;
	}
}