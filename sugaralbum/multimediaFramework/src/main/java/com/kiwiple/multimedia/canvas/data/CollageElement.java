package com.kiwiple.multimedia.canvas.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONException;

import com.kiwiple.debug.Precondition;
import com.kiwiple.imageanalysis.database.ImageData;
import com.kiwiple.multimedia.canvas.ICacheCode;
import com.kiwiple.multimedia.json.IJsonConvertible;
import com.kiwiple.multimedia.json.JsonObject;

/**
 * CollageElement.
 * 
 */
public class CollageElement extends ImageData implements ICacheCode, IJsonConvertible, Cloneable {

	// // // // // Static variable.
	// // // // //
	private static final long serialVersionUID = -1827306242556699540L;

	/**
	 * 어떠한 요소도 포함하지 않으며, 변경 불가능한 {@link List} 객체입니다.
	 * 
	 * @see Collections#unmodifiableList(List)
	 */
	public static final List<CollageElement> EMPTY_LIST = Collections.unmodifiableList(new ArrayList<CollageElement>(0));

	public static final String JSON_NAME_IMAGE_ID = "image_id";
	public static final String JSON_NAME_FILE_PATH = "file_path";
	public static final String JSON_NAME_ORIENTATION = "orientation";
	public static final String JSON_NAME_TEMPLET_ID = "templet_id";
	public static final String JSON_NAME_COORDINATE_X = "coordinate_x";
	public static final String JSON_NAME_COORDINATE_Y = "coordinate_y";
	public static final String JSON_NAME_SCALE = "scale";
	public static final String JSON_NAME_ROTATE = "rotate";
	public static final String JSON_NAME_WIDTH = "width";
	public static final String JSON_NAME_HEIGHT = "height";
	public static final String JSON_NAME_COLLAGE_WIDTH = "collage_width";
	public static final String JSON_NAME_COLLAGE_HEIGHT = "collage_height";
	public static final String JSON_NAME_FRAME_BOARD_WIDTH = "frame_board_width";
	public static final String JSON_NAME_FRAME_CORNER_RADIUS = "frame_corner_radius";
	public static final String JSON_NAME_BACKGROUND_COLOR = "background_color";
	public static final String JSON_NAME_BACKGROUND_COLOR_TAG = "background_color_tag";
	public static final String JSON_NAME_BACKGROUND_IMAGE_FILE_NAME = "background_image_file_name";

	// // // // // Constructor.
	// // // // //
	public CollageElement() {
		super();
	}

	public CollageElement(CollageElement other) {
		Precondition.checkNotNull(other);

		this.id = other.id;
		this.path = other.path;
		this.orientation = other.orientation;
		this.width = other.width;
		this.height = other.height;

		this.imageCorrectData.collageTempletId = other.imageCorrectData.collageTempletId;
		this.imageCorrectData.collageCoordinate.x = other.imageCorrectData.collageCoordinate.x;
		this.imageCorrectData.collageCoordinate.y = other.imageCorrectData.collageCoordinate.y;
		this.imageCorrectData.collageRotate = other.imageCorrectData.collageRotate;
		this.imageCorrectData.collageScale = other.imageCorrectData.collageScale;
		this.imageCorrectData.collageWidth = other.imageCorrectData.collageWidth;
		this.imageCorrectData.collageHeight = other.imageCorrectData.collageHeight;
		this.imageCorrectData.collageFrameBorderWidth = other.imageCorrectData.collageFrameBorderWidth;
		this.imageCorrectData.collageFrameCornerRadius = other.imageCorrectData.collageFrameCornerRadius;
		this.imageCorrectData.collageBackgroundColor = other.imageCorrectData.collageBackgroundColor;
		this.imageCorrectData.collageBackgroundColorTag = other.imageCorrectData.collageBackgroundColorTag;
		this.imageCorrectData.collageBackgroundImageFileName = other.imageCorrectData.collageBackgroundImageFileName;
	}

	public CollageElement(JsonObject jsonObject) throws JSONException {
		injectJsonObject(jsonObject);
	}

	// // // // // Method.
	// // // // //
	@Override
	public JsonObject toJsonObject() throws JSONException {

		JsonObject jsonObject = new JsonObject();

		jsonObject.put(JSON_NAME_IMAGE_ID, id);
		jsonObject.put(JSON_NAME_FILE_PATH, path);
		jsonObject.put(JSON_NAME_ORIENTATION, orientation);
		jsonObject.put(JSON_NAME_WIDTH, width);
		jsonObject.put(JSON_NAME_HEIGHT, height);

		jsonObject.put(JSON_NAME_TEMPLET_ID, imageCorrectData.collageTempletId);
		jsonObject.put(JSON_NAME_COORDINATE_X, imageCorrectData.collageCoordinate.x);
		jsonObject.put(JSON_NAME_COORDINATE_Y, imageCorrectData.collageCoordinate.y);
		jsonObject.put(JSON_NAME_SCALE, imageCorrectData.collageScale);
		jsonObject.put(JSON_NAME_ROTATE, imageCorrectData.collageRotate);
		jsonObject.put(JSON_NAME_COLLAGE_WIDTH, imageCorrectData.collageWidth);
		jsonObject.put(JSON_NAME_COLLAGE_HEIGHT, imageCorrectData.collageHeight);
		jsonObject.put(JSON_NAME_FRAME_BOARD_WIDTH, imageCorrectData.collageFrameBorderWidth);
		jsonObject.put(JSON_NAME_FRAME_CORNER_RADIUS, imageCorrectData.collageFrameCornerRadius);
		jsonObject.put(JSON_NAME_BACKGROUND_COLOR, imageCorrectData.collageBackgroundColor);
		jsonObject.put(JSON_NAME_BACKGROUND_COLOR_TAG, imageCorrectData.collageBackgroundColorTag);
		jsonObject.put(JSON_NAME_BACKGROUND_IMAGE_FILE_NAME, imageCorrectData.collageBackgroundImageFileName);

		return jsonObject;
	}

	public void injectJsonObject(JsonObject jsonObject) throws JSONException {
		Precondition.checkNotNull(jsonObject);

		id = jsonObject.optInt(JSON_NAME_IMAGE_ID);
		path = jsonObject.getString(JSON_NAME_FILE_PATH);
		orientation = jsonObject.optString(JSON_NAME_ORIENTATION, "0");
		width = jsonObject.getInt(JSON_NAME_WIDTH);
		height = jsonObject.getInt(JSON_NAME_HEIGHT);

		imageCorrectData.collageTempletId = jsonObject.getInt(JSON_NAME_TEMPLET_ID);
		imageCorrectData.collageCoordinate.x = jsonObject.getFloat(JSON_NAME_COORDINATE_X);
		imageCorrectData.collageCoordinate.y = jsonObject.getFloat(JSON_NAME_COORDINATE_Y);
		imageCorrectData.collageRotate = jsonObject.getFloat(JSON_NAME_ROTATE);
		imageCorrectData.collageScale = jsonObject.getFloat(JSON_NAME_SCALE);
		imageCorrectData.collageWidth = jsonObject.getInt(JSON_NAME_COLLAGE_WIDTH);
		imageCorrectData.collageHeight = jsonObject.getInt(JSON_NAME_COLLAGE_HEIGHT);
		imageCorrectData.collageFrameBorderWidth = jsonObject.getFloat(JSON_NAME_FRAME_BOARD_WIDTH);
		imageCorrectData.collageFrameCornerRadius = jsonObject.getFloat(JSON_NAME_FRAME_CORNER_RADIUS);
		imageCorrectData.collageBackgroundColor = jsonObject.getInt(JSON_NAME_BACKGROUND_COLOR);
		imageCorrectData.collageBackgroundColorTag = jsonObject.optString(JSON_NAME_BACKGROUND_COLOR_TAG);
		imageCorrectData.collageBackgroundImageFileName = jsonObject.optString(JSON_NAME_BACKGROUND_IMAGE_FILE_NAME);
	}

	@Override
	public int createCacheCode() {

		StringBuilder builder = new StringBuilder();
		builder.append(id);
		builder.append(path);
		builder.append(orientation);
		builder.append(width);
		builder.append(height);
		builder.append(imageCorrectData.collageTempletId);
		builder.append(imageCorrectData.collageCoordinate.x);
		builder.append(imageCorrectData.collageCoordinate.y);
		builder.append(imageCorrectData.collageRotate);
		builder.append(imageCorrectData.collageScale);
		builder.append(imageCorrectData.collageWidth);
		builder.append(imageCorrectData.collageHeight);
		builder.append(imageCorrectData.collageFrameBorderWidth);
		builder.append(imageCorrectData.collageFrameCornerRadius);
		builder.append(imageCorrectData.collageBackgroundColor);
		builder.append(imageCorrectData.collageBackgroundColorTag);
		builder.append(imageCorrectData.collageBackgroundImageFileName);

		return builder.toString().hashCode();
	}

	@Override
	public Object clone() {
		return new CollageElement(this);
	}
}