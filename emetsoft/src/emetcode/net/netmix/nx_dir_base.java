package emetcode.net.netmix;

import java.io.File;
import java.io.FileFilter;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import emetcode.crypto.bitshake.utils.convert;
import emetcode.crypto.bitshake.utils.file_funcs;
import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.crypto.bitshake.utils.mem_file;
import emetcode.crypto.gamal.gamal_generator;
import emetcode.net.netmix.locale.L;
import emetcode.util.devel.logger;
import emetcode.util.devel.net_funcs;

public class nx_dir_base {
	static boolean IN_DEBUG_01 = false; // do not permit ref path delete
	static boolean IN_DEBUG_1 = true;
	static boolean IN_DEBUG_2 = false;
	static boolean IN_DEBUG_4 = false; // do not permit long paths
	static boolean IN_DEBUG_5 = false; // debug long paths
	static boolean IN_DEBUG_6 = false; // old coids
	static boolean IN_DEBUG_9 = false; // create ref coids
	static boolean IN_DEBUG_10 = true; // adding coref alias

	static final int MIN_GAMAL_BITS = 1000;
	static final int MIN_GAMAL_CERTAINTY = 1000;

	private static final String fixed_ref_patt_str = "[:_\\-\\.\\w]*";
	private static final Pattern FIXED_REF_PATT = Pattern
			.compile(fixed_ref_patt_str);

	static final String fixed_ref_suf = ".fixed";

	private static final int[] COREF_DIR_SLICES_1 = { 2, 2, 3, 4, 200, 200,
			200, 200 };

	private static final String MARK_F_NM = "is_coid_dir";
	private static final int[] COID_DIR_SLICES_1 = { 2, 2, 3, 4 };

	public static final char FLD_SEP = '.';

	public static final String COID_SUFIX = FLD_SEP + "coid";
	public static final String COID_PREFIX = "coid" + FLD_SEP;

	// static final String GAMAL_SYS_FNAM = "gamal_sys.dat";
	public static String GAMAL_FILE_SUF = ".gam";

	private int net_type;

	private File base_dir;
	private File proto_dir;
	private File all_glids_dir;
	private File all_coids_dir;
	private File all_by_coref_dir;

	private int num_gamal_bits;
	private int gamal_certainty;

	public nx_dir_base(File r_dir, int the_type) {
		init_nx_dir_base(the_type);
		set_root_dir(r_dir);
	}

	public nx_dir_base(nx_dir_base orig) {
		net_type = orig.net_type;

		base_dir = orig.base_dir;
		proto_dir = orig.proto_dir;
		all_glids_dir = orig.all_glids_dir;
		all_coids_dir = orig.all_coids_dir;
		all_by_coref_dir = orig.all_by_coref_dir;
	}

	void init_nx_dir_base(int the_type) {
		net_type = the_type;
		base_dir = null;
		proto_dir = null;
		all_glids_dir = null;
		all_coids_dir = null;
		all_by_coref_dir = null;

		num_gamal_bits = MIN_GAMAL_BITS;
		gamal_certainty = MIN_GAMAL_CERTAINTY;
	}

	public int get_net_type() {
		return net_type;
	}

	private void set_root_dir(File r_dir) {
		String net_nm = net_funcs.get_net_kind_str(get_net_type());

		base_dir = null;
		proto_dir = null;
		all_glids_dir = null;
		all_coids_dir = null;
		all_by_coref_dir = null;

		if (r_dir != null) {
			base_dir = file_funcs.get_dir(r_dir);
			proto_dir = file_funcs.get_dir(base_dir, net_nm);
			all_glids_dir = file_funcs.get_dir(proto_dir, config.DN_GLIDS_DIR);
			all_coids_dir = file_funcs.get_dir(proto_dir, config.DN_COIDS_DIR);
			all_by_coref_dir = file_funcs.get_dir(proto_dir,
					config.DN_BY_COREF_DIR);

			if (!all_glids_dir.exists()) {
				throw new bad_netmix(2);
			}
			if (!all_coids_dir.exists()) {
				throw new bad_netmix(2);
			}
			if (!all_by_coref_dir.exists()) {
				throw new bad_netmix(2);
			}
		}
	}

	public File get_netmix_base_dir() {
		if (base_dir == null) {
			throw new bad_netmix(2);
		}
		return base_dir;
	}

	public File get_protocol_base_dir() {
		if (proto_dir == null) {
			throw new bad_netmix(2);
		}
		return proto_dir;
	}

	public File get_all_glids_base_dir() {
		if (all_glids_dir == null) {
			throw new bad_netmix(2);
		}
		return all_glids_dir;
	}

	public File get_all_coids_base_dir() {
		if (all_coids_dir == null) {
			throw new bad_netmix(2);
		}
		return all_coids_dir;
	}

	public File get_all_coref_base_dir() {
		if (all_by_coref_dir == null) {
			throw new bad_netmix(2);
		}
		return all_by_coref_dir;
	}

	public String toString() {
		if (base_dir == null) {
			return "NO_nx_dir_base";
		}
		return get_netmix_base_dir().toString();
	}

	// gamal

	private File get_gamal_sys_file(key_owner owr) {
		File conf_dir = get_netmix_base_dir();
		String fnm = owr.get_mikid() + GAMAL_FILE_SUF;
		File gam_ff = new File(conf_dir, fnm);
		return gam_ff;
	}

	public boolean has_gamal_sys(key_owner owr) {
		File nm = get_gamal_sys_file(owr);
		return nm.exists();
	}

	public gamal_generator get_gamal_sys(key_owner owr) {
		File nm = get_gamal_sys_file(owr);
		if (!nm.exists()) {
			throw new bad_netmix(2, String.format(L.cannot_find_gammal_sys,
					nm.toString()));
		}
		String pub = mem_file.read_encrypted_string(nm, owr);
		gamal_generator gg2 = new gamal_generator(pub);
		return gg2;
	}

	public gamal_generator start_gamal_sys(key_owner owr) {
		File nm = get_gamal_sys_file(owr);
		if (!nm.exists() || is_gamal_too_old(nm)) {
			if (IN_DEBUG_1) {
				String stk_str = logger.get_stack_str();
				logger.debug("creating_GAMA_SYS_for=" + nm + "\n" + stk_str);
			}
			if (IN_DEBUG_2) {
				logger.info(logger.get_stack_str());
			}
			SecureRandom rnd = owr.new_SecureRandom();
			gamal_generator gg1 = new gamal_generator(num_gamal_bits,
					gamal_certainty, rnd);
			mem_file.write_encrypted_string(nm, owr, gg1.get_public_string());
			if (IN_DEBUG_1) {
				logger.debug("CREATED_GAMA_SYS_for=" + nm);
			}
			return gg1;
		}

		String pub = mem_file.read_encrypted_string(nm, owr);
		gamal_generator gg2 = new gamal_generator(pub);
		return gg2;
	}

	private boolean is_gamal_too_old(File nm) {
		long lst = nm.lastModified();
		long curr = System.currentTimeMillis();
		long diff = curr - lst;
		if (diff < 0) {
			return true;
		}

		long min = config.MIN_DAYS_TO_RECREATE_GAMAL;
		long days = config.DAYS_TO_RECREATE_GAMAL;
		if (days < min) {
			days = min;
		}
		long period = config.DAY_MILLIS * days;
		if (diff > period) {
			return true;
		}

		return false;
	}

	public static gamal_generator read_gamal_sys(File ff, key_owner owr) {
		String gg_str = mem_file.read_encrypted_string(ff, owr);
		gamal_generator gam = new gamal_generator(gg_str);
		return gam;
	}
	
	public void save_gamal_sys(gamal_generator gg1, key_owner owr) {
		if (gg1 == null) {
			return;
		}
		File nm = get_gamal_sys_file(owr);
		mem_file.write_encrypted_string(nm, owr, gg1.get_public_string());
		if (IN_DEBUG_1) {
			logger.info("Saving_GAMA_SYS_for=" + nm);
		}
	}

	public File get_local_nx_dir() {
		File loc_net_dd = get_netmix_base_dir();
		return loc_net_dd;
	}

	private static String get_sliced_coid_dir_path(nx_conn_id coid) {
		if (coid == null) {
			throw new bad_netmix(2);
		}
		String val_coid = coid.toString() + COID_SUFIX;
		String dir1 = val_coid;
		dir1 = file_funcs.as_sliced_dir_path(val_coid, COID_DIR_SLICES_1);
		return dir1;
	}

	private File get_coid_dir(nx_conn_id coid) {
		String f_dd = get_sliced_coid_dir_path(coid);
		File ff = new File(get_all_coids_base_dir(), f_dd);
		return ff;
	}

	private static boolean is_ref_file_name(File ff) {
		return ff.getName().endsWith(config.DN_REF_FILE_SUFIX);
	}

	private static FileFilter get_ref_filter() {
		FileFilter ffil = new FileFilter() {
			public boolean accept(File ff) {
				return is_ref_file_name(ff);
			}
		};
		return ffil;
	}

	private static String get_sliced_coref_dir_path(String coref_str) {
		String dir1 = coref_str;
		if (IN_DEBUG_4) {
			return dir1;
		}
		dir1 = file_funcs.as_sliced_dir_path(dir1, COREF_DIR_SLICES_1);
		return dir1;
	}

	static boolean is_valid_ref(String f_ref) {
		Matcher ref_mchr = FIXED_REF_PATT.matcher(f_ref);
		boolean mch_ok = ref_mchr.matches();
		return mch_ok;
	}

	static String fix_coref(String o_ref) {
		if (o_ref == null) {
			throw new bad_netmix(2);
		}
		if (is_valid_ref(o_ref)) {
			return o_ref;
		}
		String f_ref = convert.string_to_hex_string(o_ref) + fixed_ref_suf;
		return f_ref;
	}

	static String unfix_coref(String f_str) {
		if (f_str == null) {
			throw new bad_netmix(2);
		}
		if (!f_str.endsWith(fixed_ref_suf)) {
			return f_str;
		}
		int e_idx = f_str.length() - fixed_ref_suf.length();
		if (e_idx <= 0) {
			throw new bad_netmix(2);
		}
		String uf_str = f_str.substring(0, e_idx);
		uf_str = convert.hex_string_to_string(uf_str);
		return uf_str;
	}

	static File get_coref_dir(File by_coref_dir, String coref_str) {
		String fixed_coref = fix_coref(coref_str);
		String ff_nm = get_sliced_coref_dir_path(fixed_coref);
		File ff = new File(by_coref_dir, ff_nm);
		return ff;
	}

	static File[] get_coref_dir_files_for(File coref_dd, String coref_str) {

		if (!coref_dd.exists()) {
			return null;
		}
		if (!coref_dd.isDirectory()) {
			return null;
		}

		File[] all_ff = coref_dd.listFiles(get_ref_filter());
		return all_ff;
	}

	private static nx_conn_id ref_to_coid(String nm_ref) {
		int e_idx = nm_ref.length() - config.DN_REF_FILE_SUFIX.length();
		if (e_idx < 0) {
			return null;
		}
		long cnid = convert.try_parse_long(nm_ref.substring(0, e_idx));
		if (cnid == 0) {
			return null;
		}
		return new nx_conn_id(cnid);
	}

	private List<nx_conn_id> read_corefs(String coref_str) {

		File by_coref_dd = get_all_coref_base_dir();
		File coref_dd = get_coref_dir(by_coref_dd, coref_str);
		File[] all_ff = get_coref_dir_files_for(coref_dd, coref_str);
		if (all_ff == null) {
			return new ArrayList<nx_conn_id>();
		}

		List<nx_conn_id> all_coid = new ArrayList<nx_conn_id>();
		// File all_coids_dd = bdir.get_all_coids_base_dir();
		for (File ref_ff : all_ff) {
			if (IN_DEBUG_5) {
				logger.debug("read_coref. " + ref_ff);
			}
			String nm = ref_ff.getName();
			nx_conn_id the_coid = ref_to_coid(nm);
			if (the_coid == null) {
				continue;
			}
			File coid_ff = get_coid_file(the_coid);
			if (!coid_ff.exists()) {
				logger.debug_trace();
				logger.debug("\n\n\n\n\n\n\n\n\n\nxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
						+ "DELETING_REF. COID_FILE_NOT_FOUND_FOR_REF. "
						+ "\ncoid_ff=" + coid_ff + "\nref_ff=" + ref_ff);
				file_funcs.path_delete(by_coref_dd, ref_ff);
				continue;
			}
			if (IN_DEBUG_5) {
				logger.debug("added_coid=" + the_coid);
			}
			all_coid.add(the_coid);
		}
		return all_coid;
	}

	public nx_conn_id get_coid_by_ref(String coref_str, int[] num_found) {

		boolean has_num_found = ((num_found != null) && (num_found.length > 0));
		if (has_num_found) {
			num_found[0] = 0;
		}
		if (coref_str == null) {
			return null;
		}

		List<nx_conn_id> all_coids = read_corefs(coref_str);

		if (all_coids.isEmpty()) {
			return null;
		}

		if (has_num_found) {
			num_found[0] = all_coids.size();
		}

		nx_conn_id the_coid = all_coids.get(0);
		if (IN_DEBUG_6) {
			logger.debug("got_old_coid=" + the_coid);
		}
		// File all_coids_dd = bdir.get_all_coids_base_dir();
		File old_ff = get_coid_file(the_coid);
		if (!old_ff.exists()) {
			throw new bad_netmix(2, String.format(L.no_such_old_coid_file,
					old_ff));
		}
		return the_coid;
	}

	public File get_coid_file(nx_conn_id coid) {
		File c_dir = get_coid_dir(coid);
		File ff = new File(c_dir, COID_PREFIX + coid);
		return ff;
	}

	public File make_coid_dir(nx_conn_id coid) {
		File ff = get_coid_dir(coid);
		File mark = new File(ff, MARK_F_NM);
		if (!mark.exists()) {
			ff.mkdirs();
			mem_file.concurrent_create_file(mark);
		}
		return ff;
	}

	public void write_coid_file(nx_conn_id coid, byte[] se_kk, key_owner owr) {

		File fc = get_coid_file(coid);
		make_coid_dir(coid);

		if (IN_DEBUG_1) {
			logger.debug("\nWRITING_cokey OF_COID=" + coid + "\nIN_COID_FILE="
					+ fc);
		}
		mem_file.concurrent_write_encrypted_bytes(fc, owr, se_kk);
	}

	public File get_remote_nx_dir(nx_conn_id the_coid) {
		File rem_net_dd = get_coid_dir(the_coid);
		return rem_net_dd;
	}

	public boolean has_local_glid() {
		return nx_std_coref.has_glid(get_local_nx_dir());
	}

	public void set_local_glid(key_owner owr, nx_std_coref gld) {
		nx_std_coref.set_glid(get_local_nx_dir(), owr, gld);
	}

	public nx_std_coref get_local_glid(key_owner owr) {
		return nx_std_coref.get_glid(get_local_nx_dir(), owr);
	}

	public nx_std_coref get_remote_glid(nx_conn_id the_coid, key_owner owr) {
		return nx_std_coref.get_glid(get_remote_nx_dir(the_coid), owr);
	}

	public boolean has_local_alias() {
		return nx_std_coref.has_alias(get_local_nx_dir());
	}

	public void set_local_alias(key_owner owr, nx_std_coref gld) {
		nx_std_coref.set_alias(get_local_nx_dir(), owr, gld);
	}

	public nx_std_coref get_local_alias(key_owner owr) {
		return nx_std_coref.get_alias(get_local_nx_dir(), owr);
	}

	public nx_std_coref get_remote_alias(nx_conn_id the_coid, key_owner owr) {
		return nx_std_coref.get_alias(get_remote_nx_dir(the_coid), owr);
	}

	public boolean write_coref_alias(String orig_coref_str,
			String alias_coref_str) {

		nx_conn_id the_coid = get_coid_by_ref(orig_coref_str, null);
		if (the_coid == null) {
			return false;
		}

		boolean w_ok = write_coref(alias_coref_str, the_coid);

		if (IN_DEBUG_10) {
			if (w_ok) {
				logger.info("ADDED_coref_alias=" + alias_coref_str + " orig="
						+ orig_coref_str);
			}
		}
		return w_ok;
	}

	private File get_coref_file(String coref_str, nx_conn_id the_coid) {
		File by_coref_dir = get_all_coref_base_dir();
		String ff_nm = the_coid + config.DN_REF_FILE_SUFIX;
		File rf_dd = nx_dir_base.get_coref_dir(by_coref_dir, coref_str);
		File ff = new File(rf_dd, ff_nm);
		return ff;
	}

	private boolean has_coref(String coref_str, nx_conn_id the_coid) {
		File ff = get_coref_file(coref_str, the_coid);
		return ff.exists();
	}

	public boolean write_coref(String coref_str, nx_conn_id the_coid) {

		if (coref_str == null) {
			return false;
		}
		if (the_coid == null) {
			return false;
		}
		if (has_coref(coref_str, the_coid)) {
			return false;
		}

		File ff = get_coref_file(coref_str, the_coid);
		boolean created = file_funcs.mk_parent_dir(ff);
		mem_file.concurrent_create_file(ff);

		if (!created) {
			String stk_str = logger.get_stack_str();
			logger.debug("\n\n\n\n\n\n\n\n\n\nxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
					+ "REF_COID_EXISTED!!! \n\t" + ff + "\n" + stk_str);
		} else {
			if (IN_DEBUG_9) {
				logger.debug(// "\n\n\n\n\n\nxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"+
				"Created REF_COID \n\t" + ff);
			}
		}

		nx_conn_id r_coid = get_coid_by_ref(coref_str, null);
		if (r_coid == null) {
			throw new bad_netmix(2);
		}
		boolean all_ok = (the_coid.equals(r_coid));
		return all_ok;
	}

	public File get_coid_dir_by_ref(String coref_str) {
		nx_conn_id the_coid = get_coid_by_ref(coref_str, null);
		if (the_coid == null) {
			return null;
		}
		File dd = get_remote_nx_dir(the_coid);
		return dd;
	}

	public boolean has_coid_by_ref(String coref_str) {
		nx_conn_id the_coid = get_coid_by_ref(coref_str, null);
		return (the_coid != null);
	}

	public boolean delete_coref(String coref_str) {
		return delete_coref(coref_str, false);
	}

	private static void delete_long_path_file(File by_coref_dd, File ref_ff) {
		file_funcs.path_delete(by_coref_dd, ref_ff);
		logger.info("dir_base_Deleted_LONG_PATH file='" + ref_ff + "'");
	}

	boolean delete_coref(String coref_str, boolean force_it) {
		if (IN_DEBUG_01) {
			throw new bad_netmix(2, "TRYING_TO_DELETE_REF=" + coref_str);
		}
		if (nx_std_coref.is_glid(coref_str)) {
			if (IN_DEBUG_2) {
				throw new bad_netmix(2, "TRYING_TO_DELETE_GLID_REF="
						+ coref_str);
			}
			if (!force_it) {
				return false;
			}
		}

		File by_coref_dd = get_all_coref_base_dir();
		File coref_dd = nx_dir_base.get_coref_dir(by_coref_dd, coref_str);
		File[] all_ff = nx_dir_base
				.get_coref_dir_files_for(coref_dd, coref_str);
		if (all_ff == null) {
			delete_long_path_file(by_coref_dd, coref_dd);
			return false;
		}
		for (File ff : all_ff) {
			delete_long_path_file(by_coref_dd, ff);
		}
		return true;
	}

}
