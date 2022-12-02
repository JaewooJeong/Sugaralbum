package com.kiwiple.multimedia.util;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import com.kiwiple.debug.Precondition;

/**
 * 라이브러리 개발 목적으로 사용하는 클래스입니다. 라이브러리 외부에서의 사용에 대해서는 그 유효성을 보장하지 않습니다.
 */
@SuppressWarnings("unchecked")
public final class CollectionUtils {

	/**
	 * 주어진 {@link Collection}의 각 요소를 복사한 사본을 반환합니다. 이때, 사본에 해당하는 {@code Collection}은 기본 생성자를 통해 생성되기
	 * 때문에, 기본 생성자를 제공하지 않는 {@code Collection}에 대해 본 메서드를 사용한다면 오류가 발생하게 됩니다.<br />
	 * <br />
	 * 복사는 전적으로 요소로서 포함되어 있는 각 객체가 구현한 {@link Object#clone()}에 의해 이루어집니다. 즉, {@code Collection}의
	 * 입장에서는 deep copy가 이루어지는 셈이지만, 각 요소의 관점에서는 그렇지 않을 수도 있다는 것을 염두에 두어야 합니다.
	 * 
	 * @param original
	 *            복사할 원본 {@code Collection}.
	 * @return
	 * 		주어진 {@code Collection}의 사본.
	 */
	public static <C extends Collection<S>, S extends Cloneable> C deepClone(C collection) {
		Precondition.checkNotNull(collection);

		try {
			Method cloneMethod = Object.class.getDeclaredMethod("clone");
			C copy = (C) collection.getClass().newInstance();

			Iterator<S> iterator = collection.iterator();
			while (iterator.hasNext()) {
				S element = iterator.next();
				copy.add((S) cloneMethod.invoke(element));
			}
			return copy;
		} catch (Exception exception) {
			return Precondition.assureUnreachable(exception.getMessage());
		}
	}

	public static <C extends Collection<S>, S, T extends S> C removeAll(C collection, T object) {
		Precondition.checkNotNull(collection);

		if (object == null) {
			return removeAllNull(collection);
		}

		Iterator<S> iterator = collection.iterator();
		while (iterator.hasNext()) {
			if (object.equals(iterator.next())) {
				iterator.remove();
			}
		}
		return collection;
	}

	public static <C extends Collection<S>, S, T extends S> C removeAll(C collection, T... objects) {
		Precondition.checkNotNull(collection, objects);

		collection.removeAll(Arrays.asList(objects));
		return collection;
	}

	public static <C extends Collection<S>, S, T extends S> C removeAllNull(C collection) {
		Precondition.checkNotNull(collection);

		Iterator<S> iterator = collection.iterator();
		while (iterator.hasNext()) {
			if (iterator.next() == null) {
				iterator.remove();
			}
		}
		return collection;
	}

	private CollectionUtils() {
		// do not instantiate.
	}
}
