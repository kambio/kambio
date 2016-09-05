package emetcode.net.netmix.locator_sys;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import emetcode.crypto.bitshake.utils.convert;
import emetcode.crypto.bitshake.utils.file_funcs;
import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.net.netmix.bad_netmix;
import emetcode.net.netmix.nx_std_coref;
import emetcode.net.netmix.nx_conn_id;
import emetcode.net.netmix.nx_dir_base;
import emetcode.net.netmix.nx_peer;
import emetcode.net.netmix.locale.L;
import emetcode.util.devel.dbg_slow;
import emetcode.util.devel.logger;
import emetcode.util.devel.thread_funcs;

public class nx_connector {

	static final boolean IN_DEBUG_1 = true; // bad set_locator
	static final boolean IN_DEBUG_2 = false; // req coref loc & supra
	static final boolean IN_DEBUG_3 = false; // repor glid
	static final boolean IN_DEBUG_4 = false; // find coref
	static final boolean IN_DEBUG_5 = false; // is remote
	static final boolean IN_DEBUG_6 = false; // locate
	static final boolean IN_DEBUG_7 = false; // supra_locate
	static final boolean IN_DEBUG_8 = false; // cannot locate
	static final boolean IN_DEBUG_9 = false; // found coref
	static final boolean IN_DEBUG_10 = false; // slow poll
	static final boolean IN_DEBUG_11 = false; // slow locate
	static final boolean IN_DEBUG_12 = false; // wait for report_glid
	static final boolean IN_DEBUG_13 = true; // print resp report_runner
	static final boolean IN_DEBUG_14 = false; // slow report_gli
	static final boolean IN_DEBUG_15 = false; // slow got find
	static final boolean IN_DEBUG_16 = false; // slow wait find cancel thds
	static final boolean IN_DEBUG_17 = false; // run finished
	static final boolean IN_DEBUG_18 = false; // slow_offer
	static final boolean IN_DEBUG_19 = true; // supra reporting
	static final boolean IN_DEBUG_20 = true; // supra locating

	public static final String NET_COREF_SEP = "$$";

	private static final int POLL_LOCATED_TIMEOUT_SECS = 20;
	private static final int OFFER_LOCATED_TIMEOUT_SECS = 15;

	static final int POLL_SUPRA_LOCATED_TIMEOUT_SECS = 20;
	static final int OFFER_SUPRA_LOCATED_TIMEOUT_SECS = 15;

	static final long TIMEOUT_MILLIS_FOR_SUPRA_LOCATING = 1000 * 20;

	private static final int MAX_LOCAT_ANSW = 5;

	private static final String REPORTED_LOCATION = "reported_location";

	private static final String NULL_LOCATION = "null_location";

	private static final String LOCATORS_FNAM = "locators.dat";
	private static final String SUPRA_LOCATORS_FNAM = "supra_locators.dat";

	private static final nx_locator GO_LOCATOR = new nx_locator(null, null,
			null);
	private static final nx_locator ABORT_LOCATOR = new nx_locator(null, null,
			null);

	static final nx_supra_locator GO_SUPRA_LOCATOR = new nx_supra_locator(null,
			null);
	static final nx_supra_locator ABORT_SUPRA_LOCATOR = new nx_supra_locator(
			null, null);
	static final nx_supra_locator NULL_SUPRA_LOCATOR = new nx_supra_locator(
			null, null);

	private static final String LOC_HOST = "localhost";
	private static final String LOC_ADDR = "127.0.0.1";

	private static String fix_localhost_case(String loc) {
		if (loc == null) {
			return null;
		}
		return loc.replace(LOC_HOST, LOC_ADDR);
	}

	private static boolean is_same_descr(String loc, String rem) {
		rem = fix_localhost_case(rem);
		return loc.equals(rem);
	}

	public static File get_local_locators_file(nx_dir_base dir_b) {
		File loc_dd = dir_b.get_local_nx_dir();
		File ltor_ff = new File(loc_dd, LOCATORS_FNAM);
		return ltor_ff;
	}

	public static File get_local_supra_locators_file(nx_dir_base dir_b) {
		File loc_dd = dir_b.get_local_nx_dir();
		File ltor_ff = new File(loc_dd, SUPRA_LOCATORS_FNAM);
		return ltor_ff;
	}

	public static File get_remote_locators_file(nx_dir_base dir_b,
			nx_conn_id the_coid) {
		File rem_dd = dir_b.get_remote_nx_dir(the_coid);
		File ltor_ff = new File(rem_dd, LOCATORS_FNAM);
		return ltor_ff;
	}

	public static File get_remote_supra_locators_file(nx_dir_base dir_b,
			nx_conn_id the_coid) {
		File rem_dd = dir_b.get_remote_nx_dir(the_coid);
		File ltor_ff = new File(rem_dd, SUPRA_LOCATORS_FNAM);
		return ltor_ff;
	}

	// remote

	private static List<String> read_remote_top_locations(nx_dir_base b_dir,
			key_owner owr, nx_conn_id the_coid) {
		File top_ff = nx_top_locations.get_remote_top_locations_file(b_dir,
				the_coid);
		nx_top_locations top_loc = new nx_top_locations();
		top_loc.read_top_loc(top_ff, owr);
		List<String> all_loc = top_loc.get_locations_list();
		return all_loc;
	}

	public static List<String> read_remote_locators(nx_dir_base dir_b,
			nx_conn_id the_coid, key_owner owr) {
		File ltor_ff = get_remote_locators_file(dir_b, the_coid);
		return file_funcs.read_list_file(ltor_ff, owr);
	}

	public static List<String> read_remote_supra_locators(nx_dir_base dir_b,
			nx_conn_id the_coid, key_owner owr) {
		File ltor_ff = get_remote_supra_locators_file(dir_b, the_coid);
		return file_funcs.read_list_file(ltor_ff, owr);
	}

	// local

	public static List<String> read_local_locators(nx_dir_base dir_b,
			key_owner owr) {
		File ff = get_local_locators_file(dir_b);
		return file_funcs.read_list_file(ff, owr);
	}

	public static void write_local_locators(nx_dir_base dir_b, key_owner owr,
			List<String> all_lcat) {
		File ff = get_local_locators_file(dir_b);
		file_funcs.write_list_file(ff, owr, all_lcat);
	}

	public static List<String> read_local_supra_locators(nx_dir_base dir_b,
			key_owner owr) {
		File ff = get_local_supra_locators_file(dir_b);
		return file_funcs.read_list_file(ff, owr);
	}

	public static void write_local_supra_locators(nx_dir_base dir_b,
			key_owner owr, List<String> all_lcat) {
		File ff = get_local_supra_locators_file(dir_b);
		file_funcs.write_list_file(ff, owr, all_lcat);
	}

	public static void add_local_supra_locator(nx_dir_base dir_b,
			key_owner owr, String sup_ltor) {
		List<String> all_ltor = read_local_supra_locators(dir_b, owr);
		if (!all_ltor.contains(sup_ltor)) {
			all_ltor.add(sup_ltor);
		}
		if (IN_DEBUG_2) {
			logger.info("ADDING_sup_ltor=" + sup_ltor);
		}
		write_local_supra_locators(dir_b, owr, all_ltor);
	}

	public static void remove_local_supra_locator(nx_dir_base dir_b,
			key_owner owr, String sup_ltor) {
		List<String> all_ltor = read_local_supra_locators(dir_b, owr);
		all_ltor.remove(sup_ltor);
		if (IN_DEBUG_2) {
			logger.info("REMOVING_sup_ltor=" + sup_ltor);
		}
		write_local_supra_locators(dir_b, owr, all_ltor);
	}

	private static void is_remote_coref_location_main(
			final BlockingQueue<String> located,
			final AtomicReference<nx_locator> cli_loc,
			final nx_locator ltor_cli, final String rq_coref,
			final String rq_service, final boolean rq_and_repo) {

		if (!ltor_cli.has_locator()) {
			throw new bad_netmix(2);
		}

		nx_location_request loc = null;
		if ((cli_loc != null) && !cli_loc.compareAndSet(GO_LOCATOR, ltor_cli)) {
			ltor_cli.cancel_client_request();
		} else {
			loc = ltor_cli.ask_service(rq_coref, rq_service, rq_and_repo);
		}

		// nx_location_request loc = req_coref_location_in_server(cli_loc,
		// ltor_cli, false, rq_coref, rq_and_repo);

		String remote_descr = ltor_cli.get_remote_description();

		String out_locat = NULL_LOCATION;

		boolean is_rem = (loc != null) && loc.is_target();
		if (is_rem) {
			// out_locat = remote_descr;
			String loc_port = loc.req_coref_str;
			if (loc_port != null) {
				out_locat = nx_peer
						.set_description_port(loc_port, remote_descr);
			} else {
				out_locat = remote_descr;
			}

			if (IN_DEBUG_5) {
				logger.debug("remote_descr='" + remote_descr + "'"
						+ " IS_REMOTE_GLID='" + rq_coref + "'" + " loc_port="
						+ loc_port);
			}
		}

		if (IN_DEBUG_5) {
			if (loc == null) {
				logger.info("CANNOT_CHECK_IF_GLID=" + rq_coref
						+ "_IS_IN_REMOTE=" + remote_descr);
			} else {
				logger.info("GOT_REMOTE loc=" + loc);
			}
		}

		offer_location(located, out_locat);
	}

	private static nx_location_request req_coref_location_in_server(
			AtomicReference<nx_locator> cli_loc, nx_locator ltor_cli,
			boolean is_loc_ltor, String rq_coref, boolean rq_and_repo) {

		dbg_slow sl1 = null;
		if (IN_DEBUG_11) {
			sl1 = new dbg_slow();
		}

		if (!ltor_cli.has_locator()) {
			throw new bad_netmix(2);
		}
		if ((cli_loc != null) && !cli_loc.compareAndSet(GO_LOCATOR, ltor_cli)) {
			ltor_cli.cancel_client_request();
			return null;
		}
		if (IN_DEBUG_6) {
			String loc_descr = ltor_cli.local_peer.get_description();
			String rem_descr = ltor_cli.remote_peer.get_description();
			logger.debug("req_coref_location_in_server. " + "locate_coref="
					+ rq_coref + " from=" + loc_descr + " in_remote_descr='"
					+ rem_descr);
		}
		nx_location_request loc = ltor_cli.ask_locate(rq_coref, rq_and_repo);

		if (is_loc_ltor) {
			nx_dir_base ltor_b_dir = ltor_cli.dir_base;
			key_owner ltor_owr = ltor_cli.owner;
			nx_conn_id ltor_coid = ltor_cli.cli_remote_coid;
			boolean was_canceled = ltor_cli.cli_canceled.get();

			if ((loc == null) && (ltor_coid != null) && !was_canceled) {
				File yy_ff = nx_coid_failures.get_remote_year_failures_file(
						ltor_b_dir, ltor_coid);
				nx_coid_failures.inc_num_failed(yy_ff, ltor_owr);
			}
			if ((loc != null) && (ltor_coid != null)) {
				File top_ff = nx_top_locations.get_remote_top_locations_file(
						ltor_b_dir, ltor_coid);

				String ltor_addr = ltor_cli.remote_peer.get_description();
				nx_top_locations.inc_num_conn_for(top_ff, ltor_owr, ltor_addr);
			}
		}

		if (IN_DEBUG_11) {
			String msg = "SLOW_LOCATE=null_loc";
			if (loc != null) {
				msg = "SLOW_LOCATE=" + loc.src_observed_nx_addr;
			}
			sl1.log_if_slow(msg);
		}
		return loc;
	}

	private static nx_locator get_locator_copy(nx_locator orig_ltor,
			String remote_descr, nx_conn_id use_coid) {

		key_owner owr = orig_ltor.owner;
		nx_dir_base b_dir = orig_ltor.dir_base;
		nx_peer local_pp = orig_ltor.local_peer;

		String rem_addr = nx_locator.set_port(remote_descr);

		return nx_locator.prepare_locator(local_pp, b_dir, owr, rem_addr,
				use_coid);
	}

	private static Runnable is_remote_coref_location_runner(
			final BlockingQueue<String> located,
			final AtomicReference<nx_locator> cli_loc,
			final nx_locator ltor_cli, final String rq_coref,
			final String rq_service, final boolean rq_and_repo) {

		if (!ltor_cli.has_locator()) {
			return null;
		}

		Runnable rr1 = new Runnable() {
			public void run() {
				is_remote_coref_location_main(located, cli_loc, ltor_cli,
						rq_coref, rq_service, rq_and_repo);
				if (IN_DEBUG_17) {
					logger.debug("run_finished");
				}
			}
		};
		return rr1;
	}

	private static Runnable report_coref_runner(
			final BlockingQueue<String> reported, final nx_locator ltor_cli,
			final int max_failures, final AtomicInteger num_failed) {

		if (!ltor_cli.has_locator()) {
			return null;
		}
		if (!ltor_cli.has_encryption_key()) {
			logger.info("REPORTING without COID !!!");
			throw new bad_netmix(2, "REPORTING without COID !!!");
			// return null;
		}

		key_owner owr = ltor_cli.owner;
		nx_std_coref loc_glid = ltor_cli.dir_base.get_local_glid(owr);
		if (loc_glid == null) {
			return null;
		}

		final String rp_coref = loc_glid.get_str();
		final boolean rq_and_repo = true;

		if (IN_DEBUG_3) {
			logger.info("REPORTING_rp_coref=\n\t" + rp_coref + "\n\tTO="
					+ ltor_cli.remote_peer.get_description() + "\n\tfrom="
					+ ltor_cli.local_peer.get_description());
		}

		Runnable rr1 = new Runnable() {
			public void run() {
				dbg_slow sl1 = null;
				if (IN_DEBUG_14) {
					sl1 = new dbg_slow();
				}

				nx_location_request loc = req_coref_location_in_server(null,
						ltor_cli, true, rp_coref, rq_and_repo);

				if (loc != null) {
					offer_location(reported, REPORTED_LOCATION);
				} else {
					if (max_failures != 0) {
						int vv = num_failed.incrementAndGet();
						if (vv == max_failures) {
							if (IN_DEBUG_19) {
								logger.info("SUPRA_REPORTING !!!!!!");
							}

							nx_dir_base b_dir = ltor_cli.dir_base;
							nx_peer local_pp = ltor_cli.local_peer;
							List<String> all_locators = read_local_supra_locators(
									b_dir, ltor_cli.owner);

							report_coref_with(b_dir, local_pp, all_locators,
									reported, true);
						}
					}
				}

				if (IN_DEBUG_13) {
					if (loc == null) {
						logger.info("report_glid FAILED!!! NULL_loc_req");
					} else {
						boolean rep_failed = false;
						if (loc.dest_reported_nx_addr == null) {
							rep_failed = true;
						} else if (loc.src_reported_nx_addr == null) {
							rep_failed = true;
						} else if (!loc.dest_reported_nx_addr
								.equals(loc.src_reported_nx_addr)) {
							rep_failed = true;
						}
						if (rep_failed) {
							logger.info("report_glid FAILED!!! loc=" + loc);
						}
					}
				}
				if (IN_DEBUG_14) {
					String msg = "SLOW_REPORT loc= null_loc";
					if (loc != null) {
						msg = "SLOW_REPORT loc=" + loc.dest_reported_nx_addr;
					}
					sl1.log_if_slow(msg);
				}
				if (IN_DEBUG_17) {
					logger.debug("run_finished");
				}
			}
		};
		return rr1;
	}

	private static void offer_location(BlockingQueue<String> loc_queue,
			String loc_str) {

		try {
			boolean no_o = loc_queue.offer(loc_str, OFFER_LOCATED_TIMEOUT_SECS,
					TimeUnit.SECONDS);
			if (IN_DEBUG_18) {
				if (!no_o) {
					logger.info("SLOW_OFFER. could not offer.");
				}
			}
		} catch (InterruptedException ex1) {
			throw new bad_netmix(2, ex1.toString());
		}
	}

	private static String poll_location(BlockingQueue<String> loc_queue) {
		dbg_slow sl1 = null;
		if (IN_DEBUG_10) {
			sl1 = new dbg_slow();
		}
		String curr_addr = null;
		try {
			curr_addr = loc_queue.poll(POLL_LOCATED_TIMEOUT_SECS,
					TimeUnit.SECONDS);
		} catch (InterruptedException ex1) {
			throw new bad_netmix(2, ex1.toString());
		}
		if (curr_addr == NULL_LOCATION) {
			curr_addr = null;
		}
		if (IN_DEBUG_10) {
			sl1.log_if_slow("SLOW_POLL=" + curr_addr);
		}
		return curr_addr;
	}

	private static void abort_locator(AtomicReference<nx_locator> loc_ref) {
		nx_locator loc = loc_ref.getAndSet(ABORT_LOCATOR);
		if ((loc != GO_LOCATOR) && (loc != ABORT_LOCATOR)) {
			loc.cancel_client_request();
		}
	}

	private static void req_coref_location_main(
			final BlockingQueue<String> located,
			final AtomicReference<nx_locator> cli_loc,
			final AtomicReference<nx_locator> repor_loc,
			final AtomicReference<nx_locator> obser_loc,
			final nx_locator ltor_cli, final String rq_coref,
			final String rq_service, final boolean rq_and_repo) {

		nx_location_request req_loc = req_coref_location_in_server(cli_loc,
				ltor_cli, true, rq_coref, rq_and_repo);

		if (req_loc == null) {
			if (IN_DEBUG_8) {
				logger.info("CANNOT_LOCATE=" + rq_coref);
			}
			offer_location(located, NULL_LOCATION);
			return;
		}

		String loc_descrip = fix_localhost_case(ltor_cli.local_peer
				.get_description());
		String base_thd_nm = Thread.currentThread().getName() + "-verif-";
		String reported_addr = req_loc.dest_reported_nx_addr;
		String observed_addr = nx_peer.set_description_port(reported_addr,
				req_loc.dest_observed_nx_addr);

		Runnable r_repor = null;
		Runnable r_obser = null;
		nx_locator ltor_1 = null;
		nx_locator ltor_2 = null;
		Thread thd_repor = null;
		Thread thd_obser = null;

		String resp_locat = NULL_LOCATION;

		if (req_loc.is_target()) {
			resp_locat = ltor_cli.get_remote_description();
		} else {

			BlockingQueue<String> resp_repor = new LinkedBlockingQueue<String>(
					MAX_LOCAT_ANSW);
			BlockingQueue<String> resp_obser = new LinkedBlockingQueue<String>(
					MAX_LOCAT_ANSW);

			nx_conn_id tgt_coid = ltor_cli.dir_base.get_coid_by_ref(rq_coref, null);

			if ((reported_addr != null)
					&& (!is_same_descr(loc_descrip, reported_addr))) {
				ltor_1 = get_locator_copy(ltor_cli, reported_addr, tgt_coid);
				r_repor = is_remote_coref_location_runner(resp_repor,
						repor_loc, ltor_1, rq_coref, rq_service, rq_and_repo);
			}

			if ((observed_addr != null)
					&& (!is_same_descr(loc_descrip, observed_addr))) {
				ltor_2 = get_locator_copy(ltor_cli, observed_addr, tgt_coid);
				r_obser = is_remote_coref_location_runner(resp_obser,
						obser_loc, ltor_2, rq_coref, rq_service, rq_and_repo);
			}

			if (r_repor != null) {
				String nm_ck_repor = base_thd_nm + "-REQ-" + reported_addr;
				thd_repor = thread_funcs.start_thread(nm_ck_repor, r_repor,
						false);
			}

			if (r_obser != null) {
				String nm_ck_obser = base_thd_nm + "-REQ-" + observed_addr;
				thd_obser = thread_funcs.start_thread(nm_ck_obser, r_obser,
						false);
			}

			boolean running_repor = (thd_repor != null);
			boolean running_obser = (thd_obser != null);

			String repor_addr = null;
			String obser_addr = null;
			if (running_repor) {
				repor_addr = poll_location(resp_repor);
			}
			if (running_obser) {
				obser_addr = poll_location(resp_obser);
			}
			String best_addr = obser_addr;
			if (repor_addr != null) {
				best_addr = repor_addr;
			}

			if (thd_repor != null) {
				abort_locator(repor_loc);
			}
			if (thd_obser != null) {
				abort_locator(repor_loc);
			}

			if (thd_repor != null) {
				thread_funcs.wait_for_thread(thd_repor);
			}
			if (thd_obser != null) {
				thread_funcs.wait_for_thread(thd_obser);
			}

			if (best_addr != null) {
				resp_locat = best_addr;
			}
		}

		if (resp_locat == null) {
			throw new bad_netmix(2, "null_offer");
		}

		offer_location(located, resp_locat);
	}

	private static Runnable req_coref_location_runner(
			final BlockingQueue<String> located,
			final AtomicReference<nx_locator> cli_loc,
			final AtomicReference<nx_locator> repor_loc,
			final AtomicReference<nx_locator> obser_loc,
			final nx_locator ltor_cli, final String rq_coref,
			final String rq_service, final boolean rq_and_repo) {

		if (IN_DEBUG_2) {
			logger.debug("req_coref_location_runner " + " rq_coref=" + rq_coref);
		}

		if (!ltor_cli.has_locator()) {
			return null;
		}

		Runnable rr1 = new Runnable() {
			public void run() {
				req_coref_location_main(located, cli_loc, repor_loc, obser_loc,
						ltor_cli, rq_coref, rq_service, rq_and_repo);
				if (IN_DEBUG_17) {
					logger.debug("run_finished");
				}
			}
		};
		return rr1;
	}

	private static String find_coref_location_with(nx_dir_base b_dir,
			nx_peer local_pp, String rq_coref, String rq_service,
			List<String> all_top_loc, List<String> all_locators,
			boolean rq_and_repo) {

		if (IN_DEBUG_4) {
			logger.info("find_coref_location_wit.  rq_coref=" + rq_coref);
		}

		key_owner owr = local_pp.get_owner();
		String loc_descrip = fix_localhost_case(local_pp.get_description());

		int loted_sz = all_top_loc.size() + all_locators.size() + 1;

		BlockingQueue<String> located = new LinkedBlockingQueue<String>(
				loted_sz);
		List<AtomicReference<nx_locator>> all_closers = new ArrayList<AtomicReference<nx_locator>>();
		List<Thread> all_thd = new ArrayList<Thread>();

		String curr_thd_nm = Thread.currentThread().getName();
		String ck_old_locat_thd_nm = curr_thd_nm + "-old-coref-in-";

		if (IN_DEBUG_4) {
			logger.info("\n\n\nfind_coref_location. all_top_loc=");
			logger.info(all_top_loc);
		}

		int num_lc = 0;
		for (String rem_addre : all_top_loc) {
			String rem_addr_locat = nx_locator.set_port(rem_addre);
			if (is_same_descr(loc_descrip, rem_addr_locat)) {
				continue;
			}

			AtomicReference<nx_locator> closer_gli = new AtomicReference<nx_locator>(
					GO_LOCATOR);
			all_closers.add(closer_gli);

			nx_conn_id tgt_coid = b_dir.get_coid_by_ref(rq_coref, null);

			nx_locator ltor_1 = nx_locator.prepare_locator(local_pp, b_dir,
					owr, rem_addr_locat, tgt_coid);

			Runnable is_gli = is_remote_coref_location_runner(located,
					closer_gli, ltor_1, rq_coref, rq_service, rq_and_repo);
			String nm_thd_1 = ck_old_locat_thd_nm + "-REQ-" + num_lc + "-"
					+ rem_addr_locat;

			if (is_gli != null) {
				Thread thd_gli = thread_funcs.start_thread(nm_thd_1, is_gli,
						false);
				all_thd.add(thd_gli);
			}
			num_lc++;
		}

		if (IN_DEBUG_4) {
			logger.info("find_coref_location. ALL_LOCATORS_sz="
					+ all_locators.size());
			logger.info(all_locators);
		}

		String ck_ltors_thd_nm = curr_thd_nm + "-req-locat-in-";

		int num_l = 0;
		for (String rem_addre : all_locators) {
			String rem_addr_ltor = nx_locator.set_port(rem_addre);
			if (is_same_descr(loc_descrip, rem_addr_ltor)) {
				continue;
			}
			AtomicReference<nx_locator> cli_closer = new AtomicReference<nx_locator>(
					GO_LOCATOR);
			all_closers.add(cli_closer);
			AtomicReference<nx_locator> repor_loc = new AtomicReference<nx_locator>(
					GO_LOCATOR);
			all_closers.add(repor_loc);
			AtomicReference<nx_locator> obser_loc = new AtomicReference<nx_locator>(
					GO_LOCATOR);
			all_closers.add(obser_loc);

			nx_locator ltor_2 = nx_locator.prepare_locator(local_pp, b_dir,
					owr, rem_addr_ltor, null);

			Runnable req_gli = req_coref_location_runner(located, cli_closer,
					repor_loc, obser_loc, ltor_2, rq_coref, rq_service,
					rq_and_repo);

			String nm_thd = ck_ltors_thd_nm + "-REQ-" + num_l + "-"
					+ rem_addr_ltor;
			if (req_gli != null) {
				Thread thd_gli = thread_funcs.start_thread(nm_thd, req_gli,
						false);
				all_thd.add(thd_gli);
			}
			num_l++;
		}

		dbg_slow sl1 = null;
		if (IN_DEBUG_15) {
			sl1 = new dbg_slow();
		}

		String curr_addr = null;
		while (all_thd.size() > 0) {
			curr_addr = poll_location(located);
			if (curr_addr != null) {
				break;
			}
			all_thd = thread_funcs.get_alive(all_thd);
		}

		if (IN_DEBUG_15) {
			sl1.log_if_slow("SLOW_GOT_FIND=" + curr_addr);
		}

		if (IN_DEBUG_9) {
			logger.info("find_coref_location. FOUND curr_addr=" + curr_addr);
		}

		for (AtomicReference<nx_locator> ref : all_closers) {
			abort_locator(ref);
		}

		dbg_slow sl2 = null;
		if (IN_DEBUG_16) {
			sl2 = new dbg_slow();
		}
		thread_funcs.wait_for_threads(all_thd);
		if (IN_DEBUG_16) {
			sl2.log_if_slow("SLOW_WAIT_FIND_THDS=" + curr_addr);
		}

		if (IN_DEBUG_9) {
			logger.info("find_coref_location. threads finished. FOUND curr_addr="
					+ curr_addr);
		}

		return curr_addr;
	}

	private static String[] split_net_coref(String rq_coref) {
		String[] out = { "", rq_coref };
		int sep_idx = rq_coref.lastIndexOf(NET_COREF_SEP);
		int co_idx = sep_idx + NET_COREF_SEP.length();
		boolean has_co = (co_idx < rq_coref.length());
		if ((sep_idx != -1) && has_co) {
			out[0] = rq_coref.substring(0, sep_idx);
			out[1] = rq_coref.substring(co_idx);
		}
		return out;
	}

	private static nx_conn_id get_coid_for_report(nx_dir_base b_dir,
			nx_peer local_pp, String rem_addr) {

		nx_conn_id repo_coid = b_dir.get_coid_by_ref(rem_addr, null);

		if (repo_coid == null) {
			nx_supra_locator loc_cli = new nx_supra_locator(b_dir, local_pp);
			loc_cli.set_supra_locator(rem_addr);
			repo_coid = loc_cli.supra_locate(null);

			if(repo_coid == null){
				throw new bad_netmix(2);
			}
			b_dir.write_coref(rem_addr, repo_coid);
			nx_conn_id n_coid = b_dir.get_coid_by_ref(rem_addr, null);
			if(n_coid == null){
				throw new bad_netmix(2);
			}
			if(! n_coid.equals(repo_coid)){
				throw new bad_netmix(2);
			}
		}

		return repo_coid;
	}

	private static List<Thread> report_coref_with(nx_dir_base b_dir,
			nx_peer local_pp, List<String> all_locators,
			BlockingQueue<String> reported, boolean is_supra_report) {

		key_owner owr = local_pp.get_owner();

		AtomicInteger num_failed_rep = new AtomicInteger(0);
		// int max_failures = (all_locators.size() / 2) + 1;
		int max_failures = all_locators.size();
		if (is_supra_report) {
			max_failures = 0;
		}

		List<Thread> all_thd = new ArrayList<Thread>();

		String base_thd_nm = Thread.currentThread().getName()
				+ "-report-glid-in-";
		for (String rem_addr : all_locators) {
			String rem_addr_locat = nx_locator.set_port(rem_addr);

			nx_conn_id repo_coid = get_coid_for_report(b_dir, local_pp,
					rem_addr_locat);

			nx_locator ltor_1 = nx_locator.prepare_locator(local_pp, b_dir,
					owr, rem_addr_locat, repo_coid);

			Runnable is_gli = report_coref_runner(reported, ltor_1,
					max_failures, num_failed_rep);
			String nm_thd_1 = base_thd_nm + "-REQ-" + rem_addr_locat;

			if (is_gli != null) {
				Thread thd_gli = thread_funcs.start_thread(nm_thd_1, is_gli,
						false);
				all_thd.add(thd_gli);
			}
		}

		return all_thd;
	}

	public static List<Thread> report_coref(nx_dir_base b_dir, nx_peer local_pp) {
		key_owner owr = local_pp.get_owner();

		List<String> all_locators = read_local_locators(b_dir, owr);

		boolean is_supra_report = false;

		if (all_locators.isEmpty()) {
			all_locators = read_local_supra_locators(b_dir, owr);
			is_supra_report = true;
		}

		if (all_locators.isEmpty()) {
			throw new bad_netmix(2, L.add_supra_locator_first + "\n\t dir="
					+ b_dir);
		}

		int loted_sz = all_locators.size() + 1;
		BlockingQueue<String> reported = new LinkedBlockingQueue<String>(
				loted_sz);

		List<Thread> all_thd = report_coref_with(b_dir, local_pp, all_locators,
				reported, is_supra_report);

		poll_location(reported);

		if (IN_DEBUG_12) {
			thread_funcs.wait_for_threads(all_thd);
			logger.info("reported_glid num_thds=" + all_thd.size() + " b_dir"
					+ b_dir);
		}

		return all_thd;
	}

	public static boolean ping_request(nx_dir_base b_dir, nx_peer local_pp,
			String rem_addre, String rq_coref, boolean rq_and_repo) {

		key_owner owr = local_pp.get_owner();
		String loc_descrip = fix_localhost_case(local_pp.get_description());
		String rem_addr_locat = nx_locator.set_port(rem_addre);

		if (is_same_descr(loc_descrip, rem_addr_locat)) {
			return true;
		}

		AtomicReference<nx_locator> closer_gli = new AtomicReference<nx_locator>(
				GO_LOCATOR);

		nx_conn_id tgt_coid = b_dir.get_coid_by_ref(rq_coref, null);

		nx_locator ltor_1 = nx_locator.prepare_locator(local_pp, b_dir, owr,
				rem_addr_locat, tgt_coid);

		if (!ltor_1.has_locator()) {
			return false;
		}

		nx_location_request loc = req_coref_location_in_server(closer_gli,
				ltor_1, false, rq_coref, rq_and_repo);

		boolean is_rem = (loc != null) && loc.is_target();
		return is_rem;
	}

	public static String find_coref_location(nx_dir_base b_dir,
			nx_peer local_pp, String rq_coref) {
		return find_coref_location(b_dir, local_pp, rq_coref, null, true);
	}

	public static String find_coref_location(nx_dir_base b_dir,
			nx_peer local_pp, String rq_coref, String rq_service,
			boolean rq_and_repo) {

		if (IN_DEBUG_4) {
			logger.info("Starting find_coref_location  local_pp="
					+ local_pp.get_description() + " rq_coref=" + rq_coref);
		}

		key_owner owr = local_pp.get_owner();

		List<String> all_top_loc = new ArrayList<String>();
		List<String> all_locators = new ArrayList<String>();
		List<String> all_supra_locators = new ArrayList<String>();

		nx_conn_id rq_coid = b_dir.get_coid_by_ref(rq_coref, null);
		if (rq_coid == null) {
			if (IN_DEBUG_4) {
				logger.info("find_coref_location. (rq_coid == null). b_dir="
						+ b_dir);
			}
			all_supra_locators = read_local_supra_locators(b_dir, owr);
			String[] ss = split_net_coref(rq_coref);
			if ((ss[0].length() > 0) && (ss[1].length() > 0)) {
				all_locators.add(ss[0]);
				all_supra_locators.add(ss[0]);
				rq_coref = ss[1];
			}
		} else {
			if (IN_DEBUG_4) {
				logger.info("find_coref_location. rq_coid=\n\t" + rq_coid
						+ "\n\tb_dir=" + b_dir);
			}
			all_top_loc = read_remote_top_locations(b_dir, owr, rq_coid);
			all_locators = read_remote_locators(b_dir, rq_coid, owr);
			all_supra_locators = read_remote_supra_locators(b_dir, rq_coid, owr);

			if (all_supra_locators.isEmpty()) {
				all_supra_locators = read_local_supra_locators(b_dir, owr);
			}
		}

		if (all_locators.isEmpty()) {
			all_locators = all_supra_locators;
			all_supra_locators = new ArrayList<String>();
		}

		if (all_locators.isEmpty()) {
			throw new bad_netmix(2, String.format(L.no_locators_found, b_dir));
		}

		String resp = find_coref_location_with(b_dir, local_pp, rq_coref,
				rq_service, all_top_loc, all_locators, rq_and_repo);

		if (resp == null) {
			all_top_loc.clear();
			resp = find_coref_location_with(b_dir, local_pp, rq_coref,
					rq_service, all_top_loc, all_supra_locators, rq_and_repo);
		}

		if (resp == null) {
			if (IN_DEBUG_20) {
				logger.info("SUPRA_LOCATING!! rq_coref=" + rq_coref);
			}
			resp = find_coref_location_with_supra_locators(b_dir, local_pp,
					rq_coref, rq_service, all_locators, rq_and_repo);
		}

		if ((resp == null) && (rq_coid != null)) {
			File yy_ff = nx_coid_failures.get_remote_year_failures_file(b_dir,
					rq_coid);
			nx_coid_failures.inc_num_failed(yy_ff, owr);
		}
		if ((resp != null) && (rq_coid != null)) {
			File top_ff = nx_top_locations.get_remote_top_locations_file(b_dir,
					rq_coid);
			nx_top_locations.inc_num_conn_for(top_ff, owr, resp);
		}

		return resp;
	}

	// all_supra_funcs

	public static void report_to_supra_locators(nx_dir_base b_dir,
			nx_peer local_pp, boolean rq_and_repo) {
		find_coref_location_with_supra_locators(b_dir, local_pp, null, null,
				null, rq_and_repo);
	}

	private static String find_coref_location_with_supra_locators(
			nx_dir_base b_dir, nx_peer local_pp, String rq_coref,
			String rq_service, List<String> all_locators, boolean rq_and_repo) {

		if (IN_DEBUG_20) {
			logger.info("find_coref_location_with_supra_locators. rq_coref="
					+ rq_coref);
		}

		key_owner owr = local_pp.get_owner();
		String loc_descrip = fix_localhost_case(local_pp.get_description());

		List<String> all_supra_locators = read_local_supra_locators(b_dir, owr);

		String curr_thd_nm = Thread.currentThread().getName();
		String ck_ltors_thd_nm = curr_thd_nm + "-supra-locat-in-";

		List<AtomicReference<nx_supra_locator>> all_closers = new ArrayList<AtomicReference<nx_supra_locator>>();
		List<Thread> all_thd = new ArrayList<Thread>();

		int loted_sz = all_supra_locators.size();

		BlockingQueue<nx_supra_locator> located = new LinkedBlockingQueue<nx_supra_locator>(
				loted_sz);

		int num_l = 0;
		for (String rem_addre : all_supra_locators) {
			String rem_addr_su_ltor = nx_supra_locator.set_port(rem_addre);
			if (is_same_descr(loc_descrip, rem_addr_su_ltor)) {
				continue;
			}
			AtomicReference<nx_supra_locator> cli_closer = new AtomicReference<nx_supra_locator>(
					GO_SUPRA_LOCATOR);
			all_closers.add(cli_closer);

			nx_supra_locator su_ltor_2 = prepare_supra_locator(local_pp, b_dir,
					rem_addr_su_ltor);

			Runnable req_gli = req_coref_supra_location_runner(located,
					cli_closer, su_ltor_2, rq_coref);

			String nm_thd = ck_ltors_thd_nm + "-CNN-" + num_l + "-"
					+ rem_addr_su_ltor;
			if (req_gli != null) {
				Thread thd_gli = thread_funcs.start_thread(nm_thd, req_gli,
						false);
				all_thd.add(thd_gli);
			}
			num_l++;
		}

		List<String> all_top_loc = new ArrayList<String>();
		String resp = null;

		long st_tm = System.currentTimeMillis();
		while (all_thd.size() > 0) {
			nx_supra_locator su_loc = poll_supra_locator(located);
			if (su_loc != null) {
				List<String> sel_locators = convert.diff_string_lists(
						su_loc.all_locators, all_locators);

				resp = find_coref_location_with(b_dir, local_pp, rq_coref,
						rq_service, all_top_loc, sel_locators, rq_and_repo);
				if (resp != null) {
					break;
				}
			}
			if (elapsed_time_under(st_tm, TIMEOUT_MILLIS_FOR_SUPRA_LOCATING)) {
				break;
			}
			all_thd = thread_funcs.get_alive(all_thd);
		}

		for (AtomicReference<nx_supra_locator> ref : all_closers) {
			abort_supra_locator(ref);
		}
		thread_funcs.wait_for_threads(all_thd);

		return resp;
	}

	private static boolean elapsed_time_under(long st_tm, long tm_out_millis) {
		long curr_tm = System.currentTimeMillis();
		long diff_tm = curr_tm - st_tm;
		if (diff_tm < tm_out_millis) {
			return true;
		}
		return false;
	}

	private static void offer_supra_locator(
			BlockingQueue<nx_supra_locator> loc_queue, nx_supra_locator su_loc) {
		try {
			loc_queue.offer(su_loc, OFFER_SUPRA_LOCATED_TIMEOUT_SECS,
					TimeUnit.SECONDS);
		} catch (InterruptedException ex1) {
			throw new bad_netmix(2, ex1.toString());
		}
	}

	private static void abort_supra_locator(
			AtomicReference<nx_supra_locator> loc_ref) {
		nx_supra_locator loc = loc_ref.getAndSet(ABORT_SUPRA_LOCATOR);
		if ((loc != GO_SUPRA_LOCATOR) && (loc != ABORT_SUPRA_LOCATOR)) {
			loc.cancel_client_connection();
		}
	}

	private static nx_supra_locator poll_supra_locator(
			BlockingQueue<nx_supra_locator> loc_queue) {
		nx_supra_locator su_loc = null;
		try {
			su_loc = loc_queue.poll(POLL_SUPRA_LOCATED_TIMEOUT_SECS,
					TimeUnit.SECONDS);
		} catch (InterruptedException ex1) {
			throw new bad_netmix(2, ex1.toString());
		}
		if (su_loc == NULL_SUPRA_LOCATOR) {
			su_loc = null;
		}
		return su_loc;
	}

	private static nx_supra_locator prepare_supra_locator(nx_peer local_pp,
			nx_dir_base b_dir, String remote_descr) {

		nx_supra_locator ltor_cli = new nx_supra_locator(b_dir, local_pp);
		ltor_cli.set_supra_locator(remote_descr);
		if (!ltor_cli.has_supra_locator()) {
			if (IN_DEBUG_1) {
				logger.debug("prepare_supra_locator."
						+ "COULD_NOT_SET_SUPRALOCATOR=" + remote_descr
						+ ") from=" + b_dir + " local_pp="
						+ local_pp.get_description());
			}
		}
		return ltor_cli;
	}

	private static Runnable req_coref_supra_location_runner(
			final BlockingQueue<nx_supra_locator> located,
			final AtomicReference<nx_supra_locator> cli_loc,
			final nx_supra_locator ltor_cli, final String rq_coref) {

		if (IN_DEBUG_2) {
			logger.debug("req_coref_supra_location_runner. " + " rq_coref="
					+ rq_coref);
		}

		if (!ltor_cli.has_supra_locator()) {
			return null;
		}

		Runnable rr1 = new Runnable() {
			public void run() {
				req_coref_supra_location_main(located, cli_loc, ltor_cli,
						rq_coref);
				if (IN_DEBUG_17) {
					logger.debug("run_finished");
				}
			}
		};
		return rr1;
	}

	private static void req_coref_supra_location_main(
			final BlockingQueue<nx_supra_locator> sulocated,
			final AtomicReference<nx_supra_locator> cli_suloc,
			final nx_supra_locator sultor_cli, final String rq_coref) {

		if (!sultor_cli.has_supra_locator()) {
			throw new bad_netmix(2);
		}
		if (!cli_suloc.compareAndSet(GO_SUPRA_LOCATOR, sultor_cli)) {
			sultor_cli.cancel_client_connection();
		}

		if (IN_DEBUG_7) {
			String loc_descr = sultor_cli.local_peer.get_description();
			String rem_descr = sultor_cli.remote_peer.get_description();
			logger.debug("req_coref_supra_location_main. " + "supra_locate="
					+ rq_coref + " from=" + loc_descr + " in_remote_descr='"
					+ rem_descr);
		}
		sultor_cli.supra_locate(rq_coref);

		if (!sultor_cli.all_locators.isEmpty()) {
			offer_supra_locator(sulocated, sultor_cli);
		} else {
			offer_supra_locator(sulocated, NULL_SUPRA_LOCATOR);
		}
	}

}
