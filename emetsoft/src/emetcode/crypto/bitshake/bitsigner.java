package emetcode.crypto.bitshake;

import java.io.File;

import emetcode.crypto.bitshake.utils.config;
import emetcode.crypto.bitshake.utils.console_get_key;
import emetcode.crypto.bitshake.utils.convert;
import emetcode.crypto.bitshake.utils.mem_file;

public class bitsigner {

	static final char GENERATE_OPER = 'G';
	static final char SIGN_OPER = 'S';
	static final char CONFIRM_OPER = 'C';

	static final int DEFAULT_NUM_BITS = 32;

	char the_oper;
	byte[] user_key;
	int num_bits;
	String signer_file_nm;
	String signature_file_nm;
	String target_file_nm;

	public bitsigner() {
		init_bitsigner();
	}

	void init_bitsigner() {
		the_oper = GENERATE_OPER;
		user_key = null;
		num_bits = DEFAULT_NUM_BITS;
		signer_file_nm = null;
		signature_file_nm = null;
		target_file_nm = null;
	}

	public static void main(String[] args) {
		bitsigner eng = new bitsigner();
		if (!eng.get_args(args)) {
			return;
		}
		eng.process_file();
	}

	static String help_msg = "bitsigner [-h|-v] [-g <signer_file> [-n <num>]] [(-s|-c) <signature_file>] [-f <file_name>]\n"
			+ "        [-k <password>]\n"
			+ "\n"
			+ "-g : generate signer file <signer_name>. (default option).\n"
			+ "-n : generate signer file for a <num> bits signature. (default is 32 bits).\n"
			+ "-s : sign the given <file_name> with <signer_file> into <signature_file>.\n"
			+ "-c : confirm the given <file_name> with <signer_file> and <signature_file>.\n"
			+ "-k : use <password> as key (option intended for testing).\n"
			+ "-h : show invocation info.\n"
			+ "-v : show version info.\n"
			+ "\n"
			+ "See file 'bitsigner_use.txt' in the source directory or\n"
			+ "visit 'http://yosolosoy.com/esp/cry/'\n";

	static String version_msg = "bitsigner v1.0\n"
			+ "(c) 2012. QUIROGA BELTRAN, Jose Luis. Bogota - Colombia.\n";

	public void process_file() {
		// String op = "Invalid_operation";

		byte[] target_bytes = null;
		// byte[] signer_bytes = null;
		byte[] signature_bytes = null;

		boolean needs_key = ((the_oper == GENERATE_OPER) || (the_oper == SIGN_OPER));
		if (needs_key) {
			boolean with_kf = false;
			if (user_key == null) {
				user_key = console_get_key.get_key(true, with_kf);
			}
			if (user_key == null) {
				return;
			}
		}

		if (the_oper == GENERATE_OPER) {
			// op = "Generating";
			if (signer_file_nm == null) {
				return;
			}
			if (user_key == null) {
				return;
			}

			signeer sgn = new signeer(null, user_key, num_bits);

			sgn.write_signer_data(signer_file_nm);

		} else if (the_oper == SIGN_OPER) {
			// op = "Signing";
			if (target_file_nm == null) {
				return;
			}
			if (signer_file_nm == null) {
				return;
			}
			if (signature_file_nm == null) {
				return;
			}
			if (user_key == null) {
				return;
			}

			// target_bytes = mem_file.read_bytes(new File(target_file_nm));
			target_bytes = mem_file.concurrent_read_encrypted_bytes(new File(
					target_file_nm), null);

			signeer sgn = new signeer(target_bytes, user_key, num_bits);

			signature_bytes = sgn.get_signature(signer_file_nm);
			if (signature_bytes == null) {
				return;
			}

			signature_bytes = convert.bytes_to_hex_frm_bytes(signature_bytes,
					config.SIGNATURE_ID);
			// signature_bytes = cr_tools.bytes_to_hex_bytes(signature_bytes);
			// signature_bytes = cr_tools.frame_bytes(signature_bytes,
			// cr_tools.SIGNATURE_ID, cr_tools.SIGNATURE_ID);
			// mem_file.write_bytes(new File(signature_file_nm),
			// signature_bytes);
			mem_file.concurrent_write_encrypted_bytes(new File(
					signature_file_nm), null, signature_bytes);

		} else if (the_oper == CONFIRM_OPER) {
			// op = "Confirming";
			if (target_file_nm == null) {
				return;
			}
			if (signer_file_nm == null) {
				return;
			}
			if (signature_file_nm == null) {
				return;
			}

			// target_bytes = mem_file.read_bytes(new File(target_file_nm));
			target_bytes = mem_file.concurrent_read_encrypted_bytes(new File(
					target_file_nm), null);
			signeer sgn = new signeer(signer_file_nm, target_bytes);

			// signature_bytes = mem_file.read_bytes(new
			// File(signature_file_nm));
			signature_bytes = mem_file.concurrent_read_encrypted_bytes(
					new File(signature_file_nm), null);
			signature_bytes = convert.hex_frm_bytes_to_bytes(signature_bytes,
					config.SIGNATURE_ID);
			// signature_bytes = cr_tools.unframe_bytes(signature_bytes,
			// cr_tools.SIGNATURE_ID, cr_tools.SIGNATURE_ID);
			// signature_bytes = cr_tools.hex_bytes_to_bytes(signature_bytes);

			boolean ck_sgn = sgn.check_signature(signature_bytes);
			if (ck_sgn) {
				System.out.println("Signature ok");
			} else {
				System.out.println("NOT CONFIRMED !");
			}
		}
	}

	public static String calc_out_name(String in_nam, char the_op) {
		if (in_nam == null) {
			return null;
		}

		String suf = ".invalid";
		if (the_op == GENERATE_OPER) {
			suf = ".signer";
		} else if (the_op == SIGN_OPER) {
			suf = ".signature";
		} else if (the_op == CONFIRM_OPER) {
			suf = ".confirmation";
		}

		String out_nam = in_nam + suf;
		return out_nam;
	}

	boolean has_file_names() {
		if (the_oper == GENERATE_OPER) {
			if (signer_file_nm == null) {
				return false;
			}
			return true;
		}
		if (signer_file_nm == null) {
			return false;
		}
		if (signature_file_nm == null) {
			return false;
		}
		if (target_file_nm == null) {
			return false;
		}

		return true;
	}

	boolean get_args(String[] args) {

		boolean prt_help = false;
		boolean prt_version = false;

		the_oper = GENERATE_OPER;
		signer_file_nm = null;

		int num_args = args.length;
		for (int ii = 0; ii < num_args; ii++) {
			String the_arg = args[ii];
			if (the_arg.equals("-h")) {
				prt_help = true;
			} else if (the_arg.equals("-v")) {
				prt_version = true;
			} else if ((the_arg.equals("-g")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				signer_file_nm = args[kk_idx];
			} else if ((the_arg.equals("-s")) && ((ii + 1) < num_args)) {
				the_oper = SIGN_OPER;

				int kk_idx = ii + 1;
				ii++;

				signature_file_nm = args[kk_idx];
			} else if ((the_arg.equals("-c")) && ((ii + 1) < num_args)) {
				the_oper = CONFIRM_OPER;

				int kk_idx = ii + 1;
				ii++;

				signature_file_nm = args[kk_idx];
			} else if ((the_arg.equals("-f")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				target_file_nm = args[kk_idx];
			} else if ((the_arg.equals("-k")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				user_key = args[kk_idx].getBytes();

			} else if ((the_arg.equals("-n")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				num_bits = convert.parse_int(args[kk_idx]);
			}
		}

		if (signer_file_nm == null) {
			prt_help = true;
		}
		if ((target_file_nm == null) && (the_oper != GENERATE_OPER)) {
			prt_help = true;
		}
		if ((signature_file_nm == null) && (the_oper == SIGN_OPER)) {
			signature_file_nm = calc_out_name(target_file_nm, SIGN_OPER);
		}
		if (!has_file_names()) {
			prt_help = true;
		}

		if (prt_help) {
			System.out.println(help_msg);
			return false;
		}
		if (prt_version) {
			System.out.println(version_msg);
			return false;
		}

		return true;
	}

	public static boolean check_signature(byte[] target_data, byte[] signer,
			byte[] signa) {
		signeer sgn = new signeer(signer);
		sgn.set_target_data(target_data);
		boolean ck_val = sgn.check_signature(signa);
		return ck_val;
	}

	public static boolean check_txt_signature(byte[] target_data,
			byte[] signer, byte[] signa) {
		signa = convert.hex_frm_bytes_to_bytes(signa, config.SIGNATURE_ID);
		// signa = cr_tools.unframe_bytes(signa, cr_tools.SIGNATURE_ID,
		// cr_tools.SIGNATURE_ID);
		// signa = cr_tools.hex_bytes_to_bytes(signa);

		signer = convert.hex_frm_bytes_to_bytes(signer, config.SIGNER_ID);
		// signer = cr_tools.unframe_bytes(signer, cr_tools.SIGNER_ID,
		// cr_tools.SIGNER_ID);
		// signer = cr_tools.hex_bytes_to_bytes(signer);

		boolean sg_ok = check_signature(target_data, signer, signa);
		return sg_ok;
	}

}
