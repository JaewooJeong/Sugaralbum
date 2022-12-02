package com.kiwiple.multimedia.canvas;

/**
 * {@link Region}의 제어를 받는 {@link RegionChild}의 시간 위치에 따른 출력 상태를 정의한 열거형.
 * 
 * @see FocusState#ON
 * @see FocusState#PRELIMINARY
 * @see FocusState#OFF
 */
enum FocusState {

	/**
	 * 출력되고 있는 상태를 의미합니다. {@code Region}의 {@code Scene} 목록에서 ON 상태를 가지는 Scene은 {@code Transition}이
	 * 작동하는 상태일 때에는 둘, 그렇지 않을 때에는 유일합니다.
	 */
	ON,

	/**
	 * {@link #ON} 상태가 되기 직전의 상태를 의미합니다. {@code Region}의 {@code Scene} 목록에서 {@code PRELIMINARY} 상태를
	 * 가지는 {@code Scene}은 {@code Transition}이 작동하지 않는 상태에서 마지막 {@code Scene}이 아닐 때에만 유일하게 존재합니다.
	 */
	PRELIMINARY,

	/**
	 * {@link #ON}과 {@link #PRELIMINARY} 상태가 아님을 의미합니다.
	 */
	OFF
}
