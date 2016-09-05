package emetcode.economics.passet;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import emetcode.crypto.bitshake.utils.convert;

public class config {

	public static final boolean DEBUG = true;

	public static final int NUM_BYTES_CHG_KEY = 100;

	public static final String SHA_ID = emetcode.crypto.bitshake.utils.config.SHA_ID;

	public static final int MAX_PASSET_LINE_SZ = 75;

	public static final int KB_1 = 1000;
	public static final int MB_1 = 1000 * KB_1;

	public static final int AVERAGE_BUFF_SZ = 100 * KB_1;

//	public static final String F_NAM_PASSOFT_SRC = "signed_data_of_passoft/passoft_sources.jar";
//	public static final String F_NAM_PASSOFT_SIGNA = "signed_data_of_passoft/signature_of_passoft";
//	public static final String F_NAM_PASSOFT_SIGNER = "src/passoft/signing_data/signer_of_passoft";

	public static final String INVALID_ID_SHA_STR = "invalid_sha_str";

	public static final long MIN_COLLECT_PERIOD_MILLIS = 1000;
	public static final long SEC_MILLIS = 1000L;
	public static final long MIN_MILLIS = 60000L;
	public static final long DAY_MILLIS = 86400000L;
	public static final long YEAR_MILLIS = 31556952000L;

	public static final String UTF_8_NAM = "UTF-8";
	public static final Charset UTF_8 = Charset.forName(UTF_8_NAM);

	public static final String SHA_256_STR = "SHA-256";
	public static final String SHA_512_STR = "SHA-512";

	public static final String UNKNOWN_STR = "unknown";

	public static final String no_title = "NO_TITLE";
	
	// NON FINAL ATTR

	public static int SHA_STR_NUM_CHARS = -1;

	//public static String JAR_SHA_VALUE = "INVALID_JAR_SHA_VALUE";
	//public static String JAR_SIGNATURE = "INVALID_JAR_SIGNATURE";

	public static int DEFAULT_CURRENCY = 144;
	public static int WORKING_CURRENCY = DEFAULT_CURRENCY;

//	public static int DEFAULT_TRUSTED_LEVEL = passet.TRUSTED_LEVEL;

	public static int DEFAULT_MIN_CHG_EXPO = 0;

	
	static {
		init_all_global();
		init_sha_str_num_chars();
	};

	public static void init_all_global() {
		
//		try{ check_passoft_signature(); } catch(bad_passet ex){
//		logger.info("Cannot check passoft signature"); }
		
	}

	public static void init_sha_str_num_chars() {
		List<String> all_lines = new ArrayList<String>();
		all_lines.add("UNA_LINEA");
		byte[] sha_bts = parse.calc_sha_lines(all_lines);
		String the_sha_str = convert.bytes_to_hex_string(sha_bts);
		SHA_STR_NUM_CHARS = the_sha_str.length();
	}

//	public static boolean all_global_ok() {
//		boolean c1 = (JAR_SHA_VALUE != null);
//		boolean c2 = (JAR_SIGNATURE != null);
//		boolean all_ok = (c1 && c2);
//		return all_ok;
//	}

//	public static byte[] get_passoft_signature() {
//		try {
//			URL res1 = ClassLoader
//					.getSystemResource(config.F_NAM_PASSOFT_SIGNA);
//			byte[] sgna_dat = mem_file.load_resource(res1);
//			return sgna_dat;
//		} catch (NullPointerException ex1) {
//		}
//		throw new bad_passet(2);
//	}
//
//	public static byte[] get_passoft_source_code() {
//		URL res1 = ClassLoader.getSystemResource(config.F_NAM_PASSOFT_SRC);
//		try {
//			byte[] src_dat = mem_file.load_resource(res1);
//			return src_dat;
//		} catch (NullPointerException ex1) {
//		}
//		throw new bad_passet(2);
//	}

//	public static byte[] get_passoft_signer(byte[] full_jar) {
//		if (full_jar == null) {
//			throw new bad_passet(2);
//		}
//		try {
//			byte[] sgner = null;
//			ByteArrayInputStream is = new ByteArrayInputStream(full_jar);
//			JarInputStream jin = new JarInputStream(is);
//			JarEntry nxt = null;
//			while ((nxt = jin.getNextJarEntry()) != null) {
//				String nam = nxt.getName();
//				if (nam.equals(config.F_NAM_PASSOFT_SIGNER)) {
//					sgner = mem_file.read_stream(jin);
//					break;
//				}
//			}
//			return sgner;
//		} catch (NullPointerException ex1) {
//		} catch (IOException ex1) {
//		}
//		throw new bad_passet(2);
//	}

//	public static void check_passoft_signature() {
//		config.JAR_SHA_VALUE = null;
//		config.JAR_SIGNATURE = null;
//
//		byte[] target_data = get_passoft_source_code();
//		if (target_data == null) {
//			return;
//		}
//
//		byte[] sgner = get_passoft_signer(target_data);
//		if (sgner == null) {
//			return;
//		}
//
//		byte[] sgna = get_passoft_signature();
//		if (sgna == null) {
//			return;
//		}
//
//		boolean ck_val = bitsigner
//				.check_txt_signature(target_data, sgner, sgna);
//		if (!ck_val) {
//			return;
//		}
//
//		String sha_tgt = convert.calc_sha_text(target_data);
//		String sgna_str = new String(sgna, config.UTF_8);
//
//		config.JAR_SHA_VALUE = sha_tgt;
//		config.JAR_SIGNATURE = sgna_str;
//	}

	public static boolean is_unk(String str_fld) {
		if (str_fld == null) {
			throw new bad_passet(2);
		}
		boolean is_unk_str = (str_fld == UNKNOWN_STR);
		if (!is_unk_str) {
			is_unk_str = str_fld.equals(UNKNOWN_STR);
		}
		return is_unk_str;
	}

//	public static void check_soft() {
//		String jar_sha = config.JAR_SHA_VALUE;
//		String jar_signa = config.JAR_SIGNATURE;
//
//		if (jar_sha == null) {
//			throw new bad_passet(2);
//		}
//		if (jar_signa == null) {
//			throw new bad_passet(2);
//		}
//	}

}
