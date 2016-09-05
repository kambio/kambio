package io.github.kambio.kambio;

import java.io.File;
import java.util.Collections;
import java.util.List;

import emetcode.crypto.bitshake.utils.convert;
import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.crypto.bitshake.utils.mem_file;
import emetcode.crypto.gamal.gamal_generator;
import emetcode.economics.netpasser.file_operator;
import emetcode.economics.netpasser.net_operator;
import emetcode.economics.netpasser.transaction;
import emetcode.economics.passet.channel;
import emetcode.economics.passet.config;
import emetcode.economics.passet.deno_counter;
import emetcode.economics.passet.iso;
import emetcode.economics.passet.paccount;
import emetcode.economics.passet.tag_denomination;
import emetcode.economics.passet.trissuers;
import emetcode.net.netmix.nx_conn_id;
import emetcode.net.netmix.nx_context;
import emetcode.net.netmix.nx_peer;
import emetcode.net.netmix.tcp_adapter.nx_tcp_context;
import emetcode.util.devel.net_funcs;
import android.app.Application;
import android.util.Log;

public class app_base extends Application {
	public static final String LOGTAG = "app_base";

	public static final int MIN_KEY_LENGT = 4;

	public static final String CA_ROOT_DIR_NM = "PASSETS_ROOT_DIR";

	public static final String CA_CHOOSE_FILE_ACTIVITY = "CHOOSE_FILE_ACTIVITY";
	public static final String CA_CHOOSE_COUNTRY_CODE_ACTIVITY = "CHOOSE_COUNTRY_CODE_ACTIVITY";
	public static final String CA_CHOOSE_CURRENCY_CODE_ACTIVITY = "CHOOSE_CURRENCY_CODE_ACTIVITY";
	public static final String CA_GET_KEY_ACTIVITY = "GET_KEY_ACTIVITY";
	public static final String CA_VERIFY_KEY_ACTIVITY = "VERIFY_KEY_ACTIVITY";
	public static final String CA_PEOPLE_ACTIVITY = "PEOPLE_ACTIVITY";
	public static final String CA_USER_INFO_ACTIVITY = "USER_INFO_ACTIVITY";
	public static final String CA_CONFIG_ACTIVITY = "CONFIG_ACTIVITY";
	public static final String CA_ADD_CONTACT_ACTIVITY = "ADD_CONTACT_ACTIVITY";

	public static final int CA_INVALID_RESULT = 200;
	public static final int CA_OK_RESULT = 201;
	public static final int CA_CANCEL_RESULT = 202;

	public static final char CHAR_COLON = ':';
	public static final char CHAR_SEP = '_';
	public static final char CHAR_SPC = ' ';
	public static final char CHAR_NL = '\n';

	public static final String CA_NOTES_IMG_DIR_NM = "notes_images";
	public static final String CA_SUF_NOTE_IMG_FILE_NM = "_drw";
	public static final String CA_SUF_JPG = ".jpg";

	public static final String CA_PORT_FILE_NM = "last_port.dat";

	public static final int CA_MIN_TEXT_LIST_SZ = 12;
	public static final int CA_MIN_IMAGE_LIST_HEIGHT = 50;
	public static final int CA_MIN_IMAGE_LIST_WIDTH = 50;

	public static final int CA_MAX_TEXT_LIST_SZ = 100;
	public static final int CA_MAX_IMAGE_LIST_HEIGHT = 600;
	public static final int CA_MAX_IMAGE_LIST_WIDTH = 600;

	// / config data

	public static int CA_FIRST_TCP_PORT = 3080;
	public static int CA_LAST_TCP_PORT = 3100;

	public static int CA_CONTACTS_TEXT_LIST_SZ = 30;
	public static int CA_CONTACTS_IMAGE_LIST_HEIGHT = 100;
	public static int CA_CONTACTS_IMAGE_LIST_WIDTH = 100;

	int ca_cash_deno_text_sz;
	int ca_cash_chosen_text_sz;
	int ca_cash_can_give_text_sz;
	int ca_cash_have_text_sz;

	int ca_cash_image_list_height;
	int ca_cash_image_list_width;

	int ca_notes_image_files_currency_idx;
	deno_counter ca_notes_image_files;

	// / inter activity

	private String ca_activity_nm;

	String[] ca_all_ip;
	File ca_chosen_file;
	int ca_country_idx;
	int ca_currcy_idx;
	boolean ca_filter_contacts;
	boolean ca_refresh_contacts_view;
	List<channel> ca_all_chn;
	List<channel> ca_all_acc;
	//List<channel> ca_all_dbx;
	trissuers ca_all_trusted_by_contacs;
	channel ca_contact_info;
	nx_conn_id ca_added_contact_coid;

	// / other
	private File ca_root_dir;

	boolean ca_use_i2p;

	boolean ca_is_connecting;
	boolean ca_is_accepting;

	boolean ca_is_once;
	boolean ca_is_loop;

	boolean ca_is_daemon;
	int ca_trust_lev;

	String ca_l_ip_addr;
	int ca_l_port_num;
	String ca_l_descr;

	nx_context ca_l_ctx;
	nx_peer ca_l_peer;

	String ca_r_descr;

	key_owner ca_l_owner;

	byte[] ca_start_key;
	boolean ca_renew_channel;

	transaction ca_srv_trans;

	private paccount ca_local_pss;
	paccount ca_remote_pss;

	net_operator ca_srv_net_op;
	net_operator ca_cli_net_op;
	file_operator ca_fl_op;

	Thread ca_fl_thd;
	Thread ca_srv_thd;
	Thread ca_cli_thd;

	channel ca_current_channel;

	String ca_trans_oper;

	String ca_dbox_nm;
	String ca_dbox_recepid;

	public app_base() {
		ca_init_app_base();
	}

	public void ca_init_app_base() {

		ca_cash_deno_text_sz = 30;
		ca_cash_chosen_text_sz = 30;
		ca_cash_can_give_text_sz = 30;
		ca_cash_have_text_sz = 30;

		ca_cash_image_list_height = 100;
		ca_cash_image_list_width = 100;

		ca_notes_image_files_currency_idx = -1;
		ca_notes_image_files = null;

		ca_activity_nm = null;

		ca_all_ip = null;
		ca_chosen_file = null;
		ca_country_idx = 0;
		ca_currcy_idx = -1;
		ca_filter_contacts = false;
		ca_refresh_contacts_view = false;
		ca_all_chn = null;
		ca_all_acc = null;
		//ca_all_dbx = null;
		ca_all_trusted_by_contacs = null;
		ca_contact_info = null;
		ca_added_contact_coid = null;

		ca_use_i2p = false;

		ca_is_connecting = false;
		ca_is_accepting = false;

		ca_is_once = false;
		ca_is_loop = false;

		ca_is_daemon = false;

		ca_trust_lev = 0;

		ca_l_ip_addr = null;
		ca_l_port_num = -1;
		ca_l_descr = null;

		ca_l_ctx = null;
		ca_l_peer = null;

		ca_r_descr = null;

		ca_root_dir = null;
		ca_l_owner = null;

		ca_start_key = null;
		ca_renew_channel = false;

		ca_srv_trans = null;

		ca_local_pss = null;
		ca_remote_pss = null;

		ca_srv_net_op = null;
		ca_cli_net_op = null;
		ca_fl_op = null;

		ca_fl_thd = null;
		ca_srv_thd = null;
		ca_cli_thd = null;

		ca_current_channel = null;

		ca_trans_oper = null;

		ca_dbox_nm = null;
		ca_dbox_recepid = null;
	}

	public void ca_init_root() {
		File top_dir = getFilesDir();
		ca_root_dir = new File(top_dir, CA_ROOT_DIR_NM);

		if (!ca_root_dir.exists()) {
			if (!ca_root_dir.mkdirs()) {
				throw new bad_cashapp(2, "Cannot init root=" + ca_root_dir);
			}
		}
		Log.i(LOGTAG, "ROOT_DIR=" + ca_root_dir);
	}

	public boolean ca_has_root() {
		return ((ca_root_dir != null) && ca_root_dir.exists());
	}

	public File ca_get_root() {
		if (!ca_has_root()) {
			ca_init_root();
		}
		return ca_root_dir;
	}

	public boolean ca_has_empty_root() {
		File[] all_in_root = ca_get_root().listFiles();
		int num = all_in_root.length;
		return (num == 0);
	}

	public void ca_init_l_owner(byte[] the_key) {
		ca_get_root();
		ca_l_owner = new key_owner(the_key);
		Log.i(LOGTAG, "Inited owner=" + ca_l_owner.get_mikid());
	}

	public boolean ca_has_l_owner() {
		return (ca_l_owner != null);
	}

	private void ca_init_local_passet() {
		ca_get_root();
		if (!ca_has_l_owner()) {
			throw new bad_cashapp(2, "No owner");
		}
		Log.i(LOGTAG, "Initing local_passet");

		long val = ca_l_owner.new_random_long();
		int idx_sys = (int) convert.to_interval(val, 0, 100);
		gamal_generator gg = misce.read_gamal_sys(this, idx_sys);
		
		ca_local_pss = new paccount();
		ca_local_pss.set_base_dir(ca_root_dir, ca_l_owner, net_funcs.MUDP_NET, gg);

//		emetcode.economics.passet.config.DEFAULT_TRUSTED_LEVEL = ca_local_pss
//				.get_local_recv_lv(ca_l_owner);
	}

	private void ca_init_srv_trans() {
		ca_get_root();
		if (!ca_has_passet()) {
			throw new bad_cashapp(2, "No passet");
		}
		Log.i(LOGTAG, "Initing server transaction");

		ca_srv_trans = new transaction();
		ca_srv_trans.init_local_paccount(ca_root_dir, ca_l_owner, net_funcs.MUDP_NET);
		ca_srv_trans.state_oper = transaction.NET_ACCEPT_OPER;
		ca_srv_trans.init_callers(null, ca_fl_op);
		//ca_srv_trans.trust_level = transaction.TRUST_TRUSTED;
	}

	public void ca_in_actv(String nm_actv) {
		ca_activity_nm = nm_actv;
	}

	public boolean ca_was_in_actv(String nm_actv) {
		if (ca_activity_nm == nm_actv) {
			ca_activity_nm = null;
			return true;
		}
		return false;
	}

	public paccount ca_get_passet() {
		if (ca_local_pss == null) {
			throw new bad_cashapp(2, "No passet");
		}
		return ca_local_pss;
	}

	public void ca_init_passet() {
		if (!ca_has_passet() && ca_has_l_owner()) {
			ca_init_local_passet();
		}
	}

	public boolean ca_has_passet() {
		return (ca_local_pss != null);
	}

	public boolean ca_has_remote_passet() {
		return (ca_remote_pss != null);
	}

	public boolean ca_has_currency() {
		return (ca_local_pss.has_currency());
	}

	public boolean ca_has_changed_currency() {
		int idx1 = ca_local_pss.get_working_currency();
		boolean chgd1 = (idx1 != ca_currcy_idx);
		return chgd1;
	}

	private boolean ca_has_currency_file() {
		if (ca_has_passet()) {
			File deno_ff = ca_local_pss.get_deno_file();
			if (deno_ff.exists()) {
				return true;
			}
		}
		return false;
	}

	public boolean ca_has_chosen_currency() {
		return (ca_has_currency_file() || iso
				.is_valid_currency_idx(ca_currcy_idx));
	}

	public void ca_init_currency() {
		if (ca_has_passet() && ca_has_l_owner() && ca_has_chosen_currency()) {
			tag_denomination defa_deno = ca_local_pss.read_deno_file();
			if ((defa_deno != null)
					&& !iso.is_valid_currency_idx(ca_currcy_idx)) {
				if (iso.is_valid_currency_idx(defa_deno.currency_idx)) {
					ca_currcy_idx = defa_deno.currency_idx;
				} else {
					ca_currcy_idx = config.DEFAULT_CURRENCY;
					defa_deno.currency_idx = config.DEFAULT_CURRENCY;
				}
			}
			if (iso.is_valid_currency_idx(ca_currcy_idx)) {
				if (defa_deno == null) {
					defa_deno = new tag_denomination();
				}
				defa_deno.currency_idx = ca_currcy_idx;

				ca_local_pss.set_working_currency(ca_currcy_idx);
				ca_local_pss.write_deno_file(defa_deno);

				Log.i(LOGTAG, "Wrote default denomination");
			}
		}
	}

	public void ca_init_all_ip() {
		if (ca_all_ip == null) {
			Log.i(LOGTAG, "Initing ca_all_ip");
			ca_all_ip = misce.get_all_local_ip_addr();
		}
	}

	public boolean ca_is_net_ok() {
		ca_init_all_ip();
		return (ca_all_ip.length > 0);
	}

	public void ca_init_net_addr() {
		if (!ca_has_net_addr()) {
			ca_init_all_ip();
			if (ca_all_ip.length == 1) {
				ca_l_ip_addr = ca_all_ip[0];
			}
		}
	}

	public boolean ca_has_net_addr() {
		return (ca_l_ip_addr != null);
	}

	private File get_last_port_file() {
		return new File(ca_get_root(), CA_PORT_FILE_NM);
	}

	private int read_last_port() {
		File ff = get_last_port_file();
		String pp_str = mem_file.read_string(ff);
		if (pp_str == null) {
			return CA_FIRST_TCP_PORT;
		}
		int pp = Integer.parseInt(pp_str);
		return pp;
	}

	private void write_last_port(int val) {
		File ff = get_last_port_file();
		String pp_str = "" + val;
		mem_file.write_string(ff, pp_str);
	}

	private boolean ca_init_l_peer_with(int port_num) {
		if (ca_l_peer == null) {
			throw new bad_cashapp(2, "Null ca_l_peer");
		}

		ca_l_port_num = port_num;
		ca_l_descr = ca_l_ip_addr + CHAR_COLON + ca_l_port_num;
		ca_l_peer.init_local_peer(ca_l_descr, null, true);
		if (!ca_l_peer.can_accept()) {
			return false;
		}
		write_last_port(port_num);

		Log.i(LOGTAG, "Started ca_l_peer=" + ca_l_descr);

		return true;
	}

	public boolean ca_has_l_peer() {
		boolean hh = (ca_l_peer != null);
		return hh;
	}

	public boolean ca_init_l_peer() {
		if (ca_has_l_peer()) {
			return true;
		}
		Log.i(LOGTAG, "Initing ca_l_peer");

		ca_get_root();
		ca_l_ctx = new nx_tcp_context(ca_root_dir);

		ca_l_peer = ca_l_ctx.make_peer();
		ca_l_peer.set_owner(ca_l_owner);
		return true;
	}

	public boolean ca_started_l_peer() {
		if (ca_l_peer == null) {
			return false;
		}
		if (!ca_l_peer.can_accept()) {
			return false;
		}
		if (!ca_l_peer.has_owner()) {
			return false;
		}
		return true;
	}

	public void ca_start_l_peer() {
		if (ca_l_peer == null) {
			throw new bad_cashapp(2, "No ca_l_peer");
		}
		if (ca_l_peer.can_accept()) {
			return;
		}
		Log.i(LOGTAG, "Starting ca_l_peer");

		int lt_pp = read_last_port();
		if (ca_init_l_peer_with(lt_pp)) {
			return;
		}

		for (int pp = CA_FIRST_TCP_PORT; pp < CA_FIRST_TCP_PORT; pp++) {
			if ((pp != lt_pp) && ca_init_l_peer_with(pp)) {
				return;
			}
		}
	}

	public boolean ca_started_net_opers() {
		if ((ca_srv_net_op == null) || (ca_cli_net_op == null)
				|| (ca_fl_op == null)) {
			return false;
		}
		if ((ca_fl_thd == null) || (ca_srv_thd == null) || (ca_cli_thd == null)) {
			return false;
		}
		return true;
	}

	public boolean ca_running_net_opers() {
		if (!ca_fl_thd.isAlive()) {
			return false;
		}
		if (!ca_srv_thd.isAlive()) {
			return false;
		}
		if (!ca_cli_thd.isAlive()) {
			return false;
		}
		return true;
	}

	public void ca_start_net_opers() {
		// init net opers

		Log.i(LOGTAG, "Starting net and file operators and threads");

		ca_srv_net_op = new net_operator(ca_l_peer);
		ca_cli_net_op = new net_operator(ca_l_peer);
		ca_fl_op = new file_operator(ca_l_peer);

		// fill server transac

		ca_init_srv_trans();

		// start threads

		String thd_nm = ca_l_descr + "-files";
		ca_fl_thd = nx_context.start_thread(thd_nm, ca_fl_op, false);

		ca_srv_net_op.set_first_trans(ca_srv_trans);
		String thd_nm2 = ca_l_descr + "-srv_net-";
		ca_srv_thd = nx_context.start_thread(thd_nm2,
				ca_srv_net_op.get_run_loop_server(), false);
		// srv_net_op.queue_transaction(srv_trans);

		String thd_nm3 = ca_l_descr + "-cli_net-";
		ca_cli_thd = nx_context.start_thread(thd_nm3,
				ca_cli_net_op.get_run_loop_client(), false);
		// cli_net_op.queue_transaction(cli_trans);
	}

	File ca_get_note_images_dir() {
		if (!ca_has_passet()) {
			return null;
		}
		File mkdir = ca_local_pss.get_mikid_dir();
		File imgs_dir = new File(mkdir, CA_NOTES_IMG_DIR_NM);
		return imgs_dir;
	}

	File ca_get_note_image_file(tag_denomination deno) {
		String img_res_nam = app_base.ca_get_note_image_res_name(deno);
		File imgs_dir = ca_get_note_images_dir();

		String img_file_nam = img_res_nam + app_base.CA_SUF_JPG;
		File img_ff = new File(imgs_dir, img_file_nam);
		return img_ff;
	}

	static String ca_get_note_image_res_name(tag_denomination deno) {
		String img_res_nam = iso.get_currency_code(deno.currency_idx)
				+ app_base.CHAR_SEP + deno.get_short_text_denomination(false)
				+ app_base.CA_SUF_NOTE_IMG_FILE_NM;
		return img_res_nam;
	}

	public boolean ca_has_all_chn() {
		boolean c1 = (ca_all_chn != null);
		boolean c2 = (ca_all_acc != null);
		//boolean c3 = (ca_all_dbx != null);
		if (c1 && !c2) {
			throw new bad_cashapp(2);
		}
		return c1;
	}

	public boolean ca_has_contact() {
		return (ca_contact_info != null);
	}

	public boolean ca_has_trusted_by_contacts() {
		return (ca_all_trusted_by_contacs != null);
	}

	public transaction ca_get_new_cli_trans() {
		if (!ca_has_passet()) {
			return null;
		}
		if (!ca_has_l_owner()) {
			return null;
		}
		if (!iso.is_valid_currency_idx(ca_currcy_idx)) {
			return null;
		}

		int w_currcy = ca_local_pss.get_working_currency();
		if (w_currcy != ca_currcy_idx) {
			throw new bad_cashapp(2);
		}

		transaction cli_trans = new transaction();
		cli_trans.init_local_paccount(ca_root_dir, ca_l_owner, net_funcs.MUDP_NET);
		cli_trans.set_working_currency(ca_currcy_idx);

		return cli_trans;
	}

	// cli_trans.state_oper = trans_oper;
	// cli_trans.init_callers(null, fl_op);
	// cli_trans.start_key = start_key;
	// cli_trans.coid = selected_coid;
	// cli_trans.trust_level = transaction.TRUST_ANY;
	// cli_trans.dbox_name = box_nm;
	// cli_trans.dbox_recepuk = recepuk;
	// cli_trans.r_peer_descr = ip_addr;
	// cli_trans.use_local_chosen = false;

	boolean ca_has_new_contact(){
		return (ca_added_contact_coid != null);
	}
	
	void ca_add_new_contact(){
		nx_conn_id to_add = ca_added_contact_coid;
		ca_added_contact_coid = null;
		if( ! ca_has_passet()){
			Log.i(LOGTAG, "Trying to create contact without passet !!!!");
			return;
		}
		if( ! ca_has_l_owner()){
			Log.i(LOGTAG, "Trying to create contact without owr !!!!");
			return;
		}
		paccount pss = ca_get_passet();
		key_owner owr = ca_l_owner;
		channel n_chn = channel.read_channel(pss, owr, to_add);
		if(n_chn == null){
			Log.i(LOGTAG, "Cannot find created channel !!!!");
			return;
		}
		ca_all_chn.add(n_chn);
		Collections.sort(ca_all_chn);
	}
	
	void ca_get_lang(){
		//String lang = Locale.getDefault().getLanguage();
	}
	
}
