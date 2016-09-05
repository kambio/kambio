package emetcode.economics.passet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import emetcode.crypto.bitshake.utils.console_get_key;
import emetcode.crypto.bitshake.utils.file_funcs;
import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.crypto.bitshake.utils.mem_file;
import emetcode.net.netmix.nx_conn_id;
import emetcode.net.netmix.nx_dir_base;
import emetcode.util.devel.net_funcs;

public class command_line_choose {

	private static final String TIME_OUT_FNAM = "selec_time_out.dat";
	public static final int DEFAULT_TIME_OUT_SECS = 600;

	public static final File DOT_DIR = new File(".");

	public static final char EOL = '\n';
	public static final char TAB = '\t';

	paccount local_pcc;
	paccount remote_pcc;

	int chann_sel_type;
	String chann_prm;

	public command_line_choose() {
		init_command_line_choose();
	}

	void init_command_line_choose() {
		local_pcc = null;
		remote_pcc = null;
		chann_sel_type = channel.INVALID_FILTER;
		chann_prm = null;
	}

	static String help_msg = EOL + "params: " + EOL + TAB + "[-k <key>] " + EOL
			+ TAB + "[-h|-help|-v|-r|-p|-pm|-pch|-pd|-lvt|-lvk|-lvn|-lvs]"
			+ "[-i2p|-tcp|-mudp] " + EOL + TAB + "[-t <secs>] "
			+ "[-m <iso_currency_code>] " + "[-dn <domain_name>] " + EOL + TAB
			+ "[-cr|-cf] " + "[-cn <name>|-ci <coid>|-cd <addr>] " + EOL + TAB
			+ "[*( (+|-)<num_passets> [1|2|5] [(d|z) [<num_zeros>]] )]" + EOL;

	static String help_msg2 = "-k : use <key> as the encrypt/decrypt key."
			+ EOL
			+ "---------------"
			+ EOL
			+ "-r : force selection delete (reset). shows timeout."
			+ EOL
			+ "-p : print all selected paths and selected channel."
			+ EOL
			+ "-pm : print list of all iso currency codes."
			+ EOL
			+ "-pch : print all available channels."
			+ EOL
			+ "-pd : print details for selected print option."
			+ EOL
			+ "---------------"
			+ EOL
			+ "-lvt : set working receive level to TRUSTED_LEVEL (receives only from trusted domains)."
			+ EOL
			+ "-lvk : set working receive level to KNOWN_LEVEL (receives from any dom except not trusted)."
			+ EOL
			+ "-lvn : set working receive level to NOT_TRUSTED_LEVEL (receives from any domain)."
			+ EOL
			+ "-lvs : save the working receive level as default receive level."
			+ EOL
			+ "---------------"
			+ "-i2p : use i2p protocol."
			+ EOL
			+ "-tcp : use tcp protocol."
			+ EOL
			+ "-mudp : use mudp protocol."
			+ EOL
			+ "---------------"
			+ EOL
			+ "-t : set the timeout in <secs> after wich the current selection is auto-deleted."
			+ EOL
			+ "-m : work with <iso_currency_code> as currency."
			+ EOL
			+ "-dn : select only notes issued by <domain_name>."
			+ EOL
			+ "---------------"
			+ EOL
			+ "-cr : force selected channel delete (reset). shows timeout."
			+ EOL
			+ "-cf : print channels with full info."
			+ EOL
			+ "-cn : print all channels wich trader name contains <name>. If only one, select it."
			+ EOL
			+ "-ci : print all channels wich full id starts with <coid>. If only one, select it."
			+ EOL
			+ "-cd : select the first channel wich domain name is <addr>."
			+ EOL
			+ "---------------"
			+ EOL
			+ "(rest_of_params): is a list of denomination opers. Ej: '+1 5z3 +3 2z1 -7 1z2'"
			+ EOL
			+ " means Add 1 of 5,000, add 3 of 20 and remove 7 of 100 to current selection."
			+ EOL
			+ "---------------"
			+ EOL
			+ "-h : show help invocation info."
			+ EOL
			+ "-help : show full help invocation info."
			+ EOL
			+ "-v : show version info."
			+ EOL
			+ "---------------"
			+ EOL
			+ "-tc : debugging option to create test channels."
			+ EOL
			+ "-lvr : debugging option to save the working recv level as remote level for the selected channel."
			+ EOL + "---------------" + EOL
			+ "Examples to add and remove passets from the current selection."
			+ EOL + "'+1 1'. Add 1 passet of denomination 1." + EOL
			+ "'+3 2'. Add 3 passet of denomination 2." + EOL
			+ "'+7 5'. Add 7 passet of denomination 5." + EOL
			+ "'+1 1z1'. Add 1 passet of denomination 10." + EOL
			+ "'+1 1z2'. Add 1 passet of denomination 100." + EOL
			+ "'+1 1z3'. Add 1 passet of denomination 1,000." + EOL
			+ "'+1 1z15'. Add 1 passet of denomination 1,000,000,000,000,000."
			+ EOL + "'+1 1d3'. Add 1 passet of denomination 1 / 1,000." + EOL
			+ "'+1 1d7'. Add 1 passet of denomination 1 / 10,000,000." + EOL
			+ "'-1 1z5'. Remove 1 passet of denomination 100,000." + EOL
			+ "'-1 2z1'. Remove 1 passet of denomination 20." + EOL
			+ "'-1 5z1'. Remove 1 passet of denomination 50." + EOL
			+ "'-1 5z3'. Remove 1 passet of denomination 5,000." + EOL
			+ "'-1 5d3'. Remove 1 passet of denomination 5 / 1,000." + EOL
			+ "'+3 5z2 +1 2z2 + 2 1z1'. Add  (3 * 500) + (1 * 200) + (2 * 10)."
			+ "For a total of 1720." + EOL + EOL;

	static String version_msg = "command_line_issuer v1.0"
			+ "(c) 2013. QUIROGA BELTRAN, Jose Luis. Bogota - Colombia.";

	boolean is_signed(String arg) {
		if (arg == null) {
			return false;
		}
		if (arg.length() == 0) {
			return false;
		}
		if (arg.charAt(0) == '+') {
			return true;
		}
		if (arg.charAt(0) == '-') {
			return true;
		}
		return false;
	}

	boolean get_args(String[] args) {
		int net_kind = net_funcs.TCP_NET;

		boolean prt_help = false;
		boolean prt_help2 = false;
		boolean prt_version = false;
		boolean reset_select = false;
		boolean reset_channel = false;
		boolean prt_paths = false;
		boolean prt_all_cho = true;
		boolean prt_all_iso_currencies = false;
		boolean prt_sel_chann = false;
		boolean full_prt = false;

		String prt_cho_nm = null;
		
		String selecting_dom = null;

		String test_chann_nm = null;

		File root_dir = DOT_DIR;

		key_owner owr = null;

		int cho_currcy_idx = -1;

		int n_timeout = -1;

		List<Integer> num_deno = new ArrayList<Integer>();
		List<String> denos_str = new ArrayList<String>();
		List<tag_denomination> denos = new ArrayList<tag_denomination>();

		int num_args = args.length;
		for (int ii = 0; ii < num_args; ii++) {
			String the_arg = args[ii];
			if (the_arg.equals("-h")) {
				prt_help = true;
			} else if (the_arg.equals("-help")) {
				prt_help2 = true;
			} else if (the_arg.equals("-v")) {
				prt_version = true;
			} else if (the_arg.equals("-r")) {
				reset_select = true;
			} else if (the_arg.equals("-p")) {
				prt_paths = true;
			} else if (the_arg.equals("-pm")) {
				prt_all_iso_currencies = true;
			} else if (the_arg.equals("-cr")) {
				reset_channel = true;
			} else if (the_arg.equals("-pd")) {
				full_prt = true;
			} else if (the_arg.equals("-pch")) {
				prt_sel_chann = true;
			} else if (the_arg.equals("-cf")) {
				prt_sel_chann = true;
				full_prt = true;
			} else if (the_arg.equals("-i2p")) {
				net_kind = net_funcs.I2P_NET;
			} else if (the_arg.equals("-tcp")) {
				net_kind = net_funcs.TCP_NET;
			} else if (the_arg.equals("-mudp")) {
				net_kind = net_funcs.MUDP_NET;
			} else if ((the_arg.equals("-prt_cho")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				prt_cho_nm = args[kk_idx];
			} else if ((the_arg.equals("-tc")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				test_chann_nm = args[kk_idx];
			} else if ((the_arg.equals("-cn")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				chann_sel_type = channel.BY_NAME_FILTER;
				chann_prm = args[kk_idx];
				prt_sel_chann = true;
			} else if ((the_arg.equals("-ci")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				chann_sel_type = channel.BY_COID_FILTER;
				chann_prm = args[kk_idx];
				prt_sel_chann = true;
			} else if ((the_arg.equals("-cd")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				chann_sel_type = channel.BY_PEER_FILTER;
				chann_prm = args[kk_idx];
				prt_sel_chann = true;
			} else if ((the_arg.equals("-dn")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				selecting_dom = args[kk_idx];
			} else if ((the_arg.equals("-k")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				byte[] bts = args[kk_idx].getBytes(config.UTF_8);

				owr = new key_owner(bts);
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
			} else if ((the_arg.equals("-t")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				String prm = args[kk_idx];
				n_timeout = Integer.parseInt(prm);
			} else if (is_signed(the_arg) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				if (the_arg.charAt(0) == '+') {
					the_arg = the_arg.substring(1);
				}
				int num_add;
				try {
					num_add = Integer.parseInt(the_arg);
				} catch (NumberFormatException ee) {
					System.out.println("Bad argument '" + the_arg + "'");
					return false;
				}
				String prm = args[kk_idx];

				num_deno.add(num_add);
				denos_str.add(prm);
			} else {
				prt_help = true;
				break;
			}
		}

		if (num_args == 0) {
			prt_help = true;
		}

		System.out.println();
		System.out.println();
		System.out.print("***************************************");
		System.out.print(" choose ");
		System.out.println("***************************************");

		if (prt_paths) {
			prt_all_cho = false;
		}
		if (prt_sel_chann && num_deno.isEmpty()) {
			prt_all_cho = false;
		}

		if (prt_help) {
			System.out.println(help_msg);
			return false;
		}
		if (prt_help2) {
			System.out.println(help_msg);
			System.out.println(help_msg2);
			return false;
		}
		if (prt_version) {
			System.out.println(version_msg);
			return false;
		}
		if (prt_all_iso_currencies) {
			iso.print_currencies(System.out);
			;
			return false;
		}

		if (owr == null) {
			byte[] kk = console_get_key.get_key(false, false);
			if (kk == null) {
				System.out.println("Bad key. Try again.");
				return false;
			}
			owr = new key_owner(kk);
		}

		if (!paccount.is_user(root_dir, owr)) {
			System.out.println("User does not exist in current directory.\n"
					+ root_dir);
			return false;
		}

		local_pcc = new paccount();
		local_pcc.set_base_dir(root_dir, owr, net_kind, null);

		tag_denomination defa_deno = local_pcc.read_deno_file();
		if ((defa_deno != null) && (cho_currcy_idx == -1)) {
			cho_currcy_idx = defa_deno.currency_idx;
		}
		if (cho_currcy_idx == -1) {
			cho_currcy_idx = config.DEFAULT_CURRENCY;
		}

		local_pcc.set_working_currency(cho_currcy_idx);
		local_pcc.trust_selecting = selecting_dom;

		local_pcc.prt_basic_data(owr);

		if (test_chann_nm != null) {
			nx_conn_id test_coid = new nx_conn_id();
			remote_pcc = local_pcc.get_sub_paccount(test_coid);

			nx_dir_base bb_dd = local_pcc.get_dir_base();
			File cc_ff = bb_dd.get_coid_file(test_coid);
			file_funcs.mk_parent_dir(cc_ff);
			mem_file.concurrent_create_file(cc_ff);
			bb_dd.write_coref(test_chann_nm, test_coid);
		}

		List<channel> all_chns = null;
		if (reset_channel) {
			File ch_ff = local_pcc.get_selected_channel_file();
			System.out.println("RESETING_SELECTING_CHANNEL\n" + ch_ff);
			ch_ff.delete();
		}
		if (prt_sel_chann) {
			all_chns = channel.read_all_channels(local_pcc, owr,
					chann_sel_type, chann_prm);
		}
		if (all_chns != null) {
			channel.print_channels(System.out, all_chns, full_prt);
			if ((chann_sel_type != channel.INVALID_FILTER)
					&& (all_chns.size() == 1)) {
				channel chn0 = all_chns.get(0);
				local_pcc.write_selected_channel(chn0, owr);
				System.out.println("SELECTING_CHANNEL=\n" + chn0);
			}
			if (all_chns.size() == 0) {
				System.out.println("NO CHANNELS FOUND.");
			}
		}

		channel sele_chann = local_pcc.read_selected_channel(owr);
		if (sele_chann != null) {
			System.out.println("SELECTED CHANNEL:");
			sele_chann.print(System.out, full_prt);
		} else {
			System.out.println("NO CHANNEL HAS BEEN SELECTED.");
		}

		if (!prt_all_cho && !prt_paths) {
			return false;
		}

		if (sele_chann != null) {
			nx_conn_id sele_coid = sele_chann.coid;
			remote_pcc = local_pcc.get_sub_paccount(sele_coid);
		}

		int currcy = local_pcc.get_working_currency();
		for (String dno_txt : denos_str) {
			tag_denomination deno = tag_denomination
					.parse_short_text_denomination(dno_txt, currcy);
			deno.currency_idx = currcy;
			denos.add(deno);
		}

		if (n_timeout != -1) {
			write_time_out_secs(n_timeout);
		}
		//int tm_out_secs = read_time_out_secs();

		paccount cho_pcc = local_pcc;
		paccount cho_local_pcc = local_pcc;
		if (remote_pcc != null) {
			cho_pcc = remote_pcc;
			cho_local_pcc = local_pcc;
		}

		if (reset_select) {
			cho_local_pcc.undo_all_chomarks(cho_pcc, null, owr);
		}

		if (cho_pcc != local_pcc) {
			cho_pcc.prt_basic_data(owr);
		}
		process_denos(owr, cho_pcc, cho_local_pcc, num_deno, denos);

		if (prt_paths) {
			if (full_prt) {
				List<File> all_ff = cho_local_pcc.get_passets_of_chomarks_in(cho_pcc, owr, null);
				if (!all_ff.isEmpty()) {
					System.out.println("\n\nALL_NOTES");
				}
				for (File ff : all_ff) {
					System.out.println("" + ff);
				}
			}

			File[] all_sel = cho_pcc.get_all_chomarks(owr, null);
			// List<String> all_sel = cho_pcc.read_chose_file(owr);

			boolean has_sel = (all_sel.length > 0);
			if (has_sel) {
				System.out.println("\n\nALL_REF");
			}
			for (File ff : all_sel) {
				System.out.println("" + ff);
			}

			if (!has_sel) {
				System.out.println("NO notes have been SELECTED.");
			}
		}
		if (prt_all_cho) {
			if(prt_cho_nm == null){
				cho_local_pcc.fill_all_deno_count(owr, cho_pcc, 100);
			} else {
				cho_local_pcc.fill_count_with_all_choices_in(owr, cho_pcc, prt_cho_nm);
				System.out.println("CHOICES IN " + prt_cho_nm);
			}
			cho_local_pcc.deno_cter.print_all_deno_count(System.out);
		}

		return true;
	}

	private void write_time_out_secs(int tm_out) {
		File f_tm_out = new File(local_pcc.get_currency_dir(), TIME_OUT_FNAM);
		String str_tm_out = "" + tm_out;
		mem_file.write_string(f_tm_out, str_tm_out);
	}

	public static void main(String[] args) {
		command_line_choose eng = new command_line_choose();
		eng.get_args(args);
	}

	public void process_denos(key_owner owr, paccount cho_pcc,
			paccount cho_loc_pcc, List<Integer> num_deno,
			List<tag_denomination> denos) {
		if (denos.size() != num_deno.size()) {
			throw new bad_passet(2);
		}
		int aa = 0;
		for (tag_denomination deno : denos) {
			int num = num_deno.get(aa);
			aa++;

			cho_loc_pcc.choose_num_passets(owr, cho_pcc, deno, num);
		}
	}

}
