package emetcode.economics.passet;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.net.netmix.nx_conn_id;
import emetcode.net.netmix.nx_protector;
import emetcode.util.devel.logger;

public class channel implements Comparable<channel> {

	public static final boolean IN_DEBUG_1 = false;
	public static final boolean IN_DEBUG_2 = true; // read channel
	public static final boolean IN_DEBUG_3 = true; // null channel

	public static final int INVALID_FILTER = 800;
	public static final int BY_NAME_FILTER = 802;
	public static final int BY_COID_FILTER = 804;
	public static final int BY_PEER_FILTER = 805;

	public static final int INVALID_KIND = 701;
	public static final int ACCOUNT_KIND = 702;
	public static final int USER_KIND = 703;

	public nx_conn_id coid;

	public int chn_kind;

	public tag_person trader;
	public File img_file;

	File cn_mikid_dir;

	private channel(File mikid_dd, File coids_dd, nx_conn_id the_coid,
			key_owner owr) {
		init_channel();
		if (mikid_dd != null) {
			init_user_channel(mikid_dd, coids_dd, the_coid, owr);
		}
	}

	private void init_user_channel(File the_mikid_dd, File coids_dd,
			nx_conn_id the_coid, key_owner owr) {
		coid = the_coid;

		cn_mikid_dir = the_mikid_dd;

		chn_kind = USER_KIND;

		img_file = paccount.get_user_image_file_in(cn_mikid_dir);

		if (owr != null) {
			trader = paccount.read_user_info_in(cn_mikid_dir, owr);
		} else {
			trader = paccount.read_user_txt_info_in(cn_mikid_dir);
		}
	}

	private void init_channel() {
		coid = null;

		trader = null;
		img_file = null;

		cn_mikid_dir = null;
	}

	public int compareTo(channel ch) {
		if (trader == null) {
			throw new bad_passet(2);
		}
		if (ch == null) {
			throw new bad_passet(2);
		}
		if (ch.trader == null) {
			throw new bad_passet(2);
		}
		String nm1 = trader.legal_name;
		String nm2 = ch.trader.legal_name;
		return nm1.compareTo(nm2);
	}

	private static channel read_channel_from_coid_file(paccount pcc,
			File co_ff, key_owner owr) {
		nx_conn_id n_coid = nx_protector.get_coid_from_coid_file(co_ff);
		return read_channel(pcc, owr, n_coid);
	}

	public static channel read_channel(paccount pcc, key_owner owr,
			nx_conn_id coid) {
		File co_ff = pcc.get_dir_base().get_coid_file(coid);

		File ch_dir = co_ff.getParentFile();
		if ((ch_dir == null) || !ch_dir.isDirectory()) {
			if(IN_DEBUG_2){
				String stk = logger.get_stack_str();
				logger.info("read_channel. BAD ch_dir=\n" + ch_dir + " \n" + stk);
			}
			return null;
		}
		String nm = paccount.get_mikid_dir_nm(owr.get_mikid());
		File mikid_dd = new File(ch_dir, nm);
		if (!mikid_dd.isDirectory()) {
			if(IN_DEBUG_2){
				logger.info("read_channel. null mikid_dir");
			}
			return null;
		}

		File coids_dd = pcc.get_dir_base().get_all_coids_base_dir();
		channel chn = new channel(mikid_dd, coids_dd, coid, owr);
		return chn;
	}

	public static List<channel> read_all_channels(paccount pcc, key_owner owr) {
		return read_all_channels(pcc, owr, -1, null);
	}

	public static List<channel> read_all_channels(paccount pcc, key_owner owr,
			int filter_kind, String fl_str) {
		File dir = pcc.get_dir_base().get_all_coids_base_dir();
		List<channel> all_chns = new ArrayList<channel>();
		if (!dir.isDirectory()) {
			return all_chns;
		}

		if (filter_kind == BY_PEER_FILTER) {
			nx_conn_id fst_coid = pcc.get_dir_base().get_coid_by_ref(fl_str,
					null);
			if (fst_coid == null) {
				return all_chns;
			}
			channel sel_chn = read_channel(pcc, owr, fst_coid);
			if(sel_chn != null){
				all_chns.add(sel_chn);
			} 
			if(IN_DEBUG_3){
				if(sel_chn == null){
					logger.info("read_all_channels. NULL_CHANNEL.");
				}
			}
			return all_chns;
		}

		String sub_str = null;
		if (fl_str != null) {
			sub_str = fl_str.toUpperCase();
		}

		List<File> all_ff = nx_protector
				.find_all_coid_files(pcc.get_dir_base());
		boolean to_add = true;
		for (File ff : all_ff) {
			channel chn = read_channel_from_coid_file(pcc, ff, owr);
			if ((chn != null) && (chn.trader != null)) {
				if (sub_str != null) {
					to_add = false;
					if (filter_kind == BY_NAME_FILTER) {
						String c1 = chn.trader.legal_name.toUpperCase();
						if (c1.contains(sub_str)) {
							to_add = true;
						}
					} else {
						String c1 = chn.coid.toString().toUpperCase();
						if (c1.startsWith(sub_str)) {
							to_add = true;
						}
					}
				}
				if (to_add) {
					all_chns.add(chn);
				}
			}
		}
		Collections.sort(all_chns);
		return all_chns;
	}
	
	public String toString(){
		String the_str = "COID=\n" + coid + "\nNAME=\n" + trader.legal_name;
		return the_str;
	}

	public static void print_channels(PrintStream os, List<channel> all_chns,
			boolean full_prt) {

		os.println();
		os.println("CHANNELS:");
		for (channel chn : all_chns) {
			if(chn == null){
				continue;
			}
			chn.print(os, full_prt);			
		}
		os.println();
		os.println();
	}

	public void print(PrintStream os, boolean full_prt) {
		os.print("--------------------------------------------------------");
		os.println("------------------------------");

		os.println("COID=");
		os.println(coid);
		os.print("NAME=");
		os.println(trader.legal_name);
		if (full_prt) {
			os.println("______________");
			if (cn_mikid_dir != null) {
				os.println("USR_DIR=");
				os.println(cn_mikid_dir);
			}
			os.println("______________");
			List<String> lns = trader.get_person_lines("FULL_DATA");
			parse.print_lines(os, lns);
		}
	}

	public static List<channel> filter_channels_by_id(List<channel> all_chns,
			String pref) {
		String prefix = pref.toUpperCase();
		List<channel> sel_chns = new ArrayList<channel>();
		for (channel chn : all_chns) {
			String c1 = chn.coid.toString().toUpperCase();
			if (c1.startsWith(prefix)) {
				sel_chns.add(chn);
			}
		}

		return sel_chns;
	}

	public static List<channel> filter_channels_by_name(List<channel> all_chns,
			String sub_name) {
		String sub_str = sub_name.toUpperCase();
		List<channel> sel_chns = new ArrayList<channel>();
		for (channel chn : all_chns) {
			String c1 = chn.trader.legal_name.toUpperCase();
			if (c1.contains(sub_str)) {
				sel_chns.add(chn);
			}
		}

		return sel_chns;
	}

	public static List<channel> read_all_accounts(paccount pcc, key_owner owr) {
		List<channel> all_chns = new ArrayList<channel>();
		File mk_dir = pcc.get_mikid_dir();
		List<File> all_mikids = pcc.get_all_mikid_dirs();
		for (File dd : all_mikids) {
			channel chn = null;
			if (dd.equals(mk_dir)) {
				chn = new channel(dd, null, null, owr);
			} else {
				chn = new channel(dd, null, null, null);
			}
			chn.chn_kind = ACCOUNT_KIND;
			all_chns.add(chn);
		}
		return all_chns;
	}

	private static void add_trusted_vals(List<String> lst,
			Map<String, avg_data> sum_lst) {
		for (int aa = 0; aa < lst.size(); aa++) {
			String dom = lst.get(aa);
			avg_data dat = sum_lst.get(dom);
			if (dat == null) {
				dat = new avg_data();
				dat.tot = new Double(0);
				dat.num = 0;
				sum_lst.put(dom, dat);
				dat.dom = dom;
			}
			dat.tot += aa;
			dat.num++;
		}
	}

	public static void calc_avg_vals(Map<String, avg_data> sum_lst) {
		for (Map.Entry<String, avg_data> ee : sum_lst.entrySet()) {
			avg_data dd = ee.getValue();
			dd.tot /= dd.num;
		}
	}

	public static List<String> sort_avg_vals(Map<String, avg_data> sum_lst) {
		List<avg_data> all_dat = new ArrayList<avg_data>();
		all_dat.addAll(sum_lst.values());
		Collections.sort(all_dat, get_cmp_avg());

		List<String> all_doms = new ArrayList<String>();
		for (avg_data dat : all_dat) {
			all_doms.add(dat.dom);
		}
		return all_doms;
	}

	// used in GUI
	public static trissuers get_trissuers(paccount pcc,
			List<channel> all_chn, key_owner owr) {
		Map<String, avg_data> trusted_avg = new TreeMap<String, avg_data>();
		Map<String, avg_data> non_trusted_avg = new TreeMap<String, avg_data>();
		for (channel chn : all_chn) {
			paccount remote = pcc.get_sub_paccount(chn.coid);

			trissuers t_grps = new trissuers();
			t_grps.init_trissuers(remote, owr);
			add_trusted_vals(t_grps.trusted, trusted_avg);
			add_trusted_vals(t_grps.not_trusted, non_trusted_avg);
		}
		calc_avg_vals(trusted_avg);
		calc_avg_vals(non_trusted_avg);

		Map<String, avg_data> final_trusted_avg = new TreeMap<String, avg_data>();
		List<String> final_not_trusted = new ArrayList<String>();

		for (Map.Entry<String, avg_data> ee : trusted_avg.entrySet()) {
			avg_data dat_yes = ee.getValue();
			String dom = ee.getKey();
			avg_data dat_not = non_trusted_avg.get(dom);

			if (dat_not != null) {
				if (dat_not.num < dat_yes.num) {
					final_trusted_avg.put(dom, dat_yes);
				} else {
					final_not_trusted.add(dom);
				}
			} else {
				final_trusted_avg.put(dom, dat_yes);
			}
		}

		trissuers triss = new trissuers();
		triss.trusted = sort_avg_vals(final_trusted_avg);
		triss.not_trusted = final_not_trusted;
		triss.start_trissuers();

		return triss;
	}

	private static Comparator<avg_data> get_cmp_avg() {
		Comparator<avg_data> cmp_dom = new Comparator<avg_data>() {
			public int compare(avg_data d1, avg_data d2) {
				return Double.compare(d1.tot, d2.tot);
			}
		};
		return cmp_dom;
	}

	static class avg_data {
		String dom = null;
		Double tot = null;
		int num = -1;
	}

//	public static class trusted_lists {
//		public List<String> all_trusted = null;
//		public List<String> all_not_trusted = null;
//	}

}
