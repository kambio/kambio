package emetcode.crypto.bitshake;

import emetcode.crypto.bitshake.utils.bit_array;
import emetcode.crypto.bitshake.utils.config;
import emetcode.crypto.bitshake.utils.convert;
import emetcode.crypto.bitshake.utils.mer_twist;

public class generate {

	public static long gen_oper(mer_twist rand_gen, byte[] key_bytes) {
		assert (key_bytes != null);
		long key_num_bits = bit_array.num_bits(key_bytes);
		long LONG_NUM_BITS = 64;
		long op = 0;

		long idx1 = 0;
		long idx2 = 0;

		op = rand_gen.nextLong();
		idx1 = convert.to_interval(rand_gen.nextLong(), 0, key_num_bits);
		idx2 = convert.to_interval(rand_gen.nextLong(), 0, LONG_NUM_BITS);

		boolean val = bit_array.get_bit(key_bytes, idx1);
		bit_array.set_bit(op, idx2, val);

		return op;
	}

	public static long gen_oper(mer_twist rand_gen, byte[] key_bytes,
			long max_op) {
		long op = gen_oper(rand_gen, key_bytes);
		op = convert.to_interval(op, 0, max_op);
		return op;
	}

	public static long gen_oper(mer_twist rand_gen, byte[] key_bytes,
			long min_op, long max_op) {
		long op = gen_oper(rand_gen, key_bytes);
		op = convert.to_interval(op, min_op, max_op);
		return op;
	}

	public static void shuffle_bits_with(byte[] key_bytes, mer_twist kk) {
		assert (key_bytes != null);
		long nn = convert.to_interval(kk.nextLong(),
				config.MIN_KEY_INIT_CHANGES, config.MAX_KEY_INIT_CHANGES);
		long key_num_bits = bit_array.num_bits(key_bytes);
		for (long aa = 0; aa < nn; aa++) {
			long idx1 = convert.to_interval(kk.nextLong(), 0, key_num_bits);
			long idx2 = convert.to_interval(kk.nextLong(), 0, key_num_bits);

			// key_bits.swap(idx1, idx2);
			bit_array.swap_bits(key_bytes, idx1, idx2);
		}
	}

	public static void swap_ints(int[] aa, int bb1, int bb2) {
		assert (aa != null);
		int tmp1 = aa[bb1];
		aa[bb1] = aa[bb2];
		aa[bb2] = tmp1;
	}

	public static int[] shuffle_ints(mer_twist rand_gen, int max_int) {
		int[] arr_ints = new int[max_int];
		for (int aa = 0; aa < arr_ints.length; aa++) {
			arr_ints[aa] = aa;
		}

		for (int aa = (arr_ints.length - 1); aa >= 0; aa--) {
			int r_pos = (int) (convert.to_interval(rand_gen.nextLong(), 0,
					aa + 1));
			swap_ints(arr_ints, r_pos, aa);
		}
		return arr_ints;
	}

}
