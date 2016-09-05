package emetcode.economics.netpasser;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import emetcode.crypto.bitshake.utils.file_funcs;
import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.economics.netpasser.locale.L;
import emetcode.economics.passet.bad_passet;
import emetcode.economics.passet.tag_accoglid;
import emetcode.economics.passet.trackers;
import emetcode.economics.passet.transfers_map;
import emetcode.economics.passet.config;
import emetcode.economics.passet.iso;
import emetcode.economics.passet.parse;
import emetcode.economics.passet.paccount;
import emetcode.economics.passet.tag_transfer;
import emetcode.net.netmix.bad_netmix;
import emetcode.net.netmix.nx_conn_id;
import emetcode.net.netmix.nx_connection;
import emetcode.net.netmix.nx_context;
import emetcode.net.netmix.nx_dir_base;
import emetcode.net.netmix.nx_messenger;
import emetcode.net.netmix.nx_peer;
import emetcode.net.netmix.nx_protector;
import emetcode.net.netmix.nx_std_coref;
import emetcode.net.netmix.locator_sys.nx_connector;
import emetcode.util.devel.bad_emetcode;
import emetcode.util.devel.logger;
import emetcode.util.devel.thread_funcs;

public class net_operator implements trans_operator {
	static final boolean IN_DEBUG_01 = true;
	static final boolean IN_DEBUG_2 = true; // add_verif
	static final boolean IN_DEBUG_3 = true; // set port
	static final boolean IN_DEBUG_4 = true; // updating trackers for
	static final boolean IN_DEBUG_5 = true; // create_channels
	static final boolean IN_DEBUG_6 = true; // new tracked
	static final boolean IN_DEBUG_7 = true; // recv unsigned
	static final boolean IN_DEBUG_8 = true; // recv new tracked
	static final boolean IN_DEBUG_9 = true; // prev_ids
	static final boolean IN_DEBUG_10 = true; // set_verif

	public static int MAX_SECS_TO_WAIT_FOR_THREAD = 100;

	private static final String UNKNOWN_STR = config.UNKNOWN_STR;

	public static final String NETPASSER_SERVICE_NAME = "netpasser";
	public static final long NETPASSER_PORT = 7000;
	public static final String NETPASSER_PORT_STR = "" + NETPASSER_PORT;
	public static final String NETPASSER_PORT_DESCR = nx_peer.PORT_SEP_STR
			+ NETPASSER_PORT_STR;

	private static final Pattern FLD_SEP_PATT = Pattern.compile("\\.");

	private static final String MSG_LAST_MSG = "last_message";

	private static final String MSG_PASSET_OPER = "passet_oper";
	private static final String MSG_DATA_FILES = "data_files";
	private static final String MSG_CURRENCY = "currency";
	private static final String MSG_KEEP_ALIVE = "keep_alive";
	private static final String MSG_UNSIGNED_PASSETS = "unsigned_passets";
	private static final String MSG_RECEPTACLES = "receptacles";
	private static final String MSG_SIGNED_PASSETS = "signed_passets";
	private static final String MSG_NEW_TRACKED = "new_tracked";

	private static final String MSG_GET_TRANSFERS = "get_transfers";
	private static final String MSG_START_ADD_VERIF = "start_add_verif";
	private static final String MSG_END_ADD_VERIF = "end_add_verif";

	private static final String MSG_CANCEL = "cancel";

	private static int RESUME_QUEUE_SIZE = 1;
	private static int INPUT_QUEUE_SIZE = 10;

	private boolean is_senderissuer_case;
	private AtomicBoolean is_senderissuer_connected;
	private Object senderissuer_lock;

	private AtomicBoolean running_srv;
	private boolean running_q;

	// Auxiliar queue to resume operation on the current 'my_trans'.
	// See 'queue_transaction', 'transaction.ask_oper' and 'wait_for_trans'.
	private BlockingQueue<transaction> to_resume_operation;

	private BlockingQueue<transaction> to_net_operate;

	private nx_peer l_peer;

	private nx_connection conn;
	private nx_messenger msgr;

	private transaction working_trans;
	private AtomicReference<transaction> my_trans;

	public net_operator(nx_peer local_peer) {
		init_net_operator();

		if (local_peer == null) {
			throw new bad_netpasser(2);
		}
		l_peer = local_peer;
	}

	private static net_operator get_net_operator_copy(net_operator orig) {
		net_operator nt_op_cp = new net_operator(orig.l_peer);
		nt_op_cp.init_net_operator_with(orig);
		return nt_op_cp;
	}

	private void init_net_operator_with(net_operator orig) {
		to_net_operate = orig.to_net_operate;

		if (orig.msgr != null) {
			throw new bad_netpasser(2);
		}

		if (orig.is_working()) {
			transaction trans2 = transaction
					.get_transaction_copy(orig.working_trans);
			set_first_trans(trans2);
		}
		conn = orig.conn;
	}

	void init_net_operator() {
		is_senderissuer_case = false;
		senderissuer_lock = new Object();
		is_senderissuer_connected = new AtomicBoolean(false);
		running_srv = new AtomicBoolean(true);
		running_q = true;

		my_trans = new AtomicReference<transaction>(null);

		to_resume_operation = new LinkedBlockingQueue<transaction>(
				RESUME_QUEUE_SIZE);
		to_net_operate = new LinkedBlockingQueue<transaction>(INPUT_QUEUE_SIZE);

		l_peer = null;

		init_net_state();
	}

	void notify_senderissuer_lock() {
		synchronized (senderissuer_lock) {
			is_senderissuer_connected.set(true);
			senderissuer_lock.notify();
		}
	}

	void wait_for_senderissuer_lock() {
		synchronized (senderissuer_lock) {
			while (!is_senderissuer_connected.get()) {
				try {
					senderissuer_lock.wait();
				} catch (InterruptedException ee) {
					logger.info("INTERRUPTED DURING wait_for_senderissuer_lock !!");
				}
			}
		}
	}

	private boolean has_work() {
		return (my_trans.get() != null);
	}

	public void set_first_trans(transaction w_trans) {
		if (has_work()) {
			throw new bad_netpasser(2);
		}
		working_trans = w_trans;
		my_trans.set(w_trans);
	}

	private void set_work_trans(transaction w_trans) {
		if ((w_trans != null) && (my_trans.get() != w_trans)) {
			throw new bad_netpasser(2, String.format(L.invalid_transaction,
					my_trans.get(), w_trans));
		}
		working_trans = w_trans;
	}

	private void clear_work_trans() {
		working_trans = null;
	}

	private void init_net_state() {
		clear_work_trans();
		conn = null;
		msgr = null;
		my_trans.set(null);
	}

	void restart_net_operator() {
		if (conn != null) {
			logger.debug("CLOSING CONNECTION" + conn);
			conn.close_net_connection();
		}
		init_net_state();
	}

	int get_max_msg_conn_num_by() {
		if (conn == null) {
			throw new bad_netpasser(2);
		}
		return conn.get_msg_max_num_bytes();
	}

	boolean ready_for_work() {
		boolean c1 = (l_peer != null);
		return c1;
	}

	boolean is_working() {
		boolean c1 = (working_trans != null);
		if (c1 && !ready_for_work()) {
			throw new bad_netpasser(2);
		}
		return c1;
	}

	paccount get_local_paccount() {
		if (!is_working()) {
			throw new bad_netpasser(2);
		}
		if (working_trans.local_pcc == null) {
			throw new bad_netpasser(2);
		}
		return working_trans.local_pcc;
	}

	paccount get_remote_paccount() {
		if (!is_working()) {
			throw new bad_netpasser(2);
		}
		if (working_trans.remote_pcc == null) {
			throw new bad_netpasser(2);
		}
		return working_trans.remote_pcc;
	}

	key_owner get_owner() {
		if (l_peer == null) {
			throw new bad_netpasser(2);
		}
		return l_peer.get_owner();
	}

	nx_context get_context() {
		if (l_peer == null) {
			throw new bad_netpasser(2);
		}
		return l_peer.get_context();
	}

	public void tell_finish_cli() {
		queue_transaction(new transaction());
	}

	public transaction wait_for_transaction(transaction w_trans) {
		transaction trans = wait_for_trans(w_trans);
		// verif is net oper
		if (running_q) {
			trans.state_oper = transaction.get_net_oper(trans.state_oper);
		}
		return trans;
	}

	public void queue_transaction(transaction n_trans) {
		try {
			transaction w_trans = my_trans.get();
			if ((w_trans != null) && (w_trans == n_trans)) {
				to_resume_operation.put(n_trans);
			} else {
				to_net_operate.put(n_trans);
				logger.debug("putting trans=" + n_trans + " into queue="
						+ to_net_operate);
			}
		} catch (InterruptedException ex) {
			throw new bad_netpasser(2, ex.toString());
		}
	}

	private void ck_finish() {
		if (is_working()
				&& (working_trans.state_oper == transaction.FINISH_OPER)) {
			running_q = false;
			clear_work_trans();
		}
	}

	private transaction wait_for_trans(transaction w_trans) {
		if (!ready_for_work()) {
			throw new bad_netpasser(2);
		}
		if (is_working()) {
			throw new bad_netpasser(2);
		}

		if (has_work()) {
			while (!is_working()) {
				try {
					transaction tmp_trans = to_resume_operation.poll(1,
							TimeUnit.SECONDS);
					set_work_trans(tmp_trans);
				} catch (InterruptedException e) {
					throw new bad_netpasser(2, e.toString());
				}
				if (msgr != null) {
					msgr.send_string(MSG_KEEP_ALIVE);
				}
				ck_finish();
			}
			if ((w_trans != null) && (working_trans != w_trans)) {
				throw new bad_netpasser(2);
			}
			if (working_trans.state_oper == transaction.INVALID_OPER) {
				throw new bad_netpasser(2);
			}
			return working_trans;
		}

		if (has_work()) {
			throw new bad_netpasser(2);
		}
		if (w_trans != null) {
			throw new bad_netpasser(2);
		}

		try {
			transaction tmp_trans = to_net_operate.take();
			set_first_trans(tmp_trans);
			if (!is_working()) {
				throw new bad_netpasser(2);
			}
			if (working_trans.state_oper == transaction.INVALID_OPER) {
				throw new bad_netpasser(2);
			}
			if (working_trans.state_oper == transaction.FINISH_OPER) {
				running_q = false;
			}
		} catch (InterruptedException e) {
			throw new bad_netpasser(2);
		}
		return working_trans;
	}

	void start_client_messenger() {
		transaction trans = working_trans;

		String r_dsc = trans.r_peer_descr;
		nx_dir_base b_dir = get_local_paccount().get_dir_base();

		if (r_dsc == null) {
			throw new bad_netpasser(2, L.cannot_connect_to_unkown_remote_peer);
		}

		if (!trans.r_is_addr) {
			boolean repo = trans.repo_locat;
			String r_addr = nx_connector.find_coref_location(b_dir, l_peer,
					r_dsc, NETPASSER_SERVICE_NAME, repo);
			if (r_addr == null) {
				throw new bad_netpasser(2, String.format(
						L.cannot_find_remote_host, r_dsc));
			}

			r_dsc = r_addr;
		}

		trans.init_coid(r_dsc);

		nx_peer r_peer = get_context().make_peer();
		r_peer.init_remote_peer(r_dsc, null);

		logger.debug("Connecting to remote..." + r_dsc);

		if (conn != null) {
			throw new bad_netpasser(2);
		}
		String l_dsc = l_peer.get_description();
		logger.debug("\n\t l_descr=" + l_dsc + "\n\t r_descr=" + r_dsc);
		if (l_dsc.equals(r_dsc)) {
			throw new bad_netpasser(2, String.format(
					L.cannot_connect_to_itself, l_dsc));
		}

		conn = l_peer.connect_to(r_peer);
		if (conn == null) {
			throw new bad_netpasser(2, String.format(L.could_not_connect_to,
					r_dsc));
		}

		logger.debug("Client got connection." + conn);

		msgr = new nx_messenger(null, conn, nx_protector.NULL_VERIFIER);
		if (msgr.is_to_renew_connection_key()) {
			throw new bad_netpasser(2);
		}
		msgr.set_agreed_key(trans.start_key);

		msgr.set_net_base_dir(b_dir);
		msgr.send_set_secure(null, trans.coid, true);

		logger.debug("CONNECTION SECURED." + conn);

		set_conn_info();
	}

	void start_server_messenger(nx_connection conn) {

		msgr = new nx_messenger(null, conn, nx_protector.NULL_VERIFIER);
		if (msgr.is_to_renew_connection_key()) {
			throw new bad_netpasser(2);
		}
		msgr.set_agreed_key(working_trans.start_key);
		msgr.set_net_base_dir(get_local_paccount().get_dir_base());
		msgr.recv_set_secure(true);

		if (msgr.has_secure_conn()) {
			logger.debug("CONNECTION SECURED." + conn);
		}

		nx_conn_id coid1 = working_trans.coid;
		nx_conn_id coid2 = msgr.get_coid();
		if ((coid1 != null) && coid1.equals(coid2)) {
			throw new bad_netpasser(2, String.format(L.expecting_other_coid,
					coid1, coid2));
		}

		set_conn_info();
	}

	private Scanner recv_net_message(String name) {
		String msg1 = null;
		while (true) {
			msg1 = msgr.recv_string();
			if ((msg1 != null) && !msg1.equals(MSG_KEEP_ALIVE)) {
				break;
			}
		}
		Scanner s1 = new Scanner(msg1);
		s1.useDelimiter(FLD_SEP_PATT);

		String p1 = s1.next();
		if (p1.equals(name)) {
			return s1;
		}
		throw new bad_netpasser(2, String.format(L.expecting_other_msg, p1,
				name));
	}

	public Runnable get_run_once_accept_server() {
		logger.debug("get_run_once_accept_server");
		Runnable rr1 = new Runnable() {
			public void run() {
				run_once_as_accept_server();
			}
		};
		return rr1;
	}

	public Runnable get_run_once_server() {
		logger.debug("get_run_once_server");
		Runnable rr1 = new Runnable() {
			public void run() {
				run_once_as_server();
			}
		};
		return rr1;
	}

	public Runnable get_run_once_client() {
		logger.debug("get_run_once_client");
		Runnable rr1 = new Runnable() {
			public void run() {
				run_once_as_client();
			}
		};
		return rr1;
	}

	public Runnable get_run_loop_server() {
		logger.debug("get_run_loop_server");
		Runnable rr1 = new Runnable() {
			public void run() {
				run_loop_as_server();
			}
		};
		return rr1;
	}

	public Runnable get_run_loop_client() {
		logger.debug("get_run_loop_client");
		Runnable rr1 = new Runnable() {
			public void run() {
				run_loop_as_client();
			}
		};
		return rr1;
	}

	public Runnable get_run_pending_client() {
		logger.debug("get_run_pending_client");
		Runnable rr1 = new Runnable() {
			public void run() {
				run_pending_as_client();
			}
		};
		return rr1;
	}

	private void run_loop_as_client() {
		if (!is_working()) {
			wait_for_transaction(null);
		}
		if (working_trans.is_server_transac()) {
			throw new bad_netpasser(2);
		}

		running_q = true;
		while (running_q) {
			run_once_as_client();
			clear_work_trans();
			wait_for_transaction(null);
			if (working_trans.is_server_transac()) {
				throw new bad_netpasser(2);
			}
		}

		thread_funcs.prt_active_group_thds("BEFORE_FINISH");
		log_finished();
	}

	private void run_pending_as_client() {
		if (!is_working()) {
			throw new bad_netpasser(2);
		}
		if (working_trans.is_server_transac()) {
			throw new bad_netpasser(2);
		}

		running_q = true;
		while (running_q) {
			run_once_as_client();
			clear_work_trans();
			running_q = get_pending_transaction();
		}

		thread_funcs.prt_active_group_thds("BEFORE_FINISH");
		log_finished();
	}

	private void run_loop_as_server() {
		if (!is_working()) {
			throw new bad_netpasser(2);
		}
		if (!working_trans.is_server_transac()) {
			throw new bad_netpasser(2);
		}

		running_srv.set(true);
		logger.info("Starting server=" + l_peer.get_description());

		try {
			while (running_srv.get()) {
				run_once_as_accept_server();
			}
		} catch (Exception ee) {
			logger.error(ee, "EXCEPTION COUGHT in server !!!");
		}

		logger.info("Finishing server=" + l_peer.get_description());
		thread_funcs.prt_active_group_thds("BEFORE_FINISH");
		log_finished();
	}

	private void log_finished() {
		logger.info("************** FINISHED *************");
	}

	public void kill_server() {
		running_srv.set(false);
		l_peer.kill_accept();
	}

	private void run_once_as_accept_server() {
		if (!is_working()) {
			throw new bad_netpasser(2);
		}
		if (conn != null) {
			throw new bad_netpasser(2);
		}
		conn = l_peer.accept();
		if (conn == null) {
			logger.debug("null connection...");
			return;
		}
		net_operator n_op = get_net_operator_copy(this);
		conn = null;
		String thd_nm = Thread.currentThread().getName() + "-single";
		nx_context.start_thread(thd_nm, n_op.get_run_once_server(), false);
	}

	private void run_once_as_server() {
		if (!is_working()) {
			throw new bad_netpasser(2);
		}
		set_finished_all(false);
		if (conn == null) {
			throw new bad_netpasser(2, L.no_connection_found);
		}

		logger.debug("Server got connection from "
				+ conn.get_remote_peer().get_description());

		try {
			start_server_messenger(conn);
			logger.debug("Server got messenger.");

			recv_passet_oper();
			logger.debug("Server got passet_oper.");
			set_finished_all(true);
		} catch (bad_emetcode err1) {
			send_cancel();
			logger.error(err1, "SERVER TRANSACTION ERROR. conn=" + conn);
		}

		cleanup();
		restart_net_operator();
		log_finished();
	}

	void ck_client_oper() {
		if (working_trans.state_oper == transaction.NET_CONTINUE_OPER) {
			throw new bad_netpasser(2);
		}
		if (working_trans.state_oper == transaction.NET_CANCEL_OPER) {
			throw new bad_netpasser(2);
		}
	}

	private void run_once_as_client() {
		if (!is_working()) {
			throw new bad_netpasser(2);
		}
		set_finished_all(false);
		ck_client_oper();

		logger.debug("Starting client connection to="
				+ working_trans.r_peer_descr);
		if (is_senderissuer_case) {
			logger.debug("is_senderissuer_case");
		}

		try {
			start_client_messenger();
			logger.debug("Client got messenger.");

			if (is_senderissuer_case) {
				notify_senderissuer_lock();
			}

			send_passet_oper();
			logger.debug("Client got passet_oper.");
			set_finished_all(true);
		} catch (bad_emetcode err1) {
			send_cancel();
			logger.error(err1, "CLIENT TRANSACTION ERROR. conn=" + conn);
		}

		cleanup();
		tell_user_finished();
		restart_net_operator();
		log_finished();
	}

	private void set_conn_info() {
		if (msgr == null) {
			throw new bad_netmix(2);
		}
		if (!is_working()) {
			throw new bad_netmix(2);
		}

		working_trans.set_connection_info(msgr);
	}

	public int get_net_type() {
		return get_context().get_net_type();
	}

	public String get_peer_descr() {
		return l_peer.get_description();
	}

	void set_finished_all(boolean fnsh) {
		if (working_trans != null) {
			working_trans.finished_all = fnsh;
		}
	}

	void send_cancel() {
		if (msgr != null) {
			try {
				msgr.send_string(MSG_CANCEL);
			} catch (bad_emetcode ee) {
				logger.info("COULD NOT SEND CANCEL_MSG");
			}
		}
	}

	void init_local_trissuers() {
		key_owner owr = get_owner();
		paccount loc_pcc = get_local_paccount();
		paccount rem_pcc = get_remote_paccount();

		loc_pcc.init_the_trissuers(owr);
		nx_std_coref rem_iss = msgr.get_remote_glid();
		if ((rem_iss != null) && loc_pcc.can_receive_from(rem_iss.get_str())) {
			trackers rem_trks = new trackers();
			rem_trks.init_trackers(rem_pcc, owr);
			loc_pcc.save_trackers_list(rem_iss, rem_trks.all_trackers, owr);
		}
	}

	void send_passet_oper() {
		String net_oper = transaction.get_net_oper(working_trans.state_oper);
		String peer_oper = transaction.get_net_inv_oper(net_oper);

		msgr.send_string(MSG_PASSET_OPER);
		msgr.send_string(peer_oper);

		recv_all_data_files();
		logger.debug("Received all data files.");

		init_local_trissuers();

		send_currency();
		do_net_oper(net_oper);
	}

	void recv_passet_oper() {
		if (!working_trans.is_server_transac()) {
			throw new bad_netpasser(2);
		}

		recv_net_message(MSG_PASSET_OPER);
		String n_op = msgr.recv_string();
		String net_oper = transaction.get_net_oper(n_op);

		logger.debug("Service start for net_oper=" + net_oper);

		send_all_data_files();
		logger.debug("Sent all data files.");

		init_local_trissuers();

		recv_currency();
		logger.debug("STARTING net_oper=" + net_oper);
		do_net_oper(net_oper);
	}

	void send_currency() {
		if (!is_working()) {
			throw new bad_netpasser(2);
		}
		int currcy_idx = working_trans.get_working_currency();
		msgr.send_string(MSG_CURRENCY);
		String curr_idx = "" + currcy_idx;
		msgr.send_string(curr_idx);
		logger.debug("Sent working currency="
				+ iso.get_currency_code(currcy_idx));
	}

	void recv_currency() {
		if (!is_working()) {
			throw new bad_netpasser(2);
		}
		recv_net_message(MSG_CURRENCY);
		String curr_idx = msgr.recv_string();
		int currcy_idx = Integer.parseInt(curr_idx);
		working_trans.set_working_currency(currcy_idx);
		logger.debug("Received working currency="
				+ iso.get_currency_code(currcy_idx));
	}

	void send_mutual_file_if_diff(File loc_ff, File rem_ff, key_owner owr) {
		msgr.send_file_if_diff(loc_ff, owr);
		msgr.recv_file_if_diff(rem_ff, owr, null);
	}

	void recv_mutual_file_if_diff(File loc_ff, File rem_ff, key_owner owr) {
		msgr.recv_file_if_diff(rem_ff, owr, null);
		msgr.send_file_if_diff(loc_ff, owr);
	}

	private void tell_user_finished() {
		transaction trans = working_trans;
		clear_work_trans();
		if (trans.has_user_caller()) {
			trans.queue_in_user(transaction.USER_TELL_FINISHED_OPER);
		}
	}

	// TODO: after each 'ask_file_oper'
	// do a 'send_string' in the caller func (send_xxx / recv_xxx)
	// and a 'recv_net_message' in the opposite func (send_xxx / recv_xxx)
	// to be safe on MSG_KEEP_ALIVE consistency

	private void ask_file_oper(String oper) {
		transaction trans = working_trans;
		clear_work_trans();
		trans.ask_file_oper(oper, this);
		set_work_trans(trans);
	}

	private void do_net_oper(String net_oper) {
		if (net_oper == transaction.NET_SEND_CREATE_CHANN_OPER) {
			recv_last_msg();
			send_last_msg();
			if (IN_DEBUG_5) {
				String rem_peer = msgr.get_remote_descr();
				nx_conn_id w_coid = msgr.get_coid();
				// msgr.write_coref(rem_peer, w_coid);
				String r_gli = msgr.get_remote_glid().get_str();
				logger.debug("send_create_chann_ok." + "\n r_peer=\n"
						+ rem_peer + "\n coid=\n" + w_coid
						+ "\n max_msg_num_by=\n" + get_max_msg_conn_num_by());
				if (!coid_has_channel(w_coid)) {
					logger.error(null, "NO_COID_FILE_FOR=" + w_coid);
				}
				if (!name_has_ref(r_gli)) {
					logger.error(null, "NO_REF_FOR=" + r_gli);
				}
			}
		} else if (net_oper == transaction.NET_RECV_CREATE_CHANN_OPER) {
			send_last_msg();
			recv_last_msg();
			if (IN_DEBUG_5) {
				String rem_peer = msgr.get_remote_descr();
				nx_conn_id w_coid = msgr.get_coid();
				String r_gli = msgr.get_remote_glid().get_str();
				logger.debug("recv_create_chann_ok." + "\n r_peer=\n"
						+ rem_peer + "\n coid=\n" + w_coid
						+ "\n max_msg_num_by=\n" + get_max_msg_conn_num_by());

				if (!coid_has_channel(w_coid)) {
					logger.error(null, "NO_COID_FILE_FOR=" + w_coid);
				}
				if (!name_has_ref(r_gli)) {
					logger.error(null, "NO_REF_FOR=" + r_gli);
				}
			}
		} else if (net_oper == transaction.NET_SEND_PASSETS_OPER) {
			send_transfer();
		} else if (net_oper == transaction.NET_RECV_PASSETS_OPER) {
			recv_transfer();
		} else if (net_oper == transaction.NET_SEND_GET_TRANSFER_OPER) {
			send_get_transfers();
		} else if (net_oper == transaction.NET_RECV_GET_TRANSFER_OPER) {
			recv_get_transfers();
		} else if (net_oper == transaction.NET_SEND_ADD_VERIF_OPER) {
			send_add_verif();
			recv_last_msg();
		} else if (net_oper == transaction.NET_RECV_ADD_VERIF_OPER) {
			recv_add_verif();
			send_last_msg();
		} else if (net_oper == transaction.NET_SEND_CHANGE_SPLIT_OPER) {
			send_transfer();
			recv_last_msg();
			working_trans.reset_files();
			working_trans.reset_out_files();
			recv_transfer();
		} else if (net_oper == transaction.NET_RECV_CHANGE_SPLIT_OPER) {
			recv_transfer();
			calc_split_change();
			send_last_msg();
			report_change();
			working_trans.reset_files();
			working_trans.reset_out_files();
			send_transfer();
		} else if (net_oper == transaction.NET_SEND_CHANGE_JOIN_OPER) {
			send_transfer();
			recv_last_msg();
			working_trans.reset_files();
			working_trans.reset_out_files();
			recv_transfer();
		} else if (net_oper == transaction.NET_RECV_CHANGE_JOIN_OPER) {
			recv_transfer();
			calc_join_change();
			send_last_msg();
			report_change();
			working_trans.reset_files();
			working_trans.reset_out_files();
			send_transfer();
		} else if (net_oper == transaction.NET_SEND_NEW_TRACKED_OPER) {
			send_new_tracked();
		} else if (net_oper == transaction.NET_RECV_NEW_TRACKED_OPER) {
			recv_new_tracked();
		}

	}

	String dbg_files_to_str(List<File> all_ff) {
		return Arrays.toString(file_funcs.files_to_path_list(all_ff).toArray(
				new String[0]));
	}

	String dbg_files_to_str(File[] all_ff) {
		return Arrays.toString(file_funcs.files_to_paths(all_ff).toArray(
				new String[0]));
	}

	private File[] send_unsigned(String tra_nm) {
		msgr.send_string(MSG_UNSIGNED_PASSETS);

		logger.debug("Starting send_unsigned");

		List<File> all_sel = working_trans.get_chosen(get_owner(), tra_nm);
		if (all_sel == null) {
			throw new bad_passet(2);
		}
		File[] all_pss_ff = all_sel.toArray(new File[0]);

		logger.debug("all_unsigned=\n" + dbg_files_to_str(all_sel));

		working_trans.set_files(all_pss_ff);
		msgr.send_mem_files(all_pss_ff);

		logger.debug("send_unsigned (" + all_pss_ff.length + ") ok. conn="
				+ conn);

		return all_pss_ff;
	}

	File[] get_files() {
		if (!is_working()) {
			throw new bad_netpasser(2);
		}
		return working_trans.get_files();
	}

	private List<File> get_out_files() {
		if (!is_working()) {
			throw new bad_netpasser(2);
		}
		return working_trans.get_out_files();
	}

	public File[] recv_unsigned() {
		recv_net_message(MSG_UNSIGNED_PASSETS);
		File dst_dir = get_remote_paccount().get_passet_dir();

		File[] all_usgned_ff = msgr.recv_encrypted_mem_files(dst_dir, null,
				true);

		working_trans.set_files(all_usgned_ff);
		return all_usgned_ff;
	}

	public void send_last_msg() {
		msgr.send_string(MSG_LAST_MSG);
	}

	public void recv_last_msg() {
		recv_net_message(MSG_LAST_MSG);
	}

	public void send_receptacles() {
		msgr.send_string(MSG_RECEPTACLES);
		File[] all_out = get_out_files().toArray(new File[0]);
		msgr.send_mem_files(all_out);
		logger.debug("send_receptacles (" + all_out.length + ") ok. conn="
				+ conn);
	}

	public void recv_receptacles() {
		recv_net_message(MSG_RECEPTACLES);
		File dst_dir = get_remote_paccount().get_recep_dir();
		File[] all_recep = msgr.recv_mem_files(dst_dir);
		logger.debug("recv_receptacles (" + all_recep.length + ") ok. conn="
				+ conn);
		logger.debug("all_receptacles=\n" + dbg_files_to_str(all_recep));
	}

	public void sign_receptacles() {
		ask_file_oper(transaction.FILE_SING_REPECTACLES_OPER);
		logger.debug("sign_receptacles ok. conn=" + conn);
	}

	public void make_receptacles() {
		ask_file_oper(transaction.FILE_MAKE_REPECTACLES_OPER);
		logger.debug("make_receptacles ok. conn=" + conn);
	}

	private File[] send_signed() {
		msgr.send_string(MSG_SIGNED_PASSETS);
		File[] all_out = get_out_files().toArray(new File[0]);
		msgr.send_mem_files(all_out);

		//loc_pcc.finish_chomarks_in(rem_pcc, all_in_tra_signed, owr);
		logger.debug("send_signed (" + all_out.length + ") ok. conn=" + conn);
		return all_out;
	}

	public void recv_signed() {
		recv_net_message(MSG_SIGNED_PASSETS);
		File src_dir = get_remote_paccount().get_passet_dir();

		File[] all_signa = msgr.recv_encrypted_mem_files(src_dir, null, true);

		working_trans.reset_files();
		working_trans.set_files(all_signa);

		ask_file_oper(transaction.FILE_IMPORT_OPER); // modifies all_working_iss

		logger.debug("recv_signed (" + all_signa.length + ") ok. conn=" + conn);
	}

	private void end_choice() {
		ask_file_oper(transaction.FILE_END_CHOICE_OPER);
		working_trans.reset_choice_name();
		logger.debug("end_choice ok. conn=" + conn);
	}

	private void cleanup() {
		if (is_working()) {
			working_trans.cleanup();
		}
	}

	private boolean name_has_ref(String peer_nm) {
		nx_dir_base bdir = get_local_paccount().get_dir_base();
		nx_conn_id old_coid = bdir.get_coid_by_ref(peer_nm, null);
		return (old_coid != null);
	}

	private boolean coid_has_channel(nx_conn_id the_coid) {
		File old_coid = get_local_paccount().get_dir_base().get_coid_file(
				the_coid);
		return (old_coid.exists());
	}

	private net_operator[] create_net_oper_for_each_glid(transfers_map all_iss,
			String net_op) {
		transaction trans = working_trans;

		int num_net_op = all_iss.size();
		if (num_net_op == 0) {
			logger.debug("ZERO tails in get_net_operators. ZERO net opers generated.");
			return null;
		}

		net_operator[] all_op = new net_operator[num_net_op];

		int aa = 0;
		for (Map.Entry<tag_accoglid, List<tag_transfer>> entry : all_iss
				.entrySet()) {
			all_op[aa] = null;

			tag_accoglid r_gld = entry.getKey();
			String r_descr = r_gld.get_str();

			if (r_descr.equals(UNKNOWN_STR)) {
				continue;
			}
			if (r_descr.equals(l_peer.get_description())) {
				continue;
			}

			List<tag_transfer> all_pcc = entry.getValue();

			net_operator nt_op = new net_operator(l_peer);
			transaction trans2 = transaction.get_transaction_copy(trans);
			init_redirect_transaction(trans2, r_descr, net_op, all_pcc);

			nt_op.set_first_trans(trans2);
			assert (nt_op.msgr == null);

			all_op[aa] = nt_op;
			aa++;
		}

		return all_op;
	}

	private void init_redirect_transaction(transaction nt_trans,
			String r_descr, String n_oper, List<tag_transfer> all_tra) {
		nt_trans.init_callers(null, null);
		nt_trans.coid = null;
		nt_trans.state_oper = n_oper;
		nt_trans.r_peer_descr = r_descr;
		nt_trans.r_is_addr = false;
		nt_trans.all_working_iss = all_tra;

		if (n_oper == transaction.NET_SEND_NEW_TRACKED_OPER) {
			nt_trans.all_working_iss = null;

			key_owner owr = get_owner();
			paccount local_pcc = get_local_paccount();
			List<String> tra_ids = paccount.get_passids(all_tra);
			List<String> prev_ids = local_pcc.get_all_prev_ids(all_tra);
			tra_ids.addAll(prev_ids);

			File[] all_new_trk = local_pcc.get_verif_files(tra_ids, owr);
			nt_trans.set_files(all_new_trk);
		}
	}

	private Thread start_child_net_oper(net_operator oper) {
		if (!oper.is_working()) {
			throw new bad_netpasser(2);
		}

		String thd_nm = oper.working_trans.get_child_thread_name();
		Thread thd = nx_context.start_thread(thd_nm,
				oper.get_run_once_client(), false);
		return thd;
	}

	private List<Thread> start_child_net_opers(net_operator[] net_opers) {
		List<Thread> all_thds = new ArrayList<Thread>();

		if (!is_working()) {
			throw new bad_netpasser(2);
		}
		if (net_opers == null) {
			logger.debug("ZERO net opers to start");
			return all_thds;
		}

		String sender_descr = msgr.get_remote_descr();

		logger.debug("Starting " + net_opers.length + " net_operators");
		logger.debug("Sender =" + sender_descr);

		net_operator senderissuer = null;

		for (net_operator nt_oper : net_opers) {
			if (nt_oper == null) {
				continue;
			}
			if (!nt_oper.is_working()) {
				throw new bad_netpasser(2);
			}

			transaction trans = nt_oper.working_trans;
			if (trans.r_peer_descr.equals(sender_descr)) {
				nt_oper.is_senderissuer_case = true;
				senderissuer = nt_oper;
			}

			Thread thd = start_child_net_oper(nt_oper);
			all_thds.add(thd);
		}

		if (senderissuer != null) {
			logger.debug("Waiting for senderissuer connected.");
			senderissuer.wait_for_senderissuer_lock();
			logger.debug("Got senderissuer connected !");
		}
		return all_thds;
	}

	private void send_transfer() {
		paccount rem_pcc = get_remote_paccount();
		key_owner owr = get_owner();
		
		String tra_nm = working_trans.get_choice_name();
		if(tra_nm == null){
			tra_nm = rem_pcc.start_choice(owr);
			working_trans.set_choice_name(tra_nm);
			working_trans.save_choice_name();
		}

		send_unsigned(tra_nm);
		recv_receptacles();
		sign_receptacles();
		send_signed();

		if(working_trans.get_choice_name() != tra_nm){
			throw new bad_netpasser(2);
		}
		
		end_choice();
		
		recv_last_msg();
	}

	private transfers_map create_map_by_trackers(List<tag_transfer> all_dat) {
		transfers_map iss_to_update = new transfers_map();
		paccount loc_pcc = get_local_paccount();
		key_owner owr = get_owner();
		nx_std_coref loc_glid = msgr.get_local_glid(owr);
		transfers_map all_grps_1 = transfers_map.create_by_trackers(all_dat,
				loc_glid, iss_to_update, loc_pcc, owr);

		if (IN_DEBUG_7) {
			logger.info("ALL_GRPS1=" + all_grps_1.toString());
		}

		update_channels(iss_to_update);
		return all_grps_1;
	}

	private void recv_transfer() {
		File[] all_unsig = recv_unsigned();
		update_prev_transf(all_unsig);

		make_receptacles();
		send_receptacles();
		recv_signed();

		// all_working_iss may have changed
		transfers_map all_grps_2 = create_map_by_trackers(working_trans.all_working_iss);
		String net_op3 = transaction.NET_SEND_ADD_VERIF_OPER;
		net_operator[] net_opers3 = create_net_oper_for_each_glid(all_grps_2,
				net_op3);
		List<Thread> thds_op3 = start_child_net_opers(net_opers3);
		wait_for_all_threads(thds_op3);

		send_last_msg();
	}

	private void update_prev_transf(File[] all_usgned_ff) {
		// update trackers

		List<tag_transfer> all_dat = paccount.get_all_first_passets(Arrays
				.asList(all_usgned_ff));
		working_trans.all_working_iss = all_dat;

		if (IN_DEBUG_7) {
			logger.info("RECV_UNSIGNED=" + all_dat.toString());
			logger.info("RECV_UNSIGNED_FFS=" + Arrays.toString(all_usgned_ff));
		}

		transfers_map all_grps_1 = create_map_by_trackers(all_dat);

		// get transfers

		String net_op2 = transaction.NET_SEND_GET_TRANSFER_OPER;
		net_operator[] net_opers2 = create_net_oper_for_each_glid(all_grps_1,
				net_op2);
		List<Thread> thds_op2 = start_child_net_opers(net_opers2);
		wait_for_all_threads(thds_op2);

		logger.debug("recv_unsigned (" + all_usgned_ff.length + ") ok. conn="
				+ conn);
	}

	private boolean get_pending_transaction() {
		if (!ready_for_work()) {
			throw new bad_netpasser(2);
		}
		if (has_work()) {
			throw new bad_netpasser(2);
		}
		if (is_working()) {
			throw new bad_netpasser(2);
		}

		try {
			logger.debug("Getting pending trans from queue=" + to_net_operate);
			transaction tmp_trans = to_net_operate.poll(0,
					TimeUnit.MICROSECONDS);
			if (tmp_trans == null) {
				running_q = false;
				return false;
			}

			set_first_trans(tmp_trans);
		} catch (InterruptedException e) {
			throw new bad_netpasser(2, e.toString());
		}
		if (!is_working()) {
			throw new bad_netpasser(2);
		}
		if (working_trans.state_oper == transaction.INVALID_OPER) {
			throw new bad_netpasser(2);
		}
		if (working_trans.state_oper == transaction.FINISH_OPER) {
			running_q = false;
			return false;
		}
		return true;
	}

	private void wait_for_all_threads(List<Thread> all_thds) {
		while (!all_thds.isEmpty()) {
			Thread thd = all_thds.get(0);
			wait_for_thread(thd);
			if (!thd.isAlive()) {
				Thread rm_thd = all_thds.remove(0);
				if (rm_thd != thd) {
					throw new bad_netpasser(2);
				}
			}
		}
	}

	private void wait_for_thread(Thread thd) {
		int max_secs = MAX_SECS_TO_WAIT_FOR_THREAD;
		for (int aa = 0; aa < max_secs; aa++) {
			try {
				if (thd.isAlive()) {
					thd.join(config.SEC_MILLIS);
				}
				if (msgr != null) {
					msgr.send_string(MSG_KEEP_ALIVE);
				}
				if (!thd.isAlive()) {
					return;
				}
			} catch (InterruptedException ee) {
				logger.error(ee, "Interrupted while witing for child to finish");
				return;
			}
		}
		throw new bad_netpasser(2, String.format(L.timeout_waiting_for_thread,
				thd.getName()));
	}

	private void calc_split_change() {
		ask_file_oper(transaction.FILE_CALC_SPLIT_CHANGE_OPER);
		logger.debug("calc_split_change ok." + " conn=" + conn);
	}

	private void calc_join_change() {
		ask_file_oper(transaction.FILE_CALC_JOIN_CHANGE_OPER);
		logger.debug("calc_join_change ok." + " conn=" + conn);
	}

	private void recv_get_transfers() {
		recv_net_message(MSG_GET_TRANSFERS);
		byte[] all_ids_dat = msgr.recv_bytes();
		List<String> all_ids = parse.read_byte_array_lines(all_ids_dat);

		paccount l_pcc = get_local_paccount();
		File[] all_tras = l_pcc.get_verif_files(all_ids, get_owner());

		if (all_tras.length == 0) {
			logger.info("GOT 0 transfers in issuer during recv_get_transfers");
		}
		if (all_ids.size() != all_tras.length) {
			logger.info("ids_sz_not_eq_vrf_ffs=(" + all_ids.size() + " != "
					+ all_tras.length);
		}

		msgr.send_encrypted_mem_files(all_tras, null);
		logger.debug("recv_get_transfers ok. conn=" + conn);
	}

	private File[] send_get_transfers() {
		paccount l_pcc = get_local_paccount();
		List<tag_transfer> all_iss_dat = working_trans.all_working_iss;
		byte[] all_ids_dat = get_prev_ids_data(l_pcc, all_iss_dat);

		msgr.send_string(MSG_GET_TRANSFERS);
		msgr.send_bytes(all_ids_dat);

		File tdir = l_pcc.get_verif_dir();

		File[] all_tra_ff = msgr.recv_encrypted_mem_files(tdir, null, true);
		set_verified(all_tra_ff);

		logger.debug("send_get_transfers ok. conn=" + conn
				+ "\nALL_TRANSFERS=\n" + Arrays.toString(all_tra_ff));
		return all_tra_ff;
	}

	private static byte[] get_prev_ids_data(paccount l_pcc,
			List<tag_transfer> all_iss_dat) {
		if (all_iss_dat == null) {
			throw new bad_netpasser(2);
		}
		if (all_iss_dat.isEmpty()) {
			throw new bad_netpasser(2, L.asking_for_zero_passids);
		}

		List<String> all_lines = l_pcc.get_all_prev_ids(all_iss_dat);

		if (IN_DEBUG_9) {
			logger.debug("ALL_PREV_IDS=\n" + all_lines);
			if (all_lines.isEmpty()) {
				logger.info("ALL_PREV_IDS=0 !!!!!!!");
			}
		}

		byte[] dat = parse.write_byte_array_lines(all_lines);
		return dat;
	}

	private void send_add_verif() {
		if (working_trans.all_working_iss == null) {
			throw new bad_netpasser(2);
		}

		paccount l_pcc = get_local_paccount();

		File[] all_ff = l_pcc.get_all_verif_files(
				working_trans.all_working_iss, get_owner());

		if (IN_DEBUG_2) {
			logger.debug("SEND_ADD_VERIF for=\n" + Arrays.toString(all_ff));
		}

		msgr.send_string(MSG_START_ADD_VERIF);
		msgr.send_mem_files(all_ff);

		recv_net_message(MSG_END_ADD_VERIF);

		if (IN_DEBUG_2) {
			logger.debug("send_add_verif_ok." + " conn=" + conn);
		}
	}

	private boolean recv_add_verif() {
		File src_dir = get_remote_paccount().get_passet_dir();

		recv_net_message(MSG_START_ADD_VERIF);

		if (IN_DEBUG_2) {
			logger.debug("begin recv_add_verif" + " conn=" + conn);
		}

		File[] all_ff = msgr.recv_encrypted_mem_files(src_dir, null, true);
		working_trans.set_files(all_ff);

		if (IN_DEBUG_2) {
			logger.debug("RECV_ADD_VERIF_FOR=\n" + Arrays.toString(all_ff));
		}

		boolean resp = false;

		try {
			ask_file_oper(transaction.FILE_ADD_DIFF_OPER);
			resp = true;
		} catch (bad_netpasser ee1) {
			resp = false;
		}

		msgr.send_string(MSG_END_ADD_VERIF);

		if (IN_DEBUG_2) {
			logger.debug("recv_add_verif_ok." + " conn=" + conn);
		}
		return resp;
	}

	void send_all_data_files() {
		msgr.send_string(MSG_DATA_FILES);

		paccount loc = get_local_paccount();
		paccount rem = get_remote_paccount();
		key_owner owr = get_owner();

		send_mutual_file_if_diff(loc.get_current_user_image_file(),
				rem.get_current_user_image_file(), null);

		send_mutual_file_if_diff(loc.get_current_user_info_file(),
				rem.get_current_user_info_file(), owr);

		send_mutual_file_if_diff(loc.get_trusted_file(),
				rem.get_trusted_file(), owr);

		send_mutual_file_if_diff(loc.get_not_trusted_file(),
				rem.get_not_trusted_file(), owr);

		send_mutual_file_if_diff(loc.get_all_trackers_file(),
				rem.get_all_trackers_file(), owr);

		send_mutual_file_if_diff(loc.get_next_tracker_file(),
				rem.get_next_tracker_file(), owr);
	}

	void recv_all_data_files() {
		recv_net_message(MSG_DATA_FILES);

		paccount loc = get_local_paccount();
		paccount rem = get_remote_paccount();
		key_owner owr = get_owner();

		recv_mutual_file_if_diff(loc.get_current_user_image_file(),
				rem.get_current_user_image_file(), null);

		recv_mutual_file_if_diff(loc.get_current_user_info_file(),
				rem.get_current_user_info_file(), owr);

		recv_mutual_file_if_diff(loc.get_trusted_file(),
				rem.get_trusted_file(), owr);

		recv_mutual_file_if_diff(loc.get_not_trusted_file(),
				rem.get_not_trusted_file(), owr);

		recv_mutual_file_if_diff(loc.get_all_trackers_file(),
				rem.get_all_trackers_file(), owr);

		recv_mutual_file_if_diff(loc.get_next_tracker_file(),
				rem.get_next_tracker_file(), owr);
	}

	private void set_verified(File[] all_usgned_ff) {
		// update trackers

		if (all_usgned_ff == null) {
			return;
		}
		if (all_usgned_ff.length == 0) {
			return;
		}

		List<File> all_ff = Arrays.asList(all_usgned_ff);
		List<tag_transfer> all_dat = paccount.get_all_first_passets(all_ff);

		key_owner owr = get_owner();
		paccount loc_pcc = get_local_paccount();

		create_map_by_trackers(all_dat); // update issuers

		tag_accoglid iss_gld = null;

		boolean all_ok = true;
		for (tag_transfer n_tra : all_dat) {
			tag_accoglid iss_tra = n_tra.get_root_issuer();
			if (iss_gld == null) {
				iss_gld = iss_tra;
			} else if (!iss_gld.equals(iss_tra)) {
				all_ok = false;
				if (IN_DEBUG_10) {
					logger.info("set_verified." + iss_gld + "(iss_gld) != \n"
							+ iss_tra + " (iss_tra)");
				}
				break;
			}
			if (iss_gld == null) {
				all_ok = false;
				if (IN_DEBUG_10) {
					logger.info("set_verified. (iss_gld == null)");
				}
				break;
			}

			tag_accoglid trk_tra = n_tra.get_tracker_accoglid();
			boolean is_trk_ok = loc_pcc.is_tracker(iss_gld, trk_tra, owr);
			if (!is_trk_ok) {
				all_ok = false;
				if (IN_DEBUG_10) {
					logger.info("set_verified. " + trk_tra
							+ " (trk_tra) is not tracker of \n" + iss_gld
							+ " in \n" + msgr.get_local_glid(owr));
				}
				break;
			}
		}

		if (!all_ok) {
			throw new bad_netpasser(2);
		}

		nx_std_coref rem_glid = msgr.get_remote_glid();
		tag_accoglid rem_gld = new tag_accoglid(rem_glid);

		boolean rem_is_trk = loc_pcc.is_tracker(iss_gld, rem_gld, owr);
		if (!rem_is_trk) {
			throw new bad_netpasser(2);
		}

		paccount.set_verified_tag_to(all_usgned_ff, get_owner());

		if (IN_DEBUG_8) {
			logger.debug("update_new_tracked_transf ok. conn=" + conn
					+ "\nALL_TRANSFERS=\n" + Arrays.toString(all_usgned_ff));
		}
	}

	private void send_new_tracked() {
		msgr.send_string(MSG_NEW_TRACKED);

		File[] all_v_ff = get_files();

		msgr.send_encrypted_mem_files(all_v_ff, null);

		if (IN_DEBUG_6) {
			logger.debug("send-new_tracked_ok. conn=" + conn);
			logger.debug("SEND_NEW_TRACKED_FOR=\n" + Arrays.toString(all_v_ff));
		}
	}

	private void recv_new_tracked() {
		recv_net_message(MSG_NEW_TRACKED);

		paccount l_pcc = get_local_paccount();
		File tdir = l_pcc.get_verif_dir();

		File[] all_tra_ff = msgr.recv_encrypted_mem_files(tdir, null, true);
		set_verified(all_tra_ff);

		if (IN_DEBUG_6) {
			logger.debug("RECV_NEW_TRACKED_FOR=\n"
					+ Arrays.toString(all_tra_ff));
		}
	}

	private void update_channels(transfers_map iss_to_update) {
		if (iss_to_update.isEmpty()) {
			return;
		}

		if (IN_DEBUG_4) {
			// logger.info("UPDATING_TRACKERS_FOR=");
			// logger.info(iss_to_update);
		}

		String net_op1 = transaction.NET_SEND_CREATE_CHANN_OPER;
		net_operator[] net_opers1 = create_net_oper_for_each_glid(
				iss_to_update, net_op1);
		List<Thread> thds_op1 = start_child_net_opers(net_opers1);
		wait_for_all_threads(thds_op1);
	}

	private List<tag_transfer> get_all_issued_by_delegators(
			List<tag_transfer> all_tra) {
		List<tag_transfer> tr_tra = new ArrayList<tag_transfer>();

		paccount loc_pcc = get_local_paccount();
		key_owner owr = get_owner();
		nx_std_coref loc_glid = msgr.get_local_glid(owr);
		tag_accoglid trk_gld = new tag_accoglid(loc_glid);

		for (tag_transfer the_tra : all_tra) {
			tag_accoglid iss = the_tra.get_root_issuer();
			if (loc_pcc.is_tracker(iss, trk_gld, owr)) {
				tr_tra.add(the_tra);
			}
		}
		return tr_tra;
	}

	private void report_change() {
		List<tag_transfer> all_tra = get_all_issued_by_delegators(working_trans.all_working_iss);

		transfers_map all_grps_1 = create_map_by_trackers(all_tra);

		String net_op2 = transaction.NET_SEND_NEW_TRACKED_OPER;
		net_operator[] net_opers2 = create_net_oper_for_each_glid(all_grps_1,
				net_op2);
		List<Thread> thds_op2 = start_child_net_opers(net_opers2);
		wait_for_all_threads(thds_op2);
	}

}
