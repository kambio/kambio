package emetcode.economics.netpasser;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import emetcode.crypto.bitshake.utils.file_funcs;
import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.crypto.bitshake.utils.mem_file;
import emetcode.economics.netpasser.locale.L;
import emetcode.economics.passet.channel;
import emetcode.economics.passet.config;
import emetcode.economics.passet.iso;
import emetcode.economics.passet.paccount;
import emetcode.economics.passet.tag_accoglid;
import emetcode.economics.passet.tag_denomination;
import emetcode.economics.passet.tag_transfer;
import emetcode.economics.passet.trackers;
import emetcode.economics.passet.transfers_map;
import emetcode.net.netmix.nx_conn_id;
import emetcode.net.netmix.nx_context;
import emetcode.net.netmix.nx_dir_base;
import emetcode.net.netmix.nx_peer;
import emetcode.net.netmix.nx_std_coref;
import emetcode.net.netmix.locator_sys.nx_connector;
import emetcode.net.netmix.locator_sys.nx_locator;
import emetcode.net.netmix.locator_sys.nx_supra_locator;
import emetcode.net.netmix.mudp_adapter.nx_mudp_context;
import emetcode.net.netmix.mudp_adapter.nx_mudp_peer;
import emetcode.net.netmix.tcp_adapter.nx_tcp_context;
import emetcode.util.devel.bad_emetcode;
import emetcode.util.devel.logger;
import emetcode.util.devel.net_funcs;
import emetcode.util.devel.thread_funcs;

public class command_line_transfer {

	public static final boolean IN_DEBUG_1 = true;

	public static final String PID_F_NM = "srv_PID.txt";

	public static final File DEFAULT_ROOT = new File("./test_netpasser_user");

	public static final File DOT_DIR = new File(".");

	public static final char EOL = '\n';
	public static final char TAB = '\t';

	public static final int DEFAULT_CURRENCY = emetcode.economics.passet.config.DEFAULT_CURRENCY;

	public static final long DEFAULT_PASSETNET_MUDP_TRANSFER_PORT = transaction.DEFAULT_PASSETNET_MUDP_TRANSFER_PORT;
	public static final long DEFAULT_PASSETNET_MUDP_LOCATOR_PORT = nx_locator.LOCATOR_PORT;
	public static final long DEFAULT_PASSETNET_MUDP_SUPRALOCATOR_PORT = nx_supra_locator.SUPRA_LOCATOR_PORT;

	static String help_msg = "params:" + EOL + TAB + "[-k <key>] " + EOL + TAB
			+ "[-h|-help|-v] " + "[-tcp|-mudp] " + "[-prt_locators] "
			+ "[-prt_nxt_locator] " + "[-m <iso_currency_code>] " + EOL + TAB
			+ "[-chann|-send|-recv|-split|-join|-renew_chann] " + EOL + TAB
			+ "[-port <port_num>] " + "[-r <r_descr>] " + "[-r_is_addr] " + EOL
			+ TAB + "[-is_supra] " + "[-is_locator] "
			+ "[-add_supra <net_addr>] " + "[-sk <start_key>] " + EOL + TAB
			+ "[-issue_in_next_tracker <num__iss>]"
			+ "[-set_next_locator <net_addr>] " + EOL + TAB
			+ "[-connect|-accept] " + "[-once|-loop] " + "[-kill_all] "
			+ "[-kill] " + "[-daemon] " + EOL + TAB
			+ "[-repeat <choice_name>] " + "[-f_for_cho <file_nm>] " + EOL;

	static String help_msg2 = "-k : use <key> to encrypt and decrypt files."
			+ EOL
			+ "-tcp : use tcp protocol."
			+ EOL
			+ "-mudp : use mudp protocol."
			+ EOL
			+ "-m : work with <iso_currency_code> as currency."
			+ EOL
			+ "-repeat <choice_name> : retry action with <choice_name>. "
			+ EOL
			+ "-f_for_cho <file_nm> : write the choice name used in <file_nm>. "
			+ EOL
			+ "---------------"
			+ EOL
			+ "-chann : just create the channel with the given <r_descr> (option -r)."
			+ EOL
			+ "-send : send the selected passets. See selector help."
			+ EOL
			+ "-recv : receive passets."
			+ EOL
			+ "-split : send selected passets for a split change."
			+ EOL
			+ "-join : send selected passets for a join change."
			+ EOL
			+ "---------------"
			+ EOL
			+ "-renew_chann : renew channel for future transactions with same trader."
			+ EOL
			+ "---------------"
			+ EOL
			+ "-is_supra : work as a supra-locator server."
			+ EOL
			+ "-add_supra : adds the network address <net_addr> to the list of supralocators."
			+ EOL
			+ "-set_next_locator : This supralocator will suggest <net_addr> as a locator for clients."
			+ EOL
			+ "-port : use <port_num> as local port to accept or make net connections."
			+ EOL
			+ "-r : connect to remote <r_descr>."
			+ EOL
			+ "-r_is_addr : the given <r_descr> is an adress."
			+ EOL
			+ TAB
			+ "If the option -r is not used, then it waits for a connection."
			+ EOL
			+ "---------------"
			+ EOL
			+ "-loop : loop as a service."
			+ EOL
			+ "-daemon : loop as a backround service."
			+ EOL
			+ "---------------"
			+ EOL
			+ "-sk : use <start_key> as the bootstrap key to start the connection."
			+ EOL + "-h : show invocation info." + EOL
			+ "-help : show full help invocation info." + EOL
			+ "-v : show version info." + EOL;

	static String version_msg = "command_line_issuer v1.0"
			+ "(c) 2013. QUIROGA BELTRAN, Jose Luis. Bogota - Colombia.";

	int net_kind;

	boolean prt_locators;
	boolean prt_nxt_locator;

	boolean is_supra;
	boolean is_locator;
	String addr_supra;
	String addr_nxt_locator;

	int iss_num;

	boolean is_connecting;
	boolean is_accepting;

	boolean is_once;
	boolean is_loop;

	boolean is_daemon;

	int net_port;

	String r_descr;
	boolean r_is_addr;

	File root_dir;
	byte[] l_key;
	int cho_currcy_idx;

	byte[] start_key;
	boolean renew_channel;

	String trans_oper;

	String choice_nm;
	String cho_f_nm;
	
	char dbg_skip_finish_chomarks;

	public command_line_transfer() {
		net_kind = net_funcs.TCP_NET;

		prt_locators = false;
		prt_nxt_locator = false;

		is_supra = false;
		is_locator = false;
		addr_supra = null;
		addr_nxt_locator = null;

		iss_num = 0;

		is_connecting = false;
		is_accepting = false;

		is_once = false;
		is_loop = false;

		is_daemon = false;

		net_port = transaction.DEFAULT_PASSETNET_UDP_PORT;

		r_descr = null;
		r_is_addr = false;

		root_dir = DOT_DIR;
		l_key = null;

		start_key = null;
		renew_channel = false;

		trans_oper = null;
		
		choice_nm = null;
		cho_f_nm = null;

		dbg_skip_finish_chomarks = paccount.DBG_INVALID_SKIP_FINISH_CHOMARKS;
	}

	public boolean get_args(String[] args) {

		boolean prt_help = (args.length == 0);
		boolean prt_full_help = false;
		boolean prt_version = false;

		boolean kill = false;
		boolean kill_all = false;

		boolean is_sending = false;
		boolean is_receiving = false;

		net_port = transaction.DEFAULT_PASSETNET_UDP_PORT;

		r_descr = null;
		r_is_addr = false;

		trans_oper = null;

		choice_nm = null;
		cho_f_nm = null;

		cho_currcy_idx = -1;

		int num_args = args.length;
		for (int ii = 0; ii < num_args; ii++) {
			String the_arg = args[ii];
			if (the_arg.equals("-h")) {
				prt_help = true;
			} else if (the_arg.equals("-help")) {
				prt_full_help = true;
			} else if (the_arg.equals("-v")) {
				prt_version = true;
			} else if (the_arg.equals("-tcp")) {
				net_kind = net_funcs.TCP_NET;
			} else if (the_arg.equals("-mudp")) {
				net_kind = net_funcs.MUDP_NET;
			} else if (the_arg.equals("-prt_locators")) {
				prt_locators = true;
			} else if (the_arg.equals("-prt_nxt_locator")) {
				prt_nxt_locator = true;
			} else if (the_arg.equals("-chann")) {
				trans_oper = transaction.NET_SEND_CREATE_CHANN_OPER;
				is_sending = true;
			} else if (the_arg.equals("-send")) {
				trans_oper = transaction.NET_SEND_PASSETS_OPER;
				is_sending = true;
			} else if (the_arg.equals("-recv")) {
				trans_oper = transaction.NET_RECV_PASSETS_OPER;
				is_receiving = true;
			} else if (the_arg.equals("-split")) {
				trans_oper = transaction.NET_SEND_CHANGE_SPLIT_OPER;
				is_sending = true;
			} else if (the_arg.equals("-join")) {
				trans_oper = transaction.NET_SEND_CHANGE_JOIN_OPER;
				is_sending = true;
			} else if (the_arg.equals("-is_locator")) {
				is_supra = true;
				is_locator = true;
			} else if (the_arg.equals("-is_supra")) {
				is_supra = true;
				is_locator = true;
			} else if (the_arg.equals("-connect")) {
				is_connecting = true;
			} else if (the_arg.equals("-accept")) {
				is_accepting = true;
			} else if (the_arg.equals("-once")) {
				is_once = true;
			} else if (the_arg.equals("-loop")) {
				is_loop = true;
			} else if (the_arg.equals("-kill")) {
				kill = true;
			} else if (the_arg.equals("-kill_all")) {
				kill_all = true;
			} else if (the_arg.equals("-daemon")) {
				is_daemon = true;
			} else if (the_arg.equals("-renew_chann")) {
				renew_channel = true;
			} else if (the_arg.equals("-r_is_addr")) {
				r_is_addr = true;
			} else if (the_arg.equals("-dbg_no_end_cho")) {
				dbg_skip_finish_chomarks = paccount.DBG_SET_SKIP_FINISH_CHOMARKS;
			} else if (the_arg.equals("-dbg_yes_end_cho")) {
				dbg_skip_finish_chomarks = paccount.DBG_RESET_SKIP_FINISH_CHOMARKS;
			} else if ((the_arg.equals("-m")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				String val = args[kk_idx].toUpperCase();
				int crrcy_idx = iso.get_currency_idx(val);
				if (crrcy_idx == -1) {
					System.out.println("invalid currency code. use option -pm");
					break;
				}
				cho_currcy_idx = crrcy_idx;
			} else if ((the_arg.equals("-issue_in_next_tracker"))
					&& ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				iss_num = Integer.parseInt(args[kk_idx]);
			} else if ((the_arg.equals("-set_next_locator"))
					&& ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				addr_nxt_locator = args[kk_idx];
			} else if ((the_arg.equals("-add_supra")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				addr_supra = args[kk_idx];
			} else if ((the_arg.equals("-port")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;
				String the_val = args[kk_idx];

				net_port = Integer.parseInt(the_val);
			} else if ((the_arg.equals("-r")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				r_descr = args[kk_idx];
			} else if ((the_arg.equals("-k")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				l_key = args[kk_idx].getBytes(config.UTF_8);
			} else if ((the_arg.equals("-sk")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				start_key = args[kk_idx].getBytes(config.UTF_8);
			} else if ((the_arg.equals("-repeat"))
					&& ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				choice_nm = args[kk_idx];
			} else if ((the_arg.equals("-f_for_cho"))
					&& ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				cho_f_nm = args[kk_idx];
			} else {
				prt_help = true;
			}
		}

		System.out.println();
		System.out.println();
		System.out.print("***************************************");
		System.out.print(" transfer ");
		System.out.println("***************************************");

		if (kill_all) {
			kill_all_servers();
			return false;
		}

		if (is_sending == is_receiving) {
			is_sending = false;
			is_receiving = true;
		}

		if (is_connecting == is_accepting) {
			if (is_receiving) {
				is_connecting = false;
				is_accepting = true;
			} else {
				is_connecting = true;
				is_accepting = false;
			}
		}

		if (is_once == is_loop) {
			if (is_receiving) {
				is_once = false;
				is_loop = true;
			} else {
				is_once = true;
				is_loop = false;
			}
		}

		if (is_supra || is_locator) {
			is_connecting = false;
			is_accepting = false;
		}

		if (is_accepting && is_loop) {
			is_locator = true;
		}

		if ((addr_supra != null) || (addr_nxt_locator != null)) {
			is_connecting = false;
			is_accepting = false;
			is_supra = false;
			is_locator = false;
			is_once = false;
			is_loop = false;
		}

		if (iss_num > 0) {
			is_connecting = true;
			is_accepting = false;
			is_supra = false;
			is_locator = false;
			is_once = true;
			is_loop = false;
		}

		if (prt_help) {
			System.out.println(help_msg);
			return false;
		}
		if (prt_full_help) {
			System.out.println(help_msg);
			System.out.println(help_msg2);
			return false;
		}
		if (prt_version) {
			System.out.println(version_msg);
			return false;
		}
		if (l_key == null) {
			System.out.println(help_msg);
			return false;
		}

		if (kill) {
			key_owner owr = new key_owner(l_key);
			paccount pcc = new paccount();
			pcc.set_base_dir(root_dir, owr, net_kind, null);
			kill_pid_in(pcc.get_mikid_dir());
			return false;
		}

		return true;
	}

	private void start_transfer_service() {		
		System.out.println("ROOT_DIR=" + root_dir);
		System.out.println("OPER=" + trans_oper);
		if (is_accepting) {
			System.out.println("is_accepting");
		}
		if (is_connecting) {
			System.out.println("is_connecting");
		}
		if (is_once) {
			System.out.println("is_once");
		}
		if (is_loop) {
			System.out.println("is_loop");
		}

		key_owner owr = new key_owner(l_key);

		if (!paccount.is_user(root_dir, owr)) {
			System.out.println("NO user with given <key>. See passet_issuer.");
			return;
		}

		// init transactions

		transaction srv_trans = new transaction();
		srv_trans.init_local_paccount(root_dir, owr, net_kind);
		transaction cli_trans = new transaction();
		cli_trans.init_local_paccount(root_dir, owr, net_kind);

		// add supra

		// set local paccount

		paccount local_pcc = cli_trans.local_pcc;

		// chomarks

		if (dbg_skip_finish_chomarks != paccount.DBG_INVALID_SKIP_FINISH_CHOMARKS) {
			File skip_ff = local_pcc.get_dbg_no_end_choice_file();
			if (dbg_skip_finish_chomarks == paccount.DBG_SET_SKIP_FINISH_CHOMARKS) {
				mem_file.concurrent_create_file(skip_ff);
			} else {
				skip_ff.delete();
			}
		}

		// init currency

		tag_denomination defa_deno = local_pcc.read_deno_file();
		if ((defa_deno != null) && (cho_currcy_idx == -1)) {
			cho_currcy_idx = defa_deno.currency_idx;
		}
		if (cho_currcy_idx == -1) {
			cho_currcy_idx = config.DEFAULT_CURRENCY;
		}

		srv_trans.set_working_currency(cho_currcy_idx);
		cli_trans.set_working_currency(cho_currcy_idx);

		// -chann

		if (trans_oper == transaction.NET_SEND_CREATE_CHANN_OPER) {
			if(r_descr == null){
				System.out.println("Option -r needed.");
				return;
			}
			nx_dir_base bb_dd = local_pcc.get_dir_base();
			nx_conn_id r_coid = bb_dd.get_coid_by_ref(r_descr, null);
			if (r_coid != null) {
				System.out.println("REMOTE_PEER=" + r_descr + " HAS_CHANNEL=\n"
						+ r_coid);
				return;
			}
		}

		// prt_locators

		if (prt_locators) {
			List<String> all_loctors = nx_connector.read_local_locators(
					local_pcc.get_dir_base(), owr);
			System.out.println("LOCATORS=");
			for (String loc : all_loctors) {
				System.out.println(loc);
			}
			return;
		}

		// prt_nxt_locator

		if (prt_nxt_locator) {
			String nx_loc = nx_supra_locator.read_next_locator(
					local_pcc.get_dir_base(), owr);
			System.out.println("NEXT_LOCATOR=" + nx_loc);
			return;
		}

		trackers trks = new trackers();
		if (iss_num > 0) {
			trks.init_trackers(local_pcc, owr);
			if (trks.next_tracker == null) {
				System.out.println("Use -set_next_tracker first !");
				return;
			}
		}

		// set next locator

		if (addr_nxt_locator != null) {
			nx_supra_locator.write_next_locator(local_pcc.get_dir_base(), owr,
					addr_nxt_locator);
			System.out.println("NEXT_LOCATOR=" + addr_nxt_locator);
			return;
		}

		// init l_descr

		String l_addr_for_repo = null;
		String l_addr_for_transfs = null;
		String l_addr_for_locator = null;
		String l_addr_for_supra = null;

		local_pcc.prt_basic_data(owr);
		System.out.println("Starting transfer with name '" + l_addr_for_transfs
				+ "'.");

		// init peer

		nx_context ctx = null;
		if (net_kind == net_funcs.I2P_NET) {
			logger.info("Usign i2p");
			//ctx = new nx_i2p_context(root_dir);
		} else if (net_kind == net_funcs.MUDP_NET) {
			l_addr_for_transfs = "localhost:" + net_port + ":"
					+ DEFAULT_PASSETNET_MUDP_TRANSFER_PORT;
			l_addr_for_repo = "localhost:" + net_port + ":"
					+ DEFAULT_PASSETNET_MUDP_LOCATOR_PORT;
			l_addr_for_locator = "localhost:" + net_port + ":"
					+ DEFAULT_PASSETNET_MUDP_LOCATOR_PORT;
			l_addr_for_supra = "localhost:" + net_port + ":"
					+ DEFAULT_PASSETNET_MUDP_SUPRALOCATOR_PORT;

			InetSocketAddress addr = nx_mudp_peer
					.get_address(l_addr_for_transfs);
			logger.info("Usign MUDP with addr=" + addr);
			ctx = new nx_mudp_context(root_dir, addr);
		} else {
			l_addr_for_transfs = "localhost:" + net_port;
			logger.info("Usign TCP");
			ctx = new nx_tcp_context(root_dir);
		}

		if (l_addr_for_transfs == null) {
			throw new bad_netpasser(2, L.invalid_local_address_for_transfers);
		}

		nx_peer l_for_trfs = ctx.make_peer();
		l_for_trfs.init_local_peer(l_addr_for_transfs, owr, is_accepting);

		// init operators

		net_operator srv_net_op = new net_operator(l_for_trfs);
		net_operator cli_net_op = new net_operator(l_for_trfs);
		file_operator fl_op = new file_operator(l_for_trfs);

		// fill selected coid

		nx_conn_id selected_coid = null;
		if (r_descr != null) {
			int[] num_found = new int[1];
			num_found[0] = 0;
			selected_coid = local_pcc.get_dir_base().get_coid_by_ref(r_descr,
					num_found);
			if (num_found[0] > 1) {
				System.out.println("MORE THAN ONE COID for '" + r_descr
						+ "'. MUST SPECIFY ONE.");
				return;
			}
			if (selected_coid != null) {
				move_default_chosen();

				channel chn = channel.read_channel(local_pcc, owr,
						selected_coid);
				if (chn != null) {
					System.out.println("WORKING CHANNEL=\n");
					chn.print(System.out, false);
				} else {
					System.out.println("WORKING COID=\n" + selected_coid);
				}
			}
		}

		if (selected_coid == null) {
			renew_channel = true;
		}

		// config.DEFAULT_TRUSTED_LEVEL = local_pss.get_local_recv_lv(owr);

		// get recepuk

		// fill transactions

		srv_trans.state_oper = transaction.NET_ACCEPT_OPER;
		srv_trans.init_callers(null, fl_op);
		srv_trans.start_key = start_key;
		srv_trans.coid = selected_coid;

		cli_trans.state_oper = trans_oper;
		cli_trans.init_callers(null, fl_op);
		cli_trans.start_key = start_key;
		cli_trans.coid = selected_coid;

		cli_trans.r_peer_descr = r_descr;
		cli_trans.repo_locat = false;
		cli_trans.r_is_addr = r_is_addr;

		if(choice_nm != null){
			cli_trans.set_choice_name(choice_nm);
		}
		if(cho_f_nm != null){
			cli_trans.set_file_for_choice_name(cho_f_nm);
		}
		
		// GET DIR BASE

		nx_dir_base b_dir = local_pcc.get_dir_base();
		if (b_dir == null) {
			throw new bad_netpasser(2);
		}

		// issue in tracker

		if (iss_num > 0) {
			if (trks.next_tracker == null) {
				throw new bad_netpasser(2);
			}

			String the_nxt_trk = trks.next_tracker;
			if (IN_DEBUG_1) {
				logger.info("issuing_in_nxt_tracker=" + the_nxt_trk);
			}

			nx_conn_id ltr_coid = b_dir.get_coid_by_ref(the_nxt_trk, null);
			if (ltr_coid == null) {
				ctx.stop_context();
				System.out.println("Use -chann first with '" + the_nxt_trk
						+ "'");
				return;
			}

			List<tag_transfer> all_tra = new ArrayList<tag_transfer>();
			tag_denomination iss_deno = new tag_denomination(defa_deno);
			tag_accoglid nxt_trk_acco = new tag_accoglid(the_nxt_trk);
			local_pcc.issue_passets(iss_num, owr, iss_deno, all_tra,
					nxt_trk_acco);

			if (all_tra.isEmpty()) {
				throw new bad_netpasser(2);
			}

			if (IN_DEBUG_1) {
				List<File> all_tra_ff = local_pcc.get_all_passet_files(all_tra);
				logger.info("ALL_ISSUE_TRA=" + all_tra.toString());
				logger.info("ALL_ISSUE_FFS=" + all_tra_ff.toString());

				nx_std_coref loc_glid = local_pcc.get_glid(owr);
				transfers_map all_grps_1 = transfers_map.create_by_trackers(
						all_tra, loc_glid, null, null, null);

				logger.info("ALL_ISSUE_PREV=" + all_grps_1.toString());
			}

			tag_transfer fst_tra = all_tra.get(0);
			String tra_nxt_trk = fst_tra.get_tracker_accoglid().get_str();
			if (!the_nxt_trk.equals(tra_nxt_trk)) {
				throw new bad_netpasser(2);
			}

			List<String> tra_ids = paccount.get_passids(all_tra);
			List<String> prev_ids = local_pcc.get_all_prev_ids(all_tra);

			tra_ids.addAll(prev_ids);

			if (IN_DEBUG_1) {
				logger.info("SENDING_NEW_TRACKED=" + tra_ids.toString());
			}

			File[] all_new_trk = local_pcc.get_verif_files(tra_ids, owr);

			cli_trans.state_oper = transaction.NET_SEND_NEW_TRACKED_OPER;
			cli_trans.coid = null;
			cli_trans.r_peer_descr = the_nxt_trk;
			cli_trans.r_is_addr = false;

			cli_trans.set_files(all_new_trk);

			if (IN_DEBUG_1) {
				logger.info("ISSUED_TRAS=" + all_tra.toString());
			}
		}

		// start threads

		nx_peer l_for_loca = null;
		Thread t_loca = null;
		nx_peer l_for_supra = null;
		Thread t_supra = null;
		Thread fl_thd = null;
		Thread srv_thd = null;
		Thread cli_thd = null;

		if ((addr_supra != null) && (l_addr_for_repo != null)) {
			nx_peer l_for_repo = ctx.make_peer();
			l_for_repo.init_local_peer(l_addr_for_repo, owr, false);
			add_supralocator(l_for_repo, b_dir, owr, addr_supra);
		}

		String w_thd_nm = null;
		Thread w_thd = null;

		if (is_locator && (l_addr_for_locator != null)) {
			key_owner owr_1 = key_owner.get_copy(owr);
			l_for_loca = ctx.make_peer();
			l_for_loca.init_local_peer(l_addr_for_locator, owr_1, false);
			logger.info("LOCATOR.Local_peer=" + l_for_loca);

			nx_locator l_loca = new nx_locator(b_dir, owr_1, l_for_loca);

			Map<String, String> srv_ports = new TreeMap<String, String>();
			srv_ports.put(net_operator.NETPASSER_SERVICE_NAME,
					net_operator.NETPASSER_PORT_DESCR);

			t_loca = l_loca.start_locator_server(srv_ports);
			w_thd = t_loca;
		}

		if (is_supra && (l_addr_for_supra != null)) {
			key_owner owr_2 = key_owner.get_copy(owr);
			l_for_supra = ctx.make_peer();
			l_for_supra.init_local_peer(l_addr_for_supra, owr_2, true);
			logger.info("SUPRA.Local_peer=" + l_for_supra);

			nx_supra_locator l_supra = new nx_supra_locator(b_dir, l_for_supra);
			t_supra = l_supra.start_supra_locator_server();
			w_thd = t_supra;
		}

		String thd_nm = l_addr_for_transfs + "-files";
		if (is_accepting || is_connecting) {
			fl_thd = nx_context.start_thread(thd_nm, fl_op, false);
		}

		if (is_accepting) {
			srv_net_op.set_first_trans(srv_trans);
			String thd_nm2 = l_addr_for_transfs + "-srv_net-";
			if (is_once) {
				thd_nm2 += "once";
				srv_thd = nx_context.start_thread(thd_nm2,
						srv_net_op.get_run_once_accept_server(), false);
			} else {
				if (l_addr_for_repo != null) {
					nx_peer l_for_repo = ctx.make_peer();
					l_for_repo.init_local_peer(l_addr_for_repo, owr, false);
					logger.info("Local_peer_for_reporting=" + l_for_repo);
					List<Thread> all_thds = nx_connector.report_coref(b_dir,
							l_for_repo);
					thread_funcs.wait_for_threads(all_thds);
				}

				thd_nm2 += "loop";
				srv_thd = nx_context.start_thread(thd_nm2,
						srv_net_op.get_run_loop_server(), false);
			}
			w_thd = srv_thd;
			w_thd_nm = thd_nm2;
		}

		if (is_connecting) {
			if (cli_trans.r_peer_descr == null) {
				logger.info("remote_peer_descr_is_NULL !!!");
			} else {
				cli_net_op.set_first_trans(cli_trans);
				String thd_nm2 = l_addr_for_transfs + "-cli_net-";
				if (is_loop) {
					thd_nm2 += "loop";
					cli_thd = nx_context.start_thread(thd_nm2,
							cli_net_op.get_run_loop_client(), false);
				} else {
					thd_nm2 += "once";
					cli_thd = nx_context.start_thread(thd_nm2,
							cli_net_op.get_run_pending_client(), false);
				}
				w_thd = cli_thd;
				w_thd_nm = thd_nm2;
			}
		}

		if (w_thd != null) {
			write_pid_in(local_pcc);
			thread_funcs.wait_for_thread(w_thd);
			logger.info(w_thd_nm + "ENDED_MAIN_WAITNG_THREAD");
		}

		if (is_accepting || is_connecting) {
			fl_op.tell_finish_cli();
		}

		if (l_for_loca != null) {
			l_for_loca.kill_responder();
		}

		if (l_for_supra != null) {
			l_for_supra.kill_accept();
		}

		logger.info("Waiting_for_other_threads");

		thread_funcs.wait_for_thread(t_supra);
		logger.info("ENDED t_supra");
		thread_funcs.wait_for_thread(t_loca);
		logger.info("ENDED t_loca");
		thread_funcs.wait_for_thread(srv_thd);
		logger.info("ENDED srv_thd");
		thread_funcs.wait_for_thread(cli_thd);
		logger.info("ENDED cli_thd");
		thread_funcs.wait_for_thread(fl_thd);
		logger.info("ENDED fl_thd");

		// ctx.finish_context();
		ctx.stop_context();

		logger.info("ENDING thread '" + Thread.currentThread().getName() + "'");
		// thread_funcs.prt_active_thds("ACTIVE=");
	}

	public static void main(String[] args) {
		main_transfer(args);
		// main_TEST_get_host(args);
	}

	public static void main_TEST_get_host(String[] args) {
		System.out.println("HostName:" + net_funcs.get_hostname());
		System.out.println(Arrays.toString(net_funcs.get_all_ip()));
	}

	private static void main_transfer(String[] args) {
		command_line_transfer tt = new command_line_transfer();
		boolean go_on = tt.get_args(args);
		if (!go_on) {
			return;
		}

		tt.start_transfer_service();
	}

	void write_pid_in(paccount pcc) {
		String pid = "" + thread_funcs.get_pid();
		File dir = pcc.get_mikid_dir();
		File ff = new File(dir, PID_F_NM);
		List<String> all_pids = file_funcs.read_list_file(ff, null);
		all_pids.add(pid);
		file_funcs.write_list_file(ff, null, all_pids);
	}

	void kill_pid_in(File dir) {
		if (!dir.isDirectory()) {
			return;
		}
		logger.info(dir.getPath());
		File pid_ff = new File(dir, PID_F_NM);
		if (!pid_ff.exists()) {
			return;
		}

		List<String> all_pids = file_funcs.read_list_file(pid_ff, null);

		for (String pid_str : all_pids) {
			String command = "kill " + pid_str;
			logger.info(command);
			try {
				Runtime.getRuntime().exec(command);
			} catch (IOException ee) {
				ee.printStackTrace();
			}
		}

		pid_ff.delete();
	}

	void kill_all_servers() {
		if (root_dir == null) {
			throw new bad_netpasser(2);
		}
		File[] all_ff = root_dir.listFiles();
		for (File ff : all_ff) {
			kill_pid_in(ff);
		}
	}

	void move_default_chosen() {
	}

	void add_supralocator(nx_peer l_for_repo, nx_dir_base b_dir, key_owner owr,
			String addr_supra) {

		logger.info("Adding_supra_locator '" + addr_supra + "'");

		nx_conn_id old_coid = b_dir.get_coid_by_ref(addr_supra, null);

		if (old_coid == null) {
			try {
				nx_supra_locator loc_cli = new nx_supra_locator(b_dir,
						l_for_repo);
				loc_cli.set_supra_locator(addr_supra);
				old_coid = loc_cli.supra_locate(null);
				b_dir.write_coref(addr_supra, old_coid);

				String nxt_lctor = loc_cli.next_locator;
				if (nxt_lctor != null) {
					List<String> all_loctors = nx_connector
							.read_local_locators(b_dir, owr);
					if (all_loctors.isEmpty()) {
						all_loctors.add(nxt_lctor);
						nx_connector.write_local_locators(b_dir, owr,
								all_loctors);
					}
				}
			} catch (bad_emetcode ex1) {
				logger.error(ex1, "CANNOT_CONNECT to supra_locator="
						+ addr_supra);
			}
		}
		if (old_coid == null) {
			throw new bad_netpasser(2);
		}

		nx_std_coref r_gli = b_dir.get_remote_glid(old_coid, owr);
		if (r_gli != null) {
			nx_connector.add_local_supra_locator(b_dir, owr, addr_supra);
			logger.info("ADDED_SUPRA_LOCATOR=" + addr_supra + " in " + b_dir);

			logger.info("Creating_aliases for=" + r_gli);

			b_dir.write_coref_alias(r_gli.get_str(), addr_supra);

			String locat_descr = nx_locator.set_port(addr_supra);
			b_dir.write_coref_alias(addr_supra, locat_descr);
			String supra_locat_descr = nx_supra_locator.set_port(addr_supra);
			b_dir.write_coref_alias(addr_supra, supra_locat_descr);
		}

		logger.info("Finished adding_supra_locator '" + addr_supra + "'");
	}

}
