package com.kiwiple.multimedia.canvas;

/**
 * IPreset.
 */
public abstract interface IPreset<T extends VisualizerChild.Editor<?, ?>> {

	public abstract void inject(T editor, float magnification);
}