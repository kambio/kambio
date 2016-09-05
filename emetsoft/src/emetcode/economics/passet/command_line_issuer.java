package emetcode.economics.passet;

import java.io.File;
import java.util.Collections;
import java.util.List;

import emetcode.crypto.bitshake.utils.console_get_key;
import emetcode.crypto.bitshake.utils.file_funcs;
import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.crypto.bitshake.utils.mem_file;
import emetcode.crypto.gamal.gamal_generator;
import emetcode.net.netmix.nx_dir_base;
import emetcode.net.netmix.nx_std_coref;
import emetcode.net.netmix.nx_conn_id;
import emetcode.net.netmix.nx_messenger;
import emetcode.net.netmix.nx_protector;
import emetcode.util.devel.net_funcs;

public class command_line_issuer {

	public static final int TRISSUERS_INVALID_OPER = 300;
	public static final int TRISSUERS_ADD_OPER = 301;
	public static final int TRISSUERS_REMOVE_OPER = 302;
	public static final int TRISSUERS_ADD_NOT_TRUSTED_OPER = 303;
	public static final int TRISSUERS_REMOVE_NOT_TRUSTED_OPER = 304;

	public static final int TRACKER_INVALID_OPER = 400;
	public static final int TRACKER_ADD_OPER = 401;
	public static final int TRACKER_REMOVE_OPER = 402;
	public static final int TRACKER_SET_NEXT_OPER = 403;
	public static final int TRACKER_GET_NEXT_OPER = 404;

	public static final File DOT_DIR = new File(".");

	public static final char EOL = '\n';
	public static final char TAB = '\t';

	key_owner owr;

	int ISS_num;
	int iss_num;

	tag_denomination iss_deno;

	paccount working_pcc;

	public command_line_issuer() {
		init_command_line_issuer();
	}

	void init_command_line_issuer() {
		owr = null;

		ISS_num = 0;
		iss_num = 0;

		iss_deno = new tag_denomination();

		working_pcc = null;
	}

	static String help_msg = "params:" + EOL + TAB + "[-k <key>] " + EOL + TAB
			+ "[-h|-help|-v] " + "[-c] " + "[-p] " + "[-pm] " + "[-pcc] "
			+ "[-pt] " + "[-i2p|-tcp|-mudp] " + EOL + TAB
			+ "[-m <iso_currency_code>] " + "[-n <denomination>] " + "[-s] "
			+ EOL + TAB + "[-ISSUE <num>] " + "[-issue <num>] "
			+ "[-video <sha_file> <video_url> <sha_file_url>] " + "[-demand] "
			+ EOL + TAB + "[-Dusr] " + "[-Dchn] " + "[-Df] "
			+ "[-gamal <file_name>] " + EOL + TAB + "[-snm <name>] "
			+ "[-sid <id>] " + "[-scc <iso_country_code>] "
			+ "[-sdn <domain_name>] " + EOL + TAB + "[-s2p <i2p_f_nam>] "
			+ "[-sem <email_addr>] " + "[-ssw <SWIFT_code>] " + EOL + TAB
			+ "[-sac <account_number>] " + "[-sim <jpeg_image_file>] " + EOL
			+ TAB + "[-glid|-prt_glid|-prt_dom] " + EOL + TAB
			+ "[-tat <glid> |" + " -trt <glid> |" + " -tan <glid> |"
			+ " -trn <glid>] " + EOL + TAB + "[-add_trk <addr> |"
			+ " -remove_trk <addr>]" + "[-set_next_tracker <addr> |"
			+ " -get_nxt_trk] " + EOL + TAB + "[-r <r_descr>] "
			+ "[-ck_choice_name <choice_nm>] ";

	static String help_msg2 = "-k : use <key> as the encrypt/decrypt key."
			+ EOL
			+ "-c : create user under current directory with <key> if it does not exist."
			+ EOL
			+ "---------------"
			+ EOL
			+ "-p : print current settings for user of <key>."
			+ EOL
			+ "-pm : print list of all iso currency codes."
			+ EOL
			+ "-pcc : print list of all coutry codes."
			+ EOL
			+ "-pt : print trusted lists ('trusted', 'known' and 'not trusted') of peers."
			+ EOL
			+ "-i2p : use i2p protocol."
			+ EOL
			+ "-tcp : use tcp protocol."
			+ EOL
			+ "-mudp : use mudp protocol."
			+ EOL
			+ "---------------"
			+ EOL
			+ "-m : work with <iso_currency_code> as currency."
			+ EOL
			+ "-n : work with <denomination> for issuing. See below DENOMINATION FORMAT."
			+ EOL
			+ "-s : set defaults with given values in -m -n options."
			+ EOL
			+ "---------------"
			+ EOL
			+ "-ISSUE : no video issuance. Issues <num> passets. See passet terms of use."
			+ EOL
			+ "-issue : start issuance of <num> passets. Must video sign to finish issuance."
			+ EOL
			+ "-video : finish issuance of passets in <sha_file> with <video_url> and "
			+ "<sha_file_url>."
			+ EOL
			+ TAB
			+ "See passet terms of use."
			+ EOL
			+ "-demand : demand selected passets."
			+ EOL
			+ "---------------"
			+ EOL
			+ "-Dusr : deletes the user with given key."
			+ EOL
			+ "-Dchn : deletes the selected channel."
			+ EOL
			+ "-Df : forces deletion"
			+ EOL
			+ "-gamal : use <file_name> to start the gamal system"
			+ EOL
			+ "---------------"
			+ EOL
			+ "-snm : set <name> as the issuer name."
			+ EOL
			+ "-sid : set <id> as the issuer legal id number in <iso_country_code>."
			+ EOL
			+ "-scc : set <iso_country_code> as the issuer coutry code where <id> is valid."
			+ EOL
			+ "-sdn : set <domain_name> as the issuer internet domain name."
			+ EOL
			+ "-s2p : set <i2p_f_nam> as the file name that contains the issuer i2p addr."
			+ EOL
			+ TAB
			+ "If non existant, try to generate one. Needs an i2p router running to generate."
			+ EOL
			+ "-sem : set <email_addr> as the issuer email address."
			+ EOL
			+ "-ssw : set <SWIFT_code> as the issuer's bank SWIFT code."
			+ EOL
			+ "-sac : set <account_number> as the issuer's bank account number."
			+ EOL
			+ TAB
			+ "When used, the strings in <SWIFT_code> + <account_number> must fully "
			+ EOL
			+ TAB
			+ "identify the issuer's bank account for wire transfers. "
			+ EOL
			+ TAB
			+ "Read passet terms of use."
			+ EOL
			+ "-sim : set <jpeg_image_file> as the image file."
			+ EOL
			+ "---------------"
			+ EOL
			+ "-glid : create the global id for this user (option -k)."
			+ EOL
			+ "-prt_glid : print the global id for this user (option -k)."
			+ EOL
			+ "---------------"
			+ EOL
			+ "-tat : add <glid> to trusted list of peers."
			+ EOL
			+ "-trt : remove <glid> from trusted list of peers."
			+ EOL
			+ "-tan : add <glid> to not trusted list of peers."
			+ EOL
			+ "-trn : remove <glid> from not trusted list of peers."
			+ EOL
			+ "---------------"
			+ EOL
			+ "-add_trk : add <addr> to trackers list."
			+ EOL
			+ "-remove_trk : remove <addr> from trackers list."
			+ EOL
			+ "-set_next_tracker : set <addr> as the next tracker that will be given to clients."
			+ EOL
			+ "-get_nxt_trk : get the addr of the next tracker that will be given to clients."
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
			+ "-tch : debug option to change trusted of selected channel. (ONLY for debugging)."
			+ EOL
			+ "-tak : debug option to add <addr> to known list of peers. (ONLY for debugging)."
			+ EOL
			+ "-trk : debug option to remove <addr> from known list of peers. (ONLY for debugging)."
			+ EOL + "---------------" + EOL + "DENOMINATION FORMAT." + EOL
			+ "The denomination format is the followin." + EOL + TAB
			+ "[1|2|5] [(d|z) [<num_zeros>]]" + EOL + "Examples." + EOL
			+ "5z3 is 5,000" + EOL + "2z1 is 20" + EOL + "1z2 is 100" + EOL
			+ "2d3 is 2 / 1,000" + EOL + "1z15 is 1,000,000,000,000,000" + EOL
			+ "---------------" + EOL;

	static String version_msg = "command_line_issuer v1.0"
			+ "(c) 2013. QUIROGA BELTRAN, Jose Luis. Bogota - Colombia.";

	boolean get_args(String[] args) {

		int net_kind = net_funcs.TCP_NET;

		boolean set_defaults = false;
		boolean create_user = false;
		boolean show_default_deno = false;
		boolean show_working_deno = false;
		boolean show_current_user = false;
		List<String> current_user_lines = null;

		boolean prt_help = false;
		boolean prt_help2 = false;
		boolean prt_version = false;
		boolean prt_settings = false;
		boolean prt_all_iso_currencies = false;
		boolean prt_all_iso_countries = false;
		boolean prt_trusted_lists = false;
		boolean prt_trackers = false;

		boolean truted_on_chn = false;

		boolean cho_currency_idx = false;
		boolean cho_deno_params = false;
		tag_denomination cho_deno = new tag_denomination();

		boolean del_user = false;
		boolean del_chann = false;
		boolean force_del = false;

		boolean create_glid = false;
		boolean print_glid = false;
		boolean need_key = false;

		String oper_address = null;
		int list_oper = TRISSUERS_INVALID_OPER;

		String trk_address = null;
		int trk_oper = TRACKER_INVALID_OPER;

		String gamal_file = null;

		String img_file_nm = null;

		String dom_str = null;
		tag_person iss_person = new tag_person();

		String r_descr = null;
		String cho_nm = null;

		boolean print_dom = false;

		char dbg_skip_finish_chomarks = paccount.DBG_INVALID_SKIP_FINISH_CHOMARKS;

		int num_args = args.length;
		for (int ii = 0; ii < num_args; ii++) {
			String the_arg = args[ii];
			//System.out.print("ARG==" + the_arg + "\n");
			
			if (the_arg.equals("-h")) {
				prt_help = true;
			} else if (the_arg.equals("-help")) {
				prt_help2 = true;
			} else if (the_arg.equals("-v")) {
				prt_version = true;
			} else if (the_arg.equals("-p")) {
				prt_settings = true;
				need_key = true;
			} else if (the_arg.equals("-pm")) {
				prt_all_iso_currencies = true;
			} else if (the_arg.equals("-pcc")) {
				prt_all_iso_countries = true;
			} else if (the_arg.equals("-pt")) {
				prt_trusted_lists = true;
				need_key = true;
			} else if (the_arg.equals("-i2p")) {
				net_kind = net_funcs.I2P_NET;
			} else if (the_arg.equals("-tcp")) {
				net_kind = net_funcs.TCP_NET;
			} else if (the_arg.equals("-mudp")) {
				net_kind = net_funcs.MUDP_NET;
			} else if (the_arg.equals("-c")) {
				create_user = true;
				need_key = true;
			} else if (the_arg.equals("-s")) {
				set_defaults = true;
			} else if (the_arg.equals("-tch")) {
				truted_on_chn = true;
			} else if (the_arg.equals("-Dusr")) {
				del_user = true;
			} else if (the_arg.equals("-Dchn")) {
				del_chann = true;
			} else if (the_arg.equals("-Df")) {
				force_del = true;
			} else if (the_arg.equals("-glid")) {
				create_glid = true;
			} else if (the_arg.equals("-prt_glid")) {
				print_glid = true;
			} else if (the_arg.equals("-prt_dom")) {
				print_dom = true;
			} else if (the_arg.equals("-get_nxt_trk")) {
				trk_oper = TRACKER_GET_NEXT_OPER;
			} else if (the_arg.equals("-dbg_no_end_cho")) {
				dbg_skip_finish_chomarks = paccount.DBG_SET_SKIP_FINISH_CHOMARKS;
			} else if (the_arg.equals("-dbg_yes_end_cho")) {
				dbg_skip_finish_chomarks = paccount.DBG_RESET_SKIP_FINISH_CHOMARKS;
			} else if ((the_arg.equals("-k")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				byte[] bts = args[kk_idx].getBytes(config.UTF_8);

				owr = new key_owner(bts);
				//System.out.print("new key_owner end\n");
			} else if ((the_arg.equals("-m")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				String val = args[kk_idx].toUpperCase();
				int crrcy_idx = iso.get_currency_idx(val);
				if (crrcy_idx == -1) {
					System.out.println("invalid currency code. use option -pm");
					break;
				}

				cho_deno.currency_idx = crrcy_idx;
				cho_currency_idx = true;

				need_key = true;
				show_default_deno = true;
				show_working_deno = true;

				iss_deno.currency_idx = crrcy_idx;
			} else if ((the_arg.equals("-n")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;
				String prm = args[kk_idx];

				tag_denomination deno = tag_denomination
						.parse_short_text_denomination(prm);

				cho_deno.ten_exponent = deno.ten_exponent;
				cho_deno.multiplier = deno.multiplier;
				cho_deno_params = true;

				need_key = true;
				show_default_deno = true;
				show_working_deno = true;

				iss_deno.ten_exponent = deno.ten_exponent;
				iss_deno.multiplier = deno.multiplier;
			} else if ((the_arg.equals("-ISSUE")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				ISS_num = Integer.parseInt(args[kk_idx]);
				need_key = true;
			} else if ((the_arg.equals("-issue")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				iss_num = Integer.parseInt(args[kk_idx]);
				need_key = true;
			} else if ((the_arg.equals("-gamal")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				gamal_file = args[kk_idx];
				need_key = true;
			} else if ((the_arg.equals("-snm")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				iss_person.legal_name = args[kk_idx];
				need_key = true;
			} else if ((the_arg.equals("-sid")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				iss_person.legal_id = args[kk_idx];
				need_key = true;
			} else if ((the_arg.equals("-scc")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				String val = args[kk_idx].toUpperCase();
				if (iso.is_country_code(val)) {
					iss_person.contry_code = val;
				} else {
					System.out
							.println("invalid country code. use option -pcc.");
					break;
				}
				need_key = true;
			} else if ((the_arg.equals("-sdn")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				dom_str = args[kk_idx];
				iss_person.network_domain_name = dom_str;
				need_key = true;
			} else if ((the_arg.equals("-s2p")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				iss_person.i2p_address = get_i2p_address(args[kk_idx]);
				need_key = true;
			} else if ((the_arg.equals("-sem")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				iss_person.email = args[kk_idx];
				need_key = true;
			} else if ((the_arg.equals("-ssw")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				iss_person.SWIFT_code = args[kk_idx];
				need_key = true;
			} else if ((the_arg.equals("-sac")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				iss_person.account_number = args[kk_idx];
				need_key = true;
			} else if ((the_arg.equals("-sim")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				img_file_nm = args[kk_idx];
				need_key = true;
			} else if ((the_arg.equals("-tat")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				list_oper = TRISSUERS_ADD_OPER;
				oper_address = args[kk_idx];
				need_key = true;
			} else if ((the_arg.equals("-trt")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				list_oper = TRISSUERS_REMOVE_OPER;
				oper_address = args[kk_idx];
				need_key = true;
			} else if ((the_arg.equals("-tan")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				list_oper = TRISSUERS_ADD_NOT_TRUSTED_OPER;
				oper_address = args[kk_idx];
				need_key = true;
			} else if ((the_arg.equals("-trn")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				list_oper = TRISSUERS_REMOVE_NOT_TRUSTED_OPER;
				oper_address = args[kk_idx];
				need_key = true;
			} else if ((the_arg.equals("-add_trk")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				trk_oper = TRACKER_ADD_OPER;
				trk_address = args[kk_idx];
				need_key = true;
			} else if ((the_arg.equals("-remove_trk")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				trk_oper = TRACKER_REMOVE_OPER;
				trk_address = args[kk_idx];
				need_key = true;
			} else if ((the_arg.equals("-set_next_tracker"))
					&& ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				trk_oper = TRACKER_SET_NEXT_OPER;
				trk_address = args[kk_idx];
				need_key = true;
			} else if ((the_arg.equals("-r")) && ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				r_descr = args[kk_idx];
				need_key = true;
			} else if ((the_arg.equals("-ck_choice_name"))
					&& ((ii + 1) < num_args)) {
				int kk_idx = ii + 1;
				ii++;

				cho_nm = args[kk_idx];
				need_key = true;
			} else {
				System.out.println("UNKWNOW parameter '" + the_arg + "'");
				prt_help = true;
				break;
			}
		}

		if (num_args == 0) {
			prt_help = true;
		}

		if (!print_glid && !print_dom) {
			System.out.println();
			System.out.println();
			System.out.print("***************************************");
			System.out.print(" issuer ");
			System.out.println("***************************************");
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
		}
		if (prt_all_iso_countries) {
			iso.print_countries(System.out);
		}

		if (!prt_settings && (prt_all_iso_currencies || prt_all_iso_countries)) {
			return false;
		}

		if (need_key && (owr == null)) {
			byte[] kk = console_get_key.get_key(create_user, false);
			if (kk == null) {
				System.out.println("Bad key. Try again.");
				return false;
			}
			owr = new key_owner(kk);
		}

		working_pcc = null;

		if (owr != null) {
			if (!create_user && !paccount.is_user(DOT_DIR, owr)) {
				System.out
						.println("User does not exist in current directory. Use option -c.");
			} else {
				boolean is_nw = create_user && !paccount.is_user(DOT_DIR, owr);

				gamal_generator gam = null;
				if (gamal_file != null) {
					File g_ff = new File(gamal_file);
					if (g_ff.exists()) {
						gam = nx_dir_base.read_gamal_sys(g_ff, null);
					} else {
						System.out.println("File NOT found '" + g_ff + "'");
					}
				}

				working_pcc = new paccount();
				working_pcc.set_base_dir(DOT_DIR, owr, net_kind, gam);

				if (is_nw) {
					System.out.println("User created under directory '"
							+ working_pcc.get_mikid_dir() + "'");
				}
			}
		}

		tag_denomination defa_deno = null;
		channel sele_chann = null;
		boolean has_pss = (working_pcc != null);
		if (has_pss) {
			// chomarks

			if (dbg_skip_finish_chomarks != paccount.DBG_INVALID_SKIP_FINISH_CHOMARKS) {
				File skip_ff = working_pcc.get_dbg_no_end_choice_file();
				if (dbg_skip_finish_chomarks == paccount.DBG_SET_SKIP_FINISH_CHOMARKS) {
					mem_file.concurrent_create_file(skip_ff);
				} else {
					skip_ff.delete();
				}
			}

			if (print_glid) {
				nx_std_coref gli = working_pcc.get_dir_base().get_local_glid(
						owr);
				System.out.println(gli);
				return false;
			}
			if (print_dom) {
				String the_dom = nx_protector.read_local_domain(
						working_pcc.get_dir_base(), owr);
				System.out.println(the_dom);
				return false;
			}

			defa_deno = working_pcc.read_deno_file();
			iss_deno = new tag_denomination(defa_deno);

			if (cho_deno != null) {
				if (cho_currency_idx) {
					iss_deno.currency_idx = cho_deno.currency_idx;
				}
				if (cho_deno_params) {
					iss_deno.multiplier = cho_deno.multiplier;
					iss_deno.ten_exponent = cho_deno.ten_exponent;
				}
			}

			if (img_file_nm != null) {
				File img_ff = new File(img_file_nm);
				File dst_ff = working_pcc.get_current_user_image_file();
				if (img_ff.isFile()) {
					file_funcs.concurrent_copy_file(img_ff, dst_ff);
					System.out.println("Copied image file to '" + dst_ff + "'");
				}
			}

			if (set_defaults) {
				working_pcc.write_deno_file(iss_deno);
				show_default_deno = true;
				defa_deno = iss_deno;
			}

			working_pcc.set_working_currency(iss_deno.currency_idx);

			if (dom_str != null) {
				nx_protector.write_local_domain(working_pcc.get_dir_base(),
						owr, dom_str);
			}

			boolean has_diff = working_pcc.curr_user.update_with(iss_person);

			if (has_diff) {
				current_user_lines = working_pcc.write_current_user(owr);
				show_current_user = true;
			}

			if (create_glid) {
				working_pcc.create_glid_file(owr);
			}

			paccount remote = null;
			if (r_descr != null) {
				int[] num_found = new int[1];
				num_found[0] = 0;
				nx_conn_id sele_coid = working_pcc.get_dir_base()
						.get_coid_by_ref(r_descr, num_found);
				if (num_found[0] > 1) {
					System.out.println("MORE THAN ONE COID for '" + r_descr
							+ "'. MUST SPECIFY ONE.");
					return false;
				}
				if (sele_coid != null) {
					remote = working_pcc.get_sub_paccount(sele_coid);
				}
			}

			sele_chann = working_pcc.read_selected_channel(owr);
			if (sele_chann != null) {
				System.out.println("SELECTED CHANNEL.");
				sele_chann.print(System.out, false);
			}
			if (truted_on_chn && (remote != null)) {
				System.out.println("USING TRUSTED OF REMOTE !!!");
				remote.prt_basic_data(owr);
				System.out.println("-------\n\n");
			}

			if (cho_nm != null) {
				if (remote == null) {
					System.out.println("Use option -r when using option -ck_choice_name.");
					return false;
				}
				boolean is_val_cho = remote.is_valid_transaction(owr, cho_nm);
				if (is_val_cho) {
					System.out.println("CHOICE=" + cho_nm + " IS_VALID");
				} else {
					System.out.println("CHOICE=" + cho_nm + " is_invalid!!!");
				}
			}

			if (oper_address != null) {
				if (list_oper != TRISSUERS_INVALID_OPER) {
					if (truted_on_chn) {
						if (remote != null) {
							do_trust_op(remote, owr, list_oper, oper_address);
						} else {
							System.out
									.println("Use option -r when using option -tch.");
						}
					} else {
						do_trust_op(working_pcc, owr, list_oper, oper_address);
					}
					prt_trusted_lists = true;
				}
			}

			if (prt_trusted_lists) {
				paccount to_list = working_pcc;
				if (remote != null) {
					to_list = remote;
				}

				File ff = to_list.get_trusted_file();
				System.out.println("TRUSTED list in \n" + ff);
				print_list(ff, owr);
				System.out.println("-------");

				ff = to_list.get_not_trusted_file();
				System.out.println("NOT trusted list in \n" + ff);
				print_list(ff, owr);
				System.out.println("-------");
			}

			if (trk_oper != TRACKER_INVALID_OPER) {
				trackers trks = do_trackers_op(working_pcc, owr, trk_oper,
						trk_address);
				trks.print(System.out);
			}

			if (prt_trackers) {
				trackers trks = new trackers();
				trks.init_trackers(working_pcc, owr);
				trks.print(System.out);
			}
		}

		if (prt_settings) {
			show_default_deno = true;
			show_working_deno = true;
			show_current_user = true;
		}

		if ((working_pcc != null) && show_current_user
				&& (current_user_lines == null)) {
			current_user_lines = working_pcc.curr_user
					.get_person_lines(paccount.the_user_title);
		}
		if (current_user_lines != null) {
			parse.print_line_list(current_user_lines);
			System.out.println("User data file is '"
					+ working_pcc.get_current_user_info_file().getPath() + "'");
			System.out.println();
		} else if (working_pcc != null) {
			working_pcc.prt_basic_data(owr);
		}
		if (show_default_deno && (defa_deno != null)) {
			String val = defa_deno.get_number_denomination();
			System.out.println("DEFAULT DENOMINATION= " + val);
		}
		if (show_working_deno) {
			String val = iss_deno.get_number_denomination();
			System.out.println("WORKING DENOMINATION= " + val);
		}
		if (show_default_deno && has_pss) {
			System.out.println();
			File def_deno_ff = working_pcc.get_deno_file();
			if (defa_deno == null) {
				System.out.println("Default denomination data file '"
						+ def_deno_ff + "' not found.");
			} else {
				System.out.println("Default denomination data file is '"
						+ def_deno_ff + "'");
			}
		}

		if ((working_pcc == null) || prt_all_iso_currencies
				|| prt_all_iso_countries) {
			return false;
		}

		if (owr != null) {
			if (working_pcc != null) {
				if (del_chann && (sele_chann != null)) {
					nx_conn_id coid = sele_chann.coid;
					nx_messenger.delete_coid(working_pcc.get_dir_base(), coid);
				}
				if (del_user) {
					working_pcc.delete_passet(owr, force_del);
				}
			}
		}

		return true;
	}

	private static void do_trust_op(paccount pcc2, key_owner owr,
			int list_oper, String oper_address) {
		if (oper_address == null) {
			return;
		}
		trissuers t_grps = new trissuers();
		t_grps.init_trissuers(pcc2, owr);

		String param = oper_address.toLowerCase();

		switch (list_oper) {
		case TRISSUERS_ADD_OPER:
			t_grps.trusted.add(param);
			break;
		case TRISSUERS_REMOVE_OPER:
			t_grps.trusted.remove(param);
			break;
		case TRISSUERS_ADD_NOT_TRUSTED_OPER:
			t_grps.not_trusted.add(param);
			break;
		case TRISSUERS_REMOVE_NOT_TRUSTED_OPER:
			t_grps.not_trusted.remove(param);
			break;
		}

		t_grps.update_trissuer_files(pcc2, owr);
	}

	public static void print_list(File ff, key_owner owr) {
		List<String> lst = parse.read_encrypted_lines(ff, owr);

		Collections.sort(lst);
		parse.print_line_list(lst);
	}

	public static String get_i2p_address(String file_nm) {
		return null;
	}

	public static void main(String[] args) {
		command_line_issuer eng = new command_line_issuer();
		if (!eng.get_args(args)) {
			return;
		}
		eng.process_file();
	}

	public void process_file() {
		if (ISS_num > 0) {
			working_pcc.issue_passets(ISS_num, owr, iss_deno, null, null);

			System.out.println("Issuing all passets.");
			System.out.println("See passet terms of use.");
			return;
		}
		if (iss_num > 0) {
			working_pcc.issue_passets(iss_num, owr, iss_deno, null, null);
			System.out.println("HALF issuing all passets.");
			System.out.println("Use option -video to finish issuance.");
			System.out.println("See passet terms of use.");
			return;
		}
	}

	private static trackers do_trackers_op(paccount pcc2, key_owner owr,
			int trk_oper, String trk_address) {
		if (trk_address == null) {
			return null;
		}
		trackers trks = new trackers();
		trks.init_trackers(pcc2, owr);

		String param = trk_address.toLowerCase();

		switch (trk_oper) {
		case TRACKER_ADD_OPER:
			trks.all_trackers.add(param);
			break;
		case TRACKER_REMOVE_OPER:
			trks.all_trackers.remove(param);
			break;
		case TRACKER_SET_NEXT_OPER:
			trks.next_tracker = param;
			break;
		}

		trks.update_trackers(pcc2, owr);

		return trks;
	}

}
