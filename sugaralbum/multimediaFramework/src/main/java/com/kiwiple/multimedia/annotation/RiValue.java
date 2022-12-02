package com.kiwiple.multimedia.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Resolution-independent value.<br />
 * 해상도에 비례해야 하는 수치임을 명시하기 위한 애너테이션.<br />
 * <br />
 * 기본적으로 다음과 같은 필드에 적용할 수 있습니다.
 * <ul>
 * <li>{@code float}</li>
 * <li>{@link Float}</li>
 * <li>{@code double}</li>
 * <li>{@link Double}</li>
 * </ul>
 * 단, {@link #container()}를 {@code true}로 명시했을 때에 한하여 사용자 정의 클래스에 적용할 수도 있는데, 이때에는 클래스 내부의 모든
 * {@code RiValue} 명시 필드에 대해 동일한 작업을 재귀적으로 수행합니다.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RiValue {

	boolean container() default false;
}
