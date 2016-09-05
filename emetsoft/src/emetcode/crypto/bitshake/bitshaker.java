package emetcode.crypto.bitshake;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import emetcode.crypto.bitshake.locale.L;
import emetcode.crypto.bitshake.utils.config;
import emetcode.crypto.bitshake.utils.console_get_key;
import emetcode.crypto.bitshake.utils.convert;
import emetcode.crypto.bitshake.utils.data_packer;
import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.crypto.bitshake.utils.mem_file;

public class bitshaker {
	
	private static final boolean IN_DEBUG_1 = false; // prt if small enc_dec
	
	private static final String FILE_TYPE_STR = "CRYPIT.01\n";
	private static final String FILE_HDR_STR = FILE_TYPE_STR
			+ ">>>>>>>>>>>>>>>>>>>>>\n";
	private static final String FILE_WITH_SHA_HDR_STR = FILE_TYPE_STR
			+ "WITH_SHA>>>>>>>>>>>>>\n";

	private static final byte[] FILE_HDR = FILE_HDR_STR.getBytes(config.UTF_8);
	private static final byte[] FILE_HDR_SHA = FILE_WITH_SHA_HDR_STR
			.getBytes(config.UTF_8);
	
	private static final int MAX_BY_SMALL_DATA = 20;

	byte[] data_bytes;
	byte[] user_key;
	boolean with_sha;
	boolean encry;
	boolean with_key_file;
	String in_file_nm;
	String out_file_nm;

	public bitshaker() {
		init_bitshaker();
	}

	void init_bitshaker() {
		data_bytes = null;
		user_key = null;
		with_sha = true;
		encry = true;
		with_key_file = false;
		in_file_nm = null;
		out_file_nm = null;
	}

	public static void main(String[] args) {
		bitshaker eng = new bitshaker();
		if (!eng.get_args(args)) {
			return;
		}
		eng.process_file();
	}

	static String help_msg = "bitshaker <file_name> [-e|-d|-h|-v] [-r] [-k <password>] [-o <out_name>] \n"
			+ "\n"
			+ "-e : encrypt the given <file_name>. (default option).\n"
			+ "-d : decrypt the given <file_name>.\n"
			+ "-k : use <password> as key (option intended for testing).\n"
			+ "-o : output to <out_name>.\n"
			+ "-h : show invocation info.\n"
			+ "-v : show version info.\n"
			+ "\n"
			+ "-r : raw process (-e|-d) the given <file_name>.\n"
			+ "\n"
			+ "See file 'cry_use.txt' in the source directory or\n"
			+ "visit 'http://yosolosoy.com/esp/cry/'\n";

	static String version_msg = "bitshaker v1.0\n"
			+ "(c) 2012. QUIROGA BELTRAN, Jose Luis. Bogota - Colombia.\n";
	
	private static boolean is_too_small(byte[] dat){
		boolean ss = (dat.length < MAX_BY_SMALL_DATA);
		if(IN_DEBUG_1){
			if(ss){
				System.out.print("!");
			}
		}
		return ss;
	}

	public static byte[] encrypt_bits(byte[] data_bytes, byte[] the_key) {
		// swaps all bits of data_bytes use with small data (few bytes)
		cryper the_cy = new cryper(the_key);
		return the_cy.encryit(data_bytes, true);
	}

	public static byte[] decrypt_bits(byte[] data_bytes, byte[] the_key) {
		// swaps all bits of data_bytes use with small data (few bytes)
		cryper the_cy = new cryper(the_key);
		return the_cy.decryit(data_bytes, true);
	}

	public static byte[] encrypt_bytes(byte[] data_bytes, cryper the_cy) {
		boolean all_bits = is_too_small(data_bytes);
		data_bytes = the_cy.encryit(data_bytes, all_bits);
		return data_bytes;
	}

	public static byte[] decrypt_bytes(byte[] data_bytes, cryper the_cy) {
		boolean all_bits = is_too_small(data_bytes);
		data_bytes = the_cy.decryit(data_bytes, all_bits);
		return data_bytes;
	}

	public static byte[] encrypt_bytes(byte[] data_bytes, byte[] the_key) {
		cryper the_cy = new cryper(the_key);
		return encrypt_bytes(data_bytes, the_cy);
	}

	public static byte[] decrypt_bytes(byte[] data_bytes, byte[] the_key) {
		cryper the_cy = new cryper(the_key);
		return decrypt_bytes(data_bytes, the_cy);
	}

	public static byte[] encrypt_bytes_with_sha(byte[] data_bytes, key_owner owr) {
		cryper the_cy = owr.get_cryper();
		return encrypt_bytes_with_sha(data_bytes, the_cy);
	}

	public static byte[] decrypt_bytes_with_sha(byte[] data_bytes, key_owner owr) {
		cryper the_cy = owr.get_cryper();
		return decrypt_bytes_with_sha(data_bytes, the_cy);
	}

	public static byte[] encrypt_bytes_with_sha(byte[] data_bytes,
			byte[] the_key) {
		cryper the_cy = new cryper(the_key);
		return encrypt_bytes_with_sha(data_bytes, the_cy);
	}

	public static byte[] decrypt_bytes_with_sha(byte[] data_bytes,
			byte[] the_key) {
		cryper the_cy = new cryper(the_key);
		return decrypt_bytes_with_sha(data_bytes, the_cy);
	}

	public static byte[] encrypt_bytes_with_sha(byte[] data_bytes, cryper the_cy) {
		byte[] the_sha = convert.calc_sha_bytes(data_bytes);
		byte[] to_enc = data_packer.pack_enc(data_bytes, the_sha);
		byte[] enc_dat = encrypt_bytes(to_enc, the_cy);
		return enc_dat;
	}

	public static byte[] decrypt_bytes_with_sha(byte[] data_bytes, cryper the_cy) {
		byte[] dec_dat = decrypt_bytes(data_bytes, the_cy);
		List<byte[]> unpk = data_packer.unpack_enc(dec_dat);
		if (unpk.size() != 2) {
			throw new bad_bitshake(2);
		}
		byte[] dat = unpk.get(0);
		byte[] the_sha = unpk.get(1);
		byte[] cal_sha = convert.calc_sha_bytes(dat);
		if (!Arrays.equals(the_sha, cal_sha)) {
			throw new bad_bitshake(2);
		}
		return dat;
	}

	static byte[] encrypt_file_bytes(byte[] data_bytes, byte[] the_key,
			boolean with_sha) {

		byte[] enc_dat = null;
		byte[] hd = null;
		if (with_sha) {
			hd = FILE_HDR_SHA;
			enc_dat = encrypt_bytes_with_sha(data_bytes, the_key);
		} else {
			hd = FILE_HDR;
			enc_dat = encrypt_bytes(data_bytes, the_key);
		}

		byte[] hex_enc_dat = convert.bytes_to_hex_bytes(enc_dat);

		int full_sz = hd.length + hex_enc_dat.length;
		byte[] out_bytes = new byte[full_sz];
		ByteBuffer buff = ByteBuffer.wrap(out_bytes);

		buff.put(hd);
		buff.put(hex_enc_dat);
		if (buff.remaining() != 0) {
			throw new bad_bitshake(2);
		}
		return out_bytes;
	}

	static byte[] decrypt_file_bytes(byte[] file_bytes, byte[] the_key,
			boolean with_sha) {
		byte[] the_hd = null;
		if (with_sha) {
			the_hd = FILE_HDR_SHA;
		} else {
			the_hd = FILE_HDR;
		}

		byte[] hd = Arrays.copyOf(file_bytes, the_hd.length);
		if (!Arrays.equals(hd, the_hd)) {
			throw new bad_bitshake(2, L.bad_header_found);
		}

		ByteBuffer buff = ByteBuffer.wrap(file_bytes);

		byte[] hdr = new byte[the_hd.length];
		buff.get(hdr);
		if (!Arrays.equals(hdr, the_hd)) {
			throw new bad_bitshake(2, L.bad_header_found);
		}

		int dat_sz = file_bytes.length - the_hd.length;
		byte[] hex_enc = new byte[dat_sz];
		buff.get(hex_enc);

		if (buff.remaining() != 0) {
			throw new bad_bitshake(2);
		}
		byte[] enc_dat = convert.hex_bytes_to_bytes(hex_enc);

		byte[] out_bytes = null;
		if (with_sha) {
			out_bytes = decrypt_bytes_with_sha(enc_dat, the_key);
		} else {
			out_bytes = decrypt_bytes(enc_dat, the_key);
		}

		return out_bytes;
	}

	public void process_file() {
		String op = "Decrypting";
		if (encry) {
			op = "Encrypting";
		}
		String has_sh = "";
		if (with_sha) {
			has_sh = " (with header)";
		}
		System.out.println(op + " file " + in_file_nm + " into " + out_file_nm
				+ has_sh + ".\n");

		byte[] file_bytes = mem_file.concurrent_read_encrypted_bytes(new File(
				in_file_nm), null);
		if (file_bytes == null) {
			System.out.println("Cannot read file " + in_file_nm + ".\n");
			return;
		}

		if (user_key == null) {
			user_key = console_get_key.get_key(encry, with_key_file);
		}
		if (user_key == null) {
			return;
		}

		byte[] out_bytes = null;
		if (encry) {
			out_bytes = encrypt_file_bytes(file_bytes, user_key, with_sha);
		} else {
			out_bytes = decrypt_file_bytes(file_bytes, user_key, with_sha);
		}

		if (out_bytes != null) {
			mem_file.concurrent_write_encrypted_bytes(new File(out_file_nm),
					null, out_bytes);
		}
	}

	private static String calc_out_name(String in_nam, boolean encry) {
		assert (in_nam != null);

		String suf = ".decry";
		if (encry) {
			suf = ".encry";
		}

		String out_nam = in_nam + suf;
		return out_nam;
	}

	boolean get_args(String[] args) {

		String in_name = null;
		String out_name = null;
		byte[] argkey = null;
		boolean prt_help = false;
		boolean prt_version = false;

		int num_args = args.length;
		for (int ii = 0; ii < num_args; ii++) {
			String the_arg = args[ii];
			if (the_arg.equals("-h")) {
				prt_help = true;
			} else if (the_arg.equals("-v")) {
				prt_version = true;
			} else if (the_arg.equals("-r")) {
				with_sha = false;
			} else if (the_arg.equals("-d")) {
				encry = false;
			} else if (the_arg.equals("-kf")) {
				with_key_file = true;
			} else if ((the_arg.equals("-k")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				argkey = args[kk_idx].getBytes();

			} else if ((the_arg.equals("-o")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				out_name = args[kk_idx];

			} else if (in_name == null) {
				in_name = the_arg;
			}
		}

		if (argkey != null) {
			String keystr = new String(argkey);
			if (keystr.equals(in_name)) {
				argkey = null;
				System.out.println("key same as file name. ignoring key");
			}
		}

		if (prt_help) {
			System.out.println(help_msg);
			return false;
		}
		if (prt_version) {
			System.out.println(version_msg);
			return false;
		}

		if (in_name == null) {
			System.out.println(help_msg);
			return false;
		}

		if (out_name == null) {
			out_name = calc_out_name(in_name, encry);
		}
		if (out_name == null) {
			System.out.println(help_msg);
			return false;
		}
		if (in_name.equals(out_name)) {
			System.out.println("Output name must be different to input name");
			return false;
		}

		user_key = argkey;
		in_file_nm = in_name;
		out_file_nm = out_name;

		assert (in_file_nm != null);
		assert (out_file_nm != null);

		return true;
	}

	public static long encrypt_long(long the_num, long the_key) {
		byte[] num_arr = convert.to_byte_array(the_num);
		byte[] key_arr = convert.to_byte_array(the_key);
		byte[] encr_arr = encrypt_bits(num_arr, key_arr);
		long encr_num = convert.to_long(encr_arr);
		return encr_num;
	}

	public static long decrypt_long(long encr_num, long the_key) {
		byte[] encr_arr = convert.to_byte_array(encr_num);
		byte[] key_arr = convert.to_byte_array(the_key);
		byte[] num_arr = decrypt_bits(encr_arr, key_arr);
		long the_num = convert.to_long(num_arr);
		return the_num;
	}

}
