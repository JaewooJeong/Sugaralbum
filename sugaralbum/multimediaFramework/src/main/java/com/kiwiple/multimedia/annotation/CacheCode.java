package com.kiwiple.multimedia.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import com.kiwiple.multimedia.canvas.ICacheCode;
import com.kiwiple.multimedia.canvas.ICanvasUser;

/**
 * 이미지 캐시 파일을 구분하기 위한 식별자를 생성할 때 필요한 값이라는 것을 명시하기 위한 애너테이션.<br />
 * <br />
 * 다음과 같은 필드에 적용할 수 있습니다.
 * <ul>
 * <li>primitive type</li>
 * <li>boxed primitive type</li>
 * <li>enum type</li>
 * <li>{@link String}</li>
 * <li>{@link ICacheCode}</li>
 * <li>{@link ICanvasUser}</li>
 * </ul>
 * 또한 이를 요소로 가지는 1차원 배열이나 {@link List}에 적용할 수 있습니다.<br />
 * <br />
 * 만약 해당 {@link RegionChild}가 복수 개의 캐시를 생성하고, 각 캐시의 식별자가 {@code CacheCode}가 명시된 배열이나 {@code List}에
 * 포함된 하나의 요소를 순차적으로 참조해야 한다면 {@link #indexed()}를 {@code true}로 정의하여 처리할 수 있습니다.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheCode {

	/**
	 * 애너테이션 대상이 배열 혹은 {@link List}인 경우, 이미지 캐시와 대상 배열 혹은 {@code List} 요소의 1:1 대응 여부.
	 */
	boolean indexed() default false;
}
