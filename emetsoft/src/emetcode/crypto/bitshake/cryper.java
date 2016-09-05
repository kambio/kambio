package emetcode.crypto.bitshake;

import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

import emetcode.crypto.bitshake.utils.bit_array;
import emetcode.crypto.bitshake.utils.convert;
import emetcode.crypto.bitshake.utils.mer_twist;
import emetcode.util.devel.logger;

public class cryper {

	private static final boolean IN_DEBUG_1 = false;

	private static final ReentrantLock code_lock = new ReentrantLock();

	private byte[] key_bytes;

	private mer_twist mt_for_bytes;
	private mer_twist mt_for_bits;

	private swap_opers byte_swapseq;
	private swap_opers bit_swapseq;

	public cryper(byte[] the_key) {
		init_cryper(the_key);
	}

	private void init_cryper() {
		// all_bits = false;
		key_bytes = null;

		mt_for_bytes = null;
		mt_for_bits = null;

		byte_swapseq = null;
		bit_swapseq = null;
	}

	private void init_cryper(byte[] the_key) {
		init_cryper();
		key_bytes = Arrays.copyOf(the_key, the_key.length);
	}

	public byte[] get_key_copy() {
		byte[] kk2 = Arrays.copyOf(key_bytes, key_bytes.length);
		return kk2;
	}

	public byte[] get_key() {
		return key_bytes;
	}

	void reset_cryper() {
		byte[] old_kk = key_bytes;
		init_cryper();
		key_bytes = old_kk;
	}

	private int cryper_size() {
		int the_sz = 0;
		code_lock.lock();
		try {
			if (byte_swapseq == null) {
				return 0;
			}
			if (bit_swapseq == null) {
				throw new bad_bitshake(2);
			}

			if (bit_swapseq.opers_size() != byte_swapseq.opers_size()) {
				throw new bad_bitshake(2, "s1=" + bit_swapseq.opers_size()
						+ " s2=" + byte_swapseq.opers_size());
			}
			the_sz = byte_swapseq.opers_size();
		} finally {
			code_lock.unlock();
		}
		return the_sz;
	}

	private void restart_cryper(int max_ops) {
		if (key_bytes == null) {
			throw new bad_bitshake(2);
		}
		if (key_bytes.length == 0) {
			throw new bad_bitshake(2);
		}

		code_lock.lock();
		try {

			byte[] init_kk = Arrays.copyOf(key_bytes, key_bytes.length);

			mt_for_bytes = null;
			mt_for_bits = null;

			init_mer_twists(init_kk);

			byte_swapseq = new swap_opers(mt_for_bytes, max_ops, init_kk);
			bit_swapseq = new swap_opers(mt_for_bits, max_ops, init_kk);

			if (byte_swapseq.opers_size() != max_ops) {
				throw new bad_bitshake(2);
			}
			if (bit_swapseq.opers_size() != max_ops) {
				throw new bad_bitshake(2);
			}

		} finally {
			code_lock.unlock();
		}

	}

	private long dbg_calc_key_id() {
		if (key_bytes != null) {
			return convert.calc_minisha_long(key_bytes);
		}
		return 0;
	}

	private static void prt_dbg_target(String pref, byte[] tgt, long key_id) {
		long msg_id = 0;
		if (tgt != null) {
			msg_id = convert.calc_minisha_long(tgt);
		}
		logger.debug(pref + "_MSG=" + msg_id + "  KEY_ID=" + key_id);
	}

	public byte[] encryit(byte[] tgt, boolean all_bits) {
		if (IN_DEBUG_1) {
			prt_dbg_target("BEFORE_ECRYPT", tgt, dbg_calc_key_id());
		}
		encry_bytes(tgt);
		encry_bits(tgt, all_bits);
		if (IN_DEBUG_1) {
			prt_dbg_target("AFTER_ECRYPT", tgt, dbg_calc_key_id());
		}
		return tgt;
	}

	public byte[] decryit(byte[] tgt, boolean all_bits) {
		if (IN_DEBUG_1) {
			prt_dbg_target("before_decrypt", tgt, dbg_calc_key_id());
		}
		decry_bits(tgt, all_bits);
		decry_bytes(tgt);
		if (IN_DEBUG_1) {
			prt_dbg_target("after_decrypt", tgt, dbg_calc_key_id());
		}
		return tgt;
	}

	private void init_mer_twists(byte[] the_key) {
		if (the_key == null) {
			throw new bad_bitshake(2);
		}
		assert (the_key.length > 0);
		assert (mt_for_bytes == null);
		mt_for_bytes = new mer_twist(the_key);

		generate.shuffle_bits_with(the_key, mt_for_bytes);

		assert (mt_for_bits == null);
		mt_for_bits = new mer_twist(the_key);

		assert (mt_for_bytes != null);
		assert (mt_for_bits != null);
	}

	long get_oper(swap_opers ops, int idx, long max_op) {
		long op = ops.get_op(idx);
		long fit_op = convert.to_interval(op, 0, max_op);
		return fit_op;
	}

	void byte_oper(int oper, byte[] tgt) {
		long t_sz = tgt.length;
		long v_op = get_oper(byte_swapseq, oper, t_sz);
		bit_array.swap_bytes(tgt, v_op, oper);
	}

	void bit_oper(int oper, byte[] tgt) {
		long t_sz = tgt.length * Byte.SIZE;
		long v_op = get_oper(bit_swapseq, oper, t_sz);
		bit_array.swap_bits(tgt, v_op, oper);
	}

	int num_byte_opers(byte[] tgt) {
		return tgt.length;
	}

	int num_bit_opers(byte[] tgt, boolean all_bits) {
		int nn = tgt.length;
		if (all_bits) {
			nn = nn * Byte.SIZE;
		}
		return nn;
	}

	void encry_bytes(byte[] tgt) {
		int num_ops = num_byte_opers(tgt);
		if (cryper_size() < num_ops) {
			restart_cryper(num_ops);
		}
		assert (byte_swapseq != null);
		assert (tgt != null);
		for (int aa = 0; aa < num_ops; aa++) {
			byte_oper(aa, tgt);
		}
	}

	void encry_bits(byte[] tgt, boolean all_bits) {
		int num_ops = num_bit_opers(tgt, all_bits);
		if (cryper_size() < num_ops) {
			restart_cryper(num_ops);
		}
		assert (bit_swapseq != null);
		assert (tgt != null);
		for (int aa = 0; aa < num_ops; aa++) {
			bit_oper(aa, tgt);
		}
	}

	void decry_bytes(byte[] tgt) {
		int num_ops = num_byte_opers(tgt);
		if (cryper_size() < num_ops) {
			restart_cryper(num_ops);
		}
		assert (byte_swapseq != null);
		assert (tgt != null);
		for (int aa = (num_ops - 1); aa >= 0; aa--) {
			byte_oper(aa, tgt);
		}
	}

	void decry_bits(byte[] tgt, boolean all_bits) {
		int num_ops = num_bit_opers(tgt, all_bits);
		if (cryper_size() < num_ops) {
			restart_cryper(num_ops);
		}
		assert (bit_swapseq != null);
		assert (tgt != null);
		for (int aa = (num_ops - 1); aa >= 0; aa--) {
			bit_oper(aa, tgt);
		}
	}

}
