package emetcode.net.netmix.locator_sys;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import emetcode.crypto.bitshake.utils.convert;
import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.net.netmix.bad_netmix;
import emetcode.net.netmix.config;
import emetcode.net.netmix.nx_conn_id;
import emetcode.net.netmix.nx_dir_base;
import emetcode.net.netmix.nx_peer;
import emetcode.net.netmix.nx_protector;
import emetcode.util.devel.logger;

public class nx_location_stats {
	private static final boolean IN_DEBUG_1 = true; // null last addr
	private static final boolean IN_DEBUG_2 = false; // calc_locat all_locat
	private static final boolean IN_DEBUG_3 = false; // get_stats
	private static final boolean IN_DEBUG_4 = false; // add_stats
	private static final boolean IN_DEBUG_5 = false; // readlin after each calc
	private static final boolean IN_DEBUG_6 = false; // cmp stats
	private static final boolean IN_DEBUG_7 = true; // dbg only good locators

	private static final int NUM_LOCATORS = 5;
	private static final int MAX_ADDR_LOCATOR = 2;

	private nx_conn_id coid;
	private String last_addr;
	private boolean is_old;
	private long num_conn;
	private long min_first_conn_tm;
	private long max_last_conn_tm;
	private long num_addr;
	private long num_year_failures;
	private long last_failure_tm;

	private nx_location_stats() {
		init_nx_location_stats();
	}

	private void init_nx_location_stats() {
		coid = null;
		last_addr = null;
		is_old = false;
		num_conn = 0;
		min_first_conn_tm = 0;
		max_last_conn_tm = 0;
		num_addr = 0;
		num_year_failures = 0;
		last_failure_tm = 0;
	}

	private void add_location(nx_top_location_info loc) {
		num_conn += loc.num_conn;
		if ((min_first_conn_tm == 0)
				|| (loc.time_first_conn < min_first_conn_tm)) {
			min_first_conn_tm = loc.time_first_conn;
		}
		if ((max_last_conn_tm == 0) || (loc.time_last_conn > max_last_conn_tm)) {
			max_last_conn_tm = loc.time_last_conn;
			last_addr = loc.addr;
		}
		num_addr++;
	}

	private static void add_failures(File ff, key_owner owr,
			nx_location_stats rr) {
		if (rr.is_old) {
			return;
		}
		nx_coid_failures all_fail = new nx_coid_failures();
		all_fail.read_failures(ff, owr);

		rr.last_failure_tm = all_fail.get_last_failure_tm();
		rr.num_year_failures = all_fail.calc_tot_failures();
	}

	private int calc_failure_rate() {
		int fa = 10;
		if (num_year_failures < 0) {
			return fa;
		}
		if (num_conn < 0) {
			return fa;
		}
		if (num_conn != 0) {
			double rr = (((double) num_year_failures) / ((double) num_conn));
			rr *= 10;
			fa = (int) rr;
		}
		return fa;
	}

	private static int cmp_location_stats(nx_location_stats rr1,
			nx_location_stats rr2) {
		int cc = cmp_location_stats_work(rr1, rr2);
		if (IN_DEBUG_6) {
			String resp = "======================";
			if (cc < 0) {
				resp = "<<<<<<<<<<<<<<<<<<<<<<";
			} else if (cc > 0) {
				resp = ">>>>>>>>>>>>>>>>>>>>>>";
			}
			logger.info("\n" + rr1 + "\n\t" + resp + "\n" + rr2
					+ "\n_________________");
		}
		return cc;
	}

	private static int cmp_location_stats_work(nx_location_stats rr1,
			nx_location_stats rr2) {

		if (rr1.is_old && !rr2.is_old) {
			return 1;
		}
		if (!rr1.is_old && rr2.is_old) {
			return -1;
		}

		int c1 = convert.cmp_int(rr1.calc_failure_rate(), rr2.calc_failure_rate());
		if (c1 != 0) {
			return -c1;
		}

		int c2 = convert.cmp_long(rr1.num_addr, rr2.num_addr);
		if (c2 != 0) {
			return -c2;
		}

		int c3 = convert.cmp_long(rr1.num_conn, rr2.num_conn);
		if (c3 != 0) {
			return c3;
		}

		int c4 = convert.cmp_long(rr1.last_failure_tm, rr2.last_failure_tm);
		if (c4 != 0) {
			return -c4;
		}

		int c5 = convert.cmp_long(rr1.min_first_conn_tm, rr2.min_first_conn_tm);
		if (c5 != 0) {
			return -c5;
		}

		int c6 = convert.cmp_long(rr1.max_last_conn_tm, rr2.max_last_conn_tm);
		if (c6 != 0) {
			return -c6;
		}

		return 0;
	}

	private static boolean is_old_location(long curr_tm, long loc_tm) {
		long diff = curr_tm - loc_tm;
		boolean is_old = (diff > config.YEAR_MILLIS);
		return is_old;
	}

	private static nx_location_stats calc_locations_stats_for(File ff,
			key_owner owr, long curr_tm) {

		nx_top_locations all_loc = new nx_top_locations();
		all_loc.read_top_loc(ff, owr);

		nx_location_stats old_rr = new nx_location_stats();
		old_rr.is_old = true;
		nx_location_stats rr = new nx_location_stats();
		rr.is_old = false;

		for (Map.Entry<String, nx_top_location_info> map_val : all_loc.all_top
				.entrySet()) {
			nx_top_location_info loc = map_val.getValue();
			boolean is_old = is_old_location(curr_tm, loc.time_last_conn);
			if (is_old) {
				old_rr.add_location(loc);
			} else {
				rr.add_location(loc);
			}
		}

		long num_cnn = rr.num_conn;
		if ((num_cnn == 0) && (old_rr.last_addr != null)) {
			rr = old_rr;
		}
		if (IN_DEBUG_2) {
			logger.info("calc_locat. num_conn=" + num_cnn + " top_loc=");
			logger.info(all_loc.get_print_list());
		}
		if (IN_DEBUG_1) {
			if ((rr.last_addr == null) && (all_loc.all_top.size() > 0)) {
				logger.info("calc_locat. null_addr for rr=" + rr);
				logger.info("calc_locat. num_conn=" + num_cnn + " top_loc=");
				logger.info(all_loc.get_print_list());
				logger.info("select_locators. RETURN_TO_CONTINUE");
				System.console().readLine();
			}
		}
		return rr;
	}

	private static nx_location_stats get_stats(nx_dir_base dir_b,
			nx_conn_id the_coid, key_owner owr, long curr_tm) {

		File top_ff = nx_top_locations.get_remote_top_locations_file(dir_b,
				the_coid);
		File yy_ff = nx_coid_failures.get_remote_year_failures_file(dir_b,
				the_coid);

		nx_location_stats rr = calc_locations_stats_for(top_ff, owr, curr_tm);
		add_failures(yy_ff, owr, rr);
		rr.coid = the_coid;

		if (IN_DEBUG_3) {
			logger.info("get_stats. after failures. \n\tFILE='" + top_ff
					+ "' \n\tstats=" + rr);
		}
		return rr;
	}

	private static boolean add_stats(List<nx_location_stats> top_rr,
			nx_location_stats the_rr) {

		boolean added = false;
		for (int aa = 0; aa < top_rr.size(); aa++) {
			nx_location_stats t_rr = top_rr.get(aa);
			if (cmp_location_stats(t_rr, the_rr) < 0) {
				nx_location_stats old_rr = top_rr.remove(aa);
				if (old_rr != t_rr) {
					throw new bad_netmix(2);
				}
				top_rr.add(the_rr);
				added = true;
				if (IN_DEBUG_4) {
					logger.info("add_stats. change " + t_rr.last_addr + " FOR "
							+ the_rr.last_addr);
				}
				break;
			}
		}
		return added;
	}

	private static List<nx_location_stats> select_locators(nx_dir_base dir_b,
			key_owner owr) {

		List<String> all_supra = nx_connector.read_local_supra_locators(
				dir_b, owr);
		long curr_tm = System.currentTimeMillis();
		List<nx_location_stats> top_rr = new ArrayList<nx_location_stats>();

		List<File> all_coid_ff = nx_protector.find_all_coid_files(dir_b);
		for (File co_ff : all_coid_ff) {
			nx_conn_id the_coid = nx_protector.get_coid_from_coid_file(co_ff);
			nx_location_stats rr = get_stats(dir_b, the_coid, owr, curr_tm);

			if (rr.last_addr == null) {
				continue;
			}
			if (all_supra.contains(rr.last_addr)) {
				continue;
			}

			if (top_rr.size() < NUM_LOCATORS) {
				top_rr.add(rr);
				if (IN_DEBUG_4) {
					logger.info("add_stats. ADD " + rr.last_addr);
				}
			} else {
				add_stats(top_rr, rr);
			}

			if (IN_DEBUG_5) {
				logger.info("select_locators. RETURN_TO_CONTINUE");
				System.console().readLine();
			}
		}

		return top_rr;
	}

	private static boolean dbg_is_good_locator(nx_location_stats rr) {
		boolean is_good = false;
		if (rr.num_addr < MAX_ADDR_LOCATOR) {
			is_good = true;
		}
		return is_good;
	}

	private static List<String> select_locators_addr(nx_dir_base dir_b,
			List<nx_location_stats> all_rr) {
		List<String> all_addr = new ArrayList<String>();
		for (nx_location_stats rr : all_rr) {
			String the_addr = rr.last_addr;
			if (the_addr == null) {
				throw new bad_netmix(2);
			}
			if (IN_DEBUG_7) {
				if (!dbg_is_good_locator(rr)) {
					continue;
				}
			}
			all_addr.add(the_addr);
		}
		return all_addr;
	}

	private static void remove_corefs(nx_dir_base dir_b, List<String> old_addr,
			List<String> new_addr) {

		List<String> diff_addr = new ArrayList<String>();
		diff_addr.addAll(old_addr);
		diff_addr.removeAll(new_addr);

		for (String addr : diff_addr) {
			dir_b.delete_coref(addr);
		}

	}

	private static void add_corefs(nx_dir_base dir_b,
			List<nx_location_stats> top_rr) {
		for (nx_location_stats rr : top_rr) {
			dir_b.write_coref(rr.last_addr, rr.coid);
		}
	}

	// THIS METHOD CAN BE TIME CONSUMING RUN IN A THREAD
	public static void update_locators(nx_dir_base dir_b, nx_peer local_pp, boolean rq_and_repo) {
		if (local_pp == null) {
			throw new bad_netmix(2);
		}

		key_owner owr = local_pp.get_owner();

		List<nx_location_stats> sel_loc = select_locators(dir_b, owr);

		List<String> new_locators = select_locators_addr(dir_b, sel_loc);
		List<String> old_locators = nx_connector.read_local_locators(
				dir_b, owr);

		Set<String> new_loc = new TreeSet<String>();
		new_loc.addAll(new_locators);

		Set<String> old_loc = new TreeSet<String>();
		old_loc.addAll(old_locators);

		if (!new_loc.equals(old_loc)) {
			remove_corefs(dir_b, old_locators, new_locators);
			add_corefs(dir_b, sel_loc);
			nx_connector.write_local_locators(dir_b, owr, new_locators);
		}

		nx_connector.report_to_supra_locators(dir_b, local_pp, rq_and_repo);
	}

	public String toString() {
		String loc_updt = "" + "\n\t coid=" + coid + "\n\t last_addr="
				+ last_addr + "\n\t is_old=" + is_old
				+ "\n\t num_year_failures=" + num_year_failures
				+ "\n\t failure_rate=" + calc_failure_rate() + "\n\t num_addr="
				+ num_addr + "\n\t num_conn=" + num_conn
				+ "\n\t last_failure_tm="
				+ convert.utc_to_string(last_failure_tm)
				+ "\n\t min_first_conn_tm="
				+ convert.utc_to_string(min_first_conn_tm)
				+ "\n\t max_last_conn_tm="
				+ convert.utc_to_string(max_last_conn_tm);
		return loc_updt;
	}
}
