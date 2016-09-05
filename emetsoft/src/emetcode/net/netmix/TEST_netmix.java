package emetcode.net.netmix;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import emetcode.crypto.bitshake.TEST_bitshake;
import emetcode.crypto.bitshake.utils.config;
import emetcode.crypto.bitshake.utils.convert;
import emetcode.crypto.bitshake.utils.file_funcs;
import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.crypto.bitshake.utils.mem_file;
import emetcode.crypto.bitshake.utils.mer_twist;
import emetcode.crypto.bitshake.utils.global_id;
import emetcode.crypto.gamal.gamal_generator;
import emetcode.net.netmix.locator_sys.nx_coid_failures;
import emetcode.net.netmix.locator_sys.nx_location_stats;
import emetcode.net.netmix.locator_sys.nx_connector;
import emetcode.net.netmix.locator_sys.nx_location_request;
import emetcode.net.netmix.locator_sys.nx_locator;
import emetcode.net.netmix.locator_sys.nx_supra_locator;
import emetcode.net.netmix.locator_sys.nx_top_locations;
import emetcode.net.netmix.mudp_adapter.nx_mudp_context;
import emetcode.net.netmix.tcp_adapter.nx_tcp_context;
import emetcode.util.devel.bad_emetcode;
import emetcode.util.devel.dbg_slow;
import emetcode.util.devel.logger;
import emetcode.util.devel.net_funcs;
import emetcode.util.devel.thread_funcs;

public class TEST_netmix {
	public static final boolean IN_DEBUG_1 = true;
	public static final boolean IN_DEBUG_2 = false;

	public static final File DOT_DIR = new File(".");
	public static final char EOL = '\n';
	public static final char TAB = '\t';
	public static final String DEF_NM = "server1";

	String root_dir;
	String my_name;
	String srv_name;
	boolean as_client;
	boolean use_i2p;

	nx_context ctx;
	nx_dir_base top_base;
	nx_peer l_peer;
	nx_peer r_peer;

	public TEST_netmix() {
		root_dir = ".";
		my_name = DEF_NM;
		srv_name = DEF_NM;
		as_client = false;
		use_i2p = false;

		ctx = null;
		top_base = null;
		l_peer = null;
		r_peer = null;
	}

	public static void main(String[] args) {
		// TEST_d_net_main(args);
		// test_scanner(args);
		// TEST_mudp(args);
		// TEST_mudp_2(args);
		// TEST_tcp(args);
		// TEST_long_paths(args);
		// TEST_send_recv_mudp();
		// TEST_inc_fail();
		// TEST_top_locations(args);
		// TEST_locator(args);
		TEST_supra_locator(args);
		// TEST_multi_mudp(args);
		// TEST_ref_conver(args);
		// TEST_per_alive(args);
	}

	public void start_service() {

		File rr_dd = new File(root_dir);
		if (use_i2p) {
			//ctx = new nx_i2p_context(rr_dd);
			throw new bad_netmix(2);
		} else {
			ctx = new nx_tcp_context(rr_dd);
			if (as_client) {
				my_name = "localhost:7778";
				srv_name = "localhost:7779";
			} else {
				my_name = "localhost:7779";
			}
		}

		top_base = new nx_dir_base(rr_dd, ctx.get_net_type());

		byte[] kk = null;
		if (as_client) {
			kk = "client_key".getBytes(config.UTF_8);
		} else {
			kk = "server_key".getBytes(config.UTF_8);
		}
		key_owner owr = new key_owner(kk);

		l_peer = ctx.make_peer();
		l_peer.init_local_peer(my_name, owr, true);

		if (as_client) {
			r_peer = ctx.make_peer();
			r_peer.init_remote_peer(srv_name, null);
		}

		nx_context.start_thread("THD_" + my_name, get_d_net_srv(), false);
	}

	Runnable get_d_net_srv() {
		logger.debug("get_d_net_srv");
		Runnable rr1 = new Runnable() {
			public void run() {
				logger.info("TEST_d_net_STARTED");
				if (ctx == null) {
					logger.info("Internal error. ctx is null in TEST_netmix::run.");
					throw new bad_netmix(2);
				}
				if (top_base == null) {
					logger.info("Internal error. ctx is null in TEST_netmix::run.");
					throw new bad_netmix(2);
				}
				if (l_peer == null) {
					logger.info("Internal error. l_peer is null in TEST_netmix::run.");
					throw new bad_netmix(2);
				}
				if (as_client) {
					be_client();
				} else {
					be_server();
				}
			}
		};
		return rr1;
	}

	// public void run() {
	// logger.info("TEST_d_net_STARTED");
	// if (ctx == null) {
	// logger.info("Internal error. ctx is null in TEST_netmix::run.");
	// throw new bad_netmix(2);
	// }
	// if (l_peer == null) {
	// logger.info("Internal error. l_peer is null in TEST_netmix::run.");
	// throw new bad_netmix(2);
	// }
	// if (as_client) {
	// be_client();
	// } else {
	// be_server();
	// }
	// }

	static String help_msg = "srv_TEST [-h] [-r <root_dir>] [-nm <file_nam>] [-cli] [-srv <srv_file_nam>]\n"
			+ "\n"
			+ "-h : show invocation info.\n"
			+ "-nm : use '<root_dir>/peers/<file_nam> as my peer id.\n"
			+ "-cli : be the client.\n"
			+ "\n"
			+ "-r <root_dir> : take <root_dir> as the root directory. Default is '.'\n"
			+ "-srv : use '<root_dir>/peers/<srv_file_nam> as the server peer id.\n";

	public boolean get_args(String[] args) {

		root_dir = ".";
		boolean prt_help = false;

		my_name = DEF_NM;
		srv_name = DEF_NM;
		as_client = false;

		int num_args = args.length;
		for (int ii = 0; ii < num_args; ii++) {
			String the_arg = args[ii];
			if (the_arg.equals("-h")) {
				prt_help = true;
			} else if ((the_arg.equals("-nm")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				my_name = args[kk_idx];
			} else if ((the_arg.equals("-r")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				root_dir = args[kk_idx];
			} else if ((the_arg.equals("-srv")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				srv_name = args[kk_idx];
			} else if (the_arg.equals("-cli")) {
				as_client = true;
			} else {
				prt_help = true;
			}
		}

		if (as_client && (my_name == DEF_NM)) {
			my_name = "client1";
		}

		if (prt_help) {
			System.out.println(help_msg);
			return false;
		}

		System.out.println("Starting TEST with name '" + my_name + "'.");
		System.out.flush();

		return true;
	}

	void be_server() {
		while (true) {
			nx_connection conn = l_peer.accept();
			if (conn == null) {
				continue;
			}

			logger.info("Server got connection.");

			nx_messenger msgr = new nx_messenger(top_base, conn,
					nx_protector.NULL_VERIFIER);
			String msg1 = msgr.recv_string();
			logger.info("RECEIVED '" + msg1 + "'");

			msgr.recv_set_secure(true);

			if (msgr.has_secure_conn()) {
				logger.info("CONNECTION SECURED.");
			}

			String msg2 = msgr.recv_string();
			logger.info("RECEIVED '" + msg2 + "'");

			conn.close_net_connection();
		}

		// logger.info("Finished server.");
	}

	void be_client() {
		if (r_peer == null) {
			logger.info("Internal error. r_peer is null in TEST_netmix::be_client.");
			throw new bad_netmix(2);
		}

		logger.info("Connecting client...");

		nx_connection conn = l_peer.connect_to(r_peer);
		if (conn == null) {
			return;
		}

		logger.info("Client got connection.");

		nx_messenger msgr = new nx_messenger(top_base, conn,
				nx_protector.NULL_VERIFIER);
		String msg1 = "HOLA_MUNDO";
		logger.info("SENDING '" + msg1 + "'");
		msgr.send_string(msg1);

		msgr.send_set_secure(null, null, true);

		if (msgr.has_secure_conn()) {
			logger.info("CONNECTION SECURED.");
		}

		String msg2 = "HOLA_JOSE";
		logger.info("SENDING '" + msg2 + "'");
		msgr.send_string(msg2);

		conn.close_net_connection();

		logger.info("Finished client.");
	}

	public static void inifite_wait_loop() {
		try {
			while (true) {
				Thread.sleep(5000);
			}
		} catch (InterruptedException ex1) {
			logger.error(ex1, null);
		}
	}

	public static void TEST_d_net_main(String[] args) {
		TEST_netmix srv = new TEST_netmix();

		if (!srv.get_args(args)) {
			return;
		}

		srv.start_service();

		if (!srv.as_client) {
			inifite_wait_loop();
		}
	}

	public static void test_scanner(String[] args) {
		if (args.length < 1) {
			System.out.println("need one args.");
			return;
		}
		Scanner s1 = new Scanner(args[0]);
		s1.useDelimiter("\\.");
		while (s1.hasNext()) {
			System.out.println("next=" + s1.next());
		}
	}

	public static void TEST_mudp(String[] args) {
		boolean is_serv = false;
		if (args.length < 1) {
			System.out.println("args: (-c|-s)");
			return;
		}
		if (args[0].equals("-s")) {
			is_serv = true;
		}

		// mer_twist gg = new mer_twist(System.currentTimeMillis());

		int udp_port = 5555;
		if (is_serv) {
			udp_port++;
		}

		key_owner owr = new key_owner("CLAVE_DE_JOSE".getBytes(config.UTF_8));

		InetSocketAddress addr = new InetSocketAddress("localhost", udp_port);

		String host = addr.getHostName();
		int port = addr.getPort();
		System.out.println("host=" + host + " port=" + port);

		File rr = new File("/home/jose/tmp/mudp");
		nx_context ctx = new nx_mudp_context(rr, addr);
		nx_dir_base b_dir = new nx_dir_base(rr, net_funcs.MUDP_NET);

		nx_mudp_context uctx = (nx_mudp_context) ctx;
		InetSocketAddress addr2 = uctx.udp_mgr.get_socket_address();
		String host2 = addr2.getHostName();
		int port2 = addr2.getPort();
		System.out.println("host2=" + host2 + " port2=" + port2);

		nx_peer pp1 = ctx.make_peer();

		if (is_serv) {
			System.out.println("Startitng server in port " + udp_port);
			pp1.init_local_peer("::" + 1000, owr, true);
			// mudp_connection srv = mgr.make_server_connection(1000);
			System.out.println("Got server " + pp1);
			nx_connection cnn = pp1.accept();
			if (cnn != null) {
				nx_messenger mgr = new nx_messenger(b_dir, cnn,
						nx_protector.NULL_VERIFIER);
				System.out.println("Server got connection " + cnn);
				// byte[] arr = get_message(gg);
				byte[] arr = "HOLA_MUNDO".getBytes(config.UTF_8);
				String ss = new String(arr, config.UTF_8);
				System.out.println(ss);
				System.out.println("-------------------------------");
				mgr.send_bytes(arr);
			} else {
				System.out.println("Got null connection !!!!");
			}
		} else {
			System.out.println("Startitng client in port " + udp_port);
			long srv_port = (udp_port + 1);
			nx_peer pp2 = ctx.make_peer();
			pp2.init_remote_peer("localhost:" + srv_port + ":" + 1000, null);
			nx_connection cnn = pp1.connect_to(pp2);
			// net_connection cnn = pp1.connect_to("localhost:" + srv_port + ":"
			// + 1000);
			if (cnn != null) {
				nx_messenger mgr = new nx_messenger(b_dir, cnn,
						nx_protector.NULL_VERIFIER);
				System.out.println("Client got connection " + cnn);
				System.out.println("to service " + cnn);
				byte[] arr = mgr.recv_bytes();
				// byte[] arr = cnn.receive();
				String ss = new String(arr, config.UTF_8);
				System.out.println(ss);
				System.out.println("===============================");
			}
		}

		// mgr.stop_service();
	}

	static File BASE_DIR = new File(".");

	static void send_null_dir(nx_messenger mgr) {
		File[] arr = new File[0];
		mgr.send_mem_files(arr);
	}

	static void send_dir(nx_messenger mgr, String dd) {
		if (dd == null) {
			send_null_dir(mgr);
			return;
		}
		File dir = new File(BASE_DIR, dd);
		if (!dir.exists()) {
			send_null_dir(mgr);
			return;
		}
		File[] all_ff = dir.listFiles();
		mgr.send_mem_files(all_ff);
	}

	static File[] recv_dir(nx_messenger mgr, String dd) {
		if (dd == null) {
			throw new bad_netmix(2);
		}
		File dir = new File(BASE_DIR, dd);
		return mgr.recv_mem_files(dir);
	}

	public static void TEST_tcp(String[] args) {
		boolean is_serv = false;
		if (args.length < 1) {
			System.out.println("args: (-c|-s)");
			return;
		}
		if (args[0].equals("-s")) {
			is_serv = true;
		}
		String dir = null;
		if (args.length > 1) {
			dir = args[1];
		}

		int sok_port = 5555;
		String srv_descr = "localhost:" + sok_port;

		if (!is_serv) {
			sok_port++;
		}

		key_owner owr = new key_owner("CLAVE_DE_JOSE".getBytes(config.UTF_8));

		File rr = new File("/home/jose/tmp/test_nx_tcp");
		nx_context ctx = new nx_tcp_context(rr);
		nx_dir_base b_dir = new nx_dir_base(rr, net_funcs.TCP_NET);

		nx_peer pp1 = ctx.make_peer();

		if (is_serv) {
			System.out.println("Startitng server in port " + sok_port);
			pp1.init_local_peer(srv_descr, owr, true);
			System.out.println("Got server " + pp1);
			nx_connection cnn = pp1.accept();
			if (cnn != null) {
				nx_messenger mgr = new nx_messenger(b_dir, cnn,
						nx_protector.NULL_VERIFIER);
				System.out.println("Server got connection " + cnn);

				long s_tm = System.currentTimeMillis();
				send_dir(mgr, dir);
				recv_dir(mgr, "test_netmix_server_dir");

				cnn.close_net_connection();
				long e_tm = System.currentTimeMillis();
				System.out.println("TOT_MILLIS=" + (e_tm - s_tm));
			} else {
				System.out.println("Got null connection !!!!");
			}
		} else {
			System.out.println("Startitng client in port " + sok_port);
			nx_peer pp2 = ctx.make_peer();
			pp2.init_remote_peer(srv_descr, null);
			nx_connection cnn = pp1.connect_to(pp2);
			if (cnn != null) {
				nx_messenger mgr = new nx_messenger(b_dir, cnn,
						nx_protector.NULL_VERIFIER);
				System.out.println("Client got connection " + cnn);
				System.out.println("to service " + cnn);

				long s_tm = System.currentTimeMillis();
				recv_dir(mgr, "test_netmix_client_dir");
				send_dir(mgr, dir);

				cnn.close_net_connection();
				long e_tm = System.currentTimeMillis();
				System.out.println("TOT_MILLIS=" + (e_tm - s_tm));
			}
		}

		ctx.finish_context();
	}

	// TEST_LOCATOR
	// TEST_LOCATOR
	// TEST_LOCATOR
	// TEST_LOCATOR

	static nx_conn_id create_test_coref(nx_dir_base b_dir, nx_context the_ctx,
			key_owner owr, String coref_str) {
		// nx_dir_base b_dir = the_ctx.get_dir_base();
		byte[] aux_key = "ANY_KEY_FOR_AUX_COID".getBytes();
		nx_conn_id the_coid = new nx_conn_id();
		b_dir.write_coid_file(the_coid, aux_key, owr);
		b_dir.write_coref(coref_str, the_coid);
		return the_coid;
	}

	static void init_ctx(nx_dir_base b_dir, nx_context the_ctx, key_owner owr,
			nx_conn_id coid, byte[] co_key, String the_addr,
			nx_std_coref local_gli, nx_std_coref remote_gli) {
		// nx_dir_base b_dir = the_ctx.get_dir_base();

		if (co_key != null) {
			b_dir.write_coid_file(coid, co_key, owr);
			b_dir.write_coref(the_addr, coid);

			File rem_nx_dd = b_dir.get_remote_nx_dir(coid);
			nx_std_coref.set_glid(rem_nx_dd, owr, remote_gli);
		}

		b_dir.set_local_glid(owr, local_gli);

		// File gli0 = nx_protector.get_glid_file(
		// the_ctx.get_all_coids_base_dir(), coid);
		// mem_file.write_encrypted_string(gli0, null, the_gli.get_str());
	}

	public static void TEST_locator(String[] args) {
		boolean is_serv = false;
		boolean with_coid = false;

		if (args.length < 1) {
			System.out.println("args: (-c|-s|-i)");
			return;
		}

		if (args[0].equals("-s")) {
			is_serv = true;
		}
		String cli_dir = "client";
		String srv_dir = "server";

		// key_owner kk1 = new key_owner("client_glid".getBytes());
		// key_owner kk2 = new key_owner("server_glid".getBytes());
		String coref_str_1 = "test_1_glid";
		String coref_str_2 = "test_2_glid";

		nx_std_coref cli_gli = new nx_std_coref("client_glid");
		nx_std_coref srv_gli = new nx_std_coref("server_glid");

		System.out.println("cli_gli=" + cli_gli.get_str());
		System.out.println("srv_gli=" + srv_gli.get_str());

		String w_dir = cli_dir;

		int mudp_locator_port = 1000;

		int cli_port = 5555;
		int srv_port = 5556;
		int udp_port = cli_port;
		String srv_locator_addr = "localhost:" + srv_port + ":"
				+ mudp_locator_port;
		String cli_locator_addr = "localhost:" + cli_port + ":"
				+ mudp_locator_port;

		if (is_serv) {
			udp_port = srv_port;
			w_dir = srv_dir;
		}

		key_owner owr = new key_owner("CLAVE_DE_JOSE".getBytes(config.UTF_8));

		InetSocketAddress addr = new InetSocketAddress("localhost", udp_port);

		String host = addr.getHostName();
		int port = addr.getPort();
		System.out.println("host=" + host + " port=" + port);

		File rr_base = new File("/home/jose/tmp/test_nx_locator");

		if (args[0].equals("-i")) {
			File rr0 = new File(rr_base, srv_dir);
			File rr1 = new File(rr_base, cli_dir);

			byte[] co_key = null;
			nx_conn_id coid = null;
			if (with_coid) {
				co_key = "LA_CLAVE_DEL_COID".getBytes(config.UTF_8);
				coid = new nx_conn_id();
			}

			nx_dir_base s_dir = new nx_dir_base(rr0, net_funcs.MUDP_NET);
			nx_dir_base c_dir = new nx_dir_base(rr1, net_funcs.MUDP_NET);

			nx_context s_ictx = new nx_context(rr0, net_funcs.MUDP_NET);
			init_ctx(s_dir, s_ictx, owr, coid, co_key, cli_locator_addr,
					srv_gli, cli_gli);

			nx_context c_ictx = new nx_context(rr1, net_funcs.MUDP_NET);
			init_ctx(c_dir, c_ictx, owr, coid, co_key, srv_locator_addr,
					cli_gli, srv_gli);

			create_test_coref(s_dir, s_ictx, owr, coref_str_1);
			create_test_coref(s_dir, s_ictx, owr, coref_str_2);

			// nx_dir_base b_dir = s_ictx.get_dir_base();
			nx_locator srv_db = new nx_locator(s_dir, owr, null);
			srv_db.set_reported_addr(coref_str_1, "reported_addr_gli_1");
			srv_db.set_observed_addr(coref_str_1, "observed_addr_gli_1");
			srv_db.set_reported_addr(coref_str_2, "reported_addr_gli_2");
			srv_db.set_observed_addr(coref_str_2, "observed_addr_gli_2");

			return;
		}

		File rr = new File(rr_base, w_dir);
		nx_context ctx = new nx_mudp_context(rr, addr);
		nx_dir_base b_dir = new nx_dir_base(rr, net_funcs.MUDP_NET);
		// if (args.length < 2) {
		// System.out.println("args: ((-c|-s) <old_coid> | -i)");
		// return;
		// }
		// nx_conn_id old_coid = new nx_conn_id(args[1]);

		nx_mudp_context uctx = (nx_mudp_context) ctx;
		InetSocketAddress addr2 = uctx.udp_mgr.get_socket_address();
		String host2 = addr2.getHostName();
		int port2 = addr2.getPort();
		System.out.println("host2=" + host2 + " port2=" + port2);

		nx_peer pp1 = ctx.make_peer();

		// nx_dir_base b_dir = ctx.get_dir_base();
		if (is_serv) {
			System.out.println("Starting server in port " + udp_port);
			pp1.init_local_peer("::" + mudp_locator_port, owr, false);
			nx_locator loc_srv = new nx_locator(b_dir, owr, pp1);
			System.out.println("Got server " + pp1);
			loc_srv.start_locator_server(null);

		} else {
			System.out.println("Startitng client in port " + udp_port);
			pp1.init_local_peer("::" + mudp_locator_port, owr, false);
			nx_locator loc_cli = new nx_locator(b_dir, owr, pp1);

			nx_peer pp2 = ctx.make_peer();
			pp2.init_remote_peer(srv_locator_addr, null);

			// SET THE REQUESTED GLID
			// String rq_coref = srv_gli.get_str();
			String rq_coref = coref_str_1;
			// String rq_coref = coref_str_2;

			loc_cli.set_locator(pp2, null);
			if (loc_cli.has_locator()) {
				System.out.println("locating glid='" + rq_coref + "'");
				nx_location_request loc = loc_cli.ask_locate(rq_coref, true);
				System.out.println("\n\n\nlocated glid='" + rq_coref + "'"
						+ "\nLOCATION=\n<<\n" + loc + ">>\n\n");
			} else {
				System.out.println("COULD_NOT_SET_locator !!!");
			}
		}

		logger.debug("AVG_SENT_BY_SEC="
				+ uctx.udp_mgr.num_sec_send_avg.get_avg()
				+ "\nAVG_SENT_BY_SEC_SZ="
				+ uctx.udp_mgr.num_sec_send_avg.get_sz()
				+ "\nAVG_SENT_BY_SEC_SLOT_SZ="
				+ uctx.udp_mgr.num_sec_send_avg.get_slot_sz());

		ctx.finish_context();
	}

	// TEST_SUPRA_LOCATOR
	// TEST_SUPRA_LOCATOR
	// TEST_SUPRA_LOCATOR
	// TEST_SUPRA_LOCATOR

	static void init_list(String nm, List<String> the_lst) {
		the_lst.clear();
		for (int aa = 0; aa < 10; aa++) {
			String item = nm + "-" + aa;
			the_lst.add(item);
		}
	}

	static void write_supra_files(nx_dir_base dir_b, key_owner owr,
			nx_conn_id the_coid, String coref_str) {

		List<String> all_ltor = new ArrayList<String>();
		init_list(coref_str + "-loc", all_ltor);

		List<String> all_su_ltors = new ArrayList<String>();
		init_list(coref_str + "-suloc", all_su_ltors);

		File ltor_ff = nx_connector.get_remote_locators_file(dir_b, the_coid);
		file_funcs.write_list_file(ltor_ff, owr, all_ltor);

		File su_ltors_ff = nx_connector.get_remote_supra_locators_file(dir_b,
				the_coid);
		file_funcs.write_list_file(su_ltors_ff, owr, all_su_ltors);

		List<String> ltors = nx_connector.read_remote_locators(dir_b, the_coid,
				owr);
		List<String> su_ltors = nx_connector.read_remote_supra_locators(dir_b,
				the_coid, owr);

		logger.info(coref_str + " LOCATORS=");
		logger.info(ltors);
		logger.info(coref_str + " SUPRA_LOCATORS=");
		logger.info(su_ltors);
	}

	static void init_supra_ctx(nx_dir_base b_dir, nx_context the_ctx,
			key_owner owr, nx_conn_id coid, byte[] co_key, String the_addr,
			nx_std_coref local_gli, nx_std_coref remote_gli) {
		// nx_dir_base b_dir = the_ctx.get_dir_base();

		if (co_key != null) {
			b_dir.write_coid_file(coid, co_key, owr);
			b_dir.write_coref(the_addr, coid);

			File rem_nx_dd = b_dir.get_remote_nx_dir(coid);
			nx_std_coref.set_glid(rem_nx_dd, owr, remote_gli);
		}

		b_dir.set_local_glid(owr, local_gli);

		// File gli0 = nx_protector.get_glid_file(
		// the_ctx.get_all_coids_base_dir(), coid);
		// mem_file.write_encrypted_string(gli0, null, the_gli.get_str());
	}

	public static void TEST_supra_locator(String[] args) {
		boolean is_serv = false;
		boolean with_coid = false;

		logger.info("TEST_supra_locator");

		if (args.length < 1) {
			System.out.println("args: (-c|-s|-i)");
			return;
		}

		if (args[0].equals("-s")) {
			is_serv = true;
		}
		String cli_dir = "client";
		String srv_dir = "server";

		// key_owner kk1 = new key_owner("client_glid".getBytes());
		// key_owner kk2 = new key_owner("server_glid".getBytes());
		String coref_str_1 = "test_1_glid";
		String coref_str_2 = "test_2_glid";

		key_owner owr = new key_owner("CLAVE_DE_JOSE".getBytes(config.UTF_8));

		global_id c_gid = new global_id(coref_str_1.getBytes(), owr);
		global_id s_gid = new global_id(coref_str_2.getBytes(), owr);

		nx_std_coref cli_gli = new nx_std_coref(c_gid);
		nx_std_coref srv_gli = new nx_std_coref(s_gid);

		System.out.println("cli_gli=" + cli_gli.get_str());
		System.out.println("srv_gli=" + srv_gli.get_str());

		String w_dir = cli_dir;

		long mudp_locator_port = nx_supra_locator.SUPRA_LOCATOR_PORT;

		int cli_port = 5555;
		int srv_port = 5556;
		int udp_port = cli_port;
		String srv_locator_addr = "localhost:" + srv_port + ":"
				+ mudp_locator_port;
		String cli_locator_addr = "localhost:" + cli_port + ":"
				+ mudp_locator_port;

		if (is_serv) {
			udp_port = srv_port;
			w_dir = srv_dir;
		}

		InetSocketAddress addr = new InetSocketAddress("localhost", udp_port);

		String host = addr.getHostName();
		int port = addr.getPort();
		System.out.println("host=" + host + " port=" + port);

		File rr_base = new File("/home/jose/tmp/test_nx_supra_locator");

		File rr0 = new File(rr_base, srv_dir);
		File rr1 = new File(rr_base, cli_dir);

		nx_dir_base s_dir = new nx_dir_base(rr0, net_funcs.MUDP_NET);
		nx_dir_base c_dir = new nx_dir_base(rr1, net_funcs.MUDP_NET);
		
		if(! nx_supra_locator.has_next_locator(s_dir)){
			nx_supra_locator.write_next_locator(s_dir, owr, "MY_NEXT_LOCATOR");
		}

		nx_std_coref s_gli = s_dir.get_local_glid(owr);
		if (s_gli == null) {
			s_dir.set_local_glid(owr, srv_gli);
		}

		nx_std_coref c_gli = c_dir.get_local_glid(owr);
		if (c_gli == null) {
			c_dir.set_local_glid(owr, cli_gli);
		}

		if (!s_dir.has_gamal_sys(owr)) {
			gamal_generator gam = TEST_netmix.read_gamal_sys();
			s_dir.save_gamal_sys(gam, owr);
		}

		if (args[0].equals("-i")) {

			byte[] co_key = null;
			nx_conn_id coid = null;
			if (with_coid) {
				co_key = "LA_CLAVE_DEL_COID".getBytes(config.UTF_8);
				coid = new nx_conn_id();
			}

			nx_context s_ictx = new nx_context(rr0, net_funcs.MUDP_NET);
			init_supra_ctx(s_dir, s_ictx, owr, coid, co_key, cli_locator_addr,
					srv_gli, cli_gli);

			nx_context c_ictx = new nx_context(rr1, net_funcs.MUDP_NET);
			init_supra_ctx(c_dir, c_ictx, owr, coid, co_key, srv_locator_addr,
					cli_gli, srv_gli);

			nx_conn_id co1 = create_test_coref(s_dir, s_ictx, owr, coref_str_1);
			nx_conn_id co2 = create_test_coref(s_dir, s_ictx, owr, coref_str_2);

			// nx_dir_base sb_dir = s_ictx.get_dir_base();
			write_supra_files(s_dir, owr, co1, coref_str_1);
			write_supra_files(s_dir, owr, co2, coref_str_2);

			// nx_dir_base b_dir = s_ictx.get_dir_base();
			nx_locator srv_db = new nx_locator(s_dir, owr, null);
			srv_db.set_reported_addr(coref_str_1, "reported_addr_gli_1");
			srv_db.set_observed_addr(coref_str_1, "observed_addr_gli_1");
			srv_db.set_reported_addr(coref_str_2, "reported_addr_gli_2");
			srv_db.set_observed_addr(coref_str_2, "observed_addr_gli_2");

			return;
		}

		File rr = new File(rr_base, w_dir);
		nx_context ctx = new nx_mudp_context(rr, addr);
		nx_dir_base b_dir = new nx_dir_base(rr, net_funcs.MUDP_NET);

		nx_mudp_context uctx = (nx_mudp_context) ctx;
		InetSocketAddress addr2 = uctx.udp_mgr.get_socket_address();
		String host2 = addr2.getHostName();
		int port2 = addr2.getPort();
		System.out.println("host2=" + host2 + " port2=" + port2);

		nx_peer pp1 = ctx.make_peer();

		// nx_dir_base b_dir = ctx.get_dir_base();
		if (is_serv) {
			System.out.println("Starting server in port " + udp_port);
			pp1.init_local_peer("::" + mudp_locator_port, owr, true);
			nx_supra_locator loc_srv = new nx_supra_locator(b_dir, pp1);
			System.out.println("Got server " + pp1);
			Thread thd = loc_srv.start_supra_locator_server();
			if (thd == null) {
				logger.info("CANNOT_START_NULL_SERVER");
			}
			try {
				thd.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		} else {
			// SET THE REQUESTED GLID
			// String rq_coref = srv_gli.get_str();
			// String rq_coref = coref_str_1;
			// String rq_coref = coref_str_2;
			String rq_coref = null;
			
			System.out.println("Startitng client in port " + udp_port);
			pp1.init_local_peer("::" + mudp_locator_port, owr, false);
			nx_supra_locator loc_cli = new nx_supra_locator(b_dir, pp1);
			loc_cli.set_supra_locator(srv_locator_addr);
			if (loc_cli.has_supra_locator()) {
				System.out.println("supra_locating glid='" + rq_coref + "'");
				loc_cli.supra_locate(rq_coref);

				logger.info(rq_coref + " LOCATORS=");
				logger.info(loc_cli.all_locators);
				logger.info(rq_coref + " SUPRA_LOCATORS=");
				logger.info(loc_cli.all_supra_locators);
				logger.info(rq_coref + " NEXT_LOCATOR=" + loc_cli.next_locator);
			} else {
				System.out.println("COULD_NOT_SET_locator !!!");
			}
		}

		logger.debug("AVG_SENT_BY_SEC="
				+ uctx.udp_mgr.num_sec_send_avg.get_avg()
				+ "\nAVG_SENT_BY_SEC_SZ="
				+ uctx.udp_mgr.num_sec_send_avg.get_sz()
				+ "\nAVG_SENT_BY_SEC_SLOT_SZ="
				+ uctx.udp_mgr.num_sec_send_avg.get_slot_sz());

		ctx.stop_context();
	}

	public static void TEST_long_paths(String[] args) {
		File rr = new File("/home/jose/tmp/test_netmix");
		// nx_context ctx = new nx_tcp_context(rr);
		nx_conn_id cc = new nx_conn_id();
		global_id per_gli = new global_id((File) null, (key_owner) null);
		nx_std_coref gg = new nx_std_coref(per_gli);
		// nx_dir_base b_dir = ctx.get_dir_base();
		nx_dir_base b_dir = new nx_dir_base(rr, net_funcs.TCP_NET);
		//File all_coids_dd = b_dir.get_all_coids_base_dir();
		b_dir.write_coid_file(cc, "CLAVE".getBytes(config.UTF_8), null);
		b_dir.write_coref(gg.toString(), cc);
	}

	public static void TEST_mudp_2(String[] args) {
		boolean is_serv = false;
		if (args.length < 1) {
			System.out.println("args: (-c|-s)");
			return;
		}

		boolean renew_conn = false;
		String dir = null;
		// nx_conn_id old_coid = null;
		if (args[0].equals("-s")) {
			is_serv = true;
			if (args.length > 1) {
				dir = args[1];
			}
		} else {
			if (args.length > 1) {
				// old_coid = new nx_conn_id(convert.parse_long(args[1]));
			}
			if (args.length > 2) {
				renew_conn = true;
			}
		}

		// mer_twist gg = new mer_twist(System.currentTimeMillis());

		int udp_port = 5555;
		if (is_serv) {
			udp_port++;
		}

		key_owner owr = new key_owner("CLAVE_DE_JOSE".getBytes(config.UTF_8));

		InetSocketAddress addr = new InetSocketAddress("localhost", udp_port);

		String host = addr.getHostName();
		int port = addr.getPort();
		System.out.println("host=" + host + " port=" + port);

		File rr = new File("/home/jose/tmp/test_nx_mudp_2");
		nx_context ctx = new nx_mudp_context(rr, addr);
		nx_dir_base b_dir = new nx_dir_base(rr, net_funcs.MUDP_NET);

		nx_mudp_context uctx = (nx_mudp_context) ctx;
		InetSocketAddress addr2 = uctx.udp_mgr.get_socket_address();
		String host2 = addr2.getHostName();
		int port2 = addr2.getPort();
		System.out.println("host2=" + host2 + " port2=" + port2);

		nx_peer pp1 = ctx.make_peer();

		if (is_serv) {
			System.out.println("Starting server in port " + udp_port);
			pp1.init_local_peer("::" + 1000, owr, true);
			System.out.println("Got server " + pp1);
			nx_connection cnn = pp1.accept();
			if (cnn != null) {
				nx_messenger mgr = new nx_messenger(b_dir, cnn,
						nx_protector.NULL_VERIFIER);
				System.out.println("Server got connection " + cnn);

				mgr.recv_set_secure(true);

				long s_tm = System.currentTimeMillis();
				try {
					send_dir(mgr, dir);
					recv_dir(mgr, "test_netmix_server_dir");
				} catch (bad_emetcode ex1) {
					System.out.println(ex1);
				}

				cnn.close_net_connection();
				long e_tm = System.currentTimeMillis();
				System.out.println("TOT_MILLIS=" + (e_tm - s_tm));
			} else {
				System.out.println("Got null connection !!!!");
			}
			pp1.kill_accept();

		} else {
			System.out.println("Startitng client in port " + udp_port);
			pp1.init_local_peer("::" + 1000, owr, false);

			long srv_port = (udp_port + 1);
			nx_peer pp2 = ctx.make_peer();
			pp2.init_remote_peer("localhost:" + srv_port + ":" + 1000, null);
			nx_connection cnn = pp1.connect_to(pp2);
			if (cnn != null) {
				nx_messenger mgr = new nx_messenger(b_dir, cnn,
						nx_protector.NULL_VERIFIER);
				System.out.println("Client got connection " + cnn);
				System.out.println("to service " + cnn);

				if (renew_conn) {
					System.out.println("RENEW_CONN=" + cnn);
					mgr.set_to_renew_connection_key();
				}

				mgr.send_set_secure(null, null, true);

				long s_tm = System.currentTimeMillis();
				try {
					recv_dir(mgr, "test_netmix_client_dir");
					send_dir(mgr, dir);
				} catch (bad_emetcode ex1) {
					System.out.println(ex1);
				}

				cnn.close_net_connection();
				long e_tm = System.currentTimeMillis();
				System.out.println("TOT_MILLIS=" + (e_tm - s_tm));
			}
		}

		logger.debug("AVG_SENT_BY_SEC="
				+ uctx.udp_mgr.num_sec_send_avg.get_avg()
				+ "\nAVG_SENT_BY_SEC_SZ="
				+ uctx.udp_mgr.num_sec_send_avg.get_sz()
				+ "\nAVG_SENT_BY_SEC_SLOT_SZ="
				+ uctx.udp_mgr.num_sec_send_avg.get_slot_sz());

		ctx.finish_context();
	}

	// TEST_BASIC_MUDP
	// TEST_BASIC_MUDP
	// TEST_BASIC_MUDP
	// TEST_BASIC_MUDP

	private static mer_twist get_send_recv_gen() {
		byte[] srv_by = "UNA CADENA CULAQUIERA".getBytes(config.UTF_8);
		mer_twist gg = new mer_twist(srv_by);
		return gg;
	}

	private static File get_test_top_dir(String name) {
		File rr = new File("/home/jose/tmp/test_nx_mudp/" + name);
		return rr;
	}

	private static int get_test_top_type(boolean is_mudp) {
		if (is_mudp) {
			return net_funcs.MUDP_NET;
		}
		return net_funcs.TCP_NET;
	}

	private static nx_context start_new_test_ctx(String name, int port,
			boolean is_mudp) {
		File rr = get_test_top_dir(name);

		InetSocketAddress srv_addr = new InetSocketAddress("localhost", port);

		String srv_host = srv_addr.getHostName();
		int srv_port = srv_addr.getPort();
		System.out.println(name + " in host=" + srv_host + " port=" + srv_port);

		nx_context ctx = null;
		if (is_mudp) {
			ctx = new nx_mudp_context(rr, srv_addr);
		} else {
			ctx = new nx_tcp_context(rr);
		}

		return ctx;
	}

	private static void send_recv_server(final int loc_port,
			final boolean use_mudp) {
		String nm = "THE_SERVER";
		nx_context ctx = start_new_test_ctx(nm, loc_port, use_mudp);
		File top = get_test_top_dir(nm);
		int nn_ty = get_test_top_type(use_mudp);
		nx_dir_base b_dir = new nx_dir_base(top, nn_ty);

		key_owner owr = new key_owner(nm.getBytes(config.UTF_8));
		nx_peer pp1 = ctx.make_peer();
		pp1.init_local_peer("localhost:" + loc_port + ":" + 1000, owr, true);

		gamal_generator gam = TEST_netmix.read_gamal_sys();
		b_dir.save_gamal_sys(gam, owr);

		byte[] kk = nm.getBytes();
		nx_std_coref the_glid = null;
		if (!b_dir.has_local_glid()) {
			global_id per_gli = new global_id(kk, owr);
			the_glid = new nx_std_coref(per_gli);
			b_dir.set_local_glid(owr, the_glid);
		} else {
			the_glid = b_dir.get_local_glid(owr);
		}

		// logger.info("TYPE_RETURN_TO_START_SERVER ...");
		// System.console().readLine();

		mer_twist gg = get_send_recv_gen();
		// int num_repe = (int)(convert.to_interval(gg.nextInt(), 2, 100));
		long bb = 0;
		while (true) {
			bb++;
			send_recv_server_once(b_dir, gg, ctx, loc_port, pp1, bb);
		}
		// ctx.finish_context();
		// System.out.println("FINISHED_TEST_SERVER_THREAD");
	}

	private static void send_recv_client(final int loc_port,
			final int rem_port, final boolean use_mudp) {
		String nm = "THE_CLIENT";
		nx_context ctx = start_new_test_ctx(nm, loc_port, use_mudp);
		File top = get_test_top_dir(nm);
		int nn_ty = get_test_top_type(use_mudp);
		nx_dir_base b_dir = new nx_dir_base(top, nn_ty);

		key_owner owr = new key_owner(nm.getBytes(config.UTF_8));
		nx_peer pp1 = ctx.make_peer();
		pp1.init_local_peer("localhost:" + loc_port + ":" + 1000, owr, false);
		nx_peer pp2 = ctx.make_peer();
		pp2.init_remote_peer("localhost:" + rem_port + ":" + 1000, null);

		gamal_generator gam = TEST_netmix.read_gamal_sys();
		b_dir.save_gamal_sys(gam, owr);

		byte[] kk = nm.getBytes();
		nx_std_coref the_glid = null;
		if (!b_dir.has_local_glid()) {
			global_id per_gli = new global_id(kk, owr);
			the_glid = new nx_std_coref(per_gli);
			b_dir.set_local_glid(owr, the_glid);
		} else {
			the_glid = b_dir.get_local_glid(owr);
		}

		// logger.info("TYPE_RETURN_TO_START_CLIENT ...");
		// System.console().readLine();

		mer_twist gg = get_send_recv_gen();
		// int num_repe = (int)(convert.to_interval(gg.nextInt(), 2, 100));
		long bb = 0;
		while (true) {
			bb++;
			send_recv_client_once(b_dir, gg, ctx, pp1, pp2, bb);
			for (int aa = 0; aa < 1; aa++) {
				logger.info(""
						+ bb
						+ "-"
						+ aa
						+ ".CLIENT_CONNECTION_TEST_FINISHED_OK (send_recv_client)....");
			}
			// System.console().readLine();
			// logger.info("GOT RETURN (send_recv_client)....");
		}
		// ctx.finish_context();
		// System.out.println("FINISHED_TEST_CLIENT_THREAD");
	}

	private static void send_recv_server_once(nx_dir_base b_dir, mer_twist gg,
			final nx_context ctx, final int loc_port, nx_peer pp1,
			final long repe) {

		nx_connection cnn = pp1.accept();
		if (cnn != null) {
			nx_messenger mgr = new nx_messenger(b_dir, cnn,
					nx_protector.NULL_VERIFIER);
			if (IN_DEBUG_1) {
				System.out.println("Server '" + repe + "' got connection "
						+ cnn);
			}

			mgr.recv_set_secure(true);

			cnn.close_net_connection();
		}
	}

	private static void send_recv_client_once(nx_dir_base b_dir, mer_twist gg,
			final nx_context ctx, nx_peer pp1, nx_peer pp2, final long repe) {

		nx_connection cnn = pp1.connect_to(pp2);
		if (cnn != null) {
			nx_messenger mgr = new nx_messenger(b_dir, cnn,
					nx_protector.NULL_VERIFIER);
			if (IN_DEBUG_1) {
				System.out.println("Client '" + repe + "' got connection "
						+ cnn);
				System.out.println("to service " + cnn);
			}

			String rem_descrip = pp2.get_description();
			nx_conn_id old_coid = b_dir.get_coid_by_ref(rem_descrip, null);

			if (old_coid == null) {
				System.out.println("CREATING_NEW_COID_FOR=" + rem_descrip);
				System.out.println("TYPE_RETURN_TO_CONTINUE");
				System.console().readLine();
			}

			mgr.send_set_secure(null, null, true);

			if (old_coid == null) {
				b_dir.write_coref(rem_descrip, mgr.get_coid());
			}

			cnn.close_net_connection();
		}
	}

	private static Runnable get_srv_send_recv(final int loc_port,
			final boolean use_mudp) {
		logger.debug("get_srv_send_recv");
		Runnable rr1 = new Runnable() {
			public void run() {
				send_recv_server(loc_port, use_mudp);
			}
		};
		return rr1;
	}

	private static Runnable get_cli_send_recv(final int loc_port,
			final int rem_port, final boolean use_mudp) {
		logger.debug("get_cli_send_recv");
		Runnable rr1 = new Runnable() {
			public void run() {
				send_recv_client(loc_port, rem_port, use_mudp);
			}
		};
		return rr1;
	}

	private static boolean gen_rand_bool(mer_twist gg) {
		boolean bb = ((int) (convert.to_interval(gg.nextInt(), 0, 2)) > 0);
		return bb;
	}

	private static void send_test_msg(nx_messenger mgr, byte[] mm, long num_msg) {
		mgr.send_bytes(mm);
		System.out.print('S');
		// String dbg_str = num_msg + "-S-" + mm.length + "___"
		// + nx_connection.get_prt_dbg_str(num_msg, true, false, mm);
		// logger.debug(dbg_str);
	}

	private static void recv_test_msg(nx_messenger mgr, byte[] mm1, long num_msg) {
		byte[] mm2 = mgr.recv_bytes();
		if (mm1 == mm2) {
			throw new bad_netmix(2);
		}
		if (!Arrays.equals(mm1, mm2)) {
			throw new bad_netmix(2, "FAILED_RECV");
		}
		System.out.print('R');
		// String dbg_str = num_msg + "-R-" + mm2.length + "___"
		// + nx_connection.get_prt_dbg_str(num_msg, false, false, mm2);
		// logger.debug(dbg_str);
	}

	private static final int MIN_MSG = 2;
	private static final int MAX_MSG = 100;

	static void send_all_random(mer_twist gg, nx_messenger mgr, final int repe) {
		int num_msg = (int) (convert
				.to_interval(gg.nextInt(), MIN_MSG, MAX_MSG));
		System.out.println("\n" + repe + "---" + num_msg + "\n");
		for (int aa = 0; aa < num_msg; aa++) {
			byte[] mm = TEST_bitshake.get_rand_dat(gg);

			boolean is_send = gen_rand_bool(gg);

			int lft = num_msg - aa + MIN_MSG;
			int num2 = (int) (convert.to_interval(gg.nextInt(), MIN_MSG, lft));

			if (is_send) {
				for (int bb = 0; bb < num2; bb++) {
					send_test_msg(mgr, mm, aa);
				}
			} else {
				for (int bb = 0; bb < num2; bb++) {
					recv_test_msg(mgr, mm, aa);
				}
			}
		}
		System.out.println();
		System.out.println("FINISHED_send of " + repe + "---" + num_msg + "\n");
	}

	static void recv_all_random(mer_twist gg, nx_messenger mgr, final int repe) {
		int num_msg = (int) (convert
				.to_interval(gg.nextInt(), MIN_MSG, MAX_MSG));
		System.out.println("\n" + repe + "---" + num_msg + "\n");
		for (int aa = 0; aa < num_msg; aa++) {
			byte[] mm = TEST_bitshake.get_rand_dat(gg);

			boolean is_send = gen_rand_bool(gg);

			int lft = num_msg - aa + MIN_MSG;
			int num2 = (int) (convert.to_interval(gg.nextInt(), MIN_MSG, lft));

			if (is_send) {
				for (int bb = 0; bb < num2; bb++) {
					recv_test_msg(mgr, mm, aa);
				}
			} else {
				for (int bb = 0; bb < num2; bb++) {
					send_test_msg(mgr, mm, aa);
				}
			}
		}
		System.out.println();
		System.out.println("FINISHED_recv of " + repe + "---" + num_msg + "\n");
	}

	public static void TEST_send_recv_mudp() {
		int srv_udp_port = 5555;
		int cli_udp_port = 5556;

		boolean use_mudp = true;

		String srv_thd_nm = Thread.currentThread().getName() + "-srv";
		nx_context.start_thread(srv_thd_nm,
				get_srv_send_recv(srv_udp_port, use_mudp), false);

		String cli_thd_nm = Thread.currentThread().getName() + "-cli";
		nx_context.start_thread(cli_thd_nm,
				get_cli_send_recv(cli_udp_port, srv_udp_port, use_mudp), false);

		System.out.println("FINISHED_MAIN_THREAD");
	}

	public static void TEST_top_locations(String[] args) {
		// long h_milis = 1000 * 60 * 60;
		// long d_milis = h_milis * 24;
		// long m_milis = d_milis * 30;
		//
		// if (args.length < 1) {
		// System.out.println("args: (-conn <addr>) | -p | -failed <mth>");
		// return;
		// }
		// boolean inc_num_conn = false;
		// boolean inc_num_failed = false;
		//
		// String arg0 = args[0];
		// if (arg0.equals("-conn")) {
		// inc_num_conn = true;
		// }
		// if (arg0.equals("-failed")) {
		// inc_num_failed = true;
		// }
		//
		// File loc_ff = new File("./test_locations.dat");
		// File fail_ff = new File("./test_failures.dat");
		// // key_owner owr = new key_owner("UNA_CLAVE_CUALQUIERA".getBytes());
		// key_owner owr = null;
		//
		// if (inc_num_conn) {
		// if (args.length < 2) {
		// System.out.println("args: ((-try|-conn) <addr>) | -p");
		// return;
		// }
		// String the_addr = args[1];
		// if (inc_num_conn) {
		// nx_top_locations.inc_num_conn_for(loc_ff, owr, the_addr);
		// }
		// } else if (inc_num_failed) {
		// long add_millis = 0;
		// if (args.length > 1) {
		// int mth = Integer.parseInt(args[1]);
		// add_millis = mth * m_milis;
		// }
		// long curr_tm = System.currentTimeMillis();
		// nx_coid_failures.inc_num_failed_at(fail_ff, owr, curr_tm
		// + add_millis);
		// } else {
		// nx_top_locations all_loc = new nx_top_locations();
		// all_loc.read_top_loc(loc_ff, owr);
		// List<String> all_prt = all_loc.get_print_list();
		// for (String ln_prt : all_prt) {
		// System.out.println(ln_prt);
		// }
		//
		// nx_coid_failures all_fail = new nx_coid_failures();
		// all_fail.read_failures(fail_ff, owr);
		//
		// System.out
		// .println("FAILURES=" + Arrays.toString(all_fail.failures));
		//
		// nx_location_stats rr = new nx_location_stats();
		// nx_location_stats.add_failures(fail_ff, owr, rr);
		// System.out.println("#F=" + rr.num_year_failures + "\nlast_f="
		// + convert.utc_to_string(rr.last_failure_tm));
		//
		// }
	}

	public static void TEST_inc_fail() {
		long h_milis = 1000 * 60 * 60;
		long d_milis = h_milis * 24;
		long m_milis = d_milis * 30;
		// long t_milis = m_milis * 4;
		System.out.println(nx_coid_failures.get_month(System
				.currentTimeMillis() + m_milis));
	}

	// TEST_ MULTI_CLI_SRV_MUDP
	// TEST_ MULTI_CLI_SRV_MUDP
	// TEST_ MULTI_CLI_SRV_MUDP

	private static void reqs_mudp_server(final nx_peer for_reqs) {
		if (IN_DEBUG_2) {
			logger.debug("reqs_mudp_server");
		}
	}

	static Runnable get_srv_reqs_mudp(final nx_peer for_reqs) {
		if (IN_DEBUG_2) {
			logger.debug("get_srv_reqs_mudp");
		}
		Runnable rr1 = new Runnable() {
			public void run() {
				reqs_mudp_server(for_reqs);
			}
		};
		return rr1;
	}

	public static void get_all_usr_to_switch(mer_twist gg, test_user[] all_usr,
			List<test_user> all_to_sw) {

		all_to_sw.clear();
		for (test_user uu : all_usr) {
			if (uu.is_to_switch()) {
				boolean ok1 = gen_rand_bool(gg);
				if (ok1) {
					all_to_sw.add(uu);
				}
			}
		}
	}

	public static void get_all_free_peers(test_peer[] all_pee,
			List<test_peer> all_free) {

		all_free.clear();
		for (test_peer pp : all_pee) {
			if (!pp.is_service_running()) {
				all_free.add(pp);
			}
		}
	}

	public static List<test_user> get_busy(test_user[] all_usr) {
		List<test_user> all_busy = new ArrayList<test_user>();
		for (test_user uu : all_usr) {
			if (uu.is_busy()) {
				all_busy.add(uu);
			}
		}
		return all_busy;
	}

	public static int get_usr(mer_twist gg, test_user[] all_usr,
			test_user not_me) {
		int idx_uu = -1;
		if (all_usr == null) {
			return -1;
		}
		if (all_usr.length == 0) {
			return -1;
		}
		int max_try = all_usr.length;
		while (max_try > 0) {
			int ii = (int) convert
					.to_interval(gg.nextLong(), 0, all_usr.length);
			if (all_usr[ii] != not_me) {
				idx_uu = ii;
				break;
			}
			max_try--;
		}
		return idx_uu;
	}

	public static List<test_user[]> calc_users_for_round(mer_twist gg,
			test_user[] all_usr, boolean only_logged) {

		boolean DEBUG_ONLY_ONE = false;

		List<test_user[]> all_pair = new ArrayList<test_user[]>();

		List<test_user> all_busy = get_busy(all_usr);
		while (!all_busy.isEmpty()) {
			test_user[] pair_uu = new test_user[2];

			int uu1_idx = (int) convert.to_interval(gg.nextLong(), 0,
					all_busy.size());
			pair_uu[0] = all_busy.remove(uu1_idx);

			if (all_busy.isEmpty()) {
				break;
			}

			if (only_logged) {
				int uu2_idx_r = (int) convert.to_interval(gg.nextLong(), 0,
						all_busy.size());
				pair_uu[1] = all_busy.remove(uu2_idx_r);
				all_pair.add(pair_uu);
			} else {
				int uu2_idx = get_usr(gg, all_usr, pair_uu[0]);
				if (uu2_idx != -1) {
					pair_uu[1] = all_usr[uu2_idx];
					all_pair.add(pair_uu);
				}
			}
			if (DEBUG_ONLY_ONE && !all_pair.isEmpty()) {
				break;
			}
		}
		return all_pair;
	}

	public static void print_all_users(test_user[] all_users) {
		for (test_user uu : all_users) {
			if (uu.is_busy()) {
				System.out.print(uu.my_peer.loc_port);
				System.out.print("  \t");
			}
		}
		System.out.println();
		for (test_user uu : all_users) {
			if (uu.is_busy()) {
				System.out.print(uu.name);
				System.out.print("\t");
			}
		}
		System.out.println();
		System.out.println("___________________________________");
	}

	public static String get_descr_mudp_port(String descr) {
		int idx1 = descr.lastIndexOf(nx_peer.PORT_SEP);
		int idx2 = descr.lastIndexOf(nx_peer.PORT_SEP, idx1 - 1);
		if ((idx2 != -1) && (idx1 != -1)) {
			String pp = descr.substring(idx2 + 1, idx1);
			return pp;
		}
		return descr;
	}

	public static boolean[] test_multi_locators_once(mer_twist gg,
			test_user[] all_users, boolean only_logged, boolean change_usrs) {

		boolean DEBUG_FIND_THDS = false;
		boolean DEBUG_CHANGES = true;
		boolean DEBUG_SLOW_P1 = false;
		boolean DEBUG_SLOW_P2 = false;
		boolean DEBUG_SLOW_P3 = false;

		boolean[] out = { false, false };

		dbg_slow sl1 = null;
		if (DEBUG_SLOW_P1) {
			sl1 = new dbg_slow();
		}

		List<test_user> all_to_sw = new ArrayList<test_user>();
		if (change_usrs) {
			all_to_sw.clear();
			get_all_usr_to_switch(gg, all_users, all_to_sw);
			for (test_user uu : all_to_sw) {
				if (uu.is_busy()) {
					uu.log_off();
					if (DEBUG_CHANGES) {
						logger.info("LOG_OFF=" + uu.name);
					}
				} else {
					uu.log_on(gg);
					if (DEBUG_CHANGES) {
						logger.info("LOG_ON=" + uu.name);
					}
				}
			}
			if (!all_to_sw.isEmpty()) {
				print_all_users(all_users);
			}
		}

		// wait for servers to start

		for (test_user uu : all_users) {
			if (uu.is_busy()) {
				uu.my_peer.wait_for_service_to_start();
			}
		}

		// Thread.yield();
		try {
			Thread.sleep(1);
			// Thread.sleep(100);
		} catch (InterruptedException ex1) {
			ex1.printStackTrace();
		}

		if (DEBUG_SLOW_P1) {
			sl1.log_if_slow("SLOW_PART_1");
		}

		dbg_slow sl2 = null;
		if (DEBUG_SLOW_P2) {
			sl2 = new dbg_slow();
		}

		if (DEBUG_FIND_THDS) {
			logger.info("servers_started");
		}

		// test_locators here

		List<test_user[]> all_usr_pair = new ArrayList<test_user[]>();
		all_usr_pair = calc_users_for_round(gg, all_users, only_logged);

		boolean ww_ok = !all_usr_pair.isEmpty();

		List<Thread> all_thd = new ArrayList<Thread>();

		for (test_user[] uu_pair : all_usr_pair) {
			test_user uu1 = uu_pair[0];
			test_user uu2 = uu_pair[1];
			if (uu1 == null) {
				throw new bad_netmix(2);
			}
			Thread thd = uu1.find(uu2);
			all_thd.add(thd);
		}

		thread_funcs.wait_for_threads(all_thd);

		if (DEBUG_FIND_THDS) {
			logger.info("waiting_for_find_threads");
		}

		if (DEBUG_SLOW_P2) {
			sl2.log_if_slow("SLOW_PART_2");
		}

		dbg_slow sl3 = null;
		if (DEBUG_SLOW_P3) {
			sl3 = new dbg_slow();
		}

		for (test_user[] uu_pair : all_usr_pair) {
			test_user uu1 = uu_pair[0];
			test_user uu2 = uu_pair[1];
			uu1.ck_find_result(uu2);
		}

		out[0] = !all_to_sw.isEmpty();
		out[1] = ww_ok;

		if (DEBUG_SLOW_P3) {
			sl3.log_if_slow("SLOW_PART_3");
		}

		return out;
	}

	public static void TEST_multi_mudp2(String[] args) {
		if (args.length < 2) {
			System.out.println("args: <repo> <obser>");
			return;
		}
		// return value.replaceAll("[^A-Za-z0-9]", "");
		// return value.replaceAll("[\\W]|_", "");
		// return str1.matches(...);

		String repo = args[0];
		String obser = args[1];
		String o2 = nx_peer.set_description_port(repo, obser);
		System.out.println("repo=" + repo);
		System.out.println("obser=" + obser);
		System.out.println("out=" + o2);
	}

	public static gamal_generator read_gamal_sys() {
		File ff = new File("/home/jose/tmp", "gamal_sys_0.dat");
		String gg_str = mem_file.read_string(ff);
		gamal_generator gg = new gamal_generator(gg_str);
		return gg;
	}

	public static String get_usr_nam(int num_uu) {
		String usr_nm = "usu_" + (num_uu + 1);
		return usr_nm;
	}

	public static void TEST_multi_mudp(String[] args) {
		boolean DEBUG_ALIAS = true;
		boolean DEBUG_SUPRAS = true;

		// long UPDATE_ROUND = 100;
		boolean use_mudp = true;

		int SUPRA_PORT = 7000;
		int SUBOR_PORT = 7002;
		int AUX_PORT = 7001;
		int FIRST_PORT = test_user.FIRST_UDP_PORT;

		// int MAX_USERS = 30;
		int MAX_USERS = 3;
		// int NUM_ROUNDS = 2000;
		int NUM_ROUNDS = 9900;
		// int NUM_ROUNDS = 50;
		// int CHANGE_EACH_NUM_ROUNDS = 20;
		int UNLOGED_EACH_NUM_ROUNDS = 20;

		boolean with_update_locators = true;
		boolean change_users = true;
		boolean also_unlogged = true;

		boolean op_prt_help = false;

		String op_usr_name = null;

		boolean print_top_locations = false;
		boolean update_locators = false;
		boolean print_locators = false;
		boolean print_supralocators = false;
		boolean print_failures = false;
		boolean print_glid = false;
		boolean print_boss = false;

		boolean update_all_locators = false;
		boolean print_all_top_locations = false;

		boolean create_users = false;
		boolean test_get_set_coid = false;

		int num_args = args.length;
		for (int ii = 0; ii < num_args; ii++) {
			String the_arg = args[ii];
			if (the_arg.equals("-h")) {
				op_prt_help = true;
			} else if (the_arg.equals("-update_all_locators")) {
				update_all_locators = true;
			} else if (the_arg.equals("-print_all_top_locations")) {
				print_all_top_locations = true;
			} else if (the_arg.equals("-create_users")) {
				create_users = true;
			} else if ((the_arg.equals("-test_get_set_coid"))
					&& ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				test_get_set_coid = true;
				op_usr_name = args[kk_idx];
			} else if ((the_arg.equals("-print_top_locations"))
					&& ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				print_top_locations = true;
				op_usr_name = args[kk_idx];
			} else if ((the_arg.equals("-update_locators"))
					&& ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				update_locators = true;
				op_usr_name = args[kk_idx];
			} else if ((the_arg.equals("-print_locators"))
					&& ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				print_locators = true;
				op_usr_name = args[kk_idx];
			} else if ((the_arg.equals("-print_supralocators"))
					&& ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				print_supralocators = true;
				op_usr_name = args[kk_idx];
			} else if ((the_arg.equals("-print_failures"))
					&& ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				print_failures = true;
				op_usr_name = args[kk_idx];
			} else if ((the_arg.equals("-print_glid")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				print_glid = true;
				op_usr_name = args[kk_idx];
			} else if ((the_arg.equals("-print_boss")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				print_boss = true;
				op_usr_name = args[kk_idx];
			}
		}

		if (op_prt_help) {
			System.out.println("(TEST_multi_mudp). \n" + " [-create_users]"
					+ " [-print_glid <usr_nam>]" + " [-print_boss <usr_nam>]"
					+ " [-print_all_top_locations]"
					+ " [-print_top_locations <usr_nam>]"
					+ " [-print_locators <usr_nam>]"
					+ " [-print_supralocators <usr_nam>]"
					+ " [-print_failures <usr_nam>]"
					+ " [-test_get_set_coid <usr_nam>]"
					+ " [-update_all_locators]"
					+ " [-update_locators <usr_nam>]");
			return;
		}

		boolean has_usr = (op_usr_name != null);

		test_user curr_usr = null;
		if (has_usr) {
			curr_usr = new test_user(op_usr_name, use_mudp);
			if (curr_usr.is_new()) {
				System.out
						.println("USER '" + op_usr_name + "' does NOT exist.");
				return;
			}
			curr_usr.init_std_corefs();
		}

		if (print_top_locations && has_usr) {
			curr_usr.print_top_locations();
			return;
		}

		if (print_locators && has_usr) {
			curr_usr.print_locators();
			return;
		}

		if (print_supralocators && has_usr) {
			curr_usr.print_supralocators();
			return;
		}

		if (print_failures && has_usr) {
			curr_usr.print_failures();
			return;
		}

		if (print_glid && has_usr) {
			curr_usr.print_glid();
			return;
		}

		if (print_boss && has_usr) {
			curr_usr.print_boss();
			return;
		}

		if (print_all_top_locations) {
			for (int aa = 0; aa < MAX_USERS; aa++) {
				String usr_nm = get_usr_nam(aa);
				test_user uu = new test_user(usr_nm, use_mudp);
				if (!uu.is_new()) {
					uu.init_std_corefs();
					uu.print_top_locations();
				}
			}
			return;
		}

		mer_twist gg = new mer_twist("GG_FOR_TEST_multi_mudp".getBytes());
		gamal_generator gam = TEST_netmix.read_gamal_sys();

		test_user spra_usr = new test_user("supra", use_mudp);
		spra_usr.init_std_corefs();
		spra_usr.write_info();
		spra_usr.u_dir.save_gamal_sys(gam, spra_usr.owr);

		logger.info("SUPRA_GLID=" + spra_usr.the_glid);

		test_peer spra_pee = new test_peer(SUPRA_PORT, spra_usr);
		spra_pee.set_user(spra_usr);
		spra_pee.start_peer_service();
		spra_pee.start_peer_supra_service();

		if (update_locators && has_usr) {
			curr_usr.log_on(null, false);
			curr_usr.update_locators();
			curr_usr.log_off();
			spra_pee.end_peer();
			return;
		}

		if (update_all_locators) {
			for (int aa = 0; aa < MAX_USERS; aa++) {
				String usr_nm = get_usr_nam(aa);
				test_user uu = new test_user(usr_nm, use_mudp);
				if (!uu.is_new()) {
					uu.init_std_corefs();

					logger.info("USER=" + uu.name + "_WITH_%_BUSY="
							+ uu.percentage_busy);

					uu.log_on(null, false);
					uu.update_locators();
					uu.log_off();
				}
			}
			spra_pee.end_peer();
			return;
		}

		if (test_get_set_coid) {
			test_user subor_usr = new test_user("subor", use_mudp);
			subor_usr.init_std_corefs();
			subor_usr.write_info();
			subor_usr.u_dir.save_gamal_sys(gam, subor_usr.owr);

			test_peer subor_pee = new test_peer(SUBOR_PORT, subor_usr);
			subor_pee.set_user(subor_usr);
			subor_pee.start_peer_service();
			// subor_pee.start_peer_supra_service();

			String boss_coref = spra_usr.the_glid.get_str();
			String subor_coref = subor_usr.the_glid.get_str();

			nx_conn_id spra_subor_coid = spra_usr.u_dir.get_coid_by_ref(subor_coref, null);
			if (spra_subor_coid == null) {
				spra_pee.conn_to(subor_pee.srv_conn_nm, null);
				subor_usr.add_supra_locator(spra_pee);

				spra_pee.add_subor(subor_pee);
				subor_pee.stop_peer_service();
				subor_pee.start_peer_service();
			}

			curr_usr.log_on(null, false);

			test_peer usr_pee = curr_usr.my_peer;

			nx_conn_id subor_coid = curr_usr.u_dir.get_coid_by_ref(subor_coref, null);
			if (subor_coid == null) {
				usr_pee.conn_to(subor_pee.srv_conn_nm, null);
			}

			boolean ok_v = usr_pee.lc_for_reqs.verif_subordinate_coid(
					boss_coref, subor_coref, true);

			if (!ok_v) {
				throw new bad_netmix(2);
			}

			curr_usr.log_off();

			subor_pee.end_peer();
			spra_pee.end_peer();
			return;
		}

		String supra_descr = spra_pee.srv_conn_nm;
		String supra_gli = spra_usr.the_glid.get_str();

		test_user aux_usr = new test_user("auxusr", use_mudp);
		aux_usr.init_std_corefs();
		aux_usr.u_dir.save_gamal_sys(gam, aux_usr.owr);
		test_peer aux_pee = new test_peer(AUX_PORT, aux_usr);

		int po = FIRST_PORT;
		test_user[] all_users = new test_user[MAX_USERS];
		for (int aa = 0; aa < all_users.length; aa++) {
			String usr_nm = get_usr_nam(aa);
			test_user uu = new test_user(usr_nm, use_mudp);
			uu.init_std_corefs();

			all_users[aa] = uu;

			if (uu.is_new()) {
				logger.info("NEW_test_user=" + uu.name);

				long rand_nu = gg.nextLong();
				int num_ports = (int) (convert.to_interval(rand_nu, 1, 10));
				int per_alive = (((10 - num_ports) * 10) + 10);

				uu.percentage_busy = per_alive;
				uu.min_port = po;
				po += num_ports;
				uu.max_port = po;

				if (with_update_locators) {
					uu.max_logons = (int) (convert.to_interval(gg.nextLong(),
							10, 20));
				}

				if (!uu.u_dir.has_gamal_sys(uu.owr)) {
					uu.u_dir.save_gamal_sys(gam, uu.owr);
				}

				aux_pee.set_user(uu);
				boolean is_fst = aux_pee.conn_to(supra_descr, null);
				if (is_fst) {
					uu.add_supra_locator(spra_pee);

					if (DEBUG_ALIAS) {
						logger.info("CREATING_ALIAS_FOR=" + supra_descr
								+ " \n\talias=" + spra_pee.srv_req_nm);
					}
					
					nx_dir_base b_dd = aux_pee.the_user.u_dir;

					b_dd.write_coref_alias(supra_gli, supra_descr);

					String locat_descr = nx_locator.set_port(supra_descr);
					b_dd.write_coref_alias(supra_descr, locat_descr);
					String supra_locat_descr = nx_supra_locator
							.set_port(supra_descr);
					b_dd.write_coref_alias(supra_descr, supra_locat_descr);
				}
				aux_pee.reset_user();

				if (uu.percentage_busy > 50) {
					uu.log_on(gg);
				}
			} else {
				if (uu.was_on > 0) {
					uu.log_on(null, false);
				}
			}

			logger.info("USER=" + uu.name + "_WITH_%_BUSY="
					+ uu.percentage_busy);
		}

		if (create_users) {
			finish_test(spra_pee, aux_pee, all_users);
			return;
		}

		if (DEBUG_SUPRAS) {
			logger.info("\n\nALL_SUPRA\n\n");

			for (test_user uu : all_users) {
				uu.print_supra_locators();
			}

			logger.info("\n\n\nSTART_ROUNDS\nSTART_ROUNDS\nSTART_ROUNDS\n\n\n");
		}

		print_all_users(all_users);

		for (long bb = 1; bb < NUM_ROUNDS; bb++) {

			boolean chg = change_users;
			boolean onlog = !(also_unlogged && ((bb % UNLOGED_EACH_NUM_ROUNDS) == 0));

			test_multi_locators_once(gg, all_users, onlog, chg);

			int num_act = Thread.activeCount();
			logger.info("ro=" + bb + " chg=" + chg + " only_logged=" + onlog
					+ " Finished round=" + bb + " num_thds=" + num_act);
			// System.console().readLine();
		}

		finish_test(spra_pee, aux_pee, all_users);
	}

	static void finish_test(test_peer spra_pee, test_peer aux_pee,
			test_user[] all_users) {
		logger.info("\n\n\nFINISH_ALL_PEERS\nFINISH_ALL_PEERS\nFINISH_ALL_PEERS\n\n\n");

		for (int aa = 0; aa < all_users.length; aa++) {
			test_user uu = all_users[aa];
			if (uu.is_busy()) {
				uu.log_off();
				uu.was_on = 1;
			} else {
				uu.was_on = 0;
			}
			uu.write_info();
		}

		spra_pee.end_peer();
		aux_pee.end_peer();
	}

	public static void TEST_per_alive(String[] args) {
		if (args.length < 1) {
			System.out.println("args: <num>");
			return;
		}
		int num_ports = Integer.parseInt(args[0]);
		int per_alive = (((10 - num_ports) * 10) + 10);
		System.out.println("per_alive=" + per_alive);
	}

	public static void TEST_ref_conver(String[] args) {
		if (args.length < 1) {
			System.out.println("args: <ref>");
			return;
		}
		String orig_ref = args[0];

		if (nx_dir_base.is_valid_ref(orig_ref)) {
			System.out.println("fixed=" + orig_ref);
		} else {
			String f_str = nx_dir_base.fix_coref(orig_ref);
			System.out.println("fixed=" + f_str);
			String uf_str = nx_dir_base.unfix_coref(f_str);
			if (!uf_str.equals(orig_ref)) {
				throw new bad_netmix(2);
			}

		}

		// if(orig_ref.
		// return value.replaceAll("[^A-Za-z0-9]", "");
		// return value.replaceAll("[\\W]|_", "");
		// return str1.matches(...);

	}
}

// TEST_USER
// TEST_USER
// TEST_USER
// TEST_USER

class test_user {
	public static final boolean DEBUG_FIND_START = false;
	public static final boolean DEBUG_SLOW_FIND = false;

	static final String root_dir = "/home/jose/tmp/test_nx_mudp/";
	static final int INFO_MAX_BY = 100;

	public static int FIRST_UDP_PORT = 11000;

	public String name;
	public key_owner owr;
	public nx_dir_base u_dir;
	public nx_std_coref the_glid;
	public nx_std_coref the_boss;
	public String found_addr;
	public boolean could_read;

	public int percentage_busy;
	public int num_rounds;
	public int min_port;
	public int max_port;
	public int last_port;
	public int was_on;
	public int num_logons;
	public int max_logons;
	public test_peer my_peer;

	test_user(String nm, boolean use_mudp) {
		init_user(nm, use_mudp);
	}

	private File get_usr_dir() {
		String dir_nm = "ROOT." + name;
		File rr = new File(root_dir + dir_nm);
		return rr;
	}

	private void init_user(String nm, boolean use_mudp) {
		name = nm;
		byte[] kk = nm.getBytes();
		owr = new key_owner(kk);

		File rr = get_usr_dir();

		int net_kk = net_funcs.TCP_NET;
		if (use_mudp) {
			net_kk = net_funcs.MUDP_NET;
		}

		u_dir = new nx_dir_base(rr, net_kk);

		the_glid = null;
		the_boss = null;
		found_addr = null;
		could_read = false;

		percentage_busy = 100;
		num_rounds = 0;
		min_port = FIRST_UDP_PORT;
		max_port = min_port + 5;
		last_port = 0;
		was_on = 0;
		num_logons = 0;
		max_logons = 0;
		my_peer = null;

		read_info();
	}

	boolean is_new() {
		return (!could_read);
	}

	void init_std_corefs() {
		if (!u_dir.has_local_glid()) {
			byte[] kk = owr.get_copy_of_secret_key();
			global_id per_gli = new global_id(kk, owr);
			the_glid = new nx_std_coref(per_gli);
			u_dir.set_local_glid(owr, the_glid);
		} else {
			the_glid = u_dir.get_local_glid(owr);
		}
		File nx_dd = u_dir.get_local_nx_dir();

		if (!u_dir.has_local_alias()) {
			nx_std_coref nm_ref = new nx_std_coref(name);
			u_dir.set_local_alias(owr, nm_ref);
		}

		the_boss = nx_std_coref.get_boss(nx_dd, owr);
	}

	File get_info_file() {
		File rr = get_usr_dir();
		File ff = new File(rr, "info.usr");
		return ff;
	}

	void write_info() {
		if (is_busy()) {
			throw new bad_netmix(2);
		}

		byte[] info = new byte[INFO_MAX_BY];
		ByteBuffer buff = ByteBuffer.wrap(info);
		buff.putInt(percentage_busy);
		buff.putInt(num_rounds);
		buff.putInt(min_port);
		buff.putInt(max_port);
		buff.putInt(last_port);
		buff.putInt(was_on);
		buff.putInt(num_logons);
		buff.putInt(max_logons);
		byte[] dat = Arrays.copyOf(info, buff.position());

		File dat_ff = get_info_file();
		mem_file.concurrent_write_encrypted_bytes(dat_ff, null, dat);
	}

	void read_info() {
		if (is_busy()) {
			throw new bad_netmix(2);
		}

		File dat_ff = get_info_file();
		byte[] dat = mem_file.concurrent_read_encrypted_bytes(dat_ff, null);

		if (dat != null) {
			could_read = true;
			ByteBuffer buff = ByteBuffer.wrap(dat);
			percentage_busy = buff.getInt();
			num_rounds = buff.getInt();
			min_port = buff.getInt();
			max_port = buff.getInt();
			last_port = buff.getInt();
			was_on = buff.getInt();
			num_logons = buff.getInt();
			max_logons = buff.getInt();
		} else {
			logger.info("Could NOT read file=" + dat_ff);
		}
	}

	void add_supra_locator(test_peer tpp) {
		String srv_descr = tpp.srv_req_nm;
		logger.info("USER=" + name + "_adding_supra_locator=" + srv_descr
				+ " in=\n" + u_dir);
		nx_connector.add_local_supra_locator(u_dir, owr, srv_descr);
	}

	void print_supra_locators() {
		List<String> all_supra = nx_connector.read_local_supra_locators(u_dir,
				owr);
		logger.info("\n\n\nSUPRA_LOCATORS_sz=" + all_supra.size() + " dir="
				+ u_dir);
		logger.info(all_supra);
	}

	boolean is_busy() {
		return (my_peer != null);
	}

	boolean is_to_switch() {
		int lim = percentage_busy;
		if (!is_busy()) {
			lim = (100 - percentage_busy);
		}
		num_rounds++;
		if (num_rounds >= lim) {
			num_rounds = 0;
			return true;
		}
		return false;
	}

	void log_on(mer_twist gg) {
		log_on(gg, true);
	}

	void log_on(mer_twist gg, boolean do_report) {
		if (my_peer != null) {
			throw new bad_netmix(2);
		}

		int po = min_port;
		if (last_port != 0) {
			po = last_port;
		}
		if (gg != null) {
			po = (int) (convert.to_interval(gg.nextLong(), min_port, max_port));
		}

		last_port = po;

		test_peer pp = new test_peer(po, this);
		pp.set_user(this);
		pp.start_peer_service();

		if (do_report) {
			pp.report_me();
		}

		if (max_logons != 0) {
			num_logons++;
			if (num_logons == max_logons) {
				num_logons = 0;
				update_locators();
			}
		}

		if (my_peer != pp) {
			throw new bad_netmix(2);
		}
	}

	void log_off() {
		if (my_peer == null) {
			throw new bad_netmix(2);
		}

		my_peer.end_peer();
		my_peer = null;

		if (my_peer != null) {
			throw new bad_netmix(2);
		}
	}

	Runnable do_find(final test_user uu) {
		found_addr = null;
		Runnable rr1 = new Runnable() {
			public void run() {
				if (DEBUG_FIND_START) {
					logger.info("STARTING");
				}
				dbg_slow sl2 = null;
				if (DEBUG_SLOW_FIND) {
					sl2 = new dbg_slow();
				}

				found_addr = my_peer.find_user(uu);

				if (DEBUG_SLOW_FIND) {
					sl2.log_if_slow("SLOW_FIND");
				}
			}
		};
		return rr1;
	}

	Thread find(test_user uu) {
		if (uu == null) {
			throw new bad_netmix(2);
		}
		if (!is_busy()) {
			throw new bad_netmix(2);
		}
		String expect = "offline";
		if (uu.is_busy()) {
			expect = "" + uu.my_peer.loc_port;
		}
		String thd_nm = Thread.currentThread().getName() + "-"
				+ my_peer.loc_port + "-" + name + "-find-in-" + expect + "-"
				+ uu.name;
		Thread thd = nx_context.start_thread(thd_nm, do_find(uu), false);
		return thd;
	}

	void ck_find_result(test_user uu) {
		boolean DEBUG_RESUL = false;

		if (found_addr != null) {
			if (uu.is_busy()) {
				String should_addr = uu.my_peer.srv_conn_nm;
				String should_dest = TEST_netmix
						.get_descr_mudp_port(should_addr);
				String dest = TEST_netmix.get_descr_mudp_port(found_addr);

				if (!dest.equals(should_dest)) {
					String msg = dest + " != " + should_dest
							+ ". FIND_FAILED. CANNOT find user=" + uu + " in "
							+ dest + " found_addr=" + found_addr
							+ " should_addr=" + should_addr + ". TYPE RETURN.";
					logger.info(msg);
					throw new bad_netmix(2, msg);
				} else {
					if (DEBUG_RESUL) {
						logger.info(dest + " == " + should_dest
								+ ". FIND_OK. Found user=" + uu + " in " + dest);
						// System.console().readLine();
					}
				}
			} else {
				throw new bad_netmix(2, "FOUND_NOT_LOGGED_USER " + uu.name
						+ " IN " + found_addr);
			}
		} else {
			if (uu.is_busy()) {
				throw new bad_netmix(2, "COULD_NOT_FIND " + uu.name + " IN "
						+ uu.my_peer.srv_conn_nm + " found_addr=" + found_addr);
			} else {
				if (DEBUG_RESUL) {
					logger.info("USER " + uu.name + " NOT_LOGGED."
							+ ". FIND_OK. ");
				}
			}
		}
	}

	public void print_top_locations() {
		List<File> all_coid_ff = nx_protector.find_all_coid_files(u_dir);
		for (File co_ff : all_coid_ff) {
			nx_conn_id the_coid = nx_protector.get_coid_from_coid_file(co_ff);
			File top_ff = nx_top_locations.get_remote_top_locations_file(u_dir,
					the_coid);
			nx_std_coref gli = u_dir.get_remote_glid(the_coid, owr);

			logger.info("File=" + top_ff + "\n\t" + gli);

			nx_top_locations all_loc = new nx_top_locations();
			all_loc.read_top_loc(top_ff, owr);
			List<String> all_prt = all_loc.get_print_list();

			for (String ln_prt : all_prt) {
				System.out.println(ln_prt);
			}

			System.out.println("_____________");
		}
	}

	public void print_locators() {
		List<String> all_loc = nx_connector.read_local_locators(u_dir, owr);
		for (String loc : all_loc) {
			System.out.println(loc);
		}
		System.out.println("_________________");
	}

	public void print_supralocators() {
		List<String> all_loc = nx_connector.read_local_supra_locators(u_dir,
				owr);
		for (String loc : all_loc) {
			System.out.println(loc);
		}
		System.out.println("_________________");
	}

	public void update_locators() {
		nx_location_stats.update_locators(u_dir, my_peer.p_for_conns, true);
		System.out.println("UPDATED " + name + " locators.");
	}

	public void print_failures() {
		List<File> all_coid_ff = nx_protector.find_all_coid_files(u_dir);
		for (File co_ff : all_coid_ff) {
			nx_conn_id the_coid = nx_protector.get_coid_from_coid_file(co_ff);
			File yy_ff = nx_coid_failures.get_remote_year_failures_file(u_dir,
					the_coid);
			nx_std_coref gli = u_dir.get_remote_glid(the_coid, owr);

			nx_coid_failures all_fail = new nx_coid_failures();
			all_fail.read_failures(yy_ff, owr);

			long tot = all_fail.calc_tot_failures();

			logger.info("File=" + yy_ff + "\n\t" + gli + "\n\ttot_failures="
					+ tot);
		}
	}

	public void print_glid() {
		System.out.println("GLID for " + name + "\n\t" + the_glid);
	}

	public void print_boss() {
		System.out.println("BOSS for " + name + "\n\t" + the_boss);
	}

	public String toString() {
		return name;
	}
}

// TEST_PEER
// TEST_PEER
// TEST_PEER
// TEST_PEER

class test_peer {
	public static final boolean DEBUG_CONNS = false;
	public static final boolean DEBUG_SERV = false;

	static final boolean DEBUG_NEW_COIDS = false;
	static final boolean DEBUG_REPORT_ME = false;
	static final boolean DEBUG_FOUND = false;

	static final boolean VERBOSE = false;
	static final long CONN_PORT = 1000;
	static final long REQ_PORT = nx_locator.LOCATOR_PORT;

	public int loc_port;
	public nx_context ctx;

	public test_user the_user;

	public String srv_conn_nm;
	public String srv_req_nm;

	public nx_peer p_for_conns;
	public nx_peer p_for_reqs;
	public nx_peer p_for_supra;

	public nx_locator lc_for_reqs;

	public Thread thd_for_conns;
	public Thread thd_for_reqs;
	public Thread thd_for_supra;

	test_peer(int pp, test_user uu) {
		init_peer(pp, uu);
	}

	private void init_peer(int pp, test_user uu) {
		loc_port = pp;
		the_user = uu;

		nx_dir_base dd = null;
		// nx_dir_base dd = the_user.b_dir;

		InetSocketAddress srv_addr = new InetSocketAddress("localhost",
				loc_port);

		// String srv_host = srv_addr.getHostName();
		// int srv_port = srv_addr.getPort();

		ctx = null;
		if (the_user.u_dir.get_net_type() == net_funcs.MUDP_NET) {
			ctx = new nx_mudp_context(dd, srv_addr);
		} else {
			ctx = new nx_tcp_context(dd);
		}

		logger.info("init_test_peer=" + the_user.u_dir.get_netmix_base_dir());

		srv_conn_nm = "localhost:" + loc_port + ":" + CONN_PORT;
		srv_req_nm = "localhost:" + loc_port + ":" + REQ_PORT;

		// ctx.set_dir_base(the_user.b_dir);
		nx_peer aux_pp = ctx.make_peer();
		aux_pp.init_local_peer(srv_conn_nm, the_user.owr, false);
		// ctx.set_dir_base(null);

		reset_user();

		init_service();
	}

	void init_service() {
		p_for_conns = null;
		p_for_reqs = null;
		p_for_supra = null;

		lc_for_reqs = null;

		thd_for_conns = null;
		thd_for_reqs = null;
		thd_for_supra = null;
	}

	boolean is_service_running() {
		boolean c1 = (thd_for_conns != null);
		boolean c2 = (thd_for_reqs != null);
		boolean rr = c1 || c2;
		if (!rr && (the_user != null) && the_user.is_busy()) {
			throw new bad_netmix(2, "port=" + loc_port + " user="
					+ the_user.name + " usr_port=" + the_user.my_peer.loc_port);
		}
		return rr;
	}

	void reset_user() {
		set_user(null);
	}

	void set_user(test_user uu) {
		if (is_service_running()) {
			throw new bad_netmix(2);
		}
		if (ctx == null) {
			throw new bad_netmix(2);
		}
		if ((the_user != null) && the_user.is_busy()) {
			throw new bad_netmix(2, "the_user=" + the_user.name + "_IS_BUSY");
		}
		if ((uu != null) && uu.is_busy()) {
			throw new bad_netmix(2, "user_uu=" + uu.name + "_IS_BUSY");
		}

		the_user = uu;
		if (the_user != null) {
			if (the_user.u_dir == null) {
				throw new bad_netmix(2);
			}
			// ctx.set_dir_base(the_user.b_dir);
		} else {
			// ctx.set_dir_base(null);
		}

		String unm = "NO_USR";
		if (the_user != null) {
			unm = the_user.name;
		}
		if (VERBOSE) {
			logger.info("SET_USER=" + unm + " IN_PORT=" + loc_port);
		}
	}

	private static void one_conn_multi_mudp_server(final nx_dir_base b_dir,
			final nx_connection cnn) {
		if (DEBUG_SERV) {
			logger.debug("one_conn_multi_mudp_server");
		}
		if (cnn != null) {
			nx_peer loc_pp = cnn.get_local_peer();
			nx_messenger mgr = new nx_messenger(b_dir, cnn,
					nx_protector.NULL_VERIFIER);
			if (test_peer.DEBUG_CONNS) {
				logger.info("Server '" + loc_pp.get_description()
						+ "' got connection " + cnn);
			}

			mgr.recv_set_secure(true);
			cnn.close_net_connection();
		}
	}

	private static Runnable get_srv_one_conn_multi_mudp(
			final nx_dir_base b_dir, final nx_connection cnn) {
		if (DEBUG_SERV) {
			logger.debug("get_srv_one_conn_multi_mudp");
		}
		Runnable rr1 = new Runnable() {
			public void run() {
				one_conn_multi_mudp_server(b_dir, cnn);
			}
		};
		return rr1;
	}

	private static void conns_mudp_server(final nx_dir_base b_dir,
			final nx_peer for_conns) {

		if (DEBUG_SERV) {
			logger.debug("conns_mudp_server");
		}
		while (for_conns.can_accept()) {
			nx_connection cnn = for_conns.accept();
			if (cnn != null) {
				String for_reqs_nm = Thread.currentThread().getName() + "-one";
				nx_context.start_thread(for_reqs_nm,
						get_srv_one_conn_multi_mudp(b_dir, cnn), false);
			}
		}
	}

	private static Runnable get_srv_conns_mudp(final nx_dir_base b_dir,
			final nx_peer for_conns) {
		if (DEBUG_SERV) {
			logger.debug("get_srv_conns_mudp");
		}
		Runnable rr1 = new Runnable() {
			public void run() {
				conns_mudp_server(b_dir, for_conns);
			}
		};
		return rr1;
	}

	void start_peer_service() {
		if (is_service_running()) {
			throw new bad_netmix(2);
		}
		if (the_user == null) {
			throw new bad_netmix(2);
		}
		if (the_user.is_busy()) {
			throw new bad_netmix(2);
		}
		if (VERBOSE) {
			logger.info("starting_test_service_in_port=" + loc_port
					+ " with_user=" + the_user.name);
		}

		the_user.my_peer = this;
		nx_dir_base b_dir = the_user.u_dir;

		key_owner owr1 = key_owner.get_copy(the_user.owr);
		key_owner owr2 = key_owner.get_copy(the_user.owr);

		p_for_conns = ctx.make_peer();
		p_for_conns.init_local_peer(srv_conn_nm, owr1, true);

		p_for_reqs = ctx.make_peer();
		p_for_reqs.init_local_peer(srv_req_nm, owr2, false);

		String for_conns_nm = Thread.currentThread().getName() + "-" + loc_port
				+ "-conns-" + the_user.name;
		thd_for_conns = nx_context.start_thread(for_conns_nm,
				get_srv_conns_mudp(b_dir, p_for_conns), false);

		// nx_dir_base b_dir = ctx.get_dir_base();
		lc_for_reqs = new nx_locator(b_dir, the_user.owr, p_for_reqs);
		thd_for_reqs = lc_for_reqs.start_locator_server(null);

		if (VERBOSE) {
			logger.info("STARTED_TEST_SERVICE_IN_PORT=" + loc_port
					+ " WITH_USER=" + the_user.name);
		}
	}

	void start_peer_supra_service() {
		key_owner owr3 = key_owner.get_copy(the_user.owr);

		String supra_descr = nx_supra_locator.set_port(srv_conn_nm);

		p_for_supra = ctx.make_peer();
		p_for_supra.init_local_peer(supra_descr, owr3, true);

		// nx_dir_base b_dir = ctx.get_dir_base();
		nx_dir_base b_dir = the_user.u_dir;
		nx_supra_locator su_loc_srv = new nx_supra_locator(b_dir, p_for_supra);
		thd_for_supra = su_loc_srv.start_supra_locator_server();

		logger.info("STARTED_SUPRA_LOCATOR_TEST_SERVICE_IN=" + supra_descr
				+ " WITH_USER=" + the_user.name);
	}

	void add_subor(test_peer subor) {
		if (the_user == null) {
			throw new bad_netmix(2);
		}
		if (subor == null) {
			throw new bad_netmix(2);
		}
		if (subor.the_user == null) {
			throw new bad_netmix(2);
		}

		nx_dir_base bb1 = the_user.u_dir;
		nx_dir_base bb2 = subor.the_user.u_dir;

		nx_std_coref boss_gli = the_user.the_glid;
		nx_std_coref subor_gli = subor.the_user.the_glid;

		nx_conn_id subor_coid = bb1.get_coid_by_ref(subor_gli.get_str(), null);

		if (subor_coid == null) {
			throw new bad_netmix(2);
		}

		File rem_nx_dd = bb1.get_remote_nx_dir(subor_coid);
		if (!nx_std_coref.has_boss(rem_nx_dd)) {
			nx_std_coref.set_boss(rem_nx_dd, the_user.owr, boss_gli);
		}

		File loc_nx_dd = bb2.get_local_nx_dir();
		if (!nx_std_coref.has_boss(loc_nx_dd)) {
			nx_std_coref.set_boss(loc_nx_dd, subor.the_user.owr, boss_gli);
		}
	}

	void get_set_coid(test_peer spra_pee, test_user uu) {
		uu.log_on(null, false);

		nx_dir_base bb1 = the_user.u_dir;
		nx_conn_id uu_coid = bb1.get_coid_by_ref(uu.the_glid.get_str(), null);
		if (uu_coid == null) {
			conn_to(uu.my_peer.srv_conn_nm, null);
			uu_coid = bb1.get_coid_by_ref(uu.the_glid.get_str(), null);
		}
		if (uu_coid == null) {
			throw new bad_netmix(2);
		}

		uu.log_off();
	}

	boolean conn_to(String srv_descr, nx_std_coref the_glid) {
		if (the_user == null) {
			throw new bad_netmix(2);
		}

		String cli_loc_descr = "localhost:" + loc_port + ":" + 3000;
		nx_peer pp1 = ctx.make_peer();
		pp1.init_local_peer(cli_loc_descr, the_user.owr, false);
		nx_peer pp2 = ctx.make_peer();
		pp2.init_remote_peer(srv_descr, null);

		if (DEBUG_CONNS) {
			logger.info("Trying client connection to " + pp2.get_description());
		}
		nx_connection cnn = pp1.connect_to(pp2);
		if (cnn != null) {
			nx_dir_base b_dir = the_user.u_dir;
			nx_messenger mgr = new nx_messenger(b_dir, cnn,
					nx_protector.NULL_VERIFIER);
			if (DEBUG_CONNS) {
				logger.info("Client '" + pp1.get_description()
						+ "' got connection " + cnn);
			}

			// nx_dir_base b_dir = ctx.get_dir_base();
			nx_conn_id old_coid = null;
			if (the_glid != null) {
				old_coid = b_dir.get_coid_by_ref(the_glid.get_str(), null);
			} else {
				old_coid = b_dir.get_coid_by_ref(pp2.get_description(), null);
			}

			String rem_descrip = pp2.get_description();

			boolean is_fst_tm = (old_coid == null);
			if (is_fst_tm) {
				if (DEBUG_NEW_COIDS) {
					logger.info("Creating_new_coid_for=" + rem_descrip);
				}
			}

			mgr.send_set_secure(null, null, true);
			cnn.close_net_connection();

			return is_fst_tm;
		}
		return false;
	}

	void stop_peer_service() {
		boolean DEBUG_STOP_1 = false;
		if (the_user == null) {
			throw new bad_netmix(2);
		}

		if (DEBUG_STOP_1) {
			logger.info("stopping_test_service_in_port=" + loc_port
					+ " with_user=" + the_user.name);
		}

		p_for_conns.kill_accept();
		p_for_reqs.kill_responder();

		if (DEBUG_STOP_1) {
			logger.info("stopping_2_port=" + loc_port + " with_user="
					+ the_user.name);
		}

		try {
			if (thd_for_conns != null) {
				thd_for_conns.join();
				thd_for_conns = null;
			}
			if (DEBUG_STOP_1) {
				logger.info("stopping_3_port=" + loc_port + " with_user="
						+ the_user.name);
			}
			if (thd_for_reqs != null) {
				thd_for_reqs.join();
				thd_for_reqs = null;
			}
			if (DEBUG_STOP_1) {
				logger.info("stopping_4_port=" + loc_port + " with_user="
						+ the_user.name);
			}
		} catch (InterruptedException ee) {
			ee.printStackTrace();
		}

		the_user.my_peer = null;

		if (the_user.is_busy()) {
			throw new bad_netmix(2);
		}

		init_service();
		if (VERBOSE) {
			logger.info("STOPPED_TEST_SERVICE_IN_PORT=" + loc_port
					+ " WITH_USER=" + the_user.name);
		}
	}

	void end_peer() {
		if (is_service_running()) {
			stop_peer_service();
		}

		if (ctx == null) {
			throw new bad_netmix(2);
		}

		ctx.stop_context();
	}

	void report_me() {
		// String cli_loc_descr = "localhost:" + loc_port + ":" + 4000;
		String cli_loc_descr = "localhost:" + loc_port + ":" + REQ_PORT;
		nx_peer pp1 = ctx.make_peer();
		pp1.init_local_peer(cli_loc_descr, the_user.owr, false);

		List<Thread> all_thds = nx_connector.report_coref(the_user.u_dir, pp1);
		thread_funcs.wait_for_threads(all_thds);
		if (DEBUG_REPORT_ME) {
			logger.info("REPORTED_" + cli_loc_descr + "\n\tname="
					+ the_user.name + "\n\tglid=" + the_user.the_glid);
		}
	}

	String find_user(test_user uu) {
		boolean DEBUG_SLOW_FIND_P1 = false;
		boolean DEBUG_SLOW_FIND_P2 = false;
		boolean DEBUG_SLOW_FIND_P3 = false;

		String cli_loc_descr = "localhost:" + loc_port + ":" + REQ_PORT;
		nx_peer pp1 = ctx.make_peer();
		pp1.init_local_peer(cli_loc_descr, the_user.owr, false);

		// String rq_target = uu.the_glid.get_str();
		String rq_target = uu.name;

		dbg_slow sl1 = null;
		if (DEBUG_SLOW_FIND_P1) {
			sl1 = new dbg_slow();
		}
		String curr_loc = nx_connector.find_coref_location(the_user.u_dir, pp1,
				rq_target);

		if (DEBUG_SLOW_FIND_P1) {
			sl1.log_if_slow("SLOW_FIND_P1");
		}

		if (curr_loc != null) {
			String pp2_cnn_addr = nx_peer.set_description_port(srv_conn_nm,
					curr_loc);

			if (DEBUG_FOUND) {
				logger.info("FOUND_USER=" + uu.name + "_IN=" + curr_loc
						+ " conn_to=" + pp2_cnn_addr);
			}

			dbg_slow sl2 = null;
			if (DEBUG_SLOW_FIND_P2) {
				sl2 = new dbg_slow();
			}

			boolean ping_ok = nx_connector.ping_request(the_user.u_dir, pp1,
					curr_loc, rq_target, true);

			if (!ping_ok) {
				throw new bad_netmix(2, "BAD_PING_REQ !!!");
			}

			if (DEBUG_SLOW_FIND_P2) {
				sl2.log_if_slow("SLOW_FIND_P2");
			}

			dbg_slow sl3 = null;
			if (DEBUG_SLOW_FIND_P3) {
				sl3 = new dbg_slow();
			}

			conn_to(pp2_cnn_addr, uu.the_glid);

			if (DEBUG_SLOW_FIND_P3) {
				sl3.log_if_slow("SLOW_FIND_P3");
			}

			if (DEBUG_FOUND) {
				logger.info("CONN_OK. FOUND_USER=" + uu.name + "_IN="
						+ curr_loc + " conn_to=" + pp2_cnn_addr);
			}
		}
		return curr_loc;
	}

	public void wait_for_service_to_start() {
		while (!p_for_conns.is_server_started()) {
			Thread.yield();
		}
		while (!p_for_reqs.is_server_started()) {
			Thread.yield();
		}
	}

	public String toString() {
		return srv_conn_nm;
	}
}

// TEST_BASIC_MUDP (TEST_send_recv_mudp)
// TEST_LOCATOR (TEST_locator)
// TEST_SUPRA_LOCATOR
// TEST_ MULTI_CLI_SRV_MUDP (TEST_multi_mudp)
