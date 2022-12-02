package com.kiwiple.multimedia.canvas;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import com.kiwiple.debug.L;
import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.Version;
import com.kiwiple.multimedia.annotation.CacheCode;
import com.kiwiple.multimedia.annotation.Child;
import com.kiwiple.multimedia.annotation.RiValue;
import com.kiwiple.multimedia.canvas.AbstractCanvasUser.Editor;
import com.kiwiple.multimedia.exception.MultimediaException;
import com.kiwiple.multimedia.json.JsonUtils;
import com.kiwiple.multimedia.util.DebugUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dalvik.system.DexFile;

import static com.kiwiple.multimedia.Constants.CANVAS_PACKAGE_NAME;

import androidx.annotation.Keep;

/**
 * CanvasUserEnvironment.
 */
@SuppressWarnings("unchecked")
@Keep
public final class CanvasUserEnvironment {

	// // // // // Static variable.
	// // // // //
	private static final String KEY_VALIDATED_REVISION_NUMBER = "validated_revision_number";

	private static boolean sCreated;

	// // // // // Member variable.
	// // // // //
	private final HashMap<String, Class<? extends AbstractCanvasUser>> mTypeNameClassMap;
	private final HashMap<Class<? extends AbstractCanvasUser>, String> mClassTypeNameMap;
	private final HashMap<Class<? extends AbstractCanvasUser>, List<Field>> mChildFieldsMap;
	private final HashMap<Class<? extends AbstractCanvasUser>, List<Field>> mCacheCodeFieldsMap;
	private final HashMap<Class<? extends AbstractCanvasUser>, Constructor<?>> mEditorConstructorMap;
	private final HashMap<Class<?>, List<Field>> mRiValueFieldsMap;

	private boolean mInitialized;

	// // // // // Constructor.
	// // // // //
	{
		mTypeNameClassMap = new HashMap<>();
		mClassTypeNameMap = new HashMap<>();
		mChildFieldsMap = new HashMap<>();
		mCacheCodeFieldsMap = new HashMap<>();
		mEditorConstructorMap = new HashMap<>();
		mRiValueFieldsMap = new HashMap<>();
	}

	private CanvasUserEnvironment() {
		sCreated = true;
	}

	// // // // // Static method.
	// // // // //
	static synchronized CanvasUserEnvironment create() {
		Precondition.checkState(!sCreated, "you can create a new CanvasUserEnvironment instance only once.");
		return new CanvasUserEnvironment();
	}

	// // // // // Method.
	// // // // //
	public synchronized void initialize(Context context) {
		Precondition.checkNotNull(context);
		Precondition.checkState(!mInitialized, "CanvasUserEnvironment already initialized!");

		mInitialized = true;

		try {
			/**
			 * 2017.03.17 Jaewoo
			 * kitkat 포함한 이전 버전에서 DexFile 만 가지고 원하는 package의 class를 로드 할 수
			 * 없기 때문에 kitkat 이전과 이후 버전의 가져오는 패턴을 나눈다.
 			 */
			ArrayList<Class<? extends AbstractCanvasUser>> classes = null;
			if(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT){
				DexFile dex = new DexFile(context.getPackageCodePath());
				classes = findAllRegionChild(dex);
				dex.close();
			}else {
				classes = MultiDexHelper.getAllClasses(context);
			}

			for (Class<? extends AbstractCanvasUser> type : classes) {
				L.d("prepare metadata " + type.getName() + " ...");
				String typeName = JsonUtils.toJsonString(type);

				mTypeNameClassMap.put(typeName, type);
				mClassTypeNameMap.put(type, typeName);
				registerEditorConstructor(type);
				registerChildField(type);
				registerCacheCodeField(type);
				registerRiValueField(type);
			}

			if (Version.current.isDev && DebugUtils.isDebuggable(context)) {
				SharedPreferences preferences = context.getSharedPreferences(CANVAS_PACKAGE_NAME, Context.MODE_PRIVATE);
				if (preferences.getInt(KEY_VALIDATED_REVISION_NUMBER, 0) < Version.current.revision) {
					CanvasUserValidator.validate(context, classes);
					preferences.edit().putInt(KEY_VALIDATED_REVISION_NUMBER, Version.current.revision).commit();
				}
			}
		} catch (Exception exception) {
			L.printStackTrace(exception);
			throw new MultimediaException(exception);
		}
	}

	public boolean isInitialized() {
		return mInitialized;
	}

	public String getCanvasUserTypeName(Class<? extends AbstractCanvasUser> type) {
		return mClassTypeNameMap.get(type);
	}

	public Class<? extends AbstractCanvasUser> getCanvasUserClass(String typeName) {
		return mTypeNameClassMap.get(typeName);
	}

	Constructor<?> getEditorConstructor(Class<? extends AbstractCanvasUser> type) {
		return mEditorConstructorMap.get(type);
	}

	List<Field> getChildFields(Class<? extends AbstractCanvasUser> type) {
		return mChildFieldsMap.get(type);
	}

	List<Field> getCacheCodeFields(Class<? extends AbstractCanvasUser> type) {
		return mCacheCodeFieldsMap.get(type);
	}

	List<Field> getRiValueFields(Class<?> type) {
		return mRiValueFieldsMap.get(type);
	}

	@Keep
	private ArrayList<Class<? extends AbstractCanvasUser>> findAllRegionChild(DexFile dex) {

		Enumeration<String> classNames = dex.entries();
		ArrayList<Class<? extends AbstractCanvasUser>> list = new ArrayList<>();
		try {
			while (classNames.hasMoreElements()) {

				String name = classNames.nextElement();
				if (!name.startsWith(CANVAS_PACKAGE_NAME))
					continue;

				Class<?> type = Class.forName(name);
				if (!AbstractCanvasUser.class.isAssignableFrom(type))
					continue;
				if (ReflectionUtils.isAbstractClass(type))
					continue;
				list.add((Class<? extends AbstractCanvasUser>) type);
			}
		} catch (ClassNotFoundException exception) {
			Precondition.assureUnreachable(exception.getMessage());
		}
		return list;
	}

	private void registerEditorConstructor(Class<? extends AbstractCanvasUser> type) {

		for (Class<?> declaredClass : type.getClasses()) {

			if (!Editor.class.isAssignableFrom(declaredClass)) {
				continue;
			}
			for (Constructor<?> constructor : declaredClass.getDeclaredConstructors()) {
				Class<?>[] parameters = constructor.getParameterTypes();
				if (parameters.length == 1 && parameters[0].equals(type)) {
					mEditorConstructorMap.put(type, constructor);
					return;
				}
			}
			Precondition.assureUnreachable();
		}
	}

	private void registerChildField(Class<? extends AbstractCanvasUser> type) {

		ArrayList<Field> fields = new ArrayList<>();
		for (Field field : ReflectionUtils.getDeclaredFieldWithInherited(type)) {
			if (field.isAnnotationPresent(Child.class)) {

				int modifiers = field.getModifiers();
				Precondition.checkState(!Modifier.isStatic(modifiers), "Static field cannot be annotated with @Child");

				Class<?> fieldClass = field.getType();
				if (RegionChild.class.isAssignableFrom(fieldClass)) {
					fields.add(field);
					continue;

				} else if (Collection.class.isAssignableFrom(fieldClass)) {
					if (RegionChild.class.isAssignableFrom(ReflectionUtils.getGenericType(field, 0))) {
						fields.add(field);
						continue;
					}

				} else if (Map.class.isAssignableFrom(fieldClass)) {
					Type genericType;
					switch (field.getAnnotation(Child.class).target()) {
						case KEY:
							genericType = ReflectionUtils.getGenericType(field, 0);
							break;
						case VALUE:
							genericType = ReflectionUtils.getGenericType(field, 1);
							break;
						case NON_USE:
						default:
							genericType = Precondition.assureUnreachable();
					}
					if (RegionChild.class.isAssignableFrom((Class<?>) genericType)) {
						fields.add(field);
						continue;
					}

				} else if (fieldClass.isArray()) {
					if (RegionChild.class.isAssignableFrom(fieldClass.getComponentType())) {
						fields.add(field);
						continue;
					}
				}
				Precondition.checkState(false, "@Child annotated field must only be a RegionChild.");
			}
		}
		mChildFieldsMap.put(type, Collections.unmodifiableList(fields));
	}

	private void registerCacheCodeField(Class<? extends AbstractCanvasUser> type) {

		ArrayList<Field> fields = new ArrayList<>();
		for (Field field : type.getDeclaredFields()) {
			if (field.isAnnotationPresent(CacheCode.class)) {

				int modifiers = field.getModifiers();
				Precondition.checkState(!Modifier.isStatic(modifiers), "Static field cannot be annotated with @CacheCode");

				Class<?> fieldClass = field.getType();
				if (isCacheCodeConvertible(fieldClass)) {
					Precondition.checkState(!Modifier.isFinal(modifiers), "Final field cannot be annotated with @CacheCode");
					fields.add(field);

				} else if (fieldClass.isArray() && isCacheCodeConvertible(fieldClass.getComponentType())) {
					fields.add(field);

				} else if (List.class.isAssignableFrom(fieldClass) && isCacheCodeConvertible(ReflectionUtils.getGenericType(field, 0))) {
					fields.add(field);

				} else {
					Precondition.checkState(false, "@CacheCode annotated field must implement ICacheCode.");
				}
			}
		}
		mCacheCodeFieldsMap.put(type, Collections.unmodifiableList(fields));
	}

	private boolean isCacheCodeConvertible(Class<?> type) {

		if (type.isPrimitive())
			return true;
		else if (type.equals(String.class))
			return true;
		else if (Number.class.isAssignableFrom(type))
			return true;
		else if (Enum.class.isAssignableFrom(type))
			return true;
		else if (ICacheCode.class.isAssignableFrom(type))
			return true;
		else if (ICanvasUser.class.isAssignableFrom(type))
			return true;
		else
			return false;
	}

	private void registerRiValueField(Class<?> type) {

		if (mRiValueFieldsMap.containsKey(type))
			return;

		ArrayList<Field> fields = new ArrayList<>();
		for (Field field : type.getDeclaredFields()) {
			if (field.isAnnotationPresent(RiValue.class)) {

				int modifiers = field.getModifiers();
				Precondition.checkState(!Modifier.isStatic(modifiers), "Static field cannot be annotated with @RiValue");
				Precondition.checkState(!Modifier.isFinal(modifiers), "Final field cannot be annotated with @RiValue");

				Class<?> fieldClass = field.getType();
				if (fieldClass.equals(float.class) || fieldClass.equals(Float.class)) {
					fields.add(field);

				} else if (fieldClass.equals(double.class) || fieldClass.equals(Double.class)) {
					fields.add(field);

				} else if (Object.class.isAssignableFrom(fieldClass) && field.getAnnotation(RiValue.class).container()) {
					fields.add(field);
					registerRiValueField(fieldClass);

				} else {
					Precondition.checkState(false, "@Child annotated field must be a float or double.");
				}
			}
		}
		mRiValueFieldsMap.put(type, Collections.unmodifiableList(fields));
	}
}
