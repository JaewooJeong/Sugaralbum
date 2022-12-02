package com.kiwiple.multimedia.util;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.kiwiple.debug.Precondition;

/**
 * 라이브러리 개발 목적으로 사용하는 클래스입니다. 라이브러리 외부에서의 사용에 대해서는 그 유효성을 보장하지 않습니다.
 */
@SuppressWarnings("unchecked")
public final class ArrayUtils {

	public static int sum(int... array) {
		Precondition.checkNotNull(array);

		int result = 0;
		for (int element : array) {
			result += element;
		}
		return result;
	}

	public static long sum(long... array) {
		Precondition.checkNotNull(array);

		long result = 0L;
		for (long element : array) {
			result += element;
		}
		return result;
	}

	public static float sum(float... array) {
		Precondition.checkNotNull(array);

		float result = 0.0f;
		for (float element : array) {
			result += element;
		}
		return result;
	}

	public static double sum(double... array) {
		Precondition.checkNotNull(array);

		double result = 0.0;
		for (double element : array) {
			result += element;
		}
		return result;
	}

	public static int average(int... array) {
		Precondition.checkNotNull(array);
		return Math.round((float) sum(array) / array.length);
	}

	public static long average(long... array) {
		Precondition.checkNotNull(array);
		return Math.round((double) sum(array) / array.length);
	}

	public static float average(float... array) {
		Precondition.checkNotNull(array);
		return sum(array) / array.length;
	}

	public static double average(double... array) {
		Precondition.checkNotNull(array);
		return sum(array) / array.length;
	}

	public static Integer[] box(int... array) {
		Precondition.checkNotNull(array);

		Integer[] boxed = new Integer[array.length];
		for (int i = 0; i != array.length; ++i) {
			boxed[i] = array[i];
		}
		return boxed;
	}

	public static Long[] box(long... array) {
		Precondition.checkNotNull(array);

		Long[] boxed = new Long[array.length];
		for (int i = 0; i != array.length; ++i) {
			boxed[i] = array[i];
		}
		return boxed;
	}

	public static Float[] box(float... array) {
		Precondition.checkNotNull(array);

		Float[] boxed = new Float[array.length];
		for (int i = 0; i != array.length; ++i) {
			boxed[i] = array[i];
		}
		return boxed;
	}

	public static Double[] box(double... array) {
		Precondition.checkNotNull(array);

		Double[] boxed = new Double[array.length];
		for (int i = 0; i != array.length; ++i) {
			boxed[i] = array[i];
		}
		return boxed;
	}

	public static List<Integer> boxInList(int... array) {
		Precondition.checkNotNull(array);

		ArrayList<Integer> list = new ArrayList<>(array.length);
		for (int element : array) {
			list.add(element);
		}
		return list;
	}

	public static List<Long> boxInList(long... array) {
		Precondition.checkNotNull(array);

		ArrayList<Long> list = new ArrayList<>(array.length);
		for (long element : array) {
			list.add(element);
		}
		return list;
	}

	public static List<Float> boxInList(float... array) {
		Precondition.checkNotNull(array);

		ArrayList<Float> list = new ArrayList<>(array.length);
		for (float element : array) {
			list.add(element);
		}
		return list;
	}

	public static List<Double> boxInList(double... array) {
		Precondition.checkNotNull(array);

		ArrayList<Double> list = new ArrayList<>(array.length);
		for (double element : array) {
			list.add(element);
		}
		return list;
	}

	public static int[] unbox(Integer[] array) {
		Precondition.checkNotNull((Object) array);

		int[] unboxed = new int[array.length];

		for (int i = 0; i != array.length; ++i) {
			Integer element = array[i];
			array[i] = (element == null) ? 0 : element;
		}
		return unboxed;
	}

	public static long[] unbox(Long[] array) {
		Precondition.checkNotNull((Object) array);

		long[] unboxed = new long[array.length];

		for (int i = 0; i != array.length; ++i) {
			Long element = array[i];
			array[i] = (element == null) ? 0L : element;
		}
		return unboxed;
	}

	public static float[] unbox(Float[] array) {
		Precondition.checkNotNull((Object) array);

		float[] unboxed = new float[array.length];

		for (int i = 0; i != array.length; ++i) {
			Float element = array[i];
			array[i] = (element == null) ? 0.0f : element;
		}
		return unboxed;
	}

	public static double[] unbox(Double[] array) {
		Precondition.checkNotNull((Object) array);

		double[] unboxed = new double[array.length];

		for (int i = 0; i != array.length; ++i) {
			Double element = array[i];
			array[i] = (element == null) ? 0.0 : element;
		}
		return unboxed;
	}

	public static int[] unboxCollectionInteger(Collection<Integer> collection) {
		Precondition.checkNotNull(collection);

		int[] array = new int[collection.size()];
		Iterator<Integer> iter = collection.iterator();

		for (int i = 0; iter.hasNext(); ++i) {
			Integer element = iter.next();
			array[i] = (element == null) ? 0 : element;
		}
		return array;
	}

	public static long[] unboxCollectionLong(Collection<Long> collection) {
		Precondition.checkNotNull(collection);

		long[] array = new long[collection.size()];
		Iterator<Long> iter = collection.iterator();

		for (int i = 0; iter.hasNext(); ++i) {
			Long element = iter.next();
			array[i] = (element == null) ? 0L : element;
		}
		return array;
	}

	public static float[] unboxCollectionFloat(Collection<Float> collection) {
		Precondition.checkNotNull(collection);

		float[] array = new float[collection.size()];
		Iterator<Float> iter = collection.iterator();

		for (int i = 0; iter.hasNext(); ++i) {
			Float element = iter.next();
			array[i] = (element == null) ? 0.0f : element;
		}
		return array;
	}

	public static double[] unboxCollectionDouble(Collection<Double> collection) {
		Precondition.checkNotNull(collection);

		double[] array = new double[collection.size()];
		Iterator<Double> iter = collection.iterator();

		for (int i = 0; iter.hasNext(); ++i) {
			Double element = iter.next();
			array[i] = (element == null) ? 0.0 : element;
		}
		return array;
	}

	public static <T> T random(T[] array) {
		Precondition.checkNotNull((Object) array);
		return array[(int) (Math.random() * array.length)];
	}

	public static <T> T[] copy(T[] original) {
		Precondition.checkNotNull(original);
		return Arrays.copyOf(original, original.length);
	}

	/**
	 * 주어진 배열과 같은 크기의 새로운 배열을 생성하고 각 요소를 복사하되, 복사는 전적으로 요소로서 포함되어 있는 각 객체가 구현한
	 * {@link Object#clone()}에 의해 이루어집니다. 즉, 배열의 입장에서는 deep copy가 이루어지는 셈이지만, 각 요소의 관점에서는 그렇지 않을 수도
	 * 있다는 것을 염두에 두어야 합니다.
	 * 
	 * @param original
	 *            복사할 원본 배열.
	 * @return
	 * 		주어진 배열의 사본.
	 */
	public static <T extends Cloneable> T[] deepClone(T[] original) {
		Precondition.checkNotNull((Object) original);

		try {
			Method cloneMethod = Object.class.getDeclaredMethod("clone");

			T[] result = (T[]) Array.newInstance(original.getClass().getComponentType(), original.length);
			for (int i = 0; i != original.length; ++i) {
				T object = original[i];
				result[i] = (T) (object == null ? null : cloneMethod.invoke(original[i]));
			}
			return result;
		} catch (Exception exception) {
			return Precondition.assureUnreachable(exception.getMessage());
		}
	}

	@SafeVarargs
	public static <T> T[] createArray(T... array) {
		return array;
	}

	private ArrayUtils() {
		// do not instantiate.
	}
}