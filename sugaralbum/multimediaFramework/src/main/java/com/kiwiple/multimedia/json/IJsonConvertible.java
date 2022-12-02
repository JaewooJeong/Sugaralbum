package com.kiwiple.multimedia.json;

import org.json.JSONException;

/**
 * IJsonConvertible.
 */
public interface IJsonConvertible {

	/**
	 * 객체의 상태를 {@link JsonObject}가 식별할 수 있는 형태로 변환합니다. 변환 결과로 생성된 객체는 변환 대상이 되는 객체를 변환 시점과 동일한 상태로
	 * 복원하기 위해 필요한 모든 정보를 포함해야 합니다.<br />
	 * <br />
	 * {@link JsonObject}가 식별 가능한 형태는 다음과 같습니다.
	 * 
	 * <ul>
	 * <li>{@link JsonObject}</li>
	 * <li>{@link JsonArray}</li>
	 * <li>{@link String}</li>
	 * <li>{@link Integer}</li>
	 * <li>{@link Long}</li>
	 * <li>{@link Float}</li>
	 * <li>{@link Double}</li>
	 * <li>{@link Boolean}</li>
	 * <li>{@link JsonObject#NULL}</li>
	 * <li>{@code null}</code></li>
	 * </ul>
	 * 
	 * @throws JSONException
	 *             org.json API 사용 중에 오류가 발생했을 때.
	 */
	public abstract Object toJsonObject() throws JSONException;
}
