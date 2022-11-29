package com.kiwiple.multimedia.canvas;

import com.kiwiple.multimedia.json.JsonObject;

/**
 * {@link Visualizer}가 상태를 저장하거나 복원할 때 사용하는 일련의 데이터의 호환성을 검사하거나, 가능하다면 호환 가능한 상태로 개정해주는 클래스를 구현하기 위한
 * 인터페이스.
 * 
 */
interface IScriptCorrector {

	// // // // // Method.
	// // // // //
	/**
	 * {@link JsonObject} 객체가 라이브러리와 호환이 되는지 검증합니다.
	 * 
	 * @param scriptJsonObject
	 *            검증 대상 객체.
	 * @return 호환성 상태에 대한 식별자.
	 * @see Compatibility
	 */
	public abstract Compatibility isCompatible(JsonObject scriptJsonObject);

	/**
	 * {@link JsonObject} 객체를 라이브러리와 호환 가능한 상태로 개정합니다.
	 * 
	 * @param scriptJsonObject
	 *            개정 대상 객체.
	 * @return 개정이 성공적으로 끝난 경우 true.
	 */
	public abstract boolean upgrade(JsonObject scriptJsonObject);

	// // // // // Enumeration.
	// // // // //
	/**
	 * 호환성 상태를 식별하기 위한 열거형.
	 * 
	 * @see #COMPATIBLE
	 * @see #COMPATIBLE_WITH_UPGRADE
	 * @see #INCOMPATIBLE
	 */
	static enum Compatibility {

		/**
		 * 문제 없이 호환이 가능함을 의미합니다.
		 */
		COMPATIBLE,

		/**
		 * 일부 데이터를 개정한 후에 호환이 가능함을 의미합니다.
		 */
		COMPATIBLE_WITH_UPGRADE,

		/**
		 * 개정 및 호환이 불가능함을 의미합니다.
		 */
		INCOMPATIBLE;
	}
}
