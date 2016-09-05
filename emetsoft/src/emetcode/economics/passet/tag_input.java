package emetcode.economics.passet;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import emetcode.crypto.bitshake.utils.convert;
import emetcode.util.devel.logger;

public class tag_input {

	static final tag_filglid NO_PASSET_ID = new tag_filglid("NO_PASSET_ID");

	static final tag_filglid NO_TRANSFER_ID = new tag_filglid("NO_TRANSFER_ID");

	static final boolean IN_DEBUG_1 = true; // init input
	static final boolean IN_DEBUG_2 = true; // NOT eq input

	public static final String NO_PREV_SHA = "No previous sha";

	static final String first_passet_id_title = "First passet ";
	static final String prev_passet_id_title = "Previous passet ";
	static final String kbox_pos_field = "Cryptobox position in first id.";
	static final String previous_sha_field = "Previous transfer SHA function.";

	private tag_denomination passet_deno;
	tag_filglid passet_id;
	int passet_idx;
	tag_filglid prev_transfer_id;
	String prev_transfer_sha;

	public tag_input() {
		init_tag_input();
	}

	public tag_input(long milis) {
		init_tag_input();
	}

	public tag_input(tag_input orig) {
		if (orig == null) {
			init_tag_input();
			return;
		}
		passet_deno = new tag_denomination(orig.passet_deno);
		passet_id = orig.passet_id;
		passet_idx = orig.passet_idx;
		prev_transfer_id = orig.prev_transfer_id;
		prev_transfer_sha = orig.prev_transfer_sha;
	}

	void init_tag_input() {
		passet_deno = new tag_denomination(config.DEFAULT_CURRENCY, 1, 1);
		passet_id = new tag_filglid(NO_PASSET_ID);
		passet_idx = 0;
		prev_transfer_id = new tag_filglid(NO_TRANSFER_ID);
		prev_transfer_sha = NO_PREV_SHA;
	}

	void init_tag_input_with(tag_transfer prev_tra, int idx) {
		passet_id = prev_tra.get_passet_id();
		if (prev_tra.is_direct_transfer()) {
			passet_deno = new tag_denomination(prev_tra.get_out_amount());
			passet_idx = prev_tra.get_passet_idx();
		} else if (prev_tra.is_multi_in_transfer()) {
			passet_deno = new tag_denomination(prev_tra.get_out_amount());
			passet_idx = 0;
		} else if (prev_tra.is_multi_out_transfer()) {
			if (!prev_tra.is_valid_idx(idx)) {
				throw new bad_passet(2, "BAD_INDEX=" + idx);
			}
			passet_deno = new tag_denomination(prev_tra.get_amount(idx));
			passet_idx = idx;
		}
		prev_transfer_id = prev_tra.get_transfer_id();
		prev_transfer_sha = parse.as_sha_string(prev_tra.get_transfer_sha());
	}

	private List<String> dbg_get_input_lines(String title) {
		List<String> txt = new ArrayList<String>();
		add_input_lines_to(txt, "DEBUG_INPUT ", false);
		return txt;
	}

	private void add_input_lines_to(List<String> txt, String title,
			boolean is_direct) {
		if (!passet_deno.is_valid_deno()) {
			throw new bad_passet(0);
		}
		if (is_direct) {
			title = "";
		}

		passet_deno.add_denomination_lines_to(txt, title);
		
		String fst_id_tit = title + first_passet_id_title; 
		passet_id.add_filglid_lines_to(txt, fst_id_tit);
		
		parse.add_next_field_to(txt, title, kbox_pos_field, "" + passet_idx);
		
		String prev_id_tit = title + prev_passet_id_title; 
		prev_transfer_id.add_filglid_lines_to(txt, prev_id_tit);
		
		parse.add_next_field_to(txt, title, previous_sha_field,
				prev_transfer_sha);

		parse.check_line_list(txt);
	}

	private void init_input_with(ListIterator<String> it1, String title,
			boolean is_direct) {
		if (is_direct) {
			title = "";
		}

		passet_deno.init_denomination_with(it1, title);

		String fst_id_tit = title + first_passet_id_title; 
		passet_id.init_filglid(it1, fst_id_tit);
		
		String val_pos = parse.get_next_field_from(it1, title, kbox_pos_field);

		String prev_id_tit = title + prev_passet_id_title; 
		prev_transfer_id.init_filglid(it1, prev_id_tit);
		
		prev_transfer_sha = parse.get_next_field_from(it1, title,
				previous_sha_field);

		passet_idx = convert.parse_int(val_pos);

		if (passet_idx < 0) {
			throw new bad_passet(2);
		}

		if (IN_DEBUG_1) {
			logger.debug("inited_input with pass_id=" + passet_id + " prev_id="
					+ prev_transfer_id);
		}
	}

	void init_deno(tag_denomination i_deno) {
		passet_deno = new tag_denomination(i_deno);
	}

	tag_denomination get_deno_copy() {
		return new tag_denomination(passet_deno);
	}

	static String get_position_title(String tit, int pos) {
		return tit + " " + pos + " ";
	}

	static void add_list_input_lines_to(List<tag_input> all_input,
			List<String> txt, String title, boolean is_direct) {
		boolean one_in = (all_input.size() == 1);
		if (is_direct && !one_in) {
			throw new bad_passet(2);
		}
		String tit = "";
		int pos = 0;
		for (tag_input ii : all_input) {
			tit = get_position_title(title, pos);
			ii.add_input_lines_to(txt, tit, is_direct);
			pos++;
		}
	}

	static void init_list_input_with(List<tag_input> all_input,
			ListIterator<String> it1, String title, boolean is_direct) {
		boolean one_in = (all_input.size() == 1);
		if (is_direct && !one_in) {
			throw new bad_passet(2);
		}
		String tit = "";
		int pos = 0;
		for (tag_input ii : all_input) {
			tit = get_position_title(title, pos);
			ii.init_input_with(it1, tit, is_direct);
			pos++;
		}
	}

	static List<tag_input> get_list_input(int sz) {
		List<tag_input> lst = new ArrayList<tag_input>();
		for (int aa = 0; aa < sz; aa++) {
			tag_input ii = new tag_input();
			lst.add(ii);
		}
		return lst;
	}

	static void init_inputs_with_prev_list(List<tag_input> all_input,
			List<tag_transfer> all_prev, int idx) {
		tag_transfer.ck_prev_sz_and_idx(all_prev, idx);
		all_input.clear();
		for (tag_transfer pp : all_prev) {
			tag_input ii = new tag_input();
			ii.init_tag_input_with(pp, idx);
			all_input.add(ii);
		}
	}

	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof tag_input)) {
			throw new bad_passet(2);
		}
		tag_input in_2 = (tag_input) obj;
		boolean eq1 = equal_input(in_2);
		if (IN_DEBUG_2) {
			if (!eq1) {
				List<String> in1_lines = dbg_get_input_lines("DIFFER INPUT_1");
				List<String> in2_lines = in_2.dbg_get_input_lines("INPUT2");
				logger.info(in1_lines);
				logger.info(in2_lines);
			}
		}
		return eq1;
	}

	public boolean equal_input(tag_input in_2) {
		if ((passet_deno == null) != (in_2.passet_deno == null)) {
			return false;
		}
		if ((passet_id == null) != (in_2.passet_id == null)) {
			return false;
		}
		if ((prev_transfer_id == null) != (in_2.prev_transfer_id == null)) {
			return false;
		}
		if ((prev_transfer_sha == null) != (in_2.prev_transfer_sha == null)) {
			return false;
		}

		if ((passet_deno != null) && !passet_deno.equals(in_2.passet_deno)) {
			return false;
		}
		if ((passet_id != null) && !passet_id.equals(in_2.passet_id)) {
			return false;
		}
		if (passet_idx != in_2.passet_idx) {
			return false;
		}
		if ((prev_transfer_id != null)
				&& !prev_transfer_id.equals(in_2.prev_transfer_id)) {
			return false;
		}
		if ((prev_transfer_sha != null)
				&& !prev_transfer_sha.equals(in_2.prev_transfer_sha)) {
			return false;
		}
		return true;
	}

	static boolean equal_inputs(List<tag_input> all_input_1,
			List<tag_input> all_input_2) {
		return all_input_1.equals(all_input_2);
	}

	public tag_filglid get_passet_id() {
		if (passet_id == null) {
			throw new bad_passet(2);
		}
		if (passet_id.equals(NO_PASSET_ID)) {
			throw new bad_passet(2);
		}
		return passet_id;
	}

	void set_passet_id(tag_filglid pss_id) {
		if (pss_id == null) {
			throw new bad_passet(2);
		}
		if (pss_id.equals(NO_PASSET_ID)) {
			throw new bad_passet(2);
		}
		if ((passet_id != null) && !passet_id.equals(NO_PASSET_ID)
				&& !passet_id.equals(pss_id)) {
			throw new bad_passet(2, "DIFF_IDS= " + passet_id + " != " + pss_id);
		}
		passet_id = new tag_filglid(pss_id);
	}

	static List<tag_filglid> get_prev_transfer_paths(List<tag_input> all_input) {
		List<tag_filglid> all_prv_ids = new ArrayList<tag_filglid>();
		for (tag_input ii : all_input) {
			tag_filglid prv_id = ii.prev_transfer_id;
			all_prv_ids.add(prv_id);
		}
		return all_prv_ids;
	}

	static List<tag_denomination> get_all_deno(List<tag_input> all_input) {
		List<tag_denomination> all_deno = new ArrayList<tag_denomination>();
		for (tag_input ii : all_input) {
			tag_denomination dd = new tag_denomination(ii.passet_deno);
			all_deno.add(dd);
		}
		return all_deno;
	}
}
