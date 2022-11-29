package com.kiwiple.multimedia.canvas;

import static com.kiwiple.multimedia.canvas.FocusState.OFF;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

import android.graphics.Bitmap;

import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.annotation.CacheCode;
import com.kiwiple.multimedia.annotation.Child;
import com.kiwiple.multimedia.exception.InvalidCanvasUserException;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.CollectionUtils;
import com.kiwiple.multimedia.util.DebugUtils;
import com.kiwiple.multimedia.util.Size;
import com.kiwiple.multimedia.util.StringUtils;

/**
 * {@link Region}의 제어를 받으며 작동하는 {@link AbstractCanvasUser}에 대한 추상 클래스.
 */
@SuppressWarnings("unchecked")
abstract class RegionChild extends VisualizerChild {

	// // // // // Static variable.
	// // // // //
	public static final String JSON_NAME_TYPE = "type";
	public static final String JSON_NAME_DURATION = "duration";

	static final Size[] DO_NOT_NEED_CANVAS = {};
	static final Change[] DO_NOT_HAVE_SENSITIVITIES = {};
	static final Change[] ALWAYS_SENSITIVE = {};

	private static final String CACHE_CODE_SEPARATOR = "_";
	private static final int CACHE_CODE_SEPARATOR_THRESHOLD = 32;

	// // // // // Member variable.
	// // // // //
	private final Class<? extends RegionChild> mClass;
	private final String mSimpleClassName;

	private final Region mParent;
	private final CanvasDispenser mCanvasDispenser;
	private final CacheManager mCacheManager;

	private final ArrayList<RegionChild> mLinkedChildren;

	private PixelCanvas[] mPixelCanvases;
	private String[] mCacheCodeChunks;

	private FocusState mFocusState;

	// // // // // Static method.
	// // // // //
	private static int getCacheCodeFrom(Object object) {

		if (object == null) {
			return 0;
		} else if (object instanceof ICanvasUser) {
			return ((RegionChild) object).getCacheCodeChunk(0).hashCode();
		} else if (object instanceof ICacheCode) {
			return ((ICacheCode) object).createCacheCode();
		} else {
			return object.hashCode();
		}
	}

	private static void scaleRiValues(Object object, float scale) {

		Class<?> type = object.getClass();

		for (Field field : environment.getRiValueFields(type)) {
			Object value = ReflectionUtils.getValue(object, field);

			if (value instanceof Float)
				ReflectionUtils.setValue(object, ((Float) value).floatValue() * scale, field);
			else if (value instanceof Double)
				ReflectionUtils.setValue(object, ((Double) value).doubleValue() * scale, field);
			else if (value instanceof Object)
				scaleRiValues(value, scale);
			else
				Precondition.assureUnreachable();
		}
	}

	// // // // // Constructor.
	// // // // //
	{
		mClass = getClass();
		mSimpleClassName = mClass.getSimpleName();

		mCacheManager = getCacheManager();
		mPixelCanvases = new PixelCanvas[0];
		mFocusState = OFF;

		mLinkedChildren = new ArrayList<>();
	}

	RegionChild(RegionChild parent) {
		this(parent.mParent);
	}

	RegionChild(Region parent) {
		super(parent);

		mParent = parent;
		mCanvasDispenser = parent.getCanvasDispenser();
	}

	// // // // // Method.
	// // // // //
	/**
	 * {@code RegionChild}의 구현체가 기능하기 위해 필요한 버퍼의 크기를 명시하기 위해 재정의하며, 다음과 같은 경우에 호출됩니다.
	 * <ul>
	 * <li>{@link Visualizer}의 편집 기능이 종료되었을 떄.</li>
	 * <li>{@code RegionChild}의 {@code FocusState}가 변경되었을 때.</li>
	 * </ul>
	 * 버퍼는 필요한 개수를 각각 필요한 크기만큼 명시할 수 있으며, 실제 버퍼는 {@link #getCanvas(int)}로써 반환된 {@link PixelCanvas}의
	 * 형태로 참조할 수 있습니다. {@code Region}이 모든 {@code RegionChild}가 요청한 버퍼를 필요한 때에 할당받을 수 있으면서도 최대한 적은 양의
	 * 메모리를 사용하기 위한 작업을 수행하기는 하지만, 그럼에도 불구하고 버퍼의 크기는 가능한 한 최소한으로 명시해야 합니다.<br />
	 * <br />
	 * 재정의할 때에는 메서드 내부에서 너무 많은 작업을 하지 않도록 하는 것이 좋습니다. 구현체의 상태에 따라 버퍼의 크기가 달라질 필요가 있는 경우, 버퍼의 크기를
	 * 산정하기 위한 작업은 {@link #onValidate(Changes)}에서 수행하는 것을 권장합니다. {@code Visualizer}의 편집 기능을
	 * 사용한 후에, {@code onValidate(Changes)}는 항상 {@code getCanvasRequirement()} 이전에 호출된다는 것이 보장됩니다.
	 * <br />
	 * <br />
	 * 재정의하지 않았을 때 반환되는 기본 값은 {@link #DO_NOT_NEED_CANVAS}입니다.<br />
	 * <br />
	 * <b>주의</b>: 버퍼가 필요하지 않은 경우에는 {@link #DO_NOT_NEED_CANVAS}를 반환하도록 합니다. {@code null}을 반환해서는 안
	 * 됩니다.
	 */
	Size[] getCanvasRequirement() {
		return DO_NOT_NEED_CANVAS;
	}

	final Size[] getCanvasRequirementWithChild() {

		if (!isValidated())
			return DO_NOT_NEED_CANVAS;

		ArrayList<Size> sizeList = new ArrayList<>();

		for (RegionChild child : mLinkedChildren)
			Collections.addAll(sizeList, child.getCanvasRequirementWithChild());
		Collections.addAll(sizeList, getCanvasRequirement());

		return sizeList.toArray(new Size[0]);
	}

	/**
	 * {@link #getCanvasRequirement()}에서 명시한 버퍼를 참조하기 위해 사용합니다.
	 * <p />
	 * 주어진 {@code index}에 해당하는 버퍼를 {@link PixelCanvas}의 형태로 반환하며, 이때 사용하는 첨자 값은
	 * {@code getCanvasRequirement()}에서 반환하는 배열의 첨자와 쌍을 이룹니다. 한 가지 예를 들어 다음과 같이
	 * {@code getCanvasRequirement()}를 구현했을 때:
	 * 
	 * <pre>
	 * Size[] mCanvasRequirement = { Resolution.NHD.getSize(), Resolution.FHD.getSize() };
	 * 
	 * Size[] getCanvasRequirement() {
	 * 	return mCanvasRequirement;
	 * }
	 * </pre>
	 * 
	 * {@code getCanvas(1)} 호출 시에는 {@code Resolution.FHD.getSize()} 크기에 적합한 버퍼가 반환되는 것입니다. 반환된 버퍼는
	 * 요청한 크기보다 클 수도 있지만 더 작지는 않다는 것이 보장되며, 버퍼의 논리적 크기는 요청한 크기로 미리 설정되어 있습니다.
	 * <p />
	 * 본 메서드를 통해 반환되는 {@link PixelCanvas}는 {@link FocusState}가 {@code OFF}가 아닌 상태로 변경되었을 때 자동적으로
	 * 할당되며, 반대로 {@code FocusState}가 {@code OFF}인 상태로 변경되었을 때에는 버퍼가 일괄적으로 수거됩니다. 요컨대, 본 메서드는 버퍼가 할당된
	 * 상태에서 사용했을 때에만 유효하며, 그렇지 않은 상태에서는 {@code null}이 반환됩니다. 모든 버퍼는 {@code RegionChild}가 필요할 때마다 서로
	 * 교환해가며 사용하는 공용 자원이기 때문에, 버퍼에 대한 참조 변수는 반드시 지역적으로만 선언해서 사용할 수 있도록 주의해야 합니다.
	 * <p />
	 * 다음 지점에서는 항상 유효한 버퍼 객체가 반환된다는 것이 보장되므로, 버퍼의 사용은 해당 지점에서만 사용할 것을 권장합니다.
	 * <ul>
	 * <li>{@code draw(PixelCanvas, ...)}</li>
	 * <li>{@link #onPrepare()}</li>
	 * </ul>
	 * 
	 * @param index
	 *            반환할 버퍼에 해당하는 첨자.
	 * @return {@code PixelCanvas} 객체 혹은 유효하지 않은 시점에 호출한 경우에 {@code null}.
	 */
	final PixelCanvas getCanvas(int index) {
		return mPixelCanvases[index];
	}

	final void optimizeCanvas() {

		if (!isValidated())
			return;

		Size[] sizes = getCanvasRequirement();
		for (int i = 0; i != sizes.length; ++i)
			if (mPixelCanvases[i].getCapacity() > sizes[i].product())
				mPixelCanvases[i] = mCanvasDispenser.optimizeCanvas(mPixelCanvases[i], sizes[i]);
		for (RegionChild child : mLinkedChildren)
			child.optimizeCanvas();
	}

	/**
	 * {@code RegionChild}의 구현체가 그리기 기능을 수행하기 전에 선행되어야 하는 작업이 있을 때 재정의합니다. {@code onDraw()}는 짧은 주기를
	 * 가지고 연쇄적으로 호출되는 메서드이기 때문에, 쓰잘머리 없이 반복되는 작업은 최대한 {@code onPrepare()}에 위임하는 것이 좋습니다.
	 * <p />
	 * 본 메서드는 {@link FocusState}가 {@code ON} 혹은 {@code PRELIMINARY}로 변경되었을 때 자동적으로 호출됩니다.
	 */
	void onPrepare() {
		// Do nothing.
	}

	/**
	 * {@code RegionChild}의 구현체가 그리기 기능을 수행한 후에 후행되어야 하는 작업이 있을 때 재정의합니다.<br />
	 * <br />
	 * 본 메서드는 {@link FocusState}가 {@code OFF}로 변경되었을 때 자동적으로 호출됩니다.
	 */
	void onUnprepare() {
		// Do nothing.
	}

	/**
	 * {@link Visualizer}의 편집 기능이 종료된 후, 편집된 {@code VisualizerChild}가 정상적으로 작동할 수 있는 상태인지 점검하기 위해
	 * 호출됩니다. 즉, API 사용자가 편집 기능을 적절하게 사용했는지 확인하는 데에 주안점을 두고 있습니다. 모든 {@code VisualizerChild}의 구현체는
	 * 이를 재정의하여 사용자가 조작 가능한 변수를 검사하고, 조작된 내용에 따라 추가적으로 선행되어야 하는 작업을 수행해야 합니다.<br />
	 * <br />
	 * {@code Editor}를 통해 편집된 내용이 있거나, {@link #getSensitivities()}를 통해 명시한 민감성에 해당하는 변화가 부모 객체에 있거나,
	 * 객체가 새롭게 생성되어 추가된 경우에 한하여 자동적으로 호출되며, 그 외의 경우에는 호출되지 않습니다.
	 * 
	 * @param changes
	 *            변경 내역의 상세 정보.
	 * @throws InvalidCanvasUserException
	 *             정상적으로 작동할 수 없는 상태라고 판정되었을 때.
	 * @see #checkValidity(boolean, String)
	 */
	void onValidate(Changes changes) throws InvalidCanvasUserException {
		// Do nothing.
	}

	/**
	 * 객체가 더 이상 사용되지 않는다는 것이 보장될 때 호출됩니다. {@code RegionChild}의 구현체는 이를 재정의하여 명시적으로 자원을 해제할 수 있습니다.
	 */
	void onRelease() {
		// Do nothing.
	}

	/**
	 * {@code RegionChild}는 편집 기능이 종료된 후, 어느 정도 자원이 필요한 후속 작업은 {@link #onValidate(Changes)}
	 * 에서 일괄적으로 처리하도록 설계되어 있습니다. {@code onValidate()}는 기본적으로 {@code RegionChild} 자체가 편집되어야만 호출되는데,
	 * 구현체의 종류에 따라서는 직접 소유하는 정보 외에도 전반적인 환경 즉, 부모 객체의 변화에 대해서도 민감하게 대응해야 할 필요성이 있을 수 있습니다. 이를 위해 구현체
	 * 외부의 특정 환경이 조작된 경우에도 {@code onValidate()}가 호출될 수 있도록, 본 메서드를 재정의하여 외부 변화에 대한 구현체의 민감성을 명시할 수
	 * 있습니다.<br />
	 * <br />
	 * 예컨대, 모든 {@code RegionChild}의 크기 정보는 부모 객체에 종속되어 있고, 구현체로서는 이를 제어할 수 있는 방법이 없기 때문에, 실제로 크기가
	 * 변경되어도 이에 대한 대응을 하기 위한 시점이 마땅치 않습니다. 이때 본 메서드가 {@link Change#SIZE}가 포함된 {@link Change}의 배열을
	 * 반환하도록 재정의한다면, 크기가 변경되었을 때 {@code onValidate()}가 호출되므로 적절한 대응을 할 수 있게 되는 것입니다.<br />
	 * <br />
	 * 재정의하지 않았을 때 반환되는 기본 값은 {@link #DO_NOT_HAVE_SENSITIVITIES}입니다.<br />
	 * <br />
	 * 매우 특수한 경우로, {@link #ALWAYS_SENSITIVE}를 반환하도록 재정의한다면 내부 및 외부의 변경 사항에 관계 없이 무조건적인
	 * {@code onValidate()} 호출을 보장할 수 있습니다.
	 */
	Change[] getSensitivities() {
		return DO_NOT_HAVE_SENSITIVITIES;
	}

	private final boolean isDisturbingSensitivities(Changes changes) {

		Change[] sensitivities = getSensitivities();
		if (sensitivities == ALWAYS_SENSITIVE)
			return true;
		for (Change sensitivity : sensitivities)
			if (changes.contains(sensitivity))
				return true;
		return false;
	}

	@Override
	final void validate(Changes changes) {

		changes.update(this);

		if (changes.contains(Change.RESOLUTION))
			scaleRiValues(this, changes.getDetailValue(DetailChange.MAGNIFICATION_CHANGE_RATIO));

		if (!isValidated() || isDisturbingSensitivities(changes)) {
			try {
				checkValidity(getDuration() != 0, "You must invoke setDuration()");
				onValidate(changes.clone());
				setValidated(true);

				int canvasRequirementLength = getCanvasRequirement().length;
				if (mPixelCanvases.length != canvasRequirementLength)
					mPixelCanvases = new PixelCanvas[canvasRequirementLength];
				if (isOnPreviewMode() && isCacheable()) {
					initializeChunksArray();
					for (int i = 0; i != mCacheCodeChunks.length; ++i)
						mCacheCodeChunks[i] = createCacheCodeChunk(i);
				}
			} catch (InvalidCanvasUserException exception) {
				setValidated(false);
			}
			organizeChildList(mLinkedChildren, false, false);
		}

		for (RegionChild child : mLinkedChildren)
			child.validate(changes.clone());
		clearChanges();
	}

	@Override
	final void release() {

		for (RegionChild child : mLinkedChildren)
			child.release();
		onRelease();
	}

	@Override
	public Editor<? extends RegionChild, ? extends Editor<?, ?>> getEditor() {
		return (Editor<? extends RegionChild, ? extends Editor<?, ?>>) super.getEditor();
	}

	@Override
	public JsonObject toJsonObject() throws JSONException {
		return super.toJsonObject().put(JSON_NAME_TYPE, environment.getCanvasUserTypeName(mClass));
	}

	@Override
	public Size getSize() {
		return mParent.getSize();
	}

	private void takeCanvas() {

		Size[] sizes = getCanvasRequirement();
		for (int i = 0; i != sizes.length; ++i) {
			mPixelCanvases[i] = mCanvasDispenser.getCanvas(sizes[i]);
		}
	}

	private void releaseCanvas() {

		for (int i = 0; i != mPixelCanvases.length; ++i) {
			if (mPixelCanvases[i] != null) {
				mCanvasDispenser.returnCanvas(mPixelCanvases[i]);
				mPixelCanvases[i] = null;
			}
		}
	}

	final void setFocusState(FocusState focusState) {

		if (isValidated()) {
			if (mFocusState.equals(OFF) && !focusState.equals(OFF)) {
				takeCanvas();
				prepareCanvas();
				onPrepare();
			} else if (!mFocusState.equals(OFF) && focusState.equals(OFF)) {
				releaseCanvas();
				onUnprepare();
			}
		}
		for (RegionChild child : mLinkedChildren)
			child.setFocusState(focusState);
		mFocusState = focusState;
	}

	final FocusState getFocusState() {
		return mFocusState;
	}

	int getCacheCount() {
		return 0;
	}

	public final boolean isInstanceOf(Class<?>... classes) {
		Precondition.checkArray(classes).checkNotContainsNull();

		for (Class<?> type : classes)
			if (type.isAssignableFrom(mClass))
				return true;
		return false;
	}

	public final boolean isCacheable() {
		return getCacheCount() > 0;
	}

	Bitmap createCacheAsBitmap(int index) throws IOException {
		return null;
	}

	private void prepareCanvas() {

		if (!isCacheable())
			return;

		try {
			if (isOnPreviewMode()) {
				prepareCacheFile();
				prepareCanvasWithCache();
			} else {
				prepareCanvasWithoutCache();
			}
		} catch (IOException exception) {
			exception.printStackTrace();
			setValidated(false);
		}
	}

	void prepareCanvasWithCache() throws IOException {
		// Do nothing.
	}

	void prepareCanvasWithoutCache() throws IOException {
		// Do nothing.
	}

	final boolean isCacheFilePrepared() {

		for (int i = 0, repeat = getCacheCount(); i != repeat; ++i) {
			String cacheFileName = getCacheCodeChunk(i);
			if (!mCacheManager.isImageCacheExist(cacheFileName)) {
				return false;
			}
		}
		return true;
	}

	final void prepareCacheFile() throws IOException {

		for (int i = 0, repeat = getCacheCount(); i != repeat; ++i) {
			String cacheFileName = getCacheCodeChunk(i);

			if (!mCacheManager.isImageCacheExist(cacheFileName)) {
				mCacheManager.createImageCache(cacheFileName, createCacheAsBitmap(i), true);
			}
		}
	}

	private void initializeChunksArray() {

		int chunksSize = Math.max(1, getCacheCount());
		if (mCacheCodeChunks == null || mCacheCodeChunks.length != chunksSize) {
			mCacheCodeChunks = new String[chunksSize];
		} else {
			Arrays.fill(mCacheCodeChunks, null);
		}
	}

	final ArrayList<RegionChild> createChildList(boolean addThis, boolean recursively) {

		ArrayList<RegionChild> list = new ArrayList<>();

		if (isValidated() && !recursively) {
			if (addThis)
				list.add(this);
			list.addAll(mLinkedChildren);
		} else {
			organizeChildList(list, addThis, recursively);
		}
		return list;
	}

	private void organizeChildList(ArrayList<RegionChild> dstList, boolean addThis, boolean recursively) {

		dstList.clear();
		for (Field field : environment.getChildFields(mClass)) {

			Class<?> fieldClass = field.getType();
			if (RegionChild.class.isAssignableFrom(fieldClass)) {
				RegionChild regionChild = (RegionChild) ReflectionUtils.getValue(this, field);
				dstList.add(regionChild);

			} else if (Collection.class.isAssignableFrom(fieldClass)) {
				Collection<RegionChild> childs = (Collection<RegionChild>) ReflectionUtils.getValue(this, field);
				dstList.addAll(childs);

			} else if (Map.class.isAssignableFrom(fieldClass)) {

				Map<?, ?> childs = (Map<?, ?>) ReflectionUtils.getValue(this, field);

				Iterator<RegionChild> iterator;
				switch (field.getAnnotation(Child.class).target()) {
					case KEY:
						iterator = (Iterator<RegionChild>) childs.keySet().iterator();
						break;
					case VALUE:
						iterator = (Iterator<RegionChild>) childs.values().iterator();
						break;
					default:
						iterator = Precondition.assureUnreachable();
				}
				while (iterator.hasNext())
					dstList.add(iterator.next());

			} else if (fieldClass.isArray()) {
				Object array = ReflectionUtils.getValue(this, field);
				for (int i = 0, length = Array.getLength(array); i != length; ++i)
					dstList.add((RegionChild) Array.get(array, i));

			} else {
				Precondition.assureUnreachable();
			}
		}
		CollectionUtils.removeAllNull(dstList);

		if (recursively) {
			ArrayList<RegionChild> childrenForRecursion = new ArrayList<>(dstList);

			while (!childrenForRecursion.isEmpty()) {
				ArrayList<RegionChild> children = new ArrayList<>();
				childrenForRecursion.remove(0).organizeChildList(children, false, false);

				CollectionUtils.removeAllNull(children);
				dstList.addAll(children);
				childrenForRecursion.addAll(children);
			}
		}
		if (addThis) {
			dstList.add(this);
		}
	}

	String getCacheCodeChunk(int index) {

		if (mCacheCodeChunks == null) {
			initializeChunksArray();
			return mCacheCodeChunks[index] = createCacheCodeChunk(index);

		} else if (mCacheCodeChunks[index] == null) {
			return mCacheCodeChunks[index] = createCacheCodeChunk(index);

		} else {
			return mCacheCodeChunks[index];
		}
	}

	private String createCacheCodeChunk(int index) {

		StringBuilder cacheCode = new StringBuilder();
		StringBuilder separator = new StringBuilder();

		cacheCode.append(mSimpleClassName);
		cacheCode.append(CACHE_CODE_SEPARATOR);

		Resolution resolution = getResolution();
		cacheCode.append(resolution.width);
		cacheCode.append("x");
		cacheCode.append(resolution.height);

		for (Field field : environment.getCacheCodeFields(mClass)) {

			Class<?> fieldClass = field.getType();
			if (ICanvasUser.class.isAssignableFrom(fieldClass)) {
				RegionChild regionChild = (RegionChild) ReflectionUtils.getValue(this, field);
				cacheCode.append(CACHE_CODE_SEPARATOR);
				cacheCode.append(regionChild.getCacheCodeChunk(index));

			} else if (ICacheCode.class.isAssignableFrom(fieldClass)) {
				cacheCode.append(CACHE_CODE_SEPARATOR);
				cacheCode.append(getCacheCodeFrom(field, 0));

			} else if (List.class.isAssignableFrom(fieldClass)) {
				cacheCode.append(CACHE_CODE_SEPARATOR);
				cacheCode.append(getCacheCodeFrom(field, index));

			} else if (fieldClass.isArray()) {
				cacheCode.append(CACHE_CODE_SEPARATOR);
				cacheCode.append(getCacheCodeFrom(field, index));

			} else {
				separator.append(ReflectionUtils.getValue(this, field));
			}
		}

		int separatorLength;
		while ((separatorLength = separator.length()) > 0) {
			int subStringLength = Math.min(CACHE_CODE_SEPARATOR_THRESHOLD, separatorLength);

			cacheCode.append(CACHE_CODE_SEPARATOR);
			cacheCode.append(separator.substring(0, subStringLength).hashCode());
			separator.delete(0, subStringLength);
		}
		return cacheCode.toString();
	}

	private int getCacheCodeFrom(Field field, int index) {

		Class<?> fieldClass = field.getType();
		Object value = ReflectionUtils.getValue(this, field);

		if (value == null) {
			return 0;
		} else if (fieldClass.isPrimitive() || fieldClass.equals(String.class) || ICacheCode.class.isAssignableFrom(fieldClass)) {
			return getCacheCodeFrom(value);

		} else if (fieldClass.isArray()) {
			if (field.getAnnotation(CacheCode.class).indexed()) {
				return getCacheCodeFrom(Array.get(value, index));
			} else {
				int cacheCode = 0;
				for (int i = 0, repeat = Array.getLength(value); i != repeat; ++i)
					cacheCode += getCacheCodeFrom(Array.get(value, i));
				return cacheCode;
			}
		} else if (List.class.isAssignableFrom(fieldClass)) {

			List<Object> list = (List<Object>) value;
			if (field.getAnnotation(CacheCode.class).indexed()) {
				return getCacheCodeFrom(list.get(index));
			} else {
				int cacheCode = 0;
				for (int i = 0, repeat = list.size(); i != repeat; ++i)
					cacheCode += getCacheCodeFrom(list.get(i));
				return cacheCode;
			}
		}
		return Precondition.assureUnreachable();
	}

	final String createCanvasRequirementLog(int depth) {

		Size[] rootCanvasRequirements = getCanvasRequirement();
		Size[] rootCanvasRequirementsWithChild = getCanvasRequirementWithChild();

		StringBuilder builder = new StringBuilder(128).append(mSimpleClassName);
		boolean useMultiPrintFormat = rootCanvasRequirementsWithChild.length > 1;

		if (useMultiPrintFormat) {
			float amountMB = 0.0f;
			for (Size size : rootCanvasRequirementsWithChild)
				amountMB += PixelCanvas.measureMegabytes(size.product());
			builder.append(StringUtils.format(" (amount: %.2fMB)", amountMB));
		}
		builder.append(useMultiPrintFormat ? " {\n" : rootCanvasRequirements.length == 0 ? " { null" : " { ");
		builder.append(DebugUtils.createLog(rootCanvasRequirements, useMultiPrintFormat ? depth + 1 : 0));

		int number = rootCanvasRequirements.length + 1;
		String indentation = StringUtils.repeat('\t', depth + 1);
		for (RegionChild child : mLinkedChildren) {
			String log = child.createCanvasRequirementLog(depth + 1);
			if (!log.contains("null"))
				builder.append('\n').append(indentation).append(number++).append(". ").append(log);
		}
		builder.append(useMultiPrintFormat ? "\n" + StringUtils.repeat('\t', depth) : " ").append('}');
		return builder.toString();
	}

	// // // // // Inner Class.
	// // // // //
	static abstract class Editor<C extends RegionChild, E extends Editor<C, E>> extends VisualizerChild.Editor<C, E> {

		Editor(C regionChild) {
			super(regionChild);
		}

		public final E injectPreset(IPreset<? extends VisualizerChild.Editor<C, E>> preset) {
			getObject().injectPreset(preset);
			return (E) this;
		}
	}
}
