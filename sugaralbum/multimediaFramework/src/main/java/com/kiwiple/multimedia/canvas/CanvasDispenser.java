package com.kiwiple.multimedia.canvas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.util.Size;
import com.kiwiple.multimedia.util.StringUtils;

/**
 * CanvasDispenser.
 * 
 */
class CanvasDispenser {

	// // // // // Member variable.
	// // // // //
	private final Region mRegion;
	private final TreeSet<PixelCanvas> mCanvasSet;

	private int mCanvasCount;
	private long mAllocatedByteCount;

	// // // // // Static method.
	// // // // //
	private static List<Integer> makeOptimalSizeList(List<Scene> scenes, List<Transition> transitions) {

		ArrayList<Integer> result = new ArrayList<>();
		ArrayList<Integer> concurrentSizeList = new ArrayList<>();

		for (int i = 0, scenesSize = scenes.size(); i != scenesSize; ++i) {

			updateSizeList(concurrentSizeList, scenes.get(i));

			boolean isLastScene = (i == scenesSize - 1);
			if (!isLastScene) {
				updateSizeList(concurrentSizeList, scenes.get(i + 1));
				updateSizeList(concurrentSizeList, transitions.get(i));
			}
			result = makeOptimalSizeList(result, concurrentSizeList);
			concurrentSizeList.clear();
		}
		return result;
	}

	private static ArrayList<Integer> makeOptimalSizeList(ArrayList<Integer> list1, ArrayList<Integer> list2) {

		Collections.sort(list1, Collections.reverseOrder());
		Collections.sort(list2, Collections.reverseOrder());

		ArrayList<Integer> biggerList = list1.size() >= list2.size() ? list1 : list2;
		ArrayList<Integer> smallerList = biggerList == list1 ? list2 : list1;
		ArrayList<Integer> result = new ArrayList<>();

		for (Integer size : smallerList)
			result.add(Math.max(biggerList.remove(0), size));
		result.addAll(biggerList);

		return result;
	}

	private static void updateSizeList(ArrayList<Integer> list, RegionChild regionChild) {

		if (regionChild == null)
			return;
		for (Size size : regionChild.getCanvasRequirementWithChild()) {
			list.add(size.product());
		}
	}

	// // // // // Constructor.
	// // // // //
	CanvasDispenser(Region region) {
		Precondition.checkNotNull(region);

		mRegion = region;
		mCanvasSet = new TreeSet<>(mPixelCanvasComparator);
	}

	// // // // // Method.
	// // // // //
	synchronized void update() {

		clear();

		List<Scene> scenes = mRegion.getScenes();
		List<Transition> transitions = mRegion.getTransitions();
		List<Integer> canvasSizeList = makeOptimalSizeList(scenes, transitions);

		for (int canvasSize : canvasSizeList) {
			mCanvasSet.add(new PixelCanvas(canvasSize));
			mCanvasCount += 1;
			mAllocatedByteCount += PixelCanvas.measureBytes(canvasSize);
		}
	}

	synchronized PixelCanvas getCanvas(Size size) {

		int demand = size.product();
		for (PixelCanvas canvas : mCanvasSet) {
			if (canvas.getCapacity() >= demand) {
				canvas.setImageSize(size);
				canvas.setOffset(0);

				mCanvasSet.remove(canvas);
				return canvas;
			}
		}
		return mRegion.isOnEditMode() ? new PixelCanvas(size, false) : null;
	}

	synchronized void returnCanvas(PixelCanvas pixelCanvas) {
		mCanvasSet.add(pixelCanvas);
	}

	synchronized PixelCanvas optimizeCanvas(PixelCanvas used, Size size) {

		PixelCanvas selected = getCanvas(size);

		if (selected == null) {
			return used;
		} else if (selected.getCapacity() >= used.getCapacity()) {
			returnCanvas(selected);
			return used;
		} else {
			used.copy(selected, size.product());
			selected.setImageSize(used.getImageSize());
			selected.setOffset(used.getOffset());

			returnCanvas(used);
			return selected;
		}
	}

	synchronized void clear() {

		mCanvasSet.clear();
		mCanvasCount = 0;
		mAllocatedByteCount = 0L;
	}

	@Override
	public String toString() {

		StringBuilder builder = new StringBuilder(128);

		float amountMB = mAllocatedByteCount / 1024.0f / 1024.0f;
		builder.append(CanvasDispenser.class.getSimpleName());
		builder.append(" (instantiated PixelCanvas: ");
		builder.append(StringUtils.format("%d objects, %.2fMB", mCanvasCount, amountMB));
		builder.append(") {\n");

		int number = 1;
		for (PixelCanvas canvas : mCanvasSet) {
			int capacity = canvas.getCapacity();
			builder.append('\t').append(number++).append(". ").append(capacity).append(" pixels ");
			builder.append(StringUtils.format("(%.2fMB)\n", PixelCanvas.measureMegabytes(capacity)));
		}
		return builder.append('}').toString();
	}

	// // // // // Anonymous Class.
	// // // // //
	private final Comparator<PixelCanvas> mPixelCanvasComparator = new Comparator<PixelCanvas>() {

		@Override
		public int compare(PixelCanvas lhs, PixelCanvas rhs) {

			// Don't (lhs.getCapacity() == rhs.getCapacity()). This expression is intentional.
			if (lhs == rhs)
				return 0;
			return lhs.getCapacity() > rhs.getCapacity() ? 1 : -1;
		}
	};
}