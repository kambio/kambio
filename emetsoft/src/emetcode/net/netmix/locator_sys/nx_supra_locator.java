package emetcode.net.netmix.locator_sys;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import emetcode.crypto.bitshake.utils.convert;
import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.crypto.bitshake.utils.mem_file;
import emetcode.net.netmix.bad_netmix;
import emetcode.net.netmix.nx_std_coref;
import emetcode.net.netmix.nx_conn_id;
import emetcode.net.netmix.nx_connection;
import emetcode.net.netmix.nx_context;
import emetcode.net.netmix.nx_dir_base;
import emetcode.net.netmix.nx_messenger;
import emetcode.net.netmix.nx_peer;
import emetcode.net.netmix.nx_protector;
import emetcode.util.devel.logger;

public class nx_supra_locator {

	static boolean IN_DEBUG_1 = false;
	static boolean IN_DEBUG_2 = false;
	static boolean IN_DEBUG_3 = true; // set_supra_locator, supra_locate
	static boolean IN_DEBUG_4 = false; // send/recv files
	static boolean IN_DEBUG_5 = true; // no coid in send files
	static boolean IN_DEBUG_6 = true; // start errors
	static boolean IN_DEBUG_7 = false; // got conn
	static boolean IN_DEBUG_8 = true; // cannot set_supra_locator (null_coid)

	public static final long SUPRA_LOCATOR_PORT = 9000;
	public static final String SUPRA_LOCATOR_PORT_STR = "" + SUPRA_LOCATOR_PORT;
	public static final String SUPRA_LOCATOR_PORT_DESCR = nx_peer.PORT_SEP_STR
			+ SUPRA_LOCATOR_PORT;

	private static final String NEXT_LOCATOR_FNAM = "next_locator.dat";

	nx_dir_base dir_base;
	nx_std_coref local_glid;
	nx_peer local_peer;
	nx_peer remote_peer;
	private nx_conn_id remote_coid;
	private key_owner owner;
	private nx_messenger cli_msgr;

	public List<String> all_locators;
	public List<String> all_supra_locators;
	public String next_locator;

	public nx_supra_locator(nx_dir_base all_dirs, nx_peer loc_peer) {

		init_nx_supra_locator();

		if (all_dirs == null) {
			return;
		}

		dir_base = all_dirs;
		local_peer = loc_peer;
		remote_peer = null;
		remote_coid = null;
		owner = null;
		if (loc_peer != null) {
			owner = key_owner.get_copy(loc_peer.get_owner());
		}

		local_glid = dir_base.get_local_glid(owner);

		if (IN_DEBUG_1) {
			logger.debug("nx_coref_supra_locator. local_glid=" + local_glid);
		}
	}

	private void init_nx_supra_locator() {
		dir_base = null;
		local_glid = null;
		local_peer = null;
		remote_peer = null;
		remote_coid = null;
		owner = null;
		cli_msgr = null;

		all_locators = new ArrayList<String>();
		all_supra_locators = new ArrayList<String>();
		next_locator = null;
	}

	private void server_supra_locator(final nx_connection cnn) {
		if (IN_DEBUG_2) {
			logger.debug("one_conn_multi_mudp_server");
		}
		if (cnn != null) {
			if (dir_base == null) {
				throw new bad_netmix(2);
			}

			nx_peer loc_pp = cnn.get_local_peer();
			nx_messenger mgr = new nx_messenger(dir_base, cnn,
					nx_protector.NULL_VERIFIER);
			if (mgr.is_to_renew_connection_key()) {
				throw new bad_netmix(2);
			}
			if (IN_DEBUG_7) {
				logger.info("Supra_locator_server '" + loc_pp.get_description()
						+ "' got connection " + cnn);
			}

			mgr.recv_set_secure(true);

			send_supra_locator_files(mgr);

			cnn.close_net_connection();
		}
	}

	private Runnable get_supra_server(final nx_connection cnn) {
		if (IN_DEBUG_2) {
			logger.debug("get_supra_server");
		}
		Runnable rr1 = new Runnable() {
			public void run() {
				server_supra_locator(cnn);
			}
		};
		return rr1;
	}

	private void server_main() {
		while (local_peer.can_accept()) {
			nx_connection cnn = local_peer.accept();
			if (cnn != null) {
				String supr_nm = Thread.currentThread().getName() + "-one";
				nx_context.start_thread(supr_nm, get_supra_server(cnn), false);
			}
		}
	}

	private Runnable get_supra_locator_server() {
		if (IN_DEBUG_1) {
			logger.debug("get_supra_locator_server");
		}

		if (local_peer == null) {
			throw new bad_netmix(2);
		}
		String loc_po = local_peer.get_description_port();
		if (!SUPRA_LOCATOR_PORT_STR.equals(loc_po)) {
			throw new bad_netmix(2, "mudp supralocator port must be = "
					+ SUPRA_LOCATOR_PORT_STR);
		}

		Runnable rr1 = new Runnable() {
			public void run() {
				server_main();
			}
		};
		return rr1;
	}

	public Thread start_supra_locator_server() {
		if (local_peer == null) {
			if (IN_DEBUG_6) {
				logger.info("start_supra_locator_server. local_peer is null !!!!");
			}
			return null;
		}
		if (!local_peer.can_accept()) {
			if (IN_DEBUG_6) {
				logger.info("start_supra_locator_server. local_peer cannot accept !!!!!");
			}
			return null;
		}
		if (!dir_base.has_gamal_sys(owner)) {
			// nedded in during set secure
			throw new bad_netmix(2);
		}

		String thd_nm = Thread.currentThread().getName() + "-sultor-srv-"
				+ local_peer.get_description();
		Thread loc_srv = nx_context.start_thread(thd_nm,
				get_supra_locator_server(), false);
		return loc_srv;
	}

	public void set_supra_locator(String rem_coref) {
		if (has_supra_locator()) {
			throw new bad_netmix(2);
		}
		if (local_peer == null) {
			throw new bad_netmix(2);
		}
		if (dir_base == null) {
			throw new bad_netmix(2);
		}
		if (rem_coref == null) {
			throw new bad_netmix(2);
		}

		String remote_coref = nx_supra_locator.set_port(rem_coref);

		nx_context ctx = local_peer.get_context();
		nx_peer pp2 = ctx.make_peer();
		pp2.init_remote_peer(remote_coref, null);

		remote_peer = pp2;

		String rem_po = remote_peer.get_description_port();
		if (!SUPRA_LOCATOR_PORT_STR.equals(rem_po)) {
			throw new bad_netmix(2, SUPRA_LOCATOR_PORT_STR + " != " + rem_po);
		}

		remote_coid = dir_base.get_coid_by_ref(remote_coref, null);

		nx_connection cnn = local_peer.connect_to(remote_peer);
		if (cnn != null) {
			cli_msgr = new nx_messenger(dir_base, cnn,
					nx_protector.NULL_VERIFIER);
		}

		if (IN_DEBUG_3) {
			logger.debug("set_supra_locator. remote_coid=" + remote_coid);
		}
	}

	public boolean has_supra_locator() {
		boolean hh = (cli_msgr != null);
		return hh;
	}

	public void cancel_client_connection() {
		if (cli_msgr != null) {
			cli_msgr.get_connection().stop_net_connection();
		}
	}

	private void send_supra_locator_files(nx_messenger srv_msgr) {
		String coref_str = srv_msgr.recv_string();

		if (IN_DEBUG_4) {
			logger.debug("send_supra_locator_files. coref_str=" + coref_str);
		}

		key_owner owr = srv_msgr.get_owner();

		String nx_loctr = read_next_locator(dir_base, owr);
		if (coref_str == null) {
			srv_msgr.send_string(nx_loctr);
			return;
		}

		nx_conn_id the_req_coid = null;

		the_req_coid = dir_base.get_coid_by_ref(coref_str, null);
		if (IN_DEBUG_5) {
			if (the_req_coid == null) {
				logger.info("CANNOT_FIND_COID_FOR=" + coref_str + " IN\n\t"
						+ dir_base);
			}
		}

		byte[] rem_ltors_bts = null;
		if (the_req_coid != null) {
			File rem_ltors_ff = nx_connector.get_remote_locators_file(dir_base,
					the_req_coid);
			rem_ltors_bts = mem_file.concurrent_read_encrypted_bytes(
					rem_ltors_ff, owr);
		}
		srv_msgr.send_bytes(rem_ltors_bts);

		byte[] rem_su_ltors_bts = null;
		if (the_req_coid != null) {
			File rem_su_ltors_ff = nx_connector.get_remote_supra_locators_file(
					dir_base, the_req_coid);
			rem_su_ltors_bts = mem_file.concurrent_read_encrypted_bytes(
					rem_su_ltors_ff, owr);
		}
		srv_msgr.send_bytes(rem_su_ltors_bts);

		srv_msgr.send_string(nx_loctr);
	}

	private void recv_supra_locator_files_for(String coref_str) {
		if (IN_DEBUG_4) {
			logger.debug("recv_supra_locator_files_for=" + coref_str);
		}

		cli_msgr.send_string(coref_str);

		if (coref_str == null) {
			next_locator = cli_msgr.recv_string();
			return;
		}

		byte[] rem_ltors_bts = cli_msgr.recv_bytes();
		byte[] rem_su_ltors_bts = cli_msgr.recv_bytes();
		next_locator = cli_msgr.recv_string();

		all_locators = convert.bytes_to_string_list(rem_ltors_bts);
		all_supra_locators = convert.bytes_to_string_list(rem_su_ltors_bts);

		if (all_locators == null) {
			all_locators = new ArrayList<String>();
		}
		if (all_supra_locators == null) {
			all_supra_locators = new ArrayList<String>();
		}
	}

	public nx_conn_id supra_locate(String coref_str) {
		if (IN_DEBUG_3) {
			logger.debug("supra_locate. remote_coid=" + remote_coid);
		}
		if (!has_supra_locator()) {
			throw new bad_netmix(2, "programing error. call set_supra_locator first");
		}
		// if (remote_coid == null) {
		// throw new bad_netmix(2);
		// }

		cli_msgr.send_set_secure(null, remote_coid, true);

		nx_conn_id the_coid = cli_msgr.get_coid();

		recv_supra_locator_files_for(coref_str);

		cli_msgr.get_connection().close_net_connection();

		return the_coid;
	}

	public static String set_port(String descr) {
		return nx_peer.set_description_port(SUPRA_LOCATOR_PORT_DESCR, descr);
	}

	private static File get_next_locator_file(nx_dir_base dir_b) {
		File loc_dd = dir_b.get_local_nx_dir();
		File ltor_ff = new File(loc_dd, NEXT_LOCATOR_FNAM);
		return ltor_ff;
	}

	public static boolean has_next_locator(nx_dir_base dir_b) {
		File ff = get_next_locator_file(dir_b);
		return ff.exists();
	}

	public static String read_next_locator(nx_dir_base dir_b, key_owner owr) {
		File ff = get_next_locator_file(dir_b);
		String vv = mem_file.read_encrypted_string(ff, owr);
		return vv;
	}

	public static void write_next_locator(nx_dir_base dir_b, key_owner owr,
			String nxt_loctr) {
		File ff = get_next_locator_file(dir_b);
		mem_file.write_encrypted_string(ff, owr, nxt_loctr);
	}

}
