package com.kiwiple.multimedia.canvas;

import androidx.annotation.Keep;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.kiwiple.multimedia.util.ModifierParser;

/**
 * ReflectionUtils.
 * 
 */
@Keep
final class ReflectionUtils {

	static boolean isAbstractClass(Class<?> cls) {
		return Modifier.isAbstract(cls.getModifiers());
	}

	static boolean isFinalClass(Class<?> cls) {
		return Modifier.isFinal(cls.getModifiers());
	}

	static boolean isStaticClass(Class<?> cls) {
		return Modifier.isStatic(cls.getModifiers());
	}

	static boolean isFinalField(Field field) {
		return Modifier.isFinal(field.getModifiers());
	}

	static boolean isStaticField(Field field) {
		return Modifier.isStatic(field.getModifiers());
	}

	static Object getValue(Object object, Field field) {
		return getValue(object, field, true);
	}

	static Object getValue(Object object, Field field, boolean breakProtection) {

		try {
			Object value;
			if (breakProtection && !field.isAccessible()) {
				field.setAccessible(true);
				value = field.get(object);
				field.setAccessible(false);
			} else {
				value = field.get(object);
			}
			return value;
		} catch (IllegalAccessException | IllegalArgumentException exception) {
			exception.printStackTrace();
			return null;
		}
	}

	static void setValue(Object object, Object value, Field field) {
		setValue(object, value, field, true);
	}

	static void setValue(Object object, Object value, Field field, boolean breakProtection) {

		try {
			if (breakProtection && !field.isAccessible()) {
				field.setAccessible(true);
				field.set(object, value);
				field.setAccessible(false);
			} else {
				field.set(object, value);
			}
		} catch (IllegalAccessException | IllegalArgumentException exception) {
			exception.printStackTrace();
		}
	}

	static List<Method> getDeclaredMethodsWithoutInherited(Class<?> type) {

		ArrayList<Method> result = new ArrayList<>();
		ArrayList<Method> inheritableMethods = findAllInheritableMethods(type);

		label: for (Method method : type.getDeclaredMethods()) {

			ModifierParser modifiers = new ModifierParser(method);
			if (modifiers.isBridge || method.getName().contains("$")) {
				continue;
			}
			for (Method inheritable : inheritableMethods) {
				if (isSameSignature(method, inheritable)) {
					continue label;
				}
			}
			result.add(method);
		}
		return result;
	}

	static List<Field> getDeclaredFieldWithInherited(Class<?> type) {

		ArrayList<Field> result = new ArrayList<>();

		while (type != null) {
			result.addAll(Arrays.asList(type.getDeclaredFields()));
			type = type.getSuperclass();
		}
		return result;
	}

	private static ArrayList<Method> findAllInheritableMethods(Class<?> type) {

		ArrayList<Class<?>> superClasses = new ArrayList<>();
		Collections.addAll(superClasses, type.getInterfaces());

		for (Class<?> superClass = type.getSuperclass(); superClass != null; superClass = superClass.getSuperclass()) {
			superClasses.add(superClass);
			Collections.addAll(superClasses, superClass.getInterfaces());
		}

		ArrayList<Method> methods = new ArrayList<>();

		for (Class<?> superClass : superClasses) {
			for (Method method : superClass.getDeclaredMethods()) {
				if (isInheritableMethod(method)) {
					methods.add(method);
				}
			}
		}
		return methods;
	}

	private static boolean isInheritableMethod(Method method) {

		ModifierParser modifiers = new ModifierParser(method);
		if (modifiers.isPrivate || modifiers.isBridge || modifiers.isFinal || modifiers.isStatic) {
			return false;
		}
		return true;
	}

	private static boolean isSameSignature(Method method1, Method method2) {

		if (method1.getName().equals(method2.getName())) {

			List<Class<?>> parameters1 = Arrays.asList(method1.getParameterTypes());
			List<Class<?>> parameters2 = Arrays.asList(method2.getParameterTypes());
			if (parameters1.size() == parameters2.size() && parameters1.containsAll(parameters2)) {
				return true;
			}
		}
		return false;
	}

	@Keep
	static Class<?> getGenericType(Field field, int index) {
		ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
		return (Class<?>) parameterizedType.getActualTypeArguments()[index];
	}

	private ReflectionUtils() {
		// do not instantiate.
	}
}
