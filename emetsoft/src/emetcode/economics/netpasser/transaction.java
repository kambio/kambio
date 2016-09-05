package emetcode.economics.netpasser;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.crypto.bitshake.utils.mem_file;
import emetcode.economics.netpasser.locale.L;
import emetcode.economics.passet.paccount;
import emetcode.economics.passet.tag_transfer;
import emetcode.net.netmix.nx_conn_id;
import emetcode.net.netmix.nx_messenger;
import emetcode.util.devel.logger;

public class transaction {
	public static final boolean IN_DEBUG_1 = false;
	public static final boolean IN_DEBUG_2 = true; // prt chosen

	public static final int MAX_STACK_SZ = 20;
	
	public static final int DEFAULT_PASSETNET_UDP_PORT = 8765;
	public static final int DEFAULT_PASSETNET_MUDP_TRANSFER_PORT = 7000;

	// oper maps
	//private static final Map<String, String> CANON_OPERS = new TreeMap<String, String>();
	private static final Map<String, String> NET_OPERS = new TreeMap<String, String>();
	private static final Map<String, String> NET_INV_OPERS = new TreeMap<String, String>();
	private static final Map<String, String> FILE_OPERS = new TreeMap<String, String>();
	private static final Map<String, String> USER_OPERS = new TreeMap<String, String>();

	// invalid oper
	public static final String INVALID_OPER = "invalid_oper";
	public static final String FINISH_OPER = "finish_oper";

	// net opers
	public static final String NET_ACCEPT_OPER = "net_accept";
	public static final String NET_CONTINUE_OPER = "net_continue";
	public static final String NET_CANCEL_OPER = "net_cancel";

	public static final String NET_ACKN_OPER = "net_ackn";

	public static final String NET_SEND_CREATE_CHANN_OPER = "net_send_create_chann";
	public static final String NET_RECV_CREATE_CHANN_OPER = "net_recv_create_chann";

	public static final String NET_SEND_PASSETS_OPER = "net_send_passets";
	public static final String NET_RECV_PASSETS_OPER = "net_recv_passets";

	public static final String NET_SEND_ADD_VERIF_OPER = "net_send_add_diff";
	public static final String NET_RECV_ADD_VERIF_OPER = "net_recv_add_diff";
	public static final String NET_SEND_GET_TRANSFER_OPER = "net_send_get_transfer";
	public static final String NET_RECV_GET_TRANSFER_OPER = "net_recv_get_transfer";

	public static final String NET_SEND_NEW_TRACKED_OPER = "net_send_new_tracked";
	public static final String NET_RECV_NEW_TRACKED_OPER = "net_recv_new_tracked";
	
	public static final String NET_SEND_UPLOAD_BACKUP_OPER = "net_send_upload_backup";
	public static final String NET_RECV_UPLOAD_BACKUP_OPER = "net_recv_upload_backup";
	public static final String NET_SEND_DOWNLOAD_BACKUP_OPER = "net_send_download_backup";
	public static final String NET_RECV_DOWNLOAD_BACKUP_OPER = "net_recv_download_backup";

	public static final String NET_SEND_CHANGE_SPLIT_OPER = "net_send_change_split";
	public static final String NET_RECV_CHANGE_SPLIT_OPER = "net_recv_change_split";
	public static final String NET_SEND_CHANGE_JOIN_OPER = "net_send_change_join";
	public static final String NET_RECV_CHANGE_JOIN_OPER = "net_recv_change_join";

	public static final String NET_SEND_FORWARD_DATA_OPER = "net_send_forward_data";
	public static final String NET_RECV_FORWARD_DATA_OPER = "net_recv_forward_data";

	// file opers
	static final String FILE_MAKE_REPECTACLES_OPER = "file_make_receptacles";
	static final String FILE_SING_REPECTACLES_OPER = "file_sign_receptacles";
	static final String FILE_CK_ARE_MINE_OPER = "file_ck_are_mine";
	static final String FILE_VERIFY_PASSETS_OPER = "file_verify_passets";
	static final String FILE_ADD_DIFF_OPER = "file_add_diff";
	static final String FILE_IMPORT_OPER = "file_import";
	static final String FILE_CALC_SPLIT_CHANGE_OPER = "file_calc_split_change";
	static final String FILE_CALC_JOIN_CHANGE_OPER = "file_calc_join_change";
	static final String FILE_END_CHOICE_OPER = "file_end_choice";

	// user opers
	public static final String USER_ASK_SELECT_LIST_OPER = "user_ask_select_list";
	public static final String USER_TELL_FINISHED_OPER = "user_tell_finished";

	public String state_oper;
	public boolean finished_all; // used in cashapp

	private trans_operator user_caller;
	private file_operator file_caller;
	private trans_operator last_caller;

	// to
	public nx_conn_id coid;
	public File coid_file;
	public String r_peer_descr;
	public byte[] start_key;
	public boolean r_is_addr;
	public boolean repo_locat;

	// with
	public paccount local_pcc;
	public paccount remote_pcc;	

	private File[] working_files;
	List<tag_transfer> all_working_iss;
	List<File> output_files;

	private String choice_nm;
	private File file_for_cho_nm;
	
	public transaction() {
		init_transaction();
		state_oper = FINISH_OPER;
	}

	public static transaction get_transaction_copy(transaction orig) {
		transaction trans = new transaction();
		trans.init_transaction_with(orig);
		return trans;
	}

	public void init_local_paccount(File dir, key_owner owr, int net_kind) {
		local_pcc = new paccount();
		local_pcc.set_base_dir(dir, owr, net_kind, null);
	}

	public void init_callers(trans_operator user_cllr, file_operator file_cllr) {
		user_caller = user_cllr;
		file_caller = file_cllr;
		last_caller = null;
	}

	private void init_transaction() {
		state_oper = INVALID_OPER;
		finished_all = false;

		user_caller = null;
		file_caller = null;
		last_caller = null;

		// to
		coid = null;
		coid_file = null;
		r_peer_descr = null;
		start_key = null;
		r_is_addr = false;
		repo_locat = true;

		// how much
		local_pcc = null;
		remote_pcc = null;

		working_files = null;
		all_working_iss = null;
		output_files = null;
		
		choice_nm = null;
	}

	private void init_transaction_with(transaction orig) {
		init_transaction();

		state_oper = orig.state_oper;

		user_caller = orig.user_caller;
		file_caller = orig.file_caller;

		coid = orig.coid;
		coid_file = orig.coid_file;
		r_peer_descr = orig.r_peer_descr;
		r_is_addr = orig.r_is_addr;
		repo_locat = orig.repo_locat;

		if (orig.local_pcc != null) {
			local_pcc = new paccount(orig.local_pcc);
		}
		if (orig.remote_pcc != null) {
			remote_pcc = new paccount(orig.remote_pcc);
		}
	}

	public void init_coid(String r_descr) {
		if (coid != null) {
			if (r_descr == null) {
				throw new bad_netpasser(2);
			}
			return;
		}
		if ((local_pcc != null) && (r_descr != null)) {
			coid = local_pcc.get_dir_base().get_coid_by_ref(r_descr, null);
		} else {
			coid = null;
		}
	}

	void set_connection_info(nx_messenger msgr) {
		r_peer_descr = new String(msgr.get_remote_descr());
		coid = msgr.get_coid();
		coid_file = msgr.get_coid_file();

		logger.debug("coid_file=" + coid_file);

		remote_pcc = local_pcc.get_sub_paccount(coid);
	}

	public String get_child_thread_name() {
		String trans_oper = state_oper;
		String pre_coid = "";
		if (coid != null) {
			pre_coid = "preset_coid-";
		}
		String thd_nm = Thread.currentThread().getName() + "\n\t" + pre_coid
				+ trans_oper + "-" + r_peer_descr;
		return thd_nm;
	}

	public int get_working_currency() {
		return local_pcc.get_working_currency();
	}

	public void set_working_currency(int currcy_idx) {
		local_pcc.set_working_currency(currcy_idx);
		logger.debug("local_curr_dir=" + local_pcc.get_currency_dir());

		if (remote_pcc != null) {
			remote_pcc.set_working_currency(currcy_idx, local_pcc);
			logger.debug("remote_curr_dir=" + remote_pcc.get_currency_dir());
		}
	}

	public boolean is_server_transac() {
		boolean c1 = (state_oper == NET_ACCEPT_OPER);
		return c1;
	}

	public static String get_net_inv_oper(String oper) {
		Map<String, String> dir = NET_INV_OPERS;
		if (!dir.containsKey(oper)) {
			throw new bad_netpasser(2, String.format(L.bad_inverse_oper, oper));
		}
		return dir.get(oper);
	}

	public static String get_net_oper(String oper) {
		Map<String, String> dir = NET_OPERS;
		if (!dir.containsKey(oper)) {
			throw new bad_netpasser(2, String.format(L.bad_net_oper, oper));
		}
		return dir.get(oper);
	}

	private void ask_oper(String oper, trans_operator src, trans_operator dest) {
		if (src == null) {
			throw new bad_netpasser(2);
		}
		if (dest == null) {
			throw new bad_netpasser(2);
		}
		if (oper == INVALID_OPER) {
			throw new bad_netpasser(2);
		}

		trans_operator lst_caller = last_caller;
		String old_oper = state_oper;

		state_oper = oper;
		last_caller = src;
		dest.queue_transaction(this);
		src.wait_for_transaction(this);
		last_caller = lst_caller;
		if (state_oper != transaction.NET_CONTINUE_OPER) {
			throw new bad_netpasser(2, String.format(L.bad_cancel_oper, oper));
		}
		state_oper = old_oper;
	}

	public void ask_file_oper(String oper, trans_operator src) {
		ask_oper(oper, src, file_caller);
	}

	public void ask_user_oper(String oper, trans_operator src) {
		ask_oper(oper, src, user_caller);
	}

	public void queue_in_user(String oper) {
		state_oper = oper;
		user_caller.queue_transaction(this);
	}

	public boolean has_user_caller() {
		return (user_caller != null);
	}

	public void answer_oper(String oper) {
		if (last_caller == null) {
			throw new bad_netpasser(2);
		}
		state_oper = oper;
		last_caller.queue_transaction(this);
	}

	List<File> get_chosen(key_owner owr, String tra_nm) {
		if (local_pcc == null) {
			return null;
		}
		if (remote_pcc == null) {
			return null;
		}

		List<File> all_ff = local_pcc.get_passets_of_chomarks_in(remote_pcc, owr, tra_nm);
		
		if(IN_DEBUG_2){
			logger.info("GET_CHOSEN. all_ff=" + all_ff.toString());
		}
		return all_ff;
	}
	
	private boolean has_channel() {
		boolean c1 = ((coid_file != null) && coid_file.exists());
		return c1;
	}

	public void cleanup() {
		logger.debug("CLEANUP");
		reset_files();
		reset_out_files();

		if (!has_channel() && (remote_pcc != null)) {
			logger.debug("WARNING_NO_CHANNEL_CREATED !!!");
		}
	}

	public static void init_file_opers() {
		Map<String, String> dir = FILE_OPERS;

		// file opers
		dir.put(FILE_MAKE_REPECTACLES_OPER, FILE_MAKE_REPECTACLES_OPER);
		dir.put(FILE_SING_REPECTACLES_OPER, FILE_SING_REPECTACLES_OPER);
		dir.put(FILE_CK_ARE_MINE_OPER, FILE_CK_ARE_MINE_OPER);
		dir.put(FILE_VERIFY_PASSETS_OPER, FILE_VERIFY_PASSETS_OPER);
		dir.put(FILE_ADD_DIFF_OPER, FILE_ADD_DIFF_OPER);
		dir.put(FILE_IMPORT_OPER, FILE_IMPORT_OPER);
		dir.put(FILE_CALC_SPLIT_CHANGE_OPER, FILE_CALC_SPLIT_CHANGE_OPER);
		dir.put(FILE_CALC_JOIN_CHANGE_OPER, FILE_CALC_JOIN_CHANGE_OPER);
		dir.put(FILE_END_CHOICE_OPER, FILE_END_CHOICE_OPER);
	}

	public static void init_user_opers() {
		Map<String, String> dir = USER_OPERS;

		// user opers
		//dir.put(USER_ASK_TRUST_LEVEL_OPER, USER_ASK_TRUST_LEVEL_OPER);
		dir.put(USER_ASK_SELECT_LIST_OPER, USER_ASK_SELECT_LIST_OPER);
		dir.put(USER_TELL_FINISHED_OPER, USER_TELL_FINISHED_OPER);
	}

	public static void init_net_opers() {
		Map<String, String> dir = NET_OPERS;

		// net opers
		dir.put(NET_ACCEPT_OPER, NET_ACCEPT_OPER);
		dir.put(NET_CONTINUE_OPER, NET_CONTINUE_OPER);
		dir.put(NET_CANCEL_OPER, NET_CANCEL_OPER);

		dir.put(NET_ACKN_OPER, NET_ACKN_OPER);

		dir.put(NET_SEND_CREATE_CHANN_OPER, NET_SEND_CREATE_CHANN_OPER);
		dir.put(NET_RECV_CREATE_CHANN_OPER, NET_RECV_CREATE_CHANN_OPER);

		dir.put(NET_SEND_PASSETS_OPER, NET_SEND_PASSETS_OPER);
		dir.put(NET_RECV_PASSETS_OPER, NET_RECV_PASSETS_OPER);

		dir.put(NET_SEND_ADD_VERIF_OPER, NET_SEND_ADD_VERIF_OPER);
		dir.put(NET_RECV_ADD_VERIF_OPER, NET_RECV_ADD_VERIF_OPER);
		dir.put(NET_SEND_GET_TRANSFER_OPER, NET_SEND_GET_TRANSFER_OPER);
		dir.put(NET_RECV_GET_TRANSFER_OPER, NET_RECV_GET_TRANSFER_OPER);

		dir.put(NET_SEND_NEW_TRACKED_OPER, NET_SEND_NEW_TRACKED_OPER);
		dir.put(NET_RECV_NEW_TRACKED_OPER, NET_RECV_NEW_TRACKED_OPER);
		
		dir.put(NET_SEND_UPLOAD_BACKUP_OPER, NET_SEND_UPLOAD_BACKUP_OPER);
		dir.put(NET_RECV_UPLOAD_BACKUP_OPER, NET_RECV_UPLOAD_BACKUP_OPER);
		dir.put(NET_SEND_DOWNLOAD_BACKUP_OPER, NET_SEND_DOWNLOAD_BACKUP_OPER);
		dir.put(NET_RECV_DOWNLOAD_BACKUP_OPER, NET_RECV_DOWNLOAD_BACKUP_OPER);

		dir.put(NET_SEND_CHANGE_SPLIT_OPER, NET_SEND_CHANGE_SPLIT_OPER);
		dir.put(NET_RECV_CHANGE_SPLIT_OPER, NET_RECV_CHANGE_SPLIT_OPER);
		dir.put(NET_SEND_CHANGE_JOIN_OPER, NET_SEND_CHANGE_JOIN_OPER);
		dir.put(NET_RECV_CHANGE_JOIN_OPER, NET_RECV_CHANGE_JOIN_OPER);

		dir.put(NET_SEND_FORWARD_DATA_OPER, NET_SEND_FORWARD_DATA_OPER);
		dir.put(NET_RECV_FORWARD_DATA_OPER, NET_RECV_FORWARD_DATA_OPER);

	}

	public static void init_net_inv_opers() {
		Map<String, String> dir = NET_INV_OPERS;

		// net opers
		// dir.put(NET_ACCEPT_OPER, NET_ACCEPT_OPER);
		// dir.put(NET_CONTINUE_OPER, NET_CONTINUE_OPER);
		// dir.put(NET_CANCEL_OPER, NET_CANCEL_OPER);

		// dir.put(NET_ACKN_OPER, NET_ACKN_OPER);

		dir.put(NET_SEND_CREATE_CHANN_OPER, NET_RECV_CREATE_CHANN_OPER);
		dir.put(NET_RECV_CREATE_CHANN_OPER, NET_SEND_CREATE_CHANN_OPER);

		dir.put(NET_SEND_PASSETS_OPER, NET_RECV_PASSETS_OPER);
		dir.put(NET_RECV_PASSETS_OPER, NET_SEND_PASSETS_OPER);

		dir.put(NET_SEND_ADD_VERIF_OPER, NET_RECV_ADD_VERIF_OPER);
		dir.put(NET_RECV_ADD_VERIF_OPER, NET_SEND_ADD_VERIF_OPER);
		dir.put(NET_SEND_GET_TRANSFER_OPER, NET_RECV_GET_TRANSFER_OPER);
		dir.put(NET_RECV_GET_TRANSFER_OPER, NET_SEND_GET_TRANSFER_OPER);

		dir.put(NET_SEND_NEW_TRACKED_OPER, NET_RECV_NEW_TRACKED_OPER);
		dir.put(NET_RECV_NEW_TRACKED_OPER, NET_SEND_NEW_TRACKED_OPER);
		
		dir.put(NET_SEND_UPLOAD_BACKUP_OPER, NET_RECV_UPLOAD_BACKUP_OPER);
		dir.put(NET_RECV_UPLOAD_BACKUP_OPER, NET_SEND_UPLOAD_BACKUP_OPER);
		dir.put(NET_SEND_DOWNLOAD_BACKUP_OPER, NET_RECV_DOWNLOAD_BACKUP_OPER);
		dir.put(NET_RECV_DOWNLOAD_BACKUP_OPER, NET_SEND_DOWNLOAD_BACKUP_OPER);

		dir.put(NET_SEND_CHANGE_SPLIT_OPER, NET_RECV_CHANGE_SPLIT_OPER);
		dir.put(NET_RECV_CHANGE_SPLIT_OPER, NET_SEND_CHANGE_SPLIT_OPER);
		dir.put(NET_SEND_CHANGE_JOIN_OPER, NET_RECV_CHANGE_JOIN_OPER);
		dir.put(NET_RECV_CHANGE_JOIN_OPER, NET_SEND_CHANGE_JOIN_OPER);

		dir.put(NET_SEND_FORWARD_DATA_OPER, NET_RECV_FORWARD_DATA_OPER);
		dir.put(NET_RECV_FORWARD_DATA_OPER, NET_SEND_FORWARD_DATA_OPER);
	}

	static {
		init_net_opers();
		init_net_inv_opers();
		init_file_opers();
		init_user_opers();
	};

	public void set_file(File w_ff) {
		File[] tmp_ff = new File[1];
		tmp_ff[0] = w_ff;
		set_files(tmp_ff);
	}

	public File get_file() {
		File[] tmp_ff = get_files();
		if (tmp_ff.length < 1) {
			throw new bad_netpasser(2);
		}
		return tmp_ff[0];
	}

	public void set_files(File[] w_ff) {
		if (working_files != null) {
			throw new bad_netpasser(2);
		}
		working_files = w_ff;
	}

	public void reset_files() {
		if (working_files == null) {
			return;
		}
		working_files = null;
	}

	public File[] get_files() {
		if (working_files == null) {
			throw new bad_netpasser(2);
		}
		return working_files;
	}

	void set_out_files(List<File> out_ff) {
		if (output_files != null) {
			throw new bad_netpasser(2);
		}
		output_files = out_ff;
	}

	void reset_out_files() {
		if (output_files == null) {
			return;
		}
		output_files = null;
	}

	List<File> get_out_files() {
		if (output_files == null) {
			throw new bad_netpasser(2);
		}
		return output_files;
	}

	public boolean has_finished_all() { // cashapp
		return finished_all;
	}

	public void set_choice_name(String cho_nm){
		if(choice_nm != null){
			throw new bad_netpasser(2);
		}
		choice_nm = cho_nm;
	}

	public void reset_choice_name(){
		if(choice_nm == null){
			throw new bad_netpasser(2);
		}
		choice_nm = null;
	}

	public String get_choice_name(){
		return choice_nm;
	}

	public void set_file_for_choice_name(String ff_for_cho_nm){
		if(file_for_cho_nm != null){
			throw new bad_netpasser(2);
		}
		file_for_cho_nm = new File(ff_for_cho_nm);
	}

	void save_choice_name(){
		if(file_for_cho_nm != null){
			mem_file.write_string(file_for_cho_nm, choice_nm);
			file_for_cho_nm = null;
		}
	}
}
