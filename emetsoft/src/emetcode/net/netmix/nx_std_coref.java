package emetcode.net.netmix;

import java.io.File;

import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.crypto.bitshake.utils.mem_file;
import emetcode.crypto.bitshake.utils.global_id;
import emetcode.util.devel.logger;

public class nx_std_coref {
	static final boolean IN_DEBUG_1 = false;
	static final boolean IN_DEBUG_2 = false; // send/recv std_coref
	static final boolean IN_DEBUG_3 = false; // has_std_coref_file
	static final boolean IN_DEBUG_4 = true; // write_remote_std_coref fails
	static final boolean IN_DEBUG_5 = false; // write_remote_std_coref ok

	private static final String MSG_STD_COREF = "msg_std_coref";

	private static final String LOCAL_GLID_FNAM = "local_glid.dat";
	private static final String LOCAL_ALIAS_FNAM = "local_alias.dat";
	private static final String LOCAL_BOSS_FNAM = "local_boss.dat";

	public static final String LOCAL_CONFIRM_SUF = ".ok";

	String full_val;

	private void init_td_coref(String val){
		full_val = val;
	}

	public nx_std_coref(String val) {
		init_td_coref(val);
		if (full_val == null) {
			throw new bad_netmix(2);
		}
	}

	public nx_std_coref(global_id per_gli) {
		if (per_gli == null) {
			throw new bad_netmix(2);
		}
		init_td_coref(per_gli.get_str());
	}
	
	public nx_std_coref(nx_std_coref orig) {
		if(orig == null){
			throw new bad_netmix(2);
		}
		full_val = orig.full_val;
		if (full_val == null) {
			throw new bad_netmix(2);
		}
	}

	public String toString() {
		return get_str();
	}

	public String get_str() {
		return full_val;
	}

	public static boolean is_glid(String g_str) {
		return global_id.is_person_glid(g_str);
	}

	public boolean is_glid() {
		return global_id.is_person_glid(get_str());
	}

	private static File get_std_coref_file(File nx_dd, String nm_ff) {
		if (nx_dd == null) {
			throw new bad_netmix(2);
		}
		File loc_gld_ff = new File(nx_dd, nm_ff);
		return loc_gld_ff;
	}

	private static File get_std_coref_confirm_file(File nx_dd, String nm_ff) {
		if (nx_dd == null) {
			throw new bad_netmix(2);
		}
		String ok_ff = nm_ff + LOCAL_CONFIRM_SUF;
		File loc_gld_ff = new File(nx_dd, ok_ff);
		return loc_gld_ff;
	}

	public static boolean write_std_coref_file(File dir_b, key_owner owr,
			nx_std_coref crf, String nm_ff) {
		File gld_ff = get_std_coref_file(dir_b, nm_ff);
		if (IN_DEBUG_1) {
			logger.debug("WRITING_STD_COREF=" + crf.get_str() + " in file="
					+ gld_ff);
		}
		mem_file.write_encrypted_string(gld_ff, owr, crf.toString());
		boolean w_ok = has_std_coref_file(dir_b, nm_ff);
		return w_ok;
	}

	public static nx_std_coref read_std_coref_file(File dir_b, key_owner owr,
			String nm_ff) {
		if (!has_std_coref_file(dir_b, nm_ff)) {
			return null;
		}
		File gld_ff = get_std_coref_file(dir_b, nm_ff);
		String vv = mem_file.read_encrypted_string(gld_ff, owr);
		if (vv == null) {
			return null;
		}
		nx_std_coref crf = new nx_std_coref(vv);
		return crf;
	}

	public static boolean has_std_coref_file(File dir_b, String nm_ff) {
		if(dir_b == null){
			return false;
		}
		if(nm_ff == null){
			return false;
		}
		File gld_ff = get_std_coref_file(dir_b, nm_ff);
		boolean hg = gld_ff.exists();
		if (IN_DEBUG_3) {
			String msg = "NO_STD_COREF_FILE=\n\t";
			if (hg) {
				msg = "found_std_coref_file=\n\t";
			}
			logger.debug(msg + gld_ff);
		}
		return hg;
	}

	private static boolean write_remote_std_coref(nx_messenger msgr,
			key_owner owr, String rem_crf_str, String nm_ff) {

		if (rem_crf_str == null) {
			if (IN_DEBUG_4) {
				logger.debug("wr_rem_std_crf NULL rem_crf_str nm_ff=" + nm_ff);
			}
			return false;
		}
		if (nm_ff == null) {
			throw new bad_netmix(2);
		}
		if (msgr == null) {
			throw new bad_netmix(2);
		}
		if (owr == null) {
			throw new bad_netmix(2);
		}

		File rem_dir_b = msgr.get_remote_nx_dir();
		nx_conn_id coid = msgr.get_dir_base().get_coid_by_ref(rem_crf_str, null);

		if (coid != null) {
			nx_conn_id mgr_coid = msgr.get_coid();
			if (!mgr_coid.equals(coid)) {
				if (IN_DEBUG_4) {
					logger.debug("wr_rem_std_crf FAILED (" + mgr_coid + " != "
							+ coid + ")");
				}
				return false;
			}
		}
		
		boolean has_crf = has_std_coref_file(rem_dir_b, nm_ff);
		if(has_crf){
			nx_std_coref ck_crf = read_std_coref_file(rem_dir_b, owr, nm_ff);
			if (ck_crf == null) {
				if (IN_DEBUG_4) {
					String stk = logger.get_stack_str();
					logger.debug(stk
							+ "\n wr_rem_std_crf NULL ck_crf rem_crf_str="
							+ rem_crf_str + " rem_dir_b=" + rem_dir_b
							+ " nm_ff=" + nm_ff);
				}
				return false;
			}

			String ck_crf_str = ck_crf.get_str();
			if (!ck_crf_str.equals(rem_crf_str)) {
				if (IN_DEBUG_4) {
					logger.debug("wr_rem_std_crf FAILED (" + ck_crf_str
							+ " != " + rem_crf_str + ")");
				}
				return false;
			}
			return true;
		}

		msgr.write_coref(rem_crf_str, msgr.get_coid());
		
		nx_std_coref rem_crf = new nx_std_coref(rem_crf_str);
		boolean w_ok = write_std_coref_file(rem_dir_b, owr, rem_crf, nm_ff);
		if (IN_DEBUG_4) {
			if (!w_ok) {
				logger.debug("wr_rem_std_crf FAILED rem_crf_str=" + rem_crf_str
						+ " coid=" + msgr.get_coid());
			}
		}
		if (IN_DEBUG_5) {
			if (w_ok) {
				//String stk = logger.get_stack_str();
				logger.debug("wr_rem_std_crf OK rem_crf_str=" + rem_crf_str
						+ " coid=" + msgr.get_coid() + " rem_dir_b="
						+ rem_dir_b + " nm_ff=" + nm_ff);
			}
		}
		return w_ok;
	}

	static String send_std_coref(nx_messenger msgr, key_owner owr,
			String nm_ff) {
		if (msgr == null) {
			throw new bad_netmix(2);
		}
		if (IN_DEBUG_2) {
			logger.debug("START_of_send_std_coref " + nm_ff);
		}

		File loc_dir_b = msgr.get_local_nx_dir();

		msgr.send_string(MSG_STD_COREF);

		nx_std_coref loc_crf = read_std_coref_file(loc_dir_b, owr, nm_ff);
		String nm_crf = null;
		if (loc_crf != null) {
			nm_crf = loc_crf.get_str();
		}
		msgr.send_string(nm_crf);
		boolean r_ok = msgr.recv_boolean_resp();
		if (r_ok) {
			File ok_ff = get_std_coref_confirm_file(loc_dir_b, nm_ff);
			if (!ok_ff.exists()) {
				mem_file.concurrent_create_file(ok_ff);
			}
		}

		String rem_crf_str = msgr.recv_string();
		boolean w_ok = write_remote_std_coref(msgr, owr, rem_crf_str, nm_ff);
		msgr.send_boolean_resp(w_ok);

		if (IN_DEBUG_2) {
			logger.debug("END_of_send_std_coref rem_crf_str=" + rem_crf_str);
		}
		return rem_crf_str;
	}

	static String recv_std_coref(nx_messenger msgr, key_owner owr,
			String nm_ff) {
		if (msgr == null) {
			throw new bad_netmix(2);
		}
		if (IN_DEBUG_2) {
			logger.debug("START_of_recv_std_coref");
		}

		File loc_dir_b = msgr.get_local_nx_dir();

		String msg_hd = msgr.recv_string();
		if (!msg_hd.equals(MSG_STD_COREF)) {
			throw new bad_netmix(2);
		}

		String rem_crf_str = msgr.recv_string();
		boolean w_ok = write_remote_std_coref(msgr, owr, rem_crf_str, nm_ff);
		msgr.send_boolean_resp(w_ok);

		nx_std_coref loc_crf = read_std_coref_file(loc_dir_b, owr, nm_ff);
		String nm_crf = null;
		if (loc_crf != null) {
			nm_crf = loc_crf.get_str();
		}
		msgr.send_string(nm_crf);
		boolean r_ok = msgr.recv_boolean_resp();
		if (r_ok) {
			File ok_ff = get_std_coref_confirm_file(loc_dir_b, nm_ff);
			if (!ok_ff.exists()) {
				mem_file.concurrent_create_file(ok_ff);
			}
		}

		if (IN_DEBUG_2) {
			logger.debug("END_of_recv_std_coref rem_crf_str=" + rem_crf_str);
		}
		return rem_crf_str;
	}

	public boolean equals(Object obj) {
		if(obj == null){
			return false;
		}
		if (!(obj instanceof nx_std_coref)) {
			return false;
		}
		nx_std_coref c_crf = ((nx_std_coref) obj);
		boolean str_eq = c_crf.get_str().equals(get_str());
		return str_eq;
	}
	
	static boolean has_glid(File nx_dd){
		boolean h_gli = nx_std_coref.has_std_coref_file(nx_dd, nx_std_coref.LOCAL_GLID_FNAM);
		return h_gli; 	
	}

	static void set_glid(File nx_dd, key_owner owr, nx_std_coref gld){
		nx_std_coref.write_std_coref_file(nx_dd, owr, gld,
				nx_std_coref.LOCAL_GLID_FNAM);
	}

	static nx_std_coref get_glid(File nx_dd, key_owner owr){
		nx_std_coref gli = nx_std_coref.read_std_coref_file(nx_dd, owr,
				nx_std_coref.LOCAL_GLID_FNAM);
		return gli;
	}
	
	static String send_glid(nx_messenger msgr, key_owner owr) {
		return send_std_coref(msgr, owr, LOCAL_GLID_FNAM);
	}
	
	static String recv_glid(nx_messenger msgr, key_owner owr) {
		return recv_std_coref(msgr, owr, LOCAL_GLID_FNAM);
	}
	
	static boolean has_alias(File nx_dd){
		boolean h_gli = nx_std_coref.has_std_coref_file(nx_dd, nx_std_coref.LOCAL_ALIAS_FNAM);
		return h_gli; 	
	}

	static void set_alias(File nx_dd, key_owner owr, nx_std_coref gld){
		nx_std_coref.write_std_coref_file(nx_dd, owr, gld,
				nx_std_coref.LOCAL_ALIAS_FNAM);
	}

	static nx_std_coref get_alias(File nx_dd, key_owner owr){
		nx_std_coref gli = nx_std_coref.read_std_coref_file(nx_dd, owr,
				nx_std_coref.LOCAL_ALIAS_FNAM);
		return gli;
	}
	
	static String send_alias(nx_messenger msgr, key_owner owr) {
		return send_std_coref(msgr, owr, LOCAL_ALIAS_FNAM);
	}
	
	static String recv_alias(nx_messenger msgr, key_owner owr) {
		return recv_std_coref(msgr, owr, LOCAL_ALIAS_FNAM);
	}
	
	public static boolean has_boss(File nx_dd){
		boolean h_gli = nx_std_coref.has_std_coref_file(nx_dd, nx_std_coref.LOCAL_BOSS_FNAM);
		return h_gli; 	
	}
	
	public static void set_boss(File nx_dd, key_owner owr, nx_std_coref gld){
		nx_std_coref.write_std_coref_file(nx_dd, owr, gld,
				nx_std_coref.LOCAL_BOSS_FNAM);
	}

	public static nx_std_coref get_boss(File nx_dd, key_owner owr){
		nx_std_coref gli = nx_std_coref.read_std_coref_file(nx_dd, owr,
				nx_std_coref.LOCAL_BOSS_FNAM);
		return gli;
	}
	
}
