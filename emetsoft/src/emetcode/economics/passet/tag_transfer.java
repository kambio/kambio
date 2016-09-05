package emetcode.economics.passet;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import emetcode.crypto.bitshake.utils.convert;
import emetcode.crypto.bitshake.utils.global_id;
import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.crypto.bitshake.utils.transfer_crypto;
import emetcode.economics.passet.locale.L;
import emetcode.util.devel.logger;

public class tag_transfer implements Comparable<tag_transfer> {
	static final boolean IN_DEBUG_1 = true; // null giv_kbox
	static final boolean IN_DEBUG_2 = false; // num prev
	static final boolean IN_DEBUG_3 = true; // check denos
	static final boolean IN_DEBUG_4 = true; // init_with_lines

	static final String TRANSFER_IS_ROOT = "ROOT_TRANSFER";
	static final String TRANSFER_IS_RETURN = "RETURN_TRANSFER";
	static final String TRANSFER_IS_PAYMENT = "PAYMENT_TRANSFER";
	static final String TRANSFER_IS_EXCHANGE = "EXCHANGE_TRANSFER";
	static final String TRANSFER_IS_SPLIT = "SPLIT_TRANSFER";
	static final String TRANSFER_IS_JOIN = "JOIN_TRANSFER";

	static final String[] STD_TRANSFERS_ARR = { TRANSFER_IS_ROOT,
			TRANSFER_IS_RETURN, TRANSFER_IS_PAYMENT, TRANSFER_IS_EXCHANGE,
			TRANSFER_IS_SPLIT, TRANSFER_IS_JOIN };

	static final List<String> STD_TRANSFERS = Arrays.asList(STD_TRANSFERS_ARR);

	private static final tag_filglid ROOT_TRANSFER_ID = new tag_filglid(
			"ROOT_PASSET_ID");
	private static final tag_filglid NO_PASSET_ID = new tag_filglid(
			"NO_PASSET_ID");

	static final int FINAL_CONSEC = -1;

	static final String all_input_title = "Input";
	static final String all_output_title = "Output";
	static final String all_signa_title = "Signature";

	static final String receiver_title = "Receiver";
	static final String giver_title = "Giver";

	static final String root_issuer_field = "Root issuer ";
	static final String root_transfer_field = "Root transfer ";
	static final String tracker_id_field = "Tracker ";
	static final String first_transf_sha_field = "First transfer SHA function.";
	static final String transfer_consecutive_field = "Transfer consecutive.";
	static final String description_field = "Transfer description.";
	static final String num_kbox_field = "Number of cryptoboxes.";

	static final String receiver_cryptobox_field = "Receiver cryptobox";
	static final String cryptobox_private_key_field = " cryptobox private key";
	static final String transfer_crypto_signature_field = " transfer crypto-signature";

	static final String end_of_section_mark = "end_of_section_mark";

	private tag_accoglid root_issuer;
	private tag_filglid root_transfer;

	private tag_accoglid tracker_acoglid;
	private String description;

	private List<tag_input> all_in;

	private byte[] transfer_sha;
	private tag_filglid transfer_id;

	private tag_trader rcver;
	private tag_trader giver;

	private List<tag_output> all_out;
	private List<tag_signa> all_signa;

	private int trans_consec;

	private List<String> all_lines;

	tag_transfer() {
		init_tag_transfer();
	}

	private void init_tag_transfer() {
		root_issuer = null;
		root_transfer = null;

		tracker_acoglid = new tag_accoglid();
		description = config.UNKNOWN_STR;

		all_in = tag_input.get_list_input(1);
		all_out = tag_output.get_list_output(1);
		all_signa = tag_signa.get_list_signa(1);

		transfer_sha = null;
		transfer_id = null;

		rcver = new tag_trader();
		giver = new tag_trader();

		trans_consec = 0;
		all_lines = null;

		reset_consec();
	}

	void reset_description() {
		description = config.UNKNOWN_STR;
	}

	private static String calc_transfer_id(long creat_tm, byte[] sha_bts) {
		global_id fgli = new global_id(creat_tm, sha_bts);
		String pss_id = fgli.get_str();
		return pss_id;
	}

	long get_giver_tm() {
		return giver.tr_time.milis_time;
	}

	long get_rcver_tm() {
		return rcver.tr_time.milis_time;
	}

	tag_accoglid get_receiver_accoglid() {
		return rcver.tr_glid;
	}

	public int get_consec() {
		return trans_consec;
	}

	public void reset_consec() {
		trans_consec = 0;
	}

	public boolean has_root_issuer() {
		return (get_root_issuer() != null);
	}

	public tag_accoglid get_root_issuer() {
		return root_issuer;
	}

	void init_root(tag_accoglid r_iss, tag_filglid r_tra) {
		if (root_issuer != null) {
			throw new bad_passet(2);
		}
		if (root_transfer != null) {
			throw new bad_passet(2);
		}
		if (r_iss == null) {
			throw new bad_passet(2);
		}
		if (r_tra == null) {
			throw new bad_passet(2);
		}
		root_issuer = new tag_accoglid(r_iss);
		root_transfer = new tag_filglid(r_tra);
	}

	public boolean has_tracker_accoglid() {
		return (get_tracker_accoglid() != null);
	}

	public tag_accoglid get_tracker_accoglid() {
		return tracker_acoglid;
	}

	void init_tracker_accoglid(tag_accoglid iss_gli) {
		tracker_acoglid = new tag_accoglid(iss_gli);
	}

	private tag_time get_receiver_tm() {
		return rcver.tr_time;
	}

	tag_filglid get_passet_id() {
		if (is_direct_transfer()) {
			return all_in.get(0).get_passet_id();
		}
		return get_transfer_id();
	}

	int get_passet_idx() {
		if (all_in.size() != 1) {
			return -1;
		}
		return all_in.get(0).passet_idx;
	}

	public tag_filglid get_transfer_id() {
		if (transfer_id == null) {
			throw new bad_passet(2);
		}
		return transfer_id;
	}

	public byte[] get_transfer_sha() {
		return transfer_sha;
	}

	boolean has_passet_id() {
		boolean has_it = !get_passet_id().equals(NO_PASSET_ID);
		return has_it;
	}

	void set_passet_id(String pss_id_str) {
		set_passet_id(new tag_filglid(pss_id_str));
	}

	void set_passet_id(tag_filglid pss_id) {
		set_passet_id(pss_id, 0);
	}

	void set_passet_id(tag_filglid pss_id, int idx) {
		all_in.get(idx).set_passet_id(pss_id);
	}

	tag_denomination get_out_amount() {
		if (all_out.size() != 1) {
			throw new bad_passet(2);
		}
		return get_amount(0);
	}

	int get_out_currency() {
		return get_amount(0).currency_idx;
	}

	tag_denomination get_amount(int idx) {
		if (is_direct_transfer()) {
			return all_in.get(0).get_deno_copy();
		}
		return all_out.get(idx).get_deno_copy();
	}

	public String get_str() {
		String dn_str = get_amount(0).get_short_text_denomination(true);
		if(all_out.size() > 1){
			dn_str = tag_output.all_deno_str(all_out);
		}
		return "t." + trans_consec + ":" + dn_str;
	}
	
	public String toString() {
		return get_str();
	}

	void init_in_amount(tag_denomination deno) {
		if (all_in.size() != 1) {
			throw new bad_passet(2);
		}
		all_in.get(0).init_deno(deno);
	}

	private static String as_std_description(String descr) {
		int idx_1 = STD_TRANSFERS.indexOf(descr);
		if (idx_1 != -1) {
			return STD_TRANSFERS.get(idx_1);
		}
		return descr;
	}

	boolean is_root_transfer() {
		return (description == TRANSFER_IS_ROOT);
	}

	boolean is_join_transfer() {
		return (description == TRANSFER_IS_JOIN);
	}

	boolean is_split_transfer() {
		return (description == TRANSFER_IS_SPLIT);
	}

	boolean is_valid_idx(int idx) {
		if (idx < 0) {
			return false;
		}
		if (idx >= get_num_cryptobox()) {
			return false;
		}
		return true;
	}

	private int get_num_cryptobox() {
		if (all_in.size() != all_signa.size()) {
			throw new bad_passet(2);
		}
		if (is_join_transfer()) {
			return all_in.size();
		}
		if (is_split_transfer()) {
			return all_out.size();
		}
		return 1;
	}

	private void set_num_cryptobox(int sz) {
		int in_sz = 1;
		int out_sz = 1;
		if (is_join_transfer()) {
			in_sz = sz;
		}
		if (is_split_transfer()) {
			out_sz = sz;
		}
		all_in = tag_input.get_list_input(in_sz);
		all_out = tag_output.get_list_output(out_sz);
		
		if(all_in.size() != all_signa.size()){
			all_signa = tag_signa.get_list_signa(in_sz);
		}
	}

	private static int cmp_by_transfer_id(tag_transfer in_1, tag_transfer in_2) {
		String p1 = in_1.get_transfer_id().get_str();
		String p2 = in_1.get_transfer_id().get_str();
		return p1.compareTo(p2);
	}

	private static Comparator<tag_transfer> get_comparator_by_transfer_id() {
		Comparator<tag_transfer> the_cmp = new Comparator<tag_transfer>() {
			public int compare(tag_transfer in_1, tag_transfer in_2) {
				return cmp_by_transfer_id(in_1, in_2);
			}
		};
		return the_cmp;
	}

	private static void sort_by_transfer_id(List<tag_transfer> all_prev) {
		Collections.sort(all_prev, get_comparator_by_transfer_id());
	}

	transfer_crypto get_receiver_kbox(int idx) {
		transfer_crypto r_box = all_out.get(idx).get_receiver_kbox();
		return r_box;
	}

	static void ck_prev_sz_and_idx(List<tag_transfer> all_prev, int idx) {
		if ((idx != -1) && (all_prev.size() != 1)) {
			// idx is only used when the prev_transfer is multi_out
			// in that case this transfer MUST be direct (one in - one out)
			throw new bad_passet(2);
		}
	}

	void init_with_prev(tag_transfer prev) {
		if (!prev.is_direct_transfer()) {
			throw new bad_passet(0);
		}
		init_with_prev(prev, -1);
	}

	void init_with_prev(tag_transfer prev, int prev_idx) {
		init_root(prev.get_root_issuer(), prev.root_transfer);
		init_tracker_accoglid(prev.get_tracker_accoglid());

		List<tag_transfer> prv_lst = new ArrayList<tag_transfer>();
		prv_lst.add(prev);
		init_with_prev_list(prv_lst, prev_idx);
	}

	void init_with_prev_list(List<tag_transfer> all_prev, int prev_idx) {
		if (all_prev.isEmpty()) {
			trans_consec = 0;
			return;
		}
		ck_prev_sz_and_idx(all_prev, prev_idx);

		if (IN_DEBUG_2) {
			logger.debug("init_signas_all_prev_sz=" + all_prev.size());
		}

		sort_by_transfer_id(all_prev);

		all_in = tag_input.get_list_input(all_prev.size());
		all_signa = tag_signa.get_list_signa(all_prev.size());

		tag_input.init_inputs_with_prev_list(all_in, all_prev, prev_idx);
		tag_signa.init_signas_with_prev_list(all_signa, all_prev, prev_idx);

		trans_consec = all_prev.get(0).trans_consec + 1;
		if (all_prev.size() > 1) {
			trans_consec = 0;
		}

	}

	void check_inputs(tag_transfer curr_trans) {
		if (!tag_input.equal_inputs(all_in, curr_trans.all_in)) {
			throw new bad_passet(2);
		}
	}

	ListIterator<String> init_until_outputs_with_lines(List<String> tra_lines) {
		if (tra_lines == null) {
			throw new bad_passet(2, L.null_receiver_lines);
		}
		if (all_lines != null) {
			throw new bad_passet(2);
		}
		all_lines = tra_lines;

		ListIterator<String> it1 = tra_lines.listIterator();

		parse.skip_lines(it1, parse.get_start_transfer());

		trans_consec = convert.parse_int(parse.get_next_field_from(it1, null,
				transfer_consecutive_field));

		root_issuer = new tag_accoglid();
		root_issuer.init_accoglid(it1, root_issuer_field);

		root_transfer = new tag_filglid();
		root_transfer.init_filglid(it1, root_transfer_field);

		tracker_acoglid = new tag_accoglid();
		tracker_acoglid.init_accoglid(it1, tracker_id_field);

		String read_descr = parse.get_next_field_from(it1, null,
				description_field);
		description = as_std_description(read_descr);

		String read_num_kbox = parse.get_next_field_from(it1, null,
				num_kbox_field);
		int num_kbox = convert.parse_int(read_num_kbox);
		set_num_cryptobox(num_kbox);

		tag_input.init_list_input_with(all_in, it1, all_input_title,
				is_direct_transfer());

		rcver = new tag_trader();
		rcver.init_trader_with(it1, receiver_title);

		long rcv_tm = get_rcver_tm();
		tag_output.init_list_output_with(all_out, it1, all_output_title,
				tra_lines, rcv_tm, is_direct_transfer());

		return it1;
	}

	List<String> get_lines() {
		return all_lines;
	}

	boolean has_giv_boxes(boolean ck_kbox_key) {
		return tag_signa.all_signas_have_giv_box(all_signa, ck_kbox_key);
	}

	boolean has_rcv_boxes(boolean ck_kbox_key) {
		return tag_output.all_out_have_rcv_box(all_out, ck_kbox_key);
	}

	void init_with_lines(List<String> tra_lines) {
		ListIterator<String> it1 = init_until_outputs_with_lines(tra_lines);

		if (IN_DEBUG_4) {
			logger.info("TYPE=" + description + " in_sz=" + all_in.size()
					+ " out_sz=" + all_out.size() + " sgn_sz="
					+ all_signa.size());
		}

		giver = new tag_trader();
		giver.init_trader_with(it1, giver_title);

		tag_signa.init_list_signa_with(all_signa, it1, all_signa_title,
				tra_lines, is_root_transfer());

		parse.skip_lines(it1, parse.get_end_section());
		parse.get_mark_from(it1, end_of_section_mark);

		set_transfer_id(tra_lines);

		if (is_root_transfer()) {
			set_passet_id(transfer_id);
		}
	}

	boolean is_direct_transfer() {
		boolean one_in = (all_in.size() == 1);
		boolean one_out = (all_out.size() == 1);
		return (one_in && one_out);
	}

	boolean is_multi_out_transfer() {
		boolean one_in = (all_in.size() == 1);
		boolean many_out = (all_out.size() > 1);
		if (many_out && !one_in) {
			throw new bad_passet(2);
		}
		boolean m_out = (one_in && many_out);
		if (m_out != is_split_transfer()) {
			throw new bad_passet(2);
		}
		return m_out;
	}

	boolean is_multi_in_transfer() {
		boolean multi_in = (all_in.size() > 1);
		boolean one_out = (all_out.size() == 1);
		if (multi_in && !one_out) {
			throw new bad_passet(2);
		}
		boolean m_in = (multi_in && one_out);
		if (m_in != is_join_transfer()) {
			throw new bad_passet(2);
		}
		return m_in;
	}

	void add_until_outputs_lines(tag_trader local_trader, key_owner owr,
			List<String> tra_lines) {
		if (trans_consec < 0) {
			throw new bad_passet(2);
		}

		if (!owr.has_secret()) {
			throw new bad_passet(2);
		}
		if (root_issuer == null) {
			throw new bad_passet(2);
		}
		if (root_transfer == null) {
			throw new bad_passet(2);
		}

		local_trader.set_now();
		rcver.tr_time.set_to(local_trader.tr_time.milis_time);

		List<String> rcvr_lines = local_trader.get_trader_lines(receiver_title);

		List<String> root_isr_lines = root_issuer
				.get_accoglid_lines(root_issuer_field);
		List<String> root_tra_lines = root_transfer
				.get_filglid_lines(root_transfer_field);

		List<String> trk_lines = tracker_acoglid
				.get_accoglid_lines(tracker_id_field);

		tra_lines.addAll(parse.get_start_transfer());

		parse.add_next_field_to(tra_lines, null, transfer_consecutive_field, ""
				+ get_consec());
		tra_lines.addAll(root_isr_lines);
		tra_lines.addAll(root_tra_lines);
		tra_lines.addAll(trk_lines);
		parse.add_next_field_to(tra_lines, null, description_field, description);

		int num_kbox = get_num_cryptobox();
		parse.add_next_field_to(tra_lines, null, num_kbox_field, "" + num_kbox);

		tag_input.add_list_input_lines_to(all_in, tra_lines, all_input_title,
				is_direct_transfer());

		tra_lines.addAll(rcvr_lines);

		long rcv_tm = get_rcver_tm();
		tag_output.add_list_output_lines_to(all_out, tra_lines,
				all_output_title, owr, rcv_tm, is_direct_transfer());

	}

	void add_signa_lines(tag_trader local_trader, List<String> nxt_out_lines) {
		if (trans_consec < 0) {
			throw new bad_passet(2);
		}

		local_trader.set_now();

		List<String> gver_lines = local_trader.get_trader_lines(giver_title);

		nxt_out_lines.addAll(gver_lines);

		tag_signa.add_list_signa_lines_to(all_signa, nxt_out_lines,
				all_signa_title, is_root_transfer());

		nxt_out_lines.addAll(parse.get_end_section());
		parse.add_mark_to(nxt_out_lines, end_of_section_mark);

		set_transfer_id(nxt_out_lines);
	}

	List<String> get_root_transfer_lines(tag_trader local_trader, key_owner owr) {
		if (trans_consec != 0) {
			throw new bad_passet(2);
		}
		if (all_lines != null) {
			throw new bad_passet(2);
		}
		description = TRANSFER_IS_ROOT;

		init_root(local_trader.tr_glid, ROOT_TRANSFER_ID);

		all_lines = new ArrayList<String>();
		add_until_outputs_lines(local_trader, owr, all_lines);

		add_signa_lines(local_trader, all_lines);

		set_passet_id(new tag_filglid(transfer_id.get_str()));
		return all_lines;
	}

	private void set_transfer_id(List<String> transfer_lines) {
		transfer_sha = parse.calc_sha_lines(transfer_lines);

		long issr_tm = get_receiver_tm().milis_time;
		String tra_id_str = calc_transfer_id(issr_tm, transfer_sha);

		transfer_id = new tag_filglid(tra_id_str);
	}

	void save_rcv_kbox_pvks_in_dir(File pvks_dd, key_owner owr) {
		tag_output.save_pvks(all_out, pvks_dd, owr);
	}

	void spend_in(File spent_dd, File pvks_base) {
		tag_signa.spend_all(all_signa, spent_dd, pvks_base);
	}

	File get_recep_file(File base_dd) {
		if (all_signa.size() != 1) {
			throw new bad_passet(0);
		}
		return get_recep_file(base_dd, 0);
	}

	private File get_recep_file(File base_dd, int idx) {
		return all_signa.get(idx).get_recep_file(base_dd);
	}

	void check_mine_with(File spent_dd, key_owner owr, File pvks_dd) {
		if (!is_direct_transfer()) {
			throw new bad_passet(0);
		}
		check_mine_with(spent_dd, owr, pvks_dd, 0);
	}

	void check_mine_with(File spent_dd, key_owner owr, File pvks_dd, int idx) {
		all_out.get(idx).check_mine_with(spent_dd, owr, pvks_dd);
	}

	void ck_pss_id(String pss_id_str, File pss_ff) {
		String vrf_pss_id = get_passet_id().get_str();
		boolean eq_id = vrf_pss_id.equals(pss_id_str);
		if (!eq_id) {
			throw new bad_passet(2, "\n" + vrf_pss_id + " != \n" + pss_id_str
					+ "\n WITH_FILE=" + pss_ff);
		}
	}

	List<String> get_all_prev_tra_id_str() {
		List<String> all_id_str = new ArrayList<String>();
		List<tag_filglid> all_ids = get_all_prev_tra_id();
		for(tag_filglid prv_id : all_ids){
			all_id_str.add(prv_id.get_str());
		}
		return all_id_str;
	}
	
	List<tag_filglid> get_all_prev_tra_id() {
		if (is_root_transfer()) {
			logger.debug("ERROR_in__get_all_prev_tra_id 1");
			throw new bad_passet(2);
		}
		if (all_in.isEmpty()) {
			logger.debug("ERROR_in__get_all_prev_tra_id 2");
			throw new bad_passet(2);
		}
		List<tag_filglid> all_prv_ids = tag_input.get_prev_transfer_paths(all_in); 
		if (all_prv_ids.isEmpty()) {
			logger.debug("ERROR_in__get_all_prev_tra_id 3");
			throw new bad_passet(2);
		}
		return all_prv_ids;
	}

	tag_filglid get_prev_tra_id() {
		if (!is_direct_transfer()) {
			throw new bad_passet(2);
		}
		List<tag_filglid> all_nm = tag_input.get_prev_transfer_paths(all_in);
		return all_nm.get(0);
	}

	String get_issuance_file_id() {
		return get_passet_id().get_str();
	}

	String get_file_id() {
		return get_transfer_id().get_str();
	}

	static List<tag_denomination> get_denos(Collection<tag_transfer> all_dat) {
		List<tag_denomination> all_deno = new ArrayList<tag_denomination>();
		for (tag_transfer iss : all_dat) {
			tag_denomination dd = iss.get_out_amount();
			all_deno.add(dd);
		}
		return all_deno;
	}

	void init_all_outputs_with(deno_counter cntr) {
		tag_output.init_list_output_with(all_out, cntr);
	}

	private void init_split_transfer(deno_counter cntr,
			tag_trader local_trader, key_owner owr, int min_expo) {
		init_all_outputs_with(cntr);

		all_lines = new ArrayList<String>();
		add_until_outputs_lines(local_trader, owr, all_lines);
		add_signa_lines(local_trader, all_lines);
	}

	int num_out() {
		return all_out.size();
	}

	tag_transfer get_split_transfer(tag_trader local_trader, key_owner owr,
			int min_expo) {
		tag_transfer split_trans = new tag_transfer();
		split_trans.description = TRANSFER_IS_SPLIT;
		split_trans.init_with_prev(this);

		deno_counter cntr = tag_denomination.split_transfer(this, min_expo);

		split_trans.init_split_transfer(cntr, local_trader, owr, min_expo);

		if (!split_trans.is_split_transfer()) {
			throw new bad_passet(2);
		}
		if (split_trans.transfer_id == null) {
			throw new bad_passet(2);
		}
		return split_trans;
	}

	void ck_tracker(tag_accoglid prev_gg) {
		tag_accoglid tra_gg = get_tracker_accoglid();
		if (tra_gg == null) {
			throw new bad_passet(2, "NULL_tra_gg");
		}
		if (prev_gg == null) {
			throw new bad_passet(2, "NULL_prev_gg");
		}
		if (!tra_gg.equals(prev_gg)) {
			throw new bad_passet(2);
		}
	}

	void check_denos() {
		if (is_direct_transfer()) {
			return;
		}

		if (IN_DEBUG_3) {
			logger.info("START_check_denos of " + this);
		}

		List<tag_denomination> all_in_dd = tag_input.get_all_deno(all_in);
		deno_counter dd_in = new deno_counter();
		dd_in.init_with_denos(all_in_dd);

		List<tag_denomination> all_out_dd = tag_output.get_all_deno(all_out);
		deno_counter dd_out = new deno_counter();
		dd_out.init_with_denos(all_out_dd);

		if (is_multi_out_transfer()) {
			deno_counter ck_in = tag_denomination.calc_join_denos(all_out_dd);
			boolean ok_ck_in = ck_in.equals(dd_in);
			if (!ok_ck_in) {
				throw new bad_passet(2);
			}
			if (IN_DEBUG_3) {
				logger.info("FINISHED_check_denos of " + this);
			}
			return;
		}
		if (is_multi_in_transfer()) {
			deno_counter ck_out = tag_denomination.calc_join_denos(all_in_dd);
			boolean ok_ck_out = ck_out.equals(dd_out);
			if (!ok_ck_out) {
				throw new bad_passet(2);
			}
			if (IN_DEBUG_3) {
				logger.info("FINISHED_check_denos of " + this);
			}
			return;
		}

		throw new bad_passet(2);
	}

	private static boolean cmp_times(long tm1, long tm2, boolean aprox) {
		boolean tm_ok = (tm1 <= tm2);
		if (aprox) {
			int cmp_cur = tag_time
					.cmp_aprox_time(tm1, tm2, tag_time.STD_ERR_TM);
			tm_ok = (cmp_cur <= 0);
		}
		return tm_ok;
	}

	static void check_before_time(long tm1, long tm2, boolean aprox) {
		boolean tm_ok = cmp_times(tm1, tm2, aprox);
		if (!tm_ok) {
			throw new bad_passet(2, String.format(
					L.cannot_transfer_before_time, convert.utc_to_string(tm1),
					convert.utc_to_string(tm2)));
		}
	}

	private static void check_times(long tm1, long tm2, boolean aprox) {
		boolean tm_ok = cmp_times(tm1, tm2, aprox);
		if (!tm_ok) {
			throw new bad_passet(2, String.format(
					L.inconsistent_transfer_times, convert.utc_to_string(tm1),
					convert.utc_to_string(tm2)));
		}
	}

	private void check_transfer_times(tag_transfer prev_tra) {
		long prt = prev_tra.get_rcver_tm();
		long pgt = prev_tra.get_giver_tm();
		long rt = get_rcver_tm();
		long gt = get_giver_tm();

		check_times(prt, rt, false);
		check_times(prt, gt, false);
		check_times(pgt, rt, false);
		check_times(pgt, gt, false);
		check_times(prt, pgt, true);
		check_times(rt, gt, true);
	}

	void check_all_transfer_times(List<tag_transfer> all_prev_tra) {
		for (tag_transfer prv_tra : all_prev_tra) {
			check_transfer_times(prv_tra);
		}
	}

	boolean has_same_root(tag_transfer other_tra) {
		boolean c1 = other_tra.get_root_issuer().equals(get_root_issuer());
		boolean c2 = other_tra.root_transfer.equals(root_transfer);
		boolean s_roo = (c1 && c2);
		return s_roo;
	}

	tag_filglid get_root_transfer() {
		return root_transfer;
	}

	void init_join_transfer(tag_trader local_trader,
			List<tag_transfer> to_join, tag_denomination tgt_join, key_owner owr) {

		description = TRANSFER_IS_JOIN;
		init_with_prev_list(to_join, -1);
		tag_output.init_list_output_with(all_out, tgt_join);

		all_lines = new ArrayList<String>();
		add_until_outputs_lines(local_trader, owr, all_lines);
		add_signa_lines(local_trader, all_lines);

		if (!is_join_transfer()) {
			throw new bad_passet(2);
		}
		if (transfer_id == null) {
			throw new bad_passet(2);
		}
	}

	public boolean equals(Object obj){
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof tag_transfer)) {
			throw new bad_passet(2);
		}
		tag_transfer t2 = (tag_transfer) obj;
		tag_filglid id1 = get_transfer_id();
		tag_filglid id2 = t2.get_transfer_id();
		return id1.equals(id2);
	}

	@Override
	public int compareTo(tag_transfer t2) {
		tag_filglid id1 = get_transfer_id();
		tag_filglid id2 = t2.get_transfer_id();
		String id1_str = id1.get_str();
		String id2_str = id2.get_str();
		return id1_str.compareTo(id2_str);
	}
}
