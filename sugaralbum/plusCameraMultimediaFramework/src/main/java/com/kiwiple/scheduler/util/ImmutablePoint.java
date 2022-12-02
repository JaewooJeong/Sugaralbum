package com.kiwiple.scheduler.util;

import java.io.Serializable;

/**
 * ImmutablePoint.
 * 
 */
public class ImmutablePoint implements Serializable {
    private static final long serialVersionUID = 181402026861653068L;

    public final int x;
	public final int y;

	/**
	 * 생성자. x, y 좌표를 초기화 한다. 
	 * @param x : x 좌표. 
	 * @param y : y 좌표. 
	 */
	public ImmutablePoint(int x, int y) {
		this.x = x;
		this.y = y;
	}
}
