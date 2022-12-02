package com.kiwiple.multimedia.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * ModifierParser.
 * 
 * @see Modifier
 */
public final class ModifierParser {

	public final int rawFlags;

	public final boolean isPublic;
	public final boolean isPrivate;
	public final boolean isProtected;
	public final boolean isPackage;
	public final boolean isStatic;
	public final boolean isFinal;
	public final boolean isSynchronized;
	public final boolean isVolatile;
	public final boolean isTransient;
	public final boolean isNative;
	public final boolean isInterface;
	public final boolean isAbstract;
	public final boolean isStrict;
	public final boolean isBridge;

	public ModifierParser(Field field) {
		rawFlags = field.getModifiers();

		isPublic = Modifier.isPublic(rawFlags);
		isPrivate = Modifier.isPrivate(rawFlags);
		isProtected = Modifier.isProtected(rawFlags);
		isPackage = !(isPublic || isPrivate || isProtected);
		isStatic = Modifier.isStatic(rawFlags);
		isFinal = Modifier.isFinal(rawFlags);
		isSynchronized = false;
		isVolatile = Modifier.isVolatile(rawFlags);
		isTransient = Modifier.isTransient(rawFlags);
		isNative = false;
		isInterface = false;
		isAbstract = false;
		isStrict = false;
		isBridge = false;
	}

	public ModifierParser(Method method) {
		rawFlags = method.getModifiers();

		isPublic = Modifier.isPublic(rawFlags);
		isPrivate = Modifier.isPrivate(rawFlags);
		isProtected = Modifier.isProtected(rawFlags);
		isPackage = !(isPublic || isPrivate || isProtected);
		isStatic = Modifier.isStatic(rawFlags);
		isFinal = Modifier.isFinal(rawFlags);
		isSynchronized = Modifier.isSynchronized(rawFlags);
		isVolatile = false;
		isTransient = false;
		isNative = Modifier.isNative(rawFlags);
		isInterface = false;
		isAbstract = Modifier.isAbstract(rawFlags);
		isStrict = Modifier.isStrict(rawFlags);
		isBridge = Modifier.isVolatile(rawFlags); // using isVolatile() is intentional.
	}

	public ModifierParser(Constructor<?> constructor) {
		rawFlags = constructor.getModifiers();

		isPublic = Modifier.isPublic(rawFlags);
		isPrivate = Modifier.isPrivate(rawFlags);
		isProtected = Modifier.isProtected(rawFlags);
		isPackage = !(isPublic || isPrivate || isProtected);
		isStatic = false;
		isFinal = false;
		isSynchronized = false;
		isVolatile = false;
		isTransient = false;
		isNative = false;
		isInterface = false;
		isAbstract = false;
		isStrict = false;
		isBridge = false;
	}

	public ModifierParser(Class<?> type) {
		rawFlags = type.getModifiers();

		isPublic = Modifier.isPublic(rawFlags);
		isPrivate = Modifier.isPrivate(rawFlags);
		isProtected = Modifier.isProtected(rawFlags);
		isPackage = !(isPublic || isPrivate || isProtected);
		isStatic = Modifier.isStatic(rawFlags);
		isFinal = Modifier.isFinal(rawFlags);
		isSynchronized = false;
		isVolatile = false;
		isTransient = false;
		isNative = false;
		isInterface = Modifier.isInterface(rawFlags);
		isAbstract = Modifier.isAbstract(rawFlags);
		isStrict = Modifier.isStrict(rawFlags);
		isBridge = false;
	}

	@Override
	public String toString() {

		if (rawFlags == 0 && !isPackage) {
			return "[there is no modifiers]";
		}

		StringBuilder builder = new StringBuilder();
		builder.append("[");

		if (isPublic) {
			builder.append("public|");
		}
		if (isPrivate) {
			builder.append("private|");
		}
		if (isProtected) {
			builder.append("protected|");
		}
		if (isPackage) {
			builder.append("package|");
		}
		if (isStatic) {
			builder.append("static|");
		}
		if (isFinal) {
			builder.append("final|");
		}
		if (isSynchronized) {
			builder.append("synchronized|");
		}
		if (isVolatile) {
			builder.append("volatile|");
		}
		if (isTransient) {
			builder.append("transient|");
		}
		if (isInterface) {
			builder.append("interface|");
		}
		if (isAbstract) {
			builder.append("abstract|");
		}
		if (isStrict) {
			builder.append("strict|");
		}
		if (isBridge) {
			builder.append("bridge|");
		}

		int length = builder.length();
		builder.delete(length - 1, length);
		builder.append("]");

		return builder.toString();
	}
}