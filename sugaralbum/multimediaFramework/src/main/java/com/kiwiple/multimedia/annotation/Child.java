package com.kiwiple.multimedia.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.Map;

import com.kiwiple.multimedia.canvas.Region;

/**
 * {@link RegionChild}의 내부에 포함된 {@code RegionChild}를 명시하기 위한 애너테이션.<br />
 * {@link Region}에 직접 소속되지 않는 {@code RegionChild}가 제대로 제어를 받기 위해 반드시 사용해야 합니다.<br />
 * <br />
 * 다음과 같은 필드에 적용할 수 있습니다.
 * <ul>
 * <li>{@code RegionChild}</li>
 * <li>{@code RegionChild[]}</li>
 * <li>{@link Collection}{@code <? extends RegionChild>}</li>
 * <li>{@link Map}{@code <? extends RegionChild, ?>}</li>
 * <li>{@code Map<?, ? extends RegionChild>}</li>
 * </ul>
 * 단, {@code Map}에 사용하고자 할 때에는 key-value 쌍에서 적용할 부분을 명시하기 위해 {@link #target()}을 정의해야 합니다.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Child {

	/**
	 * 애너테이션 대상이 {@link Map}인 경우에 대한 적용 부분.
	 * 
	 * @see Target
	 */
	Target target() default Target.NON_USE;

	/**
	 * {@link Map}의 key와 value를 구분하기 위한 열거형.
	 * 
	 * @see KEY
	 * @see VALUE
	 */
	public static enum Target {

		/**
		 * 애너테이션 적용 대상이 {@link Map}이 아닐 때 사용하는 기본 값입니다.
		 */
		NON_USE,

		/**
		 * {@link Map#keySet()}로부터 접근 가능한 모든 요소를 의미합니다.
		 */
		KEY,

		/**
		 * {@link Map#values()}로부터 접근 가능한 모든 요소를 의미합니다.
		 */
		VALUE;
	}
}
