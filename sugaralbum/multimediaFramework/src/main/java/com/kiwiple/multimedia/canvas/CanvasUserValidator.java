package com.kiwiple.multimedia.canvas;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import android.content.Context;

import com.kiwiple.debug.L;
import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.ModifierParser;
import com.kiwiple.multimedia.util.Size;
import com.kiwiple.multimedia.util.StringUtils;

/**
 * CanvasUserValidator.
 */
final class CanvasUserValidator {

	// // // // // Static variable.
	// // // // //
	private static final Message.Type INFO = Message.Type.INFO;
	private static final Message.Type WARN = Message.Type.WARN;
	private static final Message.Type ERROR = Message.Type.ERROR;

	private static final String FIELD_NAME_PREFIX_JSON = "JSON_";
	private static final String REGEX_FOR_JSON_NAME = "^[a-z0-9_]+$";
	private static final Pattern sJsonNamePattern = Pattern.compile(REGEX_FOR_JSON_NAME, 0);

	private static Boolean validated;

	// // // // // Member variable.
	// // // // //
	private final List<Class<? extends AbstractCanvasUser>> mClasses;
	private final List<Message> mMessages;

	private final Visualizer mVisualizer;
	private final Region mRegion;
	private final Scene mScene;

	// // // // // Static method.
	// // // // //
	static synchronized boolean validate(Context context, List<Class<? extends AbstractCanvasUser>> classes) {
		Precondition.checkCollection(classes).checkNotEmpty().checkNotContainsNull();

		if (validated == null) {
			try {
				CanvasUserValidator validator = new CanvasUserValidator(context, classes);
				validated = validator.validate();
			} catch (IOException exception) {
				exception.printStackTrace();
			}
			L.i("validation result: " + validated);
		}
		if (validated) {
			return true;
		}
		return Precondition.assureUnreachable("failed to validate library.");
	}

	private static Message newMessage(Message.Type type, String text) {
		return new Message(type, text);
	}

	private static Message newMessage(Message.Type type, String format, Object... args) {
		return new Message(type, StringUtils.format(format, args));
	}

	private static String toString(Throwable throwable) {

		StringBuilder builder = new StringBuilder();
		builder.append(throwable.getClass().getSimpleName());

		String message = throwable.getMessage();
		if (message != null && !message.trim().isEmpty()) {
			builder.append("(");
			builder.append(message);
			builder.append(")");
		}
		return builder.toString();
	}

	private static void printMessage(Message message) {

		switch (message.type) {
			case INFO:
				L.i("\t- " + message.text);
				break;
			case WARN:
				L.w("\t- " + message.text);
				break;
			case ERROR:
				L.e("\t- " + message.text);
				break;
			default:
				Precondition.assureUnreachable();
		}
	}

	private static boolean checkExistOverriddenMethod(Class<?> type, String methodName, Class<?>... methodParameterTypes) {

		try {
			Method method = type.getDeclaredMethod(methodName, methodParameterTypes);
			ModifierParser modifiers = new ModifierParser(method);
			if (modifiers.isBridge) {
				return false;
			}
		} catch (NoSuchMethodException exception) {
			return false;
		}
		return true;
	}

	// // // // // Constructor.
	// // // // //
	private CanvasUserValidator(Context context, List<Class<? extends AbstractCanvasUser>> classes) {

		mClasses = classes;
		mMessages = new ArrayList<Message>();

		mVisualizer = new Visualizer(context, new Resolution("validator resolution", new Size(16, 9)));
		mVisualizer.setEditMode(true);

		mRegion = mVisualizer.getRegion();
		mScene = new MultiLayerScene(mRegion);
	}

	// // // // // Method.
	// // // // //
	boolean validate() throws IOException {

		if (validated != null) {
			return validated;
		}

		boolean someErrorOccurred = false;

		for (Class<? extends AbstractCanvasUser> regionChildClass : mClasses) {
			if (!RegionChild.class.isAssignableFrom(regionChildClass) || regionChildClass.equals(NullTransition.class)) {
				continue; // Visualizer or Region or NullTransition.
			}

			L.d("validate " + regionChildClass.getName() + " ...");
			RegionChild sample = null;
			try {
				sample = createSample(regionChildClass);
				validateCommon(regionChildClass, sample);

				if (sample instanceof Scene) {
					validateScene(regionChildClass, (Scene) sample);
				} else if (sample instanceof Transition) {
					validateTransition(regionChildClass, (Transition) sample);
				} else if (sample instanceof Effect) {
					validateEffect(regionChildClass, (Effect) sample);
				} else if (sample instanceof Scaler) {
					validateScaler(regionChildClass, (Scaler) sample);
				}
			} catch (Exception exception) {
				exception.printStackTrace();

				someErrorOccurred = true;
				mMessages.add(newMessage(ERROR, "some error occurred with %s", toString(exception)));
			} finally {
				if (sample != null) {
					sample.release();
				}
			}
			for (Message message : mMessages) {
				someErrorOccurred |= message.type.equals(ERROR);
				printMessage(message);
			}
			mMessages.clear();
		}
		mVisualizer.release();

		return someErrorOccurred ? false : true;
	}

	private RegionChild createSample(Class<?> regionChildClass) {

		for (Constructor<?> constructor : regionChildClass.getDeclaredConstructors()) {
			Class<?>[] parameterTypes = constructor.getParameterTypes();
			if (parameterTypes.length == 1) {
				Class<?> parameterType = parameterTypes[0];
				try {
					if (parameterType.equals(Region.class)) {
						return CanvasUserFactory.createRegionChild(regionChildClass, mRegion);
					} else if (parameterType.equals(Scene.class) || parameterType.equals(MultiLayerScene.class)) {
						return CanvasUserFactory.createRegionChild(regionChildClass, mScene);
					}
				} catch (Exception exception) {
					continue;
				}
			}
		}
		return Precondition.assureUnreachable();
	}

	private void validateCommon(Class<?> regionChildClass, RegionChild sample) throws Exception {

		if (!ReflectionUtils.isFinalClass(regionChildClass)) {
			mMessages.add(newMessage(ERROR, "must be final class!"));
		}

		for (Constructor<?> constructor : regionChildClass.getDeclaredConstructors()) {
			ModifierParser modifiers = new ModifierParser(constructor);
			if (modifiers.isPublic || modifiers.isProtected) {
				mMessages.add(newMessage(ERROR, "it can only have private or package-private constructor."));
			}
		}

		for (Field field : regionChildClass.getDeclaredFields()) {
			ModifierParser modifiers = new ModifierParser(field);
			String fieldName = field.getName();

			if (fieldName.contains("$SWITCH_TABLE$")) {
				continue;
			}

			if (modifiers.isStatic && !modifiers.isFinal) {
				mMessages.add(newMessage(ERROR, "is there some reason why %s is static and non-final?", fieldName));
			}
			if (!modifiers.isFinal && !modifiers.isPrivate) {
				mMessages.add(newMessage(ERROR, "is there some reason why %s is neither final nor private?", fieldName));
			}
			if (fieldName.startsWith(FIELD_NAME_PREFIX_JSON)) {
				validateJsonField(field, sample);
			}
		}

		for (Method method : ReflectionUtils.getDeclaredMethodsWithoutInherited(regionChildClass)) {
			ModifierParser modifiers = new ModifierParser(method);
			String methodName = method.getName();

			if (modifiers.isProtected) {
				mMessages.add(newMessage(INFO, "protected access modifier is meaningless. use package-private for %s()", methodName));
			}
			if (methodName.startsWith("set")) {
				if (modifiers.isPublic) {
					mMessages.add(newMessage(WARN, "you must be sure that %s() is not relevant to draw stuff!", methodName));
				}
			}
		}
		validateEditor(regionChildClass, sample);
		validateJsonObject(regionChildClass, sample);
	}

	private void validateScene(Class<?> sceneClass, Scene sample) {

		if (!(sceneClass.getSimpleName().endsWith("Scene"))) {
			mMessages.add(newMessage(ERROR, "Scene name must be end with 'Scene'"));
		}

		int overriddenCacheMethodCount = 0;
		overriddenCacheMethodCount += checkExistOverriddenMethod(sceneClass, "getCacheCount") ? 1 : 0;
		overriddenCacheMethodCount += checkExistOverriddenMethod(sceneClass, "createCacheAsBitmap", int.class) ? 1 : 0;
		overriddenCacheMethodCount += checkExistOverriddenMethod(sceneClass, "prepareCanvasWithCache") ? 1 : 0;
		overriddenCacheMethodCount += checkExistOverriddenMethod(sceneClass, "prepareCanvasWithoutCache") ? 1 : 0;
		if (overriddenCacheMethodCount != 0 && overriddenCacheMethodCount != 4) {
			String message = "Scene must override all of or none of : { getCacheCount, createCacheAsBitmap, prepareCanvasWithCache, prepareCanvasWithoutCache }";
			mMessages.add(newMessage(ERROR, message));
		}
	}

	private void validateTransition(Class<?> transitionClass, Transition sample) {

		if (!transitionClass.getSimpleName().endsWith("Transition")) {
			mMessages.add(newMessage(ERROR, "Scene name must be end with 'Transition'"));
		}
	}

	private void validateEffect(Class<?> effectClass, Effect sample) {

		if (!effectClass.getSimpleName().endsWith("Effect")) {
			mMessages.add(newMessage(ERROR, "Scene name must be end with 'Effect'"));
		}
	}

	private void validateScaler(Class<?> scalerClass, Scaler sample) {

		if (!scalerClass.getSimpleName().endsWith("Scaler")) {
			mMessages.add(newMessage(ERROR, "Scene name must be end with 'Scaler'"));
		}
	}

	private void validateEditor(Class<?> regionChildClass, RegionChild sample) {

		boolean isEditorDeclared = false;
		for (Class<?> innerClass : regionChildClass.getDeclaredClasses()) {
			if (!AbstractCanvasUser.Editor.class.isAssignableFrom(innerClass)) {
				continue;
			}

			isEditorDeclared = true;
			ModifierParser modifiers = new ModifierParser(innerClass);
			if (!modifiers.isPublic || !modifiers.isStatic || !modifiers.isFinal) {
				mMessages.add(newMessage(ERROR, "Editor must be public static final."));
			}
		}
		if (!isEditorDeclared) {
			mMessages.add(newMessage(ERROR, "there is no Editor class."));
		}

		if (!checkExistOverriddenMethod(regionChildClass, "getEditor")) {
			mMessages.add(newMessage(ERROR, "you must override getEditor()!"));
		}

		AbstractCanvasUser.Editor<?, ?> editor = sample.getEditor();
		if (editor == null) {
			mMessages.add(newMessage(ERROR, "you must return a valid Editor from getEditor(), not null."));
		}
	}

	private void validateJsonObject(Class<?> regionChildClass, RegionChild sample) {

		try {
			JsonObject jsonObject = sample.toJsonObject();
			if (jsonObject == null) {
				mMessages.add(newMessage(ERROR, "you must return a valid JsonObject from toJsonObject(), not null."));
			} else if (jsonObject.isNull(RegionChild.JSON_NAME_TYPE)) {
				mMessages.add(newMessage(ERROR, "JsonObject must contain JSON_NAME_TYPE!"));
			}
		} catch (Exception exception) {
			exception.printStackTrace();
			mMessages.add(newMessage(ERROR, "some error occurred in toJsonObject() with %s", toString(exception)));
		}
	}

	private void validateJsonField(Field jsonField, RegionChild sample) {

		ModifierParser modifiers = new ModifierParser(jsonField);
		String fieldName = jsonField.getName();

		if (!modifiers.isFinal || !modifiers.isStatic) {
			mMessages.add(newMessage(ERROR, "is there some reason why %s is not final or not static?", fieldName));
		}

		Object value = ReflectionUtils.getValue(sample, jsonField);
		if (value instanceof String) {
			if (value == null || !sJsonNamePattern.matcher((String) value).matches()) {
				mMessages.add(newMessage(ERROR, "JSON name(%s) must consist of %s", fieldName, REGEX_FOR_JSON_NAME));
			}
		} else {
			mMessages.add(newMessage(INFO, "are you sure that %s is not a String?", fieldName));
		}
	}

	// // // // // Inner class.
	// // // // //
	static final class Message {

		final Type type;
		final String text;

		Message(Type type, String text) {
			Precondition.checkNotNull(type, text);

			this.type = type;
			this.text = text;
		}

		static enum Type {
			INFO, WARN, ERROR;
		}
	}
}