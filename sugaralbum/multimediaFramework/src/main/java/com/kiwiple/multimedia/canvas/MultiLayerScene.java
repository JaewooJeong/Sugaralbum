package com.kiwiple.multimedia.canvas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;

import android.graphics.Rect;

import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.annotation.CacheCode;
import com.kiwiple.multimedia.annotation.Child;
import com.kiwiple.multimedia.canvas.data.Viewport;
import com.kiwiple.multimedia.exception.InvalidCanvasUserException;
import com.kiwiple.multimedia.json.JsonArray;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.Size;

/**
 * 주어진 공간를 분할하고, 각 공간에 대한 이미지 출력을 {@link Scene} 객체에 위임하여 관리하는 클래스.
 */
public final class MultiLayerScene extends Scene {

	// // // // // Static variable.
	// // // // //
	public static final String JSON_VALUE_TYPE = "multi_layer_scene";

	public static final String JSON_NAME_LAYERS = "layers";
	public static final String JSON_NAME_LAYER = "layer";
	public static final String JSON_NAME_LAYER_VIEWPORT = "layer_viewport";
	public static final String JSON_NAME_TEMPLATE_ID = "template_id";

	// // // // // Member variable.
	// // // // //
	@CacheCode
	private final ArrayList<Layer> mLayers;
	@Child(target = Child.Target.KEY)
	private final HashMap<Scene, Layer> mSceneLayerMap;

	private int mTemplateId;

	// // // // // Constructor.
	// // // // //
	{
		mLayers = new ArrayList<>();
		mSceneLayerMap = new HashMap<>();
	}

	MultiLayerScene(Region parent) {
		super(parent);
	}

	MultiLayerScene(MultiLayerScene parent) {
		super(parent);
		throw new UnsupportedOperationException();
	}

	// // // // // Method.
	// // // // //
	@Override
	void onDraw(PixelCanvas dstCanvas) {

		PixelCanvas canvas = getCanvas(0);

		for (Layer layer : mLayers) {
			Rect absViewport = layer.absViewport;
			int width = absViewport.width();
			int height = absViewport.height();

			canvas.setImageSize(width, height);
			layer.scene.draw(canvas, true);
			canvas.copy(dstCanvas, 0, 0, absViewport.left, absViewport.top, width, height);
		}
	}

	@Override
	public Editor getEditor() {
		return (Editor) super.getEditor();
	}

	@Override
	void onPrepare() {
		// Do nothing.
	}

	@Override
	public JsonObject toJsonObject() throws JSONException {

		JsonObject jsonObject = super.toJsonObject();

		JsonArray layerJsonArray = new JsonArray();
		for (Layer layer : mLayers) {
			JsonObject layerJsonObject = new JsonObject();
			layerJsonObject.put(JSON_NAME_LAYER, layer.scene);
			layerJsonObject.put(JSON_NAME_LAYER_VIEWPORT, layer.viewport);
			layerJsonArray.put(layerJsonObject);
		}
		jsonObject.putOpt(JSON_NAME_LAYERS, layerJsonArray);
		jsonObject.putOpt(JSON_NAME_TEMPLATE_ID, mTemplateId, 0);

		return jsonObject;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);

		setTemplateId(jsonObject.optInt(JSON_NAME_TEMPLATE_ID));

		for (JsonObject layerJsonObject : jsonObject.optJSONArrayAsList(JSON_NAME_LAYERS, JsonObject.class)) {

			Scene layer = CanvasUserFactory.createScene(layerJsonObject.getJSONObject(JSON_NAME_LAYER), this);
			Viewport viewport = new Viewport(layerJsonObject.getJSONObject(JSON_NAME_LAYER_VIEWPORT));
			addLayer(layer, viewport);
		}
	}

	@Override
	Change[] getSensitivities() {
		return new Change[] { Change.SIZE };
	}

	@Override
	void onValidate(Changes changes) throws InvalidCanvasUserException {

		checkValidity(!mLayers.isEmpty(), "You must ensure that MultiLayerScene has at least one Layer.");

		Size size = getSize();
		for (Layer layer : mLayers) {
			layer.absViewport = layer.viewport.asActualSizeRect(size);
		}
	}

	@Override
	Size[] getCanvasRequirement() {

		Size maxSize = Size.INVALID_SIZE;
		for (Layer layer : mLayers) {
			Size size = layer.scene.getSize();
			if (size.product() > maxSize.product()) {
				maxSize = size;
			}
		}
		return (maxSize == Size.INVALID_SIZE ? DO_NOT_NEED_CANVAS : new Size[] { maxSize });
	}

	Size getLayerSize(int index) {
		return new Size(mLayers.get(index).absViewport);
	}

	Size getLayerSize(Scene scene) {
		return new Size(mSceneLayerMap.get(scene).absViewport);
	}

	Rect getLayerRect(int index) {
		return mLayers.get(index).absViewport;
	}

	Rect getLayerRect(Scene scene) {
		return mSceneLayerMap.get(scene).absViewport;
	}

	void addLayer(Scene scene, Viewport viewport) {
		addLayer(scene, viewport, mLayers.size());
	}

	void addLayer(Scene scene, Viewport viewport, int index) {
		Precondition.checkNotNull(scene, viewport);

		Layer layer = new Layer(scene, viewport);
		mLayers.add(index, layer);
		mSceneLayerMap.put(scene, layer);
	}

	void removeLayer(int index) {

		Scene scene = mLayers.remove(index).scene;
		scene.release();

		mSceneLayerMap.remove(scene);
	}

	void removeAllLayer() {

		for (Layer layer : mLayers) {
			layer.scene.release();
		}
		mLayers.clear();
		mSceneLayerMap.clear();
	}

	void replaceLayer(Scene scene, Viewport viewport, int index) {
		removeLayer(index);
		addLayer(scene, viewport, index);
	}

	void replaceLayerViewport(Viewport viewport, int index) {
		Precondition.checkNotNull(viewport);

		addLayer(mLayers.remove(index).scene, viewport, index);
		notifyChange(Change.SIZE);
	}

	/**
	 * 사용자 임의 MultiLayerScene 식별자를 등록합니다.
	 * <p />
	 * 본 메서드는 {@code MultiLayerScene}의 기능에 아무런 영향을 미치지 않습니다.
	 * 
	 * @param templateId
	 *            등록할 MultiLayerScene 식별자.
	 */
	public void setTemplateId(int templateId) {
		mTemplateId = templateId;
	}

	/**
	 * 사용자 임의 MultiLayerScene 식별자를 반환합니다.
	 */
	public int getTemplateId() {
		return mTemplateId;
	}

	/**
	 * 객체를 구성하는 {@link Scene} 목록에서 지정된 첨자에 해당하는 {@code Scene} 객체를 반환합니다.
	 * 
	 * @param index
	 *            지정할 첨자.
	 */
	public Scene getLayer(int index) {
		return mLayers.get(index).scene;
	}

	/**
	 * 객체를 구성하는 {@link Scene} 목록의 사본을 반환합니다.
	 */
	public List<Scene> getLayers() {

		ArrayList<Scene> list = new ArrayList<>();
		for (Layer layer : mLayers) {
			list.add(layer.scene);
		}
		return list;
	}

	public Viewport getLayerViewport(int index) {
		return mLayers.get(index).viewport;
	}

	public Viewport getLayerViewport(Scene layer) {
		return mSceneLayerMap.get(layer).viewport;
	}

	public List<Viewport> getLayerViewports() {

		ArrayList<Viewport> list = new ArrayList<>();
		for (Layer layer : mLayers) {
			list.add(layer.viewport);
		}
		return list;
	}

	// // // // // Inner class.
	// // // // //
	private static final class Layer implements ICacheCode {

		public final Scene scene;
		public final Viewport viewport;
		Rect absViewport;

		Layer(Scene scene, Viewport viewport) {
			Precondition.checkNotNull(scene, viewport);

			this.scene = scene;
			this.viewport = viewport;
		}

		@Override
		public int createCacheCode() {
			return scene.getCacheCodeChunk(0).hashCode();
		}
	}

	/**
	 * {@link MultiLayerScene} 객체의 일부 기능을 조작하기 위한 클래스. Visualizer가 편집 모드일 때에만 사용할 수 있습니다.
	 * 
	 * @see Visualizer.Editor
	 */
	public static final class Editor extends Scene.Editor<MultiLayerScene, Editor> {

		private Editor(MultiLayerScene multiLayerScene) {
			super(multiLayerScene);
		}

		/**
		 * {@code MultiLayerScene}을 구성하는 {@link Scene} 목록에 새로운 {@code Scene}를 생성하여 추가합니다.
		 * 
		 * @return 추가된 {@code Scene} 객체.
		 */
		public <T extends Scene> T addLayer(Class<T> sceneClass, Viewport viewport) {

			MultiLayerScene scene = getObject();
			T layer = CanvasUserFactory.createScene(sceneClass, scene);
			getObject().addLayer(layer, viewport);

			return layer;
		}

		/**
		 * {@code MultiLayerScene}을 구성하는 {@link Scene} 목록의 지정된 위치에 새로운 {@code Scene}를 생성하여 추가합니다.
		 * 
		 * @param index
		 *            추가할 위치를 지정하기 위한 첨자.
		 * @return 추가된 {@code Scene} 객체.
		 */
		public <T extends Scene> T addLayer(Class<T> sceneClass, Viewport viewport, int index) {

			MultiLayerScene scene = getObject();
			T layer = CanvasUserFactory.createScene(sceneClass, scene);
			scene.addLayer(layer, viewport, index);

			return layer;
		}

		/**
		 * {@code MultiLayerScene}을 구성하는 {@link Scene} 목록의 모든 객체를 제거합니다.
		 */
		public Editor removeAllLayer() {
			getObject().removeAllLayer();
			return this;
		}

		/**
		 * {@code MultiLayerScene}을 구성하는 {@link Scene} 목록의 지정된 위치에 해당하는 {@code Scene}를 제거합니다.
		 * 
		 * @param index
		 *            제거할 {@code Scene}에 해당하는 첨자.
		 */
		public Editor removeLayer(int index) {
			getObject().removeLayer(index);
			return this;
		}

		public <T extends Scene> T replaceLayer(Class<T> sceneClass, Viewport viewport, int index) {

			MultiLayerScene scene = getObject();
			T layer = CanvasUserFactory.createScene(sceneClass, scene);
			scene.replaceLayer(layer, viewport, index);

			return layer;
		}

		public Editor replaceLayerViewport(Viewport viewport, int index) {
			getObject().replaceLayerViewport(viewport, index);
			return this;
		}
	}
}
