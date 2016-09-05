package emetcode.crypto.bitshake.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import emetcode.crypto.bitshake.locale.L;
import emetcode.util.devel.bad_emetcode;
import emetcode.util.devel.logger;

public class convert {
	
	private static final boolean IN_DEBUG_1 = true; //unfrm

	public static long to_interval(long val, long min, long max) {
		long diff = max - min;
		if (diff <= 0) {
			return min;
		}
		long rr = (val % diff);
		if (rr < 0) {
			rr = -rr;
		}
		long resp = min + rr;
		assert (resp >= min);
		assert (resp < max);
		return resp;
	}

	public static byte[] to_byte_array(long the_num) {
		byte[] byte_arr = ByteBuffer.allocate(8).putLong(the_num).array();
		return byte_arr;
	}

	public static long to_long(byte[] byte_arr) {
		assert (byte_arr != null);
		long the_num = ByteBuffer.wrap(byte_arr).getLong();
		return the_num;
	}

	public static byte[] to_byte_array(long[] data) {
		assert (data != null);
		byte[] byte_arr = new byte[data.length * 8];
		ByteBuffer buff = ByteBuffer.wrap(byte_arr);
		buff.clear();
		for (int ii = 0; ii < data.length; ii++) {
			buff.putLong(data[ii]);
		}
		return byte_arr;
	}

	public static long[] to_long_array(byte[] byte_arr) {
		assert (byte_arr != null);
		long[] data = new long[byte_arr.length / 8];
		ByteBuffer buff = ByteBuffer.wrap(byte_arr);
		buff.clear();
		for (int ii = 0; ii < data.length; ii++) {
			data[ii] = buff.getLong();
		}
		return data;
	}

	public static byte[] to_byte_array(int the_num) {
		byte[] byte_arr = ByteBuffer.allocate(4).putInt(the_num).array();
		return byte_arr;
	}

	public static int to_int(byte[] byte_arr) {
		assert (byte_arr != null);
		int the_num = ByteBuffer.wrap(byte_arr).getInt();
		return the_num;
	}

	public static byte[] to_byte_array(int[] data) {
		assert (data != null);
		byte[] byte_arr = new byte[data.length * 4];
		ByteBuffer buff = ByteBuffer.wrap(byte_arr);
		buff.clear();
		for (int ii = 0; ii < data.length; ii++) {
			buff.putInt(data[ii]);
		}
		return byte_arr;
	}

	public static int[] to_int_array(byte[] byte_arr) {
		assert (byte_arr != null);
		int[] data = new int[byte_arr.length / 4];
		ByteBuffer buff = ByteBuffer.wrap(byte_arr);
		buff.clear();
		for (int ii = 0; ii < data.length; ii++) {
			data[ii] = buff.getInt();
		}
		return data;
	}

	public static String bytes_to_hex_frm_string(byte[] the_bytes, String frm) {
		assert (the_bytes != null);
		byte[] hex_frm_bts = bytes_to_hex_frm_bytes(the_bytes, frm);
		String the_str = new String(hex_frm_bts, config.UTF_8);
		return the_str;
	}

	public static byte[] bytes_to_hex_frm_bytes(byte[] the_bytes, String frm) {
		assert (the_bytes != null);
		byte[] hex_bts = bytes_to_hex_bytes(the_bytes);
		hex_bts = frame_bytes(hex_bts, frm, frm);
		return hex_bts;
	}

	public static String bytes_to_hex_string(byte[] the_bytes) {
		assert (the_bytes != null);
		byte[] hex_bts = bytes_to_hex_bytes(the_bytes);
		String hex_str = new String(hex_bts, config.UTF_8);
		return hex_str;
	}

	public static byte[] bytes_to_hex_bytes(byte[] the_bytes) {
		assert (the_bytes != null);
		int hx_sz = the_bytes.length * 2;
		byte[] hx_bytes = new byte[hx_sz];
		for (int ii = 0; ii < the_bytes.length; ii++) {
			String hx_str = String.format("%02x", the_bytes[ii]);
			byte[] hx_val = hx_str.getBytes();
			assert (hx_val.length == 2);
			hx_bytes[ii * 2] = hx_val[0];
			hx_bytes[(ii * 2) + 1] = hx_val[1];
		}
		return hx_bytes;
	}

	public static byte[] hex_frm_string_to_bytes(String hx_fr_str, String frm) {
		byte[] hex_frm_bts = hx_fr_str.getBytes(config.UTF_8);
		hex_frm_bts = hex_frm_bytes_to_bytes(hex_frm_bts, frm);
		return hex_frm_bts;
	}

	public static byte[] hex_frm_bytes_to_bytes(byte[] hex_bts, String frm) {
		assert (hex_bts != null);
		byte[] the_bts = unframe_bytes(hex_bts, frm, frm);
		the_bts = hex_bytes_to_bytes(the_bts);
		return the_bts;
	}

	public static byte[] hex_string_to_bytes(String hex_str) {
		byte[] hex_bts = hex_str.getBytes(config.UTF_8);
		hex_bts = hex_bytes_to_bytes(hex_bts);
		return hex_bts;
	}

	public static byte[] hex_bytes_to_bytes(byte[] the_hx_bytes) {
		assert (the_hx_bytes != null);
		if ((the_hx_bytes.length % 2) != 0) {
			throw new bad_emetcode(2, L.invalid_length);
		}
		int bytes_sz = the_hx_bytes.length / 2;
		byte[] the_bytes = new byte[bytes_sz];
		for (int ii = 0; ii < the_bytes.length; ii++) {
			byte b1 = the_hx_bytes[ii * 2];
			byte b2 = the_hx_bytes[(ii * 2) + 1];
			the_bytes[ii] = calc_val_byte(b1, b2);
		}
		return the_bytes;
	}

	public static String hex_string_to_string(String hex_str) {
		byte[] hex_bts = hex_string_to_bytes(hex_str);
		String out_str = bytes_to_str(hex_bts);
		return out_str;
	}

	public static String string_to_hex_string(String in_str) {
		byte[] in_bts = str_to_bytes(in_str);
		String hex_str = bytes_to_hex_string(in_bts);
		return hex_str;
	}

	public static byte calc_val_byte(byte upper, byte lower) {
		byte bb = (byte) (get_byte_val(upper, true) | get_byte_val(lower, false));
		return bb;
	}

	public static byte get_byte_val(byte b_exa, boolean upper) {
		byte val = 0;
		switch (b_exa) {
		case '0':
			val = 0;
			break;
		case '1':
			val = 1;
			break;
		case '2':
			val = 2;
			break;
		case '3':
			val = 3;
			break;
		case '4':
			val = 4;
			break;
		case '5':
			val = 5;
			break;
		case '6':
			val = 6;
			break;
		case '7':
			val = 7;
			break;
		case '8':
			val = 8;
			break;
		case '9':
			val = 9;
			break;
		case 'A':
			val = 10;
			break;
		case 'a':
			val = 10;
			break;
		case 'B':
			val = 11;
			break;
		case 'b':
			val = 11;
			break;
		case 'C':
			val = 12;
			break;
		case 'c':
			val = 12;
			break;
		case 'D':
			val = 13;
			break;
		case 'd':
			val = 13;
			break;
		case 'E':
			val = 14;
			break;
		case 'e':
			val = 14;
			break;
		case 'F':
			val = 15;
			break;
		case 'f':
			val = 15;
			break;
		}
		if (upper) {
			val = (byte) (val << (byte) 4);
		}
		return val;
	}

	public static String frame_sha(String sha_str) {
		return frame_string(sha_str, config.SHA_ID);
	}

	public static String unframe_sha(String sha_str) {
		return unframe_string(sha_str, config.SHA_ID);
	}

	public static String frame_string(String a_str, String frm) {
		return new String(frame_bytes(a_str.getBytes(config.UTF_8), frm, frm),
				config.UTF_8);
	}

	public static byte[] frame_bytes(byte[] arr_bytes, String pre, String suf) {
		assert (arr_bytes != null);
		if (pre.indexOf(config.DOT_BYTE) != -1) {
			throw new bad_emetcode(2, L.invalid_format_no_dots);
		}
		if (suf.indexOf(config.DOT_BYTE) != -1) {
			throw new bad_emetcode(2, L.invalid_format_no_dots);
		}

		byte[] pre_bytes = pre.getBytes(config.UTF_8);
		byte[] suf_bytes = suf.getBytes(config.UTF_8);

		int lng = arr_bytes.length + pre_bytes.length + suf_bytes.length + 2;
		byte[] fr_bytes = new byte[lng];

		ByteBuffer buff = ByteBuffer.wrap(fr_bytes);
		buff.put(pre_bytes);
		buff.put(config.DOT_BYTE);
		buff.put(arr_bytes);
		buff.put(config.DOT_BYTE);
		buff.put(suf_bytes);

		return fr_bytes;
	}

	public static String unframe_string(String a_str, String frm) {
		return new String(
				unframe_bytes(a_str.getBytes(config.UTF_8), frm, frm),
				config.UTF_8);
	}

	public static byte[] unframe_bytes(byte[] fr_bytes, String pre, String suf) {
		assert (fr_bytes != null);
		if (pre.indexOf(config.DOT_BYTE) != -1) {
			throw new bad_emetcode(2, L.invalid_format_no_dots);
		}
		if (suf.indexOf(config.DOT_BYTE) != -1) {
			throw new bad_emetcode(2, L.invalid_format_no_dots);
		}

		byte[] orig_pre_bytes = pre.getBytes();
		byte[] orig_suf_bytes = suf.getBytes();
		byte[] pre_bytes = new byte[orig_pre_bytes.length];
		byte[] suf_bytes = new byte[orig_suf_bytes.length];

		int lng = fr_bytes.length - (pre_bytes.length + suf_bytes.length + 2);
		byte[] arr_bytes = new byte[lng];

		ByteBuffer buff = ByteBuffer.wrap(fr_bytes);
		buff.get(pre_bytes);
		byte dot_ch1 = buff.get();
		buff.get(arr_bytes);
		byte dot_ch2 = buff.get();
		buff.get(suf_bytes);

		if (dot_ch1 != config.DOT_BYTE) {
			if(IN_DEBUG_1){
				logger.info("NO_DOTS_ERROR in the_fr_by=" + new String(fr_bytes, config.UTF_8));
			}
			throw new bad_emetcode(2, L.invalid_format_no_dots);
		}
		if (dot_ch2 != config.DOT_BYTE) {
			throw new bad_emetcode(2, L.invalid_format_no_dots);
		}
		if (!Arrays.equals(pre_bytes, orig_pre_bytes)) {
			throw new bad_emetcode(2, L.invalid_format_no_prefix);
		}
		if (!Arrays.equals(suf_bytes, orig_suf_bytes)) {
			throw new bad_emetcode(2, L.invalid_format_no_sufix);
		}

		return arr_bytes;
	}

	public static void add_byte(byte the_byte, long[] cters) {
		assert (cters.length == 16);
		assert (the_byte >= 0);
		assert (the_byte < 16);
		switch (the_byte) {
		case 0:
			cters[0]++;
			break;
		case 1:
			cters[1]++;
			break;
		case 2:
			cters[2]++;
			break;
		case 3:
			cters[3]++;
			break;
		case 4:
			cters[4]++;
			break;
		case 5:
			cters[5]++;
			break;
		case 6:
			cters[6]++;
			break;
		case 7:
			cters[7]++;
			break;
		case 8:
			cters[8]++;
			break;
		case 9:
			cters[9]++;
			break;
		case 10:
			cters[10]++;
			break;
		case 11:
			cters[11]++;
			break;
		case 12:
			cters[12]++;
			break;
		case 13:
			cters[13]++;
			break;
		case 14:
			cters[14]++;
			break;
		case 15:
			cters[15]++;
			break;
		}
	}

	public static byte lower_val(byte byte_val) {
		return (byte) (byte_val & (byte) (0x0F));
	}

	public static byte higher_val(byte byte_val) {
		return (byte) (((byte) (byte_val >>> 4)) & ((byte) (0x0F)));
	}

	public static byte[] append_bytes(byte[] arr_bytes, byte[] suf_bytes) {
		assert (arr_bytes != null);
		assert (suf_bytes != null);
		int lng = arr_bytes.length + suf_bytes.length;
		byte[] fr_bytes = new byte[lng];

		ByteBuffer buff = ByteBuffer.wrap(fr_bytes);
		buff.put(arr_bytes);
		buff.put(suf_bytes);

		return fr_bytes;
	}

	public static byte[] append_counters(byte[] arr_bytes) {
		assert (arr_bytes != null);
		long[] cters = count_byte_parts(arr_bytes);
		byte[] as_bts = to_byte_array(cters);
		byte[] with_cters = append_bytes(arr_bytes, as_bts);
		return with_cters;
	}

	public static void add_byte_parts(byte[] byte_arr, long[] cters) {
		assert (byte_arr != null);
		assert (cters != null);
		assert (cters.length == 16);
		for (int aa = 0; aa < byte_arr.length; aa++) {
			byte bt1 = byte_arr[aa];
			byte hi = higher_val(bt1);
			byte lo = lower_val(bt1);
			add_byte(hi, cters);
			add_byte(lo, cters);
		}
	}

	public static long[] count_byte_parts(byte[] byte_arr) {
		assert (byte_arr != null);

		long[] cters = new long[16];
		Arrays.fill(cters, 0);
		add_byte_parts(byte_arr, cters);
		return cters;
	}

	private static MessageDigest get_sha_256_calculator() {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance(config.SHA_256_STR);
		} catch (NoSuchAlgorithmException ex1) {
			throw new bad_emetcode(2);
		}
		return md;
	}
	
	public static int calc_minisha_int(byte[] sha_bts) {
		byte[] the_bits = calc_mini_sha_arr(sha_bts, Integer.SIZE);
		return convert.to_int(the_bits);
	}

	public static long calc_minisha_long(byte[] sha_bts) {
		byte[] the_bits = calc_mini_sha_arr(sha_bts, Long.SIZE);
		return convert.to_long(the_bits);
	}

	public static byte[] calc_mini_sha_arr(byte[] sha_bts, int num_sel_bits) {
		mer_twist mt_for_sha = new mer_twist(sha_bts);

		int n_sha_bits = sha_bts.length * Byte.SIZE;
		byte[] sel_bits = new byte[num_sel_bits / Byte.SIZE];

		for (int aa = 0; aa < num_sel_bits; aa++) {
			long bit_idx = (int) (convert.to_interval(mt_for_sha.nextLong(), 0,
					n_sha_bits));
			boolean val = bit_array.get_bit(sha_bts, bit_idx);
			bit_array.set_bit(sel_bits, aa, val);
		}
		return sel_bits;
	}

	public static byte[] calc_sha_bytes(byte[] data) {
		MessageDigest md = get_sha_256_calculator();
		if (md == null) {
			throw new bad_emetcode(2);
		}
		md.reset();
		byte[] sha_bytes = md.digest(data);
		return sha_bytes;
	}

	public static String calc_sha_str(byte[] data) {
		String sha_str = bytes_to_hex_string(calc_sha_bytes(data));
		return sha_str;
	}

	public static String calc_sha_text(byte[] target_data) {
		byte[] sha_bts = calc_sha_bytes(target_data);
		String sha_tgt = bytes_to_hex_frm_string(sha_bts, config.SHA_ID);
		return sha_tgt;
	}

	public static int cmp_int(int ii1, int ii2) {
		if (ii1 < ii2) {
			return -1;
		}
		if (ii1 > ii2) {
			return 1;
		}
		return 0;
	}

	public static int cmp_long(long ll1, long ll2) {
		if (ll1 < ll2) {
			return -1;
		}
		if (ll1 > ll2) {
			return 1;
		}
		return 0;
	}

	public static String bytes_to_str(byte[] kk) {
		return new String(kk, config.UTF_8);
	}

	public static byte[] str_to_bytes(String in_str) {
		return in_str.getBytes(config.UTF_8);
	}

	public static List<String> bytes_to_string_list(byte[] arr) {
		if(arr == null){
			return null;
		}
		List<String> the_lst = new ArrayList<String>();
		
		ByteArrayInputStream bs = new ByteArrayInputStream(arr);
		BufferedReader lr = new BufferedReader(new InputStreamReader(bs,
				config.UTF_8));
		try {
			String the_line = null;
			while ((the_line = lr.readLine()) != null) {
				the_lst.add(the_line);
			}
		} catch (IOException ex) {
			throw new bad_emetcode(2, ex.toString());
		}
		return the_lst;
	}

	public static byte[] string_list_to_bytes(List<String> lst) {
		if(lst == null){
			return null;
		}
		
		ByteArrayOutputStream stm = new ByteArrayOutputStream();
		OutputStreamWriter ow = new OutputStreamWriter(stm, config.UTF_8);
		BufferedWriter bw = new BufferedWriter(ow);

		for (String str : lst) {
			try {
				bw.write(str);
				bw.write('\n');
			} catch (IOException ex1) {
				throw new bad_emetcode(2, ex1.toString());
			}
		}

		try {
			bw.close();
		} catch (IOException ex2) {
			throw new bad_emetcode(2, ex2.toString());
		}
		byte[] data = stm.toByteArray();
		return data;
	}

	public static long parse_long(String val_str) {
		try {
			return Long.parseLong(val_str);
		} catch (NumberFormatException ex1) {
			throw new bad_emetcode(2, String.format(L.cannot_parse_long,
					val_str));
		}
	}

	public static long try_parse_long(String val_str) {
		try {
			return Long.parseLong(val_str);
		} catch (NumberFormatException ex1) {
			return 0;
		}
	}

	public static int parse_int(String val_str) {
		try {
			return Integer.parseInt(val_str);
		} catch (NumberFormatException ex1) {
			throw new bad_emetcode(2,
					String.format(L.cannot_parse_int, val_str));
		}
	}

	public static boolean is_valid_idx(String str, int idx){
		if(idx < 0){ return false; }
		if(idx >= str.length()){ return false; }
		return true;
	}

	public static boolean is_valid_idx(Collection<?> cc, int idx){
		if(idx < 0){ return false; }
		if(idx >= cc.size()){ return false; }
		return true;
	}
	
	public static String utc_to_string(long utc_millis) {
		String DATEFORMAT = "yyyy-MM-dd zzz HH:mm:ss.SSS";
		SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

		String utc_str = sdf.format(new Date(utc_millis));
		return utc_str;
	}

	public static List<String> diff_string_lists(List<String> to_filter,
			List<String> the_filter) {
		if(the_filter == null){
			return to_filter;
		}
		Set<String> s_lst1 = new TreeSet<String>(to_filter);
		for (String ff : the_filter) {
			if (s_lst1.contains(ff)) {
				s_lst1.remove(ff);
			}
		}
		List<String> diff = new ArrayList<String>(s_lst1);
		return diff;
	}

}
