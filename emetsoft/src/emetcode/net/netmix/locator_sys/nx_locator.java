package emetcode.net.netmix.locator_sys;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import emetcode.crypto.bitshake.bitshaker;
import emetcode.crypto.bitshake.utils.file_funcs;
import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.crypto.bitshake.utils.mem_file;
import emetcode.net.netmix.bad_netmix;
import emetcode.net.netmix.nx_std_coref;
import emetcode.net.netmix.nx_conn_id;
import emetcode.net.netmix.nx_connection;
import emetcode.net.netmix.nx_context;
import emetcode.net.netmix.nx_dir_base;
import emetcode.net.netmix.nx_peer;
import emetcode.net.netmix.nx_responder;
import emetcode.util.devel.bad_emetcode;
import emetcode.util.devel.dbg_slow;
import emetcode.util.devel.logger;
import emetcode.util.devel.thread_funcs;

public class nx_locator implements nx_responder {

	class nx_locator_entry {
		String addr;
		long read_tm;
	}

	class nx_locator_coid_entry {
		String rq_subor_coref;
		long rq_boss_coid;
		List<BlockingQueue<Long>> all_waiting;
	}

	static boolean IN_DEBUG_1 = false;
	static boolean IN_DEBUG_2 = true; // IN_REQ && OUT_REQ
	static boolean IN_DEBUG_3 = true; // make_request (cliente side)
	static boolean IN_DEBUG_4 = true; // set_addr
	static boolean IN_DEBUG_5 = true; // null response
	static boolean IN_DEBUG_6 = false; // get_addr
	static boolean IN_DEBUG_7 = false; // null-request
	static boolean IN_DEBUG_8 = true; // locator started
	static boolean IN_DEBUG_9 = true; // null cli conn
	static boolean IN_DEBUG_10 = true; // bad report
	static boolean IN_DEBUG_11 = false; // slow get_response
	static boolean IN_DEBUG_12 = false; // write ok
	static boolean IN_DEBUG_13 = true; // bad_emetcode in get_response
	static boolean IN_DEBUG_14 = true; // bad_emetcode in get_set_coid_main
	static boolean IN_DEBUG_15 = true; // verif_subor
	static boolean IN_DEBUG_16 = true; // verif_subor 2
	static boolean IN_DEBUG_17 = true; // asked for subor
	static boolean IN_DEBUG_18 = false; // get service

	public static final long LOCATOR_PORT = 8000;
	public static final String LOCATOR_PORT_STR = "" + LOCATOR_PORT;
	public static final String LOCATOR_PORT_DESCR = nx_peer.PORT_SEP_STR
			+ LOCATOR_PORT;

	private static final int MAX_ACTIVE_COID_THDS = 100;
	private static final int MAX_ACTIVE_WAITING_COID_ENTRIES = 100;

	private static final int POLL_COID_WAITER_TIMEOUT_SECS = 60;

	private static final String REPORTED_ADDR_FNAM = "reported_addr.dat";
	private static final String OBSERVED_ADDR_FNAM = "observed_addr.dat";

	private Map<String, nx_locator_entry> all_reported;
	private Map<String, nx_locator_entry> all_observed;

	private BlockingQueue<nx_locator_coid_entry> set_waiting_for_coid;
	private Map<String, nx_locator_coid_entry> all_waiting_for_coid;

	nx_dir_base dir_base;
	nx_std_coref local_glid;
	nx_std_coref local_alias;
	nx_std_coref local_boss;
	nx_peer local_peer;
	nx_peer remote_peer;
	key_owner owner;
	Map<String, String> other_service_ports;

	nx_conn_id cli_remote_coid;
	private byte[] cli_enc_key;
	nx_connection cli_conn;
	AtomicBoolean cli_canceled;

	AtomicInteger num_active_thds;

	public nx_locator(nx_dir_base all_dirs, key_owner owr, nx_peer loc_peer) {

		init_nx_locator();

		if (all_dirs == null) {
			return;
		}

		all_reported = new TreeMap<String, nx_locator_entry>();
		all_observed = new TreeMap<String, nx_locator_entry>();

		set_waiting_for_coid = new LinkedBlockingQueue<nx_locator_coid_entry>(
				MAX_ACTIVE_WAITING_COID_ENTRIES);
		all_waiting_for_coid = new TreeMap<String, nx_locator_coid_entry>();

		dir_base = all_dirs;
		local_peer = loc_peer;
		remote_peer = null;
		owner = owr;

		local_glid = dir_base.get_local_glid(owner);
		local_alias = dir_base.get_local_alias(owner);

		File nx_dd = dir_base.get_local_nx_dir();
		local_boss = nx_std_coref.get_boss(nx_dd, owner);

		if (IN_DEBUG_1) {
			logger.debug("nx_coref_locator. local_glid=" + local_glid
					+ " local_alias=" + local_alias);
		}
	}

	private void init_nx_locator() {
		all_reported = null;
		all_observed = null;

		dir_base = null;
		local_peer = null;
		owner = null;
		other_service_ports = null;

		cli_remote_coid = null;
		cli_enc_key = null;
		cli_conn = null;
		cli_canceled = new AtomicBoolean(false);

		num_active_thds = new AtomicInteger(0);
	}

	private File get_reported_addr_file(String coref_str) {
		File nm_dd = dir_base.get_coid_dir_by_ref(coref_str);
		if (nm_dd == null) {
			return null;
		}
		File ff = new File(nm_dd, REPORTED_ADDR_FNAM);
		return ff;
	}

	private File get_observed_addr_file(String coref_str) {
		File nm_dd = dir_base.get_coid_dir_by_ref(coref_str);
		if (nm_dd == null) {
			return null;
		}
		File ff = new File(nm_dd, OBSERVED_ADDR_FNAM);
		return ff;
	}

	private File get_reported_addr_file(nx_conn_id the_coid) {
		File nm_dd = dir_base.get_remote_nx_dir(the_coid);
		File ff = new File(nm_dd, REPORTED_ADDR_FNAM);
		return ff;
	}

	private File get_observed_addr_file(nx_conn_id the_coid) {
		File nm_dd = dir_base.get_remote_nx_dir(the_coid);
		File ff = new File(nm_dd, OBSERVED_ADDR_FNAM);
		return ff;
	}

	private String get_addr(Map<String, nx_locator_entry> mm, File addr_ff,
			String coref_str) {
		if (IN_DEBUG_6) {
			logger.info("get_addr. addr_ff=" + addr_ff + " coref_str="
					+ coref_str);
		}
		if (addr_ff == null) {
			return null;
		}
		long wrt_tm = addr_ff.lastModified();
		if (wrt_tm == 0) {
			return null;
		}

		String the_nm = coref_str;
		if (mm.containsKey(the_nm)) {
			nx_locator_entry ee = mm.get(the_nm);
			if (wrt_tm <= ee.read_tm) {
				return ee.addr;
			}
		}

		String addr = mem_file.read_encrypted_string(addr_ff, owner);
		nx_locator_entry n_ee = new nx_locator_entry();
		n_ee.addr = addr;
		n_ee.read_tm = wrt_tm;
		mm.put(the_nm, n_ee);
		return addr;
	}

	private void set_addr(Map<String, nx_locator_entry> mm, File addr_ff,
			String the_addr) {
		if (IN_DEBUG_4) {
			logger.info("set_addr=" + the_addr + " in_file=" + addr_ff);
		}
		if (addr_ff == null) {
			return;
		}
		file_funcs.mk_parent_dir(addr_ff);
		mem_file.write_encrypted_string(addr_ff, owner, the_addr);
		if (IN_DEBUG_12) {
			String r_addr = mem_file.read_encrypted_string(addr_ff, owner);
			if (!r_addr.equals(the_addr)) {
				logger.info("WRITE_FAILED !!!!!!\nWRITE_FAILED !!!!!!\nWRITE_FAILED !!!!!!\n");
			}
		}
	}

	public String get_reported_addr(String coref_str) {
		File nm_ff = get_reported_addr_file(coref_str);
		String addr = get_addr(all_reported, nm_ff, coref_str);
		return addr;
	}

	public String get_observed_addr(String coref_str) {
		File nm_ff = get_observed_addr_file(coref_str);
		String addr = get_addr(all_observed, nm_ff, coref_str);
		return addr;
	}

	public void set_reported_addr(String coref_str, String the_addr) {
		File nm_ff = get_reported_addr_file(coref_str);
		set_addr(all_reported, nm_ff, the_addr);
	}

	public void set_observed_addr(String coref_str, String the_addr) {
		File nm_ff = get_observed_addr_file(coref_str);
		set_addr(all_observed, nm_ff, the_addr);
	}

	public void set_reported_addr(nx_conn_id coid, String the_addr) {
		File nm_ff = get_reported_addr_file(coid);
		set_addr(all_reported, nm_ff, the_addr);
	}

	public void set_observed_addr(nx_conn_id coid, String the_addr) {
		File nm_ff = get_observed_addr_file(coid);
		set_addr(all_observed, nm_ff, the_addr);
	}

	private void server_main() {
		if (IN_DEBUG_8) {
			logger.debug("locator_started_in=" + local_peer.get_description());
		}
		while (local_peer.can_respond()) {
			local_peer.respond();
		}
	}

	private Runnable get_locator_server() {
		if (IN_DEBUG_1) {
			logger.debug("get_locator_server");
		}
		if (local_peer == null) {
			throw new bad_netmix(2);
		}

		String loc_po = local_peer.get_description_port();
		if (!LOCATOR_PORT_STR.equals(loc_po)) {
			throw new bad_netmix(2, "mudp locator port must be = "
					+ LOCATOR_PORT_STR);
		}

		local_peer.init_responder(this);

		Runnable rr1 = new Runnable() {
			public void run() {
				server_main();
			}
		};
		return rr1;
	}

	public Thread start_locator_server(Map<String, String> other_srvs) {
		other_service_ports = other_srvs;
		String thd_nm = Thread.currentThread().getName() + "-locator_server-"
				+ local_peer.get_description();
		Thread loc_srv = nx_context.start_thread(thd_nm, get_locator_server(),
				false);
		return loc_srv;
	}

	public void set_locator(nx_peer rem_peer, nx_conn_id use_coid) {
		if (has_locator()) {
			throw new bad_netmix(2);
		}
		if (local_peer == null) {
			throw new bad_netmix(2);
		}
		if (dir_base == null) {
			throw new bad_netmix(2);
		}
		if (rem_peer == null) {
			throw new bad_netmix(2);
		}
		remote_peer = rem_peer;

		String rem_po = remote_peer.get_description_port();
		if (!LOCATOR_PORT_STR.equals(rem_po)) {
			throw new bad_netmix(2, LOCATOR_PORT_STR + " != " + rem_po + " IN "
					+ remote_peer.get_description());
		}

		if (use_coid != null) {
			cli_remote_coid = use_coid;
		}

		if (cli_remote_coid == null) {
			cli_remote_coid = dir_base.get_coid_by_ref(remote_peer.get_description(), null);
		}

		cli_enc_key = null;

		if (cli_remote_coid != null) {
			// File all_coids_dd = dir_base.get_all_coids_base_dir();
			File coid_ff = dir_base.get_coid_file(cli_remote_coid);
			if (!coid_ff.exists()) {
				if (IN_DEBUG_1) {
					logger.debug("File not found " + coid_ff);
				}
				return;
			}
			cli_enc_key = mem_file.concurrent_read_encrypted_bytes(coid_ff,
					owner);

			if (cli_enc_key == null) {
				return;
			}
		}

		if (IN_DEBUG_1) {
			if (cli_enc_key == null) {
				logger.debug("cli_enc_key == null");
			}
			if (cli_remote_coid == null) {
				logger.debug("set_locator. remote_coid == null."
						+ " remote_glid=" + remote_peer.get_description()
						+ " dir_base=" + dir_base.get_netmix_base_dir());
			}
		}

		cli_conn = local_peer.get_requester(rem_peer, cli_remote_coid);
	}

	public boolean has_encryption_key() {
		return (cli_enc_key != null);
	}

	public boolean has_locator() {
		boolean hh = (cli_conn != null);
		return hh;
	}

	public void cancel_client_request() {
		if (IN_DEBUG_9) {
			if (cli_conn == null) {
				logger.info("cancel_client_request. got_null_cli_conn");
			}
		}
		if (cli_conn != null) {
			cli_canceled.set(true);
			cli_conn.stop_net_connection();

			if (!cli_conn.is_closed()) {
				throw new bad_netmix(2, "cancel_client_request FAILED !!!");
			}
		}
	}

	private nx_location_request make_request(nx_location_request srv_req) {
		if (IN_DEBUG_3) {
			logger.info("Starting make_request");
		}
		if (!has_locator()) {
			throw new bad_netmix(2);
		}

		byte[] req_dat = srv_req.get_ask_msg();
		if (req_dat == null) {
			return null;
		}

		byte[] req_msg = req_dat;
		if (cli_enc_key != null) {
			req_msg = bitshaker.encrypt_bytes_with_sha(req_dat, cli_enc_key);
			if (req_msg == null) {
				return null;
			}
		}

		byte[] resp_msg = cli_conn.request(req_msg);
		if (resp_msg == null) {
			if (IN_DEBUG_7) {
				logger.info("GOT_NULL_REQUEST cli_conn=" + cli_conn);
			}
			return null;
		}

		byte[] resp_dat = resp_msg;
		if (cli_enc_key != null) {
			resp_dat = bitshaker.decrypt_bytes_with_sha(resp_msg, cli_enc_key);
			if (resp_dat == null) {
				return null;
			}
		}
		srv_req.set_answ_msg(resp_dat);

		if (IN_DEBUG_3) {
			logger.info("GOT_REQ_RESP=\n>>>>\n" + srv_req.toString()
					+ "\n<<<<<<<\n\n");
		}

		return srv_req;
	}

	private nx_location_request ask_coid(String curr_addr, String rq_coref) {
		nx_location_request srv_req = new nx_location_request();
		srv_req.msg_code = nx_location_request.ASK_COID_OPER;
		srv_req.req_coref_str = rq_coref;
		srv_req.forward_nx_addr = curr_addr;
		nx_std_coref the_gli = dir_base.get_local_glid(owner);
		if (the_gli != null) {
			srv_req.glid_check = the_gli.get_str();
		}

		nx_location_request ans_req = make_request(srv_req);
		return ans_req;
	}

	private nx_location_request ask_set_coid(String rq_coref,
			nx_conn_id the_coid) {
		nx_location_request srv_req = new nx_location_request();
		srv_req.msg_code = nx_location_request.ASK_SET_COID_OPER;
		srv_req.req_coref_str = rq_coref;
		srv_req.coid_resp = the_coid.as_long();

		nx_location_request ans_req = make_request(srv_req);
		return ans_req;
	}

	public nx_location_request ask_locate(String rq_coref, boolean rq_and_repo) {
		nx_location_request srv_req = new nx_location_request();
		srv_req.msg_code = nx_location_request.ASK_LOCATE_OPER;
		srv_req.req_coref_str = rq_coref;
		if (rq_and_repo) {
			srv_req.src_reported_nx_addr = local_peer.get_description();
		}

		nx_location_request ans_req = make_request(srv_req);
		return ans_req;
	}

	nx_location_request ask_service(String rq_coref, String rq_service,
			boolean rq_and_repo) {
		nx_location_request srv_req = new nx_location_request();
		srv_req.msg_code = nx_location_request.ASK_SERVICE_OPER;
		srv_req.req_service = rq_service;
		srv_req.req_coref_str = rq_coref;
		if (rq_and_repo) {
			srv_req.src_reported_nx_addr = local_peer.get_description();
		}

		nx_location_request ans_req = make_request(srv_req);
		return ans_req;
	}

	private boolean is_local_coref(String req_gli) {
		if (req_gli == null) {
			return false;
		}
		if (local_glid == null) {
			return false;
		}
		if (req_gli.equals(local_glid.get_str())) {
			return true;
		}
		if (local_alias == null) {
			return false;
		}
		if (req_gli.equals(local_alias.get_str())) {
			return true;
		}
		return false;
	}

	public static String set_port(String descr) {
		return nx_peer.set_description_port(LOCATOR_PORT_DESCR, descr);
	}

	public String get_remote_description() {
		return cli_conn.get_remote_peer().get_description();
	}

	private nx_location_request get_locate_oper_response(nx_peer cli_peer,
			nx_conn_id curr_coid, nx_location_request cli_req) {

		String cli_descr = cli_peer.get_description();
		cli_req.src_observed_nx_addr = cli_descr;

		if (IN_DEBUG_2) {
			logger.info("IN_REQ=\n" + cli_req.toString());
		}

		if (curr_coid != null) {
			if (cli_req.src_reported_nx_addr != null) {
				set_reported_addr(curr_coid, cli_req.src_reported_nx_addr);
				set_observed_addr(curr_coid, cli_descr);
			}
		}

		String req_str = cli_req.req_coref_str;
		if (!is_local_coref(req_str)) {
			cli_req.dest_observed_nx_addr = get_observed_addr(req_str);
			cli_req.dest_reported_nx_addr = get_reported_addr(req_str);
		} else {
			cli_req.set_as_target();
		}

		if (IN_DEBUG_10) {
			nx_std_coref rem_gli = null;
			if (curr_coid != null) {
				rem_gli = dir_base.get_remote_glid(curr_coid, owner);
			}
			boolean is_repor = ((rem_gli != null) && (req_str != null) && req_str
					.equals(rem_gli.get_str()));
			if (is_repor) {
				String s1 = cli_req.src_reported_nx_addr;
				String s2 = cli_req.dest_reported_nx_addr;
				boolean no_good = false;
				if (s1 == null) {
					no_good = true;
				} else if (s2 == null) {
					no_good = true;
				} else if (!s1.equals(s2)) {
					no_good = true;
				}
				if (curr_coid == null) {
					throw new bad_netmix(2, "HAS_GLID_WITHOUT COID !!!!!");
				}
				File ff_1 = get_reported_addr_file(req_str);
				File ff_2 = get_reported_addr_file(curr_coid);
				if (no_good) {
					logger.info("BAD_REPORT cli_req=" + cli_req
							+ "\n\tf_coref=" + ff_1 + "\n\tf_coid=" + ff_2);
				}
			}
		}
		if (IN_DEBUG_2) {
			logger.info("OUT_REQ=\n" + cli_req.toString());
		}

		cli_req.msg_code = nx_location_request.ANSW_ALL_OK;
		return cli_req;
	}

	private nx_location_request get_service_oper_response(nx_peer cli_peer,
			nx_conn_id curr_coid, nx_location_request cli_req) {

		String cli_descr = cli_peer.get_description();
		cli_req.src_observed_nx_addr = cli_descr;

		if (curr_coid != null) {
			if (cli_req.src_reported_nx_addr != null) {
				set_reported_addr(curr_coid, cli_req.src_reported_nx_addr);
				set_observed_addr(curr_coid, cli_descr);
			}
		}

		String req_str = cli_req.req_coref_str;
		if (!is_local_coref(req_str)) {
			cli_req.msg_code = nx_location_request.ANSW_NOT_LOCAL_COREF;
			return cli_req;
		}

		cli_req.set_as_target();

		if (other_service_ports != null) {
			if(IN_DEBUG_18){
				logger.info("get_service. has_ports");
			}
			String req_svc = cli_req.req_service;
			if(req_svc != null){
				String svc_port = other_service_ports.get(req_svc);
				if(IN_DEBUG_18){
					logger.info("get_service. port=" + svc_port + " req_svc=" + req_svc);
				}
				cli_req.req_coref_str = svc_port;
			}
		}

		cli_req.msg_code = nx_location_request.ANSW_ALL_OK;
		return cli_req;
	}

	public byte[] get_response(nx_peer cli_peer, nx_conn_id curr_coid,
			byte[] msg) {

		try {
			dbg_slow sl1 = null;
			if (IN_DEBUG_11) {
				sl1 = new dbg_slow();
			}
			if (IN_DEBUG_2) {
				logger.info("Starting get_response");
			}

			byte[] enc_kk = null;
			if (curr_coid != null) {
				// File all_coids_dd = dir_base.get_all_coids_base_dir();
				File coid_ff = dir_base.get_coid_file(curr_coid);
				if (!coid_ff.exists()) {
					if (IN_DEBUG_5) {
						logger.info("get_response. UNEXISTANT_coid_file="
								+ coid_ff);
					}
					return null;
				}
				enc_kk = mem_file.concurrent_read_encrypted_bytes(coid_ff,
						owner);
				if (enc_kk == null) {
					if (IN_DEBUG_5) {
						logger.info("get_response. Cannot_read_coid_file="
								+ coid_ff);
					}
					return null;
				}
			}

			byte[] dat = msg;
			if (enc_kk != null) {
				dat = bitshaker.decrypt_bytes_with_sha(msg, enc_kk);
				if (dat == null) {
					if (IN_DEBUG_5) {
						logger.info("get_response. Cannot_decrypt_message.");
					}
					return null;
				}
			}

			nx_location_request cli_req = new nx_location_request();
			cli_req.set_ask_msg(dat);

			int rq_code = cli_req.msg_code;
			nx_location_request ans_req = null;
			switch (rq_code) {
			case nx_location_request.ASK_LOCATE_OPER:
				ans_req = get_locate_oper_response(cli_peer, curr_coid, cli_req);
				break;
			case nx_location_request.ASK_SERVICE_OPER:
				ans_req = get_service_oper_response(cli_peer, curr_coid,
						cli_req);
				break;
			case nx_location_request.ASK_COID_OPER:
				ans_req = get_coid_oper_response(cli_peer, curr_coid, cli_req);
				break;
			case nx_location_request.ASK_SET_COID_OPER:
				ans_req = get_set_coid_oper_response(cli_peer, curr_coid,
						cli_req);
				break;
			}

			byte[] resp_dat = ans_req.get_answ_msg();

			byte[] resp_msg = resp_dat;
			if (enc_kk != null) {
				resp_msg = bitshaker.encrypt_bytes_with_sha(resp_dat, enc_kk);
			}

			if (IN_DEBUG_11) {
				sl1.log_if_slow("SLOW_GET_RESPONSE cli_req=" + cli_req);
			}
			return resp_msg;

		} catch (bad_emetcode ex1) {
			if (IN_DEBUG_13) {
				logger.error(ex1, "Could not get_response in locator.");
			}
		}
		return null;
	}

	private nx_location_request get_coid_oper_response(nx_peer cli_peer,
			nx_conn_id curr_coid, nx_location_request cli_req) {

		int num_ths = num_active_thds.get();
		if (num_ths > MAX_ACTIVE_COID_THDS) {
			return null;
		}

		if (curr_coid == null) {
			if (IN_DEBUG_15) {
				logger.info("get_coid FAILED NULL coid");
			}
			cli_req.msg_code = nx_location_request.ANSW_UNTRUSTED_REQUEST;
			return cli_req;
		}
		nx_std_coref rem_gli = null;
		rem_gli = dir_base.get_remote_glid(curr_coid, owner);

		if (rem_gli == null) {
			if (IN_DEBUG_15) {
				logger.info("get_coid FAILED NULL rem_gli curr_coid="
						+ curr_coid + " dir_base=" + dir_base);
			}
			cli_req.msg_code = nx_location_request.ANSW_BAD_GLID;
			return cli_req;
		}
		if (!rem_gli.get_str().equals(cli_req.glid_check)) {
			if (IN_DEBUG_15) {
				logger.info("get_coid FAILED NULL bad check glid");
			}
			cli_req.msg_code = nx_location_request.ANSW_BAD_CHECK_GLID;
			return cli_req;
		}

		String rq_coref = cli_req.req_coref_str;
		nx_conn_id the_coid = null;
		if (is_local_coref(rq_coref)) {
			if (IN_DEBUG_15) {
				logger.info("get_coid FAILED asking for local glid");
			}
			cli_req.msg_code = nx_location_request.ANSW_BAD_REQ_GLID;
			return cli_req;
		}

		the_coid = dir_base.get_coid_by_ref(rq_coref, null);
		if (the_coid == null) {
			if (IN_DEBUG_15) {
				logger.info("get_coid FAILED NULL coid for rq_coref="
						+ rq_coref);
			}
			cli_req.msg_code = nx_location_request.ANSW_BAD_REQ_COID;
			return cli_req;
		}

		if (rem_gli.equals(local_boss)) {
			cli_req.coid_resp = the_coid.as_long();
			cli_req.msg_code = nx_location_request.ANSW_ALL_OK;
			if (IN_DEBUG_16) {
				logger.info("\n\n\n get_coid OK RETURNING COID=" + the_coid
						+ " FOR rq_coref=" + rq_coref);
			}
			return cli_req;
		}

		if (cli_req.forward_nx_addr == null) {
			if (IN_DEBUG_15) {
				logger.info("get_coid FAILED NULL forward_nx_addr rem_gli="
						+ rem_gli + " local_boss=" + local_boss);
			}
			cli_req.msg_code = nx_location_request.ANSW_BAD_FWD_ADDR;
			return cli_req;
		}

		File nx_dd = dir_base.get_remote_nx_dir(the_coid);
		nx_std_coref rem_boss = nx_std_coref.get_boss(nx_dd, owner);

		if (!rem_boss.equals(local_glid)) {
			if (IN_DEBUG_15) {
				logger.info("get_coid FAILED is not boss (" + rem_boss + " != "
						+ local_glid + ")");
			}
			cli_req.msg_code = nx_location_request.ANSW_BAD_REQ_GLID;
			return cli_req;
		}

		// start the thread

		String remote_dest_descr = cli_req.forward_nx_addr;
		String remote_dest_coref = rq_coref;
		String remote_src_descr = cli_peer.get_description();
		String remote_src_coref = rem_gli.get_str();

		Runnable rr = get_set_coid_runner(remote_dest_descr, remote_dest_coref,
				remote_src_descr, remote_src_coref, curr_coid);
		if (rr == null) {
			if (IN_DEBUG_15) {
				logger.info("get_coid FAILED NULL runner");
			}
			cli_req.msg_code = nx_location_request.ANSW_BUSY_SERVER;
			return cli_req;
		}

		String nm_thd = Thread.currentThread().getName() + "-srv_coid";
		thread_funcs.start_thread(nm_thd, rr, false);

		cli_req.msg_code = nx_location_request.ANSW_ALL_OK;
		return cli_req;
	}

	static nx_locator prepare_locator(nx_peer local_pp, nx_dir_base b_dir,
			key_owner o_owr, String remote_descr, nx_conn_id use_coid) {

		nx_locator ltor_cli = new nx_locator(b_dir, o_owr, local_pp);

		nx_context ctx = local_pp.get_context();
		nx_peer pp2 = ctx.make_peer();
		pp2.init_remote_peer(remote_descr, null);

		ltor_cli.set_locator(pp2, use_coid);
		if (!ltor_cli.has_locator()) {
			if (IN_DEBUG_1) {
				logger.debug("prepare_locator." + "COULD_NOT_SET_LOCATOR="
						+ remote_descr + " from=" + b_dir + " local_pp="
						+ local_pp.get_description());
			}
		}
		return ltor_cli;
	}

	private nx_locator prepare_child_locator(String remote_descr,
			nx_conn_id use_coid) {
		nx_locator chd = prepare_locator(local_peer, dir_base, owner,
				remote_descr, use_coid);
		if (!chd.has_locator()) {
			return null;
		}
		if (!chd.has_encryption_key()) {
			return null;
		}
		return chd;
	}

	private Runnable get_set_coid_runner(final String remote_dest_descr,
			final String remote_dest_coref, final String remote_src_descr,
			final String remote_src_coref, final nx_conn_id remote_src_coid) {

		Runnable rr1 = new Runnable() {
			public void run() {
				num_active_thds.incrementAndGet();
				try {
					get_set_coid_main(remote_dest_descr, remote_dest_coref,
							remote_src_descr, remote_src_coref, remote_src_coid);
				} catch (bad_emetcode ex1) {
					if (IN_DEBUG_14) {
						logger.error(ex1, "During req_coid_req_set_coid_main.");
					}
				}
				num_active_thds.decrementAndGet();
			}
		};
		return rr1;
	}

	private void get_set_coid_main(final String remote_dest_descr,
			final String remote_dest_coref, final String remote_src_descr,
			final String remote_src_coref, final nx_conn_id remote_src_coid) {

		nx_conn_id rem_dest_coid = dir_base.get_coid_by_ref(remote_dest_coref, null);
		if (rem_dest_coid == null) {
			if (IN_DEBUG_15) {
				logger.info("get_set_coid FAILED NULL coid for remote_dest_coref="
						+ remote_dest_coref);
			}
			return;
		}
		nx_locator ask_loc = prepare_child_locator(remote_dest_descr,
				rem_dest_coid);
		if (ask_loc == null) {
			if (IN_DEBUG_15) {
				logger.info("get_set_coid FAILED cannot prepare locator remote_dest_coref="
						+ remote_dest_coref);
			}
			return;
		}
		nx_location_request req_coid = ask_loc.ask_coid(null, remote_src_coref);
		nx_conn_id resp_coid = null;
		if (req_coid != null) {
			resp_coid = new nx_conn_id(req_coid.coid_resp);
			if (IN_DEBUG_16) {
				logger.info("\n\n\n get_coid OK GOT COID=" + resp_coid
						+ " FOR rq_coref=" + remote_src_coref);
			}
		}
		if (IN_DEBUG_15) {
			if (req_coid == null) {
				logger.info("get_set_coid FAILED NULL req_coid for remote_src_descr="
						+ remote_src_descr + " IN " + remote_dest_coref);
			}
		}

		String loc_src_descr = nx_locator.set_port(remote_src_descr);
		nx_locator answ_loc = prepare_child_locator(loc_src_descr,
				remote_src_coid);
		if (answ_loc == null) {
			if (IN_DEBUG_15) {
				logger.info("get_set_coid FAILED NULL answ_loc remote_src_descr="
						+ loc_src_descr);
			}
			return;
		}
		nx_location_request ok_req = answ_loc.ask_set_coid(remote_dest_coref,
				resp_coid);

		if (IN_DEBUG_15) {
			if (ok_req == null) {
				logger.info("get_set_coid FAILED NULL ok_req remote_dest_coref="
						+ remote_dest_coref);
			} else if (ok_req.msg_code != nx_location_request.ANSW_ALL_OK) {
				logger.info("get_set_coid FAILED NULL ok_req msg_code="
						+ ok_req.msg_code);
			}
		}
	}

	private boolean offer_coid_waiter(BlockingQueue<Long> the_queue,
			String rq_coref, nx_conn_id b_coid) {

		nx_locator_coid_entry ee = new nx_locator_coid_entry();
		ee.rq_boss_coid = b_coid.as_long();
		ee.all_waiting = new ArrayList<BlockingQueue<Long>>();
		ee.rq_subor_coref = rq_coref;
		ee.all_waiting.add(the_queue);

		boolean ok1 = set_waiting_for_coid.offer(ee);
		return ok1;
	}

	private long poll_coid_waiter(BlockingQueue<Long> the_queue) {
		Long got_coid = null;
		try {
			got_coid = the_queue.poll(POLL_COID_WAITER_TIMEOUT_SECS,
					TimeUnit.SECONDS);
		} catch (InterruptedException ex1) {
			throw new bad_netmix(2, ex1.toString());
		}
		if (got_coid == null) {
			return 0;
		}
		return got_coid;
	}

	private void update_waiting_map() {
		while (true) {
			nx_locator_coid_entry ee = set_waiting_for_coid.poll();
			if (ee == null) {
				break;
			}
			if (IN_DEBUG_17) {
				logger.info("adding coid_entry for=" + ee.rq_subor_coref);
			}
			nx_locator_coid_entry oo = all_waiting_for_coid
					.get(ee.rq_subor_coref);
			if (oo != null) {
				if (oo.rq_boss_coid != ee.rq_boss_coid) {
					continue;
				}
				oo.all_waiting.addAll(ee.all_waiting);
				continue;
			}
			all_waiting_for_coid.put(ee.rq_subor_coref, ee);
		}
	}

	private nx_location_request get_set_coid_oper_response(nx_peer cli_peer,
			nx_conn_id curr_coid, nx_location_request cli_req) {
		if (curr_coid == null) {
			if (IN_DEBUG_15) {
				logger.info("set_coid FAILED NULL coid.");
			}
			cli_req.msg_code = nx_location_request.ANSW_UNTRUSTED_REQUEST;
			return cli_req;
		}

		update_waiting_map();

		nx_locator_coid_entry ee = all_waiting_for_coid
				.get(cli_req.req_coref_str);
		if (ee == null) {
			if (IN_DEBUG_15) {
				logger.info("set_coid FAILED. Never asked for it. resp_coid="
						+ cli_req.coid_resp);
			}
			cli_req.msg_code = nx_location_request.ANSW_UNTRUSTED_REQUEST;
			return cli_req;
		}

		if (ee.rq_boss_coid != curr_coid.as_long()) {
			if (IN_DEBUG_15) {
				logger.info("set_coid FAILED bad boss coid. resp_coid="
						+ curr_coid);
			}
			cli_req.msg_code = nx_location_request.ANSW_UNTRUSTED_REQUEST;
			return cli_req;
		}

		long b_coid = curr_coid.as_long();
		if (ee.rq_boss_coid != b_coid) {
			if (IN_DEBUG_15) {
				logger.info("set_coid FAILED is not boss coid. "
						+ ee.rq_boss_coid + " != " + b_coid);
			}
			cli_req.msg_code = nx_location_request.ANSW_UNTRUSTED_REQUEST;
			return cli_req;
		}

		// / set the coid

		Long resp_coid = cli_req.coid_resp;

		List<BlockingQueue<Long>> all_ww = ee.all_waiting;
		for (BlockingQueue<Long> qq : all_ww) {
			qq.offer(resp_coid);
		}

		cli_req.msg_code = nx_location_request.ANSW_ALL_OK;
		return cli_req;
	}

	public boolean verif_subordinate_coid(String boss_coref,
			String subor_coref, boolean rq_and_repo) {

		nx_dir_base b_dir = dir_base;
		nx_peer local_pp = local_peer;

		if (IN_DEBUG_16) {
			logger.info("Starting verif_subordinate_coid local_pp="
					+ local_pp.get_description() + " rq_coref=" + subor_coref);
		}

		nx_conn_id boss_coid = b_dir.get_coid_by_ref(boss_coref, null);
		if (boss_coid == null) {
			if (IN_DEBUG_15) {
				logger.info("verif_subor NULL boss coid boss_coref="
						+ boss_coref + " b_dir=" + b_dir);
			}
			return false;
		}
		if (IN_DEBUG_16) {
			logger.info("verif_subor boss_coid=" + boss_coid + " boss_coref="
					+ boss_coref + " b_dir=" + b_dir);
		}

		nx_conn_id rq_coid = b_dir.get_coid_by_ref(subor_coref, null);
		if (rq_coid == null) {
			if (IN_DEBUG_15) {
				logger.info("verif_subor NULL subor coid rq_coid=" + rq_coid
						+ " b_dir=" + b_dir);
			}
			return false;
		}

		key_owner owr = local_pp.get_owner();

		String trus_addr = nx_connector.find_coref_location(b_dir, local_pp,
				boss_coref, null, rq_and_repo);
		if (trus_addr == null) {
			if (IN_DEBUG_15) {
				logger.info("verif_subor NULL boss addr boss_coref="
						+ boss_coref + " b_dir=" + b_dir);
			}
			return false;
		}

		String rq_addr = nx_connector.find_coref_location(b_dir, local_pp,
				subor_coref, null, rq_and_repo);
		if (rq_addr == null) {
			if (IN_DEBUG_15) {
				logger.info("verif_subor NULL subor addr subor_coref="
						+ subor_coref + " b_dir=" + b_dir);
			}
			return false;
		}

		String trus_req_addr = nx_locator.set_port(trus_addr);

		nx_locator ltor_1 = nx_locator.prepare_locator(local_pp, b_dir, owr,
				trus_req_addr, boss_coid);

		if (!ltor_1.has_locator()) {
			if (IN_DEBUG_15) {
				logger.info("verif_subor NO locator for trus_req_addr="
						+ trus_req_addr + " b_dir=" + b_dir);
			}
			return false;
		}
		if (!ltor_1.has_encryption_key()) {
			if (IN_DEBUG_15) {
				logger.info("verif_subor NO enc_key for locator for trus_req_addr="
						+ trus_req_addr + " b_dir=" + b_dir);
			}
			return false;
		}

		BlockingQueue<Long> waiter_queue = new LinkedBlockingQueue<Long>(2);
		boolean ok1 = offer_coid_waiter(waiter_queue, subor_coref, boss_coid);
		if (!ok1) {
			if (IN_DEBUG_15) {
				logger.info("offer_coid_waiter FAILED subor_coref="
						+ subor_coref + " b_dir=" + b_dir);
			}
			return false;
		}

		nx_location_request loc = ltor_1.ask_coid(rq_addr, subor_coref);
		if (loc == null) {
			if (IN_DEBUG_15) {
				logger.info("ask_coid FAILED rq_addr=" + rq_addr
						+ " subor_coref=" + subor_coref + " b_dir=" + b_dir);
			}
			return false;
		}
		if (loc.msg_code != nx_location_request.ANSW_ALL_OK) {
			if (IN_DEBUG_15) {
				logger.info("ask_coid FAILED msg_code=" + loc.msg_code
						+ " rq_addr=" + rq_addr + " subor_coref=" + subor_coref
						+ " b_dir=" + b_dir);
			}
			return false;
		}

		long ret_coid_val = poll_coid_waiter(waiter_queue);
		if (ret_coid_val == 0) {
			if (IN_DEBUG_15) {
				logger.info("poll_coid_waiter FAILED b_dir=" + b_dir);
			}
			return false;
		}
		nx_conn_id ret_coid = new nx_conn_id(ret_coid_val);
		if (!ret_coid.equals(rq_coid)) {
			if (IN_DEBUG_15) {
				logger.info("verif_subor FAILED ret_coid" + ret_coid + " != "
						+ ret_coid_val + " b_dir=" + b_dir);
			}
			return false;
		}

		return true;
	}

}
