package emetcode.net.netmix;

import java.io.File;
import java.io.FileFilter;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import emetcode.crypto.bitshake.utils.convert;
import emetcode.crypto.bitshake.utils.file_funcs;
import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.crypto.bitshake.utils.mem_file;
import emetcode.crypto.bitshake.utils.mer_twist;
import emetcode.crypto.gamal.gamal_cipher;
import emetcode.crypto.gamal.gamal_generator;
//import emetcode.net.netmix.locale.L;
import emetcode.net.netmix.locator_sys.nx_connector;
import emetcode.net.netmix.locator_sys.nx_locator;
import emetcode.net.netmix.locator_sys.nx_locators_verifier;
//import emetcode.util.devel.bad_emetcode;
import emetcode.util.devel.logger;

public class nx_protector {

	static boolean IN_DEBUG_1 = false;
	static boolean IN_DEBUG_2 = false;
	static boolean IN_DEBUG_3 = false; // generate fake exceptions
	static boolean IN_DEBUG_4 = false; // send/recv locators files
	static boolean IN_DEBUG_5 = true; // send_secure failed

	static final mer_twist dbg_rnd = new mer_twist();

	private static final char FLD_SEP = nx_dir_base.FLD_SEP;

	// private static final int[] COID_DIR_SLICES_1 = { 2, 2, 3, 4 };

	private static final String MSG_SET_SECURE = "msg_set_secure_connection";

	private static final int MIN_SEKEY_BYTES = config.NUM_BYTES_RANDOM_KEY_SZ;

	public static final String COID_SUFIX = nx_dir_base.COID_SUFIX;
	public static final String COID_PREFIX = nx_dir_base.COID_PREFIX;

	public static final String VERIFIER_FILE_NM = "verifier_file.dat";

	public static final nx_locators_verifier NULL_VERIFIER = null;

	private nx_dir_base dir_base;
	private gamal_generator gamal_sys;

	private nx_messenger the_msgr;

	private int num_sekey_bytes;

	private nx_locators_verifier verifier;

	// set_secure attr

	private nx_conn_id secty_coid;
	private byte[] secty_pre_key;
	private boolean secty_save_key;
	private String secty_rem_glid;

	public nx_protector(nx_messenger msgr, nx_locators_verifier loc_v) {
		init_nx_protector(msgr, loc_v);
	}

	private void init_nx_protector(nx_messenger msgr, nx_locators_verifier loc_v) {
		if (msgr == null) {
			throw new bad_netmix(2);
		}
		the_msgr = msgr;
		dir_base = the_msgr.get_dir_base();
		gamal_sys = null;

		num_sekey_bytes = MIN_SEKEY_BYTES;

		verifier = loc_v;

		init_secty();
	}

	private void init_secty() {
		secty_coid = null;
		secty_pre_key = null;
		secty_save_key = false;
		secty_rem_glid = null;
	}

	private key_owner get_owner() {
		return get_connection().get_local_peer().get_owner();
	}

	private byte[] find_sekey() {
		int kk_sz = config.NUM_BYTES_RANDOM_KEY_SZ;
		if (num_sekey_bytes > kk_sz) {
			kk_sz = num_sekey_bytes;
		}

		byte[] nw_key = get_owner().new_random_key(kk_sz);
		return nw_key;
	}

	private byte[] find_new_first_cokey(String pub_str, String[] enc_key) {
		if (enc_key.length != 1) {
			throw new bad_netmix(2);
		}

		SecureRandom rnd = get_owner().new_SecureRandom();
		gamal_cipher gm = new gamal_cipher(pub_str, rnd);

		int num_bts = gm.max_msg_bytes() - 1;
		byte[] mag = get_owner().new_random_key(num_bts);
		BigInteger skey = new BigInteger(1, mag);
		byte[] nw_key = skey.toByteArray();

		String enc = gm.encrypt(skey);
		enc_key[0] = enc;
		return nw_key;
	}

	private File get_coid_file(nx_conn_id coid) {
		File ff = dir_base.get_coid_file(coid);
		return ff;
	}

	// private static String get_sliced_coid_dir_path(nx_conn_id coid) {
	// if(coid == null){
	// throw new bad_netmix(2);
	// }
	// String val_coid = coid.toString() + COID_SUFIX;
	// String dir1 = val_coid;
	// dir1 = file_funcs.as_sliced_dir_path(val_coid, COID_DIR_SLICES_1);
	// return dir1;
	// }
	//
	private static FileFilter is_coid_file() {
		FileFilter ffil = new FileFilter() {
			public boolean accept(File ff) {
				if (ff.isDirectory()) {
					return false;
				}
				return ff.getName().startsWith(nx_protector.COID_PREFIX);
			}
		};
		return ffil;
	}

	private static FileFilter is_coid_sub_dir() {
		FileFilter ffil = new FileFilter() {
			public boolean accept(File ff) {
				File pp = ff.getParentFile();
				boolean is_coid_dd = ff.isDirectory() && (pp != null)
						&& pp.isDirectory()
						&& pp.getName().endsWith(nx_protector.COID_SUFIX);
				return is_coid_dd;
			}
		};
		return ffil;
	}

	// PUBLIC_METHODS

	public nx_context get_local_context() {
		return get_connection().get_local_peer().get_context();
	}

	public nx_connection get_connection() {
		if (the_msgr == null) {
			throw new bad_netmix(2);
		}
		nx_connection cnn = the_msgr.get_connection();
		if (cnn == null) {
			throw new bad_netmix(2);
		}
		return cnn;
	}

	public nx_conn_id get_coid() {
		return secty_coid;
	}

	public String get_local_descr() {
		return get_connection().get_local_peer().get_description();
	}

	public String get_remote_descr() {
		return get_connection().get_remote_peer().get_description();
	}

	public void set_agreed_key(byte[] key) {
		if (key == null) {
			return;
		}
		secty_pre_key = key;
	}

	public boolean has_secure_conn() {
		return get_connection().has_cryper();
	}

	public void set_net_base_dir(nx_dir_base bdir) {
		if (bdir == null) {
			throw new bad_netmix(2);
		}
		dir_base = bdir;
	}

	// public static File get_coid_dir(File all_coids_dd, nx_conn_id coid) {
	// String f_dd = get_sliced_coid_dir_path(coid);
	// File ff = new File(all_coids_dd, f_dd);
	// return ff;
	// }

	public static nx_conn_id get_coid_from_coid_file(File co_ff) {
		String nm_ff = co_ff.getName();
		if (!nm_ff.startsWith(COID_PREFIX)) {
			throw new bad_netmix(2);
		}
		int sep_idx = nm_ff.indexOf(nx_protector.FLD_SEP); // last char of
															// COID_PREFIX

		long cnid = convert.parse_long(nm_ff.substring(sep_idx + 1));
		nx_conn_id n_coid = new nx_conn_id(cnid);
		return n_coid;
	}

	// public static File get_coid_file(File coids_base_dir, nx_conn_id coid) {
	// File c_dir = get_coid_dir(coids_base_dir, coid);
	// File ff = new File(c_dir, COID_PREFIX + coid);
	// return ff;
	// }

	public static List<File> find_all_coid_files(nx_dir_base dir_b) {
		File dir = dir_b.get_all_coids_base_dir();
		List<File> all_ff = file_funcs.list_dir(dir, is_coid_sub_dir(),
				is_coid_file());
		return all_ff;
	}

	public boolean is_to_renew_connection_key() {
		return secty_save_key;
	}

	public void set_to_renew_connection_key() {
		if (IN_DEBUG_1) {
			logger.debug("set_to_renew connection_key");
			logger.debug("ssc_renew = RENW_COKEY");
		}
		secty_save_key = true;
	}

	public void set_to_keep_connection_key() {
		secty_save_key = false;
	}

	private boolean has_gamal_sys() {
		return (gamal_sys != null);
	}

	private gamal_generator get_gamal_sys() {
		if (has_gamal_sys()) {
			return gamal_sys;
		}
		gamal_sys = dir_base.get_gamal_sys(get_owner());
		return gamal_sys;
	}

	private void update_locations_database(String rem_reported_descr,
			String rem_observed_descr) {

		nx_locator glids_db = new nx_locator(dir_base, get_owner(), null);
		glids_db.set_reported_addr(get_coid(), rem_reported_descr);
		glids_db.set_observed_addr(get_coid(), rem_observed_descr);
	}

	public void send_location() {
		String loc_descr = get_local_descr();
		the_msgr.send_string(loc_descr);

		String rem_reported_descr = the_msgr.recv_string();
		String rem_observed_descr = get_remote_descr();

		update_locations_database(rem_reported_descr, rem_observed_descr);
	}

	public void recv_location() {
		String rem_reported_descr = the_msgr.recv_string();
		String rem_observed_descr = get_remote_descr();

		String loc_descr = get_local_descr();
		the_msgr.send_string(loc_descr);

		update_locations_database(rem_reported_descr, rem_observed_descr);
	}

	public static void write_local_domain(nx_dir_base dir_b, key_owner owr,
			String dom_str) {
		File prot_dd = dir_b.get_protocol_base_dir();
		File dom_ff = new File(prot_dd, config.LOCAL_DOMAIN_FNAM);
		mem_file.write_encrypted_string(dom_ff, owr, dom_str);
	}

	public static String read_local_domain(nx_dir_base dir_b, key_owner owr) {
		File prot_dd = dir_b.get_protocol_base_dir();
		File dom_ff = new File(prot_dd, config.LOCAL_DOMAIN_FNAM);
		String dom_str = mem_file.read_encrypted_string(dom_ff, owr);
		return dom_str;
	}

	public void write_local_domain(String dom_str) {
		write_local_domain(dir_base, get_owner(), dom_str);
	}

	public String read_local_domain() {
		return read_local_domain(dir_base, get_owner());
	}

	public File get_local_verifier_file() {
		File loc_dd = dir_base.get_local_nx_dir();
		File v_ff = new File(loc_dd, VERIFIER_FILE_NM);
		return v_ff;
	}

	public File get_remote_verifier_file() {
		File rem_dd = dir_base.get_remote_nx_dir(get_coid());
		File v_ff = new File(rem_dd, VERIFIER_FILE_NM);
		return v_ff;
	}

	void send_locator_files() {
		if (IN_DEBUG_4) {
			logger.debug("send_locator_files. starting");
		}

		key_owner owr = get_owner();

		File loc_ltors_ff = nx_connector.get_local_locators_file(dir_base);
		the_msgr.send_file_if_diff(loc_ltors_ff, owr);

		File rem_ltors_ff = nx_connector.get_remote_locators_file(dir_base,
				get_coid());
		File rem_ltors_tmp_ff = file_funcs.get_temp_file(rem_ltors_ff);
		the_msgr.recv_file_if_diff(rem_ltors_ff, owr, rem_ltors_tmp_ff);

		File loc_su_ltors_ff = nx_connector
				.get_local_supra_locators_file(dir_base);
		the_msgr.send_file_if_diff(loc_su_ltors_ff, owr);

		File rem_su_ltors_ff = nx_connector.get_remote_supra_locators_file(
				dir_base, get_coid());
		File rem_su_ltors_tmp_ff = file_funcs.get_temp_file(rem_su_ltors_ff);
		the_msgr.recv_file_if_diff(rem_su_ltors_ff, owr, rem_su_ltors_tmp_ff);

		File loc_verif_ff = get_local_verifier_file();
		the_msgr.send_file_if_diff(loc_verif_ff, owr);

		File rem_verif_ff = get_remote_verifier_file();
		File rem_verif_tmp_ff = file_funcs.get_temp_file(rem_verif_ff);
		the_msgr.recv_file_if_diff(rem_verif_ff, owr, rem_verif_tmp_ff);

		List<File> prev_files = new ArrayList<File>();
		prev_files.add(rem_ltors_ff);
		prev_files.add(rem_su_ltors_ff);

		List<File> next_files = new ArrayList<File>();
		next_files.add(rem_ltors_tmp_ff);
		next_files.add(rem_su_ltors_tmp_ff);

		boolean all_ok = true;
		if (verifier != null) {
			all_ok = verifier.verif_locator_files(rem_verif_ff,
					rem_verif_tmp_ff, prev_files, next_files);
		}

		if (all_ok) {
			if (IN_DEBUG_4) {
				logger.debug("send_locator_files. all_ok lo="
						+ rem_ltors_tmp_ff.exists() + " su="
						+ rem_su_ltors_tmp_ff.exists());
			}
			file_funcs.concurrent_move_file(rem_ltors_tmp_ff, rem_ltors_ff);
			file_funcs.concurrent_move_file(rem_su_ltors_tmp_ff,
					rem_su_ltors_ff);
		}
	}

	void recv_locator_files() {
		if (IN_DEBUG_4) {
			logger.debug("recv_locator_files. starting");
		}

		key_owner owr = get_owner();

		File rem_ltors_ff = nx_connector.get_remote_locators_file(dir_base,
				get_coid());
		File rem_ltors_tmp_ff = file_funcs.get_temp_file(rem_ltors_ff);
		the_msgr.recv_file_if_diff(rem_ltors_ff, owr, rem_ltors_tmp_ff);

		File loc_ltors_ff = nx_connector.get_local_locators_file(dir_base);
		the_msgr.send_file_if_diff(loc_ltors_ff, owr);

		File rem_su_ltors_ff = nx_connector.get_remote_supra_locators_file(
				dir_base, get_coid());
		File rem_su_ltors_tmp_ff = file_funcs.get_temp_file(rem_su_ltors_ff);
		the_msgr.recv_file_if_diff(rem_su_ltors_ff, owr, rem_su_ltors_tmp_ff);

		File loc_su_ltors_ff = nx_connector
				.get_local_supra_locators_file(dir_base);
		the_msgr.send_file_if_diff(loc_su_ltors_ff, owr);

		File rem_verif_ff = get_remote_verifier_file();
		File rem_verif_tmp_ff = file_funcs.get_temp_file(rem_verif_ff);
		the_msgr.recv_file_if_diff(rem_verif_ff, owr, rem_verif_tmp_ff);

		File loc_verif_ff = get_local_verifier_file();
		the_msgr.send_file_if_diff(loc_verif_ff, owr);

		List<File> prev_files = new ArrayList<File>();
		prev_files.add(rem_ltors_ff);
		prev_files.add(rem_su_ltors_ff);

		List<File> next_files = new ArrayList<File>();
		next_files.add(rem_ltors_tmp_ff);
		next_files.add(rem_su_ltors_tmp_ff);

		boolean all_ok = true;
		if (verifier != null) {
			all_ok = verifier.verif_locator_files(rem_verif_ff,
					rem_verif_tmp_ff, prev_files, next_files);
		}

		if (all_ok) {
			if (IN_DEBUG_4) {
				logger.debug("recv_locator_files. all_ok lo="
						+ rem_ltors_tmp_ff.exists() + " su="
						+ rem_su_ltors_tmp_ff.exists());
			}
			file_funcs.concurrent_move_file(rem_ltors_tmp_ff, rem_ltors_ff);
			file_funcs.concurrent_move_file(rem_su_ltors_tmp_ff,
					rem_su_ltors_ff);
		}
	}

	// set_secure meths

	private byte[] get_coid_key(nx_conn_id the_coid) {
		if (the_coid == null) {
			throw new bad_netmix(2);
		}
		File fc = get_coid_file(the_coid);
		if (!fc.exists()) {
			if (IN_DEBUG_1) {
				logger.debug("file does NOT exist fc=" + fc);
			}
			throw new bad_netmix(2);
		}

		byte[] the_kk = mem_file.concurrent_read_encrypted_bytes(fc,
				get_owner());
		if (IN_DEBUG_1) {
			// String str_kk = convert.bytes_to_hex_string(secu_cokey);
			logger.debug("\nREAD_cokey" + // "=" + str_kk + "\n" +
					" OF_COID=" + the_coid + "\nFROM_FILE=" + fc);
		}

		if (the_kk == null) {
			throw new bad_netmix(2);
		}
		return the_kk;
	}

	private static void ck_glid(nx_std_coref gli) {
		if (gli == null) {
			throw new bad_netmix(2);
		}
		ck_glid(gli.get_str());
	}

	private static void ck_glid(String gli) {
		if (gli == null) {
			throw new bad_netmix(2);
		}
		if (!nx_std_coref.is_glid(gli)) {
			throw new bad_netmix(2, "NOT A GLID gli=" + gli);
		}
	}

	private void set_coid_key(nx_conn_id the_coid, byte[] the_kk) {
		if (the_coid == null) {
			throw new bad_netmix(2);
		}
		if (the_kk == null) {
			throw new bad_netmix(2);
		}
		dir_base.write_coid_file(the_coid, the_kk, get_owner());

		ck_glid(secty_rem_glid);
		dir_base.write_coref(secty_rem_glid, the_coid);
	}

	void send_exch_glids() {
		if (secty_rem_glid != null) {
			throw new bad_netmix(2);
		}
		nx_std_coref loc_gli = the_msgr.get_local_glid(get_owner());
		ck_glid(loc_gli);
		the_msgr.send_string(loc_gli.get_str());

		secty_rem_glid = the_msgr.recv_string();
		ck_glid(secty_rem_glid);
		nx_conn_id old_coid = dir_base.get_coid_by_ref(secty_rem_glid, null);

		boolean loc_has_coid = (old_coid != null);
		the_msgr.send_boolean_resp(loc_has_coid);

		boolean rem_has_coid = the_msgr.recv_boolean_resp();

		if (loc_has_coid != rem_has_coid) {
			throw new bad_netmix(2);
		}

		secty_coid = old_coid;

		if (secty_coid != null) {
			byte[] the_kk = get_coid_key(secty_coid);
			get_connection().set_cryper(the_kk);
		}

	}

	void recv_exch_glids() {
		if (secty_rem_glid != null) {
			throw new bad_netmix(2);
		}
		secty_rem_glid = the_msgr.recv_string();
		ck_glid(secty_rem_glid);
		nx_conn_id old_coid = dir_base.get_coid_by_ref(secty_rem_glid, null);

		nx_std_coref loc_gli = the_msgr.get_local_glid(get_owner());
		ck_glid(loc_gli);
		the_msgr.send_string(loc_gli.get_str());

		boolean rem_has_coid = the_msgr.recv_boolean_resp();

		boolean loc_has_coid = (old_coid != null);
		the_msgr.send_boolean_resp(loc_has_coid);

		if (loc_has_coid != rem_has_coid) {
			throw new bad_netmix(2);
		}

		secty_coid = old_coid;

		if (secty_coid != null) {
			byte[] the_kk = get_coid_key(secty_coid);
			get_connection().set_cryper(the_kk);
		}
	}

	private gamal_cipher get_gamal() {
		SecureRandom rnd = get_owner().new_SecureRandom();
		gamal_generator gg = get_gamal_sys();
		gamal_cipher gm = new gamal_cipher(gg, rnd);
		return gm;
	}

	private void send_agree_first_key() {
		if (secty_coid != null) {
			throw new bad_netmix(2);
		}
		secty_save_key = true;

		boolean loc_has_pre_kk = (secty_pre_key != null);
		the_msgr.send_boolean_resp(loc_has_pre_kk);

		boolean rem_has_pre_kk = the_msgr.recv_boolean_resp();

		if (loc_has_pre_kk != rem_has_pre_kk) {
			throw new bad_netmix(2);
		}

		if (loc_has_pre_kk) {
			get_connection().set_cryper(secty_pre_key);
			return;
		}

		gamal_cipher gm = get_gamal();

		String pub_str = gm.get_public_string();
		the_msgr.send_string(pub_str);

		String enc_key = the_msgr.recv_string();

		BigInteger skey = gm.decrypt(enc_key);
		byte[] fst_key = skey.toByteArray();

		get_connection().set_cryper(fst_key);
	}

	private void recv_agree_first_key() {
		if (secty_coid != null) {
			throw new bad_netmix(2);
		}
		secty_save_key = true;

		boolean rem_has_pre_kk = the_msgr.recv_boolean_resp();

		boolean loc_has_pre_kk = (secty_pre_key != null);
		the_msgr.send_boolean_resp(loc_has_pre_kk);

		if (loc_has_pre_kk != rem_has_pre_kk) {
			throw new bad_netmix(2);
		}

		if (loc_has_pre_kk) {
			get_connection().set_cryper(secty_pre_key);
			return;
		}

		String pub_str = the_msgr.recv_string();

		String[] enc_bd = new String[1];
		byte[] fst_key = find_new_first_cokey(pub_str, enc_bd);
		String enc_key = enc_bd[0];

		the_msgr.send_string(enc_key);

		get_connection().set_cryper(fst_key);
	}

	private void send_set_coid() {
		if (!get_connection().has_cryper()) {
			throw new bad_netmix(2);
		}
		if (secty_coid != null) {
			throw new bad_netmix(2);
		}
		nx_conn_id the_coid = new nx_conn_id();

		the_msgr.send_long(the_coid.as_long());

		secty_coid = the_coid;

		// verif

		the_msgr.send_long(secty_coid.as_long());

		long verif_co_val = the_msgr.recv_long();
		nx_conn_id rem_coid = new nx_conn_id(verif_co_val);

		if (!rem_coid.equals(secty_coid)) {
			throw new bad_netmix(2);
		}
	}

	private void recv_set_coid() {
		if (!get_connection().has_cryper()) {
			throw new bad_netmix(2);
		}
		if (secty_coid != null) {
			throw new bad_netmix(2);
		}

		long co_val = the_msgr.recv_long();
		nx_conn_id the_coid = new nx_conn_id(co_val);

		secty_coid = the_coid;

		// verif

		long verif_co_val = the_msgr.recv_long();
		nx_conn_id rem_coid = new nx_conn_id(verif_co_val);

		the_msgr.send_long(secty_coid.as_long());

		if (!rem_coid.equals(secty_coid)) {
			throw new bad_netmix(2);
		}
	}

	private void send_agree_session_key() {
		if (!get_connection().has_cryper()) {
			throw new bad_netmix(2);
		}
		byte[] se_key = find_sekey();
		the_msgr.send_bytes(se_key);

		get_connection().reset_cryper();
		get_connection().set_cryper(se_key);

		the_msgr.send_boolean_resp(secty_save_key);

		if (secty_save_key) {
			set_coid_key(secty_coid, se_key);
		}
	}

	private void recv_agree_session_key() {
		if (!get_connection().has_cryper()) {
			throw new bad_netmix(2);
		}
		byte[] se_key = the_msgr.recv_bytes();

		get_connection().reset_cryper();
		get_connection().set_cryper(se_key);

		secty_save_key = the_msgr.recv_boolean_resp();

		if (secty_save_key) {
			set_coid_key(secty_coid, se_key);
		}
	}

	public void send_set_secure(nx_std_coref expected_glid,
			nx_conn_id expected_coid, boolean need_sync) {
		the_msgr.send_string(MSG_SET_SECURE);

		String fst_str = the_msgr.recv_string();
		if (!fst_str.equals(MSG_SET_SECURE)) {
			throw new bad_netmix(2);
		}

		send_exch_glids();

		if (secty_coid == null) {
			recv_agree_first_key();
			recv_set_coid();
		}

		send_agree_session_key();

		ck_expected(expected_glid, expected_coid);

		boolean sync_files = the_msgr.send_mutual_cond(need_sync);
		if (sync_files) {
			nx_std_coref.send_glid(the_msgr, get_owner());
			nx_std_coref.send_alias(the_msgr, get_owner());
			send_location();
			send_locator_files();
		}
	}

	private void ck_expected(nx_std_coref expected_glid,
			nx_conn_id expected_coid) {
		if (secty_rem_glid == null) {
			throw new bad_netmix(2);
		}
		if (secty_coid == null) {
			throw new bad_netmix(2);
		}
		if ((expected_glid != null)
				&& !secty_rem_glid.equals(expected_glid.get_str())) {
			throw new bad_netmix(2);
		}
		if ((expected_coid != null) && !expected_coid.equals(secty_coid)) {
			throw new bad_netmix(2);
		}
	}

//	public void send_set_secure() {
//		send_set_secure(null, null, true);
//	}

	public void recv_set_secure(boolean need_sync) {
		String fst_str = the_msgr.recv_string();
		if (!fst_str.equals(MSG_SET_SECURE)) {
			throw new bad_netmix(2);
		}

		the_msgr.send_string(MSG_SET_SECURE);

		recv_exch_glids();

		if (secty_coid == null) {
			send_agree_first_key();
			send_set_coid();
		}

		recv_agree_session_key();

		boolean sync_files = the_msgr.recv_mutual_cond(need_sync);
		if (sync_files) {
			nx_std_coref.recv_glid(the_msgr, get_owner());
			nx_std_coref.recv_alias(the_msgr, get_owner());
			recv_location();
			recv_locator_files();
		}
	}

}
