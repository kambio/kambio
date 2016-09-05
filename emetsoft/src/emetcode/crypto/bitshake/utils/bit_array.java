package emetcode.crypto.bitshake.utils;

import java.io.File;
import java.util.Arrays;

import emetcode.crypto.bitshake.bad_bitshake;
import emetcode.crypto.bitshake.locale.L;

public class bit_array {

	// public static final int NUM_BYTE_BITS = 8;

	public static void main(String args[]) {

		if (args.length < 1) {
			System.out.println("args: <file_name>");
			return;
		}
		// byte[] f_data = mem_file.read_bytes(new File(args[0]));
		byte[] f_data = mem_file.concurrent_read_encrypted_bytes(new File(
				args[0]), null);
		byte[] sha_bytes = convert.calc_sha_bytes(f_data);

		String sha_str = convert.bytes_to_hex_string(sha_bytes);
		System.out.println(sha_str);
	}

	public static void prt_arr(byte[] aa) {
		for (int yy = 7; yy >= 0; yy--) {
			System.out.println(get_bit(aa, yy));
		}
		System.out.println(Arrays.toString(aa));
	}

	public static void prt_arr(long aa) {
		System.out.println(aa);
		System.out.println("=" + Long.toBinaryString(aa));
		// System.out.println(Long.toHexString(aa));
	}

	// (b>>3) = div8(b)
	// (b&7) = mod8(b)

	// for byte_arrays

	public static boolean get_bit(byte[] aa, long bb) {
		int idx = (int) (bb >> 3);
		return (((aa[idx] >> (bb & 7)) & 1) != 0);
	}

	public static void set_bit(byte[] aa, long bb) {
		int idx = (int) (bb >> 3);
		aa[idx] |= (1 << (bb & 7));
	}

	public static void reset_bit(byte[] aa, long bb) {
		int idx = (int) (bb >> 3);
		aa[idx] &= ~(1 << (bb & 7));
	}

	public static void toggle_bit(byte[] aa, long bb) {
		int idx = (int) (bb >> 3);
		aa[idx] ^= (1 << (bb & 7));
	}

	public static void set_bit(byte[] aa, long bb, boolean val) {
		if (val) {
			set_bit(aa, bb);
		} else {
			reset_bit(aa, bb);
		}
	}

	public static void swap_bits(byte[] aa, long bb1, long bb2) {
		boolean tmp1 = get_bit(aa, bb1);
		set_bit(aa, bb1, get_bit(aa, bb2));
		set_bit(aa, bb2, tmp1);
	}

	public static void swap_bytes(byte[] aa, long bb1, long bb2) {
		byte tmp1 = aa[(int) bb1];
		aa[(int) bb1] = aa[(int) bb2];
		aa[(int) bb2] = tmp1;
	}

	public static long num_bits(byte[] aa) {
		return (aa.length * Byte.SIZE);
	}

	// for longs
	public static boolean get_bit(long aa, long bb) {
		assert (bb < 64);
		return (((aa >> bb) & (long) 1) != 0);
	}

	public static long set_bit(long aa, long bb) {
		assert (bb < 64);
		return (aa |= ((long) 1 << bb));
	}

	public static long reset_bit(long aa, long bb) {
		return (aa &= ~((long) 1 << bb));
	}

	public static long toggle_bit(long aa, long bb) {
		return (aa ^= ((long) 1 << bb));
	}

	public static long set_bit(long aa, long bb, boolean val) {
		if (val) {
			aa = set_bit(aa, bb);
		} else {
			aa = reset_bit(aa, bb);
		}
		return aa;
	}

	public static long swap_bits(long aa, long bb1, long bb2) {
		assert (bb1 < 64);
		assert (bb2 < 64);
		boolean tmp1 = get_bit(aa, bb1);
		aa = set_bit(aa, bb1, get_bit(aa, bb2));
		aa = set_bit(aa, bb2, tmp1);
		return aa;
	}

	public static void copy_bytes(byte[] src, byte[] dest, int dest_from) {
		if ((src == null) || (dest == null)) {
			throw new bad_bitshake(2, L.internal_err_null_src);
		}
		if ((dest_from < 0) || (dest_from >= dest.length)) {
			throw new bad_bitshake(2, L.internal_err_invalid_idx);
		}

		for (int aa = 0; (aa < src.length); aa++) {
			int idx = dest_from + aa;
			if (idx >= dest.length) {
				throw new bad_bitshake(2, L.internal_err_invalid_idx_2);
			}
			dest[idx] = src[aa];
		}
	}

}
