package com.kiwiple.multimedia.util;

import java.math.BigInteger;
import java.util.Random;

/**
 * Randomizer.
 */
public class Randomizer {

	// // // // // Static variable.
	// // // // //
	private static final int[] sRandomNumberTable = {
	/**/81006, 31498, 59375, 30502, 44868, 81279, 23585, 49678, 70014, 10523,
	/**/15458, 83481, 50187, 43375, 56644, 72076, 59403, 65469, 74760, 69509,
	/**/33469, 12510, 23095, 48016, 22064, 39774, 67373, 10555, 33345, 21787,
	/**/67198, 17176, 65996, 18317, 83083, 11921, 56254, 68437, 59481, 54778,
	/**/58037, 92261, 85504, 55690, 63488, 26451, 43223, 38009, 50567, 39191,
	/**/84983, 68312, 25519, 56158, 22390, 12823, 92390, 28947, 36708, 25393,
	/**/35554, 32935, 72889, 68772, 79774, 14336, 50716, 63003, 86391, 94074,
	/**/94368, 17632, 50962, 71908, 13105, 76285, 31819, 16884, 11665, 16594,
	/**/81311, 60479, 69985, 30952, 93067, 70056, 55229, 83226, 22555, 66447,
	/**/13823, 89887, 55828, 74452, 21692, 55847, 15960, 47521, 27784, 25728,
	/**/80422, 65437, 38797, 56261, 88300, 35980, 56656, 45662, 29219, 49257,
	/**/61307, 49468, 43344, 43700, 14074, 19739, 43275, 99444, 62545, 23720,
	/**/83873, 82557, 10002, 80093, 74645, 33109, 15281, 38759, 29342, 69408,
	/**/38110, 16855, 28922, 93758, 22885, 36706, 92542, 60270, 99599, 17983,
	/**/43892, 91189, 87226, 56935, 99836, 85489, 89693, 49475, 31941, 78065,
	/**/93683, 89664, 53927, 49885, 94979, 88848, 42642, 93218, 80305, 49428,
	/**/32748, 72121, 11972, 96914, 83264, 89016, 45140, 20362, 63242, 86255,
	/**/49211, 92963, 38625, 65312, 52156, 36400, 67050, 64058, 45489, 24165,
	/**/63365, 64224, 69475, 57512, 85097, 35054, 88673, 96593, 55902, 53320,
	/**/63576, 26373, 44610, 43748, 90399, 16770, 71609, 90916, 69002, 57180
	/**/};
	private static final int RANDOM_NUMBER_TABLE_LENGTH = sRandomNumberTable.length;
	private static final int RANDOM_NUMBER_RANGE_LOWER = 10000;
	private static final int RANDOM_NUMBER_RANGE_UPPER = Integer.MAX_VALUE - RANDOM_NUMBER_RANGE_LOWER - 1;

	private static final long LCG_MODULUS = 2L << 32;
	private static final long LCG_MULTIPLIER = 1664525L;
	private static final long LCG_INCREMENT = 1013904223L;

	// // // // // Member variable.
	// // // // //
	private int mSeed;

	// // // // // Constructor.
	// // // // //
	public Randomizer() {
		int randomInteger = RANDOM_NUMBER_RANGE_LOWER + new Random().nextInt(RANDOM_NUMBER_RANGE_UPPER);
		mSeed = new BigInteger(String.valueOf(randomInteger)).nextProbablePrime().intValue();
	}

	public Randomizer(int seed) {
		mSeed = seed;
	}

	// // // // // Method.
	// // // // //
	public void setSeed(int seed) {
		mSeed = seed;
	}

	public int getSeed() {
		return mSeed;
	}

	public int randomize(int value) {
		value += sRandomNumberTable[value % RANDOM_NUMBER_TABLE_LENGTH];
		return (int) ((LCG_MULTIPLIER * (value * mSeed) + LCG_INCREMENT) % LCG_MODULUS);
	}

	public long randomize(long value) {
		value += sRandomNumberTable[(int) (value % RANDOM_NUMBER_TABLE_LENGTH)];
		return (LCG_MULTIPLIER * (value * mSeed) + LCG_INCREMENT) % LCG_MODULUS;
	}

	public int randomizeAbs(int value) {
		return Math.abs(randomize(value));
	}

	public long randomizeAbs(long value) {
		return Math.abs(randomize(value));
	}
}
