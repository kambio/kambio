package emetcode.economics.passet;

import java.io.File;
import java.io.FileFilter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import emetcode.crypto.bitshake.utils.convert;
import emetcode.crypto.bitshake.utils.file_funcs;
import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.crypto.bitshake.utils.mem_file;
import emetcode.crypto.bitshake.utils.global_id;
import emetcode.crypto.gamal.gamal_generator;
import emetcode.economics.passet.locale.L;
import emetcode.net.netmix.bad_netmix;
import emetcode.net.netmix.nx_std_coref;
import emetcode.net.netmix.nx_conn_id;
import emetcode.net.netmix.nx_dir_base;
import emetcode.net.netmix.nx_messenger;
import emetcode.util.devel.bad_emetcode;
import emetcode.util.devel.logger;
import emetcode.util.devel.net_funcs;

public class paccount {

	static final boolean IN_DEBUG_01 = false; // choose & can give
	static final boolean IN_DEBUG_2 = true;
	static final boolean IN_DEBUG_3 = false;
	static final boolean IN_DEBUG_4 = true;
	static final boolean IN_DEBUG_6 = true; // always iss_last_tra
	static final boolean IN_DEBUG_7 = true; // try verifs
	static final boolean IN_DEBUG_8 = true; // prt first trans
	static final boolean IN_DEBUG_9 = false; // abort if check can_recv_from
	static final boolean IN_DEBUG_10 = true; // issued not mine
	static final boolean IN_DEBUG_11 = false; // not a diff_file

	// checks that for single_trans must be false
	static final boolean IN_DEBUG_12 = false; // nxt_has_same_nm
	static final boolean IN_DEBUG_13 = false; // check issuer in get_iss_tails
	static final boolean IN_DEBUG_14 = false; // check issuer in
												// get_iss_last_tra

	static final boolean IN_DEBUG_15 = true; // check & verif OK
	static final boolean IN_DEBUG_16 = true; // cannot check mine
	static final boolean IN_DEBUG_17 = true; // parent of prev_file

	static final boolean IN_DEBUG_18 = false; // signed recep (saved passet)

	static final boolean IN_DEBUG_19 = true; // print changed denos

	static final boolean IN_DEBUG_20 = true; // has_prev verif
	static final boolean IN_DEBUG_21 = true; // print issued

	static final boolean IN_DEBUG_22 = false; // print recep file
	static final boolean IN_DEBUG_23 = true; // print durign get_fst_tra
	static final boolean IN_DEBUG_24 = true; // try funcs failed

	static final boolean IN_DEBUG_25 = true; // copy_parefs
	static final boolean IN_DEBUG_26 = true; // read_sel_chann

	static final boolean IN_DEBUG_27 = true; // has_nxt_pss_rf
	static final boolean IN_DEBUG_28 = true; // invalid nxt_pss

	static final boolean IN_DEBUG_29 = true; // skip fnish chomarks test
	static final boolean IN_DEBUG_30 = true; // next_tra NOT added

	static final boolean IN_DEBUG_31 = true; // end_trans
	static final boolean IN_DEBUG_32 = true; // save_paref
	static final boolean IN_DEBUG_33 = true; // valid transact
	
	// DBG CONSTANTS

	public static final char DBG_INVALID_SKIP_FINISH_CHOMARKS = 'I';
	public static final char DBG_SET_SKIP_FINISH_CHOMARKS = 'S';
	public static final char DBG_RESET_SKIP_FINISH_CHOMARKS = 'R';

	// DIR NAMES

	private static final String DATA_DIR_NM = "data";
	private static final String TRCKS_DIR_NM = "trcks";

	private static final String VERIF_DIR_NM = "verif";
	private static final String ISSUE_DIR_NM = "issue";
	private static final String CRRCY_DIR_NM = "crrcy";
	private static final String PASSE_DIR_NM = "passe";
	private static final String PRIVA_DIR_NM = "priva";
	private static final String SPENT_DIR_NM = "spent";
	private static final String RECEP_DIR_NM = "recep";
	private static final String NET_DIR_NM = "net";
	private static final String COIDS_DIR_NM = "coids";
	private static final String PAREF_DIR_NM = "paref";
	private static final String CHOSE_DIR_NM = "chose";

	// issue oper nm

	public static final String ISSUE_SPLIT_NM = "split_";
	public static final String ISSUE_JOIN_NM = "join_";

	// file pref

	private static final String PREF_USER_NAME_FNAM = "USER_NAME_";
	private static final String PREF_USER_GLID_FNAM = "USER_GLID_";

	// file names

	private static final String SELECT_LIST_FNAM = "selection.dat";
	private static final String MIN_CHANGE_FNAM = "min_change.dat";

	// names of files on data dir

	private static final String DENOMI_FNAM = "denomination.dat";
	private static final String SELECTED_CHANN_FNAM = "selected_channel.dat";

	private static final String USER_TXT_INFO_FNAM = "user_txt_info.txt";
	private static final String USER_INFO_FNAM = "user_info.dat";
	private static final String USER_IMAGE_FNAM = "user_image.dat";

	private static final String TRUSTED_FNAM = "glids_trusted_roots.lst";
	private static final String NOT_TRUSTED_FNAM = "glids_not_trusted_roots.lst";

	private static final String ALL_TRACKERS_FNAM = "glids_all_trackers.dat";
	private static final String NEXT_TRACKER_FNAM = "glid_next_tracker.dat";

	private static final String CURRENT_CHOICE_FNAM = "current_choice.dat";

	private static final String DBG_SKIP_FINISH_CHOMARKS_FNAM = "skip_finish_chomarks.dat";

	// TITLE NAMES

	// private static final String ROOT_PASSET = ".root";
	private static final String VERIFIED_PASSET = ".verified";
	private static final String GIVING_PASSET = ".giving";
	private static final String NEXT_PASSET = ".next";
	private static final String GIVED_PASSET = ".gived";

	public static final String the_user_title = "THE_USER";
	private static final String defaul_deno_title = "DEAFULT_DENO";

	private static final char FILE_ID_SEP = '.'; // must be first point in name
	private static final char CHOICE_SEP = '_'; // must be first underline in
												// name

	private static final String PRE_MIKID = "mikid_";
	private static final String PRE_ID = "i";
	private static final String PRE_CHO_DIR = "choice_";

	private static final String SUF_PASSE = "passet";
	private static final String SUF_PAREF = "paref";
	private static final String SUF_TRCKR = ".tracker";

	// non static

	private String working_mikid;
	private int working_net_kind;
	private int working_currency;

	private File work_dir;
	private File mikid_dir;
	private File data_dir;
	private File trkrs_dir;

	private nx_dir_base bb_dir;

	private File crrcy_dir;

	private File passe_dir;
	private File verif_dir;
	private File rcept_dir;
	private File paref_dir;
	private File chose_dir;

	private File issue_dir;

	private File priva_dir;
	private File spent_dir;

	private paccount local_paccount;
	private nx_conn_id remote_coid;

	public tag_person curr_user;
	private tag_trader local_trader;

	private boolean has_ck_issuer;
	private boolean is_pass_issuer;

	String trust_selecting;

	private trissuers trust_grps;

	public deno_counter deno_cter;

	private nx_std_coref my_glid;

	public paccount() {
		init_paccount();
	}

	public paccount(paccount orig) {
		init_paccount();

		if (orig == null) {
			return;
		}

		working_mikid = orig.working_mikid;
		working_net_kind = orig.working_net_kind;
		working_currency = orig.working_currency;

		work_dir = orig.work_dir;
		mikid_dir = orig.mikid_dir;
		data_dir = orig.data_dir;
		trkrs_dir = orig.trkrs_dir;

		if (orig.bb_dir != null) {
			bb_dir = new nx_dir_base(orig.bb_dir);
		}

		crrcy_dir = orig.crrcy_dir;

		passe_dir = orig.passe_dir;
		verif_dir = orig.verif_dir;
		rcept_dir = orig.rcept_dir;
		paref_dir = orig.paref_dir;
		chose_dir = orig.chose_dir;

		issue_dir = orig.issue_dir;

		priva_dir = orig.priva_dir;
		spent_dir = orig.spent_dir;

		curr_user = orig.curr_user;
		local_trader = orig.local_trader;

		local_paccount = orig.local_paccount;
		remote_coid = orig.remote_coid;

		reset_trusted_issuers();
		deno_cter = new deno_counter();

		my_glid = null;

		reset_paccount();
	}

	void init_paccount() {
		working_mikid = null;
		working_net_kind = net_funcs.NO_NET;
		working_currency = config.DEFAULT_CURRENCY;

		work_dir = null;
		mikid_dir = null;
		data_dir = null;
		trkrs_dir = null;

		bb_dir = null;

		crrcy_dir = null;

		passe_dir = null;
		verif_dir = null;
		rcept_dir = null;
		paref_dir = null;
		chose_dir = null;

		issue_dir = null;

		priva_dir = null;
		spent_dir = null;

		curr_user = null;
		local_trader = null;

		local_paccount = null;
		remote_coid = null;

		reset_trusted_issuers();
		deno_cter = new deno_counter();

		my_glid = null;

		reset_paccount();
	}

	private boolean has_user() {
		return (curr_user != null);
	}

	private void reset_paccount() {
		has_ck_issuer = false;
		is_pass_issuer = false;
	}

	void reset_trusted_issuers() {
		trust_selecting = null;

		trust_grps = new trissuers();
	}

	public static boolean is_user(String dir_nm, key_owner owr) {
		File b_dir = new File(dir_nm);
		return is_user(b_dir, owr);
	}

	public static boolean is_user(File dir_nm, key_owner owr) {
		if (!dir_nm.exists()) {
			return false;
		}
		File aux_dir = new File(dir_nm, get_mikid_dir_nm(owr.get_mikid()));
		if (!aux_dir.exists()) {
			return false;
		}
		return true;
	}

	public void set_base_dir(File dir, key_owner owr, int net_kind,
			gamal_generator gam) {

		set_base_dir(dir, owr.get_mikid(), net_kind);
		read_current_user(owr);

		nx_dir_base ba_dir = get_dir_base();
		if (gam == null) {
			ba_dir.start_gamal_sys(owr);
		} else {
			if (!ba_dir.has_gamal_sys(owr)) {
				ba_dir.save_gamal_sys(gam, owr);
			}
		}
	}

	private void set_base_dir(File dir, String mikid_str, int net_kind) {
		set_base_dir(dir, mikid_str, net_kind, true);
	}

	public static String get_mikid_dir_nm(String mikid_str) {
		return PRE_MIKID + mikid_str;
	}

	private void set_base_dir(File dir, String mikid_str, int net_kind,
			boolean is_local) {
		init_paccount();

		working_mikid = new String(mikid_str);
		working_net_kind = net_kind;

		work_dir = file_funcs.get_dir(dir);
		mikid_dir = file_funcs.get_dir(work_dir,
				get_mikid_dir_nm(working_mikid));
		data_dir = file_funcs.get_dir(mikid_dir, DATA_DIR_NM);

		if (is_local) {
			trkrs_dir = file_funcs.get_dir(mikid_dir, TRCKS_DIR_NM);

			File nn_dir = new File(mikid_dir, NET_DIR_NM);
			bb_dir = new nx_dir_base(nn_dir, working_net_kind);
		}
	}

	public nx_dir_base get_dir_base() {
		if (bb_dir == null) {
			throw new bad_netmix(2);
		}
		return bb_dir;
	}

	public static File get_coids_dir_in(File mikid_dd) {
		if (mikid_dd == null) {
			throw new bad_passet(2);
		}
		File dd = new File(mikid_dd, COIDS_DIR_NM);
		return dd;
	}

	public File get_currency_dir() {
		if (crrcy_dir == null) {
			throw new bad_passet(2);
		}
		return crrcy_dir;
	}

	public File get_coid_usr_dir(String coid) {
		if (coid == null) {
			throw new bad_passet(2);
		}
		File usr_coid_dir = new File(bb_dir.get_all_coids_base_dir(), coid);
		File usr_dir = new File(usr_coid_dir, get_working_mikid());
		return usr_dir;
	}

	public void set_working_currency(int currcy_idx) {
		set_working_currency(currcy_idx, null);
	}

	public void set_working_currency(int currcy_idx, paccount l_pcc) {
		init_currency_dir(currcy_idx, l_pcc);
	}

	private void init_currency_dir(int currcy_idx, paccount l_pcc) {
		if (mikid_dir == null) {
			throw new bad_passet(2);
		}
		if (!mikid_dir.exists()) {
			throw new bad_passet(2);
		}

		if (!tag_denomination.is_valid_currency(currcy_idx)) {
			currcy_idx = config.DEFAULT_CURRENCY;
		}

		if ((l_pcc != null) && (l_pcc.get_working_currency() != currcy_idx)) {
			currcy_idx = l_pcc.get_working_currency();
		}

		working_currency = currcy_idx;
		String deno = iso.get_currency_code(working_currency);

		File all_currcy_dir = file_funcs.get_dir(mikid_dir, CRRCY_DIR_NM);
		crrcy_dir = file_funcs.get_dir(all_currcy_dir, deno);

		chose_dir = file_funcs.get_dir(crrcy_dir, CHOSE_DIR_NM);

		if (l_pcc == null) {
			passe_dir = file_funcs.get_dir(crrcy_dir, PASSE_DIR_NM);
			rcept_dir = file_funcs.get_dir(crrcy_dir, RECEP_DIR_NM);
			verif_dir = file_funcs.get_dir(crrcy_dir, VERIF_DIR_NM);
			paref_dir = file_funcs.get_dir(crrcy_dir, PAREF_DIR_NM);
			issue_dir = file_funcs.get_dir(crrcy_dir, ISSUE_DIR_NM);
			priva_dir = file_funcs.get_dir(crrcy_dir, PRIVA_DIR_NM);
			spent_dir = file_funcs.get_dir(crrcy_dir, SPENT_DIR_NM);
		} else {
			passe_dir = l_pcc.passe_dir;
			rcept_dir = l_pcc.rcept_dir;
			verif_dir = l_pcc.verif_dir;
			paref_dir = null;
			issue_dir = null;
			priva_dir = l_pcc.priva_dir;
			spent_dir = l_pcc.spent_dir;
		}

		reset_trusted_issuers();
		deno_cter.reset_deno_counts(currcy_idx);
	}

	public boolean has_currency() {
		boolean c1 = ((crrcy_dir != null) && crrcy_dir.exists());
		return c1;
	}

	private paccount get_sub_paccount_in(File base_dir) {
		paccount pcc2 = new paccount();
		pcc2.set_base_dir(base_dir, working_mikid, working_net_kind, false);
		pcc2.init_currency_dir(working_currency, this);
		pcc2.local_trader = new tag_trader();

		return pcc2;
	}

	public paccount get_sub_paccount(nx_conn_id coid) {
		File base_dir = bb_dir.get_remote_nx_dir(coid);
		paccount pcc = get_sub_paccount_in(base_dir);
		pcc.local_paccount = this;
		pcc.remote_coid = new nx_conn_id(coid.as_long());
		return pcc;
	}

	private static boolean is_mikid_dir(File ff) {
		if (ff.isDirectory()) {
			if (ff.getName().startsWith(PRE_MIKID)) {
				return true;
			}
		}
		return false;
	}

	static FileFilter get_mikid_filter() {
		FileFilter ffil = new FileFilter() {
			public boolean accept(File ff) {
				return is_mikid_dir(ff);
			}
		};
		return ffil;
	}

	List<File> get_all_mikid_dirs() {
		File w_dir = get_working_dir();
		File[] all_mikids = w_dir.listFiles(get_mikid_filter());
		if (all_mikids == null) {
			return new ArrayList<File>();
		}
		List<File> all_dd = Arrays.asList(all_mikids);
		return all_dd;
	}

	private List<String> get_all_paref_denos() {
		List<String> all_txt_denos = new ArrayList<String>();
		File[] all_deno = get_paref_dir().listFiles();
		for (File ff : all_deno) {
			String deno_txt = ff.getName();
			all_txt_denos.add(deno_txt);
		}
		return all_txt_denos;
	}

	private static String get_file_id_str_from(File pss) {
		try {
			char the_sep = FILE_ID_SEP;
			String pth = pss.getName();
			int idx_sep = pth.indexOf(the_sep);
			if (idx_sep == -1) {
				throw new bad_passet(2, String.format(
						L.internal_error_bad_ref_file, pss));
			}
			String id_str = pth.substring(idx_sep + 1);
			int idx_sep_2 = id_str.indexOf(the_sep);
			if (idx_sep_2 != -1) {
				id_str = id_str.substring(0, idx_sep_2);
			}
			if (id_str.equals(tag_input.NO_PASSET_ID.get_str())) {
				throw new bad_passet(2, "BAD_FILE_NAME=" + pss
						+ " NO_PASSET_ID_STR="
						+ tag_input.NO_PASSET_ID.get_str());
			}
			return id_str;
		} catch (IndexOutOfBoundsException ex1) {
			throw new bad_passet(2);
		}
	}

	static String get_joined_file_name(String file_id_str, String sufi) {
		String ff_nm = PRE_ID + FILE_ID_SEP + file_id_str + FILE_ID_SEP + sufi;
		return ff_nm;
	}

	private static String get_passet_file_name(String ff_id_str) {
		String full_nm = get_joined_file_name(ff_id_str, SUF_PASSE);
		String sld_pth = file_funcs.as_sliced_file_path(full_nm);
		return sld_pth;
	}

	private File get_paref_dir() {
		if (paref_dir == null) {
			throw new bad_passet(2);
		}
		return paref_dir;
	}

	private File get_chose_dir() {
		if (chose_dir == null) {
			throw new bad_passet(2);
		}
		return chose_dir;
	}

	private String get_deno_path(tag_denomination deno) {
		String txt_deno = deno.get_short_text_denomination(false);
		char sep = File.separatorChar;
		String pth = get_paref_dir().getPath() + sep + txt_deno;
		return pth;
	}

	private List<String> get_all_trissuers_of_deno(tag_denomination deno) {
		File deno_dir = new File(get_deno_path(deno));
		if (!deno_dir.exists()) {
			return new ArrayList<String>();
		}

		File[] all_dd = deno_dir.listFiles();
		List<String> all_iss = file_funcs.get_file_names(all_dd);
		return all_iss;
	}

	private File get_paref_full_dir(tag_transfer dat) {
		String tss_nm = dat.get_root_issuer().toString();
		tag_denomination deno = dat.get_out_amount();
		String deno_pth = get_deno_path(deno);
		File full_ff = new File(deno_pth, tss_nm);
		return full_ff;
	}

	private File get_paref_file(tag_transfer curr_trans) {
		if (is_remote_paccount()) {
			throw new bad_passet(2);
		}

		if (curr_trans == null) {
			throw new bad_passet(2);
		}

		if (!curr_trans.has_tracker_accoglid()) {
			throw new bad_passet(2);
		}

		File parf_dd = get_paref_full_dir(curr_trans);
		String ff_id_str = curr_trans.get_file_id();

		String f_nm = get_passet_file_name(ff_id_str);
		String r_nm = f_nm + SUF_PAREF;
		File ff = new File(parf_dd, r_nm);
		return ff;
	}

	private File get_passet_file_with(String ff_id) {
		String f_nm = get_passet_file_name(ff_id);
		File ff = new File(get_passet_dir(), f_nm);
		return ff;
	}

	private List<File> get_all_passet_files_with(List<String> all_ids) {
		List<File> all_pss = new ArrayList<File>();
		for (String id : all_ids) {
			File pss_ff = get_passet_file_with(id);
			all_pss.add(pss_ff);
		}
		return all_pss;
	}

	private List<String> get_all_passids_of(List<File> all_pss) {
		List<String> all_ids = new ArrayList<String>();
		for (File pss_ff : all_pss) {
			String pss_id = get_file_id_str_from(pss_ff);
			all_ids.add(pss_id);
		}
		return all_ids;
	}

	File get_passet_file(tag_transfer curr_trans) {
		return get_passet_file_with(curr_trans.get_file_id());
	}

	private File get_verif_file(String ff_id) {
		String f_nm = get_passet_file_name(ff_id);
		File ff = new File(get_verif_dir(), f_nm);
		return ff;
	}

	// next_passet funcs

	private File get_next_passet_ref_file(File pss_ff) {
		String full_pth = pss_ff.getPath() + NEXT_PASSET;
		File ff = new File(full_pth);
		return ff;
	}

	private boolean has_next_passet_ref_file(File pss_ff) {
		File rr_ff = get_next_passet_ref_file(pss_ff);
		boolean hh = rr_ff.exists();
		if (IN_DEBUG_27) {
			if (hh) {
				logger.info("has_next_passet_ref_file. FOUND_NEXT_FILE="
						+ rr_ff);
			}
		}
		return hh;
	}

	private List<tag_transfer> get_next_passet_val_transfers(File pss_ff,
			key_owner owr, List<tag_transfer> all_in_tra) {

		bad_emetcode err_vrf = try_verify_passet(pss_ff, all_in_tra, owr);
		if (err_vrf != null) {
			return new ArrayList<tag_transfer>();
		}

		File ref_ff = get_next_passet_ref_file(pss_ff);
		List<String> all_nxt_ids = file_funcs.read_list_file(ref_ff, owr);
		if (all_nxt_ids.isEmpty()) {
			throw new bad_passet(2);
		}
		int lst_id_idx = all_nxt_ids.size() - 1;
		String ff_ck_str = all_nxt_ids.remove(lst_id_idx);

		File base_dir = get_local_working_dir();
		String ck_str = file_funcs.get_rel_path(base_dir, ref_ff);
		if (!ff_ck_str.equals(ck_str)) {
			throw new bad_passet(2);
		}

		List<File> all_nxt_ff = get_all_passet_files_with(all_nxt_ids);
		List<tag_transfer> all_nx_tra = new ArrayList<tag_transfer>();
		try_verif_all_passets(all_nxt_ff, all_nx_tra, owr);
		return all_nx_tra;
	}

	private void set_next_passet_val_files(File pss_ff, key_owner owr,
			List<File> all_nxt_pss_ff) {
		File ref_ff = get_next_passet_ref_file(pss_ff);
		List<String> all_ids = get_all_passids_of(all_nxt_pss_ff);

		File base_dir = get_local_working_dir();
		String ck_str = file_funcs.get_rel_path(base_dir, ref_ff);
		all_ids.add(ck_str);
		file_funcs.write_list_file(ref_ff, owr, all_ids);
		if (IN_DEBUG_28) {
			logger.info("WRITED_NXT_PSS=" + ref_ff);
		}
	}

	private File get_next_passet_val_file(File pss_ff, key_owner owr,
			List<tag_transfer> all_in_tra) {
		int prv_sz = 0;
		if (all_in_tra != null) {
			prv_sz = all_in_tra.size();
		}
		List<tag_transfer> all_nx_tra = get_next_passet_val_transfers(pss_ff,
				owr, all_in_tra);
		if (all_nx_tra.size() != 1) {
			if (IN_DEBUG_28) {
				logger.info("get_next_passet_val_file. INVALID_NXT_TRA"
						+ all_nx_tra.toString());
			}
			throw new bad_passet(2);
		}
		tag_transfer nxt_tra = all_nx_tra.get(0);
		File out_pss_ff = get_passet_file(nxt_tra);

		if (all_in_tra != null) {
			// check ids
			if (all_in_tra.size() != (prv_sz + 1)) {
				throw new bad_passet(2);
			}
			int l_idx = all_in_tra.size() - 1;
			tag_transfer in_tra = all_in_tra.get(l_idx);
			tag_filglid in_id = in_tra.get_transfer_id();
			tag_filglid out_prv_id = nxt_tra.get_prev_tra_id();
			if (!in_id.equals(out_prv_id)) {
				tag_filglid out_id = nxt_tra.get_transfer_id();
				throw new bad_passet(2, "in_id=" + in_id + " out_prv_id="
						+ out_prv_id + " out_id=" + out_id + "\n in_ff="
						+ pss_ff + "\n out_ff=" + out_pss_ff);
			}
		}

		return out_pss_ff;
	}

	private void set_next_passet_val_file(File pss_ff, key_owner owr,
			File nxt_pss_ff) {
		List<File> nxt_lst = new ArrayList<File>();
		nxt_lst.add(nxt_pss_ff);
		set_next_passet_val_files(pss_ff, owr, nxt_lst);
	}

	private List<File> all_to_change(List<File> all_in_ff, key_owner owr,
			Set<tag_transfer> all_nxt_tra) {
		all_nxt_tra.clear();

		List<File> all_go = new ArrayList<File>();
		for (File ff : all_in_ff) {
			if (has_next_passet_ref_file(ff)) {
				List<tag_transfer> all_nxt = get_next_passet_val_transfers(ff,
						owr, null);
				if (all_nxt.isEmpty()) {
					throw new bad_passet(2);
				}
				all_nxt_tra.addAll(all_nxt);
			} else {
				all_go.add(ff);
			}
		}
		return all_go;
	}

	private File get_issuance_file(tag_transfer curr_trans) {
		String f_nm = get_passet_file_name(curr_trans.get_issuance_file_id());
		File ff = new File(get_issuance_dir(), f_nm);
		file_funcs.mk_parent_dir(ff);
		return ff;
	}

	public static List<String> get_passids(List<tag_transfer> all_iss_dat) {
		List<String> all_ids = new ArrayList<String>();
		for (tag_transfer dat : all_iss_dat) {
			all_ids.add(dat.get_file_id());
		}
		return all_ids;
	}

	public File get_passet_dir() {
		if (passe_dir == null) {
			throw new bad_passet(2);
		}
		return passe_dir;
	}

	public File get_verif_dir() {
		if (verif_dir == null) {
			throw new bad_passet(2);
		}
		return verif_dir;
	}

	File get_min_change_file() {
		File ff = new File(get_currency_dir(), MIN_CHANGE_FNAM);
		return ff;
	}

	private File get_issuance_dir() {
		if (issue_dir == null) {
			throw new bad_passet(2);
		}
		return issue_dir;
	}

	File get_pvks_dir() {
		if (priva_dir == null) {
			throw new bad_passet(2);
		}
		return priva_dir;
	}

	public File get_recep_dir() {
		if (rcept_dir == null) {
			throw new bad_passet(2);
		}
		return rcept_dir;
	}

	private File get_receptacle_file(tag_transfer curr_trans) {
		File ff = curr_trans.get_recep_file(get_recep_dir());
		if (IN_DEBUG_22) {
			String stk_str = logger.get_stack_str();
			logger.info("receptacle_file=" + ff + "\n" + stk_str);
		}
		return ff;
	}

	public File get_data_dir() {
		if (data_dir == null) {
			throw new bad_passet(2);
		}
		return data_dir;
	}

	private File get_trackers_dir() {
		if (trkrs_dir == null) {
			throw new bad_passet(2);
		}
		return trkrs_dir;
	}

	private File get_trissuer_trakers_dir(String rem_iss_glid) {
		File iss_trks_dd = new File(get_trackers_dir(), rem_iss_glid);
		return iss_trks_dd;
	}

	private File get_tracker_file(String rem_iss_glid, String trckr_glid) {
		File iss_dd = get_trissuer_trakers_dir(rem_iss_glid);
		String sld_dir = file_funcs.as_sliced_file_path(trckr_glid) + SUF_TRCKR;
		File full_pth = new File(iss_dd, sld_dir);
		return full_pth;
	}

	public File get_trusted_file() {
		File ff = new File(get_data_dir(), TRUSTED_FNAM);
		return ff;
	}

	public File get_not_trusted_file() {
		File ff = new File(get_data_dir(), NOT_TRUSTED_FNAM);
		return ff;
	}

	public File get_all_trackers_file() {
		File ff = new File(get_data_dir(), ALL_TRACKERS_FNAM);
		return ff;
	}

	public File get_next_tracker_file() {
		File ff = new File(get_data_dir(), NEXT_TRACKER_FNAM);
		return ff;
	}

	private File get_current_choice_file() {
		File ff = new File(get_data_dir(), CURRENT_CHOICE_FNAM);
		return ff;
	}

	public File get_dbg_no_end_choice_file() {
		File ff = new File(get_data_dir(), DBG_SKIP_FINISH_CHOMARKS_FNAM);
		return ff;
	}

	public File get_selected_channel_file() {
		File f_selec = new File(get_data_dir(), SELECTED_CHANN_FNAM);
		return f_selec;
	}

	public File get_deno_file() {
		File usr_dat = new File(get_data_dir(), DENOMI_FNAM);
		return usr_dat;
	}

	public File get_selection_file() {
		File f_selec = new File(get_currency_dir(), SELECT_LIST_FNAM);
		return f_selec;
	}

	public tag_denomination read_deno_file() {
		File u_ff = get_deno_file();
		List<String> deno_lines = parse.read_lines(u_ff);

		tag_denomination deno = new tag_denomination();
		if (deno_lines == null) {
			return null;
		}
		deno.init_denomination_with(deno_lines, defaul_deno_title);
		return deno;
	}

	public List<String> write_deno_file(tag_denomination deno) {
		File u_ff = get_deno_file();
		List<String> deno_lines = deno
				.get_denomination_lines(defaul_deno_title);
		parse.write_lines(u_ff, deno_lines);
		return deno_lines;
	}

	private static File get_data_file_in(File mikid_dd, String ff_nm) {
		if (mikid_dd == null) {
			throw new bad_passet(2);
		}
		if (!is_mikid_dir(mikid_dd)) {
			throw new bad_passet(2);
		}
		File data_dd = new File(mikid_dd, DATA_DIR_NM);
		File data_ff = new File(data_dd, ff_nm);
		return data_ff;
	}

	private static File get_user_txt_info_file_in(File mikid_dd) {
		return get_data_file_in(mikid_dd, USER_TXT_INFO_FNAM);
	}

	private static File get_user_info_file_in(File mikid_dd) {
		return get_data_file_in(mikid_dd, USER_INFO_FNAM);
	}

	private void init_local_trader(key_owner owr) {
		paccount pcc = local_paccount;
		if (pcc == null) {
			pcc = this;
		}

		nx_std_coref loc_glid = pcc.bb_dir.get_local_glid(owr);

		local_trader = new tag_trader(loc_glid);

		if (IN_DEBUG_4) {
			if (!local_trader.has_valid_glid()) {
				logger.info("\n\n\n\n\n\n\n\n\n\n\n\n\n\n" + "BAD_READ_gli="
						+ local_trader.tr_glid.the_glid + "\nloc_glid="
						+ loc_glid);
			}
		}
	}

	void read_current_user(key_owner owr) {
		File u_ff = get_current_user_info_file();
		curr_user = read_user(u_ff, owr);

		init_local_trader(owr);
	}

	private static tag_person read_user(File usr_ff, key_owner owr) {
		List<String> usr_lines = parse.read_encrypted_lines(usr_ff, owr);
		if (usr_lines == null) {
			throw new bad_passet(2);
		}

		tag_person per = new tag_person();
		if (!usr_lines.isEmpty()) {
			per.init_person_with(usr_lines, the_user_title);
		}
		return per;
	}

	static tag_person read_user_txt_info_in(File mikid_dd) {
		return read_user(get_user_txt_info_file_in(mikid_dd), null);
	}

	static tag_person read_user_info_in(File mikid_dd, key_owner owr) {
		return read_user(get_user_info_file_in(mikid_dd), owr);
	}

	public File get_current_user_info_file() {
		if (mikid_dir == null) {
			throw new bad_passet(2);
		}
		return get_user_info_file_in(mikid_dir);
	}

	public static File get_user_image_file_in(File mikid_dd) {
		return get_data_file_in(mikid_dd, USER_IMAGE_FNAM);
	}

	public File get_current_user_image_file() {
		return get_user_image_file_in(mikid_dir);
	}

	private void write_infor(String nm, String tss_str) {
		String f_nm = PREF_USER_NAME_FNAM + file_funcs.fix_file_name(nm);
		String f_tss = PREF_USER_GLID_FNAM + file_funcs.fix_file_name(tss_str);
		File inf_nm = new File(get_mikid_dir(), f_nm);
		File inf_tss = new File(get_mikid_dir(), f_tss);
		mem_file.concurrent_create_file(inf_nm);
		mem_file.concurrent_create_file(inf_tss);
	}

	public List<String> write_current_user(key_owner owr) {
		assert (mikid_dir != null);
		assert (curr_user != null);
		tag_person per = curr_user;

		List<String> usr_lines = per.get_person_lines(the_user_title);

		File usr_dat = get_current_user_info_file();
		parse.write_encrypted_lines(usr_dat, owr, usr_lines);

		File usr_txt = get_user_txt_info_file_in(get_mikid_dir());
		parse.write_encrypted_lines(usr_txt, null, usr_lines);

		String nm = per.legal_name;
		String dom = per.network_domain_name;
		write_infor(nm, dom);

		return usr_lines;
	}

	public tag_person get_user_data(key_owner owr) {
		File usr_dat = get_current_user_info_file();
		return read_user(usr_dat, owr);
	}

	public void update_infor(key_owner owr) {
		tag_person per = get_user_data(owr);
		String nm = per.legal_name;
		String dom = per.network_domain_name;
		write_infor(nm, dom);
	}

	public File get_mikid_dir() {
		if (mikid_dir == null) {
			throw new bad_passet(2);
		}
		return mikid_dir;
	}

	public File get_working_dir() {
		if (work_dir == null) {
			throw new bad_passet(2);
		}
		return work_dir;
	}

	private File get_local_working_dir() {
		if (is_remote_paccount()) {
			return local_paccount.get_working_dir();
		}
		return get_working_dir();
	}

	private paccount get_local_paccount() {
		if (is_remote_paccount()) {
			return local_paccount;
		}
		return this;
	}

	public int get_working_currency() {
		return working_currency;
	}

	private String get_working_mikid() {
		return working_mikid;
	}

	@SuppressWarnings("unused")
	public void issue_passets(int num_pss, key_owner owr,
			tag_denomination deno, List<tag_transfer> all_tra,
			tag_accoglid nxt_trk) {

		if (deno.currency_idx != working_currency) {
			set_working_currency(deno.currency_idx);
		}
		if (!has_user()) {
			read_current_user(owr);
		}

		bad_passet bb1 = null;

		if (!owr.has_secret()) {
			throw new bad_passet(2);
		}

		try {
			List<bad_emetcode> all_bad = new ArrayList<bad_emetcode>();
			for (int aa = 0; aa < num_pss; aa++) {
				issue_passet(all_bad, all_tra, owr, deno, nxt_trk);
			}
			if (!all_bad.isEmpty()) {
				for (bad_emetcode err : all_bad) {
					logger.error(err, "Verification FAILED");
				}
				throw new bad_passet(2);
			}

			return;

		} catch (bad_passet bb) {
			bb1 = bb;
		}

		if (bb1 == null) {
			bb1 = new bad_passet(2);
		}
		throw bb1;
	}

	public List<bad_emetcode> try_verif_all_passets(Collection<File> all_pss,
			List<tag_transfer> all_iss, key_owner owr) {
		List<bad_emetcode> all_bad = new ArrayList<bad_emetcode>();
		for (File pss : all_pss) {
			bad_emetcode err = try_verify_passet(pss, all_iss, owr);
			if (err != null) {
				all_bad.add(err);
			}
		}
		return all_bad;
	}

	File try_make_receptacle(File pss_ff, key_owner owr,
			List<tag_transfer> all_iss, paccount recv_pcc) {
		try {
			File recep = make_receptacle(pss_ff, owr, all_iss, recv_pcc);
			return recep;
		} catch (bad_emetcode ex) {
			if (IN_DEBUG_24) {
				logger.error(ex, "\n file=" + pss_ff);
			}
		}
		return null;
	}

	private File make_root_receptacle(File pss_ff, key_owner owr,
			tag_accoglid nxt_trk) {

		tag_transfer last_trans = get_first_transfer(pss_ff);
		if (!is_issuer(last_trans)) {
			throw new bad_passet(2, L.cannot_make_receptacle);
		}
		return make_receptacle_for(last_trans, -1, owr, nxt_trk);
	}

	private File make_receptacle(File pss_ff, key_owner owr,
			List<tag_transfer> all_iss, paccount recv_pcc) {
		if (recv_pcc == null) {
			throw new bad_passet(2, L.cannot_make_receptacle);
		}
		tag_transfer last_trans = verify_passet(pss_ff, all_iss, owr, recv_pcc);
		return make_receptacle_for(last_trans, -1, owr, null);

	}

	private File make_receptacle_for(tag_transfer last_trans, int prev_idx,
			key_owner owr, tag_accoglid nxt_trk) {

		long prt = last_trans.get_rcver_tm();
		long pgt = last_trans.get_giver_tm();

		if (IN_DEBUG_4) {
			if (!local_trader.has_valid_glid()) {
				logger.info("\n\n\n\n\n\n\n\nNO_gli="
						+ local_trader.tr_glid.the_glid);
			}
		}

		tag_transfer curr_trans = new tag_transfer();
		curr_trans.init_with_prev(last_trans, prev_idx);
		curr_trans.reset_description();

		File the_recp_nm = get_receptacle_file(curr_trans);
		if (the_recp_nm.exists()) {
			return the_recp_nm;
		}

		if (nxt_trk != null) {
			curr_trans.init_tracker_accoglid(nxt_trk);
		}

		List<String> recp_lines = new ArrayList<String>();
		curr_trans.add_until_outputs_lines(local_trader, owr, recp_lines);

		long rt = curr_trans.get_rcver_tm();
		tag_transfer.check_before_time(prt, rt, false);
		tag_transfer.check_before_time(pgt, rt, false);

		parse.write_lines(the_recp_nm, recp_lines);

		curr_trans.save_rcv_kbox_pvks_in_dir(get_pvks_dir(), owr);

		if (config.DEBUG) {
			logger.debug("RECEPTACLE_OK__"
					+ curr_trans.get_out_amount().get_number_denomination());
		}
		return the_recp_nm;
	}

	public bad_emetcode try_check_mine(File pss_ff, List<tag_transfer> all_iss,
			key_owner owr, paccount recv_pcc) {
		try {
			check_mine(pss_ff, all_iss, owr, recv_pcc);
			return null;
		} catch (bad_emetcode ex) {
			if (IN_DEBUG_24) {
				String val = "";
				if ((all_iss != null) && !all_iss.isEmpty()) {
					int l_ix = all_iss.size() - 1;
					val = all_iss.get(l_ix).toString();
				}
				logger.error(ex, "\n" + val + " file=" + pss_ff);
			}
			return ex;
		}
	}

	private boolean ck_receiver_is_me(tag_transfer curr_trans, key_owner owr) {
		tag_accoglid pcc_gld = new tag_accoglid(get_glid(owr));
		tag_accoglid rcvr_gld = curr_trans.get_receiver_accoglid();
		if (!pcc_gld.equals(rcvr_gld)) {
			return false;
		}
		return true;
	}

	private tag_transfer check_mine(File pss_ff, List<tag_transfer> all_iss,
			key_owner owr, paccount recv_pcc) {

		tag_transfer curr_trans = verify_passet(pss_ff, all_iss, owr, recv_pcc);
		paccount loc_pcc = get_local_paccount();
		if (!loc_pcc.ck_receiver_is_me(curr_trans, owr)) {
			throw new bad_passet(2);
		}

		if (has_next_passet_ref_file(pss_ff)) {
			List<tag_transfer> all_nxt = get_next_passet_val_transfers(pss_ff,
					owr, null);
			if (all_nxt.isEmpty()) {
				throw new bad_passet(2);
			}
			return curr_trans;
		}

		curr_trans.check_mine_with(spent_dir, owr, get_pvks_dir());
		if (IN_DEBUG_15) {
			if (!curr_trans.has_rcv_boxes(true)) {
				throw new bad_passet(2);
			}
			logger.debug("CHECK_MINE_OK__ff=" + pss_ff);
		}

		loc_pcc.save_paref_file_for(curr_trans, owr);

		return curr_trans;
	}

	File try_sign_receptacle(File pss_ff, key_owner owr,
			List<tag_transfer> all_in_tra) {
		try {
			return sign_receptacle(pss_ff, owr, all_in_tra);
		} catch (bad_emetcode ex) {
			if (IN_DEBUG_24) {
				logger.error(ex, "\n file=" + pss_ff);
			}
		}
		return null;
	}

	private File sign_root_receptacle(File pss_ff, key_owner owr,
			tag_transfer[] out_ref) {
		tag_transfer last_trans = get_first_transfer(pss_ff);
		if (!is_issuer(last_trans)) {
			throw new bad_passet(2, L.cannot_make_receptacle);
		}
		last_trans.check_mine_with(spent_dir, owr, get_pvks_dir());
		File sgned_ff = sign_receptacle_for(last_trans, -1, owr, out_ref);
		return sgned_ff;
	}

	private File sign_receptacle(File pss_ff, key_owner owr,
			List<tag_transfer> all_in_tra) {

		if (has_next_passet_ref_file(pss_ff)) {
			return get_next_passet_val_file(pss_ff, owr, all_in_tra);
		}

		tag_transfer last_trans = check_mine(pss_ff, null, owr, null);
		if (all_in_tra != null) {
			all_in_tra.add(last_trans);
		}
		File out_ff = sign_receptacle_for(last_trans, -1, owr, null);

		set_next_passet_val_file(pss_ff, owr, out_ff);
		return out_ff;
	}

	private File save_passet_for(tag_transfer curr_trans, key_owner owr,
			tag_transfer[] out_ref) {

		if ((out_ref != null) && (out_ref.length == 1)) {
			out_ref[0] = curr_trans;
		}

		File nxt_pss_ff = get_passet_file(curr_trans);

		file_funcs.mk_parent_dir(nxt_pss_ff);
		file_funcs.concurrent_delete_file(nxt_pss_ff);

		parse.write_lines(nxt_pss_ff, curr_trans.get_lines());

		paccount pcc2 = new paccount(this);
		pcc2.verify_passet(nxt_pss_ff, null, owr);

		curr_trans.spend_in(spent_dir, get_pvks_dir());

		if (IN_DEBUG_18) {
			logger.debug("SIGNED_RECEP_OK__file=\n" + nxt_pss_ff);
		}

		return nxt_pss_ff;
	}

	private File sign_receptacle_for(tag_transfer last_trans, int prev_idx,
			key_owner owr, tag_transfer[] out_ref) {

		// File prv_pss_ff = get_passet_file(last_trans);

		long prt = last_trans.get_rcver_tm();
		long pgt = last_trans.get_giver_tm();

		tag_transfer curr_trans = new tag_transfer();
		curr_trans.init_with_prev(last_trans, prev_idx);
		curr_trans.reset_description();

		File recp_nm = get_receptacle_file(curr_trans);

		if (!recp_nm.exists()) {
			throw new bad_passet(2, String.format(L.no_such_receptacle_file,
					recp_nm));
		}
		List<String> recp_lines = parse.read_lines(recp_nm);
		if (recp_lines == null) {
			throw new bad_passet(2, String.format(L.bad_receptacle_file,
					recp_nm));
		}

		curr_trans.init_until_outputs_with_lines(recp_lines);
		curr_trans.add_signa_lines(local_trader, recp_lines);

		long rt = curr_trans.get_rcver_tm();
		long gt = curr_trans.get_giver_tm();
		tag_transfer.check_before_time(prt, gt, false);
		tag_transfer.check_before_time(pgt, gt, false);
		tag_transfer.check_before_time(rt, gt, true);

		File out_ff = save_passet_for(curr_trans, owr, out_ref);

		file_funcs.concurrent_delete_file(recp_nm);
		if (IN_DEBUG_18) {
			logger.debug("deleted recp_nm=" + recp_nm);
		}

		return out_ff;
	}

	public SortedSet<File> make_receptacles_for(Collection<File> all_pss,
			key_owner owr, List<tag_transfer> all_iss, List<File> all_recep,
			paccount recv_pcc) {
		if (!has_user()) {
			read_current_user(owr);
		}

		if (all_iss != null) {
			all_iss.clear();
		}
		if (all_recep != null) {
			all_recep.clear();
		}

		SortedSet<File> all_bad = new TreeSet<File>();
		for (File the_pss : all_pss) {
			File recep = try_make_receptacle(the_pss, owr, all_iss, recv_pcc);
			if (recep == null) {
				all_bad.add(the_pss);
			} else {
				if (all_recep != null) {
					all_recep.add(recep);
				}
			}
		}
		return all_bad;
	}

	public SortedSet<File> try_check_mine_for(Collection<File> all_pss,
			key_owner owr, List<tag_transfer> all_iss, paccount recv_pcc) {
		if (!has_user()) {
			read_current_user(owr);
		}
		SortedSet<File> all_bad = new TreeSet<File>();
		for (File the_pss : all_pss) {
			bad_emetcode err_1 = try_check_mine(the_pss, all_iss, owr, recv_pcc);
			if (err_1 != null) {
				all_bad.add(the_pss);
			}
		}
		return all_bad;
	}

	public List<File> try_sign_receptacles_for(Collection<File> all_pss,
			key_owner owr, List<tag_transfer> all_in_tra) {
		if (!has_user()) {
			read_current_user(owr);
		}
		List<File> all_trans = new ArrayList<File>();
		for (File the_pss : all_pss) {
			File nxt_pss_ff = try_sign_receptacle(the_pss, owr, all_in_tra);
			if (nxt_pss_ff != null) {
				all_trans.add(nxt_pss_ff);
			}
		}
		return all_trans;
	}

	public static List<tag_transfer> get_all_first_passets(List<File> pss_ff) {
		List<tag_transfer> all_iss_dat = new ArrayList<tag_transfer>(
				pss_ff.size());
		for (File ff : pss_ff) {
			tag_transfer dat = try_get_first_passet(ff);
			if (dat != null) {
				all_iss_dat.add(dat);
			}
		}
		return all_iss_dat;
	}

	private static tag_transfer try_get_first_passet(File pss_ff) {
		try {
			tag_transfer iss_dat = get_first_transfer(pss_ff);
			return iss_dat;
		} catch (bad_emetcode ex) {
			if (IN_DEBUG_24) {
				logger.error(ex, "\n file=" + pss_ff);
			}
		}
		return null;
	}

	private static tag_transfer get_first_transfer(File pss_ff) {
		if (IN_DEBUG_23) {
			logger.info("reading_first_tra file=" + pss_ff);
		}
		List<String> core_lines = parse.read_issuance_lines(pss_ff);

		tag_transfer iss_dat = new tag_transfer();
		iss_dat.init_with_lines(core_lines);

		String id_str_ff = get_file_id_str_from(pss_ff);
		String id_str_tra = iss_dat.get_file_id();
		if (!id_str_ff.equals(id_str_tra)) {
			throw new bad_passet(2, "id_file=" + id_str_ff + " DIFFERENT_THAN="
					+ id_str_tra);
		}
		iss_dat.check_denos();

		return iss_dat;
	}

	private boolean is_remote_paccount() {
		return (local_paccount != null);
	}

	private boolean is_remote_of(paccount loc_pcc) {
		if (loc_pcc == null) {
			throw new bad_passet(2);
		}
		File remote_dir = get_passet_dir();
		File local_dir = loc_pcc.get_mikid_dir();
		boolean is_sub = remote_dir.getPath().startsWith(local_dir.getPath());
		return is_sub;
	}

	public channel read_selected_channel(key_owner owr) {
		File ff = get_selected_channel_file();
		String vv = mem_file.read_encrypted_string(ff, owr);
		if (vv == null) {
			if (IN_DEBUG_26) {
				logger.info("SEL_CHN.Cannot read ff=" + ff);
			}
			return null;
		}
		long cnid = convert.try_parse_long(vv);
		if (cnid == 0) {
			if (IN_DEBUG_26) {
				logger.info("SEL_CHN.Cannot parse=" + vv);
			}
			return null;
		}
		nx_conn_id the_coid = new nx_conn_id(cnid);

		channel chn = channel.read_channel(this, owr, the_coid);
		if (chn == null) {
			if (IN_DEBUG_26) {
				logger.info("SEL_CHN.Cannot create channel for r_coid=\n"
						+ the_coid);
			}
			return null;
		}
		if (chn.coid == null) {
			if (IN_DEBUG_26) {
				logger.info("SEL_CHN.Null coid for created channel r_coid=\n"
						+ the_coid);
			}
			return null;
		}
		return chn;
	}

	public void write_selected_channel(channel channel, key_owner owr) {
		File ff = get_selected_channel_file();
		mem_file.write_encrypted_string(ff, owr, channel.coid.toString());
	}

	public bad_emetcode try_verify_passet(File pss_ff,
			List<tag_transfer> all_iss, key_owner owr) {
		try {
			verify_passet(pss_ff, all_iss, owr);
			return null;
		} catch (bad_emetcode ex) {
			if (IN_DEBUG_24) {
				logger.error(ex, "\n file=" + pss_ff);
			}
			return ex;
		}
	}

	public boolean is_issuer(tag_transfer curr_trans) {
		if (has_ck_issuer) {
			return is_pass_issuer;
		}
		File iss_ff = get_issuance_file(curr_trans);
		is_pass_issuer = iss_ff.exists();
		has_ck_issuer = true;
		return is_pass_issuer;
	}

	private boolean is_valid_paref_file(File sel_ff, key_owner owr) {
		if (is_remote_paccount()) {
			throw new bad_passet(2);
		}
		File parf_dd = get_paref_dir();
		String rel_nm = file_funcs.get_rel_path(parf_dd, sel_ff);
		boolean ff_ok = file_funcs.is_badge_file(sel_ff, owr, rel_nm);
		return ff_ok;
	}

	private boolean is_giving(File pss_ff, key_owner owr) {
		return file_funcs.has_label_file(pss_ff, owr, GIVING_PASSET);
	}

	private File set_giving(File pss_ff, key_owner owr) {
		return file_funcs.create_label_file(pss_ff, owr, GIVING_PASSET);
	}

	private void reset_giving(File pss_ff) {
		File lb_ff = file_funcs.get_label_file(pss_ff, GIVING_PASSET);
		file_funcs.concurrent_delete_file(lb_ff);
	}

	private void save_paref_file_for(tag_transfer curr_trans, key_owner owr) {
		if (is_remote_paccount()) {
			throw new bad_passet(2);
		}
		File pss_ff = get_passet_file(curr_trans);
		if (is_giving(pss_ff, owr)) {
			return;
		}
		File parf_ff = get_paref_file(curr_trans);
		file_funcs.mk_parent_dir(parf_ff);
		File parf_dd = get_paref_dir();
		String rel_nm = file_funcs.get_rel_path(parf_dd, parf_ff);
		file_funcs.create_badge_file(parf_ff, owr, rel_nm);
		if(IN_DEBUG_32){
			String stk = logger.get_stack_str();
			logger.info("saving_paref paref=" + parf_ff + "\n rel_nm=" + rel_nm + 
					" STACK=\n" + stk);
		}
	}

	public void init_the_trissuers(key_owner owr) {
		trust_grps.init_trissuers(this, owr);
	}

	public void update_trissuers(key_owner owr, trissuers n_grps) {
		n_grps.update_trissuer_files(this, owr);
		trust_grps.init_trissuers(this, owr);
	}

	private void init_trissuer_comparator(key_owner owr) {
		trust_grps.init_trissuer_comparator(this, owr);
	}

	public boolean can_receive_from(String iss) {
		return trust_grps.can_trust(iss);
	}

	private List<File> get_trissuer_dirs_can_choose(paccount remote,
			tag_denomination deno, boolean any) {
		File deno_dir = new File(get_deno_path(deno));
		if (!deno_dir.exists()) {
			return new ArrayList<File>();
		}

		String[] arr_glids = null;
		if (!any && (trust_selecting != null)) {
			File iss_sel_dir = new File(deno_dir, trust_selecting);
			if (iss_sel_dir.exists()) {
				arr_glids = new String[1];
				arr_glids[0] = trust_selecting;
			} else {
				return new ArrayList<File>();
			}
		}

		if (arr_glids == null) {
			List<String> all_nm = get_all_trissuers_of_deno(deno);
			arr_glids = all_nm.toArray(new String[0]);
		}

		if (IN_DEBUG_01) {
			logger.info("ALL_NAMS=\n" + Arrays.toString(arr_glids));
		}

		Arrays.sort(arr_glids, trust_grps.get_trissuer_comparator());

		if (IN_DEBUG_01) {
			logger.info("SORTED_ALL_NAMS=\n" + Arrays.toString(arr_glids));
		}

		List<File> can_giv = new ArrayList<File>(arr_glids.length);

		if (remote == this) {
			any = true;
		}

		if (any) {
			for (String dd : arr_glids) {
				can_giv.add(new File(deno_dir, dd));
			}
		} else {
			if (remote != null) {
				for (String dd : arr_glids) {
					if (remote.can_receive_from(dd)) {
						logger.info("can_recv_from " + dd);
						can_giv.add(new File(deno_dir, dd));
					}
				}
			}
		}

		if (IN_DEBUG_01) {
			logger.info("CAN_GIVE=");
			logger.info(file_funcs.files_to_path_list(can_giv));
		}

		return can_giv;
	}

	private FileFilter get_paref_filter(final key_owner owr) {
		FileFilter ffil = new FileFilter() {
			public boolean accept(File ff) {
				return is_paref_file(ff, owr);
			}
		};
		return ffil;
	}

	private List<File> find_num_paref_files(key_owner owr, List<File> tss_dirs,
			int num_files) {
		if (num_files < 0) {
			throw new bad_passet(2);
		}

		FileFilter ffil = get_paref_filter(owr);

		List<File> sel = new ArrayList<File>(num_files);
		boolean have_all = false;
		for (int ii = tss_dirs.size() - 1; !have_all && (ii >= 0); ii--) {
			File tss_dd = tss_dirs.get(ii);
			List<File> all_paref = file_funcs.list_dir(tss_dd, null, ffil);
			for (File ref_ff : all_paref) {
				if (sel.size() == num_files) {
					have_all = true;
					break;
				}
				if (!ref_ff.isDirectory()) {
					sel.add(ref_ff);
				}
			}
		}
		return sel;
	}

	File rel_to_paref_file(String rel_pth) {
		File ref = new File(get_paref_dir(), rel_pth);
		return ref;
	}

	public void cho_init(key_owner owr, paccount remote) {
		if (remote != null) {
			if (!remote.is_remote_of(this)) {
				throw new bad_passet(2);
			}
			remote.init_the_trissuers(owr);
		}
		init_trissuer_comparator(owr);
	}

	public void choose_num_passets(key_owner owr, paccount remote,
			tag_denomination deno, int num_sel) {
		if (remote == null) {
			throw new bad_passet(2);
		}

		cho_init(owr, remote);
		if (num_sel > 0) {
			List<File> tss_dirs = get_trissuer_dirs_can_choose(remote, deno,
					false);
			if (tss_dirs.isEmpty()) {
				logger.info("Peer does NOT receive notes of ANY "
						+ "of the ISSUER account global ids of your notes !!! \n"
						+ "CANNOT GIVE ANY notes!!!\n" + remote.work_dir);
			}
			List<File> refs = find_num_paref_files(owr, tss_dirs, num_sel);
			if (refs.size() > num_sel) {
				throw new bad_passet(2);
			}
			// File rem_ch_dd = remote.get_chose_dir();
			File rem_ch_dd = remote.get_new_transfer_dir(owr);
			do_chomarks(rem_ch_dd, owr, refs);
		} else if (num_sel < 0) {
			num_sel = -num_sel;
			undo_chomarks(remote, null, owr, deno, num_sel);
		}
	}

	public void fill_all_deno_count(key_owner owr, paccount remote, int max_pref) {
		cho_init(owr, remote);

		int currcy_idx = get_working_currency();

		deno_cter.reset_deno_counts(currcy_idx);

		List<String> all_denos = get_all_paref_denos();
		for (String deno_txt : all_denos) {
			tag_denomination deno = tag_denomination
					.parse_short_text_denomination(deno_txt, currcy_idx);

			deno_count cc = deno_cter.get_deno_count(deno);

			List<File> all_tss = get_trissuer_dirs_can_choose(remote, deno,
					false);
			List<File> all_pref = find_num_paref_files(owr, all_tss, max_pref);
			cc.num_can_give = all_pref.size();

			List<File> all_exis_tss = get_trissuer_dirs_can_choose(remote,
					deno, true);
			List<File> all_exis_pref = find_num_paref_files(owr, all_exis_tss,
					max_pref);
			cc.num_have = all_exis_pref.size();
		}

		count_undoable_chomarks_in(remote, owr, deno_cter, null);
	}

	public void fill_count_with_all_choices_in(key_owner owr, paccount remote,
			String choice_nm) {
		cho_init(owr, remote);
		int currcy_idx = get_working_currency();
		deno_cter.reset_deno_counts(currcy_idx);
		count_undoable_chomarks_in(remote, owr, deno_cter, choice_nm);
	}

	private boolean is_paref_file(File parf, key_owner owr) {
		if (parf.isDirectory()) {
			return false;
		}
		try {
			if (!is_valid_paref_file(parf, owr)) {
				logger.info("NOT_A_VALID_PAREF=\n" + parf);
				return false;
			}
			String ff_id_str = get_file_id_str_from(parf);
			File pss_ff = get_passet_file_with(ff_id_str);
			if (!pss_ff.exists()) {
				if (IN_DEBUG_3) {
					logger.debug_trace();
					logger.info("NOT EXISTANT NOTE\n" + pss_ff
							+ " \nfrom ref\n" + parf);
				}
				return false;
			}
			return true;
		} catch (bad_emetcode ex1) {
			return false;
		}
	}

	public List<File> get_all_passet_files(List<tag_transfer> all_iss_dat) {
		List<File> all_ids = new ArrayList<File>();
		for (tag_transfer dat : all_iss_dat) {
			File ff = get_passet_file(dat);
			all_ids.add(ff);
		}
		return all_ids;
	}

	public int read_min_expo() {
		File ff = get_min_change_file();
		if (!ff.exists()) {
			return config.DEFAULT_MIN_CHG_EXPO;
		}
		String tmp_str = mem_file.read_string(ff);
		int min_expo = Integer.parseInt(tmp_str);
		return min_expo;
	}

	public void write_min_expo(int min_expo) {
		File ff = get_min_change_file();
		String vv = "" + min_expo;
		boolean ok = mem_file.write_string(ff, vv);
		if (!ok) {
			throw new bad_passet(2);
		}
	}

	private List<File> get_paref_files_of(List<tag_transfer> all_iss_dat) {
		List<File> all_ids = new ArrayList<File>();
		for (tag_transfer dat : all_iss_dat) {
			File pref_ff = get_paref_file(dat);
			if (pref_ff.exists()) {
				all_ids.add(pref_ff);
			}
		}
		return all_ids;
	}

	public void choose_passets(paccount remote, key_owner owr,
			List<tag_transfer> all_iss_dat) {
		List<File> refs = get_paref_files_of(all_iss_dat);
		// File rem_cho_dir = remote.get_chose_dir();
		File rem_ch_dd = remote.get_new_transfer_dir(owr);
		do_chomarks(rem_ch_dd, owr, refs);
	}

	public void prt_basic_data(key_owner owr) {
		print_line_separator(System.out, '=');

		paccount loc_pcc = this;
		paccount rem_pcc = null;

		if (is_remote_paccount()) {
			rem_pcc = this;
			loc_pcc = local_paccount;
		}

		if (loc_pcc == null) {
			throw new bad_passet(2);
		}
		if (loc_pcc.bb_dir == null) {
			throw new bad_passet(2);
		}
		if (loc_pcc.curr_user == null) {
			loc_pcc.read_current_user(owr);
		}
		if ((rem_pcc != null) && (rem_pcc.curr_user == null)) {
			rem_pcc.read_current_user(owr);
		}

		String loc_id = loc_pcc.working_mikid;
		String loc_curr_str = iso.get_currency_code(loc_pcc.working_currency);
		String loc_nm = loc_pcc.curr_user.legal_name;

		System.out.print("Working currency:" + loc_curr_str);
		System.out.println("\tLocal account id:'" + loc_id);
		System.out.println("Local User:'" + loc_nm);
		nx_std_coref loc_gli = loc_pcc.bb_dir.get_local_glid(owr);
		System.out.println("Local GLID '" + loc_gli + "'");

		if (rem_pcc != null) {
			String rem_nm = rem_pcc.curr_user.legal_name;
			System.out.println("Remote User:'" + rem_nm);
			nx_std_coref rem_gli = loc_pcc.bb_dir.get_remote_glid(
					rem_pcc.remote_coid, owr);
			System.out.println("Remote GLID '" + rem_gli + "'");
		}

		System.out.flush();
		print_line_separator(System.out, '-');
	}

	private static void print_line_separator(PrintStream ps, char sep) {
		for (int aa = 0; aa < 80; aa++) {
			ps.print(sep);
		}
		ps.println();
		ps.flush();
	}

	public void delete_passet(key_owner owr, boolean force_it) {
		List<channel> all_chn = channel.read_all_channels(this, owr);
		for (channel chn : all_chn) {
			nx_conn_id coid = chn.coid;
			nx_messenger.delete_coid(bb_dir, coid);
		}

		File mk_dd = get_mikid_dir();
		file_funcs.delete_dir(mk_dd);

		init_paccount();
	}

	public void create_glid_file(key_owner owr) {
		if (bb_dir.has_local_glid()) {
			return;
		}

		nx_std_coref gld = null;

		File usr_img = get_current_user_image_file();
		if (usr_img.exists()) {
			global_id per_gli = new global_id(usr_img, owr);
			gld = new nx_std_coref(per_gli);
		}
		if (gld == null) {
			File usr_inf = get_current_user_info_file();
			if (usr_inf.exists()) {
				global_id per_gli = new global_id(usr_inf, owr);
				gld = new nx_std_coref(per_gli);
			}
		}
		if (gld == null) {
			throw new bad_passet(2, L.cannot_create_glid);
		}

		if (IN_DEBUG_2) {
			logger.debug("CREATING_GLID=" + gld.get_str());
		}

		bb_dir.set_local_glid(owr, gld);
	}

	private void verify_passet(File pss_ff, List<tag_transfer> all_iss,
			key_owner owr) {
		verify_passet(pss_ff, all_iss, owr, null);
	}

	private File save_verif_file(String tra_id_str, key_owner owr,
			List<String> tra_lines) {
		File vrf_ff = get_verif_file(tra_id_str);
		boolean ok_ff = file_funcs.has_label_file(vrf_ff, owr, VERIFIED_PASSET);
		if (!ok_ff) {
			file_funcs.mk_parent_dir(vrf_ff);
			parse.write_encrypted_lines(vrf_ff, null, tra_lines);
			file_funcs.create_label_file(vrf_ff, owr, VERIFIED_PASSET);
			return null;
		}
		return vrf_ff;
	}

	private void end_verif(File pss_ff, List<tag_transfer> all_iss,
			key_owner owr, tag_transfer the_trans) {

		List<String> tra_lines = the_trans.get_lines();
		String tra_id_str = the_trans.get_transfer_id().get_str();
		File prev_vf = save_verif_file(tra_id_str, owr, tra_lines);
		if (prev_vf != null) {
			boolean eq_s = file_funcs.equal_sha(pss_ff, null, prev_vf, null);
			if (!eq_s) {
				throw new bad_passet(2);
			}
		}

		if (all_iss != null) {
			all_iss.add(the_trans);
		}

		if (IN_DEBUG_15) {
			logger.info("VERIFIED_#" + the_trans + "\n\tFILE=" + pss_ff);
		}
	}

	private tag_transfer verify_passet(File pss_ff, List<tag_transfer> all_iss,
			key_owner owr, paccount recvng_pcc) {

		tag_transfer the_trans = get_first_transfer(pss_ff);
		List<String> tra_lines = the_trans.get_lines();

		if (the_trans.is_root_transfer()) {
			throw new bad_passet(2);
		}

		final String pss_id_str = the_trans.get_passet_id().get_str();
		final int prv_idx = the_trans.get_passet_idx();

		if (recvng_pcc != null) {
			// paccount rcv_pcc = get_local_paccount();
			// if(rcv_pcc != recvng_pcc){
			// throw new bad_passet(2);
			// }
			paccount rcv_pcc = recvng_pcc;
			rcv_pcc.init_the_trissuers(owr);
			tag_accoglid iss = the_trans.get_root_issuer();
			String iss_str = iss.toString();
			if (!rcv_pcc.can_receive_from(iss_str)) {
				throw new bad_passet(2, String.format(L.not_trusted_issuer,
						iss_str));
			}
			tag_accoglid trk = the_trans.get_tracker_accoglid();
			if (!rcv_pcc.is_tracker(iss, trk, owr)) {
				throw new bad_passet(2, String.format(L.not_accepted_tracker,
						trk.get_str()));
			}
		}

		List<tag_transfer> all_prev_tra = new ArrayList<tag_transfer>();
		List<tag_filglid> all_prev = the_trans.get_all_prev_tra_id();
		for (tag_filglid prev_id : all_prev) {
			String prev_id_str = prev_id.get_str();
			File prev_ff = get_verif_file(prev_id_str);
			boolean ok_ff = file_funcs.has_label_file(prev_ff, owr,
					VERIFIED_PASSET);
			if (!ok_ff) {
				if (IN_DEBUG_17) {
					logger.info("NOT_VERIFIED_prev_for_pss_ff=" + pss_ff);
				}
				throw new bad_passet(2, String.format(L.cannot_trust_file,
						prev_ff));
			}
			tag_transfer prev_trans = get_first_transfer(prev_ff);
			all_prev_tra.add(prev_trans);
		}

		if (all_prev_tra.isEmpty()) {
			throw new bad_passet(2);
		}

		check_all_same_root(all_prev_tra, the_trans);
		the_trans.check_all_transfer_times(all_prev_tra);

		// do verif

		tag_transfer verif_trans = new tag_transfer();
		verif_trans.init_with_prev_list(all_prev_tra, prv_idx);
		if (!verif_trans.has_giv_boxes(false)) {
			throw new bad_passet(2);
		}

		if (IN_DEBUG_20) {
			logger.info("IN_VERIF_of_deno=" + verif_trans + " ff=\n\t" + pss_ff);
		}

		verif_trans.init_with_lines(tra_lines);
		verif_trans.ck_pss_id(pss_id_str, pss_ff);

		// verif ok save trans and trust file.
		end_verif(pss_ff, all_iss, owr, the_trans);
		verif_trans.ck_pss_id(pss_id_str, pss_ff);
		// RETURN_VERIF?

		if ((all_iss != null) && (the_trans != all_iss.get(all_iss.size() - 1))) {
			throw new bad_passet(2);
		}
		return the_trans;
	}

	public List<String> get_all_prev_ids(List<tag_transfer> all_tra) {
		List<String> all_ids = new ArrayList<String>();

		for (tag_transfer the_tra : all_tra) {
			List<String> all_prev = the_tra.get_all_prev_tra_id_str();
			all_ids.addAll(all_prev);
		}
		return all_ids;
	}

	public File[] get_all_verif_files(List<tag_transfer> all_iss, key_owner owr) {
		List<String> tra_ids = paccount.get_passids(all_iss);
		return get_verif_files(tra_ids, owr);
	}

	public File[] get_verif_files(List<String> all_ids, key_owner owr) {
		List<File> all_vrf = new ArrayList<File>();

		for (String tra_id_str : all_ids) {
			File tra_ff = get_verif_file(tra_id_str);
			boolean ok_ff = file_funcs.has_label_file(tra_ff, owr,
					VERIFIED_PASSET);
			if (ok_ff) {
				all_vrf.add(tra_ff);
			}
		}
		File[] arr_vrfs = all_vrf.toArray(new File[0]);
		return arr_vrfs;
	}

	public static void set_verified_tag_to(File[] all_ff, key_owner owr) {
		for (File ff : all_ff) {
			if (ff.exists()) {
				file_funcs.create_label_file(ff, owr, VERIFIED_PASSET);
			}
		}
	}

	private void issue_passet(List<bad_emetcode> all_bad,
			List<tag_transfer> all_iss, key_owner owr, tag_denomination deno,
			tag_accoglid nxt_trk) {

		reset_paccount();
		if (!has_user()) {
			throw new bad_passet(2);
		}
		if (!local_trader.has_valid_glid()) {
			throw new bad_passet(2);
		}

		if (deno.currency_idx != working_currency) {
			deno.currency_idx = working_currency;
		}

		tag_transfer root_trans = new tag_transfer();

		root_trans.init_in_amount(deno);
		root_trans.init_tracker_accoglid(local_trader.tr_glid);
		local_trader.set_now();

		List<String> txt = root_trans
				.get_root_transfer_lines(local_trader, owr);

		File issue_ff = get_issuance_file(root_trans);
		parse.write_lines(issue_ff, txt);
		if (root_trans.get_consec() != 0) {
			throw new bad_passet(2);
		}
		root_trans.save_rcv_kbox_pvks_in_dir(get_pvks_dir(), owr);

		File pss_ff = get_passet_file(root_trans);
		file_funcs.mk_parent_dir(pss_ff);

		file_funcs.concurrent_delete_file(pss_ff);
		file_funcs.concurrent_copy_file(issue_ff, pss_ff);

		if (!issue_ff.exists()) {
			throw new bad_passet(2);
		}
		if (!pss_ff.exists()) {
			throw new bad_passet(2);
		}

		String tra_id_str = root_trans.get_transfer_id().get_str();
		save_verif_file(tra_id_str, owr, txt);

		make_root_receptacle(pss_ff, owr, nxt_trk);

		tag_transfer[] root_tra_ref = new tag_transfer[1];
		File root_ff = sign_root_receptacle(pss_ff, owr, root_tra_ref);

		// save_paref_file_for(root_tra_ref[0], owr);

		bad_emetcode err = try_check_mine(root_ff, all_iss, owr, null);
		if (err != null) {
			if (IN_DEBUG_10) {
				logger.debug("issued_NOT_mine_error with file=\n" + pss_ff);
			}
			all_bad.add(err);
		}

		if (IN_DEBUG_21) {
			logger.debug("ISSUED "
					+ root_trans.get_out_amount().get_short_text_denomination(
							true));
		}
	}

	private List<tag_transfer> check_mine_passets(List<File> all_to_ck,
			key_owner owr) {
		List<tag_transfer> out_pss = new ArrayList<tag_transfer>();
		for (File pss_ff : all_to_ck) {
			check_mine(pss_ff, out_pss, owr, null);
		}
		return out_pss;
	}

	private File create_issue_tmp_dir(key_owner owr, String iss_kind) {
		if (is_remote_paccount()) {
			throw new bad_passet(2);
		}
		File iss_dd = get_issuance_dir();
		long tmp_id = System.currentTimeMillis();
		long tmp_id2 = owr.new_random_long();
		String ff_nm = iss_kind + tmp_id + '_' + tmp_id2;
		File tmp_dd = new File(iss_dd, ff_nm);
		file_funcs.get_dir(tmp_dd);
		return tmp_dd;
	}

	public List<tag_transfer> split_passets(List<File> all_in_ff,
			key_owner owr, boolean in_this_trk) {

		if (is_remote_paccount()) {
			throw new bad_passet(2);
		}

		List<tag_transfer> all_out_tras = new ArrayList<tag_transfer>();
		Set<tag_transfer> all_nxt = new TreeSet<tag_transfer>();
		List<File> all_to_split = all_to_change(all_in_ff, owr, all_nxt);
		if (all_to_split.isEmpty()) {
			all_out_tras.addAll(all_nxt);
			return all_out_tras;
		}

		List<tag_transfer> all_trans = check_mine_passets(all_to_split, owr);

		tag_transfer roo_tra = check_all_same_root(all_trans, null);
		tag_accoglid iss_gld = roo_tra.get_root_issuer();
		tag_accoglid nxt_trk = get_next_tracker_for(in_this_trk, owr, iss_gld);

		File tmp_dd = create_issue_tmp_dir(owr, ISSUE_SPLIT_NM);
		List<File> all_parefs = get_paref_files_of(all_trans);
		do_chomarks(tmp_dd, owr, all_parefs);

		int min_expo = read_min_expo();

		for (tag_transfer ii_tra : all_trans) {
			if (!ii_tra.has_root_issuer()) {
				throw new bad_passet(2);
			}
			if (!ii_tra.has_rcv_boxes(true)) {
				throw new bad_passet(2);
			}

			// create split_transfer

			tag_transfer spl_tra = ii_tra.get_split_transfer(local_trader, owr,
					min_expo);

			save_passet_for(spl_tra, owr, null);

			// create output transfers

			int num_out = spl_tra.num_out();
			for (int ii_out = 0; ii_out < num_out; ii_out++) {
				make_receptacle_for(spl_tra, ii_out, owr, nxt_trk);
				tag_transfer[] out_tra_ref = new tag_transfer[1];
				File out_ff = sign_receptacle_for(spl_tra, ii_out, owr,
						out_tra_ref);
				tag_transfer out_tra = out_tra_ref[0];

				// save_paref_file_for(out_tra, owr);
				check_mine(out_ff, null, owr, null);
				all_out_tras.add(out_tra);
			}

			File in_ff = get_passet_file(ii_tra);
			List<File> all_out_ffs = get_all_passet_files(all_out_tras);
			set_next_passet_val_files(in_ff, owr, all_out_ffs);
		}

		List<File> all_chmks = get_chomarks_for(tmp_dd, all_trans);
		finish_chomarks(owr, all_chmks);
		file_funcs.delete_dir(tmp_dd);

		all_out_tras.addAll(all_nxt);
		return all_out_tras;
	}

	tag_transfer check_all_same_root(List<tag_transfer> all_trans,
			tag_transfer roo_tra) {
		for (tag_transfer jo_tra : all_trans) {
			if (roo_tra == null) {
				roo_tra = jo_tra;
			}
			if (!roo_tra.has_same_root(jo_tra)) {
				throw new bad_passet(2);
			}
		}
		if (roo_tra == null) {
			throw new bad_passet(2);
		}
		return roo_tra;
	}

	public List<tag_transfer> join_passets(List<File> all_in_ff, key_owner owr,
			boolean in_this_trk) {

		if (is_remote_paccount()) {
			throw new bad_passet(2);
		}

		List<tag_transfer> all_out_tras = new ArrayList<tag_transfer>();
		Set<tag_transfer> all_nxt = new TreeSet<tag_transfer>();
		List<File> all_to_join = all_to_change(all_in_ff, owr, all_nxt);
		if (all_to_join.isEmpty()) {
			all_out_tras.addAll(all_nxt);
			return all_out_tras;
		}

		List<tag_transfer> all_trans = check_mine_passets(all_to_join, owr);

		tag_transfer roo_tra = check_all_same_root(all_trans, null);
		tag_accoglid iss_gld = roo_tra.get_root_issuer();
		tag_accoglid nxt_trk = get_next_tracker_for(in_this_trk, owr, iss_gld);

		File tmp_dd = create_issue_tmp_dir(owr, ISSUE_JOIN_NM);

		deno_counter cntr = tag_denomination
				.calc_join_transfer_denos(all_trans);
		List<deno_count> all_cnts = cntr.get_working_counts();

		for (deno_count cnt : all_cnts) {
			List<tag_transfer> to_join = tag_denomination
					.get_all_transfer(cnt.joined_denos);

			if (to_join.isEmpty()) {
				continue;
			}

			tag_transfer jned_tra = new tag_transfer();

			// create join_transfer

			List<File> jn_parefs = get_paref_files_of(to_join);
			do_chomarks(tmp_dd, owr, jn_parefs);

			tag_denomination tgt_join = cnt.deno;
			jned_tra.init_root(iss_gld, roo_tra.get_root_transfer());
			jned_tra.init_tracker_accoglid(roo_tra.get_tracker_accoglid());

			jned_tra.init_join_transfer(local_trader, to_join, tgt_join, owr);

			save_passet_for(jned_tra, owr, null);

			// create output transfer

			make_receptacle_for(jned_tra, -1, owr, nxt_trk);

			tag_transfer[] jn_tra_ref = new tag_transfer[1];
			File out_ff = sign_receptacle_for(jned_tra, 0, owr, jn_tra_ref);
			tag_transfer out_tra = jn_tra_ref[0];

			// save_paref_file_for(out_tra, owr);
			check_mine(out_ff, null, owr, null);
			all_out_tras.add(out_tra);

			for (tag_transfer in_tra : to_join) {
				File in_ff = get_passet_file(in_tra);
				set_next_passet_val_file(in_ff, owr, out_ff);
			}

			List<File> all_chmks = get_chomarks_for(tmp_dd, to_join);
			finish_chomarks(owr, all_chmks);
		}

		file_funcs.delete_dir(tmp_dd);

		all_out_tras.addAll(all_nxt);
		return all_out_tras;
	}

	public List<String> save_trackers_list(nx_std_coref rem_iss_gld,
			List<String> all_trcks, key_owner owr) {
		if (is_remote_paccount()) {
			throw new bad_passet(2);
		}
		List<String> all_not = new ArrayList<String>();
		String rem_iss_glid_str = rem_iss_gld.get_str();
		for (String trk_gld_str : all_trcks) {
			File trk_ff = get_tracker_file(rem_iss_glid_str, trk_gld_str);
			if (!trk_ff.exists()) {
				file_funcs.mk_parent_dir(trk_ff);
				file_funcs.create_badge_file(trk_ff, owr, rem_iss_glid_str);
			} else {
				boolean ok = file_funcs.is_badge_file(trk_ff, owr,
						rem_iss_glid_str);
				if (!ok) {
					all_not.add(trk_gld_str);
				}
			}
		}
		return all_not;
	}

	public boolean is_tracker(tag_accoglid iss_gld, tag_accoglid trk_gld,
			key_owner owr) {
		if (is_remote_paccount()) {
			throw new bad_passet(2);
		}
		if (iss_gld.equals(trk_gld)) {
			return true;
		}
		String iss_glid_str = iss_gld.get_str();
		String trk_glid_str = trk_gld.get_str();
		File trk_ff = get_tracker_file(iss_glid_str, trk_glid_str);
		return file_funcs.is_badge_file(trk_ff, owr, iss_glid_str);
	}

	public nx_std_coref get_glid(key_owner owr) {
		if (my_glid != null) {
			return my_glid;
		}
		if (is_remote_paccount()) {
			throw new bad_passet(2);
		}
		my_glid = bb_dir.get_local_glid(owr);
		return my_glid;
	}

	private void delete_all_trackers_of(String rem_iss_gld_str) {
		File iss_dd = get_trissuer_trakers_dir(rem_iss_gld_str);
		file_funcs.delete_dir(iss_dd);
	}

	void delete_not_trusted_trackers(trissuers grps) {
		if (is_remote_paccount()) {
			// DEBUG CASE ONLY
			return;
		}
		File trks_dd = get_trackers_dir();
		File[] all_triss_ff = trks_dd.listFiles();
		List<String> all_triss = file_funcs.get_file_names(all_triss_ff);

		all_triss.removeAll(grps.trusted);
		for (String iss_gld_str : all_triss) {
			delete_all_trackers_of(iss_gld_str);
		}
	}

	public tag_accoglid get_next_tracker(key_owner owr) {
		File ff = get_next_tracker_file();
		String trk = mem_file.read_encrypted_string(ff, owr);
		if (trk != null) {
			tag_accoglid nxt_trk = new tag_accoglid(trk);
			return nxt_trk;
		}
		return null;
	}

	tag_accoglid get_next_tracker_for(boolean in_this_trk, key_owner owr,
			tag_accoglid iss) {
		if (is_remote_paccount()) {
			throw new bad_passet(2);
		}
		if (in_this_trk) {
			return iss;
		}

		tag_accoglid nxt_trk = iss;
		nx_std_coref iss_gli = iss.the_glid;
		if (iss_gli == null) {
			throw new bad_passet(2, "get_next_tracker_for.GLID_IS_NULL");
		}

		nx_std_coref loc_gli = get_glid(owr);
		if (iss_gli.equals(loc_gli)) {
			tag_accoglid iss_nxt = get_next_tracker(owr);
			if (iss_nxt != null) {
				return iss_nxt;
			}
			return iss;
		}

		nx_dir_base bb_dd = get_dir_base();
		nx_conn_id iss_coid = bb_dd.get_coid_by_ref(iss.get_str(), null);
		if (iss_coid != null) {
			nx_std_coref rem_gli = bb_dd.get_remote_glid(iss_coid, owr);
			if (iss_gli.equals(rem_gli)) {
				paccount rem_pcc = get_sub_paccount(iss_coid);
				nxt_trk = rem_pcc.get_next_tracker(owr);
				return nxt_trk;
			}
		}

		return nxt_trk;
	}

	// chomarks

	private String get_deno_name_of_paref_file(File parf_ff) {
		File loc_paref_dir = get_paref_dir();
		String rel_pth = file_funcs.get_rel_path(loc_paref_dir, parf_ff);

		int idx_sep = rel_pth.indexOf(File.separatorChar);
		String deno_txt = rel_pth.substring(0, idx_sep);

		return deno_txt;
	}

	private String get_chomark_name_of(File parf_ff) {
		String deno_txt = get_deno_name_of_paref_file(parf_ff);
		String ff_nm = parf_ff.getName();
		String ch_nm = deno_txt + CHOICE_SEP + ff_nm;
		return ch_nm;
	}

	private static String get_deno_name_of_chomark_file(File ch_ff) {
		String ch_nm = ch_ff.getName();

		int idx_sep = ch_nm.indexOf(CHOICE_SEP);
		String deno_txt = ch_nm.substring(0, idx_sep);

		return deno_txt;
	}

	private File get_passet_file_of_chomark(File chmrk_ff) {
		if (is_remote_paccount()) {
			throw new bad_passet(2);
		}
		String id_str_ff = get_file_id_str_from(chmrk_ff);
		File pss_ff = get_passet_file_with(id_str_ff);
		return pss_ff;
	}

	private File get_chomark_of_paref(File rem_cho_dir, File loc_parf_ff) {
		if (is_remote_paccount()) {
			throw new bad_passet(2);
		}
		if (!rem_cho_dir.isDirectory()) {
			throw new bad_passet(2);
		}
		String ch_nm = get_chomark_name_of(loc_parf_ff);
		File rem_ch_ff = new File(rem_cho_dir, ch_nm);
		return rem_ch_ff;
	}

	private void delete_paref(File loc_parf_ff) {
		loc_parf_ff.delete();
		File loc_paref_dir = get_paref_dir();
		file_funcs.path_delete(loc_paref_dir, loc_parf_ff.getParentFile());
	}

	private void do_chomark(File rem_cho_dir, key_owner owr, File loc_parf_ff) {
		if (!is_valid_paref_file(loc_parf_ff, owr)) {
			return;
		}

		File rem_ch_ff = get_chomark_of_paref(rem_cho_dir, loc_parf_ff);

		File rem_tmp_ff = file_funcs.get_temp_file(rem_ch_ff);
		file_funcs.concurrent_move_file(loc_parf_ff, rem_tmp_ff);
		if (!rem_tmp_ff.exists()) {
			return;
		}
		delete_paref(loc_parf_ff);
		rem_tmp_ff.delete();
		
		File pss_ff = get_passet_file_of_chomark(rem_ch_ff);
		set_giving(pss_ff, owr);

		String rel_pth = file_funcs.get_rel_path(get_mikid_dir(), rem_ch_ff);
		file_funcs.create_badge_file(rem_ch_ff, owr, rel_pth);
	}

	private boolean is_chomark(key_owner owr, File rem_ch_ff) {
		if (is_remote_paccount()) {
			throw new bad_passet(2);
		}

		String rel_pth = file_funcs.get_rel_path(get_mikid_dir(), rem_ch_ff);
		boolean ch_ff_ok = file_funcs.is_badge_file(rem_ch_ff, owr, rel_pth);
		return ch_ff_ok;
	}

	private boolean undo_chomark(key_owner owr, File rem_ch_ff) {
		if (is_remote_paccount()) {
			throw new bad_passet(2);
		}

		if (!is_chomark(owr, rem_ch_ff)) {
			return false;
		}
		rem_ch_ff.delete();
		
		File pss_ff = get_passet_file_of_chomark(rem_ch_ff);
		reset_giving(pss_ff);

		bad_emetcode bd = try_check_mine(pss_ff, null, owr, null);
		if (bd != null) {
			return false;
		}
		return true;
	}

	private void finish_chomark(key_owner owr, File rem_ch_ff) {
		if (is_remote_paccount()) {
			throw new bad_passet(2);
		}

		if (!is_chomark(owr, rem_ch_ff)) {
			return;
		}

		if (!rem_ch_ff.delete()) {
			return;
		}

		File pss_ff = get_passet_file_of_chomark(rem_ch_ff);
		file_funcs.create_label_file(pss_ff, owr, GIVED_PASSET);
	}

	private void do_chomarks(File rem_cho_dir, key_owner owr,
			List<File> all_parefs) {
		if (is_remote_paccount()) {
			throw new bad_passet(2);
		}
		for (File loc_parf_ff : all_parefs) {
			do_chomark(rem_cho_dir, owr, loc_parf_ff);
		}
	}

	File[] get_all_chomarks(key_owner owr, String choice_nm) {
		if (!is_remote_paccount()) {
			throw new bad_passet(2);
		}
		String dd_nm = choice_nm;
		if (dd_nm == null) {
			dd_nm = get_next_transfer_name(owr);
			if (dd_nm == null) {
				return new File[0];
				//throw new bad_passet(2, "cho_dir=" + get_chose_dir());
			}
		}
		File ch_dd = new File(get_chose_dir(), dd_nm);
		File[] all_ff = ch_dd.listFiles();
		if(all_ff == null){
			return new File[0];
		}
		return all_ff;
	}

	public List<File> get_passets_of_chomarks_in(paccount rem_pcc,
			key_owner owr, String choice_nm) {

		if (is_remote_paccount()) {
			throw new bad_passet(2);
		}
		if (!rem_pcc.is_valid_transaction(owr, choice_nm)) {
			throw new bad_passet(2, "INVALID_CHOICE=" + choice_nm);
		}

		List<File> all_pss = new ArrayList<File>();
		File[] all_ff = rem_pcc.get_all_chomarks(owr, choice_nm);
		for (File rem_ch_ff : all_ff) {
			if (!is_chomark(owr, rem_ch_ff)) {
				continue;
			}
			File loc_pss = get_passet_file_of_chomark(rem_ch_ff);
			all_pss.add(loc_pss);
		}
		return all_pss;
	}

	private boolean undo_chomark_with_deno(tag_denomination deno,
			key_owner owr, File rem_ch_ff) {
		if ((deno != null) && !chomark_has_deno(rem_ch_ff, deno)) {
			return false;
		}
		return undo_chomark(owr, rem_ch_ff);
	}

	public void undo_all_chomarks(paccount rem_pcc, String choice_nm,
			key_owner owr) {
		undo_chomarks(rem_pcc, choice_nm, owr, null, -1);
	}

	private void undo_chomarks(paccount rem_pcc, String choice_nm,
			key_owner owr, tag_denomination deno, int num_sel) {
		if (is_remote_paccount()) {
			throw new bad_passet(2);
		}
		if (!rem_pcc.is_remote_paccount()) {
			throw new bad_passet(2);
		}

		File[] all_ff = rem_pcc.get_all_chomarks(owr, choice_nm);
		int num_un = 0;
		for (File rem_ch_ff : all_ff) {
			boolean un_ok = undo_chomark_with_deno(deno, owr, rem_ch_ff);
			if (un_ok) {
				num_un++;
			}
			if (num_un == num_sel) {
				break;
			}
		}
	}

	private boolean chomark_has_deno(File rem_ch_ff, tag_denomination deno) {
		String dno_txt1 = get_deno_name_of_chomark_file(rem_ch_ff);
		String dno_txt2 = deno.get_short_text_denomination(false);
		return dno_txt1.equals(dno_txt2);
	}

	private File get_chomark_of(File rem_cho_dir, tag_transfer curr_trans) {
		File loc_parf_ff = get_paref_file(curr_trans);
		File rem_ch_ff = get_chomark_of_paref(rem_cho_dir, loc_parf_ff);
		return rem_ch_ff;
	}

	private List<File> get_chomarks_for(File rem_cho_dir,
			List<tag_transfer> all_tra) {
		List<File> all_chmks = new ArrayList<File>();
		for (tag_transfer curr_trans : all_tra) {
			File chmk = get_chomark_of(rem_cho_dir, curr_trans);
			all_chmks.add(chmk);
		}
		return all_chmks;
	}

	private void finish_chomarks_in(paccount rem_pcc, String choice_nm, key_owner owr,
			List<tag_transfer> all_tra) {
		
		//File rem_cho_dir = rem_pcc.get_chose_dir();
		if (is_remote_paccount()) {
			throw new bad_passet(2);
		}
		if (!rem_pcc.is_remote_paccount()) {
			throw new bad_passet(2);
		}
		if(choice_nm == null){
			throw new bad_passet(2);
		}
		if(all_tra == null){
			return;
		}
		File rem_cho_dir = new File(rem_pcc.get_chose_dir(), choice_nm);
		List<File> all_chmks = get_chomarks_for(rem_cho_dir, all_tra);
		finish_chomarks(owr, all_chmks);
	}

	private void finish_chomarks(key_owner owr, List<File> all_chmks) {
		if (IN_DEBUG_29) {
			File skip_ff = get_dbg_no_end_choice_file();
			if (skip_ff.exists()) {
				logger.info("SKIPPING_FINISH_CHOMARKS skip_file=" + skip_ff);
				return;
			}
		}
		if (is_remote_paccount()) {
			throw new bad_passet(2);
		}
		for (File chmk : all_chmks) {
			finish_chomark(owr, chmk);
		}
	}

	private void count_undoable_chomarks_in(paccount rem_pcc, key_owner owr,
			deno_counter cter, String choice_nm) {
		if (is_remote_paccount()) {
			throw new bad_passet(2);
		}
		if (!rem_pcc.is_remote_paccount()) {
			return;
		}

		int currcy_idx = get_working_currency();
		File[] all_ff = rem_pcc.get_all_chomarks(owr, choice_nm);
		for (File rem_ch_ff : all_ff) {
			if (!is_chomark(owr, rem_ch_ff)) {
				continue;
			}
			String deno_txt = get_deno_name_of_chomark_file(rem_ch_ff);
			tag_denomination deno = tag_denomination
					.parse_short_text_denomination(deno_txt, currcy_idx);
			deno_count cc = cter.get_deno_count(deno);
			cc.num_chosen++;
		}
	}

	private String get_next_transfer_name(key_owner owr) {
		File curr_cho_ff = get_current_choice_file();
		String dd_nm = null;
		if (curr_cho_ff.exists()) {
			dd_nm = mem_file.read_encrypted_string(curr_cho_ff, owr);
		}
		return dd_nm;
	}

	public void end_choice(paccount rem_pcc, String choice_nm, key_owner owr,
			List<tag_transfer> all_tra) {
		if (IN_DEBUG_29) {
			File skip_ff = get_dbg_no_end_choice_file();
			if (skip_ff.exists()) {
				logger.info("NO_END_CHOICE skip_file=" + skip_ff);
				return;
			}
		}
		finish_chomarks_in(rem_pcc, choice_nm, owr, all_tra); 
		undo_chomarks(rem_pcc, choice_nm, owr, null, -1);
		File trans_dd = new File(rem_pcc.get_chose_dir(), choice_nm);
		file_funcs.delete_dir(trans_dd);
		if(IN_DEBUG_31){
			logger.info("ending_trans dir=" + trans_dd);
		}
	}

	public String start_choice(key_owner owr) {
		if (!is_remote_paccount()) {
			throw new bad_passet(2);
		}
		String dd_nm = get_next_transfer_name(owr);
		if (dd_nm == null) {
			throw new bad_passet(2);
		}
		File curr_cho_ff = get_current_choice_file();
		file_funcs.concurrent_delete_file(curr_cho_ff);
		return dd_nm;
	}

	private File get_new_transfer_dir(key_owner owr) {
		String dd_nm = get_new_transfer_name(owr);
		File n_tra_dd = file_funcs.get_dir(get_chose_dir(), dd_nm);
		return n_tra_dd;
	}

	boolean is_valid_transaction(key_owner owr, String choice_nm) {
		if (choice_nm == null) {
			if(IN_DEBUG_33){
				logger.info("not_valid.tra. null choice_nm");
			}
			return false;
		}
		if (!is_remote_paccount()) {
			if(IN_DEBUG_33){
				logger.info("not_valid.tra. not remote pacc");
			}
			return false;
		}
		String dd_nm = get_next_transfer_name(owr);
		if (dd_nm != null) {
			if (dd_nm.equals(choice_nm)) {
				if(IN_DEBUG_33){
					logger.info("not_valid.tra. not started choice=" + choice_nm);
				}
				return false;
			}
		}
		File trans_dd = new File(get_chose_dir(), choice_nm);
		if (!trans_dd.isDirectory()) {
			if(IN_DEBUG_33){
				logger.info("not_valid.tra. not existant choice=" + trans_dd);
			}
			return false;
		}

		if(IN_DEBUG_33){
			logger.info("IS_VALID.tra. file=" + trans_dd);
		}
		return true;
	}

	private String get_new_transfer_name(key_owner owr) {
		String dd_nm = get_next_transfer_name(owr);
		if (dd_nm != null) {
			return dd_nm;
		}
		File curr_cho_ff = get_current_choice_file();
		long dd_id = System.currentTimeMillis();
		dd_nm = PRE_CHO_DIR + dd_id;
		mem_file.write_encrypted_string(curr_cho_ff, owr, dd_nm);
		return dd_nm;
	}

}
