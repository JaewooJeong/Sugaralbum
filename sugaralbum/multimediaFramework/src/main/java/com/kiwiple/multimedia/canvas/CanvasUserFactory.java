package com.kiwiple.multimedia.canvas;

import android.util.Log;

import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.canvas.AbstractCanvasUser.Editor;
import com.kiwiple.multimedia.json.JsonObject;

import org.json.JSONException;

import java.lang.reflect.Constructor;

/**
 * CanvasUserFactory.
 * 
 */
@SuppressWarnings("unchecked")
final class CanvasUserFactory {

	static <T extends Scene> T createScene(Class<T> sceneClass, VisualizerChild parent) {
		return (T) createRegionChild(sceneClass, parent);
	}

	static <T extends Effect> T createEffect(Class<T> effectClass, VisualizerChild parent) {
		return (T) createRegionChild(effectClass, parent);
	}

	static <T extends Transition> T createTransition(Class<T> transitionClass, VisualizerChild parent) {
		return (T) createRegionChild(transitionClass, parent);
	}

	static <T extends Scaler> T createScaler(Class<T> scalerClass, VisualizerChild parent) {
		return (T) createRegionChild(scalerClass, parent);
	}

	static Scene createScene(JsonObject jsonObject, VisualizerChild parent) throws JSONException {
		return (Scene) createRegionChild(jsonObject, parent);
	}

	static Effect createEffect(JsonObject jsonObject, VisualizerChild parent) throws JSONException {
		return (Effect) createRegionChild(jsonObject, parent);
	}

	static Transition createTransition(JsonObject jsonObject, VisualizerChild parent) throws JSONException {
		return (Transition) createRegionChild(jsonObject, parent);
	}

	static Scaler createScaler(JsonObject jsonObject, VisualizerChild parent) throws JSONException {
		return (Scaler) createRegionChild(jsonObject, parent);
	}

	static RegionChild createRegionChild(JsonObject jsonObject, VisualizerChild parent) throws JSONException {
		Precondition.checkNotNull(jsonObject, parent);

		String typeName = jsonObject.getString(RegionChild.JSON_NAME_TYPE);
		Class<?> type = ICanvasUser.environment.getCanvasUserClass(typeName);

		if (type == null) {
			throw new JSONException("Invalid RegionChild type name: " + typeName);
		}
		RegionChild regionChild = createRegionChild(type, parent);
		regionChild.injectJsonObject(jsonObject);
		return regionChild;
	}

	static RegionChild createRegionChild(Class<?> childClass, VisualizerChild parent) {
		Precondition.checkNotNull(childClass, parent);
		Precondition.checkArgument(RegionChild.class.isAssignableFrom(childClass), "childClass must be instance of RegionChild.");
		Precondition.checkArgument(!ReflectionUtils.isAbstractClass(childClass), "childClass must not be abstract.");

		Class<?> parentClass = parent.getClass();
		Constructor<?>[] constructors = childClass.getDeclaredConstructors();

		for (Constructor<?> constructor : constructors) {
			Class<?>[] parameterTypes = constructor.getParameterTypes();
			if (parameterTypes.length != 1) {
				continue;
			}
			if (parameterTypes[0].isAssignableFrom(parentClass)) {
				try {
					constructor.setAccessible(true);
					return (RegionChild) constructor.newInstance(parent);
				} catch (Exception exception) {
					exception.printStackTrace();
				} finally {
					constructor.setAccessible(false);
				}
			}
		}
		return Precondition.assureUnreachable();
	}

	static Editor<? extends AbstractCanvasUser, ? extends Editor<?, ?>> createEditor(AbstractCanvasUser instance) {
		Precondition.checkNotNull(instance);

		Class<? extends AbstractCanvasUser> type = instance.getClass();
		Constructor<?> constructor = ICanvasUser.environment.getEditorConstructor(type);
		if(constructor == null) {
			Log.e("CanvasUserFactory", "Error - Not found reflection class file.");
			return null;
		}
		try {
			constructor.setAccessible(true);
			return (Editor<?, ?>) constructor.newInstance(instance);
		} catch (Exception exception) {
			return Precondition.assureUnreachable(exception.getMessage());
		} finally {
			constructor.setAccessible(false);
		}
	}

	private CanvasUserFactory() {
		// do not instantiate.
	}
}
