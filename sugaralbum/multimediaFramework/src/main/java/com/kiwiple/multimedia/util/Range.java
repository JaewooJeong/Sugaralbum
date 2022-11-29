package com.kiwiple.multimedia.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.kiwiple.debug.Precondition;

/**
 * 두 숫자로써 모종의 구간, 범위, 간격 등을 표현하기 위한 클래스.
 */
public final class Range implements Iterable<Integer> {

	// // // // // Member variable.
	// // // // //
	/**
	 * 시작하는 숫자.
	 */
	public final int start;

	/**
	 * 끝나는 숫자.
	 */
	public final int end;

	private final int actualStart;

	private final int actualEnd;

	/**
	 * 시작하는 숫자의 {@link BoundType}.
	 */
	public final BoundType startBoundType;

	/**
	 * 끝나는 숫자의 {@link BoundType}.
	 */
	public final BoundType endBoundType;

	// // // // // Static method.
	// // // // //
	/**
	 * 개구간 즉, 주어진 두 숫자를 포함하지 않는 Range 객체를 생성합니다.
	 * 
	 * @param start
	 *            시작하는 숫자. {@code end}보다 작거나 같아야 합니다.
	 * @param end
	 *            끝나는 숫자. {@code start}보다 크거나 같아야 합니다.
	 * @return 개구간에 해당하는 Range 객체.
	 */
	public static Range open(int start, int end) {
		return new Range(start, BoundType.OPEN, end, BoundType.OPEN);
	}

	/**
	 * 폐구간 즉, 주어진 두 숫자를 포함하는 Range 객체를 생성합니다.
	 * 
	 * @param start
	 *            시작하는 숫자. {@code end}보다 작거나 같아야 합니다.
	 * @param end
	 *            끝나는 숫자. {@code start}보다 크거나 같아야 합니다.
	 * @return 폐구간에 해당하는 Range 객체.
	 */
	public static Range closed(int start, int end) {
		return new Range(start, BoundType.CLOSED, end, BoundType.CLOSED);
	}

	/**
	 * 시작하는 숫자는 포함하지 않지만 끝나는 숫자는 포함하는 Range 객체를 생성합니다.
	 * 
	 * @param start
	 *            시작하는 숫자. {@code end}보다 작거나 같아야 합니다.
	 * @param end
	 *            끝나는 숫자. {@code start}보다 크거나 같아야 합니다.
	 * @return 개구간에 해당하는 Range 객체.
	 */
	public static Range openClosed(int start, int end) {
		return new Range(start, BoundType.OPEN, end, BoundType.CLOSED);
	}

	/**
	 * 시작하는 숫자는 포함하지만 끝나는 숫자는 포함하지 않는 Range 객체를 생성합니다.
	 * 
	 * @param start
	 *            시작하는 숫자. {@code end}보다 작거나 같아야 합니다.
	 * @param end
	 *            끝나는 숫자. {@code start}보다 크거나 같아야 합니다.
	 * @return 개구간에 해당하는 Range 객체.
	 */
	public static Range closedOpen(int start, int end) {
		return new Range(start, BoundType.CLOSED, end, BoundType.OPEN);
	}

	// // // // // Constructor.
	// // // // //
	/**
	 * 두 숫자로써 Range 객체를 생성합니다.
	 * 
	 * @param start
	 *            시작하는 숫자. {@code end}보다 작거나 같아야 합니다.
	 * @param end
	 *            끝나는 숫자. {@code start}보다 크거나 같아야 합니다.
	 * @throws IllegalArgumentException
	 *             {@code start}가 {@code end}보다 큰 경우.
	 */
	private Range(int start, BoundType startBoundType, int end, BoundType endBoundType) {
		Precondition.checkArgument(start <= end, "start must be less than or equal to end.");

		this.start = start;
		this.startBoundType = startBoundType;
		this.end = end;
		this.endBoundType = endBoundType;

		actualStart = startBoundType.equals(BoundType.OPEN) ? start + 1 : start;
		actualEnd = endBoundType.equals(BoundType.OPEN) ? end - 1 : end;
	}

	// // // // // Method.
	// // // // //
	/**
	 * {@code start}와 {@code end}의 차이 즉, {@code (end - start)}를 반환합니다.
	 * 
	 * @return {@code start}와 {@code end}의 차이.
	 */
	public int difference() {
		return end - start;
	}

	/**
	 * 포함되는 숫자의 개수를 반환합니다.
	 * 
	 * @return 포함되는 숫자의 개수.
	 */
	public int count() {

		boolean startBoundIsOpen = startBoundType.equals(BoundType.OPEN);
		boolean endBoundIsOpen = endBoundType.equals(BoundType.OPEN);

		if (start == end && (startBoundIsOpen || endBoundIsOpen)) {
			return 0;
		}

		int count = end - start + 1;
		if (startBoundIsOpen) {
			count -= 1;
		}
		if (endBoundIsOpen) {
			count -= 1;
		}
		return count;
	}

	public boolean contains(int value) {
		return value >= actualStart && value <= actualEnd;
	}

	/**
	 * 음수를 포함하고 있는지의 여부를 반환합니다.
	 * 
	 * @return 음수를 포함하고 있다면 {@code true}.
	 */
	public boolean containsNegative() {
		return actualStart < 0;
	}

	/**
	 * 양수를 포함하고 있는지의 여부를 반환합니다.
	 * 
	 * @return 양수를 포함하고 있다면 {@code true}.
	 */
	public boolean containsPositive() {
		return actualEnd > 0;
	}

	public int[] toArray() {

		int[] array = new int[count()];
		int value = startBoundType.equals(BoundType.OPEN) ? start + 1 : start;

		for (int i = 0; i != array.length; ++i, ++value) {
			array[i] = value;
		}
		return array;
	}

	public List<Integer> toList() {

		int count = count();
		ArrayList<Integer> list = new ArrayList<>(count);
		int value = startBoundType.equals(BoundType.OPEN) ? start + 1 : start;

		for (int i = 0; i != count; ++i, ++value) {
			list.add(value);
		}
		return list;
	}

	@Override
	public String toString() {
		return startBoundType.startSymbol + (start + "," + end) + endBoundType.endSymbol;
	}

	@Override
	public Iterator<Integer> iterator() {

		return new Iterator<Integer>() {

			private int current = actualStart;

			@Override
			public void remove() {
				throw new UnsupportedOperationException("This method is unsupported.");
			}

			@Override
			public Integer next() {
				return current++;
			}

			@Override
			public boolean hasNext() {
				return current <= actualEnd;
			}
		};
	}

	// // // // // Enumeration.
	// // // // //
	/**
	 * 모종의 구간을 나타내는 양 끝점의 집합적 속성을 나타내는 열거형.
	 */
	public enum BoundType {

		/**
		 * 해당 지점에 대해 열려 있음을 의미합니다.
		 */
		OPEN('(', ')'),

		/**
		 * 해당 지점에 대해 닫혀 있음을 의미합니다.
		 */
		CLOSED('[', ']');

		char startSymbol;
		char endSymbol;

		private BoundType(char startSymbol, char endSymbol) {
			this.startSymbol = startSymbol;
			this.endSymbol = endSymbol;
		}
	}
}
