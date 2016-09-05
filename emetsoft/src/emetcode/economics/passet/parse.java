package emetcode.economics.passet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.SortedSet;
import java.util.TreeSet;

import emetcode.crypto.bitshake.utils.convert;
import emetcode.crypto.bitshake.utils.file_funcs;
import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.crypto.bitshake.utils.mem_file;
import emetcode.economics.passet.locale.L;

public class parse {

	private static final char PRE_SUBTITLE_CHAR = '<';
	private static final char SUF_SUBTITLE_CHAR = '>';
	private static final char PIPE_CHAR = '|';
	//private static final char SLA_CHAR = '/';
	private static final char EOL_CHAR = '\n';
	private static final char RET_CHAR = '\r';
	private static final char SPC_CHAR = ' ';
	private static final char EQU_CHAR = '=';
	//private static final char USC_CHAR = '_';

	private static final String CONCAT_STR = "||";

	
	public static final String SHA_ID = emetcode.crypto.bitshake.utils.config.SHA_ID;

	public static final String[] HEADER = {
			"YASHUA--ישוע--YASHUA--ישוע--YASHUA--ישוע--YASHUA--ישוע--YASHUA--ישוע--YASHU",
			"This is a program generated file. Do not edit it. Doing so will corrupt it.",
			"",
			"PASSET.",
			"Unconditionally secure cryptographic text file to pass ownership of an ",
			"asset.",
			"",
			"PASSOFT.",
			"The open source software, designed to securely handle this document, ",
			"identified under title 'Prominote hash number' by the secure hash number ",
			"of the file containing all source code, all terms of use, and signing ",
			"data of said software. Said hash number securely signed with the digital ",
			"signature written below under title 'Prominote signature' using said ",
			"signing data.", "" };

	public static final String START_OF_TRANSFER_TITLE = "START_OF_TRANSFER";
	private static final String[] START_OF_TRANSFER = {
			START_OF_TRANSFER_TITLE, "" };

	public static final String START_OF_DEMAND_TITLE = "START_OF_DEMAND";
	private static final String[] START_OF_DEMAND = { START_OF_DEMAND_TITLE, "" };

	private static final String EOSEC = "END_OF_SECTION_*_";
	private static final String END_OF_SECTION_TITLE = EOSEC + EOSEC + EOSEC + EOSEC; 
	private static final String[] END_OF_SECTION_LINES = { END_OF_SECTION_TITLE, "",
			"", "", "", "", "", "", "", "" };

	// private methods

	private static void write_line(BufferedWriter bw1, String line) {
		check_line(line);
		try {
			int max_sz = config.MAX_PASSET_LINE_SZ;
			int off = 0;
			int len = line.length();
			while ((len - off) > max_sz) {
				bw1.write(line, off, max_sz);
				off += max_sz;

				bw1.write(PIPE_CHAR);
				bw1.write(PIPE_CHAR);
				bw1.newLine();
			}
			bw1.write(line, off, (len - off));
			bw1.write(PIPE_CHAR);
			bw1.newLine();
		} catch (IOException ex1) {
			throw new bad_passet(2, ex1.toString());
		}
	}

	private static void write_line_list(BufferedWriter bw1,
			List<String> all_lines) {
		try {
			for (String ln : all_lines) {
				write_line(bw1, ln);
			}
			bw1.flush();
		} catch (IOException ex1) {
			throw new bad_passet(2, ex1.toString());
		}
	}

	public static void print_lines(PrintStream os, List<String> all_lines) {
		OutputStreamWriter ow = new OutputStreamWriter(os, config.UTF_8);
		BufferedWriter bw = new BufferedWriter(ow, config.AVERAGE_BUFF_SZ);

		write_line_list(bw, all_lines);
	}

	public static void print_line_list(List<String> all_lines) {
		print_lines(System.out, all_lines);
	}

	private static String read_line(BufferedReader br1) {
		try {
			int max_sz = config.MAX_PASSET_LINE_SZ;
			StringBuilder full_ln = new StringBuilder(max_sz);
			String nxt_ln = null;
			while (((nxt_ln = br1.readLine()) != null)
					&& nxt_ln.endsWith(CONCAT_STR)) {
				full_ln.append(chop_line(nxt_ln, 2));
			}

			if (nxt_ln == null) {
				return null;
			}

			if (!is_valid_line(nxt_ln)) {
				throw new bad_passet(2, String.format(L.parse_error, nxt_ln));
			}
			full_ln.append(chop_line(nxt_ln, 1));

			String output = full_ln.toString();
			check_line(output);

			return output;
		} catch (IOException ex1) {
			throw new bad_passet(2, ex1.toString());
		}
	}

	private static List<String> read_all_lines(BufferedReader br1) {
		List<String> all_lines = new ArrayList<String>();
		String ln = null;
		while ((ln = read_line(br1)) != null) {
			all_lines.add(ln);
		}
		return all_lines;
	}

	public static List<String> read_byte_array_lines(byte[] data) {
		if (data == null) {
			return new ArrayList<String>();
		}
		if (data.length == 0) {
			return new ArrayList<String>();
		}

		InputStream stm = new ByteArrayInputStream(data);
		InputStreamReader ir = new InputStreamReader(stm, config.UTF_8);
		BufferedReader br = new BufferedReader(ir, config.AVERAGE_BUFF_SZ);

		List<String> all_lines = read_all_lines(br);

		try {
			br.close();
		} catch (IOException ex1) {
			throw new bad_passet(2, ex1.toString());
		}
		return all_lines;
	}

	public static byte[] write_byte_array_lines(List<String> all_lines) {
		ByteArrayOutputStream stm = new ByteArrayOutputStream();
		OutputStreamWriter ow = new OutputStreamWriter(stm, config.UTF_8);
		BufferedWriter bw = new BufferedWriter(ow, config.AVERAGE_BUFF_SZ);

		parse.write_line_list(bw, all_lines);

		try {
			bw.close();
		} catch (IOException ex1) {
			throw new bad_passet(2, ex1.toString());
		}
		byte[] data = stm.toByteArray();
		return data;
	}

	private static String chop_line(String ln, int num_chop) {
		return ln.substring(0, ln.length() - num_chop);
	}

	// public methods

	public static boolean has_title(List<String> t_lines, String title) {
		if (t_lines == null) {
			return false;
		}
		if (t_lines.size() == 0) {
			return false;
		}
		if (!t_lines.get(0).equals(title)) {
			return false;
		}
		return true;
	}

	public static boolean is_valid_line(String ln) {
		int len = ln.length();
		if (len < 1) {
			return false;
		}
		if (ln.charAt(len - 1) != PIPE_CHAR) {
			return false;
		}
		return true;
	}

	public static boolean is_subtitle(String title) {
		int len = title.length();
		if (len <= 2) {
			return false;
		}
		if (title.charAt(0) != PRE_SUBTITLE_CHAR) {
			return false;
		}
		if (title.charAt(len - 1) != SUF_SUBTITLE_CHAR) {
			return false;
		}
		return true;
	}

	public static String as_sha_string(byte[] sha_bts) {
		return convert.bytes_to_hex_frm_string(sha_bts, SHA_ID);
	}

	public static List<String> to_list(String[] arr_str) {
		List<String> lst = new ArrayList<String>(Arrays.asList(arr_str));
		return lst;
	}

	public static List<String> get_header() {
		return to_list(HEADER);
	}

	public static List<String> get_tail() {
		return to_list(END_OF_SECTION_LINES);
	}

	public static List<String> get_start_transfer() {
		return to_list(START_OF_TRANSFER);
	}

	// public static List<String> get_start_ack() {
	// return to_list(START_OF_ACKNOWLEDGMENT);
	// }

	public static List<String> get_start_demand() {
		return to_list(START_OF_DEMAND);
	}

	public static List<String> get_end_section() {
		return to_list(END_OF_SECTION_LINES);
	}

	public static void check_line(String line) {
		if (line.indexOf(PIPE_CHAR) != -1) {
			throw new bad_passet(2, String.format(L.parse_error, line));
		}
		if (line.indexOf(EOL_CHAR) != -1) {
			throw new bad_passet(2, String.format(L.parse_error, line));
		}
		if (line.indexOf(RET_CHAR) != -1) {
			throw new bad_passet(2, String.format(L.parse_error, line));
		}
	}

	public static void check_line_list(List<String> all_lines) {
		for (String ln : all_lines) {
			check_line(ln);
		}
	}

	public static long[] get_new_counters() {
		long[] cters = new long[16];
		Arrays.fill(cters, 0);
		return cters;
	}

	private static void update_sha(MessageDigest dgst, long[] cters,
			List<String> lst, int from_idx, int len) {
		try {
			ListIterator<String> it = lst.listIterator(from_idx);
			int ii = 0;
			for (ii = 0; ii < len; ii++) {
				if (!it.hasNext()) {
					throw new bad_passet(2);
				}
				String ln = it.next();
				byte[] bts = ln.getBytes(config.UTF_8);
				dgst.update(bts);
				if (cters != null) {
					convert.add_byte_parts(bts, cters);
				}
			}
		} catch (IndexOutOfBoundsException ex1) {
			throw new bad_passet(2, ex1.toString());
		}
	}

	private static void update_sha(MessageDigest dgst, long[] cters,
			List<String> all_lines) {
		for (String ln : all_lines) {
			byte[] bts = ln.getBytes(config.UTF_8);
			dgst.update(bts);
			if (cters != null) {
				convert.add_byte_parts(bts, cters);
			}
		}
	}

	private static byte[] get_digest_sha(MessageDigest dgst, long[] cters) {
		if (cters != null) {
			byte[] as_bts = convert.to_byte_array(cters);
			dgst.update(as_bts);
		}
		byte[] sha_bts = dgst.digest();
		return sha_bts;
	}

	public static String calc_txt_sha_of_lines(List<String> all_lines) {
		byte[] sha_bts = calc_sha_lines(all_lines);
		String sha_tgt = convert
				.bytes_to_hex_frm_string(sha_bts, config.SHA_ID);
		return sha_tgt;
	}

	private static MessageDigest get_sha_512_calculator() {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance(config.SHA_512_STR);
		} catch (NoSuchAlgorithmException ex1) {
			throw new bad_passet(2, ex1.toString());
		}
		return md;
	}

	static byte[] calc_sha_lines(List<String> all_lines) {
		MessageDigest dgst = get_sha_512_calculator();
		long[] cters = get_new_counters();
		dgst.reset();
		update_sha(dgst, cters, all_lines);
		byte[] sha_bts = get_digest_sha(dgst, cters);
		return sha_bts;
	}

	static byte[] calc_sha_lines(List<String> all_lines, int from_idx, int len) {
		MessageDigest dgst = get_sha_512_calculator();
		long[] cters = get_new_counters();
		dgst.reset();
		update_sha(dgst, cters, all_lines, from_idx, len);
		byte[] sha_bts = get_digest_sha(dgst, cters);
		return sha_bts;
	}

	public static String filter_string(String str1) {
		if (str1 == null) {
			throw new bad_passet(2);
		}
		int str_len = str1.length();
		StringBuilder out_str = new StringBuilder(str_len);

		boolean is_fst = true;
		boolean last_was_white = false;
		for (int aa = 0; aa < str1.length(); aa++) {
			char the_ch = str1.charAt(aa);
			if (Character.isISOControl(the_ch)) {
				the_ch = SPC_CHAR;
			}
			if (!Character.isDefined(the_ch)) {
				the_ch = SPC_CHAR;
			}
			if (the_ch == PIPE_CHAR) {
				the_ch = SPC_CHAR;
			}
			if (is_fst && (the_ch == EQU_CHAR)) {
				the_ch = SPC_CHAR;
			}

			boolean is_white = Character.isWhitespace(the_ch);
			if (is_white) {
				if (last_was_white) {
					continue;
				}
				last_was_white = true;
				the_ch = SPC_CHAR;
			} else {
				last_was_white = false;
				is_fst = false;
			}
			if (is_fst) {
				continue;
			}

			out_str.append(the_ch);
		}

		String output = out_str.toString();
		if (output.equals(config.UNKNOWN_STR)) {
			output = config.UNKNOWN_STR;
		}
		return output;
	}

	public static void skip_lines(ListIterator<String> it1, List<String> lines) {
		ListIterator<String> it2 = lines.listIterator();
		while (it2.hasNext()) {
			if (!it1.hasNext()) {
				throw new bad_passet(2);
			}
			String ss1 = it1.next();
			String ss2 = it2.next();
			if (!ss1.equals(ss2)) {
				// mem_file.write_string(new File("./dbg_ss1"), ss1);
				// mem_file.write_string(new File("./dbg_ss2"), ss2);
				throw new bad_passet(2, "ss1='" + ss1 + "'\nss2='" + ss2 + "'");
			}
		}
	}

	public static void add_next_title_to(List<String> txt, String title) {
		txt.add("");
		txt.add("");
		txt.add(title);
		txt.add("");
	}

	public static void get_next_title_from(ListIterator<String> it1,
			String title) {
		get_next_string(it1);
		get_next_string(it1);
		String str = get_next_string(it1);
		if (!str.equals(title)) {
			throw new bad_passet(2, String.format(L.parse_error, title));
		}
		get_next_string(it1);
	}

	public static String get_next_string(ListIterator<String> it1) {
		if (!it1.hasNext()) {
			throw new bad_passet(2);
		}
		String str = it1.next();
		if (str == null) {
			throw new bad_passet(2);
		}
		return str;
	}

	public static void add_next_field_to(List<String> txt, String title,
			String field, String val) {
		if (title == null) {
			title = "";
		}
		String full_nm = title + field;
		txt.add(full_nm);
		txt.add(val);
		txt.add("");
	}

	public static String get_next_field_from(ListIterator<String> it1,
			String title, String field) {
		if (title == null) {
			title = "";
		}
		String full_nm = title + field;
		String nxt = get_next_string(it1);
		if (!nxt.equals(full_nm)) {
			throw new bad_passet(2, String.format(L.parse_error, full_nm));
		}
		String str = get_next_string(it1);
		if (str == null) {
			throw new bad_passet(2);
		}
		get_next_string(it1);
		return str;
	}

	// public read/write funcs

	static SortedSet<String> read_encrypted_set(File ff, key_owner owr) {
		List<String> lst = read_encrypted_lines(ff, owr);
		TreeSet<String> tree = new TreeSet<String>(lst);
		return tree;
	}

	static void write_encrypted_set(File ff, key_owner owr,
			SortedSet<String> all_lines) {
		List<String> lst = new ArrayList<String>(all_lines);
		write_encrypted_lines(ff, owr, lst);
	}

	static List<String> read_encrypted_lines(File ff, key_owner owr) {
		if (!ff.exists()) {
			return new ArrayList<String>();
		}

		byte[] data = mem_file.concurrent_read_encrypted_bytes(ff, owr);
		List<String> all_lines = read_byte_array_lines(data);
		return all_lines;
	}

	static void write_encrypted_lines(File ff, key_owner owr,
			List<String> all_lines) {
		byte[] data = write_byte_array_lines(all_lines);
		mem_file.concurrent_write_encrypted_bytes(ff, owr, data);
	}

	static List<String> read_lines(File full_nam) {
		if (!full_nam.exists()) {
			return null;
		}
		return read_encrypted_lines(full_nam, null);
	}

	static void write_lines(File f_nm, List<String> all_lines) {
		write_encrypted_lines(f_nm, null, all_lines);
	}

	static void safe_append_lines(File f_nm, List<String> all_lines) {
		byte[] data = write_byte_array_lines(all_lines);

		File f_cp = file_funcs.get_temp_file(f_nm);
		file_funcs.concurrent_copy_file(f_nm, f_cp);

		mem_file.concurrent_append_bytes(f_cp, data);

		file_funcs.concurrent_delete_file(f_nm);
		file_funcs.concurrent_move_file(f_cp, f_nm);
	}

	static void append_lines(File f_nm, List<String> all_lines) {
		byte[] data = write_byte_array_lines(all_lines);
		mem_file.concurrent_append_bytes(f_nm, data);
	}

	static List<String> read_issuance_lines(File pss_ff) {
		List<String> all_lines = read_lines(pss_ff);
		if(all_lines == null){
			throw new bad_passet(2);
		}
		List<String> core_lines = get_lines_until_mark(
				all_lines.listIterator(), tag_transfer.end_of_section_mark);
		return core_lines;
	}

	public static void add_mark_to(List<String> txt, String mark) {
		txt.add(mark);
	}

	public static boolean is_mark(String line, String mark) {
		boolean eq1 = line.equals(mark);
		return eq1;
	}

	public static String get_mark_from(ListIterator<String> it1, String mark) {
		String str = it1.next();
		if (str == null) {
			throw new bad_passet(2);
		}
		if (!is_mark(str, mark)) {
			throw new bad_passet(2, String.format(L.parse_error, mark));
		}
		return str;
	}

	public static List<String> get_lines_until_mark(ListIterator<String> it1,
			String mark) {
		List<String> all_lines = new ArrayList<String>();
		if (mark == null) {
			throw new bad_passet(2);
		}
		while (it1.hasNext()) {
			String ln = it1.next();
			all_lines.add(ln);
			if (is_mark(ln, mark)) {
				break;
			}
		}
		if (all_lines.size() == 0) {
			return null;
		}
		return all_lines;
	}
}
