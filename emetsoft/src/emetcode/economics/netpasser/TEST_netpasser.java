package emetcode.economics.netpasser;

import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import emetcode.crypto.bitshake.utils.config;
import emetcode.crypto.bitshake.utils.debug_thread_dialog;
import emetcode.crypto.bitshake.utils.file_funcs;
import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.crypto.bitshake.utils.mem_file;
import emetcode.crypto.gamal.gamal_generator;
import emetcode.economics.netpasser.locale.L;
import emetcode.economics.passet.iso;
import emetcode.economics.passet.paccount;
import emetcode.economics.passet.tag_denomination;
import emetcode.net.netmix.nx_context;
import emetcode.net.netmix.nx_peer;
import emetcode.net.netmix.tcp_adapter.nx_tcp_context;
import emetcode.util.devel.logger;
import emetcode.util.devel.net_funcs;
import emetcode.util.devel.thread_funcs;

public class TEST_netpasser implements trans_operator, Runnable {
	public static final File DEFAULT_ROOT = new File("./test_netpasser_user");

	public static final int DEFAULT_CURRENCY = emetcode.economics.passet.config.DEFAULT_CURRENCY;

	public static final String DEF_NM = "localhost:7779";

	static String help_msg = "TEST_netpasser [-h] [-c] [-d <root_dir>] [-l <l_descr>] [-r <r_descr>] "
			+ "[-k <key>] [-m <currency>]"
			+ "\n"
			+ "-h : show invocation info.\n"
			+ "-c : create some passets.\n"
			+ "-d <root_dir> : take <root_dir> as the root directory. Default is '"
			+ DEFAULT_ROOT.getPath()
			+ "'\n"
			+ "-l <descr> : accept calls on <descr>. Default is '"
			+ DEF_NM
			+ "'\n"
			+ "-r <descr> : call <descr>. \n"
			+ "-k <key> : use <key> to encrypt and decrypt files. \n"
			+ "-m <currency> : use <currency> in passets. \n";

	BlockingQueue<transaction> to_user_operate;

	boolean use_i2p;
	boolean create_some_pss;
	String l_descr;
	String r_descr;

	File root_dir;
	byte[] l_key;
	int currency_idx;

	transaction srv_trans;
	transaction cli_trans;

	public TEST_netpasser() {
		to_user_operate = new LinkedBlockingQueue<transaction>(1);

		use_i2p = false;
		create_some_pss = false;
		l_descr = DEF_NM;
		r_descr = null;

		root_dir = DEFAULT_ROOT;
		l_key = "default_key".getBytes();
		currency_idx = DEFAULT_CURRENCY;

		srv_trans = null;
		cli_trans = null;
	}

	public void run() {
		logger.info("Starting user thread");

		while (true) {
			transaction trans = wait_for_transaction(null);
			debug_thread_dialog.msg("USER_INPUT");
			if (trans.state_oper == transaction.USER_TELL_FINISHED_OPER) {
				break;
			}
			//trans.trust_level = transaction.TRUST_ANY;
			trans.answer_oper(transaction.NET_CONTINUE_OPER);
		}

		// logger.info("FINISHING user thread");
	}

	public boolean get_args(String[] args) {

		root_dir = DEFAULT_ROOT;
		boolean prt_help = false;

		l_descr = DEF_NM;
		r_descr = null;

		int num_args = args.length;
		for (int ii = 0; ii < num_args; ii++) {
			String the_arg = args[ii];
			if (the_arg.equals("-h")) {
				prt_help = true;
			} else if (the_arg.equals("-c")) {
				create_some_pss = true;
			} else if ((the_arg.equals("-d")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				root_dir = new File(args[kk_idx]);
			} else if ((the_arg.equals("-l")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				l_descr = args[kk_idx];
			} else if ((the_arg.equals("-r")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				r_descr = args[kk_idx];
			} else if ((the_arg.equals("-k")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				l_key = args[kk_idx].getBytes();
			} else if ((the_arg.equals("-m")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				currency_idx = iso.currencies_map.get(args[kk_idx]);
			} else {
				prt_help = true;
			}
		}

		if (prt_help) {
			System.out.println(help_msg);
			return false;
		}

		return true;
	}

	public void start_service() {

		System.out.println("ROOT_DIR=" + root_dir);
		System.out.println("Starting TEST with name '" + l_descr + "'.");

		key_owner owr = new key_owner(l_key);
		logger.info("MIKID=" + owr.get_mikid());

		// init peer

		nx_context ctx = null;
		if (use_i2p) {
			logger.info("Usign i2p");
			//ctx = new nx_i2p_context(root_dir);
		} else {
			logger.info("Usign TCP");
			ctx = new nx_tcp_context(root_dir);
		}
		nx_peer l_peer = ctx.make_peer();
		l_peer.init_local_peer(l_descr, owr, true);

		// issue some passet transactions

		String local_descr = l_peer.get_description();
		logger.info("local peer descr=" + local_descr);
		if (create_some_pss) {
			issue_some_passets(local_descr);
		}

		// init transactions

		// srv_trans = new transaction(root_dir, owr);
		// srv_trans.set_working_currency(currency_idx);
		// cli_trans = new transaction(root_dir, owr);
		// cli_trans.set_working_currency(currency_idx);

		srv_trans.state_oper = transaction.NET_ACCEPT_OPER;

		// passet local_pss = cli_trans.local_pss;
		// selectable_list selected = new selectable_list(local_pss);
		// selected.select_all();
		// local_pss.write_selection(null, selected, owr);

		// cli_trans.fill_selectable();
		// cli_trans.selec_data.select_all();
		cli_trans.state_oper = transaction.NET_SEND_PASSETS_OPER;

		// String user_pth = srv_trans.local_pss.get_mikid_path();

		// / user_path in codis_dir for messanger

		net_operator srv_net_op = new net_operator(l_peer);
		net_operator cli_net_op = new net_operator(l_peer);
		file_operator fl_op = new file_operator(l_peer);

		// srv_trans.init_callers(null, fl_op);
		// cli_trans.init_callers(this, fl_op);

		srv_net_op.set_first_trans(srv_trans);
		cli_net_op.set_first_trans(cli_trans);

		Thread fl_thd = nx_context.start_thread("THD_files", fl_op, false);
		Thread srv_thd = nx_context.start_thread("THD_srv_net",
				srv_net_op.get_run_loop_server(), false);
		Thread cli_thd = null;
		if (r_descr != null) {
			logger.info("remote peer descr=" + r_descr);
			cli_thd = nx_context.start_thread("THD_cli_net",
					cli_net_op.get_run_once_client(), false);
		}
		Thread usr_thd = nx_context.start_thread("THD_user", this, false);

		// srv_net_op.queue_transaction(srv_trans);

		// cli_trans.r_peer_descr = r_descr;
		// if(r_descr != null){
		// logger.info("remote peer descr=" + r_descr);

		// cli_net_op.queue_transaction(cli_trans);
		// }

		if (r_descr != null) {
			try {
				usr_thd.join();

				if (cli_thd != null) {
					cli_net_op.tell_finish_cli();
					cli_thd.join();
				}

				List<Thread> skip = new ArrayList<Thread>();
				skip.add(fl_thd);
				skip.add(srv_thd);

				thread_funcs.prt_active_thds("BEFORE_ENDING=");
				// thread_funcs.wait_for_active_threads(skip);

				logger.info("KILLING SERVER");
				// srv_net_op.running_srv.set(false);
				srv_net_op.kill_server();
				srv_thd.join();
				thread_funcs.prt_active_thds("AFTER_KILLING=");

				fl_op.tell_finish_cli();
				fl_thd.join();

			} catch (InterruptedException ee) {
				// ee.printStackTrace();
			}
		}

		thread_funcs.prt_active_thds("ENDING=");
	}

	public void issue_some_passets(String loc_descr) {
		key_owner owr = new key_owner(l_key);
		paccount pcc1 = new paccount();
		pcc1.set_base_dir(root_dir, owr, net_funcs.TCP_NET, null);
		pcc1.set_working_currency(currency_idx);

		tag_denomination deno = new tag_denomination(
				pcc1.get_working_currency());
		deno.ten_exponent = 4;
		deno.multiplier = 5;

		if (use_i2p) {
			//pcc1.curr_user.i2p_address = loc_descr;
		} else {
			pcc1.curr_user.network_domain_name = loc_descr;
		}

		pcc1.issue_passets(5, owr, deno, null, null);
	}

	public void queue_transaction(transaction trans) {
		try {
			to_user_operate.put(trans);
		} catch (InterruptedException ex) {
			throw new bad_netpasser(2, ex.toString());
		}
	}

	public transaction wait_for_transaction(transaction trans0) {
		transaction trans = null;
		try {
			trans = to_user_operate.take();
			if (trans == null) {
				throw new bad_netpasser(2);
			}
			if (trans.state_oper == transaction.INVALID_OPER) {
				throw new bad_netpasser(2, L.recv_invalid_oper);
			}
		} catch (InterruptedException e) {
			throw new bad_netpasser(2);
		}
		return trans;
	}

	public static void main_TEST_netpasser(String[] args) {
		TEST_netpasser tt = new TEST_netpasser();
		if (!tt.get_args(args)) {
			return;
		}

		tt.start_service();
	}

	public static String get_thd_nam() {
		Thread curr_thd = Thread.currentThread();
		return curr_thd.getName();
	}

	public static void main_TEST_get_host(String[] args) {
		System.out.println("HostName:" + net_funcs.get_hostname());
		System.out.println(Arrays.toString(net_funcs.get_all_ip()));
	}

	public static void main_TEST_threads(String[] args) {
		TEST_netpasser tt = new TEST_netpasser();
		tt.thd_TEST();
	}

	Runnable get_code1() {
		Runnable code = new Runnable() {
			public void run() {
				thd_func1();
			}
		};
		return code;
	}

	Runnable get_code2() {
		Runnable code = new Runnable() {
			public void run() {
				thd_func2();
			}
		};
		return code;
	}

	Thread run_thd(Runnable code, String nm, boolean as_daem) {
		Thread thd1 = new Thread(code);
		thd1.setName(nm);
		thd1.setDaemon(as_daem);
		thd1.start();
		return thd1;
	}

	void thd_func2() {
		run_thd(get_code1(), get_thd_nam() + "_1", false);
		run_thd(get_code1(), get_thd_nam() + "_2", false);

		logger.info("FINISHED");
	}

	void thd_func1() {
		long val = 0;
		while (true) {
			val++;
			logger.info("prt_" + val + " active=" + Thread.activeCount());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			// prt_active_thds();
		}
	}

	void thd_TEST() {
		Thread thd = run_thd(get_code2(), get_thd_nam() + "_1", false);

		thd.interrupt();
		thread_funcs.prt_active_thds("TEST");

		logger.info("FINISHED");
	}

	static String zip_help_msg = "args: [-h] [(-z|-u) <target>] [-d <dest>] "
			+ "-h : show invocation info.\n" + "-z : zip <target> dir.\n"
			+ "-u : unzip <target> file.\n"
			+ "-d : zip/uzip into <dest> file/dir.";

	public static void main_TEST_zip_dir(String[] args) {
		File to_zip = null;
		File to_unzip = null;
		File zip_dst = null;
		boolean prt_help = false;

		int num_args = args.length;
		for (int ii = 0; ii < num_args; ii++) {
			String the_arg = args[ii];
			if (the_arg.equals("-h")) {
				prt_help = true;
			} else if ((the_arg.equals("-z")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				to_zip = new File(args[kk_idx]);
			} else if ((the_arg.equals("-u")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				to_unzip = new File(args[kk_idx]);
			} else if ((the_arg.equals("-d")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				zip_dst = new File(args[kk_idx]);
			} else {
				prt_help = true;
			}
		}

		if (prt_help) {
			System.out.println(zip_help_msg);
			return;
		}

		if (to_zip != null) {
			if (zip_dst == null) {
				zip_dst = new File(to_zip.getPath() + ".zip");
			}
			file_funcs.zip_dir(to_zip, zip_dst);
		}
		if (to_unzip != null) {
			if (zip_dst == null) {
				zip_dst = new File(".");
			}
			file_funcs.unzip_dir(to_unzip, zip_dst);
		}

		System.out.println(zip_help_msg);

	}

	public static final int MIN_GAMAL_BITS = 1000;
	public static final int MIN_GAMAL_CERTAINTY = 1000;

	public static void main_create_gamal_generators(String[] args) {

		if (args.length < 2) {
			System.out.println("args: <key> <num_gen>");
			return;
		}
		byte[] kk = args[0].getBytes(config.UTF_8);
		key_owner own = new key_owner(kk);
		int num_gen = Integer.parseInt(args[1]);
		File dd = new File(".");

		int num_gamal_bits = MIN_GAMAL_BITS;
		int gamal_certainty = MIN_GAMAL_CERTAINTY;

		for (int aa = 0; aa < num_gen; aa++) {
			SecureRandom rnd = own.new_SecureRandom();
			gamal_generator gg1 = new gamal_generator(num_gamal_bits,
					gamal_certainty, rnd);

			File nm = new File(dd, "gam_sys_" + aa + ".dat");
			mem_file.write_string(nm, gg1.get_public_string());

			System.out.println("generated=" + nm);
		}
	}

	public static void main(String[] args) {
		// wait_for_active_threads();

		// main_fix_gamal_generators(args);
		// main_create_gamal_generators(args);
		// main_TEST_zip_dir(args);
		// main_TEST_netpasser(args);
		// main_TEST_get_host(args);
		// main_TEST_threads(args);

		System.out.println("FINISHED TEST_netpasser");
	}

}
