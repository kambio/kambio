package emetcode.economics.passet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.crypto.bitshake.utils.transfer_crypto;
import emetcode.economics.passet.locale.L;
import emetcode.util.devel.logger;

public class tag_output {

	static final boolean IN_DEBUG_1 = false; // loaded pvk

	public static final String NO_RECV_CRYPTOBOX = "Invalid cryptobox";

	static final String receiver_cryptobox_field = "Receiver cryptobox";

	private tag_denomination out_deno;
	private transfer_crypto rcv_kbox;

	public tag_output() {
		init_tag_output();
	}

	public tag_output(tag_output orig) {
		init_tag_output();
		if (orig == null) {
			throw new bad_passet(2);
		}
		out_deno = new tag_denomination(orig.out_deno);
	}

	void init_tag_output() {
		out_deno = new tag_denomination(config.DEFAULT_CURRENCY, 1, 1);
		rcv_kbox = null;
	}

	transfer_crypto get_receiver_kbox() {
		if (rcv_kbox == null) {
			throw new bad_passet(2);
		}
		if (!rcv_kbox.has_cryptobox()) {
			throw new bad_passet(2, L.no_encrypted_receptacle_found);
		}
		return rcv_kbox;
	}

	tag_denomination get_deno_copy() {
		return new tag_denomination(out_deno);
	}

	private void add_output_lines_to(List<String> txt, String title,
			key_owner owr, long receiver_tm, boolean is_direct) {

		if (is_direct) {
			title = "";
		}
		if (!is_direct) {
			out_deno.add_denomination_lines_to(txt, title);
		}

		byte[] sgn_sha = parse.calc_sha_lines(txt);

		rcv_kbox = new transfer_crypto(receiver_tm);
		rcv_kbox.start_transfer_crypto(owr, sgn_sha);
		if (!rcv_kbox.has_cryptobox()) {
			throw new bad_passet(2, L.no_encrypted_receptacle_found);
		}

		parse.add_next_field_to(txt, title, receiver_cryptobox_field,
				rcv_kbox.get_cryptobox());

		parse.check_line_list(txt);
	}

	private void init_output_with(ListIterator<String> it1, String title,
			List<String> all_lines, long receiver_tm, boolean is_direct) {
		if (is_direct) {
			title = "";
		}
		if (!is_direct) {
			out_deno.init_denomination_with(it1, title);
		}

		int pre_kbox_idx = it1.nextIndex();
		rcv_kbox = new transfer_crypto(receiver_tm);
		rcv_kbox.tgt_vrf_dat = parse.calc_sha_lines(all_lines, 0, pre_kbox_idx);

		String cbox = parse.get_next_field_from(it1, title,
				receiver_cryptobox_field);
		rcv_kbox.set_cryptobox(cbox);
	}

	void save_rcv_kbox_pvks(File pvks_dd, key_owner owr) {
		if (rcv_kbox == null) {
			throw new bad_passet(2);
		}
		rcv_kbox.save_pvks_in_dir(pvks_dd, owr);
	}

	void check_mine_with(File spent_dd, key_owner owr, File pvks_dd) {
		if (rcv_kbox == null) {
			return;
		}
		rcv_kbox.load_pvks_from_dir(pvks_dd, owr);

		if (!rcv_kbox.has_cryptobox_key()) {
			throw new bad_passet(2);
		}
		if (rcv_kbox.is_puk_spent(spent_dd)) {
			throw new bad_passet(2);
		}
		if (IN_DEBUG_1) {
			logger.debug("loaded_rcv_kbox=" + rcv_kbox);
		}
	}

	static void save_pvks(List<tag_output> all_output, File pvks_dd,
			key_owner owr) {
		for (tag_output oo : all_output) {
			oo.save_rcv_kbox_pvks(pvks_dd, owr);
		}
	}

	static String get_position_title(String tit, int pos) {
		return tit + " " + pos + " ";
	}

	static void add_list_output_lines_to(List<tag_output> all_output,
			List<String> txt, String title, key_owner owr, long receiver_tm,
			boolean is_direct) {
		if (all_output == null) {
			throw new bad_passet(2);
		}
		boolean one_out = (all_output.size() == 1);
		if (is_direct && !one_out) {
			throw new bad_passet(2);
		}
		String tit = "";
		int pos = 0;
		for (tag_output oo : all_output) {
			tit = get_position_title(title, pos);
			oo.add_output_lines_to(txt, tit, owr, receiver_tm, is_direct);
			pos++;
		}
	}

	static void init_list_output_with(List<tag_output> all_output,
			ListIterator<String> it1, String title, List<String> all_lines,
			long receiver_tm, boolean is_direct) {
		boolean one_out = (all_output.size() == 1);
		if (is_direct && !one_out) {
			throw new bad_passet(2);
		}
		String tit = "";
		int pos = 0;
		for (tag_output oo : all_output) {
			tit = get_position_title(title, pos);
			oo.init_output_with(it1, tit, all_lines, receiver_tm, is_direct);
			pos++;
		}
	}

	static List<tag_output> get_list_output(int sz) {
		List<tag_output> lst = new ArrayList<tag_output>();
		for (int aa = 0; aa < sz; aa++) {
			tag_output oo = new tag_output();
			lst.add(oo);
		}
		return lst;
	}

	static void init_list_output_with(List<tag_output> all_output,
			deno_counter cntr) {
		all_output.clear();
		List<deno_count> all_deco = cntr.get_working_counts();
		for (deno_count deco : all_deco) {
			for (int aa = 0; aa < deco.num_have; aa++) {
				tag_output oo = new tag_output();
				oo.out_deno = new tag_denomination(deco.deno);
				all_output.add(oo);
			}
		}
		if (all_output.isEmpty()) {
			throw new bad_passet(2);
		}
	}

	static void init_list_output_with(List<tag_output> all_output,
			tag_denomination out_den) {
		all_output.clear();
		tag_output oo = new tag_output();
		oo.out_deno = new tag_denomination(out_den);
		all_output.add(oo);
	}

	static boolean all_out_have_rcv_box(List<tag_output> all_out,
			boolean ck_kbox_key) {
		for (tag_output oo : all_out) {
			if (oo.rcv_kbox == null) {
				return false;
			}
			if (!oo.rcv_kbox.has_cryptobox()) {
				return false;
			}
			if (ck_kbox_key) {
				if (!oo.rcv_kbox.has_cryptobox_key()) {
					throw new bad_passet(2);
				}
			}
		}
		return true;
	}

	static String all_deno_str(List<tag_output> all_out) {
		StringBuilder ss = new StringBuilder();
		ss.append("[");
		for (tag_output oo : all_out) {
			ss.append(oo.out_deno.get_short_text_denomination(true));
			ss.append("]+[");
		}
		ss.append("]");
		return ss.toString();
	}

	static List<tag_denomination> get_all_deno(List<tag_output> all_output) {
		List<tag_denomination> all_deno = new ArrayList<tag_denomination>();
		for (tag_output ii : all_output) {
			tag_denomination dd = new tag_denomination(ii.out_deno);
			all_deno.add(dd);
		}
		return all_deno;
	}
}
