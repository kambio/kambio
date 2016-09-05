package emetcode.crypto.bitshake;

import java.io.File;
import java.util.Arrays;

import emetcode.crypto.bitshake.locale.L;
import emetcode.crypto.bitshake.utils.bit_array;
import emetcode.crypto.bitshake.utils.config;
import emetcode.crypto.bitshake.utils.convert;
import emetcode.crypto.bitshake.utils.mem_file;
import emetcode.crypto.bitshake.utils.mer_twist;

public class signeer {

	static final int MIN_ARR_SZ = 64;

	byte[] key_bytes;
	byte[] target_bytes;
	int arr_sz;

	private long[] keys_arr;
	public long[] data_arr;
	public long[] encr_arr;

	private mer_twist mt_for_longs;
	private mer_twist mt_for_choosing;

	public signeer() {
		init_signeer();
	}

	public signeer(byte[] tgt_data, byte[] the_key, int n_bits) {
		init_signeer_with(tgt_data, the_key, n_bits);
	}

	public signeer(String signer_file_nm, byte[] tgt_data) {
		read_signer_data(signer_file_nm);
		target_bytes = null;
		if (tgt_data != null) {
			target_bytes = tgt_data;
		}
	}

	public signeer(byte[] sgn_puk) {
		init_signeer();
		set_signer_data(sgn_puk);
	}

	void init_signeer() {
		target_bytes = null;
		key_bytes = null;
		arr_sz = 0;

		keys_arr = null;
		data_arr = null;
		encr_arr = null;

		mt_for_longs = null;
		mt_for_choosing = null;
	}

	void init_signeer_with(byte[] tgt_data, byte[] the_key, int n_bits) {
		assert (the_key != null);
		assert (the_key.length > 0);
		init_signeer();

		target_bytes = tgt_data;

		key_bytes = Arrays.copyOf(the_key, the_key.length);
		// key_bytes = the_key;

		arr_sz = n_bits * 2;
		if (arr_sz < MIN_ARR_SZ) {
			arr_sz = MIN_ARR_SZ;
		}

		if (key_bytes != null) {
			init_generators();
			init_arrays();
		}
	}

	void init_generators() {
		assert (key_bytes != null);
		assert (key_bytes.length > 0);
		assert (mt_for_longs == null);
		mt_for_longs = new mer_twist(key_bytes);

		generate.shuffle_bits_with(key_bytes, mt_for_longs);

		assert (mt_for_choosing == null);
		mt_for_choosing = new mer_twist(key_bytes);

		assert (mt_for_longs != null);
		assert (mt_for_choosing != null);
	}

	void init_arrays() {
		long max_op = Long.MAX_VALUE;

		long[] rnd_nums1 = new long[arr_sz];
		for (int aa = 0; aa < rnd_nums1.length; aa++) {
			rnd_nums1[aa] = generate.gen_oper(mt_for_longs, key_bytes, max_op);
		}

		long[] rnd_nums2 = new long[arr_sz];
		for (int ii = 0; ii < rnd_nums2.length; ii++) {
			rnd_nums2[ii] = generate.gen_oper(mt_for_longs, key_bytes, max_op);
		}

		keys_arr = new long[arr_sz];
		int[] keys_idxs1 = generate.shuffle_ints(mt_for_choosing, arr_sz);
		assert (keys_idxs1.length == arr_sz);
		for (int kk1 = 0; kk1 < keys_idxs1.length; kk1++) {
			int idx1 = keys_idxs1[kk1];
			keys_arr[kk1] = rnd_nums1[idx1];
		}

		data_arr = new long[arr_sz];
		int[] keys_idxs2 = generate.shuffle_ints(mt_for_choosing, arr_sz);
		assert (keys_idxs2.length == arr_sz);
		for (int kk2 = 0; kk2 < keys_idxs2.length; kk2++) {
			int idx2 = keys_idxs1[kk2];
			data_arr[kk2] = rnd_nums1[idx2];
		}

		encr_arr = new long[arr_sz];
		for (int kk3 = 0; kk3 < encr_arr.length; kk3++) {
			encr_arr[kk3] = bitshaker
					.encrypt_long(data_arr[kk3], keys_arr[kk3]);
			assert (bitshaker.decrypt_long(encr_arr[kk3], keys_arr[kk3]) == data_arr[kk3]);
		}
	}

	public boolean is_equal(signeer sgn) {
		if (sgn.arr_sz != arr_sz) {
			return false;
		}
		boolean eq1 = Arrays.equals(sgn.data_arr, data_arr);
		boolean eq2 = Arrays.equals(sgn.encr_arr, encr_arr);
		return (eq1 && eq2);
	}

	public void set_target_data(byte[] tgt_data) {
		target_bytes = tgt_data;
	}

	public byte[] get_signer_data() {
		assert (data_arr.length == encr_arr.length);
		int f_sz = data_arr.length + encr_arr.length;
		long[] all_nums = new long[f_sz];
		int ii = 0;
		for (int aa = 0; aa < data_arr.length; aa++) {
			all_nums[ii] = data_arr[aa];
			all_nums[ii + 1] = encr_arr[aa];
			ii += 2;
		}
		byte[] sgn_puk = convert.to_byte_array(all_nums);
		return sgn_puk;
	}

	public void set_signer_data(byte[] sgn_puk) {
		long[] all_nums = convert.to_long_array(sgn_puk);

		init_signeer();

		arr_sz = all_nums.length / 2;
		if (arr_sz < MIN_ARR_SZ) {
			arr_sz = 0;
			throw new bad_bitshake(2, L.cannot_set_signer_data);
		}

		data_arr = new long[arr_sz];
		encr_arr = new long[arr_sz];
		int ii = 0;
		for (int aa = 0; aa < all_nums.length; aa += 2) {
			data_arr[ii] = all_nums[aa];
			encr_arr[ii] = all_nums[aa + 1];
			ii++;
		}
	}

	public void read_signer_data(String sgn_nam) {
		init_signeer();
		// byte[] file_bytes = mem_file.read_bytes(new File(sgn_nam));
		byte[] file_bytes = mem_file.concurrent_read_encrypted_bytes(new File(
				sgn_nam), null);
		file_bytes = convert.unframe_bytes(file_bytes, config.SIGNER_ID,
				config.SIGNER_ID);
		file_bytes = convert.hex_bytes_to_bytes(file_bytes);
		if (file_bytes == null) {
			throw new bad_bitshake(2, L.cannot_read_signer_data);
		}
		set_signer_data(file_bytes);
	}

	public void write_signer_data(String sgn_nam) {
		byte[] file_bytes = get_signer_data();
		file_bytes = convert.bytes_to_hex_bytes(file_bytes);
		file_bytes = convert.frame_bytes(file_bytes, config.SIGNER_ID,
				config.SIGNER_ID);
		if (file_bytes == null) {
			throw new bad_bitshake(2, L.cannot_write_signer_data_1);
		}
		mem_file.concurrent_write_encrypted_bytes(new File(sgn_nam), null,
				file_bytes);
	}

	@SuppressWarnings("unused")
	boolean can_check() {
		boolean c1 = (target_bytes != null);
		boolean c2 = (arr_sz >= MIN_ARR_SZ);
		boolean c3 = ((data_arr != null) && (data_arr.length == arr_sz));
		boolean c4 = ((encr_arr != null) && (encr_arr.length == arr_sz));
		boolean ok1 = (c1 && c2 && c3 && c4);
		if (config.DEBUG && !ok1) {
			if (!c1) {
				System.out.println("No target.");
			}
			if (!c2) {
				System.out.println("Array too small.");
			}
			if (!c3) {
				System.out.println("Invalid data array in signer.");
			}
			if (!c4) {
				System.out.println("Invalid encrypted array in signer.");
			}
		}
		return ok1;
	}

	@SuppressWarnings("unused")
	boolean can_sign() {
		boolean c1 = can_check();
		boolean c2 = ((keys_arr != null) && (keys_arr.length == arr_sz));
		boolean ok1 = (c1 && c2);
		if (config.DEBUG && !ok1) {
			if (!c2) {
				System.out.println("Invalid key array for signing.");
			}
		}
		return ok1;
	}

	public static long get_bit_num(long[] nums, long bit_idx, boolean val) {
		assert (bit_idx >= 0);
		assert (bit_idx < (nums.length / 2));
		int k_idx = (int) (bit_idx * 2);
		if (!val) {
			k_idx++;
		}
		long the_num = nums[k_idx];
		return the_num;
	}

	public byte[] get_signature(String orig_sgn_f_nm) {
		signeer sgn2 = new signeer(orig_sgn_f_nm, null);
		return get_signature(sgn2);
	}

	public byte[] get_signature(signeer orig_sgn) {
		if (!is_equal(orig_sgn)) {
			throw new bad_bitshake(2, L.given_signer_is_not_equal);
		}
		if (!can_sign()) {
			throw new bad_bitshake(2,
					L.internal_error_uncomplete_signer_arguments);
		}

		byte[] with_cters = convert.append_counters(target_bytes);
		byte[] sha_bytes = convert.calc_sha_bytes(with_cters);
		long n_sha_bits = bit_array.num_bits(sha_bytes);

		mer_twist mt_for_sha = new mer_twist(sha_bytes);

		long[] signature_arr = new long[arr_sz / 2];
		for (int aa = 0; aa < signature_arr.length; aa++) {
			long bit_idx = (int) (convert.to_interval(mt_for_sha.nextLong(), 0,
					n_sha_bits));
			boolean val = bit_array.get_bit(sha_bytes, bit_idx);
			signature_arr[aa] = get_bit_num(keys_arr, aa, val);
		}

		byte[] signature_bytes = convert.to_byte_array(signature_arr);
		return signature_bytes;
	}

	public boolean check_signature(byte[] signature_bytes) {
		long[] signature_arr = convert.to_long_array(signature_bytes);

		if ((signature_arr.length * 2) != arr_sz) {
			throw new bad_bitshake(2, L.invalid_signature_size);
		}
		if (!can_check()) {
			throw new bad_bitshake(2, L.internal_error_cannot_check);
		}

		byte[] with_cters = convert.append_counters(target_bytes);
		byte[] sha_bytes = convert.calc_sha_bytes(with_cters);
		long n_sha_bits = bit_array.num_bits(sha_bytes);

		mer_twist mt_for_sha = new mer_twist(sha_bytes);

		boolean ck_ok = true;
		for (int aa = 0; aa < signature_arr.length; aa++) {
			long bit_idx = (int) (convert.to_interval(mt_for_sha.nextLong(), 0,
					n_sha_bits));
			boolean val = bit_array.get_bit(sha_bytes, bit_idx);

			long dat1_num = get_bit_num(data_arr, aa, val);
			long enc1_num = get_bit_num(encr_arr, aa, val);
			long key1_num = signature_arr[aa];

			long dat2_num = bitshaker.decrypt_long(enc1_num, key1_num);
			long enc2_num = bitshaker.encrypt_long(dat1_num, key1_num);

			if (dat1_num != dat2_num) {
				ck_ok = false;
				break;
			}
			if (enc1_num != enc2_num) {
				ck_ok = false;
				break;
			}
		}

		return ck_ok;
	}

	public void print_arrays() {
		if (!config.DEBUG) {
			return;
		}
		if (keys_arr != null) {
			System.out.println("keys_arr=");
			System.out.println(Arrays.toString(keys_arr));
		}
		if (data_arr != null) {
			System.out.println("data_arr=");
			System.out.println(Arrays.toString(data_arr));
		}
		if (encr_arr != null) {
			System.out.println("encr_arr=");
			System.out.println(Arrays.toString(encr_arr));
		}
	}

}
