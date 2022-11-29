package com.kiwiple.multimedia.json;

import java.lang.reflect.Array;
import java.util.Locale;

import com.kiwiple.debug.Precondition;

/**
 * JsonUtils
 */
@SuppressWarnings("unchecked")
public final class JsonUtils {

	public static String toJsonString(Class<?> type) {
		Precondition.checkNotNull(type);

		if (type.isAnonymousClass()) {
			String name = type.getName();
			return toJsonString(name.substring(name.lastIndexOf('.') + 1));
		} else {
			return toJsonString(type.getSimpleName());
		}
	}

	public static String toJsonString(Enum<?> type) {
		Precondition.checkNotNull(type);
		return toJsonString(type.name());
	}

	public static String toJsonString(String string) {
		Precondition.checkNotNull(string);

		if (string.isEmpty())
			return new String();
		if (isNotContainLowerCase(string))
			return string.toLowerCase(Locale.ENGLISH);

		char[] className = string.toCharArray();
		StringBuilder builder = new StringBuilder();

		builder.append(Character.toLowerCase(className[0]));
		for (int i = 1; i != className.length; ++i) {
			char character = className[i];
			builder.append(Character.isUpperCase(character) ? "_" + Character.toLowerCase(character) : character);
		}
		return builder.toString();
	}

	private static boolean isNotContainLowerCase(String string) {

		for (char s : string.toCharArray())
			if (Character.isLetter(s) && Character.isLowerCase(s))
				return false;
		return true;
	}

	public static <T extends Enum<?>> T getEnumByJsonString(String name, Class<? extends T> type) {
		Precondition.checkNotNull(name, type);

		try {
			Object elements = type.getMethod("values").invoke(null);
			for (int i = 0, length = Array.getLength(elements); i != length; ++i) {
				T element = (T) Array.get(elements, i);
				if (name.equalsIgnoreCase(element.name()))
					return element;
			}
		} catch (Exception exception) {
			return Precondition.assureUnreachable(exception.getMessage());
		}
		throw new IllegalArgumentException("There is no value in " + type.getSimpleName());
	}

	private JsonUtils() {
		// Utility classes should have a private constructor.
	}
}
