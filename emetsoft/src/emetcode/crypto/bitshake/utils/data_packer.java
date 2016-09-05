package emetcode.crypto.bitshake.utils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import emetcode.crypto.bitshake.bad_bitshake;
import emetcode.net.netmix.config;

public class data_packer {
	
	private static final int INT_NUM_BY = Integer.SIZE / Byte.SIZE;
	
	public static byte[] pack_two(byte[] arr_one, byte[] arr_two) {
		if (arr_one == null) {
			throw new bad_bitshake(2);
		}
		if (arr_two == null) {
			throw new bad_bitshake(2);
		}
		List<byte[]> all_dat_1 = new ArrayList<byte[]>(2);
		all_dat_1.add(arr_one);
		all_dat_1.add(arr_two);
		return pack_list(all_dat_1);
	}

	public static byte[] pack_enc(byte[] enc, byte[] sha) {
		return pack_two(enc, sha);
	}
	
	public static List<byte[]> unpack_enc(byte[] data) {
		return unpack_list(data);
	}

	private static int calc_list_full_size(List<byte[]> arrs) {
		int full_sz = 0;
		for (byte[] elem : arrs) {
			if (elem == null) {
				throw new bad_bitshake(2);
			}
			full_sz += elem.length;
			if (full_sz < 0) {
				throw new bad_bitshake(2);
			}
		}
		return full_sz;
	}

	public static byte[] pack_list(List<byte[]> arrs) {
		if (arrs == null) {
			throw new bad_bitshake(2);
		}

		int num_bts_ints = ((arrs.size() + 1) * INT_NUM_BY);
		int full_sz = num_bts_ints + calc_list_full_size(arrs);

		byte[] data_bytes = new byte[full_sz];
		ByteBuffer buff = ByteBuffer.wrap(data_bytes);

		buff.putInt(arrs.size());
		for (byte[] elem : arrs) {
			if (elem == null) {
				throw new bad_bitshake(2);
			}
			buff.putInt(elem.length);
			buff.put(elem);
		}

		return data_bytes;
	}

	private static void check_array_size(int arr_sz, int rem_sz) {
		if (arr_sz < 0) {
			throw new bad_bitshake(2, "arr_sz=" + arr_sz);
		}
		if (arr_sz > rem_sz) {
			throw new bad_bitshake(2, "arr_sz=" + arr_sz + "_rem_sz=" + rem_sz);
		}
	}

	public static List<byte[]> unpack_list(byte[] data) {
		if (data == null) {
			throw new bad_bitshake(2);
		}

		ByteBuffer buff = ByteBuffer.wrap(data);
		List<byte[]> the_list = new ArrayList<byte[]>(2);

		int lst_sz = buff.getInt();
		for (int aa = 0; aa < lst_sz; aa++) {
			int arr_sz = buff.getInt();
			check_array_size(arr_sz, buff.remaining());
			byte[] arr = new byte[arr_sz];
			buff.get(arr);
			the_list.add(arr);
		}
		if (buff.remaining() != 0) {
			throw new bad_bitshake(2);
		}

		return the_list;
	}

	public static byte[] get_bytes(ByteBuffer dat) {
		int num_by = dat.getInt();
//		if (num_by > MAX_SEC_DATA_NUM_BY) {
//			throw new bad_netmix(2);
//		}
		if (num_by < 0) {
			return null;
		}
		byte[] the_bytes = new byte[num_by];
		dat.get(the_bytes);
		return the_bytes;
	}

	public static String get_str(ByteBuffer dat) {
		byte[] str_bytes = get_bytes(dat);
		if (str_bytes == null) {
			return null;
		}
		String the_str = new String(str_bytes, config.UTF_8);
		return the_str;
	}

	public static void put_bytes(ByteBuffer dat, byte[] the_bytes) {
		if (the_bytes == null) {
			dat.putInt(-1);
			return;
		}
//		if (the_bytes.length > MAX_SEC_DATA_NUM_BY) {
//			throw new bad_netmix(2);
//		}
		dat.putInt(the_bytes.length);
		dat.put(the_bytes);
	}

	public static void put_str(ByteBuffer dat, String the_str) {
		if (the_str == null) {
			dat.putInt(-1);
			return;
		}
		byte[] str_bytes = the_str.getBytes(config.UTF_8);
		put_bytes(dat, str_bytes);
	}

	
}
