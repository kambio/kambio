package emetcode.crypto.bitshake.utils;

import java.io.File;
import java.io.IOError;
import java.util.Arrays;

import emetcode.crypto.bitshake.generate;

public class console_get_key {

	public static final int MINIMUM_KEY_SIZE = 4;

	public static byte[] get_key(boolean confirm, boolean with_kf) {
		System.out.println("key:");
		byte[] the_key = ask_key(with_kf);
		if (the_key == null) {
			return null;
		}

		int min_sz = MINIMUM_KEY_SIZE;
		if (the_key.length < min_sz) {
			System.out.println("Minimum key size is " + min_sz + '.');
			return null;
		}

		if (confirm) {
			System.out.println("confirm key:");
			byte[] tmp_key = ask_key(with_kf);
			if (tmp_key == null) {
				return null;
			}

			if (!Arrays.equals(the_key, tmp_key)) {
				System.out.println("key confirmation failed.");
				Arrays.fill(the_key, (byte) 0);
				Arrays.fill(tmp_key, (byte) 0);
				the_key = null;
				tmp_key = null;
				return null;
			}
			Arrays.fill(tmp_key, (byte) 0);
		}

		// logger.debug(Arrays.toString(the_key));
		return the_key;
	}

	private static byte[] get_secret() {
		try {
			String str1 = new String(System.console().readPassword());
			byte[] bts = str1.getBytes(config.UTF_8);
			// logger.debug(Arrays.toString(bts));
			return bts;
		} catch (IOError e) {
		}
		return null;
	}

	private static boolean is_empty_chars(byte[] chs) {
		return (chs.length == 0);
	}

	private static byte[] safe_append_bytes(byte[] src1, byte[] src2) {
		int n_sz = 0;
		if (src1 != null) {
			n_sz += src1.length;
		}
		if (src2 != null) {
			n_sz += src2.length;
		}
		byte[] dest = new byte[n_sz];
		int aa = 0;
		if (src1 != null) {
			for (aa = 0; aa < src1.length; aa++) {
				dest[aa] = src1[aa];
			}
			Arrays.fill(src1, (byte) 0);
		}
		if (src2 != null) {
			for (int bb = 0; bb < src2.length; bb++) {
				dest[aa++] = src2[bb];
			}
			Arrays.fill(src2, (byte) 0);
		}
		return dest;
	}

	private static byte[] ask_key(boolean with_kf) {
		// Console cons;
		byte[] key_arr = null;
		System.out.print(">");
		byte[] tmp_str = get_secret();
		if (tmp_str == null) {
			return null;
		}

		byte[] file_bytes = null;
		if (with_kf) {
			String f_nm = new String(tmp_str);
			// file_bytes = mem_file.read_bytes(new File(f_nm));
			file_bytes = mem_file.concurrent_read_encrypted_bytes(
					new File(f_nm), null);
			if (file_bytes == null) {
				System.out.println("Invalid key file.");
				return null;
			} else {
				System.out.println("USING KEY BASE FILE");
			}
		}

		while (!is_empty_chars(tmp_str)) {
			key_arr = safe_append_bytes(key_arr, tmp_str);
			System.out.print(">");
			tmp_str = get_secret();
			if (tmp_str == null) {
				return null;
			}
		}

		if (file_bytes != null) {
			mer_twist mt1 = new mer_twist(key_arr);
			generate.shuffle_bits_with(file_bytes, mt1);
			key_arr = file_bytes;
		}

		if (file_bytes != null) {
			Arrays.fill(file_bytes, (byte) 0);
			file_bytes = null;
		}

		System.out.println();
		return key_arr;
	}

}
