package emetcode.crypto.bitshake.utils;

import java.io.File;

import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.crypto.bitshake.utils.mem_file;
import emetcode.util.devel.bad_emetcode;

public class global_id {

	public static final char GLID_SEP = '_';
	public static final String PERSON_GLID_SUF = "_pergli";
	public static final String FILE_GLID_SUF = "_filgli";

	private String full_val;

	public global_id(File ref_ff) {
		init_file_glid(ref_ff);
	}

	public global_id(File ref_ff, key_owner owr) {
		init_person_glid(ref_ff, owr);
	}

	public global_id(long creat_tm, byte[] arr_by) {
		init_global_id_with_bytes(creat_tm, arr_by, null, FILE_GLID_SUF);
	}

	public global_id(byte[] arr_by, key_owner owr) {
		init_global_id_with_bytes(0, arr_by, owr, PERSON_GLID_SUF);
	}

	public global_id(String val) {
		full_val = val;
		if (full_val == null) {
			throw new bad_emetcode(2);
		}
		if (!is_person_glid(full_val)) {
			throw new bad_emetcode(2);
		}
	}

	public global_id(global_id orig) {
		full_val = orig.full_val;
		if (full_val == null) {
			throw new bad_emetcode(2);
		}
	}

	private void init_person_glid(File ref_ff, key_owner owr) {
		byte[] sha_by = null;
		if ((ref_ff != null) && ref_ff.exists()) {
			sha_by = mem_file.calc_sha_bytes(ref_ff, null); // as if not
															// encrypted
		}
		init_global_id_with_bytes(0, sha_by, owr, PERSON_GLID_SUF);
	}

	private void init_file_glid(File ref_ff) {
		if(ref_ff == null){
			throw new bad_emetcode(2);
		}
		if(!ref_ff.exists()){
			throw new bad_emetcode(2);
		}
		byte[] sha_by = mem_file.calc_sha_bytes(ref_ff, null);
		init_global_id_with_bytes(0, sha_by, null, FILE_GLID_SUF);
	}

	private void init_global_id_with_bytes(long creation_time, byte[] sha_by, key_owner owr, String suf) {
		long the_id = 0;
		if(creation_time == 0){
			creation_time = System.currentTimeMillis();
		}
		long random_id = 0;
		if (sha_by != null) {
			the_id = convert.calc_minisha_long(sha_by);
		}
		if (the_id < 0) {
			the_id = -the_id;
		}
		if (owr != null) {
			random_id = owr.new_random_long();
			if (random_id < 0) {
				random_id = -random_id;
			}
		}
		if(!is_glid_suf(suf)){
			throw new bad_emetcode(2);
		}
		full_val = "" + creation_time + GLID_SEP + the_id;
		if(random_id != 0){
			full_val = full_val + GLID_SEP + random_id;
		}
		full_val += suf;
	}

	public String toString() {
		return get_str();
	}

	public String get_str() {
		return full_val;
	}

	public boolean equals(Object obj) {
		if(obj == null){
			return false;
		}
		if (!(obj instanceof global_id)) {
			return false;
		}
		global_id c_crf = ((global_id) obj);
		boolean str_eq = c_crf.get_str().equals(get_str());
		return str_eq;
	}

	private static boolean is_glid_suf(String str) {
		boolean is_per = (str == PERSON_GLID_SUF);
		boolean is_fil = (str == FILE_GLID_SUF);
		boolean is_suf = (is_per || is_fil);
		return is_suf;
	}
	
	public static boolean is_person_glid(String str) {
		boolean is_gli = str.endsWith(PERSON_GLID_SUF);
		return is_gli;
	}

	public static boolean is_file_glid(String str) {
		boolean is_gli = str.endsWith(FILE_GLID_SUF);
		return is_gli;
	}
}
