package emetcode.net.netmix;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import emetcode.crypto.bitshake.utils.convert;
import emetcode.crypto.bitshake.utils.file_funcs;
import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.crypto.bitshake.utils.mem_file;
import emetcode.crypto.bitshake.utils.mer_twist;
import emetcode.net.netmix.locale.L;
import emetcode.net.netmix.locator_sys.nx_locators_verifier;
import emetcode.util.devel.logger;

public class nx_messenger {

	static boolean IN_DEBUG_2 = true; // do not permit path delete
	static boolean IN_DEBUG_3 = false; // generate fake exceptions
	static boolean IN_DEBUG_8 = false; // boolean resp and send_if_diff

	static final mer_twist dbg_rnd = new mer_twist();

	private static final char FIX_SEP = '*';
	private static final char FLD_SEP = '.';

	private static final Pattern FLD_SEP_PATT = Pattern.compile("\\.");

	// private static final int MAX_COREF_STR_SZ = 100;

	private static final String MSG_FNM1 = "nx_msg_send_files_1";
	// private static final String MSG_FNM2 = "nx_msg_send_files_2";
	private static final String MSG_BOOLEAN_RESP = "nx_msg_boolean_resp";

	private static final String UTF_8_NAM = "UTF-8";
	private static final Charset UTF_8 = Charset.forName(UTF_8_NAM);

	private nx_dir_base dir_base;

	private nx_connection my_connection;
	private nx_protector ssc_protect;

	public nx_messenger(nx_dir_base b_dir, nx_connection conn,
			nx_locators_verifier loc_v) {
		init_nx_messenger(b_dir, conn, loc_v);
	}

	private void init_nx_messenger(nx_dir_base b_dir, nx_connection conn,
			nx_locators_verifier loc_v) {
		my_connection = conn;
		dir_base = b_dir;

		ssc_protect = new nx_protector(this, loc_v);
	}

	public boolean write_coref(String coref_str, nx_conn_id the_coid) {
		return dir_base.write_coref(coref_str, the_coid);
	}

	private static void delete_long_path_file(File by_coref_dd, File ref_ff) {
		file_funcs.path_delete(by_coref_dd, ref_ff);
		logger.info("Deleted_LONG_PATH file='" + ref_ff + "'");
	}

	private static boolean dbg_throw_fake_exception() {
		int vv = dbg_rnd.nextInt();
		long bool_1 = convert.to_interval(vv, 0, 2);
		return (bool_1 == 1);
	}

	// PUBLIC_METHODS

	public int get_net_type() {
		return get_context().get_net_type();
	}

	public void send_mem_file(File ff) {
		send_encrypted_mem_file(ff, null);
	}

	public void send_encrypted_mem_file(File ff, key_owner owr) {
		byte[] bts = mem_file.concurrent_read_encrypted_bytes(ff, owr);
		send_bytes(bts);
	}

	public void recv_mem_file(File ff) {
		recv_encrypted_mem_file(ff, null);
	}

	public void recv_encrypted_mem_file(File ff, key_owner owr) {
		byte[] bts = recv_bytes();
		if (bts != null) {
			mem_file.concurrent_write_encrypted_bytes(ff, owr, bts);
		}
	}

	public void send_string(String the_str) {
		if (config.DEBUG) {
			logger.debug("Sending str=" + the_str);
		}

		if (the_str == null) {
			send_bytes(null);
			return;
		}
		byte[] bt_arr = the_str.getBytes(UTF_8);
		send_bytes(bt_arr);
	}

	public String recv_string() {
		byte[] arr = recv_bytes();
		if (arr == null) {
			return null;
		}
		String the_str = new String(arr, UTF_8);

		if (config.DEBUG) {
			logger.debug("Received str=" + the_str);
		}
		return the_str;
	}

	public void send_long(long val) {
		if (config.DEBUG) {
			logger.debug("Sending long=" + val);
		}

		byte[] bt_arr = convert.to_byte_array(val);
		send_bytes(bt_arr);
	}

	public long recv_long() {
		byte[] arr = recv_bytes();
		if (arr == null) {
			throw new bad_netmix(2);
		}
		long val = convert.to_long(arr);

		if (config.DEBUG) {
			logger.debug("Received long=" + val);
		}
		return val;
	}

	public byte[] recv_bytes() {
		return get_connection().secure_recv_bytes();
	}

	public void send_bytes(byte[] msg) {
		get_connection().secure_send_bytes(msg);
	}

	public void send_mem_files(File[] arr_ff) {
		send_encrypted_mem_files(arr_ff, null);
	}

	public void send_encrypted_mem_files(File[] arr_ff, key_owner owr) {
		String msg1 = MSG_FNM1 + FLD_SEP + arr_ff.length;
		send_string(msg1);

		for (int aa = 0; aa < arr_ff.length; aa++) {
			File ff = arr_ff[aa];
			String fx_nm = ff.getName().replace(FLD_SEP, FIX_SEP);
			send_string(fx_nm);

			send_encrypted_mem_file(ff, owr);
		}
	}

	public File[] recv_mem_files(File dir) {
		return recv_encrypted_mem_files(dir, null);
	}

	public File[] recv_encrypted_mem_files(File dir, key_owner owr) {
		return recv_encrypted_mem_files(dir, owr, false);
	}

	// @SuppressWarnings("unused")
	public File[] recv_encrypted_mem_files(File dir, key_owner owr,
			boolean sliced) {
		dir = file_funcs.get_dir(dir);

		String msg1 = recv_string();

		Scanner s1 = new Scanner(msg1);
		s1.useDelimiter(FLD_SEP_PATT);

		String msg_fnm1 = s1.next();
		if (!msg_fnm1.equals(MSG_FNM1)) {
			throw new bad_netmix(2, String.format(L.cannot_recv_mem_files,
					msg_fnm1));
		}

		int nf = convert.parse_int(s1.next());
		File[] all_ff = new File[nf];
		for (int aa = 0; aa < nf; aa++) {
			String ff_nm = recv_string();
			String fx_nm = ff_nm.replace(FIX_SEP, FLD_SEP);
			if (sliced) {
				fx_nm = file_funcs.as_sliced_file_path(fx_nm);
			}

			if (IN_DEBUG_3) {
				if (dbg_throw_fake_exception()) {
					throw new bad_netmix(2,
							"\n\n\nFAKE_EXCEPTION_FAKE_EXCEPTION_FAKE_EXCEPTION\n\n\n");
				}
			}

			File full_ff = new File(dir, fx_nm);
			all_ff[aa] = full_ff;
			if (sliced) {
				file_funcs.mk_parent_dir(full_ff);
			}
			recv_encrypted_mem_file(full_ff, owr);
		}
		return all_ff;
	}

	public static void delete_coid(nx_dir_base bdir, nx_conn_id coid) {
		if (IN_DEBUG_2) {
			throw new bad_netmix(2, "TRYING_TO_DELETE_COID=" + coid);
		}
		File all_coids_dd = bdir.get_all_coids_base_dir();
		if (all_coids_dd == null) {
			return;
		}

		//File ff = nx_protector.get_coid_dir(all_coids_dd, coid);
		File ff = bdir.get_remote_nx_dir(coid);
		if (!ff.exists()) {
			return;
		}
		File pnt = ff.getParentFile();

		// try {
		// String descr = nx_protector.read_last_remote_descr_in(ff);
		//
		// if (descr != null) {
		// delete_coref_in(by_coref_dd, descr, coid);
		// File frm_ff = get_coref_confirm_file(by_coref_dd, descr,
		// coid);
		// delete_long_path_file(by_coref_dd, frm_ff);
		// }
		// } catch (bad_emetcode ex) {
		// logger.debug("Could register deletion of coid '" + coid + "'");
		// }

		file_funcs.delete_dir(ff);
		if (pnt != null) {
			delete_long_path_file(all_coids_dd, pnt);
		}

		logger.info("Deleted coid '" + coid + "'");
	}

	public static void delete_all_coids_in_data(nx_dir_base bdir,
			List<nx_conn_id> the_coids) {
		//File all_coids_dd = bdir.get_all_coids_base_dir();
		for (nx_conn_id the_coid : the_coids) {
			//File ff = nx_protector.get_coid_dir(all_coids_dd, the_coid);
			File ff = bdir.get_remote_nx_dir(the_coid);
			if (ff.exists()) {
				delete_coid(bdir, the_coid);
			}
		}
	}

	public nx_context get_context() {
		return get_connection().get_local_peer().get_context();
	}

	public nx_connection get_connection() {
		return my_connection;
	}

	public nx_conn_id get_coid() {
		return ssc_protect.get_coid();
	}

	public String get_remote_descr() {
		return get_connection().get_remote_peer().get_description();
	}

	public void set_agreed_key(byte[] key) {
		ssc_protect.set_agreed_key(key);
	}

	public boolean has_secure_conn() {
		return ssc_protect.has_secure_conn();
	}

	public nx_dir_base get_dir_base() {
		return dir_base;
	}

	public void set_net_base_dir(nx_dir_base bdir) {
		ssc_protect.set_net_base_dir(bdir);
		if (bdir == null) {
			throw new bad_netmix(2);
		}
		dir_base = bdir;
	}

	public File get_coid_file() {
		File ff = dir_base.get_coid_file(get_coid());
		return ff;
	}

	public boolean is_to_renew_connection_key() {
		return ssc_protect.is_to_renew_connection_key();
	}

	public void set_to_renew_connection_key() {
		ssc_protect.set_to_renew_connection_key();
	}

	public void set_to_keep_connection_key() {
		ssc_protect.set_to_keep_connection_key();
	}

	public void send_set_secure(nx_std_coref expected_glid,
			nx_conn_id expected_coid, boolean needs_sync) {
		ssc_protect.send_set_secure(expected_glid, expected_coid, needs_sync);
	}

//	public void send_set_secure() {
//		ssc_protect.send_set_secure();
//	}
//
	public void recv_set_secure(boolean needs_sync) {
		ssc_protect.recv_set_secure(needs_sync);
	}

	public File get_local_nx_dir() {
		return dir_base.get_local_nx_dir();
	}

	public File get_remote_nx_dir() {
		return dir_base.get_remote_nx_dir(get_coid());
	}

	public void send_boolean_resp(boolean diff_ok) {
		int resp = 0;
		if (diff_ok) {
			resp = 100;
		}
		String msg1 = MSG_BOOLEAN_RESP + FLD_SEP + resp;
		send_string(msg1);

		if (IN_DEBUG_8) {
			logger.debug("send_boolean_resp_ok." + msg1);
		}
	}

	public boolean recv_boolean_resp() {
		String msg1 = recv_string();

		Scanner s1 = new Scanner(msg1);
		s1.useDelimiter(FLD_SEP_PATT);

		String msg_fnm1 = s1.next();
		if (!msg_fnm1.equals(MSG_BOOLEAN_RESP)) {
			throw new bad_netmix(2, String.format(L.bad_message_name, msg_fnm1));
		}

		int resp = Integer.parseInt(s1.next());
		if (IN_DEBUG_8) {
			logger.debug("recv_boolean_resp_ok." + resp);
		}

		if (resp > 0) {
			return true;
		}
		return false;
	}

	public boolean send_mutual_cond(boolean cond) {
		send_boolean_resp(cond);
		boolean rem_cond = recv_boolean_resp();
		if(! cond && !rem_cond){
			return false;
		}
		return true;
	}
	
	public boolean recv_mutual_cond(boolean cond) {
		boolean rem_cond = recv_boolean_resp();
		send_boolean_resp(cond);
		if(! cond && !rem_cond){
			return false;
		}
		return true;
	}
	
	public void send_file_if_diff(File ff, key_owner owr) {
		String ff_sha = mem_file.try_calc_content_sha_str(ff, owr);
		send_string(ff_sha);

		boolean are_eq = recv_boolean_resp();
		if (!are_eq) {
			send_encrypted_mem_file(ff, owr);
		}
	}

	public boolean recv_file_if_diff(File ff, key_owner owr, File recv_ff) {
		String new_ff_sha = recv_string();
		String old_ff_sha = mem_file.try_calc_content_sha_str(ff, owr);

		boolean are_eq = old_ff_sha.equals(new_ff_sha);
		send_boolean_resp(are_eq);
		if (!are_eq) {
			if (recv_ff != null) {
				recv_encrypted_mem_file(recv_ff, owr);
			} else {
				recv_encrypted_mem_file(ff, owr);
			}
			if (IN_DEBUG_8) {
				if (ff.exists()) {
					logger.debug("recv_file_if_diff RECVED file '" + ff + "'");
				}
			}
		}
		return (!are_eq);
	}

	public key_owner get_owner() {
		return get_connection().get_local_peer().get_owner();
	}

	public nx_std_coref get_local_glid(key_owner owr) {
		return dir_base.get_local_glid(owr);
	}

	public nx_std_coref get_remote_glid(nx_conn_id the_coid, key_owner owr) {
		return dir_base.get_remote_glid(the_coid, owr);
	}

	public nx_std_coref get_remote_glid() {
		return dir_base.get_remote_glid(get_coid(), get_owner());
	}

}
