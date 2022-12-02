package com.kiwiple.multimedia.util;

import java.util.Arrays;
import java.util.Locale;

import com.kiwiple.debug.Precondition;

public final class StringUtils {

	/**
	 * No localization.
	 */
	public static String format(String format, Object... args) {
		return String.format((Locale) null, format, args);
	}

	public static String repeat(char value, int number) {
		Precondition.checkNotNegative(number);

		char[] array = new char[number];
		Arrays.fill(array, value);
		return String.valueOf(array);
	}

	private StringUtils() {
		// Do not instantiate.
	}
}
