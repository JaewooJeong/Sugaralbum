package com.kiwiple.multimedia.canvas;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;

import org.json.JSONException;

import android.util.Pair;

import com.kiwiple.debug.Precondition;
import com.kiwiple.debug.PreconditionException;
import com.kiwiple.multimedia.json.IJsonConvertible;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.Size;

/**
 * 주어진 환경에 따라 {@link PixelCanvas}에 그리기 기능을 수행하는 객체를 구현하기 위한 최상위 추상 클래스.
 * <p />
 * 기본적으로 {@code AbstractCanvasUser}는 주어진 공간에 시간 축의 특정 지점에 해당하는 이미지를 출력하는 기능을 가집니다. 즉,
 * {@code AbstractCanvasUser}의 구현체는 스스로 사용할 2차원의 공간 정보와 시간 정보를 가지고 있으며, 이를 명시하기 위해 다음의 추상 메서드를
 * 구현합니다.
 * 
 * <ul>
 * <li>{@link #getSize()}</li>
 * <li>{@link #getDuration()}</li>
 * <li>{@link #getPosition()}</li>
 * </ul>
 * 
 * 이밖에도, 적절한 그리기 기능을 제공하기 위해 구현체는 여러 정보를 가질 수 있습니다. 예컨대, 주어진 공간 전체를 단색으로 칠하는 구현체의 경우에는 색에 대한 정보를 필요로
 * 할 것이기 때문에 다음과 같은 형태를 생각해볼 수 있습니다.
 * 
 * <pre>
 * public final class SimplePaint extends AbstractCanvasUser {
 * 
 * 	private int color;
 * 
 * 	void setColor(int color) {
 * 		this.color = color;
 * 	} // Setter.
 * 
 * 	public int getColor() {
 * 		return color;
 * 	} // Getter.
 * 
 * 	...
 * }
 * </pre>
 * 
 * 주의해야 할 것은, 일반적으로 객체의 상태를 바꾸기 위해 제공되는 Setter가 출력되는 이미지를 변화시킬 것이라는 점입니다. {@code AbstractCanvasUser}
 * 는 시간 축을 진행함과 동시에 이미지를 연속적으로 출력하여 동영상을 연출해낼 수 있게끔 설계되었습니다. 만약 시간 축이 진행되고 있는 연속선상에서 객체의 상태가 변화할 경우,
 * 의도된 이미지의 흐름이 보장될 수 없을 뿐더러, 상태 변화에 따라 모종의 준비 동작이 필요한 구현체인 경우에는 미정의 동작에 빠질 위험이 매우 높습니다. 이렇듯, 객체가 잘못
 * 사용될 여지를 불식하기 위해 그리기 기능에 영향을 미치는 모든 메서드는 {@link Editor}를 통해서만 제공하도록 하고, Editor는 해당 객체가 편집 모드에 진입했을
 * 때에만 유효하도록 제한을 두는 방식을 사용합니다. 반대로 그리기 기능과 직접 관련이 있는 부분은 편집 모드일 때 접근할 수 없도록 구현체가 보장할 수 있어야 합니다.
 * <p />
 * 구현체가 현재 편집 모드인지 명시하기 위해 다음의 추상 메서드를 구현합니다.
 * 
 * <ul>
 * <li>{@link #isOnEditMode()}</li>
 * </ul>
 * 
 * ...
 * <p />
 * 
 * @see Editor
 */
@SuppressWarnings("unchecked")
abstract class AbstractCanvasUser implements ICanvasUser, IJsonConvertible {

	// // // // // Member variable.
	// // // // //
	private final Editor<? extends AbstractCanvasUser, ? extends Editor<?, ?>> mEditor;

	private final Changes mChanges;

	// // // // // Static method.
	// // // // //
	/**
	 * 시간의 흐름에 따라 선형적으로 변화하는 수치에 대해, 주어진 시간 비율에 해당하는 값을 반환합니다.
	 * 
	 * @param start
	 *            시작 수치.
	 * @param end
	 *            끝 수치.
	 * @param ratio
	 *            시간 비율. 시작점일 때 {@code 0.0f}, 끝점일 때에는 {@code 1.0f}를 사용하세요.
	 */
	static final int makeProgress(int start, int end, float ratio) {
		return start + Math.round((end - start) * ratio);
	}

	/**
	 * 시간의 흐름에 따라 선형적으로 변화하는 수치에 대해, 주어진 시간 비율에 해당하는 값을 반환합니다.
	 * 
	 * @param start
	 *            시작 수치.
	 * @param end
	 *            끝 수치.
	 * @param ratio
	 *            시간 비율. 시작점일 때 {@code 0.0f}, 끝점일 때에는 {@code 1.0f}를 사용하세요.
	 */
	static final float makeProgress(float start, float end, float ratio) {
		return start + (end - start) * ratio;
	}

	// // // // // Constructor.
	// // // // //
	AbstractCanvasUser() {
		Precondition.checkState(environment.isInitialized(), "you must invoke ICanvasUser.environment.initialize(Context)");

		mEditor = CanvasUserFactory.createEditor(this);
		mChanges = new Changes();
	}

	// // // // // Method.
	// // // // //
	/**
	 * {@link JsonObject}를 통해 객체를 구성합니다.
	 * 
	 * @param jsonObject
	 *            객체의 상태 정보를 담은 JsonObject 객체.
	 * @throws JSONException
	 *             org.json API 사용 중에 오류가 발생했을 때.
	 * @see IJsonConvertible#toJsonObject()
	 */
	abstract void injectJsonObject(JsonObject jsonObject) throws JSONException;

	@Override
	public JsonObject toJsonObject() throws JSONException {
		return null;
	}

	@Override
	public abstract Size getSize();

	@Override
	public abstract int getDuration();

	@Override
	public abstract int getPosition();

	/**
	 * 편집 기능 사용 중인지의 여부를 반환합니다.
	 * 
	 * @return 편집 모드일 때 true.
	 */
	public abstract boolean isOnEditMode();

	/**
	 * 객체를 편집하기 위한 Editor 객체를 반환합니다.
	 * 
	 * @see Editor
	 */
	public Editor<? extends AbstractCanvasUser, ? extends Editor<?, ?>> getEditor() {
		return mEditor;
	}

	@Override
	public final int getWidth() {
		return getSize().width;
	}

	@Override
	public final int getHeight() {
		return getSize().height;
	}

	@Override
	public float getProgressRatio() {
		return (float) getPosition() / getDuration();
	}

	/**
	 * 편집 기능을 통해 변경된 정보의 종류를 통지합니다. {@code AbstractCanvasUser}가 편집 모드일 때에만 유효합니다.
	 * 
	 * @param change
	 *            변경된 내용에 해당하는 {@link Change} 객체.
	 * @param details
	 *            변경된 내용의 상세 정보.
	 * @throws NullPointerException
	 *             {@code change}가 {@code null}일 때.
	 * @throws IllegalStateException
	 *             {@code AbstractCanvasUser}가 편집 모드가 아닐 때.
	 */
	@SafeVarargs
	final void notifyChange(Change change, Pair<? extends DetailChange<?>, ?>... details) {
		Precondition.checkNotNull(change);
		Precondition.checkArray(details).checkNotContainsNull();
		Precondition.checkState(isOnEditMode(), "You must call this method on edit mode.");

		for (Pair<? extends DetailChange<?>, ?> detail : details) {
			Precondition.checkNotNull(detail.first, detail.second);
			Precondition.checkArgument(change == detail.first.type, "Detail's type must equal to Change.");
			mChanges.detailMap.put(detail.first, detail.second);
		}
		mChanges.changeSet.add(change);
	}

	/**
	 * 모든 변경 내역을 지웁니다. {@code AbstractCanvasUser}가 편집 모드일 때에만 유효합니다.
	 * 
	 * @throws IllegalStateException
	 *             {@code AbstractCanvasUser}가 편집 모드가 아닐 때.
	 */
	final void clearChanges() {
		Precondition.checkState(isOnEditMode(), "You must call this method on edit mode.");

		mChanges.changeSet.clear();
		mChanges.detailMap.clear();
	}

	// // // // // Inner Class.
	// // // // //
	/**
	 * {@link AbstractCanvasUser}의 일부 기능을 조작하기 위한 최상위 추상 클래스.
	 * <p />
	 * {@code AbstractCanvasUser}의 그리기 기능에 영향을 미치는 모든 메서드는 반드시 {@code Editor} 클래스를 통해서만 제공되어야 합니다.
	 */
	public abstract static class Editor<C extends AbstractCanvasUser, E extends Editor<C, E>> {

		private final WeakReference<C> mWeakCanvasUserImpl;

		Editor(C abstractCanvasUser) {
			Precondition.checkNotNull(abstractCanvasUser);
			mWeakCanvasUserImpl = new WeakReference<C>(abstractCanvasUser);
		}

		/**
		 * 편집 대상 객체를 반환합니다.
		 */
		public C getObject() {
			return validate();
		}

		/**
		 * 편집 대상 객체가 유효한 상태인지 검사합니다.
		 * <p />
		 * 유효하지 않은 상태는 다음과 같이 정의합니다.
		 * <ul>
		 * <li>객체가 편집 모드가 아닐 때.</li>
		 * <li>Editor를 제외한 어디에서도 객체를 참조하고 있지 않을 때.</li>
		 * </ul>
		 * 
		 * @return 유효하다는 것이 보장된 편집 대상 객체.
		 * @throws PreconditionException
		 *             편집 대상 객체가 유효하지 않은 상태일 때.
		 */
		private C validate() {

			C canvasUser = mWeakCanvasUserImpl.get();

			Precondition.checkState(canvasUser != null, "canvasUser is expired.");
			Precondition.checkState(canvasUser.isOnEditMode(), "canvasUser is not on edit mode.");
			return canvasUser;
		}
	}

	static final class Changes implements Cloneable {

		private final HashSet<Change> changeSet;
		private final HashMap<DetailChange<?>, Object> detailMap;

		Changes() {
			changeSet = new HashSet<>();
			detailMap = new HashMap<>();
		}

		void update(AbstractCanvasUser source) {
			changeSet.addAll(source.mChanges.changeSet);
			detailMap.putAll(source.mChanges.detailMap);
		}

		<T> T getDetailValue(DetailChange<T> detail) {
			return (T) detailMap.get(detail);
		}

		boolean contains(Change type) {
			return changeSet.contains(type);
		}

		boolean contains(DetailChange<?> detail) {
			return detailMap.containsKey(detail);
		}

		@Override
		protected Changes clone() {

			Changes changes = new Changes();
			changes.changeSet.addAll(this.changeSet);
			changes.detailMap.putAll(this.detailMap);

			return changes;
		}
	}

	/**
	 * {@code AbstractCanvasUser}의 편집 기능을 통해 변경할 수 있는 정보의 유형을 구분하기 위한 클래스.
	 * 
	 * @see #RESOLUTION
	 * @see #SIZE
	 * @see #DURATION
	 */
	static final class Change {

		/**
		 * 그리기 영역의 해상도가 변경되었음을 의미합니다.<br />
		 * <br />
		 * 다음의 {@link DetailChange}가 함께 전달됩니다.
		 * <ul>
		 * <li>{@link DetailChange#MAGNIFICATION_CHANGE_RATIO}</li>
		 * </ul>
		 */
		static final Change RESOLUTION = new Change();

		/**
		 * 그리기 영역의 크기가 변경되었음을 의미합니다.
		 */
		static final Change SIZE = new Change();

		/**
		 * 시간 축의 전체 길이가 변경되었음을 의미합니다.
		 */
		static final Change DURATION = new Change();

		Change() {
			// Do nothing.
		}
	}

	static final class DetailChange<T> {

		static final DetailChange<Float> MAGNIFICATION_CHANGE_RATIO = new DetailChange<>(Change.RESOLUTION);

		final Change type;

		DetailChange(Change type) {
			Precondition.checkNotNull(type);
			this.type = type;
		}
	}
}