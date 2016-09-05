package emetcode.crypto.bitshake;

import java.util.Arrays;

import emetcode.crypto.bitshake.locale.L;
import emetcode.crypto.bitshake.utils.convert;
import emetcode.crypto.bitshake.utils.mer_twist;

public class swap_opers {

	long[] arr_op;

	swap_opers(mer_twist tm0, int n_op, byte[] kk0) {
		mer_twist tm = new mer_twist(tm0);		
		byte[] kk = Arrays.copyOf(kk0, kk0.length);
		
		arr_op = null;
		init_op_arr(tm, kk, n_op);
	}

	int opers_size() {
		return arr_op.length;
	}

	long get_op(int idx) {
		return arr_op[idx];
	}

	private static void copy_ops(long[] src, long[] dest, int dest_from) {
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

	private void init_op_arr(mer_twist tm, byte[] kk, int num_op) {
		if ((arr_op != null) && (num_op <= arr_op.length)) {
			return;
		}
		Runtime rt = Runtime.getRuntime();
		try {
			long[] n_arr = new long[num_op];
			if (arr_op != null) {
				copy_ops(arr_op, n_arr, 0);
			}

			int fst_aa = 0;
			if (arr_op != null) {
				fst_aa = arr_op.length;
			}
			for (int aa = fst_aa; aa < n_arr.length; aa++) {
				n_arr[aa] = generate.gen_oper(tm, kk);
			}

			arr_op = n_arr;
		} catch (OutOfMemoryError ex) {
			rt.gc();
			throw new bad_bitshake(2, ex.toString());
		}
	}

	public long get_mod_oper(long idx1, long tg_sz) {
		int blk_sz = arr_op.length;

		long idx0_blk = (idx1 / blk_sz) * blk_sz;

		long rest = (tg_sz - idx0_blk);
		if (rest < blk_sz) {
			blk_sz = (int) rest;
		}

		int op_idx = (int) (idx1 % blk_sz);
		long op = arr_op[op_idx];
		long mod_op1 = convert.to_interval(op, 0, blk_sz);
		long mod_op2 = idx0_blk + mod_op1;

		return mod_op2;
	}

	public boolean ck_op(long idx1, long max_ops, long op) {
		if (op > max_ops) {
			return false;
		}

		int blk_sz = arr_op.length;
		long num_blk = (idx1 / blk_sz);
		long idx0_blk1 = num_blk * blk_sz;
		long idx0_blk2 = (num_blk + 1) * blk_sz;

		if (op < idx0_blk1) {
			return false;
		}
		if (op >= idx0_blk2) {
			return false;
		}

		return true;
	}

}
